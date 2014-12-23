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

import com.sun.javadoc.*;
import com.sun.squawk.builder.*;
import java.lang.reflect.Modifier;

/**
 * This class produces source, javadoc and class files for the components
 * (i.e. classes, fields and methods) in a suite that can be bound to at load time.
 * The input to this process is a textual description of the suite as produced
 * by {@link Suite#printAPI} and the classpath and sourcepath used to build the suite.
 *
 */
// TODO: get rid of the SuppressWarnings annotations
@SuppressWarnings(value={"unchecked"})
public class MakeAPI extends Command {

    /**
     * File name extension that identifies a Suite, includes the '.'.
     * 
     * Cloned from com.sun.squawk.Suite.
     */
    public static final String SUITE_FILE_EXTENSION = ".suite";

    /**
     * File name extension that identifies a Suite's api, includes the '.'.
     * 
     * Cloned from com.sun.squawk.Suite.
     */
    public static final String SUITE_FILE_EXTENSION_API = ".api";

    /**
     * File name extension that identifies a Suite's metadata, includes the '.'.
     * 
     * Cloned from com.sun.squawk.Suite.
     */
    public static final String SUITE_FILE_EXTENSION_METADATA = ".metadata";
    
    static final int PUBLIC           = Modifier.PUBLIC;
    static final int PROTECTED        = Modifier.PROTECTED;
    static final int PACKAGE_PRIVATE  = 0;
    static final int PRIVATE          = Modifier.PRIVATE;

    static int order(int modifiers) {
        int modifier = modifiers & (PUBLIC | PROTECTED | PRIVATE | PACKAGE_PRIVATE);
        switch (modifier) {
            case PUBLIC:          return 3;
            case PROTECTED:       return 2;
            case PACKAGE_PRIVATE: return 1;
            case PRIVATE:         return 0;
            default:
                throw new RuntimeException("invalid modifiers");
        }
    }

    final class Constructors {

        abstract class Constructor {

            /**
             * Gets the source code declaration for this constructor.
             *
             * @param  superConstructor  the constructor this constructor will call
             * @return the source code declaration for this constructor
             */
            abstract String declaration(Constructor superConstructor);

            /**
             * Determines if this constructor's access level is equal to or greater than a given level
             *
             * @param modifier  a Java accessibility modifier
             * @return true if this constructor's access level is equal to or greater than that specified by <code>modifier</code>
             */
            abstract boolean isAccessible(int modifier);

            /**
             * Gets the actual parameters for a generated call to this method.
             *
             * @return comma separated list of default values for the parameters types of this constructor
             */
            abstract String getActualParameters();

            /**
             * Prints the source for this constructor.
             *
             * @param sf  the source file being generated
             * @param map map from class names to constructors
             */
            void print(SourceFile sf, Map map) {
                Constructor superConstructor = null;
                ClassDoc sdoc = cdoc.superclass();
                if (sdoc != null) {
                    Constructors superConstructors = (Constructors) map.get(sdoc.qualifiedName());
                    if (superConstructors != null) {
                        boolean inSamePackage = cdoc.containingPackage() == superConstructors.cdoc.containingPackage();
                        superConstructor = superConstructors.get(inSamePackage ? PACKAGE_PRIVATE : PROTECTED);
                    }
                }
                sf.enter(declaration(superConstructor));
                if (superConstructor != null) {
                    sf.println("super(" + superConstructor.getActualParameters() + ");");
                    sf.println("throw new RuntimeException();");
                }
                sf.leave();
            }
        }

        /**
         * Encapsulates a constructor declared in the source code.
         */
        class SourceConstructor extends Constructor {
            final ConstructorDoc source;
            SourceConstructor(ConstructorDoc source) {
                this.source = source;
            }

            /**
             * {@inheritDoc}
             */
            boolean isAccessible(int modifier) {
                return order(modifier) <= order(source.modifierSpecifier());
            }

            /**
             * {@inheritDoc}
             */
            String declaration(Constructor superConstructor) {
                return formatMethodDeclaration(source);
            }

            /**
             * {@inheritDoc}
             */
            String getActualParameters() {
                Parameter[] parameters = source.parameters();
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i != parameters.length; ++i) {
                    buf.append(MakeAPI.getDefaultValue(parameters[i].type()));
                    if (i != parameters.length - 1) {
                        buf.append(", ");
                    }
                }
                return buf.toString();
            }

            /**
             * {@inheritDoc}
             */
            void print(SourceFile sf, Map map) {
                sf.printDoc(source, MakeAPI.this);
                super.print(sf, map);
            }
        }

        /**
         * Encapsulates a synthesized constructor.
         */
        class SyntheticConstructor extends Constructor {
            int modifier;

            SyntheticConstructor() {
                this.modifier = PRIVATE;
            }

            /**
             * {@inheritDoc}
             */
            boolean isAccessible(int modifier) {
                if (order(modifier) > order(this.modifier)) {
                    this.modifier = modifier;
                }
                return true;
            }

            /**
             * {@inheritDoc}
             */
            String declaration(Constructor superConstructor) {
                StringBuffer decl = new StringBuffer(Modifier.toString(modifier) + ' ' + baseName(cdoc.name()) + "()");
                if (superConstructor instanceof SourceConstructor) {
                    ClassDoc[] exceptions = ( (SourceConstructor) superConstructor).source.thrownExceptions();
                    if (exceptions.length != 0) {
                        decl.append(" throws ");
                        for (int i = 0; i != exceptions.length; ++i) {
                            decl.append(ref(exceptions[i], false, false));
                            if (i != exceptions.length - 1) {
                                decl.append(", ");
                            }
                        }
                    }
                }

                return decl.toString();
            }

