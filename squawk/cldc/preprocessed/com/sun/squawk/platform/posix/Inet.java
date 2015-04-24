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

package com.sun.squawk.platform.posix;

import com.sun.squawk.VM;

/**
 *
 * Convert values between host and network byte order.
 * 
 * This is an implementation of byte order conversion routines.
 */
public class Inet {
    
    /* pure static class */
    private Inet() {}
    
    public static short swap2(int value) {
        int b0 = value & 0xFF;
        int b1 = (value >> 8) & 0xFF;
        return (short) ((b0 << 8) | b1);
    }

    public static int swap4(int value) {
        int b0 = value & 0xFF;
        int b1 = (value >> 8) & 0xFF;
        int b2 = (value >> 16) & 0xFF;
        int b3 = (value >> 24) & 0xFF;
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    public static int htonl(int hostint) {
        if (VM.isBigEndian()) {
            return hostint;
        } else {
            return swap4(hostint);
        }
    }

    public static short htons(short hostshort) {
        if (VM.isBigEndian()) {
            return hostshort;
        } else {
            return swap2(hostshort);
        }        
    }

    public static int ntohl(int netint) {
        if (VM.isBigEndian()) {
            return netint;
        } else {
            return swap4(netint);
        }
    }

    public static short ntohs(short netshort) {
         if (VM.isBigEndian()) {
            return netshort;
        } else {
            return swap2(netshort);
        }  
    }

}
