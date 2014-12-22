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

extern Std_ReturnType Rte_IoHwAbService_ImuSensorRead(/*IN*/UInt16 portDefArg1, /*IN*/UInt16 const * Data);
extern Std_ReturnType Rte_IoHwAbService_UltraSensorRead(/*IN*/UInt32 portDefArg1, /*IN*/UInt16 const * Data);

/** === ImuSWC ======================================================================= */
/** --- ImuSWCProto -------------------------------------------------------------------- */

/** ------ ImuSwcToIoHwAcceDataInPort4 */
Std_ReturnType Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwAcceDataInPort4_Read(/*IN*/UInt16 const * Data) {
    return Rte_Call_IoHwAb_IoHwAbService_IoHwAcceDataInPort2_Read(Data);
}

/** ------ ImuSwcToIoHwGyroDataInPort3 */
Std_ReturnType Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwGyroDataInPort3_Read(/*IN*/UInt16 const * Data) {
    return Rte_Call_IoHwAb_IoHwAbService_IoHwGyroDataInPort1_Read(Data);
}

/** ------ ImuSwcWriteAcceDataToPirteSwcPort2 */
Std_ReturnType Rte_Write_ImuSWC_ImuSWCProto_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16(/*IN*/UInt16 const * value) {

    /* --- Sender (ImuSWCProto_ImuSwcWriteAcceDataToPirteSwcPort2_to_PirteSWCProto_PirteSwcReadDataFromImuAccePort8) */
    {
        Rte_EnterProtectedRegion();
        memcpy(Buffers.ImuSWC.ImuSWCProto.ImuSwcWriteAcceDataToPirteSwcPort2.dataUInt16, value, sizeof(Array3));
        Rte_ExitProtectedRegion();
    }

    return RTE_E_OK;
}

/** ------ ImuSwcWriteGyroDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_ImuSWC_ImuSWCProto_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16(/*IN*/UInt16 const * value) {

    /* --- Sender (ImuSWCProto_ImuSwcWriteGyroDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromImuGyroPort7) */
    {
        Rte_EnterProtectedRegion();
        memcpy(Buffers.ImuSWC.ImuSWCProto.ImuSwcWriteGyroDataToPirteSwcPort1.dataUInt16, value, sizeof(Array3));
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuGyroPort7_dataUInt16();
    return RTE_E_OK;
}

/** === IoHwAb ======================================================================= */
/** --- IoHwAbService -------------------------------------------------------------------- */

/** ------ IoHwAcceDataInPort2 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbService_IoHwAcceDataInPort2_Read(/*IN*/UInt16 const * Data) {
    return Rte_IoHwAbService_ImuSensorRead(1, Data);
}

/** ------ IoHwGyroDataInPort1 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbService_IoHwGyroDataInPort1_Read(/*IN*/UInt16 const * Data) {
    return Rte_IoHwAbService_ImuSensorRead(0, Data);
}

/** ------ IoHwUltraDataInPort3 */
inline Std_ReturnType Rte_Call_IoHwAb_IoHwAbService_IoHwUltraDataInPort3_Read(/*IN*/UInt32 const * Data) {
    return Rte_IoHwAbService_UltraSensorRead(0, Data);
}

/** === PirteSWC ======================================================================= */
/** --- PirteSWCProto -------------------------------------------------------------------- */