            /**
             * {@inheritDoc}
             */
            String getActualParameters() {
                return "";
            }

            /**
             * {@inheritDoc}
             */
            void print(SourceFile sf, Map map) {
                sf.println("");
                sf.println("/**");
                sf.println(" * <b>Synthesized constructor to enable compilation of generated API source code.");
                sf.println(" * May not really exist and should never be called.<b>");
                sf.println(" */");
                super.print(sf, map);
            }

        }


        final List cons = new ArrayList();
        final ClassDoc cdoc;
        SyntheticConstructor defolt;

        Constructors(ClassDoc cdoc) {
            this.cdoc = cdoc;
        }

        void add(ConstructorDoc c) {
            cons.add(new SourceConstructor(c));
        }

        void addDefault(int modifier) {
            if (defolt == null) {
                defolt = new SyntheticConstructor();
                cons.add(defolt);
            }
            defolt.isAccessible(modifier);
        }

        Constructor get(int modifier) {
            for (Iterator i = cons.iterator(); i.hasNext(); ) {
                Constructor c = (Constructor)i.next();
                if (c.isAccessible(modifier)) {
                    return c;
                }
            }
            return null;
        }

        /**
         * Prints the source for this set of constructors.
         *
         * @param sf  the source file being generated
         * @param map map from class names to constructors
         */
        void print(SourceFile sf, Map map) {
            for (Iterator i = cons.iterator(); i.hasNext(); ) {
                Constructor c = (Constructor) i.next();
                c.print(sf, map);
            }
        }
    }

    /**
     * The object used to generate the source from which the final javadoc will be created.
     */
    private RootDoc root;

    /**
     * The suite for which source is currently being generated.
     */
    private Suite suite;

    /**
     * Map from class names to constructors.
     */
    private Map constructorsMap;

    /**
     * The class for which source is currently being generated.
     */
    private Suite.Class currentClass;

    /**
     * The classes referenced from within the source file currently being generated.
     */
    private Map currentSourceRefs;

    /**
     * Where the generated source, classes and javadoc will be dumped.
     */
    private File baseDir;

    /**
     * Specifies if javadoc should be created.
     */
    private boolean nodoc;

    /**
     * The args to be passed onto the 2nd invocation of javadoc.
     */
    private List javadocArgs;

    /**
     * The path to use when searching for Java source files.
     */
    private String sourcepath;

    /**
     * The path to use when searching for Java class files.
     */
    private String classpath;

    public MakeAPI(Build env) {
        super(env, "makeapi");
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "creates source, classes and HTML describing API of a suite";
    }

    /**
     * Gets the package name from a fully qualified class name.
     *
     * @param className  a fully qualified class name
     * @return the package name component of <code>className</code>
     */
    private String getPackageName(String className) {
        String packageName;
        int index = className.lastIndexOf('.');
        if (index < 0) {
            packageName = "";
        } else {
            packageName = className.substring(0, index);

        }
        return packageName;
    }

    /**
     * Gets the set of packages containing a given set of class names,
     *
     * @param classNames  a set of class names
     * @return the set of package names corresponding to <code>classNames</code>
     */
    private Set extractPackages(Set classNames) {
        Set packages = new TreeSet();
        for (Iterator i = classNames.iterator(); i.hasNext(); ) {
            String className = (String)i.next();
            String packageName = getPackageName(className);
            if (packageName.length() == 0) {
                System.err.println("warning: class " + className + " in anonymous package will not be documented");
            } else {
                packages.add(packageName);
            }
        }
        return packages;
    }

    private void run() {

        Build.clear(baseDir, false);

        Map classes = suite.getClasses();
        Set packages = extractPackages(classes.keySet());

        File srcDir = makeSource(classes.values(), packages);
        File javadocDir = nodoc ? null : makeJavadoc(packages, srcDir);
        File classesDir = new File(baseDir, "classes");
        ArrayList extraArgs = new ArrayList<String>();
//        extraArgs.add("-source");
//        extraArgs.add("1.4");
//        extraArgs.add("-target");
//        extraArgs.add("1.4");

        env.javac(classpath, classpath, baseDir, new File[] {srcDir}, true, extraArgs, false, true);

        File classesJar = new File(baseDir, "classes.jar");
        File apiJar = new File(baseDir, suite.name + "_rt.jar");
        if (!classesJar.renameTo(apiJar)) {
            throw new BuildException("could not rename " + classesJar + " to " + apiJar);
        }

        System.out.println("API tool generated these files/directories:");
        System.out.println("    " + srcDir.getAbsolutePath());
        System.out.println("    " + classesDir.getAbsolutePath());
        System.out.println("    " + apiJar.getAbsolutePath());
        if (!nodoc) {
            System.out.println("    " + javadocDir.getAbsolutePath());
        }
    }

    /**
     * Generates the source files corresponding to a set of classes in the current suite.
     *
     * @param classes   the classes to generate source for
     * @param packages  the union of all packages specified by <code>classes</code>
     * @return the base directory in which the sources were generated
     */
    private File makeSource(Collection classes, Set packages) {
        List args = new ArrayList();
        args.add("-private");
        args.add("-breakiterator");
        args.add("-sourcepath");
        args.add(sourcepath);
        if (classpath != null) {
            args.add("-bootclasspath");
            args.add(classpath);
        }
        args.add("-doclet");
        args.add(this.getClass().getName());

        args.addAll(packages);

        client = this;
        env.getJavaCompiler().javadoc((String[])args.toArray(new String[args.size()]), true);
        client = null;

        constructorsMap = makeConstructorsMap(classes);

        File srcDir = new File(baseDir, "src");
        for (Iterator iterator = classes.iterator(); iterator.hasNext(); ) {
            Suite.Class klass = (Suite.Class)iterator.next();
            ClassDoc cdoc = root.classNamed(klass.name);
            if (cdoc == null) {
                throw new BuildException("cannot find source for class " + klass.name);
            }

            // Nested classes will be processed as part of their outer class
            if (cdoc.containingClass() != null) {
                continue;
            }

            SourceFile sf = new SourceFile(cdoc);
            env.log(env.verbose, "generating source file " + sf);
            printClass(sf, cdoc, klass, false);
            sf.dump(srcDir, currentSourceRefs.values());
        }
        return srcDir;
    }


    private Map makeConstructorsMap(Collection classes) {
        Map map = new HashMap();

        getDefinedConstructors(classes, map);
        addSynthesizedConstructors(classes, map);
        return map;
    }

    private void getDefinedConstructors(Collection classes, Map map) {
        for (Iterator iterator = classes.iterator(); iterator.hasNext(); ) {
            Suite.Class klass = (Suite.Class)iterator.next();
            ClassDoc cdoc = root.classNamed(klass.name);
            if (cdoc == null) {
                throw new BuildException("cannot find source for class " + klass.name);
            }
            if (cdoc.isInterface()) {
                continue;
            }

            Constructors constructors = new Constructors(cdoc);
            map.put(cdoc.qualifiedName(), constructors);

            List cons = klass.getMethods(root, cdoc.constructors());
            for (Iterator iter = cons.iterator(); iter.hasNext();) {
                constructors.add((ConstructorDoc)iter.next());
            }

            if (constructors.cons.isEmpty() && cdoc.superclass() != null) {
                constructors.addDefault(PRIVATE);
            }
        }
    }

    private void addSynthesizedConstructors(Collection classes, Map map) throws BuildException {
        for (Iterator iterator = classes.iterator(); iterator.hasNext(); ) {
            Suite.Class klass = (Suite.Class)iterator.next();
            ClassDoc cdoc = root.classNamed(klass.name);
            if (cdoc == null) {
                throw new BuildException("cannot find source for class " + klass.name);
            }

            if (cdoc.isInterface()) {
                continue;
            }

            ClassDoc sdoc = cdoc.superclass();
            if (sdoc == null) {
                continue;
            }

            String superName = sdoc.qualifiedName();
            Constructors constructors = (Constructors)map.get(superName);
            int modifier = sdoc.containingPackage() == cdoc.containingPackage() ? PACKAGE_PRIVATE : PROTECTED;
            if (constructors.get(modifier) == null) {
                constructors.addDefault(modifier);
            }
        }
    }

    /**
     * Generates the javadoc from the generated source files.
     *
     * @param packages  the packages of the sources
     * @param srcDir    the directory in which the sources were generated
     * @return the directory in which the javadoc is output to
     */
    private File makeJavadoc(Set packages, File srcDir) {
        File javadocDir = new File(baseDir, "javadoc");
        Build.mkdir(javadocDir);
        List args = new ArrayList();
        args.addAll(javadocArgs);
        args.add("-breakiterator");
        args.add("-sourcepath");
        args.add(srcDir.getPath());
        args.add("-taglet");
        args.add("com.sun.squawk.builder.ToDoTaglet");
        args.add("-tagletpath");
        args.add("build-commands.jar");
        // Standard doclet options
        args.add("-quiet");
        args.add("-d");
        args.add(new File(baseDir, "javadoc").getPath());

        args.addAll(packages);
        // TODO Convert this to using env.javadoc instead of calling java compiler directly, this would
        // mirror the change we made to have env.javac be called instead of java compiler directly as well
        env.getJavaCompiler().javadoc((String[])args.toArray(new String[args.size()]), false);
        return javadocDir;
    }

    /*---------------------------------------------------------------------------*\
     *                            Source file printing                           *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the name to use for a type referenced by the current class. The referenced type is registered
     * if necessary so that an appropriate 'import' statement can be generated later.
     *
     * @param type       the referenced type
     * @param qualified  true if the reference in the generated source will be qualified (and
     *                   therefore not require a corresponding 'import' statement)
     * @param insideDocComment true if the reference is from with a javadoc element as opposed to
     *                   a Java source code element
     * @return the name for <code>type</code> that will be unambiguous in the source code currently being generated
     */
    String ref(Type type, boolean qualified, boolean insideDocComment) {
        ClassDoc cdoc = type.asClassDoc();
        boolean isPrimitive = (cdoc == null);
        String name;
        String dimension = type.dimension();
        if (!isPrimitive) {
            String qualifiedName = cdoc.qualifiedName();
            if (suite.getClass(qualifiedName) == null) {
                if (!insideDocComment) {
                    root.printError("class " + qualifiedName + " referenced by " + currentClass.name + " is not part of API");
                }
                name = qualifiedName;
            } else {
                if (!qualified && !currentClass.baseName().equals(baseName(cdoc.name()))) {
                    name = cdoc.name();
                    if (name.equals("Klass")) {
                        name = "Class";
                    }

                    ClassDoc ref = (ClassDoc)currentSourceRefs.get(name);
                    if (ref == null) {
                        currentSourceRefs.put(name, cdoc);
                    } else if (ref != cdoc) {
                        // the type must be qualified to disambiguate it from the
                        // other class with the same base name that is imported
                        name = qualifiedName;
                    }
                } else {
                    name = qualifiedName;
                }
            }
        } else {
            name = type.qualifiedTypeName();
        }
        return name + dimension;
    }

    /**
     * Prints the source for a class.
     *
     * @param sf     the source file being generated
     * @param cdoc   the javadoc for the class
     * @param klass  the class
     * @param nestedClass true if <code>klass</code> is a nexted class
     */
    private void printClass(SourceFile sf, ClassDoc cdoc, Suite.Class klass, boolean nestedClass) {

        currentClass = klass;
        if (!nestedClass) {
            currentSourceRefs = new TreeMap();
        }

        sf.printDoc(cdoc, this);

        boolean isInterface = cdoc.isInterface();

        String mods = cdoc.modifiers();
        StringBuffer decl = new StringBuffer(mods + ' ' + (isInterface ? "" : "class ")  + klass.baseName());
        if (cdoc.superclass() != null) {
            decl.append(" extends ").append(ref(cdoc.superclass(), true, false));
        }
        ClassDoc[] interfaces = filterPragmas(cdoc.interfaces());
        if (interfaces.length != 0) {
            decl.append(isInterface ? " extends " : " implements ");
            for (int i = 0; i != interfaces.length; ++i) {
                decl.append(ref(interfaces[i], true, false));
                if (i != interfaces.length - 1) {
                    decl.append(", ");
                }
            }
        }
        sf.enter(decl.toString());

        if (!isInterface) {
            Constructors constructors = (Constructors) constructorsMap.get(cdoc.qualifiedName());
            constructors.print(sf, constructorsMap);
        }
        printFields(sf, klass.getFields(root, cdoc.fields()), isInterface);
        printMethods(sf, klass.getMethods(root, cdoc.methods()));
        printNestedClasses(sf, cdoc.innerClasses());

        sf.leave();
    }

    /**
     * Prints the source for a set of nested classes.
     *
     * @param sf     the source file being generated
     * @param cdocs  the javadoc for the nested classes
     */
    private void printNestedClasses(SourceFile sf, ClassDoc[] cdocs) {
        for (int i = 0; i != cdocs.length; ++i) {
            ClassDoc cdoc = cdocs[i];
            Suite.Class nestedClassAPI = suite.getClass(cdoc.qualifiedName());

            // Don't print nested classes that aren't in the API
            if (nestedClassAPI != null) {
                printClass(sf, cdoc, nestedClassAPI, true);
            }
        }
    }

    /**
     * Formats a method declaration in source code syntax.
     *
     * @param mdoc  method to format
     * @return source code declaration of <code>mdoc</code>
     */
    String formatMethodDeclaration(ExecutableMemberDoc mdoc) {
        String returnType = (mdoc instanceof MethodDoc ? ref(((MethodDoc)mdoc).returnType(), false, false) + " " : "");
        String mods = mdoc.modifiers();
        if (mods.length() != 0) {
            mods += " ";
        }
        StringBuffer decl = new StringBuffer(mods + returnType + baseName(mdoc.name()) + '(');

        Parameter[] parameters = mdoc.parameters();
        for (int i = 0; i != parameters.length; ++i) {
            Parameter parameter = parameters[i];
            decl.append(ref(parameter.type(), false, false)).
                append(' ').
                append(parameter.name());
            if (i != parameters.length - 1) {
                decl.append(", ");
            }
        }
        decl.append(')');

        ClassDoc[] exceptions = filterPragmas(mdoc.thrownExceptions());
        if (exceptions.length != 0) {
            decl.append(" throws ");
            for (int i = 0; i != exceptions.length; ++i) {
                decl.append(ref(exceptions[i], false, false));
                if (i != exceptions.length - 1) {
                    decl.append(", ");
                }
            }
        }
        return decl.toString();
    }

    /**
     * Filters exceptions in the package "com.sun.squawk.pragma" from an array of exceptions.
     */
    static ClassDoc[] filterPragmas(ClassDoc[] exceptions) {
        if (exceptions.length > 0) {
            ArrayList list = new ArrayList(exceptions.length);
            for (int i = 0; i != exceptions.length; ++i) {
                ClassDoc exception = exceptions[i];
                if (exception.containingPackage().name().equals("com.sun.squawk.pragma")) {
                    continue;
                }
                list.add(exception);
            }
            exceptions = new ClassDoc[list.size()];
            list.toArray(exceptions);
        }
        return exceptions;
    }

    /**
     * Prints the source for a set of methods.
     *
     * @param sf     the source file being generated
     * @param mdocs  the javadoc for the methods
     */
    private void printMethods(SourceFile sf, List mdocs) {
        for (Iterator i = mdocs.iterator(); i.hasNext(); ) {
            MethodDoc mdoc = (MethodDoc)i.next();
            sf.printDoc(mdoc, this);
            String decl = formatMethodDeclaration(mdoc);
            if (mdoc.isNative() || mdoc.isAbstract() || mdoc.containingClass().isInterface()) {
                sf.println(decl + ';');
            } else {
                sf.enter(decl.toString());
                sf.println("throw new RuntimeException();");
                sf.leave();
            }

        }
    }

    /**
     * Prints the source for a set of fields.
     *
     * @param sf     the source file being generated
     * @param fdocs  the javadoc for the fields
     */
    private void printFields(SourceFile sf, List fdocs, boolean ownerIsInterface) {
        for (Iterator i = fdocs.iterator(); i.hasNext(); ) {
            FieldDoc fdoc = (FieldDoc)i.next();
            sf.printDoc(fdoc, this);
            String cve = fdoc.constantValueExpression();
            // Handle the case where we have an interface defining a static final via an expression such as
            //     public final static int FIOCLEX   = INSTANCE.initConstInt(0);
            // See com.sun.squawk.platform.posix.natives.Ioctl
            if (ownerIsInterface && cve == null) {
                cve = getDefaultValue(fdoc.type());
            }
            boolean needsStaticInit = false;
            if (cve == null) {
                cve = "";
                if (fdoc.isFinal()) {
                    if (fdoc.isStatic()) {
                        needsStaticInit = true;
                    } else {
                        cve = " = " + getDefaultValue(fdoc.type());
                    }
                }
            } else {
                cve = " = " + cve;
            }
            sf.println(fdoc.modifiers() + ' ' + ref(fdoc.type(), false, false) + ' ' + fdoc.name() + cve + ';');
            if (needsStaticInit) {
                sf.enter("static");
                sf.println(fdoc.name() + " = " + getDefaultValue(fdoc.type()) + ";");
                sf.leave();
            }

        }
    }
    /*---------------------------------------------------------------------------*\
     *                Javadoc type named to JNI signatures mapping               *
    \*---------------------------------------------------------------------------*/

    static final Map nameToSig;
    static {
        nameToSig = new HashMap();
        nameToSig.put("byte", "B");
        nameToSig.put("boolean", "Z");
        nameToSig.put("short", "S");
        nameToSig.put("char", "C");
        nameToSig.put("int", "I");
        nameToSig.put("long", "J");
        nameToSig.put("float", "F");
        nameToSig.put("double", "D");
        nameToSig.put("void", "V");
    }

    /**
     * Gets the JNI signature for a type.
     *
     * @param type  the type to convert
     * @return the JNI signature for <code>type</code>
     */
    static String sig(Type type) {
        String sig = (String)nameToSig.get(type.toString());
        if (sig == null) {
            int dims = type.dimension().length() / 2;
            ClassDoc cdoc = type.asClassDoc();
            if (cdoc == null) {
                // Primitive
                sig = (String)nameToSig.get(type.qualifiedTypeName());
            } else {
                String nestedClasses = "";
                ClassDoc outer = cdoc.containingClass();
                if (outer != null) {
                    ClassDoc nested = cdoc;
                    while (outer != null) {
                        String base = nested.name();
                        base = base.substring(base.lastIndexOf('.') + 1);
                        nestedClasses = "$" + base + nestedClasses;
                        nested = outer;
                        outer = outer.containingClass();
                    }
                    outer = nested;
                } else {
                    outer = cdoc;
                }

                sig = outer.qualifiedName();
                sig = "L" + sig.replace('.', '/') + nestedClasses + ";";
            }
            if (dims != 0) {
                for (int i = 0; i != dims; ++i) {
                    sig = "[" + sig;
                }
            }

            nameToSig.put(type.toString(), sig);
        }
        return sig;
    }

    /**
     * Gets the JNI signature for a method.
     *
     * @param enclosingThis  the type of the implicit this of an inner class' constructor or null
     * @param parameters     the parameter types
     * @param returnType     the return type
     * @return the JNI signature of the described method
     */
    static String sig(ClassDoc enclosingThis, Parameter[] parameters, String returnType) {
        StringBuffer sig = new StringBuffer("(");
        if (enclosingThis != null) {
            sig.append(sig(enclosingThis));
        }
        for (int i = 0; i != parameters.length; ++i) {
            sig.append(sig(parameters[i].type()));
        }
        sig.append(")").append(returnType);
        return sig.toString();
    }

    /**
     * Gets the JNI signature for a method.
     *
     * @param parameters     the parameter types
     * @param returnType     the JNI signature of the return type
     * @return the JNI signature of the described method
     */
    static String sig(Parameter[] parameters, String returnType) {
        return sig(null, parameters, returnType);
    }

    /**
     * Gets the JNI signature for a method.
     *
     * @param parameters     the parameter types
     * @param returnType     the return type
     * @return the JNI signature of the described method
     */
    static String sig(Parameter[] parameters, Type returnType) {
        return sig(parameters, sig(returnType));
    }

    /**
     * Extracts the base name of a class from a given (potentially qualified) class name.
     *
     * @param name  the name to process
     * @return the unqualified class name in <code>name</code>
     */
    static String baseName(String name) {
        int index = Math.max(name.lastIndexOf('.'), name.lastIndexOf('$'));
        if (index == -1) {
            return name;
        } else {
            return name.substring(index + 1);
        }
    }

    /**
     * Gets the top level class for a given class. This will simply be the given class
     * if it is not a nested class.
     *
     * @param cdoc  a class
     * @return the top level class for <code>cdoc</code>
     */
    static ClassDoc getTopLevelClass(ClassDoc cdoc) {
        ClassDoc outer = cdoc.containingClass();
        while (outer != null) {
            cdoc = outer;
            outer = cdoc.containingClass();
        }
        return cdoc;
    }

    /**
     * Gets a default value for a given type.
     *
     * @param type   the type to process
     * @return the default value expressed as a Java source code string
     */
    static String getDefaultValue(Type type) {
        switch (sig(type).charAt(0)) {
            case 'Z': return "false";
            case 'B':
            case 'S':
            case 'C':
            case 'I': return "0";
            case 'J': return "0L";
            case 'F': return "0F";
            case 'D': return "0D";
            default:  return "null";
        }
    }

    /*---------------------------------------------------------------------------*\
     *                           Command line entry                              *
    \*---------------------------------------------------------------------------*/

    /**
     * Prints a usage message.
     */
    public void usage(String errMsg) {
        PrintStream out = System.out;
        out.println("Usage: makeapi [-options] api sourcepath dir [javadoc_options]");
        out.println("where options include:");
        out.println();
        out.println("   -cp:<path>  where to find classes the API depends on");
        out.println("   -nodoc      do not produce javadoc");
        out.println();
    }

    /**
     * Parses the command line arguments and configures the fields of this object from them.
     *
     * @param args  the args to parse
     */
    private void parseArgs(String[] args) {

        int argc = 0;
        javadocArgs = new ArrayList();

        while (argc != args.length) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            }
            if (arg.startsWith("-cp:")) {
                classpath = Build.toPlatformPath(arg.substring("-cp:".length()), true);
                javadocArgs.add("-classpath");
                javadocArgs.add(classpath);
            } else if (arg.equals("-nodoc")) {
                nodoc = true;
            } else {
                throw new CommandException(this, "Unknown option: " + arg);
            }

            argc++;
        }

        if (args.length < (argc + 3)) {
            throw new CommandException(this, "missing api, sourcepath or dir");
        }

        String api = args[argc++];
        sourcepath = Build.toPlatformPath(args[argc++], true);
        baseDir = new File(args[argc++]);

        // Copy extra javadoc args
        while (argc != args.length) {
            javadocArgs.add(args[argc++]);
        }

        try {
            Reader reader = new FileReader(api);
            suite = Suite.parse(reader, api);
        } catch (IOException e) {
            throw new BuildException("IO error opening " + api, e);
        }
    }

    public void run(String[] args) throws BuildException {
//        if (env.isJava5SyntaxSupported()) {
//            // TODO Fix MAKEAPI for Java5
//            return;
//        }
        parseArgs(args);
        run();
    }

    /*---------------------------------------------------------------------------*\
     *                            Doclet interface                               *
    \*---------------------------------------------------------------------------*/

    /**
     * The client to which the generated javadoc RootDoc object should be attached.
     */
    private static MakeAPI client;

    /**
     * This method is called by javadoc when this class is specified by the -doclet option.
     */
    public static boolean start(RootDoc root) {
        client.root = root;
        return true;
    }
}

