//if[JAVA5SYNTAX]*/
package net.sourceforge.retroweaver.runtime.java.lang;

import java.util.Iterator;


/**
 * Replacements for methods added to java.lang.Iterable in Java 1.5, used
 * for targets of the "foreach" statement.
 */
public final class Iterable_ {

	private Iterable_() {
	}

	/**
	 * Returns an iterator for <code>iterable</code>.
	 * 
	 * @param iterable  the object to get the Iterator from
	 * @return an Iterator.
	 * @throws UnsupportedOperationException if an iterator method can not be found.
	 * @throws NullPointerException if <code>iterable</code> is null.
	 */
	public static Iterator iterator(final Object iterable) {
		if (iterable == null) {
			throw new NullPointerException(); // NOPMD by xlv
		}

		if (iterable instanceof Iterable) {
			return ((Iterable) iterable).iterator();
		}

		throw new RuntimeException("UnsupportedOperationException: iterator call on " + iterable.getClass());
	}

}
