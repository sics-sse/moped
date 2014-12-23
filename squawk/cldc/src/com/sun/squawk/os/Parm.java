/*
 * Copyright 1994-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.os;

/**
 * Base of all the parameter classes.
 *
 */
abstract class BaseParm {

    /**
     * The int value of the parameter
     */
    int ivalue;

    /**
     * The Object value of the parameter (if null then the int value is used)
     */
    Object ovalue;

    /**
     * Pointer to the next parameter
     */
    BaseParm next;

    /**
     * Constructor
     */
    protected BaseParm() {
    }

    /**
     * Constructor
     */
    protected BaseParm(BaseParm head) {
        while (head.next != null) {
            head = head.next;
        }
        head.next = this;
    }

    /**
     * Free all the malloc allocated parameter buffers.
     */
    public void free() {
        if (next != null) {
            next.free();
        }
    }

}

/**
 * Root parameter class.
 *
 */
public class Parm extends BaseParm {

    /**
     * Constructor.
     *
     * @param fnaddress address of the function to be called
     */
    public Parm(int fnaddress) {
        ivalue = fnaddress;
    }

    /**
     * Specify that the function should be called using the JNI calling convention.
     *
     * @return the root parameter
     */
    public Parm jni() {
        ovalue = "JNI";
        return this;
    }

    /**
     * Add an int parameter.
     *
     * @param x the parameter
     * @return the root parameter
     */
    public Parm parm(int x) {
        new IntParm(this, x);
        return this;
    }

    /**
     * Add a long parameter.
     *
     * @param x the parameter
     * @return the root parameter
     */
    public Parm parm(long x) {
        int int1;
        int int2;
        if (CSystem.lowParmFirst) {
            int1 = (int)x;
            int2 = (int)(x>>>32);
        } else {
            int2 = (int)x;
            int1 = (int)(x>>>32);
        }
        return parm(int1).parm(int2);
    }

/*if[FLOATS]*/

    /**
     * Add a float parameter.
     *
     * @param x the parameter
     * @return the root parameter
     */
    public Parm parm(float x) {
        return parm(Float.floatToIntBits(x));
    }

    /**
     * Add a double parameter.
     *
     * @param x the parameter
     * @return the root parameter
     */
    public Parm parm(double x) {
        return parm(Double.doubleToLongBits(x));
    }

/*end[FLOATS]*/

    /**
     * Add an Object parameter.
     *
     * @param x the parameter
     * @return the root parameter
     */
    public Parm parm(Object x) {
        new ObjectParm(this, x);
        return this;
    }

    /**
     * Add a String parameter.
     *
     * @param str the parameter
     * @return the root parameter
     */
    public Parm cstring(String str) {
        int addr = CSystem.mallocString(str);
        new MallocParm(this, addr);
        return this;
    }

    /**
     * Add a byte array parameter.
     *
     * @param bytes the parameter
     * @return the root parameter
     */
    public Parm cbytes(byte[] bytes) {
        int addr = CSystem.mallocBytes(bytes);
        new MallocParm(this, addr);
        return this;
    }

}

/**
 * Parameter class
 */
class IntParm extends BaseParm {
    IntParm(Parm head, int x) {
        super(head);
        ivalue = x;
    }
}

/**
 * Parameter class
 */
class ObjectParm extends BaseParm {
    static {
        if (System.getProperty("microedition.configuration") == null) {
            throw new RuntimeException("Object passing only supported under J2ME");
        }
    }
    ObjectParm(Parm head, Object x) {
        super(head);
        ovalue = x;
    }
}

/**
 * Parameter class
 */
class MallocParm extends IntParm {

    MallocParm(Parm head, int x) {
        super(head, x);
    }

    /**
     * Free all the malloc allocated parameter buffers.
     */
    public void free() {
        if (ivalue != 0) {
            CSystem.free(ivalue);
            ivalue = 0;
        }
        super.free();
    }
}
