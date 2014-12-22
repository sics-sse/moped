
#include <stdio.h>
#include "Os.h"
#include "Rte_UltraSWC.h"
#include "Uart.h"

char id = 3;

uint32 value = 1000;

void UltrasonicRunnable(void) {
	UInt32 distance = 0;

    Rte_Call_UltraSwcDataInPort1_Read(&distance);

//    printf("infor: UltraSWC distance: %d\r\n", distance);
	Rte_IWrite_UltrasonicRunnable_UltraSwcDataOutPort2_dataInt32(distance);
}
