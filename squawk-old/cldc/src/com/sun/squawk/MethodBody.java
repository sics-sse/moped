/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle. All Rights Reserved.
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
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */

package com.sun.squawk;

import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;

/**
 * An instance of <code>MethodBody</code> represents the Squawk bytecode for
 * a method as well as all the other information related to the bytecode
 * such as exception handler tables, oop map for the activation frame etc.
 *
 */
public final class MethodBody {

    /**
     * The enclosing method.
     */
    private final Method definingMethod;

    /**
     * The maximum size (in words) of the operand stack during execution
     * of this method.
     */
    private final int maxStack;

    /**
     * The number of words required by the parameters.
     */
    private final int parametersCount;

    /**
     * The exception handler table.
     */
    private final ExceptionHandler[] exceptionTable;

    /**
     * The debug information for the method.
     */
    private final MethodMetadata metadata;

    /**
     * The type map of the parameters and locals.
     */
    private final Klass[] localTypes;

    /**
     * The Squawk bytecode.
     */
    private final byte[] code;
    
    /**
     * Record if this is a constructor that inlined it call to a super constructor.
     */
    private boolean inlinedSuperConstructor;

/*if[TYPEMAP]*/
    /**
     * The type map describing the type of the value (if any) written to memory by each instruction in 'code'.
     */
    private final byte[] typeMap;
/*end[TYPEMAP]*/

    /**
     * Create a <code>MethodBody</code> representing a dummy object for the <code>ObjectGraphLoader</code>.
     */
    MethodBody() {
    	this.definingMethod = null;
    	this.maxStack = -1;
    	this.parametersCount = -1;
    	this.exceptionTable = null;
    	this.metadata = null;
    	this.localTypes = null;
    	this.code = null;
/*if[TYPEMAP]*/
        this.typeMap = null;
/*end[TYPEMAP]*/
    }
    
    /**
     * Creates a <code>MethodBody</code> representing the implementation details
     * of a method.
     *
     * @param definingMethod    the method in which the method body was defined
     * @param maxStack          the maximum size in words of the operand stack
     * @param locals            the types of the local variables (excludes parameters)
     * @param exceptionTable    the exception handler table
     * @param lnt               the table mapping instruction addresses to the
     *                          source line numbers that start at the addresses.
     *                          The table is encoded as an int array where the high
     *                          16-bits of each element is an instruction address and
     *                          the low 16-bits is the corresponding source line
     * @param lvt               the table describing the symbolic information for
     *                          the local variables in the method
     * @param code              the Squawk bytecode
     * @param typeMap           the type map describing the type of the value (if any) written
     *                          to memory by each instruction in 'code'
     * @param reverseParameters true if the parameters are pushed right-to-left
     * @param inlinedSuperConstructor True IFF this is a constructor that inlined java.lang.Object.<init>. 
     */
    public MethodBody(
                       Method                definingMethod,
                       int                   maxStack,
                       Klass[]               locals,
                       ExceptionHandler[]    exceptionTable,
                       int[]                 lnt,
                       ScopedLocalVariable[] lvt,
                       byte[]                code,
                       byte[]                typeMap,
                       boolean               reverseParameters,
                       boolean               inlinedSuperConstructor
                     ) {
        this.definingMethod  = definingMethod;
        this.maxStack        = maxStack;
        this.exceptionTable  = exceptionTable;
        this.metadata        = MethodMetadata.create(definingMethod.getOffset(), lvt, lnt);
        this.code = code;
/*if[TYPEMAP]*/
        this.typeMap = typeMap;
/*end[TYPEMAP]*/
        this.inlinedSuperConstructor = inlinedSuperConstructor;

        /*
         * Make an array of classes with both the parameter and local types.
         */
        Klass[] parms   = definingMethod.getRuntimeParameterTypes(reverseParameters);
        parametersCount = parms.length;

        localTypes = new Klass[parms.length+locals.length];

        int j = 0;
        for (int i = 0 ; i < parms.length ; i++, j++) {
            localTypes[j] = parms[i];
        }
        for (int i = 0 ; i < locals.length ; i++, j++) {
            localTypes[j] = locals[i];
        }

        Assert.that(parametersCount >= 0);
        Assert.that(maxStack >= 0);
    }

