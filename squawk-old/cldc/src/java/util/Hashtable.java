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

package java.util;

/*if[JAVA5SYNTAX]*/
import com.sun.squawk.Java5Marker;
/*end[JAVA5SYNTAX]*/

/**
 * This class implements a hashtable, which maps keys to values. Any
 * non-<code>null</code> object can be used as a key or as a value.
 * <p>
 * To successfully store and retrieve objects from a hashtable, the
 * objects used as keys must implement the <code>hashCode</code>
 * method and the <code>equals</code> method.
 * 
 * <p>
 * An instance of <code>Hashtable</code> has two parameters that
 * affect its efficiency: its <i>capacity</i> and its <i>load
 * factor</i>. The load factor in the CLDC implementation of
 * the hashtable class is always 75 percent.  When the number
 * of entries in the hashtable exceeds the product of the
 * load factor and the current capacity, the capacity is increased 
 * by calling the <code>rehash</code> method.
 * <p>
 * If many entries are to be made into a <code>Hashtable</code>,
 * creating it with a sufficiently large capacity may allow the
 * entries to be inserted more efficiently than letting it perform
 * automatic rehashing as needed to grow the table.
 * <p>
 * This example creates a hashtable of numbers. It uses the names of
 * the numbers as keys:
 * <p><blockquote><pre>
 *     Hashtable numbers = new Hashtable();
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
 *
 * @version 12/17/01 (CLDC 1.1)
 * @see     java.lang.Object#equals(java.lang.Object)
 * @see     java.lang.Object#hashCode()
 * @see     java.util.Hashtable#rehash()
 * @since   JDK1.0, CLDC 1.0
 */
/*if[JAVA5SYNTAX]*/
public class Hashtable<K, V> {
/*else[JAVA5SYNTAX]*/
//public class Hashtable {
/*end[JAVA5SYNTAX]*/

    /**
     * A non synchronized version of this type already exists in Squawk, delegate to it.
     */
/*if[JAVA5SYNTAX]*/
    final com.sun.squawk.util.SquawkHashtable<K, V> delegate;
/*else[JAVA5SYNTAX]*/
//  final com.sun.squawk.util.SquawkHashtable delegate;
/*end[JAVA5SYNTAX]*/

    /**
     * Constructs a new, empty hashtable with the specified initial
     * capacity.
     *
     * @param      initialCapacity   the initial capacity of the hashtable.
     * @exception  IllegalArgumentException  if the initial capacity is less
     *             than zero
     * @since      JDK1.0
     */
    public Hashtable(int initialCapacity) {
/*if[JAVA5SYNTAX]*/
        delegate = new com.sun.squawk.util.SquawkHashtable<K, V>(initialCapacity);
/*else[JAVA5SYNTAX]*/
//        delegate = new com.sun.squawk.util.SquawkHashtable(initialCapacity);
/*end[JAVA5SYNTAX]*/
        delegate.setRehasher(new com.sun.squawk.util.SquawkHashtable.Rehasher() {
            public void rehash() {
                Hashtable.this.rehash();
            }
        });
    }

    /**
     * Constructs a new, empty hashtable with a default capacity and load
     * factor.
     *
     * @since   JDK1.0
     */
    public Hashtable() {
        this(11);
    }

    /**
     * Returns an enumeration of the keys in this hashtable.
     * 
     * @return an enumeration of the keys in this hashtable.
     * @see java.util.Enumeration
     * @see     java.util.Hashtable#elements()
     * @since   JDK1.0
     */
/*if[JAVA5SYNTAX]*/
    public synchronized Enumeration<K> keys() {
/*else[JAVA5SYNTAX]*/
//    public synchronized Enumeration keys() {
/*end[JAVA5SYNTAX]*/
        return delegate.keys();
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
/*if[JAVA5SYNTAX]*/
    public synchronized Enumeration<V> elements() {
/*else[JAVA5SYNTAX]*/
//    public synchronized Enumeration elements() {
/*end[JAVA5SYNTAX]*/
        return delegate.elements();
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
    public synchronized boolean contains(Object value) {
        return delegate.contains(value);
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
    public synchronized boolean containsKey(Object key) {
        return delegate.containsKey(key);
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
/*if[JAVA5SYNTAX]*/
    @SuppressWarnings("unchecked")
    public synchronized V get(K key) {
        return (V) delegate.get(key);
/*else[JAVA5SYNTAX]*/
//    public synchronized Object get(Object key) {
//        return delegate.get(key);
/*end[JAVA5SYNTAX]*/
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
/*if[JAVA5SYNTAX]*/
    public synchronized V put(K key, V value) {
        return (V) delegate.put(key, value);
    }
/*else[JAVA5SYNTAX]*/
//    public synchronized Object put(Object key, Object value) {
//        return delegate.put(key, value);
//    }
/*end[JAVA5SYNTAX]*/

    /**
     * Removes the key (and its corresponding value) from this
     * hashtable. This method does nothing if the key is not in the hashtable.
     *
     * @param   key   the key that needs to be removed.
     * @return  the value to which the key had been mapped in this hashtable,
     *          or <code>null</code> if the key did not have a mapping.
     * @since   JDK1.0
     */
/*if[JAVA5SYNTAX]*/
    public synchronized V remove(K key) {
/*else[JAVA5SYNTAX]*/
//    public synchronized Object remove(Object key) {
/*end[JAVA5SYNTAX]*/
        return delegate.remove(key);
    }

    /**
     * Clears this hashtable so that it contains no keys.
     *
     * @since   JDK1.0
     */
    public synchronized void clear() {
        delegate.clear();
    }

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return  the number of keys in this hashtable.
     * @since   JDK1.0
     */
    public synchronized int size() {
        return delegate.size();
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return  <code>true</code> if this hashtable maps no keys to values;
     *          <code>false</code> otherwise.
     * @since   JDK1.0
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
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
        delegate.rehash();
    }

    /**
     * Returns a rather long string representation of this hashtable.
     *
     * @return  a string representation of this hashtable.
     * @since   JDK1.0
     */
    public synchronized String toString() {
        return delegate.toString();
    }

}