/** ------ PirteSwcReadCommDataFromTCUPort3 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommDataFromTCUPort3_argInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (pluginCommunicationTCUtoSCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOSCUSIGNAL, value);

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommDataFromTCUPort3_pluginCommunication(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONTCUTOSCUSIGNAL, value);
    return status;
}

/** ------ PirteSwcReadDataFromImuAccePort8 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuAccePort8_dataUInt16(/*OUT*/UInt16 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (ImuSWCProto_ImuSwcWriteAcceDataToPirteSwcPort2_to_PirteSWCProto_PirteSwcReadDataFromImuAccePort8) */
    {
        Rte_EnterProtectedRegion();
        memcpy(value, Buffers.ImuSWC.ImuSWCProto.ImuSwcWriteAcceDataToPirteSwcPort2.dataUInt16, sizeof(Array3));
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ PirteSwcReadDataFromImuGyroPort7 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuGyroPort7_dataUInt16(/*OUT*/UInt16 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (ImuSWCProto_ImuSwcWriteGyroDataToPirteSwcPort1_to_PirteSWCProto_PirteSwcReadDataFromImuGyroPort7) */
    {
        Rte_EnterProtectedRegion();
        memcpy(value, Buffers.ImuSWC.ImuSWCProto.ImuSwcWriteGyroDataToPirteSwcPort1.dataUInt16, sizeof(Array3));
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ PirteSwcReadDataFromUltraSwcPort9 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromUltraSwcPort9_dataInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (UltraSWCProto_UltraSwcDataOutPort2_to_PirteSWCProto_PirteSwcReadDataFromUltraSwcPort9) */
    {
        Rte_EnterProtectedRegion();
        *value = Buffers.UltraSWC.UltraSWCProto.UltraSwcDataOutPort2.dataInt32;
        Rte_ExitProtectedRegion();
    }

    return status;
}

/** ------ PirteSwcReadDataFromVCUPort1 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromVCUPort1_argInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (pluginCommunicationVCUtoSCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL, value);

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromVCUPort1_pluginCommunication(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONVCUTOSCUSIGNAL, value);
    return status;
}

/** ------ PirteSwcReadInstallDataFromTCUPort5 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallDataFromTCUPort5_argInt32(/*OUT*/UInt32 * value) {
    Std_ReturnType status = RTE_E_OK;

    /* --- Receiver (pluginInstallationSCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGININSTALLATIONSCUSIGNAL, value);

    return status;
}

Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication(/*OUT*/UInt8 * value) {
    Std_ReturnType status = RTE_E_OK;
    status |= Com_ReceiveSignal(COM_SIGNAL_ID_PLUGININSTALLATIONSCUSIGNAL, value);
    return status;
}

/** ------ PirteSwcWriteAckDataToTCUPort6 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU(/*IN*/UInt8 value) {

    /* --- Sender (acknowledgementSCUsignalacknowledgementVCUISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    Com_SendSignal(COM_SIGNAL_ID_ACKNOWLEDGEMENTSCUSIGNAL, &value);

    return RTE_E_OK;
}

/** ------ PirteSwcWriteCommDataToTCUPort4 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommDataToTCUPort4_argInt32(/*IN*/UInt32 value) {

    /* --- Sender (pluginCommunicationSCUtoTCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOTCUSIGNAL, &value);
    return RTE_E_OK;
}

Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(/*IN*/UInt8 const * value) {
	Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOTCUSIGNAL, value);
    return RTE_E_OK;
}

/** ------ PirteSwcWriteDataToVCUPort2 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToVCUPort2_argInt32(/*IN*/UInt32 value) {

    /* --- Sender (pluginCommunicationSCUtoVCUsignalargInt32ISig) @req SWS_Rte_04505, @req SWS_Rte_06023 */
    Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL, &value);

    return RTE_E_OK;
}

Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToVCUPort2_pluginCommunication(/*IN*/UInt8 const * value) {

	Com_SendSignal(COM_SIGNAL_ID_PLUGINCOMMUNICATIONSCUTOVCUSIGNAL, value);
    return RTE_E_OK;
}

/** === UltraSWC ======================================================================= */
/** --- UltraSWCProto -------------------------------------------------------------------- */

/** ------ UltraSwcDataInPort1 */
Std_ReturnType Rte_Call_UltraSWC_UltraSWCProto_UltraSwcDataInPort1_Read(/*IN*/UInt32 const * Data) {
    return Rte_Call_IoHwAb_IoHwAbService_IoHwUltraDataInPort3_Read(Data);
}

/** ------ UltraSwcDataOutPort2 */
Std_ReturnType Rte_Write_UltraSWC_UltraSWCProto_UltraSwcDataOutPort2_dataInt32(/*IN*/UInt32 value) {

    /* --- Sender (UltraSWCProto_UltraSwcDataOutPort2_to_PirteSWCProto_PirteSwcReadDataFromUltraSwcPort9) */
    {
        Rte_EnterProtectedRegion();
        Buffers.UltraSWC.UltraSWCProto.UltraSwcDataOutPort2.dataInt32 = value;
        Rte_ExitProtectedRegion();
    }

    Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromUltraSwcPort9_dataInt32();
    return RTE_E_OK;
}

