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

package com.sun.squawk.vm;

/**
 * This class contains the offsets for methods that must be directly accessed by the
 * VM or other Squawk tools such as the mapper. The romizer ensures that these offsets
 * are correct when it creates the image for the bootstrap suite. The offset for a method
 * is its index in the relevant table of methods.
 *
 * The name of the constant must be composed of the name of the class that defines the
 * method (with '.'s replaced by '_'s) and the name of the method with a '$' separating them.
 * Virtual methods must be prefixed with "virtual$" and to disambiguate overloaded methods,
 * the parameter types can be appended to the identifier, each prefixed with a '$'. E.g.:
 *
 *   Method                                |  Constant identifier
 *  ---------------------------------------+-------------------------------------------------
 *   Klass.getInternalName()               | virtual$com_sun_squawk_Klass$getInternalName
 *   static Klass.getInternalName(Klass)   | com_sun_squawk_Klass$getInternalName
 *   static Klass.isOop(Klass, int)        | com_sun_squawk_Klass$isOop$com_sun_squawk_Klass$int
 *   static Klass.isOop(Klass, int, char)  | com_sun_squawk_Klass$isOop$com_sun_squawk_Klass$int$char
 */
public interface MethodOffsets {
    public final static int com_sun_squawk_VM$startup                         = 1;
    public final static int com_sun_squawk_VM$undefinedNativeMethod           = 2;
    public final static int com_sun_squawk_VM$callRun                         = 3;
    public final static int com_sun_squawk_VM$getStaticOop                    = 4;
    public final static int com_sun_squawk_VM$getStaticInt                    = 5;
    public final static int com_sun_squawk_VM$getStaticLong                   = 6;
    public final static int com_sun_squawk_VM$putStaticOop                    = 7;
    public final static int com_sun_squawk_VM$putStaticInt                    = 8;
    public final static int com_sun_squawk_VM$putStaticLong                   = 9;
    public final static int com_sun_squawk_VM$yield                           = 10;
    public final static int com_sun_squawk_VM$nullPointerException            = 11;
    public final static int com_sun_squawk_VM$arrayIndexOutOfBoundsException  = 12;
    public final static int com_sun_squawk_VM$arithmeticException             = 13;
    public final static int com_sun_squawk_VM$abstractMethodError             = 14;
    public final static int com_sun_squawk_VM$arrayStoreException             = 15;
    public final static int com_sun_squawk_VM$monitorenter                    = 16;
    public final static int com_sun_squawk_VM$monitorexit                     = 17;
    public final static int com_sun_squawk_VM$checkcastException              = 18;
    public final static int com_sun_squawk_VM$class_clinit                    = 19;
    public final static int com_sun_squawk_VM$_new                            = 20;
    public final static int com_sun_squawk_VM$newarray                        = 21;
    public final static int com_sun_squawk_VM$newdimension                    = 22;
    public final static int com_sun_squawk_VM$_lcmp                           = 23;
/*if[ENABLE_SDA_DEBUGGER]*/
    public final static int com_sun_squawk_VM$reportException                 = 24;
    public final static int com_sun_squawk_VM$reportBreakpoint                = 25;
    public final static int com_sun_squawk_VM$reportStepEvent                 = 26;
/*end[ENABLE_SDA_DEBUGGER]*/
    
    public final static int virtual$java_lang_Object$toString            = 8;
    public final static int virtual$java_lang_Object$abstractMethodError = 9;
    public final static int java_lang_Object$missingMethodError          = 1;
    
/*if[FINALIZATION]*/
    public final static int virtual$java_lang_Object$finalize            = 10;
/*end[FINALIZATION]*/

}

