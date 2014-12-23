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

package com.sun.squawk.platform.posix;

import com.sun.squawk.VM;
import com.sun.squawk.VMThread;
import com.sun.cldc.jna.Pointer;
import com.sun.squawk.platform.GCFSockets;
import com.sun.cldc.jna.ptr.IntByReference;
import com.sun.squawk.platform.posix.natives.*;
import com.sun.squawk.platform.posix.natives.LibC.*;
import com.sun.squawk.util.Assert;
import java.io.IOException;

/**
 * POSIX implementation of GCFSockets that calls the BSD socket API.
 */
public class GCFSocketsImpl implements GCFSockets {

    public final static boolean DEBUG = false;

    public final static boolean NBIO_WORKS = false;

    LibC libc = LibC.INSTANCE;
    Socket sockets = Socket.INSTANCE;
    Ioctl ioctl = Ioctl.INSTANCE;

    private boolean tryFcntl = true;
        
    /** Read errno, try to clean up fd, and create exception. */
    private IOException newError(int fd, String msg)  {
        if (DEBUG) {
            VM.print(msg);
            VM.print(": errno: ");
        }
        int err_code = LibCUtil.errno();
        if (DEBUG) {
            VM.print(err_code);
            VM.println();
        }
        sockets.shutdown(fd, 2);
        libc.close(fd);
        return new IOException(" errno: " + err_code + " on fd: " + fd + " during " + msg);
    }
    
    private void set_blocking_flags(int fd, boolean is_blocking) throws IOException {
        if (tryFcntl) {
            int flags = libc.fcntl(fd, LibC.F_GETFL, 0);
            if (DEBUG) {    System.out.println("set_blocking_flags: fcntl F_GETFL = " + flags); }

            if (flags >= 0) {
                if (is_blocking == true) {
                    flags &= ~LibC.O_NONBLOCK;
                } else {
                    flags |= LibC.O_NONBLOCK;
                }
                if (DEBUG) {    System.out.println("set_blocking_flags: calling fcntl F_SETFL flags: " + flags);    }
                int res = libc.fcntl(fd, LibC.F_SETFL, flags);
                if (res != -1) {
                    return;
                }
            } else if (LibCUtil.errno() == LibC.EOPNOTSUPP) {
                tryFcntl = false; // once this fails, don't try again
            }
        }

        if (!tryFcntl) {
            if (DEBUG) {    System.out.println("set_blocking_flags: calling ioctl FIONBIO = " + !is_blocking);    }
            IntByReference setting = new IntByReference(is_blocking ? 0 : 1); /* if "is_blocking"==true, NBIO = FALSE */
            int res = ioctl.ioctl(fd, Ioctl.FIONBIO, setting);
            setting.free();
            if (DEBUG) {    System.out.println("set_blocking_flags: ioctl returned: " + res); }
            if (res >= 0) {
                return;
            }
        }
        throw newError(fd, "set_blocking_flags");
    }
    
