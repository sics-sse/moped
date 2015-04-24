/*
 * Copyright 2006-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2010-2011 Oracle. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import com.sun.squawk.flash.IMemoryHeapBlock;
import com.sun.squawk.util.DataOutputUTF8Encoder;

public class ApplicationDescriptorEntry extends RmsEntry implements IApplicationDescriptorEntry {
    public static final String PROPERTY_MIDLET_NAME = "MIDlet-Name";
    public static final String PROPERTY_MIDLET_VENDOR = "MIDlet-Vendor";
    public static final String PROPERTY_MIDLET_VERSION = "MIDlet-Version";
    public static final int TYPE = 1;

    protected String midletName;
    protected String midletVendor;
    protected String midletVersion;

    public ApplicationDescriptorEntry() {
        midletName = "";
        midletVendor = "";
        midletVersion = "";
    }
    
    public String getMidletName() {
        return midletName;
    }
    
    public String getMidletVendor() {
        return midletVendor;
    }
    
    public String getMidletVersion() {
        return midletVersion;
    }
    
    public int getType() {
        return TYPE;
    }

    public void readFrom(IMemoryHeapBlock memoryBlock) throws IOException {
        super.readFrom(memoryBlock);
        DataInputStream input = memoryBlock.getDataInputStream();
        midletName = input.readUTF();
        midletVersion = input.readUTF();
        midletVendor = input.readUTF();
    }
    
    public void setMidletName(String name) {
        midletName = name;
    }
    
    public void setMidletVendor(String vendor) {
        midletVendor = vendor;
    }
    
    public void setMidletVersion(String version) {
        midletVersion = version;
    }
    
    public void visit(IRmsEntryVisitor visitor) throws RecordStoreException {
        visitor.visitApplicationDescriptor(this);
    }
    
    public void writeTo(DataOutputStream dataOut) throws IOException {
        super.writeTo(dataOut);
        dataOut.writeUTF(midletName);
        dataOut.writeUTF(midletVersion);
        dataOut.writeUTF(midletVendor);
    }

    public int size() {
        return (2 + DataOutputUTF8Encoder.lengthAsUTF(midletName)) +
               (2 + DataOutputUTF8Encoder.lengthAsUTF(midletVersion)) +
               (2 + DataOutputUTF8Encoder.lengthAsUTF(midletVendor));
    }
    
}
