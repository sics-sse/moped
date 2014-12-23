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

package java.lang;

import com.sun.squawk.*;

/**
 * A <i>thread</i> is a thread of execution in a program. The Java
 * Virtual Machine allows an application to have multiple threads of
 * execution running concurrently.
 * <p>
 * Every thread has a priority. Threads with higher priority are
 * executed in preference to threads with lower priority.
 * <p>
 * There are two ways to create a new thread of execution. One is to
 * declare a class to be a subclass of <code>Thread</code>. This
 * subclass should override the <code>run</code> method of class
 * <code>Thread</code>. An instance of the subclass can then be
 * allocated and started. For example, a thread that computes primes
 * larger than a stated value could be written as follows:
 * <p><hr><blockquote><pre>
 *     class PrimeThread extends Thread {
 *         long minPrime;
 *         PrimeThread(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <p><blockquote><pre>
 *     PrimeThread p = new PrimeThread(143);
 *     p.start();
 * </pre></blockquote>
 * <p>
 * The other way to create a thread is to declare a class that
 * implements the <code>Runnable</code> interface. That class then
 * implements the <code>run</code> method. An instance of the class can
 * then be allocated, passed as an argument when creating
 * <code>Thread</code>, and started. The same example in this other
 * style looks like the following:
 * <p><hr><blockquote><pre>
 *     class PrimeRun implements Runnable {
 *         long minPrime;
 *         PrimeRun(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <p><blockquote><pre>
 *     PrimeRun p = new PrimeRun(143);
 *     new Thread(p).start();
 * </pre></blockquote>
 * <p>
 *
 *
 * @see     java.lang.Runnable
 * @see     java.lang.Runtime#exit(int)
 * @see     java.lang.Thread#run()
 * @since   JDK1.0
 */
public class Thread implements Runnable {

    private final VMThread vmThread;

    /**
     * The target to run (if run() is not overridden).
     */
    private Runnable target;

    /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = VMThread.MIN_PRIORITY;

   /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = VMThread.NORM_PRIORITY;

    /**
     * The maximum priority that a thread can have.
     */
    public final static int MAX_PRIORITY = VMThread.MAX_PRIORITY;

    /**
     * Returns a reference to the currently executing thread object.
     *
     * @return  the currently executing thread.
     */
    public static Thread currentThread() {
        return VMThread.currentThread().getAPIThread();
    }

    /**
     * Causes the currently executing thread object to temporarily pause
     * and allow other threads to execute.
     */
    public static void yield() {
        VMThread.yield();
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
        VMThread.sleep(millis);
    }

    /**
     * Allocates a new <code>Thread</code> object.
     * <p>
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.
     *
     * @see     java.lang.Runnable
     */
    public Thread() {
        vmThread = new VMThread(this, null);
    }

    /**
     * Allocates a new <code>Thread</code> object with a
     * specific target object whose <code>run</code> method
     * is called.
     *
     * @param   target   the object whose <code>run</code> method is called.
     */
    public Thread(Runnable target) {
        this.target = target;
        vmThread = new VMThread(this, null);
    }

    /**
     * Allocates a new <code>Thread</code> object with the given
     * target and name.
     *
     * @param   target   the object whose <code>run</code> method is called.
     * @param   name     the name of the new thread.
     */
    public Thread(Runnable target, String name) {
        this.target = target;
        vmThread = new VMThread(this, name);
    }

    /**
     * Allocates a new <code>Thread</code> object with the
     * given name.
     *
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.
     *
     * @param   name   the name of the new thread.
     */
    public Thread(String name) {
        vmThread = new VMThread(this, name);
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
     * @exception  IllegalThreadStateException  if the thread was already
     *               started.
     * @see        java.lang.Thread#run()
     */
    public synchronized void start() {
        vmThread.start();
    }

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see     java.lang.Thread#start()
     * @see     java.lang.Runnable#run()
     */
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    /**
     * Interrupts this thread.
     * <p>
     * This method does nothing if the current thread is interrupting itself.
     * <p>
     * If this thread is blocked in an invocation of the {@link Object#wait() wait()},
     * {@link Object#wait(long) wait(long)}, or {@link Object#wait(long, int) wait(long, int)}
     * methods of the {@link Object} class or of the {@link Thread#join()} or {@link Thread#sleep(long)}
     * methods of this class, then its interrupt
     * status will be cleared and it will receive an {@link InterruptedException}.
     * <p>
     * If none of the previous conditions hold then this thread's interrupt status will be set.
     * <p>
     * In an implementation conforming to the CLDC Specification, this operation
     * is not required to cancel or clean up any pending I/O operations that the
     * thread may be waiting for.
     *
     * @since JDK 1.0, CLDC 1.1
     */
    public void interrupt() {
        vmThread.interrupt();
    }

    /**
     * Tests if this thread is alive. A thread is alive if it has
     * been started and has not yet died.
     *
     * @return  <code>true</code> if this thread is alive;
     *          <code>false</code> otherwise.
     */
    public final boolean isAlive() {
        return vmThread.isAlive();
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
        vmThread.setPriority(newPriority);
    }

    /**
     * Returns this thread's priority.
     *
     * @return  this thread's name.
     * @see     #setPriority
     * @see     java.lang.Thread#setPriority(int)
     */
    public final int getPriority() {
        return vmThread.getPriority();
    }

    /**
     * Returns this thread's name.  Note that in CLDC the name
     * of the thread can only be set when creating the thread.
     *
     * @return  this thread's name.
     */
    public final String getName() {
        return vmThread.getName();
    }

    /**
     * Returns the current number of active threads in the VM.
     *
     * @return  the current number of threads in this thread's thread group.
     */
    public static int activeCount() {
        return VMThread.activeCount();
    }

    /**
     * Waits for this thread to die.
     *
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     */
    public final void join() throws InterruptedException {
        vmThread.join();
    }

    /**
     * Returns a string representation of this thread, including a unique number
     * that identifies the thread and the thread's priority.
     *
     * @return  a string representation of this thread.
     */
    public String toString() {
        return vmThread.toString();
    }
}