    /**
     * @inheritDoc
     */
    public int open(String hostname, int port, int mode) throws IOException {
        // init_sockets(); win32 only
        int fd = -1;

        fd = sockets.socket(Socket.AF_INET, Socket.SOCK_STREAM, 0);
        if (DEBUG) { System.err.println("Socket.socket() = " + fd); }
        if (fd < 0) {
            throw newError(fd, "socket create");
        }

        set_blocking_flags(fd, /*is_blocking*/ false);

        NetDB.hostent phostent;
        // hostname is always NUL terminated. See socket/Protocol.java for detail.
        phostent = NetDB.INSTANCE.gethostbyname(hostname);
        if (phostent == null) {
            throw newError(fd, "gethostbyname (herrono = " + NetDB.INSTANCE.h_errno() + ")");
        }

        Socket.sockaddr_in destination_sin = new Socket.sockaddr_in();
        destination_sin.sin_family = Socket.AF_INET;
        destination_sin.sin_port = Inet.htons((short) port);
        destination_sin.sin_addr = phostent.h_addr_list[0];

        if (DEBUG) {
            System.err.println("Socket.sockaddr_in: " + destination_sin);
            System.err.println("connect: hostname: " + hostname + " port: " + port + " mode: " + mode);
        }
        if (sockets.connect(fd, destination_sin, destination_sin.size()) < 0) {
            int err_code = LibCUtil.errno(); 
            if (err_code == LibC.EINPROGRESS || err_code == LibC.EWOULDBLOCK) {
                // When the socket is ready for connect, it becomes *writable*
                // (according to BSD socket spec of select())
                VMThread.getSystemEvents().waitForWriteEvent(fd);

                int errno = getSockOpt(fd, Socket.SO_ERROR);
				if (errno != 0) {
                    String msg;
                    if (errno == LibC.ECONNREFUSED) {
                        msg = "connect refused";
                    } else {
                        msg = String.valueOf(errno);
                    }
					throw new IOException("ConnectException: " + msg);
				}

                if (DEBUG) { System.err.println("connect back fd = " + fd); }
            } else {
                throw newError(fd, "connect");
            }
        }

        try {
			setSockOpt(fd, Socket.IPPROTO_TCP, Socket.TCP_NODELAY, 1);
		} catch (IOException ex) {
		}
        
        return fd;
    }

    /**
     * Takes an IPv4 Internet address and returns string representing the address
     * in `.' notation
     * 
     * @param in the opaque bytes of an IPv4 "struct in_addr"
     * @return String
     */
    public String inet_ntop(int in) {
        Pointer charBuf = new Pointer(Socket.INET_ADDRSTRLEN);
        IntByReference addrBuf = new IntByReference(in); // the addr is passed by value (to handle IPv6)
        String result = sockets.inet_ntop(Socket.AF_INET, addrBuf, charBuf, Socket.INET_ADDRSTRLEN);
        addrBuf.free();
        charBuf.free();
        return result;
    }

    /**
     * Opens a server TCP connection to clients.
     * Creates, binds, and listens
     *
     * @param port local TCP port to listen on
     * @param backlog listen backlog.
     *
     * @return a native handle to the network connection.
     * @throws IOException 
     */
    public int openServer(int port, int backlog) throws IOException {
        int fd = -1;

        fd = sockets.socket(Socket.AF_INET, Socket.SOCK_STREAM, 0);
        if (DEBUG) { System.err.println("Socket.socket() = " + fd); }
        if (fd < 0) {
            throw newError(fd, "socket create");
        }
        
        set_blocking_flags(fd, /*is_blocking*/ false);

        setSockOpt(fd, Socket.SO_REUSEADDR, 1);

        setSockOpt(fd, Socket.IPPROTO_TCP, Socket.TCP_NODELAY, 1);

        Socket.sockaddr_in local_sin = new Socket.sockaddr_in();
        local_sin.sin_family = Socket.AF_INET;
        local_sin.sin_port = Inet.htons((short) port);
        local_sin.sin_addr = Socket.INADDR_ANY;
        if (DEBUG) { System.err.println("Socket.bind(" + fd + ", " + local_sin + ")"); }

        if (sockets.bind(fd, local_sin, local_sin.size()) < 0) {
            throw newError(fd, "bind");
        }
        
       if (sockets.listen(fd, backlog) < 0) {
            throw newError(fd, "listen");
        }
               
        return fd;     
    }
    
