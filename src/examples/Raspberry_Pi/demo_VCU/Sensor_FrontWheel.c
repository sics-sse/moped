/*
 * Sensor_FrontWheel.c
 *
 *  Created on: 7 sep 2014
 *      Author: sse
 */

#include <stdio.h>
#include "Rte_FrontWheelSWC.h"
#include "Os.h"


UInt32 frontWheel_Speed = 0;
//front wheel
void FrontWheelSensorReadRunnable(void){

	Rte_Call_FrontWheelSwcReadDataFromWheelSensorPort2_Read(&frontWheel_Speed);
}


void FrontWheelSensorWriteRunnable(void){

	Rte_IWrite_FrontWheelSensorWriteRunnable_FrontWheelSwcWriteDataToPirteSwcPort1_dataInt32(frontWheel_Speed);

}
