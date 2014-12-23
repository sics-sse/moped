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
import com.sun.squawk.util.Assert;
import com.sun.squawk.debugger.JDWPListener.QuitException;

/**
 * A <code>CommandPacket</code> encapsulates a JDWP command packet.
 */
public final class CommandPacket extends Packet {

    /**
     * The JDWP command set identifier of the command.
     */
    private final byte set;

    /**
     * The JDWP identifier of the command within its command set.
     */
    private final byte command;

    /**
     * The reply to this command.
     */
    private volatile ReplyPacket reply;

    /**
     * Does this command expect a reply.
     */
    private final boolean needsReply;

    /**
     * Creates a packet to send a new command packet.
     *
     * @param owner      the owner of the new packet
     * @param set        the JDWP command set constant
     * @param command    the JDWP command constant
     */
    public CommandPacket(int set, int command, boolean needsReply) {
        super(allocateUniqueID());
        Assert.that(set == (set & 0xFF));
        Assert.that(command == (command & 0xFF));
        this.set = (byte)set;
        this.command = (byte)command;
        this.needsReply = needsReply;
    }

    /**
     * Creates a packet to pass through to the VM from the debugger.
     *
     * @param newOwner     the owner of the new packet
     * @param fromDebugger the packet from the debugger to be sent to the VM.
     */
//    public CommandPacket(CommandPacket fromDebugger) {
//        super(fromDebugger);
//        this.set = fromDebugger.set;
//        this.command = fromDebugger.command;
//        this.needsReply = fromDebugger.needsReply;
//    }

    /**
     * Creates a packet to encapsulate a received JDWP command packet.
     *
     * @param owner       the JDWPListener that received the packet
     * @param id          the identifier in the command packet
     * @param dataLength  the length of the data to read from <code>data</code>
     * @param data        the contents of the data field of the packet
     * @param set         the JDWP command set constant
     * @param command     the JDWP command constant
     * @throws IOException
     */
    public CommandPacket(int id, int dataLength, DataInputStream data, int set, int command, boolean needsReply) throws IOException {
        super(id, dataLength, data);
        Assert.that(set == (int)(byte)set);
        Assert.that(command == (int)(byte)command);
        this.set = (byte)set;
        this.command = (byte)command;
        this.needsReply = needsReply;
    }

    /**
     * @return the JDWP command set identifier of this command.
     */
    public int set() {
        return set & 0xFF;
    }

    /**
     * @return the JDWP command identifier of this command.
     */
    public int command() {
        return command & 0xFF;
    }

    /**
     * Creates a packet to send a reply for this command.
     *
     * @param errorCode the JDWP error code to return in the reply
     * @return          the reply packet
     */
    public ReplyPacket createReply(int errorCode) {
        return new ReplyPacket(this, errorCode);
    }

    public final boolean needsReply() {
        return needsReply;
    }

    /**
     * {@inheritDoc}
     */
    public int getFlags() {
        return needsReply ? FLAG_NEEDS_REPLY : 0;
    }

    /**
     * Sets the reply to this sent command packet.
     *
     * @param reply  the reply to this sent command
     */
    synchronized void setReply(ReplyPacket reply) {
        Assert.that(needsReply, "cannot set reply for a packet that doesn't need one");
        this.reply = reply;
    }

    /**
     * Gets the reply to this sent command packet.
     *
     * @return  the reply or null if it has not yet been received
     */
    public synchronized ReplyPacket getReply() {
        Assert.that(needsReply, "cannot get reply for a command that doesn't need one");
        return reply;
    }


    /**
     * Write the <code>set</code> and <code>command</code> fields to <code>dos</code>
     *
     * @param dos DataOutputStream
     * @throws IOException
     */
    protected void writeFields(DataOutputStream dos) throws IOException {
        dos.writeByte(set);
        dos.writeByte(command);
    }

    /*-----------------------------------------------------------------------*\
     *                 Translate JDWP commands to strings                    *
    \*-----------------------------------------------------------------------*/

