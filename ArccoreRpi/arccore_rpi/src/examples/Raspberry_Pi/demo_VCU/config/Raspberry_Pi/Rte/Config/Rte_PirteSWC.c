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
/* run Squawk*/
#if RUN_SQUAWK
char* UserClassPath = NULL;
extern int StartJVM(int argc, char *argv[]);

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
            Rte_DE_Array8 pluginInstallationVCU;
            Rte_DE_UInt32 argInt32;
        } PirteSwcReadInstallationDataFromTCUPort1;
    } Pirte_ReadInstallationDataFromTCU_Runnable;
    struct {
        struct {
            Rte_DE_UInt8 acknowledgementVCU;
        } PirteSwcWriteAcknowledgementDataToTCUPort2;
    } Pirte_WriteAcknowledgementDataToTCU_Runnable;
    struct {
        struct {
            Rte_DE_Array8 pluginCommunicationTCUtoVCU;
            Rte_DE_UInt32 argInt32;
        } PirteSwcReadCommunicationDataFromTCUPort10;
    } Pirte_ReadCommunicationDataFromTCU_Runnable;
    struct {
        struct {
            Rte_DE_UInt8 acknowledgementVCU;
            Rte_DE_Array8 pluginCommunicationVCUtoTCU;
        } PirteSwcWriteCommunicationDataToTCUPort11;
    } Pirte_WriteCommunicationDataFromTCU_Runnable;
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcWriteDataToMotorSwcPort3;
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcWriteSelectSpeedDataToMotorSwcPort15;
    } Pirte_WriteDataToMotor_Runnable;
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcWriteDataToServoSwcPort4;
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcWriteSelectSteerDataToServoSwcPort16;
    } Pirte_WriteDataToServo_Runnable;
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcReadDataFromAdcSwcPort5;
    } Pirte_ReadDataFromAdc_Runnable;
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcReadDataFromFrontWheelSwcPort6;
    } Pirte_ReadDataFromFrontWheel_Runnable;
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcReadDataFromRearWheelSwcPort7;
    } Pirte_ReadDataFromRearWheel_Runnable;
    struct {
        struct {
            Rte_DE_Array8 pluginCommunicationSCUtoVCU;
            Rte_DE_UInt32 argInt32;
        } PirteSwcReadDataFromSCUPort8;
    } Pirte_ReadDataFromSCU_Runnable;
    struct {
        struct {
            Rte_DE_Array8 pluginCommunicationVCUtoSCU;
            Rte_DE_UInt32 argInt32;
        } PirteSwcWriteDataToSCUPort9;
    } Pirte_WriteDataToSCU_Runnable;
    struct {
        struct {
            Rte_DE_Array8 PositionData;
            Rte_DE_UInt32 argInt32;
        } PirteSwcReadPositionDataFromTCUPort12;
    } Pirte_ReadPositionData_Runnable;
    struct {
        struct {
            Rte_DE_UInt32 dataInt32;
        } PirteSwcWriteLedPort13;
    } Pirte_WriteLedData_Runnable;
    struct {
        struct {
            Rte_DE_Array2 SpeedSteer;
        } PirteSwcReadSpeedSteerDataFromTCUPort14;
    } Pirte_ReadSpeedSteerData_Runnable;
} ImplDE_PirteSWCProto;

