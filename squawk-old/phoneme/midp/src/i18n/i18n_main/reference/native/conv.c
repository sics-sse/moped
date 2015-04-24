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

#include <stdlib.h>
#include <string.h>

#include <kni.h>

#include <midpError.h>
#include <midpMalloc.h>
#include <conv.h>

#if ENABLE_I18N_JAPANESE
extern LcConvMethodsRec SJISConvRec;
extern LcConvMethodsRec EUCJPConvRec;

#define NUM_LCCONV 3

static LcConvMethods lcConv[NUM_LCCONV] = {
/* 0 */    &SJISConvRec,
/* 1 */    &EUCJPConvRec,
/* 2 */    NULL, /* depositary for a platform general converter */
};

#else

#define NUM_LCCONV 1
static LcConvMethods lcConv[NUM_LCCONV] = {
/* 0 */    NULL, /* depositary for a platform general converter */
};

#endif /* ENABLE_I18N_JAPANESE */

static int
getLcConvMethodsIDByEncoding(char *encoding) {
    if (encoding && *encoding) {
        int i;
        for (i = 0; i < NUM_LCCONV; i++) {
            if (lcConv[i] == NULL) {
                break;
            }
            if (strcmp(lcConv[i]->encoding, encoding) == 0) {
                return i;
            }
        }
        if (i < NUM_LCCONV) {
            lcConv[i] = getLcGenConvMethods(encoding);
            if (lcConv[i] != NULL) {
                return i;
            }
        }
    }
    return -1;
}

#define MAX_LEN_ENC_NAME 15
static int
getLcConvMethodsID(jchar *uc, int len) {
    char enc[MAX_LEN_ENC_NAME + 1];
    int i;
    
    /* Check the length of the encoding name */
    if (len > MAX_LEN_ENC_NAME) {
        len = MAX_LEN_ENC_NAME;
    }
    for (i = 0; i < len; i++) {
        enc[i] = (char) uc[i];
    }
    enc[i] = 0;

    return getLcConvMethodsIDByEncoding(enc);
}

/**
 * Gets a handle to specific character encoding conversion routine.
 * <p>
 * Java declaration:
 * <pre>
 *     getHandler(Ljava/lang/String;)I
 * </pre>
 *
 * @param encoding character encoding
 *
 * @return identifier for requested handler, or <tt>-1</tt> if
 *         the encoding was not supported.
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_cldc_i18n_j2me_Conv_getHandler() {
    jint     result = 0;

    KNI_StartHandles(1);

    KNI_DeclareHandle(str);
    KNI_GetParameterAsObject(1, str);

    if (!KNI_IsNullHandle(str)) {
        int      strLen = KNI_GetStringLength(str);
	jchar* strBuf;

	/* Instead of always multiplying the length by sizeof(jchar),
	 * we shift left by 1. This can be done because jchar has a
	 * size of 2 bytes.
	 */
	strBuf = (jchar*)midpMalloc(strLen<<1);
	if (strBuf != NULL) { 
	    KNI_GetStringRegion(str, 0, strLen, strBuf);
	    result = getLcConvMethodsID(strBuf, strLen);
	    midpFree(strBuf);
	} else {
	    KNI_ThrowNew(midpOutOfMemoryError, NULL);
	}
    }

    KNI_EndHandles();
    KNI_ReturnInt(result);
}

