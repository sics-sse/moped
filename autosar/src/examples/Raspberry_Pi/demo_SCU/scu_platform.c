/*
 * scu_platform.c
 *
 *  Created on: 28 okt 2014
 *      Author: sse
 */
#include <stdint.h>
#include <stdio.h>
#include "Rte_PirteSWC.h"
#include "Uart.h"
#include "Os.h"
#include "MOPED_signal.h"
#include "MOPED_DEBUG.h"

#define FRAME_TYPE        		0xF0
#define DATALENGTH_H      	  	0x0F
//single frame
#define SINGLE_FRAME_SIZE       7
#define SF_PCI_BYTE             1
//first frame
#define FIRST_FRAME_SIZE 		4
#define FF_PCI_BYTE             4
//consecutive frame
#define CONSECUTIVE_FRAME_SIZE  7
#define CF_PCI_BYTE             1
#define SEQUENCE_NUMBER         0x0F

#define NO_FRAME                0xDEAD

boolean acknowledgementVCU_Flag = false;
boolean acknowledgementTCU_Flag = false;
boolean pluginCommunicationTCU_Flag = false;
boolean acknowledgementVCU_ComWrite = false;
boolean singleframe_Flag = false;
boolean firstframe_Flag = false;
boolean packageFirstFrame_Flag = false;
boolean getDistance_Flag = false;
boolean getIMUdata_Flag = false;
boolean pluginCommunicationSCUtoVCU_Flag = false;

//typedef enum {
//	SINGLE_FRAME = 0,
//	FIRST_FRAME = 1,
//	CONSECUTIVE_FRAME = 2
//} ISO15765FrameType;

//typedef enum {
//	plugin_Installation_SCU = 0,
//	acknowledgement_SCU = 1,
//	plugin_communication_VCU_to_SCU = 2,
//	plugin_communication_SCU_to_VCU = 3,
//	plugin_communication_SCU_to_TCU = 4,
//	plugin_communication_TCU_to_SCU = 5,
//	no_communication,
//} platformCommunicationType;

//typedef struct{
//	boolean Start;
//	boolean Pending;
//	boolean Done;
//}CanPackageState;

//typedef struct{
//	platformCommunicationType CommunicationType;
//	UInt32 TotalReadSize;
//	UInt32 TotalWriteSize;
//	UInt32 indexReadStart;
//	UInt32 indexReadEnd;
//	UInt32 indexWriteStart;
//	UInt32 indexWriteEnd;
//	sint8* CanReadBuffer;
//	sint8* CanWriteBuffer;
//	uint16 nextFrameRead;
//	CanPackageState PackageState;
//}CanPackage;


///**
// * definition of Ultrasonic data package
// */
//typedef struct{
//	UInt8 size;
//    UInt8 data[12];
//}DataPackage;
//DataPackage UltraData;
//DataPackage IMUData;

UInt8 sequenceNumberWrite = 0;
//sint8 BufferForPluginInstallation[1000];
//sint8 BufferForCommunication[1000];
UInt32 readTotalSize = 0;
UInt32 sizeIndexRead = 0;
//uint16 nextFrameRead = 0;
UInt8 WriteframeType = FIRST_FRAME;
UInt32 countWrite = 0;

//UInt8 ack_data;
//UInt8 ack_array[1000] = {-34, -83, -66, -17, 0, 1, 0, 1, 0, 0, 0, 2, 0, 81, 70, 89, 0, 16, 109, 101, 109, 111, 114, 121, 58, 98, 111, 111, 116, 115, 116, 114, 97, 112, 0, 0, 0, 4, 0, 0, 3, 0, -17, -25, -1, 11, -4, 63, 5, 56, -123, 2, -3, -65, 32, 64, 32, 0, 16, 0, 65, 64, 8, 126, 1, -96, 0, 0, -36, -24, 1, 0, -80, -67, 4, 0, 8, -64, 4, 0, 32, -64, 4, 0, 1, 0, 0, 0, 4, 0, 0, 0, 112, -64, 4, 0, 120, -64, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 88, -67, 4, 0, 1, 0, 0, 0, 5, 0, 0, 0, -92, 95, 1, 0, -72, -67, 4, 0, -4, 32, 1, 0, -72, -67, 4, 0, 4, -66, 4, 0, 104, -66, 4, 0, 36, 42, 4, 0, 0, 0, 0, 0, 12, 7, 0, 0, 120, 10, 0, 0, -68, -66, 4, 0, -60, -66, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, 80, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, 4, 9, 45, 0, 0, 0, -52, 7, 1, 0, -108, 7, 0, 0, -80, 7, 0, 0, -52, 7, 0, 0, -12, 7, 0, 0, 68, 8, 0, 0, 96, 8, 0, 0, -116, 8, 0, 0, -88, 8, 0, 0, -52, 8, 0, 0, 4, 9, 0, 0, 68, -66, 4, 0, 23, 0, 0, 0, 0, 3, 34, 4, -72, -67, 4, 0, 105, 0, 0, 0, -128, -52, 2, 0, -61, 22, 16, -84, 88, 23, 16, -84, 88, 25, 16, -84, 88, 24, 16, -84, 88, 21, 16, -84, 88, 26, 16, -84, 88, -67, 0, 0, 9, 0, 0, 0, -52, 7, 1, 0, -124, -66, 4, 0, -96, -66, 4, 0, 23, 0, 0, 0, 0, 3, 34, 4, -72, -67, 4, 0, 33, 0, 0, 0, -128, -52, 2, 0, -61, 64, 17, -82, 0, -30, 64, -64, 23, 0, 0, 0, 0, 7, 67, 4, -72, -67, 4, 0, 81, 0, 0, 0, -128, -52, 2, 0, -72, 1, 20, 16, -84, 88, 18, -23, 18, -82, 0, 49, 33, -88, 10, 19, 16, -84, 88, -67, 1, 0, 0, 0, 92, 123, 1, 0, 45, 0, 0, 0, -52, 7, 1, 0, -64, 35, 0, 0, 12, 7, 0, 0, -72, -67, 4, 0, -8, -66, 4, 0, 16, -65, 4, 0, 52, -65, 4, 0, 80, -65, 4, 0, -116, -65, 4, 0, -68, -65, 4, 0, -44, -65, 4, 0, -12, -65, 4, 0, 61, 0, 0, 0, 24, -54, 2, 0, 67, 76, 73, 45, 109, 97, 105, 110, 32, 100, 111, 110, 101, 13, 10, 0, 101, 0, 0, 0, 24, -54, 2, 0, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 73, 110, 112, 117, 116, 46, 109, 97, 105, 110, 40, 41, 13, 10, 0, 0, 0, 69, 0, 0, 0, 24, -54, 2, 0, 69, 105, 108, 101, 105, 116, 32, 105, 115, 32, 98, 97, 99, 107, 33, 13, 10, 0, 0, 0, -59, 0, 0, 0, 24, -54, 2, 0, 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 32, 102, 114, 111, 109, 32, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 73, 110, 112, 117, 116, 32, 40, 111, 110, 108, 121, 32, 98, 121, 116, 101, 115, 41, 33, 13, 10, 0, 0, 0, -107, 0, 0, 0, 24, -54, 2, 0, 73, 115, 32, 105, 116, 32, 114, 101, 97, 108, 108, 121, 32, 119, 111, 114, 107, 105, 110, 103, 32, 100, 121, 110, 97, 109, 105, 99, 97, 108, 108, 121, 63, 63, 63, 13, 10, 0, 0, 0, 65, 0, 0, 0, 24, -54, 2, 0, 74, 97, 107, 111, 98, 32, 105, 115, 32, 104, 101, 114, 101, 33, 13, 10, 93, 0, 0, 0, 24, -54, 2, 0, 79, 104, 44, 32, 121, 101, 115, 44, 32, 105, 116, 32, 105, 115, 33, 33, 33, 33, 33, 33, 33, 13, 10, 0, 41, 0, 0, 0, 24, -54, 2, 0, 110, 101, 119, 32, 116, 114, 121, 46, 46, 46, 0, 0, 65, 0, 0, 0, 24, -54, 2, 0, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 73, 110, 112, 117, 116, 5, 0, 0, 0, 88, -125, 1, 0, 40, -64, 4, 0, 16, -128, 1, 0, -72, -67, 4, 0, 60, -64, 4, 0, -80, -67, 4, 0, -91, 0, 0, 0, -104, -55, 0, 0, 12, 2, 15, 1, 10, 11, 112, 114, 105, 110, 116, 83, 116, 114, 105, 110, 103, 10, 3, 11, -119, 32, 0, 6, 60, 105, 110, 105, 116, 62, 1, 9, 9, 1, 4, 109, 97, 105, 110, 10, 34, 0, 0, 0, 1, 0, 0, 0, -96, 5, 2, 0, 1, 0, 0, 0, 48, 5, 2, 0, -1};

