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

import java.io.*;
import java.util.*;

/**
 * This is a utility to provide some assistance in configuring the
 * environment so that the JVM embedded in the native Squawk VM
 * will start properly.
 */
class jvmenv {

    /**
     * Find a single file matching a specified suffix and an optional
     * extra filter.
     * @param dir The directory
     * @param suffix
     * @param match
     * @return
     * @throws Exception
     */
    static String findOne(String dir, String suffix, String match) throws Exception {
        Vector results = new Vector();
        com.sun.squawk.util.Find.find(new File(dir), suffix, results, false);
        for (Enumeration e = results.elements(); e.hasMoreElements();){
            String f = (String)e.nextElement();
            if (f.indexOf(match) != -1) {
                return f;
            }
        }
        return null;
    }

    static String findOne(String dir, String suffix, String[] matches) throws Exception {
        String result = null;
        for (int i = 0; i != matches.length && result == null; i++) {
            result = findOne(dir, suffix, matches[i]);
        }
        return result;
    }

/*
    public static void main(String[] args) throws Exception {
        System.out.println(Integer.TYPE + ".getSuperclass() == "+Integer.TYPE.getSuperclass());
        Runnable r = new Runnable() { public void run() {} };
        System.out.println("r.getClass().getSuperclass() == "+r.getClass().getSuperclass());
        System.out.println("Runnable.class.getSuperclass() == "+Runnable.class.getSuperclass());
    }
*/

    public static void main(String[] args) throws Exception {
        PrintStream out = System.out;
        String javaLib;
        String jhome = System.getProperty("java.home");
        if (System.getProperty("os.name").startsWith("Windows")) {
            String jvm = findOne(jhome, "jvm.dll", new String[] { "hotspot", "client", "" });
            if (jvm != null) {
                out.println();
                out.println("To configure the environment for Squawk, try the following command:");
                out.println();
                out.println("    set JVMDLL="+jvm);
                out.println();
            } else {
                out.println();
                out.println("The JVMDLL environment variable must be set to the full path of 'jvm.dll'.");
                out.println();
            }
        } else if (System.getProperty("os.name").equalsIgnoreCase("mac os x")) {
            out.println();
            out.println("There is no need to configure the environment for Squawk on Mac OS X as the location of the");
            out.println("JavaVM framework is built into the executable.");
            out.println();
        } else {
            String jvm      = findOne(jhome, "libjvm.so", new String[] { "hotspot", "client", "" });
            String verifier = findOne(jhome, "libverify.so", "");
            if (jvm != null && verifier != null) {
                jvm      = (new File(jvm)).getParent();
                verifier = (new File(verifier)).getParent();
                out.println();
                out.println("To configure the environment for Squawk, try the following command under bash:");
                out.println();
                out.println("    export LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:"+jvm+":"+verifier+"\"");
                out.println();
                out.println("or in csh/tcsh");
                out.println();
                out.println("    setenv LD_LIBRARY_PATH=\"$LD_LIBRARY_PATH:"+jvm+":"+verifier+"\"");
                out.println();
            } else {
                out.println();
                out.println("The LD_LIBRARY_PATH environment variable must be set to include the directories");
                out.println("containing 'libjvm.so' and 'libverify.so'.");
                out.println();
            }
        }
    }

}
