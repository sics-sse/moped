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

package com.sun.squawk.builder;

import com.sun.squawk.builder.platform.Platform;
import java.io.File;
import java.io.PrintStream;

/**
 * A <code>JDK</code> instance is used to resolve the system dependent path to the
 * executable of a JDK tool such as 'javac', 'java' etc.
 */
public class JDK {

    private final File home;
    private final String executableExtension;
    private final Platform platform;

    /**
     * Creates an instance through which the tools of a JDK can be accessed.
     *
     * @param platform  the host platform
     */
    public JDK(Platform platform) {
        File home = new File(System.getProperty("java.home"));
        if (home.getPath().endsWith("jre")) {
            home = home.getParentFile();
        }
        this.home = home;
        this.executableExtension = platform.getExecutableExtension();
        this.platform = platform;
    }

    /**
     * Gets the full path to the 'java' executable.
     *
     * @return the full path to the 'java' executable
     */
    public String java() {
        return getToolPath("java");
    }

    /**
     * Gets the full path to the 'javac' executable.
     *
     * @return the full path to the 'javac' executable
     */
    public String javac() {
        return getToolPath("javac");
    }

    /**
     * Gets the full path to the 'javadoc' executable.
     *
     * @return the full path to the 'javadoc' executable
     */
    public String javadoc() {
        return getToolPath("javadoc");
    }

    /**
     * Gets the relative path to a JDK tool executable.
     *
     * @param tool   the name of the JDK tool executable without the platform specific executable extension
     * @return the path to the JDK tool executable denoted by <code>tool</code>
     */
    private String getToolPath(String tool)      {
        File path = new File(new File(home, "bin"), tool + executableExtension);
        if (!path.isFile()) {
            PrintStream out = System.out;
            out.println();
            out.println("Could not find the '"+tool+"' executable.");
            out.println();
            out.println("This is most likely due to having run the builder with a");
            out.println("JRE java executable instead of a JDK java executable.");
            out.println("To fix this, make sure that the 'bin' directory of the");
            out.println("JDK (e.g. \"C:\\j2sdk\\bin\") preceeds the directory");
            out.println("containing the JRE java executable (e.g. \"C:\\WINNT\\SYSTEM32\")");
            out.println("or specify the absolute path to the java executable when");
            out.println("running the builder (e.g. \"C:\\j2sdk\\bin\\java.exe -jar build.jar ...\")");
            out.println();
            throw new BuildException("JDK executable '" + path.getPath() + "' does not exist");
        }
        return path.getPath();
    }

    /**
     * Gets the home directory of the JDK.
     *
     * @return  the home directory of the JDK
     */
    public File getHome() {
        return home;
    }


    /**
     * Gets the directory where the machine dependent "jni_md.h" file resides.
     *
     * @return  the directory where the machine dependent "jni_md.h" file resides
     * @throws BuildException if the file cannot be found
     */
    public final File getJNI_MDIncludePath() {
        File jniMd = Build.find(home, "jni_md.h", false);
        if (jniMd == null) {
            if (platform.isMacOsX()) {
                String osVer = System.getProperty("os.version");
                String SDKVer = "/Developer/SDKs/MacOSX10.6.sdk";
                if (osVer != null && osVer.contains("10.5")) {
                    SDKVer = "/Developer/SDKs/MacOSX10.5.sdk";
                }
                jniMd = Build.find(new File(SDKVer), "jni_md.h", false);
            }
        }

        if (jniMd == null) {
            throw new BuildException("could not find 'jni_md.h' under '" + home.getPath() + "'");
        }
        return jniMd.getParentFile();
    }

}
