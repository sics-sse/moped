/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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

/**
 * Information related to each interrupt that may be handled
 * including pre-allocated messageStruct and some useful statistics.
 */
struct {
    Message msg;
    void (*handler)(int signum);
    boolean inUse;
    int      caughtCount;
    int      ignoredCount;
    jlong    timestamp;
    /* ... */
} deviceMessageBuffers[MAXSIG];

/**
 * This routine should think about nesting depth of interrupt handlers.
 * The same protocol (with kernelSignalCounter) is used in the handlers
 * for device interrupts.
 *
 * When hosted by HotSpot, for example, we don't necessarily control the
 * signal masks on entry to handlers and so need to defer interrupts. For
 * other cases (such as an ARM processor), we should be able to prevent
 * nesting of signals and so signalHandler_deferInterruptsAndDo() may be
 * a NOP wrapper.
 */
void kernelSignalHandler(int signum) {

    signalHandler_deferInterruptsAndDo(
        // sysPrint("entering kernelSignalHandler, signum \r\n");
		/**
         * Note: the incrementing/decrementing of kernelSignalCounter assumes
         * that signals cannot be nested. If they can, these operations should
         * be performed with atomic operations such as CAS or LL/SC. */
        kernelSignalCounter ++;
        if (kernelSignalCounter == 1) {
            void Squawk_kernelContinue(void);
            do {
                reenableInterrupts();
                Squawk_kernelContinue();
                deferInterrupts();
            } while (checkForNonemptyQueues());

            if (messageEvents != null) {
                cioPostEvent();
            }
        }
        kernelSignalCounter --;
    );
}

/**
 * This routine sets the signals for transitioning into kernel mode.
 * It is a bit of a hack and may belong better in platform-specific code.
 */
void os_setSignalHandlers(void) {
    int i;

    for (i = 0; i < MAXSIG; i ++) {
        createInterruptURL(deviceMessageBuffers[i].msg.name, i);
        deviceMessageBuffers[i].handler = null;
        deviceMessageBuffers[i].inUse = false;
        deviceMessageBuffers[i].caughtCount = 0;
        deviceMessageBuffers[i].ignoredCount = 0;
        deviceMessageBuffers[i].timestamp = 0;
    }
}

/**
 * This routine sends the current thread a signal to enter the kernel.
 */
void os_enterKernel(void) {
	kernelSignalHandler(0);
}


/**
 * Each device interrupt has this routine invoked as its handler.
 *
 * @param signum   identifier for the interrupt being handled
 */
void deviceSignalHandler(int signum) {
	signalHandler_deferInterruptsAndDo(
	    //iprintf("entering deviceSignalHandler, signum %d\n", signum);
		void postMessage(Address key, Message *msg);
        if (signum >= 0 && signum < MAXSIG) {
            if (deviceMessageBuffers[signum].inUse == false) {
                deviceMessageBuffers[signum].inUse = true;
                deviceMessageBuffers[signum].caughtCount ++;
                deviceMessageBuffers[signum].timestamp = sysTimeMicros();

                if (deviceMessageBuffers[signum].handler != null)
                    deviceMessageBuffers[signum].handler(signum);
				//iprintf("about to post message\n");
                postMessage(deviceMessageBuffers[signum].msg.name,
                            &deviceMessageBuffers[signum].msg);
                //iprintf("message posted\n");
            } else {

                deviceMessageBuffers[signum].ignoredCount ++;
            }
        }

        /*
            fprintf(stderr, "XXX bing %d (inUse %d caught %d ignored %d) !\n", signum,
                deviceMessageBuffers[signum].inUse,
                deviceMessageBuffers[signum].caughtCount,
                deviceMessageBuffers[signum].ignoredCount);

        /* While we're here, check the kernel event loop. */
        kernelSignalCounter ++;
        if (kernelSignalCounter == 1) {
            void Squawk_kernelContinue(void);
            do {
                reenableInterrupts();
                //iprintf("Entering kernel\n");
            	Squawk_kernelContinue();
            	//iprintf("Exiting kernel\n");
            	deferInterrupts();
	        } while (checkForNonemptyQueues());
        	if (messageEvents != null) {
                cioPostEvent();
	        }
        }
        kernelSignalCounter --;
    );
    //iprintf("exiting device signal handler\n");
}

/**
 * Here, we set up the handler for each device interrupt. The handler function
 * passed in is stored away and invoked as appropriately in the generic
 * device handler (see deviceSignalHandler()).
 *
 * @param signum   identifier for the interrupt being handled
 * @param handler  function to be invoked when this interrupt is handled
 */
boolean os_setDeviceSignalHandler(int signum, void (*handler)(int signum)) {
    if (signum >= 0 && signum < MAXSIG) {
        if (signum != kernelSignal) {
            deviceMessageBuffers[signum].handler = handler;
            return true;
        }
    }
    return false;
}

/**
 * Routine to reset the state of a device interrupt's
 * preallocated messageStruct and stats.
 *
 * @param signum   identifier for the interrupt being handled
 */
void os_interruptDone(int signum) {
    assumeInterruptsAreDisabled();
    if (signum >= 0 && signum < MAXSIG) {
        assume(deviceMessageBuffers[signum].inUse == true);
        deviceMessageBuffers[signum].inUse = false;
    }
}


/**
 * Routine to allow threads to send themselves particular signals
 * for device-specific actions.  Interrupts are a simple mechanism
 * to transition into C code from Squawk.
 *
 * @param signum   identifier for the interrupt being handled
 */
void os_sendInterrupt(int signum) {
    if (signum >= 0 && signum < MAXSIG) {
        fatalVMError("Sending interrupts from Java not implemented on the ARM platform");
    }
}


/**
 * Routine to set up handlers for device interrupts from Java. Because
 * the Java code won't have access to global function pointers, we pass
 * the name of the handling routine as a byte-array and use dlsym()
 * to look it up.
 *
 * @param signum   identifier for the interrupt being handled
 * @param handler  address of a byte-array naming the function to be ivoked as a handler
 */
#define MAX_HANDLER_NAME (128)
void os_setupInterrupt(int signum, Address handler) {
    if (handler != null) {
        fatalVMError("Can't attach C irq handler routines on the ARM platform");
    }
    os_setDeviceSignalHandler(signum, null);
}

/**
 * Routines for use by JavaDriverManager.
 */

/**
 * Set the platform-specific properties for the timer interrupts.
 *
 * @param start   time in microseconds until first timer interrupt
 * @param period  time in microseconds until each subsequent timer interrupt
 */
void os_setupAlarmInterval(int start, int period) {
    //ualarm(start, period);
}

/**
 * Return one of several stats for a device interrupt.
 *
 * @param signum   interrupt of interrest
 * @param id       an integer identifier id'ing the stat in question
 * @param the value of the stat corresponding to 'id'
 */
jlong os_getInterruptStatus(int signum, int id) {
    if (signum >= 0 && signum < MAXSIG) {
        if (id == com_sun_squawk_JavaDriverManager_STATUS_CAUGHT) {
            return deviceMessageBuffers[signum].caughtCount;
        } else if (id == com_sun_squawk_JavaDriverManager_STATUS_IGNORED) {
            return deviceMessageBuffers[signum].ignoredCount;
        } else if (id == com_sun_squawk_JavaDriverManager_STATUS_TIMESTAMP) {
            return deviceMessageBuffers[signum].timestamp;
        }
    }
    return 0;
}

inline void os_enableInterrupts() {
	//sysPrint("Enabling\r\n");
	enableARMInterrupts();
}

inline void os_disableInterrupts() {
	disableARMInterrupts();
}

