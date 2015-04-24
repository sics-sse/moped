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

import java.io.*;
import javax.microedition.io.*;

/**
 * The base class for the specialized channels for stream IO, graphic operations and event handling.
 */
public abstract class Channel implements java.io.Serializable {

    /**
     * The identifier of this channel.
     */
    private final int channelID;

    /**
     * The result of the last operation on this channel.
     */
    protected long result;

    /**
     * The owner of this channel.
     */
    protected final ChannelIO cio;

    /**
     * The event number used for blocking.
     */
    private transient Integer eventNumber;

    /**
     * Constructor.
     *
     * @param cio        the owner of this channel
     * @param channelID  the identifier of this channel
     */
    public Channel(ChannelIO cio, int channelID) {
        this.cio = cio;
        this.channelID = channelID;
    }

    /**
     * Executes an operation on the given channel.
     *
     * @param  op  the operation to perform
     * @param  i1
     * @param  i2
     * @param  i3
     * @param  i4
     * @param  i5
     * @param  i6
     * @param  o1
     * @param  o2
     * @param  o3
     * @return the result
     */
    abstract int execute(
                          int    op,
                          int    i1,
                          int    i2,
                          int    i3,
                          int    i4,
                          int    i5,
                          int    i6,
                          Object o1,
                          Object o2
                        ) throws IOException;


    /**
     * Clear the result.
     */
    public final void clearResult() {
        result = 0;
    }

    /**
     * Gets the result of the last successful operation on this channel. The value
     * returned is undefined if the last operation on this channel was not successful
     * or did not generate a result value.
     *
     * @return long the result of the last successful operation on this channel
     */
    public final long getResult() {
        return result;
    }

    /**
     * Gets the identifier of this channel.
     *
     * @return  the identifier of this channel
     */
    public final int getChannelID() {
        return channelID;
    }

    /**
     * Gets the event number used for blocking this channel.
     *
     * @return  the event number used for blocking this channel
     */
    public synchronized final int getEventNumber() {
        if (eventNumber == null) {
            eventNumber = new Integer(EventQueue.getNextEventNumber());
        }
        return eventNumber.intValue();
    }

    /**
     * Closes this channel.
     */
    public abstract void close();
}

