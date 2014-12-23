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

package com.sun.squawk;

import com.sun.squawk.pragma.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;
import java.io.PrintStream;

/**
 * The Klass class represents the types in the Squawk VM.
 *
 * The Squawk system uses a type hierarchy to simplify verification. The diagram
 * below shows this hierarchy:<p>
 *
 * <img src="doc-files/Klass-1.jpg"><p>
 *
 * The classes in the <b>Java class hierarchy</b> have the expected
 * relationships with each other as specified by the standard Java API.
 * For example, the {@link #getSuperclass()} method returns null for
 *  <code>java.lang.Object</code> as well as for classes representing interfaces
 * primitive types. The same Java API compliance holds for all the other
 * standard API methods.
 *
 * @version 1.0
 * @see     com.sun.squawk.KlassMetadata
 */

public class Klass<T> {

////public class Klass {


    /*---------------------------------------------------------------------------*\
     *      Fields of Klass, some of which may be accessed directly by the VM    *
    \*---------------------------------------------------------------------------*/

    /**
     * The pointer to self. This must be the at the same offset to the class pointer
     * in ObjectAssociation.
     */
    private final Klass self = this;

    /**
     * The virtual method table (or <i>vtable</i>) for this class. Consider the following class definitions:
     * <p><blockquote><pre>
     *     class X {
     *         void a() { ... }
     *         void b() { ... }
     *         void c() { ... }
     *     }
     *
     *     class Y extends X {
     *         void d() { ... }
     *         void e() { ... }
     *     }
     *
     *     class Z extends Y {
     *         void c() { ... } // overrides class X
     *         void f() { ... }
     *     }
     * </pre></blockquote></p>
     * The vtable for these three classes would be as follows:
     * <p><blockquote><pre>
     *           +-----+-----+-----+
     *  class X: | X.a | X.b | X.c |
     *           +-----+-----+-----+
     *
     *           +-----+-----+-----+-----+-----+
     *  class Y: | X.a | X.b | X.c | Y.d | Y.e |
     *           +-----+-----+-----+-----+-----+
     *
     *           +-----+-----+-----+-----+-----+-----+
     *  class Z: | X.a | X.b | Z.c | Y.d | Y.e | Z.f |
     *           +-----+-----+-----+-----+-----+-----+
     * </pre></blockquote></p>
     * The methods are instances of <code>MethodBody</code> until this class is
     * copied into NVM or is initialized.
     */
    private Object[] virtualMethods;

    /**
     * The table of static methods declared by this class.<p>
     *
     * The methods are instances of <code>MethodBody</code> until this class is
     * copied into NVM or is initialized.
     */
    private Object[] staticMethods;

    /**
     * The name of the class in Squawk internal form.
     *
     * @see   #getInternalName()
     */
    private final String name;

    /**
     * The class representing the component type of an array.  If this class
     * does not represent an array class this is null.
     */
    private final Klass componentType;

    /**
     * The super type of this class.
     */
    private Klass superType;

    /**
     * The ordered set of interfaces implemented by this class. If this
     * is a non-interface and non-abstract class, then this array is the
     * closure of all the interfaces implemented by this class (implicitly
     * or explicitly) that are not implemented by any super class.
     */
    private Klass[] interfaces;

    /**
     * The mapping from each interface method to the virtual method that
     * implements it. The mapping for the methods of the interface at index
     * <i>idx</i> in the <code>interfaces</code> array is at index
     * <i>idx</i> in the <code>interfaceVTableMaps</code> array. The
     * mapping is encoded as an array where a value
     * <i>m</i> at index <i>i</i> indicates that the method at
     * index <i>m</i> in the vtable of this class implements the interface
     * method at index <i>i</i> in the virtual methods table of the
     * interface class.
     */
    private short[][] interfaceVTableMaps;

    /**
     * The pool of object constants (including <code>Klass</code> instances)
     * used by the bytecodes of this class.
     */
    private Object[] objects;

    /**
     * The bit map for an instance of this class describing which
     * words of the instance contain pointer values. If this value is null
     * then the map is described by the {@link #oopMapWord}.
     */
    private UWord[] oopMap;

    /**
     * The bit map for an instance of this class describing which
     * words of the instance contain pointer values. This version of the
     * map is used when {@link #getInstanceSize} is {@link HDR#BITS_PER_WORD} or less.
     */
    private UWord oopMapWord;

    /**
     * The data map describes the multi-byte data contained within an instance
     * of this class.  The data map is used to facilitate conversion of data contained
     * within a serialized object form (such as a suite or isolate) such that it is
     * compatible with the host system.  If this value is null then the types are described
     * by the {@link #dataMapWord}.
     */
    private UWord[] dataMap;

    /**
     * This version of the data map is used when the number of multi-byte data entries is
     * {@link HDR#BITS_PER_WORD}/2 or less.
     */
    private UWord dataMapWord;

    /**
     *  The number of dataMap entries.
     */
    private short dataMapLength;

    /**
     * A mask of the constants defined in {@link Modifier}.
     */
    private int modifiers;

    /**
     * The translation state of the class.
     */
    private byte state = STATE_DEFINED;

    /**
     * The identifier for this class. If the value is positive, then it
     * is a system wide unique identifier as well as the index of the class within its suite.
     * Otherwise, it is the negation of the index of the class within its suite plus 1.
     */
    private final short id;

    /**
     * The size (in bytes) of an instance of this class.
     */
    private short instanceSizeBytes;

    /**
     * The size (in words) of the static fields of this class. As
     * static fields are not packed by the VM, each static field occupies
     * one word except for doubles and longs which occupy two words.
     */
    private short staticFieldsSize;

    /**
     * The size (in words) of the static fields of this class that are of a
     * non-primitive type. These fields are guaranteed to preceed all the
     * primitive static fields which means that only the first
     * <code>refStaticFieldsSize</code> words of the object holding that
     * static fields of this class need be considered by the garbage
     * collector as pointer fields.
     */
    private short refStaticFieldsSize;

    /**
     * The bottom 8 bits of the modifier for <init>() (if present).
     */
    private byte initModifiers;
    
    /**
     * offset given to methods that are illegal to call, such as hosted methods.
     */
    public final static int ILLEGAL_METHOD_OFFSET = 0xFFFF;

    public final static boolean ENABLE_DYNAMIC_CLASSLOADING = true;

    /*---------------------------------------------------------------------------*\
     *                       Standard java.lang.Class API                        *
    \*---------------------------------------------------------------------------*/

    private static SquawkHashtable klassToClass;
    private static Klass klassClass;

    /**
     * Gets the Class instance corresponding to a given Klass instance, creating it
     * first if necessary.
     * 
     * @param klass the Klass object
     * @return the Class object
     */
    public static synchronized Class asClass(Klass klass) {
        if (klassToClass == null) {
            klassToClass = new SquawkHashtable();
            klassClass = VM.getCurrentIsolate().getBootstrapSuite().lookup("java.lang.Class");
            Assert.always(klassClass != null, "Klass.java", 261);
        }
        Class c = (Class)klassToClass.get(klass);
        if (c == null) {
            c = (Class)GC.newInstance(klassClass);
            NativeUnsafe.setObject(c, (int)FieldOffsets.java_lang_Class$klass, klass);
            klassToClass.put(klass, c);
        }
        return c;
    }

    /**
     * Gets the Klass instance corresponding to a given Class instance.
     * @param c the Class object
     * @return the Klass object
     */
    public static Klass asKlass(Class c) {
        if (c == null) {
            throw new NullPointerException();
        }
        return (Klass)NativeUnsafe.getObject(c, (int)FieldOffsets.java_lang_Class$klass);
    }

    /**
     * Look up class from class files.
     * 
     * @param className
     * @return class, exception, or null
     * @throws HostedPragma 
     */
    private static Object forNameDynamic(String className)  

//        throws HostedPragma

    {
        ClassNotFoundException cnfe = null;
        Klass klass = null;
        Isolate isolate = VM.getCurrentIsolate();
        if (isolate.getLeafSuite().isClosed()) {
            cnfe = new ClassNotFoundException(className + " [The current isolate has no class path]");
        } else if ((className.startsWith("java.")
                || className.startsWith("javax.")
                || className.startsWith("com.sun.squawk.")
                || className.startsWith("com.sun.cldc."))) {
            String packageName = className.substring(0, className.lastIndexOf('.'));
            cnfe = new ClassNotFoundException("Prohibited package name: " + packageName);
        } else {
            TranslatorInterface translator = isolate.getTranslator();
            if (translator != null) {
                translator.open(isolate.getLeafSuite(), isolate.getClassPath());
                if (translator.isValidClassName(className)) {
                    klass = Klass.getClass(className, false);
                    translator.load(klass);
                    // Must load complete closure
                    translator.close(Suite.DEBUG);
                }
            } else {
                if (VM.isVerbose()) {
                    VM.println("[translator not found - dynamic class loading disabled]");
                }
            }
        }
        
        if (cnfe != null) {
            return cnfe;
        }
        return klass;
    }
    
    /**
     * Returns the <code>Klass</code> object associated with the class
     * with the given string name.
     * @param className the class name to lookup
     * @return Klass
     * @throws java.lang.ClassNotFoundException 
     */
    public static synchronized Klass forName(String className) throws ClassNotFoundException {
       // Verbose trace.
        if (VM.isVeryVerbose()) {
            VM.print("[Klass.forName(");
            VM.print(className);
            VM.println(")]");
        }
        
        Klass klass = Klass.lookupKlass(className);
        ClassNotFoundException cnfe = null;
        if (klass == null) {
            if (ENABLE_DYNAMIC_CLASSLOADING || VM.isHosted()) {
            	Object result = forNameDynamic(className);
            	if (result instanceof Klass) {
                    klass = (Klass)result;
                } else {
                    cnfe = (ClassNotFoundException)result;
                }
            }
        }
        if (klass != null && klass.getState() != Klass.STATE_DEFINED) {
        	klass.initialiseClass();
            return klass;
        }
        // handle error cases:
        // TODO: Simplify, but make sure TCK tests pass

        if (VM.getCurrentIsolate().getLeafSuite().shouldThrowNoClassDefFoundErrorFor(className)) {
            if (cnfe != null && cnfe.getMessage() != null) {
                throw new NoClassDefFoundError(cnfe.getMessage());
            }
            throw new NoClassDefFoundError(className);
        }
        if (cnfe != null) {
            throw cnfe;
        }

////        if (VM.getCurrentIsolate().getLeafSuite().shouldThrowNoClassDefFoundErrorFor(className)) {
////            throw new NoClassDefFoundError(className);
////        }

        throw new ClassNotFoundException(className);
    }

    void initForObjectGraphLoader(MethodBody[] virtualMethods, MethodBody[] staticMethods, Klass superType, Klass[] interfaces, Object[] objects, UWord[] oopMap, UWord oopMapWord, UWord[] dataMap, UWord dataMapWord, short dataMapLength, int modifiers, byte state, short instanceSizeBytes, short staticFieldsSize, short refStaticFieldsSize, byte initModifiers) {
    	this.virtualMethods = virtualMethods;
    	this.staticMethods = staticMethods;
        this.superType = superType;
        this.interfaces = interfaces;
        this.objects = objects;
        this.oopMap = oopMap;
        this.oopMapWord = oopMapWord;
        this.dataMap = dataMap;
        this.dataMapWord = dataMapWord;
        this.dataMapLength = dataMapLength;
        this.modifiers = modifiers;
        this.state = state;
        this.instanceSizeBytes = instanceSizeBytes;
        this.staticFieldsSize = staticFieldsSize;
        this.refStaticFieldsSize = refStaticFieldsSize;
        this.initModifiers = initModifiers;
    }
    
    /**
     * Creates a new instance of a class. This method can only be called for a non-array,
     * non-interface class that {@link #hasDefaultConstructor has a default constructor}.
     * @return new object
     */
    public final

T

////Object

	newInstance() /* throws InstantiationException, IllegalAccessException */ {
        int constructorIndex = getDefaultConstructorIndex();
        Assert.always(constructorIndex >= 0, "Klass.java", 413);

        T res = (T) GC.newInstance(this);

////        Object res = GC.newInstance(this);

        VM.callStaticOneParm(this, constructorIndex, res);
        return res;
    }

    /**
     * Returns the modifiers for this class or interface.
     * @return int representing class modifiers
     */
    public final int getModifiers() throws AllowInlinedPragma {
        return modifiers;
    }

    /**
     * Return true if this class can be allocated (not abstract or interface, etc.)
     * If true, there may be some object "obj" such that GC.getKlass(obj) == this class.
     * 
     * @return true if objects of this klass can be insta
     */
    public boolean isInstantiable() {
        return !(isInterface() || isPrimitive() || isAbstract());
    }
    /**
     * Determines if the specified <code>Class</code> object represents an
     * interface type.
     *
     * @return  <code>true</code> if this object represents an interface;
     *          <code>false</code> otherwise.
     */
    public final boolean isInterface() throws ForceInlinedPragma {
        return Modifier.isInterface(getModifiers());
    }

    /**
     * Determines if this class represents a primitive type (e.g. int, boolean, double, ...etc).
     *
     * @return   true if this class represents a primitive type
     */
    public final boolean isPrimitive() {
        return Modifier.isPrimitive(getModifiers());
    }

    /**
     * Determines if the static fields of this class are {@link Global VM global}.
     *
     * @return   true if the static fields of this class are {@link Global VM global}
     */
    public final boolean hasGlobalStatics() {
        return Modifier.hasGlobalStatics(getModifiers());
    }


//    /**
//     * Determines if this class has a finalize() method.
//     *
//     * @return true if it does
//     */
//    public final boolean hasFinalizer() {
//        return Modifier.hasFinalizer(getModifiers());
//    }


    /**
     * The index where the first "special" static method goes. "Special" methods are allocated in order:
     * "<init>", "<clinit>", "main".
     */
    private final static int CANONICAL_SPECIAL_METHOD_INDEX = 0;

    /**
     * Determines if this class has a <code>void main(String[])</code> method.
     *
     * @return true if it does
     */
    public final boolean hasMain() {
        return (modifiers & Modifier.KLASS_HAS_MAIN) != 0;
    }

   /**
     * Returns the index to the main method in the static method table.
     * Will return -1 if there is no main method.
     */
    final int getMainIndex() {
        if (hasMain()) {
            int index = CANONICAL_SPECIAL_METHOD_INDEX;
            if (hasDefaultConstructor()) {
                index++;
            }
            if (hasClinit()) {
                index++;
            }
            return index;
        } else {
            return -1;
        }
    }

    /**
     * Determines if this class has a static initializer.
     *
     * @return true if it does
     */
    public final boolean hasClinit() {
        return (modifiers & Modifier.KLASS_HAS_CLINIT) != 0;
    }

    /**
     * Returns the index to the static initializer method in the static method table.
     * Will return -1 if there is no static initializer method.
     */
    final int getClinitIndex() {
        if (hasClinit()) {
            if (hasDefaultConstructor()) {
                return CANONICAL_SPECIAL_METHOD_INDEX + 1;
            } else {
                return CANONICAL_SPECIAL_METHOD_INDEX;
            }
        } else {
            return -1;
        }
    }
    
    /**
     * Determines if this class has a default constructor.
     *
     * @return true if it does
     */
    public final boolean hasDefaultConstructor() {
        return (modifiers & Modifier.KLASS_HAS_DEFAULT_INIT) != 0;
    }

    /**
     * Returns the index to the default constructor in the static method table.
     * Will return -1 if there is no default constructor.
     */
    final int getDefaultConstructorIndex() {
        if (hasDefaultConstructor()) {
            return CANONICAL_SPECIAL_METHOD_INDEX;
        } else {
            return -1;
        }
    }

    /**
     * Gets the modifiers of this class's default constructor. The returned
     * value is meaningless unless {@link #hasDefaultConstructor} return true.
     *
     * @return the modifiers of this class's default constructor.
     */
    public final int getDefaultConstructorModifiers() {
        return initModifiers;
    }

    /**
     * Converts the object to a string. The string representation is the
     * string "class" or "interface", followed by a space, and then by the
     * fully qualified name of the class in the format returned by
     * <code>getName</code>.  If this <code>Class</code> object represents a
     * primitive type, this method returns the name of the primitive type.  If
     * this <code>Class</code> object represents void this method returns
     * "void".
     *
     * @return a string representation of this class object.
     */
    public final String toString() {
        return (isInterface() ? "interface " : "class ") + getName();
    }

    /**
     * Determines if the specified <code>Object</code> is assignment-compatible
     * with the object represented by this <code>Klass</code>.
     * @param obj object to test
     * @return true if obj is instance of this klass
     */
    public final boolean isInstance(Object obj) {
        return obj != null && isAssignableFrom(GC.getKlass(obj));
    }

    /**
     * Determines if this <code>Class</code> object represents an array class.
     *
     * @return  <code>true</code> if this object represents an array class;
     *          <code>false</code> otherwise.
     */
    public final boolean isArray() throws ForceInlinedPragma {
        return Modifier.isArray(getModifiers());
    }

    /**
     * Determines if this <code>Class</code> object represents a special class
     * that the Squawk translator and compiler convert into a primitive type. Values
     * of these types are not compatible with any other types and requires explicit
     * conversions.
     *
     * @return  <code>true</code> if this object represents a special class;
     *          <code>false</code> otherwise.
     */
    public final boolean isSquawkPrimitive() {
        return Modifier.isSquawkPrimitive(getModifiers());
    }

    /**
     * Determines if this <code>Class</code> object represents an array class
     * in the Squawk sense i.e. it is a Java array or some kind of string.
     *
     * @return  <code>true</code> if this object represents an array class;
     *          <code>false</code> otherwise.
     */
    public final boolean isSquawkArray() throws ForceInlinedPragma {
        return isSquawkArray(this);
    }

    /**
     * Static version of {@link #isSquawkArray()} so that garbage collector can
     * invoke this method on a possibly forwarded Klass object.
     */
    static boolean isSquawkArray(Klass klass) throws ForceInlinedPragma {
        return Modifier.isSquawkArray(klass.modifiers);
    }

    /**
     * Returns the size of the elements of a Squawk array.
     *
     * @return  the size in bytes
     */
    final int getSquawkArrayComponentDataSize() throws ForceInlinedPragma {
        return getSquawkArrayComponentDataSize(this);
    }

    /**
     * Static version of {@link #getSquawkArrayComponentDataSize()} so that garbage collector can
     * invoke this method on a possibly forwarded Klass object.
     */
    static int getSquawkArrayComponentDataSize(Klass klass) {
        if (false) Assert.that(isSquawkArray(klass));
        if (klass.id == CID.STRING_OF_BYTES) {
            return 1;
        } else if (klass.id == CID.STRING) {
            return 2;
        } else {
            return getDataSize(getComponentType(klass));
        }
    }

    /**
     * Returns the name of this entity in the format expected by {@link Class#getName}.
     * @return the class name in std Java format
     */
    public final String getName() {
        if (!isArray()) {
            return name;
        }
        Klass base = componentType;
        int dimensions = 1;
        while (base.isArray()) {
            base = base.getComponentType();
            dimensions++;
        }
        if (!base.isPrimitive()) {
            return name.substring(0, dimensions) + 'L' + name.substring(dimensions) + ';';
        }
        char primitiveCode = getSignatureFirstChar(base.getSystemID());
        if (false) Assert.that(primitiveCode != 'L');
        return name.substring(0, dimensions) + primitiveCode;
    }

    /**
     * Returns the <code>Class</code> representing the superclass of the entity
     * (class, interface, primitive type or void) represented by this
     * <code>Class</code>.  If this <code>Class</code> represents either the
     * <code>Object</code> class, an interface, a primitive type, or void, then
     * null is returned.  If this object represents an array class then the
     * <code>Class</code> object representing the <code>Object</code> class is
     * returned.
     *
     * @return the superclass of the class represented by this object.
     */
    public final Klass getSuperclass() {
        if (isInterface() || isPrimitive() || isInternalType()) {
            return null;
        }
        int cid = this.id;
        if (cid == CID.OBJECT || cid == CID.VOID) {
            return null;
        }
        return superType;
    }

    /** Handle extra cases that only happen during translation.
     * 
     * @param klass
     * @return
     * @throws HostedPragma
     */

    @Vm2c(macro="(false)")

     public final boolean isAssignableFrom0(Klass klass) {
        /*
         * Quick check for assigning to -T-.
         */
        if (this == Klass.TOP) {
            return true;
        }

        /*
         * Any subclass of java.lang.Object or interface class is assignable from 'null'.
         */
        if (klass == Klass.NULL) {
            return isInterface() || isSubtypeOf(OBJECT);
        }
        return false;
    }

    /**
     * Determines if the class or interface represented by this
     * <code>Class</code> object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified
     * <code>Class</code> parameter. It returns <code>true</code> if so;
     * otherwise it returns <code>false</code>. If this <code>Class</code>
     * object represents a primitive type, this method returns
     * <code>true</code> if the specified <code>Class</code> parameter is
     * exactly this <code>Class</code> object; otherwise it returns
     * <code>false</code>.
     *
     * <p> Specifically, this method tests whether the type represented by the
     * specified <code>Class</code> parameter can be converted to the type
     * represented by this <code>Class</code> object via an identity conversion
     * or via a widening reference conversion. See <em>The Java Language
     * Specification</em>, sections 5.1.1 and 5.1.4 , for details.
     *
     * @param   klass   the <code>Class</code> object to be checked
     * @return  the <code>boolean</code> value indicating whether objects
     *                  of the type <code>klass</code> can be assigned to
     *                  objects of this class
     * @exception NullPointerException if the specified Class parameter is null
     * @since JDK1.1
     */
    public final boolean isAssignableFrom(Klass klass) {
        if (false) Assert.that(getState() != STATE_ERROR && klass.getState() != STATE_ERROR);

        /*
         * Quick check for equality (the most common case) .
         */
        if (this == klass) {
            return true;
        }

        /*
         * Check to see if 'klass' is somewhere in this class's hierarchy
         */
        if (klass.isSubtypeOf(this)) {
            return true;
        }

        /*
         * Check for uncommon cases that can only occur when translating
         */
        if (isAssignableFrom0(klass)) {
            return true;
        }

        /*
         * Arrays of primitives must be exactly the same type.
         */
        if (isArray()) {
            if (klass.isArray()) {
                return getComponentType().isAssignableFrom(klass.getComponentType());
            }
        } else {
            if (isInterface()) {
                return klass.isImplementorOf(this);
            }
        }

        /*
         * Otherwise there is no match.
         */
        return false;
    }

    /**
     * Finds a resource with a given name.  This method returns null if no
     * resource with this name is found.  The rules for searching
     * resources associated with a given class are profile
     * specific.
     *
     * @param name  name of the desired resource
     * @return      a <code>java.io.InputStream</code> object.
     * @since JDK1.1
     */
    public final java.io.InputStream getResourceAsStream(String name) {
        if (false) Assert.that(getState() != STATE_ERROR);
        Isolate isolate = VM.getCurrentIsolate();
        Suite suite = isolate.getLeafSuite();
        if (suite == null) {
            suite = isolate.getBootstrapSuite();
        }
        return suite.getResourceAsStream(name, this);
    }

    /*---------------------------------------------------------------------------*\
     *                 Global constant for zero length class array.              *
    \*---------------------------------------------------------------------------*/

    /**
     * A zero length array of classes.
     */
    public static final Klass[] NO_CLASSES = {};

    /*---------------------------------------------------------------------------*\
     *                               Constructor                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Creates a class representing a type. If <code>name</code> starts with
     * '[' then the class being created represents an array type in which
     * case <code>componentType</code> must not be null.
     *
     * @param name           the name of the class being created
     * @param componentType  the class representing the component type or null
     *                       if the class being created does not represent an
     *                       array type
     * @param suiteID        the index of the class within its suite
     * @param hasSystemID    denotes if <code>suiteID</code> is also a system wide ID
     */
    private Klass(String name, Klass componentType, int suiteID, boolean hasSystemID) {
        this.name = name;
        this.id = hasSystemID ? (short)suiteID : (short)-(suiteID+1);
        this.oopMapWord = UWord.zero();
        Assert.always((suiteID & 0xFFFF) == suiteID, "Klass.java", 847);

        if (name.charAt(0) == '[') {
            if (false) Assert.that(componentType != null); // Component type can't be null.
            this.componentType = componentType;
            this.modifiers     = (Modifier.PUBLIC | Modifier.ARRAY | Modifier.SQUAWKARRAY | Modifier.SYNTHETIC);
            this.superType     = Klass.OBJECT;
            this.interfaces    = Klass.NO_CLASSES;

            // Encode the data size of the component type in a data map of length 1
            int log2ComponentDataSize;
            switch (componentType.getDataSize()) {
                case 1: log2ComponentDataSize = 0; break;
                case 2: log2ComponentDataSize = 1; break;
                case 4: log2ComponentDataSize = 2; break;
                case 8: log2ComponentDataSize = 3; break;
                default: throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere();
            }
            this.dataMapWord = UWord.fromPrimitive(log2ComponentDataSize);

        } else {
            this.componentType = null;
            this.dataMapWord = UWord.zero();
            if (hasSystemID && (suiteID == CID.STRING || suiteID == CID.STRING_OF_BYTES)) {
                this.modifiers = Modifier.SQUAWKARRAY;
                int log2ComponentDataSize = 0;
                if (suiteID == CID.STRING) {
                    log2ComponentDataSize = 1;
                }
                this.dataMapWord = UWord.fromPrimitive(log2ComponentDataSize);
            }
        }

    }

    void resetBootKlass() {
        if (!isSynthetic()) {
            state = STATE_DEFINED;
            virtualMethods = null;
            staticMethods = null;
        }
    }
    
    /**
     * Only used by UninitializedObjectClass constructor.
     *
     * @param name       the name of the class
     * @param superType  must be {@link #UNINITIALIZED_NEW}
     */
    protected Klass(String name, Klass superType) {
        this.name          = name;
        this.id            = Short.MIN_VALUE;
        this.modifiers     = Modifier.PUBLIC | Modifier.SYNTHETIC;
        this.superType     = superType;
        this.state         = STATE_CONVERTED;
        this.componentType = null;
        this.oopMapWord    = UWord.zero();
        this.dataMapWord   = UWord.zero();
    }

    /*---------------------------------------------------------------------------*\
     *                                 Setters                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * Updates the modifiers for this class by setting one or more modifier
     * flags that are not currently set. This method does not unset any
     * modifier flags that are currently set.
     *
     * @param modifiers a mask of the constants defined in {@link Modifier}
     */
    public final void updateModifiers(int modifiers) {
        // If we are setting bits that are already set, then make this a no-op
        // TODO Look at callers and see if this should be done this way or not
        // Added to handle the case of IRBuilder.opc_getstatic at runtime
        if ((this.modifiers & modifiers) == modifiers) {
            return;
        }
        if ((modifiers & Modifier.SYNTHETIC) != 0) {
            state = STATE_CONVERTED;
            interfaces = Klass.NO_CLASSES;
        }
        this.modifiers |= modifiers;

        if ((modifiers & Modifier.COMPLETE_RUNTIME_STATICS) != 0) {
            resizeStaticFields();
        }
    }

    /*---------------------------------------------------------------------------*\
     *                               Miscellaneous                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the internal class name. The names of classes in the Squawk system
     * are the same as the names returned by {@link #getName() getName} except
     * for classes representing arrays and classes representing primitive types.
     * For the former, the delimiting <code>L</code> and <code>;</code> are
     * omitted and the internal implementation classes are returned for the
     * latter. Thus:
     *
     * <blockquote><pre>
     * (new Object[3]).getClass().getInternalName()
     * </pre></blockquote>
     *
     * returns "<code>[java.lang.Object</code>" and:
     *
     * <blockquote><pre>
     * (new int[3][4][5][6][7][8][9]).getClass().getInternalName()
     * </pre></blockquote>
     *
     * returns "<code>[[[[[[[java.lang._int_</code>". The other internal names
     * for the primitive types are as follows:
     *
     * <blockquote><pre>
     * java.lang._byte_            byte
     * java.lang._char_            char
     * java.lang._double_          double
     * java.lang._float_           float
     * java.lang._int_             int
     * java.lang._long_            long
     * java.lang._short_           short
     * java.lang._boolean_         boolean
     * java.lang.Void              void
     * </pre></blockquote>
     *
     * @return   the internal class name.
     */
    public final String getInternalName() {
        return getInternalName(this);
    }

    /**
     * Static version of {@link #getInternalName()}
     *
     * @return   the internal class name.
     */
    static String getInternalName(Klass klass) {
        return klass.name;
    }

    /**
     * Gets the JNI signature of this class.
     *
     * @return the JNI signature for this class
     * @see <a href="http://java.sun.com/j2se/1.4.2/docs/guide/jni/spec/types.html">JNI Signatures</a>
     */
    public final String getSignature() {
        if (isPrimitive() || this == VOID) {
            return "" + Klass.getSignatureFirstChar(getSystemID());
        } else if (isArray()) {
            return getName().replace('.', '/');
        } else {
            return "L" + getName().replace('.', '/') + ";";
        }
    }

    /**
     * Gets the first char of the name of a class when it is in signature form.
     *
     * @param systemID  the system ID of the class to query
     * @return char    the first char of the class's signature form
     */
    public static char getSignatureFirstChar(int systemID) {
        switch (systemID) {
            case CID.BOOLEAN:  return 'Z';
            case CID.BYTE:     return 'B';
            case CID.CHAR:     return 'C';
            case CID.SHORT:    return 'S';
            case CID.INT:      return 'I';
            case CID.LONG:     return 'J';
            case CID.FLOAT:    return 'F';
            case CID.DOUBLE:   return 'D';
            case CID.VOID:     return 'V';
            default:           return 'L';

        }
    }

    /**
     * Formats the names of a given array of classes into a single string
     * with each class name separated by a space. The {@link #getName()}
     * method is used to convert each class to a name.
     *
     * @param   klasses  the classes to format
     * @return  the space separated names of the classes in <code>klasses</code>
     */
    public static String getNames(Klass[] klasses) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i != klasses.length; ++i) {
            buf.append(klasses[i].getName());
            if (i != klasses.length - 1) {
                buf.append(' ');
            }
        }
        return buf.toString();
    }

    /**
     * Gets the suite identifier for this class. A suite identifier for a class is
     * only unique within its suite.
     *
     * @return the class identifier for this class
     */
    public final int getSuiteID() throws ForceInlinedPragma  {
        return getSuiteID(this);
    }

    /**
     * Gets the system wide unique ID for this class or a negative value if it doesn't have one.
     *
     * @return the system wide unique ID for this class or a negative value if it doesn't have one
     */
    public final int getSystemID() throws AllowInlinedPragma {
        return getSystemID(this);
    }

    /**
     * Static version of {@link #getSuiteID()} so that garbage collector can
     * invoke this method on a possibly forwarded Klass object.
     */
    static int getSuiteID(Klass klass) {
        return klass.id >= 0 ? klass.id : -(klass.id + 1);
    }

