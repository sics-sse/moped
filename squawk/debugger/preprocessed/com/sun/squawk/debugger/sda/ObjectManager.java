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

package com.sun.squawk.debugger.sda;

import java.io.*;

import com.sun.squawk.*;
import com.sun.squawk.DebuggerSupport;
import com.sun.squawk.debugger.*;
import com.sun.squawk.debugger.DataType.*;
import com.sun.squawk.util.*;

/**
 * This class manages mapping objects in the VM to JDWP <code>objectID</code>s.
 *
 * This includes the JDWP types: objectID, tagged-objectID, threadID, threadGroupID,
 * stringID, classObjectID, arrayID.
 *
 * It does NOT include referenceTypeID (and subtypes classID, arrayTypeID, interfaceID),
 * methodID, or fieldID.
 *
 */
public class ObjectManager {

    /**
     * The table mapping objects that may move or be collected to identifiers and vice-versa.
     * Objects in this table may be garbage collected at any time.
     */
    private final WeakIntHashtable objects;

    /**
     * The ID allocator for objects.
     */
    int objectIDCounter = 1;

    public ObjectManager() {
        objects = new WeakIntHashtable();
    }

    /**
     * Gets a JDWP objectID for an object.
     *
     * @param object  the object to get an identifier for
     * @return an identifier for <code>object</code>
     */
    public ObjectID getIDForObject(Object object) {
        return new ObjectID(getIDForObject0(object));
    }

    private synchronized int getIDForObject0(Object object) {
        if (object == null) {
            return 0;
        }

        int romObjectID = DebuggerSupport.getIDForROMObject(object);
        if (romObjectID != 0) {
            Assert.that(romObjectID > 0);
            int objectID = -romObjectID;
            return objectID;
        } else {
            // This could be a performance problem at some point (allocates Integers until object is found).
            // Consider using an IntHashTableVisitor (or searching varient) at some point.
            Integer objectID = objects.getKey(object);

            if (objectID == null) {
                int newObjectID;
                synchronized (this) {
                    newObjectID = objectIDCounter++;
//VM.println("ObjectManager::getIDForObject - size = " + objects.size());
                }
                Object oldVal = objects.put(newObjectID, object);
                Assert.that(oldVal == null);
                return newObjectID;
            }
            return objectID.intValue();
        }
    }

    /**
     * Gets the object corresponding to a given JDWP objectID value.
     *
     * @param objectID  the identifier denoting an object
     * @return the object corresponding to <code>id</code> or null if the object has been garbage collected
     * @throws SDWPException if <code>objectID</code> does not denote a null object but the object it does denote
     *                  has been garbage collected
     */
    public synchronized Object getObjectForID(ObjectID objectID) throws SDWPException {
        int id = objectID.id;
        if (id == 0) {
            return null;
        } else if (id > 0) {
            Object object = objects.get(id);
            if (object == null) {
                throw new SDWPException(JDWP.Error_INVALID_OBJECT, "object ID denotes a non-existent or garbage collected object: " + id);
            }
            return object;
        } else {
            return DebuggerSupport.getROMObjectForID(-id);
        }
    }

    public VMThread getThreadForID(ObjectID objectID) throws SDWPException {
        try {
            return (VMThread)getObjectForID(objectID);
        } catch (ClassCastException e) {
            throw new SDWPException(JDWP.Error_INVALID_THREAD, "object ID does not denote a VMThread instance");
        }
    }

    public String getStringForID(ObjectID objectID) throws SDWPException {
        try {
            return (String)getObjectForID(objectID);
        } catch (ClassCastException e) {
            throw new SDWPException(JDWP.Error_INVALID_STRING, "object ID does not denote a String instance");
        }
    }

    public Klass getClassForID(ObjectID objectID) throws SDWPException {
        try {
            return (Klass)getObjectForID(objectID);
        } catch (ClassCastException e) {
            throw new SDWPException(JDWP.Error_INVALID_CLASS, "object ID does not denote a Klass instance");
        }
    }

    /**
     * Writes the JDWP tag and object ID of an object to a given packet stream.
     *
     * @param out     the stream to write to
     * @param object  the object to write
     * @param s       prefix to use if this write is logged. A value of null prevents logging altogether.
     */
    public void writeTaggedObject(PacketOutputStream out, Object object, String s) throws IOException {
        int tag = JDWP.Tag_OBJECT;
        ObjectID objectID = getIDForObject(object);

        if (object != null) {
            Klass klass = GC.getKlass(object);
            tag = JDWP.getTag(klass);
        }

        out.writeTaggedObjectID(new TaggedObjectID(tag, objectID.id), s);
    }
}
