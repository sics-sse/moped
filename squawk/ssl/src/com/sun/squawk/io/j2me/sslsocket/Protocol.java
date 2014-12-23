/*
 * Copyright 2000-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.io.j2me.sslsocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SecurityInfo;
import javax.microedition.io.StreamConnection;

import com.sun.midp.log.Logging;
import com.sun.midp.pki.CertStore;
import com.sun.midp.pki.X509Certificate;
import com.sun.midp.ssl.SSLStreamConnection;
import com.sun.squawk.io.ConnectionBase;
import com.sun.midp.pki.SpotCertStore;
import com.sun.spot.peripheral.TrustManager;

/**
 * Connection to the J2ME sslsocket API.
 * TODO: Currently does not work on the SPOT
 * @author  Vipul Gupta
 * @version 1.0 10/10/2000
 */

public class Protocol extends ConnectionBase /*extends NetworkConnectionBase*/ 
implements SecureConnection {
    private static final byte ABORT  = 0;
    private static final byte WARN   = 1;
    private static final byte IGNORE = 2;
    private byte certErrAction = ABORT;
    private boolean commentary = false;
    private int mode = 0;
    
    SSLStreamConnection ssc;
    
    public void SSLHandshakeUpdate(String status) {
	if (commentary) System.err.println(status);
    }

    
    


    /**
     * Open the connection
     * 
     * @param name the target for the connection
     * @param writeable a flag that is true if the caller expects 
     *                  to write to the connection.
     * @param timeouts  a flag to indicate that the called wants timeout 
     *                  exceptions
     * <p>
     * The name string for this protocol should be:
     * "sslsocket://<name or IP number>:<port number>;property1=value1;..."
     * Currently, the following properties and possible values are
     * supported. Note that these flags only allow coarse level of control
     * over the SSL handshake. For greater control, application developers
     * can use the com.sun.kssl API but bear in mind that those APIs have
     * not been standardized and may change in a later release.
     * 
     * <table>
     * <tr><th>Property Name</th>    <th>Allowed Values</th>  
     *                                          <th>Description</th></tr>
     * <tr><td>CertificateErrorHandling</td>  
     *     <td>ignore, warn</td>
     *     <td>Determines how the SSL handshake handles server certificate
     *         problems, default behavior is abort.</td></tr>
     * <tr><td>HandshakeCommentary</td> <td>on</td>
     *     <td>This causes a commentary of the SSL handshake to be
     *     written to System.err output stream as it progresses. </td></tr>
     * </table>
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) 
	throws IOException {
	
        try {
            if (protocol == null || protocol.length()==0) {
                throw new IllegalArgumentException("Protocol cannot be null or empty");
            }
    	    if (name == null || name.length()==0) {
    	            throw new IllegalArgumentException("Name cannot be null or empty");
    	        }            
            int port;
	    int idx;
	    int cidx;
	    String host = null;
	    String hostPort = null;
	    this.mode = mode;
	    if (!name.startsWith("//")) 
		throw new IOException("Bad URL format " + name);
	    idx = name.indexOf(';', 2); // find first semicolon
	    if (idx < 0) {
		hostPort = name.substring(2);
		idx = name.length();
	    } else {
		hostPort = name.substring(2, idx);
	    }
	    cidx = hostPort.indexOf(':');
	    if (cidx < 0) {
		host = hostPort;
		port = 443;
	    } else {
		host = hostPort.substring(0, cidx);
		port = Integer.parseInt(hostPort.substring(cidx + 1));
	    }
	    String nv = null;
	    String nvl = null;
	    while (idx < name.length()) { // pick up parameters
		cidx = name.indexOf(';', idx + 1);
		if (cidx < 0) {
		    nv = name.substring(idx + 1, name.length());
		    idx = name.length();
		} else {
		    nv = name.substring(idx + 1, cidx);
		    idx = cidx;
		}
		if ((nv == null) || nv.equals("")) continue;
		// We now have the name-value pair in nv, check
		// if a non-default behavior is requested
		nvl = nv.toLowerCase();
		if (nvl.compareTo("certificateerrorhandling=warn") == 0)
		    certErrAction = WARN;
		else if (nvl.compareTo("certificateerrorhandling=ignore") == 0)
		    certErrAction = IGNORE;
		else if (nvl.compareTo("handshakecommentary=on") == 0) 
		    commentary = true;
		else
		    throw new IOException("Bad parameter <" + nv + 
					  "> in URL");
	    }
	    
	    // System.out.println("Host:   <" + host + ">, " +
	    // "Port:   <" + port + ">, " +
	    // "OnErr:  <" + certErrAction + ">, " +
	    // "Cmnt:   <" + commentary + ">, " +
			       
	    // ssc = new SSLStreamConnection(host, port, this);
	    StreamConnection t =
		(StreamConnection) Connector.open("socket://" + host
			+ ":" + port);

	    // Create an SSL connection
	    // CertStore cs=SpotPublicKeyStore.getTrustedKeyStore();
            TrustManager tm = TrustManager.getTrustManager();
            SpotCertStore cs = tm.getCertStore();
	    ssc = new SSLStreamConnection(host, port, t.openInputStream(), t.openOutputStream(),cs);
	    t.close();
	    return this;
	} catch (Exception e) {
	    if (Logging.TRACE_ENABLED)
		Logging.trace(e, "sslsocket open failed.");
	    throw new IOException(e.getMessage());
	}
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    synchronized public InputStream openInputStream() throws IOException {
        if ((mode & Connector.READ) == 0)
	    throw new IOException("Connection not open for reading");   
	return (ssc.openInputStream());
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    synchronized public OutputStream openOutputStream() throws IOException {
        if ((mode & Connector.WRITE) == 0)
	    throw new IOException("Connection not open for writing");
	return (ssc.openOutputStream());
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    synchronized public void close() throws IOException {
	ssc.close();
    }





    public SecurityInfo getSecurityInfo() throws IOException {
	return ssc.getSecurityInfo();	
    }

 

}
