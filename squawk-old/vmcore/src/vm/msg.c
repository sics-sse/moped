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

/*=======================================================================*\
 *                             Private routines                          *
\*=======================================================================*/

/**
 * Support for the old C-based inter-isolate message passing scheme.
 * Included when OLD_IIC_MESSAGES is defined
 */

#if SQUAWK_64
const UWord EVENT_MESSAGE_HI_BITS_MASK = 0xFFFFFFFF80000000L;
#else
const UWord EVENT_MESSAGE_HI_BITS_MASK = 0x80000000;
#endif /* SQUAWK_64 */

/**
 * The message data structure. Keep in sync with MessageStruct.java
 * in define directory.
 */
typedef struct messageStruct {
    struct messageStruct *next;
    int                  status;
    Address              data;
    char                 name[MessageStruct_MAX_MESSAGE_KEY_SIZE];
} Message;


#if KERNEL_SQUAWK
/**
 * Macros to generate and compare device-specific interrupt urls
 */
#define createInterruptURL(buf, interrupt) sprintf(buf, "////irq%d", interrupt)
#define isDeviceKey(key)      (strncmp(key, "////irq", 7) == 0)
#define getInterruptNum(key)  (atoi((char *)key + 7))

#else
/*
 * Routines that will come from devices.c if in kernel mode
 */
INLINE void assumeInterruptsAreDisabled() { }
INLINE void *safeMalloc(unsigned length) {
//  printf("Trying to allocate %d bytes\n", length);
    return malloc(length);
}

#endif /* KERNEL_SQUAWK */

// Forward declarations
INLINE void assumeInterruptsAreDisabled();
INLINE int enableInterrupts();
INLINE void disableInterrupts();
INLINE void *safeMalloc(unsigned length);

/**
 * Allocates a message buffer.
 *
 * @return the buffer of null if none is available
 */
INLINE Message *allocateMessage(Address key) {
    int lth = getArrayLength(key);
    Address cls = getClass(key);
    Message *buf = (Message *)freeMessages;
    if (buf != null) {
        freeMessages = buf->next;
    }
    if (buf == null) {
        buf = (Message *)safeMalloc(sizeof(Message));
    }
    if (buf != null) {
        if (com_sun_squawk_Klass_id(cls) != com_sun_squawk_StringOfBytes) {
            fatalVMError("Key not com_sun_squawk_StringOfBytes");
        }
        if (lth >= MessageStruct_MAX_MESSAGE_KEY_SIZE) {
            fatalVMError("Key too large");
        } else {
            int i;
            unsigned char *chars = (unsigned char *)key;
            for (i = 0 ; i < lth ; i++) {
                buf->name[i] = chars[i];
            }
            buf->name[lth] = 0;
            buf->status = ChannelConstants_RESULT_OK;
            buf->next = null;
            buf->data = null;
        }
    }
    return buf;
}

/**
 * Frees a message buffer.
 *
 * @param addr the buffer to free
 */
INLINE void freeMessage(Message *msg) {
    assumeInterruptsAreDisabled();
#if KERNEL_SQUAWK
    /* Don't reuse device message buffers! */
    if (isDeviceKey(msg->name)) {
        return;
    }
#endif
    msg->next = (Message *)freeMessages;
    freeMessages = msg;
}

/**
 * Compairs the two messages to see if the name is the same.
 *
 * @param msg the message
 * @param key the Java string to compare the message with
 * @return true if the names are the same
 */
INLINE boolean compareMessageWithKey(Message *msg, Address key) {
    int lth = getArrayLength(key);
    Address cls = getClass(key);
    if (com_sun_squawk_Klass_id(cls) != com_sun_squawk_StringOfBytes) {
        fatalVMError("Key not com_sun_squawk_StringOfBytes");
    }
    if (lth >= MessageStruct_MAX_MESSAGE_KEY_SIZE) {
        fatalVMError("Key too large");
    } else {
        int i;
        unsigned char *chars = (unsigned char *)key;
        for (i = 0 ; i < lth ; i++) {
            if (msg->name[i] != chars[i]) {
                return false;
            }
        }
        return msg->name[lth] == 0;
    }
    return false;
}

/**
 * Prints the fields in a Message to stderr.
 *
 * @param  msg  the Message structure to print
 */
static void printMessage(Message *msg) {
    if (msg == null) {
        fprintf(stderr, "null");
    } else {
        fprintf(stderr, format("%A:{name=\"%s\" data=%A next=%A}"), msg, msg->name, msg->data, msg->next);
    }
}

