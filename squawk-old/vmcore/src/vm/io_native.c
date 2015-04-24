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


/*************** NOTE: this file is included when PLATFORM_TYPE_NATIVE=true **************************/

#ifdef _MSC_VER
#include <windows.h>
#else

#ifndef offsetof
#error "NO OFFSETOF"
#endif

#include <netdb.h>
#include <dlfcn.h>
#include <sys/stat.h>

/*#include "util.h"*/

#if defined(VXWORKS)
#include <vxWorks.h>
#include <msgQLib.h>
#include <msgQSmLib.h>
#include <stdio.h>
#include <smNameLib.h>

#include "netinet/in.h"

/*
#include <types/vxWorksCommon.h>
#include <in.h>
*/
#include <types/vxTypesOld.h>

/* can't get selectLib to load, resolving dependencies..
#include <selectLib.h>
 * so grab what we need (yuck!):
 */
#ifdef _WRS_KERNEL
extern int	    select 		(int width, fd_set *pReadFds,
					 fd_set *pWriteFds, fd_set *pExceptFds,
					 struct timeval *pTimeOut);
#else
#include <sys/select.h>
#endif

#else
#include <sys/select.h>
#endif

#endif


static volatile int io_shutting_down;

#define DEBUG_SELECT FALSE

/****** HARD CODED FOR MAC FOR NOW:  *************/


void sysFD_SET(int i1, fd_set* set) {
    FD_SET(i1, set);
}

void sysFD_CLR(int i1, fd_set* set) {
    FD_CLR(i1, set);
}

int sysFD_ISSET(int i1, fd_set* set) {
    return FD_ISSET(i1, set);
}

/*---------------------------- Structure Layouts ----------------------------*/

#define com_sun_squawk_platform_posix_callouts_Libc_Stat_layout_LEN 5
const int com_sun_squawk_platform_posix_callouts_Libc_Stat_layout[com_sun_squawk_platform_posix_callouts_Libc_Stat_layout_LEN] = {
    com_sun_squawk_platform_posix_callouts_Libc_Stat_layout_LEN, 
    sizeof(struct stat),
    offsetof(struct stat, st_mode),
    offsetof(struct stat, st_mtime),
    offsetof(struct stat, st_size)
};

#define com_sun_squawk_platform_posix_callouts_Socket_SockAddr_layout_LEN 6

#if defined(sun) || defined(_MSC_VER) || defined(linux)
const int _com_sun_squawk_platform_posix_natives_SocketImpl_sockaddr_inImpl_layout[com_sun_squawk_platform_posix_callouts_Socket_SockAddr_layout_LEN] = {
    com_sun_squawk_platform_posix_callouts_Socket_SockAddr_layout_LEN, 
    sizeof(struct sockaddr_in),
    -1,
    offsetof(struct sockaddr_in, sin_family),
    offsetof(struct sockaddr_in, sin_port),
    offsetof(struct sockaddr_in, sin_addr)
};
#else /* mac osx, vxworks */
const int _com_sun_squawk_platform_posix_natives_SocketImpl_sockaddr_inImpl_layout[com_sun_squawk_platform_posix_callouts_Socket_SockAddr_layout_LEN] = {
    com_sun_squawk_platform_posix_callouts_Socket_SockAddr_layout_LEN, 
    sizeof(struct sockaddr_in),
    offsetof(struct sockaddr_in, sin_len),
    offsetof(struct sockaddr_in, sin_family),
    offsetof(struct sockaddr_in, sin_port),
    offsetof(struct sockaddr_in, sin_addr)
};
#endif /* else */


int sysFD_SIZE; FORCE_USED
int sysSIZEOFSTAT; FORCE_USED

#include "io_native.h"

/*
 * Act like POSIX select, except when a write occurs on pipefd, simply return.
 *
 * Called as a BlockingFunction. May block indefinitely.
 */
