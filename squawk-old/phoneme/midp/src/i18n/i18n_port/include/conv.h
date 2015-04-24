/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

/**
 * @file
 * @brief Function for converting between unicode and native encoding.
 */
#ifndef _CONV_H_
#define _CONV_H_

#include <kni.h>

/**
 * Function set for converting a given locale to unicode.
 */
typedef struct _LcConvMethodsRec {
    char *encoding;
    int (*byteMaxLen)(void);
    int (*byteLen)(const unsigned char*, int);
    int (*unicodeToNative)(const jchar*, int, unsigned char*, int);
    int (*nativeToUnicode)(const unsigned char*, int, jchar*, int);
    int (*sizeOfByteInUnicode)(const unsigned char*, int, int);
    int (*sizeOfUnicodeInByte)(const jchar*, int, int);
} LcConvMethodsRec, *LcConvMethods;

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Get the conversion function set for a given locale.
 * This function should be implemented for target platform that supports
 * non-western locales.
 */
LcConvMethods getLcGenConvMethods(char*);

#ifdef __cplusplus
}
#endif

#endif /* _CONV_H_ */
