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
#ifndef RTE_IMUSWC_H_
#define RTE_IMUSWC_H_

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
#if defined(RTE_RUNNABLEAPI_ImuSwcRunnable)
#define RTE_RUNNABLEAPI
#endif

/** --- Includes ----------------------------------------------------------------------------------
 * @req SWS_Rte_02751
 * @req SWS_Rte_07131
 */
#include <Rte_DataHandleType.h>
#include <Rte_ImuSWC_Type.h>

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

extern Std_ReturnType Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwGyroDataInPort3_Read(/*IN*/UInt16 const * Data);
extern Std_ReturnType Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwAcceDataInPort4_Read(/*IN*/UInt16 const * Data);

/** @req SWS_Rte_07132
 *  @req SWS_Rte_03714 */
typedef struct {
    Rte_DE_Array3 * const ImuSwcRunnable_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16;
    Rte_DE_Array3 * const ImuSwcRunnable_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16;
} Rte_CDS_ImuSWC;

/** --- Instance handle type ---------------------------------------------------------------------- */
typedef Rte_CDS_ImuSWC const * const Rte_Instance;

/** --- Singleton instance handle -----------------------------------------------------------------
 *  @req SWS_Rte_03793
 */
extern const Rte_Instance Rte_Inst_ImuSWC;
#define self (Rte_Inst_ImuSWC)

/** --- Calibration API --------------------------------------------------------------------------- */

/** --- Per Instance Memory API ------------------------------------------------------------------- */

/** --- Single Runnable APIs ---------------------------------------------------------------------- */
#if defined(RTE_RUNNABLEAPI)
/** --- ImuSwcRunnable */
#if defined(RTE_RUNNABLEAPI_ImuSwcRunnable)

void ImuSwcRunnable(void);

static inline void Rte_IWrite_ImuSwcRunnable_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16(/*IN*/UInt16 const * value) {
    memcpy(self->ImuSwcRunnable_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16->value, value, sizeof(Array3));
}

static inline void Rte_IWrite_ImuSwcRunnable_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16(/*IN*/UInt16 const * value) {
    memcpy(self->ImuSwcRunnable_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16->value, value, sizeof(Array3));
}

static inline Std_ReturnType Rte_Call_ImuSwcToIoHwGyroDataInPort3_Read(/*IN*/UInt16 const * Data) {
    return Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwGyroDataInPort3_Read(Data);
}

static inline Std_ReturnType Rte_Call_ImuSwcToIoHwAcceDataInPort4_Read(/*IN*/UInt16 const * Data) {
    return Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwAcceDataInPort4_Read(Data);
}

#endif
#endif

/** --- All Runnable APIs ------------------------------------------------------------------------- */
#if !defined(RTE_RUNNABLEAPI)
void ImuSwcRunnable(void);

static inline void Rte_IWrite_ImuSwcRunnable_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16(/*IN*/UInt16 const * value) {
    memcpy(self->ImuSwcRunnable_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16->value, value, sizeof(Array3));
}

static inline void Rte_IWrite_ImuSwcRunnable_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16(/*IN*/UInt16 const * value) {
    memcpy(self->ImuSwcRunnable_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16->value, value, sizeof(Array3));
}

static inline Std_ReturnType Rte_Call_ImuSwcToIoHwGyroDataInPort3_Read(/*IN*/UInt16 const * Data) {
    return Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwGyroDataInPort3_Read(Data);
}

static inline Std_ReturnType Rte_Call_ImuSwcToIoHwAcceDataInPort4_Read(/*IN*/UInt16 const * Data) {
    return Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwAcceDataInPort4_Read(Data);
}
#endif

/** === FOOTER ====================================================================================
 */

#endif /* RTE_IMUSWC_H_ */

/** @req SWS_Rte_03710 */
#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */
