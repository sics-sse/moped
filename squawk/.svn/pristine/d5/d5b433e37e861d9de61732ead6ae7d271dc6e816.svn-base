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


import com.sun.squawk.vm.*;

/**
 * A tracer for method bodies.
 *
 */
public abstract class BytecodeTracer {

    /**
     * Constuctor.
     */
    public BytecodeTracer() {
    }

    /**
     * Get the current bytecode offset.
     *
     * @return the value
     */
    protected abstract int getCurrentPosition();

    /**
     * Print a string.
     *
     * @param str the string
     */
    protected abstract void print(String str);

    /**
     * Print an opcode and a string.
     *
     * @param opcode the opcode
     * @param str the string
     */
    private void print(int opcode, String str) {
        print(Mnemonics.getMnemonic(opcode)+" "+str);
    }

    /**
     * Print an opcode.
     *
     * @param opcode the opcode
     */
    private void print(int opcode) {
        print(opcode, "");
    }

    /**
     * Get the next signed byte from the method.
     *
     * @return the value
     */
    protected abstract int getByte();

    /**
     * Optional method to print the object constant
     *
     * @param index the class's object table index
     */
    protected String getObjectDetails(int index) {
        return null;
    }

    /**
     * Optional method to print the name of a local variable
     *
     * @param index the local variable's index
     * @param param true if the index refers to a parameter
     */
    protected String getVarDetails(int index, boolean param) {
        return null;
    }

    /**
     * Get the next unsigned byte from the method.
     *
     * @return the value
     */
    int getUnsignedByte() {
        return getByte() & 0xFF;
    }

    /**
     * Get the next char from the method.
     *
     * @return the value
     */
    int getChar() {
        int ch1 = getUnsignedByte();
        int ch2 = getUnsignedByte();
        if (VM.isBigEndian()) {
            return ((ch1 << 8) + (ch2 << 0));
        } else {
            return ((ch2 << 8) + (ch1 << 0));
        }
    }

    /**
     * Get the next short from the method.
     *
     * @return the value
     */
    int getShort() {
        return (short)getChar();
    }

    /**
     * Get the next int from the method.
     *
     * @return the value
     */
    int getInt() {
        int ch1 = getUnsignedByte();
        int ch2 = getUnsignedByte();
        int ch3 = getUnsignedByte();
        int ch4 = getUnsignedByte();
        if (VM.isBigEndian()) {
            return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
        } else {
            return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
        }
    }

    /**
     * Get the next long from the method.
     *
     * @return the value
     */
    long getLong() {
        if (VM.isBigEndian()) {
            return ((long)(getInt()) << 32) + (getInt() & 0xFFFFFFFFL);
        } else {
            return ((long)(getInt() & 0xFFFFFFFFL) + (getInt()) << 32);
        }
    }

/*if[FLOATS]*/

    /**
     * Get the next float from the method.
     *
     * @return the value
     */
    float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    /**
     * Get the next double from the method.
     *
     * @return the value
     */
    double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

/*end[FLOATS]*/

    /**
     * Trace the next bytecode.
     */
    protected void traceByteCode() {
        int opcode = getUnsignedByte();
        switch(opcode) {
            default:  {
                do_normal(opcode);
                return;
            }
            case OPC.ESCAPE: {
                print(opcode);
                do_normal(getUnsignedByte() + 256);
                return;
            }
            case OPC.WIDE_M1:
            case OPC.WIDE_0:
            case OPC.WIDE_1:
            case OPC.WIDE_SHORT:
            case OPC.WIDE_INT: {
                print(opcode);
                do_wide(opcode, getUnsignedByte());
                return;
            }
            case OPC.ESCAPE_WIDE_M1:
            case OPC.ESCAPE_WIDE_0:
            case OPC.ESCAPE_WIDE_1:
            case OPC.ESCAPE_WIDE_SHORT:
            case OPC.ESCAPE_WIDE_INT: {
                print(opcode);
                do_wide(opcode, getUnsignedByte() + 256);
                return;
            }
            case OPC.TABLESWITCH_I: {
                do_switch(opcode, 4);
                return;
            }
            case OPC.TABLESWITCH_S: {
                do_switch(opcode, 2);
                return;
            }
        }
    }

    /**
     * Process a constant bytecode
     *
     * @param opcode the  regular opcode
     * @return true if the bytecode was for a constant
     */
    private boolean do_const(int opcode) {
        switch(opcode) {
            case OPC.CONST_M1:      print(opcode);                 break;
            case OPC.CONST_BYTE:    print(opcode, ""+getByte());   break;
            case OPC.CONST_SHORT:   print(opcode, ""+getShort());  break;
            case OPC.CONST_CHAR:    print(opcode, ""+getChar());   break;
            case OPC.CONST_INT:     print(opcode, ""+getInt());    break;
            case OPC.CONST_LONG:    print(opcode, ""+getLong());   break;
/*if[FLOATS]*/
            case OPC.CONST_FLOAT:   print(opcode, ""+getFloat());  break;
            case OPC.CONST_DOUBLE:  print(opcode, ""+getDouble()); break;
/*end[FLOATS]*/
            default: return false;
        }
        return true;
    }

    /**
     * Process a load or store to local local variable
     *
     * @param opcode the  regular opcode
     * @return true if the bytecode was for a constant object
     * @todo handle wide versions
     */
    private boolean do_load_store(int opcode) {
        switch(opcode) {
            case OPC.LOAD_0:
            case OPC.LOAD_1:
            case OPC.LOAD_2:
            case OPC.LOAD_3:
            case OPC.LOAD_4:
            case OPC.LOAD_5:
            case OPC.LOAD_6:
            case OPC.LOAD_7:
            case OPC.LOAD_8:
            case OPC.LOAD_9:
            case OPC.LOAD_10:
            case OPC.LOAD_11:
            case OPC.LOAD_12:
            case OPC.LOAD_13:
            case OPC.LOAD_14:
            case OPC.LOAD_15:
            case OPC.STORE_0:
            case OPC.STORE_1:
            case OPC.STORE_2:
            case OPC.STORE_3:
            case OPC.STORE_4:
            case OPC.STORE_5:
            case OPC.STORE_6:
            case OPC.STORE_7:
            case OPC.STORE_8:
            case OPC.STORE_9:
            case OPC.STORE_10:
            case OPC.STORE_11:
            case OPC.STORE_12:
            case OPC.STORE_13:
            case OPC.STORE_14:
            case OPC.STORE_15:{
                int index = opcode & 0xF;
                String var = getVarDetails(index, false);
                if (var == null) {
                    var = "";
                }
                print(opcode, var);
                break;
            }
            case OPC.LOAD:
            case OPC.STORE:
            case OPC.LOAD_I2:
            case OPC.STORE_I2:
            case OPC.INC:
            case OPC.DEC:
            {
                int index = getByte();
                String var = getVarDetails(index, false);
                if (var == null) {
                    var = String.valueOf(index);
                }
                print(opcode, var);
                break;
            }
            default: return false;
        }
        return true;
    }

       /**
     * Process a load or store to local local variable
     *
     * @param opcode the  regular opcode
     * @return true if the bytecode was for a constant object
     * @todo handle wide versions
     */
    private boolean do_load_store_parm(int opcode) {
        switch(opcode) {
            case OPC.LOADPARM_0:
            case OPC.LOADPARM_1:
            case OPC.LOADPARM_2:
            case OPC.LOADPARM_3:
            case OPC.LOADPARM_4:
            case OPC.LOADPARM_5:
            case OPC.LOADPARM_6:
            case OPC.LOADPARM_7: {
                int index = opcode & 0x7;
                String var = getVarDetails(index, true);
                if (var == null) {
                    var = "";
                }
                print(opcode, var);
                break;
            }
            case OPC.LOADPARM:
            case OPC.STOREPARM:
            case OPC.LOADPARM_I2:
            case OPC.STOREPARM_I2:
            case OPC.INCPARM:
            case OPC.DECPARM:
            {
                int index = getByte();
                String var = getVarDetails(index, true);
                if (var == null) {
                    var = String.valueOf(index);
                }
                print(opcode, var);
                break;
            }
            default: return false;
        }
        return true;
    }

        /**
     * Process a constant object bytecode
     *
     * @param opcode the  regular opcode
     * @return true if the bytecode was for a constant object
     * @todo handle wide versions
     */
    private boolean do_object(int opcode) {
        switch(opcode) {
            case OPC.OBJECT_0:
            case OPC.OBJECT_1:
            case OPC.OBJECT_2:
            case OPC.OBJECT_3:
            case OPC.OBJECT_4:
            case OPC.OBJECT_5:
            case OPC.OBJECT_6:
            case OPC.OBJECT_7:
            case OPC.OBJECT_8:
            case OPC.OBJECT_9:
            case OPC.OBJECT_10:
            case OPC.OBJECT_11:
            case OPC.OBJECT_12:
            case OPC.OBJECT_13:
            case OPC.OBJECT_14:
            case OPC.OBJECT_15: {
                int index = opcode & 0xF;
                String obj = getObjectDetails(index);
                if (obj == null) {
                    obj = "";
                }
                print(opcode, obj);
                break;
            }
            case OPC.OBJECT: {
                int index = getByte() & 0xFF;
                String obj = getObjectDetails(index);
                if (obj == null) {
                    obj = "#" + index;
                }
                print(opcode, obj);
                break;
            }
            default: return false;
        }
        return true;
    }

