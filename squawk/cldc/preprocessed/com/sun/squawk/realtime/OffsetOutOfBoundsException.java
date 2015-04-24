/*
 * @(#)OffsetOutOfBoundsException.java	1.2 05/10/20
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.squawk.realtime;

/**
 * Thrown if the constructor of a {@link RawMemoryAccess}, or
 * {@link RawMemoryFloatAccess} is given an invalid address.
 *
 * @since 1.0.1 Becomes unchecked
 */
public class OffsetOutOfBoundsException extends RuntimeException {
    
    /**
     * A constructor for <code>OffsetOutOfBoundsException</code>.
     */
    public OffsetOutOfBoundsException() {
    }
    
    /**
     * A descriptive constructor for <code>OffsetOutOfBoundsException</code>.
     *
     * @param description A description of the exception.
     */
    public OffsetOutOfBoundsException(String description) {
        super(description);
    }
}
