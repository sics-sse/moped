//if[OLD_IIC_MESSAGES]
///*
// * Copyright 1994-2008 Sun Microsystems, Inc. All Rights Reserved.
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
// * 
// * This code is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License version 2
// * only, as published by the Free Software Foundation.
// * 
// * This code is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * General Public License version 2 for more details (a copy is
// * included in the LICENSE file that accompanied this code).
// * 
// * You should have received a copy of the GNU General Public License
// * version 2 along with this work; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// * 02110-1301 USA
// * 
// * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
// * Park, CA 94025 or visit www.sun.com if you need additional
// * information or have any questions.
// */
//
//package com.sun.squawk.io.j2me.msg;
//
//import java.io.*;
//
///**
// * A <code>ByteArrayInputStream</code> contains
// * an internal buffer that contains bytes that
// * may be read from the stream. An internal
// * counter keeps track of the next byte to
// * be supplied by the <code>read</code> method.
// *
// */
//interface MessageStreamCallback {
//
//    /**
//     * Signals that the input was opened.
//     *
//     * @param in the input stream
//     */
//    public abstract void inputUsed(MessageInputStream in) throws IOException;
//
//    /**
//     * Signals that the input was closed.
//     *
//     * @param in the input stream
//     */
//    public abstract void inputClosed(MessageInputStream in) throws IOException;
//
//    /**
//     * Signals that the output was flushed.
//     *
//     * @param out the output stream
//     */
//    public abstract void outputFlushed(MessageOutputStream out) throws IOException;
//
//    /**
//     * Signals that the input was closed.
//     *
//     * @param out the output stream
//     */
//    public abstract void outputClosed(MessageOutputStream out) throws IOException;
//
//}
//
//
