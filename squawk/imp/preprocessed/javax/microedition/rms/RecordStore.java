/*
 * Copyright 2000-2010 Sun Microsystems, Inc. All Rights Reserved.
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

package javax.microedition.rms;

import java.util.Vector;

import com.sun.squawk.imp.ImpGlobal;
import com.sun.squawk.rms.*;

/**
 * A class representing a record store. A record store consists of a
 * collection of records which will remain persistent across multiple
 * invocations of the MIDlet. The platform is responsible for
 * making its best effort to maintain the integrity of the
 * MIDlet's record stores throughout the normal use of the
 * platform, including reboots, battery changes, etc.
 *
 * <p>Record stores are created in platform-dependent locations,
 * which are not exposed to the MIDlets. The naming space for
 * record stores is controlled at the MIDlet suite granularity.
 * MIDlets within a MIDlet suite are allowed to create multiple
 * record stores, as long as they are each given different names.
 * When a MIDlet suite is removed from a platform all the record
 * stores associated with its MIDlets will also be removed. These
 * APIs only allow the manipulation of the MIDlet suite's own
 * record stores, and does not provide any mechanism for record
 * sharing between MIDlets in different MIDlet suites. MIDlets
 * within a MIDlet suite can access each other's record stores
 * directly.
 *
 * <p>Record store names are case sensitive and may consist of
 * any combination of up to 32 Unicode characters.
 * Record store names must be unique within the scope of a given
 * MIDlet suite. In other words, a MIDlets within a MIDlet suite
 * are is not allowed to create more than one record store with
 * the same name, however a MIDlet in different one MIDlet suites
 * are is allowed to each have a record store with the same name
 * as a MIDlet in another MIDlet suite. In that case, the record
 * stores are still distinct and separate.
 *
 * <p>No locking operations are provided in this API. Record store
 * implementations ensure that
 * all individual record store operations are atomic, synchronous,
 * and serialized, so no corruption will occur with multiple accesses.
 * However, if a MIDlet uses multiple threads
 * to access a record store, it is the MIDlet's responsibility to
 * coordinate this access or unintended consequences may result.
 * Similarly, if a platform performs transparent synchronization of
 * a record store, it is the platform's responsibility to enforce
 * exclusive access to the record store between the MIDlet and
 * synchronization engine.
 *
 * <p>Records are uniquely identified within a given record store by their
 * recordId, which is an integer value. This recordId is used as the
 * primary key for the records. The first record created in a record
 * store will have recordId equal to one (1). Each subsequent
 * record added to a RecordStore will be assigned a recordId one greater
 * than the record added before it. That is, if two records are added
 * to a record store, and the first has a recordId of 'n', the next will
 * have a recordId of 'n + 1'. MIDlets can create other indices
 * by using the <code>RecordEnumeration</code> class.
 *
 * <p>This record store uses long integers for time/date stamps, in
 * the format used by System.currentTimeMillis(). The record store
 * is time stamped with the last time it was
 * modified. The record store also maintains a <em>version</em>, which
 * is an integer that is incremented for each operation that modifies
 * the contents of the RecordStore.   These are useful
 * for synchronization engines as well as other things.
 *
 * @version MIDP 1.0
 */
public class RecordStore {

    /** number of open instances of this record store */
    private int opencount;

    /** Most recent RecordStoreEntry representing this record store */
    private IRecordStoreEntry recordStoreEntry;

    /** Name of the record store, we keep this around in order to be able to do
     * a lookup if we are ever Isolate migrated to another location
     */
    private final String recordStoreName;

    /** recordListeners of this record store */
    private final java.util.Vector recordListener;

    /*
     * This implementation assumes (and enforces) that there is only
     * one instance of a RecordStore object for any given database file
     * in the file system. This assumption is enforceable when there
     * is only one VM running, but will lead to data corruption problems
     * if multiple VM's are used to read/write into the same database.
     */

    /*
     * RecordStore Constructors
     */

