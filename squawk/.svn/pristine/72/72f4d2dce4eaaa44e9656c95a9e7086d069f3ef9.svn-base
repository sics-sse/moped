/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.romizer;

import java.io.File;

import com.sun.squawk.Romizer;

public class TestEmbeddedRomizer {

	public static void main(String... args) {
		if (!new File("build.properties").exists()) {
			throw new RuntimeException("Need to be running with user.dir being where the squawk.exclude and build.properties files are");
		}
		// use -o:squawk so that it finds the squawk.library.properties and other default files :(
		final String suiteName = "squawk";
		File suiteFile = new File(suiteName + ".suite");
		suiteFile.delete();
		EmbeddedSquawkClassLoader.runMain(Romizer.class.getName(), "-o:" + suiteName, "-exclude:squawk.exclude", "-cp:cldc/j2meclasses", "cldc/j2meclasses");
		long firstSuiteSize = suiteFile.length();
		suiteFile.delete();
		EmbeddedSquawkClassLoader.runMain(Romizer.class.getName(), "-o:" + suiteName, "-exclude:squawk.exclude", "-cp:cldc/j2meclasses", "cldc/j2meclasses");
		long secondSuiteSize = suiteFile.length();
		if (firstSuiteSize != secondSuiteSize) {
			throw new RuntimeException("Suites created did not match size");
		}
	}

}
