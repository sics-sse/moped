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

package com.sun.squawk.debugger;

import com.sun.squawk.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;

/**
 * Java(tm) Debug Wire Protocol
 */
public class JDWP {

    //VirtualMachine:
    public static final int VirtualMachine_COMMAND_SET = 1;

    /**
     * Returns the JDWP version implemented by the target VM.
     * The version string format is implementation dependent.
     */
    public static final int VirtualMachine_Version_COMMAND = 1;

    /**
     * Returns reference types for all the classes loaded by the target VM
     * which match the given signature.
     * Multple reference types will be returned if two or more class
     * loaders have loaded a class of the same name.
     * The search is confined to loaded classes only; no attempt is made
     * to load a class of the given signature.
     */
    public static final int VirtualMachine_ClassesBySignature_COMMAND = 2;

    /**
     * Returns reference types for all classes currently loaded by the
     * target VM.
     */
    public static final int VirtualMachine_AllClasses_COMMAND = 3;

    /**
     * Returns all threads currently running in the target VM .
     * The returned list contains threads created through
     * java.lang.Thread, all native threads attached to
     * the target VM through JNI, and system threads created
     * by the target VM. Threads that have not yet been started
     * and threads that have completed their execution are not
     * included in the returned list.
     */
    public static final int VirtualMachine_AllThreads_COMMAND = 4;

    /**
     * Returns all thread groups that do not have a parent. This command
     * may be used as the first step in building a tree (or trees) of the
     * existing thread groups.
     */
    public static final int VirtualMachine_TopLevelThreadGroups_COMMAND = 5;

    /**
     * Invalidates this virtual machine mirror.
     * The communication channel to the target VM is closed, and
     * the target VM prepares to accept another subsequent connection
     * from this debugger or another debugger, including the
     * following tasks:
     * <ul>
     * <li>All event requests are cancelled.
     * <li>All threads suspended by the thread-level
     * <a href="#JDWP_ThreadReference_Resume">resume</a> command
     * or the VM-level
     * <a href="#JDWP_VirtualMachine_Resume">resume</a> command
     * are resumed as many times as necessary for them to run.
     * <li>Garbage collection is re-enabled in all cases where it was
     * <a href="#JDWP_ObjectReference_DisableCollection">disabled</a>
     * </ul>
     * Any current method invocations executing in the target VM
     * are continued after the disconnection. Upon completion of any such
     * method invocation, the invoking thread continues from the
     * location where it was originally stopped.
     * <p>
     * Resources originating in
     * this VirtualMachine (ObjectReferences, ReferenceTypes, etc.)
     * will become invalid.
     */
    public static final int VirtualMachine_Dispose_COMMAND = 6;

    /**
     * Returns the sizes of variably-sized data types in the target VM.
     * The returned values indicate the number of bytes used by the
     * identifiers in command and reply packets.
     */
    public static final int VirtualMachine_IDSizes_COMMAND = 7;

    /**
     * Suspends the execution of the application running in the target
     * VM. All Java threads currently running will be suspended.
     * <p>
     * Unlike java.lang.Thread.suspend,
     * suspends of both the virtual machine and individual threads are
     * counted. Before a thread will run again, it must be resumed through
     * the <a href="#JDWP_VirtualMachine_Resume">VM-level resume</a> command
     * or the <a href="#JDWP_ThreadReference_Resume">thread-level resume</a> command
     * the same number of times it has been suspended.
     */
    public static final int VirtualMachine_Suspend_COMMAND = 8;

    /**
     * Resumes execution of the application after the suspend
     * command or an event has stopped it.
     * Suspensions of the Virtual Machine and individual threads are
     * counted. If a particular thread is suspended n times, it must
     * resumed n times before it will continue.
     */
    public static final int VirtualMachine_Resume_COMMAND = 9;

    /**
     * Terminates the target VM with the given exit code.
     * On some platforms, the exit code might be truncated, for
     * example, to the low order 8 bits.
     * All ids previously returned from the target VM become invalid.
     * Threads running in the VM are abruptly terminated.
     * A thread death exception is not thrown and
     * finally blocks are not run.
     */
    public static final int VirtualMachine_Exit_COMMAND = 10;

    /**
     * Creates a new string object in the target VM and returns
     * its id.
     */
    public static final int VirtualMachine_CreateString_COMMAND = 11;

    /**
     * Retrieve this VM's capabilities. The capabilities are returned
     * as booleans, each indicating the presence or absence of a
     * capability. The commands associated with each capability will
     * return the NOT_IMPLEMENTED error if the cabability is not
     * available.
     */
    public static final int VirtualMachine_Capabilities_COMMAND = 12;

    /**
     * Retrieve the classpath and bootclasspath of the target VM.
     * If the classpath is not defined, returns an empty list. If the
     * bootclasspath is not defined returns an empty list.
     */
    public static final int VirtualMachine_ClassPaths_COMMAND = 13;

    /**
     * Releases a list of object IDs. For each object in the list, the
     * following applies.
     * The count of references held by the back-end (the reference
     * count) will be decremented by refCnt.
     * If thereafter the reference count is less than
     * or equal to zero, the ID is freed.
     * Any back-end resources associated with the freed ID may
     * be freed, and if garbage collection was
     * disabled for the object, it will be re-enabled.
     * The sender of this command
     * promises that no further commands will be sent
     * referencing a freed ID.
     * <p>
     * Use of this command is not required. If it is not sent,
     * resources associated with each ID will be freed by the back-end
     * at some time after the corresponding object is garbage collected.
     * It is most useful to use this command to reduce the load on the
     * back-end if a very large number of
     * objects has been retrieved from the back-end (a large array,
     * for example) but may not be garbage collected any time soon.
     * <p>
     * IDs may be re-used by the back-end after they
     * have been freed with this command.
     * This description assumes reference counting,
     * a back-end may use any implementation which operates
     * equivalently.
     */
    public static final int VirtualMachine_DisposeObjects_COMMAND = 14;

    /**
     * Tells the target VM to stop sending events. Events are not discarded;
     * they are held until a subsequent ReleaseEvents command is sent.
     * This command is useful to control the number of events sent
     * to the debugger VM in situations where very large numbers of events
     * are generated.
     * While events are held by the debugger back-end, application
     * execution may be frozen by the debugger back-end to prevent
     * buffer overflows on the back end.
     * Responses to commands are never held and are not affected by this
     * command. If events are already being held, this command is
     * ignored.
     */
    public static final int VirtualMachine_HoldEvents_COMMAND = 15;

    /**
     * Tells the target VM to continue sending events. This command is
     * used to restore normal activity after a HoldEvents command. If
     * there is no current HoldEvents command in effect, this command is
     * ignored.
     */
    public static final int VirtualMachine_ReleaseEvents_COMMAND = 16;

    /**
     * Retrieve all of this VM's capabilities. The capabilities are returned
     * as booleans, each indicating the presence or absence of a
     * capability. The commands associated with each capability will
     * return the NOT_IMPLEMENTED error if the cabability is not
     * available.
     * Since JDWP version 1.4.
     */
    public static final int VirtualMachine_CapabilitiesNew_COMMAND = 17;

    /**
     * Installs new class definitions.
     */
    public static final int VirtualMachine_RedefineClassesCOMMAND = 18;

    /**
     * Set the default stratum.
     */
    public static final int VirtualMachine_SetDefaultStratum_COMMAND = 19;

    /**
     * Returns reference types for all classes currently loaded by the
     * target VM.
     * Both the JNI signature and the generic signature are
     * returned for each class.
     * Generic signatures are described in the signature attribute
     * section in the
     * <a href="http://java.sun.com/docs/books/vmspec">
     * Java Virtual Machine Specification, 3rd Edition.</a>
     * Since JDWP version 1.5.
     */
    public static final int VirtualMachine_AllClassesWithGeneric_COMMAND = 20;

    // ReferenceType:
    public static final int ReferenceType_COMMAND_SET = 2;

    /**
     * Returns the JNI signature of a reference type.
     * JNI signature formats are described in the
     * <a href="http://java.sun.com/products/jdk/1.2/docs/guide/jni/index.html">Java Native Inteface Specification</a>
     * <p>
     * For primitive classes
     * the returned signature is the signature of the corresponding primitive
     * type; for example, "I" is returned as the signature of the class
     * represented by java.lang.Integer.TYPE.
     */
    public static final int ReferenceType_Signature_COMMAND = 1;

    /**
     * Returns the instance of java.lang.ClassLoader which loaded
     * a given reference type. If the reference type was loaded by the
     * system class loader, the returned object ID is null.
     */
    public static final int ReferenceType_ClassLoader_COMMAND = 2;

    /**
     * Returns the modifiers (also known as access flags) for a reference type.
     * The returned bit mask contains information on the declaration
     * of the reference type. If the reference type is an array or
     * a primitive class (for example, java.lang.Integer.TYPE), the
     * value of the returned bit mask is undefined.
     */
    public static final int ReferenceType_Modifiers_COMMAND = 3;

    /**
     * Returns information for each field in a reference type.
     * Inherited fields are not included.
     * The field list will include any synthetic fields created
     * by the compiler.
     * Fields are returned in the order they occur in the class file.
     */
    public static final int ReferenceType_Fields_COMMAND = 4;

    /**
     * Returns information for each method in a reference type.
     * Inherited methods are not included. The list of methods will
     * include constructors (identified with the name "&lt;init&gt;"),
     * the initialization method (identified with the name "&lt;clinit&gt;")
     * if present, and any synthetic methods created by the compiler.
     * Methods are returned in the order they occur in the class file.
     */
    public static final int ReferenceType_Methods_COMMAND = 5;

    /**
     * Returns the value of one or more static fields of the
     * reference type. Each field must be member of the reference type
     * or one of its superclasses, superinterfaces, or implemented interfaces.
     * Access control is not enforced; for example, the values of private
     * fields can be obtained.
     */
    public static final int ReferenceType_GetValues_COMMAND = 6;

    /**
     * Returns the name of source file in which a reference type was
     * declared.
     */
    public static final int ReferenceType_SourceFile_COMMAND = 7;

    /**
     * Returns the classes and interfaces directly nested within this type.
     * Types further nested within those types are not included.
     */
    public static final int ReferenceType_NestedTypes_COMMAND = 8;

    /**
     * Returns the current status of the reference type. The status
     * indicates the extent to which the reference type has been
     * initialized, as described in the
     * <a href="http://java.sun.com/docs/books/vmspec/html/Concepts.doc.html#16491">VM specification</a>.
     * The returned status bits are undefined for array types and for
     * primitive classes (such as java.lang.Integer.TYPE).
     */
    public static final int ReferenceType_Status_COMMAND = 9;

    /**
     * Returns the interfaces declared as implemented by this class.
     * Interfaces indirectly implemented (extended by the implemented
     * interface or implemented by a superclass) are not included.
     */
    public static final int ReferenceType_Interfaces_COMMAND = 10;

    /**
     * Returns the class object corresponding to this type.
     */
    public static final int ReferenceType_ClassObject_COMMAND = 11;

    /**
     * Returns the value of the SourceDebugExtension attribute.
     * Since JDWP version 1.4.
     */
    public static final int ReferenceType_SourceDebugExtension_COMMAND = 12;

    /**
     * Returns the JNI signature of a reference type along with the
     * generic signature if there is one.
     * Generic signatures are described in the signature attribute
     * section in the
     * <a href="http://java.sun.com/docs/books/vmspec">
     * Java Virtual Machine Specification, 3rd Edition.</a>
     * Since JDWP version 1.5.
     * <p>
     */
    public static final int ReferenceType_SignatureWithGeneric_COMMAND = 13;