    /**
     * Accept client connections on server socket fd.
     * Blocks until a client connects.
     *
     * @param fd open server socket. See {@link #openServer}.
     *
     * @return a native handle to the network connection.
     * @throws IOException 
     */
    public int accept(int fd) throws IOException {
        Socket.sockaddr_in remote_sin = new Socket.sockaddr_in();
        IntByReference address_len = new IntByReference(4);
        int newSocket;

        try {
            if (DEBUG) { System.err.println("Socket.accept(" + fd + ", " + remote_sin + ")..."); }

            newSocket = sockets.accept(fd, remote_sin, address_len);

            if (newSocket < 0) {
                if (DEBUG) { System.err.println("Need to block for accept..."); }

                int err_code = LibCUtil.errno();
                if (err_code == LibC.EAGAIN || err_code == LibC.EWOULDBLOCK) {
                    VMThread.getSystemEvents().waitForReadEvent(fd);
                    newSocket = sockets.accept(fd, remote_sin, address_len);
                    if (newSocket < 0) {
                        throw newError(fd, "accept");
                    }
                } else {
// BUG! 
                    throw newError(fd, "accept");
                }
            }
        } finally {
            address_len.free();
        }

        set_blocking_flags(newSocket, /*is_blocking*/ false);
        // we could read info about client from remote_sin, but don't need to.

        if (DEBUG) { System.err.println("    Socket.accept(...) = " + newSocket); }
        return newSocket;
    }
    
    /**
     * @inheritDoc
     */
    public int readBuf(int fd, byte b[], int offset, int length) throws IOException {
        byte[] buf = b;
        int result;

        if (offset != 0) {
            if (DEBUG) {    System.err.println("readBuf() into temp buf"); }
            buf = new byte[length];
        }

        if (NBIO_WORKS) {
            result = libc.read(fd, buf, length); // We rely on open0() for setting the socket to non-blocking
            if (result < 0) {
                int err_code = LibCUtil.errno();
                if (err_code == LibC.EWOULDBLOCK) {
                    if (DEBUG) {    System.err.println("Wait for read in select..."); }
                    VMThread.getSystemEvents().waitForReadEvent(fd);
                    result = libc.read(fd, buf, length); // We rely on open0() for setting the socket to non-blocking
                }
                LibCUtil.errCheckNeg(result);
            }
        } else {
            // If non-blocking IO doesn't seems to be working, try this hack...

            int bAvail = available(fd);  // may throw IOException

            if (bAvail == 0) {
                if (DEBUG) {    System.err.println("Wait for read in select..."); }
                VMThread.getSystemEvents().waitForReadEvent(fd);
                bAvail = available(fd);
                if (bAvail == 0) { // woke up because connection is closed
                    if (DEBUG) {    System.err.println("readBuf(" + fd + ") signalling EOF."); }
                    return -1; // signal EOF
                }
            }
            if (DEBUG) {    System.err.println("readBuf(" + fd + ") returned from select. retry."); }

            int n = Math.min(bAvail, length); // don't read more than is asked for...
            result = libc.read(fd, buf, n); // only read what we know is there...
            LibCUtil.errCheckNeg(result);
        }

        if (result == 0) {
            // If remote side has shut down the connection gracefully, and all
            // data has been received, recv() will complete immediately with
            // zero bytes received.
            //
            // This is true for Win32/CE and Linux
            if (DEBUG) {    System.err.println("readBuf(" + fd + ") saw remote side shutdown."); }
            result = -1;
        }
        
        if (offset != 0 && result > 0) {
            System.arraycopy(buf, 0, b, offset, result);
        }
        if (DEBUG) { System.out.println("readBuf(" + fd + ") = " + result); }

        return result;
    }

    public int readByte(int fd, byte[] b) throws IOException {
        int result = -1; // EOF

        if (readBuf(fd, b, 0, 1) == 1) {
            result = b[0] & 0xFF; // do not sign-extend
        }

        return result;
    }

    public int readByte(int fd) throws IOException {
        return readByte(fd, new byte[1]);
    }
    
    /**
     * @inheritDoc
     */
    public int writeBuf(int fd, byte buffer[], int off, int len) throws IOException {
        int result = 0;
        byte[] buf = buffer;
        if (off != 0) {
            buf = new byte[len];
            System.arraycopy(buffer, off, buf, 0, len);
        }

        if (DEBUG) {    System.err.println("writeBuf(" + fd + ") before write."); }

        result = libc.write(fd, buf, len);// We rely on open0() for setting the socket to non-blocking

        if (result < 0) {
            int err_code = LibCUtil.errno();
            if (err_code == LibC.EWOULDBLOCK) {
                VMThread.getSystemEvents().waitForWriteEvent(fd);
                if (DEBUG) {    System.err.println("writeBuf(" + fd + ") returned from select. retry."); }
                result = libc.write(fd, buf, len); // We rely on open0() for setting the socket to non-blocking
            }
            if (DEBUG) {    System.err.println("writeBuf(" + fd + ") error:"); }
            LibCUtil.errCheckNeg(result);
        }

        return result;
    }

