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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class implements an unsynchronized hashtable, which maps keys to values. Any
 * non-<code>null</code> object can be used as a value, but only primitive integers (ints) can be used as keys.
 * <p>
 * An instance of <code>IntHashtable</code> has two parameters that
 * affect its efficiency: its <i>capacity</i> and its <i>load
 * factor</i>. The load factor is 0.75. When
 * the number of entries in the hashtable exceeds the product of the
 * load factor and the current capacity, the capacity is increased by
 * calling the <code>rehash</code> method.
 * <p>
 * If many entries are to be made into a <code>IntHashtable</code>,
 * creating it with a sufficiently large capacity may allow the
 * entries to be inserted more efficiently than letting it perform
 * automatic rehashing as needed to grow the table.
 * <p>
 * This example creates a hashtable of numbers. It uses the int value of
 * the numbers as keys:
 * <p><blockquote><pre>
 *     IntHashtable numbers = new IntHashtable();
 *     numbers.put(1, new Integer(1));
 *     numbers.put(2, new Integer(2));
 *     numbers.put(3, new Integer(3));
 * </pre></blockquote>
 * <p>
 * To retrieve a number, use the following code:
 * <p><blockquote><pre>
 *     Integer n = (Integer)numbers.get(2);
 *     if (n != null) {
 *         System.out.println("two = " + n);
 *     }
 * </pre></blockquote>
 * <p>
 * Note: To conserve space, the CLDC implementation
 * is based on JDK 1.1.8, not JDK 1.3.
 *
 * @version 1.42, 07/01/98 (CLDC 1.0, Spring 2000)
 * @see     java.lang.Object#equals(java.lang.Object)
 * @see     java.lang.Object#hashCode()
 * @see     IntHashtable#rehash()
 * @see     java.util.Hashtable
 */
public class IntHashtable {

    /**
     * The hash table data.
     */
    protected IntHashtableEntry table[];

    /**
     * The total number of entries in the hash table.
     */
    protected int count;

    /**
     * Rehashes the table when count exceeds this threshold.
     */
    protected int threshold;

