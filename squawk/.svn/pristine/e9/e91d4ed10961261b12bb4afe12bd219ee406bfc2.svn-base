/*
 * Copyright 2008-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.io.j2se.uei;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wait for arguments to be coming in from the Squawk side of the channel interface.
 * We will get an argument count, followed by UTF strings for each argument.
 * <code>close()</code> will indicate when the Squawk side is done sending all data
 * and I can kick off the processing of the arguments.
 */
public class ArgumentOutputStream extends OutputStream {
    protected ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    protected Protocol ueiProtocol;
    
    public ArgumentOutputStream(Protocol ueiProtocol) {
        this.ueiProtocol = ueiProtocol;
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(bytesOut.toByteArray()));
        String[] args = new String[dataIn.readInt()];
        for (int i=0; i < args.length; i++) {
            args[i] = dataIn.readUTF();
        }
        ueiProtocol.invokeEmulator(args);
    }

    @Override
    public void write(int b) throws IOException {
        ueiProtocol.log("byte: " + b + ":" + (char) b);
        bytesOut.write(b);
    }

}
