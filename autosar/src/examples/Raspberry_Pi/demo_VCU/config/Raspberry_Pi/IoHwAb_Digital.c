/*
 * This code is not automatically generated.
 * Because Pi dose not have the IoHw layer
 * in the ArcCore tools.
 *
 *  Created on: 28 okt 2014
 *      Author: sse
 */
#include <stdio.h>

#include "Platform_Types.h"
#include "IoHwAb_Digital.h"
#include "Sensors.h"

#include "MOPED_DEBUG.h"

#if defined(USE_PWM)
#include "Pwm.h"
#endif

#include "bcm2835.h"

#define PI 	   			3.14159265f

//TODO Should this conversion be done in IoHwAb or in SensorSw-C?
/**
 * Convert wheel speed from pulse counter to cm/s
 *
 * @param wheel				----- wheel type (front or rear)
 * @param startTime			----- pointer to the time since the counter started from zero
 */
static uint32 IoHwAb_Digital_CalcWheelSpeed(enum Wheel wheel, uint64 *startTime) {
	uint32 time;
	float distance, speed;
	uint32 nrPulses = Sensors_GetWheelPulse(wheel);

#if 0
	uint32 odo;
	char buf[100];
	char *cp = buf;
	odo = Sensors_GetWheelPulseTotal(wheel);
#endif

	distance = PI * WHEEL_DIAMETER * nrPulses / PULSES_PER_WHEEL_TURN;
	time = (uint32)(CURRENT_TIME - *startTime);
	speed = 1e6 * distance / time; //speed in cm/s

#if 0
	printf("wheel %d currenttime %d", wheel, CURRENT_TIME);
	printf(" distance %d", (int) (1000*distance));
	printf(" time %d", time);
	printf(" pulses %d", nrPulses);
	printf(" speed %d", (int) (1000*speed));
	printf(" odo %d\r\n", odo);
#endif

	Sensors_ResetWheelPulse(wheel);
	*startTime = CURRENT_TIME;

	return (uint32)speed;
}

//TODO: Maybe another name, since MCP3008 has 8 channels and this one specifically reads battery,
// 		e.g. IoHwAb_BatterySensorRead(0, Data) or even IoHwAb_Digital_Get(BATTERY_SENSOR, Data)
/**
 * Get the battery voltage value
 *
 * @param portDefArg1		----- not used
 * @param Data				----- pointer for data storage
 * @return status			----- read status (e.g. OK or not OK)
 */
Std_ReturnType IoHw_Read_AdcSensor(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data){
	Mcp3008_Read(MCP3008_BATTERY_CH, Data);
#if ADC_PRINT_DEBUG
	printf("infor: IoHW adc = %lu\r\n", *Data);
#endif
    return E_OK;
}

//TODO: Prefix the function name with IoHwAb_ (or even convert it to IoHwAb_Digital_Get(FRONT_WHEEL_SENSOR, Data))
//		However, this requires regeneration of the project
/**
 * Get the front wheel speed
 *
 * @param portDefArg1		----- not used
 * @param Data				----- pointer for data storage
 * @return status			----- read status (e.g. OK or not OK)
 */
Std_ReturnType IoHw_Read_FrontWheelSensor(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data){
	static uint64 startTime;
	//*Data = IoHwAb_Digital_CalcWheelSpeed(FRONT_WHEEL, &startTime);
	*Data = 0;

//	printf("In IoHw_Front_RearWheelSensor, data = %lu cm\r\n", *Data);

#if WHEEL_PRINT_DEBUG
		printf("infor: IoHW front wheel speed = %lucm/s\r\n", *Data);
#endif

    return E_OK;
}

#define SAVED_PULSE_T	5
extern uint64 pulse_t[][SAVED_PULSE_T];
extern uint64 pulse_t_n[];

//TODO: As above
/**
 * Get the rear wheel speed
 *
 * @param portDefArg1		----- not used
 * @param Data				----- pointer for data storage
 * @return status			----- read status (e.g. OK or not OK)
 */
Std_ReturnType IoHw_Read_RearWheelSensor(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data){
	static uint64 startTime;
	uint64 startTime2 = startTime;
	uint32 odo;
	int i;

	odo = Sensors_GetWheelPulseTotal(REAR_WHEEL);
	*Data = IoHwAb_Digital_CalcWheelSpeed(REAR_WHEEL, &startTime2);
	//*Data = 1000*odo + IoHwAb_Digital_CalcWheelSpeed(REAR_WHEEL, &startTime);

	uint32 realfData = IoHwAb_Digital_CalcWheelSpeed(FRONT_WHEEL, &startTime);
	uint32 fodo;
	uint32 fData;
	uint32 fDataleft;
	fodo = Sensors_GetWheelPulseTotal(FRONT_WHEEL);

	//	printf("wheels %d %d %d %d\r\n", *Data, odo, fData, fodo);

#if 1
	// received by a navigation program on the TCU
	char tbuf[200];
	// we put our calculation of average speed for REAR in fData
	uint64 curt = CURRENT_TIME;
	uint64 avgt;

	int n;

#if 0
	n = 0;
	if (curt - pulse_t[REAR_WHEEL][SAVED_PULSE_T-1] < 600000) {
	  for (i = SAVED_PULSE_T-1; i > 0; i--) {
	    if (pulse_t[REAR_WHEEL][i] - pulse_t[REAR_WHEEL][i-1] > 600000) {
	      break;
	    }
	    n++;
	  }
	}

	if (n == 0) {
	  fData = 0.0;
	} else {
	  avgt = ((pulse_t[REAR_WHEEL][SAVED_PULSE_T-1] - pulse_t[REAR_WHEEL][SAVED_PULSE_T-1-n])/n);
	  fData = 10.2*3.14/5/avgt*1000000;
	}

	n = 0;
	if (curt - pulse_t[FRONT_WHEEL][SAVED_PULSE_T-1] < 600000) {
	  for (i = SAVED_PULSE_T-1; i > 0; i--) {
	    if (pulse_t[FRONT_WHEEL][i] - pulse_t[FRONT_WHEEL][i-1] > 600000) {
	      break;
	    }
	    n++;
	  }
	}

	if (n == 0) {
	  fDataleft = 0.0;
	} else {
	  avgt = ((pulse_t[FRONT_WHEEL][SAVED_PULSE_T-1] - pulse_t[FRONT_WHEEL][SAVED_PULSE_T-1-n])/n);
	  fDataleft = 10.2*3.14/5/avgt*1000000;
	}
#else
	if (curt - pulse_t[REAR_WHEEL][SAVED_PULSE_T-1] > 600000) {
	  fData = 0.0;
	} else {
	  n = 1;
	  avgt = ((pulse_t[REAR_WHEEL][SAVED_PULSE_T-1] - pulse_t[REAR_WHEEL][SAVED_PULSE_T-1-n])/n);
	  fData = 10.2*3.14/5/avgt*1000000;
	}

	if (curt - pulse_t[FRONT_WHEEL][SAVED_PULSE_T-1] > 600000) {
	  fDataleft = 0.0;
	} else {
	  n = 1;
	  avgt = ((pulse_t[FRONT_WHEEL][SAVED_PULSE_T-1] - pulse_t[FRONT_WHEEL][SAVED_PULSE_T-1-n])/n);
	  fDataleft = 10.2*3.14/5/avgt*1000000;
	}

#endif

	sprintf(tbuf, "speed x %3d x%d x %3d x%d x%d x %3dxy",
		*Data, odo, fData, fodo, realfData, fDataleft);
	autosarSendPackageData(strlen(tbuf), tbuf);
#endif

#if 0
	printf("pulse_t");
	for (i = 0; i < pulse_t_n[REAR_WHEEL]; i++) {
	  printf(" %ld", pulse_t[REAR_WHEEL][i]);
	}
	printf("\r\n");
#endif

//	printf("In IoHw_Read_RearWheelSensor, data = %lu cm\r\n", *Data);

#if  WHEEL_PRINT_DEBUG
		printf("infor: IoHW rear speed = %lucm/s\r\n", *Data);
#endif

	return E_OK;
}


