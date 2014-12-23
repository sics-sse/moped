//if[JAVA5SYNTAX]*/
/*
 * Copyright (c) 2008 Sun Microsystems, Inc.
 * All rights reserved.
 * Use is subject to license terms.
 */
package java.lang;

import com.sun.squawk.Java5Marker;

/**
 * The Void class is an uninstantiable placeholder class to hold a
 * reference to the Class object representing the Java keyword
 * void.
 * <p>
 * This Java Card class is a subset of the JDK 1.5 Void class. Some
 * interfaces, methods and/or variables have been pruned, and/or other methods
 * simplified, in an effort to reduce the size of this class and/or eliminate
 * dependencies on unsupported features.
 *
 * @author  unascribed
 * @version 1.15, 12/01/05
 * @since   JDK1.1, Java Card 3.0
 */
@Java5Marker
public final class Void {

    /*
     * The Void class cannot be instantiated.
     */
    private Void() {}
}
