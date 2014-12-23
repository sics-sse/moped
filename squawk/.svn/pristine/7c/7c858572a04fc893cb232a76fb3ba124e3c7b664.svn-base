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

package java.lang;

import java.io.*;

import com.sun.squawk.GC;
import com.sun.squawk.Isolate;
import com.sun.squawk.Klass;
import com.sun.squawk.VM;
import com.sun.squawk.util.Assert;
import com.sun.cldc.i18n.Helper;

/**
 * The <code>System</code> class contains several useful class fields
 * and methods. It cannot be instantiated.
 *
 * @version 1.110, 12/04/99 (CLDC 1.0, Spring 2000)
 * @since   JDK1.0
 */
public final class System {

    /*
     * Threading must be initialized and class Klass initialized before System can be initialized.
     */
    static {
        Assert.that(com.sun.squawk.VMThread.currentThread() != null);
        Assert.that(VM.getCurrentIsolate().isClassKlassInitialized());
    }

    /**
     * Don't let anyone instantiate this class
     */
    private System() { }

    /**
     * The "standard" output stream. This stream is already
     * open and ready to accept output data. Typically this stream
     * corresponds to display output or another output destination
     * specified by the host environment or user.
     * <p>
     * For simple stand-alone Java applications, a typical way to write
     * a line of output data is:
     * <blockquote><pre>
     *     System.out.println(data)
     * </pre></blockquote>
     * <p>
     * See the <code>println</code> methods in class <code>PrintStream</code>.
     *
     * @see     java.io.PrintStream#println()
     * @see     java.io.PrintStream#println(boolean)
     * @see     java.io.PrintStream#println(char)
     * @see     java.io.PrintStream#println(char[])
     * @see     java.io.PrintStream#println(int)
     * @see     java.io.PrintStream#println(long)
     * @see     java.io.PrintStream#println(java.lang.Object)
     * @see     java.io.PrintStream#println(java.lang.String)
     */
    public final static PrintStream out = getOutput(false);

    private static PrintStream getOutput(boolean err) {
        String url = System.getProperty(err ? "java.lang.System.err" : "java.lang.System.out");
        if (url == null) {
            url = err ? "debug:err" : "debug:";
        }
        Isolate isolate = VM.getCurrentIsolate();
        OutputStream os;
        if (err) {
            isolate.addErr(url);
            os = isolate.stderr;
        } else {
            isolate.addOut(url);
            os = isolate.stdout;
        }
        return new PrintStream(os);
    }

    /**
     * The "standard" error output stream. This stream is already
     * open and ready to accept output data.
     * <p>
     * Typically this stream corresponds to display output or another
     * output destination specified by the host environment or user. By
     * convention, this output stream is used to display error messages
     * or other information that should come to the immediate attention
     * of a user even if the principal output stream, the value of the
     * variable <code>out</code>, has been redirected to a file or other
     * destination that is typically not continuously monitored.
     */
    public final static PrintStream err = getOutput(true);

    /**
     * Returns the current time in milliseconds.
     *
     * @return  the difference, measured in milliseconds, between the current
     *          time and midnight, January 1, 1970 UTC.
     */
    public static long currentTimeMillis() {
        return VM.getTimeMillis();
    }