Std_ReturnType IoHw_WriteSpeed_DutyCycle(/*IN*/uint32 portDefArg1, /*OUT*/uint8 * DutyCycle){

	uint32 speed = (int) ((100 + (0.55556 * ((signed char)*DutyCycle + 100) * 0.9)) * 16.38);

	Pwm_SetPeriodAndDuty(1, 20000, speed);

#if SPEED_PRINT_DEBUG
    printf("infor: write speed data %d to PWM driver: \r\n", speed);
#endif

	return E_OK;
}

extern int vcu_servo_direction;
extern int steering_min,  steering_max,  steering_zero;

Std_ReturnType IoHw_WriteServo_DutyCycle(/*IN*/uint32 portDefArg1, /*OUT*/uint8 * DutyCycle){
  signed char servo = (signed char)*DutyCycle;

  int channel = 0;

#if 0
  // Experiment with second servo.
  if (servo > 100 || servo < -100) {
    channel = 2;
    printf("channel 2 servo0: %d\r\n", servo);
    if (servo > 100) {
      servo = (servo-101)*127/26;
    } else {
      servo = -(-servo-101)*127/26;
    }
    printf("channel 2 servo1: %d\r\n", servo);
  }
#endif

  if (channel == 0) {
    servo = vcu_servo_direction*servo;

#if 0
    servo *= 1.5;
#else
    if (servo >= 0) {
      servo = servo/100.0*(steering_max - steering_zero) + steering_zero;
    } else {
      servo = servo/100.0*(steering_zero - steering_min) + steering_zero;
    }
#endif
  }

	uint32 steer = (int) ((100 + (0.55556 * (servo + 100) * 1.0)) * 16.38);

	Pwm_SetPeriodAndDuty(channel, 20000, steer);

#if SERVO_PRINT_DEBUG
	printf("infor: write servo data %d to PWM driver: \r\n", steer);
#endif

	return E_OK;
}

#define bcm2835_SetPinInTwinGpioReg bcm2835_SetWriteOnlyGpioReg
#define LED_RED		GPIO_LED_RED
#define LED_YELLOW1	GPIO_LED_YELLOW1
#define LED_YELLOW2	GPIO_LED_YELLOW2

Std_ReturnType IoHw_Write_Led(/*IN*/uint32 portDefArg1, /*IN*/uint8 portDefArg2, /*OUT*/uint32 * Pin, /*OUT*/uint8 * Level){

	uint8 LED_State = *Level;
	uint32 LED_Pin = *Pin;
#if LED_PRINT_DEBUG
	printf("infor: IoHw pin %d, level %d \r\n", LED_Pin, LED_State);

#endif
	switch (LED_Pin) {

	case GPIO_LED_RED:
		if (LED_State == 0) {
			bcm2835_SetPinInTwinGpioReg(&GPCLR0, LED_RED);
		} else {
			bcm2835_SetPinInTwinGpioReg(&GPSET0, LED_RED);
		}
		break;

	case GPIO_LED_YELLOW1:
		if (LED_State == 0) {
			bcm2835_SetPinInTwinGpioReg(&GPCLR0, LED_YELLOW1);
		} else {
			bcm2835_SetPinInTwinGpioReg(&GPSET0, LED_YELLOW1);
		}
		break;

	case GPIO_LED_YELLOW2:
		if (LED_State == 0) {
			bcm2835_SetPinInTwinGpioReg(&GPCLR0, LED_YELLOW2);
		} else {
			bcm2835_SetPinInTwinGpioReg(&GPSET0, LED_YELLOW2);
		}
		break;
	default:
		break;
	}

	return E_OK;
}
