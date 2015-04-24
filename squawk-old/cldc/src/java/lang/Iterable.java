//if[JAVA5SYNTAX]*/
package java.lang;

import java.util.Iterator;

import com.sun.squawk.Java5Marker;

/**
 * A version of the 1.5 java.lang.Iterable class for the 1.4 VM.
 */
@Java5Marker
public interface Iterable<E> {
	Iterator<E> iterator();
}
