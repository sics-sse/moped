/*
 * Sensor.c
 *
 *  Created on: 7 sep 2014
 *      Author: sse
 */
#include <stdint.h>
#include <stdio.h>
#include "Rte_AdcSWC.h"

UInt32 AdcSensorData = 0;
//adc sensor
void AdcSensorReadRunnable(void) {

	Rte_Call_AdcSwcReadDataFromAdcDriverPort2_Read(&AdcSensorData);
}

void AdcSensorWriteRunnable(void){

	Rte_IWrite_AdcSensorWriteRunnable_AdcSwcWriteDataToPirteSwcPort1_dataInt32(AdcSensorData);

}


