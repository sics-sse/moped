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

#include "Rte_ServoSWC.h"

/** === Component Data Structures =================================================================
 */

/** --------- ServoSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } ServoSwcReadDataFromPirteSwcPort1;
        struct {
            Rte_DE_Array2 SpeedSteer;
            Rte_DE_UInt32 dataInt32;
        } ServoSwcReadDataFromTCUPort2;
        struct {
            Rte_DE_UInt32 dataInt32;
        } ServoSwcReadSelectDataFromPirteSwcPort4;
    } ServoControlRunnable;
} ImplDE_ServoSWCProto;

const Rte_CDS_ServoSWC ServoSWC_ServoSWCProto = {
    .ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32 = &ImplDE_ServoSWCProto.ServoControlRunnable.ServoSwcReadDataFromPirteSwcPort1.dataInt32,
    .ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer = &ImplDE_ServoSWCProto.ServoControlRunnable.ServoSwcReadDataFromTCUPort2.SpeedSteer,
    .ServoControlRunnable_ServoSwcReadDataFromTCUPort2_dataInt32 = &ImplDE_ServoSWCProto.ServoControlRunnable.ServoSwcReadDataFromTCUPort2.dataInt32,
    .ServoControlRunnable_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32 = &ImplDE_ServoSWCProto.ServoControlRunnable.ServoSwcReadSelectDataFromPirteSwcPort4.dataInt32
};

const Rte_Instance Rte_Inst_ServoSWC = &ServoSWC_ServoSWCProto;

/** === Runnables =================================================================================
 */

/** ------ ServoSWCProto -----------------------------------------------------------------------
 */

void Rte_ServoSWCProto_ServoControlRunnable(void) {
    /* PRE */
    Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromPirteSwcPort1_dataInt32(
            &ImplDE_ServoSWCProto.ServoControlRunnable.ServoSwcReadDataFromPirteSwcPort1.dataInt32.value);

    Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromTCUPort2_SpeedSteer(
            ImplDE_ServoSWCProto.ServoControlRunnable.ServoSwcReadDataFromTCUPort2.SpeedSteer.value);

//    Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32(
//            &ImplDE_ServoSWCProto.ServoControlRunnable.ServoSwcReadSelectDataFromPirteSwcPort4.dataInt32.value);

    /* MAIN */

    ServoControlRunnable();

    /* POST */

}

