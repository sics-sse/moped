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

package com.sun.squawk.debugger.sda;

import java.lang.ref.*;
import java.util.*;

import com.sun.squawk.util.*;
import com.sun.squawk.util.SquawkVector;

/**
 * This class extends an {@link IntHashtable} in two important ways:
 *
 * 1. It wraps all inserted values in a WeakReference.
 * 2. It compares all (wrapped) values using reference equality as opposed to {@link Object#equals}.
 *
 */
public final class WeakIntHashtable extends IntHashtable {

    /**
     * Constructs a new, empty hashtable with a default capacity and load factor.
     */
    public WeakIntHashtable() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses reference equality when comparing values.
     */
    public boolean contains(Object value) {
        return (getKey(value) != null);
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>.
     * <p>
     * If <code>value</code> is an instanceof {@link WeakReference}, then it is
     * directly inserted in the table otherwise <code>value</code> is wrapped
     * in a <code>WeakReference</code> before being inserted. However, value
     * retrieved by calling the <code>get</code> method will always be the
     * value within the WeakReference.
     *
     * @param key    the key
     * @param value  the value
     * @return the previous value mapped by <code>key</code>
     * @throws IllegalArgumentException if <code>value instanceof WeakReference</code> and
     *         the referent is a WeakReference itself
     */
    public Object put(int key, Object value) throws IllegalArgumentException {
        if (value == null) {
            throw new NullPointerException();
        }
        WeakReference ref = new WeakReference(value);

        ref = (WeakReference)super.put(key, ref);
        if (ref != null) {
            value = ref.get();
            Assert.always(!(value instanceof WeakReference));
            return value;
        } else {
            return null;
        }
    }

    /**
     * Removes all the entries whose wrapped objects have been collected or cleared.
     */
    public void compact() {
        Vector deadEntries = new Vector();
        Enumeration keys = this.keys();
        Enumeration values = this.elements();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            WeakReference ref = (WeakReference) values.nextElement();
            if (ref.get() == null) {
                deadEntries.addElement(key);
            }
        }

        keys = deadEntries.elements();
        while (keys.hasMoreElements()) {
            int key = ((Integer)keys.nextElement()).intValue();
            this.remove(key);
        }
    }

    /**
     * Returns the value to which <code>key</code> is mapped in this hashtable.
     * This will return null if <code>key</code> does not map to a value or the value
     * has been garbage collected.
     *
     * @param key  the key to search for
     * @return  the value corresponding to <code>key</code> or null
     */
    public Object get(int key) {
        WeakReference ref = (WeakReference)super.get(key);
        if (ref != null) {
            Object value = ref.get();
            if (value == null) {
                // Remove the entry from the table if the object has been collected
                super.remove(key);
            }
            //Assert.always(!(value instanceof WeakReference)); // an attempt to cut down on recerive refs, but might happen?
            return value;
        } else {
            return null;
        }
    }

    /**
     * Gets the key for a given value in this table. Values in the tables are compared
     * against <code>value</code> using reference equality as opposed to {@link Object#equals}.
     *
     * @param value  the value for which the key is requested
     * @return  the key for <code>value</code> or null if <code>value</code> is not in this table
     */
    public Integer getKey(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

        Enumeration keys = this.keys();
        Enumeration values = this.elements();

        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            WeakReference ref = (WeakReference)values.nextElement();
            Object v = ref.get();
            if (value == v) {
                return (Integer)key;
            }
        }
        return null;
    }
}