    /**
     * Apps must use <code>openRecordStore()</code> to get
     * a <code>RecordStore</code> object. This constructor
     * is used internally for creating RecordStore objects.
     *
     * <code>dbCache</code> must be held/synchronized before calling
     * this constructor.
     *
     * @param recordStoreName a string to name the record store.
     * @param create if true, create the record store if it doesn't exist.
     *
     * @exception RecordStoreException if something goes wrong setting up
     *            the new RecordStore.
     * @exception RecordStoreNotFoundException if can't find the record store
     *            and create is set to false.
     */
    private RecordStore(String recordStoreName, boolean create) throws RecordStoreException, RecordStoreNotFoundException {
        this.recordStoreName = recordStoreName;
        recordListener = new java.util.Vector(3);
        recordStoreEntry = ImpGlobal.getRecordStoreManager().getRecordStore(recordStoreName, create);
        if (recordStoreEntry == null) {
            throw new RecordStoreNotFoundException(recordStoreName);
        }
    }

    /*
     * Public Static Methods
     */
    
    /**
     * Open (and possibly create) a record store associated with the
     * given MIDlet suite. If this method is called by a MIDlet when
     * the record store is already open by a MIDlet in the MIDlet suite,
     * this method returns a reference to the same RecordStore object.
     *
     * @param recordStoreName the MIDlet suite unique name, not to exceed
     *        32 characters, of the record store.
     * @param createIfNecessary if true, the record store will be created
     *        if necessary.
     *
     * @return the RecordStore object for this record store.
     *
     * @exception RecordStoreException if a record store related exception
     *            occurred.
     * @exception RecordStoreNotFoundException if the record store could
     *            not be found.
     * @exception RecordStoreFullException if the operation cannot be
     *            completed because the record store is full.
     */
    public static RecordStore openRecordStore(String recordStoreName,
            boolean createIfNecessary)
            throws RecordStoreException, RecordStoreFullException,
                   RecordStoreNotFoundException {
        Vector dbCache = ImpGlobal.getRecordStoreDbCache();
        synchronized (dbCache) {

            if (recordStoreName.length() > 32) {
                throw new RecordStoreException("record store name too long");
            }

            // Cache record store objects and ensure that there is only
            // one record store object in memory for any given record
            // store file. This is good for memory use. This is NOT safe
            // in the situation where multiple VM's may be executing code
            // concurrently. In that case, you have to sync things through
            // file locking or something similar.

            // Check the record store cache for a db with the same name
            RecordStore db;
            for (int n = 0; n < dbCache.size(); n++) {
                db = (RecordStore) dbCache.elementAt(n);
                if (db.getName().equals(recordStoreName)) {
                    db.opencount++;  // times rs has been opened
                    return db;  // return ref to cached record store
                }
            }

            /*
             * Record store not found in cache, create it.
             */
            db = new RecordStore(recordStoreName, createIfNecessary);
            /*
             * Now add the new record store to the cache
             */
            db.opencount = 1;
            dbCache.addElement(db);
            return db;
        }
    }

    /**
     * Returns an array of the names of record stores owned by the
     * MIDlet suite. Note that if the MIDlet suite does not
     * have any record stores, this function will return NULL.
     *
     * The order of RecordStore names returned is implementation
     * dependent.
     *
     * @return an array of the names of record stores owned by the
     *         MIDlet suite. Note that if the MIDlet suite does not
     *         have any record stores, this function will return NULL.
     */
    public static String[] listRecordStores() {
        try {
            return ImpGlobal.getRecordStoreManager().getRecordStoreNames();
        } catch (RecordStoreException e) {
            return null;
        }
    }