/**
 * A <code>SourceFile</code> instance is used to generate a Java source file.
 */
@SuppressWarnings(value={"unchecked"})
class SourceFile {

    private final PrintWriter out;
    private final CharArrayWriter buffer;
    private String indent = "";
    private final ClassDoc cdoc;

    /**
     * Creates a SourcePath instance to generate source for a given class.
     *
     * @param cdoc  the javadoc element describing a top level class
     */
    SourceFile(ClassDoc cdoc) {

        if (cdoc.containingClass() != null) {
            throw new BuildException("cannot request source file for nested class " + cdoc.qualifiedName());
        }
        this.cdoc = cdoc;
        buffer = new CharArrayWriter(2000);
        out = new PrintWriter(buffer);
    }

    void println(String s) {
        out.println(indent + s);
    }

    /**
     * Formats and adds the javadoc text for a Java source component (i.e. class, field or method) to this source file.
     *
     * @param doc  the javadoc element for a Java source component
     * @param api  the object tracking cross references between classes
     */
    void printDoc(Doc doc, MakeAPI api) {
        String text = doc.getRawCommentText();
        out.println();
        if (text.length() != 0) {
            text = text.replaceAll("Klass", "Class");
            out.println(indent + "/**");
            String[] lines = text.split("\\n");
            for (int i = 0; i != lines.length; ++i) {
                out.println(indent + " *" + lines[i]);
            }
            out.println(indent + " */");

            findRefs(api, doc.tags());
            findRefs(api, doc.inlineTags());
        }
    }


    private void findRefs(MakeAPI api, Tag[] tags) {
        for (int i = 0; i != tags.length; ++i) {
            Tag tag = tags[i];
            if (!tag.name().equals("Text")) {
                if (tag instanceof SeeTag) {
                    SeeTag ref = (SeeTag) tag;
                    ClassDoc cref = ref.referencedClass();
                    if (cref != null) {
                        api.ref(cref, false, true);
                    }
                }
                findRefs(api, tag.inlineTags());
            }
        }
    }

    /**
     * Enters a new scope for a method or class.
     *
     * @param decl  the declaration of the method or class
     */
    void enter(String decl) {
        out.println(indent + decl + " {");
        indent += "    ";
    }

    /**
     * Leaves the current scope.
     */
    void leave() {
        indent = indent.substring(4);
        out.println(indent + "}");
    }

    /**
     * Dumps the generated content to the file specified in this object's constructor.
     *
     * @param baseDir   the directory under which the generated source will be saved
     * @param refs      the types referenced from this source file
     */
    void dump(File baseDir, Collection refs) {
        out.flush();

        String packageName = cdoc.containingPackage().name();
        File path = getFile(baseDir);
        File dir = path.getParentFile();
        Build.mkdir(dir);

        try {
            FileWriter fw = new FileWriter(path);
            PrintWriter pw = new PrintWriter(fw);

            pw.println("/*");
            pw.println(" * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.");
            pw.println(" * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER");
            pw.println(" * ");
            pw.println(" * This code is free software; you can redistribute it and/or modify");
            pw.println(" * it under the terms of the GNU General Public License version 2");
            pw.println(" * only, as published by the Free Software Foundation.");
            pw.println(" * ");
            pw.println(" * This code is distributed in the hope that it will be useful, but");
            pw.println(" * WITHOUT ANY WARRANTY; without even the implied warranty of");
            pw.println(" * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU");
            pw.println(" * General Public License version 2 for more details (a copy is");
            pw.println(" * included in the LICENSE file that accompanied this code).");
            pw.println(" * ");
            pw.println(" * You should have received a copy of the GNU General Public License");
            pw.println(" * version 2 along with this work; if not, write to the Free Software");
            pw.println(" * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA");
            pw.println(" * 02110-1301 USA");
            pw.println(" * ");
            pw.println(" * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo");
            pw.println(" * Park, CA 94025 or visit www.sun.com if you need additional");
            pw.println(" * information or have any questions.");
            pw.println(" */");

            pw.println("package " + packageName + ";");
            pw.println();

            Set imported = new HashSet();
            for (Iterator iter = refs.iterator(); iter.hasNext(); ) {
                ClassDoc ref = MakeAPI.getTopLevelClass((ClassDoc)iter.next());
                String name = ref.qualifiedName();
                if (!ref.containingPackage().name().equals("java.lang")) {
                    if (ref != cdoc && !imported.contains(name)) {
                        pw.println("import " + ref.qualifiedName() + ";");
                        imported.add(name);
                    }
                }
            }

            fw.write(buffer.toCharArray());
            fw.close();
            path.setReadOnly();
        } catch (IOException e) {
            throw new BuildException("could not create Java source file " + path.getPath(), e);
        }
    }

