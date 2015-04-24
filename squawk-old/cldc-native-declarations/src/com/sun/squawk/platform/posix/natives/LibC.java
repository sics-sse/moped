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


package com.sun.squawk.platform.posix.natives;

import com.sun.cldc.jna.*;

/**
 *
 * Import common functions variables and constants from libc.
 */
@Includes({"<errno.h>", "<fcntl.h>", "<sys/stat.h>"})
public interface LibC extends Library {

    LibC INSTANCE = (LibC)
            Native.loadLibrary("RTLD",
                               LibC.class);

    int 

        EPERM = IMPORT,		/* Operation not permitted */

        ENOENT = IMPORT,		/* No such file or directory */

        ESRCH = IMPORT,		/* No such process */

        EINTR = IMPORT,		/* Interrupted system call */

        EIO = IMPORT,		/* Input/output error */

        ENXIO = IMPORT,		/* Device not configured */

        E2BIG = IMPORT,		/* Argument list too long */

        ENOEXEC = IMPORT,		/* Exec format error */

        EBADF = IMPORT,		/* Bad file descriptor */

        ECHILD = IMPORT,		/* No child processes */

        EDEADLK = IMPORT,		/* Resource deadlock avoided */
        /* 11 was EAGAIN */

        ENOMEM = IMPORT,		/* Cannot allocate memory */

        EACCES = IMPORT,		/* Permission denied */

        EFAULT = IMPORT,		/* Bad address */

        EBUSY = IMPORT,		/* Device busy */

        EEXIST = IMPORT,		/* File exists */

        EXDEV = IMPORT,		/* Cross-device link */

        ENODEV = IMPORT,		/* Operation not supported by device */

        ENOTDIR = IMPORT,		/* Not a directory */

        EISDIR = IMPORT,		/* Is a directory */

        EINVAL = IMPORT,		/* Invalid argument */

        ENFILE = IMPORT,		/* Too many open files in system */

        EMFILE = IMPORT,		/* Too many open files */

        ENOTTY = IMPORT,		/* Inappropriate ioctl for device */

        ETXTBSY = IMPORT,		/* Text file busy */

        EFBIG = IMPORT,		/* File too large */

        ENOSPC = IMPORT,		/* No space left on device */

        ESPIPE = IMPORT,		/* Illegal seek */

        EROFS = IMPORT,		/* Read-only file system */

        EMLINK = IMPORT,		/* Too many links */

        EPIPE = IMPORT,		/* Broken pipe */

        /* math software */
        EDOM = IMPORT,		/* Numerical argument out of domain */

        ERANGE = IMPORT,		/* Result too large */

        /* non-blocking and interrupt i/o */
        EAGAIN = IMPORT,		/* Resource temporarily unavailable */

        EWOULDBLOCK = IMPORT,		/* Operation would block */

        EINPROGRESS = IMPORT,		/* Operation now in progress */

        EALREADY = IMPORT,		/* Operation already in progress */

        /* ipc/network software -- argument errors */
        ENOTSOCK = IMPORT,		/* Socket operation on non-socket */

        EDESTADDRREQ = IMPORT,		/* Destination address required */

        EMSGSIZE = IMPORT,		/* Message too long */

        EPROTOTYPE = IMPORT,		/* Protocol wrong type for socket */

        ENOPROTOOPT = IMPORT,		/* Protocol not available */

        EPROTONOSUPPORT = IMPORT,		/* Protocol not supported */

        ENOTSUP = IMPORT,		/* Operation not supported */

        EAFNOSUPPORT = IMPORT,		/* Address family not supported by protocol family */

        EADDRINUSE = IMPORT,		/* Address already in use */

        EADDRNOTAVAIL = IMPORT,		/* Can't assign requested address */

        /* ipc/network software -- operational errors */
        ENETDOWN = IMPORT,		/* Network is down */

        ENETUNREACH = IMPORT,		/* Network is unreachable */

        ENETRESET = IMPORT,		/* Network dropped connection on reset */

        ECONNABORTED = IMPORT,		/* Software caused connection abort */

        ECONNRESET = IMPORT,		/* Connection reset by peer */

        ENOBUFS = IMPORT,		/* No buffer space available */

        EISCONN = IMPORT,		/* Socket is already connected */

        ENOTCONN = IMPORT,		/* Socket is not connected */

        ETIMEDOUT = IMPORT,		/* Operation timed out */

        ECONNREFUSED = IMPORT,		/* Connection refused */

        ELOOP = IMPORT,	/* Too many levels of symbolic links */

        ENAMETOOLONG = IMPORT,		/* File name too long */

        /* should be rearranged */
        EHOSTUNREACH = IMPORT,		/* No route to host */

        ENOTEMPTY = IMPORT,		/* Directory not empty */

        /* quotas & mush */
        EDQUOT = IMPORT,		/* Disc quota exceeded */

        ENOLCK = IMPORT,		/* No locks available */

        ENOSYS = IMPORT,		/* Function not implemented */

        EOVERFLOW = IMPORT,		/* Value too large to be stored in data type */

        ECANCELED = IMPORT,		/* Operation canceled */

        EIDRM = IMPORT,		/* Identifier removed */

        ENOMSG = IMPORT,		/* No message of desired type */

        EILSEQ = IMPORT,		/* Illegal byte sequence */

        EBADMSG = IMPORT,		/* Bad message */

        EMULTIHOP = IMPORT,		/* Reserved */

        ENODATA = IMPORT,		/* No message available on STREAM */

        ENOLINK = IMPORT,		/* Reserved */

        ENOSR = IMPORT,		/* No STREAM resources */

        ENOSTR = IMPORT,		/* Not a STREAM */

        EPROTO = IMPORT,		/* Protocol error */

        ETIME = IMPORT,		/* STREAM ioctl timeout */


        /* command values */
        F_DUPFD		= IMPORT,		/* duplicate file descriptor */
        F_GETFD		= IMPORT,		/* get file descriptor flags */
        F_SETFD		= IMPORT,		/* set file descriptor flags */
        F_GETFL		= IMPORT,	/* get file status flags */
        F_SETFL		= IMPORT,		/* set file status flags */

        /*
         * File status flags: these are used by open(2), fcntl(2).
         * They are also used (indirectly) in the kernel file structure f_flags,
         * which is a superset of the open/fcntl flags.  Open flags and f_flags
         * are inter-convertible using OFLAGS(fflags) and FFLAGS(oflags).
         * Open/fcntl flags begin with O_; kernel-internal flags begin with F.
         */
        /* open-only flags */
            O_RDONLY	= IMPORT,		/* open for reading only */
            O_WRONLY	= IMPORT,		/* open for writing only */
            O_RDWR		= IMPORT,		/* open for reading and writing */
            O_ACCMODE	= IMPORT,		/* mask for above modes */

            O_NONBLOCK	= IMPORT,		/* no delay */
            O_APPEND	= IMPORT,		/* set append mode */
            O_SYNC		= IMPORT,		/* synchronous writes */
            O_CREAT		= IMPORT,		/* create if nonexistant */
            O_TRUNC		= IMPORT,		/* truncate to zero length */
            O_EXCL		= IMPORT,		/* error if already exists */
        /* [XSI] directory restrcted delete */

        /* [XSI] directory */  S_IFBLK = IMPORT,
        /* [XSI] named pipe (fifo) */ S_IFCHR = IMPORT,
        /* [XSI] character special */ S_IFDIR = IMPORT,
        /* [XSI] type of file mask */ S_IFIFO = IMPORT,
        /* [XSI] regular */ S_IFLNK = IMPORT,
        /*
         * [XSI] The following are symbolic names for the values of type mode_t.  They
         * are bitmap values.
         */
        /* File type */
        S_IFMT = IMPORT,
        /* [XSI] block special */ S_IFREG = IMPORT,
        /* [XSI] symbolic link */ S_IFSOCK = IMPORT,
        /* [XSI] RWX mask for group */ S_IRGRP = IMPORT,
        /* [XSI] RWX mask for other */ S_IROTH = IMPORT,
        /* [XSI] RWX mask for owner */ S_IRUSR = IMPORT,
        /* [XSI] X for owner */ /* Read, write, execute/search by group */
        S_IRWXG = IMPORT,
        /* [XSI] X for group */ /* Read, write, execute/search by others */
        S_IRWXO = IMPORT,
        /* [XSI] socket */

        /* File mode */
        /* Read, write, execute/search by owner */
        S_IRWXU = IMPORT,
        /* [XSI] set user id on execution */ S_ISGID = IMPORT,
        /* [XSI] X for other */ S_ISUID = IMPORT,
        /* [XSI] set group id on execution */ S_ISVTX = IMPORT,
        /* [XSI] R for group */ S_IWGRP = IMPORT,
        /* [XSI] R for other */ S_IWOTH = IMPORT,
        /* [XSI] R for owner */ S_IWUSR = IMPORT,
        /* [XSI] W for group */ S_IXGRP = IMPORT,
        /* [XSI] W for other */ S_IXOTH = IMPORT,
        /* [XSI] W for owner */ S_IXUSR = IMPORT,

            /** set file offset to offset */
            SEEK_SET = IMPORT,
            /** set file offset to current plus offset */
            SEEK_CUR = IMPORT,
            /** set file offset to EOF plus offset */
            SEEK_END = IMPORT
        ; // END OF DEFINES
    
    /**
     * Gets the value of the C variable "errno".
     * Only valid after certain system calls, and only if the system call failed in some way.
     * 
     * @return typically a positive number
     */
    @GlobalVar int errno(); 
    
