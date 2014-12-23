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

import com.sun.squawk.util.Assert;


/**
 * An Object association is the logical extension of an object that is used
 * to hold rarely used information like the monitor and hashcode.
 * This data structure is placed between an object and its class when
 * a hash code or monitor is needed for the object. The first two words
 * of this data structure match exactly the first two in com.sun.squawk.Klass
 */
public final class ObjectAssociation {

    /**
     * The klass of the object for which this is the association.
     * *** This must be the first instance variable to match the first variable in com.sun.squawk.Klass ***
     */
    private Klass klass;

    /**
     * The copy of the vtable of the target class. This is used to speed up virtual method dispatching.
     * *** This must be the second instance variable to match the second variable in com.sun.squawk.Klass ***
     */
    private Object[] virtualMethods;

    /**
     * The monitor for the object
     */
    private Monitor monitor;

    /**
     * The hashcode the object
     */
    private int hashCode;

    /**
     * Constructor.
     *
     * @param klass the klass of the object requiring an ObjectAssociation
     */
    ObjectAssociation(Klass klass) {
        this.klass          = klass;
        this.virtualMethods = klass.getVirtualMethods();
    }

    /**
     * Set the monitor.
     *
     * @param monitor the monitor
     */
    void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Get the monitor.
     *
     * @return the monitor
     */
    Monitor getMonitor() {
        Assert.that(monitor == null || monitor instanceof Monitor);
        return monitor;
    }

    /**
     * Get the hashcode.
     *
     * @return the hashcode
     */
    int getHashCode() {
        if (hashCode == 0) {
            hashCode = VM.getNextHashcode();
        }
        Assert.that(hashCodeInUse());
        return hashCode;
    }

    /**
     * Test to see if the hash code was used.
     *
     * @return true if is was
     */
    boolean hashCodeInUse() {
        return hashCode != 0;
    }

}