    public File getFile(File baseDir) {
        return new File(baseDir, (cdoc.containingPackage().name() + '.' + cdoc.name()).replace('.', File.separatorChar) + ".java");
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getFile(new File(".")).getPath();
    }

}


/**
 * This class encapsulates the API components in a suite as described
 * by the output of {@link Suite#printAPI}.
 *
 */
@SuppressWarnings(value={"unchecked"})
final class Suite {

    /**
     * The classes in the suite sorted by class name.
     */
    private final SortedMap classes;
    private final SortedMap nestedClasses;

    /**
     * The name of the suite.
     */
    final String name;

    /**
     * Encapsulates an API class within a suite.
     */
    static class Class {

        final String name;

        /**
         * Map of "name + desc" -> Field.
         */
        final SortedMap fields;

        /**
         * Map of "name + desc" -> Method.
         */
        final SortedMap methods;

        Class(String name, SortedMap fields, SortedMap methods) {
            this.name = name;
            this.fields = fields;
            this.methods = methods;
        }

        /**
         * @return  the unqualified name of this class
         */
        String baseName() {
            return MakeAPI.baseName(name);
        }

        /**
         * Filters a list of given methods or constructors defined in the source to remove those
         * that aren't in the API.
         *
         * @param der    used for error reporting
         * @param mdocs  methods or constructors to be filtered
         * @return the elements of <code>mdocs</code> that are in the API
         */
        List getMethods(DocErrorReporter der, ExecutableMemberDoc[] mdocs) {
            List result = new ArrayList();
            boolean constructors = (mdocs instanceof ConstructorDoc[]);
nextMethod: for (Iterator iter = methods.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry entry = (Map.Entry)iter.next();
                String nameDesc = (String)entry.getKey();
                Suite.Method method = (Suite.Method)entry.getValue();
                boolean isConstructor = method.name.equals("<init>");
                if (constructors == isConstructor) {
                    for (int i = 0; i != mdocs.length; ++i) {
                        ExecutableMemberDoc mdoc = mdocs[i];
                        String sourceNameDesc;
                        if (isConstructor) {
                            ClassDoc definingClass = mdoc.containingClass();
                            ClassDoc enclosingThis = null;
                            if (definingClass.containingClass() != null && !definingClass.isStatic()) {
                                enclosingThis = definingClass.containingClass();
                            }
                            sourceNameDesc = "<init>" + MakeAPI.sig(enclosingThis, mdoc.parameters(), "V");
                        } else {
                            sourceNameDesc = mdoc.name() + MakeAPI.sig(mdoc.parameters(), ((MethodDoc)mdoc).returnType());
                        }
                        if (nameDesc.equals(sourceNameDesc)) {
                            result.add(mdoc);
                            continue nextMethod;
                        }
//System.err.println("nameDesc:       " + nameDesc);
//System.err.println("sourceNameDesc: " + sourceNameDesc);
                    }
                    der.printError("cannot find definition in source of " + name + " for method " + method);
                }
            }
            return result;
        }