    /**
     * Deletes the named record store. MIDlet suites are only allowed to
     * operate on their own record stores, including deletions. If the
     * record store is currently open by a MIDlet when this method
     * is called, or if the named record store does not exist, a
     * RecordStoreException will be thrown.
     *
     * @param recordStoreName the MIDlet suite unique record store to delete.
     *
     * @exception RecordStoreException if a record store-related exception
     *            occurred.
     * @exception RecordStoreNotFoundException if the record store could
     *            not be found.
     */
    public static void deleteRecordStore(String recordStoreName) throws RecordStoreException, RecordStoreNotFoundException {
        // Check the record store cache for a db with the same name
        Vector dbCache = ImpGlobal.getRecordStoreDbCache();
        synchronized (dbCache) {
            RecordStore db;
            for (int n = 0; n < dbCache.size(); n++) {
                db = (RecordStore) dbCache.elementAt(n);
                if (db.getName().equals(recordStoreName)) {
                    // cannot delete an open record store
                    throw new RecordStoreException("deleteRecordStore error: record store is still open");
                }
            }
            boolean found = ImpGlobal.getRecordStoreManager().deleteRecordStore(recordStoreName);
            if (!found) {
                throw new RecordStoreNotFoundException("deleteRecordStore error: file not found");
            }
        }
    }

    /*
     * Public RecordStore update operations
     */
    
    /**
     * Adds a new record to the record store. The recordId for this
     * new record is returned. This is a blocking atomic operation.
     * The record is written to persistent storage before the
     * method returns.
     *
     * @param data the data to be stored in this record. If the record
     *        is to have zero-length data (no data), this parameter
     *        may be null.
     * @param offset the index into the data buffer of the first relevant
     *        byte for this record.
     * @param numBytes the number of bytes of the data buffer to use for
     *        this record (may be zero).
     *
     * @return the recordId for the new record.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception RecordStoreException if a different record store-related
     *            exception occurred.
     * @exception RecordStoreFullException if the operation cannot be
     *            completed because the record store has no more room.
     * @exception NullPointerException if <code>data</code> is null but
     *            <code>numBytes</code> is greater than zero.
     */
    public synchronized int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreNotOpenException, RecordStoreException, RecordStoreFullException {
        checkOpen();
        if ((data == null) && (numBytes > 0)) {
            throw new NullPointerException("illegal arguments: null "
                    + "data,  numBytes > 0");
        }
        int recordId = recordStoreEntry.addRecord(data, offset, numBytes);
        // tell listeners a record has been added
        notifyRecordAddedListeners(recordId);

        // Return the new record id
        return recordId;
    }

    /**
     * The record is deleted from the record store. The recordId for this
     * record is NOT reused.
     *
     * @param recordId the ID of the record to delete.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception InvalidRecordIDException if the recordId is invalid.
     * @exception RecordStoreException if a general record store exception
     *            occurs.
     */
    public synchronized void deleteRecord(int recordId)
            throws RecordStoreNotOpenException, InvalidRecordIDException,
                   RecordStoreException {
        checkOpen();
        recordStoreEntry.deleteRecord(recordId);
        // tell listeners a record has been deleted
        notifyRecordDeletedListeners(recordId);
    }

    /**
     * This method is called when the MIDlet requests to have the
     * record store closed. Note that the record store will not
     * actually be closed until closeRecordStore() is called as many
     * times as openRecordStore() was called. In other words, the
     * MIDlet needs to make a balanced number of close calls as open
     * calls before the record store is closed.<p>
     *
     * When the record store is closed, all listeners are removed.
     * If the MIDlet attempts to perform operations on the
     * RecordStore object after it has been closed, the methods will
     * throw a RecordStoreNotOpenException.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception RecordStoreException if a different record store-related
     *            exception occurred.
     */
    public synchronized void closeRecordStore()
            throws RecordStoreNotOpenException, RecordStoreException {
        Vector dbCache = ImpGlobal.getRecordStoreDbCache();
        synchronized (dbCache) {
            checkOpen();
            opencount--;
            if (opencount <= 0) {  // free stuff - final close
                dbCache.removeElement(this);
                // closing now...no need to listen
                if (!recordListener.isEmpty()) {
                    recordListener.removeAllElements();
                }
                recordStoreEntry = null;
            }
        }
    }

    /**
     * Returns the size (in bytes) of the MIDlet data available
     * in the given record.
     *
     * @param recordId the id of the record to use in this operation.
     *
     * @return the size (in bytes) of the MIDlet data available
     *         in the given record.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception InvalidRecordIDException if the recordId is invalid.
     * @exception RecordStoreException if a general record store exception
     *            occurs.
     */
    public int getRecordSize(int recordId)
            throws RecordStoreNotOpenException, InvalidRecordIDException,
                   RecordStoreException {
        checkOpen();
        return recordStoreEntry.getRecordSize(recordId);
    }

    /**
     * Returns the data stored in the given record.
     *
     * @param recordId the id of the record to use in this operation.
     * @param buffer the byte array in which to copy the data.
     * @param offset the index into the buffer in which to start copying.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception InvalidRecordIDException if the recordId is invalid.
     * @exception RecordStoreException if a general record store exception
     *            occurs.
     * @return the number of bytes copied into the buffer, starting at
     *         index <code>offset</code>.
     *
     * @see #setRecord
     */
    public synchronized int getRecord(int recordId, byte[] buffer, int offset)
            throws RecordStoreNotOpenException, InvalidRecordIDException,
                   RecordStoreException {
        checkOpen();
        return recordStoreEntry.getRecord(recordId, buffer, offset);
    }

    /**
     * Returns a copy of the data stored in the given record.
     *
     * @param recordId the id of the record to use in this operation.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception InvalidRecordIDException if the recordId is invalid.
     * @exception RecordStoreException if a general record store exception
     *            occurs.
     *
     * @return the data stored in the given record. Note that if the record
     *         has no data, this method will return null.
     *
     * @see #setRecord
     */
    public synchronized byte[] getRecord(int recordId)
            throws RecordStoreNotOpenException, InvalidRecordIDException,
                   RecordStoreException {
        checkOpen();
        byte[] data = recordStoreEntry.getRecord(recordId);
        return data;
    }

    /**
     * Sets the data in the given record to that passed in. After
     * this method returns, a call to <code>getRecord(int recordId)</code>
     * will return an array of numBytes size containing the data supplied here.
     *
     * @param recordId the id of the record to use in this operation.
     * @param newData the new data to store in the record.
     * @param offset the index into the data buffer of the first relevant
     *        byte for this record.
     * @param numBytes the number of bytes of the data buffer to use for
     *        this record.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception InvalidRecordIDException if the recordId is invalid.
     * @exception RecordStoreException if a general record store exception
     *            occurs.
     * @exception RecordStoreFullException if the operation cannot be
     *            completed because the record store has no more room.
     *
     * @see #getRecord
     */
    public synchronized void setRecord(int recordId, byte[] newData,
            int offset, int numBytes)
            throws RecordStoreNotOpenException, InvalidRecordIDException,
                   RecordStoreException, RecordStoreFullException {
        checkOpen();

        if ((newData == null) && (numBytes > 0)) {
            throw new NullPointerException();
        }
        recordStoreEntry.setRecord(recordId, newData, offset, numBytes);
        notifyRecordChangedListeners(recordId);
    }

    /*
     * Public RecordStore accessor methods
     */

    /**
     * Returns the name of this RecordStore. Not synchronized as
     * the name is immutable.
     *
     * @return the name of this RecordStore.
     *
     * @exception RecordStoreNotOpenException if the record store is closed
     */
    public String getName() throws RecordStoreNotOpenException {
        checkOpen();
        return recordStoreName;
    }

    /**
     * Each time a record store is modified (record added, modified, deleted),
     * it's <em>version</em> is incremented. This can be used by MIDlets to
     * quickly tell if anything has been modified.
     *
     * The initial version number is implementation dependent.
     * The increment is a positive integer greater than 0.
     * The version number only increases as the RecordStore is updated.
     *
     * @return the current record store version.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     */
    public int getVersion()
            throws RecordStoreNotOpenException {
        checkOpen();
        return recordStoreEntry.getVersion();
    }

