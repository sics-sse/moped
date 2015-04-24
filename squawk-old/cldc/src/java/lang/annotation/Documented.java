//if[JAVA5SYNTAX]*/
package java.lang.annotation;

import com.sun.squawk.Java5Marker;

@Java5Marker
public @Documented @Retention( RetentionPolicy.RUNTIME) @Target( ElementType.ANNOTATION_TYPE ) @interface Documented {
}