/**
 * Gets the maximum length in bytes for a converted string.
 * <p>
 * Java declaration:
 * <pre>
 *     getMaxByteLength(I)I
 * </pre>
 *
 * @param handler handle returned from getHandler
 *
 * @return maximum byte length, or zero if the handler is not valid
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_cldc_i18n_j2me_Conv_getMaxByteLength() {
    int id = KNI_GetParameterAsInt(1);

    KNI_ReturnInt(lcConv[id] ? lcConv[id]->byteMaxLen() : 0);
}

/**
 * Gets the length of a specific converted string.
 * <p>
 * Java declaration:
 * <pre>
 *     getByteLength(I[BII)I
 * </pre>
 *
 * @param handler handle returned from getHandler
 * @param b buffer of bytes to be converted
 * @param offset offset into the provided buffer
 * @param len length of data to be processed
 *
 * @return length of the converted string, or zero if the
 *         arguments were not valid
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_cldc_i18n_j2me_Conv_getByteLength() {
    int  length = KNI_GetParameterAsInt(4);
    int  offset = KNI_GetParameterAsInt(3);
    int      id = KNI_GetParameterAsInt(1);
    char*   buf;
    jint result = 0;

    KNI_StartHandles(1);
    KNI_DeclareHandle(b);
    KNI_GetParameterAsObject(2, b);

    buf = (char*)midpMalloc(length);

    if (buf == NULL) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {

        KNI_GetRawArrayRegion(b, offset, length, (jbyte*)buf);

        result = lcConv[id] 
               ? lcConv[id]->byteLen((const unsigned char *) buf, length) 
               : 0;
        midpFree(buf);
    }

    KNI_EndHandles();
    KNI_ReturnInt(result);
}

/**
 * Converts an array of bytes to converted array of characters.
 * <p>
 * Java declaration:
 * <pre>
 *     byteToChar(I[BII[CII)I
 * </pre>
 *
 * @param handler  handle returned from getHandler
 * @param input  buffer of bytes to be converted
 * @param in_offset  offset into the provided buffer
 * @param in_len  length of data to be processed
 * @param output  buffer of converted bytes
 * @param out_offset  offset into the provided output buffer
 * @param out_len  length of data processed
 *
 * @return length of the converted string, or zero if the
 *         arguments were not valid
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_cldc_i18n_j2me_Conv_byteToChar() {
    int   outLength = KNI_GetParameterAsInt(7);
    int   outOffset = KNI_GetParameterAsInt(6);
    int    inLength = KNI_GetParameterAsInt(4);
    int    inOffset = KNI_GetParameterAsInt(3);
    int          id = KNI_GetParameterAsInt(1);
    char*     inBuf;
    jchar* outBuf;
    jint     result = 0;

    KNI_StartHandles(2);
    KNI_DeclareHandle(output);
    KNI_DeclareHandle(input);

    KNI_GetParameterAsObject(5, output);
    KNI_GetParameterAsObject(2, input);

    inBuf  = (char*)midpMalloc(inLength);
    if (inBuf != NULL) {
	/* Instead of always multiplying the length by sizeof(jchar),
	 * we shift left by 1. This can be done because jchar has a
	 * size of 2 bytes.
	 */
	outBuf = (jchar*)midpMalloc(outLength<<1);
	if (outBuf != NULL) {
	    KNI_GetRawArrayRegion(input, inOffset, inLength, (jbyte*)inBuf);
	    result = (lcConv[id] ? 
		       lcConv[id]->nativeToUnicode((const unsigned char *)
						   inBuf,  inLength, 
						   outBuf, outLength): 0);
	    KNI_SetRawArrayRegion(output, outOffset<<1, outLength<<1, 
				  (jbyte*)outBuf);

	    midpFree(inBuf);
	    midpFree(outBuf);
	} else {
	    midpFree(inBuf);
	    KNI_ThrowNew(midpOutOfMemoryError, NULL);
	}
    } else {
	KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnInt(result);
}

/**
 * Converts an array of characters to converted array of bytes.
 * <p>
 * Java declaration:
 * <pre>
 *     charToByte(I[CII[BII)I
 * </pre>
 *
 * @param handler  handle returned from getHandler
 * @param input  buffer of bytes to be converted
 * @param in_offset  offset into the provided buffer
 * @param in_len  length of data to be processed
 * @param output  buffer of converted bytes
 * @param out_offset  offset into the provided output buffer
 * @param out_len  length of data processed
 *
 * @return length of the converted string, or zero if the 
 *         arguments were not valid
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_cldc_i18n_j2me_Conv_charToByte() {
    int   outLength = KNI_GetParameterAsInt(7);
    int   outOffset = KNI_GetParameterAsInt(6);
    int    inLength = KNI_GetParameterAsInt(4);
    int    inOffset = KNI_GetParameterAsInt(3);
    int          id = KNI_GetParameterAsInt(1);
    jchar*  inBuf;
    char*    outBuf;
    jint     result = 0;

    KNI_StartHandles(2);
    KNI_DeclareHandle(output);
    KNI_DeclareHandle(input);

    KNI_GetParameterAsObject(5, output);
    KNI_GetParameterAsObject(2, input);

    /* Instead of always multiplying the length by sizeof(jchar),
     * we shift left by 1. This can be done because jchar has a
     * size of 2 bytes.
     */
    inBuf  = (jchar*)midpMalloc(inLength<<1);
    if (inBuf != NULL) {
	outBuf = (char*)midpMalloc(outLength);
	if (outBuf != NULL) {
	    KNI_GetRawArrayRegion(input, inOffset<<1, inLength<<1, 
				  (jbyte*)inBuf);
	    result = (lcConv[id] ? 
			       lcConv[id]->unicodeToNative(inBuf, inLength,
   				        (unsigned char*) outBuf, outLength):0);
	    KNI_SetRawArrayRegion(output, outOffset, outLength,(jbyte*)outBuf);

	    midpFree(inBuf);
	    midpFree(outBuf);
	} else {
	    midpFree(inBuf);
	    KNI_ThrowNew(midpOutOfMemoryError, NULL);
	}
    } else {
	KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_EndHandles();
    KNI_ReturnInt(result);
}