const Rte_CDS_PirteSWC PirteSWC_PirteSWCProto = {
    .Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU = &ImplDE_PirteSWCProto.Pirte_ReadInstallationDataFromTCU_Runnable.PirteSwcReadInstallationDataFromTCUPort1.pluginInstallationVCU,
    .Pirte_ReadInstallationDataFromTCU_Runnable_PirteSwcReadInstallationDataFromTCUPort1_argInt32 = &ImplDE_PirteSWCProto.Pirte_ReadInstallationDataFromTCU_Runnable.PirteSwcReadInstallationDataFromTCUPort1.argInt32,
    .Pirte_WriteAcknowledgementDataToTCU_Runnable_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU = &ImplDE_PirteSWCProto.Pirte_WriteAcknowledgementDataToTCU_Runnable.PirteSwcWriteAcknowledgementDataToTCUPort2.acknowledgementVCU,
    .Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU = &ImplDE_PirteSWCProto.Pirte_ReadCommunicationDataFromTCU_Runnable.PirteSwcReadCommunicationDataFromTCUPort10.pluginCommunicationTCUtoVCU,
    .Pirte_ReadCommunicationDataFromTCU_Runnable_PirteSwcReadCommunicationDataFromTCUPort10_argInt32 = &ImplDE_PirteSWCProto.Pirte_ReadCommunicationDataFromTCU_Runnable.PirteSwcReadCommunicationDataFromTCUPort10.argInt32,
    .Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU = &ImplDE_PirteSWCProto.Pirte_WriteCommunicationDataFromTCU_Runnable.PirteSwcWriteCommunicationDataToTCUPort11.acknowledgementVCU,
    .Pirte_WriteCommunicationDataFromTCU_Runnable_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU = &ImplDE_PirteSWCProto.Pirte_WriteCommunicationDataFromTCU_Runnable.PirteSwcWriteCommunicationDataToTCUPort11.pluginCommunicationVCUtoTCU,
    .Pirte_WriteDataToMotor_Runnable_PirteSwcWriteDataToMotorSwcPort3_dataInt32 = &ImplDE_PirteSWCProto.Pirte_WriteDataToMotor_Runnable.PirteSwcWriteDataToMotorSwcPort3.dataInt32,
    .Pirte_WriteDataToMotor_Runnable_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32 = &ImplDE_PirteSWCProto.Pirte_WriteDataToMotor_Runnable.PirteSwcWriteSelectSpeedDataToMotorSwcPort15.dataInt32,
    .Pirte_WriteDataToServo_Runnable_PirteSwcWriteDataToServoSwcPort4_dataInt32 = &ImplDE_PirteSWCProto.Pirte_WriteDataToServo_Runnable.PirteSwcWriteDataToServoSwcPort4.dataInt32,
    .Pirte_WriteDataToServo_Runnable_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32 = &ImplDE_PirteSWCProto.Pirte_WriteDataToServo_Runnable.PirteSwcWriteSelectSteerDataToServoSwcPort16.dataInt32,
    .Pirte_ReadDataFromAdc_Runnable_PirteSwcReadDataFromAdcSwcPort5_dataInt32 = &ImplDE_PirteSWCProto.Pirte_ReadDataFromAdc_Runnable.PirteSwcReadDataFromAdcSwcPort5.dataInt32,
    .Pirte_ReadDataFromFrontWheel_Runnable_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32 = &ImplDE_PirteSWCProto.Pirte_ReadDataFromFrontWheel_Runnable.PirteSwcReadDataFromFrontWheelSwcPort6.dataInt32,
    .Pirte_ReadDataFromRearWheel_Runnable_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32 = &ImplDE_PirteSWCProto.Pirte_ReadDataFromRearWheel_Runnable.PirteSwcReadDataFromRearWheelSwcPort7.dataInt32,
    .Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU = &ImplDE_PirteSWCProto.Pirte_ReadDataFromSCU_Runnable.PirteSwcReadDataFromSCUPort8.pluginCommunicationSCUtoVCU,
    .Pirte_ReadDataFromSCU_Runnable_PirteSwcReadDataFromSCUPort8_argInt32 = &ImplDE_PirteSWCProto.Pirte_ReadDataFromSCU_Runnable.PirteSwcReadDataFromSCUPort8.argInt32,
    .Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU = &ImplDE_PirteSWCProto.Pirte_WriteDataToSCU_Runnable.PirteSwcWriteDataToSCUPort9.pluginCommunicationVCUtoSCU,
    .Pirte_WriteDataToSCU_Runnable_PirteSwcWriteDataToSCUPort9_argInt32 = &ImplDE_PirteSWCProto.Pirte_WriteDataToSCU_Runnable.PirteSwcWriteDataToSCUPort9.argInt32,
    .Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_PositionData = &ImplDE_PirteSWCProto.Pirte_ReadPositionData_Runnable.PirteSwcReadPositionDataFromTCUPort12.PositionData,
    .Pirte_ReadPositionData_Runnable_PirteSwcReadPositionDataFromTCUPort12_argInt32 = &ImplDE_PirteSWCProto.Pirte_ReadPositionData_Runnable.PirteSwcReadPositionDataFromTCUPort12.argInt32,
    .Pirte_WriteLedData_Runnable_PirteSwcWriteLedPort13_dataInt32 = &ImplDE_PirteSWCProto.Pirte_WriteLedData_Runnable.PirteSwcWriteLedPort13.dataInt32,
    .Pirte_ReadSpeedSteerData_Runnable_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer = &ImplDE_PirteSWCProto.Pirte_ReadSpeedSteerData_Runnable.PirteSwcReadSpeedSteerDataFromTCUPort14.SpeedSteer
};

