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

package com.sun.squawk.mapper;

import com.sun.squawk.util.Comparer;

/**
 * Records the total size in bytes of objects owned by a class or package.
 */
public class OwnerStats {
    /**
     * Name of the class or package
     */
    public final String name;
    
    /**
     * Total size of a class, or all of the classes in a packge
     */
    public int size;
    
    public OwnerStats(String name) {
        this.name = name;
        this.size = 0;
    }
    
    public void updateSize(int delta) {
        size += delta;
    }
    
    public static class CompareByName implements Comparer {
        public int compare(Object a, Object b) {
            return ((OwnerStats)a).name.compareTo(((OwnerStats)b).name);
        }
    }
    
    public boolean isClass() {
        return false;
    }
}
