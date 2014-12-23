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

package com.sun.squawk.vm;

import java.util.ArrayList;

/**
 * Sentinal object used when waiting for events.
 */
public class EventQueue {

    /**
     * Pending events list.
     */
    private static final ArrayList<Integer> events = new ArrayList<Integer>();

    /**
     * Next event number.
     */
    private static int nextEventNumber = 1;

    /**
     * Flag to control waiting. We could also have done this by posting
     * an event to the queue, but this saves on eventNumbers and really is
     * a spurious poking of the wait-queue.
     */
    private static boolean noWaiting = false;

    /*
     * waitFor
     */
    static void waitFor(long time) {
        if (ChannelIO.TRACING_ENABLED) ChannelIO.trace("++waitFor "+time);
        synchronized(events) {
            if (events.isEmpty() && noWaiting != true) {
                try { events.wait(time); } catch(InterruptedException ex) {}
            }
            noWaiting = false;
        }
        if (ChannelIO.TRACING_ENABLED) ChannelIO.trace("--waitFor");
    }

    /*
     * sendNotify
     */
    static void sendNotify() {
        if (ChannelIO.TRACING_ENABLED) ChannelIO.trace("++sendNotify");
        synchronized(events) {
            noWaiting = true;
            events.notifyAll();
        }
        if (ChannelIO.TRACING_ENABLED) ChannelIO.trace("--sendNotify");
    }

    /*
     * getNextEventNumber
     */
    static int getNextEventNumber() {
        if (nextEventNumber >= Integer.MAX_VALUE-1) {
            System.err.println("Reached event number limit"); // TEMP -- Need a way to recycle event numbers
            System.exit(0);
        }
        /*
         * Make sure all event numbers are odd so they will not be the same as
         * the ones that are allocated in the message system.
         */
        if ((nextEventNumber % 2) == 0) {
            ++nextEventNumber;
        }
        return nextEventNumber++;
    }

    /*
     * unblock
     */
    static void unblock(int event) {
        synchronized(events) {
            events.add(new Integer(event));
            events.notifyAll();
        }
        if (ChannelIO.TRACING_ENABLED) ChannelIO.trace("++unblock ");
    }

    /*
     * getEvent
     */
    static int getEvent() {
        int res = 0;
        synchronized(events) {
            if (events.size() > 0) {
                Integer event = (Integer)events.remove(0);
                res = event.intValue();
            }
        }
        if (ChannelIO.TRACING_ENABLED) ChannelIO.trace("++getEvent = "+res);
        return res;
    }

    private EventQueue() {
    }

}


