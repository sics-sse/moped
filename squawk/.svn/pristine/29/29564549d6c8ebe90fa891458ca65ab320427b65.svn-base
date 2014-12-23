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

import java.io.File;
import java.io.FileFilter;

import org.jmock.integration.junit3.MockObjectTestCase;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.util.IntHashtable;

public class SimulatedNorFlashSectorTest extends MockObjectTestCase {
    
    protected SimulatedNorFlashSector sector;
    protected int testSize;
    protected byte[] bytes;

    @Override
    protected void setUp() throws Exception {
        testSize = 32;
        bytes = new byte[testSize];
        sector = new SimulatedNorFlashSector(Address.fromPrimitive(2), testSize, INorFlashSector.RMS_PURPOSED, false);
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public void testEnsureInBounds() {
        int delta = 2;
        // memoryOffset, buffer, bufferOffset, length
        sector.ensureInBounds(0, bytes, testSize - delta, delta);
        delta = testSize;
        sector.ensureInBounds(0, bytes, testSize - delta, delta);
    }
    
    public void testEnsureInBounds1() {
        int delta = 2;
        // memoryOffset, buffer, bufferOffset, length
        sector.ensureInBounds(0, bytes, delta, testSize - delta);
        delta = testSize - 2;
        sector.ensureInBounds(0, bytes, delta, testSize - delta);
    }
    
    public void testEnsureInBounds2() {
        // memoryOffset, buffer, bufferOffset, length
        try {
            sector.ensureInBounds(0, null, 0, 2);
            fail();
        } catch (NullPointerException e) {
        }
    }
    
    public void testEnsureInBounds3() {
        // memoryOffset, buffer, bufferOffset, length
        try {
            sector.ensureInBounds(testSize, bytes, 0, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }
    
    public void testEnsureInBounds4() {
        // memoryOffset, buffer, bufferOffset, length
        try {
            sector.ensureInBounds(0, bytes, 0, testSize + 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }
    
    public void testSetBytes1() {
        // memoryOffset, buffer, bufferOffset, length
        try {
            sector.setBytes(0, bytes, 0, 1);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void testSetBytes2() {
        // memoryOffset, buffer, bufferOffset, length
        try {
            sector.setBytes(1, bytes, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

    public void testInstallSectors() {
        // Setup some sectors
        final int numSectors = 10;
        final int size = 65536;
        SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(numSectors, size, INorFlashSector.RMS_PURPOSED, true);
        INorFlashSector[] sectors = SimulatedNorFlashSectorAllocator.getSingleton().getInitialSectors(INorFlashSector.RMS_PURPOSED);
        assertEquals(numSectors, sectors.length);
        // Now uninstall them and make sure they are gone
        SimulatedNorFlashSectorAllocator.getSingleton().uninstallSectors();
        sectors = SimulatedNorFlashSectorAllocator.getSingleton().getInitialSectors(INorFlashSector.RMS_PURPOSED);;
        assertEquals(0, sectors.length);
        
        // Now install them and make sure they are there
        SimulatedNorFlashSectorAllocator.getSingleton().installSectors(0, 0, INorFlashSector.RMS_PURPOSED);
        sectors = SimulatedNorFlashSectorAllocator.getSingleton().getInitialSectors(INorFlashSector.RMS_PURPOSED);;
        assertEquals(numSectors, sectors.length);
        IntHashtable addresses = new IntHashtable();
        Object marker = new Object();
        int address = 0;
        for (int i=0; i < numSectors; i++) {
            addresses.put(address, marker);
            address += size;
        }
        for (int i=0; i < numSectors; i++) {
            SimulatedNorFlashSector sector = (SimulatedNorFlashSector) sectors[i];
            assertEquals(size, sector.getSize());
            addresses.remove(sector.getStartAddress().toUWord().toInt());
        }
        assertEquals(0, addresses.size());
    }
    
    public void testSetupSectors() {
        File dir = new File(".");
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                return file.getPath().endsWith(SimulatedNorFlashSector.SECTORS_FILE_EXTENSION);
            }
        };
        
        // Test no file based exist after setup of 0
        SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(0, 2, INorFlashSector.RMS_PURPOSED, true);
        INorFlashSector[] sectors = SimulatedNorFlashSectorAllocator.getSingleton().getInitialSectors(INorFlashSector.RMS_PURPOSED);
        assertEquals(0, sectors.length);
        File[] files = dir.listFiles(filter);
        assertNotNull(files);
        assertEquals(0, files.length);

        // Test there is 1 file based after setup of 1
        SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(1, 2, INorFlashSector.RMS_PURPOSED, true);
        sectors = SimulatedNorFlashSectorAllocator.getSingleton().getInitialSectors(INorFlashSector.RMS_PURPOSED);
        assertEquals(1, sectors.length);
        files = dir.listFiles(filter);
        assertNotNull(files);
        assertEquals(1, files.length);

        // Test there is 10 file based after setup of 10
        SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(10, 2, INorFlashSector.RMS_PURPOSED, true);
        sectors = SimulatedNorFlashSectorAllocator.getSingleton().getInitialSectors(INorFlashSector.RMS_PURPOSED);
        assertEquals(10, sectors.length);
        files = dir.listFiles(filter);
        assertNotNull(files);
        assertEquals(10, files.length);
    }
    
    public void testUninstallSectors() {
        SimulatedNorFlashSectorAllocator.getSingleton().uninstallSectors();
        // Test no file based exist
        IPeripheral[] peripherals = VM.getPeripheralRegistry().getAll(SimulatedNorFlashSector.class);
        assertEquals(0, peripherals.length);

        // Set 1 up
        SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(1, 2, INorFlashSector.RMS_PURPOSED, true);
        
        SimulatedNorFlashSectorAllocator.getSingleton().uninstallSectors();
        peripherals = VM.getPeripheralRegistry().getAll(SimulatedNorFlashSector.class);
        assertEquals(0, peripherals.length);
    }
    
}
