/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.translator;

import com.sun.squawk.Klass;
import com.sun.squawk.Suite;
import com.sun.squawk.Method;
import com.sun.squawk.Modifier;
import com.sun.squawk.VM;
import com.sun.squawk.util.*;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Database of method definitions, calls, and overrides.
 *
 * 
 *
 */
public class MethodDB {
    
    private Translator translator;
    
        
    private final static int DEFAULT_TABLE_SIZE = 800;
    
    /**
     * table of {String -> Entry}
     */
    private Hashtable canonicalMethodEntryTable;
    
    /**
     * Set of all recorded method (entries)
     */
    private Hashtable allMethods;
        
    /** Creates a new instance of MethodDB */
    public MethodDB(Translator translator) {
        this.translator = translator;
        if (translator.getTranslationStrategy() >= Translator.BY_SUITE) {
            canonicalMethodEntryTable = new Hashtable(DEFAULT_TABLE_SIZE);
            allMethods = new Hashtable(DEFAULT_TABLE_SIZE);
        }
    }
    
    /**
     * Defines, calls, and overrides info for a particular method.
     */ 
    public static final class Entry {
        public final Method m;
        
        private SquawkVector calls;
        
        /** Vector of all of the methods that override this method.
         */
        private SquawkVector overrides;
        
        /** 
         * This method is an implementation of all of these super methods (usually zero or one)
         */
        private SquawkVector superMethods;
        
        /** 
         * The name that the method is looked up by:
         *     == Klass.toString(m, false)
         */
        private String fullName;
        
        /**
         * The estimated size of this method.
         */
        private int estSize;
        
        private Entry(Method m, String fullName) {
            this.m = m;
            this.fullName = fullName;
        }
        
        public void addCall(Entry calledMethod) {
            if (calls == null) {
                calls = new SquawkVector();
            }
            calls.addElement(calledMethod);
        }
        
        public SquawkVector getCalls() {
            return calls;
        }
        
        private void addOverride(Entry overridingMethod) {
            if (overrides == null) {
                overrides = new SquawkVector();
            }
            overrides.addElement(overridingMethod);
        }
        
        public void addSuperMethod(Entry superMethod) {
            if (superMethods == null) {
                superMethods = new SquawkVector();
            }
            if (!superMethods.contains(superMethod)) {
                superMethods.addElement(superMethod);
                superMethod.addOverride(this);
            }
        }
 
        /**
         * Return vector of Entries that this method directly overrides.
         * This can include one abstract method declaration or method definition from a super class,
         * plus one or more method declarations from implemented interfaces.
         */
        public SquawkVector getSuperMethods() {
            return superMethods;
        }
        
        /**
         * Return vector of Entries for methods that directly override this method.
         */
        public SquawkVector getOverrides() {
            return overrides;
        }
        
        void setSize(int estSize) {
            this.estSize = estSize;
        }
        
        /**
         * Return estimate of the number of IR nodes in this method.
         */
        public int getSize() {
            return estSize;
        }
        
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Entry) {
                Entry w = (Entry)obj;
                return methodsEqual(this.m, w.m);
            }
            return false;
        }
       
        public int hashCode() {
            return fullName.hashCode();
        }
        
        public String toString() {
            return fullName;
        }
        
    } /* Entry */
    
    /**
     * Gets the canonical Entry for the method m.
     * 
     * @param m the method to look for
     * @return the canonical Entry for method m.
     */
    public MethodDB.Entry lookupMethodEntry(Method m) {
        if (canonicalMethodEntryTable == null) {
            throw new IllegalStateException("MethodDB not used with this translation strategy");
        }
        
        String key = Klass.toString(m, false);
        Entry value = (Entry)canonicalMethodEntryTable.get(key);
        if (value == null) {
            value = new Entry(m, key);
            canonicalMethodEntryTable.put(key, value);
        }
        
        return value;
    }
    
    /**
     * Record the definition of a method.
     * Update the allMethods table and record that this method might override some other method.
     *
     * @param m the method to record.
     * @param estSize the estimated size of the method in # IR instructions. May pass zero if not tracing.
     */
    public void recordMethod(Method m, int estSize) {
        if (canonicalMethodEntryTable != null) {
            MethodDB.Entry mw = lookupMethodEntry(m);
            allMethods.put(mw, mw);
            mw.setSize(estSize);
            if (m.isStatic()) {
                return;
            }
            
            calcSuperMethods(mw);
        }
    }
     
     /**
      * Update the calls table for caller.
      *
      * @param caller 
      * @param callee the method to record.
      */
     public void recordMethodCall(MethodDB.Entry caller, Method callee) {
        if (!callee.isHosted() && !callee.isNative()) {
            MethodDB.Entry cw = lookupMethodEntry(callee);
            caller.addCall(cw);
        }
     }
     
    /**
     * A subclass can cause the methods of a superclass to become implementors
     * of interface methods, so even though the subclass may not define 
     * new methods, it must update the inherited methods as possibly callable
     * via the interface class.
     * 
     * @param mw MMethodDB.Entry of an inherited method.
     * @param interfaces array of interfaces that the subclass implements
     */
    private void calcImplementsMerge(MethodDB.Entry mw, Klass[] interfaces) {
        for (int i = 0; i < interfaces.length; i++) {
            Method superMethod = interfaces[i].lookupMethod(mw.m.getName(),
                                               mw.m.getParameterTypes(),
                                               mw.m.getReturnType(),
                                               null, // all interface methods are public, so no accessible check
                                               false);
            if (superMethod != null) {
                mw.addSuperMethod(lookupMethodEntry(superMethod));
            }
        }
    }
    
    /**
     * Calculate the methods that <code>m</code> overrides. This includes an override of
     * a super class' method, plus implementing one or more interface's method declarations.
     *
     * @param mw the Entry of the method to check
     */
    private void calcSuperMethods(MethodDB.Entry mw) {
        Assert.that(!mw.m.isStatic());
        Klass defClass = mw.m.getDefiningClass();
        Klass superType = defClass.getSuperclass();
        if (superType == null) {
            return;
        }
        
        Method superMethod = superType.lookupMethod(mw.m.getName(),
                                               mw.m.getParameterTypes(),
                                               mw.m.getReturnType(),
                                               defClass,
                                               false);
        if (superMethod != null) {
            mw.addSuperMethod(lookupMethodEntry(superMethod));
        }
        
        calcImplementsMerge(mw, defClass.getInterfaces());
    }
    
    /**
     * Given a klass, check to so if it inherits any methods form a superclass that are valid implementations of one of it's interface's methods,
     * If so, record the overrides/supermethod information.
     *
     * @param klass the klass to check.
     */
    public void computeInheritedImplementorsInfo(Klass klass) {
        if (klass == Klass.OBJECT || klass.isArray() || klass.isInterface() || klass.isSynthetic()) {
            return;
        }
        
        Klass superKlass = klass.getSuperclass();
        while (superKlass != Klass.OBJECT) {
            int count = superKlass.getMethodCount(false);
            for (int i = 0; i < count; i++) {
                Method m = superKlass.getMethod(i, false);
                MethodDB.Entry mw = lookupMethodEntry(m);
                // inherited method. check to see if callable via an interface implemented by this klass:
                calcImplementsMerge(mw, klass.getInterfaces());
            }
            superKlass = superKlass.getSuperclass();
        }
    }
    
    /**
     * Get Enumeration of all MethodDB.Entry objects.
     */
    public Enumeration getAllMethods() {
        return allMethods.elements();
    }
    
      /** 
     * This method may be an override of a method in a superclass, and/or an 
     * implementation of one or more interface methods.
     * Return true if any of those super definitions are defined outside of this suite.
     */
    private boolean isSuperMethodDefinedOustideOfSuite(MethodDB.Entry mw) {
        SquawkVector superMethods = mw.getSuperMethods();
        if (superMethods == null) {
            return false;
        }
        
        for (int i = 0; i < superMethods.size(); i++) {
            MethodDB.Entry superMethod = (MethodDB.Entry)superMethods.elementAt(i);
            if (!translator.getSuite().contains(superMethod.m.getDefiningClass())) {
                return true;
            } else if (isSuperMethodDefinedOustideOfSuite(superMethod)) {
                return true;
            }
        }
        return false;
    }
    
    /*---------------------------------------------------------------------------*\
     *                                DB Querries                                *
    \*---------------------------------------------------------------------------*/
    
    /**
     * Given the method's access, the defining class' access, and the suite type,
     * determine the final accessibility of the method outside of this suite.
     * 
     * Note that we are talking about the accessibility of a particular method, 
     * not all of the methods that override a super method. There are cases where
     * a super method is not accessible, but an override is. A latter check for overriding
     * will mark the super method as used.
     * 
     * @param mw the MMethodDB.Entry
     * @return true if an external suite could possibly access this method.
     */
    public boolean isExternallyVisible(MethodDB.Entry mw) {
        Method m = mw.m;
        int modifiers = m.getModifiers();
        int suiteType = translator.getSuiteType();
        boolean sealedPackages = (suiteType == Suite.LIBRARY) || (suiteType == Suite.APPLICATION);

        if (Modifier.isPrivate(modifiers)) {
            return false;
        } else if (translator.getSuite().isBootstrap() && VM.isInternal(m)) {
            // if the symbol is stripped, and it wasn't marked as "CrossSuitePrivate" in the library.proprties file,
            // then there is no way that this is externally visible.
            return false;
        } else {
            // It's declared externally visible, but is it really?
            Assert.that(Modifier.isPackagePrivate(modifiers) || Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers));
            
            // Check visibility upwards through parent suites:
            if (!m.isStatic()) {
                // This could be an override of a method in another suite.
                // If so, it might be called and we can't tell, so return true
                if (isSuperMethodDefinedOustideOfSuite(mw)) {
                    return true;
                }
            }
            
            // Check visibility downwards through child suites. Look for cases where a child suite
            // could call this protected or public method. Or conversley,
            // cases when there is no way a child suite could call this method.
            switch (suiteType) {
                case Suite.APPLICATION:
                    // no possible child suite:
                    return false;
                    
                case Suite.LIBRARY: {
                    // The complicated case:
                    
                    // check the methods's modifier
                    if (Modifier.isPackagePrivate(modifiers)) {
                        // child suites can't call this package-private method
                        return false;
                    } else if (Modifier.isProtected(modifiers)) {
                         if (m.getDefiningClass().isFinal() // there can be no subclass, so has same effect as package-private:
                             || !m.getDefiningClass().isPublic()) { // there can be no external subclass, so has same effect as package-private:
                            return false;
                        }
                        return true;
                    } else /* Modifier.isPublic(modifiers) */ {
                        Assert.that(Modifier.isPublic(modifiers));
                        if (!m.getDefiningClass().isPublic()) {
                            return false;
                        }
                        return true;
                    }
                }
                
                default:
                    // extendable and debuggable suites leave all symbols externally visible.
                    return true;
            }
        }
    }
    
     /**
      * Return true if <code>m</code> we can determine that there are no possibility
      * of overrides of this method.
      *
      * If we are building an application or library, we can make some closed-world assumptions and 
      * determine what is overriden. With other suite types we can't figure this out.
      *
      * @param m the method to check.
      * @return true if the method can't possibly be overriden
      */
     public boolean isNeverOverriden(Method m) {
         Assert.that(!m.isStatic());        // caller should check
         Assert.that(!m.isFinal());         // caller should check
         Assert.that(!m.isPrivate());       // caller should check
         Assert.that(!m.getDefiningClass().isFinal());     // caller should check
         
         // if the class or the method is package private, then there are no overrides 
         // of "m" beyond those in the methodOverrides table.
         
         /* Here are the access cases (note that private is handled by caller).
          *
          *  ACCESS            | APPLICATION | LIBRARY     | EXTENDABLE LIBRARY or DEBUG
          *-----------------------------------------------------------------
          *  packageprivate**  | !overridden | !overridden | false 
          *  protected         | !overridden | false       | false 
          *  public            | !overridden | false       | false 
          *
          * ** method is packageprivate or class is !public
          */
         MethodDB.Entry mw = lookupMethodEntry(m);
         if (!isExternallyVisible(mw)) {
            SquawkVector overrides = mw.getOverrides();
            if (overrides == null || overrides.size() == 0) {
                return true;
            }
         }
         
         return false;
    }
  
   /**
     * Are two methods equal? (Where does other method come from???)
     *
     * @return true if both methods are defined in the same class, with the same name and descriptor.
     */
    public static boolean methodsEqual(Method m1, Method m2) {
        if (m1 == m2) {
            return true;
        }
        if (m1.isStatic() == m2.isStatic() &&
            m1.getDefiningClass() == m2.getDefiningClass() &&
            m1.getName().equals(m2.getName()) &&
            m1.getReturnType() == m2.getReturnType()) {
            
            Klass types1[] = m1.getParameterTypes();
            Klass types2[] = m2.getParameterTypes();
            
            if (types1.length == types2.length) {
                for (int j = 0; j < types1.length; j++) {
                    if (types1[j] != (types2[j])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
}