    /**
     * Returns the number of records currently in the record store.
     *
     * @return the number of records currently in the record store.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     */
    public int getNumRecords()
            throws RecordStoreNotOpenException {
        checkOpen();
        return recordStoreEntry.getNumRecords();
    }

    /**
     * Returns the amount of space, in bytes, that the record store
     * occupies. The size returned includes any overhead associated
     * with the implementation, such as the data structures
     * used to hold the state of the record store, etc.
     *
     * @exception RecordStoreNotOpenException if the record store is
     *            not open.
     *
     * @return the size of the record store in bytes.
     */
    public int getSize()
            throws RecordStoreNotOpenException {
        checkOpen();
        try {
            return recordStoreEntry.getRecordsSize();
        } catch (RecordStoreException e) {
            throw new RecordStoreNotOpenException(e.getMessage());
        }
    }

    /**
     * Returns the amount of additional room (in bytes) available for
     * this record store to grow. Note that this is not necessarily
     * the amount of extra MIDlet-level data which can be stored,
     * as implementations may store additional data structures with
     * each record to support integration with native applications,
     * synchronization, etc.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     *
     * @return the amount of additional room (in bytes) available for
     *         this record store to grow.
     */
    public int getSizeAvailable() throws RecordStoreNotOpenException {
        checkOpen();
        try {
            return ImpGlobal.getRecordStoreManager().getSizeAvailable();
        } catch (RecordStoreException e) {
            throw new RecordStoreNotOpenException("System error: " + e.getMessage());
        }
    }

    /**
     * Returns the last time the record store was modified, in the format
     * used by System.currentTimeMillis().
     *
     * @return the last time the record store was modified, in the format
     *         used by <code>System.currentTimeMillis()</code>.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     */
    public long getLastModified()
            throws RecordStoreNotOpenException {
        checkOpen();
        return recordStoreEntry.getTimestamp();
    }

    /**
     * Adds the specified RecordListener. If the specified listener
     * is already registered, it will not be added a second time.
     * When a record store is closed, all listeners are removed.
     *
     * @param listener the RecordChangedListener to add.
     *
     * @see #removeRecordListener
     */
    public synchronized void addRecordListener(RecordListener listener) {
        if (!recordListener.contains(listener)) {
            recordListener.addElement(listener);
        }
    }

    /**
     * Removes the specified RecordListener. If the specified listener
     * is not registered, this method does nothing.
     *
     * @param listener the RecordChangedListener to remove.
     *
     * @see #addRecordListener
     */
    public synchronized void removeRecordListener(RecordListener listener) {
        recordListener.removeElement(listener);
    }

    /**
     * Returns the recordId of the next record to be added to the
     * record store. This can be useful for setting up pseudo-relational
     * relationships. That is, if you have two or more
     * record stores whose records need to refer to one another, you can
     * predetermine the recordIds of the records that will be created
     * in one record store, before populating the fields and allocating
     * the record in another record store. Note that the recordId returned
     * is only valid while the record store remains open and until a call
     * to <code>addRecord()</code>.
     *
     * @return the record id of the next record to be added to the
     *         record store.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     * @exception RecordStoreException if a different record store-related
     *            exception occurred.
     */
    public int getNextRecordID()
            throws RecordStoreNotOpenException, RecordStoreException {
        checkOpen();
        return recordStoreEntry.getNextRecordId();
    }