const Rte_Instance Rte_Inst_PirteSWC = &PirteSWC_PirteSWCProto;

/** === Runnables =================================================================================
 */

/** ------ PirteSWCProto -----------------------------------------------------------------------
 */

void Rte_PirteSWCProto_Pirte_ReadInstallationDataFromTCU_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_pluginInstallationVCU(
            ImplDE_PirteSWCProto.Pirte_ReadInstallationDataFromTCU_Runnable.PirteSwcReadInstallationDataFromTCUPort1.pluginInstallationVCU.value);

//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallationDataFromTCUPort1_argInt32(
//            &ImplDE_PirteSWCProto.Pirte_ReadInstallationDataFromTCU_Runnable.PirteSwcReadInstallationDataFromTCUPort1.argInt32.value);

    /* MAIN */

    Pirte_ReadInstallationDataFromTCU_Runnable();

    /* POST */

}

void Rte_PirteSWCProto_Pirte_WriteAcknowledgementDataToTCU_Runnable(void) {
    /* PRE */

    /* MAIN */

    Pirte_WriteAcknowledgementDataToTCU_Runnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAcknowledgementDataToTCUPort2_acknowledgementVCU(
            ImplDE_PirteSWCProto.Pirte_WriteAcknowledgementDataToTCU_Runnable.PirteSwcWriteAcknowledgementDataToTCUPort2.acknowledgementVCU.value);

}

void Rte_PirteSWCProto_Pirte_ReadCommunicationDataFromTCU_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_pluginCommunicationTCUtoVCU(
            ImplDE_PirteSWCProto.Pirte_ReadCommunicationDataFromTCU_Runnable.PirteSwcReadCommunicationDataFromTCUPort10.pluginCommunicationTCUtoVCU.value);

//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommunicationDataFromTCUPort10_argInt32(
//            &ImplDE_PirteSWCProto.Pirte_ReadCommunicationDataFromTCU_Runnable.PirteSwcReadCommunicationDataFromTCUPort10.argInt32.value);

    /* MAIN */

    Pirte_ReadCommunicationDataFromTCU_Runnable();

    /* POST */

}

void Rte_PirteSWCProto_Pirte_WriteCommunicationDataFromTCU_Runnable(void) {
    /* PRE */

    /* MAIN */

    Pirte_WriteCommunicationDataFromTCU_Runnable();

    /* POST */
//    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommunicationDataToTCUPort11_acknowledgementVCU(
//            ImplDE_PirteSWCProto.Pirte_WriteCommunicationDataFromTCU_Runnable.PirteSwcWriteCommunicationDataToTCUPort11.acknowledgementVCU.value);

    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommunicationDataToTCUPort11_pluginCommunicationVCUtoTCU(
            ImplDE_PirteSWCProto.Pirte_WriteCommunicationDataFromTCU_Runnable.PirteSwcWriteCommunicationDataToTCUPort11.pluginCommunicationVCUtoTCU.value);

}

void Rte_PirteSWCProto_Pirte_WriteDataToMotor_Runnable(void) {
    /* PRE */

    /* MAIN */

    Pirte_WriteDataToMotor_Runnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToMotorSwcPort3_dataInt32(
            ImplDE_PirteSWCProto.Pirte_WriteDataToMotor_Runnable.PirteSwcWriteDataToMotorSwcPort3.dataInt32.value);

    //TODO: Primary suspect
//    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteSelectSpeedDataToMotorSwcPort15_dataInt32(
//            ImplDE_PirteSWCProto.Pirte_WriteDataToMotor_Runnable.PirteSwcWriteSelectSpeedDataToMotorSwcPort15.dataInt32.value);

}

void Rte_PirteSWCProto_Pirte_WriteDataToServo_Runnable(void) {
    /* PRE */

    /* MAIN */

    Pirte_WriteDataToServo_Runnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToServoSwcPort4_dataInt32(
            ImplDE_PirteSWCProto.Pirte_WriteDataToServo_Runnable.PirteSwcWriteDataToServoSwcPort4.dataInt32.value);

    //TODO: Primary suspect
//    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteSelectSteerDataToServoSwcPort16_dataInt32(
//            ImplDE_PirteSWCProto.Pirte_WriteDataToServo_Runnable.PirteSwcWriteSelectSteerDataToServoSwcPort16.dataInt32.value);

}

