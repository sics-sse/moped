

#ifndef IOHWAB_DIGITAL_H_
#define IOHWAB_DIGITAL_H_

#include "Std_Types.h"

Std_ReturnType UltraSensorRead(/*IN*/uint32 portDefArg1, /*IN*/uint32* Data);

Std_ReturnType ImuSensorRead(/*IN*/uint16 portDefArg1, /*IN*/uint16* Data);
#endif /* IOHWAB_DIGITAL_H_ */


