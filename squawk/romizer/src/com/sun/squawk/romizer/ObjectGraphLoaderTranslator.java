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

import java.io.*;

import com.sun.squawk.*;

public class ObjectGraphLoaderTranslator implements TranslatorInterface {
	
	public void close(int type) {
	}

	public void convert(Klass klass) {
	}

	public String getLastClassName() {
		return null;
	}

	public byte[] getResourceData(String name) {
		return null;
	}

	public boolean isValidClassName(String name) {
		return false;
	}

	public void load(Klass klass) {
		return;
	}

	public void open(Suite suite, String classPath) {
	}

	public void printOptionProperties(PrintStream out, boolean asParameters) {
	}

	public void printTraceFlags(PrintStream out) {

	}

	public boolean processOption(String arg) {
		return false;
	}

    public boolean isOption(String arg) {
        return false;
    }

}
