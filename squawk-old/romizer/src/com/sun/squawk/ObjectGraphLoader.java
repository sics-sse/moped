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

package com.sun.squawk;

import java.io.*;
import java.util.*;

import com.sun.squawk.romizer.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;

/**
 * This class provides the ability to load an object graph created by an {@link ObjectGraphSerializer}, ie load a Squawk memory into HotSpot VM.
 * 
 * NOTES:
 * - Is it possible for an object in a suite to have an association instead of a pointer to its class ?
 *   - Does this mean there is a bug in Isolate migration ?  As how does this association get serialized ?
 *   - It is possible that the romizer ignores this, and that the object serialization that occurs at runtime is not the same
 * 
 *
 */
public class ObjectGraphLoader {
	
	protected Map<Address, Object> addressToObjectMap = new IdentityHashMap<Address, Object>();
	protected final Stack<Map<Address, Object>> addressToObjectMapStack = new Stack<Map<Address,Object>>();
	protected Map<Object, Address> objectToAddressMap = new IdentityHashMap<Object, Address>();
	protected final Stack<Map<Object, Address>> objectToAddressMapStack = new Stack<Map<Object, Address>>();
	protected ObjectGraphLoaderTranslator translator = new ObjectGraphLoaderTranslator();
	protected boolean skipKlassInternals;
    protected Suite suite;

	public ObjectGraphLoader() {
	}
	
	public Map<Object, Address> getObjectToAddressMap() {
		return objectToAddressMap;
	}
	
