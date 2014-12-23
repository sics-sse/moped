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

/**
 * Linked list of INorFlashSectorStates.
 */
public class NorFlashSectorStateList implements INorFlashSectorStateList {
    protected INorFlashSectorState head;
    protected INorFlashSectorState tail;
    protected int size = 0;

    public void addLast(INorFlashSectorState sectorState) {
        if (sectorState.getOwningList() != null) {
            sectorState.getOwningList().remove(sectorState);
        }
        size++;
        sectorState.setOwningList(this);
        if (tail == null) {
            head = tail = sectorState;
            sectorState.setNextSector(null);
            return;
        }
        tail.setNextSector(sectorState);
        tail = sectorState;
    }

    public INorFlashSectorState consumeFirst() {
        if (head == null) {
            return null;
        }
        INorFlashSectorState result;
        if (head == tail) {
            result = head;
            head = tail = null;
        } else {
            result = head;
            head = head.getNextSector();
        }
        size--;
        result.setOwningList(null);
        result.setNextSector(null);
        return result;
    }

    public INorFlashSectorState getFirst() {
        return head;
    }
    
    public void remove(INorFlashSectorState toRemove) {
        INorFlashSectorState previousSector = null;
        INorFlashSectorState sector = head;
        while (sector != null) {
            if (sector == toRemove) {
                size--;
                if (previousSector != null) {
                    previousSector.setNextSector(sector.getNextSector());
                }
                if (head == sector) {
                    head = sector.getNextSector();
                }
                if (sector == tail) {
                    tail = previousSector;
                }
                sector.setOwningList(null);
                sector.setNextSector(null);
                return;
            }
            previousSector = sector;
            sector = sector.getNextSector();
        }
    }

    public int size() {
        return size;
    }
    
}
