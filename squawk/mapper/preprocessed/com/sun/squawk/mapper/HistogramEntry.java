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

import com.sun.squawk.Klass;

/**
 * A HistogramEntry instance records the frequency and total size of instances of a single class.
 */
public class HistogramEntry {
    /**
     * Name of the class.
     */
    final String klassName;
    
    /**
     * The klass
     */
    final Klass klass;
    
    
    /**
     * Frequency count of instances of the class.
     */
    int count;
    
    /**
     * Total size (in bytes) of all the instances of the class which includes the size of the instance headers.
     */
    int size;
    
    HistogramEntry(Klass klass) {
        this.klass = klass;
        this.klassName = klass.getName();
    }
    
}
