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

package com.sun.squawk.uei.j2me;

import java.io.DataOutputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * Main entry to launch the squawk emulator on the underlying java VM.
 */
public class Launcher {
    
    public static void main(String[] args) {
        // Default to indicate error
        int returnCode = -1;
        try {
            StreamConnection connection = (StreamConnection) Connector.open("uei:");
            DataOutputStream out = connection.openDataOutputStream();
            out.writeInt(args.length);
            for (int i=0; i < args.length; i++) {
                out.writeUTF(args[i]);
            }
            out.close();
            InputStream in = connection.openInputStream();
            returnCode = in.read();
            in.close();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            System.exit(returnCode);
        }
    }
    
}