    /**
     * Process a normal bytecode
     *
     * @param opcode the  regular opcode
     */
    private void do_normal(int opcode) {
        if (do_const(opcode) ||
            do_object(opcode) ||
            do_load_store(opcode) ||
            do_load_store_parm(opcode)) {
            return;
        }
        if (isBranch(opcode)) {
            print(opcode, "("+(getByte()+getCurrentPosition())+")");
        } else if (hasWide(opcode)) {
            print(opcode, ""+getUnsignedByte());
        } else {
            print(opcode);
        }
    }

    /**
     * Process a wide bytecode
     *
     * @param widecode the wide opcode
     * @param opcode the  regular opcode
     */
    private void do_wide(int widecode, int opcode) {
        int val = 0;
        switch (widecode) {
            case OPC.WIDE_M1:
            case OPC.ESCAPE_WIDE_M1: {
                val = 0xFFFFFF00 + getUnsignedByte();
                break;
            }
            case OPC.WIDE_0:
            case OPC.ESCAPE_WIDE_0: {
                val = getUnsignedByte();
                break;
            }
            case OPC.WIDE_1:
            case OPC.ESCAPE_WIDE_1: {
                val = 0x00000100 + getUnsignedByte();
                break;
            }
            case OPC.WIDE_SHORT:
            case OPC.ESCAPE_WIDE_SHORT: {
                val = getShort();
                break;
            }
            case OPC.WIDE_INT:
            case OPC.ESCAPE_WIDE_INT: {
                val = getInt();
                break;
            }
        }
        if (isBranch(opcode)) {
            print(opcode, "("+(val+getCurrentPosition())+")");
        } else {
            print(opcode, ""+val);
        }
    }


    /**
     * Get a tableswitch entry
     *
     * @param size the table entry size
     */
    private int getSwitchEntry(int size) {
        if (size == 2) {
            return getShort();
        } else {
            return getInt();
        }
    }

    /**
     * Process a tableswitch
     *
     * @param opcode the  regular opcode
     * @param size the table entry size
     */
    private void do_switch(int opcode, int size) {
        print(opcode);
        while ((getCurrentPosition() % size) != 0) {
            print("    pad  = "+getUnsignedByte());
        }
        int low  = getSwitchEntry(size);
        print("    low  = "+low);
        int high = getSwitchEntry(size);
        print("    high = "+high);
        int loc  = getSwitchEntry(size);
        int pos  = getCurrentPosition();
        print("    def  = ("+(loc+pos)+")");
        for (int i = low ; i <= high ; i++) {
            loc = getSwitchEntry(size);
            print("    ["+i+"] = ("+(loc+pos)+")");
        }
    }

    /**
     * Determines if an instruction has a wide version. These instructions always have a
     * byte immediate parameter.
     *
     * @param opcode the opcode
     * @return true if it has
     */
    boolean hasWide(int opcode) {
        return OPC.hasWide(opcode);
    }

    /**
     * Test to see if an opcode is a branch
     *
     * @param opcode the opcode
     * @return true if it is
     */
    boolean isBranch(int opcode) {
        switch (opcode) {
            case OPC.GOTO:
            case OPC.IF_EQ_O:
            case OPC.IF_NE_O:
            case OPC.IF_CMPEQ_O:
            case OPC.IF_CMPNE_O:
            case OPC.IF_EQ_I:
            case OPC.IF_NE_I:
            case OPC.IF_LT_I:
            case OPC.IF_LE_I:
            case OPC.IF_GT_I:
            case OPC.IF_GE_I:
            case OPC.IF_CMPEQ_I:
            case OPC.IF_CMPNE_I:
            case OPC.IF_CMPLT_I:
            case OPC.IF_CMPLE_I:
            case OPC.IF_CMPGT_I:
            case OPC.IF_CMPGE_I:
            case OPC.IF_EQ_L:
            case OPC.IF_NE_L:
            case OPC.IF_LT_L:
            case OPC.IF_LE_L:
            case OPC.IF_GT_L:
            case OPC.IF_GE_L:
            case OPC.IF_CMPEQ_L:
            case OPC.IF_CMPNE_L:
            case OPC.IF_CMPLT_L:
            case OPC.IF_CMPLE_L:
            case OPC.IF_CMPGT_L:
            case OPC.IF_CMPGE_L:
            {
                return true;
            }
        }
        return false;
    }

}
