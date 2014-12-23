
#ifndef RTE_CALLBACKS_H_
#define RTE_CALLBACKS_H_

#include <Os.h>

/** @req SWS_Rte_01165
 * !req SWS_Rte_03007 Currently using the name of the signal when interacting with COM
 * !req SWS_Rte_03008 Currently using the name of the signal group when interacting with COM
 * @req SWS_Rte_03000
 * @req SWS_Rte_03001
 * !req SWS_Rte_03002 !req SWS_Rte_03005 COMCbkTAck
 * !req SWS_Rte_03775 !req SWS_Rte_03776 COMCbkTErr
 * !req SWS_Rte_02612 !req SWS_Rte_05065 COMCbkInv
 * !req SWS_Rte_02610 !req SWS_Rte_02611 COMCbkRxTOut
 * !req SWS_Rte_05084 !req SWS_Rte_05085 COMCbkTxTOut
 * @req SWS_Rte_03004
 * @req SWS_Rte_01131
 * !req SWS_Rte_07177 BackgroundEvent
 * !req SWS_Rte_02512 SwcModeSwitchEvent
 * !req SWS_Rte_01133 AsynchronousServerCallReturnsEvent
 * !req SWS_Rte_01359 DataReceivedErrorEvent
 * @req SWS_Rte_01166
 * @req SWS_Rte_07023
 * @req SWS_Rte_07024
 * @req SWS_Rte_07025
 * !req SWS_Rte_07026 No validator for this
 * !req SWS_Rte_07027 void types not supported
 * !req SWS_Rte_05193 No type casting support
 * @req SWS_Rte_01135
 * @req SWS_Rte_01359 DataReceiveErrorEvent
 * !req SWS_Rte_01137 DataSendCompletedEvent
 * !req SWS_Rte_02758 ModeSwitchAckEvent
 * !req SWS_Rte_06771 SwcModeManagerErrorEvent
 * !req SWS_Rte_07207 ExternalTriggerOccurredEvent
 * !req SWS_Rte_07208 InternalTriggerOccurredEvent
 * !req SWS_Rte_07379 DataWriteCompletedEvent
 * !req SWS_Rte_06748 InitEvent
 */

static inline void Rte_DataReceived_LedSWC_LedSWCProto_LedSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    // Activate runnable(s) LedControlRunnable, MotorControlRunnable, ServoControlRunnable
    SetEvent(TASK_ID_ActuatorTask, EVENT_MASK_LedEvent);
}

static inline void Rte_DataReceived_MotorSWC_MotorSWCProto_MotorSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    // Activate runnable(s) LedControlRunnable, MotorControlRunnable, ServoControlRunnable
    SetEvent(TASK_ID_ActuatorTask, EVENT_MASK_SpeedEvent);
}

static inline void Rte_DataReceived_MotorSWC_MotorSWCProto_MotorSwcReadDataFromTCUPort2_SpeedSteer(void) {
    // Activate runnable(s) LedControlRunnable, MotorControlRunnable, ServoControlRunnable
    SetEvent(TASK_ID_ActuatorTask, EVENT_MASK_SpeedEvent);
}

