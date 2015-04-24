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
#include <commonKNIMacros.h>
#include <ROMStructs.h>

#include <midpMalloc.h>
#include <midpRMS.h>
#include <midpUtilKni.h>
#include <midpError.h>


/**
 * @file
 *
 * Implementation for RMS Java native methods.
 */

#define getMidpRecordStoreFilePtr(__handle) \
               (unhand(struct Java_com_sun_midp_rms_RecordStoreFile, __handle))

/**
 * Free all native resources used by this object.
 *
 * @param thisObject The <code>RecordStoreFile</code> Object to finalize.
 */
static void
storageCleanup(KNIDECLARGS jobject thisObject) {
    int   handle;

    KNI_StartHandles(1);
    KNI_DeclareHandle(clazz);

    KNI_GetObjectClass(thisObject, clazz);

    handle = getMidpRecordStoreFilePtr(thisObject)->handle;
    if (handle != -1) {
        char* pszError;

        recordStoreClose(&pszError, handle);
        if (pszError != NULL) {
            recordStoreFreeError(pszError);
        }
        getMidpRecordStoreFilePtr(thisObject)->handle = -1;
    }

    KNI_EndHandles();
}

/**
 * Get the number of record stores for a MIDlet suite.
 *
 * @param suiteId ID of the suite
 *
 * @return the number of installed suites
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_rms_RecordStoreFile_getNumberOfStores) {
    SuiteIdType suiteId = KNI_GetParameterAsInt(1);
    int numberOfStores = 0;

    numberOfStores = rmsdb_get_number_of_record_stores(suiteId);
    if (numberOfStores == OUT_OF_MEM_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_ReturnInt((jint)numberOfStores);
}

/**
 * Retrieves the list of record stores a MIDlet suites owns.
 *
 * @param suiteId ID of the suite
 * @param specifies an empty array of suite store names to fill, call
 *     getNumberOfSuites to know how big to make the array
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_getRecordStoreList) {
    SuiteIdType suiteId;
    int numberOfStrings;
    int numberOfStores;
    int i;
    pcsl_string* pStoreNames = NULL;

    KNI_StartHandles(2);
    KNI_DeclareHandle(names);
    KNI_DeclareHandle(tempStringObj);

    KNI_GetParameterAsObject(2, names);
    numberOfStrings = (int)KNI_GetArrayLength(names);

    suiteId = KNI_GetParameterAsInt(1);

    numberOfStores = rmsdb_get_record_store_list(suiteId,
                                                 /* OUT */ &pStoreNames);
    if (numberOfStores == OUT_OF_MEM_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else {
        do {
            if (numberOfStores == 0) {
                break;
            }

            if (numberOfStrings > numberOfStores) {
                numberOfStrings = numberOfStores;
            }

            for (i = 0; i < numberOfStrings; i++) {
                midp_jstring_from_pcsl_string(KNIPASSARGS &pStoreNames[i], tempStringObj);
                KNI_SetObjectArrayElement(names, (jint)i, tempStringObj);
            }
        } while (0);

        free_pcsl_string_list(pStoreNames, numberOfStores);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/**
 * Remove all the Record Stores for a suite.
 *
 * @param suiteId ID of the suite
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_removeRecordStores) {
    SuiteIdType suiteId = KNI_GetParameterAsInt(1);

    if (!rmsdb_remove_record_stores_for_suite(suiteId)) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_ReturnVoid();
}

/**
 * Returns true if the suite has created at least one record store.
 *
 * @param suiteId ID of the suite
 *
 * @return true if the suite has at least one record store
 */
KNIEXPORT KNI_RETURNTYPE_BOOLEAN
KNIDECL(com_sun_midp_rms_RecordStoreFactory_suiteHasRmsData) {
    jboolean exists = KNI_FALSE;
    int status;
    SuiteIdType suiteId = KNI_GetParameterAsInt(1);

    status = rmsdb_suite_has_rms_data(suiteId);
    if (status == OUT_OF_MEM_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    } else if (status > 0) {
        exists = KNI_TRUE;
    }

    KNI_ReturnBoolean(exists);
}

/**
 * Approximation of remaining space in storage for a new record store.
 *
 * Usage Warning:  This may be a slow operation if
 * the platform has to look at the size of each file
 * stored in the MIDP memory space and include its size
 * in the total.
 *
 * @param suiteId ID of the MIDlet suite that owns the record store
 *
 * @return the approximate space available to create a
 *         record store in bytes.
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_rms_RecordStoreFile_spaceAvailableNewRecordStore) {
    long available = 0;
    SuiteIdType suiteId = KNI_GetParameterAsInt(1);

    available = rmsdb_get_new_record_store_space_available(suiteId);
    if (available == OUT_OF_MEM_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_ReturnInt((jint)available);
}

/**
 * Open a native record store file.
 *
 * @param suiteId ID of the MIDlet suite that owns the record store
 * @param name name of the record store
 * @param extension extension number to add to the end of the file name
 *
 * @return handle to a record store file
 *
 * @exception IOException if there is an error opening the file.
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_rms_RecordStoreFile_openRecordStoreFile) {
    SuiteIdType suiteId = KNI_GetParameterAsInt(1);
    int extension = KNI_GetParameterAsInt(3);
    int handle = -1;
    char* pszError;

    KNI_StartHandles(1);
    GET_PARAMETER_AS_PCSL_STRING(2, name) {
        handle = rmsdb_record_store_open(&pszError, suiteId, &name, extension);

        if (pszError != NULL) {
            if (handle == -2) {
                KNI_ThrowNew(midpRecordStoreException, pszError);
            } else {
                KNI_ThrowNew(midpIOException, pszError);
            }
            recordStoreFreeError(pszError);
        } else if (handle == -1) {
            KNI_ThrowNew(midpIOException, "cannot get filename");
        }
    } RELEASE_PCSL_STRING_PARAMETER;

    KNI_EndHandles();
    KNI_ReturnInt((jint)handle);
}

/**
 * Find how more space is available for a particular record store.
 *
 * @param handle handle of an open record store
 * @param suiteId ID of the owning suite
 *
 * @return the approximate space available to grow the
 *         record store in bytes.
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_rms_RecordStoreFile_spaceAvailableRecordStore) {
    long available = 0;
    int handle = KNI_GetParameterAsInt(1);
    SuiteIdType suiteId = KNI_GetParameterAsInt(2);

    /* the implementation may ignore the suite id */
    available = rmsdb_get_record_store_space_available(handle, suiteId);
    if (available == OUT_OF_MEM_LEN) {
        KNI_ThrowNew(midpOutOfMemoryError, NULL);
    }

    KNI_ReturnInt((jint)available);
}

/**
 * Sets the position within <code>recordStream</code> to
 * <code>pos</code>.  This will implicitly grow
 * the underlying stream if <code>pos</code> is made greater
 * than the current length of the storage stream.
 *
 * @param handle handle to a record store file
 * @param pos position within the file to move the current_pos
 *        pointer to.
 *
 * @exception IOException if there is a problem with the seek.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_setPosition) {
    long  absolutePosition = (long)KNI_GetParameterAsInt(2);
    int   handle           = KNI_GetParameterAsInt(1);
    char* pszError;

    recordStoreSetPosition(&pszError, handle, absolutePosition);
    if (pszError != NULL) {
        KNI_ThrowNew(midpIOException, pszError);
        recordStoreFreeError(pszError);
    }

    KNI_ReturnVoid();
}

/**
 * Write <code>buf</code> to <code>recordStream</code>, starting
 * at <code>offset</code> and continuing for <code>numBytes</code>
 * bytes.
 *
 * @param handle handle to a record store file
 * @param buf buffer to read out of.
 * @param offset starting point write offset, from beginning of buffer.
 * @param numBytes the number of bytes to write.
 *
 * @exception IOException if a write error occurs.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_writeBytes) {
    int   length = KNI_GetParameterAsInt(4);
    int   offset = KNI_GetParameterAsInt(3);
    int   handle = KNI_GetParameterAsInt(1);
    char* pszError;

    KNI_StartHandles(1);
    KNI_DeclareHandle(buffer);

    KNI_GetParameterAsObject(2, buffer);

    recordStoreWrite(&pszError, handle,
                     (char*)&(JavaByteArray(buffer)[offset]), length);
    if (pszError != NULL) {
        KNI_ThrowNew(midpIOException, pszError);
        recordStoreFreeError(pszError);
    }

    KNI_EndHandles();
    KNI_ReturnVoid();
}

/**
 * Commit pending writes.
 *
 * @param handle
 *
 * @exception IOException if an error occurs while flushing
 *            <code>recordStream</code>.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_commitWrite) {
    int handle = KNI_GetParameterAsInt(1);
    char* pszError;

    recordStoreCommitWrite(&pszError, handle);
    if (pszError != NULL) {
        KNI_ThrowNew(midpIOException, pszError);
        recordStoreFreeError(pszError);
    }
    KNI_ReturnVoid();
}

/**
 * Read up to <code>buf.length</code> into <code>buf</code>
 * starting at offset <code>offset</code> in <code>recordStream
 * </code> and continuing for up to <code>numBytes</code> bytes.
 *
 * @param handle handle to a record store file
 * @param buf buffer to read in to.
 * @param offset starting point read offset, from beginning of buffer.
 * @param numBytes the number of bytes to read.
 *
 * @return the number of bytes read.
 *
 * @exception IOException if a read error occurs.
 */
KNIEXPORT KNI_RETURNTYPE_INT
KNIDECL(com_sun_midp_rms_RecordStoreFile_readBytes) {
    int   length = KNI_GetParameterAsInt(4);
    int   offset = KNI_GetParameterAsInt(3);
    int   handle = KNI_GetParameterAsInt(1);
    int   bytesRead;
    char* pszError;

    KNI_StartHandles(1);
    KNI_DeclareHandle(buffer);

    KNI_GetParameterAsObject(2, buffer);
    bytesRead = recordStoreRead(&pszError, handle,
                     (char*)&(JavaByteArray(buffer)[offset]), length);
    KNI_EndHandles();

    if (pszError != NULL) {
        KNI_ThrowNew(midpIOException, pszError);
        recordStoreFreeError(pszError);
    }

    KNI_ReturnInt((jint)bytesRead);
}

/**
 * Disconnect from <code>recordStream</code> if it is
 * non null.  May be called more than once without error.
 *
 * @param handle
 *
 * @exception IOException if an error occurs closing
 *            <code>recordStream</code>.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_closeFile) {
    int handle = KNI_GetParameterAsInt(1);
    char* pszError;

    recordStoreClose(&pszError, handle);
    if (pszError != NULL) {
        KNI_ThrowNew(midpIOException, pszError);
        recordStoreFreeError(pszError);
    }
    KNI_ReturnVoid();
}

/**
 * Set the length of the specified <code>RecordStoreFile</code>
 * to <code>size</code> bytes.  If this file was previously
 * larger than <code>size</code>, the extra data is lost.
 *
 * <code>size</code> must be not greater than the current length of
 * <code>recordStream</code>
 *
 * @param handle handle to a record store file
 * @param size new size for this file
 *
 * @exception IOException if an error occurs, or if
 * <code>size</code> is less than zero.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_truncateFile) {
    int   size   = KNI_GetParameterAsInt(2);
    int   handle = KNI_GetParameterAsInt(1);
    char* pszError;

    recordStoreTruncate(&pszError, handle, size);
    if (pszError != NULL) {
        KNI_ThrowNew(midpIOException, pszError);
        recordStoreFreeError(pszError);
    }
    KNI_ReturnVoid();
}

/**
 * Native finalizer to free all native resources used by the
 * object.
 *
 * @param this The <code>RecordStoreFile</code> Object to be finalized.
 */
KNIEXPORT KNI_RETURNTYPE_VOID
KNIDECL(com_sun_midp_rms_RecordStoreFile_finalize) {
    KNI_StartHandles(1);
    KNI_DeclareHandle(instance);
    KNI_GetThisPointer(instance);

    storageCleanup(KNIPASSARGS instance);

    KNI_EndHandles();

    KNI_ReturnVoid();
}
