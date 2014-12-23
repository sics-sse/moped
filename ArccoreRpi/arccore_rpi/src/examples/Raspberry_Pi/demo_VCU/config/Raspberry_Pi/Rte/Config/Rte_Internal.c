#include <Rte_Internal.h>
#include <Rte_Callbacks.h>
#include <Rte_Calprms.h>
#include <Rte_Assert.h>
#include <Com.h>
#include <Os.h>
#include <Ioc.h>

BuffersType Buffers;
RPortStatusesType RPortStatuses;
ModeMachinesType ModeMachines;
ExclusiveAreasType ExclusiveAreas;
boolean RteInitialized = false;

/** --- Per Instance Memories ---------------------------------------------------------------------- */

extern Std_ReturnType Rte_IoHwAbProto_IoHw_Read_AdcSensor(/*IN*/UInt32 portDefArg1, /*IN*/UInt32* Data);
extern Std_ReturnType Rte_IoHwAbProto_IoHw_Read_FrontWheelSensor(/*IN*/UInt32 portDefArg1, /*IN*/UInt32* Data);
extern Std_ReturnType Rte_IoHwAbProto_IoHw_Read_RearWheelSensor(/*IN*/UInt32 portDefArg1, /*IN*/UInt32* Data);
extern Std_ReturnType Rte_IoHwAbProto_IoHw_Write_Led(/*IN*/UInt32 portDefArg1, /*IN*/UInt8 portDefArg2, /*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level);
extern Std_ReturnType Rte_IoHwAbProto_IoHw_WriteSpeed_DutyCycle(/*IN*/UInt8 portDefArg1, /*OUT*/UInt8 * DutyCycle);
extern Std_ReturnType Rte_IoHwAbProto_IoHw_WriteServo_DutyCycle(/*IN*/UInt8 portDefArg1, /*OUT*/UInt8 * DutyCycle);

/** === AdcSWC ======================================================================= */
/** --- AdcSWCProto -------------------------------------------------------------------- */

/** ------ AdcSwcReadDataFromAdcDriverPort2 */
Std_ReturnType Rte_Call_AdcSWC_AdcSWCProto_AdcSwcReadDataFromAdcDriverPort2_Read(/*IN*/UInt32* Data) {
    return Rte_Call_IoHwAb_IoHwAbProto_IoHwReadAdcDataPort1_Read(Data);
}

/** ------ AdcSwcWriteDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_AdcSWC_AdcSWCProto_AdcSwcWriteDataToPirteSwcPort1_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (AdcSWCProto_AdcSwcWriteDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromAdcSwcPort5) */
    {
        Rte_EnterProtectedRegion();
        Buffers.AdcSWC.AdcSWCProto.AdcSwcWriteDataToPirteSwcPort1.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromAdcSwcPort5_dataInt32();
    return RTE_E_OK;
}

/** === FrontWheelSWC ======================================================================= */
/** --- FrontWheelSWCProto -------------------------------------------------------------------- */

/** ------ FrontWheelSwcReadDataFromWheelSensorPort2 */
Std_ReturnType Rte_Call_FrontWheelSWC_FrontWheelSWCProto_FrontWheelSwcReadDataFromWheelSensorPort2_Read(/*IN*/UInt32* Data) {
    return Rte_Call_IoHwAb_IoHwAbProto_IoHwReadFrontWheelDataPort2_Read(Data);
}

/** ------ FrontWheelSwcWriteDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_FrontWheelSWC_FrontWheelSWCProto_FrontWheelSwcWriteDataToPirteSwcPort1_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (FrontWheelSWCProto_FrontWheelSwcWriteDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromFrontWheelSwcPort6) */
    {
        Rte_EnterProtectedRegion();
        Buffers.FrontWheelSWC.FrontWheelSWCProto.FrontWheelSwcWriteDataToPirteSwcPort1.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32();
    return RTE_E_OK;
}

/** === IoHwAb ======================================================================= */
/** --- IoHwAbProto -------------------------------------------------------------------- */

/** ------ IoHwReadAdcDataPort1 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwReadAdcDataPort1_Read(/*IN*/UInt32* Data) {
    return Rte_IoHwAbProto_IoHw_Read_AdcSensor(0, Data);
}

