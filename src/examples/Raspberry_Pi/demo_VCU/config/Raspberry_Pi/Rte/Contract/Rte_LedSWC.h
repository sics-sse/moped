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
#ifndef RTE_LEDSWC_H_
#define RTE_LEDSWC_H_

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
#if defined(RTE_RUNNABLEAPI_LedControlRunnable)
#define RTE_RUNNABLEAPI
#endif

/** --- Includes ----------------------------------------------------------------------------------
 * @req SWS_Rte_02751
 * @req SWS_Rte_07131
 */
#include <Rte_DataHandleType.h>
#include <Rte_LedSWC_Type.h>

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

extern Std_ReturnType Rte_Call_LedSWC_LedSWCProto_LedSwcWriteDataToIoHwLedPort2_PinOpt(/*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level);

/** @req SWS_Rte_07132
 *  @req SWS_Rte_03714 */
typedef struct {
    Rte_DE_UInt32 * const LedControlRunnable_LedSwcReadDataFromPirteSwcPort1_dataInt32;
} Rte_CDS_LedSWC;

/** --- Instance handle type ---------------------------------------------------------------------- */
typedef Rte_CDS_LedSWC const * const Rte_Instance;

/** --- Singleton instance handle -----------------------------------------------------------------
 *  @req SWS_Rte_03793
 */
extern const Rte_Instance Rte_Inst_LedSWC;
#define self (Rte_Inst_LedSWC)

/** --- Calibration API --------------------------------------------------------------------------- */

/** --- Per Instance Memory API ------------------------------------------------------------------- */

/** --- Single Runnable APIs ---------------------------------------------------------------------- */
#if defined(RTE_RUNNABLEAPI)
/** --- LedControlRunnable */
#if defined(RTE_RUNNABLEAPI_LedControlRunnable)

void LedControlRunnable(void);

static inline UInt32 Rte_IRead_LedControlRunnable_LedSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    return self->LedControlRunnable_LedSwcReadDataFromPirteSwcPort1_dataInt32->value;
}

static inline Std_ReturnType Rte_Call_LedSwcWriteDataToIoHwLedPort2_PinOpt(/*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level) {
    return Rte_Call_LedSWC_LedSWCProto_LedSwcWriteDataToIoHwLedPort2_PinOpt(Pin, Level);
}

#endif
#endif

/** --- All Runnable APIs ------------------------------------------------------------------------- */
#if !defined(RTE_RUNNABLEAPI)
void LedControlRunnable(void);

static inline UInt32 Rte_IRead_LedControlRunnable_LedSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    return self->LedControlRunnable_LedSwcReadDataFromPirteSwcPort1_dataInt32->value;
}

static inline Std_ReturnType Rte_Call_LedSwcWriteDataToIoHwLedPort2_PinOpt(/*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level) {
    return Rte_Call_LedSWC_LedSWCProto_LedSwcWriteDataToIoHwLedPort2_PinOpt(Pin, Level);
}
#endif

/** === FOOTER ====================================================================================
 */

#endif /* RTE_LEDSWC_H_ */

/** @req SWS_Rte_03710 */
#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */
