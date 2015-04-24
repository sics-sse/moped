/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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
import com.sun.squawk.debugger.EventManager.*;
import com.sun.squawk.debugger.EventRequest.*;
import com.sun.squawk.debugger.EventRequestModifier.*;
import com.sun.squawk.util.*;
import com.sun.squawk.Debugger;

/**
 * Variant of proxy used for sniffing JDWP packets, to debug debugger interactions.
 *
 * Used by SDP.goSniff().
 *
 */
public abstract class JDWPSniffer extends JDWPListener {
    
    public static class SnifferPacketInputStream extends PacketInputStream {

        public SnifferPacketInputStream(PacketInputStream raw) {
            super(raw);
        }

        public ObjectID readObjectID(String s) throws IOException {
            JDWPSniffer.readObjectID(this, s);
            return null;
        }

        public TaggedObjectID readTaggedObjectID(String s) throws IOException {
            JDWPSniffer.readTaggedObjectID(this, s);
            return null;
        }

        public ReferenceTypeID readReferenceTypeID(String s) throws IOException {
            JDWPSniffer.readReferenceTypeID(this, s);
            return null;
        }

        public MethodID readMethodID(String s) throws IOException {
            JDWPSniffer.readMethodID(this, s);
            return null;
        }

        public FrameID readFrameID(String s) throws IOException {
            JDWPSniffer.readFrameID(this, s);
            return null;
        }

        public FieldID readFieldID(String s) throws IOException {
            JDWPSniffer.readFieldID(this, s);
            return null;
        }

        public Location readLocation(String s) throws IOException {
            JDWPSniffer.readLocation(this, s);
            return null;
        }
    } /* SnifferPacketInputStream */
    
    /**
     * The JDWP command set handlers.
     */
    protected final IntHashtable commandSets = new IntHashtable();
    
    /**
     * Get the input stream from a command packet (sniffer needs to wrap input stream.
     * @param command packet to read from
     * @return the input stream to read with
     */
    public PacketInputStream getInputStreamFor(CommandPacket command) {
        PacketInputStream raw = command.getInputStream();
        return new SnifferPacketInputStream(raw);
    }
    
    
    public static abstract class SnifferCommandSet extends CommandSet {
        public void logReply(CommandPacket command, ReplyPacket reply, PacketInputStream in) throws IOException {
            // override to parse reply...
        } 
    }
               
    protected void processCommand(CommandPacket command) throws IOException {
        SnifferCommandSet handler = (SnifferCommandSet)commandSets.get(command.set());
        
        if (handler != null) {
            handler.handle(otherHost, command);
        }
        
        try {
            otherHost.sendCommand(command);
        } catch (SDWPException e) {
            // Just pass error replies through to debugger
        }
        
        if (command.needsReply()) {
            ReplyPacket sdaReply = command.getReply();

            if (sdaReply != null) {
                if (handler != null) {
                    handler.logReply(command, sdaReply, sdaReply.getInputStream());
                }
                sendReply(sdaReply);
            }
        }
    }
    
    /** Other VMs use diferent IDsizes, so handle them here: */
     
    public static int fieldIDSize = -1;
    public static int methodIDSize = -1;
    public static int objectIDSize = -1;
    public static int referenceTypeIDSize = -1;
    public static int frameIDSize = -1;

    static String readID(PacketInputStream in, int size) throws IOException {
        Assert.always(size > 0);
        long value = 0;
        DataInputStream dis = in.getInputStream();
        while (size-- != 0) { 
            int b = dis.readByte() & 0xFF;
            value = (value << 8) | b;
        }
        return Long.toHexString(value);
    }
    
    public static void readObjectID(PacketInputStream in, String s) throws IOException {
        String value = readID(in, objectIDSize);
        if (s != null && Log.verbose()) Log.log("in[object]    " + s + "=0x" + value);
    }

    public static void readTaggedObjectID(PacketInputStream in, String s) throws IOException {
        in.readByte("tag");
        String value = readID(in, objectIDSize);
        if (s != null && Log.verbose()) Log.log("in[t-object]  " + s + "=0x" + value);
    }

    public static void readReferenceTypeID(PacketInputStream in, String s) throws IOException {
        String value = readID(in, referenceTypeIDSize);
        if (s != null && Log.verbose()) Log.log("in[type]      " + s + "=0x" + value);
    }

    public static void readMethodID(PacketInputStream in, String s) throws IOException {
        String value = readID(in, methodIDSize);
        if (s != null && Log.verbose()) Log.log("in[method]    " + s + "=0x" + value);
    }

    public static void readFrameID(PacketInputStream in, String s) throws IOException {
        String value = readID(in, frameIDSize);
        if (s != null && Log.verbose()) Log.log("in[frame]     " + s + "=0x" + value);
    }

    public static void readFieldID(PacketInputStream in, String s) throws IOException {
        String value = readID(in, fieldIDSize);
        if (s != null && Log.verbose()) Log.log("in[field]     " + s + "=0x" + value);
    }

    public static void readLocation(PacketInputStream in, String s) throws IOException {
        in.readByte("location type tag");
        readReferenceTypeID(in, "location");
        readMethodID(in, "location");
        in.readLong("location index");
    }
    
    /***************************************************************/
    /***                  JVMSniffer                               */
    /***************************************************************/
    /**
     * Sniff event packets from JVM to debugger.
     */
    public static class JVMSniffer extends JDWPSniffer {
        
        JVMSniffer() {
            commandSets.put(JDWP.Event_COMMAND_SET,       new Event());
        }
        
        public String sourceName() {
            return "JVM";
        }
       
        static final class Event extends SnifferCommandSet {
            
            protected boolean dispatch() throws IOException {
                if (command.command() == JDWP.Event_Composite_COMMAND) {
                    Composite();
                }
                return false;
            }
            
            private void Composite() throws IOException {
                int suspendPolicy = in.readByte("suspendPolicy");
                if (Log.info()) {
                    Log.log("    suspendPolicy: " + EventRequest.getNameForJDWPSuspendPolicy(suspendPolicy));
                }
                int eventCount = in.readInt("events");
                for (int i = 0; i != eventCount; ++i) {
                    int eventKind = in.readByte("eventKind");
                    if (Log.info()) {
                        Log.log("    eventKind: " + EventRequest.getNameForJDWPEventKind(eventKind));
                    }
                    int requestID = in.readInt("requestID");
                    switch (eventKind) {
                        case JDWP.EventKind_THREAD_START:
                        case JDWP.EventKind_THREAD_END: {
                            readObjectID(in, "thread");
                            break;
                        }
                        case JDWP.EventKind_CLASS_PREPARE: {
                            readObjectID(in, "thread");
                            in.readByte("refTypeTag");
                            readReferenceTypeID(in, "typeID");
                            in.readString("signature");
                            in.readInt("status");
                            break;
                        }
                    }
                }
            }
        }
    }

    /***************************************************************/
    /*                    JDBSniffer                               */
    /***************************************************************/
    /**
     * Sniff command packets from debugger to JVM.
     */
    public static class JDBSniffer extends JDWPSniffer {
        
        public JDBSniffer() {
            commandSets.put(JDWP.EventRequest_COMMAND_SET,         new EventRequestCommand());
            commandSets.put(JDWP.VirtualMachine_COMMAND_SET,       new VirtualMachine());
            commandSets.put(JDWP.Method_COMMAND_SET,               new MethodCommand());
        }
        
        /**
         * Dummy EventRequest object suitable for logging.
         */
        static class SniffEventRequest extends EventRequest {
            public SniffEventRequest(PacketInputStream in, int kind) throws SDWPException, IOException {
                super(-999, in, kind);
            }
            public void write(PacketOutputStream out, Debugger.Event event) throws IOException {
                Assert.shouldNotReachHere();
            }
            public boolean matchKind(int eventKind) {
                return false;
            }
        }
        
          /**
         * Catch EventRequest.Set commands from debugger.
         */
        static final class EventRequestCommand extends SnifferCommandSet {
            protected boolean dispatch() throws IOException, SDWPException {
                switch (command.command()) {
                    case JDWP.EventRequest_Set_COMMAND:    return Set();
                    default: return false;
                }
            }
            
