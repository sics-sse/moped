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

package com.sun.squawk.io.connections;

import java.io.*;
import javax.microedition.io.*;

public interface ClasspathConnection extends Connection {

    public InputStream openInputStream(String name) throws IOException;
    
    /**
     * Convenience method that provides access to the bytes to be found by doing a {@link #openInputStream(String) }
     * on file named <code>name</code>.
     * 
     * @param name Name of the resource to open and fetch bytes from
     * @return byte[] bytes read in
     * @throws IOException if any error occurs opening or reading bytes from the stream {@link #openInputStream(String) }
     */
    public byte[] getBytes(String name) throws IOException;

}

