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

#include <sched.h>
#include <pthread.h>

/* Implementations of Squawk calls using standard POSIX calls.
 * Included by os.c  for platforms that support POSIX.
 */

/* not sure why this isn't picked up in limits.h */
#ifndef PTHREAD_STACK_MIN
#define	PTHREAD_STACK_MIN	((size_t)sysconf(_SC_THREAD_STACK_MIN))
#endif

#if defined(ASSUME) && ASSUME != 0
#define sysAssume(x) if (!(x))  { fprintf(stderr, "Assertion failed: \"%s\", at %s:%d\n", #x, __FILE__, __LINE__); exit(1); }
#else
#define sysAssume(x) /**/
#endif /* ASSUME */

#define sysAssumeAlways(x) if (!(x))  { fprintf(stderr, "Assertion failed: \"%s\", at %s:%d\n", #x, __FILE__, __LINE__);   exit(1);}

/* ----------------------- Time Support ------------------------*/

#ifdef USE_CLOCK_GETTIME
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

#define sys_clock_gettime clock_gettime

#else
jlong sysTimeMicros() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    /* We adjust to 1000 ticks per second */
    return (jlong)tv.tv_sec * 1000000 + tv.tv_usec;
}

jlong sysTimeMillis(void) {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return timeVal2ms(&tv);
}

#ifndef CLOCK_REALTIME
#define CLOCK_REALTIME 1
#endif

static int sys_clock_gettime(int clk_id, struct timespec *res) {
    int result;
    struct timeval curtime;
    sysAssume(clk_id == CLOCK_REALTIME);
    result = gettimeofday(&curtime, NULL);
    timeVal2TimeSpec(&curtime, res);
    return result;
};
#endif

/* ----------------------- Condvar Support ------------------------*/

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
    volatile int lockCount; /* for debugging */
} SimpleMonitor;

#if PLATFORM_TYPE_NATIVE

/**
 * threadEventMonitor is used to synchronize various native threads/interrupt handlers
 * with the Squawk thread scheduler. If Squawk has nothing to do, it will do a timed wait on
 * threadEventMonitor that can be interrupted by an event signalled from a native thread/interrupt handler.
 */
SimpleMonitor* threadEventMonitor = NULL;

/**
 * addedEvent is set TRUE by multiple writers (native threads) when adding evt, and read by the Squawk thread in osMilliSleep and cleared by
 * the Squawk thread in getEvent(). Protected by threadEventMonitor.
 */
volatile int addedEvent;

static char* monitorName(SimpleMonitor* mon) {
    sysAssumeAlways(mon);
    if (mon == threadEventMonitor) {
        return "threadEventMonitor";
    } else {
        return "unknown monitor";
    }
}
#else
static char* monitorName(SimpleMonitor* mon) {
    sysAssumeAlways(mon);
    return "unknown monitor";
}
#endif

static void monitorErrCheck(SimpleMonitor* mon, char* msg, int res, int expectedValue) {
    if (res != 0 && res != expectedValue) {
        fprintf(stderr, "unexpected result in %s on %s: %d. errno: %d \n", msg, monitorName(mon), res, errno);
    }
}

/**
 * Return true if the mutex is locked, false otherwise.
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
    sysAssumeAlways(mon);
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
        sys_clock_gettime(CLOCK_REALTIME, &abstime);
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

#if PLATFORM_TYPE_NATIVE

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
#else
#define MAX_MICRO_SLEEP 999999

/**
 * Sleep Squawk for specified milliseconds
 */
void osMilliSleep(long long millis) {
    if (millis <= 0) {
        return;
    }
    long long elapsed = sysTimeMillis();
    long long seconds = millis / 1000;
    if (seconds > 0) {
        // too long for usleep, so get close
        sleep(seconds);
    }
    elapsed = sysTimeMillis() - elapsed;
    if (elapsed < millis) {
        millis = millis - elapsed;
        long long micro = millis * 1000;
        if (micro > MAX_MICRO_SLEEP) {
            micro = MAX_MICRO_SLEEP;
        }
        usleep(micro);
    }
}
#endif

#if PLATFORM_TYPE_NATIVE
/* ----------------------- Native Task Support ------------------------*/

static int taskPriorityMap(int genericPriority) {
    switch (genericPriority) {
        case TASK_PRIORITY_LOW:
        case TASK_PRIORITY_MED:
        case TASK_PRIORITY_HI:
        default:
            return 0;
    }
}

int setTaskID(TaskExecutor* te) {
    te->id = (NativeTaskID)pthread_self();
}

/* function pointer used by pthreads: */
typedef void*(*pthread_func_t)(void*);

