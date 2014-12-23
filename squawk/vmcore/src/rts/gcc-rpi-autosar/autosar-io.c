#ifndef SHALLOW_SLEEP_CLOCK_MODE_NORMAL
#define SHALLOW_SLEEP_CLOCK_MODE_NORMAL 0
#endif

#ifndef SLEEP_MANAGER_ENABLED_IRQS
#define SLEEP_MANAGER_ENABLED_IRQS 0
#endif

#define DEVICE_DEFAULT 1
#define DEVICE_USB 2
#define DEVICE_SERIAL 3
#define DEVICE_FIRST DEVICE_DEFAULT
#define DEVICE_LAST DEVICE_SERIAL

#define WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER (DEVICE_LAST+1)
#define FIRST_IRQ_EVENT_NUMBER (WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER+1)

volatile long long clock_counter;

typedef struct dl_info {
        char      *dli_sname;     /* Name of nearest symbol */
        void      *dli_saddr;     /* Address of nearest symbol */
} Dl_info;

typedef long long int64_t;
typedef unsigned long long u_int64_t;

