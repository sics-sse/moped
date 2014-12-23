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


/*************** NOTE: this file is included when PLATFORM_TYPE_SOCKET=true **************************/

#include <sys/types.h>
#include <string.h>

#ifdef _MSC_VER
#include <winsock.h>
#else
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#endif

/*---------------------------- Code from ioport.c ----------------------------*/

/**
 * Setup the socket to the external I/O server.
 */
static void setupSocket() {
    if (iosocket <= 0) {
        char hostname[100];
        struct sockaddr_in pin;
        struct hostent *hp;
        int i;
        int pos = 0;
        int port;
        int sock;

        strcpy(hostname, "localhost");

        for (i = 0 ; ioport[i] != 0 ; i++) {
            if (ioport[i] == ':') {
                ioport[i] = 0;
                strcpy(hostname, ioport);
                pos = i + 1;
                break;
            }
        }
        port = atoi(&ioport[pos]);

#ifdef _MSC_VER
        if (iosocket < 0) {
            WSADATA wsaData;
            int wsaRes = WSAStartup(0x0101, &wsaData);
            if (wsaRes != 0) {
                printf("wsaRes=%d\n", wsaRes);
            }
        }
        iosocket = 0;
#endif
        /* go find out about the desired host machine */
        if ((hp = gethostbyname(hostname)) == 0) {
            perror("gethostbyname");
            exit(1);
        }

        /* fill in the socket structure with host information */
        memset(&pin, 0, sizeof(pin));
        pin.sin_family = AF_INET;
        pin.sin_addr.s_addr = ((struct in_addr *)(hp->h_addr))->s_addr;
        pin.sin_port = htons((short)port);

        /* grab an Internet domain socket */
        if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
            perror("socket");
            exit(1);
        }

        /* connect to PORT on HOST */
        if (connect(sock,(struct sockaddr *)  &pin, sizeof(pin)) == -1) {
            perror("connect");
            exit(1);
        }

        iosocket = sock;
    }
}

/**
 * Send some data
 *
 * @param p pointer to the data
 * @param length the length in bytes
 */
void ioport_send(void *p, int length) {
    if (send(iosocket, p, length, 0) == -1) {
        perror("send");
        exit(1);
    }
}

/**
 * Receive some data
 *
 * @param p pointer to the data
 * @param length the length in bytes
 */
void ioport_receive(void *p, int length) {
    char *buf = (char *)p;
    while (length > 0) {
        int got = recv(iosocket, buf, length, 0);
        if (got < 0) {
            perror("recv");
            exit(1);
        }
        buf += got;
        length -= got;
    }
}

/**
 * Write a char array to the I/O port whose value is initialized from a given string in Squawk memory.
 *
 * @param arr  the address of a char array in Squawk memory
 * @param count the element count
 */
static void writeIOPCharArray(Address arr, int count) {
    unsigned short *ptr = (unsigned short *)arr;
    unsigned short buf[100];
    int i;
    for (;;) {
        for (i = 0 ; i < 100 ; i++) {
            if (count == 0) {
                 ioport_send(buf, i*2);
                 return;
            }
            buf[i] = htons(*ptr++);
            count--;
        }
        ioport_send(buf, 100*2);
    }
}

/**
 * Write an int array to the I/O port whose value is initialized from a given string in Squawk memory.
 *
 * @param arr  the address of an int array in Squawk memory
 * @param count the element count
 */
static void writeIOPIntArray(Address arr, int count) {
    unsigned int *ptr = (unsigned int *)arr;
    unsigned int buf[100];
    int i;
    for (;;) {
        for (i = 0 ; i < 100 ; i++) {
            if (count == 0) {
                 ioport_send(buf, i*4);
                 return;
            }
            buf[i] = htonl(*ptr++);
            count--;
        }
        ioport_send(buf, 100*4);
    }
}

/**
 * Write an array to the I/O port whose value is initialized from a given string in Squawk memory.
 *
 * @param cno  the class number of the send buffer array
 * @param addr the array
 * @param length the number of elements in the array
 */
static void writeIOPObjectPrim(int cno, void *addr, int length) {
    int parms[2];
    parms[0] = htonl(cno);
    parms[1] = htonl(length);
    ioport_send(parms, 8);
    if (cno == CID_BYTE_ARRAY || cno == CID_STRING_OF_BYTES) {
        ioport_send(addr, length);
    } else if (cno == CID_CHAR_ARRAY || cno == CID_STRING) {
        writeIOPCharArray(addr, length);
    } else if (cno == CID_INT_ARRAY) {
        writeIOPIntArray(addr, length);
    } else {
        fatalVMError("writeIOPObjectPrim:: Invalid reference type");
    }
}

/**
 * Write an array to the I/O port whose value is initialized from a given string in Squawk memory.
 *
 * @param obj  the address of an object in Squawk memory
 * @param fill true if the buffer should be filled from squawk memory
 */