/**
 * Prints a queue of Messages to stderr.
 *
 * @param  entry   the entry in the Message queue from which to start printing
 */
static void printMessageQueue(Message *entry) {
    if (entry == null) {
        printMessage(entry);
    } else {
        Message *first = entry->next;
        do {
            entry = entry->next;
            if (entry != first)
                fprintf(stderr, ", ");
            printMessage(entry);
        } while (entry != first);
    }
}

/**
 * Dumps out all the server and client queues to stderr.
 */
static void dumpOutMessageQueues(void) {
    fprintf(stderr, "Message queues (combined):\n");
    fprintf(stderr, "Server messages\n");
    printMessageQueue(toServerMessages); fprintf(stderr, "\n");
    fprintf(stderr, "Client messages:\n");
    printMessageQueue(toClientMessages); fprintf(stderr, "\n\n");
    fprintf(stderr, "Server waiters\n");
    printMessageQueue(toServerWaiters); fprintf(stderr, "\n");
    fprintf(stderr, "Client waiters\n");
    printMessageQueue(toClientWaiters); fprintf(stderr, "\n");
    fprintf(stderr, "Message events\n");
    printMessageQueue(messageEvents); fprintf(stderr, "\n\n");
}

/**
 * Adds a message to a message queue.
 *
 * @param msg the message
 * @param msgq the queue to add the message to
 */
INLINE void addMessage(Message *msg, Address *msgq) {
    Message **q = (Message **)msgq;
    Message *entry = *q;
/*
    fprintf(stderr, "addMessage::\n    msgq=");
    printMessageQueue(*q);
    fprintf(stderr, "\n    msg=");
    printMessageQueue(msg);
    fprintf(stderr, "\n\n");
*/
    if (entry == null) {
        *q = msg;
        msg->next = msg;
    } else {
        msg->next = entry->next;
        entry->next = msg;
        *q = msg;
    }
}

/**
 * Tests to see of there is a message
 *
 * @param msgq the queue to remove the message from
 * @param key  the key to search for
 * @return true if there is a waiting message
 */
INLINE boolean testForMessage(Address *msgq, Address key) {
    Message **q = (Message **)msgq;
    Message *entry = *q;
    if (entry != null) {
        do {
            entry = entry->next;
            if (compareMessageWithKey(entry, key)) {
                return true;
            }
        } while (entry != *q);
    }
    return false;
}


/**
 * Finds and removes a message from a message queue.
 *
 * @param msgq the queue to remove the message from
 * @param key  the key to search for
 * @return the message or null is none is available
 */
INLINE Message *findAndRemoveMessage(Address *msgq, Address key) {
    Message **q = (Message **)msgq;
    Message *last;
    Message *entry = *q;
    if (entry != null) {
        do {
            last = entry;
            entry = entry->next;
            if (compareMessageWithKey(entry, key)) {
                if (entry == last) {
                    *q = null;
                } else {
                    last->next = entry->next;
                    if (*q == entry)
                        *q = last;
                }
                entry->next = null;
                return entry;
            }
        } while (entry != *q);
    }

    return null;
}

/**
 * Adds a message event to the event queue.
 *
 * @param key the Java string to compare the message with
 */
INLINE boolean addMessageEvent(Address *waitq, Address key) {
    Message *event = findAndRemoveMessage(waitq, key);
    if (event != null) {
        addMessage(event, &messageEvents);
        return true;
    }
    return false;
}

/**
 * Gets the positive integer unique identifier for a given Message data structure.
 * The unique identifier is simply the low 31 bits of 'msg's address. This routine
 * checks the assumption that the other bits of a message's address is identical
 * across all messages.
 *
 * @param  msg  the message for which a unique ID is requested
 * @return a positive integer representing a unique ID for 'msg'
 */
INLINE int getMessageID(Message *msg) {
    static UWord msgHiBits = 0;
    if (msgHiBits == 0) {
        msgHiBits = (((UWord)msg) & EVENT_MESSAGE_HI_BITS_MASK);
    }
    if (msgHiBits != (((UWord)msg) & EVENT_MESSAGE_HI_BITS_MASK)) {
        fatalVMError("message data structure not uniquely identified by the low 31 bits of its address");
    }
    return ((int)(UWord)msg) & (~EVENT_MESSAGE_HI_BITS_MASK);
}

/*=======================================================================*\
 *                             Public Interface                          *
\*=======================================================================*/

/**
 * Checks whether there is an available message event.
 *
 * @return true if an event was found
 */
