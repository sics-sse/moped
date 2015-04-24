//if[FINALIZATION]
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

/**
 * Class to record objects that require finalization.
 *
 */
public final class Finalizer implements Runnable {

    /**
     * The object requiring finalization.
     */
    private Object object;

    /**
     * The isolate of the thread that created the object.
     */
    private Isolate isolate;

    /**
     * Pointer to next finalizer in the garbage collector or isolate queue.
     */
    private Finalizer next;

    /**
     * A flag used by the Lisp2Collector to determine which queue a finalizer should be
     * put on after a collection. This collector cannot manipulate the queue during collection
     * as that would set invalid bits in the write barrier/marking bit map.
     */
    private boolean referenced;

    /**
     * Constructor.
     *
     * @param object the object that needs finalization
     */
    Finalizer(Object object) {
        this.object  = object;
        this.isolate = VM.getCurrentIsolate();
    }

    /**
     * Get the object.
     *
     * @return the object.
     */
    Object getObject() {
        return object;
    }

    /**
     * Set the next finalizer.
     *
     * @param nextFinalizer the finalizer
     */
    void setNext(Finalizer nextFinalizer) {
        next = nextFinalizer;
    }

    /**
     * Get the next finalizer.
     *
     * @return the next finalizer.
     */
    Finalizer getNext() {
        return next;
    }

    /**
     * Get the isolate.
     *
     * @return the isolate.
     */
    Isolate getIsolate() {
        return isolate;
    }

    /**
     * Queue the finalizer onto the isolate for execution.
     */
    void queueToIsolate() {
        isolate.addFinalizer(this);
    }

    /**
     * Determines if the last execution of the garbage collector determined that there were
     * no more references to the object associated with this finalizer.
     *
     * @return  true if there was at least reference to the object associated with this finalizer
     */
    boolean isReferenced() {
        return referenced;
    }

    /**
     * Sets or unsets the flag indicating that the last execution of the garbage collector determined that there were
     * no more references to the object associated with this finalizer.
     *
     * @param  flag  the new value of the flag
     */
    void setReferenced(boolean flag) {
        referenced = flag;
    }


    /**
     * Run the finalzer.
     */
    public void run() {
        try {
            VM.finalize(object);
        } catch(Throwable ex) {
        }
    }
}


