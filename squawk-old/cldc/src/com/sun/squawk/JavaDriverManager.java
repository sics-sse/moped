//if[EXCLUDE]
///*MAKE_ASSERTIONS_FATAL[true]*/
///*
// * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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
//package com.sun.squawk;
//
//import java.io.*;
//
//import javax.microedition.io.*;
//import com.sun.squawk.vm.ChannelConstants;
//import com.sun.squawk.io.*;
//import com.sun.squawk.io.j2me.msg.*;
//
//
///**
// * The Java driver manager. This manager supports dynamic loading of drivers.
// *
// */
//public class JavaDriverManager {
//
//    /**
//     * Initializes the driver used to dynamically load other drivers. It will
//     * also initialize any other drivers that were specified at startup time.
//     *
//     * @param args the command line argument array
//     */
//    public static void initializeDrivers(String[] args) {
//        (new DriverLoader()).initialize();
//        for (int i = 0; i != args.length; ++i) {
//            String name = args[i];
//            DriverLoader.loadDriver(name);
//        }
//    }
//
//    /**
//     * Sends a request to the kernel to loads and initialize a driver. This request can only be made
//     * from user code, not kernel code.
//     *
//     * @param className  fully qualified name of a class that implements {@link Driver}.
//     * @return true if the driver was successfully loaded, false otherwise
//     */
//    public static boolean loadDriver(String className) {
//        if (VM.isInKernel()) {
//            throw new IllegalStateException("cannot initiate driver load request from within kernel");
//        }
//        try {
//            StreamConnection con = (StreamConnection) Connector.open("msg:////dev-load-request");
//            DataOutputStream out = con.openDataOutputStream();
//
//            out.writeUTF(className);
//            out.close();
//
//            DataInputStream in = con.openDataInputStream();
//            boolean result = in.readBoolean();
//            in.close();
//
//            con.close();
//            return result;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    /**
//     * Returns a suitable key for an interrupt. Should be kept in sync
//     * with C-code definition of the device-name prefix.
//     *
//     * @param interrupt  identfier of interrupt to handle
//     * @return the key
//     */
//    public static String deviceInterruptName(int interrupt) {
//        return "////irq" + interrupt;
//    }
//
//    /**
//     * Main routine.
//     *
//     * @param args the command line argument array
//     */
//    public static void main(String[] args) throws Exception {
//        initializeDrivers(args);
//        while (true) {
//            VM.pause();
//            while (true) {
//                ServerConnectionHandler p = VM.getNextServerConnectionHandler();
//                if (p == null) {
//                    break;
//                }
//                p.processServerMessage();
//            }
//        }
//    }
//
//    /**
//     * Configures the interrupt handler table by indicating that a given signal/interrupt
//     * will be handled by a Java device driver. The Java device driver should already have
//     * registered its call back message system under the name returned by calling {@link #deviceInterruptName}
//     * with <code>interrupt</code>.
//     *
//     * @param interrupt  the interrupt to be handled
//     * @param handler    name of a C function that can be dynamically loaded/bound to
//     */
//    public static void setupInterrupt(int interrupt, String handler) {
//        VM.setupInterrupt(interrupt, handler);
//    }
//
///*if[KERNEL_SQUAWK_HOSTED]*/
//
//    /**
//     * Retrieves the value of a low-level statistic for a given device interrupt.
//     *
//     * @param interrupt  identifier of interrupt to query
//     * @param id         {@link #STATUS_CAUGHT}, {@link #STATUS_IGNORED} or {@link #STATUS_TIMESTAMP}
//     * @return the value of the requested status field
//     * @throws IllegalArgumentException if <code>id</code> is not one of the accepted values
//     */
//    public static long getInterruptStatus(int interrupt, int id) {
//        if (id < STATUS_CAUGHT || id > STATUS_TIMESTAMP) {
//            throw new IllegalArgumentException();
//        }
//        return VM.getInterruptStatus(interrupt, id);
//    }
//
//    /**
//     * Constant to be used with {@link #getInterruptStatus} to query the number of times
//     * a particular interrupt has been caught and handled.
//     */
//    public static final int STATUS_CAUGHT = 0;
//
//    /**
//     * Constant to be used with {@link #getInterruptStatus} to query the number of times
//     * a particular interrupt has been ignored.
//     */
//    public static final int STATUS_IGNORED = 1;
//
//    /**
//     * Constant to be used with {@link #getInterruptStatus} to query the last {@link System#currentTimeMicros time}
//     * at which a particular interrupt was caught.
//     */
//    public static final int STATUS_TIMESTAMP = 2;
//
//    /**
//     * Emulates generation of a hardware interrupt.
//     *
//     * @param signum  the signal to raise
//     */
//    public static void sendInterrupt(int signum) {
//        VM.sendInterrupt(signum);
//    }
//
//    /**
//     * Schedules the platform-specific alarm timer.
//     *
//     * @param start      interval to wait (in microseconds) before delivering first signal
//     * @param period     period to wait (in microseconds) before delivering subsequent signals
//     */
//    public static void setupAlarmInterval(int start, int period) {
//        VM.setupAlarmInterval(start, period);
//    }
//
///*end[KERNEL_SQUAWK_HOSTED]*/
//}
//
///**
// * A driver that enables dynamic device driver loading.
// */
//class DriverLoader implements Driver {
//
//    /**
//     * Initialize the driver.
//     */
//    public void initialize() {
//        ServerConnectionHandler sch;
//
//        sch = new ServerConnectionHandler("////dev-load-request") {
//            public void processConnection(StreamConnection con) {
//                processRequest(con);
//            }
//        };
//        VM.addServerConnectionHandler(sch);
//    }
//
//    /**
//     * Loads a new driver.
//     *
//     * @param con the input message connection
//     */
//    private void processRequest(StreamConnection con) {
//        try {
//            DataInputStream in = con.openDataInputStream();
//            MessageOutputStream messageOut = (MessageOutputStream) con.openOutputStream();
//            DataOutputStream out = new DataOutputStream(messageOut);
//
//            if (in.available() < 3) {
//                in.close();
//                messageOut.setStatus(ChannelConstants.RESULT_BADPARAMETER);
//                out.close();
//                con.close();
//                return;
//            }
//
//            String driver = in.readUTF();
//            in.close();
//
//            out.writeBoolean(loadDriver(driver));
//            out.close();
//            con.close();
//        } catch (IOException ex) {
//            System.err.println("IOException " + ex);
//            System.exit(1);
//        }
//    }
//
//    /**
//     * Loads and initializes a driver given the name of a class that implements {@link Driver}.
//     *
//     * @param className  name of driver class
//     * @return true if loading succeeded
//     */
//    static boolean loadDriver(String className) {
//        try {
//            Klass klass = Klass.forName(className);
//            ( (Driver) klass.newInstance()).initialize();
//            return true;
//        } catch (Exception e) {
//            System.err.println("Error loading driver from " + className + ": " + e);
//            return false;
//        }
//    }
//
//
//}
