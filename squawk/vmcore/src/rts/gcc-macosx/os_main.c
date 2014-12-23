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

/*
 * Platform dependent startup code directly included by squawk.c.spp
 */

CFBundleRef mainBundle;

/* Datatype so that parameters can be passed between threads */
typedef struct mainParametersStruct {
        int argc;
        char **argv;
} mainParameters;

void printMainParams(mainParameters* params) {
    int i;
    for (i = 0; i < params->argc; i++) {
        fprintf(stderr, "arg[%d]: %s\n", i, params->argv[i]);
    }
}

/**
 * Parse a command line string into argc, argv.
 * Modifies the cmd line str.
 *
 */
void cmdStr2argv(mainParameters* params, char* cmdLine) {
    int i;
    char* str = NULL;
    char* originalCmdLine = cmdLine;

    cmdLine = strdup(originalCmdLine);
    if (cmdLine == NULL) {
        fprintf(stderr, "can't dup command line\n");
        return;
    }

/*
fprintf(stderr, "cmdLine: %s\n", cmdLine);
*/
    i = 0;
    str = strtok(cmdLine, " ");
    if (str != NULL) {
        do {
            i++;
        } while ((str = strtok(NULL, " ")) != NULL);
    }

    // first pass wrote over cmdLine, so create new copy...
    free(cmdLine);
    cmdLine = strdup(originalCmdLine);
    if (cmdLine == NULL) {
        fprintf(stderr, "can't dup command line\n");
        return;
    }
    
    params->argc = i;
    params->argv = (char **)malloc((i + 1) * sizeof(char*));

    i = 0;
    str = strtok(cmdLine, " ");
    if (str != NULL) {
        do {
            params->argv[i++] = str;
        } while ((str = strtok(NULL, " ")) != NULL);
    }

    printMainParams(params);
}

// DEBUG
/*
void testCmdStr2argv(mainParameters* launchOptions) {
     cmdStr2argv(launchOptions, "");
     cmdStr2argv(launchOptions, "    ");
     cmdStr2argv(launchOptions, "-verbose");
     cmdStr2argv(launchOptions, "  -verbose");
     cmdStr2argv(launchOptions, "-verbose  ");
     cmdStr2argv(launchOptions, "  -verbose  ");

     cmdStr2argv(launchOptions, "-verbose foo");
     cmdStr2argv(launchOptions, "  -verbose foo");
     cmdStr2argv(launchOptions, "-verbose  foo ");
     cmdStr2argv(launchOptions, "  -verbose     foo   ");
}
*/

/**
  * Convert the Mac's CFStringRef to a nice, POSIXy string, or die trying.
  * This is a pain in the neck!
  */
char* CFString2CString(CFStringRef macStr) {
    CFIndex bufSize = CFStringGetMaximumSizeOfFileSystemRepresentation(macStr);
    char* result = (char*)malloc(bufSize);
    if (result == NULL) {
        fatalVMError("CFString2CString");
    }
    if (!CFStringGetFileSystemRepresentation(macStr, result, bufSize)) {
        fatalVMError("CFString2CString");
    }
    return result;
}

/**
 * Look for a command string stored in Info.plist. If found, parse into a parameter list
 * suitable for main(), and return true. Otherwise return false.
 */
int readArgsFromPlist(mainParameters* params) {
    CFStringRef cmdlineRef = NULL;

    cmdlineRef = CFBundleGetValueForInfoDictionaryKey(
                    mainBundle,
                    CFSTR("SquawkCommandLineParameters"));

    if (cmdlineRef != NULL && CFGetTypeID(cmdlineRef) == CFStringGetTypeID()) {
        char* cmdline = CFString2CString(cmdlineRef); // don't free this - used by argv!
        cmdStr2argv(params, cmdline);
        return true;
    }
    return false;
}


/**
 * Starts the Squawk VM in it's own thread
 *
 * @param  options  contains the parameters to pass to the real main
 */
static void* Squawk_startup(void *options) {
        mainParameters* o = (mainParameters*)options;
        Squawk_main_wrapper(o->argc, o->argv);
}

/**
 * Call back for dummy source used to make sure the CFRunLoop doesn't exit right away
 * This callback is called when the source has fired.
 *
 * @param  info
 */
