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

package com.sun.midp.rms;

import com.sun.midp.configurator.Constants;
import com.sun.midp.i3test.*;
import com.sun.midp.midlet.MIDletSuite;
import java.io.IOException;

/**
 * Test the global file resource limit by opening files up to and then past
 * the limit.  To open files, this test uses the RecordStoreFile class, which
 * is a Java-based interface that's reasonably close to direct file access.
 * RecordStoreFile is a private class inside com.sun.midp.rms, so that's why
 * this test resides in that package.
 */
public class TestFileRscLimit extends TestCase {

    /** Shorthand for the global file limit. */
    static final int LIMIT = Constants.FILE_AMS_LIMIT;

    /** The suite name used when creating files. */
    static final int SUITE = MIDletSuite.INTERNAL_SUITE_ID;

    /** The file extension used when creating files. */
    static final int EXT = RecordStoreFile.DB_EXTENSION;

    /** An array of open RecordStoreFile objects. */
    RecordStoreFile rsf[];

    /**
     * Makes a filename.
     *
     * @param i a small integer used in creating the filename
     * @return the filename just created
     */
    String makeName(int i) {
        return "TestFileRscLimit" + i;
    }

    /**
     * Creates rsf, an array of LIMIT+1 RecordStoreFile objects and opens
     * files up to LIMIT, leaving one slot unused.
     */
    public void setUp() throws IOException {
        rsf = new RecordStoreFile[LIMIT + 1];
        for (int i = 0; i < LIMIT; i++) {
            rsf[i] = new RecordStoreFile(SUITE, makeName(i), EXT);
        }
    }

    /**
     * Closes and deletes any RecordStoreFile objects found in the rsf array.
     * Ignores any errors that might occur.
     */
    public void tearDown() {
        for (int i = 0; i < rsf.length; i++) {
            if (rsf[i] != null) {
                try {
                    rsf[i].close();
                } catch (IOException ioe) { }
                RecordStoreUtil.quietDeleteFile(SUITE, makeName(i), EXT);
            }
        }
    }

    /**
     * Opens one more file than the global file resource limit and checks to
     * see that the last one throws an exception.
     */
    void testFileHandlesRscLimit() throws IOException {
        try {
            boolean exceptionThrown = false;

            setUp();
            try {
                // The following line should hit the resource limit
                // and therefore throw IOException.
                rsf[LIMIT] = new RecordStoreFile(SUITE, makeName(LIMIT), EXT);
            } catch (IOException ioe) {
                exceptionThrown = true;
            }
            assertTrue(exceptionThrown);
        } finally {
            tearDown();
        }
    }

    /** Run all tests. */
    public void runTests() throws IOException {
        declare("testFileHandlesRscLimit");
        testFileHandlesRscLimit();
    }
}

