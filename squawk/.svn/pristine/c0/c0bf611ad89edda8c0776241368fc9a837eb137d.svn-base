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

import com.sun.squawk.debugger.*;
import com.sun.squawk.debugger.DataType.MethodID;
import com.sun.squawk.*;

/**
 * A proxy for a method.
 *
 */
public class ProxyMethod {

    /**
     * The proxied method.
     */
    private final Method method;

    /**
     * The JDWP identifier for the method.
     */
    private final MethodID id;

    /**
     * The instruction offset to source code line mapping.
     */
    private LineNumberTable lnt;

    /**
     * The local variable table.
     */
    private ScopedLocalVariable[] lvt;

    /**
     * Cache of the argCount calculation.
     */
    private int argCount = -1;

    /**
     * Method should be hidden from Debugger. Pretend that it is a native.
     */
    private boolean hiddenMethod;

    /**
     * @return  the name of this method
     */
    public String getName() {
        return method.getName();
    }

    /**
     * @return the JNI signature of this method
     */
    public String getSignature() {
        return DebuggerSupport.getJNISignature(method);
    }

    /**
     * @return  the modifiers for this method
     * @see     Modifier#getJVMMethodModifiers
     */
    public int getModifiers() {
        return method.getModifiers() & Modifier.getJVMMethodModifiers();
    }

    /**
     * @return  the JDWP identifier for this method
     */
    public MethodID getID() {
        return id;
    }

    /**
     * Creates a proxy for a method.
     *
     * @param id    the method's JDWP identifier
     * @param method the method
     */
    public ProxyMethod(MethodID id, Method method) {
        this.id = id;
        this.method = method;
//        if (method == null || "com.sun.squawk.VMThread".equals(method.getDefiningClass().getInternalName())) {
//            if (Log.info()) {
//                Log.log("Hiding method " + method + " as a native ");
//            }
//            hiddenMethod = true;
//        }
    }

    /**
     * Gets the line number table. A default table is generated if the class has no line number
     * information.
     *
     * @return the line number table.
     */
    public LineNumberTable getLineNumberTable() {
        if (lnt == null) {
            Klass definingKlass = method.getDefiningClass();
            ProxyTypeManager.convertClass(definingKlass);
            int[] table = method.getLineNumberTable();
            if (table == null) {
                if (Log.info()) {
                    Log.log("No linenumber table found for " + method);
                }
                lnt = LineNumberTable.EMPTY_TABLE;
            } else if (hiddenMethod) {
                lnt = LineNumberTable.NATIVE_TABLE;
            } else {
                Object methodBody = DebuggerSupport.getMethodBody(definingKlass, id.getOffset(), id.isStatic());
                int methodLength = VM.getMethodBodyLength(methodBody);

                LineNumberTable.Entry[] entries = new LineNumberTable.Entry[table.length];
                for (int i = 0; i < table.length; i++) {
                    int e = table[i];
                    LineNumberTable.Entry entry = new LineNumberTable.Entry(e >>> 16, e & 0xFFFF);
                    entries[i] = entry;
                }
                lnt = new LineNumberTable(0, methodLength - 1, entries);
            }
        }
        return lnt;
    }

    /**
     * Gets the local variable table. A default table is generated if the class has no local variable
     * information.
     *
     * @return the local variable table.
     */
    public synchronized ScopedLocalVariable[] getVariableTable() {
        if (lvt == null) {
           Klass definingKlass = method.getDefiningClass();
           ProxyTypeManager.convertClass(definingKlass);
           lvt = method.getLocalVariableTable();
           if (lvt == null) {
               if (Log.info()) {
                   Log.log("No local variable table found for " + method);
               }
               Klass[] argTypes = method.getParameterTypes();
               int count = (method.isStatic() ? 0 : 1);
               count += argTypes.length;
               if (Log.debug()) {
                   Log.log("  constructing fake variable table for parameters: " + count);
               }
               lvt = new ScopedLocalVariable[count];
               int slot = 0;

               if (!method.isStatic()) {
                   lvt[argTypes.length] = new ScopedLocalVariable("this", definingKlass, slot++, 0, Integer.MAX_VALUE);
               }

               for (int i = 0; i < argTypes.length; i++) {
                   lvt[i] = new ScopedLocalVariable("arg-"+i, argTypes[i], slot++, 0, Integer.MAX_VALUE);
                   if (argTypes[i] == Klass.LONG 
/*if[FLOATS]*/
                           || argTypes[i] == Klass.DOUBLE
/*end[FLOATS]*/
                           ) {
                       slot++;
                   }
               }

           }
       }
       return lvt;
    }

    /**
     * Gets the number of words in the frame used for arguments (parameters).
     * This will include the slot for the receiver in a virtual method.
     *
     * @return the number of words in the frame used for arguments
     */
    public int getArgCount() {
        if (argCount == -1) {
             Klass[] argTypes = method.getParameterTypes();
             argCount = (method.isStatic() ? 0 : 1);
             for (int i = 0; i < argTypes.length; i++) {
                 Klass klass = argTypes[i];
                 argCount++;
                 if (!Klass.SQUAWK_64) {
                     if (klass == Klass.LONG 
/*if[FLOATS]*/
                             || klass == Klass.DOUBLE
/*end[FLOATS]*/
                             ) {
                         argCount++;
                     }
                 }
             }
         }
         return argCount;
     }


    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "<METHOD id: " + getID() + ", " + getName() + getSignature() + ">";
    }

    /**
     * Encapsulates line number information for the method.
     *
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdwp/jdwp-protocol.html#JDWP_Method_LineTable">LineTable Command</a>
     */
    public static class LineNumberTable {

        public final static Entry[] NO_ENTRIES = new Entry[0];
        public final static LineNumberTable EMPTY_TABLE = new LineNumberTable(0, Integer.MAX_VALUE, LineNumberTable.NO_ENTRIES);
        public final static LineNumberTable NATIVE_TABLE = new LineNumberTable(-1, -1, LineNumberTable.NO_ENTRIES);

        public static class Entry {
            public final long lineCodeIndex;
            public final int lineNumber;

            Entry(long lineCodeIndex, int lineNumber) {
                this.lineCodeIndex = lineCodeIndex;
                this.lineNumber = lineNumber;
            }
        }

        /**
         * Finds the FIRST offset representing the offset with the line number table entry after the one belonging to <code>line</code>.
         *
         * @return the offset or -1 if not found.
         */
        public long getOffsetOfLineAfter(int line) {
            if (line == -1) {
                return -1;
            }
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].lineNumber == line) {
                    if (i + 1 < this.entries.length) {
                        if (Log.debug()) {
                            Log.log("LineNumberTable.getOffsetOfLineAfter(line: " + line + "): returning " + this.entries[i + 1].lineCodeIndex);
                        }
                        return this.entries[i + 1].lineCodeIndex;
                    } else {
                        return -1;
                    }
                }
            }
            return -1;
        }

        /**
         * Looks for the line after the given offset at the given line.  Used for single step debugging.
         *
         * @return the offset or -1 if not found
         */
        public long getOffsetOfLineAfter(long offset, int line) {
            if (offset == -1 || line == -1) {
                return -1;
            }
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].lineNumber == line && this.entries[i].lineCodeIndex == offset) {
                    if (i + 1 < this.entries.length) {
                        if (Log.debug()) {
                            Log.log("LineNumberTable.getOffsetOfLineAfter(line: " + offset + ", " + line + "): returning " + this.entries[i + 1].lineCodeIndex);
                        }
                        return this.entries[i + 1].lineCodeIndex;
                    } else {
                        return -1;
                    }
                }
            }
            return -1;
        }

        /**
         * The "duplicate offset" is an offset that has line number <code>line</code>
         * but is not equal to <code>offset</code>.
         * This method returns the duplicate or -1 if not found.
         */
        public long getDuplicateOffset(long offset, int line) {
            long result = -1;

            if (offset == -1 || line == -1) {
                return result;
            }

            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].lineNumber == line && this.entries[i].lineCodeIndex != offset) {
                    result = this.entries[i].lineCodeIndex;
                    break;
                }
            }
            return result;
        }

        /**
         * Gives the corresponding line number for the given offset.
         *
         * @param offset the offset to be searched for in the line number table
         * @return the line number of <code>offset</code> or -1 if the offset does not match.
         */
        public int getLineNumber(long offset) {
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i].lineCodeIndex == offset) {
                    return this.entries[i].lineNumber;
                }
            }
            if (Log.debug()) {
                Log.log("LineNumberTable.getLineNumber(): Could not find line number for offset " + offset);
            }
            return -1;
        }

        public final long start;
        public final long end;
        public final Entry[] entries;

        LineNumberTable(long start, long end, Entry[] entries) {
            this.start = start;
            this.end = end;
            this.entries = entries;
        }
    }
}
