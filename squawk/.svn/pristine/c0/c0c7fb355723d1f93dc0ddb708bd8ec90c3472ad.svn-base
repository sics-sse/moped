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

package com.sun.squawk.flash;

import com.sun.squawk.Address;
import com.sun.squawk.peripheral.SimulatedNorFlashSector;

import junit.framework.TestCase;

public class NorFlashSectorStateListTest extends TestCase {
    protected NorFlashSectorStateList castList;
    protected INorFlashSectorStateList list;
    
    public void setUp() {
        castList = new NorFlashSectorStateList();
        list = castList;
    }
    
    public INorFlashSectorState newSector(Address address) {
        return new NorFlashSectorState(new SimulatedNorFlashSector(address, NorFlashSectorState.ERASED_HEADER_SIZE, 0, false));
    }
    
    public void testAddLast() {
        INorFlashSectorState sector1 = newSector(Address.zero());
        INorFlashSectorState sector2 = newSector(Address.fromPrimitive(1));
        
        assertEquals(0, list.size());
        list.addLast(sector1);
        assertEquals(1, list.size());
        assertSame(list, sector1.getOwningList());
        assertSame(sector1, castList.head);
        assertSame(sector1, castList.tail);
        assertNull(sector1.getNextSector());
        
        list.addLast(sector2);
        assertSame(list, sector2.getOwningList());
        assertSame(sector1, castList.head);
        assertSame(sector2, castList.tail);
        assertSame(sector2, sector1.getNextSector());
    }

    /*
     * Add a sector that already belongs to another list
     */
    public void testAddLast1() {
        INorFlashSectorState sector = newSector(Address.zero());
        NorFlashSectorStateList otherList = new NorFlashSectorStateList();
        
        otherList.addLast(sector);
        list.addLast(sector);
        assertNull(otherList.head);
        assertNull(otherList.tail);
        assertSame(list, sector.getOwningList());
        assertSame(sector, castList.head);
        assertSame(sector, castList.tail);
        assertNull(sector.getNextSector());
        
    }

    public void testConsumeFirst() {
        assertNull(list.consumeFirst());
        assertNull(list.consumeFirst());

        INorFlashSectorState sector1 = newSector(Address.zero());
        INorFlashSectorState sector2 = newSector(Address.fromPrimitive(1));
        
        list.addLast(sector1);
        assertSame(sector1, list.consumeFirst());
        assertNull(sector1.getOwningList());
        assertNull(sector1.getNextSector());
        assertNull(castList.head);
        assertNull(castList.tail);
        
        list.addLast(sector1);
        list.addLast(sector2);
        assertEquals(2, list.size());
        assertSame(sector1, list.consumeFirst());
        assertSame(sector2, list.consumeFirst());
        assertNull(sector2.getOwningList());
        assertNull(sector2.getNextSector());
        assertNull(list.consumeFirst());
    }
    
    public void testGetFirst() {
        assertNull(list.getFirst());
        INorFlashSectorState sector1 = newSector(Address.zero());
        list.addLast(sector1);
        assertSame(sector1, list.getFirst());
        assertSame(sector1, list.getFirst());
        list.consumeFirst();
        assertNull(list.getFirst());
    }

    /*
     * remove non-existent entry
     */
    public void testRemove1() {
        assertEquals(0, list.size());
        list.remove(newSector(Address.zero()));
        assertEquals(0, list.size());
    }

    /*
     * Remove first with 1 entry
     */
    public void testRemove2() {
        INorFlashSectorState sector1 = newSector(Address.zero());

        list.addLast(sector1);
        list.remove(sector1);
        assertEquals(0, list.size());
        assertNull(castList.head);
        assertNull(castList.tail);
        assertNull(sector1.getNextSector());
        assertNull(sector1.getOwningList());
    }
    
    /*
     * Remove first with 2 entries
     */
    public void testRemove3() {
        INorFlashSectorState sector1 = newSector(Address.zero());
        INorFlashSectorState sector2 = newSector(Address.fromPrimitive(1));

        list.addLast(sector1);
        list.addLast(sector2);
        list.remove(sector1);
        assertEquals(1, list.size());
        assertSame(sector2, castList.head);
        assertSame(sector2, castList.tail);
        assertNull(sector2.getNextSector());
        assertNull(sector1.getNextSector());
        assertNull(sector1.getOwningList());
    }
    
    /*
     * Remove last with 2 entries
     */
    public void testRemove4() {
        INorFlashSectorState sector1 = newSector(Address.zero());
        INorFlashSectorState sector2 = newSector(Address.fromPrimitive(1));

        list.addLast(sector1);
        list.addLast(sector2);
        list.remove(sector2);
        assertEquals(1, list.size());
        assertSame(sector1, castList.head);
        assertSame(sector1, castList.tail);
        assertNull(sector1.getNextSector());
        assertNull(sector2.getNextSector());
        assertNull(sector2.getNextSector());
        assertNull(sector2.getOwningList());
    }
    
    /*
     * Remove middle with 3 entries
     */
    public void testRemove5() {
        INorFlashSectorState sector1 = newSector(Address.zero());
        INorFlashSectorState sector2 = newSector(Address.fromPrimitive(1));
        INorFlashSectorState sector3 = newSector(Address.fromPrimitive(2));

        list.addLast(sector1);
        list.addLast(sector2);
        list.addLast(sector3);
        list.remove(sector2);
        assertEquals(2, list.size());
        assertSame(sector1, castList.head);
        assertSame(sector3, castList.tail);
        assertSame(sector3, sector1.getNextSector());
        assertNull(sector2.getNextSector());
        assertNull(sector2.getOwningList());
    }
    
}
