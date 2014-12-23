#include <stdlib.h>

#include "mcp2515.h"
#include "Can.h"
#include "CanIf_Cbk.h"

Can_FilterMaskType Can_FilterMask_CanFilterMask = 0x0;

const Can_HardwareObjectType CanHardwareObjectConfig_CanController[] = {
	{
		.CanObjectId	=	CAN_RX,
		.CanHandleType	=	CAN_ARC_HANDLE_TYPE_BASIC,
		.CanIdType		=	CAN_ID_TYPE_STANDARD,
		.CanObjectType	=	CAN_OBJECT_TYPE_RECEIVE,
		.CanFilterMaskRef =	&Can_FilterMask_CanFilterMask,
		
		.Can_Arc_EOL	= 	0
		
	},
	{
		.CanObjectId	=	CAN_TX,
		.CanHandleType	=	CAN_ARC_HANDLE_TYPE_BASIC,
		.CanIdType		=	CAN_ID_TYPE_STANDARD,
		.CanObjectType	=	CAN_OBJECT_TYPE_TRANSMIT,
		.CanFilterMaskRef =	0, // Not applicable for Transmit object
		.Can_Arc_EOL	= 	1
	},
};

const Can_ControllerConfigType CanControllerConfigData[] =
{
	{
		.CanControllerActivation	=	TRUE,
		.CanControllerSupportedBaudRates = {MCP2515_25KBPS,
											MCP2515_250KBPS,
											MCP2515_500KBPS},
		.CanControllerBaudRateId	=	2,
		.CanControllerId	 		=	0,
		.CanECUId					= 	VCU_CANID,
		.CanRxProcessing 			=	CAN_ARC_PROCESS_TYPE_INTERRUPT,
		.CanTxProcessing			=	CAN_ARC_PROCESS_TYPE_POLLING,
		.CanBusOffProcessing 		=	CAN_ARC_PROCESS_TYPE_POLLING,
		.CanWakeupProcessing		=	CAN_ARC_PROCESS_TYPE_POLLING,
		.CanCpuClockRef 			=	0,
 		.Can_Arc_Hoh 				=	&CanHardwareObjectConfig_CanController[0],
    	.Can_Arc_Loopback 			=	FALSE,
    	.Can_Arc_Fifo 				= 	0, 
    	.CanMsgIdTable 				= {
    			{
    					.msgId 		= 257,			// driverSetSpeedSteering_ID
						.taskId 	= TASK_ID_ActuatorTask,
						.eventMask 	= (EVENT_MASK_SpeedEvent | EVENT_MASK_ServoEvent)
				},
				{
						.msgId 		= 1025,			// position_ID
						.taskId 	= TASK_ID_RteTask,
						.eventMask 	= EVENT_MASK_positionDataEvent
				},
				{
						.msgId 		= 1121,			// pluginCommunicationSCUtoVCU_ID
						.taskId 	= TASK_ID_RteTask,
						.eventMask 	= EVENT_MASK_pluginCommunicationSCUEvent
				},
				{
						.msgId 		= 1129,			// pluginCommunicationTCUtoVCU_ID
						.taskId 	= TASK_ID_RteTask,
						.eventMask 	= EVENT_MASK_pluginCommunicationTCUEvent
				},
				{
						.msgId 		= 1601,			// pluginInstallationVCU_ID
						.taskId 	= TASK_ID_RteTask,
						.eventMask 	= EVENT_MASK_pluginInstallationEvent
				}
		}
  	}
};


const Can_CallbackType CanCallbackConfigData = {
    NULL, //CanIf_CancelTxConfirmation,
    CanIf_RxIndication,
    CanIf_ControllerBusOff,
    CanIf_TxConfirmation,
    NULL, //CanIf_ControllerWakeup,
    CanIf_Arc_Error
};

const Can_ConfigSetType CanConfigSetData =
{
  .CanController =	CanControllerConfigData,
  .CanCallbacks =	&CanCallbackConfigData
};

const Can_ConfigType CanConfigData = {
  .CanConfigSet =	&CanConfigSetData
};
