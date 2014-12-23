#ifndef RTE_INTERNAL_H_
#define RTE_INTERNAL_H_

#include <Rte_DataHandleType.h>

#define Rte_EnterProtectedRegion() 	imask_t state; \
	                                Irq_Save(state)

#define Rte_ExitProtectedRegion()     Irq_Restore(state)

typedef struct {
    struct {
        struct { /* --- ImuSWCProto --- */
            struct {
                Array3 dataUInt16;
            } ImuSwcWriteGyroDataToPirteSwcPort1;
            struct {
                Array3 dataUInt16;
            } ImuSwcWriteAcceDataToPirteSwcPort2;
        } ImuSWCProto;
    } ImuSWC;

    struct {
        struct { /* --- PirteSWCProto --- */
            struct {
                UInt32 argInt32;
                Array8 pluginCommunication;
            } PirteSwcWriteDataToVCUPort2;
            struct {
                UInt32 argInt32;
                Array8 pluginCommunication;
            } PirteSwcWriteCommDataToTCUPort4;
            struct {
                UInt8 acknowledgementVCU;
            } PirteSwcWriteAckDataToTCUPort6;
            struct {
                UInt32 argInt32;
                Array8 pluginCommunication;
            } PirteSwcReadDataFromVCUPort1;
            struct {
                UInt32 argInt32;
                Array8 pluginCommunication;
            } PirteSwcReadCommDataFromTCUPort3;
            struct {
                UInt32 argInt32;
                Array8 pluginCommunication;
            } PirteSwcReadInstallDataFromTCUPort5;
        } PirteSWCProto;
    } PirteSWC;

    struct {
        struct { /* --- UltraSWCProto --- */
            struct {
                UInt32 dataInt32;
            } UltraSwcDataOutPort2;
        } UltraSWCProto;
    } UltraSWC;

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

/** === ImuSWC ======================================================================= */
/** --- ImuSWCProto -------------------------------------------------------------------- */

/** ------ ImuSwcToIoHwAcceDataInPort4 */
Std_ReturnType Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwAcceDataInPort4_Read(/*IN*/UInt16 const * Data);

/** ------ ImuSwcToIoHwGyroDataInPort3 */
Std_ReturnType Rte_Call_ImuSWC_ImuSWCProto_ImuSwcToIoHwGyroDataInPort3_Read(/*IN*/UInt16 const * Data);

/** ------ ImuSwcWriteAcceDataToPirteSwcPort2 */
Std_ReturnType Rte_Write_ImuSWC_ImuSWCProto_ImuSwcWriteAcceDataToPirteSwcPort2_dataUInt16(/*IN*/UInt16 const * data);

/** ------ ImuSwcWriteGyroDataToPirteSwcPort1 */
Std_ReturnType Rte_Write_ImuSWC_ImuSWCProto_ImuSwcWriteGyroDataToPirteSwcPort1_dataUInt16(/*IN*/UInt16 const * data);

/** === IoHwAb ======================================================================= */
/** --- IoHwAbService -------------------------------------------------------------------- */

/** ------ IoHwAcceDataInPort2 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbService_IoHwAcceDataInPort2_Read(/*IN*/UInt16 const * Data);

/** ------ IoHwGyroDataInPort1 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbService_IoHwGyroDataInPort1_Read(/*IN*/UInt16 const * Data);

/** ------ IoHwUltraDataInPort3 */
Std_ReturnType Rte_Call_IoHwAb_IoHwAbService_IoHwUltraDataInPort3_Read(/*IN*/UInt32 const * Data);

/** === PirteSWC ======================================================================= */
/** --- PirteSWCProto -------------------------------------------------------------------- */

/** ------ PirteSwcReadCommDataFromTCUPort3 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommDataFromTCUPort3_argInt32(/*OUT*/UInt32 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadCommDataFromTCUPort3_pluginCommunication(/*OUT*/UInt8 * data);

/** ------ PirteSwcReadDataFromImuAccePort8 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuAccePort8_dataUInt16(/*OUT*/UInt16 * data);

/** ------ PirteSwcReadDataFromImuGyroPort7 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromImuGyroPort7_dataUInt16(/*OUT*/UInt16 * data);

/** ------ PirteSwcReadDataFromUltraSwcPort9 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromUltraSwcPort9_dataInt32(/*OUT*/UInt32 * data);

/** ------ PirteSwcReadDataFromVCUPort1 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromVCUPort1_argInt32(/*OUT*/UInt32 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadDataFromVCUPort1_pluginCommunication(/*OUT*/UInt8 * data);

/** ------ PirteSwcReadInstallDataFromTCUPort5 */
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallDataFromTCUPort5_argInt32(/*OUT*/UInt32 * data);
Std_ReturnType Rte_Read_PirteSWC_PirteSWCProto_PirteSwcReadInstallDataFromTCUPort5_pluginCommunication(/*OUT*/UInt8 * data);

/** ------ PirteSwcWriteAckDataToTCUPort6 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteAckDataToTCUPort6_acknowledgementVCU(/*IN*/UInt8 data);

/** ------ PirteSwcWriteCommDataToTCUPort4 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommDataToTCUPort4_argInt32(/*IN*/UInt32 data);
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteCommDataToTCUPort4_pluginCommunication(/*IN*/UInt8 const * data);

/** ------ PirteSwcWriteDataToVCUPort2 */
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToVCUPort2_argInt32(/*IN*/UInt32 data);
Std_ReturnType Rte_Write_PirteSWC_PirteSWCProto_PirteSwcWriteDataToVCUPort2_pluginCommunication(/*IN*/UInt8 const * data);

/** === UltraSWC ======================================================================= */
/** --- UltraSWCProto -------------------------------------------------------------------- */

/** ------ UltraSwcDataInPort1 */
Std_ReturnType Rte_Call_UltraSWC_UltraSWCProto_UltraSwcDataInPort1_Read(/*IN*/UInt32 const * Data);

/** ------ UltraSwcDataOutPort2 */
Std_ReturnType Rte_Write_UltraSWC_UltraSWCProto_UltraSwcDataOutPort2_dataInt32(/*IN*/UInt32 data);

#endif /* RTE_INTERNAL_H_ */

