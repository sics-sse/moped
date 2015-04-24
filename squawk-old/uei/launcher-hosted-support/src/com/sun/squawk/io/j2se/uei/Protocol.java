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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.microedition.io.Connection;
import javax.microedition.io.StreamConnection;

import com.sun.squawk.io.ConnectionBase;
import com.sun.squawk.uei.UeiCommandProcessor;

/**
 * Provides a connection to launch the squawk emulator via squawk on the
 * underlying java VM.
 */
public class Protocol extends ConnectionBase implements StreamConnection {
    protected ArgumentOutputStream outputStream;
    protected ReturnCodeInputStream inputStream;
    protected int returnCode;
    boolean invoked;
    PrintStream log;
    
    
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {
        returnCode = 0;
        try {
            log = new PrintStream(new FileOutputStream(new File("debug.txt")));
        } catch (IOException e) {
        }
        log("Created Connection");
        return this;
    }

    public void log(String message) {
        log.print(message);
        log.println();
        log.flush();
    }
    
    @Override
    public InputStream openInputStream() throws IOException {
        if (inputStream != null) {
            throw new IOException("Already open");
        }
        log("openInputStream");
        inputStream = new ReturnCodeInputStream(this);
        return inputStream;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        if (outputStream != null) {
            throw new IOException("Already open");
        }
        log("openOutputStream");
        outputStream = new ArgumentOutputStream(this);
        return outputStream;
    }
    
    public void invokeEmulator(String[] args) {
        // Indicate an error by default
        returnCode = -1;
        invoked = true;
        log("invokeEmulator");
        UeiCommandProcessor launcher = new UeiCommandProcessor();
        log("   invokeEmulator:a");
        returnCode = launcher.run(args);
        log("   invokeEmulator:" + returnCode);
    }
    
    public int getReturnCode() throws IOException {
        if (!invoked) {
            throw new IOException("Have not invoked emulator yet");
        }
        return returnCode;
    }
    
}
