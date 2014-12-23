

#ifndef IOHWAB_DIGITAL_H_
#define IOHWAB_DIGITAL_H_

#include "Std_Types.h"

Std_ReturnType IoHw_Read_AdcSensor(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data);

Std_ReturnType IoHw_Read_FrontWheelSensor(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data);

Std_ReturnType IoHw_Read_RearWheelSensor(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data);

Std_ReturnType IoHw_WriteSpeed_DutyCycle(/*IN*/uint32 portDefArg1, /*OUT*/uint8 * DutyCycle);

Std_ReturnType IoHw_WriteServo_DutyCycle(/*IN*/uint32 portDefArg1, /*OUT*/uint8 * DutyCycle);

Std_ReturnType IoHw_Write_Led(/*IN*/uint32 portDefArg1, /*IN*/uint8 portDefArg2, /*OUT*/uint32 * Pin, /*OUT*/uint8 * Level);
#endif /* IOHWAB_DIGITAL_H_ */


