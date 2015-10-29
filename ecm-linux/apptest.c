#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
 
#include <net/if.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <sys/time.h>
 
#include <linux/can.h>
#include <linux/can/raw.h>
#include "apptest.h"

#define SEQUENCE_NUMBER         	 0x0F
#define NO_FRAME 					 0xDEAD

#define FRAME_TYPE        		     0xF0
#define SINGLE_FRAME  				 0x0
#define SINGLE_FRAME_DATA_SIZE       7
#define SF_PCI_SIZE 				 1

#define FIRST_FRAME  				 0x1
#define FIRST_FRAME_DATA_SIZE        4
#define FF_PCI_SIZE 				 4

#define CONSECUTIVE_FRAME			 0x2
#define CONSECUTIVE_FRAME_DATA_SIZE  7
#define CF_PCI_SIZE 				 1

#define TCU_FILTER                   0x4

uint8 frameType = 0xFF;
uint16 nextFrameRead = 0;

uint32 app_index_write=0;
uint32 app_index_read = 0;

uint32 packageTotalSize = 0;
uint32 package_read_index = 0;

boolean packageReadDone = false;
uint8 package[2000];

struct can_frame frame;
char normalCanData[9];
//char app[837] = {-34, -83, -66, -17, 0, 1, 0, 1, 0, 0, 0, 2, 0, 81, 70, 89, 0, 16, 109, 101, 109, 111, 114, 121, 58, 98, 111, 111, 116, 115, 116, 114, 97, 112, 0, 0, 0, 4, 0, 0, 3, 0, -17, -25, -1, 11, -4, 63, 5, 56, -123, 2, -3, -65, 32, 64, 32, 0, 16, 0, 65, 64, 8, 126, 1, -96, 0, 0, -36, -24, 1, 0, -80, -67, 4, 0, 8, -64, 4, 0, 32, -64, 4, 0, 1, 0, 0, 0, 4, 0, 0, 0, 112, -64, 4, 0, 120, -64, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 88, -67, 4, 0, 1, 0, 0, 0, 5, 0, 0, 0, -92, 95, 1, 0, -72, -67, 4, 0, -4, 32, 1, 0, -72, -67, 4, 0, 4, -66, 4, 0, 104, -66, 4, 0, 36, 42, 4, 0, 0, 0, 0, 0, 12, 7, 0, 0, 120, 10, 0, 0, -68, -66, 4, 0, -60, -66, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, 80, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, 4, 9, 45, 0, 0, 0, -52, 7, 1, 0, -108, 7, 0, 0, -80, 7, 0, 0, -52, 7, 0, 0, -12, 7, 0, 0, 68, 8, 0, 0, 96, 8, 0, 0, -116, 8, 0, 0, -88, 8, 0, 0, -52, 8, 0, 0, 4, 9, 0, 0, 68, -66, 4, 0, 23, 0, 0, 0, 0, 3, 34, 4, -72, -67, 4, 0, 105, 0, 0, 0, -128, -52, 2, 0, -61, 22, 16, -84, 88, 23, 16, -84, 88, 25, 16, -84, 88, 24, 16, -84, 88, 21, 16, -84, 88, 26, 16, -84, 88, -67, 0, 0, 9, 0, 0, 0, -52, 7, 1, 0, -124, -66, 4, 0, -96, -66, 4, 0, 23, 0, 0, 0, 0, 3, 34, 4, -72, -67, 4, 0, 33, 0, 0, 0, -128, -52, 2, 0, -61, 64, 17, -82, 0, -30, 64, -64, 23, 0, 0, 0, 0, 7, 67, 4, -72, -67, 4, 0, 81, 0, 0, 0, -128, -52, 2, 0, -72, 1, 20, 16, -84, 88, 18, -23, 18, -82, 0, 49, 33, -88, 10, 19, 16, -84, 88, -67, 1, 0, 0, 0, 92, 123, 1, 0, 45, 0, 0, 0, -52, 7, 1, 0, -64, 35, 0, 0, 12, 7, 0, 0, -72, -67, 4, 0, -8, -66, 4, 0, 16, -65, 4, 0, 52, -65, 4, 0, 80, -65, 4, 0, -116, -65, 4, 0, -68, -65, 4, 0, -44, -65, 4, 0, -12, -65, 4, 0, 61, 0, 0, 0, 24, -54, 2, 0, 67, 76, 73, 45, 109, 97, 105, 110, 32, 100, 111, 110, 101, 13, 10, 0, 101, 0, 0, 0, 24, -54, 2, 0, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 73, 110, 112, 117, 116, 46, 109, 97, 105, 110, 40, 41, 13, 10, 0, 0, 0, 69, 0, 0, 0, 24, -54, 2, 0, 69, 105, 108, 101, 105, 116, 32, 105, 115, 32, 98, 97, 99, 107, 33, 13, 10, 0, 0, 0, -59, 0, 0, 0, 24, -54, 2, 0, 72, 101, 108, 108, 111, 32, 87, 111, 114, 108, 100, 32, 102, 114, 111, 109, 32, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 73, 110, 112, 117, 116, 32, 40, 111, 110, 108, 121, 32, 98, 121, 116, 101, 115, 41, 33, 13, 10, 0, 0, 0, -107, 0, 0, 0, 24, -54, 2, 0, 73, 115, 32, 105, 116, 32, 114, 101, 97, 108, 108, 121, 32, 119, 111, 114, 107, 105, 110, 103, 32, 100, 121, 110, 97, 109, 105, 99, 97, 108, 108, 121, 63, 63, 63, 13, 10, 0, 0, 0, 65, 0, 0, 0, 24, -54, 2, 0, 74, 97, 107, 111, 98, 32, 105, 115, 32, 104, 101, 114, 101, 33, 13, 10, 93, 0, 0, 0, 24, -54, 2, 0, 79, 104, 44, 32, 121, 101, 115, 44, 32, 105, 116, 32, 105, 115, 33, 33, 33, 33, 33, 33, 33, 13, 10, 0, 41, 0, 0, 0, 24, -54, 2, 0, 110, 101, 119, 32, 116, 114, 121, 46, 46, 46, 0, 0, 65, 0, 0, 0, 24, -54, 2, 0, 67, 108, 97, 115, 115, 76, 111, 97, 100, 101, 114, 73, 110, 112, 117, 116, 5, 0, 0, 0, 88, -125, 1, 0, 40, -64, 4, 0, 16, -128, 1, 0, -72, -67, 4, 0, 60, -64, 4, 0, -80, -67, 4, 0, -91, 0, 0, 0, -104, -55, 0, 0, 12, 2, 15, 1, 10, 11, 112, 114, 105, 110, 116, 83, 116, 114, 105, 110, 103, 10, 3, 11, -119, 32, 0, 6, 60, 105, 110, 105, 116, 62, 1, 9, 9, 1, 4, 109, 97, 105, 110, 10, 34, 0, 0, 0, 1, 0, 0, 0, -96, 5, 2, 0, 1, 0, 0, 0, 48, 5, 2, 0, -1};