    /**
     * Produce String for debugging
     *
     * @return the string
     */
    public String toString() {
        return "[bytecode for "+definingMethod.getDefiningClass().getName()+"."+definingMethod.getName();
    }

    /**
     * Gets the bytecode.
     *
     * @return the bytecode
     */
    public byte[] getCode() {
        return code;
    }

/*if[TYPEMAP]*/
    /**
     * Gets the type map describing the types in activation frame expected by each bytecode.
     *
     * @return the type map describing the types in activation frame expected by each bytecode
     */
    public byte[] getTypeMap() {
        return typeMap;
    }
/*end[TYPEMAP]*/

    /**
     * Get the type map.
     *
     * @return the type map
     */
    public Klass[] getTypes() {
        return localTypes;
    }

    /**
     * Gets the class that defined this method.
     *
     * @return the class that defined this method
     */
    public Method getDefiningMethod() {
        return definingMethod;
    }

    /**
     * Gets the class that defined this method.
     *
     * @return the class that defined this method
     */
    public Klass getDefiningClass() {
        return definingMethod.getDefiningClass();
    }

    /**
     * Get the number of parameters.
     *
     * @return the number
     */
    public int getParametersCount() {
        return parametersCount;
    }

    /**
     * Get the exception table.
     *
     * @return the number
     */
    public ExceptionHandler[] getExceptionTable() {
        return exceptionTable;
    }

    /**
     * Get the number of stack words needed.
     *
     * @return the number
     */
    public int getMaxStack() {
        return maxStack;
    }

    /**
     * Gets the debug information (if any) pertaining to this method body.
     *
     * @return  the debug information pertaining to this method body or null
     *          if there isn't any
     */
    public MethodMetadata getMetadata() {
        return metadata;
    }
    
    /**
     * True IFF this is a constructor that inlined java.lang.Object.<init>. Used by squawk verifier.
     * @return
     */
    public boolean getInlinedSuperConstructor() {
        return inlinedSuperConstructor;
    }

    /*-----------------------------------------------------------------------*\
     *                                Encoding                               *
    \*-----------------------------------------------------------------------*/

