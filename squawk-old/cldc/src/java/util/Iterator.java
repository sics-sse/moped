//if[JAVA5SYNTAX]*/
/*
 * Copyright (c) 2008 Sun Microsystems, Inc.
 * All rights reserved.
 * Use is subject to license terms.
 */
package java.util;

import com.sun.squawk.Java5Marker;

/**
 * An iterator over a collection. Iterator are especially used
 * to iterate over a collection object when it's the target of the
 * "foreach" statement.
 * 
 * This Java Card interface is a subset of the CDC 1.1 Iterator interface. Some
 * interfaces, methods and/or variables have been pruned, and/or other methods
 * simplified, in an effort to reduce the size of this class and/or eliminate
 * dependencies on unsupported features.
 * 
 * @author Josh Bloch
 * @version 1.24, 01/17/04
 * @see Enumeration
 * @see java.lang.Iterable
 * @since JDK1.2, CDC 1.1, Java Card 3.0
 */
@Java5Marker
public interface Iterator<E> {
    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     * 
     * @return <tt>true</tt> if the iterator has more elements.
     */
    boolean hasNext();

    /**
     * Returns the next element in the iteration. Calling this method repeatedly
     * until the {@link #hasNext()} method returns false will return each
     * element in the underlying collection exactly once.
     * 
     * @return the next element in the iteration.
     * @exception NoSuchElementException
     *                iteration has no more elements.
     */
    E next();
}
