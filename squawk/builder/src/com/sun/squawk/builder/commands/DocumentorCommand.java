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
import java.util.zip.*;


/**
 * This is the command that builds a collection of html documents to better
 * present the documentation supplied with Squawk.
 *
 */
public class DocumentorCommand extends Command {

    /**
     * This is the folder under the main directory of the squawk
     * source tree
     */
    private static final String DOCS_DIR = "docs";

    /**
     * Directory specific file specifying any overrides of the master properties file
     */
    private static final String PROPERTIES_FILE = "documentor.properties";

    /**
     * A file specifying the defaults of each directory
     */
    private static final String MASTER_PROPERTIES_FILE = "master.properties";

    /**
     * When searching a directory, if a file has a .template extension
     * it will be parsed for tags and a corresponding file without the
     * .template extension will be produced.  Ie.  index.html.template will
     * become index.html
     */
    private static final String TEMPLATE_SUFFIX = ".template";


    /**
     * The directory name, under the DOCS_DIR where the javadoc
     * is placed
     */
    private static final String JAVADOC_DIR = "Javadoc";

    private Build env;
    private Hashtable<String, String> replacementTags;
    private Properties globalProperties;

    private int uniqID = 0;

    public DocumentorCommand(Build env) {
        super(env, "documentor");
        this.env = env;
        replacementTags = new Hashtable<String, String>();
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "builds Squawk document library including API javadoc";
    }

    /**
     * Prints a help message to standard out and does not run the command
     */
    public void usage(String errMsg) {
        if (errMsg != null) {
            System.err.println(errMsg);
        }
        System.err.println("Usage:  [-javadoc[:api] | clean] documentor [doc-option]");
        System.err.println("where doc-option includes");
        System.err.println("     -public   generate zip of public html pages");
        System.err.println("     -h        print this help message");
        System.err.println("");
        System.err.println("typical usage:  -javadoc:api documentor");
        System.err.println("to generate full documentation listing including public API");
    }

