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

package com.sun.squawk.builder;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SvnUtil {
	
	public static List<File>getIgnoreEntries(File dir) {
		HashMap<String, List<String>> map = getHashMapFromHashdump(dir);
		List<String> ignoreValues = map.get("svn:ignore");
		List<File> ignoreFiles = new ArrayList<File>();
		if (ignoreValues == null) {
			return ignoreFiles;
		}
		for (String ignore: ignoreValues) {
			ignoreFiles.add(new File(dir, ignore));
		}
		return ignoreFiles;
	}

	public static HashMap<String, List<String>> getHashMapFromHashdump(File dir) {
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		File file = new File(dir, ".svn/dir-props");
        if (!file.canRead()) {
    		file = new File(dir, ".svn/dir-prop-base");
            if (!file.canRead()) {
            	return map;
            }
        }
    	BufferedReader reader = null;
    	try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			StringWriter writer;
			String line;
			String key = null;
			// TODO Use some better code to read the SVN properties file.
			while (true) {
				writer = new StringWriter();
				readLine(reader, writer);
				line = writer.toString();
				if (line == null || line.equals("END")) {
					break;
				}
				if (line.length() == 0) {
				} else if (line.startsWith("K ")) {
					key = reader.readLine();
				} else if (line.startsWith("V ")) {
					List<String> lines = new ArrayList<String>();
					int count = Integer.parseInt(line.substring(2));
					while (count > 0) {
						writer = new StringWriter();
						boolean encounteredCr = readLine(reader, writer);
						line = writer.toString();
						if (line.length() != 0) {
							lines.add(line);
						}
						count -= line.length() + 1;
						if (encounteredCr) {
							count--;
						}
					}
					map.put(key, lines);
				} else {
					throw new RuntimeException("Unknown entry in svn hashdump file: " + line);
				}
			}
		} catch (IOException e) {
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return map;
	}
	
	/**
	 * Simple test of the methods contained in this class.
	 */
	public static void main(String... args) {
		try {
			File file = new File("..").getCanonicalFile();
			System.out.println("Entries in " + file.getAbsolutePath());
			List<File> entries = getIgnoreEntries(file);
			System.out.println("      count "+ entries.size());
			for (File entry: entries) {
				System.out.println("   " + entry.getPath());
			}
		} catch (IOException e) {
		}
	}

	protected static boolean readLine(BufferedReader reader, StringWriter writer) throws IOException {
		boolean encounteredCr = false;
		while (true) {
			int read = reader.read();
			if (read == -1) {
				return encounteredCr;
			}
			if (read == '\r') {
				encounteredCr = true;
			} else if (read == '\n') {
				return encounteredCr;
			} else {
				writer.write(read);
			}
		}
	}
}
