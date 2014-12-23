//if[RESOURCE.CONNECTION]
/*
 * Copyright 1999-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.io.j2me.resource;

import java.io.*;
import javax.microedition.io.*;
import com.sun.squawk.io.ConnectionBase;
import com.sun.squawk.VM;
import com.sun.squawk.Suite;

/**
 * This class implements the default "resource:" protocol for KVM.
 *
 *
 * @version 1.0 2/12/2000
 */
public class Protocol extends ConnectionBase implements InputConnection {

	private byte [] resourceData;

    /**
     * Open the connection
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {
		resourceData = null;

		// look for the resource file in the current leaf suite, and then up the chain of parent suites until we find it
        Suite suite = VM.getCurrentIsolate().getLeafSuite();
        resourceData = suite.getResourceData(name);
        if (resourceData != null) {
            return this;
        }
        throw new ConnectionNotFoundException(name);
    }

    public InputStream openInputStream()
            throws IOException {
        // the resource file is stored in one of the suites in memory, so create a new input stream from there...
        return new ByteArrayInputStream(resourceData);
    }

    public void close() throws IOException {
    }
}
