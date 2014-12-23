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

package com.sun.squawk.translator.ir;

import java.util.*;

import com.sun.squawk.translator.*;
import com.sun.squawk.translator.ci.*;
import com.sun.squawk.translator.ci.CodeParser.*;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.vm.OPC;
import com.sun.squawk.vm.CID;
import com.sun.squawk.*;


/**
 * This class implements a parser for the bytecode of a Java bytecode
 * method. The parser produces a sequence of intermediate representation
 * nodes (instances of {@link Instruction}) encapsulated in an {@link IR}
 * instance.<p>
 *
 * During parsing, the graph builder also performs the task of bytecode
 * verification. It also performs a number of JVM to Squawk transformations
 * while building the IR. These transformations are:<p>
 *
 *
 */
public final class IRBuilder {

    /**
     * The throwable thrown to indicate that 'this' was assigned to in
     * a constructor.
     */
    final class OverwroteThisInConstructorError extends NoClassDefFoundError {
        OverwroteThisInConstructorError() {
        }
    }

    /**
     * The method being processed.
     */
    private final Method method;

    /**
     * The parser used to decode the bytecode stream. This also contains the
     * code related attributes found in the "Code" class file attribute.
     */
    private CodeParser codeParser;

    /**
     * The IR being built by this builder.
     */
    private IR ir;

    /**
     * The execution frame modelling usage of the operand stack and local
     * variables.
     */
    private Frame frame;

    /**
     * The stack of active exception handlers.
     */
    private Stack handlers;

    /**
     * The load instruction used to make a copy of 'this' in a constructor.
     */
    private LoadLocal copyOfThis;

    /**
     * Creates a <code>IRBuilder</code> instance.
     *
     * @param  translator  the translation context
     * @param  codeParser  the parser used to parse the contents of the "Code"
     *                     attribute of the method for which this builder
     *                     will build an IR
     */
    public IRBuilder(Translator translator, CodeParser codeParser) {
        this.codeParser = codeParser;
        this.method = codeParser.getMethod();
        try {
            try {
                this.frame = new Frame(codeParser, 0);
                this.ir = new IR();
                parseInstructions(translator, false);
            } catch (OverwroteThisInConstructorError e) {
                this.codeParser = codeParser.reset(translator);
                this.frame = new Frame(codeParser, 1);
                this.ir = new IR();
                parseInstructions(translator, true);
            }
        } catch (RuntimeException e) {
            System.err.println(codeParser.prefix(""));
            throw e;
        }
    }

    /**
     * Return the built IR.
     * @return the IR for this code
     */
    public IR getIR() {
        return ir;
    }

    /**
     * Gets the frame used to build the IR.
     *
     * @return the frame built by this builder
     */
    public Frame getFrame() {
        return frame;
    }

