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

package com.sun.squawk.debugger.sdp;

import java.io.*;

import com.sun.squawk.debugger.*;
import com.sun.squawk.debugger.DataType.*;
import com.sun.squawk.util.*;
import java.util.*;
import com.sun.squawk.*;

/**
 * A SDAListener implements the Squawk Debugger Proxy side of the JDWP protocol
 * and communicates with a (SDA) Squawk Debugger Agent running in a Squawk VM.
 *
 */
final class SDAListener extends JDWPListener {

    /**
     * The Squawk Debugger Proxy that owns this listener.
     */
    private final SDP sdp;

    /**
     * The debuggee's isolate name
     */
    private String isolateName;

    /**
     * The JDWP command set handlers.
     */
    private final IntHashtable commandSets = new IntHashtable();
    
    private Event eventHandler;
    
    private boolean forwarderStarted = false;
   
    /** 
     * Start forwarding events to debugger.
     * Called by JDBListener when it's safe.
     */
    public void enableForwardedEvents() {
        if (!forwarderStarted) {
            eventHandler.enableForwardedEvents();
            forwarderStarted = true;
        }
    }
     
    public SDAListener(SDP sdp) {
        this.sdp = sdp;
        this.eventHandler = new Event(this);
        commandSets.put(JDWP.Event_COMMAND_SET,       eventHandler);
        commandSets.put(SDWP.SquawkVM_COMMAND_SET,    new SquawkVM(eventHandler));
    }

    public Event getEventHandler() {
        return eventHandler;
    }
    
    /**
     * @return the name of the isolate being debugger
     */
    public String getIsolateName() {
        return isolateName;
    }
    
    /**
     * {@inheritDoc}
     */
    public void processCommand(CommandPacket command) throws IOException {
        try {
            SDPCommandSet handler = (SDPCommandSet) commandSets.get(command.set());
            if (handler == null || !handler.handle(sdp, this, otherHost, command)) {
                System.err.println("Unrecognized command: " + command);
            }
        } catch (IOException e) {
            System.err.println(command + " caused: " + e);
            if (command.needsReply()) {
                ReplyPacket reply = command.createReply(JDWP.Error_INTERNAL);
                sendReply(reply);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String sourceName() {
        return "SquawkVM";
    }

    /*-----------------------------------------------------------------------*\
     *                         Event Command Set (64)                        *
    \*-----------------------------------------------------------------------*/

    /**
     * Implements <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdwp/jdwp-protocol.html#JDWP_Event">Event</a>.
     */
    static final class Event extends SDPCommandSet {
        
        final SDAListener sdaListener;
        
        Event(SDAListener sdaListener) {
            this.sdaListener = sdaListener;
        }

        private boolean vmDeath;

        /**
         * Event queue of ForwardedComposite events. Can't be sent to debugger until
         * debugger has found out idsizes.
         */
        private LinkedList<ForwardedComposite> forwardedEventQueue = new LinkedList<ForwardedComposite>();
    
        /**
         * {@inheritDoc}
         */
        protected boolean dispatch() throws IOException {
            vmDeath = false;
            if (command.command() == JDWP.Event_Composite_COMMAND) {
                Composite();
                return true;
            }
            return false;
        }

        protected void postDispatch() {
            if (vmDeath) {
                host.quit();
            }
        }

        static class ForwardedComposite {
            final int suspendPolicy;
            final List events;
            
            ForwardedComposite(int suspendPolicy, List events) {
                this.suspendPolicy = suspendPolicy;
                this.events = events;
            }
        }
        
        /**
         *  A single event to be added to a ForwardedComposite.
         */
        static class ForwardedEvent {
            final int eventKind;
            final int requestID;
            ForwardedEvent(int eventKind, int requestID) {
                this.eventKind = eventKind;
                this.requestID = requestID;
            }

            final void writeHeader(PacketOutputStream out) throws IOException {
                out.writeByte(eventKind, "eventKind");
                out.writeInt(requestID, "requestID");
            }

            void writeBody(PacketOutputStream out) throws IOException {
            }
        }


        /**
         * Implements <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdwp/jdwp-protocol.html#JDWP_Event_Composite">Composite</a>.
         * This works by deconstructing a composite event from the debug agent, processing some parts, and possibly forwarding
         * interesting parts of the composite to the debugger.
         * These forwarded events will be enqueued until the debugger has asked for IDSizes.
         */
        private void Composite() throws IOException {
            int suspendPolicy = in.readByte("suspendPolicy");
            int eventCount = in.readInt("events");
            List<ForwardedEvent> events = new ArrayList<ForwardedEvent>(eventCount);
            for (int i = 0; i != eventCount; ++i) {
                int eventKind = in.readByte("eventKind");
                int requestID = in.readInt("requestID");
                switch (eventKind) {
                    case JDWP.EventKind_VM_DEATH: {
                        vmDeath = true;
                        events.add(new ForwardedEvent(eventKind, requestID));
                        break;
                    }
                    
                    case JDWP.EventKind_THREAD_START:
                    case JDWP.EventKind_THREAD_END: {
                        final ObjectID threadID = in.readObjectID("thread");
                        if (requestID == 0) {
                            // These special events are not forwarded
                            int status = in.readInt("status");
                            int suspendCount = in.readInt("suspendCount");
                            String name = in.readString("name");
                            sdp.getTPM().updateThread(threadID, name, status, suspendCount);
                        } else {
                            events.add(new ForwardedEvent(eventKind, requestID) {
                                void writeBody(PacketOutputStream out) throws IOException {
                                    out.writeObjectID(threadID, "thread");
                                }
                            });
                        }
                        break;
                    }
                    
                    // Only sent if VM support dynamic class loading...
                    case JDWP.EventKind_CLASS_PREPARE: {
                        try {
                            final ObjectID threadID = in.readObjectID("thread");
                            final byte refTypeTag = in.readByte("refTypeTag");
                            final ReferenceTypeID typeID = in.readReferenceTypeID("typeID");
                            final String sig = in.readString("signature");
                            final int status = in.readInt("status");

                            sdp.getTPM().getThread(threadID); // sanity check
                            ProxyType type = sdp.getPTM().addClass(typeID, sig, true);
                            if (type != null && ProxyTypeManager.isDebuggableKlass(type.getKlass())) {
                                int state = type.getKlass().getState();
                                if (state == Klass.STATE_LOADED || state == Klass.STATE_CONVERTED) {
                                    events.add(new ForwardedEvent(eventKind, requestID) {
                                        void writeBody(PacketOutputStream out) throws IOException {
                                            out.writeObjectID(threadID, "thread");
                                            out.writeByte(refTypeTag, "refTypeTag");
                                            out.writeReferenceTypeID(typeID, "typeID");
                                            out.writeString(sig, "signature");
                                            out.writeInt(status, "status");
                                        }
                                    });
                                } else {
                                    // give up early:
                                   vmDeath = true;
                                }
                            }
                        } catch (SDWPException e) {
                            e.printStackTrace();
                        }
                        break;
                    }

                    case JDWP.EventKind_VM_INIT: {
                        Assert.always(requestID == 0); // always automatic
                        final ObjectID threadID = in.readObjectID("thread");
                        sdaListener.isolateName = in.readString("isolate name");
                        
                        // Read data describing all the classes that were available
                        // to the application before it started (i.e. all the classes in the
                        // suite chain against which the application is bound).
                        int classes = in.readInt("classes");
//                        int ndot = classes / 10;
//                        System.out.print("Synchronizing debug state with VM for isolate " + sdaListener.isolateName + "...");
//                        System.out.flush();
                        String lastName = "";
                        for (int j = 0; j != classes; ++j) {
//                            if (j % ndot == 0) {
//                                System.out.print(".");
//                                System.out.flush();
//                            }
                            ReferenceTypeID typeID = in.readReferenceTypeID("typeID");
                            int commonPrefix = in.readByte("commonPrefix") & 0xFF;
                            String name = in.readString("name");
                            if (commonPrefix != 0) {
                                name = lastName.substring(0, commonPrefix) + name;
                            }

                            lastName = name;
                            ProxyType type = sdp.getPTM().addClass(typeID, name, false);
                            if (type != null && ProxyTypeManager.isDebuggableKlass(type.getKlass())) {
                                int state = type.getKlass().getState();
                                if (type.getKlass().isArray() ||
                                    state == Klass.STATE_LOADED ||
                                    state == Klass.STATE_CONVERTED) {
                                } else {
                                    System.err.println("Can't find classfile for " + type.getKlass() + ". The debug proxy may be looking at the wrong set of jar files.");
                                    if (sdp.quitOnError) {
                                        System.err.println("Quiting debug proxy...");
                                        host.quit();
                                    } else {
                                        System.err.println("WARNING: Continuing execution of debug proxy, but debug information may be wrong...");
                                        break;
                                    }
                                }
                            }
                        }
                        // ok to ask questions about classes...
                        sdp.setCanTalkToDebugger();
//                        System.out.println("");
                        
                        events.add(new ForwardedEvent(eventKind, requestID) {
                            void writeBody(PacketOutputStream out) throws IOException {
                                out.writeObjectID(threadID, "thread");
                            }
                        });
                        break;
                    }

                    case JDWP.EventKind_BREAKPOINT: {
                        final ObjectID threadID = in.readObjectID("thread");
                        final Location location = in.readLocation("location");
                        events.add(new ForwardedEvent(eventKind, requestID) {
                            void writeBody(PacketOutputStream out) throws IOException {
                                out.writeObjectID(threadID, "thread");
                                out.writeLocation(location, "location");
                            }
                        });
                        break;
                    }

                    case JDWP.EventKind_EXCEPTION: {
                        final ObjectID threadID = in.readObjectID("thread");
                        final Location location = in.readLocation("location");
                        final TaggedObjectID exception = in.readTaggedObjectID("exception");
                        final Location catchLocation = in.readLocation("catchLocation");
                        events.add(new ForwardedEvent(eventKind, requestID) {
                            void writeBody(PacketOutputStream out) throws IOException {
                                out.writeObjectID(threadID, "thread");
                                out.writeLocation(location, "location");
                                out.writeTaggedObjectID(exception, "exception");
                                out.writeLocation(catchLocation, "catchLocation");
                            }
                        });
                        break;
                    }

                    case JDWP.EventKind_SINGLE_STEP: {
                        final ObjectID threadID = in.readObjectID("thread");
                        final Location location = in.readLocation("location");
                        events.add(new ForwardedEvent(eventKind, requestID) {
                            void writeBody(PacketOutputStream out) throws IOException {
                                out.writeObjectID(threadID, "thread");
                                out.writeLocation(location, "location");
                            }
                        });
                        break;
                    }
                }
            }

            if (!events.isEmpty()) {
                // Notify any threads waiting for an event to have been received and passed through to the debugger.
                // We'll hold on to these until the debugger contacts us, and we start the CompositeEventForwarder.
                synchronized (forwardedEventQueue) {
                    forwardedEventQueue.addLast(new ForwardedComposite(suspendPolicy, events));
                    forwardedEventQueue.notifyAll();
                }
            }
        }
        
        class CompositeEventForwarder implements Runnable {
            public void run() {
                if (Log.info()) {
                    Log.log("Started CompositeEventForwarder thread");
                }
                
                while (true) {
                    synchronized (forwardedEventQueue) {
                        if (forwardedEventQueue.isEmpty()) {
                            try {
                                forwardedEventQueue.wait();
                            } catch (InterruptedException ex) {
                                
                            }
                        } else {
                            ForwardedComposite composite = forwardedEventQueue.removeFirst();
                            List events = composite.events;
                            
                            if (Log.info()) {
                                Log.log("Forwarding composit event");
                            }
                            
                            try {
                                CommandPacket command = new CommandPacket(JDWP.Event_COMMAND_SET, JDWP.Event_Composite_COMMAND, false);
                                PacketOutputStream out = command.getOutputStream();
                                out.writeByte(composite.suspendPolicy, "suspendPolicy");
                                out.writeInt(events.size(), "events");
                                for (Iterator iter = events.iterator(); iter.hasNext(); ) {
                                    Event.ForwardedEvent fe = (Event.ForwardedEvent)iter.next();
                                    fe.writeHeader(out);
                                    fe.writeBody(out);
                                }
                                
                                otherHost.sendCommand(command);
                            } catch (SDWPException e) {
                                // This is an asynchronous send without a reply
                                Assert.shouldNotReachHere();
                            } catch (IOException e) {
                                if (sdaListener == null || sdaListener.otherHost == null || (!sdaListener.hasQuit() && !sdaListener.otherHost.hasQuit())) {
                                    e.printStackTrace();
                                }
                            }
                            
                            // Notify any threads waiting for an event to have been received and passed through to the debugger
                            synchronized (this) {
                                this.notifyAll();
                            }
                        }
                    }
                }
            }
        } // class CompositeEventForwarder
        
        /**
         * Once the debugger is ready to start receiving events, start up the EventForwarder
         * daemon.
         */
        void enableForwardedEvents() {
            Thread forwarder = new Thread(new CompositeEventForwarder(), "EventForwarder");
            forwarder.setDaemon(true);
            forwarder.start();
        }

    }

    /*-----------------------------------------------------------------------*\
     *                         SquawkVM Command Set (128)                    *
    \*-----------------------------------------------------------------------*/

    static final class SquawkVM extends SDPCommandSet {

        private final SDAListener.Event event;

        SquawkVM(SDAListener.Event event) {
            this.event = event;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean dispatch() throws IOException {
            try {
                switch (command.command()) {
                    case SDWP.SquawkVM_SteppingInfo_COMMAND:       SteppingInfo();       break;
                    case SDWP.SquawkVM_ThreadStateChanged_COMMAND: ThreadStateChanged(); break;
                    default: return false;
                }
                return true;
            } catch (SDWPException e) {
                e.printStackTrace();
                Assert.shouldNotReachHere();
                return false;
            }
        }

        /**
         * Implements {@link SDWP#SquawkVM_SteppingInfo_COMMAND}.
         */
        private void SteppingInfo() throws IOException, SDWPException {
            if (Log.debug()) {
                Log.log("SteppingInfo: getting stepping info");
            }
            ReferenceTypeID typeID = in.readReferenceTypeID("refType");  // The class
            MethodID methodID = in.readMethodID("method");               // The method
            long bci = in.readLong("bci");

            if (Log.debug()) {
                Log.log("SteppingInfo: class typeID = " + typeID + ", methodID = " + methodID);
            }
            ProxyType definingClass = sdp.getPTM().lookup(typeID, false);
            ProxyMethod method = null;

            /* Verify that definingClass and method is not null */
            if (definingClass == null || (method = definingClass.getMethod(methodID)) == null) {
                if (Log.debug()) {
                    Log.log("SteppingInfo: definingClass and/or method was null");
                }
                throw new SDWPException(JDWP.Error_ABSENT_INFORMATION, "definingClass and/or method was null");
            }

            ProxyMethod.LineNumberTable table = method.getLineNumberTable();

            int line = table.getLineNumber(bci);
            if (Log.debug()) {
                Log.log("SteppingInfo: (offset " + bci + ") --> (line " + line + ")");
            }
            /* Calculate targetOffset, dupOffset, and afterDupOffset */
            Assert.thatFatal(table != null, "SteppingInfo: unable to acquire line number table");

            /* targetOffset:
             *   To get the target offset of line L we find the line number table entry of L and get the next entry (or -1 if not found)
             *   Tricky case: If there are TWO offsets that have L as a line number, we only look at the earliest one.
             */
            long targetBCI = table.getOffsetOfLineAfter(line);
            /* offset that has same line number as 'line' but is not equal to 'offset' */
            long dupBCI = table.getDuplicateOffset(table.getDuplicateOffset(bci, line), line);
            /* offset of next line after duplicate */
            long afterDupBCI = table.getOffsetOfLineAfter(dupBCI, table.getLineNumber(dupBCI));

            if (Log.debug()) {
                Log.log("SteppingInfo [current bci = " + bci +
                        ", target bci = " + targetBCI +
                        ", dup current line bci = " + dupBCI +
                        ", bci after current dup = " + afterDupBCI +
                        "]");
            }

            out.writeLong(targetBCI, "targetBCI");
            out.writeLong(dupBCI, "dupBCI");
            out.writeLong(afterDupBCI, "afterDupBCI");
        }

        /**
         * Implements {@link SDWP#SquawkVM_ThreadStateChanged_COMMAND}.
         */
        private void ThreadStateChanged() throws IOException {
            sdp.getTPM().updateThreads(in);
        }
    }
}
