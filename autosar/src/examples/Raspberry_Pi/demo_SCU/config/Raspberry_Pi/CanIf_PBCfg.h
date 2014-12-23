
#ifndef CANIF_PBCFG_H_
#define CANIF_PBCFG_H_

#if !(((CANIF_SW_MAJOR_VERSION == 3) && (CANIF_SW_MINOR_VERSION == 0)) )
#error CanIf: Configuration file expected BSW module version to be 3.0.*
#endif

#if !(((CANIF_AR_RELEASE_MAJOR_VERSION == 4) && (CANIF_AR_RELEASE_MINOR_VERSION == 0)) )
#error CanIf: Configuration file expected AUTOSAR version to be 4.0.*
#endif




//Number of tx l-pdus
#define CANIF_NUM_TX_LPDU	3

#define CANIF_PDU_ID_PLUGINCOMMUNICATIONVCUTOSCUPDU		0
#define CANIF_PDU_ID_PLUGININSTALLATIONSCUPDU		1
#define CANIF_PDU_ID_PLUGINCOMMUNICATIONTCUTOSCUPDU		2

#define CANIF_PDU_ID_ACKNOWLEDEGMENTSCUPDU		0
#define CANIF_PDU_ID_PLUGINCOMMUNICATIONSCUTOTCUPDU		1
#define CANIF_PDU_ID_PLUGINCOMMUNICATIONSCUTOVCUPDU		2


#endif
