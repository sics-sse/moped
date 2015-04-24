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

package com.sun.squawk.builder.gen;

import java.util.*;

/**
 * The Squawk VM instruction set definitions.
 *
 */
public final class Instruction {

    /**
     * The instruction's numeric opcode. Extended instructions have an opcode >= 256.
     */
    public final int opcode;

    /**
     * The instruction's mnemonic.
     */
    public final String mnemonic;

    /**
     * The type of the instruction's immediate parameter.
     */
    public final IParm iparm;

    /**
     * The control flow effect of this instruction.
     */
    public final Flow flow;

    /**
     * The compact version of a wide instruction.
     */
    public final Instruction compact;

    /**
     * The wide version of a compact instruction.
     */
    private Instruction wide;
    public Instruction wide() {
        return wide;
    }

    /**
     * The semantics of this instruction with respect to the operand stack.
     */
    public final String operandStackEffect;

    public void widen(int opcode, List<Instruction> list) {
        Instruction wide = new Instruction(opcode, mnemonic+"_wide", iparm, this, null, operandStackEffect, flow);
        list.add(wide);
        this.wide = wide;
    }

    private static List<Instruction> instructions;
    private static List<Instruction> floatInstructions;
    private static List<Instruction> allInstructions;

    /**
     * Gets the list of instructions in the Squawk VM instruction set.
     */
    public static List<Instruction> getInstructions() {
        if (instructions == null) {

            Factory f = new Factory(0);

            f.define(16, "const",           IParm.N, ":I",        Flow.NEXT);
            f.define(16, "object",          IParm.N, ":O",        Flow.NEXT);
            f.define(16, "load",            IParm.N, ":W",        Flow.NEXT);
            f.define(16, "store",           IParm.N, "W:",        Flow.NEXT);
            f.define(8, "loadparm",         IParm.N, ":W",        Flow.NEXT);

            f.define("wide_m1",             IParm.P, ":",         Flow.CHANGE, false);
            f.define("wide_0",              IParm.P, ":",         Flow.CHANGE, false);
            f.define("wide_1",              IParm.P, ":",         Flow.CHANGE, false);
            f.define("wide_short",          IParm.P, ":",         Flow.CHANGE, false);
            f.define("wide_int",            IParm.P, ":",         Flow.CHANGE, false);

            f.define("escape",              IParm.P, null,        Flow.CHANGE, false);
            f.define("escape_wide_m1",      IParm.P, null,        Flow.CHANGE, false);
            f.define("escape_wide_0",       IParm.P, null,        Flow.CHANGE, false);
            f.define("escape_wide_1",       IParm.P, null,        Flow.CHANGE, false);
            f.define("escape_wide_short",   IParm.P, null,        Flow.CHANGE, false);
            f.define("escape_wide_int",     IParm.P, null,        Flow.CHANGE, false);

            f.define("catch",               IParm.N, ":O",        Flow.NEXT, false);
            f.define("const_null",          IParm.N, ":O",        Flow.NEXT, false);
            f.define("const_m1",            IParm.N, ":I",        Flow.NEXT, false);
            f.define("const_byte",          IParm.B, ":I",        Flow.NEXT, false);
            f.define("const_short",         IParm.S, ":I",        Flow.NEXT, false);
            f.define("const_char",          IParm.C, ":I",        Flow.NEXT, false);
            f.define("const_int",           IParm.I, ":I",        Flow.NEXT, false);
            f.define("const_long",          IParm.L, ":L",        Flow.NEXT, false);

            f.define("object",              IParm.A, ":O",        Flow.NEXT, true);
            f.define("load",                IParm.A, ":W",        Flow.NEXT, true);
            f.define("load_i2",             IParm.A, ":L",        Flow.NEXT, true);
            f.define("store",               IParm.A, "W:",        Flow.NEXT, true);
            f.define("store_i2",            IParm.A, "L:",        Flow.NEXT, true);
            f.define("loadparm",            IParm.A, ":W",        Flow.NEXT, true);
            f.define("loadparm_i2",         IParm.A, ":L",        Flow.NEXT, true);
            f.define("storeparm",           IParm.A, "W:",        Flow.NEXT, true);
            f.define("storeparm_i2",        IParm.A, "L:",        Flow.NEXT, true);
            f.define("inc",                 IParm.A, ":",         Flow.NEXT, true);
            f.define("dec",                 IParm.A, ":",         Flow.NEXT, true);
            f.define("incparm",             IParm.A, ":",         Flow.NEXT, true);
            f.define("decparm",             IParm.A, ":",         Flow.NEXT, true);

            f.define("goto",                IParm.B, ":",         Flow.CHANGE, true);
            f.define("if_eq_o",             IParm.B, "O:",        Flow.CHANGE, true);
            f.define("if_ne_o",             IParm.B, "O:",        Flow.CHANGE, true);
            f.define("if_cmpeq_o",          IParm.B, "OO:",       Flow.CHANGE, true);
            f.define("if_cmpne_o",          IParm.B, "OO:",       Flow.CHANGE, true);
            f.define("if_eq_i",             IParm.B, "I:",        Flow.CHANGE, true);
            f.define("if_ne_i",             IParm.B, "I:",        Flow.CHANGE, true);
            f.define("if_lt_i",             IParm.B, "I:",        Flow.CHANGE, true);
            f.define("if_le_i",             IParm.B, "I:",        Flow.CHANGE, true);
            f.define("if_gt_i",             IParm.B, "I:",        Flow.CHANGE, true);
            f.define("if_ge_i",             IParm.B, "I:",        Flow.CHANGE, true);
            f.define("if_cmpeq_i",          IParm.B, "II:",       Flow.CHANGE, true);
            f.define("if_cmpne_i",          IParm.B, "II:",       Flow.CHANGE, true);
            f.define("if_cmplt_i",          IParm.B, "II:",       Flow.CHANGE, true);
            f.define("if_cmple_i",          IParm.B, "II:",       Flow.CHANGE, true);
            f.define("if_cmpgt_i",          IParm.B, "II:",       Flow.CHANGE, true);
            f.define("if_cmpge_i",          IParm.B, "II:",       Flow.CHANGE, true);
            f.define("if_eq_l",             IParm.B, "L:",        Flow.CHANGE, true);
            f.define("if_ne_l",             IParm.B, "L:",        Flow.CHANGE, true);
            f.define("if_lt_l",             IParm.B, "L:",        Flow.CHANGE, true);
            f.define("if_le_l",             IParm.B, "L:",        Flow.CHANGE, true);
            f.define("if_gt_l",             IParm.B, "L:",        Flow.CHANGE, true);
            f.define("if_ge_l",             IParm.B, "L:",        Flow.CHANGE, true);
            f.define("if_cmpeq_l",          IParm.B, "LL:",       Flow.CHANGE, true);
            f.define("if_cmpne_l",          IParm.B, "LL:",       Flow.CHANGE, true);
            f.define("if_cmplt_l",          IParm.B, "LL:",       Flow.CHANGE, true);
            f.define("if_cmple_l",          IParm.B, "LL:",       Flow.CHANGE, true);
            f.define("if_cmpgt_l",          IParm.B, "LL:",       Flow.CHANGE, true);
            f.define("if_cmpge_l",          IParm.B, "LL:",       Flow.CHANGE, true);

            f.define("getstatic_i",         IParm.A, "O:I",       Flow.CALL, true);
            f.define("getstatic_o",         IParm.A, "O:O",       Flow.CALL, true);
            f.define("getstatic_l",         IParm.A, "O:L",       Flow.CALL, true);

            f.define("class_getstatic_i",   IParm.A, ":I",        Flow.CALL, true);
            f.define("class_getstatic_o",   IParm.A, ":O",        Flow.CALL, true);
            f.define("class_getstatic_l",   IParm.A, ":L",        Flow.CALL, true);

            f.define("putstatic_i",         IParm.A, "OI:",       Flow.CALL, true);
            f.define("putstatic_o",         IParm.A, "OO:",       Flow.CALL, true);
            f.define("putstatic_l",         IParm.A, "OL:",       Flow.CALL, true);

            f.define("class_putstatic_i",   IParm.A, "I:",        Flow.CALL, true);
            f.define("class_putstatic_o",   IParm.A, "O:",        Flow.CALL, true);
            f.define("class_putstatic_l",   IParm.A, "L:",        Flow.CALL, true);

            f.define("getfield_i",          IParm.A, "O:I",       Flow.CALL, true);
            f.define("getfield_b",          IParm.A, "O:I",       Flow.CALL, true);
            f.define("getfield_s",          IParm.A, "O:I",       Flow.CALL, true);
            f.define("getfield_c",          IParm.A, "O:I",       Flow.CALL, true);
            f.define("getfield_o",          IParm.A, "O:I",       Flow.CALL, true);
            f.define("getfield_l",          IParm.A, "O:L",       Flow.CALL, true);

            f.define("getfield0_i",         IParm.A, ":I",        Flow.NEXT, true);
            f.define("getfield0_b",         IParm.A, ":I",        Flow.NEXT, true);
            f.define("getfield0_s",         IParm.A, ":I",        Flow.NEXT, true);
            f.define("getfield0_c",         IParm.A, ":I",        Flow.NEXT, true);
            f.define("getfield0_o",         IParm.A, ":I",        Flow.NEXT, true);
            f.define("getfield0_l",         IParm.A, ":L",        Flow.NEXT, true);

            f.define("putfield_i",          IParm.A, "OI:",       Flow.CALL, true);
            f.define("putfield_b",          IParm.A, "OI:",       Flow.CALL, true);
            f.define("putfield_s",          IParm.A, "OI:",       Flow.CALL, true);
            f.define("putfield_o",          IParm.A, "OI:",       Flow.CALL, true);
            f.define("putfield_l",          IParm.A, "OL:",       Flow.CALL, true);

            f.define("putfield0_i",         IParm.A, "I:",        Flow.NEXT, true);
            f.define("putfield0_b",         IParm.A, "I:",        Flow.NEXT, true);
            f.define("putfield0_s",         IParm.A, "I:",        Flow.NEXT, true);
            f.define("putfield0_o",         IParm.A, "I:",        Flow.NEXT, true);
            f.define("putfield0_l",         IParm.A, "L:",        Flow.NEXT, true);

            f.define("invokevirtual_i",     IParm.A, "O*:I",      Flow.CALL, true);
            f.define("invokevirtual_v",     IParm.A, "O*:",       Flow.CALL, true);
            f.define("invokevirtual_l",     IParm.A, "O*:L",      Flow.CALL, true);
            f.define("invokevirtual_o",     IParm.A, "O*:O",      Flow.CALL, true);

            f.define("invokestatic_i",      IParm.A, "O*:I",      Flow.CALL, true);
            f.define("invokestatic_v",      IParm.A, "O*:",       Flow.CALL, true);
            f.define("invokestatic_l",      IParm.A, "O*:L",      Flow.CALL, true);
            f.define("invokestatic_o",      IParm.A, "O*:O",      Flow.CALL, true);

            f.define("invokesuper_i",       IParm.A, "O*:I",      Flow.CALL, true);
            f.define("invokesuper_v",       IParm.A, "O*:",       Flow.CALL, true);
            f.define("invokesuper_l",       IParm.A, "O*:L",      Flow.CALL, true);
            f.define("invokesuper_o",       IParm.A, "O*:O",      Flow.CALL, true);

            f.define("invokenative_i",      IParm.A, "*:I",       Flow.CALL, true);
            f.define("invokenative_v",      IParm.A, "*:",        Flow.CALL, true);
            f.define("invokenative_l",      IParm.A, "*:L",       Flow.CALL, true);
            f.define("invokenative_o",      IParm.A, "*:O",       Flow.CALL, true);

            f.define("findslot",            IParm.A, "OO:I",      Flow.CALL, true);
            f.define("extend",              IParm.A, ":",         Flow.NEXT, true);

            f.define("invokeslot_i",        IParm.N, "IO*:I",     Flow.CALL, false);
            f.define("invokeslot_v",        IParm.N, "IO*:",      Flow.CALL, false);
            f.define("invokeslot_l",        IParm.N, "IO*:L",     Flow.CALL, false);
            f.define("invokeslot_o",        IParm.N, "IO*:I",     Flow.CALL, false);

            f.define("return_v",            IParm.N, ":",         Flow.CHANGE, false);
            f.define("return_i",            IParm.N, "I:",        Flow.CHANGE, false);
            f.define("return_l",            IParm.N, "L:",        Flow.CHANGE, false);
            f.define("return_o",            IParm.N, "O:",        Flow.CHANGE, false);

            f.define("tableswitch_i",       IParm.T, "I:",        Flow.CHANGE, false);
            f.define("tableswitch_s",       IParm.T, "I:",        Flow.CHANGE, false);

            f.define("extend0",             IParm.N, ":",         Flow.NEXT, false);

            f.define("add_i",               IParm.N, "II:I",      Flow.NEXT, false);
            f.define("sub_i",               IParm.N, "II:I",      Flow.NEXT, false);
            f.define("and_i",               IParm.N, "II:I",      Flow.NEXT, false);
            f.define("or_i",                IParm.N, "II:I",      Flow.NEXT, false);
            f.define("xor_i",               IParm.N, "II:I",      Flow.NEXT, false);
            f.define("shl_i",               IParm.N, "II:I",      Flow.NEXT, false);
            f.define("shr_i",               IParm.N, "II:I",      Flow.NEXT, false);
            f.define("ushr_i",              IParm.N, "II:I",      Flow.NEXT, false);
            f.define("mul_i",               IParm.N, "II:I",      Flow.NEXT, false);
            f.define("div_i",               IParm.N, "II:I",      Flow.CALL, false);
            f.define("rem_i",               IParm.N, "II:I",      Flow.CALL, false);
            f.define("neg_i",               IParm.N, "I:I",       Flow.NEXT, false);
            f.define("i2b",                 IParm.N, "I:I",       Flow.NEXT, false);
            f.define("i2s",                 IParm.N, "I:I",       Flow.NEXT, false);
            f.define("i2c",                 IParm.N, "I:I",       Flow.NEXT, false);
            f.define("add_l",               IParm.N, "LL:L",      Flow.NEXT, false);
            f.define("sub_l",               IParm.N, "LL:L",      Flow.NEXT, false);
            f.define("mul_l",               IParm.N, "LL:L",      Flow.NEXT, false);
            f.define("div_l",               IParm.N, "LL:L",      Flow.CALL, false);
            f.define("rem_l",               IParm.N, "LL:L",      Flow.CALL, false);
            f.define("and_l",               IParm.N, "LL:L",      Flow.NEXT, false);
            f.define("or_l",                IParm.N, "LL:L",      Flow.NEXT, false);
            f.define("xor_l",               IParm.N, "LL:L",      Flow.NEXT, false);
            f.define("neg_l",               IParm.N, "L:L",       Flow.NEXT, false);
            f.define("shl_l",               IParm.N, "IL:L",      Flow.NEXT, false);
            f.define("shr_l",               IParm.N, "IL:L",      Flow.NEXT, false);
            f.define("ushr_l",              IParm.N, "IL:L",      Flow.NEXT, false);
            f.define("l2i",                 IParm.N, "L:I",       Flow.NEXT, false);
            f.define("i2l",                 IParm.N, "I:L",       Flow.NEXT, false);
            f.define("throw",               IParm.N, "O:",        Flow.CALL, false);
            f.define("pop_1",               IParm.N, "W:",        Flow.NEXT, false);
            f.define("pop_2",               IParm.N, "WW:",       Flow.NEXT, false);
            f.define("monitorenter",        IParm.N, "O:",        Flow.CALL, false);
            f.define("monitorexit",         IParm.N, "O:",        Flow.CALL, false);
            f.define("class_monitorenter",  IParm.N, ":",         Flow.CALL, false);
            f.define("class_monitorexit",   IParm.N, ":",         Flow.CALL, false);
            f.define("arraylength",         IParm.N, "O:I:",      Flow.CALL, false);
            f.define("new",                 IParm.N, "O:O",       Flow.CALL, false);
            f.define("newarray",            IParm.N, "OI:O",      Flow.CALL, false);
            f.define("newdimension",        IParm.N, "OI:O",      Flow.CALL, false);
            f.define("class_clinit",        IParm.N, ":",         Flow.CALL, false);
            f.define("bbtarget_sys",        IParm.N, ":",         Flow.NEXT, false);
            f.define("bbtarget_app",        IParm.N, ":",         Flow.CALL, false);
            f.define("instanceof",          IParm.N, "OO:I",      Flow.CALL, false);
            f.define("checkcast",           IParm.N, "OO:O",      Flow.CALL, false);

            f.define("aload_i",             IParm.N, "OI:I",      Flow.CALL, false);
            f.define("aload_b",             IParm.N, "OI:I",      Flow.CALL, false);
            f.define("aload_s",             IParm.N, "OI:I",      Flow.CALL, false);
            f.define("aload_c",             IParm.N, "OI:I",      Flow.CALL, false);
            f.define("aload_o",             IParm.N, "OI:O",      Flow.CALL, false);
            f.define("aload_l",             IParm.N, "OI:L",      Flow.CALL, false);

            f.define("astore_i",            IParm.N, "OII:",      Flow.CALL, false);
            f.define("astore_b",            IParm.N, "OII:",      Flow.CALL, false);
            f.define("astore_s",            IParm.N, "OII:",      Flow.CALL, false);
            f.define("astore_o",            IParm.N, "OIO:",      Flow.CALL, false);
            f.define("astore_l",            IParm.N, "OIL:",      Flow.CALL, false);

            f.define("lookup_i",            IParm.N, "IO:I",      Flow.CALL, false);
            f.define("lookup_b",            IParm.N, "IO:I",      Flow.CALL, false);
            f.define("lookup_s",            IParm.N, "IO:I",      Flow.CALL, false);
            f.define("pause",               IParm.N, ":",         Flow.NEXT, false);
            
//            f.define("threadpoll",          IParm.N, ":",         Flow.CALL, false);

            instructions = f.getDefinitions();
        }
        return instructions;
    }

    public static List<Instruction> getFloatInstructions() {
        if (floatInstructions == null) {

            List<Instruction> nonFloatInstructions = getInstructions();
            Instruction last = (Instruction)nonFloatInstructions.get(nonFloatInstructions.size() - 1);
            Factory f = new Factory(last.opcode + 1);

            f.define("fcmpl",               IParm.N, "FF:I",      Flow.NEXT,   false);
            f.define("fcmpg",               IParm.N, "FF:I",      Flow.NEXT,   false);
            f.define("dcmpl",               IParm.N, "DD:I",      Flow.NEXT,   false);
            f.define("dcmpg",               IParm.N, "DD:I",      Flow.NEXT,   false);

            f.define("getstatic_f",         IParm.A, "O:F",       Flow.CALL, true);
            f.define("getstatic_d",         IParm.A, "O:D",       Flow.CALL, true);
            f.define("class_getstatic_f",   IParm.A, ":F",        Flow.CALL, true);
            f.define("class_getstatic_d",   IParm.A, ":D",        Flow.CALL, true);

            f.define("putstatic_f",         IParm.A, "OF:",       Flow.CALL, true);
            f.define("putstatic_d",         IParm.A, "OD:",       Flow.CALL, true);
            f.define("class_putstatic_f",   IParm.A, "F:",        Flow.CALL, true);
            f.define("class_putstatic_d",   IParm.A, "D:",        Flow.CALL, true);

            f.define("getfield_f",          IParm.A, "O:F",       Flow.CALL, true);
            f.define("getfield_d",          IParm.A, "O:D",       Flow.CALL, true);
            f.define("getfield0_f",         IParm.A, ":F",        Flow.NEXT, true);
            f.define("getfield0_d",         IParm.A, ":D",        Flow.NEXT, true);

            f.define("putfield_f",          IParm.A, "OF:",       Flow.CALL, true);
            f.define("putfield_d",          IParm.A, "OD:",       Flow.CALL, true);
            f.define("putfield0_f",         IParm.A, "F:",        Flow.NEXT, true);
            f.define("putfield0_d",         IParm.A, "D:",        Flow.NEXT, true);

            f.define("invokevirtual_f",     IParm.A, "O*:F",      Flow.CALL, true);
            f.define("invokevirtual_d",     IParm.A, "O*:D",      Flow.CALL, true);

            f.define("invokestatic_f",      IParm.A, "O*:F",      Flow.CALL, true);
            f.define("invokestatic_d",      IParm.A, "O*:D",      Flow.CALL, true);

            f.define("invokesuper_f",       IParm.A, "O*:F",      Flow.CALL, true);
            f.define("invokesuper_d",       IParm.A, "O*:D",      Flow.CALL, true);

            f.define("invokenative_f",      IParm.A, "*:F",       Flow.CALL, true);
            f.define("invokenative_d",      IParm.A, "*:D",       Flow.CALL, true);

            f.define("invokeslot_f",        IParm.N, "IO*:F",     Flow.CALL, false);
            f.define("invokeslot_d",        IParm.N, "IO*:D",     Flow.CALL, false);

            f.define("return_f",            IParm.N, "F:",        Flow.CHANGE, false);
            f.define("return_d",            IParm.N, "D:",        Flow.CHANGE, false);

            f.define("const_float",         IParm.F, ":F",        Flow.CHANGE, false);
            f.define("const_double",        IParm.D, ":D",        Flow.CHANGE, false);

            f.define("add_f",               IParm.N, "FF:F",      Flow.NEXT, false);
            f.define("sub_f",               IParm.N, "FF:F",      Flow.NEXT, false);
            f.define("mul_f",               IParm.N, "FF:F",      Flow.NEXT, false);
            f.define("div_f",               IParm.N, "FF:F",      Flow.NEXT, false);
            f.define("rem_f",               IParm.N, "FF:F",      Flow.NEXT, false);
            f.define("neg_f",               IParm.N, "F:F",       Flow.NEXT, false);
            f.define("add_d",               IParm.N, "DD:D",      Flow.NEXT, false);
            f.define("sub_d",               IParm.N, "DD:D",      Flow.NEXT, false);
            f.define("mul_d",               IParm.N, "DD:D",      Flow.NEXT, false);
            f.define("div_d",               IParm.N, "DD:D",      Flow.NEXT, false);
            f.define("rem_d",               IParm.N, "DD:D",      Flow.NEXT, false);
            f.define("neg_d",               IParm.N, "D:D",       Flow.NEXT, false);
            f.define("i2f",                 IParm.N, "I:F",       Flow.NEXT, false);
            f.define("l2f",                 IParm.N, "L:F",       Flow.NEXT, false);
            f.define("f2i",                 IParm.N, "F:I",       Flow.NEXT, false);
            f.define("f2l",                 IParm.N, "F:L",       Flow.NEXT, false);
            f.define("i2d",                 IParm.N, "I:D",       Flow.NEXT, false);
            f.define("l2d",                 IParm.N, "L:D",       Flow.NEXT, false);
            f.define("f2d",                 IParm.N, "F:D",       Flow.NEXT, false);
            f.define("d2i",                 IParm.N, "D:I",       Flow.NEXT, false);
            f.define("d2l",                 IParm.N, "D:L",       Flow.NEXT, false);
            f.define("d2f",                 IParm.N, "D:F",       Flow.NEXT, false);

            f.define("aload_f",             IParm.N, "OI:F",      Flow.CALL, false);
            f.define("aload_d",             IParm.N, "OI:D",      Flow.CALL, false);
            f.define("astore_f",            IParm.N, "OIF:",      Flow.CALL, false);
            f.define("astore_d",            IParm.N, "OID:",      Flow.CALL, false);

            floatInstructions = f.getDefinitions();
        }
        return floatInstructions;
    }

    public static List<Instruction> getAllInstructions() {
        if (allInstructions == null) {
            List<Instruction> list = new ArrayList<Instruction>(500);
            list.addAll(getInstructions());
            list.addAll(getFloatInstructions());
            allInstructions = list;
        }
        return allInstructions;
    }

    /**
     * Enumerated type for immediate parameters to an instruction.
     */
    public static final class IParm {

        /**
         * Size of the parameter in bytes.
         */
        public final int size;

        private IParm(int size) {
            this.size = size;
        }

        /** Constant denoting no immediate parameter. */
        public static final IParm N = new IParm(0);

        /** Constant denoting an unsigned byte parameter. */
        public static final IParm A = new IParm(1);

        /** Constant denoting a signed byte parameter. */
        public static final IParm B = new IParm(1);

        /** Constant denoting an unsigned short parameter. */
        public static final IParm C = new IParm(2);

        /** Constant denoting a signed short parameter. */
        public static final IParm S = new IParm(2);

        /** Constant denoting a signed int parameter. */
        public static final IParm I = new IParm(4);

        /** Constant denoting a signed long parameter. */
        public static final IParm L = new IParm(8);

        /** Constant denoting a float parameter. */
        public static final IParm F = new IParm(4);

        /** Constant denoting a double parameter. */
        public static final IParm D = new IParm(8);

        /** Constant denoting an instruction widened to a 2-byte parameter. */
        public static final IParm W2 = new IParm(3);

        /** Constant denoting an instruction widened to a 4-byte parameter. */
        public static final IParm W4 = new IParm(5);

        /** Constant denoting a variable length table parameter. */
        public static final IParm T = new IParm(-1);

        /** Constant denoting a prefix instruction. */
        public static final IParm P = new IParm(-1);
    }

    /**
     * Enumerated type for control flow effect of an instruction.
     */
    public static final class Flow {

        private static int nextValue;
        public final int value;
        public final String name;

        private Flow(String name) {
            value = nextValue++;
            this.name = name;
        }

        /** Constant denoting sequential control flow. */
        public static final Flow NEXT = new Flow("NEXT");

        /** Constant denoting (potentially conditional) branching control flow. */
        public static final Flow CHANGE = new Flow("CHANGE");

        /** Constant denoting a call to another method. */
        public static final Flow CALL = new Flow("CALL");
    }


    private Instruction(int opcode, String mnemonic, IParm iparm, Instruction compact,
                        Instruction wide, String operandStackEffect, Flow flow)
    {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.iparm = iparm;
        this.flow = flow;
        this.compact = compact;
        this.wide = wide;
        this.operandStackEffect = operandStackEffect;
    }


    /**
     * The factory class used to create the non-floating point instructions.
     */
    static class Factory {

        Factory(int nextOpcode) {
            this.nextOpcode = nextOpcode;
        }

        private int nextOpcode;

        private final List<Instruction> defs = new ArrayList<Instruction>();
        private final List<Instruction> wides = new ArrayList<Instruction>();

        /**
         * Defines a range of instructions that have an implicit parameter in their opcode
         * where the value of the implicit parameter is the position of the instruction
         * within the range.
         *
         * @param count   the size of the range
         * @param prefix  the prefix of each instruction's mnemonic
         * @param iparm   the type of the instruction's immediate parameter
         * @param operandStackEffect   the effect the instruction has on the operand stack
         * @param flow    the effect the instruction has on the control flow
         */
        void define(int count, String prefix, IParm iparm, String operandStackEffect, Flow flow) {
            for (int i = 0; i != count; ++i) {
                define(prefix + '_' + i, iparm, operandStackEffect, flow, false);
            }
        }

        /**
         * Defines an instruction and adds it to the list of instruction in this factory.
         *
         * @param mnemonic  the instruction's mnemonic
         * @param iparm     the type of the instruction's immediate parameter
         * @param operandStackEffect   the effect the instruction has on the operand stack
         * @param flow      the effect the instruction has on the control flow
         * @param wide      true if the instruction has a wide version
         */
        void define(String mnemonic, IParm iparm, String operandStackEffect, Flow flow, boolean wide) {
            int opcode = nextOpcode++;
            Instruction instruction = new Instruction(opcode, mnemonic, iparm, null, null, operandStackEffect, flow);
            defs.add(instruction);

            if (wide) {
                wides.add(instruction);
            }
        }

        /**
         * Gets the list of instructions that have been defined.
         *
         * @return  the list of instructions that have been defined
         */
        List<Instruction> getDefinitions() {
            for (Instruction compact: wides) {
                compact.widen(nextOpcode++, defs);

            }
            wides.clear();
            return Collections.unmodifiableList(defs);
        }
    }

}