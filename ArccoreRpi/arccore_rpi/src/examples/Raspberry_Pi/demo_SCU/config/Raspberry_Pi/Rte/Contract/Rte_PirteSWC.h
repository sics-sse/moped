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
#ifndef RTE_PIRTESWC_H_
#define RTE_PIRTESWC_H_

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
#if defined(RTE_RUNNABLEAPI_SCU_Communication_VCU_ReadRunnable) || \
defined(RTE_RUNNABLEAPI_SCU_Communication_VCU_WriteRunnable) || \
defined(RTE_RUNNABLEAPI_SCU_Communication_TCU_ReadRunnable) || \
defined(RTE_RUNNABLEAPI_SCU_Communication_TCU_WriteRunnable) || \
defined(RTE_RUNNABLEAPI_SCU_Installation_TCU_ReadRunnable) || \
defined(RTE_RUNNABLEAPI_SCU_Installation_TCU_AckRunnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadIMUSensor_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadUltrasonicSensor_Runnable)
#define RTE_RUNNABLEAPI
#endif

/** --- Includes ----------------------------------------------------------------------------------
 * @req SWS_Rte_02751
 * @req SWS_Rte_07131
 */
#include <Rte_DataHandleType.h>
#include <Rte_PirteSWC_Type.h>

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

/** @req SWS_Rte_07132
 *  @req SWS_Rte_03714 */
typedef struct {
    Rte_DE_Array3 * const Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuAccePort8_dataUInt16;
    Rte_DE_Array3 * const Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuGyroPort7_dataUInt16;
    Rte_DE_UInt32 * const Pirte_ReadUltrasonicSensor_Runnable_PirteSwcReadDataFromUltraSwcPort9_dataInt32;
    Rte_DE_UInt32 * const SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_argInt32;
    Rte_DE_Array8 * const SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_pluginCommunication;
    Rte_DE_UInt32 * const SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_argInt32;
    Rte_DE_Array8 * const SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_pluginCommunication;
    Rte_DE_UInt32 * const SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_argInt32;
    Rte_DE_Array8 * const SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication;
    Rte_DE_UInt32 * const SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_argInt32;
    Rte_DE_Array8 * const SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication;
    Rte_DE_UInt32 * const SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_argInt32;
    Rte_DE_Array8 * const SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication;
    Rte_DE_UInt8 * const SCU_Installation_TCU_AckRunnable_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU;
} Rte_CDS_PirteSWC;

/** --- Instance handle type ---------------------------------------------------------------------- */
typedef Rte_CDS_PirteSWC const * const Rte_Instance;

/** --- Singleton instance handle -----------------------------------------------------------------
 *  @req SWS_Rte_03793
 */
extern const Rte_Instance Rte_Inst_PirteSWC;
#define self (Rte_Inst_PirteSWC)

/** --- Calibration API --------------------------------------------------------------------------- */

/** --- Per Instance Memory API ------------------------------------------------------------------- */

/** --- Single Runnable APIs ---------------------------------------------------------------------- */
#if defined(RTE_RUNNABLEAPI)
/** --- SCU_Communication_VCU_ReadRunnable */
#if defined(RTE_RUNNABLEAPI_SCU_Communication_VCU_ReadRunnable)

void SCU_Communication_VCU_ReadRunnable(void);

static inline UInt8 * Rte_IRead_SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_pluginCommunication(void) {
    return self->SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_pluginCommunication->value;
}

static inline UInt32 Rte_IRead_SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_argInt32(void) {
    return self->SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_argInt32->value;
}

#endif

/** --- SCU_Communication_VCU_WriteRunnable */
#if defined(RTE_RUNNABLEAPI_SCU_Communication_VCU_WriteRunnable)

void SCU_Communication_VCU_WriteRunnable(void);

static inline void Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication(/*IN*/UInt8 const * value) {
    memcpy(self->SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication->value, value, sizeof(Array8));
}

static inline void Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_argInt32(/*IN*/UInt32 value) {
    self->SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_argInt32->value = value;
}

#endif

/** --- SCU_Communication_TCU_ReadRunnable */
#if defined(RTE_RUNNABLEAPI_SCU_Communication_TCU_ReadRunnable)

void SCU_Communication_TCU_ReadRunnable(void);

static inline UInt8 * Rte_IRead_SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_pluginCommunication(void) {
    return self->SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_pluginCommunication->value;
}

static inline UInt32 Rte_IRead_SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_argInt32(void) {
    return self->SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_argInt32->value;
}

#endif

/** --- SCU_Communication_TCU_WriteRunnable */
#if defined(RTE_RUNNABLEAPI_SCU_Communication_TCU_WriteRunnable)

void SCU_Communication_TCU_WriteRunnable(void);

static inline void Rte_IWrite_SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(/*IN*/UInt8 const * value) {
    memcpy(self->SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication->value, value, sizeof(Array8));
}

