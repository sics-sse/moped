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

#include "Rte_FrontWheelSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- FrontWheelSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } FrontWheelSwcWriteDataToPirteSwcPort1;
    } FrontWheelSensorWriteRunnable;
} ImplDE_FrontWheelSWCProto;

const Rte_CDS_FrontWheelSWC FrontWheelSWC_FrontWheelSWCProto = {
    .FrontWheelSensorWriteRunnable_FrontWheelSwcWriteDataToPirteSwcPort1_dataInt32 = &ImplDE_FrontWheelSWCProto.FrontWheelSensorWriteRunnable.FrontWheelSwcWriteDataToPirteSwcPort1.dataInt32
};

const Rte_Instance Rte_Inst_FrontWheelSWC = &FrontWheelSWC_FrontWheelSWCProto;

/** === Runnables =================================================================================
 */

/** ------ FrontWheelSWCProto -----------------------------------------------------------------------
 */

void Rte_FrontWheelSWCProto_FrontWheelSensorWriteRunnable(void) {
    /* PRE */

    /* MAIN */

    FrontWheelSensorWriteRunnable();

    /* POST */
    Rte_Write_FrontWheelSWC_FrontWheelSWCProto_FrontWheelSwcWriteDataToPirteSwcPort1_dataInt32(
            ImplDE_FrontWheelSWCProto.FrontWheelSensorWriteRunnable.FrontWheelSwcWriteDataToPirteSwcPort1.dataInt32.value);

}

void Rte_FrontWheelSWCProto_FrontWheelSensorReadRunnable(void) {
    /* PRE */

    /* MAIN */

    FrontWheelSensorReadRunnable();

    /* POST */

}

