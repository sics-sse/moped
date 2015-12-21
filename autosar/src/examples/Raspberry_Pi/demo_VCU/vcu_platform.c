/*
 * Pirte.c
 *
 *  Created on: 7 sep 2014
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

boolean pirte_write_servo = false;
boolean pirte_write_speed = false;

boolean acknowledgementTCU_Flag = false;
boolean pluginCommunicationTCU_Flag = false;
boolean acknowledgementVCU_ComWrite = false;
boolean singleframe_Flag = false;
boolean firstframe_Flag = false;
boolean packageFirstFrame_Flag = false;
boolean pluginCommunicationVCUtoSCU_Flag = false;

UInt8 sequnceNumberWrite = 0;

UInt32 readTotalSize;
UInt32 countWrite;
UInt8 WriteframeType = FIRST_FRAME;

/**
 *
 * @param tempBuffer
 * @param CANPackage
 */
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

void Can_Read_Raw_Frame(uint8* tempBuffer, CanPackage * CANPackage){

	readTotalSize = 8;
//    printf("infor (single): size %d, %d, %d, %d, %d\r\n",readTotalSize, tempBuffer[0], tempBuffer[1], tempBuffer[2], tempBuffer[3]);
    CANPackage->TotalReadSize  = readTotalSize;
//    printf("infor: size %d \r\n",CANPackage->TotalReadSize);
    CANPackage->nextFrameRead = 0;
    CANPackage->indexReadStart = 0;
    CANPackage->indexReadEnd = 0;
    //store the data
    for(int i = 0; i < CANPackage->TotalReadSize; i++){
    	CANPackage->CanReadBuffer[CANPackage->indexReadEnd] = tempBuffer[i];
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
//    printf("infor (first): size %d, %d, %d, %d, %d\r\n",readTotalSize, tempBuffer[0], tempBuffer[1], tempBuffer[2], tempBuffer[3]);
	//printf("Can_Read_First_Frame, old size %d\r\n", CANPackage->TotalReadSize);
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
//    	printf("Index: %d\r\n",CANPackage->indexReadEnd);
    }
}

/**
 *
 * @param tempBuffer
 * @param CANPackage
 */
void Can_Read_Consecutive_Frame(uint8* tempBuffer, CanPackage * CANPackage){
//	printf("infor: consecutive frame readBytes = %d\r\n", CANPackage->readBytes);
	uint16 sequenceNumber = tempBuffer[0] & SEQUENCE_NUMBER;

	if (CANPackage->TotalReadSize == 0)
	  return;

	//printf("Can_Read_Consecutive_Frame seq %d\r\n", sequenceNumber);
	//printf(" seq %d %d\r\n", sequenceNumber, CANPackage->indexReadEnd);

	if(sequenceNumber == CANPackage->nextFrameRead){
		for (int i = 0; i < CONSECUTIVE_FRAME_SIZE; i++) {
		  if (CANPackage->CommunicationType == plugin_communication_SCU_to_VCU) {
		    //printf("stuff: %d %d -> %d\r\n", CANPackage->indexReadEnd, CANPackage->CanReadBuffer[CANPackage->indexReadEnd], tempBuffer[i+CF_PCI_BYTE]);
		  }
			CANPackage->CanReadBuffer[CANPackage->indexReadEnd] = tempBuffer[i+CF_PCI_BYTE];
			CANPackage->indexReadEnd++;
			CANPackage->readBytes++;
			//			printf("Index: %d (%d)\r\n",CANPackage->indexReadEnd,
			//			       CANPackage->CanReadBuffer[CANPackage->indexReadEnd-1]);
			if(CANPackage->indexReadEnd >= BUFFERSIZE){
				CANPackage->indexReadEnd = 0;
			}
		  if (CANPackage->CommunicationType == plugin_communication_SCU_to_VCU) {
		    //printf("stuff sizes %d %d\r\n", CANPackage->TotalReadSize, (CANPackage->readBytes));
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
		  if(CANPackage->nextFrameRead != NO_FRAME) {
		    CANPackage->nextFrameRead = 0;
		  }
		}else{
			CANPackage->nextFrameRead++;
		}
	}else{
	  printf("consec: not same sequence number: %d %d %d\r\n", sequenceNumber, CANPackage->nextFrameRead, CANPackage->indexReadEnd);
		//nothing
	}


}

/**
 *
 * @param CANPackage
 */
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
	printf("infor: send single frame size: %d, data: ", data[0]);
	for (i = 0; i < length; i++) {
		data[i + SF_PCI_BYTE] = CANPackage->CanWriteBuffer[i];
		printf("%d ", data[i + SF_PCI_BYTE]);
	}
	printf("\r\n");
	//send single frame
	switch(CommunicationType){
		case plugin_Installation_VCU:
		  printf("single frame installation VCU\r\n");
			//
		break;
		case plugin_communication_VCU_to_SCU:
			//

		  printf("VCU->SCU\r\n");
		Rte_IWrite_Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(data);
		break;
		case plugin_communication_VCU_to_TCU:
		  printf("single frame VCU to TCU\r\n");
			//
		break;

		default:
		//nothing
	  	break;
	}

}