/** ------ IoHwReadFrontWheelDataPort2 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwReadFrontWheelDataPort2_Read(/*IN*/UInt32* Data) {
    return Rte_IoHwAbProto_IoHw_Read_FrontWheelSensor(0, Data);
}

/** ------ IoHwReadRearWheelDataPort3 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwReadRearWheelDataPort3_Read(/*IN*/UInt32* Data) {
    return Rte_IoHwAbProto_IoHw_Read_RearWheelSensor(0, Data);
}

/** ------ IoHwWriteLedDataPort4 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteLedDataPort4_PinOpt(/*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level) {
    return Rte_IoHwAbProto_IoHw_Write_Led(0, 0, Pin, Level);
}

/** ------ IoHwWriteServoDutyCyclePort6 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteServoDutyCyclePort6_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_IoHwAbProto_IoHw_WriteServo_DutyCycle(0, DutyCycle);
}

/** ------ IoHwWriteSpeedDutyCyclePort5 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteSpeedDutyCyclePort5_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_IoHwAbProto_IoHw_WriteSpeed_DutyCycle(0, DutyCycle);
}

/** === LedSWC ======================================================================= */
/** --- LedSWCProto -------------------------------------------------------------------- */

/** ------ LedSwcReadDataFromPirteSwcPort1 */
Std_ReturnType Rte_Read_LedSWC_LedSWCProto_LedSwcReadDataFromPirteSwcPort1_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (PirteSWCProto_PirteSwcWriteLedPort13_to_LedSWCProto_LedSwcReadDataFromPirteSwcPort1) */
    {
        Rte_EnterProtectedRegion();
        *value = Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteLedPort13.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ LedSwcWriteDataToIoHwLedPort2 */
Std_ReturnType Rte_Call_LedSWC_LedSWCProto_LedSwcWriteDataToIoHwLedPort2_PinOpt(/*OUT*/UInt32 * Pin, /*OUT*/UInt8 * Level) {
    return Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteLedDataPort4_PinOpt(Pin, Level);
}

/** === MotorSWC ======================================================================= */
/** --- MotorSWCProto -------------------------------------------------------------------- */

/** ------ MotorSwcReadDataFromPirteSwcPort1 */
Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromPirteSwcPort1_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (PirteSWCProto_PirteSwcWriteDataToMotorSwcPort3_to_MotorSWCProto_MotorSwcReadDataFromPirteSwcPort1) */
    {
        Rte_EnterProtectedRegion();
        *value = Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteDataToMotorSwcPort3.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ MotorSwcReadDataFromTCUPort2 */
Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromTCUPort2_SpeedSteer(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_DRIVERSETSPEEDSTEERINGSIGNAL, value);
    return status;
}

Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadDataFromTCUPort2_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (driverSetSpeedSteeringSignaldataInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_DRIVERSETSPEEDSTEERINGSIGNAL, value);
    /* --- Receiver (driverSetSpeedSteeringSignaldataInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_DRIVERSETSPEEDSTEERINGSIGNAL, value);

    return status;
}

/** ------ MotorSwcReadSelectDataFromPirteSwcPort4 */
Std_ReturnType Rte_Read_MotorSWC_MotorSWCProto_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (PirteSWCProto_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_to_MotorSWCProto_MotorSwcReadSelectDataFromPirteSwcPort4) */
    {
        Rte_EnterProtectedRegion();
//        *value = Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteSelectSpeedDataToMotorSwcPort15.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ MotorSwcWriteDataToIoHwPwmPort3 */
Std_ReturnType Rte_Call_MotorSWC_MotorSWCProto_MotorSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteSpeedDutyCyclePort5_Write(DutyCycle);
}

/** === PirteSWC ======================================================================= */
/** --- PirteSWCProto -------------------------------------------------------------------- */

/** ------ PirteSwcReadCommunicationDataFromTCUPort10 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_argInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (pluginCommunicationTCUtoVCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOVCUSIGNAL, value);

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_pluginInstallationVCU(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOVCUSIGNAL, value);
    return status;
}

/** ------ PirteSwcReadDataFromAdcSwcPort5 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromAdcSwcPort5_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (AdcSWCProto_AdcSwcWriteDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromAdcSwcPort5) */
    {
        Rte_EnterProtectedRegion();
        *value = Buffers.AdcSWC.AdcSWCProto.AdcSwcWriteDataToPirteSwcPort1.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ PirteSwcReadDataFromFrontWheelSwcPort6 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (FrontWheelSWCProto_FrontWheelSwcWriteDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromFrontWheelSwcPort6) */
    {
        Rte_EnterProtectedRegion();
        *value = Buffers.FrontWheelSWC.FrontWheelSWCProto.FrontWheelSwcWriteDataToPirteSwcPort1.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ PirteSwcReadDataFromRearWheelSwcPort7 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (RearWheelSWCProto_RearWheelSwcWriteDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromRearWheelSwcPort7) */
    {
        Rte_EnterProtectedRegion();
        *value = Buffers.RearWheelSWC.RearWheelSWCProto.RearWheelSwcWriteDataToPirteSwcPort1.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ PirteSwcReadDataFromSCUPort8 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromSCUPort8_argInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (pluginCommunicationSCUtoVCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL, value);

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL, value);
    return status;
}

/** ------ PirteSwcReadInstallationDataFromTCUPort1 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_argInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (pluginInstallationVCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGININSTALLATIONVCUSIGNAL, value);

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGININSTALLATIONVCUSIGNAL, value);
    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_pluginCommunicationTCUtoVCU(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;

    return status;
}

/** ------ PirteSwcReadPositionDataFromTCUPort12 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadPositionDataFromTCUPort12_argInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (positionSignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_POSITIONDATASIGNAL, value);

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadPositionDataFromTCUPort12_PositionData(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_POSITIONDATASIGNAL, value);
    return status;
}

/** ------ PirteSwcReadSpeedSteerDataFromTCUPort14 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    /* --- Unconnected */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_DRIVERSETSPEEDSTEERINGSIGNAL, value);
    return status;
}