int squawk_select(int nfds,
                fd_set * readfds, fd_set * writefds,
                fd_set * errorfds, struct timeval * timeout) {
    int pipefd = getSelectReadPipeFd();
    int maxfd = max(nfds, pipefd + 1);
    int res;
    if (DEBUG_SELECT) { fprintf(stderr, "blocking in squawk_select. nfds: %d, maxfd: %d\n", nfds, maxfd); }

    FD_SET(pipefd, readfds);
    res = select(maxfd, readfds, writefds, errorfds, timeout);
    if (res > 0) {
        if (FD_ISSET(pipefd, readfds)) {
	        if (DEBUG_SELECT) { fprintf(stderr, "squawk_select read pipe message\n"); }
            FD_CLR(pipefd, readfds);
            readSelectPipeMsg();
            res--;
        }
    }
    if (DEBUG_SELECT) { fprintf(stderr, "squawk_select returning with %d fd events\n", res); }

    /* on return, teLoopingHandler() will call signalEvent(), which will wake up Squawk thread (if needed), 
     * and wake up Java thread waiting for select to return.
     */
    return res;
};

/*
 * Called by a java thread when we need to bounce out of a blocking select call.
 * Writes a message to a well-known pipe.
 */
int cancel_squawk_select() {
    writeSelectPipeMsg();
};

/*---------------------------- Event Queue ----------------------------*/

/*
 * Stores some events that have not yet occurred, plus all events that have occurred.
 * @TODO: This is a polling mechanism. GetEvent has to search all event requests looking for events that occurred.
 *        May want to queue up events that have occurred. In any case, pay attention to linear searches, especially at 
 *        Event signalling time.
 */

EventRequest *eventRequests;

int nextEventNumber = 1;

/*
 * EventRequest has occured, so push it to the head of the list
 * WARNING: Must hold threadEventMonitor lock.
 */
void pushEventRequest(EventRequest* newRequest) {
    assumeAlways(newRequest != NULL);
    assumeAlways(newRequest->next == NULL);
    assumeAlways(threadEventMonitor->lockCount > 0); /* really want to assert that this thread holds the lock...*/
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "pushEventRequest\n"); }
    newRequest->next = eventRequests;
    eventRequests = newRequest;
}

/*
 * Signal that event has occured. Called by native thread.
 */
void signalEvent(EventRequest* evt) {
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "signalEvent() before lock\n"); }

    if (io_shutting_down) {
 //        fprintf(stderr, "signalEvent() while shutting down\n");
         return;
    }

    SimpleMonitorLock(threadEventMonitor);

    addedEvent = TRUE;
    evt->eventStatus = EVENT_REQUEST_STATUS_DONE;
    if (evt->eventKind == EVENT_REQUEST_KIND_NATIVE_TASK) {
        /* not on eventRequests yet. put it there now. */
        pushEventRequest(evt);
    }

    SimpleMonitorSignal(threadEventMonitor); /* wake up squawk thread if it was blocked in osMilliSleep(). */
    SimpleMonitorUnlock(threadEventMonitor);
#if FORCE_RESCHEDULE_FOR_NATIVE_EVENT
    bc = 0;
#endif
}

/*
 * Signal that error for event has occured. Called by native thread.
 */
void signalError(EventRequest* evt) {
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "signalError() before lock\n"); }

    if (io_shutting_down) {
 //        fprintf(stderr, "signalEvent() while shutting down\n");
         return;
    }

    SimpleMonitorLock(threadEventMonitor);

    addedEvent = TRUE;
    evt->eventStatus = EVENT_REQUEST_STATUS_ERROR;
    if (evt->eventKind == EVENT_REQUEST_KIND_NATIVE_TASK) {
        /* not on eventRequests yet. put it there now. */
        pushEventRequest(evt);
    }

    SimpleMonitorSignal(threadEventMonitor); /* wake up squawk thread if it was blocked in osMilliSleep(). */
    SimpleMonitorUnlock(threadEventMonitor);
}

/*
 * Find EventRequest by event number.
 * @return NULL if now event found.
 */
/*
EventRequest* findEvent(int eventNumber) {
    EventRequest* current = eventRequests;
    while (current != NULL) {
        if (current->eventNumber == eventNumber) {
            return current;
        } else {
            current = current->next;
        }
    }
   return NULL;
}
*/

static int getNextEventNumber() {
    int result = nextEventNumber++;
    if (result <= 0) {
        // @TODO  Statically assign event number to thread. Thread can only wait for one event at a time.
        //        Could lazily incr counter at first IO operation by thread, or search for unused ID (keeping IDs compact).
        fatalVMError("Reached event number limit");
    }
    return result;
}

#if 0
/*
 * If no other collection is holding event request, put it on eventRequests queue
 * (NOT YET USED)
 */
