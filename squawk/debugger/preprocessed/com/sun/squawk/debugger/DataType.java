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

import com.sun.squawk.util.*;

/**
 * This class encapsulates the non-primitive set of common data types that are common to many of the different JDWP
 * commands and replies.
 *
 */
public abstract class DataType {

    public abstract String toString();
    public abstract boolean equals(Object o);
    public abstract int hashCode();

    static abstract class FourByteID extends DataType {

        /**
         * The size (in bytes) of this identifier as sent over a JDWP connection.
         */
        public static final int SIZE = 4;

        /**
         * The 4-byte instance identifier.
         */
        public final int id;

        public FourByteID(int id) {
            this.id = id;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            // Only equal if type of identifer is equal as well as the value of the identifer
            if (o == null) {
                return false;
            }
            return o.getClass() == this.getClass() && ((FourByteID)o).id == this.id;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return id;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return Integer.toString(id);
        }

    }

    /**
     * Represents JDWP identifiers for object instances.
     */
    public static final class ObjectID extends FourByteID {

        /**
         * Constant for the type representing <code>null</code>.
         */
        public final static ObjectID NULL = new ObjectID(0);

        /**
         * Constant for the type representing the dummy thread group.
         */
        public final static ObjectID THREAD_GROUP = new ObjectID(-1);

        public ObjectID(int id) {
            super(id);
        }
    }

    /**
     * Represents JDWP identifiers for object instances prefixed with a one byte type tag.
     */
    public static final class TaggedObjectID extends DataType {

        /**
         * The type tag.
         */
        public final byte tag;

        /**
         * The 4-byte instance identifier.
         */
        public final int id;

        public TaggedObjectID(int tag, int id) {
            this.id = id;
            this.tag = (byte)tag;
            Assert.that(JDWP.isValidTag(tag));
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            if (o instanceof TaggedObjectID) {
                TaggedObjectID other = (TaggedObjectID) o;
                return other.id == id && other.tag == tag;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return tag + id;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "(" + (char)tag + ")" + id;
        }
    }

    /**
     * Represents JDWP identifiers for types.
     */
    public static final class ReferenceTypeID extends FourByteID {

        /**
         * Constant for the type representing <code>null</code>.
         */
        public final static ReferenceTypeID NULL = new ReferenceTypeID(0);

        public ReferenceTypeID(int id) {
            super(id);
        }
    }

    /**
     * Represents JDWP identifiers for methods. These identifiers are only unique within
     * the context of a ReferenceTypeID value.
     */
    public static final class MethodID extends FourByteID {

        public static final MethodID UNKNOWN = new MethodID(0, true);

        /**
         * Constructs a MethodID.
         *
         * @param offset   the offset of the method in the relevent table of methods (static or virtual)
         * @param isStatic specifies if the method is static
         */
        public MethodID(int offset, boolean isStatic) {
            super(encode(offset, isStatic));
        }

        /**
         * Constructs a MethodID from the encoded information send in a KDWP packet.
         *
         * @param encodedID int
         */
        public MethodID(int encodedID) {
            super(encodedID);
        }

        private static int encode(int offset, boolean isStatic) {
            return isStatic ? -(offset + 1) : (offset + 1);
        }

        /**
         * @return the offset of the method in the relevant table of methods
         */
        public int getOffset() {
            return Math.abs(id) - 1;
        }

        /**
         * @return  true if this denotes a static method, false if it denotes a virtual method
         */
        public boolean isStatic() {
            return id < 0;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "" + getOffset() + "_" + (isStatic() ? "static" : "virtual");
        }
    }

    /**
     * Represents JDWP identifiers for fields.
     */
    public static final class FieldID extends DataType {
        /**
         * The size (in bytes) of this identifier as sent over a JDWP connection.
         */
        public static final int SIZE = ReferenceTypeID.SIZE + 4;

        /**
         * The field's encoded tag, offset, and static flag.
         */
        public final int encoding;

        /**
         * The class that defined this field.
         */
        public final ReferenceTypeID definingClass;

        /**
         * Construct a field ID.
         *
         * @param tag int
         * @param offset int
         * @param isStatic boolean
         * @param definingClass ReferenceTypeID
         */
        public FieldID(int tag, int offset, boolean isStatic, ReferenceTypeID definingClass) {
            this.encoding = encode(tag, offset, isStatic);
            this.definingClass = definingClass;
        }

        /**
         * Constructs a FieldID from reading over a JDWPConnection.
         *
         * @param encoding the encoded field value sent over the "wire".
         * @param definingClass ReferenceTypeID
         */
        public FieldID(int encoding, ReferenceTypeID definingClass) {
            this.encoding = encoding;
            this.definingClass = definingClass;
            Assert.that(JDWP.isValidTag(getTag()));
        }

        private static int encode(int tag, int offset, boolean isStatic) {
            Assert.that(JDWP.isValidTag(tag));
            int encoding = offset + 1;
            Assert.that((encoding & 0xFF800000) == 0, "field offset cannot be encoded in 23 bits");
            if (isStatic) {
                encoding = -encoding;
            }
            encoding = (encoding << 8) | (0xFF & tag);
            return encoding;
        }

        /**
         * @return  the basic type of the field (one of the <code>JDWP.Tag_...</code> constants)
         */
        public byte getTag() {
            return (byte)encoding;
        }

        /**
         * @return the offset of the field
         */
        public int getOffset() {
            return (Math.abs(encoding >> 8)) - 1;
        }

        /**
         * @return  true if this denotes a static field, false if it denotes an instance field
         */
        public boolean isStatic() {
            return encoding < 0;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            if (o instanceof FieldID) {
                FieldID other = (FieldID) o;
                return other.encoding == encoding && other.definingClass.equals(definingClass);
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return definingClass.id + encoding;
        }

        public String toString() {
            return "" + (char)getTag() + getOffset() + "_" + (isStatic() ? "static" : "instance");
        }

    }

    /**
     * Represents JDWP identifiers for individual stack frames.
     */
    public static final class FrameID extends DataType {

        /**
         * The size (in bytes) of this identifier as sent over a JDWP connection.
         */
        public static final int SIZE = ObjectID.SIZE + 4;

        /**
         * The JDWP of the thread owning this frame.
         */
        public final ObjectID threadID;

        /**
         * The position of the frame within the call stack. The inner most frame has position 0,
         * it's caller has position 1 etc.
         */
        public final int frame;

        public FrameID(ObjectID thread, int frame) {
            this.threadID = thread;
            this.frame = frame;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            if (o instanceof FrameID) {
                FrameID other = (FrameID) o;
                return other.threadID == threadID && other.frame == frame;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return threadID.id + frame;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "" + threadID.id + "#" + frame;
        }
    }

    /**
     * Represents JDWP identifiers for an executable location.
     */
    public static final class Location extends DataType {

        /**
         * The size (in bytes) of this identifier as sent over a JDWP connection.
         */
        public static final int SIZE = 1 + (ReferenceTypeID.SIZE + MethodID.SIZE) + 8;

        public final byte tag;
        public final ReferenceTypeID definingClass;
        public final MethodID method;
        public final long offset;

        public Location(int tag, ReferenceTypeID definingClass, MethodID method, long offset) {
            this.tag = (byte)tag;
            this.definingClass = definingClass;
            this.method = method;
            this.offset = offset;
            Assert.that(JDWP.isValidTypeTag(tag));
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object o) {
            if (o instanceof Location) {
                Location other = (Location) o;
                return other.tag == tag && other.definingClass == definingClass &&
                    other.method == method && offset == other.offset;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return tag + definingClass.id + method.id + (int)offset;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "(" + tag + ")" + definingClass.id + "#" + method + "@" + offset;
        }
    }

}
