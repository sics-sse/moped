/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2013 Oracle Corporation. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

#define TRUE 1
#define FALSE 0

#include "system.h"
#include "systemtimer.h"
#include <syscalls-9200-io.h>
#include "syscalls-impl.h"
#include "spi.h"
#include "avr.h"
#include "mmu_fat.h"

// Define the maximum number of user-supplied command line args we can accept
#define SQUAWK_STARTUP_ARGS_MAX 20

// Override setting from platform.h
#undef PLATFORM_UNALIGNED_LOADS
#define PLATFORM_UNALIGNED_LOADS false

// don't support the NVM space, or -Xmxnvm option
#define DEFAULT_NVM_SIZE   0

#define SERVICE_CHUNK_SIZE (8*1024)
#define IODOTC "9200-io.c"

#include <stdlib.h>
#include <sys/time.h>
#include "jni.h"

#define C_HEAP_MEMORY_RESERVE (16 * 1024)

#define printf iprintf
#define fprintf fiprintf
#define sprintf siprintf

int main(int argc, char *argv[]);
extern int disableARMInterrupts();
extern void enableARMInterrupts();
void initTrapHandlers();

int dma_buffer_size;
char* dma_buffer_address;

volatile jlong sysTimeMillis(void) {
    jlong res = getMilliseconds();
    return res;
}

jlong sysTimeMicros() {
    return sysTimeMillis() * 1000;
}

extern void setup_java_interrupts();
extern void usb_state_change();


/* low-level setup task required after cold and warm restarts */
void lowLevelSetup() {
	diagnostic("in low level setup");
	mmu_enable();
	data_cache_enable();
	spi_init();
	register_usb_state_callback(usb_state_change);
	setup_java_interrupts();
	diagnostic("java interrupts setup");

#if 0 /*AT91SAM9G20*/
    error("NYI - AVR CLOCK DISABLED FOR AT91SAM9G20", -1);
#else
    synchroniseWithAVRClock();
    diagnostic("before init_system_timer");
    init_system_timer();
    diagnosticWithValue("Current time is ", getMilliseconds());
	diagnostic("system timer inited");
#endif
}

static int get_available_memory() {
	char* current;
	int size = get_ram_size();
	// malloc some heap to reserve it for interrupt event blocks etc.
	char* reserved = malloc(C_HEAP_MEMORY_RESERVE);
	// get info
	do {
		size -= 1024;
		current = malloc(size);
	} while (current == 0);
	free(current);
	free(reserved);
	// check we *really* can allocate this space
	reserved = malloc(size);
	if (reserved == 0) {
		error("Failed to reallocate memory", size);
		exit(-1);
	}
	free(reserved);
	return size;
}

/**
 * Program entrypoint (cold start).
 */