    /**
     * Returns information, including the generic signature if any,
     * for each field in a reference type.
     * Inherited fields are not included.
     * The field list will include any synthetic fields created
     * by the compiler.
     * Fields are returned in the order they occur in the class file.
     * Generic signatures are described in the signature attribute
     * section in the
     * <a href="http://java.sun.com/docs/books/vmspec">
     * Java Virtual Machine Specification, 3rd Edition.</a>
     * Since JDWP version 1.5.
     */
    public static final int ReferenceType_FieldsWithGeneric_COMMAND = 14;

    /**
     * Returns information, including the generic signature if any,
     * for each method in a reference type.
     * Inherited methodss are not included. The list of methods will
     * include constructors (identified with the name "&lt;init&gt;"),
     * the initialization method (identified with the name "&lt;clinit&gt;")
     * if present, and any synthetic methods created by the compiler.
     * Methods are returned in the order they occur in the class file.
     * Generic signatures are described in the signature attribute
     * section in the
     * <a href="http://java.sun.com/docs/books/vmspec">
     * Java Virtual Machine Specification, 3rd Edition.</a>
     * Since JDWP version 1.5.
     */
    public static final int ReferenceType_MethodsWithGeneric_COMMAND = 15;

    // ClassType:
    public static final int ClassType_COMMAND_SET = 3;

    /**
     * Returns the immediate superclass of a class.
     */
    public static final int ClassType_Superclass_COMMAND = 1;

    /**
     * Sets the value of one or more static fields.
     * Each field must be member of the class type
     * or one of its superclasses, superinterfaces, or implemented interfaces.
     * Access control is not enforced; for example, the values of private
     * fields can be set. Final fields cannot be set.
     * For primitive values, the value's type must match the
     * field's type exactly. For object values, there must exist a
     * widening reference conversion from the value's type to the
     * field's type and the field's type must be loaded.
     */
    public static final int ClassType_SetValues_COMMAND = 2;

    /**
     * Invokes a static method.
     * The method must be member of the class type
     * or one of its superclasses, superinterfaces, or implemented interfaces.
     * Access control is not enforced; for example, private
     * methods can be invoked.
     * <p>
     * The method invocation will occur in the specified thread.
     * Method invocation can occur only if the specified thread
     * has been suspended by an event.
     * Method invocation is not supported
     * when the target VM has been suspended by the front-end.
     * <p>
     * The specified method is invoked with the arguments in the specified
     * argument list.
     * The method invocation is synchronous; the reply packet is not
     * sent until the invoked method returns in the target VM.
     * The return value (possibly the void value) is
     * included in the reply packet.
     * If the invoked method throws an exception, the
     * exception object ID is set in the reply packet; otherwise, the
     * exception object ID is null.
     * <p>
     * For primitive arguments, the argument value's type must match the
     * argument's type exactly. For object arguments, there must exist a
     * widening reference conversion from the argument value's type to the
     * argument's type and the argument's type must be loaded.
     * <p>
     * By default, all threads in the target VM are resumed while
     * the method is being invoked if they were previously
     * suspended by an event or by command.
     * This is done to prevent the deadlocks
     * that will occur if any of the threads own monitors
     * that will be needed by the invoked method. It is possible that
     * breakpoints or other events might occur during the invocation.
     * Note, however, that this implicit resume acts exactly like
     * the ThreadReference resume command, so if the thread's suspend
     * count is greater than 1, it will remain in a suspended state
     * during the invocation. By default, when the invocation completes,
     * all threads in the target VM are suspended, regardless their state
     * before the invocation.
     * <p>
     * The resumption of other threads during the invoke can be prevented
     * by specifying the INVOKE_SINGLE_THREADED
     * bit flag in the <code>options</code> field; however,
     * there is no protection against or recovery from the deadlocks
     * described above, so this option should be used with great caution.
     * Only the specified thread will be resumed (as described for all
     * threads above). Upon completion of a single threaded invoke, the invoking thread
     * will be suspended once again. Note that any threads started during
     * the single threaded invocation will not be suspended when the
     * invocation completes.
     * <p>
     * If the target VM is disconnected during the invoke (for example, through
     * the VirtualMachine dispose command) the method invocation continues.
     */
    public static final int ClassType_InvokeMethod_COMMAND = 3;

    /**
     * Creates a new object of this type, invoking the specified
     * constructor. The constructor method ID must be a member of
     * the class type.
     * <p>
     * Instance creation will occur in the specified thread.
     * Instance creation can occur only if the specified thread
     * has been suspended by an event.
     * Method invocation is not supported
     * when the target VM has been suspended by the front-end.
     * <p>
     * The specified constructor is invoked with the arguments in the specified
     * argument list.
     * The constructor invocation is synchronous; the reply packet is not
     * sent until the invoked method returns in the target VM.
     * The return value (possibly the void value) is
     * included in the reply packet.
     * If the constructor throws an exception, the
     * exception object ID is set in the reply packet; otherwise, the
     * exception object ID is null.
     * <p>
     * For primitive arguments, the argument value's type must match the
     * argument's type exactly. For object arguments, there must exist a
     * widening reference conversion from the argument value's type to the
     * argument's type and the argument's type must be loaded.
     * <p>
     * By default, all threads in the target VM are resumed while
     * the method is being invoked if they were previously
     * suspended by an event or by command.
     * This is done to prevent the deadlocks
     * that will occur if any of the threads own monitors
     * that will be needed by the invoked method. It is possible that
     * breakpoints or other events might occur during the invocation.
     * Note, however, that this implicit resume acts exactly like
     * the ThreadReference resume command, so if the thread's suspend
     * count is greater than 1, it will remain in a suspended state
     * during the invocation. By default, when the invocation completes,
     * all threads in the target VM are suspended, regardless their state
     * before the invocation.
     * <p>
     * The resumption of other threads during the invoke can be prevented
     * by specifying the INVOKE_SINGLE_THREADED
     * bit flag in the <code>options</code> field; however,
     * there is no protection against or recovery from the deadlocks
     * described above, so this option should be used with great caution.
     * Only the specified thread will be resumed (as described for all
     * threads above). Upon completion of a single threaded invoke, the invoking thread
     * will be suspended once again. Note that any threads started during
     * the single threaded invocation will not be suspended when the
     * invocation completes.
     * <p>
     * If the target VM is disconnected during the invoke (for example, through
     * the VirtualMachine dispose command) the method invocation continues.
     */
    public static final int ClassType_NewInstance_COMMAND = 4;

    // ArrayType:
    public static final int ArrayType_COMMAND_SET = 4;

    /**
     * Creates a new array object of this type with a given length.
     */
    public static final int ArrayType_NewInstance_COMMAND = 1;

    //InterfaceType:
    public static final int InterfaceType_COMMAND_SET = 5;

// Method:
    public static final int Method_COMMAND_SET = 6;

    /**
     * Returns line number information for the method.
     * The line table maps source line numbers to the initial code index
     * of the line. The line table
     * is ordered by code index (from lowest to highest).
     */
    public static final int Method_LineTable_COMMAND = 1;

    /**
     * Returns variable information for the method. The variable table
     * includes arguments and locals declared within the method. For
     * instance methods, the "this" reference is included in the
     * table. Also, synthetic variables may be present.
     */
    public static final int Method_VariableTable_COMMAND = 2;

    /**
     * Retrieve the method's bytecodes as defined in the JVM Specification.
     */
    public static final int Method_Bytecodes_COMMAND = 3;

    /**
     * Determine if this method is obsolete.
     */
    public static final int Method_IsObsolete_COMMAND = 4;

    /**
     * Returns variable information for the method, including
     * generic signatures for the variables. The variable table
     * includes arguments and locals declared within the method. For
     * instance methods, the "this" reference is included in the
     * table. Also, synthetic variables may be present.
     * Generic signatures are described in the signature attribute
     * section in the
     * <a href="http://java.sun.com/docs/books/vmspec">
     * Java Virtual Machine Specification, 3rd Edition.</a>
     * Since JDWP version 1.5.
     */
    public static final int Method_VariableTableWithGeneric_COMMAND = 5;

    // Field:
    public static final int Field_COMMAND_SET = 8;

    // ObjectReference:
    public static final int ObjectReference_COMMAND_SET = 9;

    /**
     * Returns the runtime type of the object.
     * The runtime type will be a class or an array.
     */
    public static final int ObjectReference_ReferenceType_COMMAND = 1;

    /**
     * Returns the value of one or more instance fields.
     * Each field must be member of the object's type
     * or one of its superclasses, superinterfaces, or implemented interfaces.
     * Access control is not enforced; for example, the values of private
     * fields can be obtained.
     */
    public static final int ObjectReference_GetValues_COMMAND = 2;

    /**
     * Sets the value of one or more instance fields.
     * Each field must be member of the object's type
     * or one of its superclasses, superinterfaces, or implemented interfaces.
     * Access control is not enforced; for example, the values of private
     * fields can be set.
     * For primitive values, the value's type must match the
     * field's type exactly. For object values, there must be a
     * widening reference conversion from the value's type to the
     * field's type and the field's type must be loaded.
     */
    public static final int ObjectReference_SetValues_COMMAND = 3;

    /**
     * Returns monitor information for an object. All threads int the VM must
     * be suspended.
     */
    public static final int ObjectReference_MonitorInfo_COMMAND = 5;

    /**
     * Invokes a instance method.
     * The method must be member of the object's type
     * or one of its superclasses, superinterfaces, or implemented interfaces.
     * Access control is not enforced; for example, private
     * methods can be invoked.
     * <p>
     * The method invocation will occur in the specified thread.
     * Method invocation can occur only if the specified thread
     * has been suspended by an event.
     * Method invocation is not supported
     * when the target VM has been suspended by the front-end.
     * <p>
     * The specified method is invoked with the arguments in the specified
     * argument list.
     * The method invocation is synchronous; the reply packet is not
     * sent until the invoked method returns in the target VM.
     * The return value (possibly the void value) is
     * included in the reply packet.
     * If the invoked method throws an exception, the
     * exception object ID is set in the reply packet; otherwise, the
     * exception object ID is null.
     * <p>
     * For primitive arguments, the argument value's type must match the
     * argument's type exactly. For object arguments, there must be a
     * widening reference conversion from the argument value's type to the
     * argument's type and the argument's type must be loaded.
     * <p>
     * By default, all threads in the target VM are resumed while
     * the method is being invoked if they were previously
     * suspended by an event or by command.
     * This is done to prevent the deadlocks
     * that will occur if any of the threads own monitors
     * that will be needed by the invoked method. It is possible that
     * breakpoints or other events might occur during the invocation.
     * Note, however, that this implicit resume acts exactly like
     * the ThreadReference resume command, so if the thread's suspend
     * count is greater than 1, it will remain in a suspended state
     * during the invocation. By default, when the invocation completes,
     * all threads in the target VM are suspended, regardless their state
     * before the invocation.
     * <p>
     * The resumption of other threads during the invoke can be prevented
     * by specifying the INVOKE_SINGLE_THREADED
     * bit flag in the <code>options</code> field; however,
     * there is no protection against or recovery from the deadlocks
     * described above, so this option should be used with great caution.
     * Only the specified thread will be resumed (as described for all
     * threads above). Upon completion of a single threaded invoke, the invoking thread
     * will be suspended once again. Note that any threads started during
     * the single threaded invocation will not be suspended when the
     * invocation completes.
     * <p>
     * If the target VM is disconnected during the invoke (for example, through
     * the VirtualMachine dispose command) the method invocation continues.
     */
    public static final int ObjectReference_InvokeMethod_COMMAND = 6;

    /**
     * Prevents garbage collection for the given object. By
     * default all objects in back-end replies may be
     * collected at any time the target VM is running. A call to
     * this command guarantees that the object will not be
     * collected. The
     * <a href="#JDWP_ObjectReference_EnableCollection">EnableCollection</a>
     * command can be used to
     * allow collection once again.
     * <p>
     * Note that while the target VM is suspended, no garbage
     * collection will occur because all threads are suspended.
     * The typical examination of variables, fields, and arrays
     * during the suspension is safe without explicitly disabling
     * garbage collection.
     * <p>
     * This method should be used sparingly, as it alters the
     * pattern of garbage collection in the target VM and,
     * consequently, may result in application behavior under the
     * debugger that differs from its non-debugged behavior.
     */
    public static final int ObjectReference_DisableCollection_COMMAND = 7;

    /**
     * Permits garbage collection for this object. By default all
     * objects returned by the JDWP may be collected
     * at any time the target VM is running. A call to
     * this command is necessary only if garbage collection was
     * previously disabled with
     * the <a href="#JDWP_ObjectReference_DisableCollection">DisableCollection</a> command.
     */
    public static final int ObjectReference_EnableCollection_COMMAND = 8;

    /**
     * Determines whether an object has been garbage collected in the
     * target VM.
     */
    public static final int ObjectReference_IsCollected_COMMAND = 9;

    // StringReference:
    public static final int StringReference_COMMAND_SET = 10;

    /**
     * Returns the characters contained in the string.
     */
    public static final int StringReference_Value_COMMAND = 1;

    // ThreadReference:
    public static final int ThreadReference_COMMAND_SET = 11;

    /**
     * Returns the thread name.
     */
    public static final int ThreadReference_Name_COMMAND = 1;

    /**
     * Suspends the thread.
     * <p>
     * Unlike java.lang.Thread.suspend(), suspends of both
     * the virtual machine and individual threads are counted. Before
     * a thread will run again, it must be resumed the same number
     * of times it has been suspended.
     * <p>
     * Suspending single threads with command has the same
     * dangers java.lang.Thread.suspend(). If the suspended
     * thread holds a monitor needed by another running thread,
     * deadlock is possible in the target VM (at least until the
     * suspended thread is resumed again).
     * <p>
     * The suspended thread is guaranteed to remain suspended until
     * resumed through one of the JDI resume methods mentioned above;
     * the application in the target VM cannot resume the suspended thread
     * through {@link java.lang.Thread#resume}.
     * <p>
     * Note that this doesn't change the status of the thread (see the
     * <a href="#JDWP_ThreadReference_Status">ThreadStatus</a> command.)
     * For example, if it was
     * Running, it will still appear running to other threads.
     */
    public static final int ThreadReference_Suspend_COMMAND = 2;

    /**
     * Resumes the execution of a given thread. If this thread was
     * not previously suspended by the front-end,
     * calling this command has no effect.
     * Otherwise, the count of pending suspends on this thread is
     * decremented. If it is decremented to 0, the thread will
     * continue to execute.
     */
    public static final int ThreadReference_Resume_COMMAND = 3;

    /**
     * Returns the current status of a thread. The thread status
     * reply indicates the thread status the last time it was running.
     * the suspend status provides information on the thread's
     * suspension, if any.
     */
    public static final int ThreadReference_Status_COMMAND = 4;

    /**
     * Returns the thread group that contains a given thread.
     */
    public static final int ThreadReference_ThreadGroup_COMMAND = 5;

    /**
     * Returns the current call stack of a suspended thread.
     * The sequence of frames starts with
     * the currently executing frame, followed by its caller,
     * and so on. The thread must be suspended, and the returned
     * frameID is valid only while the thread is suspended.
     */
    public static final int ThreadReference_Frames_COMMAND = 6;

    /**
     * Returns the count of frames on this thread's stack.
     * The thread must be suspended, and the returned
     * count is valid only while the thread is suspended.
     * Returns JDWP.Event_errorThreadNotSuspended if not suspended.
     */
    public static final int ThreadReference_FrameCount_COMMAND = 7;

    /**
     * Returns the objects whose monitors have been entered by this thread.
     * The thread must be suspended, and the returned information is
     * relevant only while the thread is suspended.
     */
    public static final int ThreadReference_OwnedMonitors_COMMAND = 8;

    /**
     * Returns the object, if any, for which this thread is waiting
     * for monitor entry or with java.lang.Object.wait.
     * The thread must be suspended, and the returned information is
     * relevant only while the thread is suspended.
     */
    public static final int ThreadReference_CurrentContendedMonitor_COMMAND = 9;

    /**
     * Stops the thread with an asynchronous exception, as if done by
     * java.lang.Thread.stop
     */
    public static final int ThreadReference_Stop_COMMAND = 10;

    /**
     * Interrupt the thread, as if done by java.lang.Thread.interrupt
     */
    public static final int ThreadReference_Interrupt_COMMAND = 11;

    /**
     * Get the suspend count for this thread. The suspend count is the
     * number of times the thread has been suspended through the
     * thread-level or VM-level suspend commands without a corresponding resume
     */
    public static final int ThreadReference_SuspendCount_COMMAND = 12;

    // ThreadGroupReference:
    public static final int ThreadGroupReference_COMMAND_SET = 12;

    /**
     * Returns the thread group name.
     */
    public static final int ThreadGroupReference_Name_COMMAND = 1;

    /**
     * Returns the thread group, if any, which contains a given thread group.
     */
    public static final int ThreadGroupReference_Parent_COMMAND = 2;

    /**
     * Returns the threads and thread groups directly contained
     * in this thread group. Threads and thread groups in child
     * thread groups are not included.
     */
    public static final int ThreadGroupReference_Children_COMMAND = 3;

    // ArrayReference:
    public static final int ArrayReference_COMMAND_SET = 13;

    /**
     * Returns the number of components in a given array.
     */
    public static final int ArrayReference_Length_COMMAND = 1;

