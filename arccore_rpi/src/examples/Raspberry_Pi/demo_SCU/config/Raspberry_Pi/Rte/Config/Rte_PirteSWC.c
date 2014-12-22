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
#include <Rte_Calprms.h>

#include "Rte_PirteSWC.h"
#include "MOPED_DEBUG.h"

/** === Component Data Structures =================================================================
 */
char* UserClassPath = NULL;
extern int StartJVM(int argc, char *argv[]);
#if RUN_SQUAWK
int runSquawk(void) {
//	pi_printf("runKVM started\r\n");
	int result;
	char *argv[2];
//	RequestedHeapSize = 128 * 1024; // Default heap size (necessary configuration parameter)

//	AlertUser("In runKVM()");
//	pi_printf("runKVM started2\r\n");

#if 0
	UserClassPath = "."; // Default class path (necessary configuration parameter)
	argv[0] = "Hej";
//	pi_printf("runKVM started3\r\n");
#endif
#if 0
	UserClassPath = "."; // Default class path (necessary configuration parameter)
	argv[0] = "HejPartitioned1";
#endif
#if 0
	UserClassPath = "Heja.jar";
	argv[0] = "HejKNI";
#endif
#if 0
	UserClassPath = ".";
	argv[0] = "PlugInRunTimeEnvironment";
#endif
#if 1
	UserClassPath = ".";
	argv[0] = "squawk";
	//argv[1] = "-suite:HelloSimpleAutosar";
	argv[1] = "sics.PIRTE";
#endif
	result = StartJVM(2, argv);
	//	mini_uart_sendstr("JVM has been run\r\n");
	pi_printf("startup Java App completed\r\n");
	return result;

}
#endif
/** --------- PirteSWCProto --------------------------------------------------------------------
 */
struct {
    struct {
        struct {
            Rte_DE_Array8 pluginCommunication;
            Rte_DE_UInt32 argInt32;
        } PirteSwcReadDataFromVCUPort1;
    } SCU_Communication_VCU_ReadRunnable;
    struct {
        struct {
            Rte_DE_Array8 pluginCommunication;
            Rte_DE_UInt32 argInt32;
        } PirteSwcWriteDataToVCUPort2;
    } SCU_Communication_VCU_WriteRunnable;
    struct {
        struct {
            Rte_DE_Array8 pluginCommunication;
            Rte_DE_UInt32 argInt32;
        } PirteSwcReadCommDataFromTCUPort3;
    } SCU_Communication_TCU_ReadRunnable;
    struct {
        struct {
            Rte_DE_Array8 pluginCommunication;
            Rte_DE_UInt32 argInt32;
        } PirteSwcWriteCommDataToTCUPort4;
    } SCU_Communication_TCU_WriteRunnable;
    struct {
        struct {
            Rte_DE_Array8 pluginCommunication;
            Rte_DE_UInt32 argInt32;
        } PirteSwcReadInstallDataFromTCUPort5;
    } SCU_Installation_TCU_ReadRunnable;
    struct {
        struct {
            Rte_DE_UInt8 acknowledgementVCU;
        } PirteSwcWriteAckDataToTCUPort6;
    } SCU_Installation_TCU_AckRunnable;
    struct {
        struct {
            Rte_DE_Array3 dataUInt16;
        } PirteSwcReadDataFromImuAccePort8;
        struct {
            Rte_DE_Array3 dataUInt16;
        } PirteSwcReadDataFromImuGyroPort7;
    } Pirte_ReadIMUSensor_Runnable;
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcReadDataFromUltraSwcPort9;
    } Pirte_ReadUltrasonicSensor_Runnable;
} ImplDE_PirteSWCProto;

