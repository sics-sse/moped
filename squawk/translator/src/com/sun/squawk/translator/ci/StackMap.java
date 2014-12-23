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

import com.sun.squawk.util.Assert;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.util.IntHashtable;
import com.sun.squawk.translator.Translator;
import com.sun.squawk.translator.ir.Target;
import com.sun.squawk.*;

/**
 * The StackMap class provides a single static method for loading a
 * verification stack map from a "StackMap" attribute in class file.
 * A "StackMap" attribute has the following format:
 * <p><hr><blockquote><pre>
 *    StackMap_attribute {
 *        u2 attribute_name_index;
 *        u4 attribute_length;
 *        u2 number_of_entries;
 *        stack_map_frame entries[number_of_entries];
 *    }
 * </pre></blockquote><hr><p>
 * Each stack map frame has the following format:
 * <p><hr><blockquote><pre>
 *    stack_map_frame {
 *        u2 offset;
 *        u2 number_of_locals;
 *        verification_type_info locals[number_of_locals];
 *        u2 number_of_stack_items;
 *        verification_type_info stack[number_of_stack_items];
 *    }
 * </pre></blockquote><hr><p>
 * The <code>verification_type_info</code> item is a 1-byte or 3-byte entity:
 * <p><blockquote><pre>
 *   Name                    Code    Explanation
 *   ITEM_Top                0       an unknown or uninitialized value
 *   ITEM_Integer            1       a 32-bit integer
 *   ITEM_Float              2       a 32-bit floating point number
 *   ITEM_Double             3       a 64-bit floating point number
 *   ITEM_Long               4       a 64-bit integer
 *   ITEM_Null               5       the type of null
 *   ITEM_UninitializedThis  6       explained in more detail below
 *   ITEM_Object             7       explained in more detail below
 *   ITEM_Uninitialized      8       explained in more detail below
 * </pre></blockquote><p>
 * Note that the first seven types are encoded in 1 byte, and last two types are
 * encoded in 3 bytes.
 *
 * The meanings of the above types <code>ITEM_UninitializedThis</code>,
 * <code>ITEM_Object</code>, and <code>ITEM_Uninitialized</code> are as follows:
 * <p><blockquote><pre>
 *   ITEM_UninitializedThis
 *   Before a constructor (the <init> method) for a class other than
 *   java.lang.Object calls a constructor (the <init> method) of one of its
 *   superclasses, the 'this' pointer has type ITEM_UninitializedThis. (Comment:
 *   The verifier uses this type to enforce that a constructor must first invoke
 *   a superclass constructor before performing other operations on the 'this'
 *   pointer.)
 *
 *   ITEM_Object
 *   A class instance. The 1-byte type code (7) is followed by a 2-byte
 *   type_name_index (a u2). The type_name_index value must be a valid entry
 *   to the constant_pool table. The constant_pool entry at that index must be a
 *   CONSTANT_Class_info structure.
 *
 *   ITEM_Uninitialized
 *   An uninitialized class instance. The class instance has just been created by the
 *   new instruction, but a constructor (the <init> method) has not yet been
 *   invoked on it. The type code 8 is followed by a 2-byte
 *   new_instruction_index (a u2). The new_instruction_index must be a
 *   valid offset of a byte code instruction. The opcode of the byte code instruction
 *   must be new. (Comment: The uninitialized object is created by this new
 *   instruction. The verifier uses this type to enforce that an instance cannot be
 *   used until it is fully constructed.)
 * </pre></blockquote><p>
 *
 */
public class StackMap {


    /*---------------------------------------------------------------------------*\
     *                          Stack map tag constants                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Tag constant denoting type at top of verification hierarchy.
     */
    public final static int ITEM_Top = 0;

    /**
     * Tag constant denoting integer type.
     */
    public final static int ITEM_Integer = 1;

    /**
     * Tag constant denoting float type.
     */
    public final static int ITEM_Float = 2;

    /**
     * Tag constant denoting double type.
     */
    public final static int ITEM_Double = 3;

    /**
     * Tag constant denoting long type.
     */
    public final static int ITEM_Long = 4;

    /**
     * Tag constant denoting type of <code>null</code>.
     */
    public final static int ITEM_Null = 5;

    /**
     * Tag constant denoting type of <code>this</code> is in <init> method,
     * before a call to super constructor.
     */
    public final static int ITEM_UninitializedThis = 6;

    /**
     * Tag constant denoting type a class in the hierarchy rooted by
     * <code>java.lang.Object</code>.
     */
    public final static int ITEM_Object = 7;

    /**
     * Tag constant denoting the type of a new instance before a call to the
     * appropriate constructor.
     */
    public final static int ITEM_Uninitialized = 8;


    /*---------------------------------------------------------------------------*\
     *                              Constructor                                  *
    \*---------------------------------------------------------------------------*/

    /**
     * Loads a "StackMap" attribute and builds a table of <code>Target</code>
     * instances representing the entries in the stack map.
     *
     * @param   codeParser    the "Code" attribute parser
     * @param   cfr           the class file reader used to read the attribute
     * @param   constantPool  the constant pool of the enclosing class
     * @param   codeLength    the length of the bytecode array for the enclosing method
     * @return  a table of <code>Target</code> instances indexed by address
     *          representing the entries in the stack map
     */
    public static IntHashtable loadStackMap(CodeParser codeParser, ClassFileReader cfr, ConstantPool constantPool, int codeLength) {
        /*
         * Read number_of_entries
         */
        int nmaps  = cfr.readUnsignedShort("map-nmaps");
        if (nmaps == 0) {
            return null;
        } else {
            IntHashtable table = new IntHashtable(nmaps + (nmaps/4) + 1);
            int lastAddress = -1;
            final boolean trace = Translator.TRACING_ENABLED && Tracer.isTracing("maps", codeParser.getMethod().toString());

            for (int i = 0 ; i < nmaps ; i++) {
                int address = cfr.readUnsignedShort("map-address");
                if (address <= lastAddress) {
                    throw cfr.formatError("stack map ip addresses not in order");
                }
                lastAddress = address;

                /*
                 * Load the list of types in the local variables array
                 */
                Klass[] locals = loadStackMapList(codeParser, cfr, constantPool, codeLength);
                if (locals.length > codeParser.getMaxLocals()) {
                    throw cfr.formatError("stack map locals greater than max_locals");
                }

                /*
                 * Load the list of types on the operand stack
                 */
                Klass[] stack = loadStackMapList(codeParser, cfr, constantPool, codeLength);
                if (stack.length > codeParser.getMaxStack()) {
                    throw cfr.formatError("stack map stack greater than max_stack");
                }

                Target target = new Target(address, stack, locals);
                table.put(address, target);

                /*
                 * Trace.
                 */
                if (trace) {
                    Tracer.traceln("Stackmap @"+ target);
                }

            }
            return table;
        }
    }

    /**
     * Loads in a list of <code>verification_type_info</code> items.
     *
     * @param  codeParser    the "Code" attribute parser
     * @param  cfr           the class file reader used to read the attribute
     * @param  constantPool  the constant pool of the enclosing class
     * @param  codeLength    the length of the bytecode array for the enclosing method
     * @return               the list of types read in
     */
     private static Klass[] loadStackMapList(CodeParser codeParser, ClassFileReader cfr, ConstantPool constantPool, int codeLength) {
         int items = cfr.readUnsignedShort("map-items");
         if (items == 0) {
             return Klass.NO_CLASSES;
         }

         Klass[] list = new Klass[items];
         int item = 0;
         int size = items;

         for (; item < items ; item++) {
             int tag = cfr.readByte("map-tag");
             switch (tag) {
                 case ITEM_Top: {
                     list[item] = Klass.TOP;
                     break;
                 }
                 case ITEM_Integer: {
                     list[item] = Klass.INT;
                     break;
                 }
                 case ITEM_Long: {
                     list[item] = Klass.LONG;
                     ++size;
                     break;
                 }
/*if[FLOATS]*/
                 case ITEM_Float: {
                     list[item] = Klass.FLOAT;
                     break;
                 }
                 case ITEM_Double: {
                     list[item] = Klass.DOUBLE;
                     ++size;
                     break;
                 }
/*end[FLOATS]*/
                 case ITEM_Null: {
                     list[item] = Klass.NULL;
                     break;
                 }
                 case ITEM_UninitializedThis: {
                     list[item] = Klass.UNINITIALIZED_THIS;
                     break;
                 }
                 case ITEM_Object: {
                     int classIndex = cfr.readUnsignedShort("map-ITEM_Object");
                     list[item] = constantPool.getResolvedClass(classIndex, codeParser);
                     break;
                 }
                 case ITEM_Uninitialized: {
                     int address = cfr.readUnsignedShort("map-ITEM_Uninitialized");
                     if (address >= codeLength) {
                         throw cfr.formatError("start_pc of LineNumberTable is out of range");
                     }

                     list[item] = codeParser.getUninitializedObjectClass(address, null);
                     break;
                 }
                 default: {
                     throw cfr.formatError("Bad stack map item tag: "+tag);
                 }
             }
         }

         if (size != items) {
             Klass[] newList = new Klass[size];
             int javacIndex = 0;
             for (int j = 0; j < items; j++) {
                 Klass type = list[j];
                 newList[javacIndex++] = type;
                 if (type.isDoubleWord()) {
                     newList[javacIndex++] = Klass.getSecondWordType(type);
                 }
             }
             Assert.that(javacIndex == size);
             list = newList;
         }

         return list;
     }
}