//can bus initialization

uint32 Can_Init(void){
    int s;
	struct sockaddr_can addr;
	struct ifreq ifr;
	char const *ifname = "can0";
	struct can_filter rfilter;
	
	struct timeval tv = {0,1};
	
	rfilter.can_id   = TCU_FILTER; 
    rfilter.can_mask = TCU_FILTER;
	
	if((s = socket(PF_CAN, SOCK_RAW, CAN_RAW)) < 0) {
		return -1;
	}
	strcpy(ifr.ifr_name, ifname);
	ioctl(s, SIOCGIFINDEX, &ifr);
 
	addr.can_family  = AF_CAN;
	addr.can_ifindex = ifr.ifr_ifindex; 
	if(bind(s, (struct sockaddr *)&addr, sizeof(addr)) < 0) {
		return -2;
	}
	
	setsockopt(s, SOL_SOCKET, SO_RCVTIMEO, (char *)&tv, sizeof(struct timeval));
    setsockopt(s, SOL_CAN_RAW, CAN_RAW_FILTER, &rfilter, sizeof(rfilter));
	
	return s;
}

void Can_Send_First_Frame(uint32 can_socket, uint32 appsize, struct can_frame frame, uint8* app){
    uint8 length1 = 0;
	uint8 length2 = 0;
	uint8 length3 = 0;
	uint8 length4 = 0;
	uint8 nbytes = 0;
	uint8 i;
	//todo: appsize should smaller than 4095
	//send first frame, calculate the data length
	length1 = (uint8)(appsize >> 24 ) & 0x0F;
	length2 = (uint8)(appsize >> 16 ) & 0xFF;
	length3 = (uint8)(appsize >> 8 ) & 0xFF;
	length4 = (uint8) appsize & 0xFF;
	//construct the first frame
	frame.data[0] = (FIRST_FRAME << 4) + length1;
	frame.data[1] = length2;
	frame.data[2] = length3;
	frame.data[3] = length4;
	//printf("infor: send first frame %d %d %d %d ", frame.data[0], frame.data[1], frame.data[2], frame.data[3]);
	for(i = 0; i < FIRST_FRAME_DATA_SIZE; i++){
		frame.data[i + FF_PCI_SIZE] = app[app_index_write];
		//printf("%d(%d_index(%d)) ", frame.data[i + FF_PCI_SIZE], app[app_index_write],app_index_write);
		app_index_write++;
	}
//	printf("\r\n");
	//send first frame
	do{
		nbytes = write(can_socket, &frame, sizeof(struct can_frame));
	}while(nbytes < 0);
    usleep(20000);
}

