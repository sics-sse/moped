/*
 * mpu9150_sensor.c
 *
 *  Created on: 6 aug 2014
 *      Author: sse
 */
#include <stdint.h>
#include <stdio.h>
#include "mpu9150_sensor.h"
#include "I2c.h"

#define  BUFFER_LENGTH   32
uint8_t txBuffer[BUFFER_LENGTH];
uint8_t txBufferIndex = 0;
uint8_t txBufferLength = 0;

// I2C address 0x69 could be 0x68 depends on your wiring.
uint8_t MPU9150_I2C_ADDRESS = 0x68;

static void Txbuffer_Update( void ){
	txBufferIndex = 0;
	txBufferLength = 0;
}

/********************************************************************
* Function:     Write_Data()
* Input:		data
* Overview:		write the data to the txbuffer[]
********************************************************************/
static void Write_Data(uint8_t data){

	txBuffer[txBufferIndex] = data;
	++txBufferIndex;
	// update amount in buffer
	txBufferLength = txBufferIndex;
}

/********************************************************************
* Function:     Write_Sensor()
* Input:		address: register address of the mpu9150
*               data:
* Overview:		Mpu9150 sensor write.
********************************************************************/
void Write_Sensor(uint8_t addr, uint8_t data){
	i2cstate_t i2c_state;
	uint8_t sensor_addr = (MPU9150_I2C_ADDRESS);

	Write_Data(addr);
	Write_Data(data);
	i2c_state = I2c_Write(sensor_addr, txBuffer, txBufferLength);
	if ( i2c_state == I2C_TX_DONE){
		//when everything is ok, then clear the index
		//and length for the next time
		Txbuffer_Update();
	}else{
		printf("error: transfer failed\r\n");
	}

}

/********************************************************************
* Function:     Read_Sensor()
* Input:		register address of mpu9150
* Overview:		read data from Mpu9150 sensor.
********************************************************************/
int16_t Read_Sensor(uint8_t addr_l, uint8_t addr_h){
	i2cstate_t i2c_state;
	uint8_t sensor_addr = (MPU9150_I2C_ADDRESS);
	uint8_t rxBuffer[1];

	Write_Data(addr_l);
	i2c_state = I2c_Write(sensor_addr, txBuffer, txBufferLength);
	if (i2c_state == I2C_TX_DONE) {
		//when everything is ok, then clear the index
		//and length for the next time
		Txbuffer_Update();
	} else {
		printf("error: transfer failed\r\n");
	}

	//request data from MPU9150_I2C_ADDRESS
	I2c_Read(sensor_addr, rxBuffer, 1);
	uint8 data_l = rxBuffer[0];

	Write_Data(addr_h);
	i2c_state = I2c_Write(sensor_addr, txBuffer, txBufferLength);
	if (i2c_state == I2C_TX_DONE) {
		//when everything is ok, then clear the index
		//and length for the next time
		Txbuffer_Update();
	} else {
		printf("error: transfer failed\r\n");
	}

	//request data from MPU9150_I2C_ADDRESS
	I2c_Read(sensor_addr, rxBuffer, 1);
	uint8 data_h = rxBuffer[0];

	return (int16_t)((data_h<<8) + data_l);
}

/*********************************************************************
* Function:     Mpu9150_Init()
* Input:		None.
* Overview:		Performs Mpu9150 sensor initializaion and configuration.
********************************************************************/
void Mpu9150_Init(void){
	I2c_Init();
    // Clear the 'sleep' bit to start the sensor.
	Write_Sensor(MPU9150_PWR_MGMT_1, 0);
	Mpu9150_Self_Test();
	Mpu9150_Configuration();
}

/********************************************************************
* Function:     Mpu9150_Configuration()
* Input:		None.
* Overview:		Performs Mpu9150 sensor configuration.
********************************************************************/
void Mpu9150_Configuration(void) {

    //set the sample rate to 8khz/(1+7)= 1khz
	Write_Sensor(MPU9150_SMPLRT_DIV, 0x07);
	//Disable FSync, enable 256Hz DLPF
	Write_Sensor(MPU9150_CONFIG, 0x00);
	//Disable gyro self tests, scale of 500 degrees/s
	Write_Sensor(MPU9150_GYRO_CONFIG, 0x08);
	//Disable accel self tests, scale of +-2g
	Write_Sensor(MPU9150_ACCEL_CONFIG, 0x00);
	//Disable sensor output to FIFO buffer
	Write_Sensor(MPU9150_FIFO_EN, 0x00);
	//Wait for Data at Slave0
	Write_Sensor(MPU9150_I2C_MST_CTRL, 0x40);
	//Set i2c address of slave0 at 0x8C
	Write_Sensor(MPU9150_I2C_SLV0_ADDR, 0x8C);
	//Data transfer starts at an internal register within Slave 0.
	//Set where reading at slave 0 starts
	Write_Sensor(MPU9150_I2C_SLV0_REG, 0x02);
	//set offset at start reading and enable
	Write_Sensor(MPU9150_I2C_SLV0_CTRL, 0x88);
	//set i2c address at slv1 at 0x0C
	Write_Sensor(MPU9150_I2C_SLV1_ADDR, 0x0C);
	//Set where reading at slave 1 starts
	Write_Sensor(MPU9150_I2C_SLV1_REG, 0x0A);
	//Enable at set length to 1
	Write_Sensor(MPU9150_I2C_SLV1_CTRL, 0x81);
	//overvride register
	Write_Sensor(MPU9150_I2C_SLV1_DO, 0x01);
	//set delay rate
	Write_Sensor(MPU9150_I2C_MST_DELAY_CTRL, 0x03);
	Write_Sensor(0x01, 0x80);
	//set i2c slv4 delay
	Write_Sensor(MPU9150_I2C_SLV4_CTRL, 0x04);
	//override register
	Write_Sensor(MPU9150_I2C_SLV1_DO, 0x00);
	//clear usr setting
	Write_Sensor(MPU9150_USER_CTRL, 0x00);
	//override register
	Write_Sensor(MPU9150_I2C_SLV1_DO, 0x01);
	//enable master i2c mode
	Write_Sensor(MPU9150_USER_CTRL, 0x20);
	//disable slv4
	Write_Sensor(MPU9150_I2C_SLV4_CTRL, 0x13);

}

/********************************************************************
* Function:     Mpu9150_Self_Test()
* Input:		None.
* Overview:		Performs Mpu9150 sensor self test to find the
*               device address.
********************************************************************/
void Mpu9150_Self_Test(void)
{
    unsigned char Data = 0x00;

    Data = Read_Sensor(MPU9150_WHO_AM_I, 0);

    if(Data == 0x68)
    {
        printf("I2C Read Test Passed, MPU6050 Address: 0x%x\r\n", Data);
    }
    else
    {
        printf("ERROR: I2C Read Test Failed, Stopping. 0x%x\r\n", Data);
        while(1){}
    }
}