        /**
         * Filters a list of given fields defined in the source to remove those
         * that aren't in the API.
         *
         * @param der    used for error reporting
         * @param fdocs  fields to be filtered
         * @return the elements of <code>mdocs</code> that are in the API
         */
        List getFields(DocErrorReporter der, FieldDoc[] fdocs) {
            List result = new ArrayList();
nextField:  for (Iterator iter = fields.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry entry = (Map.Entry)iter.next();
                String nameDesc = (String)entry.getKey();
                Suite.Field field = (Suite.Field)entry.getValue();
                for (int i = 0; i != fdocs.length; ++i) {
                    FieldDoc fdoc = fdocs[i];
                    String sourceNameDesc = fdoc.name() + MakeAPI.sig(fdoc.type());
                    if (nameDesc.equals(sourceNameDesc)) {
                        result.add(fdoc);
                        continue nextField;
                    }
//System.err.println("nameDesc:       " + nameDesc);
//System.err.println("sourceNameDesc: " + sourceNameDesc);
                }
                der.printError("cannot find definition in source of " + name + " for field " + field);
            }

            // Add primitive constant value fields
            for (int i = 0; i != fdocs.length; ++i) {
                FieldDoc fdoc = fdocs[i];
                Object cv = fdoc.constantValue();
                if (cv != null && !(cv instanceof String)) {
                    if (!result.contains(fdoc)) {
                        result.add(fdoc);
                    }
                }
            }

            return result;
        }
    }