/**
 *
 * @param CANPackage
 */
void Can_Send_First_Frame(CanPackage* CANPackage) {
	UInt8 length1 = 0;
	UInt8 length2 = 0;
	uint8 length3 = 0;
	UInt8 length4 = 0;
	UInt8 i;
	UInt8 data[8];

	sequnceNumberWrite = 0;

	UInt32 appsize = CANPackage->TotalWriteSize;
	UInt32 CommunicationType = CANPackage->CommunicationType;
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
	//printf("infor: send first frame %d, %d, %d, %d ", data[0], data[1], data[2], data[3]);
	for (i = 0; i < FIRST_FRAME_SIZE; i++) {
		data[i + FF_PCI_BYTE] = CANPackage->CanWriteBuffer[CANPackage->indexWriteEnd];
		//		printf("%d ", data[i + FF_PCI_BYTE]);
		CANPackage->indexWriteEnd++;
//		printf("infor: writeIndex %d\r\n",CANPackage->indexWriteEnd);
	}
	//	printf("\r\n");
	//send first frame
	switch (CommunicationType) {
	case acknowledgement_VCU:
	  printf("first frame acknowledgementVCU\r\n");
		//
		break;

	case plugin_communication_VCU_to_SCU:
		Rte_IWrite_Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(data);
		break;

	case plugin_communication_VCU_to_TCU:
	  //printf("first frame VCU to TCU\r\n");
		Rte_IWrite_Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU(data);
		break;

	default:
		//nothing
		break;
	}
}


/**
 *
 * @param CANPackage
 */
void Can_Send_Consecutive_Frame(CanPackage* CANPackage) {
	UInt8 j;
	UInt8 data[8];
	UInt8 CommunicationType = CANPackage->CommunicationType;
	//printf("infor: send consecutive frame\r\n");
		//send consecutive frame, construct the first frame
	data[0] = (uint8) (CONSECUTIVE_FRAME << 4) + (sequnceNumberWrite & 0xF);
	//printf("%d ", data[0]);
	for (j = 0; j < CONSECUTIVE_FRAME_SIZE; j++) {
		data[j + CF_PCI_BYTE] = CANPackage->CanWriteBuffer[CANPackage->indexWriteEnd];
		CANPackage->indexWriteEnd++;
//		printf("infor: writeIndex %d\r\n",CANPackage->indexWriteEnd);
//		printf("%d ", data[j + CF_PCI_BYTE]);
	}
	//	printf("\r\n");

	if (sequnceNumberWrite >= 15) {
		sequnceNumberWrite = 0;
	} else {
		sequnceNumberWrite++;
	}
	//send consecutive frame
	switch (CommunicationType) {
	case acknowledgement_VCU:
	  printf("consec frame acknowledgement VCU\r\n");
		//
		break;

	case plugin_communication_VCU_to_SCU:
		Rte_IWrite_Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(data);
		break;

	case plugin_communication_VCU_to_TCU:
		Rte_IWrite_Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU(data);
		break;

	default:
		//nothing
		break;
	}

}