static void writeIOPObject(Address obj) {
    int parms[2];
    if (obj != null) {
        Address cls = getClass(obj);
        int cno = com_sun_squawk_Klass_id(cls);
        int length = getArrayLength(obj);
        writeIOPObjectPrim(cno, obj, length);
    } else {
        parms[0] = 0;
        parms[1] = 0;
        ioport_send(parms, 8);
    }
}

/**
 * Executes an operation on a given channel for an isolate via the external I/O server.
 *
 * @param  context the I/O context
 * @param  op      the operation to perform
 * @param  channel the channel number
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
static int ioport_execute(
                           int     context,
                           int     op,
                           int     channel,
                           int     i1,
                           int     i2,
                           int     i3,
                           int     i4,
                           int     i5,
                           int     i6,
                           Address send,
                           Address receive
                         ) {

    jlong startTime;
    int parms[10];
    int magic;
    int recLth = 0;
    char *recPos = null;

    int status;
    int resLth;
    int original_i1 = i1;

    /*
     * Open the TCP/IP connection if not already opened.
     */
    setupSocket();

    /*
     * Return cached result values if requested.
     */
    switch (op) {
        case ChannelConstants_CONTEXT_GETRESULT:   return result_low;
        case ChannelConstants_CONTEXT_GETRESULT_2: return result_high;
    }

    /*
     * Setup the position and length of the receive buffer if approperate.
     */
    if (receive != null) {
        recPos = (char *)receive;
        recLth = getArrayLength(receive);
        /*
         * ChannelConstants_READBUF is a special case where a subset of the receive buffer
         * may be used. The offset and length are specified using i1 and i2.
         */
        if (op == ChannelConstants_READBUF) {
            if ((i1+i2) > recLth) {
                 fprintf(stderr, "Receive buffer will overflow\n");
                 exit(-1);
            }
            recPos += i1;     // offset
            recLth = i2;      // length
            i1 = 0;
        } else {
            fprintf(stderr, "Receive buffer for op %d?????\n", op);
        }
    }

    /*
     * Another useful optimization is for the unused part of a sent buffer for
     * ChannelConstants_WRITEBUF to be omitted.
     */
    if (op == ChannelConstants_WRITEBUF) {
        i1 = 0;
    }

    /*
     * Start the timer.
     */
    startTime = sysTimeMillis();

    /*
     * Send the parameters.
     */
    parms[0] = htonl(context);
    parms[1] = htonl(op);
    parms[2] = htonl(channel);
    parms[3] = htonl(i1);
    parms[4] = htonl(i2);
    parms[5] = htonl(i3);
    parms[6] = htonl(i4);
    parms[7] = htonl(i5);
    parms[8] = htonl(i6);
    parms[9] = htonl(recLth);
    ioport_send(parms, 40);

    /*
     * Send the I/O buffers
     */
    if (op == ChannelConstants_WRITEBUF) {
        writeIOPObjectPrim(CID_BYTE_ARRAY, ((char *)send) + original_i1, i2);
    } else {
        writeIOPObject(send);
    }

    /*
     * Wait for the results.
     */
    ioport_receive(parms, 20);
    magic       = ntohl(parms[0]);
    status      = ntohl(parms[1]);
    result_low  = ntohl(parms[2]);
    result_high = ntohl(parms[3]);
    resLth      = ntohl(parms[4]);
    if (magic != 0xCAFEBABE) {
         fprintf(stderr, "Receive stream out of sync %x (op=%d) [%x:%x:%x:%x]\n", magic, op, status, result_low, result_high);
         exit(-1);
    }

    /*
     * Get the result buffer if expected.
     */
    if (resLth > 0) {
        if (resLth != recLth) {
             fprintf(stderr, "Receive buffer length mismatch %d should be %d (op=%d)\n", resLth, recLth, op);
             exit(-1);
        }
        ioport_receive(recPos, recLth);
    }

    /*
     * Adjust statictics.
     */
    io_ops_time += (sysTimeMillis() - startTime);
    io_ops_count++;

    /*
     * Return status.
     */
    return status;
}

/*---------------------------- IO Impl ----------------------------*/

/**
 * Initializes the IO subsystem.
 */
void IO_initialize() {

    /*
     * If an I/O port was set in the globals then this is used for I/O and the
     * other parameters are ingorned.
     */
    if (!ioport) {
        fatalVMError("io port not specified. Use -Xioport option");
    }
}

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

    Address s1;
    Address r1;
    int res;

    /*
     * If an I/O port was specified then use the external I/O server.
     */
    res = ioport_execute(context, op, channel, i1, i2, i3, i4, i5, i6, send, receive);

    com_sun_squawk_ServiceOperation_result = res;
}

#if KERNEL_SQUAWK
/**
 * Posts an event via ChannelIO to wake up any waiters.
 */
static void ioPostEvent(void) {
    /*
     * If an I/O port was specified then use the external I/O server.
     */
    assume (ioport != null);
    ioport_execute(-1, ChannelConstants_GLOBAL_POSTEVENT, -1, 0, 0, 0, 0, 0, 0, null, null);
}
#endif
