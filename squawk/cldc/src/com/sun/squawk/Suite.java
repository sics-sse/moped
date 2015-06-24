/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle. All Rights Reserved.
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
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */

package com.sun.squawk;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.squawk.pragma.HostedPragma;
import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.LineReader;
import com.sun.squawk.util.SquawkHashtable;
import com.sun.squawk.vm.CID;
import com.sun.squawk.vm.FieldOffsets;
import com.sun.squawk.vm.HDR;

/**
 * A suite is the unit of deployment/compilation in the Squawk system.
 */
public final class Suite {

    /*---------------------------------------------------------------------------*\
     *                            Fields and constructor                         *
    \*---------------------------------------------------------------------------*/

    /**
     * The classes in the suite.
     */
    private Klass[] classes;

    /**
     * The name of the suite.
     */
    private String name;

    /**
     * The array of metadata objects for the classes in this suite. The
     * metadata is sorted by new Suite(new File(suiteName).getName(), parentSuite, suiteType)the associated klass' suiteID, so looking up the metadata for a class involves a
     * binary search. The metadatas array may NOT contain null elements.
     */
    private KlassMetadata[] metadatas;

    private final int type;

    /**
     * The suite that this suite is bound against. That is, the classes of this
     * suite reference classes in the parent suite and its parents.
     */
    private final Suite parent;

    /**
     * Specifies whether or not this suite is open. Only an open suite can have
     * classes installed in it.
     */
    private boolean closed;

	/**
	 * Resource files embedded in the suite.
	 */
    private ResourceFile[] resourceFiles;
	
	/**
	 * Manifest properties embedded in the suite.
	 */
	private ManifestProperty [] manifestProperties;
    
    /**
     * PROPERTIES_MANIFEST_RESOURCE_NAME has already been looked for or found.
     */
    private boolean isPropertiesManifestResourceInstalled;
    
    /**
     * List of classes that should throw a NoClassDefFoundError instead of a ClassNotFoundException.
     * See implementation of {@link Klass#forName(String)} for more information.
     */
    private String[] noClassDefFoundErrorClassNames;

    /**
     * List of classes that are unused in the suite.
     * They will be deleted in the stripped version of the suite.
     * This field not saved in the suite file.
     */
    private Klass[] stripClassesLater;
	
    /**
     * Creates a new <code>Suite</code> instance.
     *
     * @param  name        the name of the suite
     * @param  parent      suite whose classes are linked to by the classes of this suite
     */
    Suite(String name, Suite parent, int type) {
        this.name = name;
        this.parent = parent;
        int count = (isBootstrap() ? CID.LAST_SYSTEM_ID + 1 : 0);
        classes = new Klass[count];
        metadatas = new KlassMetadata[0];
        resourceFiles = new ResourceFile[0];
		manifestProperties = new ManifestProperty [0];
        if (type < 0 || type > METADATA) {
            throw new IllegalArgumentException("type: " + type);
        }
        this.type = type;
        this.configuration = "complete symbolic information available";
    }