/** ------ PirteSwcWriteAcknowledgementDataToTCUPort2 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU(/*IN*/UInt8 value) {

    /* --- Sender (acknowledgementVCUsignalacknowledgementVCUISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    Com_SendSignal(COM_SIGNAL_ID_ACKNOWLEDGEMENTVCUSIGNAL, &value);

    return RTE_E_OK;
}

Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAcknowledgementDataToTCUPort2_pluginCommunicationVCUtoTCU(/*IN*/UInt8 const * value) {

    return RTE_E_OK;
}

/** ------ PirteSwcWriteCommunicationDataToTCUPort11 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU(/*IN*/UInt8 value) {

    /* --- Sender (pluginCommunicationVCUtoTCUsignalacknowledgementVCUISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOTCUSIGNAL, &value);

    return RTE_E_OK;
}

Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU(/*IN*/UInt8 const * value) {

	Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOTCUSIGNAL, value);
    return RTE_E_OK;
}

/** ------ PirteSwcWriteDataToMotorSwcPort3 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToMotorSwcPort3_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (PirteSWCProto_PirteSwcWriteDataToMotorSwcPort3_to_MotorSWCProto_MotorSwcReadDataFromPirteSwcPort1) */
    {
        Rte_EnterProtectedRegion();
        Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteDataToMotorSwcPort3.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_MotorSWC_MotorSWCProto_MotorSwcReadDataFromPirteSwcPort1_dataInt32();
    return RTE_E_OK;
}

/** ------ PirteSwcWriteDataToSCUPort9 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToSCUPort9_argInt32(/*IN*/UInt32 value) {

    /* --- Sender (pluginCommunicationVCUtoSCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL, &value);

    return RTE_E_OK;
}

Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(/*IN*/UInt8 const * value) {

	Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL, value);
    return RTE_E_OK;
}

/** ------ PirteSwcWriteDataToServoSwcPort4 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToServoSwcPort4_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (PirteSWCProto_PirteSwcWriteDataToServoSwcPort4_to_ServoSWCProto_ServoSwcReadDataFromPirteSwcPort1) */
    {
        Rte_EnterProtectedRegion();
        Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteDataToServoSwcPort4.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_ServoSWC_ServoSWCProto_ServoSwcReadDataFromPirteSwcPort1_dataInt32();
    return RTE_E_OK;
}

