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

package com.sun.squawk.debugger.sdp;

import java.io.*;
import java.util.*;

import com.sun.squawk.debugger.*;
import com.sun.squawk.debugger.DataType.*;
import com.sun.squawk.util.*;
import com.sun.squawk.*;

/**
 * A ProxyTypeManager instance is responsible for managing the collection
 * of classes that the SDP 'knows' about.
 *
 */
class ProxyTypeManager {

    private final static int DEFAULT_NUM_REFS = 700;

    /**
     * The map from Klass instances to the corresponding ProxyType instances.
     */
    private HashMap<Klass, ProxyType> classToType = new HashMap<Klass, ProxyType>(DEFAULT_NUM_REFS);

    /**
     * The map from JDWP refTypeIDs to the corresponding ProxyType instances.
     */
    private HashMap<ReferenceTypeID, ProxyType>  idToType = new HashMap<ReferenceTypeID, ProxyType>(DEFAULT_NUM_REFS);

    private JDWPListener sda;

    /**
     * Adds a class to this database.
     *
     * @param typeID         the JDWP identifier of the class
     * @param name           the class name or its JNI signature
     * @param isJNISignature specifies if <code>name</code> is a JNI signature
     * @return the ProxyType representing the class or null if the class
     *         is an {@link Klass#isInternalType internal type}
     */
    public ProxyType addClass(ReferenceTypeID typeID, String name, boolean isJNISignature) {
        Klass klass = getClass(name, isJNISignature);
        if (klass.isInternalType()) {
            return null;
        }
        ProxyType type = classToType.get(klass);
        if (type != null) {
            Assert.that(type.getID().equals(typeID));
            return type;
        }
        return createProxyType(klass, typeID);
    }

    /**
     * Gets a ProxyType instance corresponding to a given class.
     *
     * @param klass     a class
     * @param resolve   if true, resolve the type if it is not known. If false and
     *                  the type is not known, throw an SDWPException.
     * @return the ProxyType instance corresponding to <code>klass</code> or null if there isn't one
     * @throws SDWPException if the resolution fails
     */
    public ProxyType lookup(Klass klass, boolean resolve) throws SDWPException {
        ProxyType type = classToType.get(klass);
        if (type == null) {
            if (!resolve || sda == null) {
                throw new SDWPException(JDWP.Error_INVALID_CLASS, klass.getName());
            }
            try {
                CommandPacket sdaCommand = new CommandPacket(JDWP.VirtualMachine_COMMAND_SET, JDWP.VirtualMachine_ClassesBySignature_COMMAND, true);
                sdaCommand.getOutputStream().writeString(klass.getSignature(), "signature");
                ReplyPacket sdaReply = sda.sendCommand(sdaCommand);
                PacketInputStream sdaIn = sdaReply.getInputStream();

                int classes = sdaIn.readInt("classes");
                if (classes < 1) {
                    throw new SDWPException(JDWP.Error_INVALID_CLASS, "class not found for signature: " + klass.getSignature());
                }

                byte tag = sdaIn.readByte("refTypeTag");
                ReferenceTypeID typeID = sdaIn.readReferenceTypeID("typeID");
                int status = sdaIn.readInt("status");

                if (Log.debug()) {
                    Log.log("resolved signature " + klass.getSignature() + " to typeID " + typeID);
                }
                type = createProxyType(klass, typeID);
            } catch (IOException e) {
                throw new SDWPException(JDWP.Error_INVALID_CLASS, e.toString());
            }
        }
        return type;
    }