void registerEventRequest (EventRequest* newRequest) {
    assumeAlways(newRequest != NULL);
    assumeAlways(newRequest->eventKind != EVENT_REQUEST_KIND_NATIVE_TASK); /* only added when event occurs */
    SimpleMonitorLock(threadEventMonitor);

    if (eventRequests == NULL) {
        eventRequests = newRequest;
    } else {
        EventRequest* current = eventRequests;
        while (current->next != NULL) {
            current = current->next;
        }
        current->next = newRequest;
    }
    SimpleMonitorUnlock(threadEventMonitor);
}

/*
 * Java has requested wait for an event. Store the request,
 * and each time Java asks for events, signal the event if it has happened
 * (NOT YET USED)
 *
 * @return the event number
 */
int registerNewEventRequest () {
    EventRequest* newRequest = (EventRequest*)malloc(sizeof(EventRequest));
    if (newRequest == NULL) {
        //TODO set up error message for GET_ERROR and handle
        //one per channel and clean on new requests.
        return ChannelConstants_RESULT_EXCEPTION;
    }

    newRequest->eventNumber = getNextEventNumber();
    newRequest->eventKind = EVENT_REQUEST_KIND_PLAIN;
    newRequest->eventStatus = false;
    newRequest->next = NULL;

    registerEventRequest(newRequest);

    return newRequest->eventNumber;
}

/*
 * If there are outstanding events then return its eventNumber. Otherwise return 0
 */
int checkForEvents() {
    int result = 0;

    SimpleMonitorLock(threadEventMonitor);
    EventRequest* current = eventRequests;
    while (current != NULL) {
        if (current->eventStatus) {
            result = current->eventNumber;
            break;
        } else {
            current = current->next;
        }
    }
    SimpleMonitorUnlock(threadEventMonitor);
    return result;
}
#endif

static void printOutstandingEvents() {
    SimpleMonitorLock(threadEventMonitor);
    EventRequest* current = eventRequests;
    while (current != NULL) {
    	diagnosticWithValue("event request id", current->eventNumber);
    	current = current->next;
    }
    SimpleMonitorUnlock(threadEventMonitor);
}

INLINE NativeTask* toNativeTask(EventRequest* eventRequest) {
    assume(eventRequest == NULL || eventRequest->eventKind == EVENT_REQUEST_KIND_NATIVE_TASK);
    return (NativeTask*)eventRequest;
}

INLINE EventRequest* toEventRequest(NativeTask* eventRequest) {
    return (EventRequest*)eventRequest;
}
/*
 * If there are outstanding event then remove the event from the queue and return its eventNumber.
 * If no requests match the interrupt status return 0.
 *
 * Does linear search.
 */
int getEvent() {
    EventRequest* current = NULL;
    EventRequest* previous = NULL;
    if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "getEvent() before lock. 0x%p\n", eventRequests); }

    if (eventRequests == NULL) {
        /* bail out early. try again later */
        return 0;
    }

    SimpleMonitorLock(threadEventMonitor);
    if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "getEvent() after lock\n"); }
    current = eventRequests;
    addedEvent = FALSE;

    while (current != NULL) {
        if (current->eventStatus >= EVENT_REQUEST_STATUS_DONE) {
            int res = current->eventNumber;
            //unchain
            if (previous == NULL) {
                eventRequests = current->next;
            } else {
                previous->next = current->next;
            }
            current->next = NULL;
            SimpleMonitorUnlock(threadEventMonitor);

            /* TODO: Record and signal to java when eventStatus == EVENT_REQUEST_STATUS_ERROR*/

            switch (current->eventKind) {
                case EVENT_REQUEST_KIND_PLAIN: {
                    free(current);
                    break;
                }
                case EVENT_REQUEST_KIND_NATIVE_TASK: {
                    /* these events have state that must be preserved, so don't delete until Java code asks for results. */
                    break;
                }
                default: {
                    shouldNotReachHere();
                }
            }
	    	if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "getEvent() returning %d\n", res); }
            return res;
        } else {
            previous = current;
            current = current->next;
        }
    }
    SimpleMonitorUnlock(threadEventMonitor);
    if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "getEvent() returning 0\n"); }
    return 0;
}

/*---------------------------- TaskExecutor support ----------------------------*/
#define DEFAULT_STACK (16 * 1024)

