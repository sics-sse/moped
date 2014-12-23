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

#include <taskLib.h>

#define MAXARGS 11
#define LARGESTARG 64

#define CMD_LINE_FILENAME "SQUAWK_CMD_LINE"

#ifndef SQUAWK_VERSION
#define SQUAWK_VERSION "2010 FRC"
#endif

extern void freeBuffers();

void Priv_SetWriteFileAllowed(int);

int VXLOADARG(char* arg, char** argv, int argc) {
    if (arg != NULL) {              
        if (argc >= MAXARGS) {       
            fprintf(stderr, "[Squawk VM] too many args, skipping: %s\n", arg);
        } else {                     
            argv[argc] = arg;        
            argc++;                  
/*
            printf("[Squawk VM]    arg: %s\n", arg);
*/
        }
    }
    return argc;
}

/**
 * Called by OTA server after killing Squawk task
 * @TODO Should be called as part of normal shutdown, triggered by event from OTA server.
 */
void squawk_cleanup() {
    freeBuffers();
    IO_shutdown();
}

/**
 * Parse input from cmdFile into whitespace-seperated argument strings.
 * Comment lines begin with #
 *
 * @return fresh string or null if no more arguments.
 */
char* getnextArg(FILE* cmdFile) {
    int ch;
    int index = 0;
    char argbuf[LARGESTARG+1];

    ch = fgetc(cmdFile);
    while (true) {
        if (ch == '#') {
            while ((ch = fgetc(cmdFile)) != '\n') {
                // strip until end-of-line
            }
            ch = fgetc(cmdFile); // skip over \n
        } else if (ch == EOF || isspace(ch)) {
            if (index > 0) {
                // process arg
                char* copyarg = malloc(index + 1);
                argbuf[index] = '\0';
                strcpy(copyarg, argbuf);
                return copyarg;
            } else if (ch == EOF) {
                return NULL;
            }
            while (isspace(ch = fgetc(cmdFile))) {
                // strip extra spaces... terminate on EOF or non-space
            }
        } else {
            if (index >= LARGESTARG - 1) {
                argbuf[LARGESTARG - 1] = '\0';
                fprintf(stderr, "[Squawk VM] Argument too long: %s\n", argbuf);
                return NULL;
            }
            // buffer up argument until we see EOF or space:
            argbuf[index++] = ch;
            ch = fgetc(cmdFile);
        }
    }
}

/**
 * Entry point for the VxWorks operating system.
 */
int os_main(char* arg1, char* arg2, char* arg3, char* arg4, char* arg5) {
    // Convert from VxWorks argument format to normal argument format

    char* argv[MAXARGS];
    int argc = 0;
    FILE* cmdFile;

    argc = VXLOADARG("squawk.out", argv, argc);

    // load cmd line args from file
    cmdFile = fopen(CMD_LINE_FILENAME, "r");
    if (cmdFile != NULL) {
        fprintf(stderr, "[Squawk VM] Reading Squawk command line file " CMD_LINE_FILENAME "...\n");
        char* copyarg;
        while ((copyarg = getnextArg(cmdFile)) != NULL) {
            argc = VXLOADARG(copyarg, argv, argc);
        }
        fclose(cmdFile);
    } else {
/*
        fprintf(stderr, "[Squawk VM] Squawk command line file " CMD_LINE_FILENAME " not found, using default args...\n");
*/
        argc = VXLOADARG("-suite:robot", argv, argc);
        argc = VXLOADARG("-Xmxnvm:1M",   argv, argc);
    }

    // load programtic args (debugger commands, etc):
    argc = VXLOADARG(arg1, argv, argc);
    argc = VXLOADARG(arg2, argv, argc);
    argc = VXLOADARG(arg3, argv, argc);
    argc = VXLOADARG(arg4, argv, argc);
    argc = VXLOADARG(arg5, argv, argc);

    return Squawk_main_wrapper(argc, argv);
}