void arm_main(int cmdLineParamsAddr, unsigned int outstandingAvrStatus) {
	int i;

	diagnostic("in vm");
#if 1
	diagnosticWithValue("ram size", get_ram_size());
	diagnosticWithValue("mmu ram", get_mmu_ram_space_address());
	diagnosticWithValue("mmu flash", get_mmu_flash_space_address());
	diagnosticWithValue("stack top", get_stack_top_address());
	diagnosticWithValue("stack bottom", get_stack_bottom_address());
	diagnosticWithValue("usart", get_usart_rx_buffer_address());
	diagnosticWithValue("heap end", get_heap_end_address());
#endif

	page_table_init();
	if (!reprogram_mmu(TRUE)) {
		error("VM not launching because the FAT appears to be invalid", 0);
		asm(SWI_ATTENTION_CALL);
	}
	
	diagnostic("before lowLevelSetup\n");
	lowLevelSetup();
	diagnostic("low level setup complete\n");

	// Record status bits from bootloader that may require processing by Java.
#if AT91SAM9G20
#if REV8
	avrSetOutstandingEvents(outstandingAvrStatus & ((1<<POWER_CHANGE_EVENT) | (1<<STATUS_SENSOR_EVENT) | (1<<STATUS_BUTTON_EVENT) | (1<<STATUS_WATCHDOG_EVENT)));
#else
	avrSetOutstandingEvents(outstandingAvrStatus & ((1<<FAULT_EVENT) | (1<<STATUS_SENSOR_EVENT) | (1<<STATUS_BUTTON_EVENT) | (1<<WATCHDOG_EVENT) | (1<<SUPERCAP_CHARGED) | (1<<USB_EVENT)));
#endif
#else
	avrSetOutstandingEvents(outstandingAvrStatus & ((1<<BATTERY_POWER_EVENT) | (1<<STATUS_LOW_BATTERY_EVENT) | (1<<STATUS_EXTERNAL_POWER_EVENT)));
#endif

/*
    iprintf("\n");
    iprintf("Squawk VM Starting (");
	iprintf(BUILD_DATE);
	iprintf(")...\n");
*/
	
	char* startupArgs = (char*)cmdLineParamsAddr;
	char *fakeArgv[SQUAWK_STARTUP_ARGS_MAX];
	char xmx_buffer[20];

	fakeArgv[0] = "dummy"; // fake out the executable name
	int fakeArgc = 1;
	int index = 0;
	int xmx_seen = FALSE;
	/* The startupArgs structure comprises a sequence of null-terminated string
	 * with another null to indicate the end of the structure
	 */
	while (startupArgs[index] != 0) {
		if (startsWith(&startupArgs[index], "-Xmx:")) {
//          iprintf("REAL Xmx arg: %s\n", &startupArgs[index]);
			xmx_seen = TRUE;
			break;
		} else if (startsWith(&startupArgs[index], "-dma:")) {
			dma_buffer_size = parseQuantity(&startupArgs[index+5], "-dma:");
			dma_buffer_address = malloc(dma_buffer_size);
			if (dma_buffer_address == NULL) {
				iprintf("Unable to allocate %i bytes for DMA\n", dma_buffer_size);
            	exit(-1);
			}
			dma_buffer_address = (char*)(((int)dma_buffer_address & 0x000FFFFF) + UNCACHED_RAM_START_ADDDRESS);
//			iprintf("Allocated %i bytes for DMA buffers at address 0x%x\n", dma_buffer_size, (int)dma_buffer_address);
		}
		
		while (startupArgs[index] != 0) {
			index++;
		}
		// skip over the terminating null
		index++;
	}
	if (!xmx_seen) {
		siprintf(xmx_buffer, "-Xmx:%i", get_available_memory());
		fakeArgv[fakeArgc] = xmx_buffer;
//		iprintf("Faking Xmx arg: %s\n", fakeArgv[fakeArgc]);
		fakeArgc++;
	}

	index = 0;
	while (startupArgs[index] != 0) {
		if (!startsWith(&startupArgs[index], "-dma:")) {
			if (fakeArgc >= SQUAWK_STARTUP_ARGS_MAX) {
				iprintf("Number of startup args exceeds maximum permitted\n");
				exit(-1);
			}
			fakeArgv[fakeArgc] = &startupArgs[index];
//			iprintf("Parsed arg: %s\n", fakeArgv[fakeArgc]);
			fakeArgc++;
		}
		while (startupArgs[index] != 0) {
			index++;
		}
		// skip over the terminating null
		index++;
	}
	
    main(fakeArgc, fakeArgv);
    error("main function returned, restarting", 0);
    disableARMInterrupts();
    hardwareReset();
}

/**
 * Support for util.h
 */

/**
 * Gets the page size (in bytes) of the system.
 *
 * @return the page size (in bytes) of the system
 *
 * NOTE: Really 8KB or 64KB on SPOT, not 4 bytes, right?
 */
#define sysGetPageSize() 4

/**
 * Sets a region of memory read-only or reverts it to read & write.
 *
 * @param start    the start of the memory region
 * @param end      one byte past the end of the region
 * @param readonly specifies if read-only protection is to be enabled or disabled
 */
INLINE sysToggleMemoryProtection(char* start, char* end, boolean readonly) {}

/**
 * Allocate a page-aligned chunk of memory of the given size.
 * 
 * @param size size in bytes to allocate
 * @return pointer to allocated memory or null.
 */
INLINE void* sysValloc(size_t size) {
    return valloc(size);
}

/**
 * Free chunk of memory allocated by sysValloc
 * 
 * @param ptr to to chunk allocated by sysValloc
 */
INLINE void sysVallocFree(void* ptr) {
    free(ptr);
}

/** 
 * Return another path to find the bootstrap suite with the given name.
 * On some platforms the suite might be stored in an odd location
 * 
 * @param bootstrapSuiteName the name of the boostrap suite
 * @return full or partial path to alternate location, or null
 */
INLINE char* sysGetAlternateBootstrapSuiteLocation(char* bootstrapSuiteName) { return NULL; }

/* The package that conmtains the native code to use for a "NATIVE" platform type*/
 #define sysPlatformName() "Spot"

INLINE void osloop() {
	//no-op on spot platform
}

INLINE void osbackbranch() {
}

void osfinish() {
	disable_system_timer();
	diagnostic("OSFINISH");
    asm(SWI_ATTENTION_CALL);
}

