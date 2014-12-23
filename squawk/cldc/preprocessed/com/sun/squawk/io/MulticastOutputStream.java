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

package com.sun.squawk.io;

import java.util.*;
import java.io.*;

/**
 * A MulticastOutputStream can be configured to send its output to zero or more named output streams.
 * An instance of MulticastOutputStream is what is returned when opening an output stream via
 * the "multicastoutput://" protocol.
 * <p>
 * <b>Note that this implementation is not synchronized.</b> If
 * multiple threads access a <tt>MulticastOutputStream</tt> instance concurrently, and at
 * least one of the threads adds or removes streams, it <i>must</i> be
 * synchronized externally.
 * <p>
 * <b>This is a Squawk specific class.</b>
 *
 */
public final class MulticastOutputStream extends OutputStream {
    private final static int DEFAULT_SIZE = 3;

    /**
     * The streams to which output should be sent.
     */
    private Hashtable streams = new Hashtable(DEFAULT_SIZE);

    /**
     * Creates a MulticastOutputStream.
     */
    public MulticastOutputStream() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>close</code> method of <code>MulticastOutputStream</code> invokes
     * the <code>write(byte)</code> method of all the contained streams.
     */
    public void write(int b) throws IOException {
        IOException ioe = null;
        for (Enumeration e = streams.elements(); e.hasMoreElements(); ) {
            OutputStream stream = (OutputStream)e.nextElement();
            try {
                stream.write(b);
            } catch (IOException ex) {
                ioe = ex;
            }
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>close</code> method of <code>MulticastOutputStream</code> invokes
     * the <code>write(byte[], int, int)</code> method of all the contained streams.
     */
    public void write(byte b[], int off, int len) throws IOException {
        IOException ioe = null;
        for (Enumeration e = streams.elements(); e.hasMoreElements(); ) {
            OutputStream stream = (OutputStream)e.nextElement();
            try {
                stream.write(b, off, len);
            } catch (IOException ex) {
                ioe = ex;
            }
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>close</code> method of <code>MulticastOutputStream</code> invokes
     * the <code>close</code> method of all the contained streams and then removes
     * them from this multicaster.
     */
    public void close() throws IOException {
        IOException ioe = null;
        for (Enumeration e = streams.elements(); e.hasMoreElements(); ) {
            OutputStream stream = (OutputStream)e.nextElement();
            try {
                stream.close();
            } catch (IOException ex) {
                ioe = ex;
            }
        }
        streams = new Hashtable(DEFAULT_SIZE);
        if (ioe != null) {
            throw ioe;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The <code>flush</code> method of <code>MulticastOutputStream</code> invokes
     * the <code>flush</code> method of all the contained streams.
     */
    public void flush() throws IOException {
        IOException ioe = null;
        for (Enumeration e = streams.elements(); e.hasMoreElements(); ) {
            OutputStream stream = (OutputStream)e.nextElement();
            try {
                stream.flush();
            } catch (IOException ex) {
                ioe = ex;
            }
        }
        if (ioe != null) {
            throw ioe;
        }
    }

    /**
     * Adds a given output stream to those contained by this multicaster.
     *
     * @param name  the name of the stream to add
     * @param out   the stream to add
     * @return  the previous output stream identified by <code>name</code> or null if there wasn't one
     */
    public OutputStream add(String name, OutputStream out) {
        if (name == null || out == null) {
            throw new IllegalArgumentException();
        }
        return (OutputStream)streams.put(name, out);
    }

    /**
     * Removes a given output stream from those contained by this multicaster. The given stream
     * is only removed if it is non-null and {@link Object#equals equal} to a stream contained by this object.
     *
     * @param name  the name of the stream to remove
     * @return  the removed output stream or null if there was no stream identified by <code>name</code>
     */
    public OutputStream remove(String name) {
        return (OutputStream)streams.remove(name);
    }

    /**
     * Removes all the output streams to which this multicaster is redirecting output.
     *
     * @return the enumeration of OutputStream instances removed
     */
    public Enumeration removeAll() {
        Hashtable t = streams;
        streams = new Hashtable(DEFAULT_SIZE);
        return t.elements();
    }

    /**
     * Gets the number of streams contained by this multicaster.
     * Beware that due to the non-synchonized nature of this class, this count may
     * not equal the number of elements returned by {@link #listNames}.
     *
     * @return the number of streams contained by this multicaster
     */
    public int getSize() {
        return streams.size();
    }

    /**
     * Lists all names of the output streams to which this multicaster is redirecting output.
     *
     * @return  an Enumeration over all the names of the streams contained by this multicaster
     */
    public Enumeration listNames() {
        return streams.keys();
    }

    /**
     * Gets the output stream from this multicaster identified by a given name.
     *
     * @param name  the name of the stream to retrieve
     * @return  the output stream identified by <code>name</code> or null if it does not exist
     */
    public OutputStream get(String name) {
        return (OutputStream)streams.get(name);
    }
}
