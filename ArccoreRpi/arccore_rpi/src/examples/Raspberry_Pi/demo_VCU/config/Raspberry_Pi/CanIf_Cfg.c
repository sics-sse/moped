
#include "CanIf.h"

#if defined(USE_CANTP)
#include "CanTp.h"
#include "CanTp_Cbk.h"
#endif
#if defined(USE_J1939TP)
#include "J1939Tp.h"
#include "J1939Tp_Cbk.h"
#endif
#if defined(USE_PDUR)
#include "PduR.h"
#endif
#if defined(USE_CANNM)
#include "CanNm_Cbk.h"
#endif





const CanIfUserRxIndicationType CanIfUserRxIndications[] = {
#if defined(USE_CANNM)	
	CanNm_RxIndication,
#else
	NULL,
#endif	
#if defined(USE_CANTP)	
	CanTp_RxIndication,
#else
	NULL,
#endif
#if defined(USE_J1939TP)
	J1939Tp_RxIndication,
#else
	NULL,
#endif
#if defined(USE_PDUR)	
	PduR_CanIfRxIndication,
#else
	NULL,
#endif

};

const CanIfUserTxConfirmationType CanIfUserTxConfirmations[] = {
#if defined(USE_CANNM)	
	CanNm_TxConfirmation,
#else
	NULL,
#endif	
#if defined(USE_CANTP)	
	CanTp_TxConfirmation,
#else
	NULL,
#endif
#if defined(USE_J1939TP)
	J1939Tp_TxConfirmation,
#else
	NULL,
#endif
#if defined(USE_PDUR)	
	PduR_CanIfTxConfirmation,
#else
	NULL,
#endif
};







// Function callbacks for higher layers
const CanIf_DispatchConfigType CanIfDispatchConfig =
{
	.CanIfBusOffNotification		= NULL,
	.CanIfControllerModeIndication 	= NULL,
  	.CanIfWakeUpNotification 		= NULL,	// Not used
  	.CanIfWakeupValidNotification 	= NULL,	// Not used
  	.CanIfErrorNotificaton 			= NULL,	// Not used
};



