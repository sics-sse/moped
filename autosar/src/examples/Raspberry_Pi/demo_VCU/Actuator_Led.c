/*
 * Actuator_Led.c
 *
 *  Created on: 3 dec 2014
 *      Author: sse
 */
#include <stdio.h>      /* printf, fgets */
#include "Rte_LedSWC.h"
#include "Rte_Internal.h"
#include "MOPED_DEBUG.h"
#include "bcm2835.h"

boolean led_init = false;
static void Led_Init(void){
	int i;

	/* Configure the led-pins as output pins */
	bcm2835_GpioFnSel(GPIO_LED_RED, GPFN_OUT);
	bcm2835_GpioFnSel(GPIO_LED_YELLOW1, GPFN_OUT);
	bcm2835_GpioFnSel(GPIO_LED_YELLOW2, GPFN_OUT);
}

void LedControlRunnable(void){
    if(led_init == false){
    	Led_Init();
    	led_init = true;
    }

	UInt16 Led_Data = Rte_IRead_LedControlRunnable_LedSwcReadDataFromPirteSwcPort1_dataInt32();

	UInt32 Pin = (Led_Data & 0xFF00) >> 8;
	UInt8 Level = Led_Data & 0x00FF;

#if LED_PRINT
	switch (Pin) {
	case LED_RED:
		if (Level == 0) {
			printf("Led_RED on\r\n");
		} else {
			printf("Led_RED off\r\n");
		}
		break;
	case LED_YELLOW1:
		if (Level == 0) {
			printf("LED_YELLOW1 on\r\n");
		} else {
			printf("LED_YELLOW1 off\r\n");
		}
		break;
	case LED_YELLOW2:
		if (Level == 0) {
			printf("LED_YELLOW2 on\r\n");
		} else {
			printf("LED_YELLOW2 off\r\n");
		}
		break;
	default:
		break;
	}
#endif
	Rte_Call_LedSwcWriteDataToIoHwLedPort2_PinOpt(&Pin, &Level);
}
