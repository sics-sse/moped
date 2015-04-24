//if[!PLATFORM_TYPE_BARE_METAL]
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

package com.sun.cldc.jna;

import com.sun.squawk.Address;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;
import java.util.Stack;

/**
 * A TaskExecutor is a native thread that can be used to run native functions.
 */
public class TaskExecutor {
    public final static int TASK_PRIORITY_HI = 1;
    public final static int TASK_PRIORITY_MED = 2;
    public final static int TASK_PRIORITY_LOW = 3;

    public final static int MAX_NUM_CACHABLE_TASK_EXECUTORS = 8;
    
    private static int cachedTECount;

    protected Address te;


    private TaskExecutor() {
        te = Address.zero();
    }

    /**
     * Calls using this singleton class will use the default TaskExecutor behavior.
     */
    private static class DefaultTaskExecutor extends TaskExecutor {

        DefaultTaskExecutor() {
            super("cached TaskExecutor " + (cachedTECount++));
        }

        Address runBlockingFunction(Address fptr,
                int arg1, int arg2, int arg3, int arg4, int arg5,
                int arg6, int arg7, int arg8, int arg9, int arg10) {
//VM.println("DefaultTaskExecutor.runBlockingFunction()");
            checkTaskExecutor();
            return NativeUnsafe.runBlockingFunctionOn(te, fptr, arg1, arg2, arg3, arg4, arg5,
                    arg6, arg7, arg8, arg9, arg10);
        }

        /**
         * When done with this anonymous TE, either cache it for later or kill it.
         */
        void cleanup() {
            Stack cache = VM.getTaskCache();
            boolean kill = false;
            synchronized (cache) {
                if (cache.size() > MAX_NUM_CACHABLE_TASK_EXECUTORS) {
                    kill = true;
                } else {
                    cache.push(this);
                }
            }
            if (kill) {
                stopTaskExecutor();
            }
        }
    }

    /**
     * Create a native thread with the given name, priority and stack size.
     *
     * @param name name may be passed on to the native thread
     * @param priority
     * @param stacksize zero is default
     */
    public TaskExecutor(String name, int priority, int stacksize) {
        if (priority < TASK_PRIORITY_HI || priority > TASK_PRIORITY_LOW) {
            throw new IllegalArgumentException("priority");
        }
        if (stacksize < 0) {
            throw new IllegalArgumentException("stacksize");
        }

        Pointer name0 = Pointer.NULL();
        if (name != null) {
            name0 = Pointer.createStringBuffer(name);
        }
        te = NativeUnsafe.createTaskExecutor(name0.address(), priority, stacksize);
    }

   /**
     * Create a natve thread with the given name, default priority and default stack size.
     *
     * @param name name may be passed on to the native thread
     */
    public TaskExecutor(String name) {
        this(name, TASK_PRIORITY_MED, 0);
    }

    /**
     * Look for a cached TaskExecutor or create a new one.
     * @return an anonymous TaskExecutor
     */
    static TaskExecutor getCachedTaskExecutor() {
        TaskExecutor te = null;
        Stack cache = VM.getTaskCache();
        synchronized (cache) {
            if (!cache.isEmpty()) {
                te = (TaskExecutor) cache.pop();
            }
        }
        if (te == null) {
            te = new DefaultTaskExecutor();
        }
        te.checkTaskExecutor();
        return te;
    }

    /**
     * Clean up after a call
     */
    void cleanup() {
    }

    /**
     * Tell TaskExecutor to stop running new NativeTasks.
     * Any NativeTasks that were pending on the TaskExecutor's run queue
     * will be signaled as completing with an error.
     */
    public void cancelTaskExecutor() {
        checkTaskExecutor();
        NativeUnsafe.cancelTaskExecutor(te);
    }

    /**
     * Delete the native resources behind this TaskExecutor.
     * Must call cancelTaskExecutor() first. If the TaskExecutor hasn't finished cleaning up,
     * return error code.
     * 
     * @return zero on success
     */
    public int deleteTaskExecutor() {
        checkTaskExecutor();
        int result = NativeUnsafe.deleteTaskExecutor(te);
        if (result == 0) {
            te = Address.zero();
        }
        return result;
    }

    final void checkTaskExecutor() {
        if (te.isZero()) {
            throw new IllegalStateException("TaskExecutor has been closed");
        }
    }

    Address runBlockingFunction(Address fptr,
            int arg1, int arg2, int arg3, int arg4, int arg5,
            int arg6, int arg7, int arg8, int arg9, int arg10) {
//VM.println("TaskExecutor.runBlockingFunction()");
        checkTaskExecutor();
        return NativeUnsafe.runBlockingFunctionOn(te, fptr, arg1, arg2, arg3, arg4, arg5,
                                                  arg6, arg7, arg8, arg9, arg10);
    }

    /**
     * Cancel the TaskExecutor and delete when TaskExecutor done.
     */
    public void stopTaskExecutor() {
        cancelTaskExecutor();
        while (deleteTaskExecutor() != 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
    }

}
