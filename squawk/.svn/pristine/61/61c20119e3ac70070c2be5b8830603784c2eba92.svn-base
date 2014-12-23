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

#define TRUE 1
#define FALSE 0

#include <stdlib.h>
#include <sys/time.h>
#include <dlfcn.h>
#include <jni.h>

//#include "simple_stdio.c"
//#include "squawk_memory.h"
//#include "squawk_memory.c"

#define IODOTC "rpi-io.c"
//#include "stdio.h"

//#define jlong  int64_t
typedef long long int64_t;
typedef unsigned long long u_int64_t;

//#ifdef AUTOSAR
//#define sysPlatformName() "autosar"
//#else
//#define sysPlatformName() "linux"
//#endif
#define sysPlatformName() "rpi"

//#define DEFAULT_RAM_SIZE	(128*1024)
/*
 * Note that DEFAULT_NVM_SIZE = 0 means don't support nvm at all
 */
//#define DEFAULT_NVM_SIZE 	(10*1024)

//#include "os_posix.c"
/** 
 * Return another path to find the bootstrap suite with the given name.
 * On some platforms the suite might be stored in an odd location
 * 
 * @param bootstrapSuiteName the name of the boostrap suite
 * @return full or partial path to alternate location, or null
 */
INLINE char* sysGetAlternateBootstrapSuiteLocation(char* bootstrapSuiteName) {
	return NULL;
}

#ifndef AUTOSAR
INLINE long long timeVal2ms(struct timeval* tv) {
    return (long long)tv->tv_sec * 1000 + ((tv->tv_usec + 500) / 1000);
};
#endif

long long getMilliseconds(void) {
#ifdef AUTOSAR
	jlong res = GetOsTick(); //OS_TICKS2MS_OS_TICK(GetOsTick() - tickTimerStart);
	return res;
#else
	return 0;
#endif
}

jlong sysTimeMillis(void) {
#ifdef AUTOSAR
	jlong res = (jlong) getMilliseconds();
	return res;
#else
	struct timeval tv;
	gettimeofday(&tv, NULL);
	return timeVal2ms(&tv);
#endif

}

jlong sysTimeMicros() {
	return sysTimeMillis() * 1000;
}

//#define sysGetPageSize() 4

INLINE sysToggleMemoryProtection(char* start, char* end, boolean readonly) {
}

#define osloop()        /**/
#define osbackbranch()  /**/
#define osfinish()      /**/

int StartJVM(int argc, char *argv[]) {
	Squawk_main_wrapper(argc, argv);
}

/* Stubs for some squawk.c-references */
INLINE int sysGetPageSize(void) {
	return _SC_PAGESIZE;
}

//static char dynMemory[10*1048576];
unsigned char dynMemory[94371840] = "";
void* sysValloc(int size) {
//		return memget(size); //Made by Shuzhou

	static int nextFreeMemIndex;
	void *nextFreeMem = (void *) &dynMemory[nextFreeMemIndex];

	nextFreeMemIndex += size;
	if (nextFreeMemIndex > sizeof(dynMemory)) {
		nextFreeMemIndex -= size;
		return 0;
	}

	return nextFreeMem;
}
void sysVallocFree(void* ptr) {
//    free(ptr);
}

void systemGetEventHandler(void) {
}
int crc(int address, int numberOfBytes) {
	return 0;
}
void setMilliseconds(long long val) {
}
void sysWriteSeveral(char *buf, int len, int deviceType) {
}
int sysAvailable(int device_type) {
	return 0;
}
int sysReadSeveral(char *buf, int len, int deviceType) {
	return 0;
}
void freeSerialPort(int deviceType) {
}
void doShallowSleep(long long targetMillis) {
}
void doDeepSleep(long long targetMillis, int remain_powered) {
}
int get_hardware_revision(void) {
	return 1;
}
void disableARMInterrupts(void) {
}
void enableARMInterrupts(void) {
}

int getSerialPortEvent(int deviceType) {
	return 0;
}
//clock_counter(void) {}
int dma_buffer_size = 32;
char* dma_buffer_address = ((char*) 0);
#define sysGetPageSize() 4
void* get_flash_base(void) {
}
int get_flash_size(void) {
	return 4096;
}

int isSerialPortInUse(int device_type) {
	return 0;
}

typedef struct dl_info {
	char *dli_sname; /* Name of nearest symbol */
	void *dli_saddr; /* Address of nearest symbol */
} Dl_info;

#ifdef AUTOSAR
int dladdr(void* addr, Dl_info* info) {
	/*
	 SYM_TYPE ptype;

	 STATUS status = symByValueFind(sysSymTbl, (UINT)addr, &info->dli_sname, (int*)&info->dli_saddr, &ptype);

	 return status == OK ? 1 : 0;
	 */
	return 0;
}

int sigaction (int __sig, __const struct sigaction *__restrict __act,
		struct sigaction *__restrict __oact) {
	return 0;
}

/* A stub for _sbrk (copied from the newlib-documentation
 * (don't know if it works) */

caddr_t _sbrk(int incr) {
	extern char _end; /* Defined by the linker */
	static char *heap_end;
	char *prev_heap_end;

	if (heap_end == 0) {
		heap_end = &_end;
	}
	prev_heap_end = heap_end;

	heap_end += incr;
	return (caddr_t) prev_heap_end;
}
#endif /* AUTOSAR */




// Common APIs

typedef void* Address;
void jnaPrint(Address fn) {
#ifdef AUTOSAR
	output(fn);
#else
	printf("%s", (char *) fn);
#endif
}

#ifdef AUTOSAR
extern void autosarSendAckByte(char data);
#endif

void jnaSendAckByte(char data) {
#ifdef AUTOSAR
	autosarSendAckByte(data);
#endif
}

#ifdef AUTOSAR
void autosarSendPackageData(int size, char* data);
#endif
void jnaSendPackageData(int size, Address data) {
#ifdef AUTOSAR
	autosarSendPackageData(size, data);
#endif
}

#ifdef AUTOSAR
extern char* autosarFetchNewData(int startIndex, int rearIndex);
#endif
Address jnaFetchNewData(int startIndex, int rearIndex) {
#ifdef AUTOSAR
	return autosarFetchNewData(startIndex, rearIndex);
#endif
}

#ifdef AUTOSAR
extern int autosarCheckIfNewPackage();
#endif
int jnaCheckIfNewPackage() {
#ifdef AUTOSAR
	return autosarCheckIfNewPackage();
#endif
}

#ifdef AUTOSAR
extern int autosarGetLengthPackage();
#endif
int jnaGetLengthPackage() {
#ifdef AUTOSAR
	return autosarGetLengthPackage();
#endif
}

#ifdef AUTOSAR
extern int autosarGetReadStartIndex();
#endif
int jnaGetReadStartIndex() {
#ifdef AUTOSAR
	return autosarGetReadStartIndex();
#endif
}

#ifdef AUTOSAR
extern int autosarGetReadRearIndex();
#endif
int jnaGetReadRearIndex() {
#ifdef AUTOSAR
	return autosarGetReadRearIndex();
#endif
}


#ifdef AUTOSAR
extern char autosarFetchByte(int rearIndex);
#endif
char jnaFetchByte(int rearIndex) {
#ifdef AUTOSAR
	return autosarFetchByte(rearIndex);
#endif
}

////////////////////////////////////////

// VCU

#ifdef AUTOSAR
extern void autosarSendSpeedPwmData(int speed);
#endif
void jnaSendSpeedPwmData(int speed) {
#ifdef AUTOSAR
	autosarSendSpeedPwmData(speed);
#endif
}

#ifdef AUTOSAR
extern void autosarSendSteerPwmData(int servo);
#endif
void jnaSendSteerPwmData(int servo) {
#ifdef AUTOSAR
	autosarSendSteerPwmData(servo);
#endif
}

#ifdef AUTOSAR
extern int autosarFetchFrontWheelSpeed();
#endif
int jnaFetchFrontWheelSpeed() {
#ifdef AUTOSAR
	return autosarFetchFrontWheelSpeed();
#endif
}

#ifdef AUTOSAR
extern int autosarFetchBackWheelSpeed();
#endif
int jnaFetchBackWheelSpeed() {
#ifdef AUTOSAR
	return autosarFetchBackWheelSpeed();
#endif
}

#ifdef AUTOSAR
extern long long autosarReadPosition(void);
#endif
jlong jnaReadPosition() {
#ifdef AUTOSAR
	return autosarReadPosition();
#endif
}

