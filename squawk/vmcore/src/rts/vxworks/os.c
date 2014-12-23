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

/* The package that contains the native code to use for a "NATIVE" platform type*/
#define sysPlatformName() "vxworks"

#include <stdlib.h>
#include <sys/times.h>
#include <symLib.h>
#include <sysSymTbl.h>
#include <sys/mman.h>
#include "jni.h"
#include <semLib.h>
#include <taskLib.h>
#include <ioLib.h>

#define open(filename, flags) open(filename, flags, 0644)


#define jlong  int64_t

#define hasMemProtection FALSE

#if defined(ASSUME) && ASSUME != 0
#define sysAssume(x) if (!(x))  { fprintf(stderr, "Assertion failed: \"%s\", at %s:%d\n", #x, __FILE__, __LINE__);  }
#else
#define sysAssume(x) /**/
#endif /* ASSUME */

#define sysAssumeAlways(x) if (!(x))  { fprintf(stderr, "Assertion failed: \"%s\", at %s:%d\n", #x, __FILE__, __LINE__); }

/**
 * Support for util.h
 */

/**
 * Gets the page size (in bytes) of the system.
 *
 * @return the page size (in bytes) of the system
 */
int sysGetPageSize() {
    return vmPageSizeGet(); /*     0x1000 */
}

/**
 * Sets a region of memory read-only or reverts it to read & write.
 *
 * @param start    the start of the memory region
 * @param end      one byte past the end of the region
 * @param readonly specifies if read-only protection is to be enabled or disabled
 */
void sysToggleMemoryProtection(char* start, char* end, boolean readonly) {
    //fprintf(stderr, "mprotect() is not supported.  Not protecting memory at %#0.8x\n", start);
}

/**
 * Allocate a page-aligned chunk of memory of the given size.
 *
 * @param size size in bytes to allocate
 * @return pointer to allocated memory or null.
 */
INLINE void* sysValloc(size_t size) {
    if (hasMemProtection) {
        return valloc(size);
    } else {
        return malloc(size);
    }
}

/**
 * Free chunk of memory allocated by sysValloc
 *
 * @param ptr to to chunk allocated by sysValloc
 */
INLINE void sysVallocFree(void* ptr) {
    free(ptr);
}

/**
 * Return another path to find the bootstrap suite with the given name.
 * On some platforms the suite might be stored in an odd location
 *
 * @param bootstrapSuiteName the name of the boostrap suite
 * @return full or partial path to alternate location, or null
 */
INLINE char* sysGetAlternateBootstrapSuiteLocation(char* bootstrapSuiteName) {
    /* TODO: May want to do something more to find squawk.suite reliably*/
    return NULL;
}


/* ----------------------- Time Support ------------------------*/

jlong sysTimeMicros() {
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);

    /* We adjust to 1000 ticks per second */
    return ((jlong)ts.tv_sec * 1000000) + ((ts.tv_nsec + 500) / 1000);
}

jlong sysTimeMillis(void) {
    struct timespec ts;
    clock_gettime(CLOCK_REALTIME, &ts);
    return timeSpec2ms(&ts);
}

/* ----------------------- Monitor Support ------------------------*/

/*
 * SimpleMonitors supports the single-reader, multiple-writer scenario used by thread
 * scheduling:
 *  - The Squawk task may do a timed condvar wait if there's nothing else to do.
 *  - Other native tasks may signal the Squawk task to indicate that some event
 *    has occured (IO, blocking native call finished, etc.)
 */

#if 0
/* mimic POSIX condvar in VxWorks */
typedef struct SimpleMonitor_struct {
    SEM_ID mu;
    SEM_ID cv;
    int lockCount;
} SimpleMonitor;

int MAX_SIMPLE_CONDVAR_WAIT_MS;

/* why can't I find MAXINT? */
#define SQUAWK_MAXINT 0x7FFFFFFF

SimpleMonitor* SimpleMonitorCreate() {
    int ticksPerMs = CLOCKS_PER_SEC / 1000;
    MAX_SIMPLE_CONDVAR_WAIT_MS = SQUAWK_MAXINT / ticksPerMs;
    SimpleMonitor* mon = (SimpleMonitor*)malloc(sizeof(SimpleMonitor));
    mon->mu = semBCreate(SEM_Q_PRIORITY, SEM_FULL);
    mon->cv = semBCreate(SEM_Q_PRIORITY, SEM_EMPTY);
    mon->lockCount = 0;
    return mon;
}

/** return zero on success. */
int SimpleMonitorDestroy(SimpleMonitor* mon) {
    sysAssumeAlways(!mon->lockCount);
    semDelete(mon->mu);
    semDelete(mon->cv);
    free(mon);
    return 0;
}

/* Wait for signal or timeout in millis milliseconds.
 * A neg. timout indicates WAIT_FOREVER.
 * Returns true if received signal, false if timed out.
 */
int SimpleMonitorWait(SimpleMonitor* mon, jlong millis) {
    int res;

    SimpleMonitorUnlock(mon);
    if (millis < 0) {
        res = semTake(mon->cv, WAIT_FOREVER);
    } else {
        int ticksPerMs = CLOCKS_PER_SEC / 1000;
        jlong remaining = millis;
        jlong start = sysTimeMillis();

 fprintf(stderr, "waiting in SimpleMonitorWait: %llx\n", millis);
        while (true) {
            int ticks = SQUAWK_MAXINT;
            if (remaining < MAX_SIMPLE_CONDVAR_WAIT_MS) {
                ticks = ((int)remaining * ticksPerMs);
            }

            res = semTake(mon->cv, ticks);
            if (res == ERROR) {
                if (errno == S_objLib_OBJ_TIMEOUT) {
                    remaining = millis - (sysTimeMillis() - start);
                    if (remaining > 0) {
                        fprintf(stderr, "keeping waiting in SimpleMonitorWait: %llx\n", remaining);
                    }
                    continue; /* keep waiting */
                } else {
                    fprintf(stderr, "unexpected errno in semTake: %d\n", errno);
                }
            }
            break;
        }
    }
    SimpleMonitorLock(mon);
    
    return res == OK;
}

/* Signal the condvar.
 * return zero on success.
 */
int SimpleMonitorSignal(SimpleMonitor* mon) {
    sysAssumeAlways(mon->lockCount);
    return semGive(mon->cv);
}

int SimpleMonitorLock(SimpleMonitor* mon) {
    int res = semTake(mon->mu, WAIT_FOREVER);
    mon->lockCount++;
    return res;
}

int SimpleMonitorUnlock(SimpleMonitor* mon) {
    sysAssumeAlways(mon->lockCount);
    mon->lockCount--;
    return semGive(mon->mu);
}
#endif

#include <pthread.h>

/*
 * SimpleMonitors supports the single-reader, multiple-writer scenario used by thread
 * scheduling:
 *  - The Squawk task may do a timed condvar wait if there's nothing else to do.
 *  - Other native tasks may signal the Squawk task to indicate that some event
 *    has occured (IO, blocking native call finished, etc.)
 */

typedef struct SimpleMonitor_struct {
    pthread_mutex_t mu;
    pthread_cond_t cv;
    volatile int lockCount;
} SimpleMonitor;

SimpleMonitor* threadEventMonitor = NULL;

static char* monitorName(SimpleMonitor* mon) {
    sysAssumeAlways(mon);
    if (mon == threadEventMonitor) {
        return "threadEventMonitor";
    } else if (threadEventMonitor == NULL) {
        return "too early to tell monitor name";
    } else {
        return "a TaskExecutor monitor";
    }
}

INLINE void monitorErrCheck(SimpleMonitor* mon, char* msg, int res, int expectedValue) {
    if (res != 0 && res != expectedValue) {
        fprintf(stderr, "unexpected result in %s on %s: %d. errno: %d \n", msg, monitorName(mon), res, errno);
    }
}

/**
 * Return true if the moutex is locked, false otherwise.
 * Non-blocking.
 */
int SimpleMonitorIsLocked(SimpleMonitor* mon) {
    if (pthread_mutex_trylock(&mon->mu) == EBUSY) {
        return TRUE;
    } else {
        pthread_mutex_unlock(&mon->mu);
        return FALSE;
    }
}

SimpleMonitor* SimpleMonitorCreate() {
    SimpleMonitor* mon = (SimpleMonitor*)malloc(sizeof(SimpleMonitor));
    int res;
    if (mon == NULL) {
        fprintf(stderr, "out of memory in SimpleMonitorCreate\n");
        return NULL;
    }
    res = pthread_mutex_init(&(mon->mu), NULL);
    monitorErrCheck(mon, "SimpleMonitorCreate mutex", res, 0);
    res = pthread_cond_init(&(mon->cv), NULL);
    monitorErrCheck(mon, "SimpleMonitorCreate condvar", res, 0);
    mon->lockCount = 0;
    sysAssume(!SimpleMonitorIsLocked(mon));
    if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorCreate on %s\n", monitorName(mon)); }
    return mon;
}

/** Deletes the SimpleMonitor and the underlaying OS structures.
 *  return zero on success.
 */
int SimpleMonitorDestroy(SimpleMonitor* mon) {
    int res;
    sysAssumeAlways(!mon->lockCount);
    sysAssume(!SimpleMonitorIsLocked(mon));
    res = pthread_mutex_destroy(&(mon->mu));
    monitorErrCheck(mon, "SimpleMonitorDestroy mutex", res, 0);
    res = pthread_cond_destroy(&(mon->cv));
    monitorErrCheck(mon, "SimpleMonitorDestroy condvar", res, 0);
    mon->lockCount = -1;
    if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorDestroy on %s\n", monitorName(mon)); }
    free(mon);
    return 0;
}

/* Wait for signal or timeout in millis milliseconds.
 * A neg. timout indicates WAIT_FOREVER.
 * Returns true if received signal, false if timed out.
 */
int SimpleMonitorWait(SimpleMonitor* mon, jlong millis) {
    int res;
    sysAssumeAlways(mon->lockCount == 1);
    sysAssume(SimpleMonitorIsLocked(mon));

    if (millis < 0 || millis == 0x7FFFFFFFFFFFFFFFLL) {
        if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorWait on %s - untimed\n", monitorName(mon)); }
        mon->lockCount--;
        res = pthread_cond_wait(&mon->cv, &mon->mu);
        mon->lockCount++;
        monitorErrCheck(mon, "SimpleMonitorWait", res, 0);
    } else {
        struct timespec abstime;
        struct timespec duration;
        if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorWait on %s - timed\n", monitorName(mon)); }

        ms2TimeSpec(millis, &duration);
        clock_gettime(CLOCK_REALTIME, &abstime);
        addTimeSpec(&abstime, &duration);
        mon->lockCount--;
        res = pthread_cond_timedwait(&mon->cv, &mon->mu, &abstime);
        mon->lockCount++;
        monitorErrCheck(mon, "SimpleMonitorWait", res, ETIMEDOUT);
    }

    sysAssume(SimpleMonitorIsLocked(mon));
    return res == 0;
}

/* Signal the condvar.
 * return zero on success.
 */
int SimpleMonitorSignal(SimpleMonitor* mon) {
    int res;
    if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorSignal on %s\n", monitorName(mon)); }
    sysAssumeAlways(mon->lockCount);
    sysAssume(SimpleMonitorIsLocked(mon));
    res = pthread_cond_signal(&mon->cv);
    monitorErrCheck(mon, "SimpleMonitorSignal", res, 0);
    sysAssume(SimpleMonitorIsLocked(mon));
    return res;
}

int SimpleMonitorLock(SimpleMonitor* mon) {
    if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorLock on %s  - before\n", monitorName(mon)); }
    int res = pthread_mutex_lock(&mon->mu);
    monitorErrCheck(mon, "SimpleMonitorLock", res, 0);
    if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorLock on %s  - after\n", monitorName(mon)); }
    sysAssume(SimpleMonitorIsLocked(mon));
    if (mon->lockCount) {
        fprintf(stderr, "SimpleMonitorLock on %s already locked? count = %d\n", monitorName(mon), mon->lockCount );
    }
    sysAssumeAlways(mon->lockCount == 0);
    mon->lockCount++;
    return res;
}

int SimpleMonitorUnlock(SimpleMonitor* mon) {
    sysAssumeAlways(mon->lockCount == 1);
    sysAssume(SimpleMonitorIsLocked(mon));
    mon->lockCount--;
    sysAssumeAlways(mon->lockCount == 0);
    if (DEBUG_MONITORS) { fprintf(stderr, "SimpleMonitorUnlock on %s \n", monitorName(mon)); }

    int res = pthread_mutex_unlock(&mon->mu);
    monitorErrCheck(mon, "SimpleMonitorUnlock", res, 0);
    return res;
}

/* ----------------------- Sleep Support ------------------------*/

/**
 * addedEvent is set TRUE by multiple writers (native threads) when adding evt, and read by the Squawk thread
 * in osMilliSleep and cleared by the Squawk thread in getEvent().
 */
volatile int addedEvent;

/**
 * VxWorks can't be helpful and define usleep()
 */
int usleep(long microseconds) {
    struct timespec ns;

    ns.tv_sec = 0;
    ns.tv_nsec = 1000 * microseconds;

    return nanosleep(&ns, NULL);
}

/**
 * Sleep Squawk for specified milliseconds
 */
void osMilliSleep(long long millis) {
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "osMilliSleep %llx\n", millis); }

    if (millis <= 0) {
        return;
    }

    /* TRICKY: safe because "addedEvent" is only cleared by this thread. */
    if (addedEvent) {
       if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "osMilliSleep woke up before lock!\n"); }
       return;
    }

    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "osMilliSleep sleeping..\n"); }

    SimpleMonitorLock(threadEventMonitor);
    if (addedEvent || SimpleMonitorWait(threadEventMonitor, millis)) {
        if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "osMilliSleep woken up early\n"); }
    }

    SimpleMonitorUnlock(threadEventMonitor);
}

/* ----------------------- Misc Support ------------------------*/

#define USE_CUSTOM_DL_CODE 1


// Map the default dlsym handle to null
// VxWorks doesn't use the handle.
#define sys_RTLD_DEFAULT() (void*)sysSymTbl
#define RTLD_LAZY NULL

void* sysdlopen(char* name) {
    return sysSymTbl;
}

int sysdlclose(void* handle) {
    return 0;
}

void* sysdlerror() {
    return "symLib Error";
}

void* dlsym(void* handle, const char* symbol) {
    //char symName[strlen(symbol) + 1];

    //strcpy(symName, symbol);

    char* fn;
    SYM_TYPE ptype;

    STATUS status = symFindByName(sysSymTbl, (char*)symbol, &fn, &ptype);
	
    return status == OK ? fn : NULL;
}

/* fake up dladdr struct */
typedef struct dl_info {
        char      *dli_sname;     /* Name of nearest symbol */
        void      *dli_saddr;     /* Address of nearest symbol */
} Dl_info;

int dladdr(void* addr, Dl_info* info) {
    SYM_TYPE ptype;

    STATUS status = symByValueFind(sysSymTbl, (UINT)addr, &info->dli_sname, (int*)&info->dli_saddr, &ptype);

    return status == OK ? 1 : 0;
}



char* strsignal(int signal) {
    switch(signal) {
        case SIGABRT:
            return "signal: abort";
        case SIGALRM:
            return "signal: alarm clock";
        case SIGBUS:
            return "signal: bus error";
        case SIGCHLD:
            return "signal: (exit of a) child";
        case SIGCONT:
            return "signal: continue";
        case SIGEMT:
            return "signal: EMT instruction";
        case SIGFPE:
            return "signal: floating point exception";
        case SIGHUP:
            return "signal: hangup";
        case SIGILL:
            return "signal: illegal instruction";
        case SIGINT:
            return "signal: interruption";
        case SIGSEGV:
            return "signal: segmentation violation";
        case SIGSYS:
            return "signal: (bad argument to) system call";
        case SIGTERM:
            return "signal: terminate";
        case SIGTTIN:
            return "signal: TTY input";
        case SIGTTOU:
            return "signal: TTY output";
        case SIGTSTP:
            return "signal: terminal stop";
        case SIGURG:
            return "signal: urgent I/O condition";
        case SIGUSR1:
            return "signal: user-defined signal 1";
        case SIGUSR2:
            return "signal: user-defined signal 2";
        default:
            return "unknown signal";
    }
}

#define osloop()        /**/
#define osbackbranch()  /**/
#define osfinish()      /**/


/* ----------------------- Native Task Support ------------------------*/
#define MIN_STACK_SIZE (8 * 1024)
#define DEFAULT_STACK_SIZE (16 * 1024)

static int taskPriorityMap(int genericPriority) {
    switch (genericPriority) {
        case TASK_PRIORITY_LOW:
            return 200;
        case TASK_PRIORITY_MED:
            return 100;
        case TASK_PRIORITY_HI:
            return 50;
        default: {
            fprintf(stderr, "WARNING: non-std priority passed to createTaskExecutor: %d\n", genericPriority);
            return 100;
        }
    }
}

