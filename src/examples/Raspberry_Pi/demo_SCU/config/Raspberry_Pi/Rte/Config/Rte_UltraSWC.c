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

#include "Rte_UltraSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- UltraSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } UltraSwcDataOutPort2;
    } UltrasonicRunnable;
} ImplDE_UltraSWCProto;

const Rte_CDS_UltraSWC UltraSWC_UltraSWCProto = {
    .UltrasonicRunnable_UltraSwcDataOutPort2_dataInt32 = &ImplDE_UltraSWCProto.UltrasonicRunnable.UltraSwcDataOutPort2.dataInt32
};

const Rte_Instance Rte_Inst_UltraSWC = &UltraSWC_UltraSWCProto;

/** === Runnables =================================================================================
 */

/** ------ UltraSWCProto -----------------------------------------------------------------------
 */

void Rte_UltraSWCProto_UltrasonicRunnable(void) {
    /* PRE */

    /* MAIN */

    UltrasonicRunnable();

    /* POST */
    Rte_Write_UltraSWC_UltraSWCProto_UltraSwcDataOutPort2_dataInt32(ImplDE_UltraSWCProto.UltrasonicRunnable.UltraSwcDataOutPort2.dataInt32.value);

}

