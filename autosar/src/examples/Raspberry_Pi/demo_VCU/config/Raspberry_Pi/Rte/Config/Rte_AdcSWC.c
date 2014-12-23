/** === HEADER ====================================================================================
 */

/** @req SWS_Rte_01279 */
#include <Rte.h>

/** @req SWS_Rte_01257 */
#include <Os.h>

#if ((OS_AR_RELEASE_MAJOR_VERSION != RTE_AR_RELEASE_MAJOR_VERSION) || (OS_AR_RELEASE_MINOR_VERSION != RTE_AR_RELEASE_MINOR_VERSION))
#error Os version mismatch
#endif

/** @req SWS_Rte_03794 */
#include <Com.h>

#if ((COM_AR_RELEASE_MAJOR_VERSION != RTE_AR_RELEASE_MAJOR_VERSION) || (COM_AR_RELEASE_MINOR_VERSION != RTE_AR_RELEASE_MINOR_VERSION))
#error Com version mismatch
#endif

/** @req SWS_Rte_01326 */
#include <Rte_Hook.h>

#include <Rte_Internal.h>
#include <Rte_Calprms.h>

#include "Rte_AdcSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- AdcSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } AdcSwcWriteDataToPirteSwcPort1;
    } AdcSensorWriteRunnable;
} ImplDE_AdcSWCProto;

const Rte_CDS_AdcSWC AdcSWC_AdcSWCProto = {
    .AdcSensorWriteRunnable_AdcSwcWriteDataToPirteSwcPort1_dataInt32 = &ImplDE_AdcSWCProto.AdcSensorWriteRunnable.AdcSwcWriteDataToPirteSwcPort1.dataInt32
};

const Rte_Instance Rte_Inst_AdcSWC = &AdcSWC_AdcSWCProto;

/** === Runnables =================================================================================
 */

/** ------ AdcSWCProto -----------------------------------------------------------------------
 */

void Rte_AdcSWCProto_AdcSensorWriteRunnable(void) {
    /* PRE */

    /* MAIN */

    AdcSensorWriteRunnable();

    /* POST */
    Rte_Write_AdcSWC_AdcSWCProto_AdcSwcWriteDataToPirteSwcPort1_dataInt32(
            ImplDE_AdcSWCProto.AdcSensorWriteRunnable.AdcSwcWriteDataToPirteSwcPort1.dataInt32.value);

}

void Rte_AdcSWCProto_AdcSensorReadRunnable(void) {
    /* PRE */

    /* MAIN */

    AdcSensorReadRunnable();

    /* POST */

}

