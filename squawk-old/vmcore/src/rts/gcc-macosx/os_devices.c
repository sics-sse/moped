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
    struct sigaction sigact;
    int i;

    for (i = 0; i < MAXSIG; i ++) {
        createInterruptURL(deviceMessageBuffers[i].msg.name, i);
        deviceMessageBuffers[i].handler = null;
        deviceMessageBuffers[i].inUse = false;
        deviceMessageBuffers[i].caughtCount = 0;
        deviceMessageBuffers[i].ignoredCount = 0;
        deviceMessageBuffers[i].timestamp = 0;
    }

    sigact.sa_handler = kernelSignalHandler;
    sigemptyset(&sigact.sa_mask);
    sigact.sa_flags = 0;
    /* Currently ignoring errors and old value */
    sigaction(kernelSignal, &sigact, NULL);
}

/**
 * This routine sends the current thread a signal to enter the kernel.
 */
void os_enterKernel(void) {
    pthread_kill(pthread_self(), kernelSignal);
}


/**
 * Each device interrupt has this routine invoked as its handler.
 *
 * @param signum   identifier for the interrupt being handled
 */
void deviceSignalHandler(int signum) {
    signalHandler_deferInterruptsAndDo(
        void postMessage(Address key, Message *msg);
        if (signum >= 0 && signum < MAXSIG) {
            if (deviceMessageBuffers[signum].inUse == false) {
                deviceMessageBuffers[signum].inUse = true;
                deviceMessageBuffers[signum].caughtCount ++;
                deviceMessageBuffers[signum].timestamp = sysTimeMicros();

                if (deviceMessageBuffers[signum].handler != null) {
                    deviceMessageBuffers[signum].handler(signum);
                }
                postMessage(deviceMessageBuffers[signum].msg.name,
                            &deviceMessageBuffers[signum].msg);
            } else {
                deviceMessageBuffers[signum].ignoredCount ++;
            }
        }

        /*
            fprintf(stderr, "XXX bing %d (inUse %d caught %d ignored %d) !\n", signum,
                deviceMessageBuffers[signum].inUse,
                deviceMessageBuffers[signum].caughtCount,
                deviceMessageBuffers[signum].ignoredCount);
        */
        /* While we're here, check the kernel event loop. */
        kernelSignalCounter ++;
        if (kernelSignalCounter == 1) {
            void Squawk_kernelContinue(void);
            // fprintf(stderr, "Entering kernel\n");
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
            struct sigaction sigact;

            deviceMessageBuffers[signum].handler = handler;
            sigact.sa_handler = deviceSignalHandler;
            sigemptyset(&sigact.sa_mask);
            sigact.sa_flags = 0;
            /* Currently ignoring errors and old value */
            sigaction(signum, &sigact, NULL);
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
        pthread_kill(pthread_self(), signum);
    }
}


/**
 * Routine to set up handlers for device interrupts from Java. Because
 * the Java code won't have access to global function pointers, we pass
 * the name of the handling routine as a byte-array and use dlsym()
 * to look it up.
 *
 * @param signum   identifier for the interrupt being handled
 * @param handler  address of a byte-array naming the function to be invoked as a handler
 */
#define MAX_HANDLER_NAME (128)
void os_setupInterrupt(int signum, Address handler) {
    void (*fct)(int signum) = null;

    if (handler != null) {
        char handlerName[MAX_HANDLER_NAME];
        int lth = getArrayLength(handler);
        if (lth <= 0 || lth >= MAX_HANDLER_NAME) {
            fatalVMError("Handler too large");
        } else {
            char *chars = (char *)handler;
            int i;
            for (i = 0 ; i < lth ; i++) {
                handlerName[i] = chars[i];
            }
            handlerName[lth] = 0;

            fct = (void (*)(int))dlsym(RTLD_DEFAULT, handlerName);
            if (fct == null) {
                fatalVMError("Handler not found");
            }
        }
    }

    os_setDeviceSignalHandler(signum, fct);
}


/*  Methods to aid in posting ChannelIO events. */

/*  On platforms where another JVM is used to handle IO and the like,
 *  we need a separate JNIEnv context to make this work. For this reason,
 *  we start a helper thread whose sole purpose is to be woken up
 *  so that it may post an event to that JVM.
 */

static pthread_t notifyTid;
static pthread_mutex_t notifyMutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t notifyCondVar = PTHREAD_COND_INITIALIZER;
static boolean notifyRequested = false;
static boolean exitRequested = false;

/**
 * The main routine for the helper thread.
 */
void *notifyStart(void *arg) {
    JNIEnv *env = NULL;
    pthread_mutex_lock(&notifyMutex);
    while (!exitRequested) {
        if (notifyRequested) {
            if (jvm != NULL) {
                if (env == NULL)
                    (*jvm)->AttachCurrentThread(jvm, (void **)&env, NULL);
                (*env)->CallStaticIntMethod(env, channelIO_clazz, channelIO_execute, -1, ChannelConstants_GLOBAL_POSTEVENT, -1, 0, 0, 0, 0, 0, 0, null, null);
                assume(!(*env)->ExceptionOccurred(env));
            }
            notifyRequested = false;
        }
        pthread_cond_wait(&notifyCondVar, &notifyMutex);
    }
    pthread_mutex_unlock(&notifyMutex);
    return NULL;
}

/**
 * Starts the event-posting helper thread.
 */
int startNotifyThread() {
    return pthread_create(&notifyTid, NULL, notifyStart, NULL);
}

/**
 * Wake the helper thread and have it post an event. Useful when all
 * user-mode threads are waiting on some event.
 */
void os_postEvent(boolean notify) {
    static boolean once = false;

    pthread_mutex_lock(&notifyMutex);
    if (!once) {
        startNotifyThread();
        once = true;
    }
    if (notify) {
        notifyRequested = true;
    } else {
        exitRequested = true;
    }
    pthread_cond_signal(&notifyCondVar);
    pthread_mutex_unlock(&notifyMutex);
}


/**
 * Routines for use by JavaDriverManager.
 */

/**
 * Starts the platform-specific alarm timer.
 *
 * @param start   time in microseconds until first timer interrupt
 * @param period  time in microseconds until each subsequent timer interrupt
 */
void os_setupAlarmInterval(int start, int period) {
    useconds_t t;
//fprintf(stderr, "setupAlarmInterval(%d, %d)\n", start, period);
//    t = ualarm(0, 0);
//fprintf(stderr, "ualarm(0, 0) = %d\n", t);
    t = ualarm(start, period);
//fprintf(stderr, "ualarm(%d, %d) = %d\n", start, period, t);
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


/**
 * Helper routines for ExampleIODriver
 */

/**
 * length of filename to open
 */
static int os_read_fileLen;

/**
 * byte-array containing filename to open
 */
static Address os_read_file;

/**
 * number of bytes to read from file
 */
static int os_read_numberBytes;

/**
 * byte-array for read-in data from file
 */
static Address os_read_inputBuffer;

/**
 * Allows the user application to set up the file to be read.
 *
 * @param fileLen       length of filename
 * @param file          byte-array containing filename
 * @param numberBytes   number of bytes to read in
 * @param inputBuffer   byte-array in which to place the read-in data from the file
 */
void os_setUpRead(int fileLen, Address file, int numberBytes, Address inputBuffer) {
    os_read_fileLen = fileLen;
    os_read_file    = file;
    os_read_numberBytes = numberBytes;
    os_read_inputBuffer = inputBuffer;
}

/**
 * Return how many bytes were read from the file.
 */
int os_getReadCount(void) {
    return os_read_numberBytes;
}

/**
 * Routine to read in data from the file.
 *
 * @param signum  device interrupt number (ignored)
 */
void os_readData(int signum) {
    int len = os_read_fileLen;
    {
        char fname[len + 1];
        Address buf = os_read_inputBuffer;
        int num = os_read_numberBytes;
        int lth;

        lth = getArrayLength(buf);
        if (num > lth)
            num = lth;

        if (num > 0) {
            FILE *file;
            char *chars = (char *)os_read_file;
            int i;
            for (i = 0 ; i < len ; i++) {
                fname[i] = chars[i];
            }
            fname[len] = 0;

            file = fopen(fname, "r");
            if (file != NULL) {
                int c;
                chars = (char *)buf;
                for (i = 0; i < num && (c = fgetc(file)) != EOF; i ++) {
                    chars[i] = c;
                }
                os_read_numberBytes = i;
                fclose(file);
            } else {
                os_read_numberBytes = 0;
            }
        } else {
            os_read_numberBytes = 0;
        }
    }
}

