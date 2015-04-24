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

package com.sun.squawk;

import com.sun.squawk.util.Assert;
import java.io.PrintStream;

/**
 * Squawk implementation of explicit monitors for synchronization
 * 
 * @see VMThread
 * @see GC
 */
final class Monitor {

    /**
     * The thread that owns the monitor.
     */
    VMThread owner;

    /**
     * Queue of threads waiting to claim the monitor.
     */
    VMThread monitorQueue;

    /**
     * Queue of threads waiting to claim the object.
     */
    VMThread condvarQueue;

/*if[SMARTMONITORS]*/
    /**
     * Nesting depth.
     */
    short depth;

    /**
     * Flag to show if a wait occured.
     */
    boolean hasHadWaiter;
/*else[SMARTMONITORS]*/
//  int depth;
/*end[SMARTMONITORS]*/
    
    /**
     * The object that this is a monitor for. Used for debugging/assertions.
     */
    final Object object;

    /*
     * Constructor
     */
    Monitor(Object object) {
        this.object = object;
        VMThread.monitorsAllocatedCount++;
    }

    /**
     * Add a thread to the monitor wait queue.
     *
     * @param thread the thread to add
     */
    void addMonitorWait(VMThread thread) {
        thread.setInQueue(VMThread.Q_MONITOR);
        Assert.that(thread.nextThread == null);
        VMThread next = monitorQueue;
        if (next == null) {
            monitorQueue = thread;
        } else {
            while (next.nextThread != null) {
                next = next.nextThread;
            }
            next.nextThread = thread;
        }
    }
    
    /**
     * Add a thread to the head of the monitor wait queue.
     * @todo should be ordered first by priority, then by wait.
     *
     * @param thread the thread to add
     */
    void addMonitorWaitHead(VMThread thread) {
        thread.setInQueue(VMThread.Q_MONITOR);
        Assert.that(thread.nextThread == null);
        thread.nextThread = monitorQueue;
        monitorQueue = thread;
    }

    /**
     * Remove a thread from the monitor wait queue.
     *
     * @return a thread or null if there is none
     */
    VMThread removeMonitorWait() {
        VMThread thread = monitorQueue;
        if (thread != null) {
            monitorQueue = thread.nextThread;
            thread.setNotInQueue(VMThread.Q_MONITOR);
            thread.nextThread = null;
        }
        return thread;
    }

    /**
     * Add a thread to the conditional variable wait queue.
     *
     * @param thread the thread to add
     */
    void addCondvarWait(VMThread thread) {
        thread.setInQueue(VMThread.Q_CONDVAR);
        thread.monitor = this;
        Assert.that(thread.nextThread == null);
        VMThread next = condvarQueue;
        if (next == null) {
            condvarQueue = thread;
        } else {
            while (next.nextThread != null) {
                next = next.nextThread;
            }
            next.nextThread = thread;
        }
    }

    /**
     * Remove the next thread from the conditional variable wait queue.
     *
     * @return a thread or null if there is none
     */
    VMThread removeCondvarWait() {
        VMThread thread = condvarQueue;
        if (thread != null) {
            condvarQueue = thread.nextThread;
            thread.setNotInQueue(VMThread.Q_CONDVAR);
            thread.monitor = null;
            thread.nextThread = null;
        }
        return thread;
    }

    /**
     * Remove a specific thread from the conditional variable wait queue.
     *
     * @param thread the thread to remove
     */
    void removeCondvarWait(VMThread thread) {
        if (thread.inQueue(VMThread.Q_CONDVAR)) {
            VMThread next = condvarQueue;
            Assert.that(next != null);
            if (next == thread) {
                condvarQueue = thread.nextThread;
            } else {
                while (next.nextThread != thread) {
                    next = next.nextThread;
                    Assert.that(next != null);
                }
                if (next.nextThread == thread) {
                    next.nextThread = thread.nextThread;
                }
            }
            thread.setNotInQueue(VMThread.Q_CONDVAR);
            thread.monitor = null;
            thread.nextThread = null;
        }
    }
    
 
    void printWaitingThreads(PrintStream out, Object o) {
        VMThread thread = condvarQueue;
        if (condvarQueue != null || monitorQueue != null) {
            VM.outPrint("===== Monitor queues for ");
            VM.outPrint(Address.fromObject(this).toUWord().toPrimitive());
            VM.outPrint(" for object ");
            VM.outPrintln(o.toString());
        }
        if (thread != null) {
            VM.outPrintln("Threads waiting for notify:");
            while (thread != null) {
                thread.printStackTrace(out);
                thread = thread.nextThread;
            }
        }
        
        thread = monitorQueue;
        if (thread != null) {
            VM.outPrintln("Threads waiting for lock:");
            while (thread != null) {
                thread.printStackTrace(out);
                thread = thread.nextThread;
            }
        }
    }
}
