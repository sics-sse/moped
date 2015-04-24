/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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

// Define the maximum number of user-supplied command line args we can accept
#define SQUAWK_STARTUP_ARGS_MAX 20

// Override setting from platform.h
#undef PLATFORM_UNALIGNED_LOADS
#define PLATFORM_UNALIGNED_LOADS false

#define SERVICE_CHUNK_SIZE (8*1024)
#define IODOTC "eb40a-io.c"

#define TRUE 1
#define FALSE 0

#include <stdlib.h>
#include <sys/time.h>
typedef long long int64_t;
typedef unsigned long long u_int64_t;
#define jlong  int64_t
#include "jni.h"

#define printf iprintf
#define fprintf fiprintf
#define sprintf siprintf

#include "periph/pio/lib_pio.h"
/* PIO Controller Descriptor */
extern const PioCtrlDesc PIO_DESC;

int main(int argc, char *argv[]);
extern void disableARMInterrupts();
extern void enableARMInterrupts();
void initTrapHandlers();

jlong sysTimeMillis(void) {
    jlong res = getMilliseconds();
    return res;
}

jlong sysTimeMicros() {
    return sysTimeMillis() * 1000;
}

void wait() {
	unsigned int i, j;
	for (i = 1; i<400000; i++) {
		j = j + i;
	}
}

extern void set_up_java_interrupts();

int flash_addr_of_bootstrap;

/**
 * Program entrypoint.
 *
 * appAddr - addr of app to run
 * param - passed from bootloader, or 0 if running under gdb
 * 
 */
int arm_main(int bootAddr, int appAddr, int param, int cmdLineParamsAddr) {
    wait();
    
#ifdef DB_DEBUG
	extern int db_debug_enabled;
	db_debug_enabled = param;
	// record the app address for the debugger's benefit
	extern int db_app_addr;
	db_app_addr = appAddr;
#endif

	// remember bootstrap address for suite.c
	flash_addr_of_bootstrap = bootAddr;
	
	// initialize trap handlers
	initTrapHandlers();
	
	// set up vectors for java-controlled interrupts
	set_up_java_interrupts();

    // start the system timer
    init_watchdog_timer();

    // now everything is set up enable the interrupts
    enableARMInterrupts();
	
    iprintf("\n");
    iprintf("Squawk VM Starting (");
	iprintf(BUILD_DATE);
	iprintf(")...\n");
	
	char* startupArgs = (char*)cmdLineParamsAddr;
	char *fakeArgv[SQUAWK_STARTUP_ARGS_MAX + 2];
	fakeArgv[0] = "dummy"; // fake out the executable name

	char* suiteArg = "-flashsuite:00000000";
	int res = sprintf(&suiteArg[12], "%x", appAddr);
    if (res == -1) {
		iprintf("ERROR - Debug call to sprintf failed\n");
		exit(-1);
    }    	
	fakeArgv[1] = suiteArg;
	
	int fakeArgc = 2;
	int index = 0;
	/* The startupArgs structure comprises a sequence of null-terminated string
	 * with another null to indicate the end of the structure
	 */
	while (startupArgs[index] != 0) {
		fakeArgv[fakeArgc] = &startupArgs[index];
		//iprintf("Parsed arg: %s\n", fakeArgv[fakeArgc]);
		fakeArgc++;
		if (fakeArgc > SQUAWK_STARTUP_ARGS_MAX + 2) {
			iprintf("Number of startup args exceeds maximum permitted\n");
			exit(-1);
		}
		while (startupArgs[index] != 0) {
			index++;
		}
		// skip over the terminating null
		index++;
	}

/*
	char* suiteArg = "-flashsuite:00000000";
	int res = sprintf(&suiteArg[12], "%x", appAddr);
    if (res == -1) {
		iprintf("ERROR - Debug call to sprintf failed\n");
		exit(-1);
    }    	

	// Set available memory
	//int freeMem = (&__java_memory_end - &__java_memory_start);
	//iprintf("Free memory calculated to be %i\n", freeMem);
	char* mxArg = "-Xmx:00000000";
	res = sprintf(&mxArg[5], "%08i", 120000);
    if (res == -1) {
		iprintf("ERROR - call to sprintf failed\n");
		exit(-1);
    }    	
	//iprintf("MX arg is: %s\n", mxArg);
    
    // Hardcode command line arguments
#if KERNEL_SQUAWK
	char* kernelSuiteArg = "-K-flashsuite:00000000";
	res = sprintf(&kernelSuiteArg[14], "%x", appAddr);
    if (res == -1) {
		iprintf("ERROR - Debug call to sprintf failed\n");
		exit(-1);
    }    	

    int fakeArgc = 10;
    char *fakeArgv[] = {"dummy", "-verbose", "-Xkernel", "-K-Xmx:40000", "-Xmx:50000",
    	"-K-Xmxnvm:128", "-Xmxnvm:128", suiteArg, kernelSuiteArg, "squawk.application.Startup"};
#else
    int fakeArgc = 6;
    char *fakeArgv[] = {"dummy", "-verbose", mxArg, "-Xmxnvm:128", suiteArg,
    	"squawk.application.Startup"};
	// -Xmxnvm:125 set on 9 May as, empirically, the lowest value that the lisp2 collector will run with. Strange...
	// -Xmx:145000 is all the available memory (approximately).
	// TODO calculate a value for -Xmx
#endif
*/
    main(fakeArgc, fakeArgv);
    sysPrint("\r\nmain function returned, restarting\r\n");
    disableARMInterrupts();
    restartSystem();
}

