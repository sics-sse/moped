/*
 * Sensor_BackWheel.c
 *
 *  Created on: 7 sep 2014
 *      Author: sse
 */
#include <stdio.h>
#include "Rte_RearWheelSWC.h"

UInt32 rearWheel_speed = 0;
//rear wheel
void RearWheelSensorReadRunnable(void){

	Rte_Call_RearWheelSwcReadDataFromWheelSensorPort2_Read(&rearWheel_speed);
}


void RearWheelSensorWriteRunnable(void){

	Rte_IWrite_RearWheelSensorWriteRunnable_RearWheelSwcWriteDataToPirteSwcPort1_dataInt32(rearWheel_speed);

}
