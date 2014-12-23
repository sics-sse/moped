/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.util.UnexpectedException;

public class SimulatedNorFlashSectorAllocator implements INorFlashSectorAllocator {

    protected Vector sectors = new Vector();

    public static SimulatedNorFlashSectorAllocator getSingleton() {
        SimulatedNorFlashSectorAllocator result = (SimulatedNorFlashSectorAllocator) VM.getPeripheralRegistry().getSingleton(SimulatedNorFlashSectorAllocator.class);
        if (result == null) {
            result = new SimulatedNorFlashSectorAllocator();
            VM.getPeripheralRegistry().add(result);
        }
        return result;
    }
    
    public static void resetSingleton() {
        VM.getPeripheralRegistry().removeAll(SimulatedNorFlashSectorAllocator.class);
    }
    
    public synchronized INorFlashSector getExtraSector(int purpose) throws InsufficientFlashMemoryException {
        throw new InsufficientFlashMemoryException();
    }

    public INorFlashSector[] getInitialSectors(int purpose) {
        Vector purposedSectors = new Vector();
        for (int i=0, max=sectors.size(); i < max; i++) {
            INorFlashSector sector = (INorFlashSector) sectors.elementAt(i);
            if (sector.getPurpose() == purpose) {
                purposedSectors.addElement(sector);
            }
        }
        INorFlashSector[] result = new INorFlashSector[purposedSectors.size()];
        purposedSectors.copyInto(result);
        return result;
    }

    /**
     * Check to see if there are sectors already installed for the purpose
     * specified, or If there are sectors found on file system use them as is,
     * if not then setup with number of sectors and sectorSize as specified
     */
    public void installSectors(int numberOfSectors, int sectorSize, int purpose) {
        if (sectors.size() > 0) {
            int a=1;
        }
        boolean foundSome = false;
        final String sectorsFileExtension = SimulatedNorFlashSector.SECTORS_FILE_EXTENSION;
        try {
            DataInputStream fileListInput = Connector.openDataInputStream("file://./");
            while (fileListInput.available() > 0) {
                String fileName = fileListInput.readUTF();
                if (fileName.endsWith(sectorsFileExtension)) {
                    foundSome = true;
                    SimulatedNorFlashSector memorySector = new SimulatedNorFlashSector(fileName);
                    releaseSector(memorySector, purpose);
                }
            }
            fileListInput.close();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
        if (!foundSome) {
            setupSectors(numberOfSectors, sectorSize, purpose, true);
        }
    }

    public void releaseSector(INorFlashSector sector, int purpose) {
        sectors.addElement(sector);
        if (sectors.size() > 100) {
            int a = 1;
        }
    }

    public void setupSectors(int numberOfSectors, int sectorSize, int purpose, boolean useFiles) {
        uninstallSectors();
        final String sectorsFileExtension = SimulatedNorFlashSector.SECTORS_FILE_EXTENSION;
        // Delete all stores first
        try {
            DataInputStream fileListInput = Connector.openDataInputStream("file://./");
            while (fileListInput.available() > 0) {
                String fileName = fileListInput.readUTF();
                if (fileName.endsWith(sectorsFileExtension)) {
                    DataOutputStream fileDeleteOutput = Connector.openDataOutputStream("deletefiles://");
                    fileDeleteOutput.writeUTF(fileName);
                    fileDeleteOutput.close();
                }
            }
            fileListInput.close();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
        // Create new ones
        // TODO: Want to use the next line, but compiler creates a shared temp
        // causing translation problems
        // Address address = Address.zero();
        int address = 0;
        for (int i = 0; i < numberOfSectors; i++) {
            SimulatedNorFlashSector memorySector = new SimulatedNorFlashSector(
                    Address.fromPrimitive(address), sectorSize, purpose,
                    useFiles);
            releaseSector(memorySector, purpose);
            address += sectorSize;
        }
    }

    /**
     * Make it such that I have no sectors allocated.
     */
    public void uninstallSectors() {
        sectors.removeAllElements();
    }

}
