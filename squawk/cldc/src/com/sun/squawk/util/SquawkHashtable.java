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

import com.sun.squawk.GC;
import com.sun.squawk.VM;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class implements a variation of {@link java.util.Hashtable} that is unsynchronized. Any
 * non-<code>null</code> object can be used as a key or as a value.
 * <p>
 * To successfully store and retrieve objects from a hashtable, the
 * objects used as keys must implement the <code>hashCode</code>
 * method and the <code>equals</code> method.
 * 
 * <p>
 * An instance of <code>SquawkHashtable</code> has two parameters that
 * affect its efficiency: its <i>capacity</i> and its <i>load
 * factor</i>. The load factor is always 0.75. When
 * the number of entries in the hashtable exceeds the product of the
 * load factor and the current capacity, the capacity is increased by
 * calling the <code>rehash</code> method. Larger load factors use
 * memory more efficiently, at the expense of larger expected time
 * per lookup.
 * <p>
 * If many entries are to be made into a <code>SquawkHashtable</code>,
 * creating it with a sufficiently large capacity may allow the
 * entries to be inserted more efficiently than letting it perform
 * automatic rehashing as needed to grow the table.
 * <p>
 * This example creates a hashtable of numbers. It uses the names of
 * the numbers as keys:
 * <p><blockquote><pre>
 *     SquawkHashtable numbers = new SquawkHashtable();
 *     numbers.put("one", new Integer(1));
 *     numbers.put("two", new Integer(2));
 *     numbers.put("three", new Integer(3));
 * </pre></blockquote>
 * <p>
 * To retrieve a number, use the following code:
 * <p><blockquote><pre>
 *     Integer n = (Integer)numbers.get("two");
 *     if (n != null) {
 *         System.out.println("two = " + n);
 *     }
 * </pre></blockquote>
 * <p>
 * Note: To conserve space, the CLDC implementation
 * is based on JDK 1.1.8, not JDK 1.3.
 * 
 * @version 1.42, 07/01/98 (CLDC 1.0, Spring 2000)
 * @see java.lang.Object#equals(java.lang.Object)
 * @see java.lang.Object#hashCode()
 * @see java.util.Hashtable
 * @see #rehash()
 * @since JDK1.0, CLDC 1.0
 */
/*if[JAVA5SYNTAX]*/
public class SquawkHashtable<K, V> {
/*else[JAVA5SYNTAX]*/
//public class SquawkHashtable {
/*end[JAVA5SYNTAX]*/

    public static interface Rehasher {
        public void rehash();
    }

    /**
     * The hash table data. This field is zeroed when object de-serialization
     * occurs and is reformed from backupTable.
     */
/*if[JAVA5SYNTAX]*/
    private transient HashtableEntry<K, V>[] entryTable;
/*else[JAVA5SYNTAX]*/
//    private transient HashtableEntry[] entryTable;
/*end[JAVA5SYNTAX]*/

    /**
     * The backup hash table data.
     */
/*if[JAVA5SYNTAX]*/
    private HashtableEntry<K, V>[] backupTable;
/*else[JAVA5SYNTAX]*/
//    private HashtableEntry[] backupTable;
/*end[JAVA5SYNTAX]*/

    /**
     * The total number of entries in the hash table.
     */
    private int count;

    /**
     * Rehashes the table when count exceeds this threshold.
     */
    private int threshold;

    /**
     * This is used to ensure that the rehash() method in a subclass
     * java.util.SquawkHashtable is called appropriately.
     */
    private Rehasher rehasher;

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
     */
/*if[JAVA5SYNTAX]*/
    @SuppressWarnings("unchecked")
/*end[JAVA5SYNTAX]*/
    public SquawkHashtable(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        if (initialCapacity == 0) {
            initialCapacity = 1;
        }
        setTable(new HashtableEntry[initialCapacity]);
        threshold = ((initialCapacity * loadFactorPercent) / 100);
    }

    public final void setRehasher(Rehasher rehasher) {
        this.rehasher = rehasher;
    }

    /**
     * Set the hash table entries
     *
     * @param table the new array of hash table entries
     */
/*if[JAVA5SYNTAX]*/
    private void setTable(HashtableEntry<K, V>[] table) {
/*else[JAVA5SYNTAX]*/
//    private void setTable(HashtableEntry[] table) {
/*end[JAVA5SYNTAX]*/
        entryTable  = table;
        backupTable = table;
    }