    /**
     * Returns an enumeration for traversing a set of records in the
     * record store in an optionally specified order.<p>
     *
     * The filter, if non-null, will be used to determine what
     * subset of the record store records will be used.<p>
     *
     * The comparator, if non-null, will be used to determine the
     * order in which the records are returned.<p>
     *
     * If both the filter and comparator are null, the enumeration
     * will traverse all records in the record store in an undefined
     * order. This is the most efficient way to traverse all of the
     * records in a record store.<p>
     *
     * The first call to <code>RecordEnumeration.nextRecord()</code>
     * returns the record data from the first record in the sequence.
     * Subsequent calls to <code>RecordEnumeration.nextRecord()</code>
     * return the next consecutive record's data. To return the record
     * data from the previous consecutive from any
     * given point in the enumeration, call <code>previousRecord()</code>.
     * On the other hand, if after creation the first call is to
     * <code>previousRecord()</code>, the record data of the last element
     * of the enumeration will be returned. Each subsequent call to
     * <code>previousRecord()</code> will step backwards through the
     * sequence.
     *
     * @param filter if non-null, will be used to determine what
     *        subset of the record store records will be used.
     * @param comparator if non-null, will be used to determine the
     *        order in which the records are returned.
     * @param keepUpdated if true, the enumerator will keep its enumeration
     *        current with any changes in the records of the record store.
     *        Use with caution as there are possible performance
     *        consequences. If false the enumeration will not be kept
     *        current and may return recordIds for records that have been
     *        deleted or miss records that are added later. It may also
     *        return records out of order that have been  modified after the
     *        enumeration was built. Note that any changes to records in the
     *        record store are accurately reflected when the record is later
     *        retrieved, either directly or through the enumeration. The
     *        thing that is risked by setting this parameter false is the
     *        filtering and sorting order of the enumeration when records
     *        are modified, added, or deleted.
     *
     * @exception RecordStoreNotOpenException if the record store is not open.
     *
     * @see RecordEnumeration#rebuild
     *
     * @return an enumeration for traversing a set of records in the
     *         record store in an optionally specified order.
     */
    public RecordEnumeration enumerateRecords(RecordFilter filter,
            RecordComparator comparator,
            boolean keepUpdated)
            throws RecordStoreNotOpenException {
        checkOpen();
        return new RecordEnumerationImpl(this, filter,
                comparator, keepUpdated);
    }


    /*
     * Private Utility Methods
     */
    
    /**
     * Throws a RecordStoreNotOpenException if the RecordStore
     * is closed.  (A RecordStore is closed if the RandomAccessFile
     * instance variable <code>dbraf</code> is null.
     *
     * @exception RecordStoreNotOpenException if RecordStore is closed
     */
    private void checkOpen() throws RecordStoreNotOpenException {
        if (recordStoreEntry == null) {
            throw new RecordStoreNotOpenException();
        }
    }

    /**
     * Notifies all registered listeners that a record changed.
     *
     * @param recordId the record id of the changed record.
     */
    private void notifyRecordChangedListeners(int recordId) {
        for (int i = 0; i < recordListener.size(); i++) {
            RecordListener rl = (RecordListener) recordListener.elementAt(i);
            rl.recordChanged(this, recordId);
        }
    }

    /**
     * Notifies all registered listeners that a record was added.
     *
     * @param recordId the record id of the added record.
     */
    private void notifyRecordAddedListeners(int recordId) {
        for (int i = 0; i < recordListener.size(); i++) {
            RecordListener rl = (RecordListener) recordListener.elementAt(i);
            rl.recordAdded(this, recordId);
        }
    }

    /**
     * Notifies all registered listeners that a record was deleted.
     *
     * @param recordId the record id of the changed record.
     */
    private void notifyRecordDeletedListeners(int recordId) {
        for (int i = 0; i < recordListener.size(); i++) {
            RecordListener rl = (RecordListener) recordListener.elementAt(i);
            rl.recordDeleted(this, recordId);
        }
    }

    /**
     * Returns all of the recordId's currently in the record store.
     *
     * MUST be called after obtaining rsLock, e.g in a
     * <code>synchronized (rsLock) {</code> block.
     *
     * @return an array of the recordId's currently in the record store
     *         or null if the record store is closed.
     */
    int[] getRecordIDs() {
        return recordStoreEntry.getRecordIdsCopy();
    }
}