    /**
     * Returns a range of array components. The specified range must
     * be within the bounds of the array.
     */
    public static final int ArrayReference_GetValues_COMMAND = 2;

    /**
     * Sets a range of array components. The specified range must
     * be within the bounds of the array.
     * For primitive values, each value's type must match the
     * array component type exactly. For object values, there must be a
     * widening reference conversion from the value's type to the
     * array component type and the array component type must be loaded.
     */
    public static final int ArrayReference_SetValues_COMMAND = 3;

    // ClassLoaderReference:
    public static final int ClassLoaderReference_COMMAND_SET = 14;

    /**
     * Returns a list of all classes which this class loader has
     * been requested to load. This class loader is considered to be
     * an <i>initiating</i> class loader for each class in the returned
     * list. The list contains each
     * reference type defined by this loader and any types for which
     * loading was delegated by this class loader to another class loader.
     * <p>
     * The visible class list has useful properties with respect to
     * the type namespace. A particular type name will occur at most
     * once in the list. Each field or variable declared with that
     * type name in a class defined by
     * this class loader must be resolved to that single type.
     * <p>
     * No ordering of the returned list is guaranteed.
     */
    public static final int ClassLoaderReference_VisibleClasses_COMMAND = 1;

    // EventRequest:
    public static final int EventRequest_COMMAND_SET = 15;

    /**
     * Set an event request. When the event described by this request
     * occurs, an <a href="#JDWP_Event">event</a> is sent from the
     * target VM.
     */
    public static final int EventRequest_Set_COMMAND = 1;

    /**
     * Clear an event request.
     */
    public static final int EventRequest_Clear_COMMAND = 2;

    /**
     * Removes all set breakpoints.
     */
    public static final int EventRequest_ClearAllBreakpoints_COMMAND = 3;

    // EventRequest modifiers:
    /** Modifier kind values that make events conditional.
        Most are NOT supported by SDWP or Squawk.
     */
    public static final int EventRequest_MOD_COUNT = 1;
    public static final int EventRequest_MOD_CONDITIONAL = 2;
    public static final int EventRequest_MOD_THREAD_ONLY = 3;
    public static final int EventRequest_MOD_CLASS_ONLY = 4;
    public static final int EventRequest_MOD_CLASS_MATCH = 5;
    public static final int EventRequest_MOD_CLASS_EXCLUDE = 6;
    public static final int EventRequest_MOD_LOCATION_ONLY = 7;
    public static final int EventRequest_MOD_EXCEPTION_ONLY = 8;
    public static final int EventRequest_MOD_FIELD_ONLY = 9;
    public static final int EventRequest_MOD_STEP = 10;
    public static final int EventRequest_MOD_INSTANCE_ONLY = 11;

    // StackFrame:
    public static final int StackFrame_COMMAND_SET = 16;

    /**
     * Returns the value of one or more local variables in a
     * given frame. Each variable must be visible at the frame's code index.
     * Even if local variable information is not available, values can
     * be retrieved if the front-end is able to
     * determine the correct local variable index. (Typically, this
     * index can be determined for method arguments from the method
     * signature without access to the local variable table information.)
     */
    public static final int StackFrame_GetValues_COMMAND = 1;

    /**
     * Sets the value of one or more local variables.
     * Each variable must be visible at the current frame code index.
     * For primitive values, the value's type must match the
     * variable's type exactly. For object values, there must be a
     * widening reference conversion from the value's type to the
     * variable's type and the variable's type must be loaded.
     * <p>
     * Even if local variable information is not available, values can
     * be set, if the front-end is able to
     * determine the correct local variable index. (Typically, this
     * index can be determined for method arguments from the method
     * signature without access to the local variable table information.)
     */
    public static final int StackFrame_SetValues_COMMAND = 2;

    /**
     * Returns the value of the 'this' reference for this frame.
     * If the frame's method is static or native, the reply
     * will contain the null object reference.
     */
    public static final int StackFrame_ThisObject_COMMAND = 3;

    /**
     * Pop stack frames, thru and including 'frame'.
     * Since JDWP version 1.4.
     */
    public static final int StackFrame_PopFrames_COMMAND = 4;

    // ClassObjectReference:
    public static final int ClassObjectReference_COMMAND_SET = 17;

    /**
     * Returns the reference type reflected by this class object.
     */
    public static final int ClassObjectReference_ReflectedType_COMMAND = 1;

    // Event:
    public static final int Event_COMMAND_SET = 64;

    /**
     * Several events may occur at a given time in the target VM.
     * For example, there may be more than one breakpoint request
     * for a given location
     * or you might single step to the same location as a
     * breakpoint request.  These events are delivered
     * together as a composite event.  For uniformity, a
     * composite event is always used
     * to deliver events, even if there is only one event to report.
     * <P>
     * The events that are grouped in a composite event are restricted in the
     * following ways:
     * <P>
     * <UL>
     * <LI>Always singleton composite events:
     *     <UL>
     *     <LI>VM Start Event
     *     <LI>VM Death Event
     *     </UL>
     * <LI>Only with other thread start events for the same thread:
     *     <UL>
     *     <LI>Thread Start Event
     *     </UL>
     * <LI>Only with other thread death events for the same thread:
     *     <UL>
     *     <LI>Thread Death Event
     *     </UL>
     * <LI>Only with other class prepare events for the same class:
     *     <UL>
     *     <LI>Class Prepare Event}
     *     </UL>
     * <LI>Only with other class unload events for the same class:
     *     <UL>
     *     <LI>Class Unload Event
     *     </UL>
     * <LI>Only with other access watchpoint events for the same field access:
     *     <UL>
     *     <LI>Access Watchpoint Event
     *     </UL>
     * <LI>Only with other modification watchpoint events for the same field
     * modification:
     *     <UL>
     *     <LI>Modification Watchpoint Event
     *     </UL>
     * <LI>Only with other ExceptionEvents for the same exception occurrance:
     *     <UL>
     *     <LI>ExceptionEvent
     *     </UL>
     * <LI>Only with other members of this group, at the same location
     * and in the same thread:
     *     <UL>
     *     <LI>Breakpoint Event
     *     <LI>Step Event
     *     <LI>Method Entry Event
     *     <LI>Method Exit Event
     *     </UL>
     * </UL>
     */
    public static final int Event_Composite_COMMAND = 100;

