#include <stdlib.h>
#include "Can.h"
#include "mcp2515.h"
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
		.CanECUId					= 	SCU_CANID,
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
					.msgId 		= 1122,			// pluginCommunicationVCUtoSCU_ID
					.taskId 	= TASK_ID_RteTask,
					.eventMask 	= EVENT_MASK_VCUCommunicationEvent
				},
				{
					.msgId 		= 1130,			// pluginCommunicationTCUtoSCU_ID
					.taskId 	= TASK_ID_RteTask,
					.eventMask 	= EVENT_MASK_TCUCommunicationEvent
				},
				{
					.msgId 		= 1602,			// pluginInstallationSCU_ID
					.taskId 	= TASK_ID_RteTask,
					.eventMask 	= EVENT_MASK_SCUInstallationEvent
				}
		}
  	},
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
