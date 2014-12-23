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

import java.io.*;
import java.util.*;
import com.sun.squawk.builder.*;
import com.sun.squawk.builder.util.*;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.regex.*;

/**
 * This is the command that produces a jar file of all the Squawk files under CVS control.
 *
 */
public class ExportCommand extends Command {

    /**
     * Verbose execution flag.
     */
    private boolean verbose;

    /**
     * The set of excluded files.
     */
    private Set<String> excluded;

    public ExportCommand(Build env) {
        super(env, "export");
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "exports the Squawk CVS controlled distribution to a jar file";
    }

    /**
     * Displays usage message.
     *
     * @param errMsg  an error message printed first if it is not null.
     */
    public void usage(String errMsg) {
        PrintStream out = System.err;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("usage: export [-options] [jarfile]");
        out.println("where options include:");
        out.println();
        out.println("    -exclude:<file>   exclude all files whose prefixes are in 'file'");
        out.println("    -newline:<value>  specifies new line format for text files:");
        out.println("                        mac: \"\\r\" (pre Mac OS X)");
        out.println("                        unix: \"\\n\"");
        out.println("                        dos: \"\\r\\n\"");
        out.println("    -verbose          verbose execution");
        out.println("    -h                display this help message and exit");
        out.println();
    }

    /**
     * Process a file containing a list of file path prefixes and add them to a given set.
     *
     * @param files  the set to be added to
     * @param path   the path of the file to be processed
     * @throws BuildException if <code>path</code> cannot be opened or there is an IO error while reading it
     */
    private void processFileList(Set<String> files, String listFile) {
        try {
            File file = new File(listFile);
            char[] input = new char[ (int) file.length()];
            FileReader fr = new FileReader(file);
            fr.read(input);
            // Split the input as a list of strings separated
            // by one or more whitespace characters
            String[] list = new String(input).split("\\s+");
            for (int i = 0; i != list.length; ++i) {
                files.add(list[i].replace(File.separatorChar, '/'));
            }
        } catch (FileNotFoundException e) {
            throw new BuildException("list file '" + listFile + "' not found");
        } catch (IOException e) {
            throw new BuildException("IO error reading list file '" + listFile + "': " + e);
        }
    }

    /**
     * Parses a CVS/Entries file and returns two lists file names; the first is the names of
     * non-binary files and the second is the list of binary files.
     *
     * @param entries  a CVS/Entries file
     * @return a paris of lists; the non-binary files and binary files in <code>entries</code>
     */
     private String[][] parseCVSEntries(File entries) {
        try {
            Pattern pattern = Pattern.compile("([^/]?)/([^/]*)/[^/]*/[^/]*/([^/]*)/[^/]*");

            List<String> nonBinary = new ArrayList<String>();
            List<String> binary = new ArrayList<String>();

            BufferedReader br = new BufferedReader(new FileReader(entries));
            String entry = br.readLine();
            while (entry != null) {

                Matcher m = pattern.matcher(entry);
                if (m.matches()) {
                    String type = m.group(1);
                    if (type.indexOf('D') == -1) {
                        String options = m.group(3);
                        String path = m.group(2);
                        if (options.indexOf("kb") == -1) {
                            nonBinary.add(path);
                        } else {
                            binary.add(path);
                        }
                    }
                }
                entry = br.readLine();
            }
            br.close();

            String[][] lists = new String[][] {
                   nonBinary.toArray(new String[nonBinary.size()]),
                   binary.toArray(new String[binary.size()])
            };

            return lists;
        } catch (IOException e) {
            throw new BuildException("IO error while reading '" + entries.getAbsolutePath() + "': " + e);
        }
    }

    /**
     * Conditionally prints a message on the console.
     *
     * @param b   the condition
     * @param msg the message to print if <code>b</code> is true
     */
    private void log(boolean b, String msg) {
        if (b) {
            System.out.println(msg);
        }
    }

