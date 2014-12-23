/*
 * Copyright 2012 Oracle. All Rights Reserved.
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
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */


#ifndef METAL_SLEEP_H
#define	METAL_SLEEP_H

#ifdef	__cplusplus
extern "C" {
#endif

/* ----------- defined in io_metal.c: ----------- */
extern int deepSleepEnabled;           // indicates whether the feature is currently enabled (=1)
extern int sleepManagerRunning;        // assume that sleepManager is running until it calls WAIT_FOR_DEEP_SLEEP
extern int outstandingDeepSleepEvent;  // whether the sleep manager thread should be unblocked at the next reschedule
extern long long storedDeepSleepWakeupTarget; // The millis that the next deep sleep should end at
extern long long minimumDeepSleepMillis; // minimum time we're prepared to deep sleep for: avoid deep sleeping initially.
extern long long totalShallowSleepTime;    // total time the SPOT has been shallow sleeping
extern int shallow_sleep_clock_mode;

/* ----------- defined in platform-specific code: ----------- */

/*
 * Enter deep sleep
 */
extern void doDeepSleep(long long targetMillis, int remain_powered);

/*
 * Enter shallow sleep
 */
extern void doShallowSleep(long long targetMillis);

#ifdef	__cplusplus
}
#endif

#endif	/* METAL_SLEEP_H */

