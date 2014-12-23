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

package com.sun.squawk;

import com.sun.squawk.util.Comparer;
import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.SquawkVector;

/**
 * A <code>MethodMetadata</code> instance represents all the information
 * about a method body that is not absolutely required for execution. This
 * includes the information found in the JVM LineNumberTable
 * class file attributes.
 *
 */
public class MethodMetadata {

    private static boolean preserveLineNumberTables;
    private static boolean preserveLocalVariableTables;

    /**
     * @see #strip(MethodMetadata[])
     */
    static void preserveLineNumberTables() {
        preserveLineNumberTables = true;
    }

    /**
     * @see #strip(MethodMetadata[])
     */
    static void preserveLocalVariableTables() {
        preserveLocalVariableTables = true;
    }

    
    /**
     * @see #strip(MethodMetadata[])
     */
    static boolean lineNumberTablesKept() {
        return preserveLineNumberTables;
    }

    /**
     * @see #strip(MethodMetadata[])
     */
    static boolean localVariableTablesKept() {
        return preserveLocalVariableTables;
    }
    
    /**
     * The line number table.
     *
     * @see  #getLineNumberTable()
     */
    final int [] lnt;
    
    /**
     * The offset of the method in the static/virtual method table.
     */
    final int offset;

    /**
     * Creates a new <code>MethodMetadata</code> instance.
     *
     * @param offset  the offset of the method in the static/virtual method table.
     * @param lvt      the table describing the symbolic information for
     *                      the local variables in the method
     */
    MethodMetadata(int offset, int[] lnt) {
        this.offset = offset;
        this.lnt = lnt;
    }

    /**
     * Creates a new <code>MethodMetadata<code> or <code>FullMethodMetadata</code> instance.
     *
     * @param offset  the offset of the method in the static/virtual method table.
     * @param lnt      the table mapping instruction addresses to the
     *                 source line numbers that start at the addresses.
     *                 The table is encoded as an int array where the high
     *                 16-bits of each element is an instruction address and
     *                 the low 16-bits is the corresponding source line
     * @param lvt      the table describing the symbolic information for
     *                 the local variables in the method
     * @return null if both lnt and lvt are null, otherwise creates a new MethodMetadata.
     */
     static MethodMetadata create(int offset, ScopedLocalVariable[] lvt, int[] lnt) {
/*if[ENABLE_SDA_DEBUGGER]*/
        if (lvt != null) {
            return new FullMethodMetadata(offset, lvt, lnt);
        } else 
/*end[ENABLE_SDA_DEBUGGER]*/
            if (lnt != null) {
            return new MethodMetadata(offset, lnt);
        } else {
            return null;
        }
    }
     
    /**
     * Creates a copy of this object with the line number table stripped if <code>lnt == false</code>
     * and the local variable table stripped if <code>lvt == false</code>. If both parameters are false, returns null.
     *
     * @param lnt  preserve the line number table
     * @param lvt  preserver the local variable table
     * @return the stripped copy of this object or null if <code>lnt == lvt == false</code>
     */
    final MethodMetadata strip(boolean lnt, boolean lvt) {
/*if[ENABLE_SDA_DEBUGGER]*/
        if (lvt && this.getLocalVariableTable() != null) {
            return new FullMethodMetadata(this.offset, this.getLocalVariableTable(), lnt ? this.lnt : null);
        } else  
/*end[ENABLE_SDA_DEBUGGER]*/
            if (lnt && this.getLineNumberTable() != null) {
            return new MethodMetadata(this.offset, this.lnt);
        } else {
            return null;
        }
    }

    /**
     * Creates a stripped copy of an array of MethodMetadata. The value of <code>metadatas</code>
     * is not modified. The line number tables are stripped if {@link #preserveLineNumberTables}
     * has not been called. The local variable tables are stripped if {@link #preserveLineNumberTables}
     * has not been called.
     *
     * @param metadatas  the array to create a stripped copy of
     * @return the stripped copy of <code>metadatas</code>
     */
    static MethodMetadata[] strip(MethodMetadata[] metadatas) {
        if (metadatas != null) {
            if (preserveLineNumberTables || preserveLocalVariableTables) {
                SquawkVector temp = new SquawkVector(metadatas.length);
                for (int i = 0; i != metadatas.length; ++i) {
                    MethodMetadata md = metadatas[i];
                    if (md != null) {
                        temp.addElement(md.strip(preserveLineNumberTables, preserveLocalVariableTables));
                    }
                }
                if (temp.size() == 0) {
                    return null;
                }
                
                MethodMetadata[] result = new MethodMetadata[temp.size()];
                temp.copyInto(result);
                Arrays.sort(result, new MethodMetadataComparer());
                return result;
            }
        }
        return null;
    }

    /**
     * Gets the table mapping instruction addresses to the source line numbers
     * that start at the addresses. The table is encoded as an int array where
     * the high 16-bits of each element is an instruction address and the low
     * 16-bits is the corresponding source line.
     *
     * @return the line number table or null if there is no line number
     *         information for the method
     */
    public final  int[] getLineNumberTable() {
        return lnt;
    }

    /**
     * Gets a table describing the scope, name and type of each local variable
     * in the method.
     *
     * @return the local variable table or null if there is no local variable
     *         information for the method
     */
    public ScopedLocalVariable[] getLocalVariableTable() {
        return null;
    }
    
    /*---------------------------------------------------------------------------*\
     *                 Support stripped MethodMetadata arrays                          *
    \*---------------------------------------------------------------------------*/
    
    final static class MethodMetadataComparer implements Comparer {
        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         * @throws ClassCastException if the arguments' types prevent them from
         * 	       being compared by this Comparer.
         */
        public int compare(Object a, Object b) {
            MethodMetadata ma = (MethodMetadata)a;
            MethodMetadata mb = (MethodMetadata)b;
            return ma.offset - mb.offset;
        }
    }
    
    /**
     * Search through the array of MethodMetadata looking for the metadata for the method with vtable/stable index keyOffset.
     *
     * @param a array of metadata
     * @param keyOffset the offset of the method in the static/virtual method table.
     * @return the index into the MethodMetadata array, or -1 if not found.
     */
    public static int binarySearch(MethodMetadata[] a, int keyOffset) {
        
        int low = 0;
        int high = a.length-1;
        
        while (low <= high) {
            int mid = (low + high) >>> 1;
            MethodMetadata midVal = a[mid];
            int cmp = midVal.offset - keyOffset;
            
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -1;  // key not found.
    }

}