    /**
     * @inheritDoc
     */
    public int writeByte(int fd, int b) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = (byte)b;
        int result = writeBuf(fd, buf, 0, 1);
        return result;
    }

    private Pointer availableBuf = new Pointer(4);
    
    /**
     * @inheritDoc
     */
    public int available(int fd) throws IOException {
        int err = ioctl.ioctl(fd, Ioctl.FIONREAD, availableBuf.address().toUWord().toPrimitive());
        int result = availableBuf.getInt(0);
        LibCUtil.errCheckNeg(err);
        if (DEBUG) { System.err.println("available(" + fd + ") = " + result); }
        return result; 
    }

    /**
     * @inheritDoc
     */
    public void close(int fd) throws IOException {
        // NOTE: this would block the VM. A real implementation should
        // make this a async native method.
        sockets.shutdown(fd, 2);
        libc.close(fd);
        if (DEBUG) { System.out.println("close(" + fd + ")"); }
    }
    
    /**
     * set a socket option
     * 
     * @param socket socket descriptor
     * @param level Socket.SOL_SOCKET, etc.
     * @param option_name
     * @param option_value new value
     * @throws IOException on error
     */
    public void setSockOpt(int socket, int level, int option_name, int option_value) throws IOException {
        IntByReference value = new IntByReference(option_value);
        Assert.that(option_value == value.getValue());
        if (DEBUG) { System.out.println("setSockOpt(" + socket + ", " + level + ", " + option_name + ", " + option_value + ")"); }
        int err = sockets.setsockopt(socket, level, option_name, value, 4);

        Assert.that(option_value == value.getValue());
        value.free();
        LibCUtil.errCheckNeg(err);

        if (DEBUG) {
            int newValue = getSockOpt(socket, level, option_name);
            if (option_value != newValue) {
                System.out.println("FAILED: setSockOpt(" + socket + ", " + level + ", " + option_name + ", " + option_value + ")");
                System.err.println("   Ended with: " + newValue);

            }
        }
    }
  
    /**
     * get a socket option
     * 
     * @param socket socket descriptor
     * @param level Socket.SOL_SOCKET, etc.
     * @param option_name 
     * @return socket option value
     * @throws IOException on error
     */
    public int getSockOpt(int socket, int level, int option_name) throws IOException {
        IntByReference value = new IntByReference(0);
        IntByReference opt_len = new IntByReference(4);
        if (DEBUG) { System.out.println("getsockopt(" + socket + ", " + level + ", " + option_name + ")"); }

        int err = sockets.getsockopt(socket, level, option_name, value, opt_len);
        if (DEBUG) { System.out.println("    returned value: " + value.getValue() + ", size: " + opt_len.getValue()); }

        int result = value.getValue();
        value.free();
        Assert.that(opt_len.getValue() == 4);
        opt_len.free();
        LibCUtil.errCheckNeg(err);
        return result;
    }

    /**
     * set a socket option
     *
     * @param socket socket descriptor
     * @param option_name
     * @param option_value new value
     * @throws IOException on error
     */
    public void setSockOpt(int socket, int option_name, int option_value) throws IOException {
        setSockOpt(socket, Socket.SOL_SOCKET, option_name, option_value);
    }

    /**
     * get a socket option
     *
     * @param socket socket descriptor
     * @param option_name
     * @throws IOException on error
     */
    public int getSockOpt(int socket, int option_name) throws IOException {
        return getSockOpt(socket, Socket.SOL_SOCKET, option_name);
    }

}
