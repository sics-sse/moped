/*
 * Actuator.c
 *
 *  Created on: 7 sep 2014
 *      Author: sse
 */
#include <stdio.h>      /* printf, fgets */
#include "Rte_MotorSWC.h"
#include "Pwm.h"
#include "Uart.h"
#include "Rte_Internal.h"
#include "MOPED_DEBUG.h"

extern boolean pirte_write_speed;

UInt32 speed_select_option = 0;

/**
 *  	 (Y)Pirte_data|   |Selection_data (S)
 *    		         _|___|_
 *          	    |		|
 * PWM_drvier(Z)____| speed |
 *          	 	|  SWC  |
 *         		    |_______|
 *              	    |
 *              	    |(X) Manual driving
 */
//motor
void MotorControlRunnable(void){

	UInt8 speed_value_pirte = 0;
    UInt8* speed_value = NULL;

/*    //p
    speed_select_option = Rte_IRead_MotorControlRunnable_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32();
    //y
    speed_value_pirte = Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32();
    //x
    speed_value = Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer();

    if(speed_select_option < 50 ){ // priority < limit(based priority 50)
    	if(speed_value[0] != 0){
    		Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(&speed_value[0]);// z = x
#if SPEED_PRINT_DEBUG
    	printf("infor: remote control speed: %d\r\n", speed_value[0]);
#endif
    	}
    }else{
    	Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(&speed_value_pirte);//z = y
#if SPEED_PRINT_DEBUG
    	printf("infor: prite control speed: %d\r\n", speed_value_pirte);
#endif

    }*/

    if(pirte_write_speed == true){

    	speed_value_pirte = Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32();
    	Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(&speed_value_pirte);
    	pirte_write_speed = false;
#if SPEED_PRINT_DEBUG
    	printf("infor: autopath speed: %d\r\n", speed_value_pirte);
#endif

    }else{
    	speed_value = Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer();
    	Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(&speed_value[0]);
#if SPEED_PRINT_DEBUG
    	printf("infor: remote control speed: %d\r\n", speed_value[0]);
#endif
    }


}

