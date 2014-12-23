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
package com.sun.cldc.jna.platform;

import com.sun.cldc.jna.Platform;

/**
 *
 * @author dw29446
 */
public class Spot extends Platform {

    /**
     * Get the name of the package that contains the native implementation for this platform:
     */
    public String getPlatformPackageName() {
        return "com.sun.squawk.platform.spot";
    }

    /**
     * Get the name of the package that contains the native implementation for this platform:
     */
    public String getPlatformNativePackageName() {
        return "com.sun.squawk.platform.spot.natives";
    }

    public Spot() {
    }
}
