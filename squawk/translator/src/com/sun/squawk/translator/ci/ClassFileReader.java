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

package com.sun.squawk.translator.ci;

import com.sun.squawk.translator.*;
import com.sun.squawk.util.StructuredFileInputStream;

import java.io.*;

/**
 * An instance of <code>ClassFileReader</code> is used to read an input
 * stream opened on a class file.
 *
 */
public final class ClassFileReader extends StructuredFileInputStream {

    /**
     * Creates a <code>ClassFileReader</code> that reads class components
     * from a given input stream.
     *
     * @param   in        the input stream
     * @param   filePath  the file from which <code>in</code> was created
     */
    public ClassFileReader(InputStream in, String filePath) {
        super(in, filePath, "classfile");
    }

    /**
     * Throw a ClassFormatError instance to indicate there was an IO error
     * or malformed class file error while reading the class.
     *
     * @param   msg  the cause of the error
     * @return  the LinkageError raised
     */
    public Error formatError(String msg) {
        if (msg == null) {
            throw new com.sun.squawk.translator.ClassFormatError(getFileName());
        }
        throw new com.sun.squawk.translator.ClassFormatError(getFileName()+": "+msg);
    }

    /**
     * Starts the decoding of an attribute from the class file. Once the body of the
     * attribute has been decoded, there should be a call to {@link Attribute#close}
     * so that the number of bytes decoded can be verified against the number of
     * bytes expected to be decoded.
     *
     * @param pool   the pool used to decode to the attribute's name
     * @return the header of the attribute about to be decoded
     */
    public Attribute openAttribute(ConstantPool pool) {
        int    nameIndex = readUnsignedShort("attribute_name_index");
        int    length    = readInt("attribute_length");
        String name      = pool.getUtf8(nameIndex);
        return new Attribute(length, getBytesRead(), name);
    }

    /**
     * An Attribute instance encapsulates the common details of all class file attributes.
     */
    public final class Attribute {
        /**
         * The number of bytes in the attribute.
         */
        public final int length;

        /**
         * The name of the attribute.
         */
        public final String name;

        /**
         * The class file offset at which the attribute's body starts.
         */
        private final int start;

        Attribute(int length, int start, String name) {
            this.length = length;
            this.start = start;
            this.name = name;
        }

        /**
         * Forwards the read position of the encapsulating ClassFileReader to the
         * byte immediately after this attribute in the class file.
         */
        public void skip() {
            ClassFileReader.this.skip(length, name);
        }

        /**
         * Ensures that the number of bytes read from the class file while decoding this
         * attribute is equal to the number of bytes specified in this attribute's constructor.
         *
         * @throws ClassFormatError if the number of bytes read is wrong
         */
        public void close() {
            if (getBytesRead() - start != length) {
                formatError("invalid attribute_length for " + name + " attribute");
            }
        }
    }
}