/**
 * Tell TaskExecutor to stop running
 */
static void cancelTaskExecutor(TaskExecutor* te) {
     if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "cancelTaskExecutor() before lock\n"); }
     SimpleMonitorLock(te->monitor);
     te->status = TASK_EXECUTOR_STATUS_STOPPING;
     SimpleMonitorSignal(te->monitor);  /* TaskExecutor may be blocked in wait, so wake it up */
     SimpleMonitorUnlock(te->monitor);
    /* TODO: Record TaskExecutor on global list of all TaskExecutors? */
}

/**
 * Add native task "ntask" to the TaskExecutor's run queue.
 */
static int addTaskToExecutor(TaskExecutor* te, NativeTask* ntask) {
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "addTaskToExecutor() before lock\n"); }
    SimpleMonitorLock(te->monitor);
    if (te->status > TASK_EXECUTOR_STATUS_RUNNING) { /* STOPPING, DONE or ERROR */
        SimpleMonitorUnlock(te->monitor);
        return -1;
    }
    ntask->event.next = NULL;

    if (te->runQ == NULL) {
        te->runQ = ntask;
    } else {
        NativeTask* current = te->runQ;
        while (current->event.next != NULL) {
            current = toNativeTask(current->event.next);
        }
        current->event.next = toEventRequest(ntask);
    }
    if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "addTaskToExecutor() before signal\n"); }

    SimpleMonitorSignal(te->monitor);
    SimpleMonitorUnlock(te->monitor);
    return 0;
}

/**
 * Get the next task on the run queue, blocking until task is ready.getn
 * If TaskExecutor was cancelled, return NULL.
 */
static NativeTask* getNextTask(TaskExecutor* te) {
    SimpleMonitorLock(te->monitor);

    while (TRUE) {
        NativeTask* ntask;

        if (te->status != TASK_EXECUTOR_STATUS_RUNNING) { /* we've been cancelled! */
            if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "getNextTask() we've been cancelled!\n"); }
            SimpleMonitorUnlock(te->monitor);
            return NULL;
        }

        ntask = te->runQ;
        if (ntask) {
            te->runQ = toNativeTask(ntask->event.next);
            ntask->event.next = NULL;
            SimpleMonitorUnlock(te->monitor);
            //diagnosticWithValue("getNextTask() result: ", ntask);
            return ntask;
        } else {
            if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "getNextTask() before wait\n"); }
            SimpleMonitorWait(te->monitor, -1);  /* no task yet, wait */
            if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "getNextTask() after wait\n"); }
        }
    }
}

/**
 * A thread's handler that runs any NativeTasks that are placed in the run queue.
 * This will loop, waiting for NativeTasks to show up on the runQ.
 * Loop
 */
void teLoopingHandler(TaskExecutor* te) {
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "in teLoopingHandler() %p\n", te); }
    
    assume(te && te->status == TASK_EXECUTOR_STATUS_STARTING);
    setTaskID(te);
    te->status = TASK_EXECUTOR_STATUS_RUNNING;
    while (TRUE) {
        NativeTask* ntask = getNextTask(te);
        if (ntask) {
            if (DEBUG_EVENTS_LEVEL > 1) { fprintf(stderr, "in teLoopingHandler() calling handler %p\n", ntask->handler); }
            ntask->result = (*ntask->handler)(ntask->arg1, ntask->arg2, ntask->arg3, ntask->arg4, ntask->arg5, ntask->arg6, ntask->arg7, ntask->arg8, ntask->arg9, ntask->arg10);
            ntask->nt_errno = errno;
            signalEvent(toEventRequest(ntask)); /* tell squawk thread that native function result is ready */
        } else {
            assumeAlways(te->status != TASK_EXECUTOR_STATUS_RUNNING); /* we've been cancelled! */
            if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "in teLoopingHandler() cancelled, now cleanup \n"); }
            /* Cancel any pending tasks on runQ. 
             * no new tasks can be added unless status == TASK_EXECUTOR_STATUS_RUNNING, so can run without lock.
             */
            ntask = te->runQ;
            te->runQ = NULL;
            if (te->status < TASK_EXECUTOR_STATUS_DONE) {
                te->status = TASK_EXECUTOR_STATUS_DONE;
            }
            /* te may be deleted by Java thread after this point, so don't ref te: */
            te = NULL;

            while (ntask) {
                NativeTask* nextTask = toNativeTask(ntask->event.next);
                ntask->event.next = NULL;
                signalError(toEventRequest(ntask)); /* give task back to squawk thread */
                ntask = nextTask;
            }
            break;
        }
    }
}

/*---------------------------- Blocking native call support ----------------------------*/

static NativeTask* newNativeTask(TaskHandler handler, int eventNumber,
                                                int arg1, int arg2, int arg3, int arg4, int arg5,
                                                int arg6, int arg7, int arg8, int arg9, int arg10) {
    /* TODO : think about pooling instead of malloc */
    NativeTask* ntask = (NativeTask*)malloc(sizeof(NativeTask));
    if (ntask == NULL) {
        return NULL;
    }
    ntask->event.next = NULL;
    ntask->event.eventNumber = eventNumber;
    ntask->event.eventStatus = EVENT_REQUEST_STATUS_STARTING;
    ntask->event.eventKind = EVENT_REQUEST_KIND_NATIVE_TASK;

    ntask->handler = handler;
    ntask->result = 0;
    ntask->low_result = 0;
    ntask->nt_errno = 0;

    ntask->arg1 = arg1;
    ntask->arg2 = arg2;
    ntask->arg3 = arg3;
    ntask->arg4 = arg4;
    ntask->arg5 = arg5;
    ntask->arg6 = arg6;
    ntask->arg7 = arg7;
    ntask->arg8 = arg8;
    ntask->arg9 = arg9;
    ntask->arg10 = arg10;
    return ntask;
}

/**
 * Run a blocking C function with the given arguments on the specified thread (TaskExecutor).
 *
 * A java thread can wait for the function to complete by waiting for the "eventNumber", and can retrieve the
 * function result via NativeTask->result.
 *
 * This is intended to call C code that is not tightly integrated with the Squawk VM.
 * The "handler" doesn't have to worry about event numbers, task ids, or signalling events.
 */
NativeTask* runBlockingFunctionOn(TaskExecutor* te, void* function,
            int arg1, int arg2, int arg3, int arg4, int arg5,
            int arg6, int arg7, int arg8, int arg9, int arg10) {
    int rc;
    int eventNumber = getNextEventNumber();

    NativeTask* ntask = newNativeTask(function, eventNumber,
                                      arg1, arg2, arg3, arg4, arg5,
                                      arg6, arg7, arg8, arg9, arg10);
     if (ntask == NULL) {
        return NULL;
    }

    rc = addTaskToExecutor(te, ntask);
    if (rc != 0) {
        ntask->event.eventStatus = EVENT_REQUEST_STATUS_ERROR;
        ntask->nt_errno = errno;
    }

    return ntask;
}

void deleteNativeTask(NativeTask* ntask) {
    assumeAlways(ntask->event.next == NULL); /* must not be in list */
    assumeAlways(ntask->event.eventStatus > 0);
    free(ntask);
}

/*---------------------------- IO Impl ----------------------------*/
/* void ms2TimeVal(long long ms, struct timeval* tv)
 long long timeVal2ms(struct timeval* tv)
 void ms2TimeSpec(long long ms, struct timespec* ts)
 long long timeSpec2ms(struct timespec* ts)
 void timeSpec2TimeVal(struct timespec* ts, struct timeval* tv)
 void timeVal2TimeSpec(struct timeval* tv, struct timespec* ts)
 void addTimeVal(struct timeval* tv_accum, struct timeval* tv_extra) ;
 void addTimeSpec(struct timespec* ts_accum, struct timespec* ts_extra);
 */

int ioInitialized = FALSE;

/**
 * Initializes the IO subsystem.
 */
void IO_initialize() {
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "In IO_initialize\n"); }
    threadEventMonitor = SimpleMonitorCreate();
    io_shutting_down = FALSE;
    addedEvent = FALSE;
    sysFD_SIZE = sizeof(fd_set);
    sysSIZEOFSTAT = sizeof(struct stat);

    assumeAlways(offsetof(NativeTask, event.eventNumber) == (com_sun_squawk_NativeUnsafe_NATIVE_TASK_EVENTID_OFFSET * 4));
    assumeAlways(offsetof(NativeTask, result) == (com_sun_squawk_NativeUnsafe_NATIVE_TASK_RESULT_OFFSET * 4));
    assumeAlways(offsetof(NativeTask, low_result) == (com_sun_squawk_NativeUnsafe_NATIVE_TASK_LOW_RESULT_OFFSET * 4));
    assumeAlways(offsetof(NativeTask, nt_errno) == (com_sun_squawk_NativeUnsafe_NATIVE_TASK_NT_ERRNO_RESULT_OFFSET * 4));

