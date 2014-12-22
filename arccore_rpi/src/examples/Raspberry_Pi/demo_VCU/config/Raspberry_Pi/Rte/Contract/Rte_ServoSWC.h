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
#ifndef RTE_SERVOSWC_H_
#define RTE_SERVOSWC_H_

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
#if defined(RTE_RUNNABLEAPI_ServoControlRunnable)
#define RTE_RUNNABLEAPI
#endif

/** --- Includes ----------------------------------------------------------------------------------
 * @req SWS_Rte_02751
 * @req SWS_Rte_07131
 */
#include <Rte_DataHandleType.h>
#include <Rte_ServoSWC_Type.h>

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

extern Std_ReturnType Rte_Call_ServoSWC_ServoSWCProto_ServoSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle);

/** @req SWS_Rte_07132
 *  @req SWS_Rte_03714 */
typedef struct {
    Rte_DE_UInt32 * const ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32;
    Rte_DE_Array2 * const ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer;
    Rte_DE_UInt32 * const ServoControlRunnable_ServoSwcReadDataFromTCUPort2_dataInt32;
    Rte_DE_UInt32 * const ServoControlRunnable_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32;
} Rte_CDS_ServoSWC;

/** --- Instance handle type ---------------------------------------------------------------------- */
typedef Rte_CDS_ServoSWC const * const Rte_Instance;

/** --- Singleton instance handle -----------------------------------------------------------------
 *  @req SWS_Rte_03793
 */
extern const Rte_Instance Rte_Inst_ServoSWC;
#define self (Rte_Inst_ServoSWC)

/** --- Calibration API --------------------------------------------------------------------------- */

/** --- Per Instance Memory API ------------------------------------------------------------------- */

/** --- Single Runnable APIs ---------------------------------------------------------------------- */
#if defined(RTE_RUNNABLEAPI)
/** --- ServoControlRunnable */
#if defined(RTE_RUNNABLEAPI_ServoControlRunnable)

void ServoControlRunnable(void);

static inline UInt32 Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    return self->ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32->value;
}

static inline UInt8 * Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer(void) {
    return self->ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer->value;
}

static inline UInt32 Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromTCUPort2_dataInt32(void) {
    return self->ServoControlRunnable_ServoSwcReadDataFromTCUPort2_dataInt32->value;
}

static inline UInt32 Rte_IRead_ServoControlRunnable_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32(void) {
    return self->ServoControlRunnable_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32->value;
}

static inline Std_ReturnType Rte_Call_ServoSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_Call_ServoSWC_ServoSWCProto_ServoSwcWriteDataToIoHwPwmPort3_Write(DutyCycle);
}

#endif
#endif

/** --- All Runnable APIs ------------------------------------------------------------------------- */
#if !defined(RTE_RUNNABLEAPI)
void ServoControlRunnable(void);

static inline UInt32 Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    return self->ServoControlRunnable_ServoSwcReadDataFromPirteSwcPort1_dataInt32->value;
}

static inline UInt8 * Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer(void) {
    return self->ServoControlRunnable_ServoSwcReadDataFromTCUPort2_SpeedSteer->value;
}

static inline UInt32 Rte_IRead_ServoControlRunnable_ServoSwcReadDataFromTCUPort2_dataInt32(void) {
    return self->ServoControlRunnable_ServoSwcReadDataFromTCUPort2_dataInt32->value;
}

static inline UInt32 Rte_IRead_ServoControlRunnable_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32(void) {
    return self->ServoControlRunnable_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32->value;
}

static inline Std_ReturnType Rte_Call_ServoSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_Call_ServoSWC_ServoSWCProto_ServoSwcWriteDataToIoHwPwmPort3_Write(DutyCycle);
}
#endif

/** === FOOTER ====================================================================================
 */

#endif /* RTE_SERVOSWC_H_ */

/** @req SWS_Rte_03710 */
#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */
