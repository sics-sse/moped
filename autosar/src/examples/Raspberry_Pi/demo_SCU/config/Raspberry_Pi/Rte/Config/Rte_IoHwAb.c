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

#include "Rte_IoHwAb.h"

/** === Component Data Structures =================================================================
 */

/** --------- IoHwAbService --------------------------------------------------------------------
 */

const Rte_CDS_IoHwAb IoHwAb_IoHwAbService = {
    ._dummy = 0
};

const Rte_Instance Rte_Inst_IoHwAb = &IoHwAb_IoHwAbService;

/** === Runnables =================================================================================
 */

/** ------ IoHwAbService -----------------------------------------------------------------------
 */

Std_ReturnType Rte_IoHwAbService_ImuSensorRead(/*IN*/UInt16 portDefArg1, /*IN*/UInt16 const * Data) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = ImuSensorRead(portDefArg1, Data);

    /* POST */

    return retVal;
}

Std_ReturnType Rte_IoHwAbService_UltraSensorRead(/*IN*/UInt32 portDefArg1, /*IN*/UInt32 const * Data) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = UltraSensorRead(portDefArg1, Data);

    /* POST */

    return retVal;
}

