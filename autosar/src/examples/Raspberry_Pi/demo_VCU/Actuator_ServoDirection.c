/*
 * Actuator_ServoDirection.c
 *
 *  Created on: 7 sep 2014
 *      Author: sse
 */

#include <stdio.h>      /* printf, fgets */
#include "Rte_ServoSWC.h"
#include "Pwm.h"
#include "Uart.h"
#include "Rte_Internal.h"
#include "MOPED_DEBUG.h"

extern boolean pirte_write_servo;
UInt32 steer_select_option = 0;

/**
 *  	 (Y)Pirte_data|   |Select Option (S/P)
 *    		         _|___|_
 *          	    |		|
 * PWM_driver(Z)____| Servo |
 *          	 	|  SWC  |
 *         		    |_______|
 *              	    |
 *              	    |(X) Manual driving
 */

//direction
void ServoControlRunnable(void){

	 UInt8 steer_value_pirte = 0;
	 UInt8* steer_value = NULL;
/*
	//p
	steer_select_option = Rte_IRead_ServoControlRunnable_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32();
	//y
	steer_value_pirte =	Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32();
	//x
	steer_value = Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer();

	if (steer_select_option < 50) { // priority < limit(based priority 50)
		if (steer_value[0] != 0) {
			Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(&steer_value[1]); // z = x
#if SERVO_PRINT_DEBUG
			printf("infor: remote control servo: %d\r\n", steer_value[0]);
#endif
		}
	} else {
		Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(&steer_value_pirte); //z = y
#if SERVO_PRINT_DEBUG
		printf("infor: prite control servo: %d\r\n", steer_value_pirte);
#endif

	}

*/

	 if(pirte_write_servo == true){
		 steer_value_pirte = Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32();
		 Rte_Call_ServoSWC_ServoSWCProto_ServoSwcWriteDataToIoHwPwmPort3_Write(&steer_value_pirte);
		 pirte_write_servo = false;
#if SERVO_PRINT_DEBUG
		 printf("infor: autopath servo: %d\r\n", steer_value_pirte);
#endif

	 }else{
		 steer_value = Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer();
		 Rte_Call_ServoSWC_ServoSWCProto_ServoSwcWriteDataToIoHwPwmPort3_Write(&steer_value[1]);
#if SERVO_PRINT_DEBUG
		 printf("infor: remote controll servo: %d\r\n", steer_value[1]);
#endif
	 }



}


