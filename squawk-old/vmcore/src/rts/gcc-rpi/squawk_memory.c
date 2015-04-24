/*
 * squawk_memory.c
 *
 *  Created on: 31 jan 2014
 *      Author: zsz
 */
#include <string.h>
#include "squawk_memory.h"

int squawk_heap[squawk_heap_size];
struct memblock memlist;        /* List of free memory blocks     */

void *memheap;

/*
void bzero(void *s, unsigned n)
{
    memset(s, 0, n);
}
*/

void squawk_heap_init(void){
	struct memblock *pmblock;   /* memory block pointer          */
	memheap = roundmb(&squawk_heap[0]);
	memlist.next = pmblock = (struct memblock *) memheap;
	memlist.length = (uint) squawk_heap_size;
	pmblock->next = NULL;
	pmblock->length = (uint) squawk_heap_size;

}

void *memget(uint nbytes)
{
    register struct memblock *prev, *curr, *leftover;
    if (0 == nbytes)
    {
    	jnaPrint("error: fail to memget1\r\n");
        return NULL;
    }

    /* round to multiple of memblock size   */
    nbytes = (ulong)roundmb(nbytes);


    prev = &memlist;
    curr = memlist.next;
    while (curr != NULL)
    {
        if (curr->length == nbytes)
        {
            prev->next = curr->next;
            memlist.length -= nbytes;

            return (void *)(curr);
        }
        else if (curr->length > nbytes)
        {
            /* split block into two */
            leftover = (struct memblock *)((ulong)curr + nbytes);
            prev->next = leftover;
            leftover->next = curr->next;
            leftover->length = curr->length - nbytes;
            memlist.length -= nbytes;
            return (void *)(curr);
        }
        prev = curr;
        curr = curr->next;
    }

}

void memfree(void *memptr, uint nbytes)
{
    register struct memblock *block, *next, *prev;
    ulong top;

    /* make sure block is in heap */
    if ((0 == nbytes) || ((ulong)memptr < (ulong)memheap))
    {
    	jnaPrint("error: fail to memfree1\r\n");
        return;
    }

    block = (struct memblock *)memptr;
    nbytes = (ulong)roundmb(nbytes);

    prev = &memlist;
    next = memlist.next;
    while ((next != NULL) && (next < block))
    {
        prev = next;
        next = next->next;
    }

    /* find top of previous memblock */
    if (prev == &memlist)
    {
        top = (ulong)0;
    }
    else
    {
        top = (ulong)prev + prev->length;
    }

    /* make sure block is not overlapping on prev or next blocks */
    if ((top > (ulong)block)
        || ((next != NULL) && ((ulong)block + nbytes) > (ulong)next))
    {
    	jnaPrint("error: fail to memfree2\r\n");
        return;
    }

    memlist.length += nbytes;

    /* coalesce with previous block if adjacent */
    if (top == (ulong)block)
    {
        prev->length += nbytes;
        block = prev;
    }
    else
    {
        block->next = next;
        block->length = nbytes;
        prev->next = block;
    }

    /* coalesce with next block if adjacent */
    if (((ulong)block + block->length) == (ulong)next)
    {
        block->length += next->length;
        block->next = next->next;
    }
}