//    Used in experiment to compress out null entries in a suite's class table:
//    /**
//     * Reset a klass' iD after classes are removed from a suite.
//     */
//    static void setSuiteID(Klass klass, int newSuiteID) {
//        Assert.always(newSuiteID >= 0, "Klass.java", 1077);
//        if (klass.id >= 0) {                    // can't reset system classes:
//            Assert.always(newSuiteID == klass.id, "setSuiteID(" + klass + ", " + newSuiteID + ") old ID: " + klass.id, "Klass.java", 1079);
//        } else {
//            int oldSuiteID = getSuiteID(klass);
//            klass.id = (short) -(newSuiteID + 1);
//            if (newSuiteID == oldSuiteID) {
//                Assert.always(getSuiteID(klass) == oldSuiteID, "Klass.java", 1084);
//            } else {
//                Assert.always(getSuiteID(klass) < oldSuiteID, "Klass.java", 1086);
//            }
//        }
//    }

    /**
     * Static version of {@link #getSystemID()} so that garbage collector can
     * invoke this method on a possibly forwarded Klass object.
     */
    static int getSystemID(Klass klass) throws AllowInlinedPragma {
        return klass.id;
    }

    /**
     * Returns the class representing the component type of an
     * array.  If this class does not represent an array class this method
     * returns null.
     *
     * @return the class representing the component type of this
     *         class if this class is an array
     */
    public final Klass getComponentType() throws AllowInlinedPragma {
        return getComponentType(this);
    }

    /**
     * Static version of {@link #getComponentType()} so that garbage collector can
     * invoke this method on a possibly forwarded Klass object.
     */
    static Klass getComponentType(Klass klass) throws AllowInlinedPragma {
        return klass.componentType;
    }

    /**
     * Gets the size (in words) of an instance of this class.
     *
     * @return the size (in words) of an instance of this class
     */
    final int getInstanceSize() throws ForceInlinedPragma {
        return getInstanceSize(this);
    }

    /**
     * Static version of {@link #getInstanceSize}
     *
     * @return the size (in words) of an instance of this class
     */
    static int getInstanceSize(Klass klass) throws ForceInlinedPragma {
        if (false) Assert.that(!Modifier.isArray(klass.modifiers));
        if (false) Assert.that(!Modifier.isInterface(klass.modifiers));
        if (false) Assert.that(!Modifier.isSynthetic(klass.modifiers));
        return (klass.instanceSizeBytes + (HDR.BYTES_PER_WORD - 1)) / HDR.BYTES_PER_WORD;
    }

    /**
     * Get the size (in words) of the static fields of this class.
     *
     * @return the number of words
     */
    public final int getStaticFieldsSize() {
        return staticFieldsSize;
    }

    /**
     * Get the size (in words) of the static fields of this class that are of a
     * non-primitive type. These fields will precede all the primitive type
     * fields in the globals data structure for this class.
     *
     * @return the number of words
     */
    public final int getRefStaticFieldsSize() {
        return getRefStaticFieldsSize(this);
    }

    /**
     * Static version of {@link #getRefStaticFieldsSize()}
     *
     * @return the number of words
     */
    static int getRefStaticFieldsSize(Klass klass) {
        return klass.refStaticFieldsSize;
    }

    /**
     * Determines if this is a public class.
     *
     * @return   true if this is a public class
     */
    public final boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    /**
     * Determines if this is an abstract class.
     *
     * @return   true if this is an abstract class
     */
    public final boolean isAbstract() {
        return Modifier.isAbstract(getModifiers());
    }

    /**
     * Determines if this class can be subclassed.
     *
     * @return  true if this class can be subclassed
     */
    public final boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    /**
     * Determines if this class is not defined by a class file. This will
     * return false for all classes representing arrays, primitive types,
     * verification-only types (e.g. TOP and LONG2) and the type for
     * <code>void</code>. For all other classes, this method will return
     * true.
     *
     * @return  true if this class is not defined by a class file
     */
    public final boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }

    /**
     * Determines if this class does not appear in any source code.
     *
     * @return  true if this class does not appear in any source code
     */
    public final boolean isSourceSynthetic() {
        return Modifier.isSourceSynthetic(getModifiers());
    }

    /**
     * Determines if this class is used to annotate a Java component (i.e. class, method or field)
     * that is treated specially in some way by the translator and/or compiler.
     *
     * @return  true if this a pragma annotator class
     */
    public final boolean isPragma() {
       return name.startsWith("com.sun.squawk.pragma.");
    }

    /**
     * Determines if this class is only used by the VM internally and does not
     * correspond to any Java source language type.
     *
     * @return true if this is a VM internal type
     */
    public final boolean isInternalType() {
        return name.charAt(name.length() - 1) == '-';
    }

    /**
     * Determines if this class is a subclass of a specified class.
     *
     * @param    klass  the class to check
     * @return   true if this class is a subclass of <code>klass</code>.
     */
    final boolean isSubclassOf(Klass klass) {
        /*
         * Primitives never match non-primitives
         */
        if (this.isPrimitive() != klass.isPrimitive()) {
            return false;
        }
        Klass thisClass = this;
        while (thisClass != null) {
            if (thisClass == klass) {
                return true;
            }
            thisClass = thisClass.getSuperclass();
        }
        return false;
    }

    /**
     * Determines if this class is a subtype of a specified class. This test
     * uses the verification type hierarchy.
     *
     * @param    klass  the class to check
     * @return   true if this class is a subtype of <code>klass</code>.
     */
    static boolean isSubtypeOf(Klass thisClass, Klass klass) {
        while (thisClass != null) {
            if (thisClass == klass) {
                return true;
            }
            thisClass = thisClass.superType;
        }
        return false;
    }

    /**
     * Determines if this class is a subtype of a specified class. This test
     * uses the verification type hierarchy.
     *
     * @param    klass  the class to check
     * @return   true if this class is a subtype of <code>klass</code>.
     */
    final boolean isSubtypeOf(Klass klass) throws ForceInlinedPragma {
        return isSubtypeOf(this, klass);
    }

    /**
     * Determine if this class implements a specified class.
     *
     * @param    anInterface  the class to check
     * @return   true if <code>klass</code> is an interface class and this class implements it.
     */
    public final boolean isImplementorOf(Klass anInterface) {
        if (false) Assert.that(anInterface.isInterface());
        for (int i = 0 ; i < interfaces.length ; i++) {
            Klass iface = interfaces[i];
            if (iface == anInterface || iface.isImplementorOf(anInterface)) {
                return true;
            }
        }

//      if (getSuperclass() != null) { // correct, but slower except in very odd cases
        if (superType != null) {
            return superType.isImplementorOf(anInterface);
        }
        return false;
    }

    /**
     * Return true if a given class is in the same package as this class.
     *
     * @param  klass   the class to test
     * @return true if <code>klass</code> is in the same package as this class
     */
    public final boolean isInSamePackageAs(Klass klass) {
        String name1 = this.getInternalName();
        String name2 = klass.getInternalName();
        int last1 = name1.lastIndexOf('.');
        int last2 = name2.lastIndexOf('.');
        if (last1 != last2) {
            return false;
        }
        if (last1 == -1) {
            return true;
        }
        for (int i = 0 ; i < last1 ; i++) {
            if (name1.charAt(i) != name2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether or not this class is accessible by a specified class.
     *
     * @param   klass   a class that refers to this class
     * @return  true if <code>other</code> is null or has access to this class
     */
    public final boolean isAccessibleFrom(Klass klass) {
        return (klass == null || klass == this || this.isPublic() || isInSamePackageAs(klass));
    }

    /**
     * Determines whether or not this class is a reference type.
     *
     * @return  true if it is
     */
    public final boolean isReferenceType() {
        return !isPrimitive() && !isSquawkPrimitive();
    }

    /**
     * Gets the data size (in bytes) of the type represented by this class.
     *
     * @return the data size of a value of the type represented by this class
     */
    public final int getDataSize() throws ForceInlinedPragma {
        return getDataSize(this);
    }

    /**
     * Static version of {@link #getDataSize()} so that garbage collector can
     * invoke this method on a possibly forwarded Klass object.
     */
    static int getDataSize(Klass klass) {
        switch (getSystemID(klass)) {
            case CID.BOOLEAN:
            case CID.BYTECODE:
            case CID.BYTE: {
                return 1;
            }
            case CID.CHAR:
            case CID.SHORT: {
                return 2;
            }
            case CID.DOUBLE:
            case CID.LONG: {
                return 8;
            }
            case CID.FLOAT:
            case CID.INT: {
                return 4;
            }
            default: {
                return HDR.BYTES_PER_WORD;
            }
        }
    }

    /**
     * Gets the class representing the super type of this class in the
     * verification type hierarchy.
     *
     * @return     the super type of this class
     */
    public final Klass getSuperType() {
        return superType;
    }

    /**
     * Gets the list of interfaces implemented by this class.
     *
     * @return the list of interfaces implemented by this class
     */
    public final Klass[] getInterfaces() {
        if (interfaces == null) {
            return Klass.NO_CLASSES;
        } else {
            Klass[] arr = new Klass[interfaces.length];
            System.arraycopy(interfaces, 0, arr, 0, arr.length);
            return arr;
        }
    }

    /**
     * Find the virtual slot number for this class that corresponds to the slot in an interface.
     *
     * @param iklass the interface class
     * @param islot  the virtual slot of the interface
     * @return the virtual slot of this class, or -1 if not found
     */
    final int findSlot(Klass iklass, int islot) {
        if (!isAbstract()) {
            int icount = interfaces.length;
            for (int i = 0; i < icount; i++) {
                if (interfaces[i] == iklass) {
                    return interfaceVTableMaps[i][islot];
                }
            }
        }
        if (superType == null) {
            // protect against malicious code that got past "interface type erasure" in the preverifier
            // it's possible that *this* klass does not implement iklass
           return -1;
        }
        return superType.findSlot(iklass, islot);
    }
    
    /**
     * Given an index in the vtable, find the slot in the interface class that is the defines the method.
     * This does NOT search super interfaces. Otherwise, this is the reverse of findSlot.
     *
     * @param vslot  the slot of the method in the vtable
     * @param results a one element array that will be set to contain the interface class the defines the method
     * @return the interface slot of this method in the interface, or -1.
     */
    final int findISlot(int vslot, Object[] results) {
        int icount = interfaceVTableMaps.length;
        for (int i = 0; i < icount; i++) {
            short[] islots = interfaceVTableMaps[i];
            int scount = islots.length;
            for (int j = 0; j < scount; j++) {
                if (islots[j] == vslot) {
                    Klass iKlass = interfaces[i];
//					VM.print(this.getInternalName());
//					VM.print("#");
//					VM.print(vslot);
//					VM.print(" is defined in interface ");
//					VM.print(iKlass.getInternalName());
//					VM.print("#");
//					VM.print(j);
//					VM.println();
                    results[0] = iKlass;
                    return j;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the vtable for virtual methods.
     *
     * @return the vtable
     */
    public final Object[] getVirtualMethods() {
        return virtualMethods;
    }

    /**
     * Gets the table of static methods for this class.
     *
     * @return the table of static methods
     */
    public final Object[] getStaticMethods() {
        return staticMethods;
    }

    /**
     * Gets a string representation of a given field or method. If
     * <code>member</code> is a field, then the returned string will be the
     * fully qualified name (FQN) of the field's type, a space, the FQN of the
     * declaring class of the field, a period, and finally, the field's name.
     * The string returned if <code>member</code> is a method will be the same
     * as for a field (with the field type replaced by the method's return
     * type), a '(', the FQNs of the parameter types (if any) separated by a
     * ',', and finally a closing ')'. For example:
     * <p><blockquote><pre>
     *     java.lang.PrintStream java.lang.System.out
     *     int java.lang.String.indexOf(java.lang.String,int)
     * </pre></blockquote><p>
     *
     * @param   member   the field or method
     * @param   showType show the type of the field, or return type of the method
     * @return  a string representation of <code>member</code>
     */
    public static String toString(Member member, boolean showType) throws HostedPragma {
        String s = member.getFullyQualifiedName();
        if (member instanceof Method) {
            Method method = (Method)member;
            StringBuffer buf;
            
            if (showType) {
                buf = new StringBuffer(method.getReturnType().getInternalName()).append(' ').append(s);
            } else {
                buf = new StringBuffer(s);
            }
            Klass[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                buf.append("()");
            } else {
                buf.append('(');
                for (int i = 0 ; i < parameterTypes.length ; i++) {
                    buf.append(parameterTypes[i].getInternalName());
                    if (i != parameterTypes.length - 1) {
                        buf.append(',');
                    }
                }
                buf.append(')');
            }
            s = buf.toString();
        } else {
            Field field = (Field)member;
            if (showType) {
                s = field.getType().getInternalName() + ' ' + s;
            }
        }
        return s;
    }

    /**
     * Determines if a given field or method is accessible from a given klass.
     *
     * @param   member the field or method to test
     * @param   klass  the class accessing <code>member</code>
     * @return  true if <code>klass</code> can access <code>member</code>
     */
    public static boolean isAccessibleFrom(Member member, Klass klass) {
        return isAccessibleFrom(member.getDefiningClass(), member.getModifiers(), klass);
    }

    /**
     * Determines if a given field or method is accessible from a given klass.
     *
     * @param   definingClass  the class in which the member is defined
     * @param   modifiers      at least the last 8 bits of the method modifiers
     * @param   accessingKlass the class accessing the member
     * @return  true if <code>klass</code> can access <code>member</code>
     */
    public static boolean isAccessibleFrom(Klass definingClass, int modifiers, Klass accessingKlass) {
        if (false) Assert.that(Modifier.PUBLIC < 255);
        if (false) Assert.that(Modifier.PRIVATE < 255);
        if (false) Assert.that(Modifier.PROTECTED < 255);
        if (accessingKlass == null || definingClass == accessingKlass || Modifier.isPublic(modifiers)) {
            return true;
        }
        if (Modifier.isPrivate(modifiers)) {
            return false;
        }
        if (definingClass.isInSamePackageAs(accessingKlass)) {
            return true;
        }
        if (Modifier.isProtected(modifiers)) {
            return accessingKlass.getSuperclass().isSubclassOf(definingClass);
        }
        return false;
    }

    /**
     * Determines if this class represents a type whose values occupy two
     * 32-bit words.
     *
     * @return true if this class represents a type whose values occupy two
     *              32-bit words, false otherwise
     */
    public final boolean isDoubleWord() {
        return Modifier.isDoubleWord(getModifiers());
    }

    /*---------------------------------------------------------------------------*\
     *                                   DEBUG                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * A flag that controls conditional features (mainly assertions).
     */
    public static final boolean DEBUG_CODE_ENABLED = false;

    /**
     * A flag that controls conditional features (mainly assertions).
     */
    public static final boolean ASSERTIONS_ENABLED = false;

    /**
     * A flag specifying whether the {@link com.sun.squawk.util.Tracer Tracer} class is available.
     */
    public static final boolean TRACING_ENABLED = false;

    /**
     * A flag that controls conditional 64-bitness.
     */
    public static final boolean SQUAWK_64 = false;


//    void dumpMethodSymbols(PrintStream out, String fileName, Object body) {
//        if (body != null) {
//            Method method = findMethod(body);
//            if (method != null) {
//                UWord address = Address.fromObject(method).toUWord();
//                out.println("METHOD."+address.toPrimitive()+".NAME="+method);
//                out.println("METHOD."+address.toPrimitive()+".FILE="+fileName);
//                out.println("METHOD."+address.toPrimitive()+".LINETABLE="+Method.lineNumberTableAsString(method.getLineNumberTable()));
//            }
//        }
//    }
//
//    void dumpMethodSymbols(PrintStream out) {
//        if (!isArray() && !isSynthetic()) {
//            String fileName = getSourceFilePath();
//            if (fileName == null) {
//                fileName = "**UNKNOWN**";
//            }
//            for (int i = 0; i != virtualMethods.length; ++i) {
//                dumpMethodSymbols(out, fileName, virtualMethods[i]);
//            }
//            for (int i = 0; i != staticMethods.length; ++i) {
//                dumpMethodSymbols(out, fileName, staticMethods[i]);
//            }
//        }
//    }


    /*---------------------------------------------------------------------------*\
     *                                   State                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * Resizes the number of words required for static fields to account for all the
     * static fields defined by this class, including primitive constants.
     */
    private void resizeStaticFields() {
        int count = getFieldCount(true);
        for (int i = 0; i != count; ++i) {
            Field field = getField(i, true);
            int offset = field.getOffset();
            if (!Klass.SQUAWK_64 && field.getType().isDoubleWord()) {
                offset++;
            }
            if (staticFieldsSize <= offset) {
                int newSize = offset + 1;
//VM.print("increasing staticFieldsSize for ");
//VM.print(name);
//VM.print(" from ");
//VM.print(staticFieldsSize);
//VM.print(" to ");
                staticFieldsSize = (short)newSize;
//VM.print(staticFieldsSize);
//VM.println();
                // Ensure that the offsets all fit in 16 bits
                if ((staticFieldsSize & 0xFFFF) != newSize) {
                    throw new NoClassDefFoundError("static fields overflow");
                }

            }
        }
    }

    /**
     * Gets the state of this class.
     *
     * @return  one of the <code>STATE_...</code> constants
     */
    public final int getState() {
        return state;
    }

    /**
     * Updates the state of this class. The new state must be
     * logically later than the current state.
     *
     * @param  state  the new state of this class
     */
    public final void changeState(byte state) {
        if (false) Assert.that(this.state < state || state == STATE_ERROR);
        this.state = state;

        /*
         * Complete the vtable by inheriting methods
         */
        if (isArray()) {
            virtualMethods = superType.virtualMethods;
            if (false) Assert.that(virtualMethods != null);
        } else if (state == STATE_CONVERTED) {
            if (!isSynthetic() && this != Klass.OBJECT) {
                if (isInterface()) {
                    // interfaces don't need a vtable...
                    for (int i = 0; i != virtualMethods.length; i++) {
                        if (false) Assert.that(virtualMethods[i] == null);
                    }
                    virtualMethods = NO_METHODS;
                } else {
                    for (int i = 0; i != virtualMethods.length; i++) {
                        if (virtualMethods[i] == null && i < superType.virtualMethods.length) {
                            virtualMethods[i] = superType.virtualMethods[i];
                        }
                        if (!isAbstract()) {
                            if (virtualMethods[i] == null && !isSquawkPrimitive()) {
                                virtualMethods[i] = OBJECT.virtualMethods[MethodOffsets.virtual$java_lang_Object$abstractMethodError];
                                Assert.always(virtualMethods[i] != null, "Klass.java", 1722);
                            }
                        }
                    }
                    
                    // Update static methods for methods that were dead-method eliminated, hosted, or otherwise not there
                    for (int i = 0; i != staticMethods.length; i++) {
                        if (staticMethods[i] == null) {
                            staticMethods[i] = OBJECT.staticMethods[MethodOffsets.java_lang_Object$missingMethodError];
                            if (false) Assert.that(staticMethods[i] != null);
                        }
                    }
                }

                /*
                 * If the execution environment is not the romizer, then the methods must
                 * be converted to their special object form now
                 */
                if (!VM.isHosted()) {
                    fixupMethodTables();
                }
            }


            // Inform the debugger of this class
            if (!VM.isHosted() && !isInternalType()) {
                Debugger debugger = VM.getCurrentIsolate().getDebugger();
                if (debugger != null) {
                    debugger.notifyEvent(new Debugger.Event(Debugger.Event.CLASS_PREPARE, this));
                }
            }


            /*
             * Verbose trace.
             */
            if (VM.isVerbose() && !isSynthetic()) {
                System.out.println("[Loaded " + name + "]");
            }
        }
    }

    /**
     * Constant denoting the initial state of a Klass.
     */
    public final static byte STATE_DEFINED = 0;

    /**
     * Constant denoting that a Klass is currently loading.
     */
    public final static byte STATE_LOADING = 1;

    /**
     * Constant denoting that a Klass is loaded.
     */
    public final static byte STATE_LOADED = 2;

    /**
     * Constant denoting that a Klass is currently having
     * its methods translated.
     */
    public final static byte STATE_CONVERTING = 3;

    /**
     * Constant denoting that a Klass has had its methods
     * translated.
     */
    public final static byte STATE_CONVERTED = 4;

    /**
     * Constant denoting that loading or converting a Klass
     * cause a linkage error.
     */
    public final static byte STATE_ERROR = 5;

    /*---------------------------------------------------------------------------*\
     *                                Setters                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Sets the verification hierarchy super type of this class. This method
     * should only be called when creating the bootstrap types.
     *
     * @param  superType  the verification hierarchy super type of this class
     * @see    Klass
     */
    protected final void setSuperType(Klass superType) {
        if (false) Assert.that(this.superType == null || this.superType == superType); // Cannot change super type
        this.superType = superType;
    }

    /**
     * Completes the definition of this class (apart from its bytecodes) based on the
     * information parsed from a class file.
     *
     * @param  superClass             the super class
     * @param  interfaces             the implemented interfaces
     * @param  virtualMethods         the virtual methods declared
     * @param  staticMethods          the static methods declared (including all constructors)
     * @param  instanceFields         the instance fields declared
     * @param  staticFields           the static fields declared
     * @param  sourceFile             the value of the "SourceFile" attribute
     */
    public final void setClassFileDefinition(Klass             superClass,
                                       Klass[]           interfaces,
                                       ClassFileMethod[] virtualMethods,
                                       ClassFileMethod[] staticMethods,
                                       ClassFileField[]  instanceFields,
                                       ClassFileField[]  staticFields,
                                       String            sourceFile)
    {

        if (false) Assert.that(this.interfaces == null); // Cannot re-define class.

        /*
         * Set the super class
         */
        if (superClass != null) {
            if (this != STRING_OF_BYTES) {
                setSuperType(superClass);

//                if (superClass.hasFinalizer()) {
//                    modifiers |= Modifier.HASFINALIZER;
//                }

            }
        }

        /*
         * Initialize the information pertaining to the fields.
         */
        if (!isInterface()) {
            initializeInstanceFields(instanceFields);
        }
        staticFields = initializeStaticFields(staticFields);

        /*
         * Initialize the method tables and set the offsets.
         */
        initializeVTable(virtualMethods);
        initializeSTable(staticMethods);

        /*
         * Create and install the metadata for this class.
         */
        Suite suite = VM.getCurrentIsolate().getLeafSuite();
        KlassMetadata metadata = new KlassMetadata.Full(this,
                                                   virtualMethods,
                                                   staticMethods,
                                                   instanceFields,
                                                   staticFields,
                                                   sourceFile,
                                                   this.virtualMethods.length,
                                                   this.staticMethods.length);
        suite.installMetadata(metadata);

        /*
         * Compute and set the interface table and interface index table.
         */
        setInterfaces(interfaces);
    }

    /*---------------------------------------------------------------------------*\
     *                        Field offset computation                           *
    \*---------------------------------------------------------------------------*/

    /**
     * Initializes the static field information for this class based on a
     * given set of class file field definitions. The {@link #staticFieldsSize}
     * and {@link #refStaticFieldsSize} values are initialized and the offset
     * of each field is computed. The offsets for all the non-primitive fields
     * are guaranteed to be lower than the offset of any primitive field.
     *
     * @param fields  class file field definitions for the static fields
     * @return a copy of the given array sorted by offsets
     * @see   Member#getOffset()
     */
    private ClassFileField[] initializeStaticFields(ClassFileField[] fields) {
        if (fields.length == 0) {
            return fields;
        }
        int refOffset       = 0;
        int primitiveOffset = 0;
        int constantOffset  = 0;

        int refIndex       = 0;
        int primitiveIndex = 0;
        int constantIndex  = 0;

        // Set the field offsets (which are word offsets)
        // Set the offsets in two stages, first set them relative to 0 for each section, then lower down set them relative to the
        // computed offste from the prior section
        // sortedFields will end up with fields order by: ref type/object slot, primitive slot, constant value
        for (int i = 0; i != fields.length; ++i) {
            ClassFileField field = fields[i];
            Klass type = field.getType();
            if (!type.isReferenceType()) {
                int fieldModifiers = field.getModifiers();
                if (!Modifier.hasConstant(fieldModifiers) || !Modifier.isFinal(fieldModifiers)) {
                    field.setOffset(primitiveOffset++);
                    if (!Klass.SQUAWK_64 && type.isDoubleWord()) {
                        primitiveOffset++;
                    }
                    constantIndex++;
                } else {
                    field.setOffset(constantOffset++);
                    if (!Klass.SQUAWK_64 && type.isDoubleWord()) {
                        constantOffset++;
                    }
                }
            } else {
                field.setOffset(refOffset++);
                primitiveIndex++;
                constantIndex++;
            }
        }

        // Adjust the offsets of the primitive and constant fields and sort the fields
        // into a new array by these adjusted offsets
        ClassFileField[] sortedFields = new ClassFileField[fields.length];
        int constantSize = constantOffset;
        constantOffset = primitiveOffset + refOffset;
        primitiveOffset = refOffset;
        for (int i = 0; i != fields.length; ++i) {
            ClassFileField field = fields[i];
            Klass type = field.getType();
            // Get the 0 relative offset computed above
            int offset = field.getOffset();
            if (!type.isReferenceType()) {
                int fieldModifiers = field.getModifiers();
                if (!Modifier.hasConstant(fieldModifiers) || !Modifier.isFinal(fieldModifiers)) {
                    offset += primitiveOffset;
                    if (false) Assert.that(sortedFields[primitiveIndex] == null);
                    sortedFields[primitiveIndex++] = field;
                } else {
                    offset += constantOffset;
                    if (false) Assert.that(sortedFields[constantIndex] == null);
                    sortedFields[constantIndex++] = field;
                }
                field.setOffset(offset);
            } else {
                // Since refs are first, no need to set their offset as they are already zero relative
                if (false) Assert.that(sortedFields[refIndex] == null);
                sortedFields[refIndex++] = field;
            }
        }

        /* 
         * Register all global statics now. If we wait until lazily initialized at code emit time, we might skip over
         * global vars used by java->c translated methods but not by a java method (becuase Java method was dead method eliminated).
         **/
        if (hasGlobalStatics()) {
            Assert.always(VM.getCurrentIsolate().getLeafSuite().isBootstrap(), "Can't use GlobalStaticFields pragma outside of boostrap suite", "Klass.java", 1974);
            initializeGlobalStatics(this, fields);
        }
        
        /*
         * Compile-time constants must be reified at runtime for non-romized code in order for
         * the TCK to pass. There are numerous TCK tests that access these fields via either
         * 'getstatic' or 'putstatic'. The translator will inline *reads* of romized constant fields
         * from non-romized code by replacing the 'getstatic' instruction with the appropriate
         * load constant instruction. It is illegal for non-romized code to update a constant
         * field in a romized class and the translator will detect this.
         *
         * The reason constants are not reified for romized classes is that it would then mean
         * certain system classes would require initialization that otherwise wouldn't. This
         * includes classes such as the collector and Thread class that have code executed
         * before the VM has progressed far enough since startup to support class initialization.
         */
        if (!VM.isHosted() && constantSize > 0) {
            modifiers |= Modifier.COMPLETE_RUNTIME_STATICS;
        }

        int tempStaticFieldsSize;
        // Initialize the 'staticFieldsSize' and 'refStaticFieldsSize' values
        if ((modifiers & Modifier.COMPLETE_RUNTIME_STATICS) != 0) {
            tempStaticFieldsSize    = (short)(constantOffset + constantSize);
        } else {
            tempStaticFieldsSize    = (short)(constantOffset); // can skip reifying constant fields...
        }
        staticFieldsSize    = (short) tempStaticFieldsSize;
        refStaticFieldsSize = (short) primitiveOffset;

        // Ensure that the offsets all fit in 16 bits
        if ((staticFieldsSize & 0xFFFF) != tempStaticFieldsSize) {
            throw new NoClassDefFoundError("static fields overflow");
        }

        // TODO Find a way to do block bases assertions better ?
        if (Klass.ASSERTIONS_ENABLED) {
            if (false) Assert.that(refOffset >= 0, "ref static field section offsets not computed properly");
            if (false) Assert.that(primitiveOffset >= 0, "primitive static field section offsets not computed properly");
            if (false) Assert.that(constantOffset >= 0, "constant static field section offsets not computed properly");
            int currentOffset;
            boolean encounteredRef = false;
            boolean encounteredPrimitive = false;
            boolean encounteredConstant = false;
            for (int i=0; i < sortedFields.length; i++) {
                ClassFileField field = sortedFields[i];
                Klass type = field.getType();
                if (!type.isReferenceType()) {
                    int fieldModifiers = field.getModifiers();
                    if (!Modifier.hasConstant(fieldModifiers) || !Modifier.isFinal(fieldModifiers)) {
                        encounteredPrimitive = true;
                    } else {
                        encounteredConstant = true;
                        break; // stop looking. Constants not normally reified
                    }
                } else {
                    encounteredRef = true;
                }
                currentOffset = field.getOffset();
                int nextOffset = ((i+1) >= sortedFields.length)?staticFieldsSize:sortedFields[i+1].getOffset();
                if (false) Assert.that(nextOffset >= currentOffset, "static field offsets should go consistently up");
                int intendedSizeOfSlot;
                if(type.isReferenceType()) {
                    intendedSizeOfSlot = 1;
                } else {
                    intendedSizeOfSlot = (!Klass.SQUAWK_64 && type.isDoubleWord())?2:1;
                }
                if (false) Assert.that((nextOffset - currentOffset) == intendedSizeOfSlot, "static field was not allocated a big enough slot");
            }
            if (false) Assert.that(encounteredConstant?constantSize > 0:constantSize == 0, "constant static field section size not computed correctly");
            if (false) Assert.that(encounteredRef?refOffset > 0:refOffset == 0, "ref static field section size not computed correctly");
            if (false) Assert.that(encounteredPrimitive?(constantOffset - primitiveOffset) > 0:(constantOffset - primitiveOffset) == 0, "primitive static field section size not computed correctly");
        }

        return sortedFields;
    }

    /**
     * Register global static fields for this class.
     */
    private static void initializeGlobalStatics(Klass klass, ClassFileField[] fields) {
        java.util.Hashtable intGlobals  = Global.getGlobalInts();
        java.util.Hashtable addrGlobals = Global.getGlobalAddrs();
        java.util.Hashtable oopGlobals  = Global.getGlobalOops();
        for (int i = 0; i != fields.length; ++i) {
            ClassFileField field = fields[i];
            int fieldModifiers = field.getModifiers();
            Klass type = field.getType();
            String fieldname = klass.getInternalName() + "." + field.getName();
            java.util.Hashtable table;
            if (Modifier.hasConstant(fieldModifiers) && Modifier.isFinal(fieldModifiers)) {
                // skip constant static fields
                continue;
            }
            switch (type.getSystemID()) {

////                case CID.FLOAT:  // fall through ...

                case CID.DOUBLE:
                case CID.LONG:
                case CID.OFFSET:
                case CID.UWORD:
                    throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere();
                    
                case CID.BYTE:    // fall through ...
                case CID.BOOLEAN: // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...

                case CID.FLOAT:   // fall through ...

                case CID.INT: {
                    table      = intGlobals;
                    break;
                }
                case CID.ADDRESS: {
                    table      = addrGlobals;
                    break;
                }
                default: {
                    table      = oopGlobals;
                    break;
                }
            }
            
            Integer index = (Integer)table.get(fieldname);
            if (index == null) {
                index = new Integer(table.size());
                table.put(fieldname, index);
            }
        }
    }
        
    /**
     * Initializes the instance field information for this class based on a
     * given set of class file field definitions. The {@link #instanceSizeBytes}
     * {@link #oopMap} and {@link #oopMapWord} are initialized and the offset
     * of each field is computed.
     *
     * @param fields  class file field definitions for the instance fields
     * @see   Member#getOffset()
     */
    private void initializeInstanceFields(ClassFileField[] fields) {
        if (false) Assert.that(!this.isInterface());

        // Special handling for java.lang.Object
        if (this == Klass.OBJECT) {
            if (false) Assert.that(fields.length == 0); // Object cannot have instance fields.
            if (false) Assert.that(instanceSizeBytes == 0);
            oopMap = null;
            oopMapWord = UWord.zero();
            dataMap = null;
            dataMapWord = UWord.zero();
            return;
        }
        
        // Special handling for strings
        if (this == Klass.STRING || this == Klass.STRING_OF_BYTES ) {
            if (false) Assert.that(fields.length == 0); // strings cannot have instance fields.
            if (false) Assert.that(instanceSizeBytes == 0);
            oopMap = null;
            oopMapWord = UWord.zero();
            dataMap = null;
            //dataMapWord set in constructor
            return;
        }

        // Inherit 'instanceSizeBytes' and 'oopMap' from super class if there are no instance fields in this class
        if (fields.length == 0) {
            instanceSizeBytes  = superType.instanceSizeBytes;
            oopMap        = superType.oopMap;
            oopMapWord    = superType.oopMapWord;
            dataMap       = superType.dataMap;
            dataMapWord   = superType.dataMapWord;
            dataMapLength = superType.dataMapLength;
            return;
        }

        // Set the field offsets
        instanceSizeBytes = (short)initializeFieldOffsets(fields);

        // Set the size of an instance (in words) of this class
        if ((instanceSizeBytes & 0xFFFF) != instanceSizeBytes) {
            throw new NoClassDefFoundError("instance fields overflow");
        }

        // Create oop map
        initializeOopMap(fields);

        // Create data map
        initializeDataMap(fields);
    }

    /**
     * Initializes the physical offsets for each field defined by this class.
     *
     * @param fields   the fields defined by this class
     * @return the offset of the first byte after the last field
     */
    private int initializeFieldOffsets(ClassFileField[] fields) {
        int offset = GC.roundUpToWord(superType.instanceSizeBytes);
        for (int i = 0; i != fields.length; ++i) {
            ClassFileField field = fields[i];
            Klass type = field.getType();
            switch (type.getSystemID()) {
                case CID.BOOLEAN:
                case CID.BYTE: {
                    field.setOffset(offset++);
                    break;
                }
                case CID.CHAR:
                case CID.SHORT: {
                    if (offset % 2 != 0) {
                        offset++;
                    }
                    field.setOffset(offset / 2);
                    offset += 2;
                    break;
                }
                case CID.DOUBLE:
                case CID.LONG: {
                    // Doubles and longs are word aligned ...
                    int modWord = offset % HDR.BYTES_PER_WORD;
                    if (modWord != 0) {
                        offset += (HDR.BYTES_PER_WORD - modWord);
                    }
                    field.setOffset(offset / HDR.BYTES_PER_WORD);
                    // ... but always occupy 8 bytes
                    offset += 8;
                    break;
                }
                case CID.FLOAT:
                case CID.INT: {
                    int mod4 = offset % 4;
                    if (mod4 != 0) {
                        offset += (4 - mod4);
                    }
                    field.setOffset(offset / 4);
                    offset += 4;
                    break;
                }
                default: {
                    // References are word aligned ...
                    int modWord = offset % HDR.BYTES_PER_WORD;
                    if (modWord != 0) {
                        offset += (HDR.BYTES_PER_WORD - modWord);
                    }
                    field.setOffset(offset / HDR.BYTES_PER_WORD);
                    // ... and occupy 4 or 8 bytes
                    offset += HDR.BYTES_PER_WORD;
                    break;
                }
            }
        }
        return offset;
    }

    /**
     * Initialize the oop map based on the fields defined by this class and it superclasses.
     *
     * @param fields   the fields defined by this class which have been assigned their physical offsets
     */
    private void initializeOopMap(ClassFileField[] fields) {
        UWord bit;

        // Copy oopMap from parent.
        int instanceSize = getInstanceSize();
        if (instanceSize > HDR.BITS_PER_WORD) {
            oopMap = new UWord[((instanceSize + HDR.BITS_PER_WORD) - 1) / HDR.BITS_PER_WORD];
            if (superType.getInstanceSize() > HDR.BITS_PER_WORD) {
                UWord[] superOopMap = superType.oopMap;

                // Copy parent's OopMap then zero the extra data
                System.arraycopy(superOopMap, 0, oopMap, 0, superOopMap.length);
                for (int i = superOopMap.length; i < oopMap.length; ++i) {
                    oopMap[i] = UWord.zero();
                }
            } else {
                oopMap[0] = superType.oopMapWord;
                for (int i = 1; i < oopMap.length; ++i) {
                    oopMap[i] = UWord.zero();
                }
            }
        } else {
            oopMapWord = superType.oopMapWord;
        }

        // Set the bits in the map
        for (int i = 0; i != fields.length; ++i) {
            ClassFileField field = fields[i];
            if (field.getType().isReferenceType()) {
                int offset = field.getOffset();
                bit = UWord.fromPrimitive(1 << (offset % HDR.BITS_PER_WORD));
                if (instanceSize > HDR.BITS_PER_WORD) {
                    int index = offset / HDR.BITS_PER_WORD;
                    oopMap[index] = oopMap[index].or(bit);
                } else {
                    oopMapWord = oopMapWord.or(bit);
                }
            }
        }
    }

    /**
     * The size (in bits) of a data map entry.
     */
    public final static int DATAMAP_ENTRY_BITS = 2;

    /**
     * The mask used to extract a data map entry.
     */
    public final static int DATAMAP_ENTRY_MASK = 0x03;

    /**
     * The number of data map entries in a word.
     */
    public final static int DATAMAP_ENTRIES_PER_WORD = HDR.BITS_PER_WORD / DATAMAP_ENTRY_BITS;

    /**
     * Initialize the data map based on the fields defined by this class and it superclasses.
     *
     * @param fields   the fields defined by this class which have been assigned their physical offsets
     */
    private void initializeDataMap(ClassFileField[] fields) {

        int paddingEntries = GC.roundUpToWord(superType.instanceSizeBytes) - superType.instanceSizeBytes;
        int totalEntries = fields.length + superType.getDataMapLength() + paddingEntries;
        Assert.always(totalEntries <= Short.MAX_VALUE, "Class has too many fields", "Klass.java", 2302);

        // Copy dataMap from parent.
        if (totalEntries > DATAMAP_ENTRIES_PER_WORD) {
            dataMap = new UWord[ ( (totalEntries + DATAMAP_ENTRIES_PER_WORD) - 1) / DATAMAP_ENTRIES_PER_WORD];

            if (superType.getDataMapLength() > DATAMAP_ENTRIES_PER_WORD) {
                UWord[] superDataMap = superType.dataMap;

                // Copy parent's DataMap & zero the extra data fields
                System.arraycopy(superDataMap, 0, dataMap, 0, superDataMap.length);
                for (int i = superDataMap.length; i < dataMap.length; ++i) {
                    dataMap[i] = UWord.zero();
                }
            } else {
                dataMap[0] = superType.dataMapWord;
                for (int i = 1; i < dataMap.length; ++i) {
                    dataMap[i] = UWord.zero();
                }
            }
        } else {
            dataMapWord = superType.dataMapWord;
        }

        // The entries for the padding are all 0
        dataMapLength = (short)(superType.dataMapLength + paddingEntries);

        // Set the bits in the map after the parent's entries (and any padding)
        for (int i = 0; i != fields.length; ++i) {

            int/*S64*/ log2DataSize = 0;
            ClassFileField field = fields[i];

            // Determine the size of this field as a power of 2
            int dataSize = field.getType().getDataSize();
            switch (dataSize) {
                case 1:  log2DataSize = 0;        break;
                case 2:  log2DataSize = 1;        break;
                case 4:  log2DataSize = 2;        break;
                case 8:  log2DataSize = 3;        break;
                default: throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere();
            }

            // Packing 4 values per byte.  Each field corresponds to 2 bits.
            int shift = (dataMapLength % DATAMAP_ENTRIES_PER_WORD) * DATAMAP_ENTRY_BITS;
            UWord encodedDataSize = UWord.fromPrimitive(log2DataSize << shift);

            if (totalEntries > DATAMAP_ENTRIES_PER_WORD) {
                int index = dataMapLength / DATAMAP_ENTRIES_PER_WORD;
                dataMap[index] = dataMap[index].or(encodedDataSize);
            } else {
                dataMapWord = dataMapWord.or(encodedDataSize);
            }

            dataMapLength++;
        }
    }



    /**
     * Return the number of entries within the dataMap
     *
     * @return   the number of entries in the dataMap
     */
    public final int getDataMapLength() {
        return dataMapLength;
    }

    /**
     * Get the number of bytes a particular entry in the table uses.
     *
     * @param index     an index into the dataMap
     * @return     the number of bytes this entry uses
     */
    public final int getDataMapEntry(int index) {
        if (false) Assert.that(index < dataMapLength && index >= 0, "data map index out of range");

        UWord dmWord;
        // If the length is larger than a word, we look into the UWord array
        if (dataMapLength > DATAMAP_ENTRIES_PER_WORD) {
            dmWord = dataMap[index / DATAMAP_ENTRIES_PER_WORD];
        } else {
            dmWord = this.dataMapWord;
        }

        int log2DataSize = (int)(dmWord.toPrimitive() >> ((index % DATAMAP_ENTRIES_PER_WORD) * DATAMAP_ENTRY_BITS)) & DATAMAP_ENTRY_MASK;
        int dataSize = (1 << log2DataSize);
        return dataSize;
    }


    /*---------------------------------------------------------------------------*\
     *            Method tables initialization and offset computation            *
    \*---------------------------------------------------------------------------*/

    /**
     * A zero-sized table of method bodies.
     */
    private static final Object[] NO_METHODS = {};

    /**
     * Creates a table for holding method bodies. This will return a shared
     * zero-sized table if <code>count</code> is zero.
     *
     * @param   count  the number of entries in the table
     * @return  the table for storing <code>count</code> method bodies
     */
    private static Object[] createMethodTable(int count) {
        return count == 0 ? NO_METHODS : new Object[count];
    }

    /**
     * Initializes the vtable for this class as well as setting the
     * vtable offset for each virtual method.
     *
     * @param methods  the virtual methods declared in the class file
     * @see   Member#getOffset()
     */
    private void initializeVTable(ClassFileMethod[] methods) {
        if (this == Klass.OBJECT || isInterface()) {
            short offset = 0;
            for (int i = 0 ; i < methods.length ; i++) {
                ClassFileMethod method = methods[i];
                if (PragmaException.isHosted(method.getPragmas())) {
                    method.setOffset(ILLEGAL_METHOD_OFFSET);
                } else {
                    method.setOffset(offset);
                    offset++;
                }
            }
            virtualMethods = createMethodTable(offset);
        } else if (methods.length == 0) {
            virtualMethods = superType.virtualMethods; // Inherit the super class's vtable
        } else {
            int offset = superType.virtualMethods.length;
            for (int i = 0 ; i < methods.length ; i++) {
                ClassFileMethod method = methods[i];
                if (PragmaException.isHosted(method.getPragmas())) {
                    method.setOffset(ILLEGAL_METHOD_OFFSET);
                    continue;
                }

                /*
                 * Look for overridden method in the super class (accessible from this class)
                 */
                Method superMethod = superType.lookupMethod(method.getName(),
                                                            method.getParameterTypes(),
                                                            method.getReturnType(),
                                                            this, // accessible from this class
                                                            false);
                /*
                 * If the method can override the one in the super class then use the same vtable offset.
                 * Otherwise allocate a different one. This deals with the case where a sub-class that
                 * is in a different package overrides a package-private member.
                 */
                if (superMethod != null && !superMethod.getDefiningClass().isInterface()) {
                    if (false) Assert.that(superMethod.isAccessibleFrom(this)); // lookupMethod() ensures this is true
                    
                    if (superMethod.isFinal()) {
                        throw new NoClassDefFoundError("cannot override final method: " + superMethod);
                    }

                    // This is a restriction imposed by the way Squawk treats native methods
                    if (superMethod.isNative()) {
                        throw new NoClassDefFoundError("cannot override native method ");
                    }


//                    /*
//                     * Note if this is a finalize() method.
//                     */
//                    if (superMethod.getOffset() == MethodOffsets.virtual$java_lang_Object$finalize) {
//                        modifiers |= Modifier.HASFINALIZER;
//                    }

                    
                    method.setOffset(superMethod.getOffset());
                } else {
                    method.setOffset(offset++);
                }
            }
            virtualMethods = createMethodTable(offset);
        }
    }

    /**
     * Initializes the table of static methods for this class as well as
     * setting the offset for each static method.
     *
     * @param methods  the static methods declared in the class file
     * @see   Member#getOffset()
     */
    private void initializeSTable(ClassFileMethod[] methods) {
        short offset = 0;
        ClassFileMethod defaultConstructor = null;
        ClassFileMethod clinit = null;
        ClassFileMethod main = null;

        // look for special methods
        for (short i = 0; i != methods.length; ++i) {
            ClassFileMethod method = methods[i];
            if (PragmaException.isHosted(method.getPragmas())) {
                method.setOffset(ILLEGAL_METHOD_OFFSET);
            } else {
                if (method.isDefaultConstructor()) {
                    defaultConstructor = method;
                    initModifiers = (byte) method.getModifiers();
                    updateModifiers(Modifier.KLASS_HAS_DEFAULT_INIT);
                } else if (method.isClinit()) {
                    clinit = method;
                    updateModifiers(Modifier.KLASS_HAS_CLINIT);
                    Assert.always(!hasGlobalStatics(), "No static initializer can be used on GlobalStaticFields", "Klass.java", 2514); // <clinit> found for class with global variables.
                } else if (method.isMain()) {
                    updateModifiers(Modifier.KLASS_HAS_MAIN);
                    main = method;
                }
            }
        }

        // get special methods in canonical order
        if (defaultConstructor != null) {
            defaultConstructor.setOffset(offset++);
        }
        if (clinit != null) {
            clinit.setOffset(offset++);
        }
        if (main != null) {
            main.setOffset(offset++);
        }

        // do rest of methods...
        for (short i = 0; i != methods.length; ++i) {
            ClassFileMethod method = methods[i];
            if (!PragmaException.isHosted(method.getPragmas())
                    && !method.isDefaultConstructor()
                    && !method.isClinit()
                    && !method.isMain()) {
                method.setOffset(offset++);
            }
        }
        if (setMustClinit()) {
            updateModifiers(Modifier.KLASS_MUSTCLINIT);
        }
        staticMethods = createMethodTable(offset);
    }

    /**
     * Installs the method body for a given method in this class.
     *
     * @param body     the method body
     * @param isStatic specifies whether the method is static or virtual
     */
    public final void installMethodBody(MethodBody body, boolean isStatic) {
        int offset = body.getDefiningMethod().getOffset();
        Object[] methodTable = isStatic ? staticMethods : virtualMethods;
        if (false) Assert.that(offset < methodTable.length);
        methodTable[offset] = body;
        KlassMetadata klassmetadata = getMetadata();
        klassmetadata.setMethodMetadata(isStatic, offset, body.getMetadata());
    }

    /**
     * Get the source file from which the class was compiled.
     *
     * @return the file name or null if it is not available
     */
    public final String getSourceFileName() {
        KlassMetadata metadata = getMetadata();
        return metadata == null ? null : metadata.getSourceFileName();
    }

    /**
     * Get the source file path corresponding to the package path of this class.
     * For example, if this is class is <code>java.lang.Object</code>, and the
     * result of {@link #getSourceFileName()} is <code>"Object.java"</code> then
     * the value returned by this method is <code>"java/lang/Object.java"</code>.
     *
     * @return the source file path of this class or null if the source file is not known
     */
    final String getSourceFilePath() {
        String fileName = getSourceFileName();
        if (fileName != null) {
            int last = name.lastIndexOf('.');
            if (last >= 0) {
                fileName = name.substring(0, last+1).replace('.', '/') + fileName;
            }
        }
        return fileName;
    }

    /*---------------------------------------------------------------------------*\
     *                     Interface closure computation                         *
    \*---------------------------------------------------------------------------*/

    /**
     * A zero-length table of interface method to vtable offset mappings.
     */
    private static short[][] NO_INTERFACE_VTABLE_MAPS = {};

    /**
     * Adds the elements of <code>interfaces</code> to <code>closure</code>
     * that are not already in it. For each interface added, this method
     * recurses on the interfaces implemented by the added interface.
     *
     * @param closure     a collection of interfaces
     * @param interfaces  the array of interfaces to add to <code>closure</code>
     */
    private static void addToInterfaceClosure(SquawkVector closure, Klass[] interfaces) {
        for (int i = 0; i != interfaces.length; ++i) {
            Klass iface = interfaces[i];
            if (!closure.contains(iface)) {
                closure.addElement(iface);
                if (iface.interfaces.length != 0) {
                    addToInterfaceClosure(closure, iface.interfaces);
                }
            }
        }
    }

    /**
     * Computes the closure of interfaces that are implemented by this class
     * excluding those that are implemented by the super class(es). The
     * {@link #interfaces} and {@link #interfaceVTableMaps} are initialized as a
     * result of this computation.
     *
     * @param   cfInterfaces  the interfaces specified in the class file
     */
    private void setInterfaces(Klass[] cfInterfaces) {
        if (isInterface() || isAbstract()) {
            interfaces = cfInterfaces;
            interfaceVTableMaps = NO_INTERFACE_VTABLE_MAPS;
            return;
        }

        /*
         * Compute the closure of interfaces implied by the class file interfaces
         */
        SquawkVector closure = new SquawkVector(cfInterfaces.length);
        addToInterfaceClosure(closure, cfInterfaces);

        /*
         * Add all the interfaces implemented by the abstract class(es) in
         * the super class hierarchy up until the first non-abstract class
         * in the hierarchy. This is required so that the 'interfaceVTableMaps'
         * table for this class also includes the methods implemented by
         * abstract super classes (which have no such table).
         */
        Klass superClass = getSuperclass();
        while (superClass != null && superClass.isAbstract()) {
            addToInterfaceClosure(closure, superClass.interfaces);
            superClass = superClass.getSuperclass();
        }

        /*
         * Remove interfaces implemented by the non-abstract super class(es)
         */
        while (superClass != null) {
            if (!superClass.isAbstract()) {
                Klass[] superInterfaces = superClass.interfaces;
                for (int i = 0 ; i < superInterfaces.length ; i++) {
                    closure.removeElement(superInterfaces[i]);
                }
            }
            superClass = superClass.getSuperclass();
        }

        if (closure.isEmpty()) {
            interfaces = Klass.NO_CLASSES;
            interfaceVTableMaps = NO_INTERFACE_VTABLE_MAPS;
        } else {
            interfaces = new Klass[closure.size()];
            closure.copyInto(interfaces);
            interfaceVTableMaps = new short[closure.size()][];
            for (int i = 0 ; i < interfaces.length ; i++) {
                Klass iface = interfaces[i];
                int count = iface.getMethodCount(false);
                short[] vtableMap = interfaceVTableMaps[i] = new short[count];
                for (int index = 0 ; index < count ; index++) {
                    Method ifaceMethod = iface.getMethod(index, false);
                    Method implMethod = lookupMethod(
                                                      ifaceMethod.getName(),
                                                      ifaceMethod.getParameterTypes(),
                                                      ifaceMethod.getReturnType(),
                                                      null,
                                                      false
                                                    );
                    // A method implementing an interface method must be public
                    int offset;
                    if (implMethod == null || !implMethod.isPublic() || implMethod.isAbstract()) {
                        offset = MethodOffsets.virtual$java_lang_Object$abstractMethodError;
                    } else {
                        offset = implMethod.getOffset();
                        if (false) Assert.that((offset & 0xFFFF) == offset && (short)offset != -1);
                    }
                    vtableMap[index] = (short)offset;
                }
            }
        }
    }

    /*---------------------------------------------------------------------------*\
     *                        Method and field lookup                            *
    \*---------------------------------------------------------------------------*/
         
    /**
     * Finds the <code>Method</code> object representing a method in
     * this class's hierarchy. This method returns null if the method does
     * not exist.
     *
     * @param   name           the name of the method
     * @param   parameterTypes the parameter types of the method
     * @param   returnType     the return type of the method
     * @param   currentClass   the class context of this lookup or null if
     *                         there is no current class context
     * @param   isStatic       specifies a search for a static or virtual method
     * @return  the method that matches the given signature or null if there
     *                         is no match
     */
    public final Method lookupMethod(
                                String  name,
                                Klass[] parameterTypes,
                                Klass   returnType,
                                Klass   currentClass,
                                boolean isStatic
                              ) {
        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return null;
        }

        SymbolParser parser = metadata.getSymbolParser();
        int category = isStatic ? SymbolParser.STATIC_METHODS : SymbolParser.VIRTUAL_METHODS;
        int mid = parser.lookupMember(category, name, parameterTypes, returnType);
        if (mid != -1) {
            Method method = new Method(metadata, mid);
            if (isAccessibleFrom(method, currentClass)) {
                return method;
            }
        }

        /*
         * Recurse to superclass. This is done even for static method lookup
         * except when looking for <clinit> or <init>. Note that the methods
         * of java.lang.Object are searched when this is an interface as
         * these methods can be invoked via an invokeinterface instruction.
         */
        Klass superClass = isInterface() ? Klass.OBJECT : getSuperclass();
        if (superClass != null && !name.equals("<init>") && !name.equals("<clinit>")) {
            Method method = superClass.lookupMethod(name, parameterTypes, returnType, currentClass, isStatic);
            if (method != null) {
                return method;
            }
        }

        /*
         * Check implemented interfaces if this is an interface class
         */
        if (!isStatic && interfaces != null) {
            for (int i = 0; i != interfaces.length; i++) {
                Method method = interfaces[i].lookupMethod(name, parameterTypes, returnType, currentClass, false);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds the <code>Method</code> object representing a method in
     * this class, given an index in the vtable. This method returns null if the method, 
     * or metadata for the method does not exist.
     *
     * Use {@link getMethod} when you have the index of the methods defined in this class.
     * Note that there's not much point in doing this for static method - the index into the stable
     * and the index into the metadata are the same.
     *
     * @param offset the vtable offset
     * @return Method
     */
    public final Method lookupVirtualMethod(int offset) {
        Object[] table = virtualMethods;
        if (offset >= table.length) {
            return null;
        }
        
        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return null;
        }

        SymbolParser parser = metadata.getSymbolParser();
        int category = SymbolParser.VIRTUAL_METHODS;
        int mid = parser.lookupMethod(category, offset);
        if (mid >= 0) {
            return new Method(metadata, mid);
        }
        return null;
    }

    /**
     * Finds the <code>Field</code> object representing a field in
     * this class's hierarchy. This method returns null if the field does
     * not exist.
     *
     * @param   name      the name of the field
     * @param   type      the type of the field
     * @param   isStatic  specifies a search for a static or instance field
     * @return  the field that matches the given signature or null if there
     *                    is no match
     */
    public final Field lookupField(String name, Klass type, boolean isStatic) {
        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return null;
        }
        SymbolParser parser = metadata.getSymbolParser();
        final int category = isStatic ? SymbolParser.STATIC_FIELDS : SymbolParser.INSTANCE_FIELDS;
        int mid = parser.lookupMember(category, name, Klass.NO_CLASSES, type);
        if (mid != -1) {
            return new Field(metadata, mid);
        }

        /*
         * Recurse to superclass. This is done even for static field lookup.
         */
        Klass superClass = getSuperclass();
        if (superClass != null) {
            return superClass.lookupField(name, type, isStatic);
        }
        return null;

    }

    /**
     * Gets the metadata for this class that contains the symbolic information
     * for its fields and methods. This can only be called on a non-synthetic
     * class that has been loaded.
     *
     * @return the metadata for this class
     */
    private KlassMetadata getMetadata() {

        return getMetadata0();

////        if (VM.isHosted()) {
////            return getMetadata0();
////        }
////        return null;

    }
    
    /**
     * Gets the metadata for this class that contains the symbolic information
     * for its fields and methods. This can only be called on a non-synthetic
     * class that has been loaded.
     *
     * @return the metadata for this class
     */
    private KlassMetadata getMetadata0()

//        throws HostedPragma

    {
        if (isSynthetic() || isArray()) {
            return null;
        }
        return VM.getCurrentIsolate().getLeafSuite().getMetadata(this);
    }

    /**
     * Gets the number of fields declared by this class.
     *
     * @param   isStatic  specifies whether to count static or instance fields
     * @return  the number of static or instance fields (as determined by
     *                    <code>isStatic</code>) declared by this class
     */
    public final int getFieldCount(boolean isStatic) {
        int category = isStatic ? SymbolParser.STATIC_FIELDS : SymbolParser.INSTANCE_FIELDS;
        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return 0;
        }
        return metadata.getSymbolParser().getMemberCount(category);
    }

    /**
     * Gets a field declared by this class based on a given field table index.
     *
     * @param  index    the index of the desired field
     * @param  isStatic specifies whether or not the desired field is static
     * @return the field at <code>index</code> in the table of static or
     *                  instance fields (as determined by <code>isStatic</code>)
     *                  declared by this class
     */
    public final Field getField(int index, boolean isStatic) {
        int category = isStatic ? SymbolParser.STATIC_FIELDS : SymbolParser.INSTANCE_FIELDS;
        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return null;
        }
        int fid = metadata.getSymbolParser().getMemberID(category, index);
        return new Field(metadata, fid);
    }

    /**
     * Gets the number of methods declared by this class.
     *
     * @param   isStatic  specifies whether to count static or virtual methods
     * @return  the number of static or virtual methods (as determined by
     *                    <code>isStatic</code>) declared by this class
     */
    public final int getMethodCount(boolean isStatic) {
        int category = isStatic ? SymbolParser.STATIC_METHODS : SymbolParser.VIRTUAL_METHODS;
        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return 0;
        }
        return metadata.getSymbolParser().getMemberCount(category);
    }

    /**
     * Gets a method declared by this class based on a given method table index.
     *
     * @param  index    the index of the desired method
     * @param  isStatic specifies whether or not the desired method is static
     * @return the method at <code>index</code> in the table of static or
     *                  virtual methods (as determined by <code>isStatic</code>)
     *                  declared by this class
     */
    public final Method getMethod(int index, boolean isStatic) {
        int category = isStatic ? SymbolParser.STATIC_METHODS : SymbolParser.VIRTUAL_METHODS;
        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return null;
        }
        SymbolParser parser = metadata.getSymbolParser();
        if (parser.getMemberCount(category) <= index) {
            return null;
        }
        int mid = parser.getMemberID(category, index);
        return new Method(metadata, mid);
    }
    
    /**
     * Looks up the method's index in the klass. Returns the index or -1.
     *
     * @param method the method look for (Can be real byteocde array, or Method, depending on klass state.
     * @param isStatic If true, look up the index in the static method table, otherwise look up in the virtual method table.
     * @return Returns the methods index, or -1 if the method is not in the specified method table
     */
    public final int getMethodIndex(Object method, boolean isStatic) {
        Object[] methods = isStatic ? staticMethods : virtualMethods;
        for (int i = 0; i < methods.length; i++) {
            if (methods[i] == method) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gets the bytecode array for the given Method object
     *
     * @param method the Method to look for
     * @return the bytecode array object for the Method
     */
    public final Object getMethodObject(Method method) {
        if (false) Assert.that(getState() >= Klass.STATE_CONVERTED);
        if (false) Assert.that(method.getOffset() != ILLEGAL_METHOD_OFFSET);
        Object[] table = (method.isStatic() ? staticMethods : virtualMethods);
//        if (method.getOffset() < 0 || method.getOffset() >= table.length) {
//            VM.println("method " + method.getName() + " has wierd offset " + method.getOffset() + " in " + this);
//        }
        return table[method.getOffset()];
    }
    
    /**
     * Tests if the given method is the designated "missing method".
     *
     * @param  method   the method to check
     * @param  isStatic specifies whether or not the desired method is static
     * @return true if method is the missing method method
     */
    public static boolean isMissingMethodObject(Object method, boolean isStatic) {
        Object match;
        if (isStatic) {
            match = OBJECT.staticMethods[MethodOffsets.java_lang_Object$missingMethodError];
        } else {
            match = OBJECT.virtualMethods[MethodOffsets.virtual$java_lang_Object$abstractMethodError];
        }
        return method == match;
    }
    
    private Method findMethodDynamic(Object body)

//        throws HostedPragma

    {
        if (body instanceof MethodBody) {
            MethodBody mbody = (MethodBody)body;
            if (mbody.getDefiningClass() == this) {
                return mbody.getDefiningMethod();
            }
        }
        return null;
    }
    
    /**
     * Searches for the symbolic method declaration corresponding to a given method body
     * that was defined by this class.
     *
     * @param body   the method body for which the symbolic info is requested
     * @return the symbolic info for <code>body</code> or null if it is not available
     */
    Method findMethod(Object body) {
        if (ENABLE_DYNAMIC_CLASSLOADING || VM.isHosted()) {
            Method result = findMethodDynamic(body);
            if (result != null) {
                return result;
            }
        }

        KlassMetadata metadata = getMetadata();
        if (metadata == null) {
            return null;
        }

        if (VM.asKlass(NativeUnsafe.getObject(body, HDR.methodDefiningClass)) != this) {
            return null;
        }

        int methodID = -1;
        SymbolParser parser = metadata.getSymbolParser();
        int index = getMethodIndex(body, false);
        if (index >= 0) {
            methodID = parser.lookupMethod(SymbolParser.VIRTUAL_METHODS, index);
        }
        
        if (methodID < 0) {
            index = getMethodIndex(body, true);
            if (index >= 0) {
                methodID = parser.lookupMethod(SymbolParser.STATIC_METHODS, index);
            }
        }

        if (methodID >= 0) {
            return new Method(metadata, methodID);
        }
        return null;
    }

    /**
     * Test an instance oop map bit.
     *
     * @param wordIndex the word index into the instance
     * @return whether the instance word at the given index represents a reference
     */
    final boolean isInstanceWordReference(int wordIndex) throws ForceInlinedPragma {
        return isInstanceWordReference(this, wordIndex);
    }

    static boolean isInstanceWordReference(Klass klass, int wordIndex) {
        if (false) Assert.that(wordIndex < getInstanceSize(klass));
        UWord word;
        if (Klass.getInstanceSize(klass) > HDR.BITS_PER_WORD) {
            word = klass.oopMap[wordIndex / HDR.BITS_PER_WORD];
        } else {
            if (false) Assert.that(klass.oopMap == null);
            word = klass.oopMapWord;
        }
        UWord bit = UWord.fromPrimitive(1 << (wordIndex % HDR.BITS_PER_WORD));
        return word.and(bit).ne(UWord.zero());
    }


    /*---------------------------------------------------------------------------*\
     *                        Object table manipulation                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Set the object table.
     *
     * @param objects the object array
     */
    public final void setObjectTable(Object[] objects) {
        this.objects = objects;
    }

    /**
     * Get an object from the object table.
     *
     * @param index the index into the table
     * @return the result
     */
    public final Object getObject(int index) {
        return objects[index];
    }

    /**
     * Gets the index of a given object in this object table of this class.
     *
     * @param object  the object to search for
     * @return the index of <code>object</code> in this class' object table or -1 if it doesn't exist
     */
    public final int getObjectIndex(Object object) {
        for (int i = 0; i != objects.length; ++i) {
            if (objects[i] == object) {
                return i;
            }
        }
        return -1;
    }

    /*---------------------------------------------------------------------------*\
     *                               hashcode                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Returns a hashcode for this class.
     *
     * @return  a hashcode for this class
     */
    public final int hashCode() {
        return id;
    }

    /**
     * For completeness, define identity equals().
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        return this == obj;
    }

    /*---------------------------------------------------------------------------*\
     *                          Application startup                              *
    \*---------------------------------------------------------------------------*/

    /**
     * Call this class's <code>public static void main(String[])</code> method
     * if it is defined.
     *
     * @param  args  the arguments to be passed to the invocation
     * @throws NotInlinedPragma as this method saves the current frame pointer
     */
    public final void main(String[] args) throws NotInlinedPragma {
        int index = getMainIndex();
        if (index >= 0) {
            if (false) Assert.that(GC.getKlass(staticMethods[index]) == Klass.BYTECODE_ARRAY);
            VMThread thread = VMThread.currentThread();
            thread.setAppThreadTop(thread.framePointerAsOffset(VM.getFP()));
            VM.callStaticOneParm(this, index, args);
        } else {
            throw new Error("Class "+getName()+" has no main() method");
        }
    }


    /*---------------------------------------------------------------------------*\
     *                           Class initialization                            *
    \*---------------------------------------------------------------------------*/

    /**
     * The queue of classes currently being initialized.
     */
    static KlassInitializationState initializationQueue;

    /**
     * A constant denoting that a class is not initialized.
     */
    private final static int INITSTATE_NOTINITIALIZED = 0;

    /**
     * A constant denoting that class initialization is in progress.
     */
    private final static int INITSTATE_INITIALIZING = 1;

    /**
     * A constant denoting that class initialization completed successfully.
     */
    private final static int INITSTATE_INITIALIZED = 2;

    /**
     * A constant denoting that class initialization failed.
     */
    private final static int INITSTATE_FAILED = 3;

    /**
     * Gets the initialization state. This will be one of the
     * <code>INITSTATE_*</code> values.
     *
     * @return  the initialization state of this class
     */
    private int getInitializationState() {
        if (getClassState() != null) {
            return INITSTATE_INITIALIZED;
        }
        KlassInitializationState istate = initializationQueue;
        while (istate != null && istate.klass != this) {
            istate = istate.next;
        }
        if (istate == null) {
            return INITSTATE_NOTINITIALIZED;
        }
        if (istate.thread == null) {
            return INITSTATE_FAILED;
        }
        return INITSTATE_INITIALIZING;
    }

    /**
     * Sets the class initialization thread for this class, creating the
     * initialization state first if necessary.
     *
     * @param thread  the thread being used to initialize this class
     */
    private void setInitializationState(VMThread thread) {
        KlassInitializationState first = initializationQueue;
        KlassInitializationState istate = first;
        while (istate != null && istate.klass != this) {
            istate = istate.next;
        }
        if (istate == null) {
            istate = new KlassInitializationState();
            istate.next = first;
            istate.thread = thread;
            istate.klass = this;
            istate.classState = GC.newClassState(this);
            initializationQueue = istate;
        } else {
            istate.thread = thread;
        }
    }

    /**
     * Gets the thread being used to initialize this class.
     *
     * @return the thread being used to initialize this class
     */
    private VMThread getInitializationThread() {
        KlassInitializationState istate = initializationQueue;
        while (istate.klass != this) {
            istate = istate.next;
            if (false) Assert.that(istate != null);
        }
        return istate.thread;
    }

    /**
     * Gets the initialization state object for this class.
     *
     * @return the initialization state object for this class
     */
    private Object getInitializationClassState() {
        KlassInitializationState istate = initializationQueue;
        while (istate.klass != this) {
            istate = istate.next;
            if (false) Assert.that(istate != null);
        }
        return istate.classState;
    }

    /**
     * Remove the initialization state object for this class.
     */
    private void removeInitializationState() {
        KlassInitializationState istate = initializationQueue;
        KlassInitializationState prev = null;
        while (istate.klass != this) {
            prev = istate;
            istate = istate.next;
            if (false) Assert.that(istate != null);
        }
        if (prev == null) {
            initializationQueue = istate.next;
        } else {
            prev.next = istate.next;
        }
    }

    /**
     * Convert any entries in a given method table that are
     * instances of <code>MethodBody</code> into the byte arrays with special
     * headers that are the executable form for methods.
     *
     * @param  methods  the table of methods to fixup
     */
    private void fixupMethodTable(Object[] methods) {
        for (int i = 0; i != methods.length; ++i) {
            if (methods[i] instanceof MethodBody) {
                MethodBody body = (MethodBody)methods[i];
                if (false) Assert.that(body.getDefiningClass() == this);
                methods[i] = GC.newMethod(body.getDefiningClass(), body);
            }
/*
//            boolean isStatic = methods == staticMethods;
//            VM.print(name);
//            VM.print(isStatic ? ".smethod[" : ".vmethod[");
//            VM.print(i);
//            VM.print("] = ");
//            VM.printAddress(Address.asAddress(methods[i]));
//
//            Object methodBody = methods[i];
//            Klass definingClass = VM.asKlass(NativeUnsafe.getObject(methodBody, HDR.methodDefiningClass));
//            Method method = definingClass.findMethod(methodBody);
//            VM.print("  ");
//            VM.print(method);
//
//            VM.println("");
*/
        }
    }

    /**
     * Convert any entries in the method tables of this class that are
     * instances of <code>MethodBody</code> into the byte arrays with special
     * headers that are the executable form for methods.
     */
    final void fixupMethodTables() {
        fixupMethodTable(staticMethods);
        fixupMethodTable(virtualMethods);
    }

    /**
     * Get the class state for this class.
     *
     * @return the class state object or null if non exists
     */
    private Object getClassState() {
        return VM.getCurrentIsolate().getClassState(this);
    }

    /**
     * Determines if this class is initialized.
     *
     * @return true if this class is initialized
     */
    public final boolean isInitialized() {
        return getState() == STATE_CONVERTED && (!mustClinit() || getClassState() != null);
    }

    /**
     * Initialize the class such that a new or newInstance() could be performed.
     */
    final void initialiseClass() {
        if (getState() == STATE_ERROR) {
            throw new NoClassDefFoundError(getName());
        }
        if (mustClinit() && getClassState() == null) {
        	initializeInternal();
        }
    }


    final void initializeFinalsWithMetaData() {
        int count = getFieldCount(true);
        for (int i = 0; i != count; ++i) {
            Field field = getField(i, true);
            if (field.hasConstant()) {
                Object cs = getInitializationClassState();
                int offset = field.getOffset() + CS.firstVariable;
                if (field.getType().isPrimitive()) {
                    long value = field.getPrimitiveConstantValue();
                    switch (field.getType().getSystemID()) {
                        case CID.LONG:  NativeUnsafe.setLongAtWord(cs, offset, value); break;

                        case CID.DOUBLE: NativeUnsafe.setLongAtWord(cs, offset, value); break;
                        case CID.FLOAT:  NativeUnsafe.setUWord(cs, offset, UWord.fromPrimitive((int)value)); break;

////                      case CID.DOUBLE:
////                      case CID.FLOAT: throw Assert.shouldNotReachHere();

                        default: NativeUnsafe.setUWord(cs, offset, UWord.fromPrimitive((int)value)); break;
                    }
                } else {
                    String value = field.getStringConstantValue();
                    NativeUnsafe.setObject(cs, offset, value);
                }
            }
        }
    }

            
    /**
     * Initializes this class. This method implements the detailed class
     * initialization procedure described in section 2.17.5 (page 53) of
     * "The Java Virtual Machine Specification - Second Edition".
     *
     * @return the class state object
     * @see   <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/Concepts.doc.html#24237">
     *         The Java Virtual Machine Specification - Second Edition</a>
     */
    final Object initializeInternal() {
        /*
         * Test to see if there was a linkage error.
         */
        if (state == STATE_ERROR) {
            throw new NoClassDefFoundError(name);
        }

        /*
         * Step 1
         */
        synchronized(this) {
            /*
             * Step 2
             */
            if (getInitializationState() == INITSTATE_INITIALIZING) {
                if (getInitializationThread() != VMThread.currentThread()) {
                    do {
                        try {
                            wait();
                        } catch (InterruptedException e) {}
                    } while (getInitializationState() == INITSTATE_INITIALIZING);
                } else {
                    /*
                     * Step 3
                     */
                    return getInitializationClassState();
                }
            }
            /*
             * Step 4
             */
            if (getInitializationState() == INITSTATE_INITIALIZED) {
                return getClassState();
            }
            /*
             * Step 5
             */
            if (getInitializationState() == INITSTATE_FAILED) {
                throw new NoClassDefFoundError(name);
            }
            /*
             * Step 6
             */
            Assert.always(VMThread.currentThread() != null, "Klass.java", 3441);
            setInitializationState(VMThread.currentThread()); // state = INITIALIZING);
        }
        /*
         * Step 7
         */
        if (!isInterface()) {
            if (superType != null && superType.mustClinit() && superType.getInitializationState() != INITSTATE_INITIALIZED) {
                try {
                    superType.initializeInternal();
                } catch(Error ex) {
                    synchronized(this) {
                        setInitializationState(null); // state = FAILED;
                        notifyAll();
                    }
                    throw ex;
                } catch(Throwable ex) {
                    VM.fatalVMError();
                }
            }
        }
        /*
         * Step 8
         */
        try {


            if ((modifiers & Modifier.COMPLETE_RUNTIME_STATICS) != 0) {
                initializeFinalsWithMetaData();
            }

            
            clinit();
            /*
             * Step 9
             */
            synchronized(this) {
                Object cs = getInitializationClassState();
                if (false) Assert.that(NativeUnsafe.getObject(cs, CS.klass) == this);
                VM.getCurrentIsolate().addClassState(cs);
                removeInitializationState(); // state = INITIALIZED;
                notifyAll();
                return cs;
            }
        } catch (Throwable ex) {
            Error err;
            /*
             * Step 10
             */
            if (ex instanceof Error) {
                err = (Error)ex;
            } else {
                ex.printStackTrace();
                err = new Error("ExceptionInInitializer: " + name + ":" + ex);
            }
            /*
             * Step 11
             */
            synchronized(this) {
                setInitializationState(null); // state = FAILED;
                notifyAll();
            }
            throw err;
        }
    }

    /**
     * Determines if class initialization must be performed
     * for this class. Class initialization is required for a class
     * if it or any of it's super classes has a <code>&lt;clinit&gt;</code>
     * method.
     *
     * @return   true if class initialization must be performed
     *           for this class; false otherwise
     */
    public final boolean mustClinit() {
        return Modifier.mustClinit(modifiers);
    }

    /**
     * Used to set up the mustClinit modifier.
     */
    private boolean setMustClinit() {
        if (hasClinit()) {
            return true;
        } else if ((modifiers & Modifier.COMPLETE_RUNTIME_STATICS) != 0) {
            return true;
        } else if (superType == null) {
            return false;
        } else {
            return superType.setMustClinit();
        }
    }

    /**
     * Call this class's <code>&lt;clinit&gt;</code> method if it is defined.
     */
    final void clinit() {
        int index = getClinitIndex();
        if (index >= 0) {
            // Verbose trace.
            if (VM.isVeryVerbose()) {
                  VM.print("[initializing ");
                  VM.print(isInterface() ? "interface " :  "class ");
                  VM.print(name);
                  VM.println("]");
            }
            if (false) Assert.that(GC.getKlass(staticMethods[index]).id == CID.BYTECODE_ARRAY);
            VM.callStaticNoParm(this, index);
        }
    }


    /*---------------------------------------------------------------------------*\
     *                           Bootstrap classes                               *
    \*---------------------------------------------------------------------------*/

    /**
     * The root of the verification type hierarchy.
     *
     * @see  Klass
     */
    public static final Klass TOP;

    /**
     * The root of all single word types.
     */
    public static final Klass ONE_WORD;

    /**
     * The root of all two word types.
     */
    public static final Klass TWO_WORD;

    /**
     * The type for <code>boolean</code>.
     */
    public static final Klass BOOLEAN;

    /**
     * The type for <code>byte</code>.
     */
    public static final Klass BYTE;

    /**
     * The type for <code>char</code>.
     */
    public static final Klass CHAR;

    /**
     * The type for <code>short</code>.
     */
    public static final Klass SHORT;

    /**
     * The type for <code>int</code>.
     */
    public static final Klass INT;
    
    /**
     * The type for <code>float</code>.
     */
    public static final Klass FLOAT;
    
    /**
     * The type for <code>long</code>.
     */
    public static final Klass LONG;

    /**
     * The type for the second word of a <code>long</code> value.
     */
    public static final Klass LONG2;

    /**
     * The type for <code>double</code>.
     */
    public static final Klass DOUBLE;

    /**
     * The type for the second word of a <code>double</code> value.
     */
    public static final Klass DOUBLE2;
    
    /**
     * The type for <code>void</code>.
     */
    public static final Klass VOID;

    /**
     * The root type for all reference types.
     */
    public static final Klass REFERENCE;

    /**
     * The root type for all uninitialized reference types.
     */
    public static final Klass UNINITIALIZED;

    /**
     * The type for <code>this</code> in a constructor before the call to
     * the super constructor.
     */
    public static final Klass UNINITIALIZED_THIS;

    /**
     * The root of the types representing the result of the <i>new</i>
     * bytecode before it has been passed to a constructor.
     */
    public static final Klass UNINITIALIZED_NEW;

    /**
     * The type for <code>null</code>.
     */
    public static final Klass NULL;

    /**
     * The type for <code>java.lang.Object</code>.
     */
    public static final Klass OBJECT;

    /**
     * The type for <code>java.lang.String</code>.
     */
    public static final Klass STRING;

    /**
     * The type for <code>java.lang.Class</code>.
     */
    public static final Klass THROWABLE;

    /**
     * The type for <code>com.sun.squawk.Klass</code>.
     */
    public static final Klass KLASS;

    /**
     * The type for <code>java.lang.Object[]</code>.
     */
    public static final Klass OBJECT_ARRAY;

    /**
     * The type for <code>java.lang.String[]</code>.
     */
    public static final Klass STRING_ARRAY;

    /**
     * The type for <code>boolean[]</code>.
     */
    public static final Klass BOOLEAN_ARRAY;

    /**
     * The type for <code>byte[]</code>.
     */
    public static final Klass BYTE_ARRAY;

    /**
     * The type for <code>char[]</code>.
     */
    public static final Klass CHAR_ARRAY;

    /**
     * The type for <code>short[]</code>.
     */
    public static final Klass SHORT_ARRAY;

    /**
     * The type for <code>int[]</code>.
     */
    public static final Klass INT_ARRAY;

    /**
     * The type for <code>float[]</code>.
     */
    public static final Klass FLOAT_ARRAY;
    
    /**
     * The type for <code>long[]</code>.
     */
    public static final Klass LONG_ARRAY;

    /**
     * The type for <code>double[]</code>.
     */
    public static final Klass DOUBLE_ARRAY;
    
    /**
     * The type for <code>com.sun.squawk.StringOfBytes</code>.
     */
    public static final Klass STRING_OF_BYTES;

    /**
     * The type for a stack chunk.
     */
    public static final Klass LOCAL_ARRAY;

    /**
     * The type for a class state structure.
     */
    public static final Klass GLOBAL_ARRAY;

    /**
     * The type for an array of bytes that is a method.
     */
    public static final Klass BYTECODE_ARRAY;

    /**
     * The type for representing machine addresses.
     */
    public static final Klass ADDRESS;

    /**
     * The type for representing an array of machine addresses.
     */
    public static final Klass ADDRESS_ARRAY;

    /**
     * The type for representing unsigned machine words.
     */
    public static final Klass UWORD;

    /**
     * The type for representing an array of unsigned word addresses.
     */
    public static final Klass UWORD_ARRAY;

    /**
     * The type for representing the directed distance between two machine addresses.
     */
    public static final Klass OFFSET;

    /**
     * Finds one of the bootstrap classes, creating it if necessary.
     *
     * @param   superType  the super type of the bootstrap class
     * @param   name       the name of the class
     * @param   systemID   the predefined system ID for the class
     * @param   modifiers  the modifiers of the class
     * @return             the created class
     */
    private static Klass boot(Klass superType, String name, int systemID, int modifiers) {
        Isolate isolate = VM.getCurrentIsolate();
        Suite bootstrapSuite = isolate.getBootstrapSuite();
        Klass klass = bootstrapSuite.getKlass(systemID);
        if (klass != null) {
            if (false) Assert.that(klass.getSuperType() == superType);
            if (false) Assert.that(klass.getSystemID() == systemID);
            if (false) Assert.that((klass.getModifiers() & modifiers) == modifiers);
            return klass;
        }

        // Should never get here in a non-hosted system as all the bootstrap classes must be in the bootstrap suite
        Assert.always(VM.isHosted(), "Klass.java", 3793);

        return bootHosted(superType, name, systemID, modifiers, bootstrapSuite);
    }

    /**
     * Finds one of the bootstrap classes, creating it if necessary.
     *
     * @param   superType  the super type of the bootstrap class
     * @param   name       the name of the class
     * @param   systemID   the predefined system ID for the class
     * @param   modifiers  the modifiers of the class
     * @param   bootstrapSuite  the bootstrap suite
     * @return             the created class
     */
    private static Klass bootHosted(Klass superType, String name, int systemID, int modifiers, Suite bootstrapSuite) throws HostedPragma {
        Klass klass = getClass(name, systemID);
        if (false) Assert.that(bootstrapSuite.getKlass(systemID) == klass);
        klass.setSuperType(superType);
        klass.updateModifiers(modifiers | klass.getModifiers());
        return klass;
    }

    private final static int none = 0;
    private final static int publik = Modifier.PUBLIC;
    private final static int synthetic = publik | Modifier.SYNTHETIC;
    private final static int synthetic2 = synthetic | Modifier.DOUBLEWORD;
    private final static int primitive = synthetic | Modifier.PRIMITIVE;
    private final static int primitive2 = primitive | Modifier.DOUBLEWORD;
    private final static int squawkarray = publik | Modifier.SQUAWKARRAY;
    private final static int squawkprimitive = Modifier.SQUAWKPRIMITIVE;

    /**
     * Initializes the constants for the bootstrap classes.
     */
    static {
        TOP                = boot(null,          "-T-",                     CID.TOP,               synthetic);
        ONE_WORD           = boot(TOP,           "-1-",                     CID.ONE_WORD,          synthetic); // only used by translator
        TWO_WORD           = boot(TOP,           "-2-",                     CID.TWO_WORD,          synthetic2);// only used by translator

        INT                = boot(ONE_WORD,      "int",                     CID.INT,               primitive);
        BOOLEAN            = boot(INT,           "boolean",                 CID.BOOLEAN,           primitive);
        BYTE               = boot(INT,           "byte",                    CID.BYTE,              primitive);
        CHAR               = boot(INT,           "char",                    CID.CHAR,              primitive);
        SHORT              = boot(INT,           "short",                   CID.SHORT,             primitive);
        FLOAT              = boot(ONE_WORD,      "float",                   CID.FLOAT,             primitive);
        LONG               = boot(TWO_WORD,      "long",                    CID.LONG,              primitive2);
        LONG2              = boot(ONE_WORD,      "-long2-",                 CID.LONG2,             primitive2);
        DOUBLE             = boot(TWO_WORD,      "double",                  CID.DOUBLE,            primitive2);
        DOUBLE2            = boot(ONE_WORD,      "-double2-",               CID.DOUBLE2,           primitive2);
        VOID               = boot(TOP,           "void",                    CID.VOID,              synthetic);

        REFERENCE          = boot(ONE_WORD,      "-ref-",                   CID.REFERENCE,         synthetic); // only used by translator
        UNINITIALIZED      = boot(REFERENCE,     "-uninit-",                CID.UNINITIALIZED,     synthetic); // only used by translator
        UNINITIALIZED_THIS = boot(UNINITIALIZED, "-uninit_this-",           CID.UNINITIALIZED_THIS,synthetic); // only used by translator
        UNINITIALIZED_NEW  = boot(UNINITIALIZED, "-uninit_new-",            CID.UNINITIALIZED_NEW, synthetic); // only used by translator

        OBJECT             = boot(REFERENCE,     "java.lang.Object",        CID.OBJECT,            none);
        STRING             = boot(OBJECT,        "java.lang.String",        CID.STRING,            squawkarray);
        THROWABLE          = boot(OBJECT,        "java.lang.Throwable",     CID.THROWABLE,         none);
        KLASS              = boot(OBJECT,        "com.sun.squawk.Klass",    CID.KLASS,             none);
        NULL               = boot(OBJECT,        "-null-",                  CID.NULL,              synthetic);

        OBJECT_ARRAY       = boot(OBJECT,        "[java.lang.Object",       CID.OBJECT_ARRAY,      synthetic); // only used by translator/mapper
        STRING_ARRAY       = boot(OBJECT,        "[java.lang.String",       CID.STRING_ARRAY,      synthetic);
        BOOLEAN_ARRAY      = boot(OBJECT,        "[boolean",                CID.BOOLEAN_ARRAY,     synthetic); // only used by translator
        BYTE_ARRAY         = boot(OBJECT,        "[byte",                   CID.BYTE_ARRAY,        synthetic);
        CHAR_ARRAY         = boot(OBJECT,        "[char",                   CID.CHAR_ARRAY,        synthetic); // only used by translator
        SHORT_ARRAY        = boot(OBJECT,        "[short",                  CID.SHORT_ARRAY,       synthetic); // only used by translator
        INT_ARRAY          = boot(OBJECT,        "[int",                    CID.INT_ARRAY,         synthetic); // only used by translator
        LONG_ARRAY         = boot(OBJECT,        "[long",                   CID.LONG_ARRAY,        synthetic); // only used by translator
        FLOAT_ARRAY        = boot(OBJECT,        "[float",                  CID.FLOAT_ARRAY,       synthetic); // only used by translator
        DOUBLE_ARRAY       = boot(OBJECT,        "[double",                 CID.DOUBLE_ARRAY,      synthetic); // only used by translator
        
        // Ensure that all the reserved system classes are loaded if running in a hosted environment
        if (VM.isHosted()) {
            initBootstrapClassesHostedEarly();
        }
        /*
         * Special implementation types.
         */
        STRING_OF_BYTES    = boot(STRING,        "com.sun.squawk.StringOfBytes", CID.STRING_OF_BYTES,   squawkarray);
        LOCAL_ARRAY        = boot(OBJECT,        "[-local-",                CID.LOCAL_ARRAY,       synthetic);
        GLOBAL_ARRAY       = boot(OBJECT,        "[-global-",               CID.GLOBAL_ARRAY,      synthetic);
        ADDRESS            = boot(OBJECT,        "com.sun.squawk.Address",  CID.ADDRESS,           squawkprimitive);
        ADDRESS_ARRAY      = boot(OBJECT,        "[com.sun.squawk.Address", CID.ADDRESS_ARRAY,     none);
        UWORD              = boot(OBJECT,        "com.sun.squawk.UWord",    CID.UWORD,             squawkprimitive);
        UWORD_ARRAY        = boot(OBJECT,        "[com.sun.squawk.UWord",   CID.UWORD_ARRAY,       none);
        OFFSET             = boot(OBJECT,        "com.sun.squawk.Offset",   CID.OFFSET,            squawkprimitive);
        BYTECODE_ARRAY     = boot(OBJECT,        "[-bytecode-",             CID.BYTECODE_ARRAY,    synthetic);

        // Ensure that all the reserved system classes are loaded if running in a hosted environment
        if (VM.isHosted()) {
            initBootstrapClassesHostedLate();
        }

        if (false) Assert.that(Klass.LONG2.isPrimitive());
        if (false) Assert.that(Klass.DOUBLE2.isPrimitive());
    }

     /**
     * Ensure that all the reserved system classes are loaded if running in a hosted environment.
     */
    private static void initBootstrapClassesHostedEarly() throws HostedPragma {
        // Base classes have to be created before array classes
        boot(ONE_WORD,      "-local-",                     CID.LOCAL,             synthetic);
        boot(ONE_WORD,      "-global-",                    CID.GLOBAL,            synthetic);
        boot(INT,           "-bytecode-",                  CID.BYTECODE,          synthetic);
    }

    void bootLoad(TranslatorInterface translator) {
        if (!isArray() && !isSynthetic()) {
            translator.load(this);
        }
        if (isArray() && virtualMethods == null) {
            virtualMethods = superType.virtualMethods;
        }
    }
    
    /**
     * Ensure that all the reserved system classes are loaded if running in a hosted environment.
     */
    private static void initBootstrapClassesHostedLate() throws HostedPragma {
        // these classes have to be created for boostrap, but not looked up at runtime:
        boot(OBJECT,        "[[-global-",                  CID.GLOBAL_ARRAYARRAY, synthetic);
        boot(OBJECT,        "com.sun.squawk.NativeUnsafe", CID.NATIVEUNSAFE,      none);

        // load all system classes:
        try {
            Isolate isolate = VM.getCurrentIsolate();
            Suite bootstrapSuite = isolate.getBootstrapSuite();
            TranslatorInterface translator = isolate.getTranslator();
            for (int systemID = 0; systemID <= CID.LAST_SYSTEM_ID; systemID++) {
               switch (systemID) {

//                   case CID.FLOAT:
//                   case CID.FLOAT_ARRAY:
//                   case CID.DOUBLE:
//                   case CID.DOUBLE2:
//                   case CID.DOUBLE_ARRAY:
//                       break;

                    default:
                        bootstrapSuite.getKlass(systemID).bootLoad(translator);
                        break;
                }
            }
        } catch (NoClassDefFoundError noClassDefFoundError) {
            // these are fatal - don't try to defer in romizer:
            noClassDefFoundError.printStackTrace();
            throw new RuntimeException("Klass initialization failed: " + noClassDefFoundError);
        }
    }

    /**
     * Gets a class corresponding to a given name. If the class cannot be found via the leaf suite
     * of the current isolate, it will be created and installed in the leaf suite. If the class
     * represents an array, then this method also ensures that the class representing the component
     * type of the array also exists.<p>

     * If the value of <code>isFieldDescriptor</code> is true, then the format
     * of <code>name</code> is as specified in the JVM specification for
     * <a href="http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html#1169">field descriptors</a>.
     * Otherwise, the name is in Squawk {@link Klass#getInternalName() internal}
     * format.<p>
     *
     * @param   name               the name of the class to get
     * @param   isFieldDescriptor  if true, then <code>name</code> is in the format described above
     * @return  klass
     */
    public static Klass getClass(String name, boolean isFieldDescriptor) {

        // Convert a valid field descriptor into a class name in internal format
        if (isFieldDescriptor) {
            int dimensions = 0;
            while (name.charAt(dimensions) == '[') {
                dimensions++;
            }
            char first = name.charAt(dimensions);
            if (first != 'L') {
                if (false) Assert.that((name.length() - dimensions) == 1, "illegal field descriptor");
                Klass klass;
                switch (first) {
                    case 'I': klass = Klass.INT;     break;
                    case 'J': klass = Klass.LONG;    break;

                    case 'F': klass = Klass.FLOAT;   break;
                    case 'D': klass = Klass.DOUBLE;  break;

////                    case 'F':
////                    case 'D':
////                        Assert.shouldNotReachHere("floating point is not supported in this configuration: " + name);
////                        return null;

                    case 'Z': klass = Klass.BOOLEAN; break;
                    case 'C': klass = Klass.CHAR;    break;
                    case 'S': klass = Klass.SHORT;   break;
                    case 'B': klass = Klass.BYTE;    break;
                    case 'V': klass = Klass.VOID;    break;
                    default:
                        if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("Klass.java", 3993);
                        return null;
                }
                if (dimensions != 0) {
                    return getClass(name.substring(0, dimensions) + klass.getInternalName(), -1);
                } else {
                    return klass;
                }
            } else {
                if (false) Assert.that(name.charAt(name.length()-1) == ';', "illegal field descriptor");

                /*
                 * Strip the 'L' and ';'
                 */
                String baseName = name.substring(dimensions + 1, name.length() - 1);

                /*
                 * Convert from JVM internal form to Squawk internal form
                 */
                baseName = baseName.replace('/', '.');

                if (dimensions != 0) {
                    name = name.substring(0, dimensions) + baseName;
                } else {
                    name = baseName;
                }
                return getClass(name, -1);
            }
        } else {
            return getClass(name, -1);
        }
    }

    /**
     * Look up klass in current suite or one of it's parents.
     *
     * @param   name       the name of the class to lookup.
     * @return the Klass instance for <code>name</code>, or null if it doesn't exists
     */
    public static Klass lookupKlass(String name) {
        Isolate isolate = VM.getCurrentIsolate();
        Suite suite = isolate.getLeafSuite();

        if (suite == null) {
            suite = isolate.getBootstrapSuite();
        }
        
        return suite.lookup(name); 
    }

    /**
     * Lookup or create class. If the class does not already exist, then a new Klass instance is
     * created and returned.
     * 
     * @see #getClass(String, boolean)
     *
     * @param   name       the name of the class to get
     * @param   systemID   the system wide identifier reserved for the class or -1 if it doesn't have one
     * @return the Klass instance for <code>name</code>
     */
    private static Klass getClass(String name, final int systemID) {
        Isolate isolate = VM.getCurrentIsolate();

        Suite suite = isolate.getLeafSuite();
        if (suite == null) {
            suite = isolate.getBootstrapSuite();
        }

        /*
         * Look up first
         */
        Klass klass = suite.lookup(name);
        if (klass == null) {
            /*
             * Now have to create the class
             */
            if (name.charAt(0) == '[') {
                String componentName = name.substring(1);
                Klass componentType = getClass(componentName, -1);
                int suiteID = (systemID == -1 ? suite.getNextAvailableClassNumber() : systemID);
                klass = new Klass(name, componentType, suiteID, systemID != -1);
            } else {
            	if (suite.shouldThrowNoClassDefFoundErrorFor(name)) {
            		throw new NoClassDefFoundError(name);
            	}
                int suiteID = (systemID == -1 ? suite.getNextAvailableClassNumber() : systemID);
                klass = new Klass(name, null, suiteID, systemID != -1);
            }

            suite.installClass(klass);
        }
        return klass;
    }

    /*---------------------------------------------------------------------------*\
     *                           Double word types                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the type representing the second word of a double word type.
     *
     * @param   type  a double word type
     * @return  the type of the second word type of <code>type</code>
     */
    public static Klass getSecondWordType(Klass type) {

        if (type == DOUBLE) {
            return DOUBLE2;
        } else 

        {
            if (false) Assert.that(type == LONG); // Is not double word type.
            return LONG2;
        }
    }

    /*---------------------------------------------------------------------------*\
     *                          KlassInitializationState                         *
    \*---------------------------------------------------------------------------*/

    static class KlassInitializationState {
        KlassInitializationState next;
        VMThread thread;
        Klass klass;
        Object classState;
    }

}