void robotTask() {
    os_main(null, null, null, null, null);
}

void robotTask_DEBUG() {
    os_main("com.sun.squawk.debugger.sda.SDA", "com.sun.squawk.imp.MIDletMainWrapper", "MIDlet-1", null, null);
}

void squawk_printVersion() {
	printf("\n[Squawk VM] Version: %s, %s, %s\n", SQUAWK_VERSION, __DATE__, __TIME__);
	fflush(stdout);
}

/**
 * Entry point used by FRC.
 */
int squawk_StartupLibraryInit(char* arg1, char* arg2, char* arg3, char* arg4, char* arg5, char* arg6, char* arg7, char* arg8, char* arg9, char* arg10) {
    int fd;
    FUNCPTR entryPt = (FUNCPTR)robotTask;
/*
    fprintf(stderr, "[Squawk VM] Starting up...\n");
*/

    cd("/c/ni-rt/system");

    Priv_SetWriteFileAllowed(1);

    squawk_printVersion();

    fd = open("SQUAWK_DEBUG_ENABLED", O_RDONLY);
    if (fd >= 0) {
        fprintf(stderr, "[Squawk VM] File SQUAWK_DEBUG_ENABLED found, starting squawk in debug mode...\n");
        entryPt = (FUNCPTR)robotTask_DEBUG;
        close(fd);
        remove("SQUAWK_DEBUG_ENABLED");
    } else {
/*
        fprintf(stderr, "[Squawk VM] File SQUAWK_DEBUG_ENABLED not found, starting squawk in normal mode...\n");
*/
    }

    /*
    if (strncmp("DEBUG",arg1,5) == 0) {
        fprintf(stderr, "Starting squawk in debug mode...");
        entryPt = (FUNCPTR)robotTask_DEBUG;
    } else {
        fprintf(stderr, "Starting squawk in normal mode...");
    }
    */

    
    // Start robot task
    // This is done to ensure that the C++ robot task is spawned with the floating point
    // context save parameter.
    int m_taskID = taskSpawn("SquawkRobotTask",
                                            100,
                                            VX_FP_TASK,						// options
                                            64000,						// stack size
                                            entryPt,						// function to start
                                            (int)arg1, (int)arg2, (int)arg3, (int)arg4, (int)arg5,     // parameter 1 - pointer to this class
                                            (int)arg6, (int)arg7, (int)arg8, (int)arg9, (int)arg10);   // additional unused parameters
/*
    bool ok = HandleError(m_taskID);
    if (!ok) m_taskID = kInvalidTaskID;
*/
    return 0;
}


/*

void RobotBase::robotTask(FUNCPTR factory, Task *task)
{
	RobotBase::setInstance((RobotBase*)factory());
	RobotBase::getInstance().m_task = task;
	RobotBase::getInstance().StartCompetition();
}

void RobotBase::startRobotTask(FUNCPTR factory)
{
	if (strlen(SVN_REV))
	{
		printf("WPILib was compiled from SVN revision %s\n", SVN_REV);
	}
	else
	{
		printf("WPILib was compiled from a location that is not source controlled.\n");
	}

	// Check for startup code already running
	INT32 oldId = taskNameToId("FRC_RobotTask");
	if (oldId != ERROR)
	{
		// Find the startup code module.
		MODULE_ID startupModId = moduleFindByName("FRC_UserProgram.out");
		if (startupModId != NULL)
		{
			// Remove the startup code.
			unldByModuleId(startupModId, 0);
			printf("!!!   Error: Default code was still running... Please try again.\n");
			return;
		}
		printf("!!!   Error: Other robot code is still running... Unload it and then try again.\n");
		return;
	}

	// Start robot task
	// This is done to ensure that the C++ robot task is spawned with the floating point
	// context save parameter.
	Task *task = new Task("RobotTask", (FUNCPTR)RobotBase::robotTask, Task::kDefaultPriority, 64000);
	task->Start((INT32)factory, (INT32)task);
}

*/

