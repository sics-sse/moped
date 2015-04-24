/*
 * Copyright 2008-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.uei.utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** @author jfim */
public class J2MEApplicationDescriptor {
    private static final Pattern MIDLET_ENTRY_POINT_PATTERN = Pattern.compile("^\\s*MIDlet-[0-9]+:\\s*(.*),\\s*(.*),\\s*(.*)$");
    private static final Pattern MIDLET_JAR_URL_PATTERN = Pattern.compile("^\\s*MIDlet-Jar-URL:\\s*(.*)$");
    private static final Pattern MIDLET_J2ME_CONFIGURATION_PATTERN = Pattern.compile("^\\s*MicroEdition-Configuration:\\s*(.*)$");
    private static final Pattern MIDLET_J2ME_PROFILE_PATTERN = Pattern.compile("^\\s*MicroEdition-Profile:\\s*(.*)$");
    private static final Pattern MIDLET_NAME_PATTERN = Pattern.compile("^\\s*MIDlet-Name:\\s*(.*)$");
    private ArrayList<MidletEntryPoint> entryPoints = new ArrayList<MidletEntryPoint>();
    private String configuration;
    private String jarUrl;
    private String name;
    private String profile;

    /** Load an application descriptor from a JAD file. */
    public J2MEApplicationDescriptor(final File file) throws IOException {
        Reader fileReader = new FileReader(file);
        fillFieldsFromReader(fileReader);
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getJarUrl() {
        return jarUrl;
    }

    public Collection<MidletEntryPoint> getMidletEntryPoints() {
        return new ArrayList<MidletEntryPoint>(entryPoints);
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    private void fillFieldsFromReader(final Reader r) throws IOException {
        LineNumberReader reader = new LineNumberReader(r);
        String currentLine = reader.readLine();

        while (currentLine != null) {
            Matcher entryPointMatcher = MIDLET_ENTRY_POINT_PATTERN.matcher(currentLine);
            Matcher jarUrlMatcher = MIDLET_JAR_URL_PATTERN.matcher(currentLine);
            Matcher configurationMatcher = MIDLET_J2ME_CONFIGURATION_PATTERN.matcher(currentLine);
            Matcher profileMatcher = MIDLET_J2ME_PROFILE_PATTERN.matcher(currentLine);
            Matcher nameMatcher = MIDLET_NAME_PATTERN.matcher(currentLine);

            if (entryPointMatcher.matches()) {
                entryPoints.add(new MidletEntryPointImpl(entryPointMatcher.group(1),
                        entryPointMatcher.group(2),
                        entryPointMatcher.group(3)));
            } else if (jarUrlMatcher.matches()) {
                jarUrl = jarUrlMatcher.group(1);
            } else if (configurationMatcher.matches()) {
                configuration = configurationMatcher.group(1);
            } else if (profileMatcher.matches()) {
                profile = profileMatcher.group(1);
            } else if (nameMatcher.matches()) {
                name = nameMatcher.group(1);
            }

            currentLine = reader.readLine();
        }

        reader.close();
    }

    static class MidletEntryPointImpl implements MidletEntryPoint {
        private String className;
        private String icon;
        private String name;

        public MidletEntryPointImpl(final String name, final String icon,
                                    final String className) {
            this.name = name;
            this.icon = icon;
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        public String getIconName() {
            return icon;
        }

        public String getName() {
            return name;
        }
    }
}