INLINE boolean checkForMessageEvent() {
    boolean res = false;
    deferInterruptsAndDo(
        Message *last;
        Message *entry = (Message *)messageEvents;
        if (entry != null) {
            do {
                last = entry;
                entry = entry->next;
                if (isCurrentContext(entry->status, int)) {
                	res = true;
                    break;
                }
            } while ((Address)entry != messageEvents);
        }
    );
    return res;
}

/**
 * Tries to get a thread event related to the message system.
 *
 * @return true if an event was found
 */
INLINE boolean getMessageEvent() {
    boolean res = false;
    deferInterruptsAndDo(
        Message *last;
        Message *entry = (Message *)messageEvents;
        if (entry != null) {
            do {
                last = entry;
                entry = entry->next;
                if (isCurrentContext(entry->status, int)) {
                    if (entry == last) {
                        messageEvents = null;
                    } else {
                        last->next = entry->next;
                        if (messageEvents == (Address)entry)
                            messageEvents = (Address)last;
                    }

                    entry->status = ChannelConstants_RESULT_OK;
                    entry->next = null;
                    returnAddressResult(null);
                    returnIntResult(getMessageID(entry));
                    //iprintf("got Message event %s\n", entry->name);
                    freeMessage(entry);
                    res = true;
                    break;
                }
            } while ((Address)entry != messageEvents);
        }
    );
    return res;
}

/**
 * Allocates a message buffer.
 *
 * @return the buffer of null if none is available
 */
INLINE void allocateMessageBuffer() {
    Address buf = freeMessageBuffers;
    if (buf != null) {
        freeMessageBuffers = ((Address*)buf)[MessageBuffer_next];
    }
    if (buf == null) {
        buf = (Address)safeMalloc(MessageBuffer_HEADERSIZE+MessageBuffer_BUFFERSIZE);
    }
    returnAddressResult(buf);
    if (buf != null) {
        ((Address*)buf)[MessageBuffer_next]  = 0;
        ((Address*)buf)[MessageBuffer_pos]   = 0;
        ((Address*)buf)[MessageBuffer_count] = 0;
        returnIntResult(ChannelConstants_RESULT_OK);
    } else {
        returnIntResult(ChannelConstants_RESULT_MALLOCFAILURE);
    }
}

/**
 * Frees a message buffer.
 *
 * @param addr the buffer
 */
INLINE void freeMessageBuffer(Address addr) {
    assumeInterruptsAreDisabled();
    ((Address*)addr)[MessageBuffer_next] = freeMessageBuffers;
    freeMessageBuffers = addr;
    returnAddressResult(null);
    returnIntResult(ChannelConstants_RESULT_OK);
}

/**
 * Places a interrupt notification message in the server queue. This is called
 * from a low-level interrupt handler when an interrupt message is needed to be
 * sent to a Java device driver.
 *
 * @param msg a pointer to a statically allocated message data structure
 */
INLINE void sendInterruptMessage(Message *msg) {
    assumeInterruptsAreDisabled();
    addMessage(msg, &toServerMessages);
}

#if KERNEL_SQUAWK
/**
 *
 * This routine lets us keeps stats and perform final actions on
 * underlying devices once an interrupt is handled and the
 * device's connection is closed. See JavaDriverManager.java.
 *
 * In most cases, this will be the hand-shake that re-enables
 * the device's interrupts and enables it to enqueue the next
 * request.
 *
 * @param interruptNumber the interrupt id being handled
 * @param buffers         a pointer to MessageBuffer data: useful if this has been allocated
 */
INLINE void interruptDone(int interruptNumber, Address buffers) {
    void os_interruptDone(int interruptNumber);
    os_interruptDone(interruptNumber);
}
#endif

