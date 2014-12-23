/*
 * Copyright 2006-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2010-2011 Oracle. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.flash;

import javax.microedition.rms.RecordStoreException;

import com.sun.squawk.Address;

public interface INorFlashSectorState {

    public void decrementMallocedCount();

    public void erase(long sequence) throws RecordStoreException;

    public void forceErase() throws RecordStoreException;

    public int getAllocatedBlockCount();

    public Address getEndAddress();
    
    public int getFreedBlockCount();

    public INorFlashSectorState getNextSector();
    
    public INorFlashSectorStateList getOwningList();
    
    public long getSequence();
    
    public int getSize();
    
    public Address getStartAddress();
    
    public Address getWriteHeadAddress();

    public int getWriteHeadPosition();

    public int getAvailable();

    public boolean hasAvailable(int length);
    
    public boolean hasErasedHeader();
    
    public void incrementAllocatedBlockCount();

    public void incrementFreedBlockCount();

    public void readBytes(int offset, byte buffer[], int bufferStart, int bufferLength) throws RecordStoreException;
    
    public void removeErasedHeader() throws RecordStoreException;
    
    public void resetHead();
    
    public void setNextSector(INorFlashSectorState next);

    public void setOwningList(INorFlashSectorStateList list);

    public void setWriteHeadPosition(int position);
    
    public void writeBytes(byte buffer[], int bufferStart, int bufferLength) throws RecordStoreException;

    public void writeBytes(int offset, byte buffer[], int bufferStart, int bufferLength) throws RecordStoreException;

}
