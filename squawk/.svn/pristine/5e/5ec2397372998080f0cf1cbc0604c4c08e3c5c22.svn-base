/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.io.j2se;

import java.util.*;

/**
 * Helper class for parsing extra parameters from a connection URL.
 *
 */
public abstract class ParameterParser {

    /**
     * Parses the <name>=<value> pairs separated by ';' in a URL name. The pairs
     * start after the first ';' in the given name.
     *
     * @param name  the name part of a connection URL
     * @return the name stripped of the parameters (if any)
     */
    public String parse(String name) {
        int parmIndex = name.indexOf(';');
        if (parmIndex != -1) {
            String parms = name.substring(parmIndex);
            name = name.substring(0, parmIndex);
            StringTokenizer st = new StringTokenizer(parms, "=;", true);
            while (st.hasMoreTokens()) {
                try {
                    if (!st.nextToken().equals(";")) {
                        throw new NoSuchElementException();
                    }
                    String key = st.nextToken();
                    if (!st.nextToken().equals("=")) {
                        throw new NoSuchElementException();
                    }
                    String value = st.nextToken();
                    if (!parameter(key, value)) {
                        throw new IllegalArgumentException("Unknown parameter to protocol: " + key);
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("Bad param string: " + parms);
                }
            }
        }
        return name;
    }

    /**
     * Notifies a subclass of a <name>=<value> pair that has been parsed.
     *
     * @return true if the parameter was accepted
     *
     * @throws IllegalArgumentException
     */
    public abstract boolean parameter(String name, String value) throws IllegalArgumentException;
}
