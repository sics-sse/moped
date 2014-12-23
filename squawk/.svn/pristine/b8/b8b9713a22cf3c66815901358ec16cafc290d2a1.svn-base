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
 * <code>CrossIsolateThread</code> is a package-private thread class that is allowed to be created in one isolate, but execute in the context of another isolate.
 *
 * The system uses this class to start the initial thread in an isolate, and to
 * defer execution of callback handling threads. The class is simply used as a marker that it's OK to do this.
 *
 * NOTE: This class may revert to squawk-private again in the future.
 */
public class CrossIsolateThread extends Thread {

    /**
     * Allocates a new <code>CrossIsolateThread</code> object
     * to be run in the given isolate's context, with the given name.<p>
     *
     * Note that you must override CrossIsolateThread.run() if you want to do anything other than
     * run the Isolate's run() method.
     *
     * @param   target   the isolate whose <code>run</code> method is called.
     * @param   name     the name of the new thread.
     */
    public CrossIsolateThread(Isolate target, String name) {
        super(target, name);
    }

    // just a normal thread, move along...
}