    /**
     * Encode the method header. 
     *
     * @param enc encoder
     */
    void encodeHeader(ByteBufferEncoder enc) {
        int start ;
        int localsCount = localTypes.length - parametersCount;

        /*
         * Encode the type table.
         */
        start = enc.getSize();
        if (MethodHeader.ENABLE_SPECIFIC_TYPE_TABLES) {
            for (int i = 0 ; i < localTypes.length ; i++) {
                Klass k = localTypes[i];
                switch (k.getSystemID()) {
                    case CID.FLOAT:
                    case CID.LONG:
                    case CID.DOUBLE:
                    case CID.ADDRESS:
                    case CID.UWORD:
                    case CID.OFFSET:
                        enc.addUnsignedShort(k.getSystemID());
                        enc.addUnsignedInt(i);
                        break;
                }
            }
        }
        int typeTableSize = enc.getSize() - start;

        /*
         * Encode the exception table.
         */
        start = enc.getSize();
        if (exceptionTable != null) {
            for(int i = 0 ; i < exceptionTable.length ; i++) {
                ExceptionHandler handler = exceptionTable[i];
                enc.addUnsignedShort(handler.getStart());
                enc.addUnsignedShort(handler.getEnd());
                enc.addUnsignedShort(handler.getHandler());
                int handlerTypeIndex = definingMethod.getDefiningClass().getObjectIndex(handler.getKlass());
                if (handlerTypeIndex < 0 || handlerTypeIndex > 0xFFFF) {
                    System.out.println("index off in exception table for " + definingMethod + ", " + handlerTypeIndex + ", for handler class " + handler.getKlass());
                }
                enc.addUnsignedShort(handlerTypeIndex);
            }
        }
        int exceptionTableSize = enc.getSize() - start;

        /*
         * Encode the oopmap.
         */
        start = enc.getSize();
        int count = localTypes.length;
        int next = 0;
        while (count > 0) {
            int bite = 0;
            int n = (count < 8) ? count : 8;
            count -= n;
            for (int i = 0 ; i < n ; i++) {
                Klass k = localTypes[next++];
                if (k.isReferenceType()) {
                    bite |= (1<<i);
                }
            }
            enc.addUnsignedByte(bite);
        }

        Assert.that((enc.getSize() - start) == ((localsCount+parametersCount+7)/8));
        Assert.that(typeTableSize      < 32768);
        Assert.that(exceptionTableSize < 32768);
        Assert.that(localsCount        < 32768);
        Assert.that(parametersCount    < 32768);
        Assert.that(maxStack           < 32768);

        /*
         * Write the minfo area.
         *
         * The minfo is written in reverse. There are two formats, a compact one where there is no
         * type table, exception table, and the number of words for local variables,
         * parameters, and stack are all less than 32 words, and there is a large format where the only
         * limits are that none of these values may exceed 32767.
         */
        if (!MethodHeader.FORCE_LARGE_FORMAT     &&
            localsCount        < 32 &&
            parametersCount    < 32 &&
            maxStack           < 32 &&
            typeTableSize      == 0 &&
            exceptionTableSize == 0 &&
            !definingMethod.isInterpreterInvoked()
           ) {
            /*
             * Small Minfo
             */
            enc.addUnencodedByte((localsCount<<5)     | (maxStack));         // byte 1 - lllsssss
            enc.addUnencodedByte((parametersCount<<2) | (localsCount>>3));   // byte 0 - 0pppppll
        } else {
            /*
             * Large Minfo
             */
            int fmt = MethodHeader.FMT_LARGE;
            if (typeTableSize > 0) {
                writeMinfoSize(enc, typeTableSize);
                fmt |= MethodHeader.FMT_T;
            }
            if (exceptionTableSize > 0) {
                writeMinfoSize(enc, exceptionTableSize);
                fmt |= MethodHeader.FMT_E;
            }
            if (definingMethod.isInterpreterInvoked()) {
                fmt |= MethodHeader.FMT_I;
            }
            writeMinfoSize(enc, parametersCount);
            writeMinfoSize(enc, localsCount);
            writeMinfoSize(enc, maxStack);
            enc.addUnsignedByte(fmt);
        }
    }

    /**
     * Write a length into the minfo
     *
     * @param enc the encoder
     * @param value the value
     */
    private void writeMinfoSize(ByteBufferEncoder enc, int value) {
        if (value < 128) {
            enc.addUnsignedByte(value);
        } else {
            Assert.that(value < 32768);
            enc.addUnsignedByte(value & 0xFF);
            enc.addUnsignedByte(0x80|(value>>8));
        }
    }

    /**
     * Return size of the method byte array.
     *
     * @return the size in bytes
     */
    int getCodeSize() {
        return code.length;
    }

    /**
     * Write the bytecodes to VM memory.
     *
     * @param oop address of the method object
     */
    void writeToVMMemory(Object oop) {
        for (int i = 0 ; i < code.length ; i++) {
            NativeUnsafe.setByte(oop, i, code[i]);
        }
    }

/*if[TYPEMAP]*/
    /**
     * Write the type map for the bytecodes to VM memory.
     *
     * @param oop address of the method object
     */
    void writeTypeMapToVMMemory(Object oop) {
        Assert.always(VM.usingTypeMap());
        Address p = Address.fromObject(oop);
        for (int i = 0 ; i < typeMap.length ; i++) {
            NativeUnsafe.setType(p, typeMap[i], 1);
            p = p.add(1);
        }
    }
/*end[TYPEMAP]*/

  


/*if[DEBUG_CODE_ENABLED]*/
    /*-----------------------------------------------------------------------*\
     *                                Verifing                               *
    \*-----------------------------------------------------------------------*/

