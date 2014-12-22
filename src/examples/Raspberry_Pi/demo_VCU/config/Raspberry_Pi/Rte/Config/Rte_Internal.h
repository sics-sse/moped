#ifndef RTE_INTERNAL_H_
#define RTE_INTERNAL_H_

#include <Rte_DataHandleType.h>

#define Rte_EnterProtectedRegion() 	imask_t state; \
	                                Irq_Save(state)

#define Rte_ExitProtectedRegion()     Irq_Restore(state)

typedef struct {
    struct {
        struct { /* --- AdcSWCProto --- */
            struct {
                UInt32 dataInt32;
            } AdcSwcWriteDataToPirteSwcPort1;
        } AdcSWCProto;
    } AdcSWC;

    struct {
        struct { /* --- FrontWheelSWCProto --- */
            struct {
                UInt32 dataInt32;
            } FrontWheelSwcWriteDataToPirteSwcPort1;
        } FrontWheelSWCProto;
    } FrontWheelSWC;

    struct {
        struct { /* --- MotorSWCProto --- */
            struct {
                Array2 SpeedSteer;
                UInt32 dataInt32;
            } MotorSwcReadDataFromTCUPort2;
        } MotorSWCProto;
    } MotorSWC;

    struct {
        struct { /* --- PirteSWCProto --- */
            struct {
                UInt8 acknowledgementVCU;
                Array8 pluginCommunicationVCUtoTCU;
            } PirteSwcWriteAcknowledgementDataToTCUPort2;
            struct {
                UInt32 dataInt32;
            } PirteSwcWriteDataToMotorSwcPort3;
            struct {
                UInt32 dataInt32;
            } PirteSwcWriteDataToServoSwcPort4;
            struct {
                UInt32 argInt32;
                Array8 pluginCommunicationVCUtoSCU;
            } PirteSwcWriteDataToSCUPort9;
            struct {
                UInt8 acknowledgementVCU;
                Array8 pluginCommunicationVCUtoTCU;
            } PirteSwcWriteCommunicationDataToTCUPort11;
            struct {
                UInt32 dataInt32;
            } PirteSwcWriteLedPort13;
            struct {
                UInt32 dataInt32;
            } PirteSwcWriteSelectSpeedDataToMotorSwcPort15;
            struct {
                UInt32 dataInt32;
            } PirteSwcWriteSelectSteerDataToServoSwcPort16;
            struct {
                UInt32 argInt32;
                Array8 pluginInstallationVCU;
                Array8 pluginCommunicationTCUtoVCU;
            } PirteSwcReadInstallationDataFromTCUPort1;
            struct {
                UInt32 argInt32;
                Array8 pluginCommunicationSCUtoVCU;
            } PirteSwcReadDataFromSCUPort8;
            struct {
                UInt32 argInt32;
                Array8 pluginInstallationVCU;
                Array8 pluginCommunicationTCUtoVCU;
            } PirteSwcReadCommunicationDataFromTCUPort10;
            struct {
                UInt32 argInt32;
                Array8 PositionData;
            } PirteSwcReadPositionDataFromTCUPort12;
        } PirteSWCProto;
    } PirteSWC;

    struct {
        struct { /* --- RearWheelSWCProto --- */
            struct {
                UInt32 dataInt32;
            } RearWheelSwcWriteDataToPirteSwcPort1;
        } RearWheelSWCProto;
    } RearWheelSWC;

} BuffersType;

typedef struct {
    uint8 _dummy;
} RPortStatusesType;

typedef struct {
    uint8 _dummy;
} ModeMachinesType;

typedef struct {
    boolean entered;
} ExclusiveAreaType;

typedef struct {
    uint8 _dummy;
} ExclusiveAreasType;

/** === AdcSWC ======================================================================= */
/** --- AdcSWCProto -------------------------------------------------------------------- */

/** ------ AdcSwcReadDataFromAdcDriverPort2 */
Std_ReturnType Rte_Call_AdcSWC_AdcSWCProto_AdcSwcReadDataFromAdcDriverPort2_Read(/*IN*/UInt32* Data);

/** ------ AdcSwcWriteDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_AdcSWC_AdcSWCProto_AdcSwcWriteDataToPirteSwcPort1_dataInt32(/*IN*/UInt32 data);

/** === FrontWheelSWC ======================================================================= */
/** --- FrontWheelSWCProto -------------------------------------------------------------------- */

/** ------ FrontWheelSwcReadDataFromWheelSensorPort2 */
Std_ReturnType Rte_Call_FrontWheelSWC_FrontWheelSWCProto_FrontWheelSwcReadDataFromWheelSensorPort2_Read(/*IN*/UInt32* Data);

/** ------ FrontWheelSwcWriteDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_FrontWheelSWC_FrontWheelSWCProto_FrontWheelSwcWriteDataToPirteSwcPort1_dataInt32(/*IN*/UInt32 data);

/** === IoHwAb ======================================================================= */
/** --- IoHwAbProto -------------------------------------------------------------------- */

/** ------ IoHwReadAdcDataPort1 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwReadAdcDataPort1_Read(/*IN*/UInt32* Data);

/** ------ IoHwReadFrontWheelDataPort2 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwReadFrontWheelDataPort2_Read(/*IN*/UInt32* Data);

/** ------ IoHwReadRearWheelDataPort3 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwReadRearWheelDataPort3_Read(/*IN*/UInt32* Data);

/** ------ IoHwWriteLedDataPort4 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteLedDataPort4_PinOpt(/*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level);

/** ------ IoHwWriteServoDutyCyclePort6 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteServoDutyCyclePort6_Write(/*OUT*/UInt8 * DutyCycle);

