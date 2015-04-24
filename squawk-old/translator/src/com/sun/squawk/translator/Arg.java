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

package com.sun.squawk.translator;

import com.sun.squawk.VM;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.util.Assert;
import java.io.PrintStream;

/**
 * This class describes and holds the value of translator arguments
 */
public final class Arg {
    
    private static final char BOOLEAN = 'B';
    private static final char INT = 'I';
    
    private String name;
    private char type;
    private String defaultValue;
    private String usageMsg;
    private int intValue;
    private boolean boolValue;
    
    Arg(String name, char type, String defaultValue, String usageMsg) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.usageMsg = usageMsg;
    }
    
    public void setValue() {
        String result = System.getProperty(getPropertyName());
        if (type == INT) {
            intValue = parseInt(result);
        } else {
            boolValue = parseBool(result);
        }
    }
    
    public int getInt() {
        Assert.that(type == INT);
        return intValue;
    }
    
    public boolean getBool() {
        Assert.that(type == BOOLEAN);
        return boolValue;
    }
    
    void setBoolValue(boolean val) {
        boolValue = val;
    }
    
    public String getPropertyName() {
        return "translator." + name;
    }
    
    public String getOptionName() {
        return "-" + name + ":";
    }
    
    private int parseInt(String result) {
        if (result != null) {
            try {
                return Integer.parseInt(result);
            } catch (NumberFormatException e) {
                System.err.println("Illformed integer value " + result + " for translator property " + name + ". Using default value " + defaultValue);
                // fall through to pick up default
            }
        }
        try {
            return Integer.parseInt(defaultValue);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e.toString());
        }
    }
    
    private boolean parseBool(String result) {
        if (result != null) {
            result = result.toLowerCase();
            if (result.equals("true")) {
                return true;
            } else if (result.equals("false")) {
                return false;
            }
            System.err.println("Illformed boolean value " + result + " for translator property " + name + ". Using default value " + defaultValue);
        }
        return parseBool(defaultValue);
    }
    
    public char getType() {
        return type;
    }
    
    public void printOne(PrintStream out, boolean asParameters) {
        out.print("    -");
        if (!asParameters) {
            out.print("Dtranslator.");
        }
        out.print(name);
        if (asParameters) {
            out.print(":");
        } else {
            out.print("=");
        }
        out.print(usageMsg);
        if (type == INT) {
            out.println(" Default value is " + defaultValue + ".");
        } else {
            out.println("\n" +
                    "                          <bool> must be true or false. Default value is " + defaultValue + ".");
        }
        
    }
    
    public final static int HELP = 0;
    public final static int OPTIMIZE_CONSTANT_OBJECTS = 1;
    public final static int DEAD_METHOD_ELIMINATION = 2;
    public final static int DELETE_UNUSED_PRIVATE_CONSTRUCTORS = 3;
    public final static int PRINT_STATS = 4;
    public final static int VERBOSE = 5;
    public final static int DEAD_STRING_ELIMINATION = 6;
    public final static int DEAD_CLASS_ELIMINATION = 7;
//    public final static int INLINE_METHOD_LIMIT = 8;
//    public final static int INLINE_OBJECT_CONSTRUCTOR = 9;
//    public final static int OPTIMIZE_BYTECODE = 10;
//    public final static int OPTIMIZE_BYTECODE_CONTROL = 11;
//    public final static int OPTIMIZE_DEADCODE = 12;
//    public final static int INLINE_NEVER_OVERRIDDEN = 13;
    
    public final static int LAST_ARG = DEAD_CLASS_ELIMINATION;
    
    final static Arg[] translatorArgs = new Arg[LAST_ARG + 1];
    
    public static Arg get(int index) {
        return translatorArgs[index];
    }
    
    private static void initArg(int index, String name, char type, String defaultValue, String usageMsg) {
        translatorArgs[index] = new Arg(name, type, defaultValue, usageMsg);
    }
    
    static void defineOptions() {
        initArg(HELP, "help", Arg.BOOLEAN, "false",
                "Display the final state of the translator options.");
        initArg(OPTIMIZE_CONSTANT_OBJECTS, "optimizeConstantObjects", Arg.BOOLEAN, "true", // GOOD
                "<bool> Reorder class objects to allow small indexes for common objects.");
        initArg(DEAD_METHOD_ELIMINATION, "deadMethodElimination", Arg.BOOLEAN, "true", //?
                "<bool> Remove uncalled (and uncallable) methods.");
        initArg(DELETE_UNUSED_PRIVATE_CONSTRUCTORS, "deleteUnusedPrivateConstructors", Arg.BOOLEAN, "true",  
                "<bool> Remove uncalled private constructors. This will disable Class.newInstance() if deleted.");
        initArg(PRINT_STATS, "stats", Arg.BOOLEAN, "false",
                "<bool> Print translator statistics.");
        initArg(VERBOSE, "verbose", Arg.BOOLEAN, "false",
                "<bool> Print translator actions.");
        initArg(DEAD_STRING_ELIMINATION, "deadStringElimination", Arg.BOOLEAN, "true",
                "<bool> Remove unused string constants.");
        
//        initArg(OPTIMIZE_BYTECODE, "optimizeBytecode", Arg.BOOLEAN, "true",     // GOOD
//                "<bool> optimize byte codes.");
//        initArg(OPTIMIZE_BYTECODE_CONTROL, "optimizeBytecode.control", Arg.BOOLEAN, "true",     // GOOD
//                "<bool> optimize control byte codes.");
//        initArg(OPTIMIZE_DEADCODE, "optimizeDeadCode", Arg.BOOLEAN, "true", //GOOD
//                "<bool> delete unreachable bytecodes.");
//        initArg(INLINE_METHOD_LIMIT, "inlineMethodLimit", Arg.INT, "3",
//                "<n> Inline known methods with <m> bytecodes or less.\n" +
//                "                           <m> = <n> + <num parameters>. Don't inline if <n> = 0.");
//        initArg(INLINE_OBJECT_CONSTRUCTOR, "inlineObjectConstructor", Arg.BOOLEAN, "true", //?
//                "<bool> If inlining, always inline the constructor or Object.");
        initArg(DEAD_CLASS_ELIMINATION, "deadClassElimination", Arg.BOOLEAN, "true",
                "<bool> Remove unused classes.");
        // this option make the debugging proxy's job have to do whole-suite analysis the exact same way as the original suite creation, 
        // which is slow and error prone. So turn off for now. 
//        initArg(INLINE_NEVER_OVERRIDDEN, "inlineNeverOverridden", Arg.BOOLEAN, "false", 
//                "<bool> Allow inlines of methods that are not final, but in fact are never overridden.");        
        
    }
    /**
     * Read translator properties and set corresponding options.
     */
    static void setOptions() {
        for (int i = 0; i < translatorArgs.length; i++) {
            translatorArgs[i].setValue();
        }
                
        Arg verbose = get(VERBOSE);
        verbose.setBoolValue(verbose.getBool() | VM.isVerbose() | VM.isVeryVerbose() | Tracer.isTracing("converting"));
        
        if (get(HELP).getBool() || verbose.getBool()) {
            System.out.println("Translator properties and current values:");
            for (int i = 0; i < translatorArgs.length; i++) {
                Arg arg = translatorArgs[i];
                System.out.print("    ");
                System.out.print(arg.getPropertyName());
                System.out.print("=");
                if (arg.getType() == INT) {
                    System.out.println(arg.getInt());
                } else {
                    System.out.println(arg.getBool());
                }
            }
        }
    }
    
} /* Arg */
