/*
 * squawk_memory.h
 *
 *  Created on: 31 jan 2014
 *      Author: zsz
 */

#ifndef SQUAWK_MEMORY_H_
#define SQUAWK_MEMORY_H_
//#include "stddef.h"

#define squawk_heap_size  20971520    //20M

typedef unsigned int uint;
typedef unsigned char uchar;
typedef unsigned long ulong;

/* roundmb - round address up to size of memblock  */
#define roundmb(x)  (void *)( (7 + (ulong)(x)) & ~0x07 )
/* truncmb - truncate address down to size of memblock */
#define truncmb(x)  (void *)( ((ulong)(x)) & ~0x07 )

/**
 * @ingroup memory_mgmt
 *
 * Frees memory allocated with stkget().
 *
 * @param p
 *      Pointer to the topmost (highest address) word of the allocated stack (as
 *      returned by stkget()).
 * @param len
 *      Size of the allocated stack, in bytes.  (Same value passed to stkget().)
 */
#define stkfree(p, len) memfree((void *)((ulong)(p)         \
                                - (ulong)roundmb(len)       \
                                + (ulong)sizeof(ulong)),    \
                                (ulong)roundmb(len))


/**
 * Structure for a block of memory.
 */
struct memblock
{
    struct memblock *next;          /**< pointer to next memory block       */
    uint length;                    /**< size of memory block (with struct) */
};

extern struct memblock memlist;     /**< head of free memory list           */

/* Other memory data */

//extern void *_end;              /**< linker provides end of image       */
extern void *memheap;           /**< bottom of heap                     */

/* Memory function prototypes */
void usb_heap_init(void);
void *memget(uint);
void memfree(void *, uint);
void *stkget(uint);
//void bzero(void *s, unsigned n);

#endif /* SQUAWK_MEMORY_H_ */