    // Error:
    public static final int Error_NONE = 0;
    public static final int Error_INVALID_THREAD = 10;
    public static final int Error_INVALID_THREAD_GROUP = 11;
    public static final int Error_INVALID_PRIORITY = 12;
    public static final int Error_THREAD_NOT_SUSPENDED = 13;
    public static final int Error_THREAD_SUSPENDED = 14;
    public static final int Error_INVALID_OBJECT = 20;
    public static final int Error_INVALID_CLASS = 21;
    public static final int Error_CLASS_NOT_PREPARED = 22;
    public static final int Error_INVALID_METHODID = 23;
    public static final int Error_INVALID_LOCATION = 24;
    public static final int Error_INVALID_FIELDID = 25;
    public static final int Error_INVALID_FRAMEID = 30;
    public static final int Error_NO_MORE_FRAMES = 31;
    public static final int Error_OPAQUE_FRAME = 32;
    public static final int Error_NOT_CURRENT_FRAME = 33;
    public static final int Error_TYPE_MISMATCH = 34;
    public static final int Error_INVALID_SLOT = 35;
    public static final int Error_DUPLICATE = 40;
    public static final int Error_NOT_FOUND = 41;
    public static final int Error_INVALID_MONITOR = 50;
    public static final int Error_NOT_MONITOR_OWNER = 51;
    public static final int Error_INTERRUPT = 52;
    public static final int Error_INVALID_CLASS_FORMAT = 60;
    public static final int Error_CIRCULAR_CLASS_DEFINITION = 61;
    public static final int Error_FAILS_VERIFICATION = 62;
    public static final int Error_ADD_METHOD_NOT_IMPLEMENTED = 63;
    public static final int Error_SCHEMA_CHANGE_NOT_IMPLEMENTED = 64;
    public static final int Error_INVALID_TYPESTATE = 65;
    public static final int Error_HIERARCHY_CHANGE_NOT_IMPLEMENTED = 66;
    public static final int Error_DELETE_METHOD_NOT_IMPLEMENTED = 67;
    public static final int Error_UNSUPPORTED_VERSION = 68;
    public static final int Error_NAMES_DONT_MATCH = 69;
    public static final int Error_CLASS_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 70;
    public static final int Error_METHOD_MODIFIERS_CHANGE_NOT_IMPLEMENTED = 71;
    public static final int Error_NOT_IMPLEMENTED = 99;
    public static final int Error_NULL_POINTER = 100;
    public static final int Error_ABSENT_INFORMATION = 101;
    public static final int Error_INVALID_EVENT_TYPE = 102;
    public static final int Error_ILLEGAL_ARGUMENT = 103;
    public static final int Error_OUT_OF_MEMORY = 110;
    public static final int Error_ACCESS_DENIED = 111;
    public static final int Error_VM_DEAD = 112;
    public static final int Error_INTERNAL = 113;
    public static final int Error_UNATTACHED_THREAD = 115;
    public static final int Error_INVALID_TAG = 500;
    public static final int Error_ALREADY_INVOKING = 502;
    public static final int Error_INVALID_INDEX = 503;
    public static final int Error_INVALID_LENGTH = 504;
    public static final int Error_INVALID_STRING = 506;
    public static final int Error_INVALID_CLASS_LOADER = 507;
    public static final int Error_INVALID_ARRAY = 508;
    public static final int Error_TRANSPORT_LOAD = 509;
    public static final int Error_TRANSPORT_INIT = 510;
    public static final int Error_NATIVE_METHOD = 511;
    public static final int Error_INVALID_COUNT = 512;

    // EventKind:
    public static final int EventKind_VM_DISCONNECTED = 100;
    public static final int EventKind_VM_START = JDWP.EventKind_VM_INIT;
    public static final int EventKind_THREAD_DEATH = JDWP.EventKind_THREAD_END;
    public static final int EventKind_SINGLE_STEP = 1;
    public static final int EventKind_BREAKPOINT = 2;
    public static final int EventKind_FRAME_POP = 3;
    public static final int EventKind_EXCEPTION = 4;
    public static final int EventKind_USER_DEFINED = 5;
    public static final int EventKind_THREAD_START = 6;
    public static final int EventKind_THREAD_END = 7;
    public static final int EventKind_CLASS_PREPARE = 8;
    public static final int EventKind_CLASS_UNLOAD = 9;
    public static final int EventKind_CLASS_LOAD = 10;
    public static final int EventKind_FIELD_ACCESS = 20;
    public static final int EventKind_FIELD_MODIFICATION = 21;
    public static final int EventKind_EXCEPTION_CATCH = 30;
    public static final int EventKind_METHOD_ENTRY = 40;
    public static final int EventKind_METHOD_EXIT = 41;
    public static final int EventKind_VM_INIT = 90;
    public static final int EventKind_VM_DEATH = 99;

    // ThreadStatus:
    public static final int ThreadStatus_ZOMBIE = 0;
    public static final int ThreadStatus_RUNNING = 1;
    public static final int ThreadStatus_SLEEPING = 2;
    public static final int ThreadStatus_MONITOR = 3;
    public static final int ThreadStatus_WAIT = 4;

    // SuspendStatus:
    public static final int SuspendStatus_SUSPEND_STATUS_SUSPENDED = 0x1;

    // ClassStatus:
    public static final int ClassStatus_VERIFIED = 1;
    public static final int ClassStatus_PREPARED = 2;
    public static final int ClassStatus_INITIALIZED = 4;
    public static final int ClassStatus_ERROR = 8;
    public static final int ClassStatus_VERIFIED_PREPARED_INITIALIZED = 1 + 2 + 4;