//UInt32 ack_array_size = 837;
//UInt32 ack_count = 0;

UInt32 sizeIndexWrite = 0;

void Can_Read_Single_Frame(uint8* tempBuffer, CanPackage * CANPackage){

	readTotalSize = tempBuffer[0];
//    printf("infor (single): size %d, %d, %d, %d, %d\r\n",readTotalSize, tempBuffer[0], tempBuffer[1], tempBuffer[2], tempBuffer[3]);
    CANPackage->TotalReadSize  = readTotalSize;
//    printf("infor: size %d \r\n",CANPackage->TotalReadSize);
    CANPackage->nextFrameRead = 0;
    CANPackage->indexReadStart = 0;
    CANPackage->indexReadEnd = 0;
    //store the data
    for(int i = 0; i < CANPackage->TotalReadSize; i++){
    	CANPackage->CanReadBuffer[CANPackage->indexReadEnd] = tempBuffer[i+SF_PCI_BYTE];
    	CANPackage->indexReadEnd++ ;
    	CANPackage->readBytes++;
//    	printf("Index: %d\r\n",CANPackage->indexReadEnd);
    }
    CANPackage->PackageState.Done = true;
}

/**
 *
 * @param tempBuffer
 * @param CANPackage
 */
void Can_Read_First_Frame(uint8* tempBuffer, CanPackage * CANPackage){

	readTotalSize = ((tempBuffer[0] & DATALENGTH_H) << 24) + (tempBuffer[1] << 16) + (tempBuffer[2] << 8) + tempBuffer[3];
	//printf("infor: size %d, %d, %d, %d, %d\r\n",readTotalSize, tempBuffer[0], tempBuffer[1], tempBuffer[2], tempBuffer[3]);
    CANPackage->TotalReadSize  = readTotalSize;
    //printf("infor: size %d \r\n",CANPackage->TotalReadSize);
    packageFirstFrame_Flag = true;
    CANPackage->nextFrameRead = 0;
    CANPackage->indexReadStart = 0;
    CANPackage->indexReadEnd = 0;
    //store the data
    for(int i = 0; i < FIRST_FRAME_SIZE; i++){
    	CANPackage->CanReadBuffer[CANPackage->indexReadEnd] = tempBuffer[i+FF_PCI_BYTE];
    	CANPackage->indexReadEnd++ ;
    	CANPackage->readBytes++;
    	//printf("Index: %d\r\n",CANPackage->indexReadEnd);
    }
}

/**
 *
 * @param tempBuffer
 * @param CANPackage
 */
void Can_Read_Consecutive_Frame(uint8* tempBuffer, CanPackage * CANPackage){
  //printf("infor: consecutive frame\r\n");
	uint16 sequenceNumber = tempBuffer[0] & SEQUENCE_NUMBER;

	if(sequenceNumber == CANPackage->nextFrameRead){
		for (int i = 0; i < CONSECUTIVE_FRAME_SIZE; i++) {
			CANPackage->CanReadBuffer[CANPackage->indexReadEnd] = tempBuffer[i+CF_PCI_BYTE];
			CANPackage->indexReadEnd++;
			CANPackage->readBytes++;
			//printf("Index: %d\r\n",CANPackage->indexReadEnd);
			if(CANPackage->indexReadEnd >= BUFFERSIZE){
				CANPackage->indexReadEnd = 0;
			}
			if(CANPackage->TotalReadSize == (CANPackage->readBytes)){
			  //printf("endSize: %d\r\n",CANPackage->indexReadEnd);
				CANPackage->PackageState.Done = true;
				CANPackage->nextFrameRead = NO_FRAME;
				break;
			}
		}
		//printf("nextframeread %d\r\n", CANPackage->nextFrameRead);
		if(CANPackage->nextFrameRead >= 15){
			if(CANPackage->nextFrameRead != NO_FRAME)
				CANPackage->nextFrameRead = 0;
		}else{
			CANPackage->nextFrameRead++;
		}
	}else{
	  printf("consec: not same sequence number: %d %d\r\n", sequenceNumber, CANPackage->nextFrameRead);
		//nothing
	}


}


