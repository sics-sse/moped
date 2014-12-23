/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

#include "drivers/spi/spi.h"

char serialRxBuffer[129]; // 128 plus one for the "magic" byte at the beginning
char serialTxBuffer[129];

/* ***************************************
 * Flash memory support
 */

#define CYCLE1_OFFSET  0x555
#define CYCLE2_OFFSET  0xAAA
#define PROGRAM_CODE_1 0xAA
#define PROGRAM_CODE_2 0x55
#define ERASE_CODE_1   0x80
#define ERASE_CODE_2   0x30
#define WRITE_CODE_1   0xA0

/*
 * Erase the sector containing the address sectorAddress
 *
 * Return 0 if ok, 1 otherwise
 */
static int eraseSectorPrim(int base, int sectorAddress) {
	int loopCount;
	volatile unsigned short * baseAddress = (volatile unsigned short *)base;
	*(baseAddress + CYCLE1_OFFSET) = PROGRAM_CODE_1;
	*(baseAddress + CYCLE2_OFFSET) = PROGRAM_CODE_2;
	*(baseAddress + CYCLE1_OFFSET) = ERASE_CODE_1;
	*(baseAddress + CYCLE1_OFFSET) = PROGRAM_CODE_1;
	*(baseAddress + CYCLE2_OFFSET) = PROGRAM_CODE_2;
	*(baseAddress + ((sectorAddress - base)>>1)) = ERASE_CODE_2;
	loopCount = 0;
	// use the nDATA POLLING feature to test completion
	while (*(baseAddress + ((sectorAddress - base)>>1)) != 0xFFFF) {
		if (++loopCount > 10000000) {
			return 1;
		}
	}
	return 0;
}

/*
 * Write a word to flash
 */
static int writeFlashPrim(int base, int memAddress, int data) {
	int loopCount;
	volatile unsigned short * baseAddress = (volatile unsigned short *)base;
	*(baseAddress + CYCLE1_OFFSET) = PROGRAM_CODE_1;
	*(baseAddress + CYCLE2_OFFSET) = PROGRAM_CODE_2;
	*(baseAddress + CYCLE1_OFFSET) = WRITE_CODE_1;
	*(baseAddress + ((memAddress - base)>>1)) = data;
	loopCount = 0;
	// use the nDATA POLLING feature to test completion
	while ((*(baseAddress + ((memAddress - base)>>1))) != (*(baseAddress + ((memAddress - base)>>1)))) {
		if (++loopCount > 10000000) {
			return 1;
		}
	}
	return 0;
}

/*
 * ***************************************
 * Interrupt Handling Support
 *
 * See comment in AT91_AIC.java for details
 */

unsigned int java_irq_status = 0; // bit set = that irq has outstanding interrupt request

struct irqRequest {
        int eventNumber;
        int irq_mask;
        struct irqRequest *next;
};
typedef struct irqRequest IrqRequest;

IrqRequest *irqRequests;

/*
 * Interrupt vector set up
 */
#define PIO_SRC    8
#define TC0_SRC    4
#define TC1_SRC    5
#define TC2_SRC    6
#define US0_SRC    2
#define US1_SRC    3
#define IRQ0_SRC   16
#define IRQ1_SRC   17
#define IRQ2_SRC   18

extern void java_irq_hndl();

void set_up_java_interrupts() {
	// This routine is called from os.c
	// NB interrupt handler coded in java-irq-hndl.s
	at91_irq_setup (PIO_SRC,	&java_irq_hndl);
	at91_irq_setup (TC0_SRC,	&java_irq_hndl);
	at91_irq_setup (TC1_SRC,	&java_irq_hndl);
	at91_irq_setup (TC2_SRC,	&java_irq_hndl);
	at91_irq_setup (US0_SRC,	&java_irq_hndl);
	at91_irq_setup (US1_SRC,	&java_irq_hndl);
	at91_irq_setup (IRQ0_SRC,	&java_irq_hndl);
	at91_irq_setup (IRQ1_SRC,	&java_irq_hndl);
	at91_irq_setup (IRQ2_SRC,	&java_irq_hndl);
}

/*
 * Java has requested wait for an interrupt. Store the request,
 * and each time Java asks for events, signal the event if the interrupt has happened
 *
 * @return the event number
 */
int storeIrqRequest (int irq_mask) {
        IrqRequest* newRequest = (IrqRequest*)malloc(sizeof(IrqRequest));
        if (newRequest == NULL) {
        	//TODO set up error message for GET_ERROR and handle
        	//one per channel and clean on new requests.
        	return ChannelConstants_RESULT_EXCEPTION;
        }

        newRequest->next = NULL;
        newRequest->irq_mask = irq_mask;

        if (irqRequests == NULL) {
        	irqRequests = newRequest;
        	newRequest->eventNumber = 1;
        } else {
        	IrqRequest* current = irqRequests;
        	while (current->next != NULL) {
        		current = current->next;
        	}
        	current->next = newRequest;
        	newRequest->eventNumber = current->eventNumber + 1;
        }
        return newRequest->eventNumber;
}

// Forward declaration
int getEventPrim(int);
static int check_irq(int irq_mask, int clear_flag);
INLINE boolean checkForMessageEvent();

/* ioPostEvent is a no-op for us */
static void ioPostEvent(void) { }

/*
 * If there are outstanding irqRequests and one of them is for an irq that has
 * occurred remove it and return its eventNumber. Otherwise return 0
 */
int getEvent() {
        return getEventPrim(1);
}

/*
 * If there are outstanding irqRequests and one of them is for an interrupt that has
 * occurred return its eventNumber. Otherwise return 0
 */
int checkForEvents() {
        return getEventPrim(0);
}

/*
 * If there are outstanding irqRequests and one of them is for an interrupt that has
 * occurred return its eventNumber. If removeEventFlag is true, then
 * also remove the event from the queue. If no requests match the interrupt status
 * return 0.
 */
int getEventPrim(int removeEventFlag) {
        int res = 0;
        if (irqRequests == NULL) {
        	return 0;
        }
        IrqRequest* current = irqRequests;
        IrqRequest* previous = NULL;
        while (current != NULL) {
        	if (check_irq(current->irq_mask, removeEventFlag)) {
        		res = current->eventNumber;
        		//unchain
        		if (removeEventFlag) {
        			if (previous == NULL) {
        				irqRequests = current->next;
        			} else {
        				previous->next = current->next;
        			}
        			free(current);
        		}
        		break;
        	} else {
        		previous = current;
        		current = current->next;
        	}
        }
        return res;
}

/**
 * Check if an irq bit is set in the status, return 1 if yes
 * Also, clear bit if it is set and clear_flag = 1
 */
static int check_irq(int irq_mask, int clear_flag) {
        int result;
        disableARMInterrupts();
        if ((java_irq_status & irq_mask) != 0) {
        	if (clear_flag) {
            	java_irq_status = java_irq_status & ~irq_mask;
        	}
            result = 1;
        } else {
        	result = 0;
        }
        enableARMInterrupts();
        return result;
}
/*
 * End of Interrupt Handling Support
 * ***************************************
 */


/*
 * Stop the processor clock - restarts on interrupt
 */
static stopProcessor() {
	volatile unsigned int *regPtr = (volatile unsigned int*)0xFFFF4000;
	*regPtr = 1;
}


/**
 * Sleep Squawk for specified milliseconds
 */
void osMilliSleep(long long millisecondsToWait) {
    long long target = getMilliseconds() + millisecondsToWait;
    long long maxValue = 0x7FFFFFFFFFFFFFFFLL;
    if (target <= 0) target = maxValue; // overflow detected

    while (1) {
        if (checkForEvents()) break;
        if (checkForMessageEvent()) break;
        if (getMilliseconds() > target) break;
        stopProcessor();
    }
}

int retValue = 0;  // holds the value to be returned on the next "get result" call

/**
 * Executes an operation on a given channel for an isolate.
 *
 * @param  context the I/O context
 * @param  op      the operation to perform
 * @param  channel the identifier of the channel to execute the operation on
 * @param  i1
 * @param  i2
 * @param  i3
 * @param  i4
 * @param  i5
 * @param  i6
 * @param  send
 * @param  receive
 * @return the operation result
 */
 static void ioExecute(void) {
    int     context = com_sun_squawk_ServiceOperation_context;
    int     op      = com_sun_squawk_ServiceOperation_op;
    int     channel = com_sun_squawk_ServiceOperation_channel;
    int     i1      = com_sun_squawk_ServiceOperation_i1;
    int     i2      = com_sun_squawk_ServiceOperation_i2;
    int     i3      = com_sun_squawk_ServiceOperation_i3;
    int     i4      = com_sun_squawk_ServiceOperation_i4;
    int     i5      = com_sun_squawk_ServiceOperation_i5;
    int     i6      = com_sun_squawk_ServiceOperation_i6;
    Address send    = com_sun_squawk_ServiceOperation_o1;
    Address receive = com_sun_squawk_ServiceOperation_o2;

    int res = ChannelConstants_RESULT_OK;

    switch (op) {
    	case ChannelConstants_GLOBAL_CREATECONTEXT:
    		res = 1; //let all Isolates share a context for now
    		break;
    	case ChannelConstants_CONTEXT_GETCHANNEL: {
            		int channelType = i1;
            		if (channelType == ChannelConstants_CHANNEL_IRQ) {
            			res = 1;
            		} else if (channelType == ChannelConstants_CHANNEL_SPI) {
            			res = 2;
            		} else {
            			res = ChannelConstants_RESULT_BADPARAMETER;
            		}
            	}
    		break;
    	case ChannelConstants_IRQ_WAIT: {
            		int irq_no = i1;
            		if (check_irq(irq_no, 1)) {
            			res = 0;
            		} else {
        	    		res = storeIrqRequest(irq_no);
            		}
    		}
    		break;
	    case ChannelConstants_SPI_INITIALIZE: {
	    		// clk in i1
	    		// mosi in i2
	    		// miso in i3
	    		spi_initialise (i1, i2, i3);
		    }
		    break;
	    case ChannelConstants_SPI_SEND_RECEIVE_8: {
	    		// CE pin in i1
	    		// data in i2
	    		res = spi_sendReceive8(i1, i2);
		    }
		    break;
	    case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_SEND_16: {
	    		// CE pin in i1
	    		// data in i2
	    		// 16 bits in i3
	    		res = spi_sendReceive8PlusSend16(i1, i2, i3);
		    }
		    break;
	    case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_SEND_N: {
	    		// CE pin in i1
	    		// data in i2
	    		// size in i3
	    		res = spi_sendReceive8PlusSendN(i1, i2, i3, send);
		    }
		    break;
	    case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_RECEIVE_16: {
	    		// CE pin in i1
	    		// data in i2
	    		// 16 bits encoded in result
	    		res = spi_sendReceive8PlusReceive16(i1, i2);
		    }
		    break;
	    case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_VARIABLE_RECEIVE_N: {
	    		// CE pin in i1
	    		// data in i2
	    		// fifo_pin in i3
	    		// data in receive
	    		res = spi_sendReceive8PlusVariableReceiveN(i1, i2, receive, i3);
		    }
		    break;
	    case ChannelConstants_SPI_SEND_16_RECEIVE_8_PLUS_FIXED_RECEIVE_N: {
	    		// CE pin in i1
	    		// first byte data in i2
	    		// second byte data in i3
	    		// size in i4
	    		// data in receive
	    		res = spi_send16Receive8PlusFixedReceiveN(i1, i2, i3, i4, receive);
		    }
		    break;
		case ChannelConstants_SPI_SEND_AND_RECEIVE: {
				// CE pin in i1
				// delay in i2
	    		// tx size in i3
	    		// rx size in i4
	    		// rx offset in i5
	    		// tx data in send
	    		// rx data in receive
	    		spi_sendAndReceive(i1, i2, i3, i4, i5, send, receive);
			}
			break;
    	case ChannelConstants_GET_CONFIG_ADDRESS: {
    			res = FLASH_CONFIG_OFFSET + FLASH_BASE_ADDR;
    		}
    		break;
    	case ChannelConstants_GET_SERIAL_RX_BUFFER_ADDR:
    		res = (int)serialRxBuffer;
    		break;
    	case ChannelConstants_GET_SERIAL_TX_BUFFER_ADDR:
    		res = (int)serialTxBuffer;
    		break;
    	case ChannelConstants_FLASH_ERASE:
    		res = eraseSectorPrim(i1, i2);
    		break;
    	case ChannelConstants_FLASH_WRITE:
    		res = writeFlashPrim(i1, i2, i3);
    		break;
    	case ChannelConstants_SET_EXECUTION_LED:
    		bytecodeExecutionLedMask = i1;
    		break;

    	case ChannelConstants_CONTEXT_GETRESULT:
    	case ChannelConstants_CONTEXT_GETRESULT_2:
    	case ChannelConstants_CONTEXT_GETERROR:
    		res = retValue;
    		retValue = 0;
    		break;
    	case ChannelConstants_GLOBAL_GETEVENT:
    		res = getEvent();
    		break;
    	case ChannelConstants_GLOBAL_WAITFOREVENT: {
                    long long millisecondsToWait = i1;
                    millisecondsToWait = (millisecondsToWait << 32) | ((unsigned long long)i2 & 0xFFFFFFFF);
                    osMilliSleep(millisecondsToWait);
                    res = 0;
    		}
    		break;
    	case ChannelConstants_CONTEXT_DELETE:
    		// TODO delete all the outstanding events on the context
    		// But will have to wait until we have separate contexts for each isolate
    		res=0;
    		break;
        case ChannelConstants_CONTEXT_HIBERNATE:
            // TODO this is faked, we have no implementation currently.
            res = ChannelConstants_RESULT_OK;
            break;
        case ChannelConstants_CONTEXT_GETHIBERNATIONDATA:
            // TODO this is faked, we have no implementation currently.
            res = ChannelConstants_RESULT_OK;
            break;
        default:
    		res = ChannelConstants_RESULT_BADPARAMETER;
    }
    com_sun_squawk_ServiceOperation_result = res;
}

