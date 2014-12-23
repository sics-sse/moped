/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

//#define UNICODE true

/**
 * Sets up the global used to pass an int result from an INTERNAL IO operation back to the VM.
 * The value is accessed in the VM by calling VM.serviceResult().
 *
 * @param value the int value to return to the VM
 */
INLINE void returnIntResult(int value) {
    com_sun_squawk_ServiceOperation_result = value;
}

/**
 * Sets up the 2 globals used to pass a long result from an INTERNAL IO operation back to the VM.
 * The value is accessed in the VM as '(high << 32) | (low & 0x00000000FFFFFFFFL)' where
 * 'high' is the result of the next call to VM.serviceResult() and 'low' is the result
 * of a INTERNAL_LOW_RESULT channel operation.
 *
 * @param value the long value to return to the VM
 */
INLINE void returnLongResult(jlong value) {
    internalLowResult = (int)value;
    com_sun_squawk_ServiceOperation_result = (int)(value >> 32);
}

/**
 * Sets up the global used to pass an address result from an INTERNAL IO operation back to the VM.
 * The value is accessed in the VM by calling VM.addressResult().
 *
 * @param value the address value to return to the VM
 */
INLINE void returnAddressResult(Address value) {
    com_sun_squawk_ServiceOperation_addressResult = value;
}

#if PLATFORM_TYPE_BARE_METAL
#include "metal_sleep.h"
#include "io_metal.h"
#endif

#include IODOTC

#if PLATFORM_TYPE_BARE_METAL
/* Different bare metal platforms handle this differently - eg set IODOTC to a specific file.
 * Also include io_metal.c*/
#include "io_metal.c"
#endif

#ifdef OLD_IIC_MESSAGES
/*
 * Include the message processing code.
 */
#include "msg.c"
#endif

/**
 * Execute a channel operation.
 */
