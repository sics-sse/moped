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

package com.sun.squawk.builder.ccompiler;

import com.sun.squawk.builder.platform.*;
import com.sun.squawk.builder.*;
import java.io.File;
import java.io.IOException;

/**
 * The interface for the "cl" MS Visual C++ compiler.
 */
public class MscCompiler extends CCompiler {
	public static final String VISUAL_STUDIO_80_TOOLS_ENVIRONMENT_VARIABLE = "VS80COMNTOOLS";
	public static final String VISUAL_STUDIO_90_TOOLS_ENVIRONMENT_VARIABLE = "VS90COMNTOOLS";
    public static final String MS_PLATFORM_SDK_VARIABLE = "Mstools";

    protected String clCommandString;

    public MscCompiler(Build env, Platform platform) {
        super("msc", env, platform);
    }

    /**
     * {@inheritDoc}
     */
    public String options(boolean disableOpts) {
        StringBuffer buf = new StringBuffer();
        if (!disableOpts) {
            if (options.o1)             { buf.append("/O1 ");              }
            if (options.o2)             { buf.append("/O2 /Gs "); }
            if (options.o3)             { buf.append("/DMAXINLINE ");      }
        }
        if (options.tracing)            { buf.append("/DTRACE ");          }
        if (options.profiling)          { buf.append("/DPROFILING /MT ");  }
        if (options.macroize)           { buf.append("/DMACROIZE ");       }
        if (options.assume)             { buf.append("/DASSUME ");         }
        if (options.typemap)            { buf.append("/DTYPEMAP ");        }

        if (options.kernel) {
            throw new BuildException("-kernel option not supported by MscCompiler");
        }
        
        if (options.nativeVerification){ buf.append("/DNATIVE_VERIFICATION=true ");         }
        	

        // Only enable debug switch if not optimizing
        if (!options.o1 &&
            !options.o2 &&
            !options.o3)                { buf.append("/ZI ");              }

        if (options.is64) {
            throw new BuildException("-64 option not supported by MscCompiler");
        }

        buf.append("/DPLATFORM_BIG_ENDIAN=" + platform.isBigEndian()).append(' ');
        buf.append("/DPLATFORM_UNALIGNED_LOADS=" + platform.allowUnalignedLoads()).append(' ');

        return buf.append(options.cflags).append(' ').toString();
    }

    private String getDirFromEnv(String var) {
        String value = System.getProperty(var);
        if (value == null) {
            value = System.getenv(var);
            if (value != null) {
                env.log(env.verbose, "Found env variable: " + var + "=" + value);
                if (new File(value).exists()) {
                    return value;
                } else {
                    env.log(env.verbose, "directory does not exist: " + value);
                }
            }
        } else {
            env.log(env.verbose, "Found property variable: " + var + "=" + value);

            if (new File(value).exists()) {
                return value;
            } else {
                env.log(env.verbose, "directory does not exist: " + value);
            }
        }
        env.log(env.verbose, "No value for variable: " + var);
        return null;
    }

    public String getClCommandString() {
        if (clCommandString == null) {
            final String[] envvars = {
                VISUAL_STUDIO_90_TOOLS_ENVIRONMENT_VARIABLE,
                VISUAL_STUDIO_80_TOOLS_ENVIRONMENT_VARIABLE,
                MS_PLATFORM_SDK_VARIABLE};

            clCommandString = "cl";
            String toolsDirectory = null;
            for (int i = 0; i < envvars.length; i++) {
                toolsDirectory = getDirFromEnv(envvars[i]);
                if (toolsDirectory != null) {
                        break;
                    }
            }
           
            if (toolsDirectory == null) {
            	toolsDirectory = "";
            }
            try {
                String command = "cmd /C \"\"" + toolsDirectory + "vsvars32.bat\" && " + clCommandString + "\"";
            	env.log(env.verbose, "Trying to find compiler command with: " + command);
                // Try the command to see if it works, if it does work then we want to use it
                Runtime.getRuntime().exec(command);
                clCommandString = command;
            } catch (IOException e) {
            	try {
            		Runtime.getRuntime().exec(clCommandString);
            	} catch (IOException fatal) {
            		throw new BuildException("Unable to find compiler");
            	}
            }
        }
        return clCommandString;
    }
    
    /**
     * {@inheritDoc}
     */
    public File compile(File[] includeDirs, File source, File dir, boolean disableOpts) {
        File object = new File(dir, source.getName().replaceAll("\\.c", "\\.obj"));
        env.exec(getClCommandString() + " /c /nologo /wd4996 " +
                 options(disableOpts) + " " +
                 include(includeDirs, "/I") +
                 "/Fo" + object + " " + source);
        return object;
    }

    /**
     * {@inheritDoc}
     */
    public File link(File[] objects, String out, boolean dll) {
        String output;
        String exec;

        if (dll) {
            output = System.mapLibraryName(out);
            exec = " /Fe" + output + " /LD " + Build.join(objects) + " /link wsock32.lib /IGNORE:4089";
        } else {
            output = out + platform.getExecutableExtension();
            exec = " /Fe" + output + " " + Build.join(objects) + " /link /OPT:REF wsock32.lib /IGNORE:4089";
        }
        env.exec(getClCommandString() + " " + exec);
        return new File(output);
    }

    /**
     * {@inheritDoc}
     */
    public String getArchitecture() {
        return "X86";
    }
}
