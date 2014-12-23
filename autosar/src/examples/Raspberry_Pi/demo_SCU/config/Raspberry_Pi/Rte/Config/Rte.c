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
#include "mpu9150_sensor.h"

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
extern void Rte_PirteSWCProto_Pirte_ReadIMUSensor_Runnable(void);
extern void Rte_PirteSWCProto_Pirte_ReadUltrasonicSensor_Runnable(void);
extern void Rte_PirteSWCProto_SCU_Communication_TCU_ReadRunnable(void);
extern void Rte_PirteSWCProto_SCU_Communication_VCU_ReadRunnable(void);
extern void Rte_PirteSWCProto_SCU_Installation_TCU_ReadRunnable(void);
extern void Rte_PirteSWCProto_SCU_Communication_VCU_WriteRunnable(void);
extern void Rte_UltraSWCProto_UltrasonicRunnable(void);
extern void Rte_ImuSWCProto_ImuSwcRunnable(void);
extern void Rte_PirteSWCProto_SCU_Installation_TCU_AckRunnable(void);
extern void Rte_PirteSWCProto_SCU_Communication_TCU_WriteRunnable(void);

extern boolean pluginCommunicationSCUtoVCU_Flag;
extern boolean pluginCommunicationTCU_Flag;
extern boolean acknowledgementTCU_Flag;
boolean ComWrite = false;
/** === Tasks =====================================================================================
 */


void RteTask(void) { /** @req SWS_Rte_02251 */
    EventMaskType Event;
    printf("infor: RTE task\r\n");
    do {
        WaitEvent( EVENT_MASK_IMUEvent | EVENT_MASK_SCUInstallationEvent |
        		   EVENT_MASK_TCUCommunicationEvent | EVENT_MASK_UltraEvent |
        		   EVENT_MASK_VCUCommunicationEvent | EVENT_MASK_RteEvent );
        GetEvent(TASK_ID_RteTask, &Event);

        if (Event & EVENT_MASK_IMUEvent) {
            ClearEvent (EVENT_MASK_IMUEvent);
            Rte_PirteSWCProto_Pirte_ReadIMUSensor_Runnable();
        }
        if (Event & EVENT_MASK_UltraEvent) {
            ClearEvent (EVENT_MASK_UltraEvent);
            Rte_PirteSWCProto_Pirte_ReadUltrasonicSensor_Runnable();
        }
        if (Event & EVENT_MASK_SCUInstallationEvent) {
            ClearEvent (EVENT_MASK_SCUInstallationEvent);
            Rte_PirteSWCProto_SCU_Installation_TCU_ReadRunnable();
        }
        if (Event & EVENT_MASK_TCUCommunicationEvent) {
            ClearEvent (EVENT_MASK_TCUCommunicationEvent);
            Rte_PirteSWCProto_SCU_Communication_TCU_ReadRunnable();
        }
        if (Event & EVENT_MASK_VCUCommunicationEvent) {
            ClearEvent (EVENT_MASK_VCUCommunicationEvent);
            Rte_PirteSWCProto_SCU_Communication_VCU_ReadRunnable();
        }
		if (Event & EVENT_MASK_RteEvent) {
			ClearEvent(EVENT_MASK_RteEvent);
			if (pluginCommunicationTCU_Flag != false) {
				Rte_PirteSWCProto_SCU_Communication_TCU_WriteRunnable();
				ComWrite = true;
			}
	        if(pluginCommunicationSCUtoVCU_Flag == true){
	        	Rte_PirteSWCProto_SCU_Communication_VCU_WriteRunnable();
	           	ComWrite = true;
	        }
		}


        if(acknowledgementTCU_Flag == true){
        	  Rte_PirteSWCProto_SCU_Installation_TCU_AckRunnable();
        	  acknowledgementTCU_Flag = false;
        	  ComWrite = true;
          }

    } while (RTE_EXTENDED_TASK_LOOP_CONDITION);
}

void SensorTask(void) {
    EventMaskType Event;
    Mpu9150_Init();
    for(;;){
        WaitEvent(EVENT_MASK_SensorEvent);
        ClearEvent (EVENT_MASK_SensorEvent);

       	Rte_UltraSWCProto_UltrasonicRunnable();

       	Rte_ImuSWCProto_ImuSwcRunnable();

    }
}


