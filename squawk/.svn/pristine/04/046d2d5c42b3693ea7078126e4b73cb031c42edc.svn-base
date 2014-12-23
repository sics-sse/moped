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
import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.*;
import com.sun.squawk.translator.ci.CodeParser;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.*;    // Version without synchronization

/**
 * This class encapsultates the intermediate representation for a single
 * method. The instructions in the IR are stored in a sequence.<p>
 *
 */
public final class IR {

    /**
     * A constant empty array of targets.
     */
    private final static Target[] NO_TARGETS = {};

    /**
     * The first instruction in the IR.
     */
    private Instruction head;

    /**
     * The last instruction in the IR.
     */
    private Instruction tail;

    /**
     * The ordered set of {@link ExceptionHandler} instances comprising the
     * exception handler table for this IR.
     */
    private SquawkVector exceptionHandlers;

    /**
     * The targets (i.e. all basic block entries except the first) in the method.
     */
    private Target[] targets = NO_TARGETS;

    /**
     * Creates a new <code>IR</code>.
     */
    public IR() {}

    /*---------------------------------------------------------------------------*\
     *                            Instruction list                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Searches for an instruction in the list of instructions.
     *
     * @param   instruction  the instruction to search for
     * @return  true if <code>instruction</code> is found
     */
    private boolean findInstruction(Instruction instruction) {
        for (Instruction i = head; i != null; i = i.next) {
            if (instruction == i) {
                return true;
            }
        }
        return false;
    }

    /**
     * Append an instruction to the end of the instruction sequence represented
     * by this IR. The instruction sequence must not already contain
     * <code>instruction</code>.
     *
     * @param instruction  the instruction to append
     */
    public void append(Instruction instruction) {
        if (head == null) {
            Assert.that(tail == null);
            head = tail = instruction;
        } else {
            Assert.that(tail != null);
            Assert.that(!findInstruction(instruction), "instruction cannot be in IR twice");
            tail.next = instruction;
            instruction.previous = tail;
            instruction.next = null;
            tail = instruction;
        }
//Assert.that(findInstruction(instruction));
//Assert.that(findInstruction(head));
//Assert.that(findInstruction(tail));
    }

    /**
     * Prepend an instruction to the start of the instruction sequence
     * represented by this IR. The instruction sequence must not already contain
     * <code>instruction</code>.
     *
     * @param instruction  the instruction to prepend
     */
    public void prepend(Instruction instruction) {
        if (head == null) {
            Assert.that(tail == null);
            head = tail = instruction;
        } else {
            Assert.that(tail != null);
            Assert.that(!findInstruction(instruction), "instruction cannot be in IR twice");
            instruction.next = head;
            head.previous = instruction;
            head = instruction;
        }
        tail = instruction;
    }

    /**
     * Insert an instruction into the instruction sequence represented by this
     * IR. The instruction sequence must not already contain
     * <code>instruction</code> but must contain <code>pos</code>.
     *
     * @param instruction  the instruction to be inserted
     * @param pos          the instruction that <code>instruction</code> will be
     *                     inserted after
     */
    public void insertAfter(Instruction instruction, Instruction pos) {
        Assert.that(!findInstruction(instruction));
        Assert.that(findInstruction(pos));
        if (pos == tail) {
            tail = instruction;
        } else {
            pos.next.previous = instruction;
        }
        instruction.next = pos.next;
        instruction.previous = pos;
        pos.next = instruction;
    }

    /**
     * Insert an instruction into the instruction sequence represented by this
     * IR. The instruction sequence must not already contain
     * <code>instruction</code> but must contain <code>pos</code>.
     *
     * @param instruction the instruction to be inserted
     * @param pos         the instruction that <code>instruction</code> will be
     *                    inserted before
     */
    public void insertBefore(Instruction instruction, Instruction pos) {
        Assert.that(!findInstruction(instruction));
        Assert.that(findInstruction(pos));
        if (pos == head) {
            head = instruction;
        } else {
            pos.previous.next = instruction;
        }
        instruction.previous = pos.previous;
        instruction.next = pos;
        pos.previous = instruction;
    }

    /**
     * Remove an instruction from the instruction sequence represented by this
     * IR. The instruction sequence must already contain
     * <code>instruction</code>.
     *
     * @param instruction  the instruction to be removed
     */
    public void remove(Instruction instruction) {
        Assert.that(findInstruction(instruction));
        if (instruction == head) {
            head = instruction.next;
        }
        if (tail == instruction) {
            tail = instruction.previous;
        }
        if (instruction.previous != null) {
            instruction.previous.next = instruction.next;
        }
        if (instruction.next != null) {
            instruction.next.previous = instruction.previous;
        }
        instruction.next = instruction.previous = null;
//Assert.that(!findInstruction(instruction));
//Assert.that(findInstruction(head));
//Assert.that(findInstruction(tail));
    }

    /**
     * Gets the first instruction in the IR.
     *
     * @return the first instruction in the IR
     */
    public Instruction getHead() {
        return head;
    }

    /**
     * Gets the last instruction in the IR.
     *
     * @return the last instruction in the IR
     */
    public Instruction getTail() {
        return tail;
    }
    
    /**
     * Calculate number of instructions in IR.
     * @return number of IR nodes
     */
    public int size() {
        Instruction instr = head;
        int n = 0;
        while (instr != null) {
            instr = instr.getNext();
            n++;
        }
        return n;
    }

    /*---------------------------------------------------------------------------*\
     *                                Targets                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Sets the targets of the method.
     *
     * @param targets the targets of the method
     */
    public void setTargets(Target[] targets) {
        this.targets = targets;
    }

    /**
     * Gets the targets of the method.
     *
     * @return the targets of the method
     */
    public Target[] getTargets() {
        return targets;
    }

    /*---------------------------------------------------------------------------*\
     *                             Exception handlers                            *
    \*---------------------------------------------------------------------------*/

    /**
     * Adds an exception handler to the exception handler table for this IR.
     *
     * @param handler  the exception handler to add
     */
    void addExceptionHandler(IRExceptionHandler handler) {
        if (exceptionHandlers == null) {
            exceptionHandlers = new SquawkVector();
        }
        exceptionHandlers.addElement(handler);
    }

    /**
     * Gets an enumeration over the exception handlers of this IR.
     *
     * @return an enumeration over the exception handlers of this IR or
     *         <code>null</code> if this IR has no exception handlers
     */
    public Enumeration getExceptionHandlers() {
        if (exceptionHandlers == null) {
            return null;
        }
        return exceptionHandlers.elements();
    }

    /*---------------------------------------------------------------------------*\
     *                          Instruction iteration                            *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets an enumeration over the sequence of instructions in this IR.
     * The returned enumeration is invalidated if the list of instructions is
     * modified in anyway.
     *
     * @param   reverse  if true, the returned enumerator traverses the
     *                   instructions in reveres order
     * @return  an enumeration over the instructions in this IR
     */
    public Enumeration getInstructions(final boolean reverse) {
        return new Enumeration() {
            Instruction instruction = reverse ? tail : head;
            public boolean hasMoreElements() {
                return instruction != null;
            }
            public Object nextElement() {
                if (instruction == null) {
                    throw new NoSuchElementException();
                }
                Instruction result = instruction;
                instruction = reverse ? instruction.previous : instruction.next;
                return result;
            }
        };
    }


    /*---------------------------------------------------------------------------*\
     *                         Bytecode transformation                           *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the transformed method represented by this IR. This can only be
     * called once the bytecode transformation has completed.
     *
     * @param method     the method owning this code
     * @param index      the index of this method in the symbols table of the enclosing class
     * @param codeParser the code parser for the method
     * @param locals     the types of the locals (but not the parameters)
     * @param clearedSlots the number of local variables (after the first one) that need clearing
     * @param maxStack   the maximum number of stack words required
     * @param classFile  the class file
     * @return           the method body represented by this IR
     */
    public MethodBody getMethodBody(
                                     Method     method,
                                     int        index,
                                     CodeParser codeParser,
                                     Klass[]    locals,
                                     int        clearedSlots,
                                     int        maxStack,
                                     ClassFile  classFile
                                   ) {
        /*
         * Emit the bytecodes.
         */
        InstructionEmitter emitter = new InstructionEmitter(this, classFile, method, clearedSlots);
        emitter.emit();
        byte[] code = emitter.getCode();
        byte[] typeMap = null;
/*if[TYPEMAP]*/
        typeMap = emitter.getTypeMap();
/*end[TYPEMAP]*/

        /*
         * Build exception handler table
         */
        ExceptionHandler[] exceptionTable = null;
        if (exceptionHandlers != null && !exceptionHandlers.isEmpty()) {
            int lth = exceptionHandlers.size();
            exceptionTable = new ExceptionHandler[lth];
            for (int i = 0 ; i < lth ; i++) {
                IRExceptionHandler irHandler = (IRExceptionHandler)exceptionHandlers.elementAt(i);
                exceptionTable[i] = new ExceptionHandler(irHandler.getEntry().getBytecodeOffset(),
                                                         irHandler.getExit().getBytecodeOffset(),
                                                         irHandler.getCatch().getExceptionBytecodeOffset(),
                                                         irHandler.getCatch().getType()
                                                         );
            }
        }

        /*
         * If the target method needs a CLASS_CLINIT then maxStack must be at least 1.
         */
        if (maxStack == 0 && method.requiresClassClinit()) {
            maxStack++;
        }

        /*
         * Create the method body
         */
        MethodBody body = new MethodBody(
                                          method,
                                          maxStack,
                                          locals,
                                          exceptionTable,
                                          codeParser.getLineNumberTable(code),
                                          codeParser.getLocalVariableTable(),
                                          code,
                                          typeMap,
                                          Translator.REVERSE_PARAMETERS,
                                          false
                                        );

        return body;
    }

}
