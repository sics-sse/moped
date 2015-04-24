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

package com.sun.squawk;

import com.sun.squawk.pragma.*;

/**
 * This class defines the global Squawk variables that are used to communicate between
 * a normal Java thread and the Squawk system service thread.
 *
 */
public final class ServiceOperation implements GlobalStaticFields {
    
    /**
     * Purely static class should not be instantiated.
     */
    private ServiceOperation() {}

    /**
     * The an invalid service operation code.
     */
    public final static int NONE = 0;

    /**
     * Extend the current thread.
     */
    public final static int EXTEND = 1;

    /**
     * Collect the garbage.
     */
    public final static int GARBAGE_COLLECT = 2;

    /**
     * Copy an object graph.
     */
    public final static int COPY_OBJECT_GRAPH = 3;

    /**
     * Throw an exception.
     */
    public final static int THROW = 4;

    /**
     * Execute a channel I/O operation.
     */
    public final static int CHANNELIO = 5;

    /**
     * The service operation code.
     */
    private static int code;

    /**
     * The channel context (only used for CHANNELIO).
     */
    private static int context;

    /**
     * The channel operation code (only used for CHANNELIO).
     */
    private static int op;

    /**
     * The channel identifier (only used for CHANNELIO).
     */
    private static int channel;

    /**
     * Integer parameters.
     */
    private static int i1, i2, i3, i4, i5, i6;

    /**
     * Object parameters.
     */
    private static Address o1, o2;

    /**
     * The result code.
     */
    private static int result;

    /**
     * The address result in message I/O operations.
     */
    private static Address addressResult;

    /**
     * The pending exception for the next catch bytecode.
     */
     static Throwable pendingException;

    /**
     * This is the service thread operation loop.
     */
    static void execute() {
        try {
        for (;;) {
            VM.threadSwitch();
            switch(code) {
                case EXTEND: {
                    if (VMThread.extendStack(i1)) {
                        break;
                    }
/*if[DEBUG_CODE_ENABLED]*/
                    VM.print("VMThread.extendStack() failed!\r\n");
/*end[DEBUG_CODE_ENABLED]*/
                    pendingException = VM.getOutOfMemoryError(); // and drop through to THROW
                }
                case THROW: {
                	VM.print("throw\r\n");
                	VM.print(pendingException+"\r\n");
                    VM.throwException(pendingException);
                    break;
                }
                case GARBAGE_COLLECT: {
                    GC.collectGarbage(i1 != 0);
                    break;
                }
/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//                case COPY_OBJECT_GRAPH: {
//                    GC.copyObjectGraph(o1, (ObjectMemorySerializer.ControlBlock)o2.toObject());
//                    break;
//                }
/*end[ENABLE_ISOLATE_MIGRATION]*/
                case CHANNELIO: {
                    cioExecute();
                    break;
                }
                default: {
                	VM.print("default\r\n");
                    VM.fatalVMError();
                    break;
                }
            }
            o1 = null;
            o2 = null;
        }
        } finally {
            // This should not happen, but am tracking odd failure...
            VM.println("NOTE: Exiting ServiceOperation.execute()");
        }
    }

    /**
     * Execute a channel I/O operation.
     */
    private native static void cioExecute();
    
}






