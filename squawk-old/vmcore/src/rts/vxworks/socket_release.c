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

#include "sockLib.h"
#include "stdLib.h"
#include "stdio.h"

typedef struct {
	void* previous;
	void* next;
	int socketHandle;
} socketRegistration;

char initialized = 0;
socketRegistration rootSocket;


socketRegistration* getRootSocket() {
	if (!initialized) {
			rootSocket.next = &rootSocket;
			rootSocket.previous = &rootSocket;
			rootSocket.socketHandle = 0;
			initialized = 1;
	}
	return &rootSocket;
}

socketRegistration* registerSocket (int socketHandle) {
	
	socketRegistration* newSocket = malloc(sizeof(socketRegistration));
	newSocket->socketHandle = socketHandle;
	newSocket->previous = getRootSocket()->previous;
	newSocket->next = getRootSocket();
	((socketRegistration*)getRootSocket()->previous)->next = newSocket;
	getRootSocket()->previous = newSocket;
	return newSocket;
}

void clearRegistration (socketRegistration* registeredSocket) {
	((socketRegistration*)registeredSocket->next)->previous = registeredSocket->previous;
	((socketRegistration*)registeredSocket->previous)->next = registeredSocket->next;
	free(registeredSocket);	
}

void clearSocket(int socketHandle) {
	socketRegistration* registeredSocket = getRootSocket();
	while ((registeredSocket = registeredSocket->next) != getRootSocket()) {
		if (registeredSocket->socketHandle == socketHandle) {
			clearRegistration(registeredSocket);
		}
	}
}

void clearRemainingSockets () {
	while (getRootSocket()->next != getRootSocket()) {
		close(((socketRegistration*)getRootSocket()->next)->socketHandle);
		clearRegistration(getRootSocket()->next);
	}
}

