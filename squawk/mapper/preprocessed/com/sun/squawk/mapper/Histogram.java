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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sun.squawk.Klass;

/*---------------------------------------------------------------------------*\
 *                        Histogram                                          *
\*---------------------------------------------------------------------------*/

/**
 * Stores a histogram of how many instances of each type are present in the object memory
 * currently being dumped.
 */
public class Histogram {
    
    /**
     * Table of klass -> HistogramEntry
     */
    private HashMap<Klass, HistogramEntry> histogram = new HashMap<Klass, HistogramEntry>();
    
    /**
     * Sum of the HistogramEntry counts
     */
    public int totalCount = 0;
    
    /**
     * Sum of the HistogramEntry sizes (in bytes)
     */
    public int totalSize = 0;
    
    /**
     * Updates the histogram entry for a given class based on an instance being parsed by the caller.
     *
     * @param klass the klass of the instance
     * @param size  the size (in bytes) of the instance and its header
     */
    public void updateFor(Klass klass, int size) {
        HistogramEntry entry = histogram.get(klass);
        if (entry == null) {
            entry = new HistogramEntry(klass);
        }
        entry.count++;
        entry.size += size;
        totalCount++;
        totalSize += size;
        histogram.put(klass, entry);
    }
    
    /**
     * Dumps the histogram of instance frequencies or sizes for the current object memory.
     *
     * @param ofCounts  specifies if the histogram of counts or sizes is to be dumped
     */
    public void dump(PrintStream out, String prefix, boolean ofCounts) {
        SortedSet<HistogramEntry> sorted = new TreeSet<HistogramEntry>(new HistogramEntrySorter(ofCounts));
        sorted.addAll(histogram.values());
        
        for (HistogramEntry entry : sorted) {
            int value = (ofCounts ? entry.count : entry.size);
            String sValue = "" + value;
            out.print(prefix + sValue);
            for (int i = sValue.length(); i < 10; ++i) {
                out.print(' ');
            }
            out.println(entry.klassName);
        }
    }
}