    /**
     * Gets the identifier for a given JDWP command.
     *
     * @param set         a command set
     * @param command     a command with <code>set</code>
     *
     * @see <a href="http://java.sun.com/j2se/1.3/docs/guide/jpda/jdwp-protocol.html">JDWP</a>
     */
    public static String toString(int set, int command) {
        set &= 0xff;
        command &= 0xff;
        String s = Integer.toString(set);
        String c = Integer.toString(command);
/*if[!FLASH_MEMORY]*/
        switch (set) {
            case JDWP.VirtualMachine_COMMAND_SET: {
                s = "VirtualMachine";
                switch (command) {
                    case JDWP.VirtualMachine_Version_COMMAND:              c = "Version";              break;
                    case JDWP.VirtualMachine_ClassesBySignature_COMMAND:   c = "ClassesBySignature";   break;
                    case JDWP.VirtualMachine_AllClasses_COMMAND:           c = "AllClasses";           break;
                    case JDWP.VirtualMachine_AllThreads_COMMAND:           c = "AllThreads";           break;
                    case JDWP.VirtualMachine_TopLevelThreadGroups_COMMAND: c = "TopLevelThreadGroups"; break;
                    case JDWP.VirtualMachine_Dispose_COMMAND:              c = "Dispose";              break;
                    case JDWP.VirtualMachine_IDSizes_COMMAND:              c = "IDSizes";              break;
                    case JDWP.VirtualMachine_Suspend_COMMAND:              c = "Suspend";              break;
                    case JDWP.VirtualMachine_Resume_COMMAND:               c = "Resume";               break;
                    case JDWP.VirtualMachine_Exit_COMMAND:                 c = "Exit";                 break;
                    case JDWP.VirtualMachine_CreateString_COMMAND:         c = "CreateString";         break;
                    case JDWP.VirtualMachine_Capabilities_COMMAND:         c = "Capabilities";         break;
                    case JDWP.VirtualMachine_ClassPaths_COMMAND:           c = "ClassPaths";           break;
                    case JDWP.VirtualMachine_DisposeObjects_COMMAND:       c = "DisposeObjects";       break;
                    case JDWP.VirtualMachine_HoldEvents_COMMAND:           c = "HoldEvents";           break;
                    case JDWP.VirtualMachine_ReleaseEvents_COMMAND:        c = "ReleaseEvents";        break;
                    case JDWP.VirtualMachine_CapabilitiesNew_COMMAND:      c = "CapabilitiesNew";      break; 
                }
                break;
            }
            case JDWP.ReferenceType_COMMAND_SET: {
                s = "ReferenceType";
                switch (command) {
                    case JDWP.ReferenceType_Signature_COMMAND:   c = "Signature";   break;
                    case JDWP.ReferenceType_ClassLoader_COMMAND: c = "ClassLoader"; break;
                    case JDWP.ReferenceType_Modifiers_COMMAND:   c = "Modifiers";   break;
                    case JDWP.ReferenceType_Fields_COMMAND:      c = "Fields";      break;
                    case JDWP.ReferenceType_Methods_COMMAND:     c = "Methods";     break;
                    case JDWP.ReferenceType_GetValues_COMMAND:   c = "GetValues";   break;
                    case JDWP.ReferenceType_SourceFile_COMMAND:  c = "SourceFile";  break;
                    case JDWP.ReferenceType_NestedTypes_COMMAND: c = "NestedTypes"; break;
                    case JDWP.ReferenceType_Status_COMMAND:      c = "Status";      break;
                    case JDWP.ReferenceType_Interfaces_COMMAND:  c = "Interfaces";  break;
                    case JDWP.ReferenceType_ClassObject_COMMAND: c = "ClassObject"; break;
                }
                break;
            }
            case JDWP.ClassType_COMMAND_SET: {
                s = "ClassType";
                switch (command) {
                    case JDWP.ClassType_Superclass_COMMAND:   c = "Superclass";   break;
                    case JDWP.ClassType_SetValues_COMMAND:    c = "SetValues";    break;
                    case JDWP.ClassType_InvokeMethod_COMMAND: c = "InvokeMethod"; break;
                    case JDWP.ClassType_NewInstance_COMMAND:  c = "NewInstance";  break;
                }
                break;
            }
            case JDWP.ArrayType_COMMAND_SET: {
                s = "ArrayType";
                switch (command) {
                    case JDWP.ArrayType_NewInstance_COMMAND: c = "NewInstance"; break;
                }
                break;
            }
            case JDWP.InterfaceType_COMMAND_SET: {
                s = "InterfaceType";
                c = "";
                break;
            }
            case JDWP.Method_COMMAND_SET: {
                s = "Method";
                switch (command) {
                    case JDWP.Method_LineTable_COMMAND:     c = "LineTable";     break;
                    case JDWP.Method_VariableTable_COMMAND: c = "VariableTable"; break;
                    case JDWP.Method_Bytecodes_COMMAND:     c = "Bytecodes";     break;
                }
                break;
            }
            case JDWP.Field_COMMAND_SET: {
                s = "Field";
                c = "";
                break;
            }
            case JDWP.ObjectReference_COMMAND_SET: {
                s = "ObjectReference";
                switch (command) {
                    case JDWP.ObjectReference_ReferenceType_COMMAND:     c = "ReferenceType";     break;
                    case JDWP.ObjectReference_GetValues_COMMAND:         c = "GetValues";         break;
                    case JDWP.ObjectReference_SetValues_COMMAND:         c = "SetValues";         break;
                    case JDWP.ObjectReference_MonitorInfo_COMMAND:       c = "MonitorInfo";       break;
                    case JDWP.ObjectReference_InvokeMethod_COMMAND:      c = "InvokeMethod";      break;
                    case JDWP.ObjectReference_DisableCollection_COMMAND: c = "DisableCollection"; break;
                    case JDWP.ObjectReference_EnableCollection_COMMAND:  c = "EnableCollection";  break;
                    case JDWP.ObjectReference_IsCollected_COMMAND:       c = "IsCollected";       break;
                }
                break;
            }
            case JDWP.StringReference_COMMAND_SET: {
                s = "StringReference";
                switch (command) {
                    case JDWP.StringReference_Value_COMMAND: c = "Value"; break;
                }
                break;
            }
            case JDWP.ThreadReference_COMMAND_SET: {
                s = "ThreadReference";
                switch (command) {
                    case JDWP.ThreadReference_Name_COMMAND:                     c = "Name";                     break;
                    case JDWP.ThreadReference_Suspend_COMMAND:                  c = "Suspend";                  break;
                    case JDWP.ThreadReference_Resume_COMMAND:                   c = "Resume";                   break;
                    case JDWP.ThreadReference_Status_COMMAND:                   c = "Status";                   break;
                    case JDWP.ThreadReference_ThreadGroup_COMMAND:              c = "ThreadGroup";              break;
                    case JDWP.ThreadReference_Frames_COMMAND:                   c = "Frames";                   break;
                    case JDWP.ThreadReference_FrameCount_COMMAND:               c = "FrameCount";               break;
                    case JDWP.ThreadReference_OwnedMonitors_COMMAND:            c = "OwnedMonitors";            break;
                    case JDWP.ThreadReference_CurrentContendedMonitor_COMMAND:  c = "CurrentContendedMonitor";  break;
                    case JDWP.ThreadReference_Stop_COMMAND:                     c = "Stop";                     break;
                    case JDWP.ThreadReference_Interrupt_COMMAND:                c = "Interrupt";                break;
                    case JDWP.ThreadReference_SuspendCount_COMMAND:             c = "SuspendCount";             break;
                }
                break;
            }
            case JDWP.ThreadGroupReference_COMMAND_SET: {
                s = "ThreadGroupReference";
                switch (command) {
                    case JDWP.ThreadGroupReference_Name_COMMAND:     c = "Name";     break;
                    case JDWP.ThreadGroupReference_Parent_COMMAND:   c = "Parent";   break;
                    case JDWP.ThreadGroupReference_Children_COMMAND: c = "Children"; break;
                }
                break;
            }
            case JDWP.ArrayReference_COMMAND_SET: {
                s = "ArrayReference";
                switch (command) {
                    case JDWP.ArrayReference_Length_COMMAND:    c = "Length";    break;
                    case JDWP.ArrayReference_GetValues_COMMAND: c = "GetValues"; break;
                    case JDWP.ArrayReference_SetValues_COMMAND: c = "SetValues"; break;
                }
                break;
            }
            case JDWP.ClassLoaderReference_COMMAND_SET: {
                s = "ClassLoaderReference";
                switch (command) {
                    case JDWP.ClassLoaderReference_VisibleClasses_COMMAND: c = "VisibleClasses"; break;
                }
                break;
            }
            case JDWP.EventRequest_COMMAND_SET: {
                s = "EventRequest";
                switch (command) {
                    case JDWP.EventRequest_Set_COMMAND:                 c = "Set";                 break;
                    case JDWP.EventRequest_Clear_COMMAND:               c = "Clear";               break;
                    case JDWP.EventRequest_ClearAllBreakpoints_COMMAND: c = "ClearAllBreakpoints"; break;
                }
                break;
            }
            case JDWP.StackFrame_COMMAND_SET: {
                s = "StackFrame";
                switch (command) {
                    case JDWP.StackFrame_GetValues_COMMAND:  c = "GetValues";  break;
                    case JDWP.StackFrame_SetValues_COMMAND:  c = "SetValues";  break;
                    case JDWP.StackFrame_ThisObject_COMMAND: c = "ThisObject"; break;
                }
                break;
            }
            case JDWP.ClassObjectReference_COMMAND_SET: {
                s = "ClassObjectReference";
                switch (command) {
                    case JDWP.ClassObjectReference_ReflectedType_COMMAND: c = "ReflectedType"; break;
                }
                break;
            }
            case JDWP.Event_COMMAND_SET: {
                s = "Event";
                switch (command) {
                    case JDWP.Event_Composite_COMMAND: c = "Composite"; break;
                }
                break;
            }
            case SDWP.SquawkVM_COMMAND_SET: {
                s = "SDWP";
                switch (command) {
                    case SDWP.SquawkVM_SteppingInfo_COMMAND: c = "SteppingInfo"; break;
                    case SDWP.SquawkVM_AllThreads_COMMAND: c = "AllThreads"; break;
                    case SDWP.SquawkVM_ThreadStateChanged_COMMAND: c = "ThreadStateChanged"; break;
                }
                break;
            }
        }
/*end[FLASH_MEMORY]*/
        return s + "." + c;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("CommandPacket[id=").
            append(getID()).
            append(",size=").
            append(getSize()).
            append("]:").
            append(toString(set, command));
        if (Log.DEBUG_ENABLED && Log.debug()) {
            appendData(buf);
        }
        return buf.toString();
    }

}