    /**
     * Encapsulates an API field or method within a class.
     */
    static abstract class Member {
        final String name;
        final String desc;

        Member(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return name + " " + desc;
        }
    }

    /**
     * Encapsulates an API method within a class.
     */
    static class Method extends Member {
        Method(String name, String desc) {
            super(name, desc);
        }
    }

    /**
     * Encapsulates an API field within a class.
     */
    static class Field extends Member {
        Field(String name, String desc) {
            super(name, desc);
        }
    }

    /**
     * @return  the list of classes in the suite sorted by class name
     */
    SortedMap getClasses() {
        return Collections.unmodifiableSortedMap(classes);
    }

    /**
     * Gets a class from this suite.
     *
     * @param name  fully qualified name of a class
     * @return the Class instance corresponding to <code>name</code> or null if there isn't one
     */
    Class getClass(String name) {
        Class c = (Class)classes.get(name);
        if (c == null) {
            for (Iterator iter = classes.entrySet().iterator(); iter.hasNext(); ) {
                Map.Entry e = (Map.Entry)iter.next();
                String className = ((String)e.getKey()).replace('$', '.');
                if (className.equals(name)) {
                    nestedClasses.put(name, e.getValue());
                    return (Class)e.getValue();
                }
            }
        }
        return c;
    }

