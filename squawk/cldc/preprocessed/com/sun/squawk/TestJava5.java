//if[JAVA5SYNTAX]*/
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

import com.sun.squawk.Java5Marker;
import java.lang.annotation.ElementType;

@Java5Marker
public class TestJava5 {

    public static void main(String[] args) {
        System.out.print("Running: " + TestJava5.class.getName());
        for (int i = 0 ; i < args.length ; i++) {
            System.out.print(" " + args[i]);
        }
        System.out.println();

        try {
            runTests();
        } finally {
            System.out.println("runTests() returned!");
        }
    }
    
    public static void runTests() {
        testStringConcat();
        testForLoop();
        testAutoUnboxing();
        testEnums();
    }
    
    public static void testAutoUnboxing() {
        testAutoUnboxingBoolean(true, true);
        testAutoUnboxingByte((byte) 10, (byte) 10);
        testAutoUnboxingCharacter('a', 'a');
        testAutoUnboxingDouble(10.0, 10.0);
        testAutoUnboxingFloat((float) 10.0, (float) 10.0);
        testAutoUnboxingInteger(10, 10);
        testAutoUnboxingLong(10L, 10L);
        testAutoUnboxingShort((short) 10, (short) 10);
    }
    
    public static void testAutoUnboxingShort(Short i, short j) {
        System.out.print("Short = short: ");
        System.out.println(i = j);
        System.out.print("Short == short: ");
        System.out.println(i == j);
    }

    public static void testAutoUnboxingLong(Long i, long j) {
        System.out.print("Long = long: ");
        System.out.println(i = j);
        System.out.print("Long == long: ");
        System.out.println(i == j);
    }

    public static void testAutoUnboxingFloat(Float i, float j) {
        System.out.print("Float = float: ");
        System.out.println(i = j);
        System.out.print("Float == float: ");
        System.out.println(i == j);
    }

    public static void testAutoUnboxingCharacter(Character i, char j) {
        System.out.print("Character = char: ");
        System.out.println(i = j);
        System.out.print("Character == char: ");
        System.out.println(i == j);
    }

    public static void testAutoUnboxingDouble(Double i, double j) {
        System.out.print("Double = double: ");
        System.out.println(i = j);
        System.out.print("Double == double: ");
        System.out.println(i == j);
    }

    public static void testAutoUnboxingByte(Byte i, byte j) {
        System.out.print("Byte = byte: ");
        System.out.println(i = j);
        System.out.print("Byte == byte: ");
        System.out.println(i == j);
    }

    public static void testAutoUnboxingBoolean(Boolean i, boolean j) {
        System.out.print("Boolean = boolean: ");
        System.out.println(i = j);
        System.out.print("Boolean == boolean: ");
        System.out.println(i == j);
    }

    public static void testAutoUnboxingInteger(Integer i, int j) {
        System.out.print("Integer = int: ");
        System.out.println(i = j);
        System.out.print("Integer == int: ");
        System.out.println(i == j);
    }
    
    public static void testEnums() {
        System.out.print("Enum: ");
        System.out.println(ElementType.class.getName());
        System.out.println("Values:");
        for (ElementType type: ElementType.values()) {
            System.out.println(type);
        }
        System.out.print("ElementType.CONSTRUCTOR == ElementType.valueOf(\"CONSTRUCTOR\"): ");
        System.out.println(ElementType.CONSTRUCTOR == ElementType.valueOf("CONSTRUCTOR"));
    }
    
    public static void testForLoop() {
        int[] ints = new int[10];
        for (int i = 0; i < ints.length; i++) {
            ints[i] = i * 10;
        }
        for (int i : ints) {
            System.out.print(i);
            System.out.print(' ');
        }
        System.out.println();
    }

    public static void testStringConcat() {
        int a = 10;
        System.out.println("Should be 10: " + a);
    }
    
}
