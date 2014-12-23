/*
 * Imu.c
 *
 *  Created on: 8 sep 2014
 *      Author: sse
 */
#include <stdint.h>
#include <stdio.h>
#include "Rte_ImuSWC.h"
#include "Uart.h"
#include "mpu9150_sensor.h"
//extern int16_t Read_Sensor(uint8_t addr_l, uint8_t addr_h);

void ImuSwcRunnable(void){
	uint16 acc[3];
	uint16 gyr[3];

	Rte_Call_ImuSwcToIoHwGyroDataInPort3_Read(gyr);
	Rte_Call_ImuSwcToIoHwAcceDataInPort4_Read(acc);
//	printf("infor: IMUacc %d %d %d\r\n", acc[0], acc[1], acc[2]);
//	printf("infor: IMUGyr %d %d %d\r\n", gyr[0], gyr[1], gyr[2]);
	Rte_IWrite_ImuSwcRunnable_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16(gyr);
	Rte_IWrite_ImuSwcRunnable_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16(acc);

//	acc[0] = Read_Sensor(MPU9150_ACCEL_XOUT_L, MPU9150_ACCEL_XOUT_H);
//	acc[1] = Read_Sensor(MPU9150_ACCEL_YOUT_L, MPU9150_ACCEL_YOUT_H);
//	acc[2] = Read_Sensor(MPU9150_ACCEL_ZOUT_L, MPU9150_ACCEL_ZOUT_H);
//	acc[0] = 23;
//	acc[1] = 34;
//	acc[2] = 45;
//	Rte_IWrite_ImuSwcRunnable_ImuSwcToPirteSwcAcceDataOutPort2_CharArray31(acc);


//	gyr[0] = Read_Sensor(MPU9150_GYRO_XOUT_L, MPU9150_GYRO_XOUT_H);
//	gyr[1] = Read_Sensor(MPU9150_GYRO_YOUT_L, MPU9150_GYRO_YOUT_H);
//	gyr[2] = Read_Sensor(MPU9150_GYRO_ZOUT_L, MPU9150_GYRO_ZOUT_H);
//	gyr[0] = 1234;
//	gyr[1] = 5678;
//	gyr[2] = 8979;
//	Rte_IWrite_ImuSwcRunnable_ImuSwcToPirteSwcGyroDataOutPort1_CharArray31(gyr);

}
