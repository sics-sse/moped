
#ifndef PDUR_CFG_H_
#define PDUR_CFG_H_

#if !(((PDUR_SW_MAJOR_VERSION == 1) && (PDUR_SW_MINOR_VERSION == 2)) )
#error PduR: Configuration file expected BSW module version to be 1.2.*
#endif

/* @req PDUR0774 */
#if !(((PDUR_AR_RELEASE_MAJOR_VERSION == 4) && (PDUR_AR_RELEASE_MINOR_VERSION == 0)) )
#error PduR: Configuration file expected AUTOSAR version to be 4.0.*
#endif

#include "PduR_Types.h"

 						
#define PDUR_CANIF_SUPPORT 					STD_ON
#define PDUR_CANTP_SUPPORT					STD_OFF
#define PDUR_FRIF_SUPPORT					STD_OFF
#define PDUR_FRTP_SUPPORT					STD_OFF
#define PDUR_LINIF_SUPPORT					STD_OFF
#define PDUR_LINTP_SUPPORT					STD_OFF
#define PDUR_COM_SUPPORT					STD_ON
#define PDUR_DCM_SUPPORT					STD_OFF
#define PDUR_IPDUM_SUPPORT					STD_OFF
#define PDUR_SOAD_SUPPORT 					STD_OFF
#define PDUR_J1939TP_SUPPORT 				STD_OFF

#define PDUR_DEV_ERROR_DETECT				STD_OFF
#define PDUR_VERSION_INFO_API				STD_OFF
#define PDUR_ZERO_COST_OPERATION			STD_ON

#define PDUR_GATEWAY_OPERATION				STD_ON
#define PDUR_SB_TX_BUFFER_SUPPORT			STD_ON
#define PDUR_FIFO_TX_BUFFER_SUPPORT			STD_OFF

#define PDUR_MULTICAST_TOIF_SUPPORT			STD_ON
#define PDUR_MULTICAST_FROMIF_SUPPORT		STD_ON
#define PDUR_MULTICAST_TOTP_SUPPORT			STD_ON
#define PDUR_MULTICAST_FROMTP_SUPPORT		STD_ON


/* Minimum routing not supported.
#define PDUR_MINIMUM_ROUTING_UP_MODULE		COM
#define PDUR_MINIMUM_ROUTING_LO_MODULE		CAN_IF
#define PDUR_MINIMUM_ROUTING_UP_RXPDUID		((PduIdType)100)
#define PDUR_MINIMUM_ROUTING_LO_RXPDUID 	((PduIdType)255)
#define PDUR_MINIMUM_ROUTING_UP_TXPDUID 	((PduIdType)255)
#define PDUR_MINIMUM_ROUTING_LO_TXPDUID 	((PduIdType)255)
*/

// Tx buffer IDs (sorted in the same order as PduRTxBuffers array)

/* Maximum number of routing paths: 
 * N/A (ZERO_COST)
 * 
 */  

/* Zero cost definitions */
#define PDUR_PDU_ID_ACKNOWLEDEGMENTSCUPDU  CANIF_PDU_ID_ACKNOWLEDEGMENTSCUPDU
#define PDUR_REVERSE_PDU_ID_ACKNOWLEDEGMENTSCUPDU	COM_PDU_ID_ACKNOWLEDEGMENTSCUPDU
			
#define PDUR_PDU_ID_PLUGINCOMMUNICATIONSCUTOTCUPDU  CANIF_PDU_ID_PLUGINCOMMUNICATIONSCUTOTCUPDU
#define PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONSCUTOTCUPDU	COM_PDU_ID_PLUGINCOMMUNICATIONSCUTOTCUPDU
			
#define PDUR_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU  CANIF_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU
#define PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU	COM_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU
			
#define PDUR_PDU_ID_PLUGINCOMMUNICATIONTCUTOSCUPDU  COM_PDU_ID_PLUGINCOMMUNICATIONTCUTOSCUPDU
#define PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONTCUTOSCUPDU	CANIF_PDU_ID_PLUGINCOMMUNICATIONTCUTOSCUPDU
			
