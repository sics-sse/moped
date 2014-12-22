/**
 * Application Header File
 *
 * @req SWS_Rte_01003
 */

/** === HEADER ====================================================================================
 */

/** --- C++ guard ---------------------------------------------------------------------------------
 * @req SWS_Rte_03709
 */
#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */

/** --- Normal include guard ----------------------------------------------------------------------
 */
#ifndef RTE_MOTORSWC_H_
#define RTE_MOTORSWC_H_

/** --- Duplicate application include guard -------------------------------------------------------
 * @req SWS_Rte_01006
 */
#ifdef RTE_APPLICATION_HEADER_FILE
#error Multiple application header files included.
#endif
#define RTE_APPLICATION_HEADER_FILE

/** --- Single runnable API -----------------------------------------------------------------------
 * @req SWS_Rte_02751
 */
#if defined(RTE_RUNNABLEAPI_MotorControlRunnable)
#define RTE_RUNNABLEAPI
#endif

/** --- Includes ----------------------------------------------------------------------------------
 * @req SWS_Rte_02751
 * @req SWS_Rte_07131
 */
#include <Rte_DataHandleType.h>
#include <Rte_MotorSWC_Type.h>

/** --- PIM DATA TYPES ------------------------------------------------------------------------------ */

/** === BODY ======================================================================================
 */

/** @req SWS_Rte_03731
 *  @req SWS_Rte_07137
 *  @req SWS_Rte_07138
 *  !req SWS_Rte_06523
 *  @req SWS_Rte_03730
 *  @req SWS_Rte_07677
 *  @req SWS_Rte_02620
 *  @req SWS_Rte_02621
 *  @req SWS_Rte_01055
 *  @req SWS_Rte_03726 */

/** @req SWS_Rte_01343
 *  @req SWS_Rte_01342
 *  !req SWS_Rte_06524
 *  @req SWS_Rte_01053
 */

extern Std_ReturnType Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle);

/** @req SWS_Rte_07132
 *  @req SWS_Rte_03714 */
typedef struct {
    Rte_DE_UInt32 * const MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32;
    Rte_DE_Array2 * const MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer;
    Rte_DE_UInt32 * const MotorControlRunnable_MotorSwcReadDataFromTCUPort2_dataInt32;
    Rte_DE_UInt32 * const MotorControlRunnable_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32;
} Rte_CDS_MotorSWC;

/** --- Instance handle type ---------------------------------------------------------------------- */
typedef Rte_CDS_MotorSWC const * const Rte_Instance;

/** --- Singleton instance handle -----------------------------------------------------------------
 *  @req SWS_Rte_03793
 */
extern const Rte_Instance Rte_Inst_MotorSWC;
#define self (Rte_Inst_MotorSWC)

/** --- Calibration API --------------------------------------------------------------------------- */

/** --- Per Instance Memory API ------------------------------------------------------------------- */

/** --- Single Runnable APIs ---------------------------------------------------------------------- */
#if defined(RTE_RUNNABLEAPI)
/** --- MotorControlRunnable */
#if defined(RTE_RUNNABLEAPI_MotorControlRunnable)

void MotorControlRunnable(void);

static inline UInt32 Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    return self->MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32->value;
}

static inline UInt8 * Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer(void) {
    return self->MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer->value;
}

static inline UInt32 Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromTCUPort2_dataInt32(void) {
    return self->MotorControlRunnable_MotorSwcReadDataFromTCUPort2_dataInt32->value;
}

static inline UInt32 Rte_IRead_MotorControlRunnable_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32(void) {
    return self->MotorControlRunnable_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32->value;
}

static inline Std_ReturnType Rte_Call_MotorSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(DutyCycle);
}

#endif
#endif

/** --- All Runnable APIs ------------------------------------------------------------------------- */
#if !defined(RTE_RUNNABLEAPI)
void MotorControlRunnable(void);

static inline UInt32 Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    return self->MotorControlRunnable_MotorSwcReadDataFromPirteSwcPort1_dataInt32->value;
}

static inline UInt8 * Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer(void) {
    return self->MotorControlRunnable_MotorSwcReadDataFromTCUPort2_SpeedSteer->value;
}

static inline UInt32 Rte_IRead_MotorControlRunnable_MotorSwcReadDataFromTCUPort2_dataInt32(void) {
    return self->MotorControlRunnable_MotorSwcReadDataFromTCUPort2_dataInt32->value;
}

static inline UInt32 Rte_IRead_MotorControlRunnable_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32(void) {
    return self->MotorControlRunnable_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32->value;
}

static inline Std_ReturnType Rte_Call_MotorSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(DutyCycle);
}
#endif

/** === FOOTER ====================================================================================
 */

#endif /* RTE_MOTORSWC_H_ */

/** @req SWS_Rte_03710 */
#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */
