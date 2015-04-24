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
 *  Defines used by PLATFORM_TYPE_NATIVE=true. Included early, before rts/io.c.
 */

#ifndef _IO_NATIVE_H
#define	_IO_NATIVE_H

#ifdef	__cplusplus
extern "C" {
#endif

#ifdef _MSC_VER
#include <winsock2.h> /* for some reason timeval is defined here */
#else
#include <sys/times.h>
#endif

#if (PLATFORM_TYPE_BARE_METAL == 0)
/*---------------------------------------------------------------------------*\
 *                                Time Conversions                           *
\*---------------------------------------------------------------------------*/

INLINE void ms2TimeVal(long long ms, struct timeval* tv) {
    tv->tv_sec = ms / 1000;
    tv->tv_usec = ((long)(ms % 1000)) * 1000;
};

INLINE long long timeVal2ms(struct timeval* tv) {
    return (long long)tv->tv_sec * 1000 + ((tv->tv_usec + 500) / 1000);
};

#ifndef _MSC_VER
INLINE void ms2TimeSpec(long long ms, struct timespec* ts) {
    ts->tv_sec = ms / 1000;
    ts->tv_nsec = ((long)(ms % 1000)) * 1000000;
};

INLINE long long timeSpec2ms(struct timespec* ts) {
    return (long long)ts->tv_sec * 1000 + ((ts->tv_nsec + 500000) / 1000000);
};

INLINE void timeSpec2TimeVal(struct timespec* ts, struct timeval* tv) {
    tv->tv_sec = ts->tv_sec;
    tv->tv_usec = (ts->tv_nsec + 500) / 1000;
};

INLINE void timeVal2TimeSpec(struct timeval* tv, struct timespec* ts) {
    ts->tv_sec  = tv->tv_sec;
    ts->tv_nsec = tv->tv_usec * 1000;
};
#endif

#define MAX_USEC (   1000000 - 1)
#define MAX_NSEC (1000000000 - 1)
#define MAX_SEC 0x7fffffff

INLINE void addTimeVal(struct timeval* tv_accum, struct timeval* tv_extra) {
    tv_accum->tv_sec += tv_extra->tv_sec;
    tv_accum->tv_usec += tv_extra->tv_usec;
    if (tv_accum->tv_usec > MAX_USEC) {
        tv_accum->tv_usec -= MAX_USEC;
        tv_accum->tv_sec++;
    }
    if (tv_accum->tv_sec < 0) {
        tv_accum->tv_sec = MAX_SEC;
    }
};

#ifndef _MSC_VER
INLINE void addTimeSpec(struct timespec* ts_accum, struct timespec* ts_extra) {
    ts_accum->tv_sec += ts_extra->tv_sec;
    ts_accum->tv_nsec += ts_extra->tv_nsec;
    if (ts_accum->tv_nsec > MAX_NSEC) {
        ts_accum->tv_nsec -= MAX_NSEC;
        ts_accum->tv_sec++;
    }
    if (ts_accum->tv_sec < 0) {
        ts_accum->tv_sec = MAX_SEC;
    }
};
#endif

#endif /* !PLATFORM_TYPE_BARE_METAL*/
/*---------------------------------------------------------------------------*\
 *                                  EventRequest                             *
\*---------------------------------------------------------------------------*/

/**
 * DEBUG_EVENTS_LEVEL can be 0, 1, 2, etc. */
#define DEBUG_EVENTS_LEVEL 0
#define DEBUG_MONITORS 0


#define EVENT_REQUEST_KIND_PLAIN 0
#define EVENT_REQUEST_KIND_NATIVE_TASK 1

/* eventStatus values.
 *    value <= 0 : getevent() should continue waiting.
 *    value > 0  : getevent() should return. event or error has occurred.
 */
typedef enum {
            EVENT_REQUEST_STATUS_STARTING = -1,
            EVENT_REQUEST_STATUS_RUNNING,
            EVENT_REQUEST_STATUS_DONE,
            EVENT_REQUEST_STATUS_ERROR} EventRequestStatus;
            
struct eventRequest {
        struct eventRequest *next;
        int eventNumber;
        EventRequestStatus eventStatus; /* zero until event or error occurred */
        int eventKind;                  /* if non-zero, then EventRequest is part of extended data structure */
};
typedef struct eventRequest EventRequest;

void signalEvent(EventRequest* evt);

/*---------------------------------------------------------------------------*\
 *                                NativeTask                                 *
\*---------------------------------------------------------------------------*/

#define FORCE_RESCHEDULE_FOR_NATIVE_EVENT 1

typedef void* NativeTaskID;

#define TASK_PRIORITY_LOW 3
#define TASK_PRIORITY_MED 2
#define TASK_PRIORITY_HI 1

/* function pointer used by NativeTask */
typedef (*TaskHandler)(int arg1, int arg2, int arg3, int arg4, int arg5,
                       int arg6, int arg7, int arg8, int arg9, int arg10);

/**
 * NativeTask defines the data needed to call a C function in another thread, and get a result back.
 * It is also an EventRequest - the java caller is going to wait until the task completes.
 *
 * WARNING: NativeUnsafe.java has hardcoded offsets for result, low_result, nt_errno, etc.
 *          These hardcoded values are validated at startup in IO_initialize().
 */
typedef struct {
    EventRequest event;

    /* Java code can read these directly via unsafe */
    int volatile result;     /* the result returned by called function (the hi 32-bits if a 64-bit result) */
    int volatile low_result; /* NOT USED YET: low bits of a 64-bit result */

    int volatile nt_errno;      /* the system error number */

    int arg1, arg2, arg3, arg4, arg5,
        arg6, arg7, arg8, arg9, arg10;

    TaskHandler handler;
} NativeTask;

#if PLATFORM_TYPE_NATIVE

typedef enum {
            TASK_EXECUTOR_STATUS_STARTING = 0,
            TASK_EXECUTOR_STATUS_RUNNING,
            TASK_EXECUTOR_STATUS_STOPPING,
            TASK_EXECUTOR_STATUS_DONE,
            TASK_EXECUTOR_STATUS_ERROR
            } TaskExecutorStatus;

typedef struct TaskExecutor_struct {
    struct SimpleMonitor_struct* monitor;
    NativeTask* volatile runQ;
    volatile TaskExecutorStatus status;
    int te_errno;
    NativeTaskID id;
} TaskExecutor;

/**
 * Create a new TaskExecutor and native thread.
 */
TaskExecutor* createTaskExecutor(char* name, int priority, int stacksize);

/**
 * Tell TaskExecutor to stop running
 */
static void cancelTaskExecutor(TaskExecutor* te);

/**
 * Delete TaskExecutor struct...
 */
static int deleteTaskExecutor(TaskExecutor* te);

int setTaskID(TaskExecutor* te);

void teLoopingHandler(TaskExecutor* te);

NativeTask* runBlockingFunctionOn(TaskExecutor* te, void* function,
            int arg1, int arg2, int arg3, int arg4, int arg5,
            int arg6, int arg7, int arg8, int arg9, int arg10);

void deleteNativeTask(NativeTask* ntask);

#else /* PLATFORM_TYPE_NATIVE */

typedef struct TaskExecutor_struct {
    int te_errno;
    NativeTaskID id;
} TaskExecutor;

NORETURN void fatalVMError(char *msg);

/* define some stubs...*/


TaskExecutor* createTaskExecutor(char* name, int priority, int stacksize) {
    fatalVMError("TaskExecutor not supported in this configuration");
    return NULL;
}
static void cancelTaskExecutor(TaskExecutor* te) {
    fatalVMError("TaskExecutor not supported in this configuration");
}
static int deleteTaskExecutor(TaskExecutor* te) {
    fatalVMError("TaskExecutor not supported in this configuration");
}

NativeTask* runBlockingFunction(void* function,
            int arg1, int arg2, int arg3, int arg4, int arg5,
            int arg6, int arg7, int arg8, int arg9, int arg10) {
    fatalVMError("NativeTasks not supported in this configuration");
    return NULL;

}

NativeTask* runBlockingFunctionOn(TaskExecutor* te, void* function,
            int arg1, int arg2, int arg3, int arg4, int arg5,
            int arg6, int arg7, int arg8, int arg9, int arg10) {
    fatalVMError("NativeTasks not supported in this configuration");
}

void deleteNativeTask(NativeTask* ntask) {
     fatalVMError("NativeTasks not supported in this configuration");
}

#endif /* PLATFORM_TYPE_NATIVE */

/*---------------------------------------------------------------------------*\
 *                               Select Pipe                                 *
\*---------------------------------------------------------------------------*/

/**
 * We use a blocking select call, and we need to unblock it when we add or remove items from
 * the read/write sets.
 * We create a pipe that our select will wait on. So native thread is waiting for select to complete,
 * and Java thread may write to the pipe when it wants to update the wait sets.
 *
 * The code in io_native hides the existence of the pipe form the higher-level IO code.
 */
int initSelectPipe();
int cleanupSelectPipe();
int readSelectPipeMsg();
int writeSelectPipeMsg();
int getSelectReadPipeFd();

#ifdef	__cplusplus
}
#endif

#endif	/* _IO_NATIVE_H */