	protected byte[] getBytesAt(Address address) {
		if (address.isZero()) {
			return null;
		}
		Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (byte[]) object;
		}
        int length = GC.getArrayLengthNoCheck(address);
        byte[] bytes = new byte[length];
        for (int i = 0 ; i < length; i++) {
        	bytes[i] = (byte) (NativeUnsafe.getByte(address, i) & 0xFF);
        }
        addressToObjectMap.put(address, bytes);
        objectToAddressMap.put(bytes, address);
        return bytes;
	}

    protected Klass getKlassAt(Address address) {
		if (address.isZero()) {
			return null;
		}
    	Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (Klass) object;
		}
        Address nameAddress = NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$name));
        String name = getStringAt(nameAddress);
        Klass klass = Klass.getClass(name, false);
        addressToObjectMap.put(address, klass);
        objectToAddressMap.put(klass, address);
        if (!skipKlassInternals) {
        	initKlassInternals(klass);
        }
        return klass;
    }
    
    protected void initKlassInternals(Klass klass) {
    	Address address = objectToAddressMap.get(klass);
    	MethodBody[] virtualMethodBodies = getMethodBodiesAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$virtualMethods)));
    	MethodBody[] staticMethodBodies = getMethodBodiesAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$staticMethods)));
        Klass superType = getKlassAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$superType)));
        Klass[] interfaces = getKlassesAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$interfaces)));
        UWord[] oopMap = getUWordsAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$oopMap)));
        UWord oopMapWord = NativeUnsafe.getUWord(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$oopMapWord));
        UWord[] dataMap = getUWordsAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$dataMap)));
        UWord dataMapWord = NativeUnsafe.getUWord(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$dataMapWord));
        short dataMapLength = (short)NativeUnsafe.getShort(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$dataMapLength));
        int modifiers = NativeUnsafe.getInt(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$modifiers));
        byte state = (byte) NativeUnsafe.getByte(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$state));
        short instanceSizeBytes = (short) NativeUnsafe.getShort(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$instanceSizeBytes));
        short staticFieldsSize = (short) NativeUnsafe.getShort(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$staticFieldsSize));
        short refStaticFieldsSize = (short) NativeUnsafe.getShort(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$refStaticFieldsSize));
        byte initModifiers = (byte) NativeUnsafe.getByte(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$initModifiers));
        klass.initForObjectGraphLoader(virtualMethodBodies, staticMethodBodies, superType, interfaces, null, oopMap, oopMapWord, dataMap, dataMapWord, dataMapLength, modifiers, state, instanceSizeBytes, staticFieldsSize, refStaticFieldsSize, initModifiers);
    }
    
    protected Klass[] getKlassesAt(Address address) {
		if (address.isZero()) {
			return null;
		}
        Klass[] klasses;
    	Object object = addressToObjectMap.get(address);
		if (object != null) {
            klasses = (Klass[]) object;
			return klasses;
		}
        int length = GC.getArrayLengthNoCheck(address);
        klasses = new Klass[length];
        for (int i=0; i < klasses.length; i++) {
        	klasses[i] = getKlassAt(NativeUnsafe.getAddress(address, i));
            if (klasses[i] == null) {
                suite.installFillerClass();
            }
        }
        addressToObjectMap.put(address, klasses);
        objectToAddressMap.put(klasses, address);
        return klasses;
    }

    protected KlassMetadata getKlassMetadataAt(Address address) {
		if (address.isZero()) {
			return null;
		}
    	Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (KlassMetadata) object;
		}
        Address definedKlassAddress = NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_KlassMetadata$definedClass));
        Klass definedClass = getKlassAt(definedKlassAddress);
        Address symbolsAddress = NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_KlassMetadata$symbols));
        byte[] symbols = getBytesAt(symbolsAddress);
        Address classTableAddress = NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_KlassMetadata$classTable));
        Klass[] classTable = getKlassesAt(classTableAddress);

        KlassMetadata metadata = new KlassMetadata(definedClass, symbols, classTable);
        addressToObjectMap.put(address, metadata);
        objectToAddressMap.put(metadata, address);
        return metadata;
    }
    
    protected KlassMetadata[] getKlassMetadatasAt(Address address) {
		if (address.isZero()) {
			return null;
		}
    	Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (KlassMetadata[]) object;
		}
        int length = GC.getArrayLengthNoCheck(address);
        KlassMetadata[] metadatas = new KlassMetadata[length];
        for (int i=0; i < metadatas.length; i++) {
        	Address metadataAddress = NativeUnsafe.getAddress(address, i);
        	KlassMetadata metadata = getKlassMetadataAt(metadataAddress);
        	metadatas[i] = metadata;
        }
        addressToObjectMap.put(address, metadatas);
        objectToAddressMap.put(metadatas, address);
        return metadatas;
    }

    protected MethodBody getMethodBodyAt(Address address) {
		if (address.isZero()) {
			return null;
		}
    	Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (MethodBody) object;
		}
		MethodBody methodBody = new MethodBody();
        addressToObjectMap.put(address, methodBody);
        objectToAddressMap.put(methodBody, address);
        return methodBody;
    }

    protected MethodBody[] getMethodBodiesAt(Address address) {
		if (address.isZero()) {
			return null;
		}
    	Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (MethodBody[]) object;
		}
        int length = GC.getArrayLengthNoCheck(address);
        MethodBody[] methodBodies = new MethodBody[length];
        for (int i=0; i < methodBodies.length; i++) {
            Address methodBodyAddress = NativeUnsafe.getAddress(address, i);
        	methodBodies[i] = getMethodBodyAt(methodBodyAddress);
        }
        addressToObjectMap.put(address, methodBodies);
        objectToAddressMap.put(methodBodies, address);
        return methodBodies;
    }

	protected String getStringAt(Address address) {
		if (address.isZero()) {
			return null;
		}
		Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (String) object;
		}
        int length = GC.getArrayLengthNoCheck(address);
        // get the class ID of the string
        Address klassAddress = NativeUnsafe.getAddress(address, HDR.klass);
        int classID = NativeUnsafe.getShort(klassAddress, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Klass$id));
        Assert.that(classID == CID.STRING || classID == CID.STRING_OF_BYTES);
        // assume it is an 8-bit string
        StringBuffer buf = new StringBuffer(length);
        for (int i = 0 ; i < length; i++) {
            char ch;
            if (classID == CID.STRING) {
                ch = (char) NativeUnsafe.getChar(address, i);
            } else {
                ch = (char)(NativeUnsafe.getByte(address, i) & 0xFF);
            }
            buf.append(ch);
        }
        String string = buf.toString();
        addressToObjectMap.put(address, string);
        objectToAddressMap.put(string, address);
        return string;
	}
	
	public Suite loadSuite(String url) throws IOException {
        String bootstrapSuiteProperty = System.getProperty(ObjectMemory.BOOTSTRAP_URI_PROPERTY);
        if (bootstrapSuiteProperty == null) {
            bootstrapSuiteProperty = "file://squawk.suite";
        }
        
        if (NativeUnsafe.getMemorySize() == 0) {
            GC.initialize();
        }
        
        ObjectMemory objectMemory = GC.lookupReadOnlyObjectMemoryBySourceURI(url);
        if (objectMemory == null) {
    		if (url.equals(bootstrapSuiteProperty)) {
    			objectMemory = GC.lookupReadOnlyObjectMemoryBySourceURI(ObjectMemory.BOOTSTRAP_URI);
    		}
        }
        if (objectMemory != null) {
        	Assert.that(objectMemory.getRoot() instanceof Suite);
        	return (Suite) objectMemory.getRoot();
        }

        objectMemory = ObjectMemoryLoader.load(url, false).objectMemory;
        if (objectMemory == null) {
        	throw new IOException("No object memories found with URL: " + url);
        }
        ArrayList<ObjectMemory> objectMemories = new ArrayList<ObjectMemory>();
        while (objectMemory != null) {
            objectMemories.add(0,objectMemory);
            objectMemory = objectMemory.getParent();
        }
        Address allocTop = Address.zero();
        Suite parentSuite = null;
        for (ObjectMemory eachObjectMemory : objectMemories) {
            Object root = eachObjectMemory.getRoot();
            if (root instanceof Address) {
            	Suite suite = getSuiteAt((Address) root, parentSuite);
            	eachObjectMemory.setRoot(suite);
            	parentSuite = suite;
                GC.registerReadOnlyObjectMemory(eachObjectMemory);
                allocTop = allocTop.add(NativeUnsafe.getMemorySize());
            	// Get all String objects, I should really go through and get all objects, but lets do this for now.
                Address end = eachObjectMemory.getStart().add(eachObjectMemory.getSize());
                for (Address block = eachObjectMemory.getStart(); block.lo(end); ) {
                    Address object = GC.blockToOop(block);
                    
                    Address classOrAssociation = NativeUnsafe.getAddress(object, HDR.klass);
                    Assert.that(!classOrAssociation.isZero());
                    Address klassAddress = NativeUnsafe.getAddress(classOrAssociation, (int)FieldOffsets.com_sun_squawk_Klass$self);
                    Assert.that(!klassAddress.isZero());
                    Klass klass = getKlassAt(klassAddress);
                    if (Klass.STRING.isAssignableFrom(klass)) {
                    	getStringAt(object);
                    }
                    block = object.add(GC.getBodySize(klass, object));
                }
        		// Load the .metadata suite
                String metadataUrl = (suite.isBootstrap()?bootstrapSuiteProperty:eachObjectMemory.getURI()) + Suite.FILE_EXTENSION_METADATA;
                if (false && ObjectMemoryLoader.SIGNATURE_SCHEME == ObjectMemoryLoader.CHAINED_SIGNATURE) {
                    // we loaded the "unisgned-" version of teh suite, but the medata suite does not have that name (it is never signed), so look for correct name.
                    int index = metadataUrl.indexOf("unsigned-");
                    int len = "unsigned-".length();
                    if (index > 0) {
                        String newURL = metadataUrl.substring(0, index) + metadataUrl.substring(index + len);
                        metadataUrl = newURL;
                    }
                }
            	try {
                	int memorySizePrior = NativeUnsafe.getMemorySize();
	            	ObjectMemory metadataObjectMemory = ObjectMemoryLoader.load(metadataUrl, false).objectMemory;
	            	Suite metadataSuite;
	            	pushObjectMap();
	            	try {
	            		metadataSuite = getSuiteAt((Address) metadataObjectMemory.getRoot(), suite);
	            	} finally {
		            	popObjectMap();
	            	}
            		metadataSuite.pushUpMetadatas();
            		NativeUnsafe.setMemorySize(memorySizePrior);
            	} catch (IOException e) {
            	    throw new UnexpectedException("Unable to find metadata suite: " + metadataUrl, e);
            	}
            } else if (root instanceof Suite) {
            	parentSuite = (Suite) root;
            } else {
            	Assert.shouldNotReachHere();
            }
            GC.setAllocTop(allocTop);
        }
        ObjectGraphSerializer.addObjectsToAddress(getObjectToAddressMap());
        parentSuite.checkSuite();
        return parentSuite;
	}

	protected Suite getSuiteAt(Address address, Suite parent) {
		if (address.isZero()) {
			return null;
		}
    	Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (Suite) object;
		}
		String name = getStringAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Suite$name)));
		suite = new Suite(name, parent, NativeUnsafe.getInt(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Suite$type)));

		VM.setCurrentIsolate(null);
        Isolate isolate = new Isolate(null, null, suite);
        isolate.setTranslator(translator);
        VM.setCurrentIsolate(isolate);
        
        // Force Klass init, as when in hosted mode it will load the bootstrap classes on its own
        Klass.getClass("-null-", false);

        skipKlassInternals = true;
        Klass[] klasses = getKlassesAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Suite$classes)));
        skipKlassInternals = false;
        for (int i=0; i < klasses.length; i++) {
        	Klass klass = klasses[i];
            if (klass != null) {
                initKlassInternals(klass);
                if (suite.getKlass(i) != klass) {
                    System.err.println("Odd: classes are not equal at " + i);
                    System.err.println("    suite.getKlass(i): " + suite.getKlass(i));
                    System.err.println("    klass: " + klass);
                }
                Assert.that(suite.getKlass(i) == klass);
            }
		}

        KlassMetadata[] metadatas = getKlassMetadatasAt(NativeUnsafe.getAddress(address, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Suite$metadatas)));
        if (metadatas != null) {
            for (KlassMetadata metadata : metadatas) {
                Assert.that(metadata != null);
                suite.installMetadata(metadata);
            }
        }

        suite.close();
        if (suite.getType() == Suite.METADATA && suite.getClassCount() != 0) {
            throw new RuntimeException("Metadata suites should not have any classes. " + suite + " has " + suite.getClassCount());
        }
        addressToObjectMap.put(address, suite);
        objectToAddressMap.put(suite, address);
		return suite;
	}
	
	protected UWord[] getUWordsAt(Address address) {
		if (address.isZero()) {
			return null;
		}
		Object object = addressToObjectMap.get(address);
		if (object != null) {
			return (UWord[]) object;
		}
        int length = GC.getArrayLengthNoCheck(address);
        UWord[] uwords = new UWord[length];
        for (int i = 0 ; i < length; i++) {
        	uwords[i] = NativeUnsafe.getUWord(address, i);
        }
        addressToObjectMap.put(address, uwords);
        objectToAddressMap.put(uwords, address);
        return uwords;
	}

	protected void popObjectMap() {
        addressToObjectMap = addressToObjectMapStack.pop();
        objectToAddressMap = objectToAddressMapStack.pop();
	}
	
	// Take a copy of the object and address maps, such that none of the objects read in from metadata suite are used for follow on suites
	protected void pushObjectMap() {
		addressToObjectMapStack.push(addressToObjectMap);
		addressToObjectMap = new IdentityHashMap<Address, Object>(addressToObjectMap);
		objectToAddressMapStack.push(objectToAddressMap);
		objectToAddressMap = new IdentityHashMap<Object, Address>(objectToAddressMap);
	}
	
}
