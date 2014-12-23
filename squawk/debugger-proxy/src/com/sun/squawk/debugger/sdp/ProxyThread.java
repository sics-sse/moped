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

package com.sun.squawk.debugger.sdp;

import com.sun.squawk.debugger.*;
import com.sun.squawk.debugger.DataType.*;
import com.sun.squawk.*;

/**
 * Mirrors a thread on the Squawk VM. This is used to reduce VM <-> proxy traffic
 * when a debug client (e.g. Netbeans) makes a large amount of thread status requests
 * even when there hasn't been any notified change in thread status.
 *
 */
class ProxyThread extends Thread {

    public final ObjectID id;
    private int suspendCount;
    private int status;

    public ProxyThread(ObjectID id, String name, int status, int suspendCount) {
        super(name);
        this.id = id;
        this.suspendCount = suspendCount;
        this.status = status;
    }

    public void setStatus(int s) {
        status = s;
    }

    public int getStatus() {
        return status;
    }

    public void setSuspendCount(int count) {
        suspendCount = count;
        if (count == 0) {
            if (Log.info()) {
                Log.log("resumed thread " + getName());
            }
        } else {
            if (Log.info()) {
                Log.log("suspended thread " + getName());
            }
        }
    }

    public int getSuspendCount() {
        return suspendCount;
    }

    public boolean isSuspended() {
        return suspendCount > 0;
    }

    public boolean isZombie() {
        return status == JDWP.ThreadStatus_ZOMBIE;
    }

    public String toString() {
        return "" + id + ": " + getName() + " status=" + status + " suspendCount=" + suspendCount;
    }
}