/**
 *
 * @param CANPackage
 */
void Can_Send_Package(CanPackage* CANPackage){
    UInt32 pakcageSize = CANPackage->TotalWriteSize;

    // printf("Can_Send_Package %d %d\r\n", pakcageSize, WriteframeType);

    if (pakcageSize == 0) {
      return;
    }

	if (pakcageSize < 8) {
		Can_Send_Single_Frame(CANPackage);
		singleframe_Flag = true;
	} else {
		switch (WriteframeType)
		{
			case FIRST_FRAME:
				//send first frame
				Can_Send_First_Frame(CANPackage);
				if((pakcageSize - FIRST_FRAME_SIZE)% CONSECUTIVE_FRAME_SIZE == 0){
					countWrite = (pakcageSize - FIRST_FRAME_SIZE)/ CONSECUTIVE_FRAME_SIZE;
				}else{
					countWrite = (pakcageSize - FIRST_FRAME_SIZE)/ CONSECUTIVE_FRAME_SIZE + 1;
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

boolean plugin_new_packageFromTCU = false;

/**
 *
 * @param CANPackage
 */
void Can_Read_Package(CanPackage* CANPackage){

  static long cnt = 0;

    UInt8 frameType = 0;
    UInt8 tempBuffer[8];
    UInt8* pluginData = NULL;

    UInt8 CommunicationType = CANPackage->CommunicationType;

    cnt++;

#if 0
    printf("read package %ld %d, start = %d, end = %d\r\n",
	   cnt, CommunicationType,
	   CANPackage->indexReadStart, CANPackage->indexReadEnd);
#endif

    switch(CommunicationType)
    {
    	case plugin_Installation_VCU:
    		pluginData = Rte_IRead_Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU();
        	break;
    	case plugin_communication_SCU_to_VCU:
    		pluginData = Rte_IRead_Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU();
    		break;
    	case plugin_communication_TCU_to_VCU:
    		pluginData = Rte_IRead_Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU();
    	    break;
		case position_communication_TCU_to_VCU:
    		pluginData = Rte_IRead_Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_PositionData();
    	    break;
		default:
		  printf("CommunicationType %d\r\n", CommunicationType);
    	    break;
    }
#if 0
    if (CANPackage->CommunicationType == plugin_communication_SCU_to_VCU) {
      printf("vcu copy 1: ");
      for (int i = 0; i < 8; i++)
	printf(" %d", pluginData[i]);
      printf("\r\n");
    }
#endif
    memcpy(tempBuffer, pluginData, 8);
#if 0
    if (CANPackage->CommunicationType == plugin_communication_SCU_to_VCU) {
      printf("vcu copy 2: ");
      for (int i = 0; i < 8; i++)
	printf(" %d", tempBuffer[i]);
      printf("\r\n");
    }
#endif
    //get type of frame
    if (CommunicationType == position_communication_TCU_to_VCU) {
      frameType = 9;
    } else {
      frameType = (tempBuffer[0] & FRAME_TYPE) >> 4;
    }
    //printf("infor: frameType %d\r\n",frameType);
    switch(frameType){
    case 9:
    		Can_Read_Raw_Frame(tempBuffer, CANPackage);
      break;
    	case SINGLE_FRAME:
    		Can_Read_Single_Frame(tempBuffer, CANPackage);
    	break;
    	case FIRST_FRAME:
	  //printf("first frame: %p %p\r\n", pluginData, tempBuffer);
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

#if 0
    printf("end of read package %ld %d, start = %d, end = %d\r\n",
	   cnt,
	   CommunicationType,
	   CANPackage->indexReadStart, CANPackage->indexReadEnd);
#endif
}

static CanPackage *mm(CanPackage *x)
{
  return x;
}

static boolean plugin_install = false; //temp

CanPackage PackageReadFromSCU;
UInt32 *VCUSCUtotp;

CanPackage PackageInstallation;
void Pirte_ReadInstallationDataFromTCU_Runnable(void){
	//first time initialization
	if (plugin_install == false) {
	  VCUSCUtotp = &PackageReadFromSCU.TotalReadSize;

		PackageInstallation.CommunicationType = plugin_Installation_VCU;
		PackageInstallation.indexReadStart = 0;
		PackageInstallation.indexReadEnd = 0;
		PackageInstallation.TotalReadSize = 0;
		PackageInstallation.nextFrameRead = 0;
		PackageInstallation.PackageState.Done = false;
		PackageInstallation.readBytes = 0;
		PackageInstallation.CanReadBuffer = &BufferForPluginInstallaiton[0];
		plugin_new_packageFromTCU = true;
		plugin_install = true;
	}

	Can_Read_Package(&PackageInstallation);

	if (PackageInstallation.PackageState.Done == true
	    // Arndt
	    //	    && mm(&PackageInstallation)->indexReadStart == mm(&PackageInstallation)->indexReadEnd
)
{
	printf("ready? %d %d\n\r", PackageInstallation.indexReadStart, PackageInstallation.indexReadEnd);
//		printf("infor: app data ");
//		for (int i = 0; i < PackageInstallation.TotalReadSize; i++) {
//			printf("%d ", BufferForPluginInstallaiton[i]);
//		}
//		printf("//total %d\r\n", PackageInstallation.indexReadEnd);
		PackageInstallation.CommunicationType = no_communication;
//		PackageInstallation.indexReadEnd = 0;
		PackageInstallation.TotalReadSize = 0;
		PackageInstallation.nextFrameRead = 0;
		PackageInstallation.PackageState.Done = false;
		PackageInstallation.readBytes = 0;
		PackageInstallation.CanReadBuffer = NULL;
		plugin_install = false;
//		acknowledgementVCU_Flag = true;// temp test
	}

}


void Pirte_WriteAcknowledgementDataToTCU_Runnable(void){
//	ack_data = 0xFE;
	printf("infor: ack %d\r\n", ack_data);
	Rte_IWrite_Pirte_WriteAcknowledgementDataToTCU_Runnable_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU(ack_data);
}

static boolean init_readFromTCU = false;
CanPackage PackageReadFromTCU;
void Pirte_ReadCommunicationDataFromTCU_Runnable(void){

	if(init_readFromTCU == false){
		PackageReadFromTCU.CommunicationType = plugin_communication_TCU_to_VCU;
		PackageReadFromTCU.indexReadStart = 0;
		PackageReadFromTCU.TotalReadSize = 0;
		PackageReadFromTCU.indexReadEnd = 0;
		PackageReadFromTCU.nextFrameRead = 0;
		PackageReadFromTCU.PackageState.Done = false;
		PackageReadFromTCU.CanReadBuffer = &BufferForCommunication[0];
		init_readFromTCU = true;
	}

	Can_Read_Package(&PackageReadFromTCU);

	if(PackageReadFromTCU.PackageState.Done == true){
//	  printf("infor: TCU app data ");
//	  for(int i = 0; i < PackageReadFromTCU.TotalReadSize; i++){
//		 printf("%d ", BufferForCommunication[i]);
//	  }
//	  printf("//total %d\r\n", PackageReadFromTCU.indexReadEnd);
//	  PackageReadFromTCU.TotalReadSize = 0;
	  PackageReadFromTCU.indexReadEnd = 0;
	  PackageReadFromTCU.nextFrameRead = 0;
	  PackageReadFromTCU.PackageState.Done = false;
	  PackageReadFromTCU.CommunicationType = no_communication;
	  PackageReadFromTCU.readBytes = 0;
	  init_readFromTCU = false;
//	  acknowledgementTCU_Flag = true;
//	  pluginCommunicationTCU_Flag = true;
	}
}

CanPackage PackageWriteToTCU;
boolean init_WriteToTCU = false;
void Pirte_WriteCommunicationDataFromTCU_Runnable(void) {

	if (init_WriteToTCU == false) {
		PackageWriteToTCU.CanWriteBuffer = &ack_array[0];
		PackageWriteToTCU.CommunicationType = plugin_communication_VCU_to_TCU;
		PackageWriteToTCU.TotalWriteSize = writeTotalsize;
		PackageWriteToTCU.indexWriteStart = 0;
		PackageWriteToTCU.indexWriteEnd = 0;
		PackageWriteToTCU.PackageState.Done = false;
		init_WriteToTCU = true;
	}

	Can_Send_Package(&PackageWriteToTCU);

	if (PackageWriteToTCU.PackageState.Done == true) {
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

static boolean init_readFromSCU = false;
CanPackage PackageReadFromSCU;
boolean plugin_new_packagefromSCU = false;

int write_ok = 7;

UInt32 *VCUSCUtotp;

void Pirte_ReadDataFromSCU_Runnable(void){
  if (PackageReadFromSCU.TotalReadSize == 0) {
    //printf("write ok <- 6\r\n");
    write_ok = 6;
  }

	if (!write_ok) {
	  //printf("write ok == 0\r\n");
	  return;
	}

	if (init_readFromSCU == false) {

	  VCUSCUtotp = &PackageReadFromSCU.TotalReadSize;
		PackageReadFromSCU.CommunicationType = plugin_communication_SCU_to_VCU;
		PackageReadFromSCU.indexReadStart = 0;
		PackageReadFromSCU.indexReadEnd = 0;
		PackageReadFromSCU.TotalReadSize = 0;
		PackageReadFromSCU.nextFrameRead = 0;
		PackageReadFromSCU.PackageState.Done = false;
		PackageReadFromSCU.readBytes = 0;
		PackageReadFromSCU.CanReadBuffer = &BufferForCommunicationSCUtoVCU[0];
		plugin_new_packagefromSCU = true;
		init_readFromSCU = true;
	}

	Can_Read_Package(&PackageReadFromSCU);

	//printf("PackageReadFromSCU.PackageState.Done = %d\r\n",
	//     PackageReadFromSCU.PackageState.Done);
	if (PackageReadFromSCU.PackageState.Done == true) {
//		printf("infor: SCU app data ");
//		for (int i = 0; i < PackageReadFromSCU.TotalReadSize; i++) {
//			printf("%d ", BufferForCommunicationSCUtoVCU[i]);
//		}
//		printf("//total %d\r\n", PackageReadFromSCU.indexReadEnd);
		//TODO: Why is this done here???
	  //PackageReadFromSCU.TotalReadSize = 0;
		PackageReadFromSCU.nextFrameRead = 0;
		PackageReadFromSCU.PackageState.Done = false;
		PackageReadFromSCU.CommunicationType = no_communication;
		PackageReadFromSCU.CanReadBuffer = NULL;
		PackageReadFromSCU.readBytes  = 0;
		init_readFromSCU = false;

		write_ok = 0;
		//printf("write ok <- 0\r\n");

	}

}

CanPackage PackageWriteToSCU;
boolean init_WriteToSCU = false;
void Pirte_WriteDataToSCU_Runnable(void){
	if (init_WriteToSCU == false) {
		PackageWriteToSCU.CanWriteBuffer = &ack_array[0];
		PackageWriteToSCU.CommunicationType = plugin_communication_VCU_to_SCU;
		//PackageWriteToSCU.TotalWriteSize = 837;
		PackageWriteToSCU.TotalWriteSize = writeTotalsize;
		PackageWriteToSCU.indexWriteStart = 0;
		PackageWriteToSCU.indexWriteEnd = 0;
		PackageWriteToSCU.PackageState.Done = false;
		init_WriteToSCU = true;
	}

	Can_Send_Package(&PackageWriteToSCU);

	if (PackageWriteToSCU.PackageState.Done == true) {
		PackageWriteToSCU.CanWriteBuffer = NULL;
		PackageWriteToSCU.CommunicationType = no_communication;
		PackageWriteToSCU.TotalWriteSize = 0;
		PackageWriteToSCU.indexWriteStart = 0;
		PackageWriteToSCU.indexWriteEnd = 0;
		PackageWriteToSCU.PackageState.Done = false;
		init_WriteToSCU = false;
		pluginCommunicationVCUtoSCU_Flag = false;
	}

}

UInt32 Select_Speed = 0;
void Pirte_WriteDataToMotor_Runnable(void){

//	Rte_IWrite_Pirte_WriteDataToMotor_Runnable_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32(Select_Speed);
}

UInt32 Select_Servo = 0;
void Pirte_WriteDataToServo_Runnable(void){

//	Rte_IWrite_Pirte_WriteDataToServo_Runnable_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32(Select_Servo);
}

void Pirte_ReadDataFromAdc_Runnable(void){
	UInt32 Adc_Data = 0;
	Adc_Data = Rte_IRead_Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32();
#if ADC_PRINT_DEBUG
	printf("infor: Pirte adc:%d\r\n", Adc_Data);
#endif
}

void Pirte_ReadDataFromFrontWheel_Runnable(void){

    UInt32 frontwheelSpeed = 0;
    frontwheelSpeed = Rte_IRead_Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32();

#if WHEEL_PRINT_DEBUG
    if(frontwheelSpeed > 0)
    printf("infor: Pirte front_Speed:%d\r\n", frontwheelSpeed);
#endif
}

void Pirte_ReadDataFromRearWheel_Runnable(void){

	UInt32 rearwheelSpeed = 0;
	rearwheelSpeed = Rte_IRead_Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32();

#if WHEEL_PRINT_DEBUG
	if(rearwheelSpeed > 0)
	printf("infor: Pirte rear_Speed:%d\r\n", rearwheelSpeed);
#endif
}



static boolean init_position = false; //temp
static CanPackage vehiclePosition;
uint8 vehiclePositionData[20];
signed short vehiclePositionX; // (2 bytes), measured in cm; use 32767 if no marker is found
signed short vehiclePositionY; // (2 bytes), measured in cm; use 32767 if no marker is found
unsigned char vehicleOrientation; // (1 byte), values in range 0..255; use 0 if no marker is found
unsigned char vehiclePositionQualityFactor;	// (1 byte), values in range 0..100%; use 0 if no marker is found
unsigned short vehiclePositionAge;			// (2 bytes), measured in ms

void Pirte_ReadPositionData_Runnable(void){

   //first time initialization
   	if (init_position == false) {
//        printf("infor: read position data\r\n");
   		vehiclePosition.CommunicationType = position_communication_TCU_to_VCU;
   		vehiclePosition.indexReadStart = 0;
   		vehiclePosition.indexReadEnd = 0;
   		vehiclePosition.TotalReadSize = 0;
   		vehiclePosition.nextFrameRead = 0;
   		vehiclePosition.PackageState.Done = false;
   		vehiclePosition.readBytes = 0;
   		vehiclePosition.CanReadBuffer = &vehiclePositionData[0];
   		init_position = true;
   	}

   	Can_Read_Package(&vehiclePosition);

   	if (vehiclePosition.PackageState.Done == true) {
   		vehiclePositionX = (((uint16) vehiclePositionData[1]) << 8) + vehiclePositionData[0];
   		vehiclePositionY = (((uint16) vehiclePositionData[3]) << 8) + vehiclePositionData[2];
   		vehicleOrientation = vehiclePositionData[4];
   		vehiclePositionQualityFactor = vehiclePositionData[5];
   		vehiclePositionAge = (((uint16)vehiclePositionData[7]) << 8) + vehiclePositionData[6];
#if POSITION_PRINT
   		printf("infor: position data: %d %d %d %d %d\r\n",
   				(int) vehiclePositionX,
   				(int) vehiclePositionY,
   				(int) vehicleOrientation,
   				(int) vehiclePositionQualityFactor,
   				(int) vehiclePositionAge);
#endif

   		vehiclePosition.CommunicationType = no_communication;
   		vehiclePosition.TotalReadSize = 0;
   		vehiclePosition.nextFrameRead = 0;
   		vehiclePosition.PackageState.Done = false;
   		vehiclePosition.readBytes = 0;
   		vehiclePosition.CanReadBuffer = NULL;
   		vehiclePosition.indexReadStart = 0;
   		vehiclePosition.indexReadEnd = 0;
   		init_position = false;
   	}

}

UInt8 Pin = 0;
UInt8 Level = 0;

void Pirte_WriteLedData_Runnable(void){
    UInt16 Value = 0;
    Value = ((UInt16)Pin << 8) + Level;
	Rte_IWrite_Pirte_WriteLedData_Runnable_PirteSwcWriteLedPort13_dataInt32(Value);
}

//TODO: Primary suspect
UInt8 Speed_Value;
UInt8 Servo_Value;
void Pirte_ReadSpeedSteerData_Runnable(void){
	UInt8* SpeedSteer = Rte_IRead_Pirte_ReadSpeedSteerData_Runnable_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer();
	Speed_Value = SpeedSteer[0];
	Servo_Value = SpeedSteer[1];
}

/**
 *
 * this part is for java
 */
UInt32 getFrontWheelSpeed(void) {
	UInt32 frontwheelSpeed = 0;
	frontwheelSpeed = Rte_IRead_Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32();
//	printf("infor: Pirte front_Speed:%d\r\n", frontwheelSpeed);
	return frontwheelSpeed;
}

UInt32 getRearWheelSpeed(void) {
	UInt32 rearwheelSpeed = 0;
	rearwheelSpeed = Rte_IRead_Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32();
//	printf("infor: Pirte rear_Speed:%d\r\n", rearwheelSpeed);
	return rearwheelSpeed;
}


void PirteWriteSpeedPwmData(int speed) {

	pirte_write_speed = true;
	Rte_IWrite_Pirte_WriteDataToMotor_Runnable_PirteSwcWriteDataToMotorSwcPort3_dataInt32(speed);
	Rte_PirteSWCProto_Pirte_WriteDataToMotor_Runnable();

}

void PirteWriteSteerPwmData(int servo) {

	pirte_write_servo = true;

	Rte_IWrite_Pirte_WriteDataToServo_Runnable_PirteSwcWriteDataToServoSwcPort4_dataInt32(servo);
	Rte_PirteSWCProto_Pirte_WriteDataToServo_Runnable();

}


int readAdcDataFromPort(void) {
	uint32 volt = Rte_IRead_Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32();
//	float val = volt * 5 / 1024.0 / 0.32;
//	printf("[Pirte - %s]", val);
//	static char s[10];
//	ftoa(val, s);
//	ftoa(233.007, s);
//	printf("votage: %sV\r\n", s);
//	int v = 233;
	return volt;
}

uint64 readPositionFromPort(void){

	uint64 positionData = 0;

	positionData =    (((uint64)vehiclePositionX) << 48) + (((uint64)vehiclePositionY) << 32)
			        + (((uint64)vehicleOrientation) << 24) + (((uint64)vehiclePositionQualityFactor) << 16) + vehiclePositionAge;
	printf("infor: %lu\r\n", positionData);
//	printf("bit %lu, %lu, %lu, %lu, %lu\r\n",(((uint64)vehiclePositionX) << 48),
// 			(((uint64)vehiclePositionY) << 32),
//            (((uint64)vehicleOrientation) << 24),
//            (((uint64)vehiclePositionQualityFactor) << 16) ,
//            vehiclePositionAge);

	return positionData;
}