void cioExecute(void) {
/*  int     context = com_sun_squawk_ServiceOperation_context; */
    int     op      = com_sun_squawk_ServiceOperation_op;
/*  int     channel = com_sun_squawk_ServiceOperation_channel; */
    int     i1      = com_sun_squawk_ServiceOperation_i1;
    int     i2      = com_sun_squawk_ServiceOperation_i2;
 /* int     i3      = com_sun_squawk_ServiceOperation_i3;
    int     i4      = com_sun_squawk_ServiceOperation_i4;
    int     i5      = com_sun_squawk_ServiceOperation_i5;
    int     i6      = com_sun_squawk_ServiceOperation_i6;
  */
    Address o1      = com_sun_squawk_ServiceOperation_o1;
/*  Address o2      = com_sun_squawk_ServiceOperation_o2; */
    FILE   *vmOut   = streams[currentStream];

    switch (op) {

        case ChannelConstants_INTERNAL_SETSTREAM: {
            com_sun_squawk_ServiceOperation_result = setStream(i1);
            assume(streams[currentStream] != null);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTSTRING: {
            printJavaString(o1, vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTCHAR: {
            fprintf(vmOut, "%c", i1);
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTINT: {
            fprintf(vmOut, "%d", i1);
            fflush(vmOut);
            break;
        }

/* use this as a proxy for /*if[FLOATS], which can't be used because this file isn't an .spp file */
#ifdef F_POS_INFINITY
        case ChannelConstants_INTERNAL_PRINTDOUBLE: {
            fprintf(vmOut, format("%D"), lb2d(makeLong(i1, i2)));
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTFLOAT: {
            fprintf(vmOut, "%f", ib2f(i1));
            fflush(vmOut);
            break;
        }
#endif

        case ChannelConstants_INTERNAL_PRINTUWORD: {
            jlong val = makeLong(i1, i2);
            fprintf(vmOut, format("%A"), (UWord)val);
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTOFFSET: {
            jlong val = makeLong(i1, i2);
            fprintf(vmOut, format("%O"), val);
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTLONG: {
            jlong val = makeLong(i1, i2);
            fprintf(vmOut, format("%L"), val);
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTADDRESS: {
            Address val = o1;
            fprintf(vmOut, format("%A"), val);
            if (hieq(val, com_sun_squawk_VM_romStart) && lo(val, com_sun_squawk_VM_romEnd)) {
                fprintf(vmOut, format(" (image @ %O)"), Address_sub(val, com_sun_squawk_VM_romStart));
            }
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTBYTES: {
            Address bytes = o1;
            int offset = i1;
            int len = i2;
            int blen = (int)getArrayLength(bytes);
            int i;
            /*fprintf(stderr, "IPB: %d:%d\n", offset, len);*/
            assumeAlways(offset >= 0 && len >= 0);
            assumeAlways(offset + len <= blen);
            for (i = 0; i < len; i++) {
                fprintf(vmOut, "%c", getByte(bytes, offset + i));
            }
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTCONFIGURATION: {
            fprintf(stderr, "native VM build flags: %s\n", BUILD_FLAGS);
            break;
        }

#if DEBUG_CODE_ENABLED
        case ChannelConstants_INTERNAL_PRINTGLOBALS: {
            printGlobals();
            fflush(vmOut);
            break;
        }

        case ChannelConstants_INTERNAL_PRINTGLOBALOOPNAME: {
#if TRACE
            fprintf(vmOut, "%s", getGlobalOopName(i1));
#else
            fprintf(vmOut, "Global oop:%d", i1);
#endif /* TRACE */
            fflush(vmOut);
            break;
        }
#endif /* DEBUG_CODE_ENABLED */

        case ChannelConstants_INTERNAL_GETPATHSEPARATORCHAR: {
            com_sun_squawk_ServiceOperation_result = pathSeparatorChar;
            break;
        }

        case ChannelConstants_INTERNAL_GETFILESEPARATORCHAR:  {
            com_sun_squawk_ServiceOperation_result = fileSeparatorChar;
            break;
        }

        case ChannelConstants_INTERNAL_GETTIMEMICROS_HIGH: {
            returnLongResult(sysTimeMicros());
            break;
        }

        case ChannelConstants_INTERNAL_GETTIMEMILLIS_HIGH: {
            returnLongResult(sysTimeMillis());
            break;
        }

        case ChannelConstants_INTERNAL_LOW_RESULT: {
            com_sun_squawk_ServiceOperation_result = internalLowResult;
            break;
        }

        case ChannelConstants_INTERNAL_STOPVM: {
            stopVM(i1);
            break;
        }

#ifdef OLD_IIC_MESSAGES
        case ChannelConstants_INTERNAL_ALLOCATE_MESSAGE_BUFFER: {
            deferInterruptsAndDo(
                allocateMessageBuffer();
            );
            break;
        }

        case ChannelConstants_INTERNAL_FREE_MESSAGE_BUFFER: {
            deferInterruptsAndDo(
                freeMessageBuffer(o2);
            );
            break;
        }

        case ChannelConstants_INTERNAL_SEND_MESSAGE_TO_SERVER: {
            sendMessage(o1, o2, i1, &toServerMessages, &toServerWaiters);
#if KERNEL_SQUAWK
            /*
             * We could use a special _TO_KERNEL type for messages
             * to the driver's request url but this is simpler.
             */
            if (kernelMode && !inKernelMode()) {
                void Squawk_enterKernel();
                Squawk_enterKernel();
            }
#endif /* KERNEL_SQUAWK */
            break;
        }

        case ChannelConstants_INTERNAL_RECEIVE_MESSAGE_FROM_CLIENT: {
            receiveMessage(o1, &toServerMessages, &toServerWaiters);
            break;
        }

        case ChannelConstants_INTERNAL_SEND_MESSAGE_TO_CLIENT: {
            sendMessage(o1, o2, i1, &toClientMessages, &toClientWaiters);
            break;
        }

        case ChannelConstants_INTERNAL_RECEIVE_MESSAGE_FROM_SERVER: {
            receiveMessage(o1, &toClientMessages, &toClientWaiters);
            break;
        }

        case ChannelConstants_INTERNAL_SEARCH_SERVER_HANDLERS: {
            searchServerHandlers(o2);
            break;
        }

        case ChannelConstants_GLOBAL_WAITFOREVENT: {
            if (!checkForMessageEvent()) {
                ioExecute();
            }
            break;
        }

        case ChannelConstants_GLOBAL_GETEVENT: {
            if (!getMessageEvent()) {
                ioExecute();
            }
            break;
        }
#endif /* OLD_IIC_MESSAGES */

        /* WARNING! NOT 64-bit safe! */
        case ChannelConstants_INTERNAL_NATIVE_PLATFORM_NAME: {
            com_sun_squawk_ServiceOperation_result = (int)sysPlatformName();
            break;
        }

#if NATIVE_VERIFICATION
        case ChannelConstants_INTERNAL_COMPUTE_SHA1_FOR_MEMORY_REGION:{
            int address=i1;
            int numberOfBytes=i2;
            unsigned char* buffer_to_write_sha_hash_into = o1;
            sha_for_memory_region(buffer_to_write_sha_hash_into,address,numberOfBytes);
            break;
        }
#endif
        default: {
            ioExecute();
        }
    }
}

/**
 * Post an event to the channelIO subsystem to wake up any waiters.
 */
static void cioPostEvent(void) {
#if KERNEL_SQUAWK
    ioPostEvent();
#endif
}
