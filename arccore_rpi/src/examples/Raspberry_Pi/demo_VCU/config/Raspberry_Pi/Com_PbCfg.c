
#if defined(USE_PDUR)
#include "PduR.h"
#include "PduR_PbCfg.h"
#endif

#if defined(USE_CANTP)
#include "CanTp.h"
#include "CanTp_PBCfg.h"
#endif

#include "Com.h"
#include "Com_PbCfg.h"

#include "MemMap.h"

#if (COM_MAX_BUFFER_SIZE < 51)
#error Com: The configured ram buffer size is less than required! (51 bytes required)
#endif
#if (COM_MAX_N_IPDUS < 8)
#error Com: Configured maximum number of Pdus is less than the number of Pdus in configuration!
#endif
#if (COM_MAX_N_SIGNALS < 8)
#error Com: Configured maximum number of signals is less than the number of signals in configuration!
#endif
#if (COM_MAX_N_GROUP_SIGNALS < 0)
#error Com: Configured maximum number of groupsignals is less than the number of groupsignals in configuration!
#endif

/*
 * Signal init values.
 */
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginInstallationVCUsignal[8] = { 0x00};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationVCUtoTCUsignal[8] = { 0};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationTCUtoVCUsignal[8] = { 0};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_acknowledgementVCUsignal[1] = { 0};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_driverSetSpeedSteeringSignal[2] = { 0};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationVCUtoSCUSignal[8] = { 0};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationSCUtoVCUSignal[8] = { 0};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_positionDataSignal[8] = { 0};

/*
 * Group signal definitions
 */
SECTION_POSTBUILD_DATA const ComGroupSignal_type ComGroupSignal[] = {
	{
		.Com_Arc_EOL = 1
	}
};


/*
 * SignalGroup GroupSignals lists.
 */



/*
 * Signal group masks.
 */


/*
 * Signal definitions
 */
SECTION_POSTBUILD_DATA const ComSignal_type ComSignal[] = {

	{ // pluginInstallationVCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGININSTALLATIONVCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGININSTALLATIONVCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginInstallationVCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginCommunicationVCUtoTCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOTCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONVCUTOTCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= TRIGGERED,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationVCUtoTCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginCommunicationTCUtoVCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOVCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONTCUTOVCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationTCUtoVCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // acknowledgementVCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_ACKNOWLEDGEMENTVCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_ACKNOWLEDGEMENTVCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= TRIGGERED,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_acknowledgementVCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 8,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // driverSetSpeedSteeringSignal
		.ComHandleId 				= COM_SIGNAL_ID_DRIVERSETSPEEDSTEERINGSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_DRIVERSETSPEEDSTEERINGPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_driverSetSpeedSteeringSignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 16,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginCommunicationVCUtoSCUSignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= TRIGGERED,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationVCUtoSCUSignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginCommunicationSCUtoVCUSignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationSCUtoVCUSignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // positionDataSignal
		.ComHandleId 				= COM_SIGNAL_ID_POSITIONDATASIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_POISTIONDATAPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_positionDataSignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{
		.Com_Arc_EOL				= 1
	}
};


/*
 * I-PDU group definitions
 */
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIPduGroup[] = {

	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},

	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},

	{
		.Com_Arc_EOL				= 1
	}
};


/* 
 * IPdu signal lists. 
 */

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginInstallationVCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGININSTALLATIONVCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_alacknowledgementVCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_ACKNOWLEDGEMENTVCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationTCUtoVCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOVCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationVCUtoTCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOTCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_driverSetSpeedSteeringComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_DRIVERSETSPEEDSTEERINGSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationSCUtoVCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationVCUtoSCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_positionDataComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_POSITIONDATASIGNAL],
	NULL
};


/*
 * I-PDU group ref lists
 */
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginInstallationVCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_alacknowledgementVCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationTCUtoVCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationVCUtoTCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_driverSetSpeedSteeringComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationSCUtoVCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationVCUtoSCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_positionDataComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};


/*
 * I-PDU definitions
 */
SECTION_POSTBUILD_DATA const ComIPdu_type ComIPdu[] = {	

	{ // pluginInstallationVCUComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_PLUGININSTALLATIONVCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginInstallationVCUComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= NONE,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginInstallationVCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // alacknowledgementVCUComIPdu
		.ArcIPduOutgoingId			= PDUR_PDU_ID_ACKNOWLEDGEMENTVCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 1,
		.ComIPduDirection			= SEND,
		.ComIPduGroupRefs			= ComIpduGroupRefs_alacknowledgementVCUComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= DIRECT,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_alacknowledgementVCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationTCUtoVCUComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONTCUTOVCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationTCUtoVCUComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= NONE,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationTCUtoVCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationVCUtoTCUComIPdu
		.ArcIPduOutgoingId			= PDUR_PDU_ID_PLUGINCOMMUNICATIONVCUTOTCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= SEND,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationVCUtoTCUComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= DIRECT,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationVCUtoTCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // driverSetSpeedSteeringComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_DRIVERSETSPEEDSTEERINGPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 2,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_driverSetSpeedSteeringComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= NONE,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_driverSetSpeedSteeringComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationSCUtoVCUComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationSCUtoVCUComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= NONE,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationSCUtoVCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationVCUtoSCUComIPdu
		.ArcIPduOutgoingId			= PDUR_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= SEND,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationVCUtoSCUComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= DIRECT,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationVCUtoSCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // positionDataComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_POISTIONDATAPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_positionDataComIPdu,
		.ComTxIPdu = {
			.ComTxIPduMinimumDelayFactor	= 0,
			.ComTxIPduUnusedAreasDefault	= 0,
			.ComTxModeTrue = {
				.ComTxModeMode						= NONE,
				.ComTxModeNumberOfRepetitions		= 0,
				.ComTxModeRepetitionPeriodFactor	= 0,
				.ComTxModeTimeOffsetFactor			= 0,
				.ComTxModeTimePeriodFactor			= 0,
			},
		},
		.ComIPduSignalRef			= ComIPduSignalRefs_positionDataComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{
		.Com_Arc_EOL				= 1
	}
};

SECTION_POSTBUILD_DATA const Com_ConfigType ComConfiguration = {
	.ComConfigurationId 			= 1,
	.ComNofIPdus					= 8,
	.ComNofSignals					= 8,
	.ComNofGroupSignals				= 0,
	.ComIPdu 						= ComIPdu,
	.ComIPduGroup 					= ComIPduGroup,
	.ComSignal						= ComSignal,
	.ComGroupSignal					= ComGroupSignal
};