void Can_Send_Consecutive_Frame(uint32 can_socket, uint32 count, struct can_frame frame, uint8* app){
    uint32 i, j;
int cnt;
	uint8 sequnceNumber = 0;
	uint8 nbytes = 0;
	//printf("infor: send consecutive frame(s) %d\r\n", count);
	for(i = 0; i < count; i++){
//printf("consec frame %d %d\r\n", i, sequnceNumber);
		//send consecutive frame, construct the first frame
		frame.data[0] = (uint8)(CONSECUTIVE_FRAME << 4) + (sequnceNumber & 0xF);
		//printf("%d ", frame.data[0]);
		for(j = 0; j < CONSECUTIVE_FRAME_DATA_SIZE; j++){
			frame.data[j + CF_PCI_SIZE] = app[app_index_write];
			//printf("%d(%d_index(%d)) ", frame.data[j + CF_PCI_SIZE],app[app_index_write],app_index_write);	
			app_index_write++;
			
		}
		//printf("\r\n");
		//send consecutive frame
cnt = 0;
		do{
	cnt += 1;
			nbytes = write(can_socket, &frame, sizeof(struct can_frame));
		}while(nbytes < 0);

                if(sequnceNumber >= 15){
 //     	printf("seqnum = 0 %d %d %d %d\r\n", i, count, cnt, nbytes);
                        sequnceNumber = 0;
                }else{
                        sequnceNumber++;
                }
                usleep(20000);
        }
  //  printf("\r\n");
}

void Can_Send_Package (uint32 can_socket, struct can_frame frame, uint32 appsize, uint8* bytes){
	uint32 count = 0;
	app_index_write = 0;
 	//send first frame
    Can_Send_First_Frame(can_socket, appsize, frame, bytes);
	if((appsize-FIRST_FRAME_DATA_SIZE)%CONSECUTIVE_FRAME_DATA_SIZE == 0){
		count = (appsize-FIRST_FRAME_DATA_SIZE)/CONSECUTIVE_FRAME_DATA_SIZE;
	}else{
		count = (appsize-FIRST_FRAME_DATA_SIZE)/CONSECUTIVE_FRAME_DATA_SIZE + 1;
	}
	
	//send consecutive frame
printf("cons1\r\n");
	Can_Send_Consecutive_Frame(can_socket, count, frame, bytes);
printf("cons2\r\n");
}

void Can_Read_Single_Frame(struct can_frame frame, uint8* package){
    uint8 package_index = 0;
	uint8 dataLength = 0;
	uint8 i = 0;
	dataLength = frame.data[0] & 0x0F;
	if(dataLength){
		printf("infor: read single frame %d", frame.data[0]);
		for(i = 0; i < dataLength; i ++){
			package[package_index] = frame.data[i + SF_PCI_SIZE];
			package_index++;
			printf("%d ", frame.data[i + SF_PCI_SIZE]);
		}
		printf("\r\n");
		packageReadDone = true;
	}
}

void Can_Read_First_Frame(struct can_frame frame, uint8* package){
    uint8 i = 0;
	package_read_index = 0;
	nextFrameRead = 0;
    for(i = 0; i < FIRST_FRAME_DATA_SIZE; i++){
    	package[package_read_index] = frame.data[i+FF_PCI_SIZE];
    	package_read_index++;
    	// printf("%d ",package[i]);
		//printf("FF_Index: %d\r\n",package_read_index);
    }
	// printf("\r\n");
}

void Can_Read_Consecutive_Frame(struct can_frame frame, uint8* package){
	//printf("infor: consecutive frame\r\n");
    uint32 i = 0;
	uint16 sequenceNumber = frame.data[0] & SEQUENCE_NUMBER;
	//printf("sequenceNumber = %d\r\n", sequenceNumber);
	if(sequenceNumber == nextFrameRead){
		//printf("nextFrameRead = %d\r\n", nextFrameRead);
		for (i = 0; i < CONSECUTIVE_FRAME_DATA_SIZE; i++) {
			package[package_read_index] = frame.data[i+CF_PCI_SIZE];
			package_read_index++;
			//printf("CF_Index: %d\r\n",package_read_index);
			if(packageTotalSize == package_read_index){
				//printf("endSize: %d\r\n",package_read_index);
				nextFrameRead = NO_FRAME;
				packageReadDone = true;
				break;
			}
		}
		if(nextFrameRead >= 15){
			if(nextFrameRead != NO_FRAME){
				nextFrameRead = 0;
			}
		}else{
			nextFrameRead++;
			// printf("nextFrameRead = %d\r\n", nextFrameRead);
		}
/*		if (packageTotalSize == package_read_index) {
			printf("infor: app data ");
			for (i = 0; i < package_read_index; i++) {
				printf("%d ", package[i]);
			}
			package_read_index = 0;
			//printf("//total %d\r\n", package_read_index);
		}*/
	}else{
		//nothing
	}
}

uint8* Can_Read_Package (int can_socket, uint32 can_ID){
	uint32 canID = 0;
	//printf("infor: can read package\r\n");
    do{
		read(can_socket, &frame, sizeof(struct can_frame));
		canID = frame.can_id;
		if(canID != can_ID){
		  if (((frame.data[0] & FRAME_TYPE) >> 4) == FIRST_FRAME) {
		    printf("infor: p canId not match %d %d\r\n", canID, can_ID);
		  }
		  // The code used to return NULL here, which meant that we
		  // gave up just because one foreign message appeared, and
		  // with two senders it meant that we almost never could read
		  // a full package.
		  // Now, instead, it may happen that the message we wait for
		  // was lost, and no other message will come. Then we just
		  // loop here, which is not good.
		  // The more reliable future solution is to keep separate buffers
		  // for the possible senders.

		  // Update: this needs more thought. While simultaneous
		  // publishing works better with the change described above,
		  // installation acknowledgements don't arrive anymore,
		  // which is a worse problem.
		  return NULL;

		} else {
		  //printf("infor: can_id %d\r\n", canID);
			frameType = (frame.data[0] & FRAME_TYPE) >> 4;
			switch(frameType){
				case SINGLE_FRAME:
					//uint8 dataLength;
					//dataLength = frame.data[0] & 0x0F;
		
					Can_Read_Single_Frame(frame, package);
				break;
			
				case FIRST_FRAME:
					packageTotalSize = ((frame.data[0] & 0x0F) << 24) + (frame.data[1] << 16) + (frame.data[2] << 8) + frame.data[3];
					//printf("infor: read first frame size %d\r\n", packageTotalSize);
					//package = (uint8 *) malloc( sizeof(uint8)*packageTotalSize );
					Can_Read_First_Frame(frame, package);
				break;
			
				case CONSECUTIVE_FRAME:
					//printf("infor: read consecutive\r\n");
					Can_Read_Consecutive_Frame(frame, package);
				break;
			
				default:
				break;
			}
		}
	}while(packageReadDone == false);
    packageReadDone = false;
	return package; 
}

