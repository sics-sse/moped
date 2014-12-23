

#if !(((CANTP_AR_RELEASE_MAJOR_VERSION == 4) && (CANTP_AR_RELEASE_MINOR_VERSION == 0)) )
#error CanTp: Configuration file expected AUTOSAR version to be 4.0.*
#endif

#if !(((CANTP_SW_MAJOR_VERSION == 1 ) && (CANTP_SW_MINOR_VERSION == 0)) )
#error CanTp: Configuration file expected BSW module version to be 1.0.*
#endif

#ifndef CANTP_CFG_H_
#define CANTP_CFG_H_

#include "CanTp_Types.h"
#define CANTP_MAX_NOF_SDUS				3

#define FRTP_CANCEL_TRANSMIT_REQUEST 	STD_ON
#define CANTP_VERSION_INFO_API          STD_OFF  /**< Build version info API */
#define CANTP_DEV_ERROR_DETECT          STD_ON
 
#define NO_REFERRING_TX_INDEX			0xFFFF

/** Not Supported*/
#define CANTP_CHANGE_PARAMETER_API		STD_OFF
/** Not Supported*/
#define CANTP_READ_PARAMETER_API		STD_OFF

#define CANTP_MAX_NO_CHANNELS			1

#endif /* CANTP_CFG_H_ */

