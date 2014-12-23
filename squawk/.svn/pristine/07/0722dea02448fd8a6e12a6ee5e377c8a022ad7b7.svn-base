/*
 * Copyright 1999-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.platform;

import java.io.IOException;

/**
 * Interface for native peer of a file handle
 */
public interface GCFFile {

    /**
     * Connect file handler to the abstract file target. This operation should
     * not trigger any access to the native filesystem.
     *
     * @param rootName The name of the root directory.
     *
     * @param fileName Full path to the file to be handled by this handler.
     *
     * @throws IllegalArgumentException if filename contains characters
     * not allowed by the file system. This check should not involve
     * any actual access to the filesystem.
     */
    public void connect(String rootName, String fileName);

//    /**
//     * Creates dedicated private working directory for the MIDlet suite.
//     * Does nothing if specified root is not private root or the directory
//     * already exists.
//     *
//     * @param rootName the name of file root
//     */
//    public void createPrivateDir(String rootName) throws IOException;

    /**
     * Open the file for reading, on the underlying file system. File name is
     * passed in the link#connect() method.
     *
     * @throws IOException if file is directory, file does not exists,
     *                     if file is already open for read or other
     *                     I/O error occurs
     */
    public void openForRead() throws IOException;

    /**
     * Closes for reading the file that was open by openForRead method.
     * If the file is already closed for reading this method does nothing.
     *
     * @throws IOException if file is directory, file does not exists or other
     *                     I/O error occurs
     */
    public void closeForRead() throws IOException;

    /**
     * Open the file for writing, on the underlying file system. File name is
     * passed in the link#connect() method.
     *
     * @throws IOException if file is directory, file does not exists,
     * i                   if file is already open for write or other
     *                     I/O error occurs
     */
    public void openForWrite() throws IOException;

    /**
     * Closes for writing the file that was open by openForWrite method.
     * If the file is already closed for writing this method does nothing.
     *
     * @throws IOException if file is directory, file does not exists or other
     *                     I/O error occurs
     */
    public void closeForWrite() throws IOException;

    /**
     * Closes the file for both reading and writing.
     * If the file is already closed for reading and writing this method does 
     * nothing.
     *
     * @throws IOException if file is directory, file does not exists or other
     *                     I/O error occurs
     */    
    public void closeForReadWrite() throws IOException;

//    /**
//     * Gets a filtered list of files and directories contained in a directory.
//     * The directory is the handler's target as specified in
//     * <code>create()</code>.
//     *
//     * @param   filter String against which all files and directories are
//     *          matched for retrieval.  An asterisk ("*") can be used as a
//     *          wildcard to represent 0 or more occurrences of any character.
//     *          If null no filtering is performed
//     * @param   includeHidden boolean indicating whether files marked as hidden
//     *          should be included or not in the list of files and directories
//     *          returned.
//     * @return  An Enumeration of strings, denoting the files and directories
//     *          in the directory matching the filter. Directories are denoted
//     *          with a trailing slash "/" in their returned name.  The
//     *          Enumeration has zero length if the directory is empty or no
//     *		files and/or directories are found matching the given filter.
//     *		Any current directory indication (".") and any parent directory
//     *		indication ("..") is not included in the list of files and
//     *		directories returned.
//     * @throws  IOException if invoked on a file, the directory does not exist,
//     *		the directory is not accessible, or an I/O error occurs.
//     * @throws  IllegalArgumentException if filter contains any path
//     *		specification or is an invalid filename for the platform
//     *		(e.g. contains characters invalid for a filename on the
//     *           platform).
//     */
//    public Vector list(String filter, boolean includeHidden)
//                                                        throws IOException;
//
//    /**
//     * List filesystem roots available on the device. For the description of
//     * the correct root format see <code>FileConnection</code> documentation.
//     * @return array of roots
//     */
//    public Vector listRoots();
//
    /**
     * Create file corresponding to this file handler. The
     * file is created immediately on the actual file system upon invocation of
     * this method.  Files are created with zero length and data can be put
     * into the file through write method after opening the file.This method
     * does not create any directories specified in the file's path.
     *
     * @throws IOException if invoked on the existing file or unexpected error
     *                     occurs.
     */
    public void create() throws IOException;

    /**
     * Check is file or directory corresponding to this filehandler exists.
     *
     * @return true if file exists, otherwise false
     */
    public boolean exists();

    /**
     * Check is file corresponding to this filehandler exists and is a
     * directory.
     *
     * @return true if directory exists, otherwise false
     */
    public boolean isDirectory();

    /**
     * Deletes the file or directory associated with this handler.
     * The file or directory is deleted immediately on
     * the actual file system upon invocation of this method. Previously open
     * native file should be closed.The
     * handler instance object remains connected and available for use.
     *
     * @throws  IOException If the target is a directory and it is not empty,
     *      the connection target does not exist or is unaccessible, or
     *      an unspecified error occurs preventing deletion of the target.
     */
    public void delete() throws IOException;

//    /**
//     * Renames the selected file or directory to a new name in the same
//     * directory.  The file or directory is renamed immediately on the actual
//     * file system upon invocation of this method. No file or directory by the
//     * original name exists after this method call. Previously open native file
//     * should be closed. The handler
//     * instance object remains connected and available for use,
//     * referring now to the file or directory by its new name.
//     *
//     * @param   newName The new name of the file or directory.  The name must
//     *          be the full qualified name of the new file
//     * @throws  IOException if the connection's target does not exist, the
//     *          connection's target is not accessible, a file or directory
//     *          already exists by the <code>newName</code>, or
//     *          <code>newName</code> is an invalid filename for the platform
//     *          (e.g. contains characters invalid in a filename on
//     *          the platform).
//     */
//    public void rename(String newName) throws IOException;
//
//    /**
//     * Truncates the file, discarding all data from the given byte offset to
//     * the current end of the file.  If the byte offset provided is greater
//     * than or equal to the file's current byte count, the method returns
//     * without changing the file.
//     *
//     * @param   byteOffset the offset into the file from which truncation
//     *          occurs.
//     * @throws  IOException if invoked on a directory or the file does not
//     *          exist or is not accessible.
//     */
//    public void truncate(long byteOffset) throws IOException;

    /**
     * Determines the size of a file on the file system. The size of a file
     * always represents the number of bytes contained in the file; there is
     * no pre-allocated but empty space in a file. Users should perform an
     * explicit <code>flush()</code> on any open output streams to the file
     * prior to invoking this method to ensure accurate results.
     *
     * @return  The size in bytes of the selected file, or -1 if the
     *          file does not exist or is not accessible.
     * @throws  IOException if the method is invoked on a directory.
     */
    public long fileSize() throws IOException;

//    /**
//     * Determines the size in bytes on a file system of all of the files
//     * that are contained in a directory.
//     *
//     * @param   includeSubDirs if <code>true</code>, size calculation will
//     *          include all subdirectories' size.
//     * @return  The size in bytes occupied by the files included in the
//     *          directory, or -1 if the directory does not exist or is
//     *          not accessible.
//     * @throws  IOException if some error occures while accessing the directory.
//     */
//    public long directorySize(boolean includeSubDirs) throws IOException;
//
//    /**
//     * Check if file corresponding to this filehandler exists and has a 
//     * read permission.
//     *
//     * @return true if file has read permission, otherwise false
//     */
//    public boolean canRead();
//
//    /**
//     * Check is file corresponding to this filehandler exists and has a
//     * write permission.
//     *
//     * @return true if file has write permission, otherwise false
//     */
//    public boolean canWrite();
//
//    /**
//     * Check is file corresponding to this filehandler exists and is
//     * hidden.
//     *
//     * @return true if file is hidden, otherwise false
//     */
//    public boolean isHidden();

//    /**
//     * Sets the file or directory readable attribute to the
//     * indicated value.  The readable attribute for the file on the actual
//     * file system is set immediately upon invocation of this method. If the
//     * file system doesn't support a settable read attribute, this method is
//     * ignored and <code>canRead()</code> always returns true.
//     *
//     * @param   readable The new state of the readable flag of the
//     *          selected file.
//     * @throws	IOException if the connection's target does not exist or is not
//     *		accessible.
//     * @see     #canRead
//     */
//    public void setReadable(boolean readable) throws IOException;
//
//    /**
//     * Sets the file or directory associated with this file handler writable
//     * attribute to the
//     * indicated value.  The writable attribute for the file on the actual
//     * file system is set immediately upon invocation of the method. If the
//     * file system doesn't support a settable write attribute, this method is
//     * ignored and <code>canWrite()</code> always returns true.
//     *
//     * @param   writable The new state of the writable flag of the selected
//     *                   file.
//     * @throws  IOException if the connection's target does not exist or is not
//     *          accessible.
//     * @see     #canWrite
//     */
//    public void setWritable(boolean writable) throws IOException;
//
//    /**
//     * Sets the hidden attribute of the file associated with this file handler
//     * to the value provided.  The attribute is applied to the file on the
//     * actual file system immediately upon invocation of this method if the
//     * file system
//     * and platform support it. If the file system doesn't support a hidden
//     * attribute, this method is ignored and <code>isHidden()</code> always
//     * returns false.  Since the exact definition of hidden is
//     * system-dependent,
//     * this method only works on file systems that support a settable file
//     * attribute. For example, on Win32 and FAT file systems, a file may be
//     * considered hidden if it has been marked as such in the file's
//     * attributes; therefore this method is applicable.  However on UNIX
//     * systems a file may be considered to be hidden if its name begins with a
//     * period character ('.'). In the UNIX case, this method may be ignored and
//     * the method to make a file hidden may be the <code>rename()</code>
//     * method.
//     *
//     * @param   hidden The new state of the hidden flag of the selected file.
//     * @throws  IOException if the connection's target does not exist or is not
//     *          accessible.
//     * @see     #isHidden
//     */
//    public void setHidden(boolean hidden) throws IOException;

//    /**
//     * Returns the time that the file denoted by this file handler
//     * was last modified.
//     *
//     * @return time when the file was last modified.
//     */
//    public long lastModified();
//
//    /**
//     * Creates a directory corresponding to the directory
//     * string provided in the connect() method.
//     * The directory is created immediately on the actual
//     * file system upon invocation of this method.  Directories in the
//     * specified path are not recursively created and must be explicitly
//     * created before subdirectories can be created.
//     *
//     * @throws  IOException if invoked on an existing directory or on any file
//     *          (<code>create()</code> is used to create files), the target
//     *		file system is not accessible, or an unspecified error occurs
//     *		preventing creation of the directory.
//     */
//    public void mkdir() throws IOException;

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes, blocks until at least one byte is available.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException;

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @return     number of bytes written
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    public int write(byte b[], int off, int len) throws IOException;

    /**
     * Forces any buffered output bytes to be written out.
     * The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written that have been buffered by the connection,
     * should immediately be written to their intended destination.
     * <p>
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException;

    /**
     * Sets the location for the next write operation.
     * @param offset location for next write
     * @throws IOException if an error occurs
     */
    public void positionForWrite(long offset) throws IOException;

//    /**
//     * Determines the free memory that is available on the file system the file
//     * or directory resides on. This may only be an estimate and may vary based
//     * on platform-specific file system blocking and metadata information.
//     *
//     * @return  The available size in bytes on a file system, or -1 if an
//     *          error occurs.
//     */
//    public long availableSize();
//
//    /**
//     * Determines the total size of the file system the connection's target
//     * resides on.
//     *
//     * @return  The total size of the file system in bytes, or -1 if an
//     *          error occurs.
//     */
//    public long totalSize();

    /**
     * Returns a string that contains all characters forbidden for the use on
     * the given platform except "/" (forward slash) which is always considered
     * illegal. If there are no such characters an empty string is returned.
     * @return string of characters not allowed in file names
     */
    public String illegalFileNameChars();

//    /**
//     * Determines the used memory of a file system the connection's target
//     * resides on.  This may only be an estimate and may vary based
//     * on platform-specific file system blocking and metadata information.
//     *
//     * @return  The used size of bytes on a file system, or -1 if an
//     *          error occurs.
//     */
//    public long usedSize();

    /**
     * Close file associated with this handler. Open file and all system
     * resources should be released by this call. Handler object can be
     * reused by subsequent call to connect().
     *
     * @throws IOException if I/O error occurs
     */
    public void close() throws IOException;
}
