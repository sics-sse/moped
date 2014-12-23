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

import com.sun.squawk.debugger.EventRequestModifier.*;
import com.sun.squawk.util.*;
import com.sun.squawk.Debugger.Event;

/**
 * An <code>EventRequest</code> instance is used register a request for notification
 * of a particular event on behalf of a debugger.
 *
 * Events are requested by the debugger client, which also sends an event request ID. This ID is used to enable the
 * debugger to clear event requests.
 *
 * Events can be be filtered on the VM so that only certian class load events, or method entry events (etc) are actually
 * sent to the debugger client. These filters are described as "modifiers" in the event request, and are modelled by
 * the {@link Modifier} class.
 *
 */
public abstract class EventRequest {

    /**
     * The <code>JDWP.EventKind</code> constant for the event being monitored.
     */
    public final int kind;

    /**
     * Should the current thread, all (isolate) threads, or no threads be suspended when the event occurs.
     * See JDWP.SuspendPolicy
     */
    public final int suspendPolicy;

    /**
     * Used by debugger to cancel requests. An ID of zero indicates an automatic event that can't be cleared.
     */
    public final int id;

    /**
     * List of modifiers that filter out certain events.
     */
    public final EventRequestModifier[] modifiers;

    /**
     * Determines if this request may match events of a given kind.
     *
     * @param eventKind  the event kind to query
     * @return true if this event request may match <code>eventKind</code> events
     */
    public boolean matchKind(int eventKind) {
        return eventKind == kind;
    }

