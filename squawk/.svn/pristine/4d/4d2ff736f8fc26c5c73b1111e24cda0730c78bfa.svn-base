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

package com.sun.squawk.util;

/**
 * A version of Java SE's java.util.Comparator interface.<p>
 * 
 * A comparison function, which imposes a <i>total ordering</i> on some
 * collection of objects.  Comparers can be passed to a sort method (such as
 * <tt>Collections.sort</tt>) to allow precise control over the sort order.
 * Comparers can also be used to control the order of certain data
 * structures (such as <tt>TreeSet</tt> or <tt>TreeMap</tt>).<p>
 *
 * The ordering imposed by a Comparer <tt>c</tt> on a set of elements
 * <tt>S</tt> is said to be <i>consistent with equals</i> if and only if
 * <tt>(compare((Object)e1, (Object)e2)==0)</tt> has the same boolean value as
 * <tt>e1.equals((Object)e2)</tt> for every <tt>e1</tt> and <tt>e2</tt> in
 * <tt>S</tt>.<p>
 *
 * Caution should be exercised when using a Comparer capable of imposing an
 * ordering inconsistent with equals to order a sorted set (or sorted map).
 * Suppose a sorted set (or sorted map) with an explicit Comparer <tt>c</tt>
 * is used with elements (or keys) drawn from a set <tt>S</tt>.  If the
 * ordering imposed by <tt>c</tt> on <tt>S</tt> is inconsistent with equals,
 * the sorted set (or sorted map) will behave "strangely."  In particular the
 * sorted set (or sorted map) will violate the general contract for set (or
 * map), which is defined in terms of <tt>equals</tt>.<p>
 * 
 * For example, if one adds two keys <tt>a</tt> and <tt>b</tt> such that
 * <tt>(a.equals((Object)b) && c.compare((Object)a, (Object)b) != 0)</tt> to a
 * sorted set with Comparer <tt>c</tt>, the second <tt>add</tt> operation
 * will return false (and the size of the sorted set will not increase)
 * because <tt>a</tt> and <tt>b</tt> are equivalent from the sorted set's
 * perspective.<p>
 *
 * Note: It is generally a good idea for Comparers to implement
 * <tt>java.io.Serializable</tt>, as they may be used as ordering methods in
 * serializable data structures (like <tt>TreeSet</tt>, <tt>TreeMap</tt>).  In
 * order for the data structure to serialize successfully, the Comparer (if
 * provided) must implement <tt>Serializable</tt>.<p>
 *
 * For the mathematically inclined, the <i>relation</i> that defines
 * the <i>total order</i> that a given Comparer <tt>c</tt> imposes on a
 * given set of objects <tt>S</tt> is:<pre>
 *       {(x, y) such that c.compare((Object)x, (Object)y) &lt;= 0}.
 * </pre> The <i>quotient</i> for this total order is:<pre>
 *       {(x, y) such that c.compare((Object)x, (Object)y) == 0}.
 * </pre>
 *
 * It follows immediately from the contract for <tt>compare</tt> that the
 * quotient is an <i>equivalence relation</i> on <tt>S</tt>, and that the
 * natural ordering is a <i>total order</i> on <tt>S</tt>.  When we say that
 * the ordering imposed by <tt>c</tt> on <tt>S</tt> is <i>consistent with
 * equals</i>, we mean that the quotient for the natural ordering is the
 * equivalence relation defined by the objects' <tt>equals(Object)</tt>
 * method(s):<pre>
 *       {(x, y) such that x.equals((Object)y)}.
 * </pre><p>
 *
 *
 * @see Arrays#sort(Object[], Comparer)
 */
public interface Comparer {
    
    /**
     * Compares its two arguments for order.  Returns a negative integer,
     * zero, or a positive integer as the first argument is less than, equal
     * to, or greater than the second.<p>
     *
     * The implementor must ensure that <tt>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>compare(x, y)</tt> must throw an exception if and only
     * if <tt>compare(y, x)</tt> throws an exception.)<p>
     *
     * The implementor must also ensure that the relation is transitive:
     * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
     * <tt>compare(x, z)&gt;0</tt>.<p>
     *
     * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
     * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
     * <tt>z</tt>.<p>
     *
     * It is generally the case, but <i>not</i> strictly required that 
     * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
     * any comparator that violates this condition should clearly indicate
     * this fact.  The recommended language is "Note: this comparator
     * imposes orderings that are inconsistent with equals."
     * 
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * 	       first argument is less than, equal to, or greater than the
     *	       second. 
     * @throws ClassCastException if the arguments' types prevent them from
     * 	       being compared by this Comparer.
     */
    public int compare(Object o1, Object o2);
}
