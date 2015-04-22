/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk;

import com.sun.squawk.Debugger.*;
import com.sun.squawk.pragma.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;
import java.io.PrintStream;
import com.sun.squawk.platform.Platform;
import com.sun.squawk.platform.SystemEvents;
import java.util.Enumeration;

/**
 * The Squawk implementation of threads.
 * 
 * @see Monitor
 */
public final class VMThread implements GlobalStaticFields {

    /*-----------------------------------------------------------------------*\
     *                          Global VM variables                          *
    \*-----------------------------------------------------------------------*/

    /**
     * Flag to help early VM bringup.
     */
    private final static boolean FATAL_MONITOR_ERRORS = false;

    /**
     * The initial size (in words) of a thread's stack.
     */
    private final static int INITIAL_STACK_SIZE = 168;

    /**
     * Limit stack size growth when stack would be 1/MAX_STACK_GROWTH_FRACTION of the heap. 
     */
    private final static int MAX_STACK_GROWTH_FRACTION = 8;

    /**
     * The minimum size (in words) of a thread's stack. This constant accounts for the
     * number of slots required for the meta-info slots at the beginning of the chunk
     * plus the slots required to successfully make the initial call to VM.do_callRun()
     *
     * Update: this should also include a few more slots to be able to execute the
     *         killThread() routine. Exactly how many is a guess, but this value should be increased
     *         if there is ever a stack extension while processing killThread().
     */
    final static int MIN_STACK_SIZE = SC.limit + FP.FIXED_FRAME_SIZE + 70;  // checked 9/2007

    /**
     * The current executing thread.
     */
    private static VMThread currentThread;

    /**
     * The thread to be executed after the next threadSwap().
     */
    private static VMThread otherThread;

    /**
     * The service thread for GC etc.
     */
    private static VMThread serviceThread;

    /**
     * The stack for the service thread. This address corresponds
     * with the object pointer to the chunk. That is, it is {@link HDR#arrayHeaderSize}
     * bytes past the memory chunk allocated for the stack. The native launcher
     * which allocated this chunk will have written the length of the stack
     * array header word. This will be used to subsequently format this block
     * as an object of type {@link Klass#LOCAL_ARRAY}.
     */
    private static Address serviceStack;

    /**
     * The queue of runnable threads.
     */
    private static ThreadQueue runnableThreads;

    /**
     * The queue of timed waiting threads.
     */
    private static TimerQueue timerQueue;

    /**
     * The 'name' of the next thread.
     */
    private static int nextThreadNumber;

    /**
     * Hashtable of threads waiting for an event.
     */
    private static EventHashtable events;
    
    /**
     * Hashtable of threads waiting for an OS event.
     */
    private static EventHashtable osevents;
    
    /**
     * Count of contended monitorEnters
     */
    private static int contendedEnterCount;
    
    /**
     * Count of monitors allocated
     */
    static int monitorsAllocatedCount;
    
    /**
     * Count of thread context switching.
     *
     * This does not include system-level switches that occur for GC, exception throwing, etc.
     */
    static int threadSwitchCount;
    
    /**
     * Time that system spent waiting - this covers idle as well as some system time.
     * Stored as two ints instead of longs, since GlobalStaticFields can't handle long values.
     */
    static int waitTimeHi32;
    static int waitTimeLo32;
    
    /**
     * Handler for OS events...
     */
    static SystemEvents systemEvents;

    /**
     * Maximum time that system will wait for IO, interrupts, etc.
     * WARNING: This can break system sleeping, and should only be used in emergencies.
     *
     * NOTE: global statics cannot handle "long" variables, so encode Long.MAX_VALUE as -1,
     * and other values as 1..Int.MAX_VALUE.
     */
    private static int max_wait; // initialized to -1 in initializeThreading().
    
    /**
     * If true, only schedule threads with system priority.
     */
    static boolean systemThreadsOnly;


    /*-----------------------------------------------------------------------*\
     *                            The public API                             *
    \*-----------------------------------------------------------------------*/

    /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = 1;

   /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = 5;

    /**
     * The maximum priority that a user thread can have.
     */
    public final static int MAX_PRIORITY = 10;
    
    /**
     * The maximum priority that a system thread can have.
     */
    public final static int MAX_SYS_PRIORITY = 12;
    public final static int REAL_MAX_SYS_PRIORITY = 14;
    
    /**
     * Return the number of Thread objects allocated during the lifetime of this JVM.
     * @return threads allocated
     */
    public static int getThreadsAllocatedCount() {
        return nextThreadNumber;
    }
    
    /**
     * Return the number of times that a thread was blocked trying to synchronize on an object.
     *
     * Note that this counts the initial contention. A thread may be released to aquire the lock,
     * but another thread (potentially higher priority) runs first, and actually acquires the lock.
     * The first thread will then have to wait again.
     * @return monitor contention count
     */
    public static int getContendedMontorEnterCount() {
        return contendedEnterCount;
    }
    
    /**
     * Return the number of monitors allocated.
     *
     * Often, uncontended locking is handled by the interpreter in the pendingMonitors cache. But if the 
     * cache is full, or there is contention, or Object.wait() is used, or a thread is switched out while 
     * holding a virtual monitor, then a real monitor has to be allocated for an object. It is possible for 
     * the monitor for an object to come and go, so there is the possibility of "monitor object thrashing".
     * @return  number of monitors allocated
     */
    public static int getMonitorsAllocatedCount() {
        return monitorsAllocatedCount;
    }
    
    /**
     * Return count of thread context switching.
     *
     * This does not include system-level switches that occur for GC, exception throwing, etc.
     * @return user-level thread switches
     */
    public static int getThreadSwitchCount() {
        return threadSwitchCount;
    }

    /**
     * Set the maximum time that system will wait for IO, interrupts, etc.
     * WARNING: This can break system sleeping, and should only be used in emergencies.
     *
     * @param max max wait time in ms. Must be > 0, and either Long.MAX_VALUE or < Integer.MAX_VALUE.
     */
    static void setMaxSystemWait(long max) {
        if (max <= 0) {
            throw new IllegalArgumentException();
        }
        if (max == Long.MAX_VALUE) {
            max_wait = -1;
        } else if (max > Integer.MAX_VALUE) {
            throw new IllegalArgumentException();
        } else {
            max_wait = (int)max;
        }
    }

   /**
     * Allocates a new <code>VMThread</code> object to support a given API Thread instance.
     * <p>
     * <b>This constructor should only be called from a java.lang.Thread constructor</b>.
     *
     * @param   apiThread  the API thread instance supported by this VM thread
     * @param   name the thread name, or null. If null, thread name generated from threadNumber.
     */
    public VMThread(Thread apiThread, String name) {
        Assert.always(apiThread != null);
        this.apiThread    = apiThread;
        this.threadNumber = nextThreadNumber++;
        this.state        = NEW;
        this.stackSize    = INITIAL_STACK_SIZE;
/*if[ENABLE_MULTI_ISOLATE]*/
        Object target = NativeUnsafe.getObject(apiThread, (int)FieldOffsets.java_lang_Thread$target);
        if (target instanceof Isolate) {
            if (apiThread instanceof CrossIsolateThread) {
                this.isolate  = (Isolate)target;
            } else {
                throw new SecurityException("No permision to create a cross-isolate thread");
            }
        } else {
            this.isolate  = VM.getCurrentIsolate();
        }
/*else[ENABLE_MULTI_ISOLATE]*/
//      this.isolate  = VM.getCurrentIsolate();
/*end[ENABLE_MULTI_ISOLATE]*/

        if (currentThread != null) {
            priority = (byte)currentThread.getPriority();
            if (priority > MAX_PRIORITY) {
                // don't inherit system priority
                priority = NORM_PRIORITY;
            }
        } else {
            priority = NORM_PRIORITY;
        }
        
        if (name != null) {
            this.name = name;
        } else {
            if (threadNumber == 0) {
                this.name = "Thread-0";
            } else {
                this.name = "Thread-".concat(String.valueOf(threadNumber));
            }
        }
    }

    /**
     * Returns a reference to the currently executing thread object.
     *
     * @return the currently executing thread
     */
    public static VMThread currentThread() {
        return currentThread;
    }

    /**
     * Sets the daemon state of the thread.
     * If this thread is alive, an IllegalThreadStateException is thrown.
     * 
     * @param value if true, set thread as a daemon
     */
    public void setDaemon(boolean value) {
       /*
        * Check that the thread has not yet been started (as in J2SE)
        */
        if (state != NEW) {
            throw new IllegalThreadStateException();
        }
        
        isDaemon = value;
    }

    /**
     * Gets the daemon state of the thread.
     * @return true if thread is a daemon thread
     */
    public boolean isDaemon() {
        return isDaemon;
    }
    