const Rte_CDS_PirteSWC PirteSWC_PirteSWCProto = {
    .SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_pluginCommunication = &ImplDE_PirteSWCProto.SCU_Communication_VCU_ReadRunnable.PirteSwcReadDataFromVCUPort1.pluginCommunication,
    .SCU_Communication_VCU_ReadRunnable_PirteSwcReadDataFromVCUPort1_argInt32 = &ImplDE_PirteSWCProto.SCU_Communication_VCU_ReadRunnable.PirteSwcReadDataFromVCUPort1.argInt32,
    .SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_pluginCommunication = &ImplDE_PirteSWCProto.SCU_Communication_VCU_WriteRunnable.PirteSwcWriteDataToVCUPort2.pluginCommunication,
    .SCU_Communication_VCU_WriteRunnable_PirteSwcWriteDataToVCUPort2_argInt32 = &ImplDE_PirteSWCProto.SCU_Communication_VCU_WriteRunnable.PirteSwcWriteDataToVCUPort2.argInt32,
    .SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_pluginCommunication = &ImplDE_PirteSWCProto.SCU_Communication_TCU_ReadRunnable.PirteSwcReadCommDataFromTCUPort3.pluginCommunication,
    .SCU_Communication_TCU_ReadRunnable_PirteSwcReadCommDataFromTCUPort3_argInt32 = &ImplDE_PirteSWCProto.SCU_Communication_TCU_ReadRunnable.PirteSwcReadCommDataFromTCUPort3.argInt32,
    .SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_pluginCommunication = &ImplDE_PirteSWCProto.SCU_Communication_TCU_WriteRunnable.PirteSwcWriteCommDataToTCUPort4.pluginCommunication,
    .SCU_Communication_TCU_WriteRunnable_PirteSwcWriteCommDataToTCUPort4_argInt32 = &ImplDE_PirteSWCProto.SCU_Communication_TCU_WriteRunnable.PirteSwcWriteCommDataToTCUPort4.argInt32,
    .SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication = &ImplDE_PirteSWCProto.SCU_Installation_TCU_ReadRunnable.PirteSwcReadInstallDataFromTCUPort5.pluginCommunication,
    .SCU_Installation_TCU_ReadRunnable_PirteSwcReadInstallDataFromTCUPort5_argInt32 = &ImplDE_PirteSWCProto.SCU_Installation_TCU_ReadRunnable.PirteSwcReadInstallDataFromTCUPort5.argInt32,
    .SCU_Installation_TCU_AckRunnable_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU = &ImplDE_PirteSWCProto.SCU_Installation_TCU_AckRunnable.PirteSwcWriteAckDataToTCUPort6.acknowledgementVCU,
    .Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuGyroPort7_dataUInt16 = &ImplDE_PirteSWCProto.Pirte_ReadIMUSensor_Runnable.PirteSwcReadDataFromImuGyroPort7.dataUInt16,
    .Pirte_ReadIMUSensor_Runnable_PirteSwcReadDataFromImuAccePort8_dataUInt16 = &ImplDE_PirteSWCProto.Pirte_ReadIMUSensor_Runnable.PirteSwcReadDataFromImuAccePort8.dataUInt16,
    .Pirte_ReadUltrasonicSensor_Runnable_PirteSwcReadDataFromUltraSwcPort9_dataInt32 = &ImplDE_PirteSWCProto.Pirte_ReadUltrasonicSensor_Runnable.PirteSwcReadDataFromUltraSwcPort9.dataInt32
};

const Rte_Instance Rte_Inst_PirteSWC = &PirteSWC_PirteSWCProto;

/** === Runnables =================================================================================
 */

/** ------ PirteSWCProto -----------------------------------------------------------------------
 */

void Rte_PirteSWCProto_SCU_Communication_VCU_ReadRunnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromVCUPort1_pluginCommunication(
            ImplDE_PirteSWCProto.SCU_Communication_VCU_ReadRunnable.PirteSwcReadDataFromVCUPort1.pluginCommunication.value);

//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromVCUPort1_argInt32(
//            &ImplDE_PirteSWCProto.SCU_Communication_VCU_ReadRunnable.PirteSwcReadDataFromVCUPort1.argInt32.value);

    /* MAIN */

    SCU_Communication_VCU_ReadRunnable();

    /* POST */

}

