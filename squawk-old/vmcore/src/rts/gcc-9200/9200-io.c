/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle. All Rights Reserved.
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

#include "spi.h"
#include "i2c.h"
#include "flash.h"
#include "avr.h"
#include "system.h"

/**************************************************************************
 * Sleep support
 **************************************************************************/

#define SHALLOW_SLEEP_CLOCK_SWITCH_THRESHOLD 20
static const int peripheral_bus_speed[] = {PERIPHERAL_BUS_SPEEDS};
extern int shallow_sleep_clock_mode;

/*
 * Enter deep sleep
 */
void doDeepSleep(long long targetMillis, int remain_powered) {
	long long millisecondsToWait = targetMillis - getMilliseconds();
	if (remain_powered) {
		avrSetAlarmAndWait(millisecondsToWait);
		synchroniseWithAVRClock();
	} else {
    	unsigned int statusReturnedFromDeepSleep = deepSleep(millisecondsToWait);
	    lowLevelSetup(); //need to repeat low-level setup after a restart
    	avrSetOutstandingEvents(statusReturnedFromDeepSleep);
	}
}

/*
 * Enter shallow sleep
 */
void doShallowSleep(long long targetMillis) {
	long long start_time;
	long long last_time;
	int cpsr;
	int main_clock_sleep = FALSE;
	start_time = getMilliseconds();
	last_time = start_time;
	if ((shallow_sleep_clock_mode != SHALLOW_SLEEP_CLOCK_MODE_NORMAL) && (targetMillis - start_time > SHALLOW_SLEEP_CLOCK_SWITCH_THRESHOLD)) {
		main_clock_sleep = TRUE;
		setupClocks(peripheral_bus_speed[shallow_sleep_clock_mode]);
		cpsr = disableARMInterrupts();
		switch (shallow_sleep_clock_mode) {
			case SHALLOW_SLEEP_CLOCK_MODE_45_MHZ:
				select_45_clock();
				break;
			case SHALLOW_SLEEP_CLOCK_MODE_18_MHZ:
				select_18_clock();
				break;
			case SHALLOW_SLEEP_CLOCK_MODE_9_MHZ:
				select_9_clock();
				break;
			default:
				error("Ignoring invalid clock mode", shallow_sleep_clock_mode);
				break;
		}
		setARMInterruptBits(cpsr);
	}
	while (1) {
		if (checkForEvents()) break;
#ifdef OLD_IIC_MESSAGES
		if (checkForMessageEvent()) break;
#endif
		last_time = getMilliseconds();
		if (last_time > targetMillis) break;
		stopProcessor();
	}
	if (main_clock_sleep) {
		cpsr = disableARMInterrupts();
		switch (shallow_sleep_clock_mode) {
			case SHALLOW_SLEEP_CLOCK_MODE_45_MHZ:
				select_normal_clock_from_plla();
				break;
			case SHALLOW_SLEEP_CLOCK_MODE_18_MHZ:
			case SHALLOW_SLEEP_CLOCK_MODE_9_MHZ:
				select_normal_clock_from_main();
				break;
		}
		setARMInterruptBits(cpsr);
		setupClocks(MASTER_CLOCK_FREQ);
	}
	totalShallowSleepTime += (last_time - start_time);
}

/******************************************************************
 * Serial port support
 ******************************************************************/
int serialPortInUse[] = {0,0,0,0,0,0};

/* Java has requested serial chars */
int getSerialPortEvent(int device_type) {
	serialPortInUse[device_type-DEVICE_FIRST] = 1;
	return device_type;
}

int isSerialPortInUse(int device_type) {
	return serialPortInUse[device_type-DEVICE_FIRST];
}

void freeSerialPort(int device_type) {
	serialPortInUse[device_type-DEVICE_FIRST] = 0;
}

/*
 * ****************************************************************
 * Interrupt Handling Support
 *
 * See comment in AT91_AIC.java for details
 * ****************************************************************
 */

unsigned int java_irq_status; 
extern void java_irq_hndl();

