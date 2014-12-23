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
 * Signal management.
 */

/**
 * Disable (and defer) the delivery of interrupts. Important,
 * for example, when entering the kernel or when interacting
 * with the message queues.
 *
 * returns 0 for success, the result from errno otherwise.
 */
int os_disableSignals(sigset_t *oldSigSet) {
    sigset_t sigSet;

    sigfillset(&sigSet);
    if (sigprocmask(SIG_SETMASK, &sigSet, oldSigSet) < 0) {
        return errno;
    }
    return 0;
}

/**
 * Enable delivery of interrupts.
 */
int os_enableSignals(sigset_t *sigSet) {
    if (sigprocmask(SIG_SETMASK, sigSet, NULL) < 0) {
        return errno;
    }
    return 0;
}

/**
 * Macros for enabling and disabling interrupts.
 */
#define deferInterruptsAndDo(action)                    \
    do {                                                \
        sigset_t savedSigSet;                           \
        os_disableSignals(&savedSigSet);                \
        disableInterrupts();                            \
        { action; }                                     \
        enableInterrupts();                             \
        os_enableSignals(&savedSigSet);                 \
    } while(0)

#define signalHandler_deferInterruptsAndDo(action)  deferInterruptsAndDo(action)

#define reenableInterrupts()                            \
    do {                                                \
        enableInterrupts();                             \
        os_enableSignals(&savedSigSet);                 \
    } while(0)

#define deferInterrupts()                               \
    do {                                                \
        os_disableSignals(NULL);                        \
        disableInterrupts();                            \
    } while(0)
