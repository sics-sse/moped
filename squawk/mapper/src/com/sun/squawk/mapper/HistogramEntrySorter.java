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

import java.util.Comparator;

/**
 * A Comparator to sort histogram entries by their 'count' fields.
 */
public class HistogramEntrySorter implements Comparator<HistogramEntry> {
    /**
     * Specifies if sorting is to be by the 'count' or 'size' field.
     */
    private final boolean sortByCount;
    
    /**
     * Create a sorter to sort HistogramEntrys by their 'count' or 'size' fields.
     *
     * @param sortByCount  specifies if sorting is to be by the 'count' or 'size' field
     */
    HistogramEntrySorter(boolean sortByCount) {
        this.sortByCount = sortByCount;
    }
    
    /**
     * Compares two HistogramEntrys.
     *
     * @param o1 Object
     * @param o2 Object
     * @return int
     */
    public int compare(HistogramEntry e1, HistogramEntry e2) {
        if (e1 == e2) {
            return 0;
        }
        int result = sortByCount ? e1.count - e2.count : e1.size - e2.size;
        if (result == 0) {
            return e1.klassName.compareTo(e2.klassName);
        } else {
            return result;
        }
    }
}
