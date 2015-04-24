//if[JAVA5SYNTAX]*/
package java.lang.annotation;

import com.sun.squawk.Java5Marker;

/**
 * A mirror of java.lang.annotation.ElementType.
 *
 * @author Toby Reyelts
 *
 */
@Java5Marker
public enum ElementType {
  ANNOTATION_TYPE,
  CONSTRUCTOR,
  FIELD,
  LOCAL_VARIABLE,
  METHOD,
  PACKAGE,
  PARAMETER,
  TYPE;
}