/** ------ PirteSwcWriteLedPort13 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteLedPort13_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (PirteSWCProto_PirteSwcWriteLedPort13_to_LedSWCProto_LedSwcReadDataFromPirteSwcPort1) */
    {
        Rte_EnterProtectedRegion();
        Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteLedPort13.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_LedSWC_LedSWCProto_LedSwcReadDataFromPirteSwcPort1_dataInt32();
    return RTE_E_OK;
}

/** ------ PirteSwcWriteSelectSpeedDataToMotorSwcPort15 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (PirteSWCProto_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_to_MotorSWCProto_MotorSwcReadSelectDataFromPirteSwcPort4) */
    {
        Rte_EnterProtectedRegion();
//        Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteSelectSpeedDataToMotorSwcPort15.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

//    Rte_DataReceived_MotorSWC_MotorSWCProto_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32();
    return RTE_E_OK;
}

/** ------ PirteSwcWriteSelectSteerDataToServoSwcPort16 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (PirteSWCProto_PirteSwcWriteSelectSteerDataToServoSwcPort16_to_ServoSWCProto_ServoSwcReadSelectDataFromPirteSwcPort4) */
    {
        Rte_EnterProtectedRegion();
//        Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteSelectSteerDataToServoSwcPort16.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

//    Rte_DataReceived_ServoSWC_ServoSWCProto_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32();
    return RTE_E_OK;
}

/** === RearWheelSWC ======================================================================= */
/** --- RearWheelSWCProto -------------------------------------------------------------------- */

/** ------ RearWheelSwcReadDataFromWheelSensorPort2 */
Std_ReturnType Rte_Call_RearWheelSWC_RearWheelSWCProto_RearWheelSwcReadDataFromWheelSensorPort2_Read(/*IN*/UInt32* Data) {
    return Rte_Call_IoHwAb_IoHwAbProto_IoHwReadRearWheelDataPort3_Read(Data);
}

/** ------ RearWheelSwcWriteDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_RearWheelSWC_RearWheelSWCProto_RearWheelSwcWriteDataToPirteSwcPort1_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (RearWheelSWCProto_RearWheelSwcWriteDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromRearWheelSwcPort7) */
    {
        Rte_EnterProtectedRegion();
        Buffers.RearWheelSWC.RearWheelSWCProto.RearWheelSwcWriteDataToPirteSwcPort1.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32();
    return RTE_E_OK;
}

/** === ServoSWC ======================================================================= */
/** --- ServoSWCProto -------------------------------------------------------------------- */

/** ------ ServoSwcReadDataFromPirteSwcPort1 */
Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromPirteSwcPort1_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (PirteSWCProto_PirteSwcWriteDataToServoSwcPort4_to_ServoSWCProto_ServoSwcReadDataFromPirteSwcPort1) */
    {
        Rte_EnterProtectedRegion();
        *value = Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteDataToServoSwcPort4.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ ServoSwcReadDataFromTCUPort2 */
Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromTCUPort2_SpeedSteer(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    /* --- Unconnected */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_DRIVERSETSPEEDSTEERINGSIGNAL, value);
    return status;
}

Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadDataFromTCUPort2_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;
    /* --- Unconnected */

    return status;
}

/** ------ ServoSwcReadSelectDataFromPirteSwcPort4 */
Std_ReturnType Rte_Read_ServoSWC_ServoSWCProto_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (PirteSWCProto_PirteSwcWriteSelectSteerDataToServoSwcPort16_to_ServoSWCProto_ServoSwcReadSelectDataFromPirteSwcPort4) */
    {
        Rte_EnterProtectedRegion();
//        *value = Buffers.PirteSWC.PirteSWCProto.PirteSwcWriteSelectSteerDataToServoSwcPort16.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ ServoSwcWriteDataToIoHwPwmPort3 */
Std_ReturnType Rte_Call_ServoSWC_ServoSWCProto_ServoSwcWriteDataToIoHwPwmPort3_Write(/*OUT*/UInt8 * DutyCycle) {
    return Rte_Call_IoHwAb_IoHwAbProto_IoHwWriteServoDutyCyclePort6_Write(DutyCycle);
}