    /**
     * The load factor for the hashtable.
     */
    private static final int loadFactorPercent = 75;

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity.
     *
     * @param      initialCapacity   the initial capacity of the hashtable.
     * @exception  IllegalArgumentException  if the initial capacity is less
     *             than zero
     * @since      JDK1.0
     */
    public IntHashtable(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
        table = new IntHashtableEntry[initialCapacity];
        threshold = (int)((initialCapacity * loadFactorPercent) / 100);
    }

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor.
     *
     * @since   JDK1.0
     */
    public IntHashtable() {
        this(11);
    }

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     * @since   JDK1.0
     */
    public final int size() {
        return count;
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return  <code>true</code> if this hashtable maps no keys to values;
     *          <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public final boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns an enumeration of the keys in this hashtable.
     *
     * @return  an enumeration of the keys in this hashtable.
     * @see     java.util.Enumeration
     * @see     java.util.Hashtable#elements()
     * @since   JDK1.0
     */
    public Enumeration keys() {
        return new HashtableEnumerator(table, true);
    }

    /**
     * Returns an enumeration of the values in this hashtable.
     * Use the Enumeration methods on the returned object to fetch the elements
     * sequentially.
     *
     * @return  an enumeration of the values in this hashtable.
     * @see     java.util.Enumeration
     * @see     java.util.Hashtable#keys()
     * @since   JDK1.0
     */
    public Enumeration elements() {
        return new HashtableEnumerator(table, false);
    }

    /**
     * Visitor
     *
     * @param visitor the visitor object
     */
     public void visit(IntHashtableVisitor visitor) {
        int index = table.length;
        IntHashtableEntry entry = null;
        for (;;) {
            if (entry == null) {
                while ((index-- > 0) && ((entry = table[index]) == null));
            }
            if (entry == null) {
                break;
            }
            visitor.visitIntHashtable(entry.key, entry.value);
            entry = entry.next;
        }
     }

    /**
     * Tests if some key maps into the specified value in this hashtable.
     * This operation is more expensive than the <code>containsKey</code>
     * method.
     *
     * @param      value   a value to search for.
     * @return     <code>true</code> if some key maps to the
     *             <code>value</code> argument in this hashtable;
     *             <code>false</code> otherwise.
     * @exception  NullPointerException  if the value is <code>null</code>.
     * @see        java.util.Hashtable#containsKey(java.lang.Object)
     * @since      JDK1.0
     */
    public boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        IntHashtableEntry tab[] = table;
        for (int i = tab.length ; i-- > 0 ;) {
            for (IntHashtableEntry e = tab[i] ; e != null ; e = e.next) {
                if (e.value.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tests if the specified object is a key in this hashtable.
     *
     * @param   key   possible key.
     * @return  <code>true</code> if the specified object is a key in this
     *          hashtable; <code>false</code> otherwise.
     * @see     java.util.Hashtable#contains(java.lang.Object)
     * @since   JDK1.0
     */
    public boolean containsKey(int key) {
         if (get(key) != null) {
             return true;
         }
         return false;
    }

    /**
     * Returns the value to which the specified key is mapped in this hashtable.
     *
     * @param   key   a key in the hashtable.
     * @return  the value to which the key is mapped in this hashtable;
     *          <code>null</code> if the key is not mapped to any value in
     *          this hashtable.
     * @see     java.util.Hashtable#put(java.lang.Object, java.lang.Object)
     * @since   JDK1.0
     */
    public Object get(int key) {
        IntHashtableEntry tab[] = table;
        int index = (key & 0x7FFFFFFF) % tab.length;
        for (IntHashtableEntry e = tab[index] ; e != null ; e = e.next) {
            if (e.key == key) {
                return e.value;
            }
        }
        return null;
    }

    /**
     * Rehashes the contents of the hashtable into a hashtable with a
     * larger capacity. This method is called automatically when the
     * number of keys in the hashtable exceeds this hashtable's capacity
     * and load factor.
     *
     * @since   JDK1.0
     */
    protected void rehash() {
        int oldCapacity = table.length;
        IntHashtableEntry oldTable[] = table;

        int newCapacity = oldCapacity * 2 + 1;
        IntHashtableEntry newTable[] = new IntHashtableEntry[newCapacity];

        threshold = (int)((newCapacity * loadFactorPercent) / 100);
        table = newTable;

        for (int i = oldCapacity ; i-- > 0 ;) {
            for (IntHashtableEntry old = oldTable[i] ; old != null ; ) {
                IntHashtableEntry e = old;
                old = old.next;

                int index = (e.key & 0x7FFFFFFF) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>.
     * <p>
     * The value can be retrieved by calling the <code>get</code> method
     * with a key that is equal to the original key.
     *
     * @param      key     the hashtable key.
     * @param      value   the value.
     * @return     the previous value of the specified key in this hashtable,
     *             or <code>null</code> if it did not have one.
     * @exception  NullPointerException  if the key or value is
     *               <code>null</code>.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable#get(java.lang.Object)
     * @since   JDK1.0
     */
    public Object put(int key, Object value) {
        // Make sure the value is not null
        if (value == null) {
            throw new NullPointerException();
        }

        // Makes sure the key is not already in the hashtable.
        IntHashtableEntry tab[] = table;
        int index = (key & 0x7FFFFFFF) % tab.length;
        for (IntHashtableEntry e = tab[index] ; e != null ; e = e.next) {
            if (e.key == key) {
                Object old = e.value;
                e.value = value;
                return old;
            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            rehash();
            tab = table;
            index = (key & 0x7FFFFFFF) % tab.length;
        }

        // Creates the new entry.
        IntHashtableEntry e = new IntHashtableEntry();
        e.key = key;
        e.value = value;
        e.next = tab[index];
        tab[index] = e;
        count++;
        return null;
    }

    /**
     * Removes the key (and its corresponding value) from this
     * hashtable. This method does nothing if the key is not in the hashtable.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the key had been mapped in this hashtable,
     *          or <code>null</code> if the key did not have a mapping.
     * @since   JDK1.0
     */
    public Object remove(int key) {
        IntHashtableEntry tab[] = table;
        int index = (key & 0x7FFFFFFF) % tab.length;
        for (IntHashtableEntry e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
            if (e.key == key) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                count--;
                return e.value;
            }
        }
        return null;
    }

    /**
     * Clears this hashtable so that it contains no keys.
     *
     * @since   JDK1.0
     */
    public void clear() {
        IntHashtableEntry tab[] = table;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        count = 0;
    }

    /**
     * Returns a rather long string representation of this hashtable.
     *
     * @return  a string representation of this hashtable.
     * @since   JDK1.0
     */
    public String toString() {
        return SquawkHashtable.enumerationsToString(keys(), elements(), size());
    }

    /**
     * A hashtable enumerator class.  This class should remain opaque
     * to the client. It will use the Enumeration interface.
     */
    static class HashtableEnumerator implements Enumeration {
        boolean keys;
        int index;
        IntHashtableEntry table[];
        IntHashtableEntry entry;

        HashtableEnumerator(IntHashtableEntry table[], boolean keys) {
            this.table = table;
            this.keys = keys;
            this.index = table.length;
        }

        public boolean hasMoreElements() {
            if (entry != null) {
                return true;
            }
            while (index-- > 0) {
                if ((entry = table[index]) != null) {
                    return true;
                }
            }
            return false;
        }

        public Object nextElement() {
            if (entry == null) {
                while ((index-- > 0) && ((entry = table[index]) == null));
            }
            if (entry != null) {
                IntHashtableEntry e = entry;
                entry = e.next;
                return keys ? new Integer(e.key) : e.value;
            }
            throw new NoSuchElementException();
        }
    }
}

/**
 * Hashtable collision list.
 */
class IntHashtableEntry {
    int key;
    Object value;
    IntHashtableEntry next;
}