            private boolean Set() throws SDWPException, IOException {
                int kind = in.readByte("eventKind");
                if (Log.info()) {
                    EventRequest request = new SniffEventRequest(in, kind);
                    Log.log("    " + request);
                }
                return false;
            }
        } // EventRequestCommand
        
        /**
         * Catch Method_LineTable_COMMAND from debugger.
         */
        static final class MethodCommand extends SnifferCommandSet {
            
            protected boolean dispatch() throws IOException, SDWPException {
                switch (command.command()) {
                    case JDWP.Method_LineTable_COMMAND:      LineTable(); break;
                }
                return false;
            }
            
            public void logReply(CommandPacket command, ReplyPacket reply, PacketInputStream in) throws IOException {
                // override to parse reply...
                switch (command.command()) {
                    case JDWP.Method_LineTable_COMMAND:              LineTableReply(in);              break;
                }
            }
            
            
            private void LineTable() throws SDWPException, IOException {
                in.readReferenceTypeID("definingClass");
                in.readMethodID("methodID");
            }
            
            private void LineTableReply(PacketInputStream in) throws IOException {
                in.readLong("start");
                in.readLong("end");
                int lines = in.readInt("lines");
                for (int i = 0; i != lines; ++i) {
                    in.readLong("    lineCodeIndex");
                    in.readInt( "    lineNumber");
                }
            }
        } // MethodCommand
        
        static final class VirtualMachine extends SnifferCommandSet {
            
            protected boolean dispatch() throws IOException, SDWPException {
                switch (command.command()) {
                    case JDWP.VirtualMachine_ClassesBySignature_COMMAND:   ClassesBySignature();     break;
                }
                return false;
            }
            
            public void logReply(CommandPacket command, ReplyPacket reply, PacketInputStream in) throws IOException {
                // override to parse reply...
                switch (command.command()) {
                    case JDWP.VirtualMachine_Version_COMMAND:              Version(in);              break;
                    case JDWP.VirtualMachine_IDSizes_COMMAND:              IDSizes(in);              break;
                    case JDWP.VirtualMachine_Capabilities_COMMAND:         Capabilities(in);         break;
                    case JDWP.VirtualMachine_CapabilitiesNew_COMMAND:      CapabilitiesNew(in);      break;
                    case JDWP.VirtualMachine_ClassPaths_COMMAND:           ClassPaths(in);           break;
                    case JDWP.VirtualMachine_ClassesBySignature_COMMAND:   ClassesBySignatureReply(in); break;
                    case JDWP.VirtualMachine_AllClasses_COMMAND:           AllClassesReply(in, false); break;
                    case JDWP.VirtualMachine_AllClassesWithGeneric_COMMAND:AllClassesReply(in, true); break;
                }
            }
            
            private void ClassesBySignature() throws IOException {
                in.readString("signature");
            }
            
            private void ClassesBySignatureReply(PacketInputStream in) throws IOException {
                int count = in.readInt("classes");
                for (int i = 0; i < count; i++) {
                    in.readByte("refTypeTag");
                    readReferenceTypeID(in, "typeID");
                    in.readInt("status");
                }
            }
            
           private void AllClassesReply(PacketInputStream in, boolean withGeneric) throws IOException {
                int count = in.readInt("classes");
                for (int i = 0; i < count; i++) {
                    in.readByte("refTypeTag");
                    readReferenceTypeID(in, "typeID");
                    in.readString("signature");
                    if (withGeneric) {
                        in.readString("generic signature");
                    }
                    in.readInt("status");
                }
            }
            
            private void Version(PacketInputStream in) throws IOException {
                in.readString("description");
                in.readInt("jdwpMajor"); /* major version */
                in.readInt("jdwpMinor"); /* minor version */
                in.readString("vmVersion");
                in.readString("vmName");
            }
            