/**
 * Flip the watchdog LED every 5,00 back branches
 */
int bytecodeExecutionLedMask = 0;
int count = 0;
int led_is_on = 0;
void updateLEDStatus() {
	if (bytecodeExecutionLedMask != 0) {
	    count = count + 1;
	    if (count % 500 == 0) {
	        //iprintf("Count: %i\n", count);
	        if (led_is_on) {
				at91_pio_write (&PIO_DESC, bytecodeExecutionLedMask, PIO_SET_OUT );
	            led_is_on = 0;
	        } else {
				at91_pio_write (&PIO_DESC, bytecodeExecutionLedMask, PIO_CLEAR_OUT );
	            led_is_on = 1;
	        }
	    }
	}
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
    updateLEDStatus();
}

void osfinish() {
    disableARMInterrupts();
    restartSystem();
}

/***************************
 * Trap handler set up
 * 
 * The bootloader sets up the trap vectors so that they indirect
 * through the six words following the 8 vectors themselves.
 * The order of the six words is:
 *              Restart (not touched here, so that it still runs the bootloader)
 *              Undef
 *              SWI
 *              PrefetchAbort
 *              DataAbort
 */

extern UndefHandler;
extern SWIHandler;
extern PrefetchAbortHandler;
extern DataAbortHandler;

void initTrapHandlers() {
	int* vectorPointer = (int*)(9 * 4); // skip 8 vectors + reset indirection
	*vectorPointer = (int)&UndefHandler;
	vectorPointer++;
	*vectorPointer = (int)&SWIHandler;
	vectorPointer++;
	*vectorPointer = (int)&PrefetchAbortHandler;
	vectorPointer++;
	*vectorPointer = (int)&DataAbortHandler;
}

/***************************
 * Trap handler
 * 
 * addr = addr of instruction that caused the trap
 * code = code for the trap
 * 		1 Undefined instruction
 * 		2 Software trap
 * 		3 Prefetch abort
 * 		4 Data abort
 */
void armTrapHandler (int addr, int code) {
	sysPrint("***************************\r\n");
	sysPrint("TRAP ");
	sysOutputHexInt(code);
	sysPrint(" AT 0x");
	sysOutputHexInt(addr);
	sysPrint("\r\n");	
	sysPrint("***************************\r\n");
	restartSystem();
}