#ifdef AUTOSAR
extern long autosarFetchAdcData();
#endif
long jnaFetchAdcData() {
#ifdef AUTOSAR
	return autosarFetchAdcData();
#endif
}

#ifdef AUTOSAR
extern int autosarReadPluginDataSizeFromSCU();
#endif
int jnaReadPluginDataSizeFromSCU() {
#ifdef AUTOSAR
	return autosarReadPluginDataSizeFromSCU();
#endif
}

#ifdef AUTOSAR
extern char autosarReadPluginDataByteFromSCU(int index);
#endif
char jnaReadPluginDataByteFromSCU(int index) {
#ifdef AUTOSAR
	return autosarReadPluginDataByteFromSCU(index);
#endif
}

#ifdef AUTOSAR
extern void autosarResetPluginDataSizeFromSCU(void);
#endif
void jnaResetPluginDataSizeFromSCU() {
#ifdef AUTOSAR
	autosarResetPluginDataSizeFromSCU();
#endif
}

#ifdef AUTOSAR
extern void autosarSetLED(int pin, int val);
#endif
void jnaSetLED(int pin, int val) {
#ifdef AUTOSAR
	autosarSetLED(pin, val);
#endif
}

#ifdef AUTOSAR
extern int autosarFetchSpeedFromPirte();
#endif
int jnaFetchSpeedFromPirte() {
#ifdef AUTOSAR
	return autosarFetchSpeedFromPirte();
#endif
}

#ifdef AUTOSAR
extern int autosarFetchSteerFromPirte();
#endif
int jnaFetchSteerFromPirte() {
#ifdef AUTOSAR
	return autosarFetchSteerFromPirte();
#endif
}

#ifdef AUTOSAR
extern void autosarSetSpeedWithSelector(int speed, int selector);
#endif
void jnaSetSpeedWithSelector(int speed, int selector) {
#ifdef AUTOSAR
	autosarSetSpeedWithSelector(speed, selector);
#endif
}

#ifdef AUTOSAR
extern void autosarSetSteerWithSelector(int steer, int selector);
#endif
void jnaSetSteerWithSelector(int steer, int selector) {
#ifdef AUTOSAR
	autosarSetSteerWithSelector(steer, selector);
#endif
}

#ifdef AUTOSAR
extern int autosarReadPluginDataSizeFromTCU();
#endif
int jnaReadPluginDataSizeFromTCU() {
#ifdef AUTOSAR
	return autosarReadPluginDataSizeFromTCU();
#endif
}

#ifdef AUTOSAR
extern char autosarReadPluginDataByteFromTCU(int index);
#endif
char jnaReadPluginDataByteFromTCU(int index) {
#ifdef AUTOSAR
	return autosarReadPluginDataByteFromTCU(index);
#endif
}

#ifdef AUTOSAR
extern void autosarResetPluginDataSizeFromTCU(void);
#endif
void jnaResetPluginDataSizeFromTCU() {
#ifdef AUTOSAR
	autosarResetPluginDataSizeFromTCU();
#endif
}

#ifdef AUTOSAR
void autosarSetSelect(int selector);
#endif
void jnaSetSelect(int selector) {
#ifdef AUTOSAR
	autosarSetSelect(selector);
#endif
}

////////////////////////////////////////

// SCU
#ifdef AUTOSAR
extern int autosarReadUltrasonicData();
#endif
int jnaReadUltrasonicData() {
#ifdef AUTOSAR
	return autosarReadUltrasonicData();
#endif
}

#ifdef AUTOSAR
extern void autosarWritePluginData2VCU(int size, char* data);
#endif
void jnaWritePluginData2VCU(int size, Address data) {
#ifdef AUTOSAR
	return autosarWritePluginData2VCU(size, data);
#endif
}

#ifdef AUTOSAR
extern long long autosarReadIMUPart1(void);
#endif
jlong jnaReadIMUPart1() {
#ifdef AUTOSAR
	return autosarReadIMUPart1();
#endif
}

#ifdef AUTOSAR
extern long long autosarReadIMUPart2(void);
#endif
jlong jnaReadIMUPart2() {
#ifdef AUTOSAR
	return autosarReadIMUPart2();
#endif
}