    /**
     * Determines if all the modifiers of this request match a given event.
     *
     * @param event   the details and thread context of the event
     * @return true if all the modifiers of this request match the given event
     */
    public boolean matchModifiers(EventRequestModifier.Matcher matcher, Event event) {
        for (int i = 0; i < modifiers.length; i++) {
            if (!modifiers[i].matches(matcher, event)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Does any event specific setup after it has been registered with an event manager.
     *
     * @param vm    the VM being debugged
     */
    public void registered() {}

    /**
     * Does any event specific clearing after it has been de-registered from an event manager.
     *
     * @param vm    the VM being debugged
     */
    public void cleared() {}

    /**
     * Creates an <code>EventRequest</code>.
     *
     * @param kind     the <code>JDWP.EventKind</code> to create
     * @param suspendPolicy the suspend policy to use
     */
    protected EventRequest(int kind, int suspendPolicy) {
        this.id = 0;
        this.kind = kind;
        this.suspendPolicy = suspendPolicy;
        this.modifiers = new EventRequestModifier[0];
        Assert.that(getNameForJDWPSuspendPolicy(this.suspendPolicy) != null);
    }

    /**
     * Creates an <code>EventRequest</code> based on the data read from a JDWP packet.
     *
     * @param in       the <code>PacketInputStream</code> to read the request details from
     * @param vm       the VM being debugged
     * @param kind     the <code>JDWP.EventKind</code> to create
     * @throws SDWPException if the request is invalid
     * @throws IOException if there was an IO error reading the request
     */
    protected EventRequest(int id, PacketInputStream in, int kind) throws SDWPException, IOException {
        this.id = id;
        this.kind = kind;
        this.suspendPolicy = in.readByte("suspendPolicy");
        int numMods = in.readInt("modifiers");
        this.modifiers = new EventRequestModifier[numMods];

        Assert.that(this.id != 0);
        Assert.that(getNameForJDWPSuspendPolicy(this.suspendPolicy) != null);

        for (int i = 0; i < numMods; i++) {
            modifiers[i] = readModifier(in, kind);
        }
    }

    /**
     * Creates a EventRequestModifier from a given <code>PacketInputStream</code>.
     *
     * @param in       the PacketInputStream
     * @param kind     the kind of event request object that the modifier applies to
     * @return         the created EventRequestModifier
     * @throws SDWPException if the data read from <code>in</code> is invalid or not well formed
     * @throws IOException if there is an IO error reading from <code>in</code>
     */
    protected EventRequestModifier readModifier(PacketInputStream in, int kind) throws SDWPException, IOException {
        int modKind = in.readByte("modKind");
        EventRequestModifier modifier;
        switch (modKind) {
            case JDWP.EventRequest_MOD_COUNT:          modifier = new Count(in);                    break;
            case JDWP.EventRequest_MOD_CLASS_ONLY:     modifier = new ClassOnly(in, kind);          break;
            case JDWP.EventRequest_MOD_CLASS_MATCH:    modifier = new ClassMatch(in, kind, false);  break;
            case JDWP.EventRequest_MOD_CLASS_EXCLUDE:  modifier = new ClassMatch(in, kind, true);   break;
            case JDWP.EventRequest_MOD_LOCATION_ONLY:  modifier = new LocationOnly(in, kind);       break;
            case JDWP.EventRequest_MOD_EXCEPTION_ONLY: modifier = new ExceptionOnly(in, kind);      break;
            case JDWP.EventRequest_MOD_STEP:           modifier = new Step(in, kind);               break;
            case JDWP.EventRequest_MOD_THREAD_ONLY:    modifier = new ThreadOnly(in, kind);         break;
            default: throw new SDWPException(JDWP.Error_NOT_IMPLEMENTED, "Unimplemented modkind " + modKind);
        }
        return modifier;
    }

    /**
     * Gets the name string corresponding to a given <code>JDWP.EventKind</code> constant.
     *
     * @param eventKind   the constant to translate
     * @return the name of the event kind, or null if no translation exists
     */
    public static String getNameForJDWPEventKind(int eventKind) {
        switch (eventKind) {
/*if[!FLASH_MEMORY]*/
            case JDWP.EventKind_SINGLE_STEP:        return "SINGLE_STEP";
            case JDWP.EventKind_BREAKPOINT:         return "BREAKPOINT";
            case JDWP.EventKind_FRAME_POP:          return "FRAME_POP";
            case JDWP.EventKind_EXCEPTION:          return "EXCEPTION";
            case JDWP.EventKind_USER_DEFINED:       return "USER_DEFINED";
            case JDWP.EventKind_THREAD_START:       return "THREAD_START";
            case JDWP.EventKind_THREAD_END:         return "THREAD_END";
            case JDWP.EventKind_CLASS_PREPARE:      return "CLASS_PREPARE";
            case JDWP.EventKind_CLASS_UNLOAD:       return "CLASS_UNLOAD";
            case JDWP.EventKind_CLASS_LOAD:         return "CLASS_LOAD";
            case JDWP.EventKind_FIELD_ACCESS:       return "FIELD_ACCESS";
            case JDWP.EventKind_FIELD_MODIFICATION: return "FIELD_MODIFICATION";
            case JDWP.EventKind_EXCEPTION_CATCH:    return "EXCEPTION_CATCH";
            case JDWP.EventKind_METHOD_ENTRY:       return "METHOD_ENTRY";
            case JDWP.EventKind_METHOD_EXIT:        return "METHOD_EXIT";
            case JDWP.EventKind_VM_INIT:            return "VM_INIT";
            case JDWP.EventKind_VM_DEATH:           return "VM_DEATH";
            case JDWP.EventKind_VM_DISCONNECTED:    return "ISOLATE_DEATH";
/*end[FLASH_MEMORY]*/
            default:                                return Integer.toString(eventKind);
        }
    }

    /**
     * Gets the name string corresponding to a given <code>JDWP.SuspendPolicy</code> constant.
     *
     * @param policy  the <code>JDWP.SuspendPolicy</code>  to translate
     * @return        the name of the SuspendPolicy, or null if <oce>policy</code> is invalid.
     */
    public static String getNameForJDWPSuspendPolicy(int policy) {
        switch (policy) {
/*if[!FLASH_MEMORY]*/
            case JDWP.SuspendPolicy_NONE:         return "NONE";
            case JDWP.SuspendPolicy_EVENT_THREAD: return "THREAD";
            case JDWP.SuspendPolicy_ALL:          return "ALL";
/*end[FLASH_MEMORY]*/
            default:                              return Integer.toString(policy);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        String name = this.getClass().getName();
        name = name.substring(name.lastIndexOf('$') + 1);
        String res = "<" + name + " id:" + id
            + " kind:" + getNameForJDWPEventKind(kind)
            + " suspendPolicy:" + getNameForJDWPSuspendPolicy(suspendPolicy)
            + " modifiers[" + modifiers.length + "]:{";
        for (int i = 0; i < modifiers.length; i++) {
            if (modifiers[i] != null) { // can be null during initialization
                res = res + " " + modifiers[i].toString();
            }
        }
        return res + " }>";
    }
}
