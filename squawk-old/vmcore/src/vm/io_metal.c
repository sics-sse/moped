/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011-2012 Oracle. All Rights Reserved.
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
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */


// General helper method
long long rebuildLongParam(int i1, int i2) {
    return ((long long)i1 << 32) | ((long long)i2 & 0xFFFFFFFF);
}


/**************************************************************************
 * Sleep support
 **************************************************************************/
int deepSleepEnabled = 0;           // indicates whether the feature is currently enabled (=1)
int sleepManagerRunning = 1;        // assume that sleepManager is running until it calls WAIT_FOR_DEEP_SLEEP
int outstandingDeepSleepEvent = 0;  // whether the sleep manager thread should be unblocked at the next reschedule
long long storedDeepSleepWakeupTarget; // The millis that the next deep sleep should end at
long long minimumDeepSleepMillis = 0x7FFFFFFFFFFFFFFFLL; // minimum time we're prepared to deep sleep for: avoid deep sleeping initially.
long long totalShallowSleepTime;    // total time the SPOT has been shallow sleeping
int shallow_sleep_clock_mode = SHALLOW_SLEEP_CLOCK_MODE_NORMAL;

static void setDeepSleepEventOutstanding(long long target) {
    storedDeepSleepWakeupTarget = target;
    outstandingDeepSleepEvent = 1;
    diagnosticWithValue("setDeepSleepEventOutstanding  - hi", (int)(target >> 32));
    diagnosticWithValue("setDeepSleepEventOutstanding  - lo", (int)(target & 0xFFFFFFFFL));
}

/**
 * Sleep Squawk for specified milliseconds
 */
void osMilliSleep(long long millisecondsToWait) {
    long long target = ((long long) getMilliseconds()) + millisecondsToWait;
    if (target <= 0) {
        target = 0x7FFFFFFFFFFFFFFFLL; // overflow detected
    }
    if ((millisecondsToWait < 0x7FFFFFFFFFFFFFFFLL) && deepSleepEnabled && 
         !sleepManagerRunning && (millisecondsToWait >= minimumDeepSleepMillis)) {
        setDeepSleepEventOutstanding(target);
    } else {
        doShallowSleep(target);
    }
}

/*
 * ****************************************************************
 * Interrupt Handling Support
 *
 * See comment in AT91_AIC.java for details
 * ****************************************************************
 */

// set in java_irq_hndl() (.c or .s)
volatile long long last_device_interrupt_time;

unsigned int java_irq_status = 0; // bit set = that irq has outstanding interrupt request

struct irqRequest {
        int eventNumber;
        int irq_mask;
        struct irqRequest *next;
};
typedef struct irqRequest IrqRequest;

IrqRequest *irqRequests;

extern void java_irq_hndl();

extern void setup_java_interrupts();

// Forward declarations
int getEvent(int, int);
static int check_irq(int irq_mask, int clear_flag);

#ifdef OLD_IIC_MESSAGES
INLINE boolean checkForMessageEvent();
#endif

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
        return ChannelConstants_RESULT_MALLOCFAILURE;
    }

    newRequest->next = NULL;
    newRequest->irq_mask = irq_mask;

    diagnosticWithValue("storeIrqRequest  - irqRequests", (int)irqRequests);
    diagnosticWithValue("    - newRequest", (int)newRequest);
    diagnosticWithValue("    - irq_mask", irq_mask);

    if (irqRequests == NULL) {
        irqRequests = newRequest;
        newRequest->eventNumber = FIRST_IRQ_EVENT_NUMBER;
    } else {
        IrqRequest* current = irqRequests;
        while (current->next != NULL) {
            current = current->next;
        }
        current->next = newRequest;
        newRequest->eventNumber = current->eventNumber + 1;
        assume(newRequest->eventNumber >= 0);
    }
    
    diagnosticWithValue("    - eventNumber", newRequest->eventNumber);
    return newRequest->eventNumber;
}

/* ioPostEvent is a no-op for us */
static void ioPostEvent(void) { }

/*
 * If there are outstanding irqRequests and one of them is for an interrupt that has
 * occurred return its eventNumber. Otherwise return 0
 */
int checkForEvents() {
    return getEvent(false, false);
}

static void printOutstandingEvents() {
	IrqRequest* current = irqRequests;
	while (current != NULL) {
    	diagnosticWithValue("    - eventNumber", current->irq_mask);
    	current = current->next;
    }
}

/*
 * If there are outstanding irqRequests and one of them is for an interrupt that has
 * occurred return its eventNumber. If removeEventFlag is true, then
 * also remove the event from the queue. If no requests match the interrupt status
 * return 0.
 */