#define PDUR_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU  COM_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU
#define PDUR_REVERSE_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU	CANIF_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU
			
#define PDUR_PDU_ID_PLUGININSTALLATIONSCUPDU  COM_PDU_ID_PLUGININSTALLATIONSCUPDU
#define PDUR_REVERSE_PDU_ID_PLUGININSTALLATIONSCUPDU	CANIF_PDU_ID_PLUGININSTALLATIONSCUPDU
			
		
	
  



#if PDUR_ZERO_COST_OPERATION == STD_ON
// Zero cost operation support active.
/* @req PDUR287 */
#if PDUR_CANIF_SUPPORT == STD_ON
#include "Com.h"
#define PduR_CanIfRxIndication Com_RxIndication
#define PduR_CanIfTxConfirmation Com_TxConfirmation
#else
#define PduR_CanIfRxIndication(... )
#define PduR_CanIfTxConfirmation(...)
#endif

#if PDUR_CANTP_SUPPORT == STD_ON
#include "Dcm.h"
#define PduR_CanTpProvideRxBuffer Dcm_ProvideRxBuffer
#define PduR_CanTpRxIndication Dcm_RxIndication
#define PduR_CanTpProvideTxBuffer Dcm_ProvideTxBuffer
#define PduR_CanTpTxConfirmation Dcm_TxConfirmation
#else
#define PduR_CanTpProvideRxBuffer(...)
#define PduR_CanTpRxIndication(...)
#define PduR_CanTpProvideTxBuffer(...)
#define PduR_CanTpTxConfirmation(...)
#endif

#if PDUR_LINIF_SUPPORT == STD_ON 
#include "Com.h"
#define PduR_LinIfRxIndication Com_RxIndication
#define PduR_LinIfTxConfirmation Com_TxConfirmation
#define PduR_LinIfTriggerTransmit Com_TriggerTransmit
#else
#define PduR_LinIfRxIndication(...)
#define PduR_LinIfTxConfirmation(...)
#define PduR_LinIfTriggerTransmit(...)
#endif

#if PDUR_SOAD_SUPPORT == STD_ON
#include "Dcm.h"
#define PduR_SoAdTpProvideRxBuffer Dcm_ProvideRxBuffer
#define PduR_SoAdTpRxIndication Dcm_RxIndication
#define PduR_SoAdTpProvideTxBuffer Dcm_ProvideTxBuffer
#define PduR_SoAdTpTxConfirmation Dcm_TxConfirmation
#else
#define PduR_SoAdProvideRxBuffer(...)
#define PduR_SoAdRxIndication(...)
#define PduR_SoAdProvideTxBuffer(...)
#define PduR_SoAdTxConfirmation(...)
#endif

#if PDUR_J1939TP_SUPPORT == STD_ON
#include "Dcm.h"
#define PduR_J1939TpProvideRxBuffer Dcm_ProvideRxBuffer
#define PduR_J1939TpRxIndication Dcm_RxIndication
#define PduR_J1939TpProvideTxBuffer Dcm_ProvideTxBuffer
#define PduR_J1939TpTxConfirmation Dcm_TxConfirmation
#else
#define PduR_J1939TpProvideRxBuffer(...)
#define PduR_J1939TpRxIndication(...)
#define PduR_J1939TpProvideTxBuffer(...)
#define PduR_J1939TpTxConfirmation(...)
#endif

#if PDUR_COM_SUPPORT == STD_ON
#include "CanIf.h"
#define PduR_ComTransmit CanIf_Transmit
#else
#define PduR_ComTransmit(... )	(E_OK)
#endif

#if PDUR_DCM_SUPPORT == STD_ON
#include "CanIf.h"
#define PduR_DcmTransmit CanIf_Transmit
#else
#define PduR_DcmTransmit(... )	(E_OK)
#endif

#endif // endif PDUR_ZERO_COST_OPERATION

#endif
