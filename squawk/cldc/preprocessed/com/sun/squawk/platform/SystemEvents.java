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

package com.sun.squawk.platform;

/**
 * Abstract class that handles waiting for OS events like waiting for socket input.
 * typically handled by calling select() or poll().
 *
 */
public abstract class SystemEvents implements Runnable {

    /**
     * Wait for an OS event, with a timeout. Signal VMThread when event occurs.
     *
     * Try not to allocate if there are no events...
     * @param timout in ms
     */
    protected abstract void waitForEvents(long timout);

    public abstract void waitForReadEvent(int fd);

    public abstract void waitForWriteEvent(int fd);

    protected SystemEvents() {
    }

    public abstract void startIO();

    public abstract void run();

    public abstract void cancelIOHandler();

    public abstract void setMaxWait(long max);
}
