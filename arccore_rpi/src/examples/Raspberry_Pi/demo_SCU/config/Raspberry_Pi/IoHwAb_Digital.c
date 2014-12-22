/*
 * This code is not automatically generated.
 * Because Pi dose not have the IoHw layer
 * in the ArcCore tools.
 *
 *  Created on: 28 okt 2014
 *      Author: sse
 */
#include <stdint.h>
#include <stdio.h>

#include "IoHwAb.h"
#include "IoHwAb_Internal.h"
#include "IoHwAb_Digital.h"

#if defined(USE_DIO)
#include "Dio.h"
#else
#error "DIO Module is needed by IOHWAB"
#endif

//#if defined(USE_I2C)
#include "mpu9150_sensor.h"
//#endif

#define IS_VALID_DIO_LEVEL(_x) ((STD_LOW == (_x)) || (STD_HIGH == (_x)))

extern uint32 UltrasonicSensor_Read(void);
Std_ReturnType UltraSensorRead(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data){

	*Data  = UltrasonicSensor_Read();
//    printf("infor: IoHw distance: %d\r\n", *Data);
	return E_OK;
}


Std_ReturnType ImuSensorRead(/*IN*/uint16 portDefArg1, /*IN*/uint16* Data){
    IMUSensortype Type;
    Type = portDefArg1;
    switch(Type){
    case Gyro:
    	Data[0] = Read_Sensor(MPU9150_GYRO_XOUT_L, MPU9150_GYRO_XOUT_H);
    	Data[1] = Read_Sensor(MPU9150_GYRO_YOUT_L, MPU9150_GYRO_YOUT_H);
    	Data[2] = Read_Sensor(MPU9150_GYRO_ZOUT_L, MPU9150_GYRO_ZOUT_H);
//    	Data[0] = 23;
//    	Data[1] = 34;
//    	Data[2] = 45;
//    	printf("infor: IoHWgyo: %d %d %d\r\n", Data[0], Data[1], Data[2]);
    	break;
    case Acc:
		Data[0] = Read_Sensor(MPU9150_ACCEL_XOUT_L, MPU9150_ACCEL_XOUT_H);
		Data[1] = Read_Sensor(MPU9150_ACCEL_YOUT_L, MPU9150_ACCEL_YOUT_H);
		Data[2] = Read_Sensor(MPU9150_ACCEL_ZOUT_L, MPU9150_ACCEL_ZOUT_H);
//		Data[0] = 66;
//		Data[1] = 88;
//		Data[2] = 99;
//		printf("infor: IoHWacc: %d %d %d\r\n", Data[0], Data[1], Data[2]);
    	break;
    default:
    	break;
    }

    return E_OK;
}