static inline void Rte_IWrite_SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_argInt32(/*IN*/UInt32 value) {
    self->SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_argInt32->value = value;
}

#endif

/** --- SCU_Installation_TCU_ReadRunnable */
#if defined(RTE_RUNNABLEAPI_SCU_Installation_TCU_ReadRunnable)

void SCU_Installation_TCU_ReadRunnable(void);

static inline UInt8 * Rte_IRead_SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication(void) {
    return self->SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication->value;
}

static inline UInt32 Rte_IRead_SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_argInt32(void) {
    return self->SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_argInt32->value;
}

#endif

/** --- SCU_Installation_TCU_AckRunnable */
#if defined(RTE_RUNNABLEAPI_SCU_Installation_TCU_AckRunnable)

void SCU_Installation_TCU_AckRunnable(void);

static inline void Rte_IWrite_SCU_Installation_TCU_AckRunnable_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU(/*IN*/UInt8 value) {
    self->SCU_Installation_TCU_AckRunnable_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU->value = value;
}

#endif

/** --- Pirte_ReadIMUSensor_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadIMUSensor_Runnable)

void Pirte_ReadIMUSensor_Runnable(void);

static inline UInt16 * Rte_IRead_Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuGyroPort7_dataUInt16(void) {
    return self->Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuGyroPort7_dataUInt16->value;
}

static inline UInt16 * Rte_IRead_Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuAccePort8_dataUInt16(void) {
    return self->Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuAccePort8_dataUInt16->value;
}

#endif

/** --- Pirte_ReadUltrasonicSensor_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadUltrasonicSensor_Runnable)

void Pirte_ReadUltrasonicSensor_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadUltrasonicSensor_Runnable_PirteSwcReadDataFromUltraSwcPort9_dataInt32(void) {
    return self->Pirte_ReadUltrasonicSensor_Runnable_PirteSwcReadDataFromUltraSwcPort9_dataInt32->value;
}

#endif
#endif

/** --- All Runnable APIs ------------------------------------------------------------------------- */
#if !defined(RTE_RUNNABLEAPI)
void SCU_Communication_VCU_ReadRunnable(void);

static inline UInt8 * Rte_IRead_SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_pluginCommunication(void) {
    return self->SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_pluginCommunication->value;
}

static inline UInt32 Rte_IRead_SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_argInt32(void) {
    return self->SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_argInt32->value;
}

void SCU_Communication_VCU_WriteRunnable(void);

static inline void Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication(/*IN*/UInt8 const * value) {
    memcpy(self->SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication->value, value, sizeof(Array8));
}

static inline void Rte_IWrite_SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_argInt32(/*IN*/UInt32 value) {
    self->SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_argInt32->value = value;
}

void SCU_Communication_TCU_ReadRunnable(void);

static inline UInt8 * Rte_IRead_SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_pluginCommunication(void) {
    return self->SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_pluginCommunication->value;
}

static inline UInt32 Rte_IRead_SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_argInt32(void) {
    return self->SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_argInt32->value;
}

void SCU_Communication_TCU_WriteRunnable(void);

static inline void Rte_IWrite_SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(/*IN*/UInt8 const * value) {
    memcpy(self->SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication->value, value, sizeof(Array8));
}

static inline void Rte_IWrite_SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_argInt32(/*IN*/UInt32 value) {
    self->SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_argInt32->value = value;
}

void SCU_Installation_TCU_ReadRunnable(void);

static inline UInt8 * Rte_IRead_SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication(void) {
    return self->SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication->value;
}

static inline UInt32 Rte_IRead_SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_argInt32(void) {
    return self->SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_argInt32->value;
}

void SCU_Installation_TCU_AckRunnable(void);

static inline void Rte_IWrite_SCU_Installation_TCU_AckRunnable_PirteSwcWriteAckDataToTCUPort6_acknowledgementTCU(/*IN*/UInt8 value) {
    self->SCU_Installation_TCU_AckRunnable_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU->value = value;
}

void Pirte_ReadIMUSensor_Runnable(void);

static inline UInt16 * Rte_IRead_Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuGyroPort7_dataUInt16(void) {
    return self->Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuGyroPort7_dataUInt16->value;
}

static inline UInt16 * Rte_IRead_Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuAccePort8_dataUInt16(void) {
    return self->Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuAccePort8_dataUInt16->value;
}

void Pirte_ReadUltrasonicSensor_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadUltrasonicSensor_Runnable_PirteSwcReadDataFromUltraSwcPort9_dataInt32(void) {
    return self->Pirte_ReadUltrasonicSensor_Runnable_PirteSwcReadDataFromUltraSwcPort9_dataInt32->value;
}

#endif

/** === FOOTER ====================================================================================
 */

#endif /* RTE_PIRTESWC_H_ */

/** @req SWS_Rte_03710 */
#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */
