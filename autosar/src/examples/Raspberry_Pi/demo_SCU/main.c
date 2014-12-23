/*
 * SCU_main.c
 *
 *  Created on: 28 okt 2014
 *      Author: sse
 */


#include <stdint.h>
#include <string.h>
#include <stdio.h>
#include "Os.h"
#include "Mcu.h"
#include "arc.h"
#include "EcuM.h"

#include "bcm2835.h"
#include "Uart.h"

#include "Can.h"
#include "Can.h"
#include "CanIf.h"
#include "Com.h"
#include "CanTp.h"

#include "MOPED_DEBUG.h"

#define TEST1 TRUE  // scu sending, vcu and tcu reading
#define TEST2 TRUE  // vcu sending, scu and tcu reading'

extern boolean ComWrite;

extern int runSquawk(void);

void SquawkTask(void){
	 printf("Squawk task\r\n");
#if RUN_SQUAWK
	 runSquawk();
#endif
	 TerminateTask();
}

void StartupTask( void ) {
    pi_printf("infor: start up\r\n");

    EcuM_StartupTwo();

	// Startup CanIf due to ComM is missing in this example
	CanIf_SetControllerMode(CANIF_CanIfCtrlCfg, CANIF_CS_STARTED);
	CanIf_SetPduMode(CANIF_CanIfCtrlCfg, CANIF_SET_ONLINE);

	 /** Setup Com stack with necessary parameters**/
	Com_IpduGroupVector groupVector;
	//Start the IPDU group
	Com_ClearIpduGroupVector(groupVector);

	Com_SetIpduGroup(groupVector, COM_PDU_GROUP_ID_TXCOMIPDUGROUP, TRUE);
	Com_SetIpduGroup(groupVector, COM_PDU_GROUP_ID_RXCOMIPDUGROUP, TRUE);
	Com_IpduGroupControl(groupVector, TRUE);
//	pi_printf("infor: done\r\n");
	TerminateTask();

}

void CanFunctionTask(void) {

    printf("CanFunctionTask\r\n");

	for(;;){

		WaitEvent(EVENT_MASK_CanFunctionEvent);
		ClearEvent(EVENT_MASK_CanFunctionEvent);
//		printf("get can event\r\n");
#if !CAN_INTERRUPT
		Can_MainFunction_Read();
#endif
		Com_MainFunctionRx();

        if(ComWrite == true){
//        	printf("comTx\r\n");
        	Com_MainFunctionTx();
        	ComWrite = false;
        }

	}
}

void OsIdle( void ) {
	for(;;) {}
}

/*void SensorTask(void) {
    EventMaskType Event;
    for(;;){
        WaitEvent(EVENT_MASK_SensorEvent);
        ClearEvent (EVENT_MASK_SensorEvent);

       	Rte_UltraSWCProto_UltrasonicRunnable();

    }
}*/




