/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011-20012 Oracle Corporation. All Rights Reserved.
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
package tests;

import com.sun.squawk.GC;
import com.sun.squawk.VM;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class HelloWorld extends MIDlet {
    protected void destroyApp(boolean arg0)
            throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }
    public final static int COUNT = 100;

    protected void startApp() throws MIDletStateChangeException {
        System.out.println("TimeIt");
        long t;
        long bytes;
        long throwCount;
        Object mark;

        System.out.println("---------------------------");
        Utilities.getUTCTimestamp(); // prime it....
        VM.Stats.initHeapStats();
        System.gc();
        mark = new Object();
        t = System.currentTimeMillis();
        bytes = GC.getBytesAllocatedSinceLastGC();
        throwCount = VM.Stats.getThrowCount();
        for (int i = 0; i < COUNT; i++) {
            String tmp = Utilities.getUTCTimestamp();
        }
        t = System.currentTimeMillis() - t;
        bytes = GC.getBytesAllocatedSinceLastGC() - bytes;
        throwCount = VM.Stats.getThrowCount() - throwCount;
        VM.Stats.printHeapStats(mark, false);
        System.out.println("getUTCTimestamp ORIG takes: " + (t) + "ms");
        System.out.println("    bytes: " + bytes);
        System.out.println("    throwCount: " + throwCount);
        System.out.println("---------------------------");
        System.out.flush();
        

        Utilities.getUTCTimestamp2(System.currentTimeMillis()); // prime it....
        VM.Stats.initHeapStats();
        System.gc();
        mark = new Object();
        t = System.currentTimeMillis();
        bytes = GC.getBytesAllocatedSinceLastGC();
        throwCount = VM.Stats.getThrowCount();
        for (int i = 0; i < COUNT; i++) {
            String tmp = Utilities.getUTCTimestamp2(System.currentTimeMillis());
        }
        t = System.currentTimeMillis() - t;
        bytes = GC.getBytesAllocatedSinceLastGC() - bytes;
        throwCount = VM.Stats.getThrowCount() - throwCount;
        VM.Stats.printHeapStats(mark, false);
        System.out.println("getUTCTimestamp 2 takes: " + (t) + "ms");
        System.out.println("    bytes: " + bytes);
        System.out.println("    throwCount: " + throwCount);
        System.out.println("---------------------------");
        System.out.flush();
        
        if (!Utilities.getUTCTimestamp(t).equals(Utilities.getUTCTimestamp2(t))) {
            System.err.println("*************");
            System.err.println("getUTCTimestamp() != getUTCTimestamp2()");
            System.err.println("*************");
        }
        
        Utilities.getUTCTimestamp3(System.currentTimeMillis()); // prime it....
        VM.Stats.initHeapStats();
        System.gc();
        mark = new Object();
        t = System.currentTimeMillis();
        bytes = GC.getBytesAllocatedSinceLastGC();
        throwCount = VM.Stats.getThrowCount();
        for (int i = 0; i < COUNT; i++) {
            String tmp = Utilities.getUTCTimestamp3(System.currentTimeMillis());
        }
        t = System.currentTimeMillis() - t;
        bytes = GC.getBytesAllocatedSinceLastGC() - bytes;
        throwCount = VM.Stats.getThrowCount() - throwCount;
        VM.Stats.printHeapStats(mark, false);
        System.out.println("getUTCTimestamp 3 takes: " + (t) + "ms");
        System.out.println("    bytes: " + bytes);
        System.out.println("    throwCount: " + throwCount);
        System.out.println("---------------------------");
        System.out.flush();
        notifyDestroyed();
    }
}