    /*---------------------------------------------------------------------------*\
     *                                  Getters                                  *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets this suite's name.
     *
     * @return  this suite's name
     */
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
    	this.name = name;
    }

    /**
     * Gets the parent suite of this suite.
     *
     * @return the parent suite of this suite
     */
    public Suite getParent() {
        return parent;
    }

    /**
     * Gets the URI identifier of the serialized form of this suite.
     *
     * @return the URI from which this suite was loaded or null if the suite was dynamically created
     */
    public String getURI() {
        ObjectMemory om = getReadOnlyObjectMemory();
        if (om != null) {
            return om.getURI();
        } else {
            return null;
        }
    }

    /**
     * Gets the number of classes in this suite.
     *
     * @return the number of classes in this suite
     */
    public int getClassCount() {
        return classes.length;
    }

    /**
     * Determines if this suite is closed. Open an open suite can have classes installed in it.
     *
     * @return boolean
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Get the suite's type (APPLICATION, LIBRARY, etc.
     * @return int representing suite type
     */
    public int getType() {
        return type;
    }

    /**
     * Determines if this is the bootstrap suite containing the system classes.
     *
     * @return true if this suite has no parent
     */
    public boolean isBootstrap() {
        return parent == null;
    }

    /**
     * Gets the next available number for a class that will be installed in this suite.
     * The value returned by this method will never be the same for this suite.
     *
     * @return the next available number for a class that will be installed in this suite
     */
    int getNextAvailableClassNumber() {
        return getClassCount();
    }

    /**
     * Gets the class in this suite corresponding to a given class number.
     *
     * @param   suiteID  the class number of the class to retrieve
     * @return  the class corresponding to <code>suiteID</code>
     */
    public Klass getKlass(int suiteID) {
        Assert.that(suiteID < classes.length);
        return classes[suiteID];
    }

	/**
	 * Gets the contents of a resource file embedded in the suite. 
     * Search parent suites for data before this suite.
	 *
	 * @param name the name of the resource file whose contents is to be retrieved
	 * @return the resource data, or null if the resource file doesn't exist
	 */
	public byte [] getResourceData(String name) {
        int index = getResourceDataIndex(name);
        if (index < 0) {
            return null;
        }
		return resourceFiles[index].data;
	}
    
    /**
	 * Gets the index of the resource file embedded in the suite.
	 *
	 * @param name the name of the resource file whose contents is to be retrieved
	 * @return the index of the resource data, or -1 if the resource file doesn't exist
	 */
	public int getResourceDataIndex(String name) {
        int index = -1;
        // Look in parents first
        if (!isBootstrap()) {
            index = parent.getResourceDataIndex(name);
            if (index >= 0) {
                return index;
            }
        }
        for (int i=0; i < resourceFiles.length; i++) {
            if (resourceFiles[i].name.equals(name)) {
                index = i;
                break;
            }
        }
        return index;
	}

	/**
	 * Return all of the resource files defined for this suite.
	 * 
	 * @return
	 */
	public ResourceFile[] getResourceFiles() {
	    return resourceFiles;
	}

	/**
	 * Gets the names of all manifest properties embedded in this suite.
	 * 
     * @return enumeration over the names
	 */
    Enumeration getManifestPropertyNames() {
		Vector names = new Vector(manifestProperties.length);
		for (int i = 0; i < manifestProperties.length; i++) {
			names.addElement(manifestProperties[i].name);
		}
		return names.elements();
	}
	
	/**
	 * Gets the value of an {@link Suite#PROPERTIES_MANIFEST_RESOURCE_NAME} property embedded in the suite.
	 *
	 * @param name the name of the property whose value is to be retrieved
	 * @return the property value
	 */
	String getManifestProperty(String name) {
		int index = Arrays.binarySearch(manifestProperties, name, ManifestProperty.comparer);
        if (index < 0) {
            // To support dynamic class loading we need to follow the same semantics as Klass.forName
            // which is to look to see if we can't dynamically load a property if its not found
            if (isClosed() || isPropertiesManifestResourceInstalled) {
                return null;
            }
            // The following should automatically install the properties if there is a manifest
            InputStream input = getResourceAsStream(PROPERTIES_MANIFEST_RESOURCE_NAME, null);
            if (input != null) {
                try {input.close();} catch (IOException e) {/*nothing*/};
            }
            isPropertiesManifestResourceInstalled = true;
            index = Arrays.binarySearch(manifestProperties, name, ManifestProperty.comparer);
            if (index < 0) {
                return null;
            }
        }
		return manifestProperties [index].value;
	}
	
    /**
     * Finds a resource with a given name.  This method returns null if no
     * resource with this name is found.  The rules for searching
     * resources associated with a given class are profile
     * specific.
     *
     * @param name  name of the desired resource
     * @param klass Used to get the absolute path to resource if name is not absolute, if null, then assume resource name is absolute
     * @return      a <code>java.io.InputStream</code> object.
     * @since JDK1.1
     */
    final java.io.InputStream getResourceAsStream(String name, Klass klass) {
        if ((name.length() > 0 && name.charAt(0) == '/')) {
            name = name.substring(1);
        } else if (klass != null) {
            String className = klass.getName();
            int dotIndex = className.lastIndexOf('.');
            if (dotIndex >= 0) {
                name = className.substring(0, dotIndex + 1).replace('.', '/') + name;
            }
        }
        byte[] bytes = getResourceData(name);
        if (bytes == null) {
/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
            // TODO Should we throw exceptions here like forName ?, I do not think so, since getting resources is not
            // as hard a requirement as being able to find a class ?
            if (isClosed()) {
                return null;
            }
            Isolate isolate = VM.getCurrentIsolate();
            TranslatorInterface translator = isolate.getTranslator();
            if (translator == null) {
                return null;
            }
            translator.open(isolate.getLeafSuite(), isolate.getClassPath());
            bytes = translator.getResourceData(name);
            if (bytes == null) {
                return null;
            }
/*else[ENABLE_DYNAMIC_CLASSLOADING]*/
//          return null;
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/
        }
        return new ByteArrayInputStream(bytes);
    }

    /**
     * Gets a string representation of this suite. The string returned is
     * name of this suite with "suite " prepended.
     *
     * @return  the name of this suite with "suite " prepended
     */
    public String toString() {
        return "suite " + name + " [type: " + typeToString(type) + ", closed: " + closed + ", parent: " + parent + "]";
    }

    public void printSuiteInfo() {
        System.out.println(this);

        int classcount = 0;
        for (int i = 0; i < classes.length; i++) {
            if (classes[i] != null) {
                classcount++;
            }
        }
        System.out.println("    classes length: " + classes.length + " classes count: " + classcount);
        System.out.println("    metadata length: " + ((metadatas != null) ? metadatas.length : 0));
        if (getParent() != null) {
            getParent().printSuiteInfo();
        }
    }

    /**
     * Gets a reference to the ObjectMemory containing this suite in read-only memory.
     *
     * @return  the ObjectMemory containing this suite if it is in read-only memory or null
     */
    private ObjectMemory getReadOnlyObjectMemoryHosted() throws HostedPragma {
        ObjectMemory result = GC.lookupReadOnlyObjectMemoryByRoot(this);
        if (result != null) {
            return result;
        }
        String uri = (isBootstrap() ? ObjectMemory.BOOTSTRAP_URI : "file://" + name + FILE_EXTENSION);
        return GC.lookupReadOnlyObjectMemoryBySourceURI(uri);
    }

    /**
     * Gets a reference to the ObjectMemory containing this suite in read-only memory.
     *
     * @return  the ObjectMemory containing this suite if it is in read-only memory or null
     */
    ObjectMemory getReadOnlyObjectMemory() {
        if (VM.isHosted()) {
            return getReadOnlyObjectMemoryHosted();
        } else {
            return GC.lookupReadOnlyObjectMemoryByRoot(this);
        }
    }

    /*---------------------------------------------------------------------------*\
     *                            Class installation                             *
    \*---------------------------------------------------------------------------*/

    /**
     * Installs a given class into this suite.
     *
     * @param klass  the class to install
     */
    private void installClass0(Klass klass, int suiteID) {
        checkWrite();
        if (suiteID < classes.length) {
            Assert.that(classes[suiteID] == null, klass + " already installed");
        } else {
            Klass[] old = classes;
            classes = new Klass[suiteID + 1];
            System.arraycopy(old, 0, classes, 0, old.length);
        }
        classes[suiteID] = klass;
    }

    /**
     * Installs a given class into this suite.
     *
     * @param klass  the class to install
     */
    public void installClass(Klass klass) {
        installClass0(klass, klass.getSuiteID());
    }

    /**
     * Copy the boot classes from the original bootstrap suite to new bootstrap suite.
     * If during romize process (like in tck run), a malformed class is detected, we re-run the romizer. 
     * We need to copy over the 
     * 
     * @param originalbootstrap 
     */
    public void reinstallBootClasses(Suite originalbootstrap) {
        checkWrite();
        Isolate isolate = VM.getCurrentIsolate();
        TranslatorInterface translator = isolate.getTranslator();
        for (int i = 0; i <= CID.LAST_SYSTEM_ID; i++) {
            Klass klass = originalbootstrap.getKlass(i);
            classes[klass.getSuiteID()] = klass;
        }
        for (int i = 0; i <= CID.LAST_SYSTEM_ID; i++) {
            Klass klass = originalbootstrap.getKlass(i);
            if (klass != null) {
                klass.resetBootKlass();
                klass.bootLoad(translator);
            }
        }
    }

    /**
     * DCE can remove classes, but we want to keep IDs constant, so install
     * dummy entries...
     */
    public void installFillerClass() {
        int suiteID = getNextAvailableClassNumber();
        installClass0(null, suiteID);
    }

    /**
     * Installs the metadata for a class into this suite. This class to which
     * the metadata pertains must already have been installed and there must
     * be no metadata currently installed for the class.
     *
     * @param metadata  the metadata to install
     */
    private void installMetadata0(KlassMetadata metadata, Klass klass, boolean replace) {
        checkWrite();
        checkSuite();
        int metaindex = getMetadataIndex(klass);
        Assert.that(metadata != null);

        if (metaindex >= 0) {
            if (replace) {
                metadatas[metaindex] = metadata; // replace existing
            } else {
                Assert.that(false, "metadata for " + klass + "already installed");
            }
        } else {
            int newIndex = -metaindex - 1;
            KlassMetadata[] old = metadatas;
            metadatas = new KlassMetadata[metadatas.length + 1];
            System.arraycopy(old, 0, metadatas, 0, newIndex);
            System.arraycopy(old, newIndex,
                    metadatas, newIndex + 1,
                    old.length - newIndex);
            metadatas[newIndex] = metadata;

            // still sorted?
            if (newIndex != 0) {
                Assert.that((klass.getSuiteID() > metadatas[newIndex - 1].getDefinedClass().getSuiteID()));
            }
            if (newIndex < metadatas.length - 1) {
                Assert.that((klass.getSuiteID() < metadatas[newIndex + 1].getDefinedClass().getSuiteID()));
            }
        }
        Assert.that(getMetadata0(klass) == metadata, "replacing: " + replace + ", metadata: " + metadata + ", getMetadata0(klass): " + getMetadata0(klass));
        checkSuite();
    }

    /**
     * Installs the metadata for a class into this suite. This class to which
     * the metadata pertains must already have been installed and there must
     * be no metadata currently installed for the class.
     *
     * @param metadata  the metadata to install
     */
    void installMetadata(KlassMetadata metadata) {
        installMetadata0(metadata, metadata.getDefinedClass(), false);
    }

    public void setUnusedClasses(Klass[] klasses) {
        stripClassesLater = klasses;
    }
    
    /**
     * Installs the metadatas found in metadataSuite directly into my metadatas.  This is done by the Romizer
     * as it saves all of the original metadata into a separate suite, but on loading, these need to be put back
     * into the original Suite.  This original suite turns out to be the parent of the suite containing the metadata
     * 
     * @param metadataSuite
     */
    void pushUpMetadatas() {
        Assert.that(parent != null);
        checkSuite();
        parent.checkSuite();

        boolean oldclosed = parent.closed;
        parent.closed = false; // temp re-open

        for (int i = 0; i < metadatas.length; i++) {
            KlassMetadata km = metadatas[i];
            Assert.that(km != null);
            Klass klass = km.getDefinedClass();
            if (parent.contains(klass)) {
                parent.installMetadata0(km, km.getDefinedClass(), true);
            } else {
                System.out.println("!!! Metadata " + km + " has no klass in parent. klass =" + klass);
            }
        }
        parent.closed = oldclosed;
        parent.checkSuite();
    }
    
    /**
     * If a {@link Klass#forName(String)} is performed and class requested is not found AND
     * its added to our list of {@link #classesToNoClassDefFoundError} then we will throw a
     * {@link NoClassDefFoundError}.
     * 
     * @param classNames
     */
	public void addNoClassDefFoundErrorClassNames(String[] classNames) {
		if (noClassDefFoundErrorClassNames == null && (classNames == null || classNames.length == 0)) {
			return;
		}
		if (noClassDefFoundErrorClassNames == null) {
			noClassDefFoundErrorClassNames = new String[0];
		}
		String[] newNames = new String[noClassDefFoundErrorClassNames.length + classNames.length];
		System.arraycopy(noClassDefFoundErrorClassNames, 0, newNames, 0, noClassDefFoundErrorClassNames.length);
		System.arraycopy(classNames, 0, newNames, noClassDefFoundErrorClassNames.length, classNames.length);
		noClassDefFoundErrorClassNames = newNames;
	}

    /**
     * If a {@link Klass#forName(String)} is performed and class requested is not found AND
     * its added to our list of {@link #classesToNoClassDefFoundError} then we will throw a
     * {@link NoClassDefFoundError}.
     * 
     * @param className
     */
    String[] getNoClassDefFoundErrorClassNames() {
        return noClassDefFoundErrorClassNames;
    }
    
    boolean shouldThrowNoClassDefFoundErrorFor(String className) {
        if (noClassDefFoundErrorClassNames == null) {
            return false;
        }
        for (int i=0; i < noClassDefFoundErrorClassNames.length; i++) {
        	if (noClassDefFoundErrorClassNames[i].equals(className)) {
        		return true;
        	}
        }
        if (parent == null) {
        	return false;
        }
        return parent.shouldThrowNoClassDefFoundErrorFor(className);
    }

    /*---------------------------------------------------------------------------*\
     *                          MIDlet data installation                          *
    \*---------------------------------------------------------------------------*/

	/**
	 * Installs a collection of resource files into this suite. 
	 *
	 * @param resourceFile file to install
	 */
	public void installResource(ResourceFile resourceFile) {
		checkWrite();
        if (resourceFile.name.toUpperCase().equals(PROPERTIES_MANIFEST_RESOURCE_NAME)) {
            if (resourceFile.length == 0) {
                return; // ignore empty manifest files
            }
            isPropertiesManifestResourceInstalled = true;
            // Add the properties defined in the manifest file
            loadProperties(resourceFile.data);
        }
        int index = getResourceDataIndex(resourceFile.name); 
        if (index < 0) {
//          if (VM.isVerbose()) {
            System.out.println("[Including resource: " + resourceFile.name + "]");
//          }
            System.arraycopy(resourceFiles, 0, resourceFiles = new ResourceFile[resourceFiles.length + 1], 0, resourceFiles.length - 1);
            resourceFiles[resourceFiles.length - 1] = resourceFile;
        } else { // replace duplicates
            System.out.println("[Replacing resource: " + resourceFile.name + "]");
            resourceFiles[index] = resourceFile;
        }
	}

    /**
     * Return true if character is a tab or space. Note that readLine() strips off '\n' and '\r'.
     */
    static boolean isWhiteSpace(char ch) {
        return (ch == ' ') || (ch == '\t');
    }
    
    /**
     * Strip the leading white space characters from string "Src", starting from index "start".
     */
    static String stripLeadingWS(String src, int start) {
        int len = src.length();
        while ((start < len) && isWhiteSpace(src.charAt(start))) {
            start++;
        }
        return src.substring(start);
    }
    
    /** 
     * Parse properties from jar manifest file. Based on manifest spec:
     *     http://java.sun.com/j2se/1.4.2/docs/guide/jar/jar.html
     *
     * ABOUT "application descriptors", WHICH ARE NOT SUPPORTED BY THIS METHOD:
     * Note that this syntax is slightly different than the "application descriptor" syntax in the IMP and MIDP specs.
     * An "application descriptor" does not support "continuation lines", or trailing spaces in a value. This is
     * an known annoyance of the MIDP spec.  In addition, the MIDP 1.0 and IMP 1.0 specs have in a bug in the BNF,
     * such that white space is REQUIRED before and after the value. The MIDP 2.0 specs correctly show that such 
     * white space is optional.
     */
    protected void loadProperties(byte[] bytes) {
        LineReader reader = new LineReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
        String line;
        try {
            String key = null;
            String value = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    // empty line, just ignore and go on
                    // NOTE - spec says that this ends the main section. Is this right?
                    continue;
                }
                
                int keyEnd = line.indexOf(':');
                boolean continuationLine = isWhiteSpace(line.charAt(0));
                if (continuationLine) {
                    if ((key == null || value == null)) {
                        throw new IOException("Illformed continuation line :" + line);
                    }
                    value = value + stripLeadingWS(line, 0);
                } else if (keyEnd > 0) {
                    if (key != null) {
                        setProperty(key, value);
                    }
                    key = line.substring(0, keyEnd);
                    value = stripLeadingWS(line, keyEnd+1);
                    // leave this data until next time around.
                } else {
                    throw new IOException("Illformed property line :" + line);
                }
            }
            
            if (key != null) {
                setProperty(key, value);
            }
        } catch (IOException e) {
            if (VM.isVerbose()) {
                System.out.println("Error while loading properties: " + e.getMessage());
            }
        }
    }
    
    public void setProperty(String key, String value) {
        Assert.that(value != null);
        ManifestProperty property = new ManifestProperty(key, value);
        installProperty(property);
    }
    
    /**
	 * Installs a collection of IMlet property values into this suite.
	 *
	 * @param property IMlet property to install
	 */
	public void installProperty(ManifestProperty property) {
		checkWrite();
        // There could be more than one manifest property for a given key,
        // as is the case if JAD properties are added, so take this into account
        int index = Arrays.binarySearch(manifestProperties, property.name, ManifestProperty.comparer);
        if (index < 0) {
            if (VM.isVerbose()) {
                System.out.println("[Adding property key: |" + property.name + "| value: |" + property.value + "|]");
            }
            System.arraycopy(manifestProperties, 0, manifestProperties = new ManifestProperty[manifestProperties.length + 1], 0, manifestProperties.length - 1);
            manifestProperties[manifestProperties.length - 1] = property;
            Arrays.sort(manifestProperties, ManifestProperty.comparer);
        } else {
            if (VM.isVerbose()) {
                System.out.println("[Overwriting property key: |" + property.name + "| value: |" + property.value + "|]");
            }
            manifestProperties[index] = property;
        }
	}

    /*---------------------------------------------------------------------------*\
     *                              Class lookup                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the <code>KlassMetadata</code> instance from this suite
     * corresponding to a specified class.
     *
     * @param    klass  a class
     * @return   the <code>KlassMetadata</code> instance corresponding to
     *                <code>klass</code> or <code>null</code> if there isn't one
     */
    KlassMetadata getMetadata0(Klass klass) {
        if (metadatas != null /*&& contains(klass)*/) {
            int metaindex = Arrays.binarySearch(metadatas, klass, KlassMetadata.comparer);
            if (metaindex >= 0) {
                KlassMetadata metadata = metadatas[metaindex];
                Assert.that(metadata != null);
                Assert.that(metadata.getDefinedClass() == klass, "metadata.getDefinedClass(): " + metadata.getDefinedClass() + " klass: " + klass);
                return metadata;
            }
        }
        Assert.that(searchForMetadata(klass) == null);
        return null;
    }

    /**
     * Gets the <code>KlassMetadata</code> instance from this suite and its parents
     * corresponding to a specified class.
     *
     * @param    klass  a class
     * @return   the <code>KlassMetadata</code> instance corresponding to
     *                <code>klass</code> or <code>null</code> if there isn't one
     */
    KlassMetadata getMetadata(Klass klass) {
        // Look in parents first
        if (!isBootstrap()) {
            KlassMetadata metadata = parent.getMetadata(klass);
            if (metadata != null) {
                return metadata;
            }
        }

        if (contains(klass)) {
            return getMetadata0(klass);
        }
        return null;
    }

    KlassMetadata searchForMetadata(Klass klass) {
        // Look in parents first
        if (!isBootstrap()) {
            KlassMetadata metadata = parent.searchForMetadata(klass);
            if (metadata != null) {
                return metadata;
            }
        }

        if (metadatas != null && contains(klass)) {
            for (int i = 0; i < metadatas.length; i++) {
                if (metadatas[i].getDefinedClass() == klass) {
                    return metadatas[i];
                }
            }
        }
        return null;
    }

    /**
     * Gets the index of the metadata for this class in this suite.
     *
     * @param    klass  a class
     * @return   the index or a negative number
     */
    int getMetadataIndex(Klass klass) {
        Assert.that(metadatas != null);
        Assert.that(contains(klass) || (type == METADATA && parent.contains(klass)), this + " doesn't contain " + klass);
        return Arrays.binarySearch(metadatas, klass, KlassMetadata.comparer);
    }

    /**
     * Gets the <code>Klass</code> instance from this suite corresponding
     * to a specified class name in internal form.
     *
     * @param   name     the name (in internal form) of the class to lookup
     * @return  the <code>Klass</code> instance corresponding to
     *                   <code>internalName</code> or <code>null</code> if there
     *                   isn't one.
     */
    public Klass lookup(String name) {
        // Look in parents first
        if (!isBootstrap()) {
            Klass klass = parent.lookup(name);
            if (klass != null) {
                return klass;
            }
        }
        
        for (int i = 0 ; i < classes.length ; i++) {
            Klass klass = classes[i];
            if (klass != null) {
                if (klass.getInternalName().compareTo(name) == 0) { // bootstrapping issues prevent the use of equals()
                    return klass;
                }
            }
        }
        return null;
    }

    public void showclasses() {
        for (int i = 0 ; i < classes.length ; i++) {
            Klass klass = classes[i];
            if (klass != null) {
		VM.println("Klass lookup sees " + klass.getInternalName());
            }
        }
    }

    public void showparentclasses() {
	parent.showclasses();
    }

    /**
     * Returns true if this suite contains the given klass.
     *
     * @param   klass     the klass
     * @return  true if klass belongs to this suite
     */
    public boolean contains(Klass klass) {
        int id = klass.getSuiteID();
        if (id < classes.length && classes[id] != null) {
            if (classes[id] == klass) {
                return true;
            } else if (klass.getInternalName().equals(classes[id].getInternalName())) {
                System.out.println("!!! KLASSES NOT EQUAL, BUT SAME NAME: " + klass + " != " + classes[id]);
            }
        }
        return false;
    }

    /**
     * Ensures that this suite is not in read-only memory before being updated.
     *
     * @throws IllegalStateException if this suite is closed
     * @throws IllegalStoreException if this suite is in read-only memory
     */
    private void checkWrite() {
        if (closed) {
            throw new IllegalStateException(this + " is closed");
        }
        if (!VM.isHosted() && !GC.inRam(this)) {
            throw new IllegalStoreException("trying to update read-only object: " + this);
        }
    }

    /*---------------------------------------------------------------------------*\
     *                            hashcode & equals                              *
    \*---------------------------------------------------------------------------*/

    /**
     * Compares this suite with another object for equality. The result is true
     * if and only if <code>other</code> is a <code>Suite</code> instance
     * and its name is equal to this suite's name.
     *
     * @param   other   the object to compare this suite against
     * @return  true if <code>other</code> is a <code>Suite</code> instance
     *                  and its name is equal to this suite's name
     */
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof Suite) {
            return name.equals(((Suite)other).name);
        }
        return false;
    }

    /**
     * Returns a hashcode for this suite which is derived solely from the
     * suite's name.
     *
     * @return  the hashcode of this suite's name
     */
    public final int hashCode() {
        return name.hashCode();
    }
    
    /**
     * Gets the Suite corresponding to a given URI, loading it if necessary.
     * NOTE: Suite loading is enabled by the ENABLE_SUITE_LOADING build property.
     *
     * @param uri   the URI identifying the object memory
     * @param errorOnIOException if true, throw an Error if an IOException occurs, otherwise return null.
     * @return the Suite inside the object memory identified by <code>uri</code>
     * @throws Error if the suite denoted by URI is not available or there was
     *         a problem while loading it
     */
    static Suite getSuite(String uri, boolean errorOnIOException) throws Error {
	return getSuite(uri, errorOnIOException, false);
    }

	static Suite getSuite(String uri, boolean errorOnIOException,
			      boolean alwaysLoad) throws Error {
	VM.println("getSuite " + alwaysLoad + " " + uri);
	ObjectMemory om;
	if (alwaysLoad) {
	    om = null;
	} else {
	    om = GC.lookupReadOnlyObjectMemoryBySourceURI(uri);
	}

        if (om == null) {
/*if[ENABLE_SUITE_LOADING]*/      		
    		try {
		    // Arndt: changed true to false
                om = ObjectMemoryLoader.load(uri, false).objectMemory;
            } catch (IOException e) {
                if (errorOnIOException) {
                    e.printStackTrace();
                    throw new Error("IO error while loading suite from '" + uri + "': " + e);
                } else {
                    return null;
                }
            }
            
            /*else[ENABLE_SUITE_LOADING]*/
//            if (errorOnIOException) {
//                throw new Error("Suite loading not supported: " + uri);
//            } else {
//                return null;
//            }
/*end[ENABLE_SUITE_LOADING]*/
        }

        Object root = om.getRoot();
        if (!(root instanceof Suite)) {
	    VM.println("getSuite: blop");
            throw new Error("object memory in '" + om.getURI() + "' does not contain a suite");
        }
        Suite rootSuite = (Suite)root;
        return (Suite)rootSuite;
    }
    
    /**
     * Gets the Suite corresponding to a given URI, loading it if necessary.
     *
     * @param uri   the URI identifying the object memory
     * @return the Suite inside the object memory identified by <code>uri</code>
     * @throws Error if the suite denoted by URI is not available or there was
     *         a problem while loading it
     */
     static Suite getSuite(String uri) throws Error {
         return getSuite(uri, true);
     }

    /**
     * Describes the configuration of the suite.
     */
    private String configuration;

    /**
     * Gets the configuration of the suite.
     *
     * @return the configuration of the suite
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * Serializes the object graph rooted by this suite and writes it to a given stream.
     *
     * @param  dos       the DataOutputStream to which the serialized suite should be written
     * @param  uri       the URI identifier of the serialized suite
     * @param  bigEndian the endianess to be used when serializing this suite
     *
     * @return if hosted, returns the objectMemory that suite was saved to
     * @throws OutOfMemoryError if there was insufficient memory to do the save
     * @throws IOException if there was some IO problem while writing the output
     */
    public ObjectMemory save(DataOutputStream dos, String uri, boolean bigEndian) throws HostedPragma, java.io.IOException, OutOfMemoryError {
        stripClassesLater = null; // don't save this...
        ObjectMemorySerializer.ControlBlock cb = VM.copyObjectGraph(this);
        ObjectMemory parentMemory = null;
        if (!isBootstrap()) {
            parentMemory = parent.getReadOnlyObjectMemory();
            Assert.always(parentMemory != null); // "parent not found: " + parent
        }
        checkSuite();
        ObjectMemorySerializer.save(dos, uri, cb, parentMemory, bigEndian);

        ObjectMemory objectMemory;
        if (VM.isHosted()) {
            objectMemory = saveHosted(uri, cb, parentMemory);
        } else {
        	objectMemory = null;
        }
        return objectMemory;
    }

