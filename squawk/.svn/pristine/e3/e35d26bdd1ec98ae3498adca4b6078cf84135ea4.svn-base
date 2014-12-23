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

#define TRUE 1
#define FALSE 0

#include <stdlib.h>
#include <sys/time.h>
#include <dlfcn.h>
#include <jni.h>

#define jlong  int64_t

/* The package that conmtains the native code to use for a "NATIVE" platform type*/
#if defined(sun)
#define sysPlatformName() "solaris"
#else
#define sysPlatformName() "linux"
#endif

#include "os_posix.c"

/** 
 * Return another path to find the bootstrap suite with the given name.
 * On some platforms the suite might be stored in an odd location
 * 
 * @param bootstrapSuiteName the name of the boostrap suite
 * @return full or partial path to alternate location, or null
 */
INLINE char* sysGetAlternateBootstrapSuiteLocation(char* bootstrapSuiteName) { return NULL; }

#if PLATFORM_TYPE_DELEGATING
jint createJVM(JavaVM **jvm, void **env, void *args) {
    jint (JNICALL *CreateJavaVM)(JavaVM **jvm, void **env, void *args) = 0;
    const char* name = "libjvm.so";
    void* libVM = dlopen(name, RTLD_LAZY);
    if (libVM == 0) {
        fprintf(stderr, "Cannot load %s\n", name);
        fprintf(stderr, "Please add the directories containing libjvm.so and libverify.so\n");
        fprintf(stderr, "to the LD_LIBRARY_PATH environment variable.\n");
        return false;
    }

    CreateJavaVM = (jint (JNICALL *)(JavaVM **,void **, void *)) dlsym(libVM, "JNI_CreateJavaVM");

    if (CreateJavaVM == 0) {
        fprintf(stderr,"Cannot resolve JNI_CreateJavaVM in %s\n", name);
        return false;
    }

    return CreateJavaVM(jvm, env, args) == 0;
}
#endif

#define osloop()        /**/
#define osbackbranch()  /**/
#define osfinish()      /**/
