/*
 * MOPED_signal.h
 *
 *  Created on: 7 okt 2014
 *      Author: sse
 */

#ifndef MOPED_SIGNAL_H_
#define MOPED_SIGNAL_H_

#include "Platform_Types.h"
#include "Rte_Type.h"

#define BUFFERSIZE              5000

//definition of CAN Frame
typedef enum {
	SINGLE_FRAME = 0,
	FIRST_FRAME = 1,
	CONSECUTIVE_FRAME = 2
} ISO15765FrameType;

//definiton of communication type
typedef enum {
	plugin_Installation_SCU = 0,
	acknowledgement_SCU = 1,
	plugin_communication_VCU_to_SCU = 2,
	plugin_communication_SCU_to_VCU = 3,
	plugin_communication_SCU_to_TCU = 4,
	plugin_communication_TCU_to_SCU = 5,
	no_communication,
} platformCommunicationType;

//definition of package state
typedef struct{
	boolean Start;
	boolean Pending;
	boolean Done;
}CanPackageState;

//definition of CAN package
typedef struct{
	platformCommunicationType CommunicationType;
	UInt32 TotalReadSize;
	UInt32 TotalWriteSize;
	UInt32 indexReadStart;
	UInt32 indexReadEnd;
	UInt32 readBytes;
	UInt32 indexWriteStart;
	UInt32 indexWriteEnd;
	uint16 nextFrameRead;
	sint8* CanReadBuffer;
	sint8* CanWriteBuffer;
	CanPackageState PackageState;
}CanPackage;

/**
 * definition of Ultasonic data package
 */
typedef struct{
	UInt8 size;
    UInt8 data[12];
}DataPackage;

DataPackage UltraData;
DataPackage IMUData;



//All the variable used for Java should be defined here
boolean Ack_Flag;
UInt8 ack_data;
sint8 BufferForPluginInstallaiton[BUFFERSIZE];
sint8 BufferForCommunication[BUFFERSIZE];
UInt8 ack_array[BUFFERSIZE];
UInt32 ack_array_size;
UInt32 ack_count;
UInt32 countRead;

//new
UInt32 writeTotalsize;

//extern signed char driverSetSpeed;   	  	 			//TCU ----> VCU   1 byte       Peroid = 10ms     Unit:%
//extern signed char driverSetSteering;   				//TCU ----> VCU   1 byte       Peroid = 10ms     Unit:%
//
//extern signed short vehiclePositionX;   			 	//TCU ----> VCU   2 bytes      Peroid = 500ms    Unit:cm
//extern signed short vehiclePositionY;  	 			 	//TCU ----> VCU   2 bytes      Peroid = 500ms    Unit:cm
//
//extern unsigned char vehicleOrientation;			    //TCU ----> VCU   1 byte       Peroid = 500ms    Unit:0...255
//extern unsigned char vehiclePositionQualityFactor;      //TCU ----> VCU   1 byte       Peroid = 500ms    Unit:0...100%
//
//extern char plugInInstallationVCU[8];                   //TCU ----> VCU   0..8 bytes   Aperiodic
//extern char plugInInstallationSCU[8];                   //TCU ----> VCU   0..8 bytes   Aperiodic
//
//extern boolean acknowledgementVCU;                      //VCU ----> TCU   1byte        Aperiodic
//extern boolean acknowledgementSCU;                      //SCU ----> TCU   1byte        Aperiodic
//
//extern char plugInCommunicationVCUtoSCU[8];             //VCU ----> SCU   0..8 bytes   Aperiodic
//extern char plugInCommunicationSCUtoVCU[8];             //SCU ----> VCU   0..8 bytes   Aperiodic
//
//extern char plugInCommunicationVCUtoTCU[8];             //VCU ----> TCU   0..8 bytes   Aperiodic
//extern char plugInCommunicationTCUtoVCU[8];             //TCU ----> VCU   0..8 bytes   Aperiodic
//
//extern char plugInCommunicationSCUtoTCU[8];             //SCU ----> TCU   0..8 bytes   Aperiodic
//extern char plugInCommunicationTCUtoSCU[8];             //TCU ----> SCU   0..8 bytes   Aperiodic


#endif /* MOPED_SIGNAL_H_ */
