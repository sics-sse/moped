/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.peripheral;

import com.sun.squawk.Address;

/**
 * Define a segment of memory which is defined as being of type NOR Flash.
 * The read-only mode of NOR memories is similar to reading from a common memory,
 * provided address and data bus is mapped correctly, so NOR flash memory is much
 * like any address-mapped memory. NOR flash memories can be used as execute-in-place
 * memory (XIP), meaning it behaves as a ROM memory mapped to a certain address.
 * A NOR flash sector can be completely erased, setting all bits to 1s.  Writing simply
 * sets some bits from 1 to 0.  Setting a bit from 0 to 1, requires the complete sector
 * to be erased.
 */
public interface INorFlashSector {
    public static final int USER_PURPOSED = 1;
    public static final int SYSTEM_PURPOSED = 2;
    public static final int RMS_PURPOSED = 3;

    public void erase();
    
    public void getBytes(int memoryOffset, byte[] buffer, int bufferOffset, int length);
    
    public byte getErasedValue();
    
    public int getPurpose();
    
    public int getSize();
    
    public Address getStartAddress();
    
    public void setBytes(int memoryOffset, byte[] buffer, int bufferOffset, int length);

}