    /**
     * Verify a new method.
     *
     * @param oop the pointer to the encoded method
     */
    void verifyMethod(Object oop) {
        /*
         * Check the basic parameters.
         */
        int localCount = localTypes.length - parametersCount;
        Assert.that(MethodHeader.decodeLocalCount(oop)     == localCount);
        Assert.that(MethodHeader.decodeParameterCount(oop) == parametersCount);
        Assert.that(MethodHeader.decodeStackCount(oop)     == maxStack);

        /*
         * Check the oopmap.
         */
        if (localTypes.length > 0) {
            int offset = MethodHeader.decodeOopmapOffset(oop);
            for (int i = 0 ; i < localTypes.length ; i++) {
                Klass k = localTypes[i];
                int pos = i / 8;
                int bit = i % 8;
                int bite = NativeUnsafe.getUByte(oop, offset+pos);
                boolean isOop = ((bite>>bit)&1) != 0;
                if (k.isReferenceType()) {
                    Assert.that(isOop == true);
                } else {
                    Assert.that(isOop == false);
                }
            }
        }

        /*
         * Check the exception table.
         */
        if (MethodHeader.decodeExceptionTableSize(oop) == 0) {
            Assert.that(exceptionTable == null || exceptionTable.length == 0);
        } else {
            Assert.that(exceptionTable != null && exceptionTable.length > 0);
            int size   = MethodHeader.decodeExceptionTableSize(oop);
            int offset = MethodHeader.decodeExceptionTableOffset(oop);
            VMBufferDecoder dec = new VMBufferDecoder(oop, offset);
            for (int i = 0 ; i < exceptionTable.length ; i++) {
                ExceptionHandler handler = exceptionTable[i];
                Assert.that(dec.readUnsignedShort() == handler.getStart());
                Assert.that(dec.readUnsignedShort() == handler.getEnd());
                Assert.that(dec.readUnsignedShort() == handler.getHandler());
                Assert.that(getDefiningClass().getObject(dec.readUnsignedShort()) == handler.getKlass());
            }
            dec.checkOffset(offset + size);
        }

        /*
         * Check the type table.
         */
        if (MethodHeader.decodeTypeTableSize(oop) == 0) {
            for (int i = 0 ; i < localTypes.length ; i++) {
                Klass k = localTypes[i];
/*if[FLOATS]*/
                Assert.that(k != Klass.FLOAT && k != Klass.DOUBLE);
/*end[FLOATS]*/
                Assert.that(k != Klass.LONG && !k.isSquawkPrimitive());
            }
        } else {
            int size   = MethodHeader.decodeTypeTableSize(oop);
            int offset = MethodHeader.decodeTypeTableOffset(oop);
            VMBufferDecoder dec = new VMBufferDecoder(oop, offset);
            for (int i = 0 ; i < localTypes.length ; i++) {
                Klass k = localTypes[i];
                if (k == Klass.LONG || k.isSquawkPrimitive()
/*if[FLOATS]*/
                    || k == Klass.FLOAT || k == Klass.DOUBLE
/*end[FLOATS]*/
                    ) {
                    Assert.that(dec.readUnsignedShort() == k.getSystemID());
                    Assert.that(dec.readUnsignedInt() == i);
                }
            }
            dec.checkOffset(offset + size);
        }

        /*
         * Check the bytecodes.
         */
        Assert.that(GC.getArrayLengthNoCheck(oop) == code.length);
        if (!VM.usingTypeMap()) {
            for (int i = 0; i < code.length; i++) {
                Assert.that(NativeUnsafe.getByte(oop, i) == code[i]);
            }
        }
    }
/*end[DEBUG_CODE_ENABLED]*/

}
