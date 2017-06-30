#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h> 
#include <net/if.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <linux/can.h>
#include <linux/can/raw.h>
#include "../apptest.h"

static int CAN_DLC = 2;
extern int Can_Init(void);
extern void Can_Send_Package(uint32 can_socket, struct can_frame frame, uint32 appsize, uint8* bytes);
extern uint8* Can_Read_Package (uint32 can_socket, uint32 can_ID);
extern uint32 packageTotalSize;

char normalCanData[9];

int init_can(void) {
	return Can_Init();
}

int sendData(int chn_num, int can_id, char* data) {
        struct can_frame frame;
        frame.can_id = can_id;
        frame.can_dlc = CAN_DLC;
        int i;
        for(i=0;i<CAN_DLC;i++) {
                frame.data[i] = data[i];
        }
        return write(chn_num, &frame, sizeof(struct can_frame));
}

void sendBigData(int chn_num, int can_id, int can_dlc, int dataSize, char* data) {
	struct can_frame frame;
	frame.can_id = can_id;
	frame.can_dlc = can_dlc;
	Can_Send_Package((uint32)chn_num, frame, (uint32)dataSize, (uint8*)data);
}

char* receiveData(int chn_num, int can_id) {
  //printf("javaCanLib 1 %d %d %d\r\n", chn_num, can_id, packageTotalSize);
  char *p = (char*) Can_Read_Package((uint32)chn_num, (uint32)can_id);
  //printf("javaCanLib 2 %d %d %d -> %p\r\n", chn_num, can_id, packageTotalSize, p);
  return p;
}

extern char* Can_Read_Frame(int ch_num, int can_id);
char* receiveByteData(int ch_num, int can_id) {
	return Can_Read_Frame(ch_num, can_id);
}

int getPackageSize() {
	//printf("@ getPackageSize: %d", packageTotalSize);
	return (int)packageTotalSize;
}

void resetPackageSize() {
	packageTotalSize = 0;
}
