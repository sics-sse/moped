/*
 * Copyright 2004-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.vm;

public final class ChannelConstants {
    
    /**
     * Purely static class should not be instantiated.
     */
    private ChannelConstants() {}

    /**
     * The channel identifier for the generic IO channel.
     */
    public final static int CHANNEL_GENERIC     = 1;

/*if[!ENABLE_CHANNEL_GUI]*/
    /**
     * The last fixed channel number.
     */
    public final static int CHANNEL_LAST_FIXED  = CHANNEL_GENERIC;
/*else[ENABLE_CHANNEL_GUI]*/
//    /**
//     * The channel identifier for the GUI input channel.
//     */
//    public final static int CHANNEL_GUIIN       = 2;
//
//    /**
//     * The channel identifier for the GUI output channel.
//     */
//    public final static int CHANNEL_GUIOUT      = 3;
//    /**
//     * The last fixed channel number.
//     */
//    public final static int CHANNEL_LAST_FIXED  = CHANNEL_GUIOUT;
/*end[ENABLE_CHANNEL_GUI]*/

/*if[!OLD_IIC_MESSAGES]*/
/*else[OLD_IIC_MESSAGES]*/
//    /**
//     * The channel for message I/O.
//     */
//    public final static int CHANNEL_MESSAGEIO   = 4;
//
//    /**
//     * The last fixed channel number.
//     */
//    public final static int CHANNEL_LAST_FIXED  = CHANNEL_MESSAGEIO;
/*end[OLD_IIC_MESSAGES]*/

/*if[!ENABLE_CHANNEL_GUI]*/
/*else[ENABLE_CHANNEL_GUI]*/
//    /**
//     * The GUI input repaint message.
//     */
//    public final static int GUIIN_REPAINT       = 0;
//
//    /**
//     * The GUI key input message.
//     */
//    public final static int GUIIN_KEY           = 1;
//
//    /**
//     * The GUI mouse message.
//     */
//    public final static int GUIIN_MOUSE         = 2;
//
//    /**
//     * The GUI exit message.
//     */
//    public final static int GUIIN_EXIT          = 3;
//
//    /**
//     * The GUI input repaint message.
//     */
//    public final static int GUIIN_HIBERNATE     = 4;
/*end[ENABLE_CHANNEL_GUI]*/


    public final static int

        /* Channel I/O result codes */

        RESULT_OK                               = 0,
        RESULT_BADCONTEXT                       = -1,
        RESULT_EXCEPTION                        = -2,
        RESULT_BADPARAMETER                     = -3,
        RESULT_MALLOCFAILURE                    = -4,

        /* I/O channel opcodes */

        GLOBAL_CREATECONTEXT                    = 1,
        GLOBAL_GETEVENT                         = 2,
        GLOBAL_POSTEVENT                        = 3,
        GLOBAL_WAITFOREVENT                     = 4,

        CONTEXT_DELETE                          = 5,
        CONTEXT_HIBERNATE                       = 6,
        CONTEXT_GETHIBERNATIONDATA              = 7,
        CONTEXT_GETCHANNEL                      = 8,
        CONTEXT_FREECHANNEL                     = 9,
        CONTEXT_GETRESULT                       = 10,
        CONTEXT_GETRESULT_2                     = 11,
        CONTEXT_GETERROR                        = 12,
        LAST_BASIC_OPCODE                       = CONTEXT_GETERROR,

        /*
         * Opcodes for Generic connections
         * Too hard to conditionally define these. Hosted-support depends on them,
         * and we'd like to use the same hosted-support jar file no matter which PLATFORM_TYPE was used.
         */
        OPENCONNECTION                          = 13,       
        CLOSECONNECTION                         = 14,
        ACCEPTCONNECTION                        = 15,
        OPENINPUT                               = 16,
        CLOSEINPUT                              = 17,
        WRITEREAD                               = 18,
        READBYTE                                = 19,
        READSHORT                               = 20,
        READINT                                 = 21,
        READLONG                                = 22,
        READBUF                                 = 23,
        SKIP                                    = 24,
        AVAILABLE                               = 25,
        MARK                                    = 26,
        RESET                                   = 27,
        MARKSUPPORTED                           = 28,
        OPENOUTPUT                              = 29,
        FLUSH                                   = 30,
        CLOSEOUTPUT                             = 31,
        WRITEBYTE                               = 32,
        WRITESHORT                              = 33,
        WRITEINT                                = 34,
        WRITELONG                               = 35,
        WRITEBUF                                = 36,
        LAST_GENERIC_CONNECTION_OPCODE          = WRITEBUF,

        PLATFORM_OPCODES                        = LAST_GENERIC_CONNECTION_OPCODE + 1,
        DLOPEN                                  = PLATFORM_OPCODES + 0,
        DLCLOSE                                 = PLATFORM_OPCODES + 1,
        DLERROR                                 = PLATFORM_OPCODES + 2,
        DLSYM                                   = PLATFORM_OPCODES + 3,
        LAST_PLATFORM_TYPE_OPCODE               = DLSYM,

