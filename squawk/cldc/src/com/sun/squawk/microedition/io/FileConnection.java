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

package com.sun.squawk.microedition.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Enumeration;

/**
 * This class is defined by the JSR-75 specification
 * <em>PDA Optional Packages for the J2ME&trade; Platform</em>
 */
// JAVADOC COMMENT ELIDED
public interface FileConnection extends javax.microedition.io.StreamConnection {

    // JAVADOC COMMENT ELIDED
    public boolean isOpen();

    // JAVADOC COMMENT ELIDED
    public InputStream openInputStream() throws IOException;
    
    // JAVADOC COMMENT ELIDED
    public DataInputStream openDataInputStream() throws IOException;
    
    // JAVADOC COMMENT ELIDED
    public OutputStream openOutputStream() throws IOException;
    
    // JAVADOC COMMENT ELIDED
    public DataOutputStream openDataOutputStream() throws IOException;
    
    // JAVADOC COMMENT ELIDED
    public OutputStream openOutputStream(long byteOffset) throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public long totalSize();
//
//    // JAVADOC COMMENT ELIDED
//    public long availableSize();
//
//    // JAVADOC COMMENT ELIDED
//   public long usedSize();
//
//    // JAVADOC COMMENT ELIDED
//    public long directorySize(boolean includeSubDirs) throws IOException;
//
    // JAVADOC COMMENT ELIDED
    public long fileSize() throws IOException;

//    // JAVADOC COMMENT ELIDED
//    public boolean canRead();
//
//    // JAVADOC COMMENT ELIDED
//    public boolean canWrite();
//
//    // JAVADOC COMMENT ELIDED
//    public boolean isHidden();
//
//    // JAVADOC COMMENT ELIDED
//    public void setReadable(boolean readable) throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public void setWritable(boolean writable)throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public void setHidden(boolean hidden) throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public Enumeration list() throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public Enumeration list(String filter, boolean includeHidden)
//	throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public void mkdir() throws IOException;
//
//    // JAVADOC COMMENT ELIDED
    public void create() throws IOException;

    // JAVADOC COMMENT ELIDED
    public abstract boolean exists();

    // JAVADOC COMMENT ELIDED
    public boolean isDirectory();

//    // JAVADOC COMMENT ELIDED
    public void delete() throws java.io.IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public abstract void rename(String newName) throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public abstract void truncate(long byteOffset) throws IOException;
//
//    // JAVADOC COMMENT ELIDED
//    public abstract void setFileConnection(String fileName) throws IOException;

    // JAVADOC COMMENT ELIDED
    public String getName();

    // JAVADOC COMMENT ELIDED
    public String getPath();

    // JAVADOC COMMENT ELIDED
    public String getURL();

//    // JAVADOC COMMENT ELIDED
//    public long lastModified();
}

