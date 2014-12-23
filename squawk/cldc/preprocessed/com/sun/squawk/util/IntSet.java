/*
 * Copyright 1995-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.util;

import com.sun.cldchi.jvm.JVM;



/**
 * The <code>IntSet</code> class is an unsynchronized set of integers.
 * 
 * The main operations are add, remove, size, and getInts.
 *
 */
public final class IntSet {

    /**
     * The array buffer into which the components of the vector are
     * stored. The capacity of the vector is the length of this array buffer.
     */
    protected int elementData[];

    /**
     * The number of valid components in the vector.
     */
    protected int elementCount;

    /**
     * Constructs an empty vector with the specified initial capacity and
     * capacity increment.
     *
     * @param   initialCapacity     the initial capacity of the vector.
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    public IntSet(int initialCapacity) {
        super();
        if (initialCapacity < 0) {
            throw new IllegalArgumentException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Illegal Capacity: "+ initialCapacity
/* #endif */
            );
        }
        this.elementData = new int[initialCapacity];
    }

    /**
     * Constructs an empty vector.
     */
    public IntSet() {
        this(10);
    }

    /**
     * Returns the underlying int array. Only iterate over 
     * first size() elements.
     * @return underlying int array
     */
    public int[] getElements() {
        return elementData;
    }

    /**
     * This implements the unsynchronized semantics of ensureCapacity.
     * Synchronized methods in this class can internally call this
     * method for ensuring capacity without incurring the cost of an
     * extra synchronization.
     *
     * @see java.util.Vector#ensureCapacity(int)
     */
    private void ensureCapacityHelper(int minCapacity) {
        int oldCapacity = elementData.length;
        int oldData[] = elementData;
        int newCapacity = oldCapacity * 2;
        if (newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        elementData = new int[newCapacity];
        JVM.unchecked_int_arraycopy(oldData, 0, 
                                    elementData, 0, elementCount);
    }

    /**
     * Returns the number of components in this set.
     *
     * @return  the number of components in this set.
     */
    public final int size() {
        return elementCount;
    }

    /**
     * Tests if the specified object is a component in this vector.
     *
     * @param   elem   an object.
     * @return  <code>true</code> if the specified object is a component in
     *          this vector; <code>false</code> otherwise.
     */
    public final boolean contains(int elem) {
        return indexOf(elem, 0) >= 0;
    }

    /**
     * Searches for the first occurrence of the given argument, beginning
     * the search at <code>index</code>, and testing for equality using
     * the <code>equals</code> method.
     *
     * @param   elem    a value.
     * @param   index   the index to start searching from.
     * @return  the index of the first occurrence of the object argument in
     *          this vector at position <code>index</code> or later in the
     *          vector; returns <code>-1</code> if the object is not found.
     * @see     java.lang.Object#equals(java.lang.Object)
     */
    private int indexOf(int elem, int index) {
        for (int i = index ; i < elementCount ; i++) {
            if (elem == elementData[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds the specified int to this set,,
     * increasing its size by one.
     *
     * @param   val   the component to be added.
     */
    public void add(int val) {
        int newcount = elementCount + 1;
        if (newcount > elementData.length) {
            ensureCapacityHelper(newcount);
        }
        elementData[elementCount++] = val;
    }

    /**
     * Removes the first occurrence of the argument from this set.
     *
     * @param   val   the component to be removed.
     * @return  <code>true</code> if the argument was a component of this
     *          set; <code>false</code> otherwise.
     */
    public boolean remove(int val) {
        int i = indexOf(val, 0);
        if (i >= 0) {
            int j = elementCount - i - 1;
            if (j > 0) {
                JVM.unchecked_int_arraycopy(elementData, i + 1,
                        elementData, i, j);
            }
            elementCount--;
            return true;
        }
        return false;
    }

    /**
     * Removes all components from this vector and sets its size to zero.
     */
    public void removeAll() {
        elementCount = 0;
    }

}