/**
 * Checks the message key to see if is an attempt to send a message to
 * an interrupt handler. If this is the case then the low-level interrupt
 * handler is called to inform it that the interrupt has been processed
 * and that its statically allocated message data structure can be reused.
 *
 * Recall that the model for interacting with devices is:
 *     - application opens "////dev-request" (or equivalent) passing in parameters
 *     - JavaDriverManager's loop processes the request sending data to the device
 *     - the device, on completion, sends an interrupt and a result is written back
 *          to the activeRequest
 *       - this is done by having the signal handler send its statically allocated
 *            (dummy) message to "////irq<nn>" and calling Squawk_kernelContinue()
 *            if we're not already in kernel-mode (see Squawk_enterKernel() for the
 *            protocol).
 *       - the last action of the processing of the message for "////irq<nn>" will
 *            call close on "con" with its dummy message and that is how we end up
 *            here.
 * Note that interrupts should be reenabled by interruptDone() and not by returning
 * out of the interrupt handler.
 *
 * Alternative view: sendMessage() is invoked as part of closing a connection.
 * We are checking to see if we need to pass an actual message or whether
 * we are closing a connection to a device (which is not expecting a message).
 * In the latter case, we instead call "interruptDone()"--for example,
 * to gather statistics--and return.
 *
 * The current naming convention is that device interrupts are labelled
 * "irq<nn>". This can be revisited but should be kept in sync with
 * JavaDriverManager.java.
 *
 * @param key a pointer to a Java string with the message key
 * @param buffers         a pointer to MessageBuffer data: useful if this has been allocated
 */
INLINE boolean checkForDeviceInterrupt(Address key, Address buffers) {
#if KERNEL_SQUAWK
    if (isDeviceKey(key)) {
        int interruptNumber = getInterruptNum(key);
        interruptDone(interruptNumber, buffers);
        return true;
    }
#endif
    return false;
}

/**
 * Places a message in a message queue.
 *
 * @param key     a pointer to a Java string with the message key
 * @param buffers the address of the buffer list
 * @param status  out-of-band status for message
 * @param msgq    the message queue address
 * @param waitq   the wait queue address
 */
INLINE void sendMessage(Address key, Address buffers, int status, Address *msgq, Address *waitq) {
    deferInterruptsAndDo(
        if (checkForDeviceInterrupt(key, buffers) == false) {
            Message *msg = allocateMessage(key);
            if (msg == null) {
                returnIntResult(ChannelConstants_RESULT_MALLOCFAILURE);
            } else {
                msg->status = status;
                msg->data = buffers; // The list of buffers
                addMessage(msg, msgq);
                addMessageEvent(waitq, key);
                returnIntResult(ChannelConstants_RESULT_OK);
            }
            returnAddressResult(null);
        } else {
            returnIntResult(ChannelConstants_RESULT_OK);
            returnAddressResult(null);
        }
    );
}


/**
 * Attempts to take a message from a message queue.
 *
 * @param key    a pointer to a Java string with the message key
 * @param msgq   the message queue address
 * @param waitq  the wait queue address
 */
INLINE void receiveMessage(Address key, Address *msgq, Address *waitq) {
    deferInterruptsAndDo(
        Message *msg = findAndRemoveMessage(msgq, key);
        if (msg != null) {
            if (msg->status == ChannelConstants_RESULT_OK) {
                returnAddressResult(msg->data);
            } else {
                returnAddressResult(null);
            }
            returnIntResult(msg->status);
            freeMessage(msg);
        } else {
            Message *event = allocateMessage(key);
            if (event == null) {
                returnIntResult(ChannelConstants_RESULT_MALLOCFAILURE);
            } else {
                setContext(event->status, int);
                addMessage(event, waitq);
                returnIntResult(getMessageID(event));
            }
            returnAddressResult(null);
        }
    );
}


/**
 * Searches the list of ServerConnectionHandlers looking for the first one
 * for which there is a waiting message.
 *
 * @param handlers the list of handlers
 */
INLINE void searchServerHandlers(Address handler) {
    deferInterruptsAndDo(
        //dumpOutMessageQueues();
        while (handler != null) {
            Address key = com_sun_squawk_io_ServerConnectionHandler_name(handler);
            // fprintf(stderr, "   handler is %p key is %p (%s)\n", handler, key, (key ? key : "(null)"));
            if (testForMessage(&toServerMessages, key)) {
                break;
            }
            handler = com_sun_squawk_io_ServerConnectionHandler_next(handler);
        }
        // fprintf(stderr, "   ---> handler is %p\n", handler);
        returnAddressResult(handler);
        returnIntResult(ChannelConstants_RESULT_OK);
    );
}



/**
 * Searched the list of ServerConnectionHandler looking for the first one
 * for which there is a waiting message.
 *
 * @param handlers the list of handlers
 */
INLINE boolean checkForNonemptyQueues(void) {
    Address handler = com_sun_squawk_VM_serverConnectionHandlers;
    assumeInterruptsAreDisabled();
    while (handler != null) {
        Address key = com_sun_squawk_io_ServerConnectionHandler_name(handler);
        if (testForMessage(&toServerMessages, key)) {
            return true;
        }
        handler = com_sun_squawk_io_ServerConnectionHandler_next(handler);
    }
    return false;
}

