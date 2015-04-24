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
import com.sun.squawk.translator.ir.*;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.*;

/**
 * An instance of <code>LocalVariableTableEntry</code> represents a single entry
 * in a "LocalVariableTable" class file attribute.
 *
 * @see   <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#5956">
 *         The Java Virtual Machine Specification - Second Edition</a>
 */
public class LocalVariableTableEntry {

    /**
     * The position in the instruction sequence at which the scope of the
     * variable starts.
     */
    private final Position start;

    /**
     * The position in the instruction sequence at which the scope of the
     * variable ends.
     */
    private final Position end;

    /**
     * The name of the local variable.
     */
    private final String name;

    /**
     * The type of the local variable.
     */
    private final Klass type;

    /**
     * The index of the local variable in the local variable array for the
     * associated method.
     */
    private final int index;

    /**
     * The IR representation of the local variable.
     */
    private Local local;

    /**
     * Creates a new <code>LocalVariableTableEntry</code> instance.
     *
     * @param  start    the position in the instruction sequence at which the
     *                  scope of the variable starts
     * @param  end      the position in the instruction sequence at which the
     *                  scope of the variable ends
     * @param  name     the name of the variable
     * @param  type     the type of the variable
     * @param  index    the index of the local variable
     */
    LocalVariableTableEntry(Position start, Position end, String name, Klass type, int index) {
        this.start = start;
        this.end   = end;
        this.name  = name;
        this.type  = type;
        this.index = index;
    }

    /**
     * Gets the name of the local variable represented by this entry.
     *
     * @return the name of the local variable represented by this entry
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of the local variable represented by this entry.
     *
     * @return the type of the local variable represented by this entry
     */
    public Klass getType() {
        return type;
    }

    /**
     * Sets the <code>Local</code> instance corresponding to the variable
     * described by this entry.
     *
     * @param local  the <code>Local</code> instance corresponding this variable
     */
    public void setLocal(Local local) {
        Assert.that(this.local == null || this.local == local, "cannot overwrite local");
        this.local = local;
    }

    /**
     * Gets the index of this variable in the original JVM local variable array.
     *
     * @return  the index of the local variable
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the logical slot index of this variable.
     *
     * @param   the number of slots occupied by the parameters
     * @return  the logical slot index of the local variable or -1 if the scope
     *          represented by this entry does not use the variable
     */
    public int getSlot(int paramSlotCount) {
        if (local == null) {
            // This entry is for a scope in which the local variable is
            // visible but never actually used. The value for this variable
            // within the scope may not preserved by Squawk.
            return -1;
        } else if (local.isParameter()) {
            return local.getSquawkParameterIndex();
        } else {
            return local.getSquawkLocalIndex() + paramSlotCount;
        }
    }

    /**
     * Gets the position in the instruction sequence at which the scope of the
     * variable starts.
     *
     * @return  the starting position of this local's scope
     */
    public Position getStart() {
        return start;
    }

    /**
     * Gets the position in the instruction sequence at which the scope of the
     * variable ends.
     *
     * @return  the ending position of this local's scope
     */
    public Position getEnd() {
        return end;
    }

    /**
     * Determines if a given variable index and current instruction address
     * correspond with this local variable.
     *
     * @param  index    a local variable index
     * @param  address  an instruction address
     * @return  true if <code>index</code> equals the index of this local
     *                  variable and <code>address</code> falls within the
     *                  range of code covered by this local variable
     */
    public boolean matches(int index, int address) {
        return this.index == index && this.start.getBytecodeOffset() <= address && address < this.end.getBytecodeOffset();
    }
}
