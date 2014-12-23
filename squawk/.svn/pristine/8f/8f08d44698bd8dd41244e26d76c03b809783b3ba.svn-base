//if[SUITE_VERIFIER]
/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.translator.ir.verifier;

import com.sun.squawk.vm.*;
import com.sun.squawk.util.*;
import java.util.Vector;
import com.sun.squawk.translator.Translator;
import com.sun.squawk.translator.ir.InstructionEmitter;
import com.sun.squawk.*;

/**
 * The Squawk internal bytecode verifier.
 *
 * It verifies the following: <ul>
 * <li>the types read from and written to local variable and parameters
 *       throughout the method match the types of those local variables and
 *       parameters specified by the method</li>
 * <li>the input types to each instruction and method are correct</li>
 * <li>the stack is empty at all backwards branches, method calls, etc.</li>
 * <li>the size of the stack at each point in the method does not depend
 *       on the execution path used to reach that point, and that the types on
 *       the stack are compatible for all execution paths</li>
 * <li>branch targets are all the start of an instruction</li>
 * <li>the start and end of the blocks of code covered by exception handlers
 *       are all the start of an instruction, or in the case of the end of such
 *       a block, one byte past the end of the method</li>
 * <li>the start of each exception handler is the start of an instruction</li>
 * </ul>
 *
 * Like the Java verifier, this verifier performs iterative data flow analysis
 * to determine the types of elements on the stack.  It keeps track of which
 * objects from the object table and integer constants are on the stack and
 * in local variables.
 *
 */
public abstract class VerifierBase {

    /**
     * Define verifier types.
     */
    protected final static Klass OOP       = Klass.REFERENCE,
                                 OBJECT    = Klass.OBJECT,
                                 ADDRESS   = Klass.ADDRESS,
                                 UWORD     = Klass.UWORD,
                                 THROWABLE = Klass.THROWABLE,
                                 INT       = Klass.INT,
                                 LONG      = Klass.LONG,
                                 FLOAT     = Klass.FLOAT,
                                 DOUBLE    = Klass.DOUBLE,
                                 BOOLEAN   = Klass.BOOLEAN,
                                 BYTE      = Klass.BYTE,
                                 SHORT     = Klass.SHORT,
                                 USHORT    = Klass.CHAR,
                                 VOID      = Klass.VOID,
                                 NULL      = Klass.NULL;

    /**
     * These are not really used.  They are just necessary for
     * Verifier.java (which is automatically generated) to be
     * able to compile.
     */
    protected final static int EQ = 0, NE = 1, GT = 2, LT = 3, GE = 4, LE = 5;

    /**
     * The trace flag.
     */
    private boolean trace;

    /**
     * The instruction parameter.
     */
    protected int iparm;

    /**
     * The current opcode.
     */
    protected int opcode;

    /**
     * The method body.
     */
    private MethodBody body;

    /**
     * The class containing the method.
     */
    private Klass klass;

    /**
     * The bytecode array.
     */
    private byte[] code;

    /**
     * The current instruction pointer.
     */
    private int ip;

    /**
     * Stack frame states at various points in the method.
     */
    private Frame frame;

    /**
     * Verify a method body.
     *
     * @param body the method body to verify
     */
    public void verify(MethodBody body) {
        Method method = body.getDefiningMethod();
        this.klass    = body.getDefiningClass();
        this.body     = body;
        this.trace    = Translator.TRACING_ENABLED && Tracer.isTracing("squawkverifier", method.toString());
        code          = body.getCode();
        ip            = 0;
        frame = new Frame(body);

        /*
         * Trace.
         */
        MethodBodyTracer mbt = null;
        if (trace) {
            Tracer.traceln("");
            Tracer.traceln("++++ Squawk verifier trace for " + method + " ++++");
            //Tracer.traceln("maxstack = "+stack.length);
            mbt = new MethodBodyTracer(body);
            mbt.traceHeader();
        }

        /*
         * Iterate over the bytecodes.
         */
        try {
            do {
                while (ip < code.length) {
                    if (trace) {
                        Tracer.trace("sp=" + frame.getSP() + " ");
                    }

                    frame.setIP(ip);
                    opcode = fetchUByte();
                    do_switch();

                    if (trace) {
                        Tracer.traceln(mbt.traceUntil(ip));
                    }
                }
                frame.finished();
            } while (frame.hasChanged());
        } catch (RuntimeException t) {
            String src = getSourceLocation(body, ip);
            System.out.println("Verify error in " + method + "@" + ip + src + ": ");
            System.out.println("body: ");
            new BytecodePrinter(System.out, code).trace();
            t.printStackTrace();
            System.out.println("ir: ");
            mbt = new MethodBodyTracer(body);
            mbt.traceAll();
            throw t;
        } catch (NoClassDefFoundError t) {
            String src = getSourceLocation(body, ip);
            System.out.println("Verify error in " + method + "@" + ip + src + ": ");
            System.out.println("body: ");
            new BytecodePrinter(System.out, code).trace();
            t.printStackTrace();
            System.out.println("ir: ");
            mbt = new MethodBodyTracer(body);
            mbt.traceAll();
            throw t;
        }

        /*
         * Trace.
         */
        if (trace) {
            Tracer.traceln("---- Squawk verifier trace for " + method + " ----");
        }
    }

