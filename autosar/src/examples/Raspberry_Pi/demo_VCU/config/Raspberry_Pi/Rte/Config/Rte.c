/**
 * Generated RTE
 *
 * @req SWS_Rte_01169
 */

/** === HEADER ====================================================================================
 */

/** @req SWS_Rte_01279 */
#include <Rte.h>

/** @req SWS_Rte_01257 */
#include <Os.h>

#if ((OS_AR_RELEASE_MAJOR_VERSION != RTE_AR_RELEASE_MAJOR_VERSION) || (OS_AR_RELEASE_MINOR_VERSION != RTE_AR_RELEASE_MINOR_VERSION))
#error Os version mismatch
#endif

/** @req SWS_Rte_03794 */
#include <Com.h>

#if ((COM_AR_RELEASE_MAJOR_VERSION != RTE_AR_RELEASE_MAJOR_VERSION) || (COM_AR_RELEASE_MINOR_VERSION != RTE_AR_RELEASE_MINOR_VERSION))
#error Com version mismatch
#endif

/** @req SWS_Rte_01326 */
#include <Rte_Hook.h>

#include <Rte_Internal.h>

/** === Os Macros =================================================================================
 */

#define END_OF_TASK(taskName) TerminateTask()

#define ARC_STRINGIFY(value)  ARC_STRINGIFY2(value)
#define ARC_STRINGIFY2(value) #value

#if defined(ARC_INJECTED_HEADER_RTE_C)
#define  THE_INCLUDE ARC_STRINGIFY(ARC_INJECTED_HEADER_RTE_C)
#include THE_INCLUDE
#undef   THE_INCLUDE
#endif

#if !defined(RTE_EXTENDED_TASK_LOOP_CONDITION)
#define RTE_EXTENDED_TASK_LOOP_CONDITION 1
#endif

extern ModeMachinesType ModeMachines;

/** === Generated API =============================================================================
 */

/** === Runnables =================================================================================
 */
extern void Rte_LedSWCProto_LedControlRunnable(void);
extern void Rte_MotorSWCProto_MotorControlRunnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadDataFromAdc_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadDataFromFrontWheel_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadDataFromRearWheel_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadDataFromSCU_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadInstallationDataFromTCU_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadPositionData_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadSpeedSteerData_Runnable(void);
extern void Rte_ServoSWCProto_ServoControlRunnable(void);

extern void Rte_AdcSWCProto_AdcSensorWriteRunnable(void);
extern void Rte_AdcSWCProto_AdcSensorReadRunnable(void);
extern void Rte_FrontWheelSWCProto_FrontWheelSensorWriteRunnable(void);
extern void Rte_FrontWheelSWCProto_FrontWheelSensorReadRunnable(void);
extern void Rte_RearWheelSWCProto_RearWheelSensorWriteRunnable(void);
extern void Rte_RearWheelSWCProto_RearWheelSensorReadRunnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadCommunicationDataFromTCU_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_WriteAcknowledgementDataToTCU_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_WriteCommunicationDataFromTCU_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_WriteDataToSCU_Runnable(void);

extern void SpeedSensor_Init(void);

extern void Rte_PirteSWCProto_Pirte_ReadPositionData_Runnable(void);
extern boolean acknowledgementTCU_Flag;
extern boolean pluginCommunicationTCU_Flag;
extern boolean pluginCommunicationVCUtoSCU_Flag;
boolean ComWrite = false;

/** === Tasks =====================================================================================
 *
 */
void SensorTask(void) {
	EventMaskType Event;
	printf("sensor task\r\n");
	SpeedSensor_Init();
	do {
		WaitEvent(EVENT_MASK_SensorEvent);
		GetEvent(TASK_ID_SensorTask, &Event);

		if (Event & EVENT_MASK_SensorEvent) {
			ClearEvent(EVENT_MASK_SensorEvent);

//			printf("Sensor event cleared\r\n");

			//adc sensor
			Rte_AdcSWCProto_AdcSensorReadRunnable();
			Rte_AdcSWCProto_AdcSensorWriteRunnable();

			//front wheel sensor
			Rte_FrontWheelSWCProto_FrontWheelSensorReadRunnable();
			Rte_FrontWheelSWCProto_FrontWheelSensorWriteRunnable();

			//rear wheel sensor
			Rte_RearWheelSWCProto_RearWheelSensorReadRunnable();
			Rte_RearWheelSWCProto_RearWheelSensorWriteRunnable();

		}

	} while (RTE_EXTENDED_TASK_LOOP_CONDITION);
}

