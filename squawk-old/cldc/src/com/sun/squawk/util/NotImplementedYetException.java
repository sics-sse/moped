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

package com.sun.squawk.util;

/**
 * Thrown to indicate that an attempt has been made to invoke
 * code that is not yet complete.  This is a utility class.
 */
public class NotImplementedYetException extends RuntimeException {

    /**
     * Throw a new instance of a <code>NotImplementedYetException</code>.
     * Helper method in order to get around fact that if we put an explicit throw
     * in our code, any code that follows is normally indicated as unreachable.
     * This way developer can provide some code even though it may not be
     * complete.
     */
    public static void throwNow() {
        throw new NotImplementedYetException();
    }
    
    /**
     * Dont throw the exception, just stay as a marker.
     */
    public static void mark() {
        System.out.println("Some function is not yet completely implemented");
    }
    
    /**
     * Constructs an <code>NotImplementedYetException</code> with no detail message.
     */
    public NotImplementedYetException() {
        super();
    }

    /**
     * Constructs an <code>NotImplementedYetException</code> with the specified
     * detail message.
     *
     * @param   s   the detail message.
     */
    public NotImplementedYetException(String s) {
        super(s);
    }
}