void Rte_PirteSWCProto_SCU_Communication_VCU_WriteRunnable(void) {
    /* PRE */

    /* MAIN */

    SCU_Communication_VCU_WriteRunnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToVCUPort2_pluginCommunication(
            ImplDE_PirteSWCProto.SCU_Communication_VCU_WriteRunnable.PirteSwcWriteDataToVCUPort2.pluginCommunication.value);

//    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToVCUPort2_argInt32(
//            ImplDE_PirteSWCProto.SCU_Communication_VCU_WriteRunnable.PirteSwcWriteDataToVCUPort2.argInt32.value);

}

void Rte_PirteSWCProto_SCU_Communication_TCU_ReadRunnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommDataFromTCUPort3_pluginCommunication(
            ImplDE_PirteSWCProto.SCU_Communication_TCU_ReadRunnable.PirteSwcReadCommDataFromTCUPort3.pluginCommunication.value);

//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommDataFromTCUPort3_argInt32(
//            &ImplDE_PirteSWCProto.SCU_Communication_TCU_ReadRunnable.PirteSwcReadCommDataFromTCUPort3.argInt32.value);

    /* MAIN */

    SCU_Communication_TCU_ReadRunnable();

    /* POST */

}

void Rte_PirteSWCProto_SCU_Communication_TCU_WriteRunnable(void) {
    /* PRE */

    /* MAIN */

    SCU_Communication_TCU_WriteRunnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(
            ImplDE_PirteSWCProto.SCU_Communication_TCU_WriteRunnable.PirteSwcWriteCommDataToTCUPort4.pluginCommunication.value);

//    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommDataToTCUPort4_argInt32(
//            ImplDE_PirteSWCProto.SCU_Communication_TCU_WriteRunnable.PirteSwcWriteCommDataToTCUPort4.argInt32.value);

}

void Rte_PirteSWCProto_SCU_Installation_TCU_ReadRunnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication(
            ImplDE_PirteSWCProto.SCU_Installation_TCU_ReadRunnable.PirteSwcReadInstallDataFromTCUPort5.pluginCommunication.value);

//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallDataFromTCUPort5_argInt32(
//            &ImplDE_PirteSWCProto.SCU_Installation_TCU_ReadRunnable.PirteSwcReadInstallDataFromTCUPort5.argInt32.value);

    /* MAIN */

    SCU_Installation_TCU_ReadRunnable();

    /* POST */

}

void Rte_PirteSWCProto_SCU_Installation_TCU_AckRunnable(void) {
    /* PRE */

    /* MAIN */

    SCU_Installation_TCU_AckRunnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU(
            ImplDE_PirteSWCProto.SCU_Installation_TCU_AckRunnable.PirteSwcWriteAckDataToTCUPort6.acknowledgementVCU.value);

}

void Rte_PirteSWCProto_Pirte_ReadIMUSensor_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuGyroPort7_dataUInt16(
            ImplDE_PirteSWCProto.Pirte_ReadIMUSensor_Runnable.PirteSwcReadDataFromImuGyroPort7.dataUInt16.value);

    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuAccePort8_dataUInt16(
            ImplDE_PirteSWCProto.Pirte_ReadIMUSensor_Runnable.PirteSwcReadDataFromImuAccePort8.dataUInt16.value);

    /* MAIN */

    Pirte_ReadIMUSensor_Runnable();

    /* POST */

}

void Rte_PirteSWCProto_Pirte_ReadUltrasonicSensor_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromUltraSwcPort9_dataInt32(
            &ImplDE_PirteSWCProto.Pirte_ReadUltrasonicSensor_Runnable.PirteSwcReadDataFromUltraSwcPort9.dataInt32.value);

    /* MAIN */

    Pirte_ReadUltrasonicSensor_Runnable();

    /* POST */

}