static inline void Rte_DataReceived_MotorSWC_MotorSWCProto_MotorSwcReadSelectDataFromPirteSwcPort4_dataInt32(void) {
    // Activate runnable(s) LedControlRunnable, MotorControlRunnable, ServoControlRunnable
    SetEvent(TASK_ID_ActuatorTask, EVENT_MASK_SpeedSelectEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromAdcSwcPort5_dataInt32(void) {
    // Activate runnable(s) Pirte_ReadDataFromAdc_Runnable, Pirte_ReadDataFromFrontWheel_Runnable, Pirte_ReadDataFromSCU_Runnable, Pirte_ReadInstallationDataFromTCU_Runnable, Pirte_ReadPositionData_Runnable, Pirte_ReadSpeedSteerData_Runnable, Pirte_ReadDataFromRearWheel_Runnable
    SetEvent(TASK_ID_RteTask, EVENT_MASK_AdcEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32(void) {
    // Activate runnable(s) Pirte_ReadDataFromAdc_Runnable, Pirte_ReadDataFromFrontWheel_Runnable, Pirte_ReadDataFromSCU_Runnable, Pirte_ReadInstallationDataFromTCU_Runnable, Pirte_ReadPositionData_Runnable, Pirte_ReadSpeedSteerData_Runnable, Pirte_ReadDataFromRearWheel_Runnable
    SetEvent(TASK_ID_RteTask, EVENT_MASK_FrontWheelEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32(void) {
    // Activate runnable(s) Pirte_ReadDataFromAdc_Runnable, Pirte_ReadDataFromFrontWheel_Runnable, Pirte_ReadDataFromSCU_Runnable, Pirte_ReadInstallationDataFromTCU_Runnable, Pirte_ReadPositionData_Runnable, Pirte_ReadSpeedSteerData_Runnable, Pirte_ReadDataFromRearWheel_Runnable
    SetEvent(TASK_ID_RteTask, EVENT_MASK_RearWheelEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU(void) {
    // Activate runnable(s) Pirte_ReadDataFromAdc_Runnable, Pirte_ReadDataFromFrontWheel_Runnable, Pirte_ReadDataFromSCU_Runnable, Pirte_ReadInstallationDataFromTCU_Runnable, Pirte_ReadPositionData_Runnable, Pirte_ReadSpeedSteerData_Runnable, Pirte_ReadDataFromRearWheel_Runnable
    SetEvent(TASK_ID_RteTask, EVENT_MASK_pluginCommunicationSCUEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU(void) {
    // Activate runnable(s) Pirte_ReadDataFromAdc_Runnable, Pirte_ReadDataFromFrontWheel_Runnable, Pirte_ReadDataFromSCU_Runnable, Pirte_ReadInstallationDataFromTCU_Runnable, Pirte_ReadPositionData_Runnable, Pirte_ReadSpeedSteerData_Runnable, Pirte_ReadDataFromRearWheel_Runnable
    SetEvent(TASK_ID_RteTask,
            EVENT_MASK_AdcEvent | EVENT_MASK_FrontWheelEvent | EVENT_MASK_pluginCommunicationSCUEvent | EVENT_MASK_pluginCommunicationTCUEvent
                    | EVENT_MASK_positionDataEvent | EVENT_MASK_SpeedSteerEvent | EVENT_MASK_RearWheelEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadPositionDataFromTCUPort12_PositionData(void) {
    // Activate runnable(s) Pirte_ReadDataFromAdc_Runnable, Pirte_ReadDataFromFrontWheel_Runnable, Pirte_ReadDataFromSCU_Runnable, Pirte_ReadInstallationDataFromTCU_Runnable, Pirte_ReadPositionData_Runnable, Pirte_ReadSpeedSteerData_Runnable, Pirte_ReadDataFromRearWheel_Runnable
    SetEvent(TASK_ID_RteTask, EVENT_MASK_positionDataEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer(void) {
    // Activate runnable(s) Pirte_ReadDataFromAdc_Runnable, Pirte_ReadDataFromFrontWheel_Runnable, Pirte_ReadDataFromSCU_Runnable, Pirte_ReadInstallationDataFromTCU_Runnable, Pirte_ReadPositionData_Runnable, Pirte_ReadSpeedSteerData_Runnable, Pirte_ReadDataFromRearWheel_Runnable
    SetEvent(TASK_ID_RteTask, EVENT_MASK_SpeedSteerEvent);
}

static inline void Rte_DataReceived_ServoSWC_ServoSWCProto_ServoSwcReadDataFromPirteSwcPort1_dataInt32(void) {
    // Activate runnable(s) LedControlRunnable, MotorControlRunnable, ServoControlRunnable
    SetEvent(TASK_ID_ActuatorTask, EVENT_MASK_ServoEvent);
}

static inline void Rte_DataReceived_ServoSWC_ServoSWCProto_ServoSwcReadDataFromTCUPort2_SpeedSteer(void) {
    // Activate runnable(s) LedControlRunnable, MotorControlRunnable, ServoControlRunnable
    SetEvent(TASK_ID_ActuatorTask, EVENT_MASK_ServoEvent);
}

static inline void Rte_DataReceived_ServoSWC_ServoSWCProto_ServoSwcReadSelectDataFromPirteSwcPort4_dataInt32(void) {
    // Activate runnable(s) LedControlRunnable, MotorControlRunnable, ServoControlRunnable
    SetEvent(TASK_ID_ActuatorTask, EVENT_MASK_SpeedSelectEvent);
}

#endif