#define WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER (DEVICE_LAST+1)
#define FIRST_IRQ_EVENT_NUMBER (WAIT_FOR_DEEP_SLEEP_EVENT_NUMBER+1)
#define SLEEP_MANAGER_ENABLED_IRQS (1 << AT91C_ID_FIQ)

void usb_state_change()	{
	int cpsr = disableARMInterrupts();
#if AT91SAM9G20
	java_irq_status |= (1<<10); // USB Device ID
#else
	java_irq_status |= (1<<11); // USB Device ID
#endif
    setARMInterruptBits(cpsr);
}

void setup_java_interrupts() {
	// This routine is called from os.c
	// NB interrupt handler coded in java-irq-hndl.s
	unsigned int id;
#if AT91SAM9G20
    diagnosticWithValue("initial val of irqRequests", (int)irqRequests);
#endif
	for (id = 0; id <= 31; id++) {
		if (!((1 << id) & RESERVED_PERIPHERALS)) {
			at91_irq_setup (id, &java_irq_hndl);
		}
	}
}

void systemGetEventHandler() {
    // since this function gets called frequently it's a good place to put
    // the call that periodically resyncs our clock with the power controller
    maybeSynchroniseWithAVRClock();
}

int avr_low_result = 0;

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
 static void ioExecuteSys(void) {
//  int     context = com_sun_squawk_ServiceOperation_context;
    int     op      = com_sun_squawk_ServiceOperation_op;
//  int     channel = com_sun_squawk_ServiceOperation_channel;
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

        case ChannelConstants_SPI_SEND_RECEIVE_8:
            // CE pin in i1
            // SPI config in i2
            // data in i3
            res = spi_sendReceive8(i1, i2, i3);
            break;
        case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_SEND_16:
            // CE pin in i1
            // SPI config in i2
            // data in i3
            // 16 bits in i4
            res = spi_sendReceive8PlusSend16(i1, i2, i3, i4);
            break;
        case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_SEND_N:
            // CE pin in i1
            // SPI config in i2
            // data in i3
            // size in i4
            res = spi_sendReceive8PlusSendN(i1, i2, i3, i4, send);
            break;
        case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_RECEIVE_16:
            // CE pin in i1
            // SPI config in i2
            // data in i3
            // 16 bits encoded in result
            res = spi_sendReceive8PlusReceive16(i1, i2, i3);
            break;
        case ChannelConstants_SPI_SEND_RECEIVE_8_PLUS_VARIABLE_RECEIVE_N:
            // CE pin in i1
            // SPI config in i2
            // data in i3
            // fifo_pin in i4
            // fifo pio in i5
            // data in receive
            res = spi_sendReceive8PlusVariableReceiveN(i1, i2, i3, receive, i4, i5);
            break;
        case ChannelConstants_SPI_SEND_AND_RECEIVE:
            // CE pin in i1
            // SPI config in i2
            // tx size in i4
            // rx size in i5
            // rx offset in i6
            // tx data in send
            // rx data in receive
            spi_sendAndReceive(i1, i2, i4, i5, i6, send, receive);
            break;
        case ChannelConstants_SPI_SEND_AND_RECEIVE_WITH_DEVICE_SELECT:
            // CE pin in i1
            // SPI config in i2
            // device address in i3
            // tx size in i4
            // rx size in i5
            // rx offset in i6
            // tx data in send
            // rx data in receive
            spi_sendAndReceiveWithDeviceSelect(i1, i2, i3, i4, i5, i6, send, receive);
            break;
        case ChannelConstants_SPI_PULSE_WITH_DEVICE_SELECT:
            // CE pin in i1
            // device address in i2
            // pulse duration in i3
            spi_pulseWithDeviceSelect(i1, i2, i3);
            break;
        case ChannelConstants_SPI_GET_MAX_TRANSFER_SIZE:
            res = SPI_DMA_BUFFER_SIZE;
            break;

        case ChannelConstants_I2C_OPEN:
            i2c_open();
            break;
        case ChannelConstants_I2C_CLOSE:
            i2c_close();
            break;
        case ChannelConstants_I2C_SET_CLOCK_SPEED:
            // clock speed in i1
            i2c_setClockSpeed(i1);
            break;
        case ChannelConstants_I2C_READ:
            // slave address in i1
            // internal address in i2
            // internal address size in i3
            // rx offset in i4
            // rx size in i5
            // rx data in receive
            res = i2c_read(i1, i2, i3, i4, i5, receive);
            break;
        case ChannelConstants_I2C_WRITE:
            // slave address in i1
            // internal address in i2
            // internal address size in i3
            // tx offset in i4
            // tx size in i5
            // tx data in send
            res = i2c_write(i1, i2, i3, i4, i5, send);
            break;
        case ChannelConstants_I2C_BUSY:
            res = i2c_busy();
            break;
        case ChannelConstants_I2C_PROBE:
            // slave address in i1
            // probe data in i2
            res = i2c_probe(i1, i2);
            break;

        case ChannelConstants_FLASH_ERASE:
            data_cache_disable();
            res = flash_erase_sector((Flash_ptr)i2);
            data_cache_enable();
            // the 9200 seems to lose time during flash operations, so need to reset the clock
            synchroniseWithAVRClock();
            break;
    	case ChannelConstants_FLASH_WRITE: {
                int i, d, address = i1, size = i2, offset = i3;
                char *buffer = (char*) send;
                data_cache_disable();
                res = flash_write_words((Flash_ptr) address, (unsigned char*) (((int) send) + offset), size);
                data_cache_enable();
                // the 9200 seems to lose time during flash operations, so need to reset the clock
                synchroniseWithAVRClock();
            }
            break;
            
    	case ChannelConstants_USB_GET_STATE:
            res = usb_get_state();
            break;

        case ChannelConstants_AVR_GET_TIME_HIGH: {
        	jlong avr_time = avrGetTime();
        	avr_low_result = (int) avr_time;
        	res = (int)(avr_time >> 32);
        	}
        	break;

        case ChannelConstants_AVR_GET_TIME_LOW:
        	res = avr_low_result;
        	break;
        	
        case ChannelConstants_AVR_GET_STATUS:
        	res = avrGetOutstandingEvents();
        	break;

        case ChannelConstants_WRITE_SECURED_SILICON_AREA:
        	data_cache_disable();
        	write_secured_silicon_area((Flash_ptr)i1, (short)i2);
        	data_cache_enable();
        	break;
        	
        case ChannelConstants_READ_SECURED_SILICON_AREA:
        	data_cache_disable();
        	read_secured_silicon_area((unsigned char*)send);
        	data_cache_enable();
        	break;
        	
        case ChannelConstants_ENABLE_AVR_CLOCK_SYNCHRONISATION:
    		enableAVRClockSynchronisation(i1);
    		res = 0;
        	break;

        case ChannelConstants_GET_PUBLIC_KEY: {
            int maximum_length = i1;
            char* buffer_to_write_public_key_into = send;
            res = retrieve_public_key(buffer_to_write_public_key_into, maximum_length);
        }
    	    break;
    	    
    	case ChannelConstants_REPROGRAM_MMU: {
    		reprogram_mmu(FALSE);
    		}
    		break;
        case ChannelConstants_GET_ALLOCATED_FILE_SIZE: {
        	res = get_allocated_file_size(i1);
    		}
    		break;
        case ChannelConstants_GET_FILE_VIRTUAL_ADDRESS: {
        	res = get_file_virtual_address(i1, send);
    		}
    		break;
    	case ChannelConstants_GET_DMA_BUFFER_SIZE:
    		res = dma_buffer_size;
    		break;
    	case ChannelConstants_GET_DMA_BUFFER_ADDRESS:
    		res = (int)dma_buffer_address;
    		break;
        case ChannelConstants_GET_RECORDED_OUTPUT: {
            int len = i1;
            int just_last = i2;
            char* buf = send;
            res = read_recorded_output(buf, len, just_last);
            }
            break;
            
        default:
            res = ChannelConstants_RESULT_BADPARAMETER;
    }
    com_sun_squawk_ServiceOperation_result = res;
}

