/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.compiler;

import java.util.*;

/**
 *
 * @version 1.0
 */
public class SymbolicFixupTable {
    public SymbolicFixupTable() {
        fixupTables = new Hashtable ();
    }

    /**
     * Integer -> Hashtable
     */
    private Hashtable fixupTables;

    private Hashtable findCategory (String category) {
        if (!fixupTables.containsKey(category)) {
            fixupTables.put(category, new Hashtable ());
        }

        return (Hashtable) fixupTables.get(category);
    }

    public void addFixup(String category, int position, String symbol) {
        findCategory(category).put(new Integer(position), symbol);
    }

    public Enumeration getFixups(String category) {
        return findCategory(category).keys();
    }

    public String getFixupSymbol(String category, int position) {
        Hashtable categoryTable = findCategory(category);
        Integer pos = new Integer (position);
        if (categoryTable.containsKey(pos)) {
            return (String) categoryTable.get(pos);
        } else {
            return null;
        }
    }

    public void print() {
        System.out.println("Symbolic fixups:");
        Enumeration categories = fixupTables.keys();
        while (categories.hasMoreElements()) {
            String category = (String) categories.nextElement();
            Enumeration fixups = getFixups(category);
            while (fixups.hasMoreElements()) {
                Integer pos = (Integer) fixups.nextElement();
                System.out.println(category + "\t" + pos + "\t" + getFixupSymbol(category, pos.intValue()));
            }
        }
    }

    public static final String ABS_32 = "abs32";
    public static final String REL_32 = "rel32";
    public static final String REL_24 = "rel24";
}