    /**
     * Read all the instructions for the method and build the IR graph.
     *
     * @param translator  the translation context
     * @param copyThis    makes a copy of 'this'
     */
    private void parseInstructions(Translator translator, boolean copyThis) {
        final boolean trace = Translator.TRACING_ENABLED && Tracer.isTracing("jvmverifier", method.toString());

        /*
         * Verification tracing.
         */
        if (trace) {
            Tracer.traceln("");
            Tracer.traceln("++++ Verifying " + method + "++++");
        }

        /*
         * Save a copy of 'this' if this is a constructor and 'this' is
         * assigned to
         */
        if (copyThis && method.isConstructor()) {
            this.copyOfThis = copyThis();
        }

        /*
         * Flags if there is direct control flow to the current instruction
         * from its lexical predecessor.
         */
        boolean flowFromPredecessor = true;

        /*
         * If this is a synchronized method then execute a monitorenter
         * and start a 'try' block
         */
        Target finallyTarget = null;
        if (method.isSynchronized()) {
            finallyTarget = frame.createFinallyTargetForSynchronizedMethod();

            /*
             * Execute the monitorenter
             */
            enterSynchronizedMethod();

            /*
             * Open the try block
             */
            opc_try(finallyTarget);
        }

        /*
         * Read through all the bytecodes
         */
        do {
            /*
             * Get the next opcode
             */
            int opcode = codeParser.parseOpcode();

            /*
             * Parse the pseudo opcodes
             */
            PseudoOpcode[] pseudoOpcodes = codeParser.getLastPseudoOpcodes();
            if (pseudoOpcodes != null) {
                processPseudoInstructions(pseudoOpcodes, flowFromPredecessor);
            }

            /*
             * If it is not possible to flow into this instruction then there
             * must be a stackmap entry for this ip address.
             */
            if (!flowFromPredecessor) {
                codeParser.getTarget(codeParser.getLastOpcodeAddress());
            }

            /*
             * Verify that the current frame state is compatible with the
             * entry point of any active exception handlers
             */
            verifyActiveExceptionHandlers();

            /*
             * Assume that this instruction will flow into the next one
             */
            boolean fallsThrough = true;

            /*
             * Verification tracing.
             */
            if (trace) {
                frame.traceFrameState(opcode,codeParser.getLastOpcodeAddress());
            }

            /*
             * Dispatch to the specific bytecode
             */
            switch (opcode) {
                case Opcode.opc_nop:             break;
                case Opcode.opc_aconst_null:     opc_constant(new ConstantObject(Klass.NULL, null)); break;
                case Opcode.opc_iconst_m1:       opc_constant(new ConstantInt(   new Integer(-1)));  break;
                case Opcode.opc_iconst_0:        opc_constant(new ConstantInt(   new Integer(0)));   break;
                case Opcode.opc_iconst_1:        opc_constant(new ConstantInt(   new Integer(1)));   break;
                case Opcode.opc_iconst_2:        opc_constant(new ConstantInt(   new Integer(2)));   break;
                case Opcode.opc_iconst_3:        opc_constant(new ConstantInt(   new Integer(3)));   break;
                case Opcode.opc_iconst_4:        opc_constant(new ConstantInt(   new Integer(4)));   break;
                case Opcode.opc_iconst_5:        opc_constant(new ConstantInt(   new Integer(5)));   break;
                case Opcode.opc_lconst_0:        opc_constant(new ConstantLong(  new Long(0L)));     break;
                case Opcode.opc_lconst_1:        opc_constant(new ConstantLong(  new Long(1L)));     break;
/*if[FLOATS]*/
                case Opcode.opc_fconst_0:        opc_constant(new ConstantFloat( new Float(0F)));    break;
                case Opcode.opc_fconst_1:        opc_constant(new ConstantFloat( new Float(1F)));    break;
                case Opcode.opc_fconst_2:        opc_constant(new ConstantFloat( new Float(2F)));    break;
                case Opcode.opc_dconst_0:        opc_constant(new ConstantDouble(new Double(0D)));   break;
                case Opcode.opc_dconst_1:        opc_constant(new ConstantDouble(new Double(1D)));   break;
/*end[FLOATS]*/
                case Opcode.opc_bipush:          opc_constant(new ConstantInt(   new Integer(codeParser.parseByteOperand())));         break;
                case Opcode.opc_sipush:          opc_constant(new ConstantInt(   new Integer(codeParser.parseShortOperand())));        break;
                case Opcode.opc_ldc:             opc_constant(Constant.create(   codeParser.parseConstantPoolOperand( false, false))); break;
                case Opcode.opc_ldc_w:           opc_constant(Constant.create(   codeParser.parseConstantPoolOperand( true,  false))); break;
                case Opcode.opc_ldc2_w:          opc_constant(Constant.create(   codeParser.parseConstantPoolOperand( true,  true)));  break;
                case Opcode.opc_iload:           opc_load(Klass.INT,             codeParser.parseLocalVariableOperand(false, false));  break;
                case Opcode.opc_lload:           opc_load(Klass.LONG,            codeParser.parseLocalVariableOperand(false, true));   break;
/*if[FLOATS]*/
                case Opcode.opc_fload:           opc_load(Klass.FLOAT,           codeParser.parseLocalVariableOperand(false, false));  break;
                case Opcode.opc_dload:           opc_load(Klass.DOUBLE,          codeParser.parseLocalVariableOperand(false, true));   break;
/*end[FLOATS]*/
                case Opcode.opc_aload:           opc_load(Klass.REFERENCE,       codeParser.parseLocalVariableOperand(false, false));  break;
                case Opcode.opc_iload_0:         opc_load(Klass.INT,       0); break;
                case Opcode.opc_iload_1:         opc_load(Klass.INT,       1); break;
                case Opcode.opc_iload_2:         opc_load(Klass.INT,       2); break;
                case Opcode.opc_iload_3:         opc_load(Klass.INT,       3); break;
                case Opcode.opc_lload_0:         opc_load(Klass.LONG,      0); break;
                case Opcode.opc_lload_1:         opc_load(Klass.LONG,      1); break;
                case Opcode.opc_lload_2:         opc_load(Klass.LONG,      2); break;
                case Opcode.opc_lload_3:         opc_load(Klass.LONG,      3); break;
/*if[FLOATS]*/
                case Opcode.opc_fload_0:         opc_load(Klass.FLOAT,     0); break;
                case Opcode.opc_fload_1:         opc_load(Klass.FLOAT,     1); break;
                case Opcode.opc_fload_2:         opc_load(Klass.FLOAT,     2); break;
                case Opcode.opc_fload_3:         opc_load(Klass.FLOAT,     3); break;
                case Opcode.opc_dload_0:         opc_load(Klass.DOUBLE,    0); break;
                case Opcode.opc_dload_1:         opc_load(Klass.DOUBLE,    1); break;
                case Opcode.opc_dload_2:         opc_load(Klass.DOUBLE,    2); break;
                case Opcode.opc_dload_3:         opc_load(Klass.DOUBLE,    3); break;
/*end[FLOATS]*/
                case Opcode.opc_aload_0:         opc_load(Klass.REFERENCE, 0); break;
                case Opcode.opc_aload_1:         opc_load(Klass.REFERENCE, 1); break;
                case Opcode.opc_aload_2:         opc_load(Klass.REFERENCE, 2); break;
                case Opcode.opc_aload_3:         opc_load(Klass.REFERENCE, 3); break;
                case Opcode.opc_iaload:          opc_arrayload(Klass.INT_ARRAY);    break;
                case Opcode.opc_laload:          opc_arrayload(Klass.LONG_ARRAY);   break;
/*if[FLOATS]*/
                case Opcode.opc_faload:          opc_arrayload(Klass.FLOAT_ARRAY);  break;
                case Opcode.opc_daload:          opc_arrayload(Klass.DOUBLE_ARRAY); break;
/*end[FLOATS]*/
                case Opcode.opc_aaload:          opc_arrayload(Klass.OBJECT_ARRAY); break;
                case Opcode.opc_baload:          opc_arrayload(Klass.BYTE_ARRAY);   break;
                case Opcode.opc_caload:          opc_arrayload(Klass.CHAR_ARRAY);   break;
                case Opcode.opc_saload:          opc_arrayload(Klass.SHORT_ARRAY);  break;
                case Opcode.opc_istore:          opc_store(Klass.INT,       codeParser.parseLocalVariableOperand(false, false)); break;
                case Opcode.opc_lstore:          opc_store(Klass.LONG,      codeParser.parseLocalVariableOperand(false, true));  break;
/*if[FLOATS]*/
                case Opcode.opc_fstore:          opc_store(Klass.FLOAT,     codeParser.parseLocalVariableOperand(false, false)); break;
                case Opcode.opc_dstore:          opc_store(Klass.DOUBLE,    codeParser.parseLocalVariableOperand(false, true));  break;
/*end[FLOATS]*/
                case Opcode.opc_astore:          opc_store(Klass.REFERENCE, codeParser.parseLocalVariableOperand(false, false)); break;
                case Opcode.opc_istore_0:        opc_store(Klass.INT,       0);      break;
                case Opcode.opc_istore_1:        opc_store(Klass.INT,       1);      break;
                case Opcode.opc_istore_2:        opc_store(Klass.INT,       2);      break;
                case Opcode.opc_istore_3:        opc_store(Klass.INT,       3);      break;
                case Opcode.opc_lstore_0:        opc_store(Klass.LONG,      0);      break;
                case Opcode.opc_lstore_1:        opc_store(Klass.LONG,      1);      break;
                case Opcode.opc_lstore_2:        opc_store(Klass.LONG,      2);      break;
                case Opcode.opc_lstore_3:        opc_store(Klass.LONG,      3);      break;
/*if[FLOATS]*/
                case Opcode.opc_fstore_0:        opc_store(Klass.FLOAT,     0);      break;
                case Opcode.opc_fstore_1:        opc_store(Klass.FLOAT,     1);      break;
                case Opcode.opc_fstore_2:        opc_store(Klass.FLOAT,     2);      break;
                case Opcode.opc_fstore_3:        opc_store(Klass.FLOAT,     3);      break;
                case Opcode.opc_dstore_0:        opc_store(Klass.DOUBLE,    0);      break;
                case Opcode.opc_dstore_1:        opc_store(Klass.DOUBLE,    1);      break;
                case Opcode.opc_dstore_2:        opc_store(Klass.DOUBLE,    2);      break;
                case Opcode.opc_dstore_3:        opc_store(Klass.DOUBLE,    3);      break;
/*end[FLOATS]*/
                case Opcode.opc_astore_0:        opc_store(Klass.REFERENCE, 0);      break;
                case Opcode.opc_astore_1:        opc_store(Klass.REFERENCE, 1);      break;
                case Opcode.opc_astore_2:        opc_store(Klass.REFERENCE, 2);      break;
                case Opcode.opc_astore_3:        opc_store(Klass.REFERENCE, 3);      break;
                case Opcode.opc_iastore:         opc_arraystore(Klass.INT_ARRAY);    break;
                case Opcode.opc_lastore:         opc_arraystore(Klass.LONG_ARRAY);   break;
/*if[FLOATS]*/
                case Opcode.opc_fastore:         opc_arraystore(Klass.FLOAT_ARRAY);  break;
                case Opcode.opc_dastore:         opc_arraystore(Klass.DOUBLE_ARRAY); break;
/*end[FLOATS]*/
                case Opcode.opc_aastore:         opc_arraystore(Klass.OBJECT_ARRAY); break;
                case Opcode.opc_bastore:         opc_arraystore(Klass.BYTE_ARRAY);   break;
                case Opcode.opc_castore:         opc_arraystore(Klass.CHAR_ARRAY);   break;
                case Opcode.opc_sastore:         opc_arraystore(Klass.SHORT_ARRAY);  break;
                case Opcode.opc_pop:             opc_pop(false);                     break;
                case Opcode.opc_pop2:            opc_pop(true);                      break;
                case Opcode.opc_dup:
                case Opcode.opc_dup_x1:
                case Opcode.opc_dup_x2:
                case Opcode.opc_dup2:
                case Opcode.opc_dup2_x1:
                case Opcode.opc_dup2_x2:
                case Opcode.opc_swap:            opc_stackop(opcode); break;
                case Opcode.opc_iadd:            opc_arithmetic(Klass.INT,    OPC.ADD_I);   break;
                case Opcode.opc_ladd:            opc_arithmetic(Klass.LONG,   OPC.ADD_L);   break;
                case Opcode.opc_isub:            opc_arithmetic(Klass.INT,    OPC.SUB_I);   break;
                case Opcode.opc_lsub:            opc_arithmetic(Klass.LONG,   OPC.SUB_L);   break;
                case Opcode.opc_imul:            opc_arithmetic(Klass.INT,    OPC.MUL_I);   break;
                case Opcode.opc_lmul:            opc_arithmetic(Klass.LONG,   OPC.MUL_L);   break;
                case Opcode.opc_idiv:            opc_arithmetic(Klass.INT,    OPC.DIV_I);   break;
                case Opcode.opc_ldiv:            opc_arithmetic(Klass.LONG,   OPC.DIV_L);   break;
                case Opcode.opc_irem:            opc_arithmetic(Klass.INT,    OPC.REM_I);   break;
                case Opcode.opc_lrem:            opc_arithmetic(Klass.LONG,   OPC.REM_L);   break;
                case Opcode.opc_ineg:            opc_negation(  Klass.INT,    OPC.NEG_I);   break;
                case Opcode.opc_lneg:            opc_negation(  Klass.LONG,   OPC.NEG_L);   break;
                case Opcode.opc_ishl:            opc_shift(     Klass.INT,    OPC.SHL_I);   break;
                case Opcode.opc_lshl:            opc_shift(     Klass.LONG,   OPC.SHL_L);   break;
                case Opcode.opc_ishr:            opc_shift(     Klass.INT,    OPC.SHR_I);   break;
                case Opcode.opc_lshr:            opc_shift(     Klass.LONG,   OPC.SHR_L);   break;
                case Opcode.opc_iushr:           opc_shift(     Klass.INT,    OPC.USHR_I);  break;
                case Opcode.opc_lushr:           opc_shift(     Klass.LONG,   OPC.USHR_L);  break;
                case Opcode.opc_iand:            opc_arithmetic(Klass.INT,    OPC.AND_I);   break;
                case Opcode.opc_land:            opc_arithmetic(Klass.LONG,   OPC.AND_L);   break;
                case Opcode.opc_ior:             opc_arithmetic(Klass.INT,    OPC.OR_I);    break;
                case Opcode.opc_lor:             opc_arithmetic(Klass.LONG,   OPC.OR_L);    break;
                case Opcode.opc_ixor:            opc_arithmetic(Klass.INT,    OPC.XOR_I);   break;
                case Opcode.opc_lxor:            opc_arithmetic(Klass.LONG,   OPC.XOR_L);   break;
                case Opcode.opc_iinc:            opc_iinc(codeParser.parseLocalVariableOperand(false, false), codeParser.parseByteOperand()); break;
                case Opcode.opc_i2l:             opc_conversion(Klass.INT,    Klass.LONG,   OPC.I2L); break;
                case Opcode.opc_l2i:             opc_conversion(Klass.LONG,   Klass.INT,    OPC.L2I); break;
                case Opcode.opc_i2b:             opc_conversion(Klass.INT,    Klass.BYTE,   OPC.I2B); break;
                case Opcode.opc_i2c:             opc_conversion(Klass.INT,    Klass.CHAR,   OPC.I2C); break;
                case Opcode.opc_i2s:             opc_conversion(Klass.INT,    Klass.SHORT,  OPC.I2S); break;
                case Opcode.opc_lcmp:            opc_comparison(Klass.LONG,   ComparisonOp.LCMP);  break;
                case Opcode.opc_ifeq:            opc_if(Klass.INT, OPC.IF_EQ_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_ifne:            opc_if(Klass.INT, OPC.IF_NE_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_iflt:            opc_if(Klass.INT, OPC.IF_LT_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_ifge:            opc_if(Klass.INT, OPC.IF_GE_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_ifgt:            opc_if(Klass.INT, OPC.IF_GT_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_ifle:            opc_if(Klass.INT, OPC.IF_LE_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_icmpeq:       opc_ifcompare(Klass.INT, OPC.IF_CMPEQ_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_icmpne:       opc_ifcompare(Klass.INT, OPC.IF_CMPNE_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_icmplt:       opc_ifcompare(Klass.INT, OPC.IF_CMPLT_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_icmpge:       opc_ifcompare(Klass.INT, OPC.IF_CMPGE_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_icmpgt:       opc_ifcompare(Klass.INT, OPC.IF_CMPGT_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_icmple:       opc_ifcompare(Klass.INT, OPC.IF_CMPLE_I, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_acmpeq:       opc_ifcompare(Klass.REFERENCE, OPC.IF_CMPEQ_O, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_if_acmpne:       opc_ifcompare(Klass.REFERENCE, OPC.IF_CMPNE_O, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_goto:            opc_goto(codeParser.parseBranchOperand(false)); fallsThrough = false; break;
                case Opcode.opc_jsr:             throw codeParser.verifyError("'jsr' is unsupported");
                case Opcode.opc_ret:             throw codeParser.verifyError("'ret' is unsupported");
                case Opcode.opc_tableswitch:     opc_tableswitch();        fallsThrough = false; break;
                case Opcode.opc_lookupswitch:    opc_lookupswitch();       fallsThrough = false; break;
                case Opcode.opc_ireturn:         opc_return(Klass.INT);    fallsThrough = false; break;
                case Opcode.opc_lreturn:         opc_return(Klass.LONG);   fallsThrough = false; break;
/*if[FLOATS]*/
                case Opcode.opc_freturn:         opc_return(Klass.FLOAT);  fallsThrough = false; break;
                case Opcode.opc_dreturn:         opc_return(Klass.DOUBLE); fallsThrough = false; break;
/*end[FLOATS]*/
                case Opcode.opc_areturn:         opc_return(Klass.REFERENCE); fallsThrough = false; break;
                case Opcode.opc_return:          opc_return(Klass.VOID);   fallsThrough = false; break;
                case Opcode.opc_getstatic:       opc_getstatic(codeParser.parseFieldOperand(true,  opcode)); break;
                case Opcode.opc_putstatic:       opc_putstatic(codeParser.parseFieldOperand(true,  opcode)); break;
                case Opcode.opc_getfield:        opc_getfield(codeParser.parseFieldOperand( false, opcode)); break;
                case Opcode.opc_putfield:        opc_putfield(codeParser.parseFieldOperand( false, opcode)); break;
                case Opcode.opc_invokevirtual: {
                    Method callee = codeParser.parseMethodOperand(false, false);
                    if (callee.getDefiningClass().isInterface()) {
                        opc_invokeinterface(callee, Opcode.opc_invokevirtual);
                    } else {
                        opc_invokevirtual(callee);
                    }
                    break;
                }
                case Opcode.opc_invokespecial:   opc_invokespecial(codeParser.parseMethodOperand(false, false));  break;
                case Opcode.opc_invokestatic:    opc_invokestatic(codeParser.parseMethodOperand(true, false));    break;
                case Opcode.opc_invokeinterface: {
                    Method callee = codeParser.parseMethodOperand(false, true);
                    if (callee.getDefiningClass() != Klass.OBJECT) {
                        opc_invokeinterface(callee, Opcode.opc_invokeinterface);
                    } else {
                        opc_invokevirtual(callee);
                    }
                    break;
                }
                case Opcode.opc_new:             opc_new(codeParser.parseNewOperand()); break;
                case Opcode.opc_newarray:        opc_newarray(codeParser.parseNewArrayOperand()); break;
                case Opcode.opc_anewarray:       opc_newarray(Klass.getClass("["+codeParser.parseClassOperand(). getInternalName(), false)); break;
                case Opcode.opc_arraylength:     opc_arraylength(); break;
                case Opcode.opc_athrow:          opc_throw(); fallsThrough = false; break;
                case Opcode.opc_checkcast:       opc_checkcast(codeParser.parseClassOperand());  break;
                case Opcode.opc_instanceof:      opc_instanceof(codeParser.parseClassOperand()); break;
                case Opcode.opc_monitorenter:    opc_monitorenter(false); break;
                case Opcode.opc_monitorexit:     opc_monitorexit(false);  break;
                case Opcode.opc_wide:            opc_wide(codeParser.parseUnsignedByteOperand()); break;
                case Opcode.opc_multianewarray:  opc_multianewarray(codeParser.parseClassOperand(), codeParser.parseUnsignedByteOperand()); break;
                case Opcode.opc_ifnull:          opc_if(Klass.REFERENCE, OPC.IF_EQ_O, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_ifnonnull:       opc_if(Klass.REFERENCE, OPC.IF_NE_O, codeParser.parseBranchOperand(false)); break;
                case Opcode.opc_goto_w:          opc_goto(codeParser.parseBranchOperand(true)); fallsThrough = false; break;
                case Opcode.opc_jsr_w:           throw codeParser.verifyError("'jsr_w' is unsupported");
                case Opcode.opc_breakpoint:      throw codeParser.verifyError("'breakpoint' is unsupported");
/*if[FLOATS]*/
                case Opcode.opc_fadd:            opc_arithmetic(Klass.FLOAT,  OPC.ADD_F);             break;
                case Opcode.opc_dadd:            opc_arithmetic(Klass.DOUBLE, OPC.ADD_D);             break;
                case Opcode.opc_fsub:            opc_arithmetic(Klass.FLOAT,  OPC.SUB_F);             break;
                case Opcode.opc_dsub:            opc_arithmetic(Klass.DOUBLE, OPC.SUB_D);             break;
                case Opcode.opc_fmul:            opc_arithmetic(Klass.FLOAT,  OPC.MUL_F);             break;
                case Opcode.opc_dmul:            opc_arithmetic(Klass.DOUBLE, OPC.MUL_D);             break;
                case Opcode.opc_fdiv:            opc_arithmetic(Klass.FLOAT,  OPC.DIV_F);             break;
                case Opcode.opc_ddiv:            opc_arithmetic(Klass.DOUBLE, OPC.DIV_D);             break;
                case Opcode.opc_frem:            opc_arithmetic(Klass.FLOAT,  OPC.REM_F);             break;
                case Opcode.opc_drem:            opc_arithmetic(Klass.DOUBLE, OPC.REM_D);             break;
                case Opcode.opc_fneg:            opc_negation(  Klass.FLOAT,  OPC.NEG_F);             break;
                case Opcode.opc_dneg:            opc_negation(  Klass.DOUBLE, OPC.NEG_D);             break;
                case Opcode.opc_i2f:             opc_conversion(Klass.INT,    Klass.FLOAT,  OPC.I2F); break;
                case Opcode.opc_i2d:             opc_conversion(Klass.INT,    Klass.DOUBLE, OPC.I2D); break;
                case Opcode.opc_l2f:             opc_conversion(Klass.LONG,   Klass.FLOAT,  OPC.L2F); break;
                case Opcode.opc_l2d:             opc_conversion(Klass.LONG,   Klass.DOUBLE, OPC.L2D); break;
                case Opcode.opc_f2i:             opc_conversion(Klass.FLOAT,  Klass.INT,    OPC.F2I); break;
                case Opcode.opc_f2l:             opc_conversion(Klass.FLOAT,  Klass.LONG,   OPC.F2L); break;
                case Opcode.opc_f2d:             opc_conversion(Klass.FLOAT,  Klass.DOUBLE, OPC.F2D); break;
                case Opcode.opc_d2i:             opc_conversion(Klass.DOUBLE, Klass.INT,    OPC.D2I); break;
                case Opcode.opc_d2l:             opc_conversion(Klass.DOUBLE, Klass.LONG,   OPC.D2L); break;
                case Opcode.opc_d2f:             opc_conversion(Klass.DOUBLE, Klass.FLOAT,  OPC.D2F); break;
                case Opcode.opc_fcmpl:           opc_comparison(Klass.FLOAT,  OPC.FCMPL);      break;
                case Opcode.opc_fcmpg:           opc_comparison(Klass.FLOAT,  OPC.FCMPG);      break;
                case Opcode.opc_dcmpl:           opc_comparison(Klass.DOUBLE, OPC.DCMPL);      break;
                case Opcode.opc_dcmpg:           opc_comparison(Klass.DOUBLE, OPC.DCMPG);      break;
/*end[FLOATS]*/
                default:                         throw codeParser.verifyError("invalid opcode "+opcode);
            }
            flowFromPredecessor = fallsThrough;
        } while (!codeParser.atEof());

        /*
         * Ensure that control flow doesn't fall off the method
         */
        if (flowFromPredecessor) {
            throw codeParser.verifyError("execution falls off end of code");
        }

        /*
         * Ensure that no branch targets or exception entry targets pointing
         * into the middle of an instruction
         */
        int targetCount = codeParser.getTargetCount() + (finallyTarget == null ? 0 : 1);
        if (targetCount != 0) {
            Target[] targets = new Target[targetCount];
            Enumeration e = codeParser.getTargets();
            int index = 0;
            if (e != null) {
                while (e.hasMoreElements()) {
                    Target target = (Target)e.nextElement();
                    if (target.getTargetedInstruction() == null) {
                        throw codeParser.verifyError("branch into middle of instruction");
                    }
                    targets[index++] = target;
                }
            }
            if (finallyTarget != null) {
                targets[index++] = finallyTarget;
            }
            ir.setTargets(targets);

            codeParser.verifyUninitializedObjectClassAddresses();
        }

        /*
         * There may be an exception handler whose active code range may include
         * the last instruction
         */
        PseudoOpcode[] pseudoOpcodes = codeParser.getLastPseudoOpcodes();
        if (pseudoOpcodes != null) {
            processPseudoInstructions(pseudoOpcodes, flowFromPredecessor);
        }

        /*
         * If this is a synchronized method then place the handler here that will
         * release the monitor if an exception is thrown
         */
        if (method.isSynchronized()) {
            Assert.that(finallyTarget != null);
            opc_tryend(finallyTarget);
            frame.growMaxStack(1);
            opc_catch(finallyTarget);
            exitSynchronizedMethod();
            opc_throw();
            frame.resetMaxStack();
        }

        codeParser.verifyExceptionHandlerInstructionAddresses();

        /*
         * Verification tracing.
         */
        if (trace) {
            Tracer.traceln("");
            Tracer.traceln("---- Verifying " + method + "----");
        }
    }

    /**
     * Processes one or more pseudo opcodes.
     *
     * @param pseudoOpcodes  the pseudo opcodes to process
     * @param flowFromPredecessor  specifies if there is implicit control flow from the preceeding instruction
     */
    private void processPseudoInstructions(PseudoOpcode[] pseudoOpcodes, boolean flowFromPredecessor) {
        Assert.that(pseudoOpcodes != null && pseudoOpcodes.length != 0);
        final boolean trace = Translator.TRACING_ENABLED && Tracer.isTracing("jvmverifier", method.toString());
        
        for (int i = 0 ; i < pseudoOpcodes.length ; i++) {
            PseudoOpcode pseudoOpcode = pseudoOpcodes[i];

            /*
             * Tracing.
             */
            if (trace) {
                Tracer.traceln("Psuedo instruction @"+ codeParser.getLastOpcodeAddress()+" "+pseudoOpcode);
            }

            /*
             * Process.
             */
            Object context = pseudoOpcode.getContext();
            switch (pseudoOpcode.getTag()) {
                case PseudoOpcode.TRYEND: {
                    ExceptionHandler ehItem = (ExceptionHandler)context;
                    opc_tryend(codeParser.getTarget(ehItem.getHandler()));
                    break;
                }
                case PseudoOpcode.TRY: {
                    ExceptionHandler ehItem = (ExceptionHandler)context;
                    opc_try(codeParser.getTarget(ehItem.getHandler()));
                    break;
                }
                case PseudoOpcode.CATCH: {
                    opc_catch((Target)context);
                    break;
                }
                case PseudoOpcode.TARGET: {
                    opc_target((Target)context, flowFromPredecessor);
                    break;
                }
                case PseudoOpcode.POSITION: {
                    opc_position((Position)context);
                    break;
                }
                default: {
                    Assert.shouldNotReachHere();
                }
            }
        }
    }

    /**
     * Verifies that the current frame state is compatible with the
     * entry point of any active exception handlers
     */
    private void verifyActiveExceptionHandlers() {
        if (handlers != null) {
            Enumeration e = handlers.elements();
            while (e.hasMoreElements()) {
                IRExceptionHandler handler = (IRExceptionHandler)e.nextElement();
                Target target = handler.getTarget();
                frame.mergeLocals(target, false);
            }
        }
    }

    /**
     * Processes an instruction that delimits a basic block.
     *
     * @param delimiter     the instruction delimiting a basic block
     * @param defaultTarget the first target (if any) of <code>delimiter</code>
     * @param targets       the extra targets (if any) of <code>delimiter</code>
     */
    private void processBasicBlockDelimiter(Instruction delimiter, Target defaultTarget, Target[] targets) {
        if (defaultTarget != null) {
            processBasicBlockDelimiterTarget(defaultTarget);
        }
        if (targets != null) {
            for (int i = 0 ; i < targets.length ; i++) {
                Target target = targets[i];
                processBasicBlockDelimiterTarget(target);
            }
        }
    }

    /**
     * Processes the target of a basic block delimiting instruction. This
     * means verifying the stack map at the target against the current frame
     * state. In addition, if the target corresponds to a backward branch,
     * then any items on the stack are marked for spilling and the targeted
     * instruction is marked as a backward branch target.
     *
     * @param target  the branch target
     */
    private void processBasicBlockDelimiterTarget(Target target) {
        verifyTarget(target);
    }

    /**
     * Verifies that the current state of the frame is consistent with the
     * state expected at a target address according to the stack map at that
     * address.
     *
     * @param target  the target address
     */
    private void verifyTarget(Target target) {
        frame.mergeStack(target, false);
        frame.mergeLocals(target, false);
        if (target.getAddress() <= codeParser.getLastOpcodeAddress()) {
            if (frame.containsType(Klass.UNINITIALIZED)) {
                /* 
                 * There are a set of TCK tests, including javasoft.sqe.tests.vm.cldc.typechecker.check.check013.check01301m1.check01301m1_wrapper,
                 * that declare some locals and stack are UNINITIALIZED, then do an infinite loop (goto itself).
                 * Squawk is very unhappy to see either UNINITIALIZED in anywhere in the frame, or any values on the stack.
                 * WHAT TO DO?
                 * Note that the TCK tests never execute these byte codes - they aren't even reachable. Dead code elimination would (if implemented) actually delete the
                 * these problem byte codes. So tag the backward branch as "fatal", and in the emitter, warn that this code is likely to die if executed. Then implement
                 * dead code elimination, so emitter never sees the code.
                 */
                frame.traceTarget(target);
                target.markAsFatalTarget();
            }
            target.markAsBackwardBranchTarget();
        } else {
            target.markAsForwardBranchTarget();
        }
    }

    /**
     * Gets the object reference for a <i>putfield</i> or <i>getfield</i>
     * instruction.
     *
     * @param field       the referenced field
     * @param isPutfield  specifies if this is being called for <i>putfield</i>
     *                    which requires extra verification if used in a
     *                    constructor to allow assignment to fields of the
     *                    uninitialized object if the fields are declared in
     *                    the constructor's enclosing class
     * @return            the object reference
     */
    private StackProducer getObjectForInstanceField(Field field, boolean isPutfield) {

        /*
         * If the field is protected and in a different package than the
         * receiver must be compatible with the type of the method
         * being verified
         */
        Klass expectedType;
        if (field.isProtected() && !method.getDefiningClass(). isInSamePackageAs(field.getDefiningClass())) {
            expectedType = method.getDefiningClass(); // Receiver must be same kind as method being verified
        } else {
            expectedType = field.getDefiningClass();
        }

        /*
         * Since UNINITIALIZED_THIS is not assignable to any object type,
         * need to do some special tests when UNINITIALIZED_THIS is legal
         * as a receiver for a 'putfield' (i.e. when inside a constructor
         * and the field being written is declared in the current class).
         */
        if (isPutfield && method.isConstructor() && field.getDefiningClass() == method.getDefiningClass()) {
            StackProducer instruction = frame.pop(Klass.REFERENCE);
            if (instruction.getType() == Klass.UNINITIALIZED_THIS) {
                return instruction;
            }
            frame.push(instruction); // Just re-push the receiver to now perform the standard test
        }

        return frame.pop(expectedType);
    }

    /**
     * Makes a copy of 'this' in a constructor that subsequently overwrites it.
     *
     * @return  the load instruction used in making the copy
     */
    private LoadLocal copyThis() {
        int indexForSavedThis = codeParser.getMaxLocals();
        Local thisLocal = frame.allocateLocal(Klass.UNINITIALIZED_THIS, indexForSavedThis);

        /*
         * Insert code that copies 'this' into the special variable
         */
        LoadLocal load = new LoadLocal(Klass.UNINITIALIZED_THIS, frame.load(0, Klass.REFERENCE));
        append(load);

        StoreLocal store = new StoreLocal(thisLocal, frame.pop(Klass.REFERENCE));
        append(store);

        return load;
    }


    /*---------------------------------------------------------------------------*\
     *                      IR appending and stack pushing                       *
    \*---------------------------------------------------------------------------*/

    /**
     * Append a given instruction to the IR. If the instruction pushes a value
     * to the operand stack, then the push is also done here.
     *
     * @param instruction  the instruction to append
     */
    private void append(Instruction instruction) {
        if (instruction.constrainsStack()) {
            frame.spillStack();
        }

        instruction.setBytecodeOffset(codeParser.getLastOpcodeAddress());
        ir.append(instruction);
        if (instruction instanceof StackProducer) {
            StackProducer producer = (StackProducer)instruction;
            if (producer.getType() != Klass.VOID) {
                frame.push(producer);
            } else {
                Assert.that(producer instanceof Invoke);
            }
        }
    }


    /*---------------------------------------------------------------------------*\
     *                            Method monitors                                *
    \*---------------------------------------------------------------------------*/

    /**
     * Inserts code to obtain the monitor required for synchronizing the
     * method being processed.
     */
    private void enterSynchronizedMethod() {
        Assert.that(method.isSynchronized());
        if (!method.isStatic()) {
            frame.growMaxStack(1);
            opc_load(Klass.REFERENCE, 0);
            frame.resetMaxStack();
        }
        opc_monitorenter(method.isStatic());
    }

    /**
     * Inserts code to release the monitor required for synchronizing the
     * method being processed.
     */
    private void exitSynchronizedMethod() {
        Assert.that(method.isSynchronized());
        if (!method.isStatic()) {
            frame.growMaxStack(1);
            opc_load(Klass.REFERENCE, 0);
            frame.resetMaxStack();
        }
        opc_monitorexit(method.isStatic());
    }


    /*---------------------------------------------------------------------------*\
     *                       Method invocation parameters                        *
    \*---------------------------------------------------------------------------*/

    /**
     * Pops and type checks the parameters off the stack for a method invocation
     * and returns them in an array. The parameters popped include the
     * implicit <code>this</code> parameter for a non-static method but it
     * is not type checked - this is the caller's responsibility.
     *
     * @param   callee  the method being invoked
     * @return          the parameters of the invocation
     */
    private StackProducer[] popInvokeParameters(Method callee) {
        int extra = (!callee.isStatic() || callee.isConstructor()) ? 1 : 0;
        Klass[] parameterTypes = callee.getParameterTypes();
        int parameterCount = parameterTypes.length + extra;
        StackProducer[] parameters = new StackProducer[parameterCount];
        for (int i = parameterTypes.length - 1 ; i >= 0 ; --i) {
            parameters[--parameterCount] = frame.pop(parameterTypes[i]);
        }
        if (extra == 1) {
            parameters[0] = frame.pop(Klass.REFERENCE);
        }
        if (Translator.REVERSE_PARAMETERS) {
            spillParameters(parameters);
        }
        return parameters;
    }

     /**
      * Spill all the parameters in a list.
      *
      * @param parameters the parameters of the invocation
      */
     private void spillParameters(StackProducer[] parameters) {
         for (int i = 0 ; i < parameters.length ; i++) {
             frame.spill(parameters[i]);
         }
     }

     /**
      * Insert an instruction producing a slot number to the head of a parameter list.
      *
      * @param parameters the existing parameter producing instructions
      * @param slot       the instruction producing the slot
      * @return           the new parameter list
      */
     private StackProducer[] insertSlotParameter(StackProducer[] parameters, StackProducer slot) {
         StackProducer[] newParms = new StackProducer[parameters.length + 1];
         if (Translator.REVERSE_PARAMETERS) {
             newParms[0] = slot;
             System.arraycopy(parameters, 0, newParms, 1, parameters.length);
         } else {
             System.arraycopy(parameters, 0, newParms, 0, parameters.length);
             newParms[parameters.length] = slot;
         }
         return newParms;
     }

    /**
     * Make sure actualType is NOT a Squawk Primitive (Address, UWord, etc) being passed as a parameter of type Object.
     * 
     * @param callee
     * @param actualType
     * @param expectedType
     */
    private void verifyNonSquawkPrimitive(Method callee, Klass actualType, Klass expectedType) {
        if (actualType.isSquawkPrimitive() && (actualType != expectedType)) {
            Klass definingClass = callee.getDefiningClass();
            if (expectedType.getSystemID() == CID.OBJECT 
                    && (definingClass.getSystemID() == CID.NATIVEUNSAFE || definingClass.getInternalName().equals("com.sun.squawk.GC"))) {
                return; // NATIVEUNSAFE and GC methods are passed objects and addresses interchangably.
            }
            
            System.err.println("name: " + definingClass.getInternalName());
            String type = actualType.getName();
            throw codeParser.verifyError("In call to " + callee + ", " + type + " values can only be passsed as parameters of type " +
                    type + " not as type " + expectedType.getName());
        }
    }
    
     /**
      * Verifies that a Squawk Primitive "instance" is not being passed as an object to an unsuspecting method...
      * 
      * @param  callee         the non-static method being invoked
      * @param  parameters         the actual parameters to the method
      */
    private void verifyNonSquawkPrimitiveParameters(Method callee, StackProducer[] parameters) {
        Klass[] expectedTypes = callee.getParameterTypes();
        int extra = 0;
        if (!callee.isStatic() || callee.isConstructor()) {
            verifyNonSquawkPrimitive(callee, parameters[0].getType(), callee.getDefiningClass());
            extra = 1;
        }
        
        for (int i = 0; i < expectedTypes.length; i++) {
            Klass actualType = parameters[i + extra].getType();
            verifyNonSquawkPrimitive(callee,actualType, expectedTypes[i]);
        }
    }
             
     /**
      * Verifies that type of the parameter for <code>this</code> in a
      * non-static method is correct.
      *
      * @param  callee         the non-static method being invoked
      * @param  thisParameter  the parameter for <code>this</code>
      */
     private void verifyThisParameter(Method callee, StackProducer thisParameter) {
         Assert.that(!callee.isStatic() || callee.isConstructor());
         Klass expectedThisType;

         /*
          * If the method is protected and in a different package then
          * 'this' must be assignable to the class of the method
          * being verified.
          *
          * IGNORE FOR NOW: The final clause handles the case where the call is coming from a subclass, even though it isna't a call to the subclass's "this".
          * This case is illegal at the source code level, but appears a leagl reading of the JVM Spec, and the TCK tests this?
          *          if (callee.isProtected() && !method.getDefiningClass().isInSamePackageAs(callee.getDefiningClass()) && !callee.getDefiningClass().isAssignableFrom(method.getDefiningClass())) {
          */
         if (callee.isProtected() && !method.getDefiningClass().isInSamePackageAs(callee.getDefiningClass())) {
             expectedThisType = method.getDefiningClass();
         } else {
             expectedThisType = callee.getDefiningClass();
         }

         /*
          * Check that 'this' is of the correct type
          */
         if (expectedThisType.isInterface()) {
             // Interfaces are treated like java.lang.Object in the verifier according to the CLDC spec.
             expectedThisType = Klass.OBJECT;
         }
         if (!expectedThisType.isAssignableFrom(thisParameter.getType())) {
            throw codeParser.verifyError("invalid type for 'this' parameter in non-static method:"+
                                         " expected '"+expectedThisType+
                                         "' received '"+thisParameter.getType() +"'");
         }
     }


    /*---------------------------------------------------------------------------*\
     *                        Instruction processing                             *
    \*---------------------------------------------------------------------------*/

    /**
     * Processes an instruction that pushes a constant onto the operand stack.
     *
     * @param instruction  the instruction that pushes a constant onto
     *                     the operand stack
     */
    private void opc_constant(Constant instruction) {
        append(instruction);
    }

    /**
     * Processes an instruction that loads a value from a local variable and
     * pushes it onto the operand stack.
     *
     * @param type   the base type of the local variable
     * @param index  the index of the local variable
     */
    private void opc_load(Klass type, int index) {
        Local local = frame.load(index, type);
        type = frame.getDerivedLocalTypeAt(index);
        LoadLocal instruction = new LoadLocal(type, local);
        append(instruction);
    }

    /**
     * Processes an instruction that pops a value off the operand stack and
     * stores it into a local variable.
     *
     * @param type   the base type of the local variable
     * @param index  the index of the local variable
     */
    private void opc_store(Klass type, int index) {
        StackProducer value = frame.pop(type);
        frame.verifyLocalVariableIndex(type, index);
        if (value.getType().isSquawkPrimitive()) {
            type = value.getType();
        }
        Local local = frame.allocateLocal(type, index);

        if (index == 0) {
            if (frame.getDerivedLocalTypeAt(0) == Klass.UNINITIALIZED_THIS) {
                throw codeParser.verifyError("constructor does not initialize receiver");
            }

            // Detect overwriting of 'this'
            if (copyOfThis == null && method.isConstructor()) {
                throw new OverwroteThisInConstructorError();
            }
        }

        frame.store(index, value.getType(), local);
        StoreLocal instruction = new StoreLocal(local, value);
        append(instruction);
    }

    /**
     * Processes an instruction that pops a value off the operand stack and
     * stores it into an array at a given index.
     *
     * @param arrayType  the type of the array
     */
    private void opc_arraystore(Klass arrayType) {
        if (!arrayType.isArray()) {
            throw codeParser.verifyError("expected array class");
        }
        Klass componentType = arrayType.getComponentType();
        StackProducer value = frame.pop(componentType);
        StackProducer index = frame.pop(Klass.INT);
        StackProducer array;

        if (arrayType == Klass.BYTE_ARRAY) {
            /*
             * 'baload' is used to access byte arrays and boolean arrays
             */
            array = frame.pop(Klass.OBJECT);
            if (array.getType() != Klass.BYTE_ARRAY && array.getType() != Klass.BOOLEAN_ARRAY && array.getType() != Klass.NULL) {
                throw codeParser.verifyError("invalid 'bastore'");
            }
        } else {
            array = frame.pop(arrayType);
            /*
             * Special handling for 'aastore'
             */
            if (arrayType == Klass.OBJECT_ARRAY) {
                arrayType = array.getType();
                if (arrayType != Klass.NULL) {
                    componentType = arrayType.getComponentType();
                    if (!componentType.isArray() && !value.getType().isArray()) {
                        /*
                         * As a special case, if both the array component type and
                         * the type are both non-array types (or NULL), then
                         * allow the aastore - it will be checked at runtime.
                         */
                    } else {
                        if (!componentType.isAssignableFrom(value.getType())) {
                            throw codeParser.verifyError("invalid 'aastore'");
                        }
                    }
                }
                frame.verifyUseOfSquawkPrimitive(componentType, value.getType());
            } else {
                Assert.that(componentType.isPrimitive());
                if (array.getType() == null || array.getType() == Klass.NULL) {
                    // let runtime throw NPE
                } else if (array.getType() != arrayType) {
                    throw codeParser.verifyError("invalid array " + array.getType() + " for array store of type " + arrayType.getComponentType().getName());
                }
            }
        }
        ArrayStore instruction = new ArrayStore(componentType, array, index, value);
        append(instruction);
    }

    /**
     * Processes an instruction that loads a value from an array at a given index
     * and pushes it to the operand stack.
     *
     * @param arrayType  the type of the array
     */
    private void opc_arrayload(Klass arrayType) {
        StackProducer index = frame.pop(Klass.INT);
        StackProducer array;

        if (arrayType == Klass.BYTE_ARRAY) {
            /*
             * 'baload' is used to access byte arrays and boolean arrays.
             * Check that the receiver type is one of these and set the result
             * to Type.INT in both cases.
             */
            array = frame.pop(Klass.OBJECT);
            if (array.getType() != Klass.BYTE_ARRAY && array.getType() != Klass.BOOLEAN_ARRAY && array.getType() != Klass.NULL) {
                throw codeParser.verifyError("invalid 'baload'");
            }
        } else {
            array = frame.pop(arrayType);
            if (arrayType.getComponentType().isPrimitive()) {
                if (array.getType() == null || array.getType() == Klass.NULL) {
                    // let runtime throw NPE
                } else if (array.getType() != arrayType) {
                    throw codeParser.verifyError("invalid array " + array.getType() + " for array load of type " + arrayType.getComponentType().getName());
                }
            } else {
                arrayType = array.getType();
            }
        }

        Klass componentType = (arrayType != Klass.NULL ? arrayType.getComponentType() : Klass.NULL);
        ArrayLoad instruction = new ArrayLoad(componentType, array, index);
        append(instruction);
    }

    /**
     * Processes an instruction that pops one or two words off the
     * operand stack.
     *
     * @param pop2  specifies if one or two words are popped
     */
    private void opc_pop(boolean pop2) {
        if (pop2) {
            if (!frame.isTopDoubleWord()) {
                append(new Pop(frame.pop(Klass.ONE_WORD)));
                append(new Pop(frame.pop(Klass.ONE_WORD)));
            } else {
                append(new Pop(frame.pop(Klass.TWO_WORD)));
            }
        } else {
            append(new Pop(frame.pop(Klass.ONE_WORD)));
        }
    }

    /**
     * Processes one of the untyped stack manipulation instructions.
     *
     * @param opcode  the opcode of an untyped stack manipulation instruction
     * @see   StackOp
     */
    private void opc_stackop(int opcode) {
        /*
         * Special code to try and replace simple dup's with a constant
         */
        if (opcode == Opcode.opc_dup && !frame.isTopDoubleWord()) {
            Instruction last = ir.getTail();
            if (last instanceof Constant) {
                Constant instruction = (Constant)last;
                opc_constant(Constant.create(instruction.getValue()));
                return;
            }
        }
        frame.doStackOp(opcode);
        StackOp instruction = new StackOp(opcode);
        append(instruction);
    }

    /**
     * Processes an instruction that pops two values off the operand stack
     * performs a binary arithmetic operation on them and pushes the result.
     *
     * @param type    the type of the inputs to and value resulting from
     *                the operation
     * @param opcode  the Squawk opcode corresponding to the operation
     */
    private void opc_arithmetic(Klass type, int opcode) {
        StackProducer right = frame.pop(type);
        StackProducer left  = frame.pop(type);
        ArithmeticOp instruction = new ArithmeticOp(left, right, opcode);
        append(instruction);
    }

    /**
     * Processes an instruction that pops two values off the operand stack
     * performs a binary comparison operation on them and pushes the result.
     *
     * @param type    the type of the inputs to the operation
     * @param opcode  the Squawk opcode corresponding to the operation
     */
    private void opc_comparison(Klass type, int opcode) {
        StackProducer right = frame.pop(type);
        StackProducer left  = frame.pop(type);
        ComparisonOp instruction = new ComparisonOp(left, right, opcode);
        append(instruction);
    }

    /**
     * Processes an instruction that pops a value of a given type from the
     * operand stack, converts it to a value of another type and pushes
     * the result.
     *
     * @param  to     the type the value is converted to
     * @param  from   the type the value is converted from
     * @param  opcode the Squawk opcode corresponding to the operation
     */
    private void opc_conversion(Klass from, Klass to, int opcode) {
        StackProducer value = frame.pop(from);
        ConversionOp instruction = new ConversionOp(to, value, opcode);
        append(instruction);
    }

    /**
     * Processes an instruction that pops a value of a given type from the
     * operand stack, negates it and pushes the result
     *
     * @param  type   the type of the value being negated
     * @param  opcode the Squawk opcode corresponding to the operation
     */
    private void opc_negation(Klass type, int opcode) {
        StackProducer value = frame.pop(type);
        NegationOp instruction = new NegationOp(value, opcode);
        append(instruction);
    }

    /**
     * Processes an instruction that pops an integer and another value from the
     * operand stack, shifts the value by the integer amount and pushes the
     * result.
     *
     * @param  type   the type of the value being shifted
     * @param  opcode the Squawk opcode corresponding to the operation
     */
    private void opc_shift(Klass type, int opcode) {
        StackProducer shift = frame.pop(Klass.INT);
        StackProducer value = frame.pop(type);
        ArithmeticOp instruction = new ArithmeticOp(value, shift, opcode);
        append(instruction);
    }

    /**
     * Processes an instruction that adds an integer value to an integer
     * typed local variable.
     *
     * @param index  the index of the local variable
     * @param value  the value to be the local variable
     */
    private void opc_iinc(int index, int value) {
        if (value == 1 || value == -1) {
            Local local = frame.load(index, Klass.INT);
            IncDecLocal instruction = new IncDecLocal(local, value == 1);
            append(instruction);
        } else {
            frame.growMaxStack(2);
            opc_load(Klass.INT, index);
            // there are more special cases for positive constants than neg, so keep constant positive and flip operation
            if (value > 0) {
                opc_constant(new ConstantInt(new Integer(value)));
                opc_arithmetic(Klass.INT, OPC.ADD_I);
            } else {
                opc_constant(new ConstantInt(new Integer(-value)));
                opc_arithmetic(Klass.INT, OPC.SUB_I);
            }
            opc_store(Klass.INT, index);
            frame.resetMaxStack();
        }
    }

    /**
     * Processes an instruction that transfers the flow of control to a
     * specified address.
     *
     * @param target  the address to which the flow of control is transferred
     */
    private void opc_goto(Target target) {
        Branch instruction = new Branch(target);
        append(instruction);
        processBasicBlockDelimiter(instruction, target, null);
    }

    /**
     * Processes an instruction that pops a value off the operand stack and
     * compares it against 0 or null, depending on the type of the value.
     *
     * @param  type    the type of the value to be compared
     * @param  opcode  the opcode denoting the semantics of the comparison
     * @param  target  the address to control will transfer if the result of
     *                 the comparison is true
     */
    private void opc_if(Klass type, int opcode, Target target) {
        StackProducer value = frame.pop(type);

        Instruction tail = ir.getTail();
        if (tail instanceof ComparisonOp) {
            ComparisonOp instruction = (ComparisonOp)tail;

            /*
             * Determine the appropriate IF... opcode for the operation
             */
            if (instruction.isLCMP()) {
                int ifOpcode = OPC.IF_CMPEQ_L;
                int offset = opcode - OPC.IF_EQ_I;
                Assert.that(offset >= 0 && offset < 6);
                ifOpcode += offset;

                /*
                 * Push the operands of the comparison back onto the stack so that
                 * they can be processed by opc_ifcompare
                 */
                frame.push(instruction.getLeft());
                frame.push(instruction.getRight());

                /*
                 * Remove the comparison instruction
                 */
                ir.remove(instruction);
                opc_ifcompare(instruction.getLeft().getType(), ifOpcode, target);
                return;
            }
        }

        if (value.getType().isSquawkPrimitive()) {
            throw codeParser.verifyError(value.getType().getName() + " must use EQ() and NE() for equality comparisons");
        }

        If instruction = new If(value, opcode, target);
        append(instruction);
        processBasicBlockDelimiter(instruction, target, null);
    }

    /**
     * Processes an instruction that pops two values off the operand stack and
     * compares them against each other.
     *
     * @param  type    the type of the values to be compared
     * @param  opcode  the opcode denoting the semantics of the comparison
     * @param  target  the address to control will transfer if the result of
     *                 the comparison is true
     */
    private void opc_ifcompare(Klass type, int opcode, Target target) {
        StackProducer right = frame.pop(type);
        StackProducer left = frame.pop(type);
        if (left.getType().isSquawkPrimitive() || right.getType().isSquawkPrimitive()) {
            throw codeParser.verifyError(left.getType().getName() + " values must be compared with built in methods (eq(), ne(), lt() etc.)");
        }
        IfCompare instruction = new IfCompare(left, right, opcode, target);
        append(instruction);
        processBasicBlockDelimiter(instruction, target, null);
    }

    /**
     * Processes a point in the bytecode stream that is referred to in a
     * line number or local variable table.
     *
     * @param position the position instruction
     */
    private void opc_position(Position position) {
        Assert.that(position.getBytecodeOffset() == codeParser.getLastOpcodeAddress());
        append(position);
    }

    /**
     * Processes a point in the bytecode stream that is the explicit target
     * of a control flow instruction. This also denotes the entry point to
     * a new basic block.
     *
     * @param target  the target of the instruction
     * @param flowFromPredecessor  specifies if there is implicit control
     *                flow from the preceeding instruction
     */
    private void opc_target(Target target, boolean flowFromPredecessor) {
        if (!flowFromPredecessor) {
            /*
             * There is no flow from the lexical predecessor so ignore the
             * derived stack and locals and re-initialize them from the target.
             */
            frame.resetStack(target, false);
            frame.resetLocals(target);
        } else {
            /*
             * Verify recorded stack map against currently derived types.
             */
            frame.mergeStack(target, true);
            frame.mergeLocals(target, true);
        }

        /*
         * Allocate the instruction.
         */
        Phi instruction = new Phi(target);
        append(instruction);
    }

    /**
     * Processes a point in the bytecode stream that is the entry of an
     * exception handler.
     *
     * @param target  the address of the entry to an exception handler
     */
    private void opc_catch(Target target) {

        /*
         * Occasionally a TCK test or optimized code (e.g. BCO) will contain
         * a sequence of code such that there is something on the stack at the
         * 'goto' that delimits a try-catch block. This value will be correctly
         * spilled and filled as necessary. However, to correctly handle the
         * code in the exception handler, the stack must be cleared on entry
         * to the handler, just as the stack is conceptually cleared during
         * execution.
         */

        if (target.getStack().length != 1) {
            throw codeParser.verifyError("invalid stack map for exception handler");
        }

        frame.resetLocals(target);
        frame.resetStack(target, true);
        Klass throwableKlass = target.getStack()[0];
        Catch instruction = new Catch(throwableKlass, target);
        append(instruction);

        /*
         * Ensure the producers of any brain-dead TCK tests are spilled.
         */
        StackProducer[] producers = instruction.getTarget().getDerivedStack();
        for (int i = 0 ; i < producers.length ; i++) {
            if (producers[i] != null) {
                frame.spill(producers[i]);
            }
        }
    }

    /**
     * Processes a point in the bytecode stream at which an exception handler
     * becomes active.
     *
     * @param target  the target representing the entry to the exception
     *                handler
     */
    private void opc_try(Target target) {
        IRExceptionHandler handler = new IRExceptionHandler(target);
        Try instruction = new Try();
        handler.setEntry(instruction);
        if (handlers == null) {
            handlers = new Stack();
        }
        handlers.push(handler);
        append(instruction);
    }

    /**
     * Processes a point in the bytecode stream at which an exception handler
     * becomes deactive.
     *
     * @param target  the target representing the entry to the exception
     *                handler
     */
    private void opc_tryend(Target target) {
        if (handlers == null || handlers.isEmpty()) {
            throw codeParser.verifyError("invalid start_pc in exception handler");
        }
        IRExceptionHandler handler = (IRExceptionHandler)handlers.pop();
        TryEnd instruction = new TryEnd();
        handler.setExit(instruction);
        ir.addExceptionHandler(handler);
        append(instruction);
    }

    /**
     * Processes an instruction that is prefixed with the <i>wide</i> bytecode.
     *
     * @param opcode  the opcode of the instruction prefixed by <i>wide</i>
     */
    private void opc_wide(int opcode) {
        switch (opcode) {
            case Opcode.opc_iinc:   opc_iinc(codeParser.parseLocalVariableOperand(true, false), codeParser.parseShortOperand()); break;

            case Opcode.opc_iload:  opc_load(Klass.INT,        codeParser.parseLocalVariableOperand(true, false)); break;
            case Opcode.opc_lload:  opc_load(Klass.LONG,       codeParser.parseLocalVariableOperand(true, true )); break;
            case Opcode.opc_aload:  opc_load(Klass.REFERENCE,  codeParser.parseLocalVariableOperand(true, false)); break;

            case Opcode.opc_istore: opc_store(Klass.INT,       codeParser.parseLocalVariableOperand(true, false)); break;
            case Opcode.opc_lstore: opc_store(Klass.LONG,      codeParser.parseLocalVariableOperand(true, true )); break;
            case Opcode.opc_astore: opc_store(Klass.REFERENCE, codeParser.parseLocalVariableOperand(true, false)); break;
                
/*if[FLOATS]*/
            case Opcode.opc_fload:  opc_load(Klass.FLOAT,      codeParser.parseLocalVariableOperand(true, false)); break;
            case Opcode.opc_dload:  opc_load(Klass.DOUBLE,     codeParser.parseLocalVariableOperand(true, true )); break;
                
            case Opcode.opc_fstore: opc_store(Klass.FLOAT,     codeParser.parseLocalVariableOperand(true, false)); break;
            case Opcode.opc_dstore: opc_store(Klass.DOUBLE,    codeParser.parseLocalVariableOperand(true, true )); break;
/*end[FLOATS]*/
            default:                throw codeParser.verifyError("invalid wide opcode");
        }
    }

    /**
     * Processes a <i>tableswitch</i> instruction.
     */
    private void opc_tableswitch() {
        StackProducer key = frame.pop(Klass.INT);
        codeParser.parseSwitchPadding();
        Target defaultTarget = codeParser.parseBranchOperand(true);
        int low  = codeParser.parseIntOperand();
        int high = codeParser.parseIntOperand();
        if (high - low < 0) {
            throw codeParser.verifyError("unordered tableswitch");
        }
        int cases = high - low + 1;
        TableSwitch instruction = new TableSwitch(key, low, high,defaultTarget);
        for (int caseValue = low ; --cases >= 0 ; caseValue++) {
            Target target = codeParser.parseBranchOperand(true);
            instruction.addTarget(caseValue, target);
        }
        append(instruction);
        processBasicBlockDelimiter(instruction, defaultTarget, instruction.getTargets());
    }

    /**
     * Processes a <i>lookupswitch</i> instruction.
     */
    private void opc_lookupswitch() {
        frame.growMaxStack(1); // for the lookup table
        StackProducer key = frame.pop(Klass.INT);
        codeParser.parseSwitchPadding();
        Target defaultTarget = codeParser.parseBranchOperand(true);
        int npairs  = codeParser.parseIntOperand();
        LookupSwitch instruction = new LookupSwitch(key, npairs, defaultTarget);
        int lastCaseValue = -1;
        for (int i = 0 ; i < npairs ; i++) {
            int caseValue = codeParser.parseIntOperand();
            if (i != 0 && caseValue <= lastCaseValue) {
                throw codeParser.verifyError("unordered lookupswitch");
            }
            Target target = codeParser.parseBranchOperand(true);
            instruction.addTarget(i, caseValue, target);
            lastCaseValue = caseValue;
        }
        instruction.finishBuilding();
        append(instruction);
        processBasicBlockDelimiter(instruction, defaultTarget, instruction.getTargets());
        frame.resetMaxStack();
    }

    /**
     * Processes an <code>ireturn</code>, <i>lreturn</i>, <i>freturn</i>,
     * <i>dreturn</i>, <i>areturn</i> or <i>return</i> instruction.
     *
     * @param   returnType  the type of the value returned or <code>null</code>
     *                      if processing a <code>void</code> return
     */
    private void opc_return(Klass returnType) {
        boolean isConstructor = method.isConstructor() && !method.isReplacementConstructor();
        StackProducer value;
        if (method.getReturnType() == Klass.VOID || isConstructor) {
            if (returnType != Klass.VOID) {
                throw codeParser.verifyError("invalid return for void method");
            }
            if (isConstructor) {
                boolean error;
                if (copyOfThis != null) {
                    error = (method.getDefiningClass() != copyOfThis.getType());
                } else {
                    error = (frame.containsType(Klass.UNINITIALIZED_THIS));
                }
                if (error) {
                    throw codeParser.verifyError("constructor does not initialize receiver");
                }
            }
            value = null;
        } else {
            value = frame.pop(returnType);
            Klass declaredType = method.getReturnType();
            Klass localType = frame.getLocalTypeFor(declaredType);
            boolean valid;
            if (localType == Klass.REFERENCE) {
                valid = declaredType.isInterface() || declaredType.isAssignableFrom(value.getType());
            } else {
                valid = localType.isAssignableFrom(value.getType());
            }
            if (!valid) {
                throw codeParser.verifyError("invalid return for non-void method");
            }
        }
        if (method.isSynchronized()) {
            if (method.getReturnType() != Klass.VOID) {
                frame.spill(value);
            }
            exitSynchronizedMethod();
        }
        if (isConstructor) {
            Assert.that(value == null);
            /*
             * Insert a load of 'this' (i.e. the object being constructed).
             */
            frame.growMaxStack(1);
            if (copyOfThis == null) {
                if (frame.getDerivedLocalTypeAt(0) == method.getDefiningClass()) {
                    // Normal case
                    opc_load(method.getDefiningClass(), 0);
                } else {
                    //
                    // This is required (at least) to pass the following CLDC TCK 1.1 test:
                    //
                    //    javasoft.sqe.tests.vm.cldc.classfmt.classfmt022.classfmt02201m1_1.classfmt02201m1_1_wrapper
                    //
                    // which contains dead code that must verify correctly.
                    opc_constant(new ConstantObject(Klass.NULL, null));
                }
            } else {
                LoadLocal load = new LoadLocal(copyOfThis.getType(), copyOfThis.getLocal());
                append(load);
            }
            frame.resetMaxStack();
            value = frame.pop(method.getDefiningClass());
        }
        Return instruction = new Return(value);
        append(instruction);
        processBasicBlockDelimiter(instruction, null, null);
    }

    /**
     * Processes a <i>monitorenter</i> instruction.
     *
     * @param isForClass specifies if this is obtaining a monitor on a class
     *                   to implement static method syncronization
     */
    private void opc_monitorenter(boolean isForClass) {
        MonitorEnter instruction;
        if (!isForClass) {
            StackProducer object = frame.pop(Klass.REFERENCE);
            instruction = new MonitorEnter(object);
        } else {
            instruction = new MonitorEnter(null);
        }
        append(instruction);
    }

    /**
     * Processes a <i>monitorexit</i> instruction.
     *
     * @param isForClass specifies if this is releasinga monitor on a class
     *                   to implement static method syncronization
     */
    private void opc_monitorexit(boolean isForClass) {
        MonitorExit instruction;
        if (!isForClass) {
            StackProducer object = frame.pop(Klass.REFERENCE);
            instruction = new MonitorExit(object);
        } else {
            instruction = new MonitorExit(null);
        }
        append(instruction);
    }

    /**
     * Processes an instruction that pops an instance off the stack that is
     * assignable to <code>Throwable</code> and throws it.
     *
     */
    private void opc_throw() {
        StackProducer throwable = frame.pop(Klass.THROWABLE);
        Throw instruction = new Throw(throwable);
        append(instruction);
        processBasicBlockDelimiter(instruction, null, null);
    }

    /**
     * Processes an instruction that pops an object from the operand stack
     * and pushes the value of a field of the object.
     *
     * @param field  the referenced field
     */
    private void opc_getfield(Field field) {
        StackProducer object = getObjectForInstanceField(field, false);
        GetField instruction = new GetField(field, object);
        append(instruction);
    }

    /**
     * Processes an instruction that pops an object and a value from the
     * operand stack and assigns the value to a field of the object.
     *
     * @param field  the referenced field
     */
    private void opc_putfield(Field field) {
        StackProducer value  = frame.pop(field.getType());
        StackProducer object = getObjectForInstanceField(field, true);
        PutField instruction = new PutField(field, object, value);
        append(instruction);
    }

    /**
     * Processes an instruction that pushes the value of a static field to
     * the operand stack.
     *
     * @param field  the referenced field
     */
    private void opc_getstatic(Field field) {
        if (field.hasConstant()) {
            if (VM.getCurrentIsolate().getLeafSuite().contains(field.getDefiningClass())) {
                // Class structure can be modified so set the flag indicating that it
                // should reify its constant fields when it is initialized.
            	// Assume that a class defined in another suite is NOT modifiable.
                field.getDefiningClass().updateModifiers(Modifier.COMPLETE_RUNTIME_STATICS);
            } else {
                // The Class object is read-only so replace 'getstatic' with
                // the appropriate load constant instruction
                if (field.getType().isPrimitive()) {
                    long value = field.getPrimitiveConstantValue();
                    switch (field.getType().getSystemID()) {
                        case CID.BYTE:
                        case CID.BOOLEAN:
                        case CID.SHORT:
                        case CID.CHAR:
                        case CID.INT: {
                            append(new ConstantInt(new Integer((int)value)));
                            return;
                        }
                        case CID.LONG:  {
                            append(new ConstantLong(new Long(value)));
                            return;
                        }
/*if[FLOATS]*/
                        case CID.FLOAT: {
                            append(new ConstantFloat(new Float(Float.intBitsToFloat((int)value))));
                            return;
                        }
                        case CID.DOUBLE: {
                            append(new ConstantDouble(new Double(Double.longBitsToDouble(value))));
                            return;
                        }
/*else[FLOATS]*/
//                          case CID.FLOAT:
//                          case CID.DOUBLE:
//                              Assert.shouldNotReachHere("floats not supported");
/*end[FLOATS]*/
                        default:
                            throw Assert.shouldNotReachHere();
                    }
                } else {
                    append(new ConstantObject(Klass.STRING, field.getStringConstantValue()));
                    return;
                }
            }
        }

        // java   getstatic stack ops: ... -> value (stack usage: 1)
        // squawk getstatic stack ops: ... -> value, or class_object -> value  (stack usage: 1)
        // so should not change max stack.

        GetStatic instruction = new GetStatic(field);
        append(instruction);
    }

    /**
     * Processes an instruction that pops a value from the
     * operand stack and assigns it to a static field.
     *
     * @param field  the referenced field
     */
    private void opc_putstatic(Field field) {
        if (field.hasConstant() && field.getType().isPrimitive()) {

            if (VM.getCurrentIsolate().getLeafSuite().contains(field.getDefiningClass())) {
                // Class structure can be modified so set the flag indicating that it
                // should reify its constant fields when it is initialized.
            	// Assume class defined in a parent suite to be read only
                field.getDefiningClass().updateModifiers(Modifier.COMPLETE_RUNTIME_STATICS);
            } else {
                throw Assert.shouldNotReachHere("writing to constant field of immutable class not supported");
            }
        }

        // java   putstatic stack ops: value -> ...  (stack usage: 1)
        // squawk putstatic stack ops: value -> ..., or class_object, value -> ....  (stack usage: 2)
        // so change maxstack, but after append.

        StackProducer value = frame.pop(field.getType());
        PutStatic instruction = new PutStatic(field, value);
        append(instruction);
        if (field.getDefiningClass() != method.getDefiningClass()) {
            frame.growMaxStack(1); // for the class_ version
        }
        frame.resetMaxStack();
    }

    /**
     * Processes an instruction that invokes a virtual method.
     *
     * @param callee  the invoked method
     */
    private void opc_invokevirtual(Method callee) {
        if (!callee.isHosted()) {
            if (callee.isConstructor() || callee.isClassInitializer()) {
                throw codeParser.verifyError("expected invokespecial");
            }
            StackProducer[] parameters = popInvokeParameters(callee);
            verifyThisParameter(callee, parameters[0]);
            verifyNonSquawkPrimitiveParameters(callee, parameters);
            InvokeVirtual instruction = new InvokeVirtual(callee, parameters);
            append(instruction);
        }
        else {
            removeHostedMethodInvocation(callee);
        }
    }

    /**
     * Removes a call to a method that only runs in a hosted environment.
     *
     * @param callee  the hosted-only method being called
     */
    private void removeHostedMethodInvocation(Method callee) {
//System.err.println("removing call to " + callee + " at " + codeParser.prefix(""));

        Klass params[] = callee.getParameterTypes();
        OperandVisitor visitor = new OperandVisitor() {
            public StackProducer doOperand(Instruction instruction, StackProducer operand) {
                removeOperand(operand, this);
                return operand;
            }
        };

        for (int i = params.length - 1; i >= 0; --i) {
            StackProducer operand = frame.pop(params[i]);
            removeOperand(operand, visitor);
        }
        if (!callee.isStatic() || callee.isConstructor()) {
            StackProducer operand = frame.pop(callee.getDefiningClass());
            removeOperand(operand, visitor);
        }

        Klass returnType = callee.getReturnType();
        if (returnType != Klass.VOID) {
            if (returnType.isPrimitive()) {
                switch (returnType.getSystemID()) {
                    default:         opc_constant(new ConstantInt(new Integer(0)));    break;
                    case CID.LONG:   opc_constant(new ConstantLong(new Long(0L)));     break;
/*if[FLOATS]*/
                    case CID.FLOAT:  opc_constant(new ConstantFloat(new Float(0F)));   break;
                    case CID.DOUBLE: opc_constant(new ConstantDouble(new Double(0D))); break;
/*end[FLOATS]*/
                }
            } else {
                opc_constant(new ConstantObject(Klass.NULL, null));
            }
        }
    }

    /**
     * Removes an instruction that is an operand to another instruction that is
     * being removed. This also recursively removes the operands of this operand.
     *
     * @param operand  the operand to remove
     * @param visitor  a visitor used to recursively remove the operands of <code>operand</code>
     */
    private void removeOperand(StackProducer operand, OperandVisitor visitor) {
        operand.visit(visitor);
        if (!(operand instanceof StackMerge)) {
            ir.remove(operand);
        }
    }

    /**
     * Processes an instruction that invokes a constructor.
     *
     * @param callee the invoked constructor
     */
    private void opc_invokeinit(Method callee) {
        frame.growMaxStack(1); // for the class object
        StackProducer[] parameters = popInvokeParameters(callee);
        final boolean trace = Translator.TRACING_ENABLED && Tracer.isTracing("jvmverifier", method.toString());

        verifyNonSquawkPrimitiveParameters(callee, parameters);

        /*
         * Update the type of 'this' if the enclosing method is a constructor and this a
         * chained constructor invocation (i.e. a call to another constructor in the same
         * class or a super class).
         */
        StackProducer thisParameter = parameters[0];
        final boolean chainedConstructorInvoked = (thisParameter.getType() == Klass.UNINITIALIZED_THIS);
        Klass initializedType;
        if (chainedConstructorInvoked) {
            initializedType = method.getDefiningClass();
            thisParameter.updateType(initializedType);
            if (copyOfThis != null) {
                copyOfThis.updateType(initializedType);
            }
            for (int i = 1 ; i < parameters.length ; i++) {
                StackProducer parameter = parameters[i];
                if (parameter.getType() == Klass.UNINITIALIZED_THIS) {
                    parameter.updateType(initializedType);
                }
            }

            frame.replaceTypeWithType(Klass.UNINITIALIZED_THIS, initializedType); // handles locals and stack

            Klass currentKlass = method.getDefiningClass();
            Klass calleeKlass = callee.getDefiningClass();
            if (!method.isConstructor() || (currentKlass != calleeKlass && currentKlass.getSuperclass() != calleeKlass)) {
                throw codeParser.verifyError("invalid constructor call");
            }

        } else {

            /*
             * Given that the receiver was the result of 'new' then its
             * type must be a UninitializedObjectClass otherwise this is
             * an attempt to re-initialise an initialized object.
             */
            UninitializedObjectClass uninitializedType;
            try {
                uninitializedType = (UninitializedObjectClass)thisParameter.getType();
            } catch (ClassCastException e) {
                throw codeParser.verifyError("expected uninitialized object");
            }

            initializedType = uninitializedType.getInitializedType();
            frame.replaceTypeWithType(uninitializedType, initializedType);
            thisParameter.updateType(initializedType);
        }

       /*
        * Check now to see if the uninitialized object was duped. If so,
        * there should be exactly one more copy of the instance on the
        * stack. This instance should also be popped as the result of the
        * 'invokeinit' instruction being created will replace it.
        */
       int numRemovableDups = 0;
       if (thisParameter.isDuped() && !chainedConstructorInvoked) {
           /*
            * Ensure that the uninitialised object was only duped by the 'dup'
            * bytecode. While other stack manipulation bytecodes can legally
            * be applied to an uninitialised object, trying to propogate their
            * effects to the value returned by the constructor will require
            * extensive analysis. Given that this should never occur (even in
            * the TCK), we just assert this property here.
            *
            * We are looking for sequences like:
            *     new
            *     dup
            *     dup-n (n is zero or more)
            *     [params for constructor]
            *     invokeinit
            *     .... note that one + n dups of the new object are now on the stack...
            *
            * and convert into:
            *    new
            *    [params for constructor (reversed)]
            *    invokestatic (pushing result)
            *    dup-n (n is zero or more)
            *     .... note that one + n dups of the new object are now on the stack...
            *
            * Note that TCK code sometimes inserts code between the dups and the invoke, which use up the dupped values. There may 
            * not be a value on the stack after the invokeinit.
            *    
            */
           StackOp stackOp = (StackOp)thisParameter.getNext();
           while (frame.getTopOfStack() == thisParameter) {
               Assert.always(stackOp.getOpcode() == Opcode.opc_dup, "cannot translate '"+ Opcode.mnemonics[stackOp.getOpcode()] + "' of uninitialized object");
               numRemovableDups++;
               Instruction nextInstr = stackOp.getNext();
               frame.pop(thisParameter.getType());
               ir.remove(stackOp);
               if (nextInstr  instanceof StackOp) {
                   stackOp = (StackOp)nextInstr;
               } else {
                   break;
               }
           }

            /*
             * 'undo' the duping of the uninitialized object
             */
            if (numRemovableDups > 0) {
                thisParameter.cancelDuping();
            }
        } 
       
       //  Assert.always(frame.getStackSize() == 0); // dups have been removed, parameters accounted for
        verifyThisParameter(callee, thisParameter);
        InvokeStatic instruction = new InvokeStatic(callee, parameters);
        instruction.updateType(initializedType);
        append(instruction);

        /*
         * Insert pop instructions to remove any extra copies of the
         * initialised object on the stack after the constructor has been
         * called
         */
        if (numRemovableDups == 0) {
            opc_pop(false);
        } else {
            while (--numRemovableDups != 0) {
                opc_stackop(Opcode.opc_dup);
            }
        }
        frame.resetMaxStack();
    }

    /**
     * Processes an instruction that invokes a super method.
     *
     * @param callee  the invoked method
     */
    private void opc_invokesuper(Method callee) {
        frame.growMaxStack(1); // for the class object
        StackProducer[] parameters = popInvokeParameters(callee);
        verifyThisParameter(callee, parameters[0]);
        verifyNonSquawkPrimitiveParameters(callee, parameters);
        InvokeSuper instruction = new InvokeSuper(callee, parameters);
        append(instruction);
        frame.resetMaxStack();
    }

    /**
     * Processes an instruction that invokes a constructor or a
     * private or super method.
     *
     * @param callee  the invoked method
     */
    private void opc_invokespecial(Method callee) {
        if (!callee.isHosted()) {
            if (callee.isConstructor()) {
                opc_invokeinit(callee);
            } else if (callee.isFinal() || callee.isPrivate()) {
                opc_invokevirtual(callee);
            } else {
                /*
                 * The callee must be somewhere in superclass hierarchy
                 */
                Klass currentKlass = method.getDefiningClass();
                if (!callee.getDefiningClass().isAssignableFrom(currentKlass)) {
                    throw codeParser.verifyError("invalid invokespecial");
                }
                if (callee.isAbstract()) {
                	throw new com.sun.squawk.translator.AbstractMethodError(callee.toString());
                }
                opc_invokesuper(callee);
            }
        } else {
            removeHostedMethodInvocation(callee);
        }
    }

    /**
     * Processes an instruction that invokes a static method.
     *
     * @param callee  the invoked method
     */
    private void opc_invokestatic(Method callee) {
        if (!callee.isHosted()) {
            if (callee.isClassInitializer()) {
                throw codeParser.verifyError("call to <clinit> with invokestatic is invalid");
            }
            if (callee.isConstructor()) {
                throw codeParser.verifyError("call to <init> with invokestatic is invalid");
            }
            frame.growMaxStack(1); // for the class object
            StackProducer[] parameters = popInvokeParameters(callee);
            verifyNonSquawkPrimitiveParameters(callee, parameters);
            InvokeStatic instruction = new InvokeStatic(callee, parameters);
            append(instruction);
            frame.resetMaxStack();
        } else {
            removeHostedMethodInvocation(callee);
        }
    }

    /**
     * Processes an instruction that invokes an interface method.
     *
     * @param callee  the invoked method
     * @param opcode  the opcode of the instruction - must be <i>invokevirtual</i> or <i>invokeinterface</i>
     */
    private void opc_invokeinterface(Method callee, int opcode) {
        Assert.that(opcode == Opcode.opc_invokeinterface || opcode == Opcode.opc_invokevirtual);
        if (opcode == Opcode.opc_invokeinterface) {
            // This verification of the 'count' operand is required for TCK compliance.
            // The actual value of count is never used by the Squawk VM.
            int count = codeParser.parseByteOperand();
            Klass[] types = callee.getParameterTypes();
            if (count != types.length + 1) {
                int expected = 1;
                for (int i = 0; i != types.length; ++i) {
                    expected += types[i].isDoubleWord() ? 2 : 1;
                }
                if (expected != count) {
                    throw codeParser.verifyError("invalid 'count' operand to invokeinterface");
                }
            }

            if (codeParser.parseByteOperand() != 0) {
                throw codeParser.verifyError("fourth operand to invokeinterface is not 0");
            }
        }
        frame.growMaxStack(1); // +1 for the class object
        int min = 3 - frame.getStackSize();
        if (min > 0) {
            frame.growMaxStack(min); // need three slots to call findslot
        }
        StackProducer[] parameters = popInvokeParameters(callee);
        StackProducer receiver = parameters[0];

        verifyThisParameter(callee, receiver);
        verifyNonSquawkPrimitiveParameters(callee, parameters);

        /*
         * Build a FindSlot instruction
         */
        spillParameters(parameters);
        receiver.setDuped(frame);
        StackProducer findSlot = new FindSlot(callee, receiver);
        append(findSlot);
        frame.spill(findSlot);
        StackProducer fslot = frame.pop(Klass.INT);
        Assert.that(fslot == findSlot);

        /*
         * Build an InvokeSlot that includes the FindSlot result as the first parameter.
         */
        parameters = insertSlotParameter(parameters, findSlot);
        InvokeSlot instruction = new InvokeSlot(callee, parameters);
        append(instruction);
        frame.resetMaxStack();
    }

    /**
     * Processes an instruction that creates a new instance of a specified
     * class and pushes it to the operand stack.
     *
     * @param uninitializedType  the type of the uninitialized object created by <i>new</i>
     */
    private void opc_new(UninitializedObjectClass uninitializedType) {
        /*
         * Spill extraneous values on the stack
         */
        New instruction = new New(uninitializedType);
        append(instruction);
    }

    /**
     * Processes an instruction that pops a value from the operand stack and
     * uses it to create an new array of a specified type whose length is
     * determined by the popped value.
     *
     * @param type  the type of the array
     */
     private void opc_newarray(Klass type) {
         frame.growMaxStack(1); // for the class object
         StackProducer length = frame.pop(Klass.INT);
         NewArray instruction = new NewArray(type, length);
         append(instruction);
         frame.resetMaxStack();
    }

    /**
      * Processes an instruction that pops an array reference from the operand
      * stack and pushes its length.
      *
      */
    private void opc_arraylength() {
        StackProducer array = frame.pop(Klass.OBJECT);
        if (array.getType() !=Klass.NULL && !array.getType().isArray()) {
            throw codeParser.verifyError("invalid arraylength");
        }
        ArrayLength instruction = new ArrayLength(array);
        append(instruction);
    }

    /**
     * Processes an instruction that pops an object from the operand stack,
     * casts it to specified type and pushes it back to the stack.
     *
     * @param to  the type the object is cast to
     */
    private void opc_checkcast(Klass to) {
        frame.growMaxStack(1); // for the class object
        StackProducer object = frame.pop(Klass.OBJECT);
        CheckCast instruction = new CheckCast(to, object);
        append(instruction);
        frame.resetMaxStack();
    }

    /**
     * Processes an instruction that pops an object from the operand stack,
     * tests to see if it is assignable to a specified type and pushes the
     * boolean result of the test back to the stack.
     *
     * @param checkType  the type the object is being tested against
     */
    private void opc_instanceof(Klass checkType) {
        frame.growMaxStack(1); // for the class object
        StackProducer object = frame.pop(Klass.OBJECT);
        InstanceOf instruction = new InstanceOf(checkType, object);
        append(instruction);
        frame.resetMaxStack();
    }

    /**
     * Processes an instruction that creates a multi dimensional array
     * initializing at least the first dimension.
     *
     * @param type        the type of the array
     * @param dimensions  the number of dimensions to be initialized
     */
    private void opc_multianewarray(Klass type, int dimensions) {
        if (!type.isArray()) {
            throw codeParser.verifyError("invalid type for multianewarray");
        }
        if (dimensions < 1 || dimensions > Translator.countArrayDimensions(type.getInternalName())) {
            throw codeParser.verifyError("invalid dimensions for multianewarray");
        }

        StackProducer[] dimensionLengths = new StackProducer[dimensions];

        if (dimensions == 1) {
            dimensionLengths[0] = frame.pop(Klass.INT);
        } else {
            for (int i = dimensions - 1 ; i >= 0 ; i--) {
                dimensionLengths[i] = frame.pop(Klass.INT);

                /*
                 * Each dimension must be spilt
                 */
                frame.spill(dimensionLengths[i]);
            }
        }

        /*
         * Create a 'NewArray' for the first dimension
         */
        NewArray instruction = new NewArray(type, dimensionLengths[0]);
        append(instruction);

        /*
         * Create 'NewDimension's for the remaining dimensions.
         */
        for (int i = 1 ; i < dimensionLengths.length ; i++) {
            NewDimension newDimension = new NewDimension(frame.pop(type), dimensionLengths[i]);
            append(newDimension);
        }
    }
}