/*if[!ENABLE_CHANNEL_GUI]*/
        LAST_GUI_OPCODE                         = LAST_PLATFORM_TYPE_OPCODE;
/*else[ENABLE_CHANNEL_GUI]*/
//        GUI_OPCODES                           = LAST_PLATFORM_TYPE_OPCODE + 1; /* Opcodes for KAWT graphics API */
//        SETWINDOWNAME                           = GUI_OPCODES + 0,
//        SCREENWIDTH                             = GUI_OPCODES + 1,
//        SCREENHEIGHT                            = GUI_OPCODES + 2,
//        BEEP                                    = GUI_OPCODES + 3,
//        SETOFFSCREENMODE                        = GUI_OPCODES + 4,
//        FLUSHSCREEN                             = GUI_OPCODES + 5,
//        CREATEIMAGE                             = GUI_OPCODES + 6,
//        CREATEMEMORYIMAGE                       = GUI_OPCODES + 7,
//        GETIMAGE                                = GUI_OPCODES + 8,
//        IMAGEWIDTH                              = GUI_OPCODES + 9,
//        IMAGEHEIGHT                             = GUI_OPCODES + 10,
//        DRAWIMAGE                               = GUI_OPCODES + 11,
//        FLUSHIMAGE                              = GUI_OPCODES + 12,
//        CREATEFONTMETRICS                       = GUI_OPCODES + 13,
//        FONTSTRINGWIDTH                         = GUI_OPCODES + 14,
//        FONTGETHEIGHT                           = GUI_OPCODES + 15,
//        FONTGETASCENT                           = GUI_OPCODES + 16,
//        FONTGETDESCENT                          = GUI_OPCODES + 17,
//        SETFONT                                 = GUI_OPCODES + 18,
//        SETCOLOR                                = GUI_OPCODES + 19,
//        SETCLIP                                 = GUI_OPCODES + 20,
//        DRAWSTRING                              = GUI_OPCODES + 21,
//        DRAWLINE                                = GUI_OPCODES + 22,
//        DRAWOVAL                                = GUI_OPCODES + 23,
//        DRAWRECT                                = GUI_OPCODES + 24,
//        FILLRECT                                = GUI_OPCODES + 25,
//        DRAWROUNDRECT                           = GUI_OPCODES + 26,
//        FILLROUNDRECT                           = GUI_OPCODES + 27,
//        FILLARC                                 = GUI_OPCODES + 28,
//        FILLPOLYGON                             = GUI_OPCODES + 29,
//        REPAINT                                 = GUI_OPCODES + 30,
//        LAST_GUI_OPCODE                         = REPAINT;
/*end[ENABLE_CHANNEL_GUI]*/


            
/*if[DEBUG_CODE_ENABLED]*/
    private static final String[] Mnemonics = {
        "[invalid opcode]",
        "GLOBAL_CREATECONTEXT",     // 1
        "GLOBAL_GETEVENT",          // 2
        "GLOBAL_POSTEVENT",         // 3
        "GLOBAL_WAITFOREVENT",      // 4
        "CONTEXT_DELETE",           // 5
        "CONTEXT_HIBERNATE",        // 6
        "CONTEXT_GETHIBERNATIONDATA", // 7
        "CONTEXT_GETCHANNEL",       // 8
        "CONTEXT_FREECHANNEL",      // 9
        "CONTEXT_GETRESULT",        // 10
        "CONTEXT_GETRESULT_2",      // 11
        "CONTEXT_GETERROR",         // 12
        "OPENCONNECTION ",          // 13
        "CLOSECONNECTION ",         // 14
        "ACCEPTCONNECTION ",        // 15
        "OPENINPUT",                // 16
        "CLOSEINPUT",               // 17
        "WRITEREAD",                // 18
        "READBYTE",                 // 19
        "READSHORT",                // 20
        "READINT",                  // 21
        "READLONG",                 // 22
        "READBUF",                  // 23
        "SKIP",                     // 24
        "AVAILABLE",                // 25
        "MARK",                     // 26
        "RESET",                    // 27
        "MARKSUPPORTED",            // 28
        "OPENOUTPUT",               // 29
        "FLUSH",                    // 30
        "CLOSEOUTPUT",              // 31
        "WRITEBYTE",                // 32
        "WRITESHORT",               // 33
        "WRITEINT",                 // 34
        "WRITELONG",                // 35
        "WRITEBUF"                  // 36
    };

    public static String getMnemonic(int op) {
        try {
            return Mnemonics[op];
        } catch (ArrayIndexOutOfBoundsException ex) {
            return "opcode:" + op;
        }
    }
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * PLATFORM CHANNELS
     */
    public static final int CHANNEL_IRQ                     = CHANNEL_LAST_FIXED + 1;
    public static final int CHANNEL_SPI                     = CHANNEL_LAST_FIXED + 2;
    public static final int CHANNEL_LAST_PLATFORM           = CHANNEL_SPI;

    /*
     * PLATFORM OPCODES
     * NOTE: Sparse channel IDs creates fat switch statements. Try to pack...
     */

    public static final int PLATFORM_OPCODE                             = LAST_GUI_OPCODE + 1;
    /**
     * The irq wait message.
     */
    public static final int IRQ_WAIT                                    = PLATFORM_OPCODE + 0;
    public static final int GET_HARDWARE_REVISION                       = PLATFORM_OPCODE + 1;
    public static final int GET_LAST_DEVICE_INTERRUPT_TIME_ADDR         = PLATFORM_OPCODE + 2;
    public static final int GET_CURRENT_TIME_ADDR                       = PLATFORM_OPCODE + 3;
    
    public static final int SPI_OPCODES                                 = PLATFORM_OPCODE + 4;
    public static final int SPI_SEND_RECEIVE_8                          = SPI_OPCODES + 0;
    public static final int SPI_SEND_RECEIVE_8_PLUS_SEND_16             = SPI_OPCODES + 1;
    public static final int SPI_SEND_RECEIVE_8_PLUS_SEND_N              = SPI_OPCODES + 2;
    public static final int SPI_SEND_RECEIVE_8_PLUS_RECEIVE_16          = SPI_OPCODES + 3;
    public static final int SPI_SEND_RECEIVE_8_PLUS_VARIABLE_RECEIVE_N  = SPI_OPCODES + 4;
    public static final int SPI_SEND_AND_RECEIVE_WITH_DEVICE_SELECT     = SPI_OPCODES + 5;
    public static final int SPI_SEND_AND_RECEIVE                        = SPI_OPCODES + 6;
    public static final int SPI_GET_MAX_TRANSFER_SIZE                   = SPI_OPCODES + 7;
    public static final int SPI_PULSE_WITH_DEVICE_SELECT                = SPI_OPCODES + 8;

    public static final int I2C_OPCODES                         = SPI_OPCODES + 9;
    public static final int I2C_OPEN                            = I2C_OPCODES + 0;
    public static final int I2C_CLOSE                           = I2C_OPCODES + 1;
    public static final int I2C_SET_CLOCK_SPEED                 = I2C_OPCODES + 2;
    public static final int I2C_READ                            = I2C_OPCODES + 3;
    public static final int I2C_WRITE                           = I2C_OPCODES + 4;
    public static final int I2C_BUSY                            = I2C_OPCODES + 5;
    public static final int I2C_PROBE                           = I2C_OPCODES + 6;

    public static final int SLEEP_OPCODES                       = I2C_OPCODES + 7;
    public static final int DEEP_SLEEP                          = SLEEP_OPCODES + 0;
    public static final int SHALLOW_SLEEP                       = SLEEP_OPCODES + 1;
    public static final int WAIT_FOR_DEEP_SLEEP                 = SLEEP_OPCODES + 2;
    public static final int DEEP_SLEEP_TIME_MILLIS_HIGH         = SLEEP_OPCODES + 3;
    public static final int DEEP_SLEEP_TIME_MILLIS_LOW          = SLEEP_OPCODES + 4;
    public static final int SET_DEEP_SLEEP_ENABLED              = SLEEP_OPCODES + 5;
    public static final int SET_MINIMUM_DEEP_SLEEP_TIME         = SLEEP_OPCODES + 6;
    public static final int TOTAL_SHALLOW_SLEEP_TIME_MILLIS_HIGH = SLEEP_OPCODES + 7;
    public static final int TOTAL_SHALLOW_SLEEP_TIME_MILLIS_LOW = SLEEP_OPCODES + 8;
    public static final int SET_SHALLOW_SLEEP_CLOCK_MODE        = SLEEP_OPCODES + 9;

    public static final int MISC_OPCODES                        = SLEEP_OPCODES + 10;

    public static final int FLASH_ERASE                         = MISC_OPCODES + 0;
    public static final int FLASH_WRITE                         = MISC_OPCODES + 1;

    public static final int USB_GET_STATE                       = MISC_OPCODES + 2;

    public static final int GET_SERIAL_CHARS                    = MISC_OPCODES + 3;
    public static final int WRITE_SERIAL_CHARS                  = MISC_OPCODES + 4;
    public static final int AVAILABLE_SERIAL_CHARS              = MISC_OPCODES + 5;
    
    public static final int AVR_GET_TIME_HIGH                   = MISC_OPCODES + 6;
    public static final int AVR_GET_TIME_LOW                    = MISC_OPCODES + 7;
    public static final int AVR_GET_STATUS                      = MISC_OPCODES + 8;
    
    public static final int WRITE_SECURED_SILICON_AREA          = MISC_OPCODES + 9;
    public static final int READ_SECURED_SILICON_AREA           = MISC_OPCODES + 10;
    
    public static final int SET_SYSTEM_TIME                     = MISC_OPCODES + 11;
    public static final int ENABLE_AVR_CLOCK_SYNCHRONISATION    = MISC_OPCODES + 12;

    public static final int GET_PUBLIC_KEY                      = MISC_OPCODES + 13;

    public static final int COMPUTE_CRC16_FOR_MEMORY_REGION     = MISC_OPCODES + 14;
    
    public static final int REPROGRAM_MMU                       = MISC_OPCODES + 15;
    public static final int GET_ALLOCATED_FILE_SIZE             = MISC_OPCODES + 16;
    public static final int GET_FILE_VIRTUAL_ADDRESS            = MISC_OPCODES + 17;
    
    public static final int GET_DMA_BUFFER_SIZE                 = MISC_OPCODES + 18;
    public static final int GET_DMA_BUFFER_ADDRESS              = MISC_OPCODES + 19;

    public static final int GET_RECORDED_OUTPUT                 = MISC_OPCODES + 20;
    public static final int GET_VAR_ADDR                        = MISC_OPCODES + 21;

    public static final int GET_SYSTEM_CORE_CLOCK               = MISC_OPCODES + 22;

/*if[ENABLE_ETHERNET_SUPPORT]*/
    /*
     * Ethernet related OPCODES
     */
    public static final int ETHERNET_OP                         = MISC_OPCODES + 23;
    public static final int ETHERNET_INIT                       = ETHERNET_OP + 0;
    public static final int ETHERNET_CONFIG_NETIF               = ETHERNET_OP + 1;
    public static final int ETHERNET_START_PROCESS              = ETHERNET_OP + 2;
    public static final int ETHERNET_STOP_PROCESS               = ETHERNET_OP + 3;
    public static final int ETHERNET_BIND                       = ETHERNET_OP + 4;
    public static final int ETHERNET_LISTEN                     = ETHERNET_OP + 5;
    public static final int ETHERNET_SEND                       = ETHERNET_OP + 6;
    public static final int ETHERNET_CONNECT                    = ETHERNET_OP + 7;
    public static final int ETHERNET_CLOSE                      = ETHERNET_OP + 8;    
    public static final int ETHERNET_TCP_NEW                    = ETHERNET_OP + 9;
    public static final int ETHERNET_CHECK_EVENT                = ETHERNET_OP + 10;
    public static final int ETHERNET_POP_EVENT                  = ETHERNET_OP + 11;
    public static final int ETHERNET_GET_EVENT_OPCODE           = ETHERNET_OP + 12;
    public static final int ETHERNET_GET_EVENT_PCB              = ETHERNET_OP + 13;
    public static final int ETHERNET_GET_EVENT_BUF              = ETHERNET_OP + 14;
    public static final int ETHERNET_GET_EVENT_BUF_PTR          = ETHERNET_OP + 15;
    public static final int ETHERNET_GET_EVENT_BUF_PUSHTOAPP    = ETHERNET_OP + 16;    
    public static final int ETHERNET_GET_EVENT_BUF_LEN          = ETHERNET_OP + 17;
    public static final int ETHERNET_GET_EVENT_ERR              = ETHERNET_OP + 18;
    public static final int ETHERNET_GET_EVENT_ARG              = ETHERNET_OP + 19;
    public static final int ETHERNET_COPY_PBUF                  = ETHERNET_OP + 20;
    public static final int ETHERNET_FREE_PBUF                  = ETHERNET_OP + 21;
    public static final int ETHERNET_SET_SCKOPT                 = ETHERNET_OP + 22;
    public static final int ETHERNET_GET_SCKOPT                 = ETHERNET_OP + 23;
    public static final int GET_ETHERNET_EVENT                  = ETHERNET_OP + 24;
    public static final int ETHERNET_DNS_SETSERVER              = ETHERNET_OP + 25;
    public static final int ETHERNET_DNS_GETSERVER              = ETHERNET_OP + 26;
/*end[ENABLE_ETHERNET_SUPPORT]*/

    /*
     * Index of variable in C. 
     */
    public static final int VAR_JAVA_IRQ_GPIO_STATUS            = 0;
    
    /*
     * Internal codes used to execute C code on the service stack.
     */
    public final static int
        INTERNAL_SETSTREAM                      = 1000,
        INTERNAL_OPENSTREAM                     = 1001,
        INTERNAL_PRINTCHAR                      = 1002,
        INTERNAL_PRINTSTRING                    = 1003,
        INTERNAL_PRINTINT                       = 1004,
        INTERNAL_PRINTFLOAT                     = 1005,
        INTERNAL_PRINTDOUBLE                    = 1006,
        INTERNAL_PRINTUWORD                     = 1007,
        INTERNAL_PRINTOFFSET                    = 1008,
        INTERNAL_PRINTLONG                      = 1009,
        INTERNAL_PRINTADDRESS                   = 1010,
        INTERNAL_LOW_RESULT                     = 1011,
        INTERNAL_GETTIMEMILLIS_HIGH             = 1012,
        INTERNAL_GETTIMEMICROS_HIGH             = 1013,
        INTERNAL_STOPVM                         = 1014,
        INTERNAL_NATIVE_PLATFORM_NAME           = 1015,
        INTERNAL_PRINTCONFIGURATION             = 1016,
        INTERNAL_PRINTGLOBALOOPNAME             = 1017,
        INTERNAL_PRINTGLOBALS                   = 1018,
        INTERNAL_GETPATHSEPARATORCHAR           = 1019,
        INTERNAL_GETFILESEPARATORCHAR           = 1020,
        INTERNAL_COMPUTE_SHA1_FOR_MEMORY_REGION = 1021,
        INTERNAL_PRINTBYTES                     = 1022,
		RPI_PRINTSTRING							= 1023,

/*if[OLD_IIC_MESSAGES]*/
        /* Message I/O Operations */
        INTERNAL_ALLOCATE_MESSAGE_BUFFER        = 1024,
        INTERNAL_FREE_MESSAGE_BUFFER            = 1025,
        INTERNAL_SEND_MESSAGE_TO_SERVER         = 1026,
        INTERNAL_SEND_MESSAGE_TO_CLIENT         = 1027,
        INTERNAL_RECEIVE_MESSAGE_FROM_SERVER    = 1028,
        INTERNAL_RECEIVE_MESSAGE_FROM_CLIENT    = 1029,
        INTERNAL_SEARCH_SERVER_HANDLERS         = 1030,
        LAST_OPCODE                             = 1030;
/*else[OLD_IIC_MESSAGES]*/
//      LAST_OPCODE                             = 1023;
/*end[OLD_IIC_MESSAGES]*/


}
