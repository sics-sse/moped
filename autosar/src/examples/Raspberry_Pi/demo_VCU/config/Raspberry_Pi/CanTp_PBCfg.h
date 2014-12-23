

#ifndef CANTP_PBCFG_H_
#define CANTP_PBCFG_H_

#if !(((CANTP_AR_RELEASE_MAJOR_VERSION == 4) && (CANTP_AR_RELEASE_MINOR_VERSION == 0)) )
#error CanTp: Configuration file expected AUTOSAR version to be 4.0.*
#endif

#if !(((CANTP_SW_MAJOR_VERSION == 1 ) && (CANTP_SW_MINOR_VERSION == 0)) )
#error CanTp: Configuration file expected BSW module version to be 1.0.*
#endif

#include "CanTp_Types.h"




/* This is also the index in the configuration list defined in CanTp_Cfg.c*/
#define CANTP_PDU_ID_PLUGININSTALLATIONVCUPDU 0 /* pluginInstallationVCUCanTpRxNSdu */

/* The number of Sdus */
#define CANTP_NOF_SDUS 1

/* TxFcPdus not referenced as TxNPdu */
#define CANTP_PDU_ID_PLUGINCOMMUNICATIONVCUTOTCUPDU 1

/* The number of pdus */
#define CANTP_NOF_PDUS 2

#endif /* CANTP_PBCFG_H_ */

