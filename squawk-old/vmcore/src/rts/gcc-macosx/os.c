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

/* The package that conmtains the native code to use for a "NATIVE" platform type*/
#define sysPlatformName() "macosx"

#include "os_posix.c"

/* This "standard" C function is not provided on Mac OS X */
char* strsignal(int signum) {
    switch (signum) {
        case SIGABRT:     return "SIGABRT";
        case SIGFPE:      return "SIGFPE";
        case SIGILL:      return "SIGILL";
        case SIGINT:      return "SIGINT";
        case SIGSEGV:     return "SIGSEGV";
        case SIGTERM:     return "SIGTERM";
        default:          return "<unknown signal>";
    }
}

/* defined in os_main.c */
static char* sysGetAlternateBootstrapSuiteLocation(char* bootstrapSuiteName);

#if PLATFORM_TYPE_DELEGATING
jint createJVM(JavaVM **jvm, void **env, void *args) {
    return JNI_CreateJavaVM(jvm, env, args) == 0;
}
#endif


#define osloop()        /**/
#define osbackbranch()  /**/
#define osfinish()      /**/