void Can_Send_Single_Frame(CanPackage* CANPackage) {
	UInt8 length = 0;
	UInt8 i;
	UInt8 data[8];
	UInt8 CommunicationType = CANPackage->CommunicationType;
	//todo: appsize should smaller than 4095
	//send first frame, calculate the data length
	length = (UInt8)CANPackage->TotalWriteSize;
	//construct the first frame
	data[0] = length;
	//printf("infor: send single frame size: %d, data: ", data[0]);
	for (i = 0; i < length; i++) {
		data[i + SF_PCI_BYTE] = CANPackage->CanWriteBuffer[i];
		//printf("%d ", data[i + SF_PCI_BYTE]);
	}
	//printf("\r\n");
	//send single frame
	switch(CommunicationType){
		case acknowledgement_SCU:
			//
		break;

		case plugin_communication_SCU_to_VCU:
			Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication(data);
		break;

		case plugin_communication_SCU_to_TCU:
			Rte_IWrite_SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(data);
		break;

		default:
		//nothing
	  	break;
	}
	CANPackage->PackageState.Done = true;
}

void Can_Send_First_Frame(CanPackage* CANPackage) {
	UInt8 length1 = 0;
	UInt8 length2 = 0;
	uint8 length3 = 0;
	UInt8 length4 = 0;
	UInt8 i;
	UInt8 data[8];

	UInt32 appsize = CANPackage->TotalWriteSize;
	UInt32 CommunicationType = CANPackage->CommunicationType;

	sequenceNumberWrite = 0;

	//send first frame, calculate the data length
	length1 = (uint8) (appsize >> 24) & 0x0F;
	length2 = (uint8) (appsize >> 16) & 0xFF;
	length3 = (uint8) (appsize >> 8) & 0xFF;
	length4 = (uint8) appsize & 0xFF;
	//construct the first frame
	data[0] = (FIRST_FRAME << 4) + length1;
	data[1] = length2;
	data[2] = length3;
	data[3] = length4;
//	printf("infor: send first frame %d, %d, %d, %d ", data[0], data[1], data[2], data[3]);
	for (i = 0; i < FIRST_FRAME_SIZE; i++) {
		data[i + FF_PCI_BYTE] = CANPackage->CanWriteBuffer[CANPackage->indexWriteEnd];
//		printf("%d ", data[i + FF_PCI_BYTE]);
		CANPackage->indexWriteEnd++;
//		printf("infor: writeIndex %d\r\n",CANPackage->indexWriteEnd);
	}
//	printf("\r\n");
	//send first frame
	switch (CommunicationType) {
		case acknowledgement_SCU:
		break;

		case plugin_communication_SCU_to_VCU:
			Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication(data);
		break;

		case plugin_communication_SCU_to_TCU:
			Rte_IWrite_SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(data);
		break;

	default:
		//nothing
		break;
	}

}


void Can_Send_Consecutive_Frame(CanPackage* CANPackage) {
	UInt8 j;
	UInt8 data[8];
	UInt8 CommunicationType = CANPackage->CommunicationType;
//	printf("infor: send consecutive frame\r\n");
		//send consecutive frame, construct the first frame
	data[0] = (uint8) (CONSECUTIVE_FRAME << 4) + (sequenceNumberWrite & 0xF);
//	printf("%d ", data[0]);
	for (j = 0; j < CONSECUTIVE_FRAME_SIZE; j++) {
		data[j + CF_PCI_BYTE] = CANPackage->CanWriteBuffer[CANPackage->indexWriteEnd];
		CANPackage->indexWriteEnd++;
//		printf("infor: writeIndex %d\r\n",CANPackage->indexWriteEnd);
//		printf("%d ", data[j + CF_PCI_BYTE]);
	}
//	printf("\r\n");

	if (sequenceNumberWrite >= 15) {
		sequenceNumberWrite = 0;
	} else {
		sequenceNumberWrite++;
	}
	//send consecutive frame
	switch(CommunicationType){
			case acknowledgement_SCU:
				//
			break;
			case plugin_communication_SCU_to_VCU:
				//
				Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication(data);
			break;
			case plugin_communication_SCU_to_TCU:
				Rte_IWrite_SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(data);
			break;

			default:
			//nothing
		  	break;
		}

}

