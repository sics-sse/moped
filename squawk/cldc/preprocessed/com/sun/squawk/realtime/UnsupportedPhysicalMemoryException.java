/*
 * @(#)UnsupportedPhysicalMemoryException.java	1.2 05/10/20
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.squawk.realtime;

/**
 * Thrown when the underlying hardware does not support the type of 
 * physical memory requested from an instance of
 * one of the physical memory or raw memory access classes.
 *
 * @see RawMemoryAccess
 * @see RawMemoryFloatAccess
 * 
 * 
 * @since 1.0.1 Becomes unchecked
 */
public class UnsupportedPhysicalMemoryException extends RuntimeException
{
    /**
     * A constructor for <code>UnsupportedPhysicalMemoryException</code>.
     */
    public UnsupportedPhysicalMemoryException(){
	super();
    }

    /**
     * A descriptive constructor for 
     * <code>UnsupportedPhysicalMemoryException</code>.
     *
     * @param description The description of the exception.
     */
    public UnsupportedPhysicalMemoryException(String description) {
	super(description);
    }
}
