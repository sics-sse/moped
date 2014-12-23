//if[JAVA5SYNTAX]*/
/*
 * Copyright (c) 2009 Sun Microsystems, Inc.
 * All rights reserved.
 * Use is subject to license terms.
 */
package java.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method declaration is intended to override a
 * method declaration in a superclass.  If a method is annotated with
 * this annotation type but does not override a superclass method,
 * compilers are required to generate an error message.
 *
 * @author  Joshua Bloch
 * @since JDK1.5, Java Card 3.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Override {
}
