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

import com.sun.squawk.util.SquawkHashtable;
import com.sun.squawk.util.SquawkVector;

/**
 * Place holder for all peripheral types known by Squawk.
 * 
 *
 */
public class PeripheralRegistry {
    private final static int DEFAULT_SIZE = 3;
    
    protected final SquawkVector registeredPeripherals;
    protected SquawkHashtable peripheralArraysByType;
    
    public PeripheralRegistry() {
        registeredPeripherals = new SquawkVector(DEFAULT_SIZE);
    }
    
    public void add(IPeripheral peripheral) {
        registeredPeripherals.addElement(peripheral);
        peripheralArraysByType = null;
    }
    
    public IPeripheral getSingleton(Class type) {
        IPeripheral[] ofType = getAll(type);
        if (ofType.length == 1) {
            return ofType[0];
        }
        return null;
    }
    
    /**
     * Return all peripherals of type <code>type</code>.
     * @param type
     * @return array of peripherals
     */
    public IPeripheral[] getAll(Class type) {
        if (peripheralArraysByType == null) {
            peripheralArraysByType = new SquawkHashtable(DEFAULT_SIZE);
        }
        IPeripheral[] peripherals = (IPeripheral[]) peripheralArraysByType.get(type);
        if (peripherals == null) {
            SquawkVector ofType = new SquawkVector();
            for (int i=0, max=registeredPeripherals.size(); i < max; i++) {
                IPeripheral peripheral = (IPeripheral) registeredPeripherals.elementAt(i);
                if (type.isAssignableFrom(peripheral.getClass())) {
                    ofType.addElement(peripheral);
                }
            }
            peripherals = new IPeripheral[ofType.size()];
            ofType.copyInto(peripherals);
            peripheralArraysByType.put(peripherals.getClass(), peripherals);
        }
        return peripherals;
    }
    
    public void removeAll(Class type) {
        peripheralArraysByType = null;
        for (int i=0; i < registeredPeripherals.size(); ) {
            Object peripheral = registeredPeripherals.elementAt(i);
            if (type.isAssignableFrom(peripheral.getClass())) {
                registeredPeripherals.removeElementAt(i);
            } else {
                i++;
            }
        }
    }
    
}
