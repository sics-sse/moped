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

#include "Rte_RearWheelSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- RearWheelSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } RearWheelSwcWriteDataToPirteSwcPort1;
    } RearWheelSensorWriteRunnable;
} ImplDE_RearWheelSWCProto;

const Rte_CDS_RearWheelSWC RearWheelSWC_RearWheelSWCProto = {
    .RearWheelSensorWriteRunnable_RearWheelSwcWriteDataToPirteSwcPort1_dataInt32 = &ImplDE_RearWheelSWCProto.RearWheelSensorWriteRunnable.RearWheelSwcWriteDataToPirteSwcPort1.dataInt32
};

const Rte_Instance Rte_Inst_RearWheelSWC = &RearWheelSWC_RearWheelSWCProto;

/** === Runnables =================================================================================
 */

/** ------ RearWheelSWCProto -----------------------------------------------------------------------
 */

void Rte_RearWheelSWCProto_RearWheelSensorWriteRunnable(void) {
    /* PRE */

    /* MAIN */

    RearWheelSensorWriteRunnable();

    /* POST */
    Rte_Write_RearWheelSWC_RearWheelSWCProto_RearWheelSwcWriteDataToPirteSwcPort1_dataInt32(
            ImplDE_RearWheelSWCProto.RearWheelSensorWriteRunnable.RearWheelSwcWriteDataToPirteSwcPort1.dataInt32.value);

}

void Rte_RearWheelSWCProto_RearWheelSensorReadRunnable(void) {
    /* PRE */

    /* MAIN */

    RearWheelSensorReadRunnable();

    /* POST */

}

