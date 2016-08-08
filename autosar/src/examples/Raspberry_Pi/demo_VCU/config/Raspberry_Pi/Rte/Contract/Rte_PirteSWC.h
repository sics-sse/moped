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
#if defined(RTE_RUNNABLEAPI_Pirte_ReadInstallationDataFromTCU_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_WriteAcknowledgementDataToTCU_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadCommunicationDataFromTCU_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_WriteCommunicationDataFromTCU_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_WriteDataToMotor_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_WriteDataToServo_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromAdc_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromFrontWheel_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromRearWheel_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromSCU_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_WriteDataToSCU_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadPositionData_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_WriteLedData_Runnable) || \
defined(RTE_RUNNABLEAPI_Pirte_ReadSpeedSteerData_Runnable)
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
    Rte_DE_UInt32 * const Pirte_WriteLedData_Runnable_PirteSwcWriteLedPort13_dataInt32;
    Rte_DE_Array8 * const Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU;
    Rte_DE_UInt32 * const Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_argInt32;
    Rte_DE_UInt8 * const Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU;
    Rte_DE_Array8 * const Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU;
    Rte_DE_UInt8 * const Pirte_WriteAcknowledgementDataToTCU_Runnable_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU;
    Rte_DE_UInt32 * const Pirte_WriteDataToMotor_Runnable_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32;
    Rte_DE_UInt32 * const Pirte_WriteDataToServo_Runnable_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32;
    Rte_DE_UInt32 * const Pirte_WriteDataToMotor_Runnable_PirteSwcWriteDataToMotorSwcPort3_dataInt32;
    Rte_DE_UInt32 * const Pirte_WriteDataToServo_Runnable_PirteSwcWriteDataToServoSwcPort4_dataInt32;
    Rte_DE_UInt32 * const Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_argInt32;
    Rte_DE_Array8 * const Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU;
    Rte_DE_UInt32 * const Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32;
    Rte_DE_UInt32 * const Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32;
    Rte_DE_UInt32 * const Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32;
    Rte_DE_UInt32 * const Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_argInt32;
    Rte_DE_Array8 * const Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU;
    Rte_DE_UInt32 * const Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_argInt32;
    Rte_DE_Array8 * const Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU;
    Rte_DE_Array8 * const Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_PositionData;
    Rte_DE_UInt32 * const Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_argInt32;
    Rte_DE_Array2 * const Pirte_ReadSpeedSteerData_Runnable_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer;
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
/** --- Pirte_ReadInstallationDataFromTCU_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadInstallationDataFromTCU_Runnable)

void Pirte_ReadInstallationDataFromTCU_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU(void) {
    return self->Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_argInt32(void) {
    return self->Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_argInt32->value;
}

#endif

/** --- Pirte_WriteAcknowledgementDataToTCU_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_WriteAcknowledgementDataToTCU_Runnable)

void Pirte_WriteAcknowledgementDataToTCU_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteAcknowledgementDataToTCU_Runnable_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU(/*IN*/UInt8 value) {
    self->Pirte_WriteAcknowledgementDataToTCU_Runnable_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU->value = value;
}

#endif

/** --- Pirte_ReadCommunicationDataFromTCU_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadCommunicationDataFromTCU_Runnable)

void Pirte_ReadCommunicationDataFromTCU_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU(void) {
    return self->Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_argInt32(void) {
    return self->Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_argInt32->value;
}

#endif

/** --- Pirte_WriteCommunicationDataFromTCU_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_WriteCommunicationDataFromTCU_Runnable)

void Pirte_WriteCommunicationDataFromTCU_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU(/*IN*/UInt8 value) {
    self->Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU->value = value;
}

static inline void Rte_IWrite_Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU(/*IN*/UInt8 const * value) {
    memcpy(self->Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU->value, value, sizeof(Array8));
}

#endif

/** --- Pirte_WriteDataToMotor_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_WriteDataToMotor_Runnable)

void Pirte_WriteDataToMotor_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteDataToMotor_Runnable_PirteSwcWriteDataToMotorSwcPort3_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToMotor_Runnable_PirteSwcWriteDataToMotorSwcPort3_dataInt32->value = value;
}

static inline void Rte_IWrite_Pirte_WriteDataToMotor_Runnable_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToMotor_Runnable_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32->value = value;
}

#endif

/** --- Pirte_WriteDataToServo_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_WriteDataToServo_Runnable)

void Pirte_WriteDataToServo_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteDataToServo_Runnable_PirteSwcWriteDataToServoSwcPort4_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToServo_Runnable_PirteSwcWriteDataToServoSwcPort4_dataInt32->value = value;
}

static inline void Rte_IWrite_Pirte_WriteDataToServo_Runnable_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToServo_Runnable_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32->value = value;
}

#endif

/** --- Pirte_ReadDataFromAdc_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromAdc_Runnable)

void Pirte_ReadDataFromAdc_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32(void) {
    return self->Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32->value;
}

#endif

/** --- Pirte_ReadDataFromFrontWheel_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromFrontWheel_Runnable)

void Pirte_ReadDataFromFrontWheel_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32(void) {
    return self->Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32->value;
}

#endif

/** --- Pirte_ReadDataFromRearWheel_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromRearWheel_Runnable)

void Pirte_ReadDataFromRearWheel_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32(void) {
    return self->Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32->value;
}

#endif

/** --- Pirte_ReadDataFromSCU_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadDataFromSCU_Runnable)

void Pirte_ReadDataFromSCU_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU(void) {
    return self->Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_argInt32(void) {
    return self->Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_argInt32->value;
}

#endif

/** --- Pirte_WriteDataToSCU_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_WriteDataToSCU_Runnable)

void Pirte_WriteDataToSCU_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(/*IN*/UInt8 const * value) {
    memcpy(self->Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU->value, value, sizeof(Array8));
}

static inline void Rte_IWrite_Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_argInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_argInt32->value = value;
}

#endif

/** --- Pirte_ReadPositionData_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadPositionData_Runnable)

void Pirte_ReadPositionData_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_PositionData(void) {
    return self->Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_PositionData->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_argInt32(void) {
    return self->Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_argInt32->value;
}

#endif

/** --- Pirte_WriteLedData_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_WriteLedData_Runnable)

void Pirte_WriteLedData_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteLedData_Runnable_PirteSwcWriteLedPort13_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteLedData_Runnable_PirteSwcWriteLedPort13_dataInt32->value = value;
}

#endif

/** --- Pirte_ReadSpeedSteerData_Runnable */
#if defined(RTE_RUNNABLEAPI_Pirte_ReadSpeedSteerData_Runnable)

void Pirte_ReadSpeedSteerData_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadSpeedSteerData_Runnable_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer(void) {
    return self->Pirte_ReadSpeedSteerData_Runnable_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer->value;
}

#endif
#endif

/** --- All Runnable APIs ------------------------------------------------------------------------- */
#if !defined(RTE_RUNNABLEAPI)
void Pirte_ReadInstallationDataFromTCU_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU(void) {
    return self->Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_argInt32(void) {
    return self->Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_argInt32->value;
}

void Pirte_WriteAcknowledgementDataToTCU_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteAcknowledgementDataToTCU_Runnable_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU(
        /*IN*/UInt8 value) {
    self->Pirte_WriteAcknowledgementDataToTCU_Runnable_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU->value = value;
}

void Pirte_ReadCommunicationDataFromTCU_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU(
        void) {
    return self->Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_argInt32(void) {
    return self->Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_argInt32->value;
}

void Pirte_WriteCommunicationDataFromTCU_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU(
        /*IN*/UInt8 value) {
    self->Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU->value = value;
}

static inline void Rte_IWrite_Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU(
        /*IN*/UInt8 const * value) {
    memcpy(self->Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU->value, value,
            sizeof(Array8));
}

void Pirte_WriteDataToMotor_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteDataToMotor_Runnable_PirteSwcWriteDataToMotorSwcPort3_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToMotor_Runnable_PirteSwcWriteDataToMotorSwcPort3_dataInt32->value = value;
}

static inline void Rte_IWrite_Pirte_WriteDataToMotor_Runnable_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToMotor_Runnable_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32->value = value;
}

void Pirte_WriteDataToServo_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteDataToServo_Runnable_PirteSwcWriteDataToServoSwcPort4_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToServo_Runnable_PirteSwcWriteDataToServoSwcPort4_dataInt32->value = value;
}

static inline void Rte_IWrite_Pirte_WriteDataToServo_Runnable_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToServo_Runnable_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32->value = value;
}

void Pirte_ReadDataFromAdc_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32(void) {
    return self->Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32->value;
}

void Pirte_ReadDataFromFrontWheel_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32(void) {
    return self->Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32->value;
}

void Pirte_ReadDataFromRearWheel_Runnable(void);

static inline UInt32 Rte_IRead_Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32(void) {
    return self->Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32->value;
}

void Pirte_ReadDataFromSCU_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU(void) {
  //	printf("value on port from SCU: %d\r\n", *(self->Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU->value));
	return self->Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_argInt32(void) {
    return self->Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_argInt32->value;
}

void Pirte_WriteDataToSCU_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(/*IN*/UInt8 const * value) {
    memcpy(self->Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU->value, value, sizeof(Array8));
}

static inline void Rte_IWrite_Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_argInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_argInt32->value = value;
}

void Pirte_ReadPositionData_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_PositionData(void) {
    return self->Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_PositionData->value;
}

static inline UInt32 Rte_IRead_Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_argInt32(void) {
    return self->Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_argInt32->value;
}

void Pirte_WriteLedData_Runnable(void);

static inline void Rte_IWrite_Pirte_WriteLedData_Runnable_PirteSwcWriteLedPort13_dataInt32(/*IN*/UInt32 value) {
    self->Pirte_WriteLedData_Runnable_PirteSwcWriteLedPort13_dataInt32->value = value;
}

void Pirte_ReadSpeedSteerData_Runnable(void);

static inline UInt8 * Rte_IRead_Pirte_ReadSpeedSteerData_Runnable_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer(void) {
    return self->Pirte_ReadSpeedSteerData_Runnable_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer->value;
}

#endif

/** === FOOTER ====================================================================================
 */

#endif /* RTE_PIRTESWC_H_ */

/** @req SWS_Rte_03710 */
#ifdef __cplusplus
} /* extern "C" */
#endif /* __cplusplus */
