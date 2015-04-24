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

package com.sun.squawk.util;

import java.io.*;
import com.sun.squawk.*;

/**
 * An instance of <code>StructuredInputStream</code> is used to read a file that
 * must conform to some given format such as a class file or a suite file.
 *
 */
public abstract class StructuredFileInputStream {

    /**
     * The path of the file being read.
     */
    private final String filePath;

    /**
     * The DataInputStream that this class delegates to.
     */
    private final DataInputStream in;

    /**
     * The tracing feature that must be enabled if tracing is to be enabled for the
     * methods in this class.
     */
    private final String traceFeature;

    /**
     * The number of bytes that have been read so far.
     */
    private int bytesRead;

    /**
     * Creates a <code>StructuredFileInputStream</code> that reads class components
     * from a given input stream.
     *
     * @param   in           the input stream
     * @param   filePath     the file from which <code>in</code> was created
     * @param   traceFeature the tracing feature that must be enabled if tracing is to be enabled
     */
    public StructuredFileInputStream(InputStream in, String filePath, String traceFeature) {
        if (in instanceof DataInputStream) {
            this.in = (DataInputStream)in;
        } else {
            this.in = new DataInputStream(in);
        }
        this.filePath = filePath;
        this.traceFeature = traceFeature;
    }

    /**
     * Gets the name of the file from which this reader is reading.
     *
     * @return  the name of the file from which this reader is reading
     */
    public final String getFileName() {
        return filePath;
    }

    /**
     * Throw a LinkageError to indicate there was an IO error or the file did not
     * conform to the structure expected by the client of this class.
     *
     * @param   msg  the cause of the error
     * @return  the LinkageError raised
     */
    public abstract Error formatError(String msg);

    /**
     * Gets the number of bytes that have been read so far. This only accounts for
     * whole units that have been successfully read. For example, if a call to
     * {@link #readInt} failed with an IOException, any bytes read during
     * the call will not be included in the total.
     *
     * @return int
     */
    public final int getBytesRead() {
        return bytesRead;
    }

    /**
     * Reads some bytes from the class file and stores them into the buffer
     * array <code>b</code>. The number of bytes read is equal to the
     * length of <code>b</code>.
     *
     * @param  b  the buffer to fill
     * @param   prefix  the optional prefix used when tracing this read
     */
    public final void readFully(byte[] b, String prefix) {
        try {
            in.readFully(b);
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix + ":{read " + b.length + " bytes}");
            }
            bytesRead += b.length;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Reads an integer from the class file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readInt()
     */
    public final int readInt(String prefix) {
        try {
            int value = in.readInt();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            bytesRead += 4;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Reads an unsigned short from the class file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readUnsignedShort()
     */
    public final int readUnsignedShort(String prefix) {
        try {
            int value = in.readUnsignedShort();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            bytesRead += 2;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Reads an unsigned byte from the file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readUnsignedByte()
     */
    public final int readUnsignedByte(String prefix) {
        try {
            int value = in.readUnsignedByte();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            ++bytesRead;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Reads a signed short from the file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readShort()
     */
    public final int readShort(String prefix) {
        try {
            int value = in.readShort();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            bytesRead += 2;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Reads a signed byte from the file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readByte()
     */
    public final int readByte(String prefix) {
        try {
            int value = in.readByte();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            ++bytesRead;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Reads a long value from the file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readLong()
     */
    public final long readLong(String prefix) {
        try {
            long value = in.readLong();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            bytesRead += 8;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

/*if[FLOATS]*/

    /**
     * Reads a float value from the file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readFloat()
     */
    public final float readFloat(String prefix) {
        try {
            float value = in.readFloat();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            bytesRead += 4;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Reads a double value from the file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readDouble()
     */
    public final double readDouble(String prefix) {
        try {
            double value = in.readDouble();
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            bytesRead += 8;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

/*end[FLOATS]*/

    /**
     * Reads a UTF-8 encoded string from the file.
     *
     * @param   prefix  the optional prefix used when tracing this read
     * @return  the value read
     * @see     DataInputStream#readUTF()
     */
    public final String readUTF(String prefix) {
        try {
            int utflen = in.readUnsignedShort();
            String value = com.sun.squawk.util.DataInputUTF8Decoder.readUTF(in, true, utflen);
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":"+value);
            }
            bytesRead += 2 + utflen;
            return value;
        } catch (EOFException oef) {
            throw formatError("truncated file");
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the file.
     * <p>
     *
     * @param      n   the number of bytes to be skipped.
     * @param   prefix  the optional prefix used when tracing this read
     */
    public final void skip(int n, String prefix) {
        try {
            if (in.skip(n) != n) {
                throw formatError("truncated file");
            }
            if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing(traceFeature, filePath)) {
                Tracer.traceln(prefix+":{skipped " + n + " bytes}");
            }
            bytesRead += n;
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }


    /**
     * Ensures that the input stream is at the end of the file.
     *
     * @throws a LinkageError if the input stream is not at the end of the file
     */
    public void readEOF() {
        /*
         * Ensure there are no extra bytes
         */
        try {
            in.readByte();
        } catch (EOFException e) {
            /* normal case */
            return;
        } catch (IOException ioe) {
        }
        throw formatError("extra bytes in class file");
    }

    /**
     * Closes this reader and releases any system resources
     * associated with the underlying input stream.
     */
    public final void close() {
        try {
            in.close();
        } catch (IOException ioe) {
            throw formatError(ioe.toString());
        }
    }
}
