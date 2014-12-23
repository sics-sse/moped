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

package com.sun.squawk.platform.posix.natives;

import com.sun.cldc.jna.*;

/**
 *
 * java wrapper around #include <sys/ioctl.h>
 */
@Includes({"<sys/types.h>", "<sys/ioctl.h>", "<sys/filio.h>"})
public interface Ioctl extends Library {

    Ioctl INSTANCE = (Ioctl)
            Native.loadLibrary("RTLD",
                               Ioctl.class);

    /*
     * Ioctl's have the command encoded in the lower word, and the size of
     * any in or out parameters in the upper word.  The high 3 bits of the
     * upper word are used to encode the in/out status of the parameter.
     */

    public final static int IOCPARM_MASK = IMPORT;		/* parameter length, at most 13 bits */

   // public final static int IOCPARM_MAX = IMPORT;	/* max size of ioctl args */
    /* no parameters */
    public final static int IOC_VOID = IMPORT;
    /* copy parameters out */
    public final static int IOC_OUT = IMPORT;
    /* copy parameters in */
    public final static int IOC_IN = IMPORT;
    /* copy paramters in and out */
    public final static int IOC_INOUT = IMPORT;
    /* mask for IN/OUT/VOID */
    //public final static int IOC_DIRMASK = IMPORT;


    public final static int FIOCLEX = IMPORT;		/* set close on exec on fd */
    public final static int FIONCLEX = IMPORT;		/* remove close on exec */
    public final static int FIONREAD = IMPORT;	/* get # bytes to read */
    public final static int FIONBIO = IMPORT;	/* set/clear non-blocking i/o */
    public final static int FIOASYNC = IMPORT;	/* set/clear async i/o */
    public final static int FIOSETOWN = IMPORT;	/* set owner */
    public final static int FIOGETOWN = IMPORT;	/* get owner */
    //public final static int FIODTYPE = IMPORT;	    /* get d_type */


    /**
     * Perform IO control operation <code>request</code> on device <code>fd</code>.
     *
     * @param fd an open file descriptor
     * @param request am encded value containing the requested operation and the arguments
     * @param i1
     * @return -1 on error
     */
    int ioctl(int fd, int request, int i1);

    /**
     * Perorm IO control operation <code>request</code> on device <code>fd</code>.
     *
     * @param fd an open file descriptor
     * @param request am encded value containing the requested operation and the arguments
     * @param i1
     * @param i2
     * @return -1 on error
     */
    int ioctl(int fd, int request, int i1, int i2);

    /**
     * Perorm IO control operation <code>request</code> on device <code>fd</code>.
     *
     * @param fd an open file descriptor
     * @param request am encded value containing the requested operation and the arguments
     * @param i1
     * @param i2
     * @param i3
     * @return -1 on error
     */
    int ioctl(int fd, int request, int i1, int i2, int i3);

}
