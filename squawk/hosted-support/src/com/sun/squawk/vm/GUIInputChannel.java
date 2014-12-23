//if[ENABLE_CHANNEL_GUI]
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

import java.util.*;
import com.sun.squawk.vm.ChannelConstants;

/**
 * Special channel for input events.
 */
public class GUIInputChannel extends Channel {

    /**
     * JIT-time constant controlling tracing.
     */
    private static final boolean TRACING_ENABLED = ChannelIO.TRACING_ENABLED;

    /**
     * The queue of input events for this channel.
     */
    /*private*/ final Vector inputQueue = new Vector();

    /**
     *
     */
    private boolean blocked;

    /**
     * Creates the channel.
     *
     * @param cio        the owner of the channel
     * @param channelID  the identifier of the channel
     */
    public GUIInputChannel(ChannelIO cio, int channelID) {
        super(cio, channelID);
        if (channelID != ChannelConstants.CHANNEL_GUIIN) {
            throw new RuntimeException("The GUI input channel must have identifier " + ChannelConstants.CHANNEL_GUIIN);
        }
    }

    /**
     * {@inheritDoc}
     */
    synchronized int execute(
                              int    op,
                              int    i1,
                              int    i2,
                              int    i3,
                              int    i4,
                              int    i5,
                              int    i6,
                              Object o1,
                              Object o2
                            ) {
        switch (op) {
            case ChannelConstants.READLONG: {
                if (inputQueue.size() == 0) {
                    blocked = true;
                    return getEventNumber(); // Block the channel
                }
                Long event = (Long)inputQueue.firstElement();
                inputQueue.removeElementAt(0);
                result = event.longValue();
                if (TRACING_ENABLED) ChannelIO.trace("execute result = "+result);
                break;
            }
            default: throw new RuntimeException("Illegal channel operation "+op);
        }
        return 0;
    }

    /**
     * Unblocks the thread waiting on this channel.
     */
    private void unblock() {
        if (blocked) {
            blocked = false;
            result = 0; // blocked
            cio.unblock(getEventNumber()); // Unblock the channel
        }
    }

    /**
     * Adds an event to the queue of events.
     */
    synchronized void addToGUIInputQueue(int key1_high, int key1_low, int key2_high, int key2_low) {
        long key1 = (key1_high << 16) | (key1_low & 0x0000FFFF);
        long key2 = (key2_high << 16) | (key2_low & 0x0000FFFF);
        long event = key1 << 32 | (key2 & 0x00000000FFFFFFFFL);
//System.err.println("GUIInputChannel["+hashCode()+"," + System.identityHashCode(inputQueue)+ "] - addToGUIInputQueue");
        if (TRACING_ENABLED) traceGUIEvent(event);
        inputQueue.addElement(new Long(event));
        unblock();
    }

    /**
     * Closes this channel.
     */
    public void close() {
        unblock();
    }

    /**
     * Debugging.
     */
    private void traceGUIEvent(long result) {
        int key1 = (int)(result >> 32);
        int key2 = (int)(result);
        int key1_H = (key1 >> 16) & 0xFFFF;
        int key1_L =  key1 & 0xFFFF;
        int key2_H = (key2 >> 16) & 0xFFFF;
        int key2_L =  key2 & 0xFFFF;
        ChannelIO.trace("["+cio.getCIOIndex()+"] "+key1_H+":"+key1_L+":"+key2_H+":"+key2_L);
    }
}