void sourceCallBack (void *info) {}

/** 
 * Return another path to find the bootstrap suite with the given name.
 * On some platforms the suite might be stored in an odd location
 * 
 * @param bootstrapSuiteName the name of the boostrap suite
 * @return full or partial path to alternate location, or null
 */
char* sysGetAlternateBootstrapSuiteLocation(char* bootstrapSuiteName) {
    if (mainBundle != null) {
        CFURLRef    suiteURL;
        // Look for a resource in the main bundle by name and type.

        suiteURL = CFBundleCopyResourceURL( mainBundle,
                        CFStringCreateWithCString(NULL, bootstrapSuiteName, kCFStringEncodingASCII),
                        NULL,
                        NULL );
        char pathBuf[512];
        if (CFURLGetFileSystemRepresentation(suiteURL, TRUE, (UInt8 *)pathBuf, 511)) {
            int len = strlen(pathBuf);
            char* result = (char*)malloc(len+1);
            return strcpy(result, pathBuf);
        } else {
            printf("CFURLGetFileSystemRepresentation failed\n");
        }
    }
    return NULL;
}

/**
 * Apple specific initialization of the IO subsystem. This function creates a new thread in which to run Squawk
 * and the embedded JVM and leaves the initial thread in an empty main loop.
 *
 * @param  argc  the command line arguments
 * @param  argv  number of command line arguments
 */
int os_main(int argc, char *argv[]) {
    int i;
    CFRunLoopSourceContext sourceContext;

    /* Grab parameters to pass to thread */
    mainParameters* launchOptions = (mainParameters*)malloc(sizeof(mainParameters));
    launchOptions->argc = argc;
    launchOptions->argv = argv;

    // Get the main bundle for the app
    mainBundle = CFBundleGetMainBundle();
    if (mainBundle != NULL /*&& argc > 1 && startsWith(argv[1], "-psn")*/) {
        // may or may not be app! fprintf(stderr, "Squawk started as application, instead of tool. Look for command line in Info.plist.\n");

        // testCmdStr2argv(launchOptions);

        readArgsFromPlist(launchOptions);
    }

    /* Start the thread that runs the VM. */
    pthread_t squawkThread;

    /* create a new pthread copying the stack size of the initial pthread */
    struct rlimit limit;
    size_t stack_size = 0;
    int rc = getrlimit(RLIMIT_STACK, &limit);
    if (rc == 0) {
        if (limit.rlim_cur != 0LL) {
            stack_size = (size_t)limit.rlim_cur;
        }
    }
    pthread_attr_t thread_attr;
    pthread_attr_init(&thread_attr);
    pthread_attr_setscope(&thread_attr, PTHREAD_SCOPE_SYSTEM);
    pthread_attr_setdetachstate(&thread_attr, PTHREAD_CREATE_DETACHED);
    if (stack_size > 0) {
        pthread_attr_setstacksize(&thread_attr, stack_size);
    }

    /* Start the thread that will execute the Squawk VM. */
    pthread_create(&squawkThread, &thread_attr, Squawk_startup, launchOptions);
    pthread_attr_destroy(&thread_attr);

    /* Create a a sourceContext to be used by our source that makes */
    /* sure the CFRunLoop doesn't exit right away */
    sourceContext.version = 0;
    sourceContext.info = NULL;
    sourceContext.retain = NULL;
    sourceContext.release = NULL;
    sourceContext.copyDescription = NULL;
    sourceContext.equal = NULL;
    sourceContext.hash = NULL;
    sourceContext.schedule = NULL;
    sourceContext.cancel = NULL;
    sourceContext.perform = &sourceCallBack;

    /* Create the Source from the sourceContext */
    CFRunLoopSourceRef sourceRef = CFRunLoopSourceCreate (NULL, 0, &sourceContext);

    /* Use the constant kCFRunLoopCommonModes to add the source to the set of objects */
    /* monitored by all the common modes */
    CFRunLoopAddSource (CFRunLoopGetCurrent(),sourceRef,kCFRunLoopCommonModes);

    /* Park this thread in the runloop */
    CFRunLoopRun();

    return 0;
}
