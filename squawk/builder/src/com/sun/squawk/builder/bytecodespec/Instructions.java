/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.builder.bytecodespec;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

public class Instructions {
    public static List<Instruction> getInstructions() {
        ArrayList<Instruction> l = new ArrayList<Instruction>();

        l.add(new CONST_I());
        l.add(new OBJECT_I());
        l.add(new LOAD_I());
        l.add(new STORE_I());
        l.add(new LOADPARM_I());
        l.add(new WIDE_I());
        l.add(new WIDE_SHORT());
        l.add(new WIDE_INT());
        l.add(new CATCH());
        l.add(new CONST_NULL());
        l.add(new CONST_BYTE());
        l.add(new CONST_SHORT());
        l.add(new CONST_CHAR());
        l.add(new CONST_INT());
        l.add(new CONST_LONG());
        l.add(new OBJECT());
        l.add(new LOAD());
        l.add(new LOAD_I2());
        l.add(new STORE());
        l.add(new STORE_I2());
        l.add(new LOADPARM());
        l.add(new LOADPARM_I2());
        l.add(new STOREPARM());
        l.add(new STOREPARM_I2());
        l.add(new INC());
        l.add(new DEC());
        l.add(new INCPARM());
        l.add(new DECPARM());
        l.add(new GOTO());
        l.add(new IF_EQ_O());
        l.add(new IF_NE_O());
        l.add(new IF_CMPCOND_O());
        l.add(new IF_COND_I());
        l.add(new IF_CMPCOND_I());
        l.add(new IF_COND_L());
        l.add(new IF_CMPCOND_L());
        l.add(new GETSTATIC_I());
        l.add(new GETSTATIC_O());
        l.add(new GETSTATIC_L());
        l.add(new CLASS_GETSTATIC_I());
        l.add(new CLASS_GETSTATIC_O());
        l.add(new CLASS_GETSTATIC_L());
        l.add(new PUTSTATIC_I());
        l.add(new PUTSTATIC_O());
        l.add(new PUTSTATIC_L());
        l.add(new CLASS_PUTSTATIC_I());
        l.add(new CLASS_PUTSTATIC_O());
        l.add(new CLASS_PUTSTATIC_L());
        l.add(new GETFIELD_I());
        l.add(new GETFIELD_B());
        l.add(new GETFIELD_S());
        l.add(new GETFIELD_C());
        l.add(new GETFIELD_O());
        l.add(new GETFIELD_L());
        l.add(new GETFIELD0_I());
        l.add(new GETFIELD0_B());
        l.add(new GETFIELD0_S());
        l.add(new GETFIELD0_C());
        l.add(new GETFIELD0_O());
        l.add(new GETFIELD0_L());
        l.add(new PUTFIELD_I());
        l.add(new PUTFIELD_B());
        l.add(new PUTFIELD_S());
        l.add(new PUTFIELD_O());
        l.add(new PUTFIELD_L());
        l.add(new PUTFIELD0_I());
        l.add(new PUTFIELD0_B());
        l.add(new PUTFIELD0_S());
        l.add(new PUTFIELD0_O());
        l.add(new PUTFIELD0_L());
        l.add(new INVOKEVIRTUAL_I());
        l.add(new INVOKEVIRTUAL_V());
        l.add(new INVOKEVIRTUAL_L());
        l.add(new INVOKEVIRTUAL_O());
        l.add(new INVOKESTATIC_I());
        l.add(new INVOKESTATIC_V());
        l.add(new INVOKESTATIC_O());
        l.add(new INVOKESTATIC_L());
        l.add(new INVOKESUPER_I());
        l.add(new INVOKESUPER_V());
        l.add(new INVOKESUPER_L());
        l.add(new INVOKESUPER_O());
        l.add(new RETURN_V());
        l.add(new RETURN_I());
        l.add(new RETURN_L());
        l.add(new RETURN_O());
        l.add(new ADD_I());
        l.add(new SUB_I());
        l.add(new AND_I());
        l.add(new OR_I());
        l.add(new XOR_I());
        l.add(new SHL_I());
        l.add(new SHR_I());
        l.add(new USHR_I());
        l.add(new MUL_I());
        l.add(new DIV_I());
        l.add(new REM_I());
        l.add(new NEG_I());
        l.add(new I2B());
        l.add(new I2S());
        l.add(new I2C());
        l.add(new ADD_L());
        l.add(new SUB_L());
        l.add(new MUL_L());
        l.add(new DIV_L());
        l.add(new REM_L());
        l.add(new AND_L());
        l.add(new OR_L());
        l.add(new XOR_L());
        l.add(new NEG_L());
        l.add(new SHL_L());
        l.add(new SHR_L());
        l.add(new USHR_L());
        l.add(new L2I());
        l.add(new I2L());
        l.add(new POP_1());
        l.add(new POP_2());
        l.add(new MONITORENTER());
        l.add(new MONITOREXIT());
        l.add(new CLASS_MONITORENTER());
        l.add(new CLASS_MONITOREXIT());
        l.add(new ARRAYLENGTH());
        l.add(new NEW());
        l.add(new NEWARRAY());
        l.add(new INSTANCEOF());
        l.add(new CHECKCAST());
        l.add(new ALOAD_I());
        l.add(new ALOAD_B());
        l.add(new ALOAD_S());
        l.add(new ALOAD_C());
        l.add(new ALOAD_O());
        l.add(new ALOAD_L());
        l.add(new ASTORE_I());
        l.add(new ASTORE_B());
        l.add(new ASTORE_S());
        l.add(new ASTORE_O());
        l.add(new ASTORE_L());
        l.add(new ESCAPE());
        l.add(new INVOKENATIVE_I());
        l.add(new INVOKENATIVE_V());
        l.add(new INVOKENATIVE_L());
        l.add(new INVOKENATIVE_O());
        l.add(new FINDSLOT());
        l.add(new EXTEND());
        l.add(new INVOKESLOT_I());
        l.add(new INVOKESLOT_V());
        l.add(new INVOKESLOT_L());
        l.add(new INVOKESLOT_O());
        l.add(new LOOKUP_I());
        l.add(new LOOKUP_B());
        l.add(new LOOKUP_S());
        l.add(new EXTEND0());
        l.add(new TABLESWITCH_I());
        l.add(new TABLESWITCH_S());
        l.add(new NEWDIMENSION());
        l.add(new CLASS_CLINIT());
        l.add(new THROW());
        l.add(new BBTARGET_SYS());
        l.add(new BBTARGET_APP());

        return l;
    }


    static class ADD_I extends TwoOperandArithmetic {
        public String getName() {
            return "add_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Add <code>int</code>");
        }
    }

    static class ADD_L extends TwoOperandArithmetic {
        public String getName() {
            return "add_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Add <code>long</code>");
        }
    }

    abstract static class ALOAD extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "aload_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Load element of type <code>" + getType() + "</code> from array");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>arrayref</i>, <i>index</i> =&gt; ..., <i>value</i>");
        }

        public void printDescription(PrintWriter out) {
            out.println("Just like the corresponding Java bytecode.");
        }
    }


    static class ALOAD_B extends ALOAD {

        protected char getTypeLetter() {
            return 'b';
        }

        protected String getType() {
            return "byte";
        }
    }


    static class ALOAD_C extends ALOAD {
        protected char getTypeLetter() {
            return 'c';
        }

        protected String getType() {
            return "char";
        }
    }


    static class ALOAD_I extends ALOAD {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }


    static class ALOAD_L extends ALOAD {

        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }
    }


    static class ALOAD_O extends ALOAD {

        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }
    }


    static class ALOAD_S extends ALOAD {

        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }

    static class AND_I extends TwoOperandArithmetic {
        public String getName() {
            return "and_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Boolean AND <code>int</code>");
        }
    }

    static class AND_L extends TwoOperandArithmetic {
        public String getName() {
            return "and_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Boolean AND <code>long</code>");
        }
    }

    static class ARRAYLENGTH extends AbstractInstruction {
        public String getName() {
            return "arraylength";
        }


        public void printOperation(PrintWriter out) {
            out.println("Get the length of an array");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>arrayref</i> =&gt; ..., <i>length</i>");
        }
    }

    public abstract static class ASTORE extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "astore_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Store element of type <code>" + getType() + "</code> to array");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>arrayref</i>, <i>index</i>, <i>value</i> =&gt; ...");
        }

        public void printDescription(PrintWriter out) {
            out.println("Just like the corresponding Java bytecode.");
        }
    }


    static class ASTORE_B extends ASTORE {

        protected char getTypeLetter() {
            return 'b';
        }

        protected String getType() {
            return "byte";
        }
    }


    static class ASTORE_I extends ASTORE {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }

    static class ASTORE_L extends ASTORE {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }
    }

    static class ASTORE_O extends ASTORE {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }

        public void printDescription(PrintWriter out) {
            out.println("Only the inputs to this instruction may be on the stack prior to its execution.");
        }
    }


    static class ASTORE_S extends ASTORE {
        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }

    public abstract static class BBTARGET extends AbstractInstruction {
        public void printOperation(PrintWriter out) {
            out.println("Placeholder for the target of a backward branch");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("_ =&gt; _");
        }
    }


    static class BBTARGET_APP extends BBTARGET {
        public String getName() {
            return "bbtarget_app";
        }
    }

    static class BBTARGET_SYS extends BBTARGET {
        public String getName() {
            return "bbtarget_sys";
        }
    }

    static class CATCH extends AbstractInstruction {
        public String getName() {
            return "catch";
        }

        public void printOperation(PrintWriter out) {
            out.println("Start an exception handler");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("_ =&gt; <i>exception</i>");
        }
    }

    static class CHECKCAST extends AbstractInstruction {
        public String getName() {
            return "checkcast";
        }

        public void printOperation(PrintWriter out) {
            out.println("Check that object is of given type");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>reference</i>, <i>class</i> =&gt; ..., <i>reference</i>");
        }
    }

    static class CLASS_CLINIT extends AbstractInstruction {
        public String getName() {
            return "class_clinit";
        }

        public void printOperation(PrintWriter out) {
            out.println("Initialize the current class, if it has not been initialized already");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("_ =&gt; _");
        }
    }

    static class CLASS_GETSTATIC_I extends AbstractInstruction {
        public String getName() {
            return "class_getstatic_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>int</code> or <code>float</code> static field from the current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class CLASS_GETSTATIC_L extends AbstractInstruction {
        public String getName() {
            return "class_getstatic_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>long</code> or <code>double</code> static field from current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class CLASS_GETSTATIC_O extends AbstractInstruction {
        public String getName() {
            return "class_getstatic_o";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>reference</code> static field from the current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class CLASS_MONITORENTER extends AbstractInstruction {
        public String getName() {
            return "class_monitorenter";
        }

        public void printOperation(PrintWriter out) {
            out.println("Enter monitor for current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("_ =&gt; _");
        }
    }

    static class CLASS_MONITOREXIT extends AbstractInstruction {
        public String getName() {
            return "class_monitorexit";
        }

        public void printOperation(PrintWriter out) {
            out.println("Exit monitor for current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("_ =&gt; _");
        }
    }

    static class CLASS_PUTSTATIC_I extends AbstractInstruction {
        public String getName() {
            return "class_putstatic_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Assign <code>int</code> or <code>float</code> static field from the current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class CLASS_PUTSTATIC_L extends AbstractInstruction {
        public String getName() {
            return "class_putstatic_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Assign <code>long</code> or <code>double</code> static field from current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class CLASS_PUTSTATIC_O extends AbstractInstruction {
        public String getName() {
            return "class_putstatic_o";
        }

        public void printOperation(PrintWriter out) {
            out.println("Assign <code>reference</code> static field from the current class");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class CONST_BYTE extends AbstractInstruction {
        public String getName() {
            return "const_byte";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push 8 bit constant onto the stack");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>value</i></td></tr></Table>");
        }
    }

    static class CONST_CHAR extends AbstractInstruction {
        public String getName() {
            return "const_char";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push an unsigned 16 bit constant (or <code>char</code>) onto the stack");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>valuebyte1</i></td></tr>");
            out.println("<tr><td><i>valuebyte2</i></td></tr></Table>");
        }
    }

    static class CONST_I extends AbstractInstruction {
        public String getName() {
            return "const_&lt;i&gt;";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>int</code> constant");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printForms(PrintWriter out) {
            printForm(out, "const_m1", 85);
            for (int i = 0; i <= 15; i++) {
                out.println("<br>");
                printForm(out, "const_" + i, i);
            }
        }
    }

    static class CONST_INT extends AbstractInstruction {
        public String getName() {
            return "const_int";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push 32 bit constant onto the stack");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>valuebyte1</i></td></tr>");
            out.println("<tr><td><i>valuebyte2</i></td></tr>");
            out.println("<tr><td><i>valuebyte3</i></td></tr>");
            out.println("<tr><td><i>valuebyte4</i></td></tr></Table>");
        }
    }

    static class CONST_LONG extends AbstractInstruction {
        public String getName() {
            return "const_long";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push 64 bit constant onto the stack");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>valuebyte1</i></td></tr>");
            out.println("<tr><td><i>valuebyte2</i></td></tr>");
            out.println("<tr><td><i>valuebyte3</i></td></tr>");
            out.println("<tr><td><i>valuebyte4</i></td></tr>");
            out.println("<tr><td><i>valuebyte5</i></td></tr>");
            out.println("<tr><td><i>valuebyte6</i></td></tr>");
            out.println("<tr><td><i>valuebyte7</i></td></tr>");
            out.println("<tr><td><i>valuebyte8</i></td></tr></Table>");
        }
    }

    static class CONST_NULL extends AbstractInstruction {
        public String getName() {
            return "const_null";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>null</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }
    }

    static class CONST_SHORT extends AbstractInstruction {
        public String getName() {
            return "const_short";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push 16 bit constant onto the stack");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>valuebyte1</i></td></tr>");
            out.println("<tr><td><i>valuebyte2</i></td></tr></Table>");
        }
    }

    static class DEC extends AbstractInstruction {
        public String getName() {
            return "dec";
        }

        public void printOperation(PrintWriter out) {
            out.println("Decrement a single word local variable by 1");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class DECPARM extends AbstractInstruction {
        public String getName() {
            return "decparm";
        }

        public void printOperation(PrintWriter out) {
            out.println("Decrement a single word parameter by 1");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class DIV_I extends TwoOperandArithmetic {
        public String getName() {
            return "div_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Divide <code>int</code>");
        }
    }

    static class DIV_L extends TwoOperandArithmetic {
        public String getName() {
            return "div_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Divide <code>long</code>");
        }
    }

    static class ESCAPE extends AbstractInstruction {
        public String getName() {
            return "escape";
        }

        public void printOperation(PrintWriter out) {
            out.println("Add 256 to following bytecode");
        }

        public void printFormat(PrintWriter out) {
            printForm(out, "escape", 77);
            printForm(out, "escape_wide_m1", 78);
            printForm(out, "escape_wide_0", 79);
            printForm(out, "escape_wide_1", 80);
            printForm(out, "escape_wide_short", 81);
            printForm(out, "escape_wide_int", 82);
        }

        public void printOperandStack(PrintWriter out) {
            out.println("See resulting bytecode description");
        }

        public void printDescription(PrintWriter out) {
            out.println("There are more than 256 Squawk bytecodes.  The way bytecode");
            out.println("instructions above 255 are represented is with the escape bytecode");
            out.println("followed by something else.  The various wide forms of the escape");
            out.println("bytecode modify the resulting instruction the same way as the wide");
            out.println("bytecode.");
        }
    }

    static class EXTEND extends AbstractInstruction {
        public String getName() {
            return "extend";
        }

        public void printOperation(PrintWriter out) {
            out.println("Prepare method for execution");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("_ =&gt; _");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>words</i></td></tr></Table>");
        }

        public void printDescription(PrintWriter out) {
            out.println("This instruction goes at the beginning of a method.");
            out.println("The inline parameter denotes how many words should be zeroed out.");
        }
    }

    static class EXTEND0 extends AbstractInstruction {
        public String getName() {
            return "extend0";
        }

        public void printOperation(PrintWriter out) {
            out.println("Prepare method for execution");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("_ =&gt; _");
        }

        public void printDescription(PrintWriter out) {
            out.println("This instruction goes at the beginning of a method.");
            out.println("It serves as shorthand for the <code>extend</code> bytecode");
            out.println("with an inline parameter of zero.");
        }
    }

    static class FINDSLOT extends AbstractInstruction {
        public String getName() {
            return "findslot";
        }

        public void printOperation(PrintWriter out) {
            out.println("Look up an interface method in an object's virtual method table");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>reference</i>, <i>class</i> =&gt; ..., <i>result</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }

        public void printDescription(PrintWriter out) {
            out.println("This bytecode takes as input from the stack an object and an interface");
            out.println("that object implements.  The inline input is the index number corresponding");
            out.println("to some interface method to be invoked.  The instruction then looks up that");
            out.println("method in the object's virtual method table and pushes the method's index number");
            out.println("in the table onto the stack.  This instruction and the");
            out.println("invokeslot bytecodes serve as a replacement for the invokeinterface Java bytecode.");
        }
    }

    public abstract static class GETFIELD extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "getfield_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>" + getType() + "</code> instance field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>reference</i> =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    public abstract static class GETFIELD0 extends AbstractInstruction {

        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "getfield0_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Loads a <code>" + getType() + "</code> value from an instance field of the object in parameter 0.");
            out.println("That is, the object accessed via <code>loadparm_0</code>.");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }


    static class GETFIELD0_B extends GETFIELD0 {
        protected char getTypeLetter() {
            return 'b';
        }

        protected String getType() {
            return "byte";
        }
    }


    static class GETFIELD0_C extends GETFIELD0 {
        protected char getTypeLetter() {
            return 'c';
        }

        protected String getType() {
            return "char";
        }
    }


    static class GETFIELD0_I extends GETFIELD0 {

        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }


    static class GETFIELD0_L extends GETFIELD0 {

        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }
    }


    static class GETFIELD0_O extends GETFIELD0 {

        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }
    }


    static class GETFIELD0_S extends GETFIELD0 {
        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }


    static class GETFIELD_B extends GETFIELD {
        protected char getTypeLetter() {
            return 'b';
        }

        protected String getType() {
            return "byte";
        }
    }


    static class GETFIELD_C extends GETFIELD {
        protected char getTypeLetter() {
            return 'c';
        }

        protected String getType() {
            return "char";
        }
    }
    static class GETFIELD_I extends GETFIELD {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }


    static class GETFIELD_L extends GETFIELD {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }
    }


    static class GETFIELD_O extends GETFIELD {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }
    }


    static class GETFIELD_S extends GETFIELD {
        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }

    static class GETSTATIC_I extends AbstractInstruction {
        public String getName() {
            return "getstatic_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>int</code> or <code>float</code> static field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>class</i> =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class GETSTATIC_L extends AbstractInstruction {
        public String getName() {
            return "getstatic_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>long</code> or <code>double</code> static field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>class</i> =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class GETSTATIC_O extends AbstractInstruction {
        public String getName() {
            return "getstatic_o";
        }

        public void printOperation(PrintWriter out) {
            out.println("Push <code>reference</code> static field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>class</i> =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class GOTO extends AbstractInstruction {
        public String getName() {
            return "goto";
        }

        public void printOperation(PrintWriter out) {
            out.println("Branch always");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }

        public void printDescription(PrintWriter out) {
            out.println("The stack must be empty for any backward branch.");
        }
    }

    static class I2B extends OneOperandArithmetic {
        public String getName() {
            return "i2b";
        }

        public void printOperation(PrintWriter out) {
            out.println("Convert <code>int</code> to <code>byte</code>");
        }
    }

    static class I2C extends OneOperandArithmetic {
        public String getName() {
            return "i2c";
        }

        public void printOperation(PrintWriter out) {
            out.println("Convert <code>int</code> to <code>char</code>");
        }
    }

    static class I2L extends OneOperandArithmetic {
        public String getName() {
            return "i2l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Convert <code>int</code> to <code>long</code>");
        }
    }

    static class I2S extends OneOperandArithmetic {
        public String getName() {
            return "i2s";
        }

        public void printOperation(PrintWriter out) {
            out.println("Convert <code>int</code> to <code>short</code>");
        }
    }

    public abstract static class IF_CMPCOND extends AbstractInstruction {


        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value1</i>, <i>value2</i> =&gt; ...");
        }

        public void printDescription(PrintWriter out) {
            out.println("Only the input values for this instruction can be on the stack for any backward branch.");
        }
    }

    static class IF_CMPCOND_I extends IF_CMPCOND {
        public String getName() {
            return "if_cmp&lt;cond&gt;_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Branch if <code>int</code> comparison succeeds");
        }

        public void printForms(PrintWriter out) {
            printForm(out, "if_cmpeq_i", 115);
            out.println("<br>");
            printForm(out, "if_cmpne_i", 116);
            out.println("<br>");
            printForm(out, "if_cmplt_i", 117);
            out.println("<br>");
            printForm(out, "if_cmple_i", 118);
            out.println("<br>");
            printForm(out, "if_cmpgt_i", 119);
            out.println("<br>");
            printForm(out, "if_cmpge_i", 120);
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class IF_CMPCOND_L extends IF_CMPCOND {
        public String getName() {
            return "if_cmp&lt;cond&gt;_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Branch if <code>long</code> comparison succeeds");
        }

        public void printForms(PrintWriter out) {
            printForm(out, "if_cmpeq_l", 127);
            out.println("<br>");
            printForm(out, "if_cmpne_l", 128);
            out.println("<br>");
            printForm(out, "if_cmplt_l", 129);
            out.println("<br>");
            printForm(out, "if_cmple_l", 130);
            out.println("<br>");
            printForm(out, "if_cmpgt_l", 131);
            out.println("<br>");
            printForm(out, "if_cmpge_l", 132);
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class IF_CMPCOND_O extends IF_CMPCOND {
        public String getName() {
            return "if_cmp&lt;cond&gt;_o";
        }

        public void printOperation(PrintWriter out) {
            out.println("Branch if <code>reference</code> comparison succeeds");
        }

        public void printForms(PrintWriter out) {
            printForm(out, "if_cmpeq_o", 107);
            out.println("<br>");
            printForm(out, "if_cmpne_o", 108);
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    public abstract static class IF_COND extends AbstractInstruction {
        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printDescription(PrintWriter out) {
            out.println("Only the input value for this instruction can be on the stack for any backward branch.");
        }
    }

    static class IF_COND_I extends IF_COND {
        public String getName() {
            return "if_&lt;cond&gt;_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Branch if <code>int</code> comparison with zero succeeds");
        }

        public void printForms(PrintWriter out) {
            printForm(out, "if_eq_i", 109);
            out.println("<br>");
            printForm(out, "if_ne_i", 110);
            out.println("<br>");
            printForm(out, "if_lt_i", 111);
            out.println("<br>");
            printForm(out, "if_le_i", 112);
            out.println("<br>");
            printForm(out, "if_gt_i", 113);
            out.println("<br>");
            printForm(out, "if_ge_i", 114);
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class IF_COND_L extends IF_COND {
        public String getName() {
            return "if_&lt;cond&gt;_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Branch if <code>long</code> comparison with zero succeeds");
        }

        public void printForms(PrintWriter out) {
            printForm(out, "if_eq_l", 121);
            out.println("<br>");
            printForm(out, "if_ne_l", 122);
            out.println("<br>");
            printForm(out, "if_lt_l", 123);
            out.println("<br>");
            printForm(out, "if_le_l", 124);
            out.println("<br>");
            printForm(out, "if_gt_l", 125);
            out.println("<br>");
            printForm(out, "if_ge_l", 126);
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class IF_EQ_O extends IF_COND {
        public String getName() {
            return "if_eq_o";
        }


        public void printOperation(PrintWriter out) {
            out.println("Branch if <code>reference</code> is null");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class IF_NE_O extends IF_COND {
        public String getName() {
            return "if_ne_o";
        }


        public void printOperation(PrintWriter out) {
            out.println("Branch if <code>reference</code> is not null");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class INC extends AbstractInstruction {
        public String getName() {
            return "inc";
        }

        public void printOperation(PrintWriter out) {
            out.println("Increment a single word local variable by 1");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class INCPARM extends AbstractInstruction {
        public String getName() {
            return "incparm";
        }

        public void printOperation(PrintWriter out) {
            out.println("Increment a single word parameter by 1");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class INSTANCEOF extends AbstractInstruction {
        public String getName() {
            return "instanceof";
        }

        public void printOperation(PrintWriter out) {
            out.println("Determine if object is of given type");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>reference</i>, <i>class</i> =&gt; ..., <i>result</i>");
        }
    }

    public abstract static class INVOKENATIVE extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();
        abstract protected String getResultingStack();

        public String getName() {
            return "invokenative_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Invoke native method with return type <code>" + getType() + "</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("[[... <i>arg2</i>], <i>arg1</i>] =&gt; " + getResultingStack());
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }


    static class INVOKENATIVE_I extends INVOKENATIVE {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }

    static class INVOKENATIVE_L extends INVOKENATIVE {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKENATIVE_O extends INVOKENATIVE {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }

    static class INVOKENATIVE_V extends INVOKENATIVE {
        protected char getTypeLetter() {
            return 'v';
        }

        protected String getType() {
            return "void";
        }

        protected String getResultingStack() {
            return "_";
        }
    }

    public abstract static class INVOKESLOT extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();
        abstract protected String getResultingStack();

        public String getName() {
            return "invokeslot_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Invoke method from vtable with return type <code>" + getType() + "</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("[[... <i>arg2</i>], <i>arg1</i>,] <i>reference</i>, <i>index</i> =&gt; " + getResultingStack());
        }

        public void printDescription(PrintWriter out) {
            out.println("This instruction invokes the method out of the vtable for the given object");
            out.println("at the given index.  Used with <code>findslot</code> to replace Java's");
            out.println("<code>invokeinterface</code>.");
        }
    }


    static class INVOKESLOT_I extends INVOKESLOT {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKESLOT_L extends INVOKESLOT {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }

    static class INVOKESLOT_O extends INVOKESLOT {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }

    static class INVOKESLOT_V extends INVOKESLOT {
        protected char getTypeLetter() {
            return 'v';
        }

        protected String getType() {
            return "void";
        }

        protected String getResultingStack() {
            return "_";
        }
    }

    public abstract static class INVOKESTATIC extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();
        abstract protected String getResultingStack();

        public String getName() {
            return "invokestatic_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Invoke static method with return type <code>" + getType() + "</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("[[... <i>arg2</i>], <i>arg1</i>], <i>class</i> =&gt; " + getResultingStack());
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }


    static class INVOKESTATIC_I extends INVOKESTATIC {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKESTATIC_L extends INVOKESTATIC {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKESTATIC_O extends INVOKESTATIC {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKESTATIC_V extends INVOKESTATIC {
        protected char getTypeLetter() {
            return 'v';
        }

        protected String getType() {
            return "void";
        }

        protected String getResultingStack() {
            return "_";
        }
    }

    public abstract static class INVOKESUPER extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();
        abstract protected String getResultingStack();

        public String getName() {
            return "invokesuper_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Invoke superclass method with return type <code>" + getType() + "</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("[[... <i>arg2</i>], <i>arg1</i>], <i>reference</i>, <i>class</i> =&gt; " + getResultingStack());
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }


    static class INVOKESUPER_I extends INVOKESUPER {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKESUPER_L extends INVOKESUPER {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKESUPER_O extends INVOKESUPER {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKESUPER_V extends INVOKESUPER {
        protected char getTypeLetter() {
            return 'v';
        }

        protected String getType() {
            return "void";
        }

        protected String getResultingStack() {
            return "_";
        }
    }

    public abstract static class INVOKEVIRTUAL extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();
        abstract protected String getResultingStack();

        public String getName() {
            return "invokevirtual_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Invoke virtual method with return type <code>" + getType() + "</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("[[... <i>arg2</i>], <i>arg1</i>], <i>reference</i> =&gt; " + getResultingStack());
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }


    static class INVOKEVIRTUAL_I extends INVOKEVIRTUAL {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKEVIRTUAL_L extends INVOKEVIRTUAL {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKEVIRTUAL_O extends INVOKEVIRTUAL {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }

        protected String getResultingStack() {
            return "<i>value</i>";
        }
    }


    static class INVOKEVIRTUAL_V extends INVOKEVIRTUAL {
        protected char getTypeLetter() {
            return 'v';
        }

        protected String getType() {
            return "void";
        }

        protected String getResultingStack() {
            return "_";
        }
    }

    static class L2I extends OneOperandArithmetic {
        public String getName() {
            return "l2i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Convert <code>long</code> to <code>int</code>");
        }
    }

    static class LOAD extends AbstractInstruction {
        public String getName() {
            return "load";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load single word value from local variable");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class LOADPARM extends AbstractInstruction {
        public String getName() {
            return "loadparm";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load single word value from parameter");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class LOADPARM_I extends AbstractInstruction {
        public String getName() {
            return "loadparm_&lt;i&gt;";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load single word value from parameter");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printForms(PrintWriter out) {
            for (int i = 0; i <= 7; i++) {
                printForm(out, "loadparm_" + i, i+64);
                out.println("<br>");
            }
        }
    }

    static class LOADPARM_I2 extends AbstractInstruction {
        public String getName() {
            return "loadparm_i2";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load double word value from parameter");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class LOAD_I extends AbstractInstruction {
        public String getName() {
            return "load_&lt;i&gt;";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load single word value from local variable");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printForms(PrintWriter out) {
            for (int i = 0; i <= 15; i++) {
                printForm(out, "load_" + i, i+32);
                out.println("<br>");
            }
        }
    }

    static class LOAD_I2 extends AbstractInstruction {
        public String getName() {
            return "load_i2";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load double word value from local variable");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    public abstract static class LOOKUP extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "lookup_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Look up value in sorted array");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>key</i>, <i>arrayref</i> =&gt; ..., <i>index</i>");
        }

        public void printDescription(PrintWriter out) {
            out.println("This instruction is used to replace Java's <code>lookupswitch</code>.");
            out.println("The array referenced by <i>arrayref</i> must be of type <code>" + getType() + "[]</code>");
            out.println("and in ascending order.  If the key was not found in the array, the instruction");
            out.println("returns -1.");
        }
    }

    static class LOOKUP_B extends LOOKUP {
        protected char getTypeLetter() {
            return 'b';
        }

        protected String getType() {
            return "byte";
        }
    }

    static class LOOKUP_I extends LOOKUP {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }


    static class LOOKUP_S extends LOOKUP {
        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }

    static class MONITORENTER extends AbstractInstruction {
        public String getName() {
            return "monitorenter";
        }

        public void printOperation(PrintWriter out) {
            out.println("Enter monitor for object");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("<i>reference</i> =&gt; _");
        }
    }

    static class MONITOREXIT extends AbstractInstruction {
        public String getName() {
            return "monitorexit";
        }

        public void printOperation(PrintWriter out) {
            out.println("Exit monitor for object");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("<i>reference</i> =&gt; _");
        }
    }

    static class MUL_I extends TwoOperandArithmetic {
        public String getName() {
            return "mul_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Multiply <code>int</code>");
        }
    }

    static class MUL_L extends TwoOperandArithmetic {
        public String getName() {
            return "mul_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Multiply <code>long</code>");
        }
    }

    static class NEG_I extends OneOperandArithmetic {
        public String getName() {
            return "neg_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Negate <code>int</code>");
        }
    }

    static class NEG_L extends OneOperandArithmetic {
        public String getName() {
            return "neg_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Negate <code>long</code>");
        }
    }

    static class NEW extends AbstractInstruction {
        public String getName() {
            return "new";
        }

        public void printOperation(PrintWriter out) {
            out.println("Allocate an object");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>class</i> =&gt; <i>object</i>");
        }
    }

    static class NEWARRAY extends AbstractInstruction {
        public String getName() {
            return "newarray";
        }

        public void printOperation(PrintWriter out) {
            out.println("Allocate an array");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>length</i>, <i>class</i> =&gt; ..., <i>arrayref</i>");
        }
    }

    static class NEWDIMENSION extends AbstractInstruction {
        public String getName() {
            return "newdimension";
        }

        public void printOperation(PrintWriter out) {
            out.println("Add a new dimension to an array");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>arrayref</i>, <i>length</i> =&gt; ..., <i>arrayref</i>");
        }
    }

    static class OBJECT extends AbstractInstruction {
        public String getName() {
            return "object";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load a constant object from the static class object table");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class OBJECT_I extends AbstractInstruction {
        public String getName() {
            return "object_&lt;i&gt;";
        }

        public void printOperation(PrintWriter out) {
            out.println("Load a constant object from the static class object table");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("... =&gt; ..., <i>value</i>");
        }

        public void printForms(PrintWriter out) {
            for (int i = 0; i <= 15; i++) {
                printForm(out, "object_" + i, i+16);
                out.println("<br>");
            }
        }
    }

    static class OR_I extends TwoOperandArithmetic {
        public String getName() {
            return "or_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Boolean OR <code>int</code>");
        }
    }

    static class OR_L extends TwoOperandArithmetic {
        public String getName() {
            return "or_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Boolean OR <code>long</code>");
        }
    }

    public abstract static class OneOperandArithmetic extends AbstractInstruction {
        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ..., <i>result</i>");
        }
    }

    static class POP_1 extends AbstractInstruction {
        public String getName() {
            return "pop_1";
        }

        public void printOperation(PrintWriter out) {
            out.println("Pop one word from the stack");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }
    }

    static class POP_2 extends AbstractInstruction {
        public String getName() {
            return "pop_2";
        }

        public void printOperation(PrintWriter out) {
            out.println("Pop two words from the stack");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }
    }

    public abstract static class PUTFIELD extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "putfield_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Assign <code>" + getType() + "</code> instance field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>reference</i>, <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    public abstract static class PUTFIELD0 extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "putfield0_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Assigns a <code>" + getType() + "</code> value to an instance field of the object in parameter 0.");
            out.println("That is, the object accessed via <code>loadparm_0</code>.");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }


    static class PUTFIELD0_B extends PUTFIELD0 {

        protected char getTypeLetter() {
            return 'b';
        }

        protected String getType() {
            return "byte";
        }
    }


    static class PUTFIELD0_I extends PUTFIELD0 {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }


    static class PUTFIELD0_L extends PUTFIELD0 {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }
    }


    static class PUTFIELD0_O extends PUTFIELD0 {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }
    }


    static class PUTFIELD0_S extends PUTFIELD0 {
        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }


    static class PUTFIELD_B extends PUTFIELD {
        protected char getTypeLetter() {
            return 'b';
        }

        protected String getType() {
            return "byte";
        }
    }


    static class PUTFIELD_I extends PUTFIELD {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }


    static class PUTFIELD_L extends PUTFIELD {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }
    }


    static class PUTFIELD_O extends PUTFIELD {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }
    }


    static class PUTFIELD_S extends PUTFIELD {
        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }

    static class PUTSTATIC_I extends AbstractInstruction {
        public String getName() {
            return "putstatic_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Assign <code>int</code> or <code>float</code> static field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i>, <i>class</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class PUTSTATIC_L extends AbstractInstruction {
        public String getName() {
            return "putstatic_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Assign <code>long</code> or <code>double</code> static field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i>, <i>class</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class PUTSTATIC_O extends AbstractInstruction {
        public String getName() {
            return "putstatic_o";
        }

        public void printOperation(PrintWriter out) {
            out.println("Assign <code>reference</code> static field");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i>, <i>class</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class REM_I extends TwoOperandArithmetic {
        public String getName() {
            return "rem_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Remainder <code>int</code>");
        }
    }

    static class REM_L extends TwoOperandArithmetic {
        public String getName() {
            return "rem_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Remainder <code>long</code>");
        }
    }

    public abstract static class RETURN extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();
        abstract protected String getIncomingStack();

        public String getName() {
            return "return_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Return from method with return type <code>" + getType() + "</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println(getIncomingStack() + " =&gt; _");
        }
    }


    static class RETURN_I extends RETURN {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }

        protected String getIncomingStack() {
            return "<i>value</i>";
        }
    }


    static class RETURN_L extends RETURN {
        protected char getTypeLetter() {
            return 'l';
        }

        protected String getType() {
            return "long";
        }

        protected String getIncomingStack() {
            return "<i>value</i>";
        }
    }


    static class RETURN_O extends RETURN {
        protected char getTypeLetter() {
            return 'o';
        }

        protected String getType() {
            return "reference";
        }

        protected String getIncomingStack() {
            return "<i>value</i>";
        }
    }


    static class RETURN_V extends RETURN {
        protected char getTypeLetter() {
            return 'v';
        }

        protected String getType() {
            return "void";
        }

        protected String getIncomingStack() {
            return "_";
        }
    }

    static class SHL_I extends TwoOperandArithmetic {
        public String getName() {
            return "shl_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Shift left <code>int</code>");
        }
    }

    static class SHL_L extends TwoOperandArithmetic {
        public String getName() {
            return "shl_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Shift left <code>long</code>");
        }
    }

    static class SHR_I extends TwoOperandArithmetic {
        public String getName() {
            return "shr_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Arithmetic shift right <code>int</code>");
        }
    }

    static class SHR_L extends TwoOperandArithmetic {
        public String getName() {
            return "shr_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Arithmetic shift right <code>long</code>");
        }
    }

    static class STORE extends AbstractInstruction {
        public String getName() {
            return "store";
        }

        public void printOperation(PrintWriter out) {
            out.println("Store single word value into local variable");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class STOREPARM extends AbstractInstruction {
        public String getName() {
            return "storeparm";
        }

        public void printOperation(PrintWriter out) {
            out.println("Store single word value to parameter");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class STOREPARM_I2 extends AbstractInstruction {
        public String getName() {
            return "storeparm_i2";
        }

        public void printOperation(PrintWriter out) {
            out.println("Store double word value to parameter");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class STORE_I extends AbstractInstruction {
        public String getName() {
            return "store_&lt;i&gt;";
        }

        public void printOperation(PrintWriter out) {
            out.println("Store single word value into local variable");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printForms(PrintWriter out) {
            for (int i = 0; i <= 15; i++) {
                printForm(out, "store_" + i, i+48);
                out.println("<br>");
            }
        }
    }

    static class STORE_I2 extends AbstractInstruction {
        public String getName() {
            return "store_i2";
        }

        public void printOperation(PrintWriter out) {
            out.println("Store double word value into local variable");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value</i> =&gt; ...");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>index</i></td></tr></Table>");
        }
    }

    static class SUB_I extends TwoOperandArithmetic {
        public String getName() {
            return "sub_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Subtract <code>int</code>");
        }
    }

    static class SUB_L extends TwoOperandArithmetic {
        public String getName() {
            return "sub_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Subtract <code>long</code>");
        }
    }

    public abstract static class TABLESWITCH extends AbstractInstruction {
        abstract protected char getTypeLetter();
        abstract protected String getType();

        public String getName() {
            return "tableswitch_" + getTypeLetter();
        }

        public void printOperation(PrintWriter out) {
            out.println("Table switch, with key of type <code>" + getType() + "</code>");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("<i>key</i> =&gt; _");
        }

        public void printFormat(PrintWriter out) {
            out.println("FIXME: this one's tricky.");
        }
    }


    static class TABLESWITCH_I extends TABLESWITCH {
        protected char getTypeLetter() {
            return 'i';
        }

        protected String getType() {
            return "int";
        }
    }

    static class TABLESWITCH_S extends TABLESWITCH {
        protected char getTypeLetter() {
            return 's';
        }

        protected String getType() {
            return "short";
        }
    }

    static class THROW extends AbstractInstruction {
        public String getName() {
            return "throw";
        }

        public void printOperation(PrintWriter out) {
            out.println("Throw an exception");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("<i>exception</i> =&gt; _");
        }
    }

    public abstract static class TwoOperandArithmetic extends AbstractInstruction {
        public void printOperandStack(PrintWriter out) {
            out.println("..., <i>value1</i>, <i>value2</i> =&gt; ..., <i>result</i>");
        }
    }

    static class USHR_I extends TwoOperandArithmetic {
        public String getName() {
            return "ushr_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Logical shift right <code>int</code>");
        }
    }

    static class USHR_L extends TwoOperandArithmetic {
        public String getName() {
            return "ushr_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Logical shift right <code>long</code>");
        }
    }

    static class WIDE_I extends AbstractInstruction {
        public String getName() {
            return "wide_&lt;i&gt;";
        }

        public void printOperation(PrintWriter out) {
            out.println("Set high order bits of index for subsequent bytecode instruction");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("Same as modified instruction");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>&lt;opcode&gt;</i></td></tr>");
            out.println("<tr><td><i>indexbyte</i></td></tr></Table>");
        }

        public void printForms(PrintWriter out) {
            printForm(out, "wide_m1", 72);
            out.println("<br>");
            printForm(out, "wide_0", 73);
            out.println("<br>");
            printForm(out, "wide_1", 74);
        }
    }

    static class WIDE_INT extends AbstractInstruction {
        public String getName() {
            return "wide_int";
        }

        public void printOperation(PrintWriter out) {
            out.println("Extend inlined data for subsequent bytecode instruction to 32 bits");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("Same as modified instruction");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>&lt;opcode&gt;</i></td></tr>");
            out.println("<tr><td><i>indexbyte1</i></td></tr>");
            out.println("<tr><td><i>indexbyte2</i></td></tr>");
            out.println("<tr><td><i>indexbyte3</i></td></tr>");
            out.println("<tr><td><i>indexbyte4</i></td></tr></Table>");
        }
    }

    static class WIDE_SHORT extends AbstractInstruction {
        public String getName() {
            return "wide_short";
        }

        public void printOperation(PrintWriter out) {
            out.println("Extend inlined data for subsequent bytecode instruction to 16 bits");
        }

        public void printOperandStack(PrintWriter out) {
            out.println("Same as modified instruction");
        }

        public void printFormat(PrintWriter out) {
            out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr>");
            out.println("<tr><td><i>&lt;opcode&gt;</i></td></tr>");
            out.println("<tr><td><i>indexbyte1</i></td></tr>");
            out.println("<tr><td><i>indexbyte2</i></td></tr></Table>");
        }
    }

    static class XOR_I extends TwoOperandArithmetic {
        public String getName() {
            return "xor_i";
        }

        public void printOperation(PrintWriter out) {
            out.println("Boolean XOR <code>int</code>");
        }
    }

    static class XOR_L extends TwoOperandArithmetic {
        public String getName() {
            return "xor_l";
        }

        public void printOperation(PrintWriter out) {
            out.println("Boolean XOR <code>long</code>");
        }
    }


}