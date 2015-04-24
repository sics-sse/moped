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
import com.sun.squawk.*;

/**
 * This class represents the type of the value pushed to the operand stack
 * by a <i>new</i> bytecode before the corresponding constructor is
 * called on it.
 *
 */
public final class UninitializedObjectClass extends Klass {

    /**
     * The class specified by the operand of the <i>new</i> bytecode.
     */
    private Klass initializedType;

    /**
     * Creates a new <code>UninitializedObjectClass</code> instance to
     * represent the type of the value pushed on the operand stack by a
     * <i>new</i> bytecode. The name must be <code>"new@"</code> with the
     * address of the <i>new</i> bytecode appended. For example:<p>
     * <p><blockquote><pre>
     *     "new@45"
     * </pre></blockquote><p>
     *
     * @param name            the name of the type
     * @param initializedType the class specified by the operand of the
     *                        <i>new</i> bytecode (may be null)
     */
    public UninitializedObjectClass(String name, Klass initializedType) {
        super(name, Klass.UNINITIALIZED_NEW);
        this.initializedType = initializedType;
    }

    /**
     * Determines if the initialized type has been set. The value will not have
     * been set if this instance is the result of a
     * <code>ITEM_Uninitialized</code> entry in a stack map.
     *
     * @return  true if the initialized type has been set
     * @see     StackMap
     */
    public boolean hasInitializedTypeBeenSet() {
        return initializedType != null;
    }

    /**
     * Updates the initialized type. This must only be called once per instance
     * of <code>UninitializedObjectClass</code>.
     *
     * @param initializedType  the class specified by the operand of the
     *                         <i>new</i> bytecode
     * @see   #hasInitializedTypeBeenSet()
     */
    public void setInitializedType(Klass initializedType) {
        Assert.that(this.initializedType == null, "cannot change initialized type");
        this.initializedType = initializedType;
    }

    /**
     * Gets the class specified by the operand of the <i>new</i> bytecode.
     *
     * @return the class specified by the operand of the <i>new</i> bytecode
     */
    public Klass getInitializedType() {
        Assert.that(initializedType != null, "initialized type not yet set");
        return initializedType;
    }

    /**
     * {@inheritDoc}
     */
//    public int getClassID() {
//        Assert.that(initializedType != null);
//        return initializedType.getClassID();
//    }

}
