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

#include <kni.h>

#include <midpMalloc.h>
#include <localeMethod.h>
#include <localeInfo.h>
#include <conv.h>
#include "conv_intern.h"


/**
 * A locale methods definition.
 */
typedef struct _LcMethodsRec {
    char               *locale;
    char               *encoding;
    LcConvMethods       conv;
} LcMethodsRec, *LcMethods;

static LcMethods lc = NULL;


/**
 * Initializes the locale system.
 */
void 
initLocaleMethod() {
    char* locale = NULL;
    char* encoding = NULL;
    LcConvMethods conv = NULL;
    static int isCalled = 0;

    /* return if it's already called */
    if (isCalled) {
        return;
    }
    /* set isCalled to avoid another call */
    isCalled = 1;

    /* get the current system locale info that midp supports */
    getLocaleInfo(&locale, &encoding);
    /* both locale and encoding have to be returned */
    if (!locale || !encoding) {
        return;
    }

    /* find a converter for the encoding */
    conv = getLcConvMethods(encoding);
    if (!conv) {
        return;
    }

    lc = (LcMethods) midpMalloc(sizeof(LcMethodsRec));
    if (lc == NULL) {
        return;
    }

    lc->locale   = locale;
    lc->encoding = encoding;
    lc->conv     = conv;
}

/**
 * Finalizes the locale system.
 */
void 
finalizeLocaleMethod() {
    if (lc != NULL) {
	midpFree(lc->locale);                /* Allocated in getLocaleInfo() */
        midpFree(lc);
        lc = NULL;
    }
}

/**
 * Determines if the locale is supported
 *
 * @return <tt>1</tt> if the locale support is available,
 *         otherwise <tt>0</tt>
 */
int
isLocaleSupported() {
    return (lc) ? 1 : 0;
}

/**
 * Gets the locale encoding name.
 *
 * @return a 'C' string with the encoding name
 */
char*
getLocaleEncoding() {
    if (lc) {
        return lc->encoding;
    }
    return "ISO8859_1";
}

/*
 * Gets the locale name.
 *
 * @return a 'C' string with the locale name
 */
char*
getLocaleName() {
    if (lc) {
        return lc->locale;
    }
    /* The MIDP spec says that the locale name is NULL by default. */
    return NULL;
}


/*
 * Converts the Unicode string into the current locale native string.
 */
int 
unicodeToNative(const jchar *ustr, int ulen, unsigned char *bstr, int blen) {
    if (lc && lc->conv) {
        return lc->conv->unicodeToNative(ustr, ulen, bstr, blen);
    } else {
        ulen = (blen < ulen) ? blen : ulen;
        blen = ulen;
        while (--blen >= 0) {
            bstr[blen] = (char) ustr[blen];
        }
        return ulen;
    }
}

/*
 * Converts the current locale native string into the Unicode string.
 */
int 
nativeToUnicode(const unsigned char *bstr, int blen, jchar *ustr, int ulen) {
    if (lc && lc->conv) {
        return lc->conv->nativeToUnicode(bstr, blen, ustr, ulen);
    } else {
        blen = (ulen < blen) ? ulen : blen;
        ulen = blen;
        while (--ulen >= 0) {
            ustr[ulen] = (jchar) bstr[ulen];
        }
        return blen;
    }
}