    /**
     * Generate a zip file will the given files
     * @param fileList     the <code>Vector</code> containing list of <code>File</code>'s to zip
     * @param outputFile   where to place the output zip file
     */
    public void buildZip(Vector<File> fileList, File outputFile) throws Exception {

            // Make zip file
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputFile));

            byte data[] = new byte[2048];

            // Add each file
            for (File f: fileList) {
                FileInputStream fi = new FileInputStream(f);
                ZipEntry entry = new ZipEntry(f.getPath());

                try {
                    zos.putNextEntry(entry);
                } catch (java.util.zip.ZipException ex1) {
                    // skip over this entry
                    continue;
                }

                int count;
                while ( (count = fi.read(data, 0, 2048)) != -1) {
                    zos.write(data, 0, count);
                }

                fi.close();
            }

            zos.close();
    }

    /**
     * {@inheritDoc}
     */
    public void run(String[] args) {
        try {

            boolean buildPublic = false;

            // only one allowable args
            if(args != null && args.length > 0 && args[0] != null) {

                // build a zip file of the public html
                if(args[0].startsWith("-public")) {
                    buildPublic = true;
                } else {
                    throw new CommandException(this, "Unknown command: " + args[0]);
                }
            }

            // load properties
            loadGlobalProperties();

            // build java doc if required
            buildJavaDoc();

            // search for and build from templates
            buildTemplates();

            // zip up public html documents
            if (buildPublic) {

                // zip up appropriate files etc
                if (globalProperties.containsKey("publicWebZipfile") && globalProperties.containsKey("publicWebDirectories")) {
                    File zipfile = new File(DOCS_DIR, globalProperties.getProperty("publicWebZipfile"));

                    StringTokenizer st = new StringTokenizer(globalProperties.getProperty("publicWebDirectories", ""), ";");
                    Vector<File> fileList = new Vector<File>();

                    while (st.hasMoreTokens()) {
                        File f = new File(DOCS_DIR, st.nextToken());

                        if (f.isDirectory()) {
                            fileList.addAll(this.getFileList(f));
                        }

                        if (f.isFile()) {
                            fileList.add(f);
                        }
                    }

                    // only build zip if more than 1 file
                    if (fileList.size() > 0) {
                        // build zip file
                        buildZip(fileList, zipfile);

                    } else {
                        System.err.println("No files included for -public. Zip not created");
                    }
                } else {
                    throw new IOException("Could not locate \"publicWebZipfile\" or \"publicWebDirectories\" properties in " +
                                          MASTER_PROPERTIES_FILE);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error occurred building document index: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadGlobalProperties() throws IOException {
        // Grab the global properties. If it can't load it, an exception will
        // be thrown
        File propertiesFile = new File(DOCS_DIR, MASTER_PROPERTIES_FILE);
        globalProperties = new Properties();
        FileInputStream inputStream = new FileInputStream(propertiesFile);
        try {
            globalProperties.load(inputStream);
        } finally {
            inputStream.close();
        }
    }

    private Vector<File> getFileList(File start) {

        Vector<File> v = new Vector<File>();

        File[] files = getValidFiles(start);

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                v.add(files[i]);
            }

            if (files[i].isDirectory()) {
                v.addAll(this.getFileList(files[i]));
            }
        }

        return v;
    }

    private File[] getValidFiles(File start) {
        // get all matching files/directories, recurse dirs
        // Folder properties
        final Properties p = getDirectoryProperties(start);
        final Hashtable<String, String> filesToExclude = getFilesToIgnore(p);

        // Filter files
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // Don't include files starting with "."
                if (name.startsWith(".")) return false;

                // Don't include files that are in this folders exclude list
                if (filesToExclude.containsKey(name)) return false;

                // Only include those files with valid extensions
                String includedFileExtensions = p.getProperty("defaultFileTypes", "");
                StringTokenizer st = new StringTokenizer(includedFileExtensions, ";");
                while(st.hasMoreTokens()) {
                    if(name.toLowerCase().endsWith(st.nextToken().toLowerCase())) {
                        return true;
                    }
                }

                // Allow all directories that were not in the exclude list
                if(new File(dir, name).isDirectory()) {
                    return true;
                }

                // The file didn't match our parameters and should be exluded
                return false;
            }
        };

        File[] files = start.listFiles(filter);
        return files;
    }

    private void buildTemplates() throws IOException {

        // doing this static for the moment
        File docBase = new File(DOCS_DIR);

        // set file filter
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(TEMPLATE_SUFFIX));
            }
        };

        File[] templateList = docBase.listFiles(filter);

        for(int i = 0; i < templateList.length; i++ ) {
            uniqID = 0;
            buildIndexPage(templateList[i]);
        }
    }

    /**
     * Build the API javadoc for each of the targets
     */
    private void buildJavaDoc() {
        // build javadoc in API only mode

        if(env.runJavadoc) {
            env.checkDependencies = true;

            // Build each target and generate javadoc
            env.runCommand("<all>", new String[] {});
        }
    }


    /*---------------------------------------------------------------------------*\
     *                 Methods to build the document table                       *
    \*---------------------------------------------------------------------------*/

    /**
     * Search the document directories and build template webpages
     * @param docBase    The directory under /docs to generate document table data
     */
    private void buildDocumentTableTag(String docBase) {
        File baseDoc = new File(DOCS_DIR, docBase);

        String tagName = "docList:" + docBase;

        // If we've already built this document hierachy, don't do it again.
        if(replacementTags.containsKey(tagName)) {
            return;
        }

        String docTable = buildFolderInfo(baseDoc, new File(DOCS_DIR));

        // Add tag data
        docTable = "<div id=\"docTable\">" + docTable;

        docTable += "<p>";
        docTable += ("<span id=\"" + uniqID + "_plus\" class=\"showHideAll\">");
        docTable += ("<a href=\"javascript: showAll('" + uniqID + "');\">Show All</a>");
        docTable += "&nbsp;|&nbsp;";
        docTable += ("<a href=\"javascript: hideAll('" + uniqID + "');\">Hide All</a>");
        docTable += ("</span>");

        docTable +=  "</div>";

        replacementTags.put(tagName, docTable);
    }


    /**
     * Determine folder info, and build HTML description of this folder and
     * any interesting files within.
     *
     * @param dir       the folder to examine
     * @param recurse   whether to recurse into any subdirectories
     * @return the HTML description of this folder and its children
     */
    private String buildFolderInfo(File dir, File docBase) {

        StringBuffer sb = new StringBuffer();

        // Folder properties
        final Properties p = getDirectoryProperties(dir);
        boolean recurseSubFolders = new Boolean(p.getProperty("recurseIntoSubFolders")).booleanValue();

        // This directory gets removed when "clean" is run. Bit neater to do this here
        // then do recreate documentor.properties file.
        if(dir.getName().equals(JAVADOC_DIR)) {
            recurseSubFolders = false;
        }

        sb.append("<div class=\"child\">");

        File[] dirs = getValidFiles(dir);

        // Print out files at this level first
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].isFile()) {
                sb.append(printFileOrFolder(p, dirs[i], docBase));
            }
        }

        // Print out directories
        for (int i = 0; i < dirs.length; i++) {
            if (dirs[i].isDirectory()) {
               if( recurseSubFolders) {
                   sb.append(printFileOrFolder(p, dirs[i], docBase));
                   sb.append(buildFolderInfo(dirs[i], docBase));
               } else {
                   sb.append(printFileOrFolder(p, dirs[i], docBase));
               }
            }
        }

        sb.append("</div>");
        return sb.toString();
    }

    /**
     * Generates a <code>Properties</code> object with the key/values of the master properties file
     * overridden by any directory specific parameters.
     *
     * @param dir         the directory to search for the overriding properties file
     * @return  the new <code>Properties</code> object
     */
    private Properties getDirectoryProperties(File dir) {

        // Grab properties file if one exists
        File propertiesFile = new File(dir, PROPERTIES_FILE);
        Properties p = new Properties();

        // copy the default values from the global file
        for(Enumeration<?> e = globalProperties.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            p.put(key, globalProperties.get(key));
        }

        // override defaults if file exists
        if(propertiesFile.canRead()) {
            try {
                p.load(new FileInputStream(propertiesFile));
            } catch (IOException ex) {
                //System.err.println("Cannot read info file");
            }
        }
        return p;
    }


    /**
     * Get the HTML description of this file or folder.  This will also include a
     * description if available.
     *
     * @param p        the <code>Properties</code> specifying directory specifics
     * @param file     the file or folder to generate output for
     * @return   the <code>StringBuffer</code> containing the html output for this file or folder
     */
    private StringBuffer printFileOrFolder(Properties p, File file, File docBase) {

        // Where to place output
        StringBuffer sb = new StringBuffer();

        // A unique id for each div/span we create
        uniqID++;

        // Check if there is a description
        String desc = getFileDescription(p, file);
        String alias = getFileAlias(file);

        // Format for file or directory
        sb.append("<div id=\"" + uniqID + "\" class=\"" + (file.isDirectory()? "dir" : "file") + "\">");

        if( desc != null) {
            // Check if there is a description
            sb.append("<span id=\"" + uniqID + "_plus\" class=\"plus\">");
            sb.append("<a href=\"javascript: show(" + uniqID + ");\">+</a>");
            sb.append("</span>");

            sb.append("<span id=\"" + uniqID + "_minus\" class=\"minus\">");
            sb.append("<a href=\"javascript: hide(" + uniqID + ");\">-</a>");
            sb.append("</span>");
        } else {
            sb.append("<span class=\"plus\">&nbsp;</span>");
        }

        sb.append("&nbsp;");

        String indexFile = p.getProperty("indexFile");

        if(file.isDirectory() && new File(file, indexFile).exists()) {
            sb.append("<span class=\"dirWithIndex\">");
            sb.append("<a href=\"");
            sb.append(getURLtoFile(new File(file, indexFile), docBase).toCharArray());
            sb.append("\">");
            sb.append(alias);
            sb.append("</a></span>");

        } else if( file.isDirectory()) {
            sb.append("<span class=\"dirWithoutIndex\">");
            sb.append(alias);
            sb.append("</span>");
        } else {
            sb.append("<span class=\"fileLink\">");
            sb.append("<a href=\"");
            sb.append(getURLtoFile(file, docBase).toCharArray());
            sb.append("\">");
            sb.append(alias);
            sb.append("</a></span>");
        }


        // Add the description
        if( desc != null) {
            sb.append("<div id=\"" + uniqID + "_desc\" class=\"description\">");
            sb.append(desc);
            sb.append("</div>");
        }

        sb.append("</div>");

        return sb;
    }

    /**
     * If an alias appears in the meta file associated with the original,
     * change the display name.f
     *
     * @param f     the alias of the file
     * @return   the alias if it exists, otherwise just the file name.
     */
    private String getFileAlias(File file) {

        File metaFile;
        if(!file.isDirectory()) {
            // Same name as original file, with .meta
            metaFile = new File(file.getParentFile(), file.getName() + ".meta");
        } else {
            // A file called .meta
            metaFile = new File(file, ".meta");
        }

        String description = file.getName();

        if(metaFile.canRead()) {
            String line = "";

            try {
                BufferedReader br = new BufferedReader(new FileReader(metaFile));
                line = br.readLine();
                br.close();
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
                // do nothing
            }

            if(line.startsWith("displayName")) {
                description = line.substring(line.indexOf('=')+1).trim();
            }

        }


        return description;
    }

    /**
     * Gets the relative path to this file or folder from the
     * document base directory.
     *
     * @param f    the file of which to construct the relative directory
     * @return the string reprepresenting the relative path to the supplied file.
     */
    private String getURLtoFile(File f) {
        return getURLtoFile(f, new File(DOCS_DIR));
    }

    /**
     * Gets the relative path to this file or folder from the supplied
     * document base directory.
     *
     * @param f    the <code>File</code> of which to construct the relative directory
     * @param base the <code>File</code> of the base directory
     * @return the string reprepresenting the relative path to the supplied file.
     */
    private String getURLtoFile(File f, File base) {

        // We strip the base dir from f
        String relativeLink = f.getAbsolutePath().substring(base.getAbsolutePath().length() + 1);

        return relativeLink;
    }

    /**
     * Attempt to load the description of the supplied file or directory.  If
     * an image is to be loaded, an img tag will be included after the description
     * in the description file
     *
     * @param p       the properties file associated with the parent directory
     * @param file    the file or folder whos description we are looking for
     * @return a string of the description or null if no description is available
     */
    private String getFileDescription(Properties p, File file) {

        File metaFile;
        if(!file.isDirectory()) {
            // Same name as original file, with .meta
            metaFile = new File(file.getParentFile(), file.getName() + ".meta");
        } else {
            // A file called .meta
            metaFile = new File(file, ".meta");
        }

        String description = null;

        if(metaFile.canRead()) {
            byte[] filedata = null;
            try {
                FileInputStream fis = new FileInputStream(metaFile);
                filedata = new byte[(int)metaFile.length()];
                fis.read(filedata);
                fis.close();
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
                // do nothing
            }

            description = new String(filedata);

            // This file includes an alias definition, strip it
            if(description.startsWith("displayName")) {
                if(description.indexOf('\n') != -1) {
                    description = description.substring(description.indexOf('\n') + 1);
                    if(description.length() == 0) {
                        description = null;
                    }
                } else {
                    description = null;
                }

            }
        }

        // Build an <img src=".."> for images
        // image types must be declared in the global file
        // Only include those files with valid extensions
        String defaultImageTypes = p.getProperty("defaultImageTypes", "");
        StringTokenizer st = new StringTokenizer(defaultImageTypes, ";");
        while(st.hasMoreTokens()) {
            if(file.getName().toLowerCase().endsWith(st.nextToken().toLowerCase())) {
                String imageTag = "<img src=\"" + getURLtoFile(file) + "\">";
                if(description == null) {
                    description = imageTag;
                } else {
                    description += imageTag;
                }
                break;
            }
        }

        // Search for tags within the description
        if(description != null) {
            searchForTags(description);
        }

        return description;
    }

    /**
     * Build a list of files to exclude when printing out table. Entries are
     * read from a <code>Properties</code> object and are expected to be
     * separated by a semi-colon ";"
     *
     * @param p the <code>Properties</code> for this folder
     * @return a hashtable of entries
     */
    private Hashtable<String, String> getFilesToIgnore(Properties p) {

        Hashtable<String, String> exludedFiles = new Hashtable<String, String>();

        String exludedFilesString = p.getProperty("excludeFiles", "");
        StringTokenizer st = new StringTokenizer(exludedFilesString, ";");

        while(st.hasMoreElements()) {
            String filename = st.nextToken();
            exludedFiles.put(filename, filename);
        }

        return exludedFiles;
    }


    /*---------------------------------------------------------------------------*\
     *                 Methods to build template files                           *
    \*---------------------------------------------------------------------------*/


    /**
     * Swap tags in <outputname>.template and output to <outputname>
     */
    private void buildIndexPage(File templateFile) throws IOException {


        // Strip the .template off the filename
        File outputFile = new File(templateFile.getParentFile(),
                                   templateFile.getName().substring(0,
            templateFile.getName().length() - (TEMPLATE_SUFFIX.length())));

        // Remove previously generated index file
        if(outputFile.exists()) {
            outputFile.delete();
        }

        // Open template
        FileInputStream fis = new FileInputStream(templateFile);

        // Read into array
        byte[] fileData = new byte[ (int) templateFile.length()];
        fis.read(fileData);
        fis.close();

        // Make appropriate substitutions
        String s = new String(fileData);

        searchForTags(s);

        s = fixTags(s, false);

        // Write out generated index file
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(s.getBytes());
        fos.close();
   }

    private void searchForTags(String s) {
        // Figure out the tag requests, and build as appropriate
        int searchIndex = 0;
        String sub = s;
        while( (searchIndex = sub.indexOf("<!")) != -1) {

            // Move to start of tag
            sub = sub.substring(searchIndex);

            String fullTag = "";
            String tagName = "";
            String tagParameter = "";

            if(sub.indexOf(">") == -1) {
                // print out some debugging help if html is malformed
                System.err.println("**MALFORMED TAG**");
                System.err.println(sub.substring(0, Math.min(sub.length(), 50)));
                return;
            }

            // Full tag, ie <!tagDetails>
            fullTag = sub.substring(0, sub.indexOf(">") + 1);

            // If we have a parameter, get it out
            int colonIndex = -1;
            if( (colonIndex = fullTag.indexOf(":")) != -1 ) {
                tagName = fullTag.substring("<!".length(), colonIndex);
                tagParameter = fullTag.substring(colonIndex + 1, fullTag.length() - 1);

            } else {
                tagName = fullTag.substring("<!".length(), fullTag.length() - 1);
            }


            if(tagName.startsWith("docList")) {
                // Build document table info for supplied directory
                buildDocumentTableTag(tagParameter);
            }
            else {
                // do nothing
            }

            // Move to end of tag
            sub = sub.substring(fullTag.length());

        }
    }

   /**
    * Make any string replacements to file
    *
    * @param templateData    the template file
    * @param replacementTags true if this call is being used to fix the replacement tags
    * @return  the template file with tags replaced
    */
   private String fixTags(String templateData, boolean fixingReplacementTags) {
       String fixedTemplateData = templateData;

       // Grab the date this was generated
       replacementTags.put("date", new Date().toString());

       // Look at each tag type, and fix as appropriate
       for(String tagName: replacementTags.keySet()) {
           String replacementData = replacementTags.get(tagName);

           // for some reason, replaceAll munges the slashes
           replacementData = replacementData.replace('\\', '/');

           // fix any tags in the replacement data
           if(!fixingReplacementTags) {
               replacementData = fixTags(replacementData, true);

               // Refix slashes
               replacementData = replacementData.replace('\\', '/');
           }

           // Make replacement
           fixedTemplateData = fixedTemplateData.replaceAll("<!" + tagName + ">", replacementData);
       }

       return fixedTemplateData;
   }

    /**
     * {@inheritDoc}
     */
    public void clean() {

        File dir = new File(DOCS_DIR);

        // load properties
        try {
            loadGlobalProperties();
        } catch (IOException ex) {
            globalProperties = new Properties();
        }

        if (dir.exists() && dir.isDirectory()) {

            // Filter files
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name) {

                    if (new File(dir, name + TEMPLATE_SUFFIX).exists()) {
                        return true;
                    }

                    if(globalProperties.containsKey("publicWebZipfile")) {
                        if (name.toLowerCase().equals(globalProperties.get("publicWebZipfile"))) {
                            return true;
                        }
                    }

                    return false;
                }
            };

            // List files/folders that pass our filter
            File[] dirs = dir.listFiles(filter);

            // Remove generated files
            for (int i = 0; i < dirs.length; i++) {
                dirs[i].delete();
            }
        }

        // Remove the javadoc directory.
        File javadoc = new File(dir, JAVADOC_DIR);
        Build.clear(javadoc, true);
    }
}