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

#include "Rte_LedSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- LedSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } LedSwcReadDataFromPirteSwcPort1;
    } LedControlRunnable;
} ImplDE_LedSWCProto;

const Rte_CDS_LedSWC LedSWC_LedSWCProto = {
    .LedControlRunnable_LedSwcReadDataFromPirteSwcPort1_dataInt32 = &ImplDE_LedSWCProto.LedControlRunnable.LedSwcReadDataFromPirteSwcPort1.dataInt32
};

const Rte_Instance Rte_Inst_LedSWC = &LedSWC_LedSWCProto;

/** === Runnables =================================================================================
 */

/** ------ LedSWCProto -----------------------------------------------------------------------
 */

void Rte_LedSWCProto_LedControlRunnable(void) {
    /* PRE */
    Rte_Read_LedSWC_LedSWCProto_LedSwcReadDataFromPirteSwcPort1_dataInt32(
            &ImplDE_LedSWCProto.LedControlRunnable.LedSwcReadDataFromPirteSwcPort1.dataInt32.value);

    /* MAIN */

    LedControlRunnable();

    /* POST */

}

