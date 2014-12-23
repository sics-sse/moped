/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.translator;

import com.sun.squawk.Klass;
import com.sun.squawk.Method;
import com.sun.squawk.util.ArrayHashtable;
import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.Comparer;
import com.sun.squawk.util.Tracer;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The ObjectTable keeps track of constant objects referenced by a method or a class.
 * These are reference counted.
 *
 * Each method (<code>Code</code> object) keeps an ObjectTable of objects used by the method.
 * After (optional) dead-method elimination, the used method's ObjectTables are merged into a ClassFile's
 * ObjectTable, sorted by static reference count.
 */
public class ObjectTable {

   /**
     * Hashtable of constant objects.
     */
    private ArrayHashtable objectTable = new ArrayHashtable();

    /**
     * Index to the next available object table entry.
     */
    private int nextIndex;

    private final Klass definedClass;

    /**
     * If true, try to do dead string elimination on this class
     */
    private boolean safeToDoDeadStringElim;

    /**
     * If true, try to do dead class elimination
     */
    private static boolean safeToDoDeadMethodElim;

    private static final Hashtable vm2cClasses;

    static {
        // @todo: FIX VM2C so it doesn't depend on constant table for strings.
        // We shouldn't do dead string elimination on vm2c classes as long as vm2c relies on
        // the constant table for string constants.
        vm2cClasses = new Hashtable();
        vm2cClasses.put("com.sun.squawk.VM",                            "VM2C Class");
        vm2cClasses.put("com.sun.squawk.GarbageCollector",              "VM2C Class");
/*if[GC2C]*/
/*if[GC_com.sun.squawk.Lisp2GenerationalCollector]*/
        vm2cClasses.put("com.sun.squawk.Lisp2GenerationalCollector",    "VM2C Class");
        vm2cClasses.put("com.sun.squawk.Lisp2GenerationalCollector$MarkingStack", "VM2C Class");
/*end[GC_com.sun.squawk.Lisp2GenerationalCollector]*/
/*if[GC_com.sun.squawk.Lisp2Collector]*/
        vm2cClasses.put("com.sun.squawk.Lisp2Collector",                "VM2C Class");
        vm2cClasses.put("com.sun.squawk.Lisp2Collector$MarkingStack",   "VM2C Class");
/*end[GC_com.sun.squawk.Lisp2Collector]*/
/*end[GC2C]*/
        safeToDoDeadMethodElim = Arg.get(Arg.DEAD_METHOD_ELIMINATION).getBool();
    }

    ObjectTable(Klass definedClass) {
        this.definedClass = definedClass;
        if (vm2cClasses.get(definedClass.getInternalName()) == null) {
            safeToDoDeadStringElim = Arg.get(Arg.DEAD_STRING_ELIMINATION).getBool();
        }
    }

    /**
     * Add an object to the object table.
     *
     * @param object the object to add
     */
    public void addConstantObject(Object object) {
        ObjectCounter counter = (ObjectCounter)objectTable.get(object);
        if (counter == null) {
            counter = new ObjectCounter(object, nextIndex++);
            objectTable.put(object, counter);
        } else {
            counter.inc();
        }
    }

    /**
     * Merge the object counter information into this object table
     * @param otherOC
     */
    void mergeConstantObject(ObjectCounter otherOC) {
        Object object = otherOC.getObject();
        ObjectCounter counter = (ObjectCounter)objectTable.get(object);
        if (counter == null) {
            counter = new ObjectCounter(object, nextIndex++);
            counter.setCount(otherOC.getCounter());
            objectTable.put(object, counter);
        } else {
            counter.setCount(counter.getCounter() + otherOC.getCounter());
        }
    }

    private static int[] INT_ARRAY_DUMMY = new int[0];
    private static short[] SHORT_ARRAY_DUMMY = new short[0];
    private static byte[] BYTE_ARRAY_DUMMY = new byte[0];

    public static int compareIgnoringCount(Object o1, Object o2) {
        // Now do ordering based on class
        Class class1 = o1.getClass();
        Class class2 = o2.getClass();
        if (class1 != class2) {
            return class1.getName().compareTo(class2.getName());
        }

        // Now order based on value
        int diff;
        if (class1 == Klass.class) {
            return ((Klass) o1).getName().compareTo(((Klass) o2).getName());
        } else if (class1 == String.class) {
            return ((String) o1).compareTo((String) o2);
        } else if (class1 == INT_ARRAY_DUMMY.getClass()) {
            int[] arr1 = (int[]) o1;
            int[] arr2 = (int[]) o2;
            if ((diff = arr1.length - arr2.length) != 0) {
                return diff;
            }
            for (int i = 0; i < arr1.length; ++i) {
                if ((diff = arr1[i] - arr2[i]) != 0) {
                    return diff;
                }
            }
        } else if (class1 == SHORT_ARRAY_DUMMY.getClass()) {
            short[] arr1 = (short[]) o1;
            short[] arr2 = (short[]) o2;
            if ((diff = arr1.length - arr2.length) != 0) {
                return diff;
            }
            for (int i = 0; i < arr1.length; ++i) {
                if ((diff = arr1[i] - arr2[i]) != 0) {
                    return diff;
                }
            }
        } else if (class1 == BYTE_ARRAY_DUMMY.getClass()) {
            byte[] arr1 = (byte[]) o1;
            byte[] arr2 = (byte[]) o2;
            if ((diff = arr1.length - arr2.length) != 0) {
                return diff;
            }
            for (int i = 0; i < arr1.length; ++i) {
                if ((diff = arr1[i] - arr2[i]) != 0) {
                    return diff;
                }
            }
        } else {
            // Need to add another 'else' clause if this ever occurs
            throw Assert.shouldNotReachHere("unknown object table type: " + class1);
        }
        return 0;
    }
    
    /**
     * Sorts the object table according to the access count. Elements with the same
     * access count are sorted by class name and then by value. This guarantees a
     * deterministic sort order for object tables in the bootstrap suite.
     *
     * This method actually just sets all of the ObjectCounter's offset field to
     * be in sorted order. The array is created and sorted later.
     */
    public void sortObjectTable() {
        ObjectCounter[] list = new ObjectCounter[objectTable.size()];
        Enumeration e = objectTable.elements();
        for (int i = 0 ; i < list.length ; i++) {
            list[i] = (ObjectCounter)e.nextElement();
        }

        if (list.length > 1) {
            if (list.length <= 16) {
                // sorting alphabetically:
                Arrays.sort(list, new Comparer() {
                    public int compare(Object o1, Object o2) {
                        ObjectCounter t1 = (ObjectCounter) o1;
                        ObjectCounter t2 = (ObjectCounter) o2;
                        return compareIgnoringCount(t1.getObject(), t2.getObject());
                    }
                });
            } else {
                // large object tables are sorted so the most comonly used objects occur earlier,
                // so a smaller bytecode can be used.
                Arrays.sort(list, new Comparer() {
                    public int compare(Object o1, Object o2) {
                        ObjectCounter t1 = (ObjectCounter) o1;
                        ObjectCounter t2 = (ObjectCounter) o2;
                        if (t1.getCounter() < t2.getCounter()) {
                            return 1;
                        } else if (t1.getCounter() > t2.getCounter()) {
                            return -1;
                        } else {
                            return compareIgnoringCount(t1.getObject(), t2.getObject());
                        }
                    }
                });
            }
        }
//System.err.println("object table for " + definedClass.getInternalName());
        for (int i = 0 ; i < list.length ; i++) {
            ObjectCounter oc = list[i];
//System.err.println("  " + i + "\t" + oc.getCounter() + "\t" + oc.getClass() + "\t" + oc.getObject());
            oc.setIndex(i);
        }
    }

    /**
     * Get the index of an object in the object table.
     *
     * @param object the object to index
     * @param recordUse if true, count this as an "emitted use"
     * @return the index
     * @throws java.util.NoSuchElementException if the object table does not contain <code>object</code>
     */
    public int getConstantObjectIndex(Object object, boolean recordUse) {
        ObjectCounter counter = (ObjectCounter)objectTable.get(object);
        if (counter == null) {
            throw new java.util.NoSuchElementException();
        }
        return counter.getIndex();
    }

    /**
     * Merge the objects and usage counts from methodObjectTable into this object table
     * @param methodObjectTable
     * @param stringsOnly if true only merge in string objects (for to preserve strings used by vm2c)
     */
    private void mergeObjectTable(ObjectTable methodObjectTable, boolean stringsOnly) {
        Enumeration e = methodObjectTable.objectTable.elements();
        while (e.hasMoreElements()) {
            ObjectCounter moc = (ObjectCounter)e.nextElement();
            if (!stringsOnly || (moc.getObject() instanceof String)) {
               mergeConstantObject(moc);
            }
        }
    }

    void mergeMethodsObjectTable(Translator translator, Code[] methodsCode, boolean isStatic) {
        for (int i = 0; i < methodsCode.length; i++) {
            Method method = definedClass.getMethod(i, isStatic);
            Code code = methodsCode[i];
            if (!method.isHosted() && !method.isAbstract() && !method.isNative()) {
                Assert.that(code != null);
                boolean unusedMethod = safeToDoDeadMethodElim && !translator.dme.isMarkedUsed(method);
                if (unusedMethod) {
                    if (safeToDoDeadStringElim) {
                        if (Translator.TRACING_ENABLED && Tracer.isTracing("converting", method.toString())) {
                            Tracer.traceln("Ignoring objects used by unused method " + method);
                        }
                    } else {
                        mergeObjectTable(code.getObjectTable(), true);
                    }
                } else {
                    mergeObjectTable(code.getObjectTable(), false);
                }
            }
        }
    }

    
    /**
     * Gets the object table as an array of objects that have been sorted by frequency of access.
     *
     * @return the sorted object array
     */
    Object[] getConstantObjectArray() {
        Object[] list = new Object[objectTable.size()];
        Enumeration e = objectTable.elements();
        for (int i = 0 ; i < list.length ; i++) {
            list[i] = e.nextElement();
        }
        Arrays.sort(list, new Comparer() {
            public int compare(Object o1, Object o2) {
                if (o1 == o2) {
                    return 0;
                }
                ObjectCounter t1 = (ObjectCounter)o1;
                ObjectCounter t2 = (ObjectCounter)o2;
                if (t1.getIndex() < t2.getIndex()) {
                    return -1;
                } else if (t1.getIndex() > t2.getIndex()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (int i = 0 ; i < list.length ; i++) {
            ObjectCounter oc = ((ObjectCounter) list[i]);
            list[i] = oc.getObject();
        }
        return list;
    }

}

/**
 * Class used to keep track of the number of times a constant object is referenced in a class.
 */
final class ObjectCounter {

    /**
     * The object being counted.
     */
    private Object object;

    /**
     * The index of the object in the object table.
     */
    private int index;

    /**
     * Use counter.
     */
    private int counter;

    /**
     * Constructor.
     *
     * @param index the initial index
     */
    ObjectCounter(Object object, int index) {
        this.object = object;
        this.index  = index;
		this.counter = 1;
    }

    /**
     * Get the object being counted.
     *
     * @return the object
     */
    public Object getObject() {
        return object;
    }

    /**
     * Get the index.
     *
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the index.
     *
     * @param index the index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Add 1 to the counter.
     */
    public void inc() {
        counter++;
    }

    /**
     * Get the counter value.
     *
     * @return the value
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Gets a String representation.
     *
     * @return the string
     */
    public final String toString() {
        return "index = "+index+" counter = "+counter+" object = "+object;
    }

    void setCount(int counter) {
        this.counter = counter;
    }
}
