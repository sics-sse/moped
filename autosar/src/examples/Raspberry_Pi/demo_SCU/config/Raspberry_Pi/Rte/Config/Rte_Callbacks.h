
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

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadCommDataFromTCUPort3_pluginCommunication(void) {
    // Activate runnable(s) Pirte_ReadIMUSensor_Runnable, SCU_Installation_TCU_ReadRunnable, SCU_Communication_TCU_ReadRunnable, Pirte_ReadUltrasonicSensor_Runnable, SCU_Communication_VCU_ReadRunnable
    SetEvent(TASK_ID_RteTask,
            EVENT_MASK_IMUEvent | EVENT_MASK_SCUInstallationEvent | EVENT_MASK_TCUCommunicationEvent | EVENT_MASK_UltraEvent
                    | EVENT_MASK_VCUCommunicationEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuGyroPort7_dataUInt16(void) {
    // Activate runnable(s) Pirte_ReadIMUSensor_Runnable, SCU_Installation_TCU_ReadRunnable, SCU_Communication_TCU_ReadRunnable, Pirte_ReadUltrasonicSensor_Runnable, SCU_Communication_VCU_ReadRunnable
    SetEvent(TASK_ID_RteTask,     EVENT_MASK_IMUEvent );
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromUltraSwcPort9_dataInt32(void) {
    // Activate runnable(s) Pirte_ReadIMUSensor_Runnable, SCU_Installation_TCU_ReadRunnable, SCU_Communication_TCU_ReadRunnable, Pirte_ReadUltrasonicSensor_Runnable, SCU_Communication_VCU_ReadRunnable
    SetEvent(TASK_ID_RteTask,EVENT_MASK_UltraEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadDataFromVCUPort1_pluginCommunication(void) {
    // Activate runnable(s) Pirte_ReadIMUSensor_Runnable, SCU_Installation_TCU_ReadRunnable, SCU_Communication_TCU_ReadRunnable, Pirte_ReadUltrasonicSensor_Runnable, SCU_Communication_VCU_ReadRunnable
    SetEvent(TASK_ID_RteTask,
            EVENT_MASK_IMUEvent | EVENT_MASK_SCUInstallationEvent | EVENT_MASK_TCUCommunicationEvent | EVENT_MASK_UltraEvent
                    | EVENT_MASK_VCUCommunicationEvent);
}

static inline void Rte_DataReceived_PirteSWC_PirteSWCProto_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication(void) {
    // Activate runnable(s) Pirte_ReadIMUSensor_Runnable, SCU_Installation_TCU_ReadRunnable, SCU_Communication_TCU_ReadRunnable, Pirte_ReadUltrasonicSensor_Runnable, SCU_Communication_VCU_ReadRunnable
    SetEvent(TASK_ID_RteTask,
            EVENT_MASK_IMUEvent | EVENT_MASK_SCUInstallationEvent | EVENT_MASK_TCUCommunicationEvent | EVENT_MASK_UltraEvent
                    | EVENT_MASK_VCUCommunicationEvent);
}

#endif