    /**
     * Parses an API descriptor file.
     *
     * @param reader
     * @param source
     * @return Suite
     */
    static Suite parse(Reader reader, String source) {

        StreamTokenizer st = new StreamTokenizer(reader);

        st.resetSyntax();
        st.whitespaceChars(0, ' ');
        st.wordChars('!', '}');
        st.eolIsSignificant(false);

        SortedMap classes = new TreeMap();

        String className = null;
        SortedMap fields = null;
        SortedMap methods = null;

        String label = parseWord(st, source, MakeAPI.SUITE_FILE_EXTENSION);
        String suiteName = parseWord(st, source, null);

        do {
            label = parseWordOrEOF(st, source, ".class .method .field");

            if (label == null || label.equals(".class")) {
                if (className != null) {
                    // Finish previous class
                    Class klass = new Class(className, fields, methods);
                    classes.put(className, klass);
                }
                if (label == null) {
                    break;
                }
            }

            if (label.equals(".class")) {
                className = parseWord(st, source, null);
                fields = new TreeMap();
                methods = new TreeMap();
            } else {
                String name = parseWord(st, source, null);
                String desc = parseWord(st, source, null);

                if (label.equals(".field")) {
                    fields.put(name + desc, new Field(name, desc));
                } else {
                    methods.put(name + desc, new Method(name, desc));
                }
            }
        } while (true);

        return new Suite(suiteName, classes);
    }

    private Suite(String name, SortedMap classes) {
        this.classes = classes;
        this.nestedClasses = new TreeMap();
        this.name = name;
    }


    private static String parseWord(StreamTokenizer st, String source, String validSet) {
        String value = parseWordOrEOF(st, source, validSet);
        if (value == null) {
            throw new BuildException("reached unexpected end-of-file while parsing " + source);
        }
        return value;
    }

    private static String parseWordOrEOF(StreamTokenizer st, String source, String validSet) {
        try {
            int token = st.nextToken();
            if (token == StreamTokenizer.TT_EOF) {
                return null;
            }
            if (token != StreamTokenizer.TT_WORD) {
                throw new BuildException(source + ": invalid token " + st);
            }
            String value = st.sval;
            if (validSet != null && validSet.indexOf(value) == -1) {
                throw new BuildException(source + ": invalid token " + st +
                                           ", expected one of \"" + validSet + "\"");
            }
            return value;
        } catch (IOException e) {
            throw new BuildException("IO error while parsing line " + st.lineno(), e);
        }
    }
}