    /**
     * provides for control over descriptors.
     *
     * @param fd a descriptor to be operated on by cmd
     * @param cmd one of the cmd constants
     * @param arg 
     * @return a value that depends on the cmd.
     */
    int fcntl(int fd, int cmd, int arg);
    
    /**
     * open or create a file for reading or writing
     *
     * @param name String
     * @param oflag std libc open flags
     * @param mode  the mode for any created file
     * @return If successful, returns a non-negative integer, termed a file descriptor.  Returns
     *         -1 on failure, and sets errno to indicate the error.
     */
    int open(String name, int oflag, int mode);
    
    /**
     * delete a descriptor
     * 
     * @param fd a descriptor to be operated on by cmd
     * @return Upon successful completion, a value of 0 is returned.  Otherwise, a value of -1 is returned
     *         and the global integer variable errno is set to indicate the error.
     */
    int close(int fd);
    
    /**
     * Flush output on a descriptor
     * 
     * @param fd a descriptor to be flushed
     * @return Upon successful completion, a value of 0 is returned.  Otherwise, a value of -1 is returned
     *         and the global integer variable errno is set to indicate the error.
     */
    int fsync(int fd);

    /**
     * reposition read/write file offset
     * 
     * @param fd file descriptor
     * @param offset the offset to seek to
     * @param whence the kind of offset (SEEK_SET, SEEK_CUR, or SEEK_END)
     * @return the resulting offset location as measured in
     *         bytes from the beginning of the file.  If error, -1 is returned and errno is set
     *         to indicate the error.
     */
    int lseek(int fd, long offset, int whence);
    
    /**
     * read input
     * 
     * @param fd file descriptor
     * @param buf data buffer to read into
     * @param nbyte number of bytes to read
     * @return the number of bytes actually read is returned.  Upon reading end-of-file, zero
     *         is returned.  If error, a -1 is returned and the global variable errno is set to indicate
     *         the error
     */
    int read(int fd, byte[] buf, int nbyte);
    
    /**
     * write output
     * 
     * @param fd file descriptor
     * @param buf data buffer to write
     * @param nbyte number of bytes to read
     * @return the number of bytes which were written is returned.  If error,
     *         -1 is returned and the global variable errno is set to indicate the error.
     */
    int write(int fd, byte[] buf, int nbyte);
    
    /**
     * C struct stat
     * //    struct stat {
     * //        dev_t		st_dev;		/* [XSI] ID of device containing file             4 0
     * //        ino_t	  	st_ino;		/* [XSI] File serial number                       4 4
     * //        mode_t	 	st_mode;	/* [XSI] Mode of file (see below)             2 8
     * //        nlink_t		st_nlink;	/* [XSI] Number of hard links                 2 10
     * //        uid_t		st_uid;		/* [XSI] User ID of the file                      4 12
     * //        gid_t		st_gid;		/* [XSI] Group ID of the file                     4 16
     * //        dev_t		st_rdev;	/* [XSI] Device ID                                4 20
     * //        time_t		st_atime;	/* [XSI] Time of last access                  4 24
     * //        long		st_atimensec;	/* nsec of last access                        4 28
     * //        time_t		st_mtime;	/* [XSI] Last data modification time          4 32
     * //        long		st_mtimensec;	/* last data modification nsec                4 36
     * //        time_t		st_ctime;	/* [XSI] Time of last status change           4 40
     * //        long		st_ctimensec;	/* nsec of last status change                 4 44
     * //        off_t		st_size;	/* [XSI] file size, in bytes                      8 48
     * //        blkcnt_t	st_blocks;	/* [XSI] blocks allocated for file                8
     * //        blksize_t	st_blksize;	/* [XSI] optimal blocksize for I/O                4
     * //        __uint32_t	st_flags;	/* user defined flags for file                4
     * //        __uint32_t	st_gen;		/* file generation number                     4
     * //        __int32_t	st_lspare;	/* RESERVED: DO NOT USE!                          4
     * //        __int64_t	st_qspare[2];	/* RESERVED: DO NOT USE!                      16
     * //     };
     */
    public static class stat extends Structure {
        public final static int 
                EPERM = IMPORT;
        
        /** mode_t */
        public int st_mode;
        /** time_t Last data modification time */
        public int st_mtime;
        /** file size, in bytes */
        public long st_size;

    }
    
        /**
     * Get information on the open file with file descriptor "fd".
     *
     * @param fd file descriptor
     * @param stat Stat structure that will be filled with the current values
     * @return -1 is returned if an error occurs, otherwise zero is returned
     */
    int fstat(int fd, stat stat);

    /**
     * Get information on the named "name".
     *
     * @param name String
     * @param stat Stat structure that will be filled with the current values
     * @return -1 is returned if an error occurs, otherwise zero is returned
     */
    int stat(String name, stat stat);
    
}