
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

#if (COM_MAX_BUFFER_SIZE < 41)
#error Com: The configured ram buffer size is less than required! (41 bytes required)
#endif
#if (COM_MAX_N_IPDUS < 6)
#error Com: Configured maximum number of Pdus is less than the number of Pdus in configuration!
#endif
#if (COM_MAX_N_SIGNALS < 6)
#error Com: Configured maximum number of signals is less than the number of signals in configuration!
#endif
#if (COM_MAX_N_GROUP_SIGNALS < 0)
#error Com: Configured maximum number of groupsignals is less than the number of groupsignals in configuration!
#endif

/*
 * Signal init values.
 */
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationSCUtoVCUsignal[8] = { 0x00};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationTCUtoSCUsignal[8] = { 0x00};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationVCUtoSCUsignal[8] = { 0x00};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_acknowledgementSCUsignal[1] = { 0};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginInstallationSCUsignal[8] = { 0x00};
SECTION_POSTBUILD_DATA const uint8 Com_SignalInitValue_pluginCommunicationSCUtoTCUsignal[8] = { 0};

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

	{ // pluginCommunicationSCUtoVCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= TRIGGERED,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationSCUtoVCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginCommunicationTCUtoSCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOSCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONTCUTOSCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationTCUtoSCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginCommunicationVCUtoSCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationVCUtoSCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // acknowledgementSCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_ACKNOWLEDGEMENTSCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_ACKNOWLEDGEMENTSCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= TRIGGERED,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_acknowledgementSCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 8,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginInstallationSCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGININSTALLATIONSCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGININSTALLATIONSCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= PENDING,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= TRUE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginInstallationSCUsignal,
		.ComBitPosition 			= 0,
		.ComBitSize 				= 64,
		.ComSignalEndianess 		= COM_OPAQUE,
		.ComSignalType 				= UINT8_N,
		.Com_Arc_IsSignalGroup 		= 0,
		.ComGroupSignal 			= NULL,
		.ComRxDataTimeoutAction 	= COM_TIMEOUT_DATA_ACTION_NONE,
		.Com_Arc_EOL 				= 0
	},

	{ // pluginCommunicationSCUtoTCUsignal
		.ComHandleId 				= COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOTCUSIGNAL,
		.ComIPduHandleId 			= COM_PDU_ID_PLUGINCOMMUNICATIONSCUTOTCUPDU,
		.ComFirstTimeoutFactor 		= 0,
		.ComNotification 			= COM_NO_FUNCTION_CALLOUT,
		.ComTimeoutFactor 			= 0,
		.ComTimeoutNotification 	= COM_NO_FUNCTION_CALLOUT,
		.ComErrorNotification 		= COM_NO_FUNCTION_CALLOUT,
		.ComTransferProperty 		= TRIGGERED,
		.ComUpdateBitPosition 		= 0,
		.ComSignalArcUseUpdateBit 	= FALSE,
		.ComSignalInitValue 		= Com_SignalInitValue_pluginCommunicationSCUtoTCUsignal,
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
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXCOMIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},

	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXCOMIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},

	{
		.Com_Arc_EOL				= 1
	}
};


/* 
 * IPdu signal lists. 
 */

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_acknowledgementSCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_ACKNOWLEDGEMENTSCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationSCUtoTCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOTCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationSCUtoVCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationTCUtoSCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOSCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginCommunicationVCUtoSCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL],
	NULL
};

SECTION_POSTBUILD_DATA const ComSignal_type * const ComIPduSignalRefs_pluginInstallationSCUComIPdu[] = {
	&ComSignal[COM_SIGNAL_ID_PLUGININSTALLATIONSCUSIGNAL],
	NULL
};


/*
 * I-PDU group ref lists
 */
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_acknowledgementSCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXCOMIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationSCUtoTCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXCOMIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationSCUtoVCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_TXCOMIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationTCUtoSCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXCOMIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginCommunicationVCUtoSCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXCOMIPDUGROUP,
		.Com_Arc_EOL 				= 0
	},
	{
		.Com_Arc_EOL				= 1
	}
};
SECTION_POSTBUILD_DATA const ComIPduGroup_type ComIpduGroupRefs_pluginInstallationSCUComIPdu[] = {
	{
		.ComIPduGroupHandleId 		= COM_PDU_GROUP_ID_RXCOMIPDUGROUP,
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

	{ // acknowledgementSCUComIPdu
		.ArcIPduOutgoingId			= PDUR_PDU_ID_ACKNOWLEDGEMENTSCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 1,
		.ComIPduDirection			= SEND,
		.ComIPduGroupRefs			= ComIpduGroupRefs_acknowledgementSCUComIPdu,
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
		.ComIPduSignalRef			= ComIPduSignalRefs_acknowledgementSCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationSCUtoTCUComIPdu
		.ArcIPduOutgoingId			= PDUR_PDU_ID_PLUGINCOMMUNICATIONSCUTOTCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= SEND,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationSCUtoTCUComIPdu,
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
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationSCUtoTCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationSCUtoVCUComIPdu
		.ArcIPduOutgoingId			= PDUR_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= SEND,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationSCUtoVCUComIPdu,
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
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationSCUtoVCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationTCUtoSCUComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONTCUTOSCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationTCUtoSCUComIPdu,
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
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationTCUtoSCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginCommunicationVCUtoSCUComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginCommunicationVCUtoSCUComIPdu,
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
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginCommunicationVCUtoSCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{ // pluginInstallationSCUComIPdu
		.ArcIPduOutgoingId			= PDUR_REVERSE_PDU_ID_PLUGININSTALLATIONSCUPDU,
		.ComRxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComTxIPduCallout			= COM_NO_FUNCTION_CALLOUT,
		.ComIPduSignalProcessing 	= IMMEDIATE,
		.ComIPduSize				= 8,
		.ComIPduDirection			= RECEIVE,
		.ComIPduGroupRefs			= ComIpduGroupRefs_pluginInstallationSCUComIPdu,
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
		.ComIPduSignalRef			= ComIPduSignalRefs_pluginInstallationSCUComIPdu,
		.ComIPduDynSignalRef		= NULL,
		.Com_Arc_EOL				= 0
	},   

	{
		.Com_Arc_EOL				= 1
	}
};

SECTION_POSTBUILD_DATA const Com_ConfigType ComConfiguration = {
	.ComConfigurationId 			= 1,
	.ComNofIPdus					= 6,
	.ComNofSignals					= 6,
	.ComNofGroupSignals				= 0,
	.ComIPdu 						= ComIPdu,
	.ComIPduGroup 					= ComIPduGroup,
	.ComSignal						= ComSignal,
	.ComGroupSignal					= ComGroupSignal
};