/** ------ IoHwWriteSpeedDutyCyclePort5 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteSpeedDutyCyclePort5_Write(/*OUT*/UInt8 * DutyCycle);

/** === LedSWC ======================================================================= */
/** --- LedSWCProto -------------------------------------------------------------------- */

/** ------ LedSwcReadDataFromPirteSwcPort1 */
Std_ReturnType Rte_Read_LedSWC_LedSWCProto_LedSwcReadDataFromPirteSwcPort1_dataInt32(/*OUT*/UInt32 * data);

/** ------ LedSwcWriteDataToIoHwLedPort2 */
Std_ReturnType Rte_Call_LedSWC_LedSWCProto_LedSwcWriteDataToIoHwLedPort2_PinOpt(/*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level);

/** === MotorSWC ======================================================================= */
/** --- MotorSWCProto -------------------------------------------------------------------- */

/** ------ MotorSwcReadDataFromPirteSwcPort1 */
Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromPirteSwcPort1_dataInt32(/*OUT*/UInt32 * data);

/** ------ MotorSwcReadDataFromTCUPort2 */
Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromTCUPort2_SpeedSteer(/*OUT*/UInt8 * data);
Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromTCUPort2_dataInt32(/*OUT*/UInt32 * data);

/** ------ MotorSwcReadSelectDataFromPirteSwcPort4 */
Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32(/*OUT*/UInt32 * data);

/** ------ MotorSwcWriteDataToIoHwPwmPort3 */
Std_ReturnType Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle);

/** === PirteSWC ======================================================================= */
/** --- PirteSWCProto -------------------------------------------------------------------- */

/** ------ PirteSwcReadCommunicationDataFromTCUPort10 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_argInt32(/*OUT*/UInt32 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_pluginInstallationVCU(/*OUT*/UInt8 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU(/*OUT*/UInt8 * data);

/** ------ PirteSwcReadDataFromAdcSwcPort5 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromAdcSwcPort5_dataInt32(/*OUT*/UInt32 * data);

/** ------ PirteSwcReadDataFromFrontWheelSwcPort6 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32(/*OUT*/UInt32 * data);

/** ------ PirteSwcReadDataFromRearWheelSwcPort7 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32(/*OUT*/UInt32 * data);

/** ------ PirteSwcReadDataFromSCUPort8 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromSCUPort8_argInt32(/*OUT*/UInt32 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU(/*OUT*/UInt8 * data);

/** ------ PirteSwcReadInstallationDataFromTCUPort1 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_argInt32(/*OUT*/UInt32 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU(/*OUT*/UInt8 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_pluginCommunicationTCUtoVCU(/*OUT*/UInt8 * data);

/** ------ PirteSwcReadPositionDataFromTCUPort12 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadPositionDataFromTCUPort12_argInt32(/*OUT*/UInt32 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadPositionDataFromTCUPort12_PositionData(/*OUT*/UInt8 * data);

/** ------ PirteSwcReadSpeedSteerDataFromTCUPort14 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer(/*OUT*/UInt8 * data);

/** ------ PirteSwcWriteAcknowledgementDataToTCUPort2 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU(/*IN*/UInt8 data);
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAcknowledgementDataToTCUPort2_pluginCommunicationVCUtoTCU(/*IN*/UInt8 const * data);

/** ------ PirteSwcWriteCommunicationDataToTCUPort11 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU(/*IN*/UInt8 data);
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU(/*IN*/UInt8 const * data);

/** ------ PirteSwcWriteDataToMotorSwcPort3 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToMotorSwcPort3_dataInt32(/*IN*/UInt32 data);

/** ------ PirteSwcWriteDataToSCUPort9 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToSCUPort9_argInt32(/*IN*/UInt32 data);
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(/*IN*/UInt8 const * data);

/** ------ PirteSwcWriteDataToServoSwcPort4 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToServoSwcPort4_dataInt32(/*IN*/UInt32 data);

/** ------ PirteSwcWriteLedPort13 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteLedPort13_dataInt32(/*IN*/UInt32 data);

/** ------ PirteSwcWriteSelectSpeedDataToMotorSwcPort15 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32(/*IN*/UInt32 data);

/** ------ PirteSwcWriteSelectSteerDataToServoSwcPort16 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32(/*IN*/UInt32 data);

/** === RearWheelSWC ======================================================================= */
/** --- RearWheelSWCProto -------------------------------------------------------------------- */

/** ------ RearWheelSwcReadDataFromWheelSensorPort2 */
Std_ReturnType Rte_Call_RearWheelSWC_RearWheelSWCProto_RearWheelSwcReadDataFromWheelSensorPort2_Read(/*IN*/UInt32* Data);

/** ------ RearWheelSwcWriteDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_RearWheelSWC_RearWheelSWCProto_RearWheelSwcWriteDataToPirteSwcPort1_dataInt32(/*IN*/UInt32 data);

/** === ServoSWC ======================================================================= */
/** --- ServoSWCProto -------------------------------------------------------------------- */

/** ------ ServoSwcReadDataFromPirteSwcPort1 */
Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromPirteSwcPort1_dataInt32(/*OUT*/UInt32 * data);

/** ------ ServoSwcReadDataFromTCUPort2 */
Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromTCUPort2_SpeedSteer(/*OUT*/UInt8 * data);
Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromTCUPort2_dataInt32(/*OUT*/UInt32 * data);

/** ------ ServoSwcReadSelectDataFromPirteSwcPort4 */
Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32(/*OUT*/UInt32 * data);

/** ------ ServoSwcWriteDataToIoHwPwmPort3 */
Std_ReturnType Rte_Call_ServoSWC_ServoSWCProto_ServoSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle);

#endif /* RTE_INTERNAL_H_ */