            private void IDSizes(PacketInputStream in) throws IOException {
                fieldIDSize         = in.readInt("fieldIDSize");
                methodIDSize        = in.readInt("methodIDSize");
                objectIDSize        = in.readInt("objectIDSize");
                referenceTypeIDSize = in.readInt("referenceTypeIDSize");
                frameIDSize         = in.readInt("frameIDSize");
            }
            
            private void Capabilities(PacketInputStream in) throws IOException {
                in.readBoolean("canWatchFieldModification");
                in.readBoolean("canWatchFieldAccess");
                in.readBoolean("canGetBytecodes");
                in.readBoolean("canGetSyntheticAttribute");
                in.readBoolean("canGetOwnedMonitorInfo");
                in.readBoolean("canGetCurrentContendedMonitor");
                in.readBoolean("canGetMonitorInfo");
            }
            
            private void CapabilitiesNew(PacketInputStream in) throws IOException {
                in.readBoolean("canWatchFieldModification");       // Can the VM watch field modification, and therefore can it send the Modification Watchpoint Event?
                in.readBoolean("canWatchFieldAccess");             // Can the VM watch field access, and therefore can it send the Access Watchpoint Event?
                in.readBoolean("canGetBytecodes");                 // Can the VM get the bytecodes of a given method?
                in.readBoolean("canGetSyntheticAttribute");        // Can the VM determine whether a field or method is synthetic? (that is, can the VM determine if the method or the field was invented by the compiler?)
                in.readBoolean("canGetOwnedMonitorInfo");          // Can the VM get the owned monitors infornation for a thread?
                in.readBoolean("canGetCurrentContendedMonitor");   // Can the VM get the current contended monitor of a thread?
                in.readBoolean("canGetMonitorInfo");               // Can the VM get the monitor information for a given object?
                in.readBoolean("canRedefineClasses");              // Can the VM redefine classes?
                in.readBoolean("canAddMethod");                    // Can the VM add methods when redefining classes?
                in.readBoolean("canUnrestrictedlyRedefineClasses");// Can the VM redefine classesin arbitrary ways?
                in.readBoolean("canPopFrames");                    // Can the VM pop stack frames?
                in.readBoolean("canUseInstanceFilters");           // Can the VM filter events by specific object?
                in.readBoolean("canGetSourceDebugExtension");      // Can the VM get the source debug extension?
                in.readBoolean("canRequestVMDeathEvent");          // Can the VM request VM death events?
                in.readBoolean("canSetDefaultStratum");            // Can the VM set a default stratum?
                in.readBoolean("canGetInstanceInfo");              // Can the VM return instances, counts of instances of classes and referring objects? 
                in.readBoolean("canRequestMonitorEvents");         // Can the VM request monitor events?
                in.readBoolean("canGetMonitorFrameInfo");          // Can the VM get monitors with frame depth info?  
                in.readBoolean("canUseSourceNameFilters");         // Can the VM filter class prepare events by source name?  
                in.readBoolean("canGetConstantPool");              // Can the VM return the constant pool information?  
                in.readBoolean("canForceEarlyReturn");             // Can the VM force early return from a method?  
                in.readBoolean("reserved22");                      // Reserved for future capability
                in.readBoolean("reserved23");                      // Reserved for future capability
                in.readBoolean("reserved24");                      // Reserved for future capability
                in.readBoolean("reserved25");                      // Reserved for future capability
                in.readBoolean("reserved26");                      // Reserved for future capability
                in.readBoolean("reserved27");                      // Reserved for future capability
                in.readBoolean("reserved28");                      // Reserved for future capability
                in.readBoolean("reserved29");                      // Reserved for future capability
                in.readBoolean("reserved30");                      // Reserved for future capability
                in.readBoolean("reserved31");                      // Reserved for future capability
                in.readBoolean("reserved32");                      // Reserved for future capability
            }
            
            private void ClassPaths(PacketInputStream in) throws IOException {
                in.readString("baseDir");
                in.readInt("classpaths");
                in.readInt("bootclasspaths");
            }
        } // VirtualMachine
        
        
        public String sourceName() {
            return "Debugger";
        }
       
    } // VirtualMachine
}