void Can_Send_Package(CanPackage* CANPackage){
    UInt32 packageSize = CANPackage->TotalWriteSize;

	if (packageSize < 8) {
		Can_Send_Single_Frame(CANPackage);
		singleframe_Flag = true;
	} else {
		switch (WriteframeType)
		{
			case FIRST_FRAME:
				//send first frame
				Can_Send_First_Frame(CANPackage);
				if((packageSize - FIRST_FRAME_SIZE)% CONSECUTIVE_FRAME_SIZE == 0){
					countWrite = (packageSize - FIRST_FRAME_SIZE)/ CONSECUTIVE_FRAME_SIZE;
				}else{
					countWrite = (packageSize - FIRST_FRAME_SIZE)/ CONSECUTIVE_FRAME_SIZE + 1;
				}
				WriteframeType = CONSECUTIVE_FRAME;
			break;

			case CONSECUTIVE_FRAME:
				//send consecutive frame
				Can_Send_Consecutive_Frame(CANPackage);
				countWrite--;
				if (countWrite == 0){
					firstframe_Flag = true;
					WriteframeType = FIRST_FRAME;
					CANPackage->PackageState.Done = true;
				}
			break;

			default:
			//nothing
			break;
		}

	}
}

void Can_Read_Package(CanPackage* CANPackage){

    UInt8 frameType = 0;
    UInt8 tempBuffer[8];
    UInt8* pluginData = NULL;

    UInt8 CommunicationType = CANPackage->CommunicationType;

    //printf("read package, start = %d, end = %d\r\n", CANPackage->indexReadStart, CANPackage->indexReadEnd);
    switch(CommunicationType)
    {
    	case plugin_Installation_SCU:
    		pluginData = Rte_IRead_SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication();
        	break;
    	case plugin_communication_VCU_to_SCU:
    		pluginData = Rte_IRead_SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_pluginCommunication();
    		break;
    	case plugin_communication_TCU_to_SCU:
    		pluginData = Rte_IRead_SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_pluginCommunication();
    	    break;
    	default:
		  printf("CommunicationType %d\r\n", CommunicationType);
    	    break;
    }
    memcpy(tempBuffer, pluginData, 8);
    //get type of frame
    frameType = (tempBuffer[0] & FRAME_TYPE) >> 4;
//    printf("infor: frameType %d\r\n",frameType);
    switch(frameType){
    	case SINGLE_FRAME:
    		Can_Read_Single_Frame(tempBuffer, CANPackage);
    	break;
    	case FIRST_FRAME:
    		Can_Read_First_Frame(tempBuffer, CANPackage);
    	break;

    	case CONSECUTIVE_FRAME:
    		Can_Read_Consecutive_Frame(tempBuffer, CANPackage);
    	break;

    	default:
		  printf("unknown frameType %d\r\n", frameType);
    		//nothing
    		break;
    }
}

static boolean init_install = false; //temp
CanPackage PackageInstallation;
boolean plugin_new_packageFromTCU = false;
void SCU_Installation_TCU_ReadRunnable(void) {

	if(init_install == false){
		PackageInstallation.CommunicationType = plugin_Installation_SCU;
		PackageInstallation.indexReadStart = 0;
		PackageInstallation.indexReadEnd = 0;
		PackageInstallation.TotalReadSize = 0;
		PackageInstallation.nextFrameRead = 0;
		PackageInstallation.PackageState.Done = false;
		PackageInstallation.readBytes = 0;
		PackageInstallation.CanReadBuffer = &BufferForPluginInstallation[0];
		plugin_new_packageFromTCU = true;
		init_install = true;
	}
	//printf("SCU_Installation_TCU_ReadRunnable\r\n");
	Can_Read_Package(&PackageInstallation);
	if (PackageInstallation.PackageState.Done == true) {
	printf("ready? %d %d\n\r", PackageInstallation.indexReadStart, PackageInstallation.indexReadEnd);
//		printf("infor: app data ");
//		for (int i = 0; i < PackageInstallation.TotalReadSize; i++) {
//			printf("%d ", BufferForPluginInstallation[i]);
//		}
//		printf("//total %d\r\n", PackageInstallation.indexReadEnd);
		PackageInstallation.CommunicationType = no_communication;
		//PackageInstallation.indexReadEnd = 0;
		PackageInstallation.TotalReadSize = 0;
		PackageInstallation.nextFrameRead = 0;
		PackageInstallation.PackageState.Done = false;
		PackageInstallation.readBytes = 0;
		PackageInstallation.CanReadBuffer = NULL;
		init_install = false;
	}
}

