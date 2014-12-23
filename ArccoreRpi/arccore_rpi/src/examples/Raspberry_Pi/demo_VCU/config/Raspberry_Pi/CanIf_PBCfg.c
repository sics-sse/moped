
#include "CanIf.h"
#include "CanIf_PBCfg.h"

#if defined(USE_CANTP)
#include "CanTp.h"
#include "CanTp_PBCfg.h"
#endif
#if defined(USE_CANNM)
#include "CanNm.h"
#include "CanNm_PBCfg.h"
#endif
#if defined(USE_PDUR)
#include "PduR.h"
#include "PduR_PbCfg.h"
#endif

#include "MemMap.h"


		
			
			
				
			
			


// Data for init configuration CanIfInitConfiguration

SECTION_POSTBUILD_DATA const CanIf_HthConfigType CanIfHthConfigData_CanIfInitHohCfg[] =
{
	{ 
    	.CanIfHthType 				= CAN_ARC_HANDLE_TYPE_BASIC,
    	.CanIfCanControllerIdRef 	= CANIF_CanIfCtrlCfg,
    	.CanIfHthIdSymRef 			= CAN_TX,
    	.CanIf_Arc_EOL 				= TRUE,
	},
};

SECTION_POSTBUILD_DATA const CanIf_HrhConfigType CanIfHrhConfigData_CanIfInitHohCfg[] =
{
	{
    	.CanIfHrhType 				= CAN_ARC_HANDLE_TYPE_BASIC,
    	.CanIfSoftwareFilterHrh 	= TRUE,
    	.CanIfCanControllerHrhIdRef = CANIF_CanIfCtrlCfg,
    	.CanIfHrhIdSymRef 			= CAN_RX,
    	.CanIf_Arc_EOL				= TRUE,
  	},
};

SECTION_POSTBUILD_DATA const CanIf_InitHohConfigType CanIfHohConfigData[] = { 
	{
		.CanIfHrhConfig 	= CanIfHrhConfigData_CanIfInitHohCfg,
	    .CanIfHthConfig 	= CanIfHthConfigData_CanIfInitHohCfg,
    	.CanIf_Arc_EOL 		= TRUE,
	},
};

SECTION_POSTBUILD_DATA const CanIf_TxBufferConfigType CanIfBufferCfgData[] = {
	{
		.CanIfBufferSize = 0,
		.CanIfBufferHthRef = &CanIfHthConfigData_CanIfInitHohCfg[0],
		.CanIf_Arc_BufferId = 0
	},
};

SECTION_POSTBUILD_DATA const CanIf_TxPduConfigType CanIfTxPduConfigData[] = {
	{
		.CanIfTxPduId 				= PDUR_REVERSE_PDU_ID_ACKNOWLEDGEMENTVCUPDU,
    	.CanIfCanTxPduIdCanId 		= 1284,
    	.CanIfCanTxPduIdDlc 		= 1,
    	.CanIfCanTxPduType 			= CANIF_PDU_TYPE_STATIC,
#if ( CANIF_PUBLIC_READTXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadTxPduNotifyStatus = FALSE,
#endif
    	.CanIfTxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserTxConfirmation 	= PDUR_CALLOUT,
    	.CanIfTxPduBufferRef		= &CanIfBufferCfgData[0],
    	.PduIdRef 					= NULL,
	},
	{
		.CanIfTxPduId 				= PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONVCUTOTCUPDU,
    	.CanIfCanTxPduIdCanId 		= 1124,
    	.CanIfCanTxPduIdDlc 		= 8,
    	.CanIfCanTxPduType 			= CANIF_PDU_TYPE_STATIC,
#if ( CANIF_PUBLIC_READTXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadTxPduNotifyStatus = FALSE,
#endif
    	.CanIfTxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserTxConfirmation 	= PDUR_CALLOUT,
    	.CanIfTxPduBufferRef		= &CanIfBufferCfgData[0],
    	.PduIdRef 					= NULL,
	},
	{
		.CanIfTxPduId 				= PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU,
    	.CanIfCanTxPduIdCanId 		= 1122,
    	.CanIfCanTxPduIdDlc 		= 8,
    	.CanIfCanTxPduType 			= CANIF_PDU_TYPE_STATIC,
#if ( CANIF_PUBLIC_READTXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadTxPduNotifyStatus = FALSE,
#endif
    	.CanIfTxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserTxConfirmation 	= PDUR_CALLOUT,
    	.CanIfTxPduBufferRef		= &CanIfBufferCfgData[0],
    	.PduIdRef 					= NULL,
	},
};

