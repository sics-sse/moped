//if[NEW_IIC_MESSAGES]
/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.io.mailboxes;

/**
 * Interface for objects that may be passed in ObjectEnvelopes between Isolates.
 * The copyFrom method allows a class to specify how objects should be copied.
 *
 * Note that an ICopiable object should a public no-args constructor in order to be
 * copiable.
 *
 */
public interface ICopiable {
    
    /** 
     * Set the state of this object based on the state of object <code>o</code>.
     *
     * This method should be careful not to store pointers to either the original or copied
     * object. The copyFrom is likely to be called in the context of the sending Isolate, but 
     * this object is destined for use by the receiving Isolate.
     *
     * @param o the object the copy from
     */
    public void copyFrom(Object o);
}