    /**
     * Get the hash table entries
     *
     * @return the array of hash table entries
     */
/*if[JAVA5SYNTAX]*/
    private HashtableEntry<K, V>[] getTable() {
/*else[JAVA5SYNTAX]*/
//        private HashtableEntry[] getTable() {
/*end[JAVA5SYNTAX]*/
        if (entryTable == null) {
            rehash(backupTable, backupTable.length);
        }
        return entryTable;
    }

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor.
     */
    public SquawkHashtable() {
        this(11);
    }

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     */
    public final int size() {
        return count;
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return  <code>true</code> if this hashtable maps no keys to values;
     *          <code>false</code> otherwise.
     */
    public final boolean isEmpty() {
        return count == 0;
    }

    /**
     * Returns an enumeration of the keys in this hashtable.
     * 
     * @return an enumeration of the keys in this hashtable.
     * @see java.util.Enumeration
     * @see #elements()
     */
/*if[JAVA5SYNTAX]*/
    public Enumeration<K> keys() {
        return new HashtableEnumerator<K>(getTable(), true);
/*else[JAVA5SYNTAX]*/
//    public Enumeration keys() {
//        return new HashtableEnumerator(getTable(), true);
/*end[JAVA5SYNTAX]*/
    }

    /**
     * Returns an enumeration of the values in this hashtable.
     * Use the Enumeration methods on the returned object to fetch the elements
     * sequentially.
     * 
     * @return an enumeration of the values in this hashtable.
     * @see java.util.Enumeration
     * @see #keys()
     */
/*if[JAVA5SYNTAX]*/
    public Enumeration<V> elements() {
        return new HashtableEnumerator<V>(getTable(), false);
/*else[JAVA5SYNTAX]*/
//    public Enumeration elements() {
//        return new HashtableEnumerator(getTable(), false);
/*end[JAVA5SYNTAX]*/
    }

    /**
     * Tests if some key maps into the specified value in this hashtable.
     * This operation is more expensive than the <code>containsKey</code>
     * method.
     * 
     * @param value   a value to search for.
     * @return <code>true</code> if some key maps to the
     *             <code>value</code> argument in this hashtable;
     *             <code>false</code> otherwise.
     * @exception NullPointerException  if the value is <code>null</code>.
     * @see #containsKey(java.lang.Object)
     */
    public boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }

/*if[JAVA5SYNTAX]*/
        HashtableEntry<K, V> tab[] = getTable();
/*else[JAVA5SYNTAX]*/
//        HashtableEntry tab[] = getTable();
/*end[JAVA5SYNTAX]*/
        for (int i = tab.length ; i-- > 0 ;) {
/*if[JAVA5SYNTAX]*/
            for (HashtableEntry<K, V> e = tab[i] ; e != null ; e = e.next) {
/*else[JAVA5SYNTAX]*/
//        	for (HashtableEntry e = tab[i] ; e != null ; e = e.next) {
/*end[JAVA5SYNTAX]*/
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
     * @param key   possible key.
     * @return <code>true</code> if the specified object is a key in this
     *          hashtable; <code>false</code> otherwise.
     * @see #contains(java.lang.Object)
     */
    public boolean containsKey(Object key) {
         if (get(key) != null) {
             return true;
         }
         return false;
    }

    /**
     * Returns the value to which the specified key is mapped in this hashtable.
     * 
     * @param key   a key in the hashtable.
     * @return the value to which the key is mapped in this hashtable;
     *          <code>null</code> if the key is not mapped to any value in
     *          this hashtable.
     * @see #put(java.lang.Object, java.lang.Object)
     */
    public Object get(Object key) {
/*if[JAVA5SYNTAX]*/
        HashtableEntry<K, V> tab[] = getTable();
/*else[JAVA5SYNTAX]*/
//        HashtableEntry tab[] = getTable();
/*end[JAVA5SYNTAX]*/
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
/*if[JAVA5SYNTAX]*/
        for (HashtableEntry<K, V> e = tab[index] ; e != null ; e = e.next) {
/*else[JAVA5SYNTAX]*/
//        for (HashtableEntry e = tab[index] ; e != null ; e = e.next) {
/*end[JAVA5SYNTAX]*/
            if ((e.hash == hash) && e.key.equals(key)) {
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
     * This is not a public method, made it public to enable the com.sun.squawk.util.SquawkHashtable.rehash to delegate to this one.
     * 
     */
    public void rehash() {
/*if[JAVA5SYNTAX]*/
        HashtableEntry<K, V>[] oldTable = getTable();
/*else[JAVA5SYNTAX]*/
//        HashtableEntry[] oldTable = getTable();
/*end[JAVA5SYNTAX]*/
        rehash(oldTable, oldTable.length * 2 + 1);
    }

    /**
     * Rehashes the contents of the hashtable into a new hashtable.
     *
     * @param oldTable the HashtableEntry to be rehashed
     * @param newCapacity the size of the new table
     */
/*if[JAVA5SYNTAX]*/
    @SuppressWarnings("unchecked")
    private void rehash(HashtableEntry<K, V>[] oldTable, int newCapacity) {
/*else[JAVA5SYNTAX]*/
//    private void rehash(HashtableEntry[] oldTable, int newCapacity) {
/*end[JAVA5SYNTAX]*/
        setTable(null); // safety
        int oldCapacity = oldTable.length;
/*if[JAVA5SYNTAX]*/
        HashtableEntry<K, V>[] newTable = new HashtableEntry[newCapacity];
/*else[JAVA5SYNTAX]*/
//        HashtableEntry[] newTable = new HashtableEntry[newCapacity];
/*end[JAVA5SYNTAX]*/
        threshold = ((newCapacity * loadFactorPercent) / 100);
        for (int i = oldCapacity ; i-- > 0 ;) {
/*if[JAVA5SYNTAX]*/
            for (HashtableEntry<K, V> old = oldTable[i] ; old != null ; ) {
                HashtableEntry<K, V> e = old;
/*else[JAVA5SYNTAX]*/
//            for (HashtableEntry old = oldTable[i] ; old != null ; ) {
//                HashtableEntry e = old;
/*end[JAVA5SYNTAX]*/
                old = old.next;
                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
        setTable(newTable);
    }

    /**
     * Maps the specified <code>key</code> to the specified
     * <code>value</code> in this hashtable. Neither the key nor the
     * value can be <code>null</code>.
     * <p>
     * The value can be retrieved by calling the <code>get</code> method
     * with a key that is equal to the original key.
     * 
     * @param key     the hashtable key.
     * @param value   the value.
     * @return the previous value of the specified key in this hashtable,
     *             or <code>null</code> if it did not have one.
     * @exception NullPointerException  if the key or value is
     *               <code>null</code>.
     * @see java.lang.Object#equals(java.lang.Object)
     * @see #get(java.lang.Object)
     */
/*if[JAVA5SYNTAX]*/
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
/*else[JAVA5SYNTAX]*/
//    public Object put(Object key, Object value) {
/*end[JAVA5SYNTAX]*/
        // Make sure the value is not null
        if (value == null) {
            throw new NullPointerException();
        }

        // Makes sure the key is not already in the hashtable.
/*if[JAVA5SYNTAX]*/
        HashtableEntry<K, V> tab[] = getTable();
/*else[JAVA5SYNTAX]*/
//        HashtableEntry tab[] = getTable();
/*end[JAVA5SYNTAX]*/
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
/*if[JAVA5SYNTAX]*/
        for (HashtableEntry<K, V> e = tab[index] ; e != null ; e = e.next) {
/*else[JAVA5SYNTAX]*/
//        for (HashtableEntry e = tab[index] ; e != null ; e = e.next) {
/*end[JAVA5SYNTAX]*/
            if ((e.hash == hash) && e.key.equals(key)) {
                Object old = e.value;
                e.value = value;
/*if[JAVA5SYNTAX]*/
                return (V) old;
/*else[JAVA5SYNTAX]*/
//                return old;
/*end[JAVA5SYNTAX]*/
            }
        }

        if (count >= threshold) {
            // Rehash the table if the threshold is exceeded
            if (rehasher != null) {
                rehasher.rehash();
            } else {
                rehash();
            }
            tab = getTable();
            index = (hash & 0x7FFFFFFF) % tab.length;
        }

        // Creates the new entry.
/*if[JAVA5SYNTAX]*/
        HashtableEntry<K, V> e = new HashtableEntry<K, V>();
/*else[JAVA5SYNTAX]*/
//        HashtableEntry e = new HashtableEntry();
/*end[JAVA5SYNTAX]*/
        e.hash = hash;
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
     */
/*if[JAVA5SYNTAX]*/
    public V remove(K key) {
        HashtableEntry<K, V> tab[] = getTable();
/*else[JAVA5SYNTAX]*/
//        public Object remove(Object key) {
//            HashtableEntry tab[] = getTable();
/*end[JAVA5SYNTAX]*/
        int hash = key.hashCode();
        int index = (hash & 0x7FFFFFFF) % tab.length;
/*if[JAVA5SYNTAX]*/
        for (HashtableEntry<K, V> e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
/*else[JAVA5SYNTAX]*/
//        for (HashtableEntry e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
/*end[JAVA5SYNTAX]*/
            if ((e.hash == hash) && e.key.equals(key)) {
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
     */
    public void clear() {
/*if[JAVA5SYNTAX]*/
        HashtableEntry<K, V> tab[] = getTable();
/*else[JAVA5SYNTAX]*/
//        HashtableEntry tab[] = getTable();
/*end[JAVA5SYNTAX]*/
        for (int index = tab.length; --index >= 0; ) {
            tab[index] = null;
        }
        count = 0;
    }

    /**
     * Returns a rather long string representation of this hashtable.
     *
     * @return  a string representation of this hashtable.
     */
    public String toString() {
        return SquawkHashtable.enumerationsToString(keys(), elements(), size());
    }
    
    /**
     * Return the current capacity of the hashtable.
     * @return the current capacity
     */
    public final int capacity() {
        return entryTable.length;
    }
    
    /**
     * Return the internal table. Used by GC for bookkeeping.
     * 
     * THIS IS PRIVATE TO THE BOOTSTRAP SUITE
     * 
     * @return the internal table
     */
/*if[JAVA5SYNTAX]*/
    public final HashtableEntry<K, V>[] getEntryTable() {
/*else[JAVA5SYNTAX]*/
//    public final HashtableEntry[] getEntryTable() {
/*end[JAVA5SYNTAX]*/
        return entryTable;
    }

    /**
     * Utility class to get a rather long string representation of any kind of hashtable.
     *
     * @param keys 
     * @param elements 
     * @param size 
     * @return  a string representation of this hashtable.
     */
/*if[JAVA5SYNTAX]*/
    public static String enumerationsToString(Enumeration<?> keys, Enumeration<?> elements, int size) {
/*else[JAVA5SYNTAX]*/
//    public static String enumerationsToString(Enumeration keys, Enumeration elements, int size) {
/*end[JAVA5SYNTAX]*/
        int max = size - 1;
        StringBuffer buf = new StringBuffer();
        buf.append('{');

        for (int i = 0; i <= max; i++) {
            String s1 = keys.nextElement().toString();
            String s2 = elements.nextElement().toString();
            buf.append(s1).append('=').append(s2);
            if (i < max) {
                buf.append(", ");
            }
        }
        buf.append('}');
        return buf.toString();
    }

    /**
     * A hashtable enumerator class.  This class should remain opaque
     * to the client. It will use the Enumeration interface.
     */
/*if[JAVA5SYNTAX]*/
    class HashtableEnumerator<E> implements Enumeration<E> {
        HashtableEntry<K, V>[] table;
        HashtableEntry<K, V> entry;
/*else[JAVA5SYNTAX]*/
//  class HashtableEnumerator implements Enumeration {
//      HashtableEntry[] table;
//      HashtableEntry entry;
/*end[JAVA5SYNTAX]*/
        
        boolean keys;
        int index;

/*if[JAVA5SYNTAX]*/
        HashtableEnumerator(HashtableEntry<K, V>[] table, boolean keys) {
/*else[JAVA5SYNTAX]*/
//      HashtableEnumerator(HashtableEntry[] table, boolean keys) {
/*end[JAVA5SYNTAX]*/
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

/*if[JAVA5SYNTAX]*/
        @SuppressWarnings("unchecked")
        public E nextElement() {
/*else[JAVA5SYNTAX]*/
//        public Object nextElement() {
/*end[JAVA5SYNTAX]*/
            if (entry == null) {
                while ((index-- > 0) && ((entry = table[index]) == null)) {
                    // skip empty
                }
            }
            if (entry != null) {
/*if[JAVA5SYNTAX]*/
                HashtableEntry<K, V> e = entry;
                entry = e.next;
                return (E) (keys ? e.key : e.value);
/*else[JAVA5SYNTAX]*/
//                HashtableEntry e = entry;
//                entry = e.next;
//                return keys ? e.key : e.value;
/*end[JAVA5SYNTAX]*/
            }
            throw new NoSuchElementException();
        }
    }
    
    public static void printTable(SquawkHashtable table) {
        HashtableEntry[] array = table.entryTable;
        for (int i = 0; i < array.length; i++) {
            HashtableEntry entry = array[i];
            if (entry != null) {
                VM.print("    key: ");
                if (entry.key != null) {
                    GC.printObject(entry.key);
                } else {
                    VM.print("null");
                }
                VM.print("    value: ");
                if (entry.value != null) {
                    GC.printObject(entry.value);
                } else {
                    VM.print("null");
                }
            }
        }
    }
}

/**
 * SquawkHashtable collision list.
 */
/*if[JAVA5SYNTAX]*/
class HashtableEntry<K, V> {
    int hash;
    K key;
    V value;
    HashtableEntry<K, V> next;
/*else[JAVA5SYNTAX]*/
//class HashtableEntry {
//    int hash;
//    Object key;
//    Object value;
//    HashtableEntry next;
/*end[JAVA5SYNTAX]*/
}
