//if[JAVA5SYNTAX]*/
package java.lang.annotation;

import com.sun.squawk.Java5Marker;

/**
 * A mirror of java.lang.annotation.RetentionPolicy.
 *
 * @author Toby Reyelts
 *
 */
@Java5Marker
public enum RetentionPolicy {
  CLASS,
  RUNTIME,
  SOURCE;
}
