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

import com.sun.squawk.VMThread;
import java.io.IOException;

/**
 * Helper statics for LibC.
 */
public class LibCUtil {

    private static final boolean DEBUG = false;

    /**
     * Utility class that checks result. If result indicates an error (-1), then reads errno() and throws exception.
     * @param result
     * @return result
     * @throws java.io.IOException
     */
    public static int errCheckNeg(int result) throws IOException {
        if (result == -1) {
            if (DEBUG) {
                System.err.println("err = " + result);
            }
            throw new IOException("errno: " + LibCUtil.errno());
        } else {
            return result;
        }
    }

    /**
     * Utility class that warns if the result indicates an error.
     * If result indicates an error (-1), then reads errno() and prints a warning message.
     * @param result
     * @return result
     */
    public static int errWarnNeg(int result) {
        if (result == -1) {
            System.err.println("WARNING: errno: " + LibCUtil.errno());
        }
        return result;
    }

    /**
     * Return the system errno value from the last native function call made by this Java thread.
     * @return
     */
    public static int errno() {
        return VMThread.currentThread().getErrno();
    }

    private LibCUtil() { }

}