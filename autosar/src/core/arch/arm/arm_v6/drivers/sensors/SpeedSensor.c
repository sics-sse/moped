/*
 * SpeedSensor.c
 *
 *  Created on:  Sep 4, 2014
 *      Author:  Zhang Shuzhou
 *  Reviewed on:
 *     Reviewer:
 *
 * This class implements a reflective sensor
 * (Photologic® Reflective Object Sensor OPB715Z, OPB716Z, OPB717Z, OPB718Z)
 * that is used to calculate wheel rotational speed.
 *
 */
#include <stdio.h>

#include "bcm2835.h"

#include "isr.h"
#include "irq_types.h"

#include "Sensors.h"

static uint32 pulse[2];
static uint32 pulse_total[2];

#define SAVED_PULSE_T	5
uint32 pulse_t[2][SAVED_PULSE_T];
uint32 pulse_t_n[2];

/**
 * Interrupt service routine that counts the number of detected pulses
 *
 * Each time a reflection signal is captured by the sensor, this interrupt
 * is activated and a counter (unique for each wheel pair) is increased.
 */
void SpeedSensor_Isr(void) {
  static int readc = 0;

  if (bcm2835_ReadGpioPin(&GPEDS0, 2)) {
    readc++;
    bcm2835_Sleep(100);
    int yy = bcm2835_ReadGpioPin(&GPEDS0, 2);
    int xx = bcm2835_ReadGpioPin(&GPLEV0, 2);
    printf("%3d gpio 2 EDS = %d, LEV = %d\r\n",
	   readc, yy, xx);
    bcm2835_ClearEventDetectPin(2);
  }

  if (bcm2835_ReadGpioPin(&GPEDS0, GPIO_FRONT_SPEED)) {
    pulse[FRONT_WHEEL]++;
    pulse_total[FRONT_WHEEL]++;

    uint64 t = CURRENT_TIME;
    int n = pulse_t_n[FRONT_WHEEL];
    if (n < SAVED_PULSE_T) {
      pulse_t[FRONT_WHEEL][n] = t;
      pulse_t_n[FRONT_WHEEL] = n + 1;
    } else {
      for (int i = 1; i < SAVED_PULSE_T; i++) {
	pulse_t[FRONT_WHEEL][i-1] = pulse_t[FRONT_WHEEL][i];
      }
      pulse_t[FRONT_WHEEL][n-1] = t;
    }
    bcm2835_ClearEventDetectPin(GPIO_FRONT_SPEED);
  }

  if (bcm2835_ReadGpioPin(&GPEDS0, GPIO_REAR_SPEED)) {
    pulse[REAR_WHEEL]++;
    pulse_total[REAR_WHEEL]++;

    uint64 t = CURRENT_TIME;
    int n = pulse_t_n[REAR_WHEEL];
    if (n < SAVED_PULSE_T) {
      pulse_t[REAR_WHEEL][n] = t;
      pulse_t_n[REAR_WHEEL] = n + 1;
    } else {
      for (int i = 1; i < SAVED_PULSE_T; i++) {
	pulse_t[REAR_WHEEL][i-1] = pulse_t[REAR_WHEEL][i];
      }
      pulse_t[REAR_WHEEL][n-1] = t;
    }


    bcm2835_ClearEventDetectPin(GPIO_REAR_SPEED);
  }
}

/**
 * Check if a wheel index is valid
 *
 * @param wheel		   		----- wheel type (typically rear or front)
 * @return 0/1				----- 1 if this is a valid wheel type
 * 								  0 otherwise
 */
static uint8 SpeedSensor_IsValidWheel(enum Wheel wheel) {
	if (wheel >= NO_WHEEL || wheel < 0) {
		printf("ERROR: There is no such wheel (%d). Was it a coding miss?\r\n", wheel);
		return 0;
	}

	return 1;
}

/**
 * Speed sensor initialization
 *
 * Basically, initialize GPIO pins connected to the speed sensor
 * to detect rising edges. Also, install an interrupt to count the
 * rising edges.
 */
void SpeedSensor_Init(void){

	/* Configure the pins connected to the wheel speed sensor as input pins */
	bcm2835_GpioFnSel(GPIO_FRONT_SPEED, GPFN_IN);
	bcm2835_GpioFnSel(GPIO_REAR_SPEED, GPFN_IN);

    /* Enable the pins to detect falling edge signals */
	bcm2835_SetReadWriteGpioReg(&GPFEN0, GPIO_FRONT_SPEED);
	bcm2835_SetReadWriteGpioReg(&GPFEN0, GPIO_REAR_SPEED);

	/* Reset edge statuses (sometimes they have been seen to be non-zero at start-up) */
	bcm2835_ClearEventDetectPin(GPIO_FRONT_SPEED);
	bcm2835_ClearEventDetectPin(GPIO_REAR_SPEED);

	/* Install an interrupt to handle falling edge signals
	 * Format: ISR_INSTALL(_name, _entryFunction, _irqVector, _priority, _appOwner)
	 * (for more details on irqVector, see the interrupts table on p.113 in BCM2835-ARM-Peripherals.pdf) */
    ISR_INSTALL_ISR2("GPIO0", bcm2835_GpioIsr, BCM2835_IRQ_ID_GPIO_0, 2, 0);

	/* Enable the installed interrupt by setting the appropriate HW register */
	bcm2835_SetReadWriteGpioReg(&IRQ_ENABLE1, BCM2835_IRQ_ID_GPIO_0);
}

/**
 * Get the number of detected pulses for a wheel pair, since the last counter reset
 *
 * @param wheel		    	----- wheel type (rear or front)
 * @return pulse			----- pulse counter
 */
uint32 Sensors_GetWheelPulse(enum Wheel wheel) {
	if (!SpeedSensor_IsValidWheel(wheel)) {
		return -1;
	}

	return pulse[wheel];
}

uint32 Sensors_GetWheelPulseTotal(enum Wheel wheel) {
	if (!SpeedSensor_IsValidWheel(wheel)) {
		return -1;
	}

	return pulse_total[wheel];
}

/**
 * Reset pulse counter for a wheel pair
 *
 * @param wheel		    	----- wheel type (rear or front)
 */
void Sensors_ResetWheelPulse(uint8 wheel) {
	if (SpeedSensor_IsValidWheel(wheel)) {
		pulse[wheel] = 0;
	}
}
