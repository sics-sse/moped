

#if !(((CANIF_SW_MAJOR_VERSION == 3) && (CANIF_SW_MINOR_VERSION == 0)) )
#error CanIf: Configuration file expected BSW module version to be 3.0.*
#endif

/* @req 4.0.3/CANIF021 */
#if !(((CANIF_AR_RELEASE_MAJOR_VERSION == 4) && (CANIF_AR_RELEASE_MINOR_VERSION == 0)) )
#error CanIf: Configuration file expected AUTOSAR version to be 4.0.*
#endif

#ifndef CANIF_CFG_H_
#define CANIF_CFG_H_

#include "Can.h"


#define CANIF_PUBLIC_CANCEL_TRANSMIT_SUPPORT			STD_OFF  // Not supported
#define CANIF_PUBLIC_CHANGE_BAUDRATE_SUPPORT			STD_OFF  // Not supported
#define CANIF_PUBLIC_DEV_ERROR_DETECT					STD_ON
#define CANIF_PUBLIC_MULTIPLE_DRV_SUPPORT				STD_OFF  // Not supported
#define CANIF_PUBLIC_PN_SUPPORT							STD_OFF  // Not supported
#define CANIF_PUBLIC_READRXPDU_DATA_API					STD_OFF  // Not supported
#define CANIF_PUBLIC_READRXPDU_NOTIFY_STATUS_API		STD_OFF  // Not supported
#define CANIF_PUBLIC_READTXPDU_NOTIFY_STATUS_API		STD_OFF  // Not supported
#define CANIF_PUBLIC_SETDYNAMICTXID_API					STD_OFF  // Not supported
#define CANIF_PUBLIC_TX_BUFFERING						STD_OFF
#define CANIF_PUBLIC_TXCONFIRM_POLLING_SUPPORT			STD_OFF  // Not supported
#define CANIF_PUBLIC_VERSION_INFO_API					STD_OFF
#define CANIF_PUBLIC_WAKEUP_CHECK_VALID_BY_NM			STD_OFF  // Not supported
#define CANIF_PUBLIC_WAKEUP_CHECK_VALIDATION_SUPPORT	STD_OFF  // Not supported

#define CANIF_PRIVATE_DLC_CHECK							STD_ON

//
#define CANIF_CTRL_WAKEUP_SUPPORT						STD_OFF  // Not supported 
#define CANIF_TRCV_WAKEUP_SUPPORT						STD_OFF  // Not supported

#define CANIF_CTRLDRV_TX_CANCELLATION					STD_OFF

// ArcCore
#define CANIF_ARC_RUNTIME_PDU_CONFIGURATION				STD_OFF  // Not supported
#define CANIF_ARC_TRANSCEIVER_API						STD_OFF  // Not supported

//Software filter
#define CANIF_PRIVATE_SOFTWARE_FILTER_TYPE_MASK


// Identifiers for the elements in CanIfControllerConfig[]
// This is the ConfigurationIndex in CanIf_InitController()
//typedef enum {
////	CANIF_CanIfCtrlCfg_CONFIG_0,
////	CANIF_CHANNEL_CONFIGURATION_CNT
//} CanIf_Arc_ConfigurationIndexType;

typedef enum {
	CANIF_CanIfCtrlCfg,
	CANIF_CHANNEL_CNT
} CanIf_Arc_ChannelIdType;

#define CANIF_CONTROLLER_ID_CanController	CANIF_CanIfCtrlCfg



	
#define	CANNM_CALLOUT    0u
#define	CANTP_CALLOUT    1u
#define	J1939TP_CALLOUT  2u
#define	PDUR_CALLOUT     3u



#include "CanIf_ConfigTypes.h"


#endif