//    /**
//     * Serializes the object graph rooted by this suite and writes it to a given stream.
//     *
//     * @param  dos       the DataOutputStream to which the serialized suite should be written
//     * @param  uri       the URI identifier of the serialized suite
//     * @param  bigEndian the endianess to be used when serializing this suite
//     *
//     * @throws OutOfMemoryError if there was insufficient memory to do the save
//     * @throws IOException if there was some IO problem while writing the output
//     */
//    public void saveKlassMetadatas(DataOutputStream dos, String uri, boolean bigEndian) throws HostedPragma, java.io.IOException, OutOfMemoryError {
//        int originalMemorySize = NativeUnsafe.getMemorySize();
//        ObjectMemorySerializer.ControlBlock cb = VM.copyObjectGraph(metadatas);
//        ObjectMemorySerializer.save(dos, uri, cb, getReadOnlyObjectMemory(), bigEndian);
//        if (VM.isHosted()) {
//            saveHosted(uri, cb, null);
//        }
//        NativeUnsafe.setMemorySize(originalMemorySize);
//    }

    /**
     * Serializes the object graph rooted by this suite and writes it to a given stream.
     * FIXME: what does this method REALLY do?
     *
     * @param  uri       the URI identifier of the serialized suite
     */
    private ObjectMemory saveHosted(String uri, ObjectMemorySerializer.ControlBlock cb, ObjectMemory parentMemory) throws HostedPragma {
        Address start = parentMemory == null ? Address.zero() : parentMemory.getEnd();
        int hash = ObjectMemoryLoader.hash(cb.memory);
        ObjectMemory om = new ObjectMemory(start, cb.memory.length, uri, this, hash, parentMemory);
        GC.registerReadOnlyObjectMemory(om);
        return om;
    }

    /**
     * Denotes a suite that encapsulates an application. The classes of an application
     * can not be linked against.
     */
    public static final int APPLICATION = 0;

    /**
     * Denotes a suite that encapsulates a library. The classes of a library
     * can be linked against but the library itself cannot be extended by virtue
     * of other classes linking against it's package private components.
     */
    public static final int LIBRARY = 1;

    /**
     * Denotes a suite that encapsulates an open library. The classes of an open library
     * can be linked against and the library itself can be extended by virtue
     * of other classes linking against it's package private components.
     */
    public static final int EXTENDABLE_LIBRARY = 2;

    /**
     * Denotes a suite that is being debugged. This suite retains all its symbolic information
     * when closed.
     */
    public static final int DEBUG = 3;
    
    /**
     * Denotes a suite that contains all the KlassMetadata for its parent suite.  This is to allow
     * the parent suite to have zero symbolic information, but still have the information
     * available.
     */
    public static final int METADATA = 4;

    /**
     * File name extension that identifies a Suite, includes the '.'.
     * 
     * Duplicated in builder, com.sun.squawk.builder.commands.MakeAPI
     */
    public static final String FILE_EXTENSION = ".suite";

    /**
     * File name extension that identifies a Suite's api, includes the '.'.
     * 
     * Duplicated in builder, com.sun.squawk.builder.commands.MakeAPI
     */
    public static final String FILE_EXTENSION_API = ".api";

    /**
     * File name extension that identifies a Suite's metadata, includes the '.'.
     * 
     * Duplicated in builder, com.sun.squawk.builder.commands.MakeAPI
     */
    public static final String FILE_EXTENSION_METADATA = ".metadata";
    
    /**
     * Denotes the name of the resource that represents the resource name from which I extract
     * properties from when an {@link #installResource(ResourceFile)} is done.
     */
    public static final String PROPERTIES_MANIFEST_RESOURCE_NAME = "META-INF/MANIFEST.MF";

    /**
     * Given one of the defined suite types, return an English string describing the
     * suite type.
     *
     * @param suiteType One of APPLICATION, LIBRARY, EXTENDABLE_LIBRARY, or DEBUG.
     * @return a string describing the suite type.
     */
    public static String typeToString(int suiteType) {
        switch (suiteType) {
            case APPLICATION:
                return "application";
            case LIBRARY:
                return "library";
            case EXTENDABLE_LIBRARY:
                return "extendable library";
            case DEBUG:
                return "debug";
            case METADATA:
                return "metadata";
            default:
                return "?";
        }
    }

    /**
     * Closes this suite. Once closed, a suite is immutable (and may well reside in
     * read-only memory) and cannot have any more classes installed in it
     */
    public void close() {
        closed = true;
    }

    /**
     * Create a new KlassMetadata array that has all of the elements of originalMetadatas except
     * metadata for classes in the strippedClasses set. 
     * @param originalmetadatas, sorted by suiteID, with no nulls
     * @param strippedClasses set of classes to be stripped
     * @return new array, sorted by suiteID, with no nulls
     */
    private static KlassMetadata[] removeStrippedKlassesFromMetadata(KlassMetadata[] originalMetadatas, SquawkHashtable deadClasses) {
        if (originalMetadatas == null) {
            return null;
        }
        
        Vector metadatasV = new Vector(originalMetadatas.length);
        for (int i = 0; i < originalMetadatas.length; i++) {
            KlassMetadata metadata = originalMetadatas[i];
            if (metadata != null) {
                if (deadClasses.contains(metadata.getDefinedClass())) {
//                    if (VM.isVerbose()) {
//                        System.out.println("Removing metadata from suite for : " + metadata.getDefinedClass());
//                    }
                } else {
                    metadatasV.addElement(metadata);
                }
            }
        }
        KlassMetadata[] newmetadatas = new KlassMetadata[metadatasV.size()];
        metadatasV.copyInto(newmetadatas);
        return newmetadatas;
    }

    /**
     * Creates a copy of this suite with its symbolic information stripped according to
     * the given parameters.
     *
     * @param type  specifies the type of the suite after closing. Must be
     *              {@link #APPLICATION}, {@link #LIBRARY}, {@link #EXTENDABLE_LIBRARY} or {@link #DEBUG}.
     * @param name new suite name
     * @param parent
     * @return stripped copy of this suite
     */
    public Suite strip(int type, String name, Suite parent) {
        if (type < APPLICATION || type > METADATA) {
            throw new IllegalArgumentException();
        }
        checkSuite();
        
        Suite copy = new Suite(name, parent, type);
        SquawkHashtable deadClasses = null; // set of stripped classes

        if (stripClassesLater != null) {
            if (VM.isVerbose()) {
                System.out.println("Removing " + stripClassesLater.length + " classes from " + copy);
            }

            // make set of classes to be removed
            deadClasses = new SquawkHashtable(stripClassesLater.length);
            for (int i = 0; i < stripClassesLater.length; i++) {
                Klass deadClass = stripClassesLater[i];
                deadClasses.put(deadClass, deadClass);
            }
        }

        if (type == METADATA) {
        	copy.classes = new Klass[0];
            // it's finally "later":
            if (stripClassesLater == null) {
                copy.metadatas = new KlassMetadata[metadatas.length];
                System.arraycopy(metadatas, 0, copy.metadatas, 0, metadatas.length);
            } else {
                // it's finally "later"- strip metadata for dead classes:
                copy.metadatas = removeStrippedKlassesFromMetadata(metadatas, deadClasses);
            }
        } else {
            copy.classes = new Klass[classes.length];

            if (noClassDefFoundErrorClassNames != null) {
	            copy.noClassDefFoundErrorClassNames = new String[noClassDefFoundErrorClassNames.length];
	            System.arraycopy(noClassDefFoundErrorClassNames, 0, copy.noClassDefFoundErrorClassNames, 0, noClassDefFoundErrorClassNames.length);
            }

            copy.resourceFiles = new ResourceFile[resourceFiles.length];
            System.arraycopy(resourceFiles, 0, copy.resourceFiles, 0, resourceFiles.length);

    		copy.manifestProperties = new ManifestProperty [manifestProperties.length];
    		System.arraycopy(manifestProperties, 0, copy.manifestProperties, 0, manifestProperties.length);

            KlassMetadata[] tempMetadatas = KlassMetadata.strip(this, metadatas, type); // tempMetadatas is still sorted by suiteID, but may contain nulls.

            if (stripClassesLater == null) {
                System.arraycopy(classes, 0, copy.classes, 0, classes.length);
                copy.metadatas = removeStrippedKlassesFromMetadata(tempMetadatas, new SquawkHashtable(0)); // remove nulls, but not dead classes.
            } else {
                // it's finally "later":

//                // this version strips out the null entries in the classes array, and renumbers the classes. System gets too confused by this though...
//                int newlength = classes.length - stripClassesLater.length;
//                Vector classesV = new Vector(newlength);
//                for (int i = 0; i < classes.length; i++) {
//                    Klass klass = classes[i];
//                    if (deadClasses.contains(klass)) {
//                        if (VM.isVerbose()) {
//                            System.out.println("Removing from suite: " + klass);
//                        }
//                    } else {
//                        int count = classesV.size();
//                        Assert.that(count <= klass.getSuiteID());
//                        Klass.setSuiteID(klass, count);
//                        classesV.addElement(klass);
//
//                    }
//                }
//                copy.classes = new Klass[classesV.size()];
//                classesV.copyInto(copy.classes);

                // strip dead classes
                for (int i = 0; i < copy.classes.length; i++) {
                    Klass klass = classes[i];
                    if (klass != null && deadClasses.contains(klass)) {
                        if (Klass.DEBUG_CODE_ENABLED && VM.isVerbose()) {
                            System.out.println("Removing from suite: " + klass);
                        }
                        copy.classes[i] = null;
                    } else {
                        copy.classes[i] = klass;
                    }
                }

                // strip metadata for dead classes:
               copy.metadatas = removeStrippedKlassesFromMetadata(tempMetadatas, deadClasses);

//                for (int i = 0; i < copy.classes.length; i++) {
//                    Klass klass = copy.classes[i];
//                    if (klass != null) {
//                        int metadataindex = (copy.metadatas == null) ? -1 : copy.getMetadataIndex(klass);
//                        System.out.println("classes[" + i + "] = " + klass + ", id = " + klass.getSuiteID());
//                        if (metadataindex >= 0) {
//                           System.out.println("metadata[" + metadataindex + "] = metadata for " + copy.metadatas[metadataindex].getDefinedClass());
//                      }
//                    }
//                }
            }
        }

        copy.updateConfiguration(type);
		copy.checkSuite();
        return copy;
    }

    /**
     * Updates the configuration description of this suite based on the parameters that it
     * is {@link #strip stripped} with.
     */
    private void updateConfiguration(int type) {
        if (type == DEBUG) {
            configuration = "symbols not stripped";
        } else {
            configuration = "symbols stripped in " + typeToString(type) + " mode";
        }
    }

    void checkSuite() {
        for (int i = 0; i < classes.length; i++) {
            Klass klass = classes[i];
            if (klass != null) {
                Assert.always(klass.getSuiteID() == i);
                KlassMetadata km = getMetadata(klass);
                if (km != null) {
                    if (km.getDefinedClass() != klass) {
                        System.out.println("<><><><><><><><> klass is " + klass + " metadata is for " + km.getDefinedClass());
                        System.out.println("<><><><><><><><> klass ID is " + klass.getSuiteID() + " metadata is for ID " + km.getDefinedClass().getSuiteID());
                        System.out.println("<><><><><><><><> i is " + i);
                        System.out.println("<><><><><><><><> suite is " + this);
                    }
                    Assert.always(km.getDefinedClass() == klass);
                }
            }
        }

        if (metadatas != null) {
            int orderCheck = -1;
            for (int i = 0; i < metadatas.length; i++) {
                KlassMetadata km = metadatas[i];
                Assert.that(km!= null);
                Klass klass = km.getDefinedClass();
                //Assert.that(contains(klass));
                Assert.that(klass.getSuiteID() > orderCheck, " metadatas not sorted by klass suite ID");
                orderCheck = klass.getSuiteID();
            }
        }
    }

    void printMetadatainfo() {
        if (metadatas != null) {
            for (int i = 0; i < metadatas.length; i++) {
                KlassMetadata km = metadatas[i];
                System.out.println("metadata[" + i + "] = metadata for " + metadatas[i].getDefinedClass());
            }
        }
    }
}