    private String getSourceLocation (MethodBody body, int ip) {
        String src = "(" + body.getDefiningClass().getSourceFileName();
        MethodMetadata metadata = body.getMetadata();
        if (metadata != null) {
            int[] lnt = metadata.getLineNumberTable();
            src += ":" + Method.getLineNumber(lnt, ip);
        }
        src += ")";
        return src;
    }

    /*-----------------------------------------------------------------------*\
     *                          Bytecode dispatching                         *
    \*-----------------------------------------------------------------------*/


    /**
     * Prefix for bytecode with no parameter.
     */
    protected void iparmNone() {
    }

    /**
     * Prefix for bytecode with a byte parameter.
     */
    protected void iparmByte() {
        iparm = fetchByte();
    }

    /**
     * Prefix for bytecode with an unsigned byte parameter.
     */
    protected void iparmUByte() {
        iparm = fetchUByte();
    }

    /**
     * Execute the next bytecode.
     */
    abstract protected void do_switch();

    /**
     * Add 256 to the next unsigned byte and jump to that bytecode execution.
     */
    protected void do_escape() {
        opcode = fetchUByte() + 256;
        do_switch();
    }

    /**
     * Or the (parameter<<8) into the value of the next bytecode and then
     * dispatch to the wide version of the opcode.
     * @param n
     */
    protected void do_wide(int n) {
        opcode = fetchUByte() + OPC.Properties.WIDE_DELTA;
        iparm  = fetchUByte() | (n<<8);
        do_switch();
    }

    /**
     * Load the inlined short as the value of the next bytecode and then
     * dispatch to the wide version of the opcode.
     */
    protected void do_wide_short() {
        opcode = fetchUByte() + OPC.Properties.WIDE_DELTA;
        iparm  = fetchShort();
        do_switch();
    }

    /**
     * Load the inlined int as the value of the next bytecode and then
     * dispatch to the wide version of the opcode.
     */
    protected void do_wide_int() {
        opcode = fetchUByte() + OPC.Properties.WIDE_DELTA;
        iparm  = fetchInt();
        do_switch();
    }

    /**
     * Or the (parameter<<8) in to the value of the next bytecode and then
     * dispatch to the wide version of the opcode.
     * @param n
     */
    protected void do_escape_wide(int n) {
        opcode = fetchUByte() + 256 + OPC.Properties.ESCAPE_WIDE_DELTA;
        iparm  = fetchUByte() | (n<<8);
        do_switch();
    }

    /**
     * Load the inlined short as the value of the next bytecode and then
     * dispatch to the wide version of the opcode.
     */
    protected void do_escape_wide_short() {
        opcode = fetchUByte() + 256 + OPC.Properties.ESCAPE_WIDE_DELTA;
        iparm  = fetchShort();
        do_switch();
    }

    /**
     * Load the inlined int as the value of the next bytecode and then
     * dispatch to the wide version of the opcode.
     */
    protected void do_escape_wide_int() {
        opcode = fetchUByte() + 256 + OPC.Properties.ESCAPE_WIDE_DELTA;
        iparm  = fetchInt();
        do_switch();
    }


    /*-----------------------------------------------------------------------*\
     *                           Instruction decoding                        *
    \*-----------------------------------------------------------------------*/

    /**
     * Fetch a byte from ip++.
     *
     * @return the value
     */
    protected int fetchByte() {
        return code[ip++];
    }

    /**
     * Fetch an unsigned byte from from ip++.
     *
     * @return the value
     */
    protected int fetchUByte() {
        return fetchByte() & 0xFF;
    }

    /**
     * Fetch a short from ip++.
     *
     * @return the value
     */
    protected int fetchShort() {
        if (!VM.isBigEndian()) {
            int b1 = fetchUByte();
            int b2 = fetchByte();
            return (b2 << 8) | b1;
        } else {
            int b1 = fetchByte();
            int b2 = fetchUByte();
            return (b1 << 8) | b2;
        }
    }

    /**
     * Fetch a unsigned short from ip++.
     *
     * @return the value
     */
    protected int fetchUShort() {
        int b1 = fetchUByte();
        int b2 = fetchUByte();
        if (!VM.isBigEndian()) {
            return (b2 << 8) | b1;
        } else {
            return (b1 << 8) | b2;
        }
    }

    /**
     * Fetch an int from ip++.
     *
     * @return the value
     */
    protected int fetchInt() {
        int b1 = fetchUByte();
        int b2 = fetchUByte();
        int b3 = fetchUByte();
        int b4 = fetchUByte();
        if (!VM.isBigEndian()) {
            return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        } else {
            return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        }
    }

