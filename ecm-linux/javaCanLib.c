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

#define TCU_FILTER 0x4

static int CAN_DLC = 2;
extern int Can_Init(void);
extern void Can_Send_Package(uint32 can_socket, struct can_frame frame, uint32 appsize, uint8* bytes);
extern uint8* Can_Read_Package (uint32 can_socket, uint32 can_ID);
extern uint32 packageTotalSize;

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
                //printf("%d,",frame.data[i]);
        }
        //printf("\r\n");
        return write(chn_num, &frame, sizeof(struct can_frame));
}

void sendBigData(int chn_num, int can_id, int can_dlc, int dataSize, char* data) {
	struct can_frame frame;
	frame.can_id = can_id;
	frame.can_dlc = can_dlc;
	Can_Send_Package((uint32)chn_num, frame, (uint32)dataSize, (uint8*)data);
}

char* receiveData(int chn_num, int can_id) {
	//printf("JNA: receiveData\r\n");
	return (char*)Can_Read_Package((uint32)chn_num, (uint32)can_id);
	/*if(data != NULL) {
		int i;
		for(i=0;i<packageTotalSize;i++) {
			printf("[%d]", data[i]);
		}
		printf("\r\n");
	}*/
	//return (char*)data;
}

char* receiveByteData(int chn_num, int can_id) {
	//printf("receiving ...\r\n");
	struct can_filter rfilter;
	struct can_frame frame;
    uint32 CAN_ID = 0;
	struct timeval tv = {0,1};
	int len = 0;
	char tmp[9];
	uint8 i;
	
	rfilter.can_id   = TCU_FILTER; /* SFF frame */
	rfilter.can_mask = TCU_FILTER;
	setsockopt(chn_num, SOL_SOCKET, SO_RCVTIMEO, (char *)&tv, sizeof(struct timeval));
    setsockopt(chn_num, SOL_CAN_RAW, CAN_RAW_FILTER, &rfilter, sizeof(rfilter));
	
	//frame.can_id = can_id;
	read(chn_num, &frame, sizeof(struct can_frame));
        CAN_ID = frame.can_id;
	
    if(CAN_ID != can_id){
        //printf("infor: canid not match\r\n");
        return NULL;
    }else{
		len = frame.can_dlc;
		printf("received %d bytes data\r\n", len);
		printf("@ Receive character %s\r\n", frame.data);
	
		tmp[0] = len;
		for(i=1;i<=len;i++) {
			tmp[i] = frame.data[i-1];
		}
		
	}
	char* res = tmp;
	return res;
}

int getPackageSize() {
	return (int)packageTotalSize;
}

void resetPackageSize() {
	packageTotalSize = 0;
}
