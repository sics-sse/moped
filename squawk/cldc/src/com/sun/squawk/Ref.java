/*
 * Copyright 2003-2008 Sun Microsystems, Inc. All Rights Reserved.
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

import java.lang.ref.*;

import com.sun.squawk.util.*;


/**
 * Abstract base class for reference objects.  This class defines the
 * operations common to all reference objects. Because reference objects are
 * implemented in close cooperation with the garbage collector, this class may
 * not be subclassed directly.
 *
 * @version  12/19/01 (CLDC 1.1)
 * @since    JDK1.2, CLDC 1.1
 */
public class Ref {

    /**
     * The next reference in the list.
     */
    Ref next;

    /**
     * The (weak) reference to an object
     */
    Address referent;

    /* -- Referent accessor and setters -- */

    /**
     * Returns this reference object's referent.  If this reference object has
     * been cleared, either by the program or by the garbage collector, then
     * this method returns <code>null</code>.
     *
     * @return   The object to which this reference refers, or
     *           <code>null</code> if this reference object has been cleared
     */
    public final Object get() {
        return referent.toObject();
    }

    /**
     * Clears this reference object.
     */
    public final void clear() {
        this.referent = Address.zero();
    }

    /* -- Constructors -- */

    public Ref(Object referent) {

        if (referent == null) {
            clear();
            return;
        }

        this.referent = Address.fromObject(referent);

        // Add to the global list this weak reference
        GC.getCollector().addWeakReference(this);
    }

    public String toString() {
        return "referrent: " + referent.toUWord().toPrimitive();
    }

/*if[DEBUG_CODE_ENABLED]*/
/*if[FINALIZATION]*/
    static boolean objectFinalized;
    static boolean weakReferenceFinalized;
/*end[FINALIZATION]*/

    public static void main(String[] args) {

        String C = new String("C");
        String F = new String("F");

        WeakReference refA = new WeakReference(new String("A")); // collectable RAM object
        WeakReference refB = new WeakReference("B");             // permanent ROM object
        WeakReference refC = new WeakReference(C);               // non-collectable RAM object
        WeakReference refD = new WeakReference(new String("D")); // collectable RAM object
        WeakReference refE = new WeakReference("E");             // permanent ROM object
        WeakReference refF = new WeakReference(F);               // non-collectable RAM object

        System.gc();
        VMThread.yield();

        Assert.that(refA.get() == null);
        Assert.that(refB.get() != null);
        Assert.that(refC.get() != null);
        Assert.that(refD.get() == null);
        Assert.that(refE.get() != null);
        Assert.that(refF.get() != null);


/*if[FINALIZATION]*/
        // Check that finalizer's and weak references work together
        ObjectWithFinalizerTest();
        WeakReferenceWithFinalizerTest();
/*end[FINALIZATION]*/

        // When calling WeakReferenceWithFinalizerTest() the activation record contains a pointer
        // to the WeakReferenceWithFinalizer, thus, we must return from the method before
        // verifying that the finalizer has been called.
        System.gc();
        VMThread.yield();
/*if[FINALIZATION]*/
        Assert.that(weakReferenceFinalized);
/*end[FINALIZATION]*/

    }

/*if[FINALIZATION]*/
    /**
     * Used to test that when an object has a finalizer and is also wrapped by
     * a weak reference that its finalizer is called ad that the weak reference
     * is also released.
     */
    private static void ObjectWithFinalizerTest() {

        class ObjectWithFinalizer {
            protected void finalize() {
                Ref.objectFinalized = true;
            }
        }

        WeakReference refG = new WeakReference(new ObjectWithFinalizer());   // object with finalizer

        // Run the finalizer
        System.gc();
        VMThread.yield();

        // The finalizer will only be added to the queue after the previous gc. Therefore this object should
        // still be live.
        Assert.that(refG.get() != null);
        Assert.that(objectFinalized);

        System.gc();
        VMThread.yield();
        Assert.that(refG.get() == null);
    }



    /**
     * Used to test that when the WeakReference class is extended to include
     * a finalizer, that the finalizer is called and hence the weak reference
     * can be released.
     */
    private static void WeakReferenceWithFinalizerTest() {

        //Class that extends weak reference to test that finalize() will work.
        class WeakReferenceWithFinalizer extends Ref {
           WeakReferenceWithFinalizer(Object ref) {
               super(ref);
           }

           protected void finalize() {
               Ref.weakReferenceFinalized = true;
           }
       }
        new WeakReferenceWithFinalizer(new String("A")); // collectable RAM object
    }
/*end[FINALIZATION]*/
/*end[DEBUG_CODE_ENABLED]*/
}