#ifndef _MSC_VER
    jlong t1 = 500; /* ms */
    jlong t2 = 0;
    struct timeval tval, tval2;
    struct timespec tspec, tspec2;

    ms2TimeVal(t1, &tval);
    timeVal2TimeSpec(&tval, &tspec);
    t2 = timeSpec2ms(&tspec);
    if (t1 != t2) {
        fprintf(stderr, "conversions: Expected %lld, got %lld\n", t1, t2);
    }

    t2 = 0;
    ms2TimeVal(t2, &tval2);
    addTimeVal(&tval, &tval2);
    t2 = timeVal2ms(&tval);
    if (t1 != t2) {
        fprintf(stderr, "add timeval: Expected %lld, got %lld\n", t1, t2);
    }


    t2 = 0;
    ms2TimeSpec(t2, &tspec2);
    addTimeSpec(&tspec, &tspec2);
    t2 = timeSpec2ms(&tspec);
    if (t1 != t2) {
        fprintf(stderr, "add timespec: Expected %lld, got %lld\n", t1, t2);
    }
#endif

    initSelectPipe();
    ioInitialized = TRUE;
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "Done IO_initialize\n"); }
}

/**
 * Cleanup the IO subsystem.
 */
void IO_shutdown() {
    io_shutting_down = TRUE;
    if (!ioInitialized) {
        return;
    }
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "In IO_shutdown\n"); }
    cleanupSelectPipe();

    printOutstandingEvents();
    eventRequests = NULL;

    SimpleMonitorDestroy(threadEventMonitor);
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "Done IO_shutdown\n"); }
}

/******* per-context data ************/

/* From ChannelIO.java:
    private int context;
    private boolean rundown;
    private SerializableIntHashtable channels = new SerializableIntHashtable();
    private int nextAvailableChannelID = ChannelConstants.CHANNEL_LAST_FIXED + 1;
    private String exceptionClassName;
    private long theResult;
*/

char* exceptionClassName;

long long retValue = 0;  // holds the value to be returned on the next "get result" call

/**
 * Registers an exception that occurred on a non-channel specific call to this IO system.
 *
 * @param exceptionClassName   the name of the class of the exception that was raised
 * @return the negative value returned to the Squawk code indicating both that an error occurred
 *         as well as the length of the exception class name
 */
int raiseException(char* theExceptionClassName) {
    if (*theExceptionClassName == 0) {
        theExceptionClassName = "?raiseException?";
    }
    exceptionClassName = theExceptionClassName;
    return ChannelConstants_RESULT_EXCEPTION;
}

/**
 * Get the next character of the error.
 *
 * @return the next character or 0 if none remain
 */
int getError() {
    if (exceptionClassName != NULL) {
        int ch = *exceptionClassName;
        if (ch != '\0') {
            exceptionClassName++;
        } else {
            exceptionClassName = null;
        }
        return ch;
    }
    return 0;
}

/**
 * for timing purposes
 */
void squawk_dummy_func() {

}

/* INTERNAL DYNAMIC SYMBOL SUPPORT */
typedef struct dlentryStruct {
    const char* name;
    void* entry;
} dlentry;

static dlentry dltable[] = {
    {"sysFD_SIZE",      &sysFD_SIZE},
    {"sysSIZEOFSTAT",   &sysSIZEOFSTAT},
    {"sysFD_CLR",       &sysFD_CLR},
    {"sysFD_SET",       &sysFD_SET},
    {"sysFD_ISSET",     &sysFD_ISSET},
    {"com_sun_squawk_platform_posix_callouts_Libc_Stat_layout", (void*)&com_sun_squawk_platform_posix_callouts_Libc_Stat_layout},
    {"_com_sun_squawk_platform_posix_natives_SocketImpl_sockaddr_inImpl_layout", (void*)&_com_sun_squawk_platform_posix_natives_SocketImpl_sockaddr_inImpl_layout},
    {"squawk_select",   &squawk_select},
    {"cancel_squawk_select", &cancel_squawk_select},
    {"squawk_dummy_func", &squawk_dummy_func},
};

#define DL_TABLE_SIZE (sizeof(dltable) / sizeof(dlentry))
    
#ifndef USE_CUSTOM_DL_CODE
#define sys_RTLD_DEFAULT() RTLD_DEFAULT

void* sysdlopen(char* name) {
    return dlopen(name, RTLD_LAZY);
}

int sysdlclose(void* handle) {
    return dlclose(handle);
}

void* sysdlerror() {
    return dlerror();
}

#endif /* !USE_CUSTOM_DL_CODE */

void* sysdlsym(void* handle, char* name) {
    int i;
    for (i = 0; i < DL_TABLE_SIZE; i++) {
        if (strcmp(name, dltable[i].name) == 0) {
            return dltable[i].entry;
        }
    }

    if (handle == 0) {
        handle = sys_RTLD_DEFAULT();
    }
    
    return (void*)dlsym(handle, name);
}

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
    int     op      = com_sun_squawk_ServiceOperation_op;
    int     i1      = com_sun_squawk_ServiceOperation_i1;
    int     i2      = com_sun_squawk_ServiceOperation_i2;

    int res = ChannelConstants_RESULT_OK;
    switch (op) {

        /*--------------------------- GLOABL OPS ---------------------------*/
    	case ChannelConstants_GLOBAL_CREATECONTEXT:  {
            res = 1; //let all Isolates share a context for now
            break;
        }

    	case ChannelConstants_GLOBAL_GETEVENT: {
            res = getEvent();
            // improve fairness of thread scheduling - see bugzilla #568
            // @TODO: Check that bare-metal version is OK: It unconditionally resets the bc.
            //        This can give current thread more time, if there was no event.
            //        better idea is to give new thread new quanta in threadswitch?
            if (res) {
                bc = -TIMEQUANTA;
            }
            break;
        }

    	case ChannelConstants_GLOBAL_WAITFOREVENT: {
            long long millisecondsToWait = makeLong(i1, i2);
            osMilliSleep(millisecondsToWait);
            res = 0;
            break;
    	}

    	// case ChannelConstants_GLOBAL_POSTEVENT: // handled by external process, not Squawk

        /*--------------------------- CONTEXT OPS ---------------------------*/
    	case ChannelConstants_CONTEXT_DELETE: {
            // TODO delete all the outstanding events on the context
            // But will have to wait until we have separate contexts for each isolate
            res=0;
            break;
        }

    	case ChannelConstants_CONTEXT_GETERROR: {
            res = getError();
            break;
        }
    		
        case ChannelConstants_CONTEXT_HIBERNATE: {
            // TODO this is faked, we have no implementation currently.
            res = ChannelConstants_RESULT_OK;
            break;
        }

        case ChannelConstants_CONTEXT_GETHIBERNATIONDATA: {
            // TODO this is faked, we have no implementation currently.
            res = ChannelConstants_RESULT_OK;
            break;
        }

    	case ChannelConstants_CONTEXT_GETRESULT: {
            res = (int)retValue;
            break;
        }

    	case ChannelConstants_CONTEXT_GETRESULT_2: {
            res = (int)(retValue >> 32);
            retValue = 0;
            break;
        }

        /*--------------------------- POSIX NATIVE OPS ---------------------------*/

        case ChannelConstants_DLOPEN: {
            res = (int)sysdlopen((char*)i1);
            break;
        }

        case ChannelConstants_DLCLOSE: {
            res = (int)sysdlclose((void*)i1);
            break;
        }

        case ChannelConstants_DLERROR: {
            res = (int)sysdlerror();
            break;
        }

        case ChannelConstants_DLSYM: {
            res = (int)sysdlsym((void*) i1, (char*)i2);
            break;
        }

        /*--------------------------- CHANNEL OPS ---------------------------*/

        default: {
            res = ChannelConstants_RESULT_BADPARAMETER;
        }
    }
    com_sun_squawk_ServiceOperation_result = res;
}

#if KERNEL_SQUAWK
/**
 * Posts an event via ChannelIO to wake up any waiters.
 */
static void ioPostEvent(void) {
    /*
     * Check if there is no embedded JVM.
     */
    void os_postEvent(boolean notify);
    os_postEvent(true);
}
#endif