UInt8 Ack_TCU = 0xFE;
void SCU_Installation_TCU_AckRunnable(void) {

	printf("infor: ack tcu %d\r\n", ack_data);
	Rte_IWrite_SCU_Installation_TCU_AckRunnable_PirteSwcWriteAckDataToTCUPort6_acknowledgementTCU(ack_data);

}

CanPackage PackageWriteToVCU;
boolean init_writeToVCU = false;
void SCU_Communication_VCU_WriteRunnable(void){

	if( PackageWriteToVCU.TotalWriteSize < 7){
		Can_Send_Single_Frame(&PackageWriteToVCU);
		pluginCommunicationSCUtoVCU_Flag = false;
	} else{
		Can_Send_Package(&PackageWriteToVCU);
	}

	if(PackageWriteToVCU.PackageState.Done == true){
		PackageWriteToVCU.CanWriteBuffer = NULL;
		PackageWriteToVCU.CommunicationType = no_communication;
		PackageWriteToVCU.TotalWriteSize = 0;
		PackageWriteToVCU.indexWriteStart = 0;
		PackageWriteToVCU.indexWriteEnd = 0;
		PackageWriteToVCU.PackageState.Done = false;
		init_writeToVCU = false;
		pluginCommunicationSCUtoVCU_Flag = false;
	}
}

static boolean init_readFromVCU = false;
static CanPackage PackageReadFromVCU;
void SCU_Communication_VCU_ReadRunnable(void){

	if (init_readFromVCU == false) {
		PackageReadFromVCU.CommunicationType = plugin_communication_VCU_to_SCU;
		PackageReadFromVCU.TotalReadSize = 0;
		PackageReadFromVCU.indexReadEnd = 0;
		PackageReadFromVCU.PackageState.Done = false;
		PackageReadFromVCU.CanReadBuffer = &BufferForCommunication[0];
		init_readFromVCU = true;
	}

	Can_Read_Package(&PackageReadFromVCU);

	if (PackageReadFromVCU.PackageState.Done == true) {
		printf("infor: app data ");
		for (int i = 0; i < PackageReadFromVCU.TotalReadSize; i++) {
			printf("%d ", BufferForCommunication[i]);
		}
		printf("//total %d\r\n", PackageReadFromVCU.indexReadEnd);
		init_readFromVCU = false;
		//	  acknowledgementTCU_Flag = true;

		//Arndt: commented out
		//pluginCommunicationSCUtoVCU_Flag = true;
	}
}

static boolean init_readFromTCU = false;
static CanPackage PackageReadFromTCU;
void SCU_Communication_TCU_ReadRunnable(void){
	if(init_readFromTCU == false){
		PackageReadFromTCU.CommunicationType = plugin_communication_TCU_to_SCU;
		PackageReadFromTCU.TotalReadSize = 0;
		PackageReadFromTCU.indexReadEnd = 0;
		PackageReadFromTCU.PackageState.Done = false;
		PackageReadFromTCU.CanReadBuffer = &BufferForCommunication[0];
		init_readFromTCU = true;
	}

	Can_Read_Package(&PackageReadFromTCU);
	if(PackageReadFromTCU.PackageState.Done == true){
//	  printf("infor: app data ");
//	  for(int i = 0; i < PackageReadFromTCU.TotalReadSize; i++){
//		 printf("%d ", BufferForCommunication[i]);
//	  }
//	  printf("//total %d\r\n", PackageReadFromTCU.indexReadEnd);
	  init_readFromTCU = false;
//	  acknowledgementTCU_Flag = true;
//	  pluginCommunicationTCU_Flag = true;
	}
}