void Rte_PirteSWCProto_Pirte_ReadDataFromAdc_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromAdcSwcPort5_dataInt32(
            &ImplDE_PirteSWCProto.Pirte_ReadDataFromAdc_Runnable.PirteSwcReadDataFromAdcSwcPort5.dataInt32.value);

    /* MAIN */

    Pirte_ReadDataFromAdc_Runnable();

    /* POST */

}

void Rte_PirteSWCProto_Pirte_ReadDataFromFrontWheel_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromFrontWheelSwcPort6_dataInt32(
            &ImplDE_PirteSWCProto.Pirte_ReadDataFromFrontWheel_Runnable.PirteSwcReadDataFromFrontWheelSwcPort6.dataInt32.value);

    /* MAIN */

    Pirte_ReadDataFromFrontWheel_Runnable();

    /* POST */

}

void Rte_PirteSWCProto_Pirte_ReadDataFromRearWheel_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromRearWheelSwcPort7_dataInt32(
            &ImplDE_PirteSWCProto.Pirte_ReadDataFromRearWheel_Runnable.PirteSwcReadDataFromRearWheelSwcPort7.dataInt32.value);

    /* MAIN */

    Pirte_ReadDataFromRearWheel_Runnable();

    /* POST */

}

void Rte_PirteSWCProto_Pirte_ReadDataFromSCU_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromSCUPort8_pluginCommunicationSCUtoVCU(
            ImplDE_PirteSWCProto.Pirte_ReadDataFromSCU_Runnable.PirteSwcReadDataFromSCUPort8.pluginCommunicationSCUtoVCU.value);

    //TODO: Not in v3
//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromSCUPort8_argInt32(
//            &ImplDE_PirteSWCProto.Pirte_ReadDataFromSCU_Runnable.PirteSwcReadDataFromSCUPort8.argInt32.value);

    /* MAIN */

    Pirte_ReadDataFromSCU_Runnable();





    /* POST */

}

void Rte_PirteSWCProto_Pirte_WriteDataToSCU_Runnable(void) {
    /* PRE */

    /* MAIN */

    Pirte_WriteDataToSCU_Runnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToSCUPort9_pluginCommunicationVCUtoSCU(
            ImplDE_PirteSWCProto.Pirte_WriteDataToSCU_Runnable.PirteSwcWriteDataToSCUPort9.pluginCommunicationVCUtoSCU.value);

//    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToSCUPort9_argInt32(
//            ImplDE_PirteSWCProto.Pirte_WriteDataToSCU_Runnable.PirteSwcWriteDataToSCUPort9.argInt32.value);

}

void Rte_PirteSWCProto_Pirte_ReadPositionData_Runnable(void) {
    /* PRE */
    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadPositionDataFromTCUPort12_PositionData(
            ImplDE_PirteSWCProto.Pirte_ReadPositionData_Runnable.PirteSwcReadPositionDataFromTCUPort12.PositionData.value);

//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadPositionDataFromTCUPort12_argInt32(
//            &ImplDE_PirteSWCProto.Pirte_ReadPositionData_Runnable.PirteSwcReadPositionDataFromTCUPort12.argInt32.value);

    /* MAIN */

    Pirte_ReadPositionData_Runnable();

    /* POST */

}

void Rte_PirteSWCProto_Pirte_WriteLedData_Runnable(void) {
    /* PRE */

    /* MAIN */

    Pirte_WriteLedData_Runnable();

    /* POST */
    Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteLedPort13_dataInt32(
            ImplDE_PirteSWCProto.Pirte_WriteLedData_Runnable.PirteSwcWriteLedPort13.dataInt32.value);

}

void Rte_PirteSWCProto_Pirte_ReadSpeedSteerData_Runnable(void) {
	//TODO: Not in v3
//    /* PRE */
//    Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadSpeedSteerDataFromTCUPort14_SpeedSteer(
//            ImplDE_PirteSWCProto.Pirte_ReadSpeedSteerData_Runnable.PirteSwcReadSpeedSteerDataFromTCUPort14.SpeedSteer.value);
//
//    /* MAIN */
//
//    Pirte_ReadSpeedSteerData_Runnable();
//
//    /* POST */

}

