/*
 * java.c
 *
 *  Created on: Apr 10, 2014
 *      Author: zeni
 */

#include <stdint.h>
#include <stdio.h>
#include "Uart.h"
#include "Os.h"
#include "MOPED_signal.h"

extern boolean acknowledgementTCU_Flag;
extern CanPackage PackageInstallation;
extern boolean pluginCommunicationTCU_Flag;
extern boolean plugin_new_packageFromTCU;

// common APIs
void output(char* s) {
	pi_printf(s);
}

void output2(char* s, int length) {
  pi_printf2(s, length);
}

char autosarFetchByte(int rearIndex) {
	char res;
	if (PackageInstallation.indexReadStart > rearIndex) {
		res = BufferForPluginInstallation[PackageInstallation.indexReadStart++];
		if (PackageInstallation.indexReadStart == BUFFERSIZE)
			PackageInstallation.indexReadStart = 0;
	} else if (PackageInstallation.indexReadStart < rearIndex) {
		res = BufferForPluginInstallation[PackageInstallation.indexReadStart++];
	}
	return res;
}

void autosarSendAckByte(char data) {
	ack_data = data;
	acknowledgementTCU_Flag = true;
}

void autosarSendPackageData(int size, char* data) {
	int i;

	// pluginCommunicationTCU_Flag true means that ack_array still contains
	// stuff that hasn't been sent yet, so we drop this package.
	if (pluginCommunicationTCU_Flag) {
	  return;
	}

	for (i = 0; i < size; i++) {
		ack_array[i] = data[i];
	}
	writeTotalsize = size;
	pluginCommunicationTCU_Flag = true;
}

char* autosarFetchNewData(int startIndex, int rearIndex) {
	char* res;
	if (startIndex > rearIndex) {
		int size = BUFFERSIZE - startIndex + rearIndex;
		char data[size];
		for (int i = 0; i < size; i++) {
			data[i] = BufferForPluginInstallation[startIndex++];
			if (startIndex == BUFFERSIZE)
				startIndex = 0;
		}
		res = data;
	} else if (startIndex < rearIndex) {
		int size = rearIndex - startIndex;
		char data[size];
		for (int j = 0; j < size; j++) {
			data[j] = BufferForPluginInstallation[startIndex++];
		}
		res = data;
	}
	PackageInstallation.indexReadStart = startIndex;
	return res;
}


int autosarCheckIfNewPackage() {
	if (plugin_new_packageFromTCU == true) {
		plugin_new_packageFromTCU = false;
		return 1;
	} else {
		return 0;
	}
}

extern UInt32 readTotalSize;
int autosarGetLengthPackage() {
	return readTotalSize;
}

int autosarGetReadStartIndex() {
	return PackageInstallation.indexReadStart;
}

int autosarGetReadRearIndex() {
	return PackageInstallation.indexReadEnd;
}

////////////////////////////////////////////////

// VCU APIs
#if VCU
extern int PirteWriteSpeedPwmData(int speed);
extern int PirteWriteSteerPwmData(int servo);
extern UInt32 getFrontWheelSpeed(void);
extern UInt32 getRearWheelSpeed(void);
extern int readAdcDataFromPort(void);
extern uint64 readPositionFromPort(void);
extern CanPackage PackageReadFromSCU;
extern CanPackage PackageReadFromTCU;
extern Rte_PirteSWCProto_Pirte_WriteLedData_Runnable(void);
extern UInt8 Pin;
extern UInt8 Level;
extern UInt32 Select_Speed;
extern UInt32 Select_Servo;
int select = 0;
#endif

void autosarSendSpeedPwmData(int speed) {
//#if VCU
//	printf("[autosarSendPwmData][speed: %d]",speed);
	PirteWriteSpeedPwmData(speed);
//#endif
}

void autosarSendSteerPwmData(int servo) {
#if VCU
//	printf("[autosarSendPwmData][servo: %d]",servo);
	PirteWriteSteerPwmData(servo);
#endif
}

int autosarFetchFrontWheelSpeed(void) {
#if VCU
	return (int)getFrontWheelSpeed();
#else
	return 0;
#endif
}

int autosarFetchSteer(void) {
#if VCU
	return 0;
#else
	return 0;
#endif
}

int autosarFetchBackWheelSpeed(void) {
#if VCU
	return (int)getRearWheelSpeed();
#else
	return 0;
#endif
}

long autosarFetchAdcData(void) {
#if VCU
	int volt = readAdcDataFromPort();
	float val = volt * 3.3 / 1024.0 / 0.32;
	return val * 10000;
#else
	return 0;
#endif
}

