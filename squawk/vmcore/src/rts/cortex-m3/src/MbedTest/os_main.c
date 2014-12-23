/**************************************************************************//**
 * @file     main.c
 * @brief    CMSIS Cortex-M3 Blinky example
 *           Blink a LED using CM3 SysTick
 * @version  V1.03
 * @date     24. September 2009
 *
 * @note
 * Copyright (C) 2009 ARM Limited. All rights reserved.
 *
 * @par
 * ARM Limited (ARM) is supplying this software for use with Cortex-M 
 * processor based microcontrollers.  This file can be freely distributed 
 * within development tools that are supporting such ARM based processors. 
 *
 * @par
 * THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
 * ARM SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
 * CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
 *
 ******************************************************************************/

#include "LPC17xx.h"
#include <stdio.h>
#include <stdlib.h>
 		#include <errno.h>
 		#include <string.h>
 		#include <sys/stat.h>
 		#include <sys/types.h>

#define C_HEAP_MEMORY_RESERVE (16 * 1024)

volatile uint32_t msTicks;                            /* counts 1ms timeTicks */
/*----------------------------------------------------------------------------
  SysTick_Handler
 *----------------------------------------------------------------------------*/
void SysTick_Handler(void) {
  msTicks++;                        /* increment counter necessary in Delay() */
}

/*------------------------------------------------------------------------------
  delays number of tick Systicks (happens every 1 ms)
 *------------------------------------------------------------------------------*/
__INLINE static void Delay (uint32_t dlyTicks) {
  uint32_t curTicks;

  curTicks = msTicks;
  while ((msTicks - curTicks) < dlyTicks);
}

/*------------------------------------------------------------------------------
  configer LED pins
 *------------------------------------------------------------------------------*/
__INLINE static void LED_Config(void) {

  LPC_GPIO1->FIODIR = 0xFFFFFFFF;               /* LEDs PORT1 are Output */
}

/*------------------------------------------------------------------------------
  Switch on LEDs
 *------------------------------------------------------------------------------*/
__INLINE static void LED_On (uint32_t led) {

  LPC_GPIO1->FIOPIN |=  (led);                  /* Turn On  LED */
}


/*------------------------------------------------------------------------------
  Switch off LEDs
 *------------------------------------------------------------------------------*/
__INLINE static void LED_Off (uint32_t led) {

  LPC_GPIO1->FIOPIN &= ~(led);                  /* Turn Off LED */
}

/*
static int get_available_memory() {
	char* current;
	int size = get_ram_size();
	// malloc some heap to reserve it for interrupt event blocks etc.
	char* reserved = malloc(C_HEAP_MEMORY_RESERVE);
	// get info
	do {
		size -= 1024;
		current = malloc(size);
	} while (current == 0);
	free(current);
	free(reserved);
	// check we *really* can allocate this space
	reserved = malloc(size);
	if (reserved == 0) {
		error("Failed to reallocate memory", size);
		exit(-1);
	}
	free(reserved);
	return size;
}

*/

#include <stdio.h>


#undef errno
extern int errno;
extern int  _end;

caddr_t _sbrk ( int incr )
{
  static unsigned char *heap = NULL;
  unsigned char *prev_heap;

  if (heap == NULL) {
    heap = (unsigned char *)&_end;
  }
  prev_heap = heap;

  heap += incr;

  return (caddr_t) prev_heap;
}

int link(char *old, char *new) {
return -1;
}

int _close(int file) {
           return -1;
        }

int _fstat(int file, struct stat *st) {
       st->st_mode = S_IFCHR;
        return 0;
}

int _isatty(int file) {
            return 1;
          }

int _lseek(int file, int ptr, int dir) {
            return 0;
          }

int _read(int file, char *ptr, int len) {
	file =file;
	ptr= ptr;
	len= len;
	return 0;
}

int _write(int file, char *ptr, int len) {
            return len;
          }

/*----------------------------------------------------------------------------
  MAIN function
 ----------------------------------------------------------------------------*/
int main (void) {

  if (SysTick_Config(SystemCoreClock / 1000)) { /* Setup SysTick Timer for 1 msec interrupts  */
    while (1);                                  /* Capture error */
  }
  
  LED_Config();                             
 
  while(1) {
	  	printf("Hello world\r\n");
 //   LED_On ((1<<18));                           /* Turn on the LED. */
    Delay (100);                                /* delay  100 Msec */
 //   LED_Off ((1<<18));                          /* Turn off the LED. */
    Delay (100);                                /* delay  100 Msec */
  }
  
}
