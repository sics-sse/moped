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

#include "Rte_MotorSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- MotorSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } MotorSwcReadDataFromPirteSwcPort1;
        struct {
            Rte_DE_Array2 SpeedSteer;
            Rte_DE_UInt32 dataInt32;
        } MotorSwcReadDataFromTCUPort2;
        struct {
            Rte_DE_UInt32 dataInt32;
        } MotorSwcReadSelectDataFromPirteSwcPort4;
    } MotorControlRunnable;
} ImplDE_MotorSWCProto;

const Rte_CDS_MotorSWC MotorSWC_MotorSWCProto = {
    .MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32 = &ImplDE_MotorSWCProto.MotorControlRunnable.MotorSwcReadDataFromPirteSwcPort1.dataInt32,
    .MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer = &ImplDE_MotorSWCProto.MotorControlRunnable.MotorSwcReadDataFromTCUPort2.SpeedSteer,
    .MotorControlRunnable_MotorSwcReadDataFromTCUPort2_dataInt32 = &ImplDE_MotorSWCProto.MotorControlRunnable.MotorSwcReadDataFromTCUPort2.dataInt32,
    .MotorControlRunnable_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32 = &ImplDE_MotorSWCProto.MotorControlRunnable.MotorSwcReadSelectDataFromPirteSwcPort4.dataInt32
};

const Rte_Instance Rte_Inst_MotorSWC = &MotorSWC_MotorSWCProto;

/** === Runnables =================================================================================
 */

/** ------ MotorSWCProto -----------------------------------------------------------------------
 */

void Rte_MotorSWCProto_MotorControlRunnable(void) {
    /* PRE */
    Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromPirteSwcPort1_dataInt32(
            &ImplDE_MotorSWCProto.MotorControlRunnable.MotorSwcReadDataFromPirteSwcPort1.dataInt32.value);

    Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromTCUPort2_SpeedSteer(
            ImplDE_MotorSWCProto.MotorControlRunnable.MotorSwcReadDataFromTCUPort2.SpeedSteer.value);

//    Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32(
//            &ImplDE_MotorSWCProto.MotorControlRunnable.MotorSwcReadSelectDataFromPirteSwcPort4.dataInt32.value);

    /* MAIN */

    MotorControlRunnable();

    /* POST */

}

