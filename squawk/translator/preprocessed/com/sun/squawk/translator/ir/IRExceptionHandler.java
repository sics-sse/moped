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

import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.ir.instr.*;

/**
 * An instance of <code>ExceptionHandler</code> represents an exception
 * handler expressed in terms of IR instructions.
 */
public class IRExceptionHandler {

    /**
     * The instruction denoting the position in the IR at which this handler
     * becomes active.
     */
    private Try entry;

    /**
     * The instruction denoting the position in the IR at which this handler
     * becomes deactive.
     */
    private TryEnd exit;

    /**
     * The target (and stack map) representing the address of the entry to
     * the exception handler.
     */
    private final Target target;

    /**
     * Constructor should only be called by an instance of
     * <code>IRBuilder</code>.
     *
     * @param target  the object encapsulating the stack map for the address of
     *                the entry to this exception handler
     */
    IRExceptionHandler(Target target) {
        this.target = target;
    }


    /*---------------------------------------------------------------------------*\
     *                                   Setters                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Sets the instruction denoting the position in the IR at which
     * this handler becomes active.<p>
     *
     * <b>This method should only be called by an instance of
     * <code>IRBuilder</code>.</b>
     *
     * @param entry  the instruction denoting the position in the IR
     *               at which this handler becomes active
     */
    void setEntry(Try entry) {
        Assert.that(this.entry == null, "cannot reset entry");
        this.entry = entry;
    }

    /**
     * Sets the instruction denoting the position in the IR at which
     * this handler becomes deactive.<p>
     *
     * <b>This method should only be called by an instance of
     * <code>IRBuilder</code>.</b>
     *
     * @param exit   the instruction denoting the position in the IR
     *               at which this handler becomes deactive
     */
    void setExit(TryEnd exit) {
        Assert.that(this.exit == null, "cannot reset exit");
        this.exit = exit;
    }


    /*---------------------------------------------------------------------------*\
     *                                   Getters                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the instruction denoting the position in the IR at which
     * this handler becomes active.<p>
     *
     * @return  the instruction denoting the position in the IR
     *          at which this handler becomes active
     */
    public Try getEntry() {
        return entry;
    }

    /**
     * Gets the instruction denoting the position in the IR at which
     * this handler becomes deactive.<p>
     *
     * @return  the instruction denoting the position in the IR
     *          at which this handler becomes deactive
     */
    public TryEnd getExit() {
        return exit;
    }

    /**
     * Gets the instruction denoting the position in the IR at which the
     * code for this handler starts.
     *
     * @return  the entry position of this handler's code
     */
    public Catch getCatch() {
        return target.getCatch();
    }

    /**
     * Gets the object encapsulating the stack map for the address of
     * the entry to this exception handler
     *
     * @return the object encapsulating the stack map for the address of
     *         the entry to this exception handler
     */
    public Target getTarget() {
        return target;
    }
}
