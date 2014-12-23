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

package com.sun.squawk.builder.commands;

import java.io.File;

import sun.tools.jar.Main;

import com.sun.squawk.builder.Build;
import com.sun.squawk.builder.BuildException;
import com.sun.squawk.builder.Command;

public class MakePlatformStubs extends Command {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public MakePlatformStubs(Build env) {
        super(env, "makeplatformstubs");
	}

	@Override
	public void run(String[] args) throws BuildException {
		final File outDir = new File("platform-stubs/");
        Build.mkdir(outDir);
        Build.clear(outDir, false);
        final File tempDir = new File("API/squawk");
        RomCommand romCommand = new RomCommand(env);
        
        // build squawk-imp-1.0.jar
        Build.clear(tempDir, false);
        romCommand.runRomizer("cldc", "imp");
    	env.mainProgrammatic("makeapi", "-nodoc", "squawk.suite.api", "cldc/preprocessed:imp/preprocessed", tempDir.getPath());
    	File apiJarFile = new File(tempDir, "squawk_rt.jar");
    	File stubJarFile = new File(outDir, "squawk-imp-1.0.jar");
    	Build.cp(apiJarFile, stubJarFile, false);
    	// jar
    	new Main(System.out, System.err, "jar").run(new String[] {"umf", "imp/resources/META-INF/MANIFEST.MF", stubJarFile.getPath()});

    	stubJarFile = new File(outDir, "noclasses-imp-1.0.jar");
    	// jar
    	new Main(System.out, System.err, "jar").run(new String[] {"cmf", "imp/resources/META-INF/MANIFEST.MF", stubJarFile.getPath()});
	}

}