    /**
     * Copies an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * A subsequence of array components are copied from the source
     * array referenced by <code>src</code> to the destination array
     * referenced by <code>dst</code>. The number of components copied is
     * equal to the <code>length</code> argument. The components at
     * positions <code>srcOffset</code> through
     * <code>srcOffset+length-1</code> in the source array are copied into
     * positions <code>dstOffset</code> through
     * <code>dstOffset+length-1</code>, respectively, of the destination
     * array.
     * <p>
     * If the <code>src</code> and <code>dst</code> arguments refer to the
     * same array object, then the copying is performed as if the
     * components at positions <code>srcOffset</code> through
     * <code>srcOffset+length-1</code> were first copied to a temporary
     * array with <code>length</code> components and then the contents of
     * the temporary array were copied into positions
     * <code>dstOffset</code> through <code>dstOffset+length-1</code> of the
     * destination array.
     * <p>
     * If <code>dst</code> is <code>null</code>, then a
     * <code>NullPointerException</code> is thrown.
     * <p>
     * If <code>src</code> is <code>null</code>, then a
     * <code>NullPointerException</code> is thrown and the destination
     * array is not modified.
     * <p>
     * Otherwise, if any of the following is true, an
     * <code>ArrayStoreException</code> is thrown and the destination is
     * not modified:
     * <ul>
     * <li>The <code>src</code> argument refers to an object that is not an
     *     array.
     * <li>The <code>dst</code> argument refers to an object that is not an
     *     array.
     * <li>The <code>src</code> argument and <code>dst</code> argument refer to
     *     arrays whose component types are different primitive types.
     * <li>The <code>src</code> argument refers to an array with a primitive
     *     component type and the <code>dst</code> argument refers to an array
     *     with a reference component type.
     * <li>The <code>src</code> argument refers to an array with a reference
     *     component type and the <code>dst</code> argument refers to an array
     *     with a primitive component type.
     * </ul>
     * <p>
     * Otherwise, if any of the following is true, an
     * <code>IndexOutOfBoundsException</code> is
     * thrown and the destination is not modified:
     * <ul>
     * <li>The <code>srcOffset</code> argument is negative.
     * <li>The <code>dstOffset</code> argument is negative.
     * <li>The <code>length</code> argument is negative.
     * <li><code>srcOffset+length</code> is greater than
     *     <code>src.length</code>, the length of the source array.
     * <li><code>dstOffset+length</code> is greater than
     *     <code>dst.length</code>, the length of the destination array.
     * </ul>
     * <p>
     * Otherwise, if any actual component of the source array from
     * position <code>srcOffset</code> through
     * <code>srcOffset+length-1</code> cannot be converted to the component
     * type of the destination array by assignment conversion, an
     * <code>ArrayStoreException</code> is thrown. In this case, let
     * <b><i>k</i></b> be the smallest nonnegative integer less than
     * length such that <code>src[srcOffset+</code><i>k</i><code>]</code>
     * cannot be converted to the component type of the destination
     * array; when the exception is thrown, source array components from
     * positions <code>srcOffset</code> through
     * <code>srcOffset+</code><i>k</i><code>-1</code>
     * will already have been copied to destination array positions
     * <code>dstOffset</code> through
     * <code>dstOffset+</code><i>k</I><code>-1</code> and no other
     * positions of the destination array will have been modified.
     * (Because of the restrictions already itemized, this
     * paragraph effectively applies only to the situation where both
     * arrays have component types that are reference types.)
     *
     * @param      src          the source array.
     * @param      srcPos       start position in the source array.
     * @param      dst          the destination array.
     * @param      dstPos       start position in the destination data.
     * @param      length       the number of array elements to be copied.
     * @exception  IndexOutOfBoundsException  if copying would cause
     *               access of data outside array bounds.
     * @exception  ArrayStoreException  if an element in the <code>src</code>
     *               array could not be stored into the <code>dest</code> array
     *               because of a type mismatch.
     * @exception  NullPointerException if either <code>src</code> or
     *               <code>dst</code> is <code>null</code>.
     */
    public static void arraycopy(Object src, int srcPos, Object dst, int dstPos, int length) {
        if ((src == null) || (dst == null)) {
            throw new NullPointerException();
        }

        Klass srcClass = GC.getKlass(src);
        Klass dstClass = GC.getKlass(dst);
        if (!srcClass.isArray() || !dstClass.isArray()) {
            throw new ArrayStoreException();
        }

        Klass srcComponentType = srcClass.getComponentType();
        Klass dstComponentType = dstClass.getComponentType();
        boolean primitive  = srcComponentType.isPrimitive() ||
                             dstComponentType.isPrimitive() ||
                             srcComponentType.isSquawkPrimitive() ||
                             dstComponentType.isSquawkPrimitive();
        if (primitive && srcComponentType != dstComponentType) {
            throw new ArrayStoreException(srcComponentType.getName() + " != " + dstComponentType.getName());
        }

        int srcEnd = length + srcPos;
        int dstEnd = length + dstPos;
        if (
                (length < 0) ||
                (srcPos < 0) ||
                (dstPos < 0) ||
                (length > 0 && (srcEnd < 0 || dstEnd < 0)) ||
                (srcEnd > GC.getArrayLength(src)) ||
                (dstEnd > GC.getArrayLength(dst))
           ) {
            throw new ArrayIndexOutOfBoundsException();
        }
        
        if (length > 0) {
            if (!primitive) {
                if (!dstComponentType.isAssignableFrom(srcComponentType)) {
                    Object[] srcArray = (Object[]) src;
                    Object[] dstArray = (Object[]) dst;
                    for (int i = 0; i < length; i++) {
                        Object item = srcArray[srcPos + i];
                        if (item != null && !dstComponentType.isAssignableFrom(GC.getKlass(item))) {
                            throw new ArrayStoreException();
                        }
                        dstArray[dstPos + i] = item;
                    }
                } else {
                    VM.arraycopyObject0(src, srcPos, dst, dstPos, length);
                }
            } else {
                VM.arraycopyPrimitive0(src, srcPos, dst, dstPos, length, srcComponentType.getDataSize());
            }
        }
    }

