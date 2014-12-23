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

package com.sun.squawk.uei.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Displays the output of an ant process, filtering stdout to remove the ant
 * gunk([echo], [java], etc.) and displaying stderr verbatim.
 * 
 * @author jfim
 */
public class OutputDisplayer {
    private final OutputDisplayer displayerInstance = this;
    private final Process process;
    private boolean threadHasFinished = false;
    
    public OutputDisplayer(final Process process) {
        this.process = process;
    }
    
    public int waitForProcessEnd() {
        final OutputStream out = System.out;
        final OutputStream err = System.err;
        final InputStream in = System.in;
        
        // Pipe stdin into process
        Thread stdinThread = new Thread(new Runnable() {
            public void run() {
                OutputStream stdin = process.getOutputStream();
                byte buffer[] = new byte[4096];
                
                try {
                    while (!threadHasFinished) {
                        int availableBytes = in.available();
                        
                        if (availableBytes > 0) {
                            int readBytes = in.read(buffer);
                            stdin.write(buffer, 0, readBytes);
                            stdin.flush();
                        }
                        
                        try {
                            Thread.sleep(50);
                        } catch (final InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    
                    stdin.close();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        // Display stderr verbatim, notifying the displayer when EOF is reached
        Thread stderrThread = new Thread(new Runnable() {
            public void run() {
                InputStream stderr = process.getErrorStream();
                byte buffer[] = new byte[4096];
                
                try {
                    // Attempt to read from stderr
                    int numBytesRead = stderr.read(buffer);
                    
                    while (numBytesRead != -1) {
                        // Display bytes read from stderr
                        if (numBytesRead > 0) {
                            synchronized (err) {
                                err.write(buffer, 0, numBytesRead);
                            }
                        }
                        
                        numBytesRead = stderr.read(buffer);
                    }
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
                
                synchronized (displayerInstance) {
                    // Mark as having finished
                    threadHasFinished = true;
                    
                    // Wake up the main thread
                    displayerInstance.notifyAll();
                }
            }
        });
        
        // Display stdout verbatim, notifying the displayer when EOF is reached
        Thread stdoutThread = new Thread(new Runnable() {
            public void run() {
                InputStream stdout = process.getInputStream();
                byte buffer[] = new byte[4096];
                
                try {
                    // Attempt to read from stdout
                    int numBytesRead = stdout.read(buffer);
                    
                    while (numBytesRead != -1) {
                        // Display bytes read from stdout
                        if (numBytesRead > 0) {
                            synchronized (out) {
                                out.write(buffer, 0, numBytesRead);
                            }
                        }
                        
                        numBytesRead = stdout.read(buffer);
                    }
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
                
                synchronized (displayerInstance) {
                    // Mark as having finished
                    threadHasFinished = true;
                    
                    // Wake up the main thread
                    displayerInstance.notifyAll();
                }
            }
        });
        
        // Start all threads
        stdinThread.start();
        stderrThread.start();
        stdoutThread.start();
        
        // Wait for at least one thread to finish
        while (!threadHasFinished) {
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (final InterruptedException ex) {
                // Ignore spurious wake ups
            }
        }
        
        // At least one thread has finished, wait for all of them to finish
        while (stdinThread.isAlive()) {
            try {
                stdinThread.join();
            } catch (final InterruptedException ex) {
                // Ignore spurious wake ups
            }
        }
        
        while (stderrThread.isAlive()) {
            try {
                stderrThread.join();
            } catch (final InterruptedException ex) {
                // Ignore spurious wake ups
            }
        }
        
        while (stdoutThread.isAlive()) {
            try {
                stdoutThread.join();
            } catch (final InterruptedException ex) {
                // Ignore spurious wake ups
            }
        }
        
        return process.exitValue();
    }
}