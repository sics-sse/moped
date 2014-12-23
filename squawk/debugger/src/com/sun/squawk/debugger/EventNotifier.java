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

package com.sun.squawk.debugger;

import java.io.*;

import com.sun.squawk.*;
import com.sun.squawk.debugger.DataType.*;
import com.sun.squawk.util.*;

/**
 * An <code>EventNotifier</code> is used to synchronize communication between
 * a thread producing a JDWP event and consumer of such events. It also
 * encapsulates the details of the event as well as the JDWP identifier
 * of the thread in which the event occurred.
 *
 */
public final class EventNotifier {

    /**
     * An EventConsumer consumes events delivered via an EventNotifier.
     */
    public static interface Consumer {

        /**
         * Determines if this consumer is still interested in consuming events.
         *
         * @return trus if this consumer in no longer consuming events
         */
        public boolean isDone();

        /**
         * Consumes an event.
         *
         * @param event  the event
         * @throws IOException if an IO error occurs
         */
        public void consumeEvent(Debugger.Event event) throws IOException;
    }

    /**
     * This is the one-element queue of events being signalled. 
     */
    private Debugger.Event event;

    /**
     * Called by an event producer to notify a waiting consumer of an event.
     * This method will be called on the producer's thread that caused
     * the event. This thread will be blocked until a consumer has
     * consumed the event.
     *
     * @param event     the event being reported
     * @param consumer  the consumer of the event
     */
    synchronized public void produceEvent(Debugger.Event evt, Consumer consumer) {
        Assert.that(evt.getThread() != null && evt.getThreadID() != null);
        
        if (Log.verbose()) {
            Log.log("Event producer (A): event produced: " + evt);
        }

        // WAIT (A):
        // Block until the buffer is empty
        while (event != null) {
            if (consumer.isDone()) {
                notifyAll();
                return;
            }
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        event = evt;

        if (Log.DEBUG_ENABLED && Log.debug()) {
            Log.log("Event producer (B): notifying consumers: " + evt);
        }

        // Notify all threads blocked on this notifier. Any other producer threads will be
        // blocked in the while loop above and so a waiting consumer thread will eventually
        // be given a chance to run.
        notifyAll();

        if (Log.DEBUG_ENABLED && Log.debug()) {
            Log.log("Event producer (C): waiting for event consumer to finish: " + evt);
        }

        // WAIT (B):
        // Wait until a consumer thread consumes this event.
        // Note that the consumer might suspendForDebugger() this thread.
        // We may be suspended for a long time, and some other event may be in the buffer now.
        while (event == evt) {
            if (consumer.isDone()) {
                notifyAll();
                return;
            }
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        // Event has been handled, and the thread has been resumed.
        if (Log.DEBUG_ENABLED && Log.debug()) {
            Log.log("Event producer (D): resuming after: " + evt);
        }
    }

    /**
     * Consumes an event produced by a producer. The current thread will block until an
     * event is available for consumption. Once the given consumer has consumed the
     * event, the producer of the event is awakened.
     *
     * @param consumer   the object that will consume the event
     */
    public synchronized void consumeEvent(Consumer consumer) {
        // WAIT (C):
        // Wait for a producer to produce an event
        if (Log.DEBUG_ENABLED && Log.debug()) {
            Log.log("Event consumer (A): waiting for event");
        }
        
        while (event == null) {
            if (consumer.isDone()) {
                event = null;
                // Notify all waiting producers so that eventually the producer of the
                // event just consumed will relinquish this notifier object and allow
                // other producers to produce events.
                notifyAll();
                return;
            }
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        if (Log.DEBUG_ENABLED && Log.debug()) {
            Log.log("Event consumer (B): got event: " + event);
        }
        try {
            // Note that we can't release the event buffer, allowing the producer to continue, until
            // the consumer consumes the event, which may decide to suspend (forDebugger) the producer.
            consumer.consumeEvent(event);
        } catch (IOException e) {
            if (Log.info()) {
                Log.log("IO error while notifying debugger of " + event + ": " + e);
            }
        }
        
        // Notify all waiting producers so that eventually the producer of the
        // event just consumed will relinquish this notifier object and allow
        // other producers to produce events.
        event = null;
        notifyAll();
    }
}

