/*
 * main.c
 *
 *  Created on: 22 sep 2014
 *      Author: sse
 */

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include "Os.h"
#include "Mcu.h"
#include "arc.h"
#include "EcuM.h"

#include "bcm2835.h"
#include "Uart.h"
#include "MOPED_DEBUG.h"

#include "Can.h"
#include "Can.h"
#include "CanIf.h"
#include "Com.h"
#include "CanTp.h"

extern boolean ComWrite;

extern int runSquawk(void);

void SquawkTask(void){
	printf("Squawk task\r\n");
	for (;;) {
		WaitEvent(EVENT_MASK_SquawkEvent);
		ClearEvent(EVENT_MASK_SquawkEvent);
#if RUN_SQUAWK
		runSquawk();
#endif
		printf("Squawk task loop\r\n");
	}
}

struct ecu_config {
  char *mac;
  char *conf;
};

struct ecu_config configs[] = {
  // VCU car 1
  {"b827eb3510df", "servo -1"},
  // VCU car 2
  {"b827eb3aebfd", "servo 1"},
};

int vcu_servo_direction = 1;

static void parse_config(void) {
  int i, j;
  int matched = 0;

  for (i = 0; i < sizeof(configs)/sizeof(configs[0]); i++) {
    //printf("checking %s\r\n", configs[i].mac);
    if (strcmp(bcm2835_mac_address, configs[i].mac) == 0) {
      matched = 1;
      printf("config: %s\r\n", configs[i].conf);
      vcu_servo_direction = atoi(&configs[i].conf[6]);
      if (vcu_servo_direction != 1 && vcu_servo_direction != -1) {
	printf("invalid servo direction %d\r\n",
	       vcu_servo_direction);
	vcu_servo_direction = 1;
      }
      break;
    }
  }
  if (!matched) {
    printf("No configuration info was found for this ECU\r\n");
  }
}


void StartupTask( void ) {
    pi_printf("infor: start up\r\n");

    bcm2835_read_mac_address();

    parse_config();

    EcuM_StartupTwo();

	// Startup CanIf due to ComM is missing in this example
	CanIf_SetControllerMode(CANIF_CanIfCtrlCfg, CANIF_CS_STARTED);
	CanIf_SetPduMode(CANIF_CanIfCtrlCfg, CANIF_SET_ONLINE);

	 /** Setup Com stack with necessary parameters**/
	Com_IpduGroupVector groupVector;
	Com_ClearIpduGroupVector(groupVector);
	//Start the IPDU group
	Com_SetIpduGroup(groupVector, COM_PDU_GROUP_ID_TXIPDUGROUP, TRUE);
	Com_SetIpduGroup(groupVector, COM_PDU_GROUP_ID_RXIPDUGROUP, TRUE);
	Com_IpduGroupControl(groupVector, TRUE);

	TerminateTask();

}

void CanFunctionTask(void) {

    printf("CanFunctionTask\r\n");

	for(;;){

		WaitEvent(EVENT_MASK_CanFunctionEvent);
		ClearEvent(EVENT_MASK_CanFunctionEvent);
#if !CAN_INTERRUPT
		Can_MainFunction_Read();
#endif
		Com_MainFunctionRx();

		if (ComWrite == true) {
		  Com_MainFunctionTx();
		  ComWrite = false;
		}
	}
}

void OsIdle( void ) {
	for(;;) {}
}