    /**
     * Fetch a long from ip++.
     *
     * @return the value
     */
    protected long fetchLong() {
        long b1 = fetchUByte();
        long b2 = fetchUByte();
        long b3 = fetchUByte();
        long b4 = fetchUByte();
        long b5 = fetchUByte();
        long b6 = fetchUByte();
        long b7 = fetchUByte();
        long b8 = fetchUByte();
        if (!VM.isBigEndian()) {
            return (b8 << 56) | (b7 << 48) | (b6 << 40) | (b5 << 32) | (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        } else {
            return (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
        }
    }

/*if[FLOATS]*/
    /**
     * Fetch a float from ip++.
     *
     * @return the value
     */
    protected float fetchFloat() {
        return Float.intBitsToFloat(fetchInt());
    }

    /**
     * Fetch a double from ip++.
     *
     * @return the value
     */
    protected double fetchDouble() {
        return Double.longBitsToDouble(fetchLong());
    }
/*end[FLOATS]*/



    /*-----------------------------------------------------------------------*\
     *                               Verification                            *
    \*-----------------------------------------------------------------------*/

    /**
     * Throw a VerifyError if the given condition is false.
     *
     * @param check the condition to check
     * @param msg error message if the condition is false
     */
    private void check(boolean cond, String msg) {
        VerifyError.check(cond, frame.getIP(), msg);
    }

    /**
     * Throw a VerifyError if the given condition is false.
     * This will throw an error with no error message.
     *
     * @param check the condition to check
     * @see #check(boolean,java.lang.String)
     */
    private void check(boolean cond) {
        VerifyError.check(cond, frame.getIP());
    }

    /**
     * Grow a type to the corresponding type used as an instance field.
     * This concept is meaningless for floating point or reference types,
     * so in those cases this method simply returns its input.  For integer
     * types, boolean variables are stored as bytes.
     */
    private static Klass growInstance(Klass k) {
        if (k == BOOLEAN)
            return BYTE;
        else
            return k;
    }

    /**
     * Grow a type to the corresponding type used as a static field, local
     * variable, parameter, or stack element.
     * This concept is meaningless for floating point or reference types,
     * so in those cases this method simply returns its input.  For integer
     * types, everything smaller than int is represented as int.
     */
    private static Klass grow(Klass k) {
        if (k == BOOLEAN || k == USHORT || k == SHORT || k == BYTE) {
            return INT;
        } else {
            return k;
        }
    }

    /**
     * Check that the given specific type is assignable to the given general
     * type.  The general type is the type given by some bytecode, such as int for _i,
     * oop for _o, or short for _s.  The specific type is the derived type.
     * If it is not assignable, throw a VerifyError.
     *
     * @param generalType OOP, INT, FLOAT, etc.
     * @param specificType exact derived type from the stack
     */
    private void checkGeneralType(Klass generalType, Klass specificType) {
        check(Frame.isAssignable((generalType == OOP) ? OBJECT : generalType, specificType),
              "" + specificType + " not assignable to " + generalType);
    }


    /*-----------------------------------------------------------------------*\
     *                               Instructions                            *
    \*-----------------------------------------------------------------------*/

    protected void do_const_null() {
        frame.push(NULL);
        frame.fallthrough();
    }

    private void do_const(int n, Klass type) {
        frame.pushConstInt(n, type);
        frame.fallthrough();
    }

    protected void do_const(int n) {
        do_const(n, (n == 0 || n == 1) ? BOOLEAN : BYTE);
    }

    protected void do_object(int n) {
        frame.pushObject(klass.getObject(n));
        frame.fallthrough();
    }

    protected void do_loadparm(int n) {
        frame.loadparm(n);
        frame.fallthrough();
    }

    protected void do_load(int n) {
        frame.load(n);
        frame.fallthrough();
    }

    protected void do_storeparm(int n) {
        frame.storeparm(n);
        frame.fallthrough();
    }

    protected void do_store(int n) {
        frame.store(n);
        frame.fallthrough();
    }

    protected void do_const_byte() {
        do_const(fetchByte(), BYTE);
    }

    protected void do_const_short() {
        do_const(fetchShort(), SHORT);
    }

    protected void do_const_char() {
        do_const(fetchUShort(), USHORT);
    }

    protected void do_const_int() {
        do_const(fetchInt(), INT);
    }

    protected void do_const_long() {
        fetchLong();
        frame.push(LONG);
        frame.fallthrough();
    }

    protected void do_const_float() {
/*if[FLOATS]*/
        fetchFloat();
        frame.push(FLOAT);
        frame.fallthrough();
/*else[FLOATS]*/
//      throw new Error("No floating point");
/*end[FLOATS]*/
    }

    protected void do_const_double() {
/*if[FLOATS]*/
        fetchDouble();
        frame.push(DOUBLE);
        frame.fallthrough();
/*else[FLOATS]*/
//      throw new Error("No floating point");
/*end[FLOATS]*/
    }

    protected void do_object() {
        do_object(iparm);
    }

    protected void do_load() {
        do_load(iparm);
    }

    protected void do_load_i2() {
        do_load(iparm);
    }

    protected void do_store() {
        do_store(iparm);
    }

    protected void do_store_i2() {
        do_store(iparm);
    }

    protected void do_loadparm() {
        do_loadparm(iparm);
    }

    protected void do_loadparm_i2() {
        do_loadparm(iparm);
    }

    protected void do_storeparm() {
        do_storeparm(iparm);
    }

    protected void do_storeparm_i2() {
        do_storeparm(iparm);
    }

    private void do_stack_inc(int c) {
        Integer i = frame.popConstInt();
        if (i == null)
            frame.push(INT);
        else
            frame.pushConstInt(i.intValue() + c);
    }

    private void do_inc(int c) {
        frame.load(iparm);
        do_stack_inc(c);
        frame.store(iparm);
        frame.fallthrough();
    }

    protected void do_inc() {
        do_inc(1);
    }

    protected void do_dec() {
        do_inc(-1);
    }

    private void do_incparm(int c) {
        frame.loadparm(iparm);
        do_stack_inc(c);
        frame.storeparm(iparm);
        frame.fallthrough();
    }

    protected void do_incparm() {
        do_incparm(1);
    }

    protected void do_decparm() {
        do_incparm(-1);
    }

    private void setTarget(int current, int offset) {
        int target = current + offset;
        if (offset >= 0) {
            frame.addTarget(target);
        } else {
            frame.mayCauseGC();
            if (!frame.isStackEmpty())
                frame.printStack();
            check(frame.isStackEmpty(), "stack not empty at backward branch");
            check((code[target]&0xFF) == OPC.BBTARGET_SYS || (code[target]&0xFF) == OPC.BBTARGET_APP,
                  "backward branch does not target bbtarget instruction");
            frame.addBackwardsTarget(target);
        }
    }

    protected void do_goto() {
        setTarget(ip, iparm);
    }

    protected void do_if(int operands, int cc, Klass t) {
        frame.pop(t);
        if (operands == 2) {
            frame.pop(t);
        }
        setTarget(ip, iparm);
        frame.fallthrough();
    }

    private static int getFieldByteOffset(Klass t, int offset) {
        if (t == DOUBLE || t == LONG || t == OOP) {
            return HDR.BYTES_PER_WORD * offset;
        } else {
            return t.getDataSize() * offset;
        }
    }

    private static int getOffset(Field f, boolean isStatic) {
        if (isStatic){
            return f.getOffset();
        } else {
            return getFieldByteOffset(f.getType(), f.getOffset());
        }
    }

    private Klass getType(Klass startK, int offset, boolean isStatic) {
        Klass k = startK;
        while (k != null && k.getFieldCount(isStatic) == 0) {
            k = k.getSuperclass();
        }
        check(k!= null, "could not find field with offset " + offset + " in " + startK);
        int startOffset = getOffset(k.getField(0, isStatic), isStatic);
        while (startOffset > offset) {
            do {
                k = k.getSuperclass();
            } while (k.getFieldCount(isStatic) == 0);
            startOffset = getOffset(k.getField(0, isStatic), isStatic);
        }
        for (int i = 0; i < k.getFieldCount(isStatic); i++) {
            Field f = k.getField(i, isStatic);
            check(getOffset(f, isStatic) <= offset,
                  "could not find field with offset " + offset + " in " + k);
            if (getOffset(f, isStatic) == offset) {
                return f.getType();
            }
        }
        check(false, "could not find field with offset " + offset + " in " + k);
        return null;
    }

    private Klass getStaticType(Klass k, int offset) {
        return getType(k, offset - CS.firstVariable, true);
    }

    private Klass getInstanceType(Klass k, int offset) {
        return getType(k, offset, false);
    }

    private void checkStaticFieldOffset(int offset, boolean isRef, Klass klass) {
        check(offset >= 0, "invalid static field offset");
        if (isRef) {
            check(offset < klass.getRefStaticFieldsSize(), "invalid reference static field offset");
        } else {
            check(offset >= klass.getRefStaticFieldsSize() && offset < klass.getStaticFieldsSize(), "invalid reference static field offset");
        }
    }

    protected void do_getstatic(Klass t) {
        frame.mayCauseGC();
        Klass fklass = (Klass)frame.popObject();
        checkStaticFieldOffset(iparm - CS.firstVariable, t == OOP, fklass);
        Klass fieldKlass = getStaticType(fklass, iparm);
        check(frame.isStackEmpty(), "stack not empty at getstatic");
        frame.push(fieldKlass);
        checkGeneralType(t, fieldKlass);
        frame.fallthrough();
    }

    protected void do_class_getstatic(Klass t) {
        frame.pushObject(klass);
        do_getstatic(t);
    }

    protected void do_putstatic(Klass t) {
        frame.mayCauseGC();
        Klass fklass = (Klass)frame.popObject();
        checkStaticFieldOffset(iparm - CS.firstVariable, t == OOP, fklass);
        Klass fieldKlass = getStaticType(fklass, iparm);
        frame.pop(fieldKlass);
        check(frame.isStackEmpty(), "stack not empty at putstatic");
        checkGeneralType(t, fieldKlass);
        frame.fallthrough();
    }

    protected void do_class_putstatic(Klass t) {
        frame.pushObject(klass);
        do_putstatic(t);
    }

    protected void do_getfield(Klass t) {
        frame.mayCauseGC();
        Klass fklass = frame.pop();
        check(!fklass.isPrimitive(), "attempted to get a field from a primitive type");
        check(!fklass.isArray(), "attempted to get a field from an array");
        if (fklass != NULL) {
            Klass fieldKlass = getInstanceType(fklass, getFieldByteOffset(t, iparm));
            frame.push(fieldKlass);
            checkGeneralType(t, fieldKlass);
        } else {
            if (t == OOP) {
                t = OBJECT;
            }
            frame.push(t);
        }
        frame.fallthrough();
    }

    protected void do_getfield0(Klass t) {
        frame.loadparm(0);
        do_getfield(t);
    }

    private void do_putfield_internal(Klass t, Klass klass, Klass valueKlass) {
        frame.mayCauseGC();
        check(!klass.isPrimitive(), "attempted to put a field to a primitive type");
        check(!klass.isArray(), "attempted to put a field to an array");
        if (klass != NULL) {
            Klass fieldKlass = getInstanceType(klass, getFieldByteOffset(t, iparm));
            check(Frame.isAssignable(growInstance(fieldKlass), valueKlass),
                  "" + valueKlass + " not assignable to " + growInstance(fieldKlass));
            checkGeneralType(t, fieldKlass);
        }
        frame.fallthrough();
    }

    protected void do_putfield(Klass t) {
        Klass valueKlass = frame.pop();
        Klass fklass = frame.pop();
        do_putfield_internal(t, fklass, valueKlass);
    }

    protected void do_putfield0(Klass t) {
        Klass valueKlass = frame.pop();
        frame.loadparm(0);
        Klass fklass = frame.pop();
        do_putfield_internal(t, fklass, valueKlass);
    }

    private Method getVirtualMethod(Klass k, int index) {
        if (k == null) {
            return null;
        }

        // Look for the method in a superclass.  If we find it,
        // we don't care if it's overridden, because all we need
        // is the signature.
        Method m = getVirtualMethod(k.getSuperclass(), index);
        if (m != null) {
            return m;
        }

        // Look for the virtual method here.
        for (int i = 0; i < k.getMethodCount(false); i++) {
            m = k.getMethod(i, false);
            if (!m.isHosted() && m.getOffset() == index) {
                return m;
            }
        }
        return null;
    }

    private static Method getStaticMethod(Klass k, int index) {
        if (k == null) {
            return null;
        }

        int mcount = k.getMethodCount(true);
        for (int i = 0; i < mcount; i++) {
            Method m = k.getMethod(i, true);
            if (!m.isHosted() && m.getOffset() == index) {
                return m;
            }
        }
        return getStaticMethod(k.getSuperclass(), index);
    }

    private void do_invoke(Method m, Klass t, Klass rtype) {
        frame.mayCauseGC();
        Klass params[] = m.getParameterTypes();
        if (Translator.REVERSE_PARAMETERS) {
            for (int i = 0; i < params.length; i++) {
                frame.pop(grow(params[i]));
            }
        } else {
            for (int i = params.length - 1; i >= 0; i--) {
                frame.pop(grow(params[i]));
            }
        }
        check(frame.isStackEmpty(), "stack not empty after popping parameters to callee");
        checkGeneralType(t, rtype);
        if (t != VOID) {
            frame.push(rtype);
        }
        frame.fallthrough();
    }
    
    private void do_invoke(Method m, Klass t) {
        do_invoke(m, t, m.getReturnType());
    }

    protected void do_invokevirtual(Klass t) {
        Klass fklass = null;
        if (Translator.REVERSE_PARAMETERS) {
            fklass = frame.pop();
        } else {
            Vector stack = new Vector();
            while (!frame.isStackEmpty()) {
                fklass = frame.pop();
                stack.addElement(fklass);
            }
            for (int i = stack.size() - 2; i >= 0; i--) {
                frame.push((Klass)stack.elementAt(i));
            }
        }
        if (fklass == NULL) {
            while (!frame.isStackEmpty()) {
                frame.pop();
            }
            if (t != VOID) {
                frame.push((t == OOP) ? NULL : t);
            }
            frame.fallthrough();
        } else {
            if (fklass.isInterface()) {
                fklass = Klass.OBJECT;
            }
            Method m = getVirtualMethod(fklass, iparm);
            check(m != null, "could not find virtual method of index " + iparm + " in " + fklass);
            do_invoke(m, t);
        }
    }
    protected void do_invokestatic(Klass t) {
        Klass fklass = (Klass)frame.popObject();
        Method m = getStaticMethod(fklass, iparm);
        check(m.getDefiningClass() == fklass, "method " + m + " is not defined in " + fklass);
        check(m != null, "could not find static method of index " + iparm + " in " + fklass);
        if (m.isConstructor()) {
            boolean isChainedConstructor = false;
            Method caller = this.body.getDefiningMethod();
            Klass params[] = m.getParameterTypes();
            frame.mayCauseGC();
            
            if (!Translator.REVERSE_PARAMETERS) {
                for (int i = params.length - 1; i >= 0; i--) {
                    frame.pop(grow(params[i]));
                }
            }
            
            if (m.isReplacementConstructor()) {
                frame.pop(m.getDefiningClass());
            } else {
                isChainedConstructor = caller.isConstructor()
                && (caller.getDefiningClass() == m.getDefiningClass() ||
                        caller.getDefiningClass().getSuperclass() == m.getDefiningClass())
                        && frame.isParmUninitialized(0);
                frame.popForInitialization(m.getDefiningClass());
            }
            
            if (Translator.REVERSE_PARAMETERS) {
                for (int i = 0; i < params.length; i++) {
                    frame.pop(grow(params[i]));
                }
            }
            
            if (!frame.isStackEmpty()) {
                frame.printStack();
            }
            check(frame.isStackEmpty(), "stack not empty after popping parameters to callee");
            Klass rtype = m.getReturnType();
            if (isChainedConstructor) {
                rtype = caller.getDefiningClass();
            }
            checkGeneralType(t, rtype);
            if (t != VOID) {
                frame.push(rtype);
            }
            frame.fallthrough();
        } else {
            do_invoke(m, t);
        }
    }

    protected void do_invokesuper(Klass t) {
        Klass superklass = (Klass)frame.popObject();
        Klass fklass = null;
        if (Translator.REVERSE_PARAMETERS) {
            fklass = frame.pop();
        } else {
            Vector stack = new Vector();
            while (!frame.isStackEmpty()) {
                fklass = frame.pop();
                stack.addElement(fklass);
            }
            for (int i = stack.size() - 2; i >= 0; i--) {
                frame.push((Klass)stack.elementAt(i));
            }
        }
        check(Frame.isAssignable(superklass, fklass), "invalid superclass");
        Method m = getVirtualMethod(superklass, iparm);
        do_invoke(m, t);
    }

    protected void do_invokenative(Klass t) {
        frame.mayCauseGC();
        if (iparm == Native.com_sun_squawk_VM$getGlobalOop) {
            Integer ii = frame.popConstInt();
            check(frame.isStackEmpty(), "stack not empty after popping parameters to com.sun.squawk.VM.getGlobalOop");
            if (ii != null) {
                int i = ii.intValue();
                String name = InstructionEmitter.getGlobalOopVariable(i);
                if (name != null) {
                    int lastPeriod = name.lastIndexOf('.');
                    String fieldname = name.substring(lastPeriod + 1);
                    String classname = name.substring(0, lastPeriod);
                    String internalClassname = "L" + classname.replace('.', '/') + ";";
                    Klass k = Klass.getClass(internalClassname, true);
                    for (int j = 0; j < k.getFieldCount(true); j++) {
                        Field f = k.getField(j, true);
                        if (fieldname.equals(f.getName())) {
                            frame.push(f.getType());
                            frame.fallthrough();
                            return;
                        }
                    }
                }

                frame.pushConstInt(i);
            }
            else
                frame.push(INT);
        }
        NativeVerifierHelper.do_invokenative(frame, iparm);
        frame.fallthrough();
    }

    protected void do_findslot() {
        frame.mayCauseGC();
        Klass interfaceklass = (Klass)frame.popObject();
        frame.pop();
        check(frame.isStackEmpty(), "stack not empty after popping input to findslot instruction");
        Method m = getVirtualMethod(interfaceklass, iparm);
        frame.pushMethodSlot(m);
        frame.fallthrough();
    }

    protected void do_invokeslot(Klass t) {
        frame.mayCauseGC();
        Method m = frame.popMethodSlot();
        if (Translator.REVERSE_PARAMETERS) {
            frame.pop(m.getDefiningClass());
        } else {
            Vector stack = new Vector();
            Klass fklass = null;
            while (!frame.isStackEmpty()) {
                fklass = frame.pop();
                stack.addElement(fklass);
            }
            check(Frame.isAssignable(m.getDefiningClass(), fklass), "input to invokeslot does not match input to findslot");
            for (int i = stack.size() - 2; i >= 0; i--) {
                frame.push((Klass)stack.elementAt(i));
            }
        }
        do_invoke(m, t);
    }

    protected void do_return(Klass t) {
        if (t != VOID) {
            Klass type = frame.pop();
            checkGeneralType(t, type);
        }
        frame.doReturn();
        //Assert.that(frame.isStackEmpty()); <-- Not a valid assumption for the TCK
    }

    protected void do_tableswitch(Klass t) {
        frame.pop(INT);
        int size = (t == SHORT) ? 2 : 4;
        while ((ip % size) != 0) {
            fetchByte();
        }
        int low  = getSwitchEntry(size);
        int high = getSwitchEntry(size);
        int loc  = getSwitchEntry(size);
        int pos  = ip;
        setTarget(pos, loc);
        for (int i = low; i <= high; i++) {
            loc = getSwitchEntry(size);
            setTarget(pos, loc);
        }
    }

    private int getSwitchEntry(int size) {
        if (size == 2) {
            return fetchShort();
        } else {
            return fetchInt();
        }
    }

    protected void do_extend0() {
        check(frame.getIP() == 0, "extend bytecode at somewhere other than the beginning of the method");
        check(frame.isStackEmpty(), "stack not empty at extend bytecode");
        frame.fallthrough();
    }

    protected void do_extend() {
        check(frame.getIP() == 0, "extend bytecode at somewhere other than the beginning of the method");
        check(frame.isStackEmpty(), "stack not empty at extend bytecode");
        for (int i = 1; i <= iparm; i++) {
            do_const_null();
            do_store(i);
        }
        frame.fallthrough();
    }

    protected void do_add(Klass t) {
        frame.pop(t);
        frame.pop(t);
        frame.push(t);
        frame.fallthrough();
    }

    protected void do_sub(Klass t) {
        do_add(t);
    }

    protected void do_and(Klass t) {
        Klass k1 = frame.pop(t);
        Klass k2 = frame.pop(t);
        if (t == INT) {
            if (k1 == BOOLEAN)
                frame.push(k2);
            else if (k2 == BOOLEAN)
                frame.push(k1);
            else if (k1 == BYTE)
                frame.push(k2);
            else if (k2 == BYTE)
                frame.push(k1);
            else
                frame.push(INT);
        }
        else
            frame.push(t);
        frame.fallthrough();
    }

    protected void do_or(Klass t) {
        do_and(t);
    }

    protected void do_xor(Klass t) {
        do_and(t);
    }

    protected void do_shl(Klass t) {
        frame.pop(INT);
        frame.pop(t);
        frame.push(t);
        frame.fallthrough();
    }

    protected void do_shr(Klass t) {
        do_shl(t);
    }

    protected void do_ushr(Klass t) {
        do_shl(t);
    }

    protected void do_mul(Klass t) {
        do_add(t);
    }

    protected void do_div(Klass t) {
        if (t != FLOAT && t != DOUBLE) {
            frame.mayCauseGC();
        }
        do_add(t);
    }

    protected void do_rem(Klass t) {
        if (t != FLOAT && t != DOUBLE) {
            frame.mayCauseGC();
        }
        do_add(t);
    }

    protected void do_neg(Klass t) {
        frame.pop(t);
        frame.push(t);
        frame.fallthrough();
    }

    protected void do_throw() {
        frame.mayCauseGC();
        frame.pop(THROWABLE);
        check(frame.isStackEmpty(), "stack not empty after throw instruction");
    }

    protected void do_catch() {
        frame.mayCauseGC();
        frame.clearStack();
        frame.pushHandlerException();
        frame.fallthrough();
    }

    protected void do_pop(int n) {
        while (n > 0) {
            Klass k = frame.pop();
            if (!Klass.SQUAWK_64 && (k == DOUBLE || k == LONG))
                n -= 2;
            else
                n--;
        }
        check(n == 0, "attempted to split a two-word primitive");
        frame.fallthrough();
    }

    protected void do_monitorenter() {
        frame.mayCauseGC();
        frame.pop(OOP);
        check(frame.isStackEmpty(), "stack not empty on monitorenter");
        frame.fallthrough();
    }

    protected void do_monitorexit() {
        frame.mayCauseGC();
        frame.pop(OOP);
        check(frame.isStackEmpty(), "stack not empty on monitorexit");
        frame.fallthrough();
    }

    protected void do_class_monitorenter() {
        frame.mayCauseGC();
        check(frame.isStackEmpty(), "stack not empty on class_monitorenter");
        frame.fallthrough();
    }

    protected void do_class_monitorexit() {
        frame.mayCauseGC();
        check(frame.isStackEmpty(), "stack not empty on class_monitorexit");
        frame.fallthrough();
    }

    protected void do_arraylength() {
        frame.mayCauseGC();
        Klass fklass = frame.pop();
        check(fklass == NULL || fklass.isArray(), "attempted to get a length of something other than an array");
        frame.push(INT);
        frame.fallthrough();
    }

    protected void do_new() {
        frame.mayCauseGC();
        Klass fklass = (Klass)frame.popObject();
        check(frame.isStackEmpty(), "stack not empty on new instruction");
        frame.pushUninitialized(fklass);
        frame.fallthrough();
    }

    protected void do_newarray() {
        frame.mayCauseGC();
        Klass fklass = (Klass)frame.popObject();
        frame.pop(INT);
        check(frame.isStackEmpty(), "stack not empty on newarray instruction");
        frame.push(fklass);
        frame.fallthrough();
    }

    protected void do_newdimension() {
        frame.mayCauseGC();
        frame.pop(INT);
        Klass fklass = frame.pop();
        check(frame.isStackEmpty(), "stack not empty on newdimension instruction");
        frame.push(fklass);
        frame.fallthrough();
    }

    protected void do_class_clinit() {
        frame.mayCauseGC();
        check(frame.isStackEmpty(), "stack not empty on class_clinit instruction");
        frame.fallthrough();
    }

    protected void do_bbtarget_sys() {
        frame.bbtarget();
        check(frame.isStackEmpty(), "stack not empty at backward branch target");
        frame.fallthrough();
    }

    protected void do_bbtarget_app() {
        do_bbtarget_sys();
    }

    protected void do_instanceof() {
        frame.mayCauseGC();
        Klass fklass = (Klass)frame.popObject();
        frame.pop(OOP);
        check(frame.isStackEmpty(), "stack not empty on instanceof");
        frame.push(BOOLEAN);
        frame.fallthrough();
    }

    protected void do_checkcast() {
        frame.mayCauseGC();
        Klass castklass = (Klass)frame.popObject();
        Klass fklass = frame.pop();
        check(frame.isStackEmpty(), "stack not empty on checkcast");
        frame.push(castklass);
        frame.fallthrough();
    }

    protected void do_aload(Klass t) {
        frame.mayCauseGC();
        frame.pop(INT);
        Klass fklass = frame.pop();
        if (fklass != NULL) {
            check(fklass.isArray(), "attempted to aload from something other than an array");
            Klass componentKlass = fklass.getComponentType();
            checkGeneralType(t, componentKlass);
            frame.push(componentKlass);
        }
        else
            frame.push((t == OOP) ? NULL : t);
        frame.fallthrough();
    }

    protected void do_astore(Klass t) {
        frame.mayCauseGC();
        Klass valueKlass = frame.pop();
        frame.pop(INT);
        Klass arrayKlass = frame.pop();
        if (arrayKlass != NULL) {
            check(arrayKlass.isArray(), "attempted to astore to something other than an array");
        }
        if (t == OOP) {
            check(frame.isStackEmpty(), "stack not empty after astore");
        }
        frame.fallthrough();
    }

    protected void do_lookup(Klass t) {
        frame.mayCauseGC();
        Klass arrayKlass = frame.pop();
        check(arrayKlass.isArray(), "" + arrayKlass + " is not an array type");
        check(Frame.isAssignable(arrayKlass.getComponentType(), BYTE), "array input to lookup is of wrong type");
        frame.pop(INT);
        check(frame.isStackEmpty(), "stack not empty on lookup");
        frame.push(INT);
        frame.fallthrough();
    }

    protected void do_res(int n) {
        // TODO: Is this right?
        frame.fallthrough();
    }

    protected void do_pause() {
        // TODO: Is this right?
        frame.fallthrough();
    }

    private void do_conversion(Klass from, Klass to) {
        frame.pop(from);
        frame.push(to);
        frame.fallthrough();
    }

    protected void do_i2b() {
        do_conversion(INT, BYTE);
    }

    protected void do_i2s() {
        do_conversion(INT, SHORT);
    }

    protected void do_i2c() {
        do_conversion(INT, USHORT);
    }

    protected void do_l2i() {
        do_conversion(LONG, INT);
    }

    protected void do_i2l() {
        do_conversion(INT, LONG);
    }

    protected void do_i2f() {
        do_conversion(INT, FLOAT);
    }

    protected void do_l2f() {
        do_conversion(LONG, FLOAT);
    }

    protected void do_f2i() {
        do_conversion(FLOAT, INT);
    }

    protected void do_f2l() {
        do_conversion(FLOAT, LONG);
    }

    protected void do_i2d() {
        do_conversion(INT, DOUBLE);
    }

    protected void do_l2d() {
        do_conversion(LONG, DOUBLE);
    }

    protected void do_f2d() {
        do_conversion(FLOAT, DOUBLE);
    }

    protected void do_d2i() {
        do_conversion(DOUBLE, INT);
    }

    protected void do_d2l() {
        do_conversion(DOUBLE, LONG);
    }

    protected void do_d2f() {
        do_conversion(DOUBLE, FLOAT);
    }

    protected void do_fcmpl() {
/*if[FLOATS]*/
        frame.pop(FLOAT);
        frame.pop(FLOAT);
        frame.push(INT);
        frame.fallthrough();
/*else[FLOATS]*/
//      throw new Error("No floating point");
/*end[FLOATS]*/
    }
    
    protected void do_fcmpg() {
        do_fcmpl();
    }
    
    protected void do_dcmpl() {
/*if[FLOATS]*/
        frame.pop(DOUBLE);
        frame.pop(DOUBLE);
        frame.push(INT);
        frame.fallthrough();
/*else[FLOATS]*/
//      throw new Error("No floating point");
/*end[FLOATS]*/
    }
    
    protected void do_dcmpg() {
        do_dcmpl();
    }

}

class BytecodePrinter extends BytecodeTracer {
    private java.io.PrintStream out;
    private byte code[];
    private int pos;

    BytecodePrinter(java.io.PrintStream out, byte code[]) {
        this.out = out;
        this.code = code;
        pos = 0;
    }

    protected int getByte() {
        return code[pos++];
    }

    protected int getCurrentPosition() {
        return pos;
    }

    protected void print(String str) {
        out.print(str);
    }

    private String align(int n) {
        String s = "" + n;
        while (s.length() < 5)
            s = " " + s;
        return s;
    }

    public void trace() {
        while (pos < code.length) {
            out.print(align(pos) + ": ");
            traceByteCode();
            out.println();
        }
    }
}
