//if[!AUTOGEN_JNA_NATIVES]
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

/* **** HAND_GENERATED FILE
 */

package com.sun.squawk.platform.posix.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public abstract class SocketImpl implements Socket {

    /*----------------------------- methods -----------------------------*/
    private final Function getsockoptPtr;
    
    public int getsockopt(int arg0, int arg1, int arg2, ByReference arg3, IntByReference arg4) {
        Pointer var3 = arg3.getPointer();
        Pointer var4 = arg4.getPointer();
        int result0 = getsockoptPtr.call5(arg0, arg1, arg2, var3, var4);
        int result = (int)result0;
        return result;
    }
    
    private final Function bindPtr;
    
    public int bind(int arg0, sockaddr_in arg1, int arg2) {
        arg1.allocateMemory();
        arg1.write();
        Pointer var1 = arg1.getPointer();
        int result0 = bindPtr.call3(arg0, var1, arg2);
        int result = (int)result0;
        arg1.read();
        arg1.freeMemory();
        return result;
    }
    
    private final Function listenPtr;
    
    public int listen(int arg0, int arg1) {
        int result0 = listenPtr.call2(arg0, arg1);
        int result = (int)result0;
        return result;
    }
    
    private final Function shutdownPtr;
    
    public int shutdown(int arg0, int arg1) {
        int result0 = shutdownPtr.call2(arg0, arg1);
        int result = (int)result0;
        return result;
    }
    
    private final Function inet_ntopPtr;
    
    public String inet_ntop(int arg0, IntByReference arg1, Pointer arg2, int arg3) {
        Pointer var1 = arg1.getPointer();
        int result0 = inet_ntopPtr.call4(arg0, var1, arg2, arg3);
        String result = Function.returnString(result0);
        return result;
    }
    
    private final Function socketPtr;
    
    public int socket(int arg0, int arg1, int arg2) {
        int result0 = socketPtr.call3(arg0, arg1, arg2);
        int result = (int)result0;
        return result;
    }
    
    private final Function acceptPtr;
    
    public int accept(int arg0, sockaddr_in arg1, IntByReference arg2) {
        arg1.allocateMemory();
        arg1.write();
        Pointer var1 = arg1.getPointer();
        Pointer var2 = arg2.getPointer();
        int result0 = acceptPtr.call3(arg0, var1, var2);
        int result = (int)result0;
        arg1.read();
        arg1.freeMemory();
        return result;
    }
    
    private final Function connectPtr;
    
    public int connect(int arg0, sockaddr_in arg1, int arg2) {
        arg1.allocateMemory();
        arg1.write();
        Pointer var1 = arg1.getPointer();
        int result0 = connectPtr.call3(arg0, var1, arg2);
        int result = (int)result0;
        arg1.read();
        arg1.freeMemory();
        return result;
    }
    
    private final Function setsockoptPtr;
    
    public int setsockopt(int arg0, int arg1, int arg2, ByReference arg3, int arg4) {
        Pointer var3 = arg3.getPointer();
        int result0 = setsockoptPtr.call5(arg0, arg1, arg2, var3, arg4);
        int result = (int)result0;
        return result;
    }
    
    private final Function inet_ptonPtr;
    
    public boolean inet_pton(String arg0, IntByReference arg1) {
        Pointer var0 = Pointer.createStringBuffer(arg0);
        Pointer var1 = arg1.getPointer();
        int result0 = inet_ptonPtr.call2(var0, var1);
        boolean result = (result0 == 0) ? false : true;
        var0.free();
        return result;
    }
    
    public SocketImpl() {
        NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();
        getsockoptPtr = jnaNativeLibrary.getFunction("getsockopt");
        bindPtr = jnaNativeLibrary.getFunction("bind");
        listenPtr = jnaNativeLibrary.getFunction("listen");
        shutdownPtr = jnaNativeLibrary.getFunction("shutdown");
        inet_ntopPtr = jnaNativeLibrary.getFunction("inet_ntop");
        socketPtr = jnaNativeLibrary.getFunction("socket");
        acceptPtr = jnaNativeLibrary.getFunction("accept");
        connectPtr = jnaNativeLibrary.getFunction("connect");
        setsockoptPtr = jnaNativeLibrary.getFunction("setsockopt");
        inet_ptonPtr = jnaNativeLibrary.getFunction("inet_pton");
    }
    
    public static class sockaddr_inImpl extends DynamicStructure {
    
        protected sockaddr_inImpl() {
            sockaddr_in o = (sockaddr_in)this;
			o.sin_len = size(); // default....
		}

        private final static int[] layout = initLayout(sockaddr_inImpl.class, 4);
        
        /* layout indexes */
        final static int SIN_LEN_INDEX = 1;
        final static int SIN_FAMILY_INDEX = 2;
        final static int SIN_PORT_INDEX = 3;
        final static int SIN_ADDR_INDEX = 4;
        
        public int[] getLayout() {
            return layout;
        }

        public void read() {
            Pointer p = getPointer();
            sockaddr_in o = (sockaddr_in)this;
            if (layout[SIN_LEN_INDEX] >= 0) {
                o.sin_len = p.getByte(layout[SIN_LEN_INDEX]) & 0xFF;
                o.sin_family = p.getByte(layout[SIN_FAMILY_INDEX]) & 0xFF;
            } else {    // Solaris and ?
                o.sin_family = p.getShort(layout[SIN_FAMILY_INDEX]) & 0xFFFF;
            }
            o.sin_port    = p.getShort(layout[SIN_PORT_INDEX]) & 0xFFFF;
            o.sin_addr    = p.getInt(layout[SIN_ADDR_INDEX]);
        }

        public void write() {
            Pointer p = getPointer();
            sockaddr_in o = (sockaddr_in)this;
            clear();
            if (layout[SIN_LEN_INDEX] >= 0) {
                p.setByte(layout[SIN_LEN_INDEX], (byte) o.sin_len);
                p.setByte(layout[SIN_FAMILY_INDEX], (byte) o.sin_family);
            } else {   // Solaris and ?
                p.setShort(layout[SIN_FAMILY_INDEX], (byte) o.sin_family);
            }
            p.setShort(layout[SIN_PORT_INDEX],  (short)o.sin_port);
            p.setInt(layout[SIN_ADDR_INDEX],    o.sin_addr);
        }

    }
    

}


