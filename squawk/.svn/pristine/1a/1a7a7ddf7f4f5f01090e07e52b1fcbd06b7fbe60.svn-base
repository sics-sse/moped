/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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

package com.sun.squawk.imp;

import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.sun.midp.midlet.MIDletTunnel;
import com.sun.squawk.flash.NorFlashMemoryHeap;
import com.sun.squawk.peripheral.INorFlashSector;
import com.sun.squawk.peripheral.SimulatedNorFlashSectorAllocator;
import com.sun.squawk.pragma.GlobalStaticFields;
import com.sun.squawk.rms.IRecordStoreManager;
import com.sun.squawk.rms.RecordStoreManager;

/**
 * This class declares IMP global variables that are shared among all isolates.
 */
public class ImpGlobal implements GlobalStaticFields {
    
    /**
     * Purely static class should not be instantiated.
     */
    private ImpGlobal() {}

    protected static IRecordStoreManager recordStoreManager;
    protected static Vector recordStoreDbCache;
    // Initialized specially via the MIDlet constructor 
    public static MIDletTunnel midLetTunnel;
    
    public static void forceEraseRecordStores() {
        new NorFlashMemoryHeap(INorFlashSector.RMS_PURPOSED).forceEraseAll();
        resetRecordStoreManager();
    }
    
    public static Vector getRecordStoreDbCache() {
        if (recordStoreDbCache == null) {
            recordStoreDbCache = new Vector(3);
        }
        return recordStoreDbCache;
    }
    
    public synchronized static IRecordStoreManager getRecordStoreManager() throws RecordStoreException {
        if (recordStoreManager == null) {
/*if[!FLASH_MEMORY]*/
            if (SimulatedNorFlashSectorAllocator.getSingleton().getInitialSectors(INorFlashSector.RMS_PURPOSED).length == 0) {
                SimulatedNorFlashSectorAllocator.getSingleton().installSectors(16, 65536, INorFlashSector.RMS_PURPOSED);
            }
/*end[FLASH_MEMORY]*/
            recordStoreManager = new RecordStoreManager(new NorFlashMemoryHeap(INorFlashSector.RMS_PURPOSED));
        }
        return recordStoreManager;
    }
    
    public static long getRecordStoreManagerErasedSequenceCurrentValue() throws RecordStoreException {
        return getRecordStoreManager().getErasedSequenceCurrentValue();
    }
    
    public synchronized static void resetRecordStoreManager() {
        recordStoreManager = null;
        recordStoreDbCache = null;
    }
    
}
