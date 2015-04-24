/*
 * @(#)SizeOutOfBoundsException.java	1.2 05/10/20
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.squawk.realtime;

/**
 * Thrown if the constructor of a {@link RawMemoryAccess}, or
 * {@link RawMemoryFloatAccess} is given an invalid size or if an 
 * accessor method on one of the above classes would cause access to 
 * an invalid address.
 *
 * @since 1.0.1 Becomes unchecked
 */
public class SizeOutOfBoundsException extends RuntimeException
{
    /**
     * A constructor for <code>SizeOutOfBoundsException</code>.
     */
    public SizeOutOfBoundsException() {
	super();
    }

    /**
     * A descriptive constructor for <code>SizeOutOfBoundsException</code>.
     *
     * @param description The description of the exception.
     */
    public SizeOutOfBoundsException(String description) {
	super(description);
    }
}