    /**
     * Returns the same hashcode for the given object as
     * would be returned by the default method hashCode(),
     * whether or not the given object's class overrides
     * hashCode().
     * The hashcode for the null reference is zero.
     *
     * @param x object for which the hashCode is to be calculated
     * @return  the hashCode
     * @since   JDK1.1
     */
    public static int identityHashCode(Object x) {
        return x == null ? 0 : GC.getHashCode(x);
    }

    /**
     * Gets the system property indicated by the specified key.
     *
     * @param      key   the name of the system property.
     * @return     the string value of the system property,
     *             or <code>null</code> if there is no property with that key.
     *
     * @exception  NullPointerException if <code>key</code> is
     *             <code>null</code>.
     * @exception  IllegalArgumentException if <code>key</code> is empty.
     */
    public static String getProperty(String key) {
        if (key == null) {
            throw new NullPointerException(/*"key can't be null"*/);
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException(/*"key can't be empty"*/);
        }

        /*
         * These are the hard-coded properties that cannot be changed.
         */
        if (key.equals("microedition.configuration"))                   { return "CLDC-1.1"; }
        if (key.equals("microedition.encoding"))                        { return Helper.defaultEncoding; }
        if (key.equals("microedition.locale"))                          { return "en-US"; }
        if (key.equals("microedition.platform"))                        { return "j2me"; }
//      if (key.equals("microedition.profiles"))                        { return MIDlet.SUPPORTED_PROFILE; }
        if (key.equals("microedition.profiles"))                        { return "IMP-1.0"; }
/*if[!FLASH_MEMORY]*/
        if (key.equals("awtcore.classbase"))                            { return "awtcore.impl.squawk"; }
/*end[FLASH_MEMORY]*/
        if (key.equals("javax.microedition.io.Connector.protocolpath")) { return "com.sun.squawk.io"; }
        if (key.equals("file.separator"))	                            { return "" + VM.getFileSeparatorChar(); }
        if (key.equals("path.separator"))                               { return "" + VM.getPathSeparatorChar(); }

        if (VM.getCurrentIsolate() == null) {
            return null;
        }
        String value = VM.getCurrentIsolate().getProperty(key);
        return value;
    }

    /**
     * Terminates the currently running Java application. The
     * argument serves as a status code; by convention, a nonzero
     * status code indicates abnormal termination.
     * <p>
     * If called by a MIDlet, a SecurityException will be thrown. MIDlets should call MIDlet.notifyDestroyed() instead.
     * <p>
     * This method calls the <code>exit</code> method in class
     * <code>Runtime</code>. This method never returns normally.
     * <p>
     * The call <code>System.exit(n)</code> is effectively equivalent
     * to the call:
     * <blockquote><pre>
     * Runtime.getRuntime().exit(n)
     * </pre></blockquote>
     *
     * @param      status   exit status.
     * @see        java.lang.Runtime#exit(int)
     */
    public static void exit(int status) {
        Runtime.getRuntime().exit(status);
    }

    /**
     * Runs the garbage collector.
     * <p>
     * Calling the <code>gc</code> method suggests that the Java Virtual
     * Machine expend effort toward recycling unused objects in order to
     * make the memory they currently occupy available for quick reuse.
     * When control returns from the method call, the Java Virtual
     * Machine has made a best effort to reclaim space from all discarded
     * objects.
     * <p>
     * The call <code>System.gc()</code> is effectively equivalent to the
     * call:
     * <blockquote><pre>
     * Runtime.getRuntime().gc()
     * </pre></blockquote>
     *
     * @see     java.lang.Runtime#gc()
     */
    public static void gc() {
        Runtime.getRuntime().gc();
    }

}