    /**
     * Determines if a given file is to be excluded from the distribution.
     *
     * @param relativePath  the path of the file to test relative to the Squawk base dir
     * @return  true if the file is to be excluded
     */
    private boolean isExcluded(String relativePath) {
        for (String prefix: excluded) {
            if (relativePath.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a set of files to a jar file stream. If the files being added are not binary files,
     * then they are written with their line endings using the current value of the system
     * property "line.separator".
     *
     * @param zos        the jar file stream
     * @param baseDir    the base directory used to which the entry name will be relative
     * @param dir        the directory containing the files
     * @param fileNames  the names of the files in <code>dir</code> to be added
     * @param binary     specifies if the files being added are binary files
     */
    private void addFiles(ZipOutputStream zos, File baseDir, File dir, String[] fileNames, boolean binary) {
        for (int i = 0; i != fileNames.length; ++i) {
            String fileName = fileNames[i];
            File file = new File(dir, fileName);

            String relativePath = FileSet.getRelativePath(baseDir, file).replace(File.separatorChar, '/');
            if (isExcluded(relativePath)) {
                log(true, "excluding: " + relativePath);
                continue;
            }

            String entryName = "Squawk/" + relativePath;
            ZipEntry e = new ZipEntry(entryName);
            e.setTime(file.lastModified());
            if (file.length() == 0) {
                e.setMethod(ZipEntry.STORED);
                e.setSize(0);
                e.setCrc(0);
            }

            try {

                zos.putNextEntry(e);
                if (binary) {
                    log(verbose, "adding binary: " + entryName);
                    byte[] buf = new byte[ (int) file.length()];
                    int len;
                    InputStream is = new BufferedInputStream(new FileInputStream(file));
                    while ( (len = is.read(buf, 0, buf.length)) != -1) {
                        zos.write(buf, 0, len);
                    }
                    is.close();
                } else {
                    log(verbose, "adding non-binary: " + entryName);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    PrintStream out = new PrintStream(zos);
                    String line = br.readLine();
                    while (line != null) {
                        out.println(line);
                        line = br.readLine();
                    }
                    br.close();
                }
                zos.closeEntry();
            } catch (IOException ioe) {
                throw new BuildException("IO error while adding '" + file.getAbsolutePath() + "' to jar file: " + ioe);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run(String[] args) {

        String oldLineSeparator = System.getProperty("line.separator");
        excluded = new HashSet<String>();
        verbose = false;

        int argc = 0;
        while (args.length != argc) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            } else if (arg.startsWith("-exclude:")) {
                processFileList(excluded, arg.substring("-exclude:".length()));
            } else if (arg.startsWith("-newline:")) {
                String value = arg.substring("-newline:".length());
                if (value.equals("mac")) {
                    System.setProperty("line.separator", "\r");
                } else if (value.equals("dos")) {
                    System.setProperty("line.separator", "\r\n");
                } else if (value.equals("unix")) {
                    System.setProperty("line.separator", "\n");
                } else {
                    throw new CommandException(this, "Invalid new line type: " + value);
                }
            } else if (arg.equals("-verbose")) {
                verbose = true;
            } else {
                throw new CommandException(this, "Invalid option: " + arg);
            }
            argc++;
        }

        File jarFile = (argc == args.length ? createDefaultJarFile() : new File(args[argc]));
        try {
            FileOutputStream fos = new FileOutputStream(jarFile);
            ZipOutputStream zos = new JarOutputStream(fos);
            FileSet fs = new FileSet(new File("."), new FileSet.NameSelector("Entries"));
            for (File entries: fs.list()) {
                if (entries.isFile()) {
                    File dir = entries.getParentFile();
                    if (dir.getName().equals("CVS")) {
                        dir = dir.getParentFile();

                        String[][] files = parseCVSEntries(entries);
                        addFiles(zos, fs.getBaseDir(), dir, files[0], false);
                        addFiles(zos, fs.getBaseDir(), dir, files[1], true);
                    }
                }
            }
            zos.close();
        } catch (IOException e) {
            throw new BuildException("IO error creating jar file", e);
        }

        log(true, "Created distribution in " + jarFile.getAbsolutePath());
        System.setProperty("line.separator", oldLineSeparator);
    }

    /**
     * Creates the jar file based on today's date.
     *
     * @return  a file with the name "Squawk-<year>_<month>_<day-of-month>.jar"
     */
    private File createDefaultJarFile() {
        File jarFile;
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());

        String year = "" + today.get(Calendar.YEAR);
        String month = "" + (today.get(Calendar.MONTH) + 1);
        String day = "" + today.get(Calendar.DAY_OF_MONTH);

        if (month.length() == 1) {
            month = "0" + month;
        }
        if (day.length() == 1) {
            day = "0" + day;
        }

        String date = year + '_' + month + '_' + day;
        jarFile = new File("Squawk-" + date + ".jar");
        return jarFile;
    }
}