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

package com.sun.squawk.builder.launcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Launcher {

    public static void main(String[] args) {
        ClassLoader loader;
        List<URL> urls = new ArrayList<URL>();
        boolean verbose = false;
        for (String arg: args) {
            if (arg.equalsIgnoreCase("-v") || arg.equalsIgnoreCase("-verbose")) {
                verbose = true;
            }
        }
        try {
            URL toolsJar = getToolsJar(verbose);
            if (toolsJar != null) {
                urls.add(toolsJar);
            }
            addBuildCommandsJars(urls);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Problems building class path to launch builder", e);
        }
        loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
        String treeClassName = "com.sun.tools.javac.tree.JCTree";
        try {
            loader.loadClass(treeClassName);
        } catch (ClassNotFoundException e1) {
//            throw new RuntimeException("Failed to find an appropriate compiler interface class: " + treeClassName);
        }
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class<?> buildClass = loader.loadClass("com.sun.squawk.builder.Build");
            Method mainMethod = buildClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Problems finding builder", e);
        } catch (SecurityException e) {
            throw new RuntimeException("Problems finding builder", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Problems finding builder", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Problems finding builder", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Problems finding builder", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addBuildCommandsJars(List<URL> urls) throws MalformedURLException {
        try {
            Class.forName("com.sun.squawk.builder.Build");
            // If Build class is already on my classpath, then go ahead and use it then.
            return;
        } catch (ClassNotFoundException e1) {
        }
		URL launcherJarUrl = Launcher.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            // URL's don't handle encoded spaces well, switch to URI:
            File launcherJarFile = new File(launcherJarUrl.toURI().getPath());
            File buildCommandsJarFile = new File(launcherJarFile.getParent(), "build-commands.jar");
            if (!buildCommandsJarFile.exists()) {
                // the above doesn't work if path has space! ???!.
                buildCommandsJarFile = new File("build-commands.jar");
            }
            if (buildCommandsJarFile.exists()) {
                try {
                    JarFile jar = new JarFile(buildCommandsJarFile);
                    Manifest manifest = jar.getManifest();
                    if (manifest != null) {
                        Attributes attributes = manifest.getMainAttributes();
                        if (attributes != null) {
                            String classPathString = attributes.getValue(Attributes.Name.CLASS_PATH);
                            if (classPathString != null) {
                                StringTokenizer tokenizer = new StringTokenizer(classPathString, " ");
                                while (tokenizer.hasMoreTokens()) {
                                    String token = tokenizer.nextToken();
                                    File file = new File(token);
                                    urls.add(file.toURI().toURL());
                                }
                            }
                        }
                    }
                    urls.add(buildCommandsJarFile.toURI().toURL());
                    return;
                } catch (IOException e) {
                }
            }
		   throw new RuntimeException("Unable to locate build-commands.jar.  Expected to find it at " + buildCommandsJarFile.getPath());
        } catch (URISyntaxException uRISyntaxException) {
            throw new RuntimeException("Unable to locate build-commands.jar: " + uRISyntaxException);
        }
    }
    
    public static URL getToolsJar(boolean verbose) {
        // firstly check if the tools jar is already in the classpath
        boolean toolsJarAvailable = false;
        if (verbose) {
            System.out.print("java.version=");
            System.out.print(System.getProperty("java.version"));
            System.out.println();
            System.out.print("java.home=");
            System.out.print(System.getProperty("java.home"));
            System.out.println();
        }
        // Cloned from com.sun.squawk.builder.JavaCompiler.initializeTools()
        String javacClassName = System.getProperty("builder.tools.javac.class", "com.sun.tools.javac.Main");
        try {
            if (verbose) {
                System.out.print("Looking for ");
                System.out.print(javacClassName);
                System.out.print(" in classpath");
                System.out.println();
            }
            // just check whether this throws an exception
            Class.forName(javacClassName);
            if (verbose) {
                System.out.print("  Found it");
                System.out.println();
            }
            toolsJarAvailable = true;
        } catch (Exception e1) {
            if (verbose) {
                System.out.print("  Failed");
                System.out.println();
            }
        }
        if (toolsJarAvailable) {
            if (verbose) {
                System.out.print("Found compiler, no need to extend classpath, ");
                System.out.print(javacClassName);
                System.out.print(" already in classpath");
                System.out.println();
            }
            return null;
        }
        if (verbose) {
            System.out.print("Failed to find compiler in classpath, need to extend classpath");
            System.out.println();
        }
        String javaHome = System.getProperty("java.home");
        Throwable cause = null;
        try {
            File toolsJar = new File(javaHome + "/lib/tools.jar").getCanonicalFile();
            if (verbose) {
                System.out.print("Looking for tools.jar in ");
                System.out.print(toolsJar);
                System.out.println();
            }
            if (toolsJar.exists()) {
                if (verbose) {
                    System.out.print("  Found it, adding to classpath");
                    System.out.println();
                }
                return toolsJar.toURI().toURL();
            }
            if (verbose) {
                System.out.print("  Failed");
                System.out.println();
            }
            String lookFor = File.separator + "jre";
            if (javaHome.toLowerCase(Locale.US).endsWith(lookFor)) {
                javaHome = javaHome.substring(0, javaHome.length() - lookFor.length());
                toolsJar = new File(javaHome + "/lib/tools.jar").getCanonicalFile();
                if (verbose) {
                    System.out.print("Now looking for tools.jar in ");
                    System.out.print(toolsJar);
                    System.out.println();
                }
                if (toolsJar.exists()) {
                    if (verbose) {
                        System.out.print("  Found it, adding to classpath");
                        System.out.println();
                    }
                    return toolsJar.toURI().toURL();
                }
            }
        } catch (IOException e) {
            cause = e;
        }
        throw new RuntimeException("Unable to locate tools.jar.  Try -v or -verbose and relaunch to see where attempts to locate were made", cause);
    }
    
}