int setTaskID(TaskExecutor* te) {
    te->id = (void*)taskIdSelf();
}

/**
 * Create a new TaskExecutor and native thread.
 */
TaskExecutor* createTaskExecutor(char* name, int priority, int stacksize) {
    int taskID;
    TaskExecutor* te = (TaskExecutor*)malloc(sizeof(TaskExecutor));

    if (te == NULL) {
        return NULL;
    }

    if (stacksize == 0) {
        stacksize = DEFAULT_STACK_SIZE;
    }
    if (stacksize < MIN_STACK_SIZE) {
        stacksize = MIN_STACK_SIZE;
    }
    
    te->runQ = NULL;
    te->monitor = SimpleMonitorCreate();
    if (te->monitor == NULL) {
        te->status = EVENT_REQUEST_STATUS_ERROR;
        te->te_errno = errno;
        if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "In createTaskExecutor, error in SimpleMonitorCreate: %d%s\n", errno); }
        return te;
    }

    te->status = TASK_EXECUTOR_STATUS_STARTING;

    taskID = taskCreate(name,
                        taskPriorityMap(priority),
                            VX_FP_TASK,				// options
                            stacksize,					// stack size
                            (void*)teLoopingHandler,           // function to start
                            (int)te, 0, 0, 0, 0, 0, 0, 0, 0, 0);

    if (taskID == ERROR) {
        te->status = EVENT_REQUEST_STATUS_ERROR;
        te->te_errno = errno;
        if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "In createTaskExecutor, error in taskCreate: %d%s\n", errno); }
    } else {
        if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "In createTaskExecutor, about to start new thread %s\n", name); }
        if (taskActivate(taskID) == ERROR) {
            te->status = EVENT_REQUEST_STATUS_ERROR;
            te->te_errno = errno;
            if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "In createTaskExecutor, error in taskActivate: %d%s\n", errno); }
        }
    }
    /* TODO: Record TaskExecutor on global list of all TaskExecutors */
    return te;
}

/**
 * Delete the TaskExecutor. Returns zero on success.
 * Returns -1 if TaskExecutor is not done.
 */
static int deleteTaskExecutor(TaskExecutor* te) {
    if (te->status != TASK_EXECUTOR_STATUS_DONE) {
        usleep(100);
        if (te->status != TASK_EXECUTOR_STATUS_DONE) {
            return -1;
        }
    }
    
    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "deleteTaskExecutor()\n"); }
    sysAssumeAlways(te->runQ == NULL);
    SimpleMonitorDestroy(te->monitor);
    free(te);
    return 0;
    /* TODO: Record TaskExecutor on global list of all TaskExecutors */
}

/*---------------------------------------------------------------------------*\
 *                               Select Pipe                                 *
\*---------------------------------------------------------------------------*/

#define SELECT_PIPE_NAME "/pipe/squawk-select-pipe"

static int selectPipeFD = 0;

int initSelectPipe() {
    int rc = pipeDevCreate (SELECT_PIPE_NAME, 100, 10);
    sysAssume(rc == OK);
    selectPipeFD = open(SELECT_PIPE_NAME, O_RDWR | O_NONBLOCK);
//fprintf(stderr, "initSelectPipe: fd = %d\n", selectPipeFD);
    return (selectPipeFD == -1) ? -1 : 0;
}

int cleanupSelectPipe() {
    int rc;
    if (selectPipeFD >= 0) {
        rc = close(selectPipeFD);
        sysAssume(rc == OK);
    }
    rc = pipeDevDelete(SELECT_PIPE_NAME, FALSE);
    sysAssume(rc == OK);
    return rc;
}

int readSelectPipeMsg() {
    char dummy = 5;
    int rc;
    while ((rc == read(selectPipeFD, &dummy, 1)) == 1) {

    }
    return rc;
}

int numMessages(int fd) {
    int num;
    int res = ioctl(fd, FIONMSGS, (int)&num);
    if (res == ERROR) {
        return ERROR;
    } else {
        return num;
    }
}

/** This should be non-blocking...*/
int writeSelectPipeMsg()  {
    char dummy = 5;
    int pending = numMessages(selectPipeFD);
    int res;
    if (pending == 0) {
//fprintf(stderr, "Sending message to cancel select call.\n");
        res = write(selectPipeFD, &dummy, 1);
    }
    return res;
}

int getSelectReadPipeFd() {
    return selectPipeFD;
}
