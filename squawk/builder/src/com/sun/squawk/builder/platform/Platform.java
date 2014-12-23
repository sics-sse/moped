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

package com.sun.squawk.builder.platform;

import java.io.File;
import java.io.PrintStream;
import com.sun.squawk.builder.*;
import com.sun.squawk.builder.ccompiler.*;

/**
 * This class abstracts the properties of the underlying operating system that are
 * required for C compilation.
 */
public abstract class Platform {

    /**
     * Factory method to create an Platform instance based on the value of the "os.name" and
     * "os.arch" {@link System#getProperties system properties}.
     *
     * @param env the build env
     * @return the Platform instance or null if "os.name" and "os.arch" denote an unknown Platform
     */
    public static Platform createPlatform(Build env) {
        String osName = getHostOsName().toLowerCase();
        String osArch = getHostArchitecture().toLowerCase();

		if (isX86Architecture(osArch)) {
			if (osName.startsWith("windows")) {
				return new Windows_X86(env);
			} else if (osName.startsWith("sunos")) {
				return new SunOS_X86(env);
			} else if (osName.startsWith("linux")) {
				return new Linux_X86(env);
			} else if (osName.startsWith("mac os x")) {
				return new MacOSX_X86(env); 
			} 
		}
		if (osArch.equals("ppc")) {
			if (osName.startsWith("mac os x")) {
				return new MacOSX_PPC(env);
			} else if (osName.startsWith("linux")) {
				return new Linux_PPC(env);
            } else if (osName.startsWith("vxworks")) {
                return new VxWorks_PPC(env);
			}
		}
		if (osArch.equals("sparc")) {
			if (osName.startsWith("sunos")) {
				return new SunOS_Sparc(env);
			}
		}
		throw new RuntimeException("Unsupported/Unknown host os name \"" + osName + "\" and architecture \"" + osArch + "\" combination.");
    }

    /**
     * Gets the name of the operating system.
     *
     * @return the name of the operating system
     */
    public static String getHostOsName() {
    	return System.getProperty("os.name");
    }

    /**
     * Gets the name of the operating system architecture.
     *
     * @return the name of the operating system architecture
     */
    public static String getHostArchitecture() {
        return System.getProperty("os.arch");
    }
    
    /**
     * Returns true on any varient of x86 architectures and OSs
     *
     * @return boolean
     */
    public static boolean isX86Architecture() {
    	String arch = getHostArchitecture();
        return isX86Architecture(arch);
    }

    /**
     * Returns true on any varient of x86 architectures and OSs
     *
     * @return boolean
     */
    public static boolean isX86Architecture(String arch) {
    	arch = arch.toLowerCase();
        return arch.equals("x86") || arch.equals("i386") || arch.equals("amd64") || arch.equals("ia64") || arch.equals("x86_64");
    }

    /**
     * The builder context for this Platform instance.
     */
    final Build env;

    public Platform(Build env) {
        this.env = env;
    }

    /**
     * Gets the directories in which the JVM dynamic libraries are located that are
     * required for starting an embedded JVM via the Invocation API.
     *
     * @return  one or more library paths separated by ':'
     * @throws BuildException if the libraries could not be found
     */
    public abstract String getJVMLibraryPath();

    /**
     * Gets the extension used for executable files in this Platform.
     *
     * @return the extension used for executable files in this Platform
     */
    public abstract String getExecutableExtension();

    /**
     * Gets the directory where the platform specific tools (such as the preverifier) live.
     *
     * @return  the tools directory
     */
    public abstract File getToolsDir();

    /**
     * Gets the preverifier executable.
     *
     * @return the file representing the preverifier executable
     */
    public final File preverifier() {
        return new File(getToolsDir(), "preverify" + getExecutableExtension());
    }

    /**
     * Gets the name of the operating system.
     *
     * @return the name of the operating system
     */
    public final String getName() {
    	return getHostOsName();
    }

    /**
     * Gets the name of the operating system architecture.
     *
     * @return the name of the operating system architecture
     */
    public final String getArchitecture() {
        return getHostArchitecture();
    }
    
    /**
     * Gets a string that describes the concrete Platform.
     *
     * @return a string that describes the concrete Platform
     */
    public final String toString() {
        return getName() + "-" + getArchitecture();
    }

    /**
     * Creates an instance of CCompiler that reflects the 'default' C compiler for this Platform.
     *
     * @return the default C compiler on this platform
     */
    public abstract CCompiler createDefaultCCompiler();

    /**
     * Displays a message describing what environment variables need to be set so that the
     * embedded JVM can be started properly by the squawk executable.
     *
     * @param out PrintStream  the stream on which to print the message
     */
    public abstract void showJNIEnvironmentMessage(PrintStream out);

    /**
     * Returns whether the platform is big endian. Used to set C compilation and romizer
     * flags.
     *
     * @return  true is the platform is big endian
     */
    public boolean isBigEndian() {
        return false;
    }
    
    public boolean isMacOsX() {
        return false;
    }

    /**
     * Returns whether the platform allows unaligned loads.  Used to set the C compilation
     * flags.
     *
     * @return  true is the platform supports unaligned loads.
     */
    public boolean allowUnalignedLoads() {
        return true;
    }


}
