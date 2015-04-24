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
import java.util.*;

import com.sun.squawk.*;
import com.sun.squawk.debugger.DataType.*;

/**
 * The <code>EventManager</code> class manages requests from a remote
 * entity that wants to be notified when certain events occur. The
 * event manager registers the requests and co-ordinates notifications
 * when an event occurs for which there may be a matching request.
 *
 */
public abstract class EventManager implements Runnable, EventNotifier.Consumer {

    /**
     * The object that is the conduit for events sent from an event producer.
     */
    protected final EventNotifier notifier = new EventNotifier();

    /**
     * The set of registered event requests.
     */
    protected Vector requests = new Vector();

    private boolean done;

    private final EventRequestModifier.Matcher matcher;

    protected EventManager(EventRequestModifier.Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Main event handling loop of the event manager.
     */
    public void run() {
        while (!done) {
            notifier.consumeEvent(this);
        }

        // Clear all pending events
        clear(0, 0);

        // Wake up any app threads blocked on the notifier
        synchronized (notifier) {
            notifier.notifyAll();
        }

        if (Log.info()) {
            Log.log("Completed shutdown");
        }
    }

    public boolean quit() {
        if (!done) {
            done = true;

            if (Log.info()) {
                Log.log("Initiating shutdown of event manager...");
            }

            // Wake up any threads waiting on the notifier
            synchronized (notifier) {
                notifier.notifyAll();
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Registers an event request with this manager.
     *
     * @param request  the event request to add
     */
    public void register(EventRequest request) {
        requests.addElement(request);
        request.registered();
        if (Log.info()) {
            Log.log("Registered handler for event: " + request);
        }
        if (Log.DEBUG_ENABLED && Log.debug()) {
            logRegisteredRequests();
        }
    }

    /**
     * Removes some event request(s) from this manager.
     *
     * @param eventKind the <code>JDWP.EventKind</code> of the request(s) to remove or 0 to remove all requests
     * @param requestID   the ID of the request to remove or 0 to remove all requests of <code>eventKind</code>
     * @return true if at least one request was removed, false otherwise
     */
    public boolean clear(int eventKind, int requestID) {
        Vector kept = null;
        Vector cleared = null;

        for (Enumeration e = requests.elements(); e.hasMoreElements(); ) {
            EventRequest request = (EventRequest)e.nextElement();
            if (requestID == 0 ? (eventKind != 0 && request.kind != eventKind) : (request.id != requestID)) {
                if (kept == null) {
                    kept = new Vector(requests.size());
                }
                kept.addElement(request);
            } else {
                if (cleared == null) {
                    cleared = new Vector();
                }
                cleared.addElement(request);
            }
        }
        if (kept != null) {
            requests = kept;
        } else {
            requests.removeAllElements();
        }

        if (cleared != null) {
            for (Enumeration e = cleared.elements(); e.hasMoreElements(); ) {
                EventRequest request = (EventRequest) e.nextElement();
                if (Log.info()) {
                    Log.log("Clearing event request: " + request);
                }
                request.cleared();
            }
        }
        if (Log.DEBUG_ENABLED && Log.debug()) {
            logRegisteredRequests();
        }
        return cleared != null;
    }

    /**
     * Returns an enumeration of all the event requests of a given kind.
     *
     * @param eventKind  the <code>JDWP.EventKind</code> of events to retrieve
     * @return an enumeration of all registered events whose kind matches <code>eventKind</code>
     */
    public Enumeration getEventsOfKind(int eventKind) {
        Vector matchingEvents = new Vector();
        for (Enumeration e = requests.elements(); e.hasMoreElements(); ) {
            EventRequest request = (EventRequest) e.nextElement();
            if (request.kind == eventKind) {
                matchingEvents.addElement(request);
            }
        }
        return matchingEvents.elements();
    }

    /**
     * @param event 
     * @see EventNotifier#produceEvent
     */
    public final void produceEvent(Debugger.Event event) {
        notifier.produceEvent(event, this);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isDone() {
        return done;
    }

    /**
     * Summary of the requests that matched given event.
     */
    public static class MatchedRequests {
        public final Vector requests;
        public final boolean vmDeath;
        public final int suspendPolicy;
        public MatchedRequests(Vector requests, boolean vmDeath, int suspendPolicy) {
            this.requests = requests;
            this.vmDeath = vmDeath;
            this.suspendPolicy = suspendPolicy;
        }
    }

    /**
     * Finds the registered requests that match a given event.
     * @param event to match against requests
     * @return set of matching event requsts, or null
     */
    public MatchedRequests matchRequests(Debugger.Event event) {
        Vector matchedRequests = null;
        int suspendPolicy = JDWP.SuspendPolicy_NONE;

        // find matching events:
        boolean vmDeath = false;
        for (Enumeration e = requests.elements(); e.hasMoreElements(); ) {
            EventRequest request = (EventRequest) e.nextElement();
            if (Log.DEBUG_ENABLED && Log.debug()) {
                Log.log("Testing event request for match: " + request);
            }
            if (request.matchKind(event.kind) && request.matchModifiers(matcher, event)) {
                if (Log.verbose()) {
                    Log.log("Matched event request: " + request);
                }
                if (matchedRequests == null) {
                    matchedRequests = new Vector();
                }
                matchedRequests.addElement(request);
                if (request.kind == JDWP.EventKind_VM_DEATH) {
                    vmDeath = true;
                }

                if (request.suspendPolicy > suspendPolicy) {
                    suspendPolicy = request.suspendPolicy;
                }
            }
        }
        return new MatchedRequests(matchedRequests, vmDeath, suspendPolicy);
    }

    /**
     * Consumes an event from an event producer.
     *
     * @param event    the details of the event
     * @throws IOException if an IO error occurs
     */
    public void consumeEvent(Debugger.Event event) throws IOException {

        if (done) {
            // Cut event handling short if the event manager has been told to shutdown
            return;
        }

        if (Log.info()) {
            Log.log("Got event: " + event);
        }

        // Find the matched requests, if any
        MatchedRequests mr = matchRequests(event);

        // construct composite event
        if (mr.requests != null) {
            try {
                send(event, mr);
            } catch (SDWPException e) {
                if (Log.info()) {
                    Log.log("Error while notifying debugger of " + event + ": " + e);
                }
            }
        } else {
            if (Log.info()) {
                Log.log("No matching event request found");
            }
        }

        if (mr.vmDeath) {
            quit();
        }
    }

    public void logRegisteredRequests() {
/*if[DEBUG_CODE_ENABLED]*/
        Log.log("Registered event requests [count=" + requests.size() + "]: ");

        for (Enumeration e = requests.elements(); e.hasMoreElements(); ) {
            EventRequest request = (EventRequest) e.nextElement();
            Log.log("    " + request);
        }
/*end[DEBUG_CODE_ENABLED]*/
    }

    public abstract void send(Debugger.Event event, MatchedRequests mr) throws IOException, SDWPException;
    
    /**
     * Events handled by proxy are odd, and those by the agent are even.
     * @param id the id to check
     * @return true if the event request was handled locally
     */
    public abstract boolean isMyEventRequestID(int id);
    
}
