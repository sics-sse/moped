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

#include "Rte_ImuSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- ImuSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_Array3 dataUInt16;
        } ImuSwcWriteAcceDataToPirteSwcPort2;
        struct {
            Rte_DE_Array3 dataUInt16;
        } ImuSwcWriteGyroDataToPirteSwcPort1;
    } ImuSwcRunnable;
} ImplDE_ImuSWCProto;

const Rte_CDS_ImuSWC ImuSWC_ImuSWCProto = {
    .ImuSwcRunnable_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16 = &ImplDE_ImuSWCProto.ImuSwcRunnable.ImuSwcWriteGyroDataToPirteSwcPort1.dataUInt16,
    .ImuSwcRunnable_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16 = &ImplDE_ImuSWCProto.ImuSwcRunnable.ImuSwcWriteAcceDataToPirteSwcPort2.dataUInt16
};

const Rte_Instance Rte_Inst_ImuSWC = &ImuSWC_ImuSWCProto;

/** === Runnables =================================================================================
 */

/** ------ ImuSWCProto -----------------------------------------------------------------------
 */

void Rte_ImuSWCProto_ImuSwcRunnable(void) {
    /* PRE */

    /* MAIN */

    ImuSwcRunnable();

    /* POST */
    Rte_Write_ImuSWC_ImuSWCProto_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16(
            ImplDE_ImuSWCProto.ImuSwcRunnable.ImuSwcWriteGyroDataToPirteSwcPort1.dataUInt16.value);

    Rte_Write_ImuSWC_ImuSWCProto_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16(
            ImplDE_ImuSWCProto.ImuSwcRunnable.ImuSwcWriteAcceDataToPirteSwcPort2.dataUInt16.value);

}

