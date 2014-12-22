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

/** --------- IoHwAbProto --------------------------------------------------------------------
 */

const Rte_CDS_IoHwAb IoHwAb_IoHwAbProto = {
    ._dummy = 0
};

const Rte_Instance Rte_Inst_IoHwAb = &IoHwAb_IoHwAbProto;

/** === Runnables =================================================================================
 */

/** ------ IoHwAbProto -----------------------------------------------------------------------
 */

Std_ReturnType Rte_IoHwAbProto_IoHw_Read_AdcSensor(/*IN*/UInt32 portDefArg1, /*IN*/UInt32* Data) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = IoHw_Read_AdcSensor(portDefArg1, Data);

    /* POST */

    return retVal;
}

Std_ReturnType Rte_IoHwAbProto_IoHw_Read_FrontWheelSensor(/*IN*/UInt32 portDefArg1, /*IN*/UInt32* Data) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = IoHw_Read_FrontWheelSensor(portDefArg1, Data);

    /* POST */

    return retVal;
}

Std_ReturnType Rte_IoHwAbProto_IoHw_Read_RearWheelSensor(/*IN*/UInt32 portDefArg1, /*IN*/UInt32* Data) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = IoHw_Read_RearWheelSensor(portDefArg1, Data);

    /* POST */

    return retVal;
}

Std_ReturnType Rte_IoHwAbProto_IoHw_Write_Led(/*IN*/UInt32 portDefArg1, /*IN*/UInt8 portDefArg2, /*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = IoHw_Write_Led(portDefArg1, portDefArg2, Pin, Level);

    /* POST */

    return retVal;
}

Std_ReturnType Rte_IoHwAbProto_IoHw_WriteSpeed_DutyCycle(/*IN*/UInt8 portDefArg1, /*OUT*/UInt8 * DutyCycle) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = IoHw_WriteSpeed_DutyCycle(portDefArg1, DutyCycle);

    /* POST */

    return retVal;
}

Std_ReturnType Rte_IoHwAbProto_IoHw_WriteServo_DutyCycle(/*IN*/UInt8 portDefArg1, /*OUT*/UInt8 * DutyCycle) {
    /* PRE */

    /* MAIN */

    Std_ReturnType retVal = IoHw_WriteServo_DutyCycle(portDefArg1, DutyCycle);

    /* POST */

    return retVal;
}

