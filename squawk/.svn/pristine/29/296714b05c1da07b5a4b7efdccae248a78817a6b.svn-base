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

import java.io.PrintWriter;
import java.lang.reflect.Field;

public abstract class AbstractInstruction implements Instruction {
    public void printOperation(PrintWriter out) {
        out.println("FIXME: Not done yet!");
    }

    public void printFormat(PrintWriter out) {
        out.println("<Table Border=\"1\"><tr><td><i>" + getName() + "</i></td></tr></Table>");
    }

    public void printForms(PrintWriter out) {
        printForm(out, getName(), getOpcode());
    }

    protected final void printForm(PrintWriter out, String name, int opcode) {
        sbdocgen.reportOpcode(opcode);

        out.print("<i>" + name + "</i> = ");
        out.print(opcode);
        String h = Integer.toHexString(opcode);
        while (h.length() < 2)
            h = "0" + h;
        out.println(" (0x" + h + ")");
    }

    static final Class<?> opcClass;
    static {
        try {
            opcClass = Class.forName("com.sun.squawk.vm.OPC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not find class com.sun.squawk.vm.OPC");
        }
    }

    final int getOpcode() {
        String fieldName = getName().toUpperCase();
        try {
            Field field = opcClass.getDeclaredField(fieldName);
            int opcode = field.getInt(null);
            return opcode;
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("could not get value of com.sun.squawk.vm.OPC." + fieldName, ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("could not get value of com.sun.squawk.vm.OPC." + fieldName, ex);
        } catch (SecurityException ex) {
            throw new RuntimeException("could not get value of com.sun.squawk.vm.OPC." + fieldName, ex);
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException("could not get value of com.sun.squawk.vm.OPC." + fieldName, ex);
        }
    }

    public void printOperandStack(PrintWriter out) {
        out.println("FIXME: Not done yet!");
    }

    public void printDescription(PrintWriter out) {
        out.println("FIXME: Not done yet!");
    }

    public boolean hasNotes() {
        return false;
    }

    public void printNotes(PrintWriter out) {
        return;
    }

    public boolean hasExceptions() {
        return false;
    }

    public void printExceptions(PrintWriter out) {
        return;
    }

    public int compareTo(Instruction i) {
        return getName().toLowerCase().compareTo(i.getName().toLowerCase());
    }

}