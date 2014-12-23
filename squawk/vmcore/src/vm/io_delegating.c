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


/*************** NOTE: this file is included when PLATFORM_TYPE_DELEGATING=true **************************/

/*---------------------------------------------------------------------------*\
 *                                 Channel I/O                               *
\*---------------------------------------------------------------------------*/

#define ptrForJni(addr)             ((signed char *)addr)
#define freeJVMObject(jobject)      (*JNI_env)->DeleteGlobalRef(JNI_env, jobject)


/**
 * Asserts a given condition.
 *
 * @param condition  the condition to test
 * @param msg        the optional message that will be passed to 'fatalVMError'
 *                   if 'condition' is false
 */
static void jni_assume(int condition, char *msg) {
    if (!condition) {
        if (msg == null) {
            msg = "JNI assume failure";
        }
        if (JNI_env != null && (*JNI_env)->ExceptionOccurred(JNI_env)) {
            (*JNI_env)->ExceptionDescribe(JNI_env);
        }
        JNI_env = null;
        fatalVMError(msg);
    }
}

/**
 * Check the last jni call worked
 *
 * @param msg        the optional message that will be passed to 'fatalVMError'
 *                   if the last JNI call failed
 */
static void jni_check(char *msg) {
    jni_assume(!(*JNI_env)->ExceptionOccurred(JNI_env), msg);
}

/**
 * Creates a string in the embedded JVM whose value is initialized from a given string in Squawk memory.
 *
 * @param string   the address of an array of bytes or chars implementing the body of a Squawk string
 * @param classID  the identifier of the String class (CID_STRING or CID_STRING_OF_BYTES)
 * @return the corresponding String object in the embedded JVM
 */
static Address createJVMString(Address string, int classID, int length) {
    Address jvmString;

    jni_assume(string != null, "must not be a null string");
    jni_assume(classID == CID_STRING || classID == CID_STRING_OF_BYTES, "invalid string classID");
    if (classID == CID_STRING) {
        jvmString = (*JNI_env)->NewString(JNI_env, (const jchar*)string, length);
    } else {
        /*
         * Make a null-terminated copy of the string that conforms to the format expected by NewStringUTF
         */
        void *newBuffer(UWord size, const char *desc, boolean fatalIfFail);
        void freeBuffer(Address buffer);
        char* asciiString = (char *)newBuffer(length + 1, "createJVMString", true);
        memcpy(asciiString, string, length);
        asciiString[length] = 0;
        jvmString = (*JNI_env)->NewStringUTF(JNI_env, asciiString);
        freeBuffer(asciiString);
    }
    jni_assume(jvmString != null, "String allocation failed");
    return jvmString;
}

/**
 * Creates a byte array in the embedded JVM whose contents are initialized from a given byte array in Squawk memory.
 *
 * @param array  the address of a byte array
 * @param fill   true if the elements of the returned JVM array should be filled from 'array'
 * @return the corresponding byte array object in the embedded JVM
 */
static Address createJVMByteArray(Address array, int length, boolean fill) {
    Address jvmByteArray;
    assume(array != 0); // This test should be done in Java code so that a NullPointerException can be thrown
    jvmByteArray = (*JNI_env)->NewByteArray(JNI_env, length);
    jni_assume(jvmByteArray != null, "Byte array allocation failed");
    if (fill) {
        (*JNI_env)->SetByteArrayRegion(JNI_env,  jvmByteArray, 0, length, ptrForJni(array));
        jni_check("Byte array copy failed");
    }
    return jvmByteArray;
}

/**
 * Creates a char array in the embedded JVM whose contents are initialized from a given char array in Squawk memory.
 *
 * @param array  the address of a char array in Squawk memory
 * @param fill   true if the elements of the returned JVM array should be filled from 'array'
 * @return the corresponding char array object in the embedded JVM
 */
static Address createJVMCharArray(Address array, int length, boolean fill) {
    Address jvmCharArray;
    assume(array != 0); // This test should be done in Java code so that a NullPointerException can be thrown
    jvmCharArray = (*JNI_env)->NewCharArray(JNI_env, length);
    jni_assume(jvmCharArray != null, "Char array allocation failed");
    if (fill) {
        (*JNI_env)->SetCharArrayRegion(JNI_env,  jvmCharArray, 0, length, (jchar *)ptrForJni(array));
        jni_check("Char array copy failed");
    }
    return jvmCharArray;
}

/**
 * Creates an int array in the embedded JVM whose contents are initialized from a given int array in Squawk memory.
 *
 * @param array  the address of an int array in Squawk memory
 * @param fill   true if the elements of the returned JVM array should be filled from 'array'
 * @return the corresponding int array object in the embedded JVM
 */
static Address createJVMIntArray(Address array, int length, boolean fill) {
    Address jvmIntArray;
    assume(array != 0); // This test should be done in Java code so that a NullPointerException can be thrown
    jvmIntArray = (*JNI_env)->NewIntArray(JNI_env, length);
    jni_assume(jvmIntArray != null, "Int array allocation failed");
    if (fill) {
        (*JNI_env)->SetIntArrayRegion(JNI_env,  jvmIntArray, 0, length, (jint*)ptrForJni(array));
        jni_check("Int array copy failed");
    }
    return jvmIntArray;
}

/**
 * Creates an object in the embedded JVM whose contents are initialized from a given object in Squawk memory.
 * This routine only handles Strings, byte arrays, char arrays and int arrays.
 *
 * @param object  the address of an object in Squawk memory
 * @param fill    true if the return object should be initialized from the Squawk object
 * @return the corresponding object in the embedded JVM
 */
static Address createJVMObject(Address object, boolean fill) {
    if (object != 0) {
        Address cls = getClass(object);
        int id = com_sun_squawk_Klass_id(cls);
        Address jvmObject;
        if (id == CID_STRING || id == CID_STRING_OF_BYTES) {
            jvmObject = createJVMString(object, id, getArrayLength(object));
        } else if (id == CID_BYTE_ARRAY) {
            jvmObject = createJVMByteArray(object, getArrayLength(object), fill);
        } else if (id == CID_CHAR_ARRAY) {
            jvmObject = createJVMCharArray(object, getArrayLength(object), fill);
        } else if (id == CID_INT_ARRAY) {
            jvmObject = createJVMIntArray(object, getArrayLength(object), fill);
        } else {
            fatalVMError("createJVMObject:: Invalid reference type");
        }
        return jvmObject;
    }
    return null;
}

/**
 * Updates the contents of a byte array from the contents of a byte array in the embedded JVM.
 *
 * @param  array     the address of a byte array
 * @param  jvmArray  a handle to a byte array in the embedded JVM
 * @param  offset    the starting index
 * @param  length    the number of bytes to copy
 */
static void updateFromJVMByteArray(Address array, Address jvmArray, int offset, int length) {
    assume(array != 0 && jvmArray != null); // This test should be done in Java code so that a NullPointerException can be thrown
    (*JNI_env)->GetByteArrayRegion(JNI_env, jvmArray, offset, length, ptrForJni(array+offset));
    jni_check("Byte array copy back failed");
}

/**
 * Initializes the IO subsystem.
 *
 * @param  classPath   the class path with which to start the embedded JVM
 * @param  args        extra arguments to pass to the embedded JVM
 * @param  argc        the number of extra arguments in 'args
 */
void CIO_initialize(char *classPath, char** args, int argc) {
    /*
     * Create the embedded Java VM now
     */
    jint createJVM(JavaVM **, void **env, void *args);
    JavaVMInitArgs vm_args;
    JavaVMOption   options[MAX_JVM_ARGS + 1];

    char *buf = (char *)malloc(strlen("-Djava.class.path=")+strlen(classPath)+strlen(":hosted-support/classes")+1);
    sprintf(buf, "-Djava.class.path=%s%chosted-support%cclasses", classPath, (char)pathSeparatorChar, (char)fileSeparatorChar);

    // A version 1.4 Java VM is required
    vm_args.version = JNI_VERSION_1_4;

    vm_args.options  = options;
    options[0].optionString = buf;
    vm_args.nOptions = 1;

    /*
     * Disable the JIT as it has stability problems on at least one platform (Solaris)
     * and the slow down is not noticeable anyway.
     * 
     * Try it on again. It makes a 20% difference with the suite creator.
     * if there is trouble, users can disable from command line:
     * -J-Djava.compiler=NONE
     */
            /*  options[vm_args.nOptions++].optionString = "-Djava.compiler=NONE";*/

    /*
     * Add command line oprtions.
     */
    while (argc-- != 0) {
        options[vm_args.nOptions++].optionString = *args;
        args++;
    }
/*
{
int i;
fprintf(stderr, "Starting embedded JVM with options \"");
for (i = 0; i != vm_args.nOptions; i++) {
    fprintf(stderr, "%s ", vm_args.options[i].optionString);
}
fprintf(stderr, "\"\n");
}
*/
    createJVM(&jvm, (void**)&JNI_env, &vm_args);

    if (JNI_env != null) {
        channelIO_clazz = (*JNI_env)->FindClass(JNI_env, "com/sun/squawk/vm/ChannelIO");
        jni_assume(channelIO_clazz != null, "Can't find com.sun.squawk.vm.ChannelIO");

        channelIO_execute = (*JNI_env)->GetStaticMethodID(JNI_env, channelIO_clazz, "execute", "(IIIIIIIIILjava/lang/Object;Ljava/lang/Object;)I");
        jni_assume(channelIO_execute != null, "Couldn't find method: channelIO_execute()");

    } else {
        fprintf(stderr, "Warning: Error creating Java VM -- I/O subsystem will be disabled\n");
    }
}

/**
 * Shuts down the IO subsystem.
 */
//void CIO_finalize() {
//    if (jvm != 0) {
//        (*jvm)->DestroyJavaVM(jvm);
//        jvm = 0;
//    }
//}

/**
 * Executes an operation on a given channel for an isolate.
 *
 * @param  context the I/O context
 * @param  op      the operation to perform
 * @param  channel the identifier of the channel to execute the operation on
 * @param  i1
 * @param  i2
 * @param  i3
 * @param  i4
 * @param  i5
 * @param  i6
 * @param  send
 * @param  receive
 * @return the operation result
 */
 static void ioExecute(void) {
    int     context = com_sun_squawk_ServiceOperation_context;
    int     op      = com_sun_squawk_ServiceOperation_op;
    int     channel = com_sun_squawk_ServiceOperation_channel;
    int     i1      = com_sun_squawk_ServiceOperation_i1;
    int     i2      = com_sun_squawk_ServiceOperation_i2;
    int     i3      = com_sun_squawk_ServiceOperation_i3;
    int     i4      = com_sun_squawk_ServiceOperation_i4;
    int     i5      = com_sun_squawk_ServiceOperation_i5;
    int     i6      = com_sun_squawk_ServiceOperation_i6;
    Address send    = com_sun_squawk_ServiceOperation_o1;
    Address receive = com_sun_squawk_ServiceOperation_o2;

    Address s1;
    Address r1;
    int res;

    {
        /*
         * Always return 0 if there is no embedded JVM.
         */
        if (JNI_env == null) {
            res = 0;
        } else {
            s1 = createJVMObject(send,   true);
            r1 = createJVMObject(receive, false);
//fprintf(stderr, ">>> %d %d %d\n", cio, op, channelID);
            res = (*JNI_env)->CallStaticIntMethod(JNI_env, channelIO_clazz, channelIO_execute, context, op, channel, i1, i2, i3, i4, i5, i6, s1, r1);
            jni_check("CIO_execute failure");
//fprintf(stderr, " = %d\n", res);
            if (s1 != null) freeJVMObject(s1);
            if (r1 != null) {
                updateFromJVMByteArray(receive, r1, i1, i2);
                freeJVMObject(r1);
            }
        }
    }
    com_sun_squawk_ServiceOperation_result = res;
}

#if KERNEL_SQUAWK
/**
 * Posts an event via ChannelIO to wake up any waiters.
 */
static void ioPostEvent(void) {

    {
        /*
         * Check if there is no embedded JVM.
         */
        if (JNI_env != null) {
            void os_postEvent(boolean notify);
            os_postEvent(true);
        }
    }
}
#endif