    // TypeTag:
    public static final int TypeTag_CLASS = 1;
    public static final int TypeTag_INTERFACE = 2;
    public static final int TypeTag_ARRAY = 3;

    /**
     * Gets the <code>JDWP.TypeTag_...</code> value corresponding to a given class.
     *
     * @param klass  the class to convert
     * @return the <code>JDWP.TypeTag_...</code> value corresponding to <code>klass</code>
     */
    public static byte getTypeTag(Klass klass) {
        if (klass.isArray()) {
            return TypeTag_ARRAY;
        } else if (klass.isInterface()) {
            return TypeTag_INTERFACE;
        } else {
            return TypeTag_CLASS;
        }
    }

    /**
     * Return true if the tag is a valid <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdwp/jdwp-protocol.html#JDWP_TYPETAG">JDWP Type Tag</a> value.
     * Type tags are a sparse set that doesn't include zero, so it's a good value to
     * validate.
     *
     * @param typeTag int
     * @return boolean
     */
    public static boolean isValidTypeTag(int typeTag) {
        switch (typeTag) {
            case TypeTag_CLASS:
            case TypeTag_INTERFACE:
            case TypeTag_ARRAY:
                return true;
            default:
                return false;
        }
    }

    // Tag:
    public static final int Tag_ARRAY         = 91;  // '['
    public static final int Tag_BYTE          = 66;  // 'B'
    public static final int Tag_CHAR          = 67;  // 'C'
    public static final int Tag_OBJECT        = 76;  // 'L'
    public static final int Tag_FLOAT         = 70;  // 'F'
    public static final int Tag_DOUBLE        = 68;  // 'D'
    public static final int Tag_INT           = 73;  // 'I'
    public static final int Tag_LONG          = 74;  // 'J'
    public static final int Tag_SHORT         = 83;  // 'S'
    public static final int Tag_VOID          = 86;  // 'V'
    public static final int Tag_BOOLEAN       = 90;  // 'Z'
    public static final int Tag_STRING        = 115; // 's'
    public static final int Tag_THREAD        = 116; // 't'
    public static final int Tag_THREAD_GROUP  = 103; // 'g'
    public static final int Tag_CLASS_LOADER  = 108; // 'l'
    public static final int Tag_CLASS_OBJECT  = 99;  // 'c'

    /**
     * Given a klass, returns the corresponding {@link com.sun.squawk.debugger.JDWP.Tag} value.
     *
     * @param klass Klass
     * @return the JDWP.Tag value
     */
    public static byte getTag(Klass klass) {
        if (klass.isArray()) {
            return Tag_ARRAY;
        } else {
            int tag = Klass.getSignatureFirstChar(klass.getSystemID());
            if (tag == Tag_OBJECT) {
                switch (klass.getSystemID()) {
                    case CID.STRING_OF_BYTES:
                    case CID.STRING:   tag = Tag_STRING;       break;
                    case CID.BYTECODE: tag = Tag_BYTE;         break;
                    case CID.KLASS:    tag = Tag_CLASS_OBJECT; break;
                    case CID.LOCAL:
                    case CID.GLOBAL:
                    case CID.ADDRESS:
                    case CID.OFFSET:
                    case CID.UWORD:    tag = Klass.SQUAWK_64 ?
                                             Tag_LONG :
                                             Tag_INT;          break;
                    default:
                        while (klass != null) {
                            if (klass.getInternalName().equals("java.lang.Thread")) {
                                tag = Tag_THREAD;
                                break;
                            }
                            klass = klass.getSuperclass();
                        }
                        break;
                }
            }
            Assert.that(isValidTag(tag));
            return (byte) tag;
        }
    }

    /**
     * Return true if the tag is a valid <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdwp/jdwp-protocol.html#JDWP_TAG">JDWP Tag</a> value.
     * Tags are a sparse set that doesn't include zero, so it's a good value to
     * validate.
     *
     * @param tag int
     * @return boolean
     */
    public static boolean isValidTag(int tag) {
        switch (tag) {
            case Tag_ARRAY:
            case Tag_BYTE:
            case Tag_CHAR:
            case Tag_OBJECT:
            case Tag_FLOAT:
            case Tag_DOUBLE:
            case Tag_INT:
            case Tag_LONG:
            case Tag_SHORT:
            case Tag_VOID:
            case Tag_BOOLEAN:
            case Tag_STRING:
            case Tag_THREAD:
            case Tag_THREAD_GROUP:
            case Tag_CLASS_LOADER:
            case Tag_CLASS_OBJECT:
                return true;
            default:
                 return false;
        }
    }
    
    /**
     * Return true if the tag represents an reference type <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdwp/jdwp-protocol.html#JDWP_TAG">JDWP Tag</a> value.
     * @param tag int
     * @return boolean
     */
    public static boolean isReferenceTag(int tag) {
        switch (tag) {
            case Tag_ARRAY:
            case Tag_OBJECT:
            case Tag_STRING:
            case Tag_THREAD:
            case Tag_THREAD_GROUP:
            case Tag_CLASS_LOADER:
            case Tag_CLASS_OBJECT:
                return true;
            default:
                 return false;
        }
    }

    // StepDepth:
    public static final int StepDepth_INTO = 0;
    public static final int StepDepth_OVER = 1;
    public static final int StepDepth_OUT = 2;

    // StepSize:
    public static final int StepSize_MIN = 0;
    public static final int StepSize_LINE = 1;

    // SuspendPolicy:
    public static final int SuspendPolicy_NONE = 0;
    public static final int SuspendPolicy_EVENT_THREAD = 1;
    public static final int SuspendPolicy_ALL = 2;

    // InvokeOptions:
    public static final int InvokeOptions_INVOKE_SINGLE_THREADED = 0x01;
    public static final int InvokeOptions_INVOKE_NONVIRTUAL = 0x02;

    private JDWP() {
    }
}
