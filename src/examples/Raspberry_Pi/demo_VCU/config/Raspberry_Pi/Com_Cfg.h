
#ifndef COM_CFG_H
#define COM_CFG_H

#if !(((COM_SW_MAJOR_VERSION == 1) && (COM_SW_MINOR_VERSION == 0)) )
#error Com: Configuration file expected BSW module version to be 1.0.*
#endif

#if !(((COM_AR_RELEASE_MAJOR_VERSION == 4) && (COM_AR_RELEASE_MINOR_VERSION == 0)) )
#error Com: Configuration file expected AUTOSAR version to be 4.0.*
#endif

#define COM_DEV_ERROR_DETECT					STD_ON
#define COM_VERSION_INFO_API					STD_OFF

#define COM_MAX_BUFFER_SIZE						150

#define COM_MAX_N_IPDUS							20
#define COM_MAX_N_SIGNALS						40
#define COM_MAX_N_GROUP_SIGNALS					40
#define COM_N_SUPPORTED_IPDU_GROUPS				4

#define COM_E_INVALID_FILTER_CONFIGURATION		101
#define COM_E_INITIALIZATION_FAILED				102
#define COM_E_INVALID_SIGNAL_CONFIGURATION		103
#define COM_INVALID_PDU_ID						104
#define COM_INVALID_SIGNAL_ID					109
#define COM_ERROR_SIGNAL_IS_SIGNALGROUP			105

#define COM_E_TOO_MANY_IPDU						106
#define COM_E_TOO_MANY_SIGNAL					107
#define COM_E_TOO_MANY_GROUPSIGNAL				108

#define CPU_ENDIANESS							COM_BIG_ENDIAN


// Notifications


// Rx callouts

// Tx callouts


#endif /*COM_CFG_H*/