/**
 * Gets the length of a specific converted string as an array of 
 * Unicode bytes.
 * <p>
 * Java declaration:
 * <pre>
 *     sizeOfByteInUnicode(I[BII)I
 * </pre>
 *
 * @param handler  handle returned from getHandler
 * @param b  buffer of bytes to be converted
 * @param offset  offset into the provided buffer
 * @param length  length of data to be processed
 *
 * @return length of the converted string, or zero if the
 *         arguments were not valid
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_cldc_i18n_j2me_Conv_sizeOfByteInUnicode() {
    int   length = KNI_GetParameterAsInt(4);
    int   offset = KNI_GetParameterAsInt(3);
    int       id = KNI_GetParameterAsInt(1);
    char    *buf;
    jint  result = 0;

    KNI_StartHandles(1);
    KNI_DeclareHandle(b);

    KNI_GetParameterAsObject(2, b);
    buf = (char*)midpMalloc(length);

    if (buf == NULL) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        KNI_GetRawArrayRegion(b, offset, length, (jbyte*)buf);

        result = lcConv[id] 
               ? lcConv[id]->sizeOfByteInUnicode((const unsigned char *)buf, 
                                                  offset, length) 
               : 0;

        midpFree(buf);
    }

    KNI_EndHandles();
    KNI_ReturnInt(result);
}

/**
 * Gets the length of a specific converted string as an array of 
 * Unicode characters.
 * <p>
 * Java declaration:
 * <pre>
 *     sizeOfUnicodeInByte(I[CII)I
 * </pre>
 *
 * @param handler  handle returned from getHandler
 * @param c  buffer of characters to be converted
 * @param offset  offset into the provided buffer
 * @param length  length of data to be processed
 *
 * @return length of the converted string, or zero if the
 *         arguments were not valid
 */
KNIEXPORT KNI_RETURNTYPE_INT
Java_com_sun_cldc_i18n_j2me_Conv_sizeOfUnicodeInByte() {
    int   length = KNI_GetParameterAsInt(4);
    int   offset = KNI_GetParameterAsInt(3);
    int       id = KNI_GetParameterAsInt(1);
    jchar *buf;
    jint  result = 0;

    KNI_StartHandles(1);
    KNI_DeclareHandle(c);

    KNI_GetParameterAsObject(2, c);

    /* Instead of always multiplying the length by sizeof(jchar),
     * we shift left by 1. This can be done because jchar has a
     * size of 2 bytes.
     */
    buf = (jchar*)midpMalloc(length<<1);

    if (buf == NULL) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        KNI_GetRawArrayRegion(c, offset<<1, length<<1, (jbyte*)buf);

        result = lcConv[id] 
               ? lcConv[id]->sizeOfUnicodeInByte((const jchar *)buf, 
                                                  offset, length) 
               : 0;
        midpFree(buf);
    }

    KNI_EndHandles();
    KNI_ReturnInt(result);
}

LcConvMethods
getLcConvMethods(char *encoding)
{
    int index = getLcConvMethodsIDByEncoding(encoding);

    if (index != -1) {
        return lcConv[index];
    }

    return NULL;
}