    /**
     * Adds a given thread to the timer queue.
     *
     * Note: this method may assign globals and threads into local variables and
     * so should not be on the stack of a call that eventually calls 'reschedule'
     *
     * @param thread   the thread to add
     * @param millis   the time to wait on the queue
     */
    private static void addToTimerQueue(VMThread thread, long millis) {
        Assert.that(thread != null);
        thread.setInQueue(Q_TIMER);
        timerQueue.add(thread, millis);
    }

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution) for the specified number of milliseconds. The thread
     * does not lose ownership of any monitors.
     *
     * @param      millis   the length of time to sleep in milliseconds.
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     * @see        java.lang.Object#notify()
     */
    public static void sleep(long millis) throws InterruptedException {
        if (millis < 0) {
            throw new IllegalArgumentException("negative sleep time");
        }
        // Was the thread interrupted?
        currentThread.handlePendingInterrupt();
        if (millis > 0) {
/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//          startFinalizers();
/*end[FINALIZATION]*/
            addToTimerQueue(currentThread, millis);
            reschedule();
            // Was the thread interrupted?
            currentThread.handlePendingInterrupt();
        }
    }

    /**
     * Adjust the target times of waiting threads to account for changes to the system clock.
     * Only adjust when time has moved backwards (e.i. only make threads wake up earlier, not later).
     * 
     * EXAMPLE:
     * A SPOT needs to do something at an absolute time, like launch a missile from Kwajalein Island at 15:24, as well as 
     * periodically sensor for turtles in the launch area every two minutes.
     * Then the SPOT's clock is updated to a more correct value from GPS. What should happen?
     * 
     * Look at a few cases with these assumptions:
     * - Initially SPOT thinks the time is 15:21.
     * - The periodic timer has already waited one minute, so there's 1 minute left to wait.
     * - Initial thread wait states:
     *      - Periodic task (PT) should fire in 1 minute. PT target time = 15:22
     *      - Absolute task (AT) should fire in 3 minutes. AT target time = 15:24
     * 
     * A) Clock gets adjusted - it's really it's 15:22. deltaT = + 1
     *  - Goal:
     *      - Periodic task should fire in 1 minute (from new time).  Perfect PT target time = 15:23
     *      - Absolute task should fire in 2 minutes (from new time). Perfect AT target time = 15:24
     * 
     * - Solution 1*: Do nothing. PT = 15:22, AT = 15:24. Low-level sleep for periodic task will complete early, but utility class can reschedule. Absolute task will be on time.
     * - Solution 2: Add deltaT to target times. PT = 15:23, AT = 15:25. Periodic task will not fire early, but absolute task will be late.
     * 
     * B) Clock gets adjusted - it's really it's 15:20. deltaT = - 1
     * Goal:
     *      - Periodic task should fire in 1 minute (from new time).  Perfect PT target time = 15:21
     *      - Absolute task should fire in 4 minutes (from new time). Perfect AT target time = 15:24
     * 
     * - Solution 1: Do nothing. PT = 15:22, AT = 15:24. Low-level sleep for periodic task will fire late. Absolute task will be on time.
     * - Solution 2*: Add deltaT to target times. PT = 15:21, AT = 15:23. Periodic task will fire on time. Absolute task will fire early, but utility class can reschedule.
     * 
     * In order to preserve sanity we should adjust the wait times when the time moves backwards (new clock < old clock), 
     * and do nothing if time moves forward. This will allow some sleep() and wait() methods to complete sooner than asked, 
     * but the sleep/wait can rescheduled by the caller. In no case will a thread wake up 
     * later than if the clock had not changed.
     * 
     * @param deltaT change in time in ms. Must be negative.
     */
    static void adjustWaits(long deltaT) {
        Assert.that(deltaT < 0);
        timerQueue.adjustWaits(deltaT);
    }

    /**
     * Adds a given thread to the queue of runnable threads.
     *
     * Note: this method may assign globals and threads into local variables and
     * so should not be on the stack of a call that eventually calls 'reschedule'
     *
     * @param thread   the thread to add
     */
    private static void addToRunnableThreadsQueue(VMThread thread) {
        Assert.that(!thread.isServiceThread());
        thread.checkInQueue(Q_NONE);
        runnableThreads.add(thread);
    }


   /**
    * Causes the currently executing thread object to temporarily pause
    * and allow other threads to execute.
    */
    public static void yield() {
/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//      startFinalizers();
/*end[FINALIZATION]*/
        addToRunnableThreadsQueue(currentThread);
        reschedule();
    }

    /**
     * Causes this thread to begin execution; the Java Virtual Machine
     * calls the <code>run</code> method of this thread.
     * <p>
     * The result is that two threads are running concurrently: the
     * current thread (which returns from the call to the
     * <code>start</code> method) and the other thread (which executes its
     * <code>run</code> method).
     *
     * @exception  IllegalThreadStateException  if the thread was already started.
     * @see        java.lang.Thread#run()
     */
    public void start() {

       /*
        * Check that the thread has not yet been started.
        */
        if (state != NEW) {
            throw new IllegalThreadStateException();
        }

        /*
         * Initialize the new thread and add it to the list of runnable threads.
         */
        baptiseThread();
    }

    /**
     * Tests if this thread is alive. A thread is alive if it has
     * been started and has not yet died.
     *
     * @return  <code>true</code> if this thread is alive;
     *          <code>false</code> otherwise.
     */
    public final boolean isAlive() {
        return state == ALIVE;
    }

    /**
     * Changes the priority of this thread.
     *
     * @param newPriority priority to set this thread to
     * @exception  IllegalArgumentException  If the priority is not in the
     *             range <code>MIN_PRIORITY</code> to
     *             <code>MAX_PRIORITY</code>.
     * @see        #getPriority
     * @see        java.lang.Thread#getPriority()
     * @see        java.lang.Thread#MAX_PRIORITY
     * @see        java.lang.Thread#MIN_PRIORITY
     */
    public final void setPriority(int newPriority) {
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        priority = (byte)newPriority;
    }
    
    /**
     * Changes the priority of this thread. Allows setting priority to "system" levels.
     *
     * @param newPriority priority to set this thread to
     * @exception  IllegalArgumentException  If the priority is not in the
     *             range <code>MIN_PRIORITY</code> to
     *             <code>MAX_SYS_PRIORITY</code>.
     * @see        #getPriority
     * @see        java.lang.Thread#getPriority()
     * @see        java.lang.Thread#MAX_PRIORITY
     * @see        java.lang.Thread#MIN_PRIORITY
     */
    public final void setSystemPriority(int newPriority) {
        if (newPriority > REAL_MAX_SYS_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        priority = (byte)newPriority;
    }

    /**
     * If true, only schedule threads with system priority. Can only be set by a thread with system priority.
     * @param systemOnly 
     */
    public static void setSystemThreadsOnly(boolean systemOnly) {
        if (currentThread.priority > MAX_PRIORITY) {
            systemThreadsOnly = systemOnly;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns this thread's priority.
     *
     * @return  this thread's name.
     * @see     #setPriority
     * @see     java.lang.Thread#setPriority(int)
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * Returns the current number of active threads in the VM.
     *
     * @return the current number of active threads
     */
    public static int activeCount() {
        return runnableThreads.size() + 1;
    }

    /**
     * Waits for this thread to die.
     *
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     */
    public final void join() throws InterruptedException {
        if (this != currentThread && isAlive()) {

            // Was the current thread interrupted?
            currentThread.handlePendingInterrupt();

            Assert.that(currentThread.nextThread == null);
            currentThread.nextThread = this.joiners;
            this.joiners = currentThread;
// if (currentThread.isolate != this.isolate) {
//                VM.println("NOTE: Cross-isolate join: " + currentThread + " is waiting to joing " + this);
// }
            Assert.that(currentThread.waitingToJoin == null);
            currentThread.waitingToJoin = this;
            currentThread.setInQueue(VMThread.Q_JOIN);
            reschedule();

            // Was the current thread interrupted?
            currentThread.handlePendingInterrupt();
        }
    }

/*if[ENABLE_MULTI_ISOLATE]*/
    /**
     * Waits for an isolate to stop.
     *
     * @param isolate the isolate to wait for
     */
    static void isolateJoin(Isolate isolate) {
        if (currentThread.isolate == isolate) {
            throw new RuntimeException("Isolate cannot join itself");
        }
        if (!isolate.isHibernated()) {
            Assert.that(currentThread.nextThread == null);
            currentThread.setInQueue(VMThread.Q_ISOLATEJOIN);
            isolate.addJoiner(currentThread);
            reschedule();
        }
    }
/*end[ENABLE_MULTI_ISOLATE]*/
    
/*if[ENABLE_MULTI_ISOLATE]*/
    /**
     * Handle case where thread of one isolate is waiting for monitor owned by other isolate, while 
     * isolate is is being hibernated.
     *
     * Actually check both ways.
     */
    private static void handleCrossIsolateSynchronization(Isolate isolate) {
/*if[DEBUG_CODE_ENABLED]*/
        for (java.util.Enumeration e = isolate.getChildThreads(); e.hasMoreElements(); ) {
            VMThread thread = (VMThread)e.nextElement();
            if (thread.monitor != null) {
                VMThread ownerThread = thread.monitor.owner;

                if (ownerThread == null) {
                    // isolate is exiting/hibernating, but a thread is waiting for an unowned monitor. should be ok...
                } else if (ownerThread.isolate != isolate) {
                    VM.print("######## WARNING: Isolate ");
                    VM.print(isolate.getName());
                    VM.println(" is exiting/hibernating, but a thread is waiting for monitor owned by another isolate.");
                    VM.print("    waiting thread: ");
                    VM.println(thread.toString());
                    VM.print("    owning thread: ");
                    VM.println(ownerThread.toString());
                } else if (ownerThread == thread) {
                    // hibernting thread owns the monitor, are any crossislaote threads waiting for it?
                    VMThread next = thread.monitor.monitorQueue;
                    while (next != null) {
                        if (next.isolate != isolate) {
                            VM.print("######## WARNING: Isolate ");
                            VM.print(isolate.getName());
                            VM.println(" is exiting/hibernating, but a thread in another isolate is waiting for monitor owned by this isolate.");
                            VM.print("    waiting thread: ");
                            VM.println(next.toString());
                            VM.print("    owning thread: ");
                            VM.println(thread.toString());
                        }
                        next = next.nextThread;
                    }
                }
            }
        }
/*end[DEBUG_CODE_ENABLED]*/
    }
/*end[ENABLE_MULTI_ISOLATE]*/
    
    /**
     * Hibernate all the threads in the isolate.
     *
     * @param isolate the isolate whose threads are to be hibernated
     */
    private static void hibernateIsolate0(Isolate isolate) {
/*if[ENABLE_MULTI_ISOLATE]*/
        handleCrossIsolateSynchronization(isolate);
        /*
         * Enable all the threads waiting for the isolate to stop.
         */
        VMThread list = isolate.getJoiners();
        startJoiners(list, VMThread.Q_ISOLATEJOIN);
/*end[ENABLE_MULTI_ISOLATE]*/
        /*
         * Prune the runnable threads and add them to the isolate.
         */
        runnableThreads.prune(isolate);

        /*
         * Prune the timer threads and add them to the isolate.
         */
        timerQueue.prune(isolate);

        /*
         * Iterate through the events
         */
        events.prune(isolate);
    }

    /**
     * Visit method for EventHashtable visitor.
     *
     * @param key the key
     * @param value the value
     */
/*
    public void visitIntHashtable(int key, Object value, Object context) {
        Isolate isolate = (Isolate)context;
        Thread t = (Thread)value;
        if (t.isolate == isolate) {
            Thread t2 = findEvent(key);
            Assert.that(t == t2);
            Assert.that(t.nextThread == null);
            t.setInQueue(Thread.HIBERNATEDRUN);
            isolate.addToHibernatedRunThread(t);
        }
    }
*/
    /**
     * Hibernate all the threads in a given isolate.
     *
     * @param isolate  the isolate whose threads are to be hibernated
     * @param forExit  true if the isolate is being exited
     */
    static void hibernateIsolate(Isolate isolate, boolean forExit) {

        /*
         * Do things to other threads in separate function so that there are
         * no dangling references to other threads in this activation record.
         */
        hibernateIsolate0(isolate);

/*if[ENABLE_MULTI_ISOLATE]*/
        /*
         * Add the current thread if it is in this isolate.
         */
        if (currentThread.isolate == isolate) {
            Assert.that(currentThread.nextThread == null);
            currentThread.setInQueue(VMThread.Q_HIBERNATEDRUN);
            isolate.addToHibernatedRunThread(currentThread);
            reschedule();
        }
/*else[ENABLE_MULTI_ISOLATE]*/
//      VM.stopVM(isolate.getExitCode());
/*end[ENABLE_MULTI_ISOLATE]*/
    }

/*if[ENABLE_MULTI_ISOLATE]*/
    /**
     * Unhibernate the isolate.
     *
     * @param isolate the isolate
     */
    static void unhibernateIsolate(Isolate isolate) {
        /*
         * Add back the timer threads.
         */
        VMThread threads = isolate.getHibernatedTimerThreads();
        while (threads != null) {
            VMThread thread = threads;
            threads = thread.nextTimerThread;
            thread.nextTimerThread = null;
            long time = thread.time;
            if (time == 0) {
                time = 1;
            }
            timerQueue.add(thread, time);
        }

        /*
         * Add back the runnable threads.
         */
        threads = isolate.getHibernatedRunThreads();
        while (threads != null) {
            VMThread thread = threads;
            threads = thread.nextThread;
            thread.nextThread = null;
            thread.setNotInQueue(VMThread.Q_HIBERNATEDRUN);
            addToRunnableThreadsQueue(thread);
        }
    }
/*end[ENABLE_MULTI_ISOLATE]*/
    
    /**
     * Gets the name of this thread. If {@link #setName} has never been called for this
     * thread, the return value will be of the from "Thread-<n>" where 'n' is a unique numeric
     * identifier for this thread.
     *
     * @return thread name in standard format
     */
    public final String getName() {
        return name;
    }

    /**
     * Sets the name of this thread.
     *
     * @param name  the new name for this thread
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a string representation of this thread, including a unique number
     * that identifies the thread and the thread's priority.
     *
     * @return  a string representation of this thread.
     */
    public String toString() {
        return getName().
            concat(" (pri=").
            concat(String.valueOf(getPriority())).
            concat(")]");
    }

    /**
     * Handler for OS events...
     */
    public static SystemEvents getSystemEvents() {
        return systemEvents;
    }

    /*-----------------------------------------------------------------------*\
     *                              Thread state                             *
    \*-----------------------------------------------------------------------*/

    /**
     * Thread state values.
     */
    private final static byte NEW = 0, ALIVE = 1, DEAD = 2;

    /**
     * Queue names.
     */
    final static byte Q_NONE=0, Q_MONITOR = 1, Q_CONDVAR = 2, Q_RUN = 3, Q_EVENT = 4, Q_JOIN = 5, Q_ISOLATEJOIN = 6, Q_HIBERNATEDRUN = 7, Q_TIMER = 8;

    /**
     * The Isolate under which the thread is running
     */
    private final Isolate isolate;

    /**
     * The execution stack for the thread.
     */
    private Object stack;

    /**
     * The size of the stack that will be created for this thread.
     */
    private final int stackSize;

    /**
     * The API thread instance.
     */
    private final Thread apiThread;

    /**
     * The state of the thread.
     */
    private byte state;

    /**
     * The execution priority.
     */
    byte priority;

    /**
     * The queue the thread is in.
     */
    private byte inqueue;

    /**
     * Reference used for enqueueing in the ready, monitor wait, condvar wait, or join queues.
     */
    VMThread nextThread;

    /**
     * The thread that this thread (and possibly other threads) are waiting to join.
     */
    private VMThread waitingToJoin;

    /**
     * Flag to show if thread is a daemon.
     */
    private boolean isDaemon;

    /**
     * Reference used for enqueueing in the timer queue.
     */
    VMThread nextTimerThread;

    /**
     * Threads waiting for this thread to die.
     */
    private VMThread joiners;

    /**
     * Time to emerge from the timer queue.
     */
    long time;

    /**
     * The numeric identifier of the thread. This is only unique within this thread's isolate.
     */
    private final int threadNumber;

    /**
     * The name of the thread;
     */
    private String name;

/*if[ENABLE_SDA_DEBUGGER]*/
    /**
     * The breakpoint hit by this currently being reported to an attached debugger.
     */
    private HitBreakpoint hitBreakpoint;

    /**
     * This contains information regarding the currently active stepping of this thread.
     * It will be null if this thread is not stepping.
     */
    private SingleStep step;
/*end[ENABLE_SDA_DEBUGGER]*/
    
    /**
     * The monitor when the thread is in the condvar queue.
     */
    Monitor monitor;

    /**
     * The interrupt status.
     */
    boolean pendingInterrupt;

    /**
     * The offset (in bytes) from the top of this thread's stack of the inner most
     * system frame before the application entry point of the thread. That is, the
     * frame from which the {@link Thread#run} or the <code>static void main(String[])</code>
     * method was called.
     */
    private Offset appThreadTop;

/*if[SMARTMONITORS]*/
    /**
     * Saved monitor nesting depth.
     */
    private short monitorDepth;

    /**
     * Maximum nesting depth.
     */
    private final static short MAXDEPTH = Short.MAX_VALUE;
/*else[SMARTMONITORS]*/
//  int monitorDepth;
//  private final static int   MAXDEPTH = Integer.MAX_VALUE;
/*end[SMARTMONITORS]*/

    private int errno; /* save errno after native calls so java thread's will see correct errno value. */


    /**
     * Fail if thread invarients are true.
     */
    private void checkInvarients() throws AllowInlinedPragma {
        Assert.that(monitorDepth >= 0);
        Assert.that((monitor != null) ? (monitorDepth > 0) : true);
        Assert.that((this == serviceThread) ? (monitor == null) : true); // service thread should never be waiting for a monitor...
        Assert.that((this == serviceThread) ? (inqueue == Q_NONE) : true); // service thread should never be waiting for a anything
        Assert.that((inqueue == Q_NONE) ? (nextThread == null) : true);
        Assert.that((inqueue == Q_CONDVAR) ==  (monitor != null)); // if on condvar, then montir must be set. Otherwise monitor must be clear
        Assert.that((stack == null) ? (state == DEAD) : true);
/*if[DEBUG_CODE_ENABLED]*/
        if (stack != null) {
            GC.checkSC(this);
        }
/*end[DEBUG_CODE_ENABLED]*/ 
    }

/*if[ENABLE_SDA_DEBUGGER]*/
    /*-----------------------------------------------------------------------*\
     *                           Debugger support                            *
    \*-----------------------------------------------------------------------*/

    /**
     * Debugger suspension is implemented by disallowing reschedule() from choosing to
     * run a thread that has a positive debugSuspendCount.
     *
     * The thread being suspended or resumed may be on any queue, including the run
     * queue, or may move from one queue to another, but won't run until it is both
     * on the run queue, AND has a zero debugSuspendCount.
     */
    private int debuggerSuspendCount;

    /**
     * The method and frame context of the debugger event currently being reported to
     * the debugger subsystem.
     */
    private ExecutionPoint eventEP;

    /**
     * Gets the value of the debugger suspension counter for this thread.
     *
     * @return the suspension count for this thread
     */
    public final int getDebuggerSuspendCount() {
        Assert.that(debuggerSuspendCount >= 0);
        return debuggerSuspendCount;
    }

    /**
     * Increases the suspension count of this thread.
     *
     * @return the new suspension count for this thread
     */
    public final int suspendForDebugger() {
        Assert.that(debuggerSuspendCount >= 0);
        Assert.that(this != currentThread);
        return ++debuggerSuspendCount;
    }

    /**
     * Decreases the suspension count of this thread.
     *
     * @param forDetach  if true, the count is set to 0
     * @return the new suspension count for this thread
     */
    public final int resumeForDebugger(boolean forDetach) {
        Assert.that(this != currentThread);
        if (forDetach){
            debuggerSuspendCount = 0;
        } else {
            if (debuggerSuspendCount > 0) {
                debuggerSuspendCount--;
            }
        }
        Assert.that(debuggerSuspendCount >= 0);
        return debuggerSuspendCount;
    }

    /**
     * Gets the method and frame context of this thread at which a debugger event occurred.
     *
     * @return null if this thread is not currently reporting an event to the debugger subsystem
     */
    public ExecutionPoint getEventExecutionPoint() {
//VM.println("get eventEP: " + eventEP + " debuggerSuspendCount = " + debuggerSuspendCount);
        return eventEP;
    }

    /**
     * Gets the combined values for <code>state</code> and <code>queue</code>.
     *
     * @return the queue in the least significant byte, and the state in the next least significant byte.
     */
    public final int getInternalStatus() {
        return (state << 8) | inqueue;
    }

    /**
     * @return Returns the step info object.
     */
    public final SingleStep getStep() {
        return step;
    }

    /**
     * Sets the object that will put this thread into single stepping mode the next time
     * it is scheduled to run.
     *
     * @param  step  the details of the requested step
     */
    public final void setStep(SingleStep step) {
        Assert.that(this != currentThread);
        Assert.that(this.step == null, "overlapping single step request detected too late");
        this.step = step;
    }

    /**
     * Removes the object (if any) that is keeping this thread in single stepping mode.
     */
    public final void clearStep() {
        step = null;
    }

    /**
     * Reports a non-exception breakpoint hit on this thread to a debugger. This thread is
     * blocked until the event manager in the debugger has processed the event.
     *
     * @param hitFO    the frame in which the breakpoint was hit
     * @param hitBCI   the bytecode index of the instruction at which the breakpoint was hit
     * @param debugger the debugger to report the exception to
     * @return the object used to report the breakpoint
     */
    void reportBreakpoint(Offset hitFO, Offset hitBCI, Debugger debugger) {
        Assert.that(hitBreakpoint == null);
        hitBreakpoint = new HitBreakpoint(this, hitFO, hitBCI);

        // Cannot report a breakpoint for this thread if it has already been removed
        // from the runnable queue. This will occur if there is a breakpoint in the
        // thread switch/breakpoint reporting code and we are already reporting
        // another breakpoint
        if (this.inqueue == 0) {
/*if[DEBUG_CODE_ENABLED]*/
VM.println("=== In reportBreakpoint():");
hitBreakpoint.dumpState();
VM.print("   bcount: ");
VM.println(VM.branchCount());
/*end[DEBUG_CODE_ENABLED]*/

            eventEP = hitBreakpoint.getLocation();
//VM.println("set eventEP: " + eventEP);
            debugger.notifyEvent(new Debugger.BreakpointEvent(eventEP));
//VM.println("clearing eventEP: " + eventEP);
            eventEP = null;

            // This thread has finished reporting the breakpoint and has been
            // resumed by the debugger.
        }

        if (hitBreakpoint != null) {
            hitBreakpoint.setState(HitBreakpoint.BP_REPORTED);
        }
    }

    /**
     * Records an exception that must be reported to a debugger once this thread
     * is scheduled to be run. This call is always made on the service thread.
     *
     * @param exception the exception thrown/raised
     * @param throwFO   the frame in which the exception was thrown
     * @param throwBCI  the bytecode index of the instruction at which the exception was thrown
     * @param catchFO   the frame that will catch exception
     * @param catchBCI  the bytecode index of the exception handler
     * @return the object that will be used to report the exception
     */
    HitBreakpoint recordExceptionToReport(Offset throwFO, Offset throwBCI, Throwable exception, Offset catchFO, Offset catchBCI) {
        Assert.that(hitBreakpoint == null);
        hitBreakpoint = new HitBreakpoint(this, throwFO, throwBCI, exception, catchFO, catchBCI);
/*if[DEBUG_CODE_ENABLED]*/
VM.println("=== In recordExceptionToReport: HitBreakpoint.EXC_HIT:");
hitBreakpoint.dumpState();
/*end[DEBUG_CODE_ENABLED]*/
        return hitBreakpoint;
    }

    /**
     * Reports an exception breakpoint hit on this thread to a debugger. This thread is
     * blocked until the event manager in the debugger has processed the event.
     *
     * @param debugger the debugger to report the exception to
     * @return the object used to report the breakpoint
     */
    void reportException(Debugger debugger) throws Throwable {
        HitBreakpoint hbp = hitBreakpoint;

/*if[DEBUG_CODE_ENABLED]*/
VM.println("=== In reportException():");
hbp.dumpState();
VM.print("   bcount: ");
VM.println(VM.branchCount());
/*end[DEBUG_CODE_ENABLED]*/

        Throwable exception = hbp.getException();
        Assert.that(exception != null);

        try {
            eventEP = hbp.getLocation();
//VM.println("set eventEP: " + eventEP);
            debugger.notifyEvent(new Debugger.ExceptionEvent(exception, eventEP, hbp.getCatchLocation(), hbp.isCaught()));
//VM.println("clearing eventEP: " + eventEP);
            eventEP = null;
        } catch (Throwable e) {
            e.printStackTrace();
        }

/*if[DEBUG_CODE_ENABLED]*/
VM.println("=== after reportException():");
hbp.dumpState();
VM.print("   bcount: ");
VM.println(VM.branchCount());
/*end[DEBUG_CODE_ENABLED]*/
        hbp.setState(HitBreakpoint.EXC_REPORTED);
        throw exception; // rethrow original exception
    }

    /**
     * Reports completion of a step on this thread to a debugger. This thread is
     * blocked until the event manager in the debugger has processed the event.
     *
     * @param fo   offset (in bytes) from top of stack of the frame stepped to
     * @param bci  the bytecode index of the instruction stepped to
     */
    void reportStepEvent(Offset fo, Offset bci) {

        Debugger debugger = VM.getCurrentIsolate().getDebugger();
        Assert.that(!fo.isZero());
        Object mp = VM.getMP(frameOffsetAsPointer(fo));
        Assert.that(mp != null);

/*if[DEBUG_CODE_ENABLED]*/
VM.println("=== In reportStepEvent() " + step);
VM.print("Proposing stepEvent @ mp:");
VM.printAddress(mp);
VM.print(" bci: ");
VM.printOffset(bci);
VM.println();
/*end[DEBUG_CODE_ENABLED]*/

        eventEP = new ExecutionPoint(fo, bci, mp);
//VM.println("set eventEP: " + eventEP);
        debugger.notifyEvent(new Debugger.SingleStepEvent(eventEP));
//VM.println("clearing eventEP: " + eventEP);
        eventEP = null;
    }

    /**
     * Gets the hit breakpoint info for this thread.
     *
     * @return  the hit breakpoint info or null if this thread is not currently reporting a breakpoint.
     */
    HitBreakpoint getHitBreakpoint() {
        return hitBreakpoint;
    }

    /**
     * Clears the object used to report a breakpoint hit.
     */
    public void clearBreakpoint() {
        hitBreakpoint = null;
    }
/*else[ENABLE_SDA_DEBUGGER]*/
//    /**
//     * Increases the suspension count of this thread.
//     *
//     * @return the new suspension count for this thread
//     */
//    public final int suspendForDebugger() {
//        Assert.shouldNotReachHere();
//        return 0;
//    }
//
//    /**
//     * Decreases the suspension count of this thread.
//     *
//     * @param forDetach  if true, the count is set to 0
//     * @return the new suspension count for this thread
//     */
//    public final int resumeForDebugger(boolean forDetach) {
//        Assert.shouldNotReachHere();
//        return 0;
//    }
//    /**
//     * Gets the value of the debugger suspension counter for this thread.
//     *
//     * @return the suspension count for this thread
//     */
//    public final int getDebuggerSuspendCount() {
//        return 0;
//    }
/*end[ENABLE_SDA_DEBUGGER]*/

    void setAppThreadTop(Offset fp) {
        appThreadTop = fp;
    }

    Offset getAppThreadTop() {
        return appThreadTop;
    }


    /*-----------------------------------------------------------------------*\
     *                           The implementation                          *
    \*-----------------------------------------------------------------------*/

    /**
     * Initialize the threading system.
     */
    static void initializeThreading() {
        nextThreadNumber    = 0;
        runnableThreads     = new ThreadQueue();
        timerQueue          = new TimerQueue();
        events              = new EventHashtable();
        osevents            = new EventHashtable();
        currentThread       = asVMThread(new Thread()); // Startup using a dummy thread
        serviceThread       = currentThread;
        max_wait            = -1; // encode value of Long.MAX_VALUE

        /*
         * Convert the block of memory allocated for the service thread's stack into a
         * proper object of type Klass.LOCAL_ARRAY.
         */
        GC.setHeaderClass(serviceStack, Klass.LOCAL_ARRAY);
        
        /*
         * NOTE: The service stack has no backpointer to the service thread, and
         * is not GC.registerStackChunks(). It is allocated by C code, and isn't really in the heap?
         */
        //NativeUnsafe.setObject(serviceStack, SC.owner, serviceThread);
        serviceThread.stack = serviceStack.toObject();
    }
    
    /**
     * Initialize the threading system.
     */
    static void initializeThreading2() {
/*if[PLATFORM_TYPE_NATIVE]*/
        systemEvents = Platform.createSystemEvents();
        systemEvents.startIO();
/*else[PLATFORM_TYPE_NATIVE]*/
//       systemEvents = null;
/*end[PLATFORM_TYPE_NATIVE]*/
    }

    /**
     * Gets the isolate of the thread.
     *
     * @return the isolate
     */
    public Isolate getIsolate() {
        return isolate;
    }

    public Thread getAPIThread() {
        return apiThread;
    }

    public static VMThread asVMThread(Thread thread) {
        if (thread == null) {
            return null;
        }
        return (VMThread)NativeUnsafe.getObject(thread, (int)FieldOffsets.java_lang_Thread$vmThread);
    }

    /**
     * Determines if this thread is the service thread.
     */
    public boolean isServiceThread() {
        return this == serviceThread;
    }

    /**
     * Gets the number of this thread which is only guaranteed to be unique
     * within this thread's isolate.
     *
     * @return an integer identifier for this thread
     */
    public final int getThreadNumber() {
        return threadNumber;
    }

    /**
     * Special thread starter that reschedules the currently executing thread.
     */
    final void primitiveThreadStart() {
        start();
        rescheduleNext();
    }

/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//    /**
//     * Start any pending finalizers.
//     */
//    private static void startFinalizers() {
//        while (true) {
//            Finalizer finalizer = currentThread.isolate.removeFinalizer();
//            if (finalizer == null) {
//                 break;
//            }
//            try {
//                new Thread(finalizer).start();
//            } catch(OutOfMemoryError ex) {
//                currentThread.isolate.addFinalizer(finalizer); // Try again sometime later.
//                return;
//            }
//        }
//    }
/*end[FINALIZATION]*/

    /**
     * Prepare a thread for execution.
     */
    private void baptiseThread() {
        Assert.that(currentThread != null);
        Assert.always(state == NEW);
        stack = newStack(stackSize, this, true);
        if (stack == null) {
VM.println("creating stack: " + stackSize);
            throw VM.getOutOfMemoryError();
        }

//VM.print("Thread::baptiseThread - stack size = ");
//VM.println(stackSize);
        state = ALIVE;

//VM.print("Thread::baptiseThread - owner of stack chunk ");
//VM.printAddress(stack);
//VM.print(" = ");
//VM.printAddress(NativeUnsafe.getObject(stack, SC.owner));
//VM.println();

        isolate.addThread(this);
        addToRunnableThreadsQueue(this);

/*if[ENABLE_SDA_DEBUGGER]*/        
        if (VM.isThreadingInitialized()) {
            Debugger debugger = VM.getCurrentIsolate().getDebugger();
            if (debugger != null) {
                debugger.notifyEvent(new Debugger.Event(Debugger.Event.THREAD_START, this));
            }
        }
/*end[ENABLE_SDA_DEBUGGER]*/
    }

    /**
     * End thread execution.
     * Never returns unless exception is thrown. Ordinarily switches to another thread.
     */
    private void killThread(boolean nicely, boolean uncaughtException) {
/*if[DEBUG_CODE_ENABLED]*/
        if (monitor != null && (monitor.owner == this)) {
            // hibernating thread owns the monitor, are any crossislaote threads waiting for it?
            VMThread next = monitor.monitorQueue;
            while (next != null) {
                if (next.isolate != isolate) {
                    VM.print("######## WARNING: thread in isolate ");
                    VM.print(isolate.getName());
                    VM.println(" is exiting/hibernating, but a thread in another isolate is waiting for monitor owned by this isolate.");
                    VM.print("    waiting thread: ");
                    VM.println(next.toString());
                    VM.print("    owning thread: ");
                    VM.println(this.toString());
                }
                next = next.nextThread;
            }
        }
/*end[DEBUG_CODE_ENABLED]*/
 
        Assert.always(state == ALIVE);
        
/*if[ENABLE_SDA_DEBUGGER]*/
        // do notification before setting state = dead, otherwise sync code gets confused...
        if (VM.isThreadingInitialized()) {
            Debugger debugger = VM.getCurrentIsolate().getDebugger();
            if (debugger != null) {
                debugger.notifyEvent(new Debugger.Event(Debugger.Event.THREAD_END, this));
            }
        }
/*end[ENABLE_SDA_DEBUGGER]*/
        
//VM.print("Thread::killThread - owner of stack chunk "); VM.printAddress(stack); VM.print(" = "); VM.printAddress(NativeUnsafe.getObject(stack, SC.owner)); VM.println();
        boolean exitIsolate = false;
        if (nicely) {
            // this can trigger shutdown hooks, and might certainly yield
            exitIsolate = isolate.removeThread(this);
        }
        
        VMThread list = joiners;
        joiners = null;
        startJoiners(list, VMThread.Q_JOIN);
        
        if (exitIsolate) {
            isolate.exit(uncaughtException ? 1 : 0);
        }
        
        state = DEAD;
        
        // Can't zero stack here - Must zero atomically with other stack-reelated backpointers, 
        // in same method as VM.threadSwitch() call. Otherwise a poorly-timed extendStack could occur
        // and cause a threadSwitch in the middle of killing the thread.
        abandonThread();
    }

    /**
     * Starts a list of waiting threads.
     *
     * @param list the list of waiting threads
     * @param queueName the queue name (JOIN, or ISOLATEJOIN)
     */
    private static void startJoiners(VMThread list, byte queueName) {
        while (list != null) {
            VMThread next = list.nextThread;
            list.nextThread = null;
            list.waitingToJoin = null;
            list.setNotInQueue(queueName);
            if (list.isolate.isAlive()) {
                addToRunnableThreadsQueue(list);
            }
            list = next;
        }
    }

    /**
     * Get the thread's stack
     *
     * @return the stack
     */
    final Object getStack() throws ForceInlinedPragma {
        return stack;
    }

    /**
     * Converts an address within the stack of this thread to an offset (in bytes)
     * relative to the top of the stack.
     *
     * @param fp  the address to convert
     * @return the value of <code>fp</code> as an offset
     */
    final Offset framePointerAsOffset(Address fp) throws ForceInlinedPragma {
        int size = GC.getArrayLengthNoCheck(stack);
        Address stackEnd = Address.fromObject(stack).add(size * HDR.BYTES_PER_WORD);
/*
//if (!(fp.hi(Address.fromObject(stack)) && fp.loeq(stackEnd))) {
//    VM.print("fp=");
//    VM.printAddress(fp);
//    VM.print(" stack=");
//    VM.printAddress(stack);
//    VM.print(" stackEnd=");
//    VM.printAddress(stackEnd);
//    VM.println();
//}
*/
        Assert.that(fp.hi(Address.fromObject(stack)) && fp.loeq(stackEnd));
        return stackEnd.diff(fp);
    }

    /**
     * Converts an offset (in bytes) relative to the top of this thread's stack
     * to an address within the stack.
     *
     * @param fp   the offset to convert
     * @return the offset as an address
     */
    final Address frameOffsetAsPointer(Offset fp) throws ForceInlinedPragma {
        int size = GC.getArrayLengthNoCheck(stack) * HDR.BYTES_PER_WORD;
        Assert.that(fp.ge(Offset.zero()) && fp.lt(Offset.fromPrimitive(size)));
        Address stackEnd = Address.fromObject(stack).add(size);
        return stackEnd.subOffset(fp);
    }

    private static int stacksAllocatedCount;
    private static int maxStackSize;
    
    /**
     * Return the number of stacks allocated.
     *
     * Stacks are allocated for each thread, and as more frames are needed, new stacks 
     * are created to replace the original stacks (typically at 2x the size of the original stack).
     * The default stack size is about 160 words.
     *
     * @return total number of stacks ever allocated
     */
    public static int getStacksAllocatedCount() {
        return stacksAllocatedCount;
    }
    
    /**
     * Return size of the largest stack ever allocated, in words.
     * @return largest stack size ever allocated
     */
    public static int getMaxStackSize() {
        return maxStackSize;
    }
    
    
    private static void threadGC(boolean userThread, boolean fullGC) {

    	if (userThread) {
            VM.collectGarbage(fullGC);
        } else {
            GC.collectGarbage(fullGC);
        }

    }
    
    /**
     * Allocates a new stack.
     *
     * @param size       the size of the stack in words
     * @param owner      the owner of the new stack
     * @param userThread true if call is made on a user mode thread
     * @return the stack or null if none could be allocated
     */
    private static Object newStack(int size, VMThread owner, boolean userThread) {
        Object stack = GC.getExcessiveGC() ? null : GC.newStack(size, owner);
        if (stack == null) {
            threadGC(userThread, false);
            stack = GC.newStack(size, owner);
            if (stack == null) {
                threadGC(userThread, true);
                stack = GC.newStack(size, owner);
            }
        }
        
        stacksAllocatedCount++;
        if (size > maxStackSize) {
            maxStackSize = size;
        }
        return stack;
    }

    /**
     * Extends the stack of the currently executing thread.
     * <p>
     * This code is called from the core VM using the service stack and it is very important
     * that there are no non-null pointers in the inner most activation record on the stack.
     * This is guaranteed by the fact that this inner most method will *always* be at the
     * very first instruction which will *always* be an EXTEND instruction.
     *
     * @param overflow the number of words by which the frame of the inner method
     *        overflows the current stack size
     * @return false if the allocation failed
     */
    static boolean extendStack(int overflow) {
        /*
         * Stack extension will not work if com.sun.squawk.Klass has not been initialized as
         * the Klass.LOCAL static variable will be null.
         */
        Assert.always(VM.isCurrentIsolateInitialized()); // "cannot extend stack until com.sun.squawk.Class is initialized"
        Assert.always(currentThread == VMThread.serviceThread);
        // figure out first size to try:
        final int oldSize = GC.getArrayLength(otherThread.stack);
        final int minSize = oldSize + overflow;
        int newSize;
        // don't double in size when approaching fraction of heap
        int fraction = (int)((GC.totalMemory() / (HDR.BYTES_PER_WORD * MAX_STACK_GROWTH_FRACTION)));
        if (minSize > fraction) {
            newSize = oldSize + (oldSize / 2);
            if (newSize < minSize) {
                newSize = minSize + (minSize / 2);
            }
        } else {
            newSize = oldSize * 2;
            if (newSize < minSize) {
                newSize = minSize * 2;
            }
        }
        /*
         * Allocate a new stack and copy the contents of the old stack.
         */
        Object newStack = newStack(newSize, otherThread, false);
        if (newStack == null) {
            newSize = minSize;
            newStack = newStack(newSize, otherThread, false);
        }
        if (newStack == null) {
            return false;
        } else {
            GC.stackCopy(otherThread.stack, newStack);
            otherThread.stack = newStack;
            Assert.that(GC.getKlass(NativeUnsafe.getAddress(NativeUnsafe.getAddress(newStack, SC.lastFP), FP.method)) == Klass.BYTECODE_ARRAY);
            return true;
        }
    }

    /**
     * Call the run() method of a thread. This is called by the VM when a new thread is started.
     * The call sequence is that Thread.start() calls Thread.reschedule() which calls VM.switchToThread()
     * which calls VM.callRun() which calls this function.
     *
     * @throws NotInlinedPragma as this method saves the current frame pointer
     */
    final void callRun() throws NotInlinedPragma {
        boolean uncaughtException = false;
        try {
            boolean didAbort = false;
            try {
                VMThread thread = VMThread.currentThread;
                Assert.always(thread == this);
                thread.appThreadTop = thread.framePointerAsOffset(VM.getFP());
                apiThread.run(); // yes, run(), not start().
            } catch (OutOfMemoryError e) {
                uncaughtException = true;
                VM.print("Uncaught out of memory error on thread - aborting isolate ");
                VM.printThread(this);
                VM.println();
                isolate.abort(999);
                didAbort = true;
            } catch (Throwable ex) {
                uncaughtException = true;
                VM.printExceptionAndTrace(ex, "Uncaught exception in Thread.run():");
            }
/*
//VM.print("killing thread ");
//VM.print(threadNumber);
//VM.print(", bcount = ");
//VM.println(VM.branchCount());
*/
            killThread(!didAbort, uncaughtException);
             // Never returns unless exception is thrown. Ordinarily switches to another thread.
        } catch (OutOfMemoryError e) {
            // This almost certainly means that the value of MIN_STACK_SIZE should be increased
            // as there should always be enough stack left to complete the killThread() call
            VM.println("Uncaught out of memory error while killing thread - aborting isolate");
        } catch (Throwable e) {
            VM.printExceptionAndTrace(e, "Uncaught exception while killing thread - aborting isolate:");
        }
        try {
            isolate.abort(999);
        } catch (Throwable e) {
            VM.print("Uncaught ");
            VM.print(GC.getKlass(e).getInternalName());
            VM.print(" while aborting isolate [");
            VM.println("]");
        }
        killThread(!true, uncaughtException);
        // Never returns unless exception is thrown. Ordinarily switches to another thread.
        VM.fatalVMError();
    }

    /**
     * Primitive method to choose the next executable thread.
     */
    private static void rescheduleNext() {
        Assert.that(GC.isSafeToSwitchThreads());
        VMThread thread = null;

        /*
         * Loop until there is something to do.
         */
        while (true) {

            /*
             * Add any threads that are ready to be restarted.
             */
            int event;
            while ((event = VM.getEvent()) != 0) {
                signalEvent(event);
            }

            /*
             * Add any threads waiting for a certain time that are now due.
             */
//VM.println("Add any threads waiting for a certain time that are now due.");
            while ((thread = timerQueue.next()) != null) {
                Assert.that(thread.isAlive());
                Monitor monitor = thread.monitor;
                /*
                 * If the thread is wait()ing on a monitor then remove it
                 * from the conditional variable wait queue.
                 */
                if (monitor != null) {
                    monitor.removeCondvarWait(thread);
                    /* Stop wait for condvar, and make runnable.
                     * when eventually run, the code in monitorWait will attempt to grab the monitor.
                     */
                } else {
                    /*
                     * Otherwise it is just waking up from a sleep() so it is now
                     * ready to run.
                     */
                    thread.setNotInQueue(Q_TIMER);
                }
                addToRunnableThreadsQueue(thread);
            }
//VM.println("Break if there is something to do.");
            /*
             * Break if there is something to do.
             */
            if ((thread = runnableThreads.next()) != null) {
                break;
            }

            /*
             * Wait for an event or until timeout.
             */
//VM.println("Wait for an event or until timeout..");
            long delta = timerQueue.nextDelta();
            if (delta > 0) {
                if (delta == Long.MAX_VALUE && events.size() == 0 && osevents.size() == 0) {
                    /*
                     * This situation will usually only come about if the bootstrap
                     * isolate called System.exit() instead of VM.stopVM(). However,
                     * it will also occur if an isolate is dead-locked and all other
                     * isolates are waiting for it to complete.
                     */
                    VM.println("=== DEAD-LOCK STATUS: ===");
                    Isolate.printAllIsolateStates(System.err);
                    Assert.shouldNotReachHere("Dead-locked system: no schedulable threads");
                }
//VM.println("waitForEvent timeout");
                // Emergency switch in case waitForEvent() "breaks" (misses wakeups and hangs)
                // This hasn't happenned, but need a fix that can be run in the field.
                if (max_wait != -1 && delta > max_wait) {
                    delta = max_wait;
                }
                long waitTime = VM.getTimeMillis();
                long oldWaitTimeTotal = getTotalWaitTime();
                VM.waitForEvent(delta);
                waitTime = VM.getTimeMillis() - waitTime;
                oldWaitTimeTotal += waitTime;
                waitTimeHi32 = VM.getHi(oldWaitTimeTotal);
                waitTimeLo32 = VM.getLo(oldWaitTimeTotal);
            }
        }
        
//VM.println("scheduling thread " + thread);

        /*
         * Set the next thread.
         */
        Assert.that(thread != null);
        thread.checkInQueue(Q_NONE);
        otherThread = thread;
    }
    
    public static long getTotalWaitTime() {
        return VM.makeLong(waitTimeHi32, waitTimeLo32);
    }
    
    /**
     * Print thread state one one line.
     * 
     * Will print on the stream <code>out</code> unless an error occurs while printing on that stream,
     * such as a null stream, or IO error on the stream.
     * 
     * If a printing error occurs, this falls back on printing via VM.print(), etc.
     * 
     * @param out the stream to print on.
     */
    public void printState(PrintStream out) {
        VM.outPrint(out, "thread ");
        VM.outPrint(out, getName());
        VM.outPrint(out, " priority: ");
        VM.outPrint(out, getPriority());

/*if[DEBUG_CODE_ENABLED]*/
        String stateStr = "NONE";
        String waitingStr = "NONE";
        switch (state) {
            case NEW:
                stateStr = "NEW";
                break;
            case ALIVE:
                stateStr = "ALIVE";
                break;
            case DEAD:
                stateStr = "DEAD";
                break;
        }
        
        switch (inqueue) {
            case Q_MONITOR:
                waitingStr = "MONITOR";
                break;
            case Q_CONDVAR:
                waitingStr = "CONDVAR";
                break;
            case Q_RUN:
                waitingStr = "RUNNABLE";
                break;
            case Q_EVENT:
                waitingStr = "EVENT";
                break;
            case Q_JOIN:
                waitingStr = "JOIN";
                break;
            case Q_ISOLATEJOIN:
                waitingStr = "ISOLATEJOIN";
                break;
            case Q_HIBERNATEDRUN:
                waitingStr = "HIBERNATEDRUN";
                break;
            case Q_TIMER:
                waitingStr = "TIMER";
                break;
        }
        
        VM.outPrint(out, " state: ");
        VM.outPrint(out, stateStr);
        VM.outPrint(out, " queue: ");
        VM.outPrint(out, waitingStr);
        
        switch (inqueue) {
            case Q_MONITOR:
                VM.outPrint(out, " waiting to lock object in ");
                Monitor m = lookupROMMonitor();
                if (m != null) {
                    VM.outPrint(out, "ROM " + m.object);
                } else {
                    VM.outPrint(out, "in heap");
                }
                break;
            case Q_CONDVAR:
                VM.outPrint(out, " waiting on condvar for object ");
                if (monitor != null) {
                    // in an Object.wait()...
                    VM.outPrint(out, monitor.object.toString());
                } else {
                    VM.outPrint(out, "???");
                }
                break;
            case Q_EVENT:
                VM.outPrint(out, " waiting for ");
                if (waitingforEvent()) {
                    VM.outPrint(out, "low-level event");
                } else if (waitingforOSEvent()) {
                    VM.outPrint(out, "OS event");
                } else {
                    VM.outPrint(out, "???");
                }
                break;
            case Q_JOIN:
                VM.outPrint(out, " waiting to join " + waitingToJoin);
                break;
            case Q_TIMER:
                VM.outPrint(out, " waiting for ms (remaining): ");
                long delta = time - VM.getTimeMillis();
                VM.outPrint(out, delta);
                break;
        }

        if (pendingInterrupt) {
            VM.outPrint(out, " pendingInterrupt! ");
        }
/*else[DEBUG_CODE_ENABLED]*/
//        VM.outPrint(out, " state: ");
//        VM.outPrint(out, state);
//        VM.outPrint(out, " queue: ");
//        VM.outPrint(out, inqueue);
/*end[DEBUG_CODE_ENABLED]*/
        
        VM.outPrintln(out);
    }
    
    
/*if[DEBUG_CODE_ENABLED]*/
    /**
     * This is a (SLOW) method to try to find the monitor that a thread is trying to lock.
     * @return the monitor that this thread is trying to lock, or null.
     */
    private Monitor lookupROMMonitor() {
        Assert.that(inQueue(Q_MONITOR));
        SquawkHashtable monitorTable = getIsolate().getMonitorHashtable();
        Enumeration e = monitorTable.elements();
        while (e.hasMoreElements()) {
            Monitor m = (Monitor) e.nextElement();
            VMThread thr = m.monitorQueue;
            while (thr != null) {
                if (thr == this) {
                    return m;
                }
                thr = thr.nextThread;
            }
        }
        return null;
    }
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * Print a stack trace for this thread.<p>
     * 
     * Will print on the stream <code>out</code> unless an error occurs while printing on that stream,
     * such as a null stream, or IO error on the stream.
     * 
     * If a printing error occurs, this falls back on printing via VM.print(), etc.
     *
     * @param stream
     */
    public void printStackTrace(PrintStream stream) {
        ExecutionPoint[] trace = VM.reifyStack(this, -1);
        for (int i = 0; i != trace.length; ++i) {
            stream.print("    ");
            if (trace[i] != null) {
                trace[i].print(stream);
            } else {
                stream.print("undecipherable");
            }
            stream.println();
        }
    }

    /**
     * Context switches to another thread.
     *
     * @throws NotInlinedPragma  as the frame of this method will be the inner most frame on the
     *                           current thread's stack. The inner most frame on any stack does
     *                           not have it's local variables scanned by the garbage collector.
     *                           As such, this method <b>must not</b> use any local variables.
     */
    private static void reschedule() throws NotInlinedPragma {
        fixupPendingMonitors();  // Convert any pending monitors to real ones
        threadSwitchCount++;
/*if[DEBUG_CODE_ENABLED]*/
        if (!GC.isGCEnabled()) {
            throw new IllegalStateException("reschedule while GC disabled!");
        }
/*end[DEBUG_CODE_ENABLED]*/
        rescheduleNext();        // Select the next thread
        VM.threadSwitch();       // and switch
        currentThread.checkInQueue(Q_NONE);
        currentThread.checkInvarients();
    }

    /**
     * Called to terminate running on a thread.
     * Never returns unless exception is thrown. Ordinarily switches to another thread.
     *
     * @throws NotInlinedPragma  as the frame of this method will be the inner most frame on the
     *                           current thread's stack. The inner most frame on any stack does
     *                           not have it's local variables scanned by the garbage collector.
     *                           As such, this method <b>must not</b> use any local variables.
     */
    private void abandonThread() throws NotInlinedPragma {
        Assert.that(state == DEAD); // should only be called by killThread()
        // @todo: actually shouldn't have any pending monitors when exiting a thread
        fixupPendingMonitors();  // Convert any pending monitors to real ones
        rescheduleNext();        // Select the next thread
        
        // Set the state related to the stack cunk atomically as ar as GC is concerned.
        // This means that we can't extend the stack. But considering that we are being called 
        // after the Thread.run() method has run and returned, there should be enough stack to 
        // complete the deregisterStackChunk() call.
        boolean oldState = GC.setAllocationEnabled(false);
        // Remove the connection between the stack chunk and this thread which will
        // indicate to the garbage collector that the stack chunk is dead
        //GC.getCollector().deregisterStackChunk(stack); // clears the owner field too.
        NativeUnsafe.setObject(stack, SC.owner, null);
        NativeUnsafe.setAddress(stack, SC.lastFP, Address.zero());
        stack = null;
        GC.setAllocationEnabled(oldState);
        VM.threadSwitch();       // and switch
    }

    /**
     * Get the 'other' thread.
     *
     * @return the other thread
     */
    static VMThread getOtherThread() {
        return otherThread;
    }

    /**
     * Get the 'other' thread.
     *
     * @return the other thread
     */
    static Object getOtherThreadStack() {
        return otherThread.stack;
    }

    /* ------------------- squawk events ---------------------*/
    /**
     * Block a thread waiting for an event.
     *
     * Note: The bulk of the work is done in this function so that there are
     * no dangling references to other threads or globals in the activation record
     * that calls reschedule().
     *
     * @param event the event number to wait for
     */
    private static void waitForEvent0(int event) {
/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//      startFinalizers();
/*end[FINALIZATION]*/
        VMThread t = currentThread;
        t.setInQueue(VMThread.Q_EVENT);
        events.put(event, t);
        Assert.that(t.nextThread == null);
    }

    /**
     * Block a thread waiting for an event.
     *
     * @param event the event number to wait for
     */
    static void waitForEvent(int event) {
        waitForEvent0(event);
        reschedule();
        Assert.that(!currentThread.inQueue(VMThread.Q_EVENT) || currentThread.nextThread == null);
    }

    /**
     * Return true if this thread is waiting for an event.
     * DIAGNOSTIC CODE: slow
     * @return
     */
    private boolean waitingforEvent() {
        return events.contains(this);
    }

    /**
     * Restart a thread blocked on an event.
     *
     * @param event the event number to unblock
     */
    private static void signalEvent(int event) {
        VMThread thread = events.findEvent(event);
        if (thread != null) {
            addToRunnableThreadsQueue(thread);
        }
    }


    /* OS events are just like squawk events, but the event IDs come from the OS and may confict with squawk event IDs
     * so we need to keep them seperate. Put a class around these two!!!!
     */

    /**
     * Block a thread waiting for an OS event.
     *
     * Note: The bulk of the work is done in this function so that there are
     * no dangling references to other threads or globals in the activation record
     * that calls reschedule().
     *
     * @param event the event number to wait for
     */
    private static void waitForOSEvent0(int event) {
/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//        startFinalizers();
/*end[FINALIZATION]*/
        VMThread t = currentThread;
        t.setInQueue(VMThread.Q_EVENT);
        osevents.put(event, t);
        Assert.that(t.nextThread == null);
    }

    /**
     * Block a thread waiting for an event.
     *
     * @param event the event number to wait for
     */
    public static void waitForOSEvent(int event) {
        waitForOSEvent0(event);
        reschedule();
        Assert.that(!currentThread.inQueue(VMThread.Q_EVENT) || currentThread.nextThread == null);
    }

    /**
     * Return true if this thread is waiting for an OS event.
     * DIAGNOSTIC CODE: slow
     * @return
     */
    private boolean waitingforOSEvent() {
        return osevents.contains(this);
    }

    /**
     * Restart a thread blocked on an event.
     *
     * @param event the event number to unblock
     */
    public static void signalOSEvent(int event) {
        if (false) {
            VM.print("signalOSEvent: ");
            VM.print(event);
            VM.println();
        }

        VMThread thread = osevents.findEvent(event);
        if (thread != null) {
            addToRunnableThreadsQueue(thread);
        } else {
            VM.print("!!! no thread found waiting on event");
        }
    }

    /**
     * Return the system errno value from the last time a native function was called.
     *
     * @return zero if no error
     */
    public int getErrno() {
        return errno;
    }

    /**
     * Store the errno after calling a blocking native function.
     * NOTE: non-blocking native functions set this in the interpreter loop as part of the NativeUnsafe.call()
     * @param newErrno
     */
    void setErrno(int newErrno) {
        errno = newErrno;
    }

    /**
     * Print message for tracing monitorEnter, Exit, etc.
     *
     * @param msg string to prefix trace with
     * @param monitor the monitor being acted on
     * @param the object that the monitor is assigned to.
     */
//    private static void traceMonitor(String msg, Monitor monitor, Object object) {
//        VM.print(msg);
//        VM.print(" thread-");
//        VM.print(currentThread.threadNumber);
//        VM.print(" on object ");
//        VM.printAddress(object);
//        VM.print(" with monitor ");
//        VM.printAddress(monitor);
//        VM.println();
//        Assert.always(object == monitor.object);
//    }
    
    /**
     * Throws an IllegalMonitorStateException.
     *
     * @param monitor the monitor being acted on
     * @param the object that the monitor is assigned to.
     */
    private static void throwIllegalMonitorStateException(Monitor monitor, Object object) {
//traceMonitor("throwIllegalMonitorStateException. Thread is not the owner of object: ", monitor, object);

        if (FATAL_MONITOR_ERRORS) {
            VM.fatalVMError();
        }
        throw new IllegalMonitorStateException("current thread (" + Thread.currentThread() + ") not owner (" + monitor.owner + ")");
    }

    /**
     * Let go of the monitor, and allow a thread waiting for the lock to become runnable.
     * Does not give the waiting thread the monitor - the waiter must try to get the lock.
     *
     * @param monitor the monitor
     * @return true if there was a thread waiting for the monitor
     */
    private static boolean releaseMonitor(Monitor monitor) {
        /*
         * Drop the lock
         */
        monitor.owner = null;
        monitor.depth = 0;
        
        /*
        * Try and remove a thread from the wait queue.
        */
        VMThread waiter = monitor.removeMonitorWait();
        if (waiter != null /*&& waiter.isAlive()*/) {       // Is this right?
            Assert.that(waiter.isAlive());
//traceMonitor("releaseMonitor make thread runnable: ", monitor, monitor.object);
//VM.print("   made runnable - thread-");
//VM.print(waiter.getThreadNumber());
//VM.println();
            /*
             * Restart execution of the thread.
             */
            addToRunnableThreadsQueue(waiter);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create real monitors for objects with pending monitors.
     */
    static void fixupPendingMonitors() {
        Assert.that(currentThread != null);
        Object object = VM.removeVirtualMonitorObject();
        while (object != null) {
            Monitor monitor = GC.getMonitor(object);
            if (monitor.owner == null) {
//traceMonitor("fixupPendingMonitors: ", monitor, object);

                Assert.that(monitor.depth == 0);
                monitor.depth = 1;
                monitor.owner = currentThread;
            } else {
//traceMonitor("fixupPendingMonitors re-locked: ", monitor, object);

                Assert.that(monitor.owner == currentThread);
                Assert.that(monitor.depth > 0 && monitor.depth < MAXDEPTH); // startup code verifies that MONITOR_CACHE_SIZE < MAXDEPTH

                monitor.depth++;
            }
            object = VM.removeVirtualMonitorObject();
        }
  //      currentThread.checkInvarients();
    }

    /**
     * Gets a monitor.
     *
     * @param object the object to be synchronized upon
     */
    static Monitor getMonitor(Object object) {
        fixupPendingMonitors();  // Convert any pending monitors to real ones
        return GC.getMonitor(object);
    }

    /**
     * After a thread fails to get a monitor, or ends a monitorWait, it must try to aquire the monitor.
     * On exit, the currentThread will be the owner of the monitor. Note that the monitor may have 
     * been been deleted and replaced since we came back from a reschedule, so get the monitor
     * from the object again.
     *
     * Only two methods actually claim ownership of a monitor. 
     *  1) monitorEnter() if monitor is un-owned
     *  2) retryMonitor() after contention in monitorEnter(), and waking up after a monitorWait().
     *
     * @param object the object with the monitor we are trying to acquire.
     * @return the current Monitor for the object.
     */
    static Monitor retryMonitor(Object object) {
        // see if we can get montitor now.
        Monitor monitor = getMonitor(object);
        while (monitor.owner != null) {
//traceMonitor("retryMonitor: Woke up without lock. retry: ", monitor, object);

            Assert.that(monitor.owner != currentThread);
            // if not, cut to the head of the queue (we've already waited our turn.)
            monitor.addMonitorWaitHead(currentThread);
            reschedule();
            currentThread.checkInvarients();
            monitor = getMonitor(object);
        }

//traceMonitor("retryMonitor: Now has the lock: ", monitor, object);
        
        Assert.that(monitor.owner == null);
        Assert.that(monitor.depth == 0);
       /*
        * Set the monitor's ownership and nesting depth.
        */
        monitor.owner = currentThread;
        monitor.depth = currentThread.monitorDepth;
        Assert.that(currentThread.monitorDepth > 0);
        
        currentThread.monitor = null;
        currentThread.monitorDepth = 0;
        return monitor;
    }
    
    /**
     * Enters a monitor.
     *
     * @param object the object to be synchronized upon
     */
    static void monitorEnter(Object object) {
        currentThread.checkInvarients();
        Monitor monitor = getMonitor(object);
        if (monitor.owner == null) {
//traceMonitor("monitorEnter:  1st lock", monitor, object);
            /*
             * Unowned monitor, make the current thread the owner.
             */
            monitor.owner = currentThread;
            monitor.depth = 1;

        } else if (monitor.owner == currentThread) {
//traceMonitor("monitorEnter:  nested lock", monitor, object);

            /*
             * Thread already owns the monitor, increment depth.
             */
            if (monitor.depth == MAXDEPTH) {
/*if[DEBUG_CODE_ENABLED]*/
                VM.println("monitorEnter:");
/*end[DEBUG_CODE_ENABLED]*/
                throw VM.getOutOfMemoryError();
            }
            monitor.depth++;
        } else {
//traceMonitor("monitorEnter: Must wait for lock: ", monitor, object);

/* 
//   if (!monitor.owner.isAlive()) {
//        VM.println("Error in monitorEnter by " + currentThread);
//        VM.println("The owner of monitor " + monitor + " is not alive " + monitor.owner);
//    } else if (runnableThreads.size() == 0) {
//         VM.println("Error in monitorEnter by " + currentThread);
//         VM.println("The owner of monitor " + monitor + " is not runnable:");
//         monitor.owner.printState();
//    }
*/
            contendedEnterCount++;
            /*
             * Add to the wait queue and set the depth for when thread is restarted.
             */
            currentThread.monitorDepth = 1;
            monitor.addMonitorWait(currentThread);
            reschedule();
            
            // Can we actually get the monitor? Try and try again.
            // Note that the Monitor may have been replaced while we were rescheduled
            monitor = retryMonitor(object);
 
 // TODO: Why need an explicit monitor? If we could get a virtual monitor now, that would be fine.
     
//traceMonitor("monitorEnter: Got lock after waiting: ", monitor, object);        

            /*
             * Safety.
             */
            currentThread.checkInvarients();
            Assert.that(currentThread.isolate.isExited() || monitor.owner == currentThread);
        }
    }

    /**
     * Exit a monitor.
     *
     * @param object the object to be unsynchronized
     */
    static void monitorExit(Object object) {
        currentThread.checkInvarients();
        Monitor monitor = getMonitor(object);

        /*
         * Throw an exception if things look bad
         */
        if (monitor.owner != currentThread) {
            throwIllegalMonitorStateException(monitor, object);
        }

        /*
         * Safety.
         */
        Assert.that(monitor.depth > 0);
//traceMonitor("monitorExit: ", monitor, object);

        /*
         * Try to restart a thread if the nesting depth is zero
         */
        if (--monitor.depth == 0) {
            if (releaseMonitor(monitor)) {
               /*
                * Let waiting thread try to execute. If a waiting thread has >= priority to current thread,
                * it will run now.
                */

                if (monitor.hasHadWaiter) {
                    addToRunnableThreadsQueue(currentThread);
                    reschedule();
                }
            } else {
/*if[SMARTMONITORS]*/
                /*
                 * Remove the monitor if it was not used for a wait() operation.
                 */
                Assert.that(monitor.owner == null && monitor.monitorQueue == null);
                if (monitor.condvarQueue == null) {
                    // TODO: Isn't condvarQueue==null enough? If there is no one currently waiting on the condvar, why keep the monitor around?
                    //       The monitorWait will retry to aquire the monitor, so it should be safe.  There may be a performance advantage to keeping monitor around though.
                    //       Consider normal wait+notify chain:
                    //       Thread A Waits:
                    //          1) A.monitorEnter()
                    //          2) A.montiorWait()
                    //          3) A.releaseMonitor()
                    //
                    //       Thread B notifies:
                    //          1) B.monitorEnter()
                    //          2) B.monitorNotify()
                    //              2.1) Remove A from monitor.condVarQueue
                    //          3) B.monitorExit()
                    //            3.1) No threads on monitor's monitorQueue OR condvarQueue.
                    //            3.2) GC.removeMonitor()
                    //          4) A.retryMonitor() // might re-allocate a monitor object???
//traceMonitor("monitorExit: GC.removeMonitor: ", monitor, object);
                    GC.removeMonitor(object, !monitor.hasHadWaiter);
                }
/*end[SMARTMONITORS]*/
            }
        }

        /*
         * Check that the monitor's depth is zero if it is not in use.
         */
        Assert.that(monitor.owner != null || monitor.condvarQueue != null || monitor.monitorQueue != null || monitor.depth == 0);
        currentThread.checkInvarients();
    }

    /**
     * Checks whether this thread's <i>interrupted status</i> has been set and if so,
     * clears it and then throws an InteruptedException.
     *
     * @throws InterruptedException if this thread's <i>interrupted status</i> is set
     */
    private void handlePendingInterrupt() throws InterruptedException {
        if (pendingInterrupt) {
            pendingInterrupt = false;
            throw new InterruptedException();
        }
    }

    /**
     * Wait for an object to be notified.
     *
     * @param object the object to wait on
     * @param delta the timeout period
     *
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     */
    public static void monitorWait(Object object, long delta) throws InterruptedException {
        VMThread theCurrentThread = VMThread.currentThread;
        theCurrentThread.checkInvarients();
        Monitor monitor = getMonitor(object);

        // Throw an exception if things look bad
        if (monitor.owner != theCurrentThread) {
            throwIllegalMonitorStateException(monitor, object);
        }

        // Was the thread interrupted?
        theCurrentThread.handlePendingInterrupt();

/*if[SMARTMONITORS]*/
        // Record that the monitor was waited upon.
        monitor.hasHadWaiter = true;
/*end[SMARTMONITORS]*/

        // Add to timer queue if time is > 0
        if (delta > 0) {
            timerQueue.add(theCurrentThread, delta);
        }

        // Save the nesting depth so it can be restored when it regains the monitor.
        theCurrentThread.monitorDepth = monitor.depth;

        // Add to the wait queue
        monitor.addCondvarWait(theCurrentThread);

        // Having relinquished the monitor, get the next thread off its wait queue.
        releaseMonitor(monitor);
//traceMonitor("monitorWait: released monitor: ", monitor, object);

        // Wait for a notify or a timeout.
        Assert.that(monitor.condvarQueue != null);
        Assert.that(theCurrentThread.monitor == monitor);
        reschedule();
        
        // OK, wait has been notified or timed out.
        // Can we actually get the monitor? Try and try again.
        // Note that the Monitor may have been replaced while we were rescheduled
        monitor = retryMonitor(object);
        
        
 // TODO: Why need an explicit monitor? If monitorDepth==1, and we could get a virtual monitor now, that would be fine.
        
//traceMonitor("monitorWait: woke up and re-locked: ", monitor, object);

        // Was the thread interrupted?
        theCurrentThread.handlePendingInterrupt();

        // Safety...
        theCurrentThread.checkInvarients();
        Assert.that(monitor.owner == theCurrentThread);
    }

    /**
     * Notify an object.
     *
     * @param object the object be notified
     * @param notifyAll flag to notify all waiting threads
     */
    public static void monitorNotify(Object object, boolean notifyAll) {

/*if[SMARTMONITORS]*/
        /*
         * If the object is on the pending monitor queue there cannot be another thread to notify.
         */
        if (VM.hasVirtualMonitorObject(object)) {
            Assert.that(!GC.hasRealMonitor(object));
//traceMonitor("monitorNotify FASTPATH: ", null, object);
            return;
        }
/*end[SMARTMONITORS]*/

        /*
         * Signal any waiting threads.
         */
        Monitor monitor = getMonitor(object);

        /*
         * Throw an exception if the object is not owned by the current thread.
         */
        if (monitor.owner != currentThread) {
            throwIllegalMonitorStateException(monitor, object);
        }

        /*
         * Try and restart a thread.
         */
        do {
            /*
             * Get the next waiting thread.
             */
            VMThread waiter = monitor.removeCondvarWait();
            if (waiter == null) {
                break;
            }
//traceMonitor("monitorNotify: ", monitor, object);
//VM.print("   notifying thread- ");
//VM.println(waiter.threadNumber);

            /*
             * Remove timeout if there was one and restart
             */
            timerQueue.remove(waiter);
            monitor.addMonitorWait(waiter);

        /*
         * Loop if it this is a notifyAll operation.
         */
        } while (notifyAll);
        
        /*
         * Don't reschedule yet. We still have the lock.
         * This current thread will eventually do a final monitorExit(), and select 
         * a waiting thread to be runnable.
         */
    }

    /**
     * Test the thread to see if it is in a queue.
     */
    final boolean inQueue(byte name) {
        return inqueue == name;
    }

    /**
     * Assert that this thread in in the expected queue.
     */
    final void checkInQueue(byte expectedQueue) throws AllowInlinedPragma {
/*if[DEBUG_CODE_ENABLED]*/
        if (inqueue != expectedQueue) {
            VM.print(this.name);
            VM.print("::setInQueue: old=");
            VM.print(inqueue);
            VM.print(" expectedQueue=");
            VM.println(expectedQueue);
             if (isServiceThread()) {
                 VM.println("is service thread");
             }
        }
/*end[DEBUG_CODE_ENABLED]*/
        Assert.that(inqueue == expectedQueue);
    }

    /**
     * Declare the thread to be in a queue.
     */
    final void setInQueue(byte name) {
//VM.print(this.name);
//VM.print("::setInQueue: inqueue=");
//VM.print(inqueue);
//VM.print(" name=");
//VM.println(name);
        Assert.that(isServiceThread() ? name == Q_NONE : true); // service thread waits for no-one?

        Assert.that(monitor == null); // monitor should only be set after setInQueue(Q_CONDVAR)
        Assert.that(inqueue == Q_NONE);

        // **** NEVER try to single step with the debugger past the next statement ****
        inqueue = name;
    }

    /**
     * Declare the thread to be not in a queue.
     */
    final void setNotInQueue(byte name) {
//VM.print(this.name);
//VM.print("::setNotInQueue: inqueue=");
//VM.print(inqueue);
//VM.print(" name=");
//VM.println(name);
        Assert.that(inqueue == name);

        // **** NEVER try to single step with the debugger past the next statement ****
        inqueue = 0;
    }

    /**
     * Interrupts this thread.
     * <p>
     * This method does nothing if the current thread is interrupting itself.
     * <p>
     * If this thread is blocked in an invocation of the {@link Object#wait()},
     * {@link Object#wait(long)}, or {@link Object#wait(long, int)} methods of the
     * class, or of the {@link Thread#join()}, {@link Thread#join(long)},
     * {@link Thread#join(long, int)}, {@link Thread#sleep(long)}, or
     * {@link Thread#sleep(long, int)} methods of this class, then its interrupt
     * status will be cleared and it will receive an {@link InterruptedException}.
     * <p>
     * If none of the previous conditions hold then this thread's interrupt status will be set.
     */
    public void interrupt() {
        if (state == ALIVE && currentThread != this) {
//VM.print("Thread::interrupt - bcount = ");
//VM.println(VM.branchCount());

            // Interrupt a join
            if (waitingToJoin != null) {

                // Remove this thread from the list of joiners
                if (waitingToJoin.joiners == this) {
                    waitingToJoin.joiners = this.nextThread;
                } else {
                    VMThread prev = waitingToJoin.joiners;
                    while (prev.nextThread != this) {
                        prev = prev.nextThread;
                        Assert.that(prev != null);
                    }
                    prev.nextThread = this.nextThread;
                }

                // Move this thread to the runnable thread queue
                waitingToJoin = null;
                this.setNotInQueue(Q_JOIN);
                addToRunnableThreadsQueue(this);
            }

            // If the thread is waiting on a monitor then remove it
            // from the conditional variable wait queue.
            Monitor thisMonitor = this.monitor;
            if (thisMonitor != null) {

                // May also be on a timer queue
                timerQueue.remove(this);

                thisMonitor.removeCondvarWait(this);
                
                // allow waiter to be runnable,
                // the waiter will have to contend for the lock.
                addToRunnableThreadsQueue(this);
            } else {
                if (inQueue(Q_TIMER)) {
                    timerQueue.remove(this);
                    this.setNotInQueue(Q_TIMER);
                    addToRunnableThreadsQueue(this);
                }
            }
            pendingInterrupt = true;
        }
    }
    
    /**
     * Answer the time in millis until another thread is runnable. Will return
     * zero if another thread is already runnable, otherwise the delta until the
     * first thread on the timer queue becomes runnable, otherwise Long.MAX_VALUE 
     * if there are no threads on the timer queue. This method takes no account of
     * events. 
     * 
     * @return time in millis
     */
    static long getTimeBeforeAnotherThreadIsRunnable() {
    	if (runnableThreads.size() > 0) {
    		return 0;
    	} else {
    		return timerQueue.nextDelta();
    	}
    }

	public static Thread[] getRunnableThreads() {
		Thread[] result = new Thread[runnableThreads.size()];
		VMThread t = runnableThreads.first;
		int i = 0;
		while (t != null) {
			result[i++] = t.getAPIThread();
			t = t.nextThread;
		}
		return result;
	}
    
//    /** debug code */
//    public static void main(String[] args) {
//        final Object lock = new Object();
//        final String lock2 = "LOCK2";
//        
//        Thread t1 = new Thread(new Runnable() {
//            public void run() {
//                synchronized (lock) {
//
//                    System.err.println("Got lock (1)! Now wait");
//                    try {
//                        lock.wait(100);
//                    } catch (InterruptedException ex) {
//                    }
//
//                }
//            }
//        }, "foo-1");
//        
//        Thread t2 = new Thread(new Runnable() {
//            public void run() {
//                synchronized (lock) {
//                    System.err.println("Got lock (2)!");
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException ex) {
//                    }
//
//                }
//            }
//        }, "foo-2");
//        
//        Thread t3 = new Thread(new Runnable() {
//            public void run() {
//                synchronized (lock) {
//                    System.err.println("Got lock(3)!");
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException ex) {
//                    }
//
//                }
//            }
//        }, "foo-3");
//
//        Thread t4 = new Thread(new Runnable() {
//            public void run() {
//                synchronized (lock2) {
//                    System.err.println("Got lock(4)!");
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException ex) {
//                    }
//
//                }
//            }
//        }, "foo-4");
//
//        synchronized (lock2) {
//            t1.start();
//            t2.start();
//            t3.start();
//            t4.start();
//
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException ex) {
//            }
//            Isolate.printAllIsolateStates(System.err);
//        }
//    }
    
} /* VMThread */

/*=======================================================================*\
 *                              ThreadQueue                              *
\*=======================================================================*/

final class ThreadQueue {

    /**
     * The first thread in the queue.
     */
    VMThread first;

    /**
     * The count of threads in the queue.
     */
    int count;

    /**
     * Add a thread to the queue.
     *
     * @param thread the thread to add
     */
    void add(VMThread thread) {
        Assert.that(thread.isAlive());
        thread.setInQueue(VMThread.Q_RUN);
        count++;
        if (first == null) {
            first = thread;
        } else {
            int thisPriority = thread.priority;
            if (first.priority < thisPriority) {
                thread.nextThread = first;
                first = thread;
            } else {
                VMThread last = first;
                while (last.nextThread != null && last.nextThread.priority >= thisPriority) {
                    last = last.nextThread;
                }
                thread.nextThread = last.nextThread;
                last.nextThread = thread;
            }
        }
    }

    /**
     * Get the number of elements in the queue.
     *
     * @return the count
     */
    int size() {
        return count;
    }

    /**
     * Get the next thread in the queue.
     *
     * Note that threads suspended by the debugger may be in (or added to) the runnable
     * queue at any time, but they will be skipped over for scheduling purposes.
     * This makes it simple for suspended threads to be notified of events, or come off of
     * timers the normal way.
     *
     * @return a thread or null if there is none
     */
    VMThread next() {
        VMThread thread = first;

/*if[ENABLE_SDA_DEBUGGER]*/
        VMThread skipped = null;
        
        // Skip over threads suspended by the debugger
        while (thread != null && thread.getDebuggerSuspendCount() != 0) {
            skipped = thread;
            thread = thread.nextThread;
        }
/*else[ENABLE_SDA_DEBUGGER]*/
//      final VMThread skipped = null;
/*end[ENABLE_SDA_DEBUGGER]*/
        
        if (VMThread.systemThreadsOnly && thread.priority <= VMThread.MAX_PRIORITY) {
            return null;
        }

        if (thread != null) {
            thread.setNotInQueue(VMThread.Q_RUN);
            if (skipped == null) {
                first = thread.nextThread;
            } else {
                skipped.nextThread = thread.nextThread;
            }
            thread.nextThread = null;
            count--;
        }
        return thread;
    }

    /**
     * Remove all the threads in this queue that are owned by <code>isolate</code>
     * and add them to the queue of hibernated runnable threads in the isolate.
     *
     * @param isolate  the isolate whose runnable threads are to be removed
     */
    void prune(Isolate isolate) {
        VMThread oldQueue = first;
        count = 0;
        first = null;
        while(oldQueue != null) {
            VMThread thread = oldQueue;
            oldQueue = oldQueue.nextThread;
            thread.nextThread = null;
            thread.setNotInQueue(VMThread.Q_RUN);
            if (thread.getIsolate() != isolate) {
                add(thread);
            } else {
                thread.setInQueue(VMThread.Q_HIBERNATEDRUN);
                isolate.addToHibernatedRunThread(thread);
            }
        }
    }
} /* ThreadQueue */


/*=======================================================================*\
 *                               TimerQueue                              *
\*=======================================================================*/

final class TimerQueue {

    /**
     * The first thread in the queue.
     */
    VMThread first;

    /**
     * Add a thread to the queue.
     *
     * @param thread the thread to add
     * @param delta the time period
     */
    void add(VMThread thread, long delta) {
        Assert.that(thread.nextTimerThread == null);
        long time = VM.getTimeMillis() + delta;
        if (time < 0) {

           /*
            * If delta is so huge that the time went negative then just make
            * it a very large value. The universe will end before the error
            * can be detected.
            */
            time = Long.MAX_VALUE;
        }
        thread.time = time;
        if (first == null) {
            first = thread;
        } else {
            if (first.time > time) {
                thread.nextTimerThread = first;
                first = thread;
            } else {
                VMThread last = first;
                while (last.nextTimerThread != null && last.nextTimerThread.time < time) {
                    last = last.nextTimerThread;
                }
                thread.nextTimerThread = last.nextTimerThread;
                last.nextTimerThread = thread;
            }
        }
    }

    /**
     * Get the next thread in the queue that has reached its time.
     *
     * @return a thread or null if there is none
     */
    VMThread next() {
        VMThread thread = first;
        if (thread == null || thread.time > VM.getTimeMillis()) {
            return null;
        }
        first = first.nextTimerThread;
        thread.nextTimerThread = null;
        Assert.that(thread.time != 0);
        thread.time = 0;
        return thread;
    }

    /**
     * Remove a specific thread from the queue.
     *
     * @param thread the thread
     */
    void remove(VMThread thread) {
        if (first == null) {
            Assert.that(thread.time == 0);
            return;
        }
        if (thread.time == 0) {
            return;
        }
        thread.time = 0;
        if (thread == first) {
            first = thread.nextTimerThread;
            thread.nextTimerThread = null;
            return;
        }
        VMThread p = first;
        while (p.nextTimerThread != null) {
            if (p.nextTimerThread == thread) {
                p.nextTimerThread = thread.nextTimerThread;
                thread.nextTimerThread = null;
                return;
            }
            p = p.nextTimerThread;
        }
        VM.fatalVMError();
    }

    /**
     * Get the time delta to the next event in the queue.
     *
     * @return the time
     */
    long nextDelta() {
        if (first != null) {
            long now = VM.getTimeMillis();
            if (now >= first.time) {
                return 0;
            }
            if (first.time == Long.MAX_VALUE) {
                return first.time; // wait "forever"
            }
            return first.time - now;
        } else {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Remove all the threads in this queue that are owned by <code>isolate</code>
     * and add them to the queue of hibernated timer-blocked threads in the isolate.
     *
     * @param isolate  the isolate whose timer-blocked threads are to be removed
     */
    void prune(Isolate isolate) {
        start:
        while (true) {
            VMThread t = first;
            while (t != null) {
                if (t.getIsolate() == isolate) {
                    long time = t.time - VM.getTimeMillis();
                    remove(t);
                    t.time = time;
                    isolate.addToHibernatedTimerThread(t);
                    continue start;
                }
                t = t.nextTimerThread;
            }
            break;
        }
    }

    /**
     * Adjust the times of all threads in the timer queue. Note that this does not reorder the threads in the timer queue.
     * @param deltaT ms (must be negative).
     */
    void adjustWaits(long deltaT) {
        Assert.that(deltaT < 0);
        VMThread thread = first;
        while (thread != null) {
            long time = thread.time;
            if (time != Long.MAX_VALUE) { // if not "wait forever"
                time = time + deltaT;
                if (time < 0) {
                    /*
                     * If the new time is much, much earlier than the old time, delta will be large negative number.
                     * Set new wakeup time to the past, so it will wake up the thread as soon as possible.
                     */
                    time = 0;
                }
                thread.time = time;
            }
            thread = thread.nextTimerThread;
        }
    }

} /* TimerQueue */

/**
 * Extension of IntHashtable that enables the pruning the threads of hibernated isolates.
 */
final class EventHashtable extends IntHashtable implements IntHashtableVisitor {

    /**
     * The isolate being pruned.
     */
    private transient Isolate isolate;

    /**
     * Prune the isolates out of the event hash table.
     *
     * @param isolate the isolate remove
     */
    void prune(Isolate isolate) {
        this.isolate = isolate;
        visit(this);
        this.isolate = null;
    }

    /**
     * Visit method for EventHashtable visitor.
     *
     * @param key the key
     * @param value the value
     */
    public void visitIntHashtable(int key, Object value) {
        VMThread t = (VMThread)value;
        if (t.getIsolate() == isolate) {
            VMThread t2 = findEvent(key); // removes event from table
            Assert.that(t == t2);
            Assert.that(t.nextThread == null);
            t.setInQueue(VMThread.Q_HIBERNATEDRUN);
            isolate.addToHibernatedRunThread(t);
        }
    }
    
    /**
     * Finds and removes a thread blocked on an event.
     *
     * @param event the event number to unblock
     */
    VMThread findEvent(int event) {
        VMThread thread = (VMThread)remove(event);
        if (thread != null) {
            thread.setNotInQueue(VMThread.Q_EVENT);
            Assert.that(thread.nextThread == null);
        }
        return thread;
    }
} /* EventHashtable */
