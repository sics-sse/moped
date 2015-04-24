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

import java.util.HashMap;

import com.sun.squawk.util.Assert;
import java.util.Set;

/**
 * Special kind of OwnerStats for classes.
 */
public class ClassOwnerStats extends OwnerStats {
    
    /**
     * Table of name -> ClassOwnerStats.
     */
    public static HashMap<String, ClassOwnerStats> allKlassStats;
    
    public ClassOwnerStats(String name) {
        super(name);
        Object oldval = allKlassStats.put(name, this);
        Assert.that(oldval == null);
    }
    
    public boolean isClass() {
        return true;
    }
    
    /**
     * Look up the ClassOwnerStats for the named class.
     *
     * @param name the getName() of the klass.
     * @rteurn the ClassOwnerStats for the klass.
     */
    public static ClassOwnerStats getKlassStats(String name) {
        return allKlassStats.get(name);
    }
    
    public static void reset() {
        allKlassStats = new HashMap<String, ClassOwnerStats>();
    }
    
    public static Set<String> getKlassNames() {
        return allKlassStats.keySet();
    }
    
} // class ClassOwnerStats