void ActuatorTask(void) { /** @req SWS_Rte_02251 */
    EventMaskType Event;
    do {
        WaitEvent(	EVENT_MASK_LedEvent | EVENT_MASK_SpeedEvent | EVENT_MASK_SpeedSelectEvent |
        			EVENT_MASK_ServoEvent | EVENT_MASK_SteerSelectEvent);
        GetEvent(TASK_ID_ActuatorTask, &Event);

		if (Event & EVENT_MASK_SpeedSelectEvent) {
			ClearEvent(EVENT_MASK_SpeedSelectEvent);
//			Rte_MotorSWCProto_MotorControlRunnable();
		}
		if (Event & EVENT_MASK_SteerSelectEvent) {
			ClearEvent(EVENT_MASK_SteerSelectEvent);
//			Rte_ServoSWCProto_ServoControlRunnable();
		}

        if (Event & EVENT_MASK_SpeedEvent) {
            ClearEvent (EVENT_MASK_SpeedEvent);
            Rte_MotorSWCProto_MotorControlRunnable();
        }
        if (Event & EVENT_MASK_ServoEvent) {
            ClearEvent (EVENT_MASK_ServoEvent);
            Rte_ServoSWCProto_ServoControlRunnable();
        }

        if (Event & EVENT_MASK_LedEvent) {
            ClearEvent (EVENT_MASK_LedEvent);
            Rte_LedSWCProto_LedControlRunnable();
        }

    } while (RTE_EXTENDED_TASK_LOOP_CONDITION);
}

void RteTask(void) { /** @req SWS_Rte_02251 */
    EventMaskType Event;
    do {
        WaitEvent(	EVENT_MASK_AdcEvent | EVENT_MASK_FrontWheelEvent | EVENT_MASK_pluginCommunicationSCUEvent |
               		EVENT_MASK_pluginCommunicationTCUEvent | EVENT_MASK_positionDataEvent |
               		EVENT_MASK_SpeedSteerEvent | EVENT_MASK_RearWheelEvent |
					EVENT_MASK_pluginInstallationEvent | EVENT_MASK_RteFunctionEvent);
        GetEvent(TASK_ID_RteTask, &Event);


		if (Event & EVENT_MASK_AdcEvent) {
			ClearEvent(EVENT_MASK_AdcEvent);
			Rte_PirteSWCProto_Pirte_ReadDataFromAdc_Runnable();
		}
		if (Event & EVENT_MASK_FrontWheelEvent) {
			ClearEvent(EVENT_MASK_FrontWheelEvent);
			Rte_PirteSWCProto_Pirte_ReadDataFromFrontWheel_Runnable();
		}
		if (Event & EVENT_MASK_pluginCommunicationSCUEvent) {
			ClearEvent(EVENT_MASK_pluginCommunicationSCUEvent);
			Rte_PirteSWCProto_Pirte_ReadDataFromSCU_Runnable();
		}
		if (Event & EVENT_MASK_pluginCommunicationTCUEvent) {
			ClearEvent(EVENT_MASK_pluginCommunicationTCUEvent);
			//printf("info: rte pirte read CommunicationDataFromTCU\r\n");
			Rte_PirteSWCProto_Pirte_ReadCommunicationDataFromTCU_Runnable();
		}
		if (Event & EVENT_MASK_positionDataEvent) {
			ClearEvent(EVENT_MASK_positionDataEvent);
			//printf("info: rte pirte read position data\r\n");
			Rte_PirteSWCProto_Pirte_ReadPositionData_Runnable();
		}
		if (Event & EVENT_MASK_SpeedSteerEvent) {
			ClearEvent (EVENT_MASK_SpeedSteerEvent);
			Rte_PirteSWCProto_Pirte_ReadSpeedSteerData_Runnable();
		}
		if (Event & EVENT_MASK_RearWheelEvent) {
			ClearEvent(EVENT_MASK_RearWheelEvent);
			Rte_PirteSWCProto_Pirte_ReadDataFromRearWheel_Runnable();
		}
		if (Event & EVENT_MASK_pluginInstallationEvent) {
			ClearEvent(EVENT_MASK_pluginInstallationEvent);
			//printf("info:Installation\r\n");
			Rte_PirteSWCProto_Pirte_ReadInstallationDataFromTCU_Runnable();
		}

		//Rte_PirteSWCProto_Pirte_WriteLedData_Runnable();

		//ack 1 byte
		if (acknowledgementTCU_Flag == true) {
			Rte_PirteSWCProto_Pirte_WriteAcknowledgementDataToTCU_Runnable();
			acknowledgementTCU_Flag = false;
			ComWrite = true;
		}
		//ack much more than 8 bytes
		if (Event & EVENT_MASK_RteFunctionEvent) {
			ClearEvent(EVENT_MASK_RteFunctionEvent);
			if (pluginCommunicationTCU_Flag == true) {
				Rte_PirteSWCProto_Pirte_WriteCommunicationDataFromTCU_Runnable();
				ComWrite = true;
			}
			if (pluginCommunicationVCUtoSCU_Flag == true) {
				Rte_PirteSWCProto_Pirte_WriteDataToSCU_Runnable();
				ComWrite = true;
			}
		}
    } while (RTE_EXTENDED_TASK_LOOP_CONDITION);
}

