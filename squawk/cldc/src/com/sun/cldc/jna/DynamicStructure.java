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

package com.sun.cldc.jna;

/**
 * A DynamicStructure is a structure with support for getting the field offsets 
 * for a particular platform from native code.
 * 
 * A native data "layout" structure must be defined that contains the offsets. By convention,
 * the name of the data structure is CLASSNAME+"_layout", where CLASSNAME is
 * equal to class.getName().replace('.', '_').replace('$', '_');
 * 
 * The C layout structure is an array of 4-byte words. The first element is the length total of this layout structure itself in words.
 * The second element is the size of the structure being described in bytes.
 * The remaining elements are the offsets of the fields of interest to the java code. The order isn't important, but it's typically in
 * the order that the fields are defined in the C and java structures.
 * 
 * The Java format of the layout is an int array. It's like the C layout, but without first "layout length" field.
 * The first element of the Java is the size of the C structure in bytes.
 * 
 * Example:
 * 
 * IN C:
 * #define com_sun_squawk_platform_posix_callouts_Libc_Stat_layout_LEN 5
 * const int com_sun_squawk_platform_posix_callouts_Libc_Stat_layout[com_sun_squawk_platform_posix_callouts_Libc_Stat_layout_LEN] = {
 *         com_sun_squawk_platform_posix_callouts_Libc_Stat_layout_LEN, 
 *         sizeof(struct stat),
 *         offsetof(struct stat, st_mode),
 *         offsetof(struct stat, st_mtime),
 *         offsetof(struct stat, st_size)
 *     }
 * 
 * IN JAVA:
 * 
 * package com.sun.squawk.platform.posix.callouts;
 *
 * class LibC {
 * 
 *      static class Stat extends DynamicStructure {
 *          final static int ST_MODE_INDEX   = 1;
 *          final static int ST_MTIME_INDEX  = 2;
 *          final static int ST_SIZE_INDEX   = 3;
 *
 *          final static int[] layout = DynamicStructure.initLayout(Stat.class, 3);
 * 
 *          public int[] getLayout() {
 *              return layout;
 *          }
 * 
 *          public void read() {
 *               Pointer p = getPointer();
 *               st_mode  = p.getShort(layout[ST_MODE_INDEX]) & 65535;
 *               st_mtime = p.getInt(layout[ST_MTIME_INDEX]);
 *               st_size  = p.getLong(layout[ST_SIZE_INDEX]);
 *           }
 *       
 *          ....
 *      }
 *      ....
 * }
 */
public abstract class DynamicStructure extends Structure {
    public final static boolean DEBUG = false;

    /** The first element of the layout structure in Java is the size of the C structure in bytes */
    public final static int STRUCTURE_SIZE_INDEX = 0;
    
    /**
     * Read the C layout structure into a Java array.
     * 
     * This is typically called by a subclass an stored in a static.
     * @param c the class
     * @return a array of ints of length numFields + 1 containing the layout information
     * @throws IllegalStateException if the C structure has less than numFields items
     */
    protected static int[] initLayout(Class c, int numFields) {
        String name = c.getName().replace('.', '_').replace('$', '_')+ "_layout";
        VarPointer p =  NativeLibrary.getDefaultInstance().getGlobalVariableAddress(name, (numFields + 2) * 4);
        int len = p.getInt(0);
        if (len - 2 < numFields) {
            throw new IllegalStateException();
        }
        if (DEBUG) {    System.out.println("Loading Structure defn for " + name); }
        int[] result = new int[numFields + 1];
        if (DEBUG) {    System.out.println("    native Layout fields: " + len + ", requested: " + (numFields + 2)); }

        for (int i = 1; i < numFields + 2; i++) {
            result[i-1] = p.getInt(i * 4);
        if (DEBUG) {    System.out.println("    layoutdata: " +  result[i-1]); }
        }

        return result;
    }

    /** 
     * Return the structure layout used by this class. Typical implementations return 
     * a static variable that has been initialized with initLayout.
     * 
     * @return the layout
     */
     public abstract int[] getLayout();
     
     /**
      * Return the size of this structure.
      * The size is the first element of the layout array.
      * 
      * @return the size of the native C structure in bytes
      */
     public int size() {
         return getLayout()[STRUCTURE_SIZE_INDEX];
     }
     
}