    /**
     * Resolves a JDWP type identifier to a ProxyType instance.
     *
     * @param typeID    a JDWP type identifier
     * @param resolve   if true, resolve the type if it is not known. If false and
     *                  the type is not known, throw an SDWPException.
     * @return the ProxyType corresponding to <code>typeID</code>
     * @throws SDWPException   if the resolution fails
     */
    public ProxyType lookup(ReferenceTypeID typeID, boolean resolve) throws SDWPException {
        ProxyType type = idToType.get(typeID);
        if (type == null) {
            if (!resolve || sda == null) {
                throw new SDWPException(JDWP.Error_INVALID_CLASS, typeID.toString());
            }
            try {
                CommandPacket sdaCommand = new CommandPacket(JDWP.ReferenceType_COMMAND_SET, JDWP.ReferenceType_Signature_COMMAND, true);
                sdaCommand.getOutputStream().writeReferenceTypeID(typeID, "typeID");
                PacketInputStream sdaIn = sda.sendCommand(sdaCommand).getInputStream();
                String sig = sdaIn.readString("signature");

                if (Log.debug()) {
                    Log.log("resolved typeID " + typeID + " to signature " + sig);
                }
                Klass klass = getClass(sig, true);
                type = createProxyType(klass, typeID);
            } catch (IOException e) {
                throw new SDWPException(JDWP.Error_INVALID_CLASS, e.toString());
            }
        }
        return type;
    }

    /**
     * Get a class based on a given name. If a classfile for the given class is found
     * and successfully loaded, the {@link Klass#getState state} of the returned Klass
     * instance is {@link Klass#STATE_LOADED} or {@link Klass#STATE_CONVERTED}.
     * Otherwise, the state will be {@link Klass#STATE_DEFINED} (classfile not found)
     * or {@link Klass#STATE_ERROR} (error while loading or converting the classfile).
     *
     * @param name  the class name or its JNI signature
     * @param isJNISignature specifies if <code>name</code> is a JNI signature
     * @return the Klass object corresponding to <code>name</code>
     */
    private Klass getClass(String signature, boolean isJNISignature) {
        Klass klass = Klass.getClass(signature, isJNISignature);
        Isolate isolate = VM.getCurrentIsolate();
        TranslatorInterface translator = isolate.getTranslator();
        Assert.that(translator != null, "Couldn't get translator");
        try {
            translator.load(klass); // will only load if not loaded
        } catch (java.lang.NoClassDefFoundError e) {
            System.err.println("Error: could not find classfile for " + klass.getName() + " - most likely caused by incorrect '-cp' argument");
        } catch (LinkageError e) {
            System.err.println("Error: could not resolve class " + klass.getName());
            e.printStackTrace();
        }
        return klass;
    }

    /**
     * Ensures that the class is converted. Used to make sure linenumber tables are set.
     *
     * @param klass  the klass to convert
     */
    static synchronized void convertClass(Klass klass) {
        Isolate isolate = VM.getCurrentIsolate();
        TranslatorInterface translator = isolate.getTranslator();
        Assert.that(translator != null, "Couldn't get translator");
        try {
            translator.convert(klass);
        } catch (LinkageError e) {
            System.err.println("warning: could not convert class " + klass.getName());
            e.printStackTrace();
        }
    }

    /**
     * Creates a ProxyType for a class.
     *
     * @param klass    the class
     * @param id       the JDWP identifier for the class
     * @return the created ProxyType
     */
    private ProxyType createProxyType(Klass klass, ReferenceTypeID id) {
        ProxyType type;
        if (klass.getState() == Klass.STATE_LOADED || klass.getState() == Klass.STATE_CONVERTED) {
            type = new ProxyType(klass, id, this);
        } else {
            type = new UndefinedProxyType(klass, id, this);
        }

        Object previous = classToType.put(klass, type);
        Assert.that(previous == null);
        previous = idToType.put(id, type);
        Assert.that(previous == null);

        return type;
    }

    /**
     * Gets the types in this manager.
     *
     * @return  an unmodifiable collection view of the types
     */
    public Collection getTypes() {
        return Collections.unmodifiableCollection(idToType.values());
    }

    /**
     * Sets the connection used to talk to the VM.
     *
     * @param vm  the connection to the VM
     */
    public void setVM(JDWPListener vm) {
        this.sda = vm;
    }
    
    /**
     * Is this a class we should tell debugger about?
     *
     * @param klass the klass
     * @return true if debugger should know about this class.
     */
    public static boolean isDebuggableKlass(Klass klass) {
        return !klass.isInternalType() && !klass.isSquawkPrimitive();
    }
}