static CanPackage PackageWriteToTCU;
boolean init_WriteToTCU = false;
void SCU_Communication_TCU_WriteRunnable(void){

	if(init_WriteToTCU == false){
		PackageWriteToTCU.CanWriteBuffer = &ack_array[0];
		PackageWriteToTCU.CommunicationType = plugin_communication_SCU_to_TCU;
		PackageWriteToTCU.TotalWriteSize = writeTotalsize;
		PackageWriteToTCU.indexWriteStart = 0;
		PackageWriteToTCU.indexWriteEnd = 0;
		PackageWriteToTCU.PackageState.Done = false;
		init_WriteToTCU = true;
	}
	Can_Send_Package(&PackageWriteToTCU);
	if(PackageWriteToTCU.PackageState.Done == true){
		PackageWriteToTCU.CanWriteBuffer = NULL;
		PackageWriteToTCU.CommunicationType = no_communication;
		PackageWriteToTCU.TotalWriteSize = 0;
		PackageWriteToTCU.indexWriteStart = 0;
		PackageWriteToTCU.indexWriteEnd = 0;
		PackageWriteToTCU.PackageState.Done = false;
		init_WriteToTCU = false;
		pluginCommunicationTCU_Flag = false;
	}
}
uint64 IMU_acc = 0;
uint64 IMU_gyr = 0;
void Pirte_ReadIMUSensor_Runnable(void){
	UInt16* acc;
	UInt16* gyr;

	acc = Rte_IRead_Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuAccePort8_dataUInt16();
	gyr = Rte_IRead_Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuGyroPort7_dataUInt16();
#if IMU_PRINT
//    printf("infor: acc: %d %d %d\r\n", acc[0], acc[1], acc[2]);
//    printf("infor: Gyro: %d %d %d\r\n", gyr[0], gyr[1], gyr[2]);
#endif
    IMU_acc =  ((uint64)acc[0]<<32) + ((uint64)acc[1]<<16) + (uint64)acc[2];
    IMU_gyr =  ((uint64)gyr[0]<<32) + ((uint64)gyr[1]<<16) + (uint64)gyr[2];
	//IMUData.size = 12;
	//convert data (acceleration and geography) from UINT16 to UINT8, and prepare for data transfer
	//on the CAN-bus
//	IMUData.data[0] = (acc[0] >> 8) & 0xFF;
//	IMUData.data[1] = acc[0] & 0xFF;
//	IMUData.data[2] = (acc[1] >> 8) & 0xFF;
//	IMUData.data[3] = acc[1] & 0xFF;
//	IMUData.data[4] = (acc[2] >> 8) & 0xFF;
//	IMUData.data[5] = acc[2] & 0xFF;
//	//gyr
//	IMUData.data[6] = (gyr[0] >> 8) & 0xFF;
//	IMUData.data[7] = gyr[0] & 0xFF;
//	IMUData.data[8] = (gyr[1] >> 8) & 0xFF;
//	IMUData.data[9] = gyr[1] & 0xFF;
//	IMUData.data[10] = (gyr[2] >> 8) & 0xFF;
//	IMUData.data[11] = gyr[2] & 0xFF;
//	getIMUdata_Flag = true;
}


UInt16 UltraDistance = 0;
void Pirte_ReadUltrasonicSensor_Runnable(void){

	UltraDistance = Rte_IRead_Pirte_ReadUltrasonicSensor_Runnable_PirteSwcReadDataFromUltraSwcPort9_dataInt32();

#if DISTANCE_PRINT
	printf("infor: pirte dis %d\r\n", UltraDistance);
#endif

}


/**
 * this part is for java
 */
UInt16 readDistanceFromPort(void){

    return UltraDistance;

}

//void SCUwriteTOVCU(UInt16 data){
//	UInt8 DistanceH = 0;
//	UInt8 DistanceL = 0;
//    UInt8 UltraData[2];
//
//	UltraData[0] = 0;
//	UltraData[1] = 0;
//
//
//	DistanceH = (data >> 8) & 0xFF;
//	DistanceL = data & 0xFF;
//
//	UltraData[0] = DistanceH;
//	UltraData[1] = DistanceL;
//
//
//	Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication(UltraData);
//	pluginCommunicationSCUtoVCU_Flag = true;
//}