SECTION_POSTBUILD_DATA const CanIf_RxPduConfigType CanIfRxPduConfigData[] = {
	{
		.CanIfCanRxPduId 			= PDUR_PDU_ID_PLUGININSTALLATIONVCUPDU,
    	.CanIfCanRxPduLowerCanId 	= 1601,
    	.CanIfCanRxPduUpperCanId 	= 1601,
    	.CanIfCanRxPduDlc 			= 8,
#if ( CANIF_PUBLIC_READRXPDU_DATA_API == STD_ON )    
    	.CanIfReadRxPduData 		= FALSE,
#endif    
#if ( CANIF_PUBLIC_READRXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadRxPduNotifyStatus = FALSE, 
#endif
		.CanIfCanRxPduHrhRef		= &CanIfHrhConfigData_CanIfInitHohCfg[0],
    	.CanIfRxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserRxIndication 		= PDUR_CALLOUT,
    	.CanIfCanRxPduCanIdMask 	= 0x7FF,
    	.PduIdRef	 				= NULL,
	},
	{
		.CanIfCanRxPduId 			= PDUR_PDU_ID_PLUGINCOMMUNICATIONTCUTOVCUPDU,
    	.CanIfCanRxPduLowerCanId 	= 1129,
    	.CanIfCanRxPduUpperCanId 	= 1129,
    	.CanIfCanRxPduDlc 			= 8,
#if ( CANIF_PUBLIC_READRXPDU_DATA_API == STD_ON )    
    	.CanIfReadRxPduData 		= FALSE,
#endif    
#if ( CANIF_PUBLIC_READRXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadRxPduNotifyStatus = FALSE, 
#endif
		.CanIfCanRxPduHrhRef		= &CanIfHrhConfigData_CanIfInitHohCfg[0],
    	.CanIfRxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserRxIndication 		= PDUR_CALLOUT,
    	.CanIfCanRxPduCanIdMask 	= 0x7FF,
    	.PduIdRef	 				= NULL,
	},
	{
		.CanIfCanRxPduId 			= PDUR_PDU_ID_DRIVERSETSPEEDSTEERINGPDU,
    	.CanIfCanRxPduLowerCanId 	= 257,
    	.CanIfCanRxPduUpperCanId 	= 257,
    	.CanIfCanRxPduDlc 			= 2,
#if ( CANIF_PUBLIC_READRXPDU_DATA_API == STD_ON )    
    	.CanIfReadRxPduData 		= FALSE,
#endif    
#if ( CANIF_PUBLIC_READRXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadRxPduNotifyStatus = FALSE, 
#endif
		.CanIfCanRxPduHrhRef		= &CanIfHrhConfigData_CanIfInitHohCfg[0],
    	.CanIfRxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserRxIndication 		= PDUR_CALLOUT,
    	.CanIfCanRxPduCanIdMask 	= 0x7FF,
    	.PduIdRef	 				= NULL,
	},
	{
		.CanIfCanRxPduId 			= PDUR_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU,
    	.CanIfCanRxPduLowerCanId 	= 1121,
    	.CanIfCanRxPduUpperCanId 	= 1121,
    	.CanIfCanRxPduDlc 			= 8,
#if ( CANIF_PUBLIC_READRXPDU_DATA_API == STD_ON )    
    	.CanIfReadRxPduData 		= FALSE,
#endif    
#if ( CANIF_PUBLIC_READRXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadRxPduNotifyStatus = FALSE, 
#endif
		.CanIfCanRxPduHrhRef		= &CanIfHrhConfigData_CanIfInitHohCfg[0],
    	.CanIfRxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserRxIndication 		= PDUR_CALLOUT,
    	.CanIfCanRxPduCanIdMask 	= 0x7FF,
    	.PduIdRef	 				= NULL,
	},
	{
		.CanIfCanRxPduId 			= PDUR_PDU_ID_POISTIONDATAPDU,
    	.CanIfCanRxPduLowerCanId 	= 1025,
    	.CanIfCanRxPduUpperCanId 	= 1025,
    	.CanIfCanRxPduDlc 			= 8,
#if ( CANIF_PUBLIC_READRXPDU_DATA_API == STD_ON )    
    	.CanIfReadRxPduData 		= FALSE,
#endif    
#if ( CANIF_PUBLIC_READRXPDU_NOTIFY_STATUS_API == STD_ON )
    	.CanIfReadRxPduNotifyStatus = FALSE, 
#endif
		.CanIfCanRxPduHrhRef		= &CanIfHrhConfigData_CanIfInitHohCfg[0],
    	.CanIfRxPduIdCanIdType 		= CANIF_CAN_ID_TYPE_11,
    	.CanIfUserRxIndication 		= PDUR_CALLOUT,
    	.CanIfCanRxPduCanIdMask 	= 0x7FF,
    	.PduIdRef	 				= NULL,
	},
};

SECTION_POSTBUILD_DATA const CanIf_TxBufferConfigType *const CanIfCtrlCfg_BufferList[] = {
	/* CanIfBufferCfg */
	&CanIfBufferCfgData[0],
};


SECTION_POSTBUILD_DATA const CanIf_Arc_ChannelConfigType CanIf_Arc_ChannelConfig[CANIF_CHANNEL_CNT] = { 
	{
		/* CanIfCtrlCfg */
		.CanControllerId = CAN_CONTROLLER_CanController,
		.NofTxBuffers = 1,
		.TxBufferRefList = CanIfCtrlCfg_BufferList
	},
};
// This container contains the init parameters of the CAN
// Multiplicity 1.
SECTION_POSTBUILD_DATA const CanIf_InitConfigType CanIfInitConfig =
{
	.CanIfConfigSet 					= 0, // Not used  
	.CanIfNumberOfCanRxPduIds 			= 5,
	.CanIfNumberOfCanTXPduIds 			= 3,
	.CanIfNumberOfDynamicCanTXPduIds	= 0, // Not used
	.CanIfNumberOfTxBuffers				= 1,

	// Containers
	.CanIfBufferCfgPtr					= CanIfBufferCfgData,
	.CanIfHohConfigPtr 					= CanIfHohConfigData,
	.CanIfRxPduConfigPtr 				= CanIfRxPduConfigData,
	.CanIfTxPduConfigPtr 				= CanIfTxPduConfigData,
};

// This container includes all necessary configuration sub-containers
// according the CAN Interface configuration structure.
SECTION_POSTBUILD_DATA const CanIf_ConfigType CanIf_Config =
{
	
	.InitConfig 					= &CanIfInitConfig,
	.TransceiverConfig 				= NULL, // Not used
	.Arc_ChannelConfig			 	= CanIf_Arc_ChannelConfig,  
};