int getEvent(int removeEventFlag, int fiqOnly) {
    int res = 0;
    int device_type;
    
    diagnosticWithValue("getEvent - removeEventFlag|fiqOnly:", (((removeEventFlag & 0xFFFF) << 16)) | (fiqOnly & 0xFFFF));
    
    if (irqRequests != NULL) {
    	IrqRequest* current = irqRequests;
        IrqRequest* previous = NULL;
        while (current != NULL) {
        	if ((!fiqOnly || (current->irq_mask & SLEEP_MANAGER_ENABLED_IRQS) != 0) && check_irq(current->irq_mask, removeEventFlag)) {
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
    }
    
    if (res == 0 && !fiqOnly) {
    	// check for serial chars available
    	for (device_type = DEVICE_FIRST; device_type<=DEVICE_LAST; device_type++) {
	    	if (isSerialPortInUse(device_type) && sysAvailable(device_type)) {
	    		res = device_type;
	    		if (removeEventFlag) {
	    			freeSerialPort(device_type);
	    		}
		    	break;
	    	}
    	}
    }

   	if (res == 0) {
    	if (outstandingDeepSleepEvent) {
    		sleepManagerRunning = 1;
    		res = WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER;
    	}
   	}
    
#ifdef ENABLE_ETHERNET_SUPPORT
    if (res == 0) {
        if (ethernetEventAvailable(LWIP_EFLAG_NEW)) {            
            res = WAIT_FOR_ETHERNET_EVENT_NUMBER;
        }
    }
#endif
    
	if (removeEventFlag) {
		// always clear the deep sleep event, as we will want to reconsider
		// whether deep sleep is appropriate after any event.
		outstandingDeepSleepEvent = 0;
	}
    
    if (res == 0) {
        diagnostic("    - no events. Waiting events:");
        // printOutstandingEvents();
    } else {
        diagnosticWithValue("    - got eventNumber", res);
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
//  int     context = com_sun_squawk_ServiceOperation_context;
    int     op      = com_sun_squawk_ServiceOperation_op;
//  int     channel = com_sun_squawk_ServiceOperation_channel;
    int     i1      = com_sun_squawk_ServiceOperation_i1;
    int     i2      = com_sun_squawk_ServiceOperation_i2;
    int     i3      = com_sun_squawk_ServiceOperation_i3;
//  int     i4      = com_sun_squawk_ServiceOperation_i4;
//  int     i5      = com_sun_squawk_ServiceOperation_i5;
//  int     i6      = com_sun_squawk_ServiceOperation_i6;
    Address send    = com_sun_squawk_ServiceOperation_o1;
    Address receive = com_sun_squawk_ServiceOperation_o2;

    int res = ChannelConstants_RESULT_OK;

    switch (op) {
        case ChannelConstants_GLOBAL_CREATECONTEXT: {
            res = 1; //let all Isolates share a context for now
            break;
        }

        case ChannelConstants_GLOBAL_GETEVENT: {
            systemGetEventHandler(); // platform may want to do special processing here...
            // don't return any events other than FIQ while sleep manager is running because
            // the sleep manager might call SHALLOW_SLEEP and any threads
            // unblocked before that point will end up waiting for sleep to
            // finish even though they are runnable
            res = getEvent(true, sleepManagerRunning);
            // improve fairness of thread scheduling - see bugzilla #568
            bc = -TIMEQUANTA;
            break;
        }

        case ChannelConstants_GLOBAL_WAITFOREVENT: {
            long long millisecondsToWait = rebuildLongParam(i1, i2);
            osMilliSleep(millisecondsToWait);
            res = ChannelConstants_RESULT_OK;
            break;
        }

        case ChannelConstants_CONTEXT_DELETE:  {
            // TODO delete all the outstanding events on the context
            // But will have to wait until we have separate contexts for each isolate
            res = ChannelConstants_RESULT_OK;
            break;
        }

        case ChannelConstants_CONTEXT_HIBERNATE: {
            // TODO this is faked, we have no implementation currently.
            res = ChannelConstants_RESULT_OK;
            break;
        }

        case ChannelConstants_CONTEXT_GETHIBERNATIONDATA: {
            // TODO this is faked, we have no implementation currently.
            res = ChannelConstants_RESULT_OK;
            break;
        }

        case ChannelConstants_CONTEXT_GETCHANNEL: {
            int channelType = i1;
            if (channelType == ChannelConstants_CHANNEL_IRQ) {
                res = 1;
            } else if (channelType == ChannelConstants_CHANNEL_SPI) {
                res = 2;
            } else {
                res = ChannelConstants_RESULT_BADPARAMETER;
            }
            break;
        }

        case ChannelConstants_CONTEXT_GETRESULT:
        case ChannelConstants_CONTEXT_GETRESULT_2: {
            res = retValue;
            retValue = 0;
            break;
        }
        
        case ChannelConstants_CONTEXT_GETERROR: {
            res = *((char*) retValue);
            if (res == 0)
                retValue = 0;
            else
                retValue++;
            break;
        }
        
        case ChannelConstants_OPENCONNECTION: {
            res = ChannelConstants_RESULT_EXCEPTION;
            retValue = (int) "javax.microedition.io.ConnectionNotFoundException";
            break;
        }
        
        case ChannelConstants_IRQ_WAIT: {
            int irq_no = i1;
            if (check_irq(irq_no, 1)) {
                res = 0;
            } else {
                res = storeIrqRequest(irq_no);
                if (res == ChannelConstants_RESULT_MALLOCFAILURE) {
                    retValue = (int) "java.lang.OutOfMemoryError";
                    res = ChannelConstants_RESULT_EXCEPTION;
                }
            }
            break;
        }

        case ChannelConstants_AVAILABLE_SERIAL_CHARS: {
            int deviceType = i1;
            // TODO: Should we truncate to boolean or not? MBED uses boolean, SPOT uses char count...
            //res = sysAvailable(deviceType) ? 1 : 0;
            res = sysAvailable(deviceType);
            break;
        }

        case ChannelConstants_GET_SERIAL_CHARS: {
            int deviceType = i3;
            if (sysAvailable(deviceType)) {
                // Return 0 if there are chars available (which we will return in the receive param)
                res = 0;
                int offset = i1;
                int len = i2;
                int* countBuf = send;
                char* buf = receive;
                *countBuf = sysReadSeveral(buf + offset, len, deviceType);
                freeSerialPort(deviceType); // free serial port for future use
            } else {
                // Otherwise return event number to say there might be later
                res = getSerialPortEvent(deviceType);
            }
            break;
        }

        case ChannelConstants_WRITE_SERIAL_CHARS: {
            int offset = i1;
            int len = i2;
            int deviceType = i3;
            char* buf = send;
            sysWriteSeveral(buf + offset, len, deviceType);
            res = 0;
            break;
        }

        case ChannelConstants_GET_HARDWARE_REVISION: {
            res = get_hardware_revision();
            break;
        }
 
        case ChannelConstants_DEEP_SLEEP: {
            doDeepSleep(rebuildLongParam(i1, i2), i3);
            res = 0;
            break;
        }
        
        case ChannelConstants_SHALLOW_SLEEP: {
            long long target = rebuildLongParam(i1, i2);
            if (target <= 0) target = 0x7FFFFFFFFFFFFFFFLL; // overflow detected
            doShallowSleep(target);
            res = 0;
            break;
        }
        
        case ChannelConstants_WAIT_FOR_DEEP_SLEEP: {
            minimumDeepSleepMillis = rebuildLongParam(i1, i2);
            sleepManagerRunning = 0;
            res = WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER;
            diagnosticWithValue("WAIT_FOR_DEEP_SLEEP -- ", i2);
            break;
        }

        case ChannelConstants_DEEP_SLEEP_TIME_MILLIS_HIGH: {
            res = (int) (storedDeepSleepWakeupTarget >> 32);
            break;
        }

        case ChannelConstants_DEEP_SLEEP_TIME_MILLIS_LOW: {
            res = (int) (storedDeepSleepWakeupTarget & 0xFFFFFFFF);
            break;
        }

        case ChannelConstants_TOTAL_SHALLOW_SLEEP_TIME_MILLIS_HIGH: {
            res = (int) (totalShallowSleepTime >> 32);
            break;
        }

        case ChannelConstants_TOTAL_SHALLOW_SLEEP_TIME_MILLIS_LOW: {
            res = (int) (totalShallowSleepTime & 0xFFFFFFFF);
            break;
        }

        case ChannelConstants_SET_MINIMUM_DEEP_SLEEP_TIME: {
            minimumDeepSleepMillis = rebuildLongParam(i1, i2);
            res = 0;
            break;
        }

        case ChannelConstants_SET_SHALLOW_SLEEP_CLOCK_MODE: {
            shallow_sleep_clock_mode = i1;
            res = 0;
            break;
        }

        case ChannelConstants_GET_LAST_DEVICE_INTERRUPT_TIME_ADDR: {
            res = (int) &last_device_interrupt_time;
            break;
        }

        case ChannelConstants_GET_CURRENT_TIME_ADDR: {
            res = (int) &clock_counter;
            break;
        }

        case ChannelConstants_SET_DEEP_SLEEP_ENABLED: {
            deepSleepEnabled = i1;
            res = 0;
            break;
        }

        case ChannelConstants_SET_SYSTEM_TIME: {
            setMilliseconds(rebuildLongParam(i1, i2));
            res = 0;
            break;
        }

        case ChannelConstants_COMPUTE_CRC16_FOR_MEMORY_REGION: {
            int address = i1;
            int numberOfBytes = i2;
            res = crc(address, numberOfBytes);
            break;
        }

        case ChannelConstants_GET_DMA_BUFFER_SIZE: {
            res = dma_buffer_size;
            break;
        }

        case ChannelConstants_GET_DMA_BUFFER_ADDRESS: {
            res = (int) dma_buffer_address;
            break;
        }
        
        default: {
            ioExecuteSys(); // do platform-specific
            res = com_sun_squawk_ServiceOperation_result; // result set by ioExecuteSys.
            break;
        }
    }
    com_sun_squawk_ServiceOperation_result = res;
}