/**
 * Create a new TaskExecutor and native thread.
 */
TaskExecutor* createTaskExecutor(char* name, int priority, int stacksize) {
    TaskExecutor* te = (TaskExecutor*)malloc(sizeof(TaskExecutor));
    pthread_attr_t attr;
    pthread_t id;
    /* sched_param param; */
    int rc; /* return code */

    if (te == NULL) {
        return NULL;
    }
    te->runQ = NULL;
    te->monitor = SimpleMonitorCreate();
    te->status = TASK_EXECUTOR_STATUS_STARTING;

    pthread_attr_init(&attr);
    /* set priority */
   /* rc = pthread_attr_getschedparam (&attr, &param);
    param.sched_priority = taskPriorityMap(priority);
    rc = pthread_attr_setschedparam (&attr, &param); */

    rc = pthread_attr_setstacksize(&attr, (PTHREAD_STACK_MIN > stacksize) ? PTHREAD_STACK_MIN : stacksize);

    if (DEBUG_EVENTS_LEVEL) { fprintf(stderr, "In createTaskExecutor, about to start new thread %s\n", name); }
    rc = pthread_create(&id, &attr, (pthread_func_t)teLoopingHandler, te);

    if (rc != 0) {
        te->status = TASK_EXECUTOR_STATUS_ERROR;
        te->te_errno = errno;
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
        return -1;
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

static int pfd[2];

static int setNonBlocking(int fd) {
    int res = -1;
    int flags = fcntl(fd, F_GETFL, 0);
	// fprintf(stderr, "initSelectPipe: fcntl returned: %d\n", flags);

    if (flags >= 0) {
		// fprintf(stderr, "set_blocking_flags: calling fcntl F_SETFL flags: %d\n", flags | O_NONBLOCK);
        if (fcntl(fd, F_SETFL, flags | O_NONBLOCK) == -1) {
			// fprintf(stderr, "initSelectPipe: fcntl F_SETFL failed. errno = %d\n", errno);
        }
    } else {
		// fprintf(stderr, "initSelectPipe: fcntl F_GETFL failed. errno = %d\n", errno);
    }
    return res;
}

int initSelectPipe() {
    int rc = pipe(pfd);
    sysAssume(rc == 0);
    rc = setNonBlocking(pfd[0]);
    rc = setNonBlocking(pfd[1]);
	// fprintf(stderr, "initSelectPipe: read df = %d, write fd = %d\n", pfd[0], pfd[1]);
    return rc;
}

int cleanupSelectPipe() {
    int rc;
    rc = close(pfd[0]);
    sysAssume(rc == 0);
    rc = close(pfd[1]);
    sysAssume(rc == 0);
    return rc;
}

int readSelectPipeMsg() {
    int dummy = 5;
    int rc;
    while ((rc == read(pfd[0], &dummy, 1)) == 1) {

    }
    return rc;
}

int writeSelectPipeMsg()  {
    char dummy = 5;
    int res = write(pfd[1], &dummy, 1);
//fprintf(stderr, "Sending message to cancel select call. res = %d, errno = %d\n", res, errno);
    return res;
}

int getSelectReadPipeFd() {
    return pfd[0];
}
#endif /* PLATFORM_TYPE_NATIVE */

/* ----------------------- Memory Support ------------------------*/


/**
 * Gets the page size (in bytes) of the system.
 *
 * @return the page size (in bytes) of the system
 */
int sysGetPageSize(void) {
    return sysconf(_SC_PAGESIZE);
}

/**
 * Sets a region of memory read-only or reverts it to read & write.
 *
 * @param start    the start of the memory region
 * @param end      one byte past the end of the region
 * @param readonly specifies if read-only protection is to be enabled or disabled
 */
void sysToggleMemoryProtection(char* start, char* end, boolean readonly) {
    size_t len = end - start;
    if (mprotect(start, len, readonly ? PROT_READ : PROT_READ | PROT_WRITE) != 0) {
        fprintf(stderr, "Could not toggle memory protection: %s\n", strerror(errno));
    }
}

/**
 * Allocate a page-aligned chunk of memory of the given size.
 *
 * @param size size in bytes to allocate
 * @return pointer to allocated memory or null.
 */
INLINE void* sysValloc(size_t size) {
//#ifdef sun
    //buffer = malloc(actualSize);    this may have been work-around for solaris bug 4846556, now fixed.
//#else
    return valloc(size);
//#endif /* sun */
}

/**
 * Free chunk of memory allocated by sysValloc
 *
 * @param ptr to to chunk allocated by sysValloc
 */
INLINE void sysVallocFree(void* ptr) {
    free(ptr);
}
