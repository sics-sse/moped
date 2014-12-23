


#include "CanTp.h"
#include "CanTp_PBCfg.h"
#if defined(USE_CANIF)
#include "CanIf.h"
#include "CanIf_PBCfg.h"
#endif
#if defined(USE_PDUR)
#include "PduR.h"
#include "PduR_PbCfg.h"
#endif

#include "MemMap.h"

#define CANTP_MAIN_FUNCTION_PERIOD_TIME_MS	5
#define CANTP_CONVERT_MS_TO_MAIN_CYCLES(x)  ((x)/CANTP_MAIN_FUNCTION_PERIOD_TIME_MS)


SECTION_POSTBUILD_DATA const CanTp_GeneralType CanTpGeneralConfig =
{
  .main_function_period = 5,
  .number_of_sdus = CANTP_NOF_SDUS,
  .number_of_pdus = CANTP_NOF_PDUS,
  .padding = 0
};


//NSa
SECTION_POSTBUILD_DATA const CanTp_NSaType CanTpNSaConfig_RxNSdu_pluginInstallationVCUCanTpRxNSdu = 
{
   .CanTpNSa = 0
};




//NTa
SECTION_POSTBUILD_DATA const CanTp_NTaType CanTpNTaConfig_RxNSdu_pluginInstallationVCUCanTpRxNSdu = 
{
   .CanTpNTa = 0
};




SECTION_POSTBUILD_DATA const CanTp_NSduType CanTpNSduConfigList[] =
{
	{	/* pluginInstallationVCUCanTpRxNSdu */
		.direction = ISO15765_RECEIVE,
		/*lint -e651 */				
		.configData.CanTpRxNSdu.CanIf_FcPduId = CANIF_PDU_ID_PLUGINCOMMUNICATIONVCUTOTCUPDU,
		.configData.CanTpRxNSdu.PduR_PduId = PDUR_PDU_ID_PLUGININSTALLATIONVCUPDU,
		.configData.CanTpRxNSdu.CanTpRxChannel = 0,
		.configData.CanTpRxNSdu.CanTpAddressingFormant = CANTP_STANDARD,
		.configData.CanTpRxNSdu.CanTpBs = 0,
		.configData.CanTpRxNSdu.CanTpNar = CANTP_CONVERT_MS_TO_MAIN_CYCLES(1000),
		.configData.CanTpRxNSdu.CanTpNbr = CANTP_CONVERT_MS_TO_MAIN_CYCLES(20),
		.configData.CanTpRxNSdu.CanTpNcr = CANTP_CONVERT_MS_TO_MAIN_CYCLES(1000),
		.configData.CanTpRxNSdu.CanTpRxDI = 8,
		.configData.CanTpRxNSdu.CanTpRxPaddingActivation = CANTP_ON,
		.configData.CanTpRxNSdu.CanTpRxTaType = CANTP_PHYSICAL,
		.configData.CanTpRxNSdu.CanTpWftMax = 255,
		.configData.CanTpRxNSdu.CanTpSTmin = 5,
		.configData.CanTpRxNSdu.CanTpNSa = &CanTpNSaConfig_RxNSdu_pluginInstallationVCUCanTpRxNSdu,
		.configData.CanTpRxNSdu.CanTpNTa = &CanTpNTaConfig_RxNSdu_pluginInstallationVCUCanTpRxNSdu,
		.listItemType = CANTP_END_OF_LIST	
	},
};


/* Test */
SECTION_POSTBUILD_DATA const CanTp_RxIdType CanTp_RxIdList[] = 
{
   /* pluginInstallationVCUCanTpRxNSdu */
   {
      .CanTpAddressingMode = CANTP_STANDARD,
      .CanTpNSduIndex = CANTP_PDU_ID_PLUGININSTALLATIONVCUPDU,
      .CanTpReferringTxIndex = NO_REFERRING_TX_INDEX
   },
	/* CanTpTxFcNPdu */
	{
      .CanTpAddressingMode = CANTP_STANDARD,
      .CanTpNSduIndex = CANTP_PDU_ID_PLUGININSTALLATIONVCUPDU,
      .CanTpReferringTxIndex = NO_REFERRING_TX_INDEX
	},
};

SECTION_POSTBUILD_DATA const CanTp_ConfigType CanTpConfig =
{
  .CanTpNSduList 	= 	CanTpNSduConfigList,
  .CanTpGeneral 	= 	&CanTpGeneralConfig,
  .CanTpRxIdList	=	CanTp_RxIdList
};



