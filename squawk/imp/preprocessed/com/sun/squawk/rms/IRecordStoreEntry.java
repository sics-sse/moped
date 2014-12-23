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

package com.sun.squawk.rms;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

public interface IRecordStoreEntry extends IRmsEntry {

    public void absorbTempEntry(IRecordStoreEntry tempEntry);
    
    public int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreException, RecordStoreFullException;
    
    public void setRecord(int recordId, byte[] data, int offset, int numBytes) throws InvalidRecordIDException, RecordStoreException, RecordStoreFullException;
    
    public void deleteRecord(int recordId) throws InvalidRecordIDException, RecordStoreException;
    
    public void deleteRecords() throws RecordStoreException;

    public String getName();
    
    public int getNextRecordId();
    
    public int getNumRecords();
    
    public int getId();
    
    public byte[] getRecord(int recordId) throws InvalidRecordIDException, RecordStoreException;

    public int getRecord(int recordId, byte[] buffer, int offset) throws InvalidRecordIDException, RecordStoreException;

    public int[] getRecordIdsCopy();
    
    public int getRecordSize(int recordId) throws InvalidRecordIDException, RecordStoreException;
    
    public int getRecordsSize() throws RecordStoreException;

    public long getTimestamp();
    
    public int getVersion();
    
    public void reVisitRecordEntry(IRecordEntry record) throws RecordStoreException;

    public void setId(int id);
    
    public void setName(String name);
    
    public void visitRecordEntry(IRecordEntry record) throws RecordStoreException;
    
}
