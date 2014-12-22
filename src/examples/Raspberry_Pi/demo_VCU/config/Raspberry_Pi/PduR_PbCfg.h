
#ifndef PDUR_PBCFG_H
#define PDUR_PBCFG_H

#if !(((PDUR_SW_MAJOR_VERSION == 1) && (PDUR_SW_MINOR_VERSION == 2)) )
#error PduR: Configuration file expected BSW module version to be 1.2.*
#endif

#if !(((PDUR_AR_RELEASE_MAJOR_VERSION == 4) && (PDUR_AR_RELEASE_MINOR_VERSION == 0)) )
#error PduR: Configuration file expected AUTOSAR version to be 4.0.*
#endif

#include "PduR_Types.h"

// Zero cost operation support active.
#if defined(USE_CANIF)
#include "CanIf.h"
#include "CanIf_PBCfg.h"
#endif
#if defined(USE_COM)
#include "Com.h"
#include "Com_PbCfg.h"
#endif

extern const PduR_PBConfigType PduR_Config;

//  PduR Polite Defines.

#endif