long long autosarReadPosition(void) {
#if VCU
	long long res = readPositionFromPort();
	return res;
#else
	return 0;
#endif
}

int autosarReadPluginDataSizeFromSCU(void) {
#if VCU
	return PackageReadFromSCU.TotalReadSize;
#else
	return 0;
#endif
}

char autosarReadPluginDataByteFromSCU(int index) {
#if VCU
	return (char)BufferForCommunicationSCUtoVCU[index];
#else
	return 0;
#endif
}

void autosarResetPluginDataSizeFromSCU(void) {
#if VCU
	PackageReadFromSCU.TotalReadSize = 0;
#endif
}

extern uint32 led_pattern0;
extern uint32 led_count;

void autosarSetLED(int pin, int val) {
  if (pin == -1) {
    led_pattern0 = val;
    led_count = 0;
    return;
  }

#if VCU
	switch(pin) {
	case 1:
		Pin = GPIO_LED_RED;
		break;
	case 2:
		Pin = GPIO_LED_YELLOW1;
		break;
	case 3:
		Pin = GPIO_LED_YELLOW2;
		break;
	}
	Level = val;
	Rte_PirteSWCProto_Pirte_WriteLedData_Runnable();
#endif
}

void autosarSetSpeedWithSelector(int speed, int selector) {
#if VCU
	Select_Speed = selector;
	autosarSendSpeedPwmData(speed);
#endif
}

void autosarSetSteerWithSelector(int steer, int selector) {
#if VCU
	Select_Servo = selector;
	autosarSendSteerPwmData(steer);
#endif
}

int autosarReadPluginDataSizeFromTCU(void) {
#if VCU
	return PackageReadFromTCU.TotalReadSize;
#else
	return 0;
#endif
}

char autosarReadPluginDataByteFromTCU(int index) {
#if VCU
	return (char)BufferForCommunication[index];
#else
	return 0;
#endif
}

void autosarResetPluginDataSizeFromTCU(void) {
#if VCU
	PackageReadFromTCU.TotalReadSize = 0;
#endif
}

int autosarFetchSpeedFromPirte(void) {
#if VCU
	return 0;
#else
	return 0;
#endif
}

int autosarFetchSteerFromPirte(void) {
#if VCU
	return 0;
#else
	return 0;
#endif
}

void autosarSetSelect(int selector) {
#if VCU
	select = selector;
#endif
}

////////////////////////////////////////////////

// SCU
#if SCU
UInt16 readDistanceFromPort(void);
extern DataPackage UltraData;
extern boolean getDistance_Flag;
extern boolean pluginCommunicationSCUtoVCU_Flag;
extern CanPackage PackageWriteToVCU;
extern uint64 IMU_acc;
extern uint64 IMU_gyr;
#endif

// VCU
#if VCU
extern boolean pluginCommunicationVCUtoSCU_Flag;
extern CanPackage PackageWriteToSCU;
#endif

int autosarReadUltrasonicData(void) {
#if SCU
	return readDistanceFromPort();
#else
	return 0;
#endif
}

void autosarWritePluginData2VCU(int size, char* value) {
#if SCU
	//write to VCU, for test
	PackageWriteToVCU.TotalWriteSize = size;
	PackageWriteToVCU.CanWriteBuffer = &value[0];
	PackageWriteToVCU.CommunicationType = plugin_communication_SCU_to_VCU;
	PackageWriteToVCU.indexWriteEnd = 0;
	PackageWriteToVCU.indexReadStart = 0;
	PackageWriteToVCU.PackageState.Done = false;
    pluginCommunicationSCUtoVCU_Flag = true;
#endif
#if VCU
	PackageWriteToSCU.TotalWriteSize = size;
	PackageWriteToSCU.CanWriteBuffer = &value[0];
	PackageWriteToSCU.CommunicationType = plugin_communication_VCU_to_SCU;
	PackageWriteToSCU.indexWriteEnd = 0;
	PackageWriteToSCU.indexReadStart = 0;
	PackageWriteToSCU.PackageState.Done = false;
    pluginCommunicationVCUtoSCU_Flag = true;
#endif
}

long long autosarReadIMUPart1(void) {
#if SCU
	return (long long)IMU_acc;
#else
	return 0;
#endif
}

long long autosarReadIMUPart2(void) {
#if SCU
	return (long long)IMU_gyr;
#else
	return 0;
#endif
}
