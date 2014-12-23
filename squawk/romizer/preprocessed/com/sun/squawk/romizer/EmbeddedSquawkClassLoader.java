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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Load all classes requested as being defined by me, except for those contained in
 *   java.**
 *   sun.**
 * This allows commands such as Romizer to be able to run multiple times in the same VM, without having to worry
 * about what Class initialization not occuring on all classes used.
 * 
 *
 */
public class EmbeddedSquawkClassLoader extends ClassLoader {

	public static void runMain(String className, String... args) {
		EmbeddedSquawkClassLoader loader = new EmbeddedSquawkClassLoader();
		try {
			Class<?> mainClass = loader.loadClass(className);
			Method mainMethod = mainClass.getMethod("main", new Class[] { String[].class });
			mainMethod.invoke(null, new Object[] { args });
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Can't find class " + className + " in current class path", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	public EmbeddedSquawkClassLoader() {
	}

	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// First, check if the class has already been loaded
		Class<?> c = findLoadedClass(name);
		if (c == null) {
			if (name.startsWith("java.")) {
				return super.loadClass(name, resolve);
			}
			if (name.startsWith("sun.")) {
				return super.loadClass(name, resolve);
			}
			URL classResource = getResource(name.replace('.', '/') + ".class");
			if (classResource == null) {
				return super.loadClass(name, resolve);
			}
			try {
				InputStream classInput = classResource.openStream();
				byte[] buffer = new byte[classInput.available()];
				int offset = 0;
				while (offset < buffer.length) {
					int read = classInput.read(buffer, offset, buffer.length - offset);
					offset += read;
				}
				if (classInput.available() != 0) {
//					ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(classInput.available());
//					while (classInput.available() != 0) {
//						bytesOut.write(classInput.read());
//					buffer = bytesOut.toByteArray();
					throw new RuntimeException("Have not implemented this case yet");
				}
				c = defineClass(name, buffer, 0, buffer.length);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (resolve) {
			resolveClass(c);
		}
		return c;
	}

}