//static int test_loop;

char* Can_Read_Frame(int chn_num, int can_id) {
        //printf("receiving ...\r\n");
        uint32 CAN_ID = 0;
        int len = 0;
        uint8 i;

        read(chn_num, &frame, sizeof(struct can_frame));
        CAN_ID = frame.can_id;

       if(CAN_ID != can_id){
			//printf("infor: canid not match\r\n");
			return 0;
        }else{
                len = frame.can_dlc;
                //printf("received %d bytes data\r\n", len);
                //printf("infor: %d loop\r\n", test_loop);
                //test_loop++;
                //printf("@ Receive character %s\r\n", frame.data);
                normalCanData[0] = len;
                for(i=1;i<=len;i++) {
                    normalCanData[i] = frame.data[i-1];
                }

        }
      	CAN_ID = 0;
		frame.can_id = 0;
        return &normalCanData[0];
}
/*
int i_test = 0;
int main(int argc, char **argv)
{
	//variable define
	uint32 i;
	//uint8 nbytes = 0;
	//uint32 appsize=0;
    uint32 can_socket = 0;
	//uint32 acknowledgementVCU = 0xFF;
	uint32 can_ID;
	// struct can_frame frame;
   
	//uint8 bytes[1000];
	uint8* buffer;
    //CAN socket initialization
    can_socket = Can_Init();
    if(can_socket == -1){
		printf("Error while opening socket\r\n");
		exit(1);
    }else if(can_socket == -2){
	    printf("Error while binding socket\r\n");
		exit(1);
	}else{
	    printf("successful init CAN bus\r\n");
	}

	//int test_len;
while(1){*/
	/*frame.can_id  = 1601;
	frame.can_dlc = 8;
	//prepare the data
	appsize = prepareApp(bytes);
	Can_Send_Package(can_socket, frame, appsize, bytes);*/
/*
	//read acknowledgement	
	can_ID = 1284;
	buffer = Can_Read_Frame(can_socket, can_ID);
	if(buffer != NULL){
		printf("infor: app data ");
		for (i = 0; i < buffer[0]; i++) {
	        printf("@@@@ %d ", buffer[i]);
		}
		printf("\r\n");
	}
		
	can_ID = 1292;
	buffer = Can_Read_Package(can_socket, can_ID);
	if(buffer != NULL){
		printf("infor: app data ");
		for (i = 0; i < packageTotalSize; i++) {
			printf("%d ", buffer[i]);
		}
		printf("//total %d\r\n", package_read_index);
	}
	
	can_ID = 1129;
	buffer = Can_Read_Package(can_socket, can_ID);
	
	if(buffer != NULL){
		printf("infor: app data ");
		for (i = 0; i < packageTotalSize; i++) {
			printf("%d ", buffer[i]);
		}
		printf("//total %d\r\n", package_read_index);
	}
		
	can_ID = 1124;
	buffer = receiveByteData(can_socket, can_ID);
	
	if(buffer != NULL){
		printf("infor: app data ");
		for (i = 0; i < packageTotalSize; i++) {
			printf("%d ", buffer[i]);
		}
		printf("//total %d\r\n", package_read_index);
		printf("%d loop\r\n", i_test++);
	}

	
	
	
	
	
	while(acknowledgementVCU != 0xFE){
		nbytes = read(can_socket, &frame, sizeof(struct can_frame));
		printf("can_id: %d data length: %d data: ", frame.can_id, frame.can_dlc);
	    for (i = 0; i < frame.can_dlc; i++){
	        printf("%d ", frame.data[i]);
		}
		printf("\n");
		acknowledgementVCU = frame.data[0];
	}
	//printf("infor: test done!\r\n");
}
	close(can_socket);
    return 0;
}
*/











































