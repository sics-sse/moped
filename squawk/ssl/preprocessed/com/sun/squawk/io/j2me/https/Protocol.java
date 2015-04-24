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

package com.sun.squawk.io.j2me.https;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SecurityInfo;
import javax.microedition.io.StreamConnection;

import com.sun.squawk.Isolate;
import com.sun.squawk.VM;
import com.sun.squawk.io.ConnectionBase;
import com.sun.squawk.io.j2me.https.httpsutil.DateParser;

/**
 * This class implements the necessary functionality
 * for an HTTP connection.
 */
public class Protocol extends ConnectionBase implements HttpsConnection {

    private static final String HTTP_PROXY_MANIFEST_PROPERTY = "com.sun.squawk.io.j2me.http.Protocol-HttpProxy";
    
    private int index; // used by URL parsing functions
    private String url;
    private String protocol;
    private String host;
    private String file;
    private String ref;
    private String query;
    private int port = 443;
    private int responseCode;
    private String responseMsg;
    private Hashtable reqProperties;
    private Hashtable headerFields;
    private String[] headerFieldNames;
    private String[] headerFieldValues;
    private String method;
    private int opens;
    private boolean opened=false;
    private int mode;
    private boolean timeouts;

    private boolean connected;

    /*
     * In/out Streams used to buffer input and output
     */
    private PrivateInputStream in;
    private PrivateOutputStream out;

    /*
     * The streams from the underlying socket connection.
     */
    private SecureConnection streamConnnection;
    private DataOutputStream streamOutput;
    private DataInputStream streamInput;

    /*
     * A shared temporary buffer used in a couple of places
     */
    private StringBuffer stringbuffer;
    private String http_version = "HTTP/1.1";

    /**
     * Create a new instance of this class.
     * We are initially unconnected.
     */
    public Protocol() {
        reqProperties = new Hashtable();
        headerFields = new Hashtable();
        stringbuffer = new StringBuffer(32);
        opens = 0;
        connected = false;
        method = GET;
        responseCode = -1;
        protocol = "https";
    }

    public Connection open(String protocol, String url, int mode, boolean timeouts) throws IOException {
        
        if(protocol == null || protocol.length()==0){
            throw new IllegalArgumentException("Protocol cannot be null or empty");
        }
        
        if (mode != Connector.READ && mode != Connector.WRITE && mode != Connector.READ_WRITE) {
            throw new IllegalArgumentException("illegal mode: " + mode);
        }
        
        if (opened) {
            throw new IOException("already connected");
        }
        
        this.url = url;
        this.mode = mode;
        this.timeouts = timeouts;
        parseURL();
        opened=true;
        opens++;
        return this;
    }

    /*
     * Close
     */
    public void close() throws IOException {
        if(opened){
            opened=false;
            if (--opens == 0) {
                disconnect();
            }
        }
    }

    /*
     * Open the input stream if it has not already been opened.
     * @exception IOException is thrown if it has already been
     * opened.
     */
    public InputStream openInputStream() throws IOException {

        if (in != null) {
            throw new IOException("already open");
        }

        // If the connection was opened and closed before the
        // data input stream is accessed, throw an IO exception
        if (!opened) {
            throw new IOException("connection is closed");
        }

        // Check that the connection was opened for reading
        if (mode != Connector.READ && mode != Connector.READ_WRITE) {
            throw new IOException("write-only connection");
        }

        connect();
        opens++;
        in = new PrivateInputStream();
        return in;
    }

    public OutputStream openOutputStream() throws IOException {

        if (mode != Connector.WRITE && mode != Connector.READ_WRITE) {
            throw new IOException("read-only connection");
        }

        // If the connection was opened and closed before the
        // data output stream is accessed, throw an IO exception
        if (!opened) {
            throw new IOException("connection is closed");
        }

        if (out != null) {
            throw new IOException("already open");
        }

        opens++;
        out = new PrivateOutputStream();
        return out;
    }

    /**
     * PrivateInputStream to handle chunking for HTTP/1.1.
     */
    class PrivateInputStream extends InputStream {

        int bytesleft;   // Number of bytes left in current chunk
        int bytesread;   // Number of bytes read since the stream was opened
        boolean chunked; // True if Transfer-Encoding: chunked
        boolean eof;     // True if EOF seen
        private boolean opened=false;

        PrivateInputStream() throws IOException {
            bytesleft = 0;
            bytesread = 0;
            chunked = false;
            eof = false;
            opened=true;

            // Determine if this is a chunked datatransfer and setup
            String te = (String)headerFields.get("transfer-encoding");
            if (te != null && te.equals("chunked")) {
                chunked = true;
                bytesleft = readChunkSize();
                eof = (bytesleft == 0);
            }
        }

        /**
         * Returns the number of bytes that can be read (or skipped over)
         * from this input stream without blocking by the next caller of
         * a method for this input stream.
         *
         * This method simply returns the number of bytes left from a
         * chunked response from an HTTP 1.1 server.
         */
        public int available() throws IOException {

            if (connected) {
                return bytesleft ;
            } else {
                throw new IOException("connection is not open");
            }
        }

        /**
         * Reads the next byte of data from the input stream.
         * The value byte is returned as an <code>int</code> in
         * the range <code>0</code> to <code>255</code>.
         * If no byte is available because the end of the stream
         * has been reached, the value <code>-1</code> is returned.
         * This method blocks until input data is available, the
         * end of the stream is detected, or an exception is thrown.
         *
         * <p> A subclass must provide an implementation of this method.
         *
         * @return     the next byte of data, or <code>-1</code>
         *             if the end of the stream is reached.
         * @exception  IOException  if an I/O error occurs.
         */
        public int read() throws IOException {

            // Be consistent about returning EOF once encountered.
            if (eof) {
                return -1;
            }

            /* If all the current chunk has been read and this
             * is a chunked transfer then read the next chunk length.
             */
            if (bytesleft <= 0 && chunked) {
                readCRLF();    // Skip trailing \r\n

                bytesleft = readChunkSize();
                if (bytesleft == 0) {
                    eof = true;
                    return -1;
                }
            }

            int ch = streamInput.read();
            eof = (ch == -1);
            bytesleft--;
            bytesread++;
            return ch;
        }

        /**
         * Reads some number of bytes from the input stream and
         * stores them into the buffer array <code>b</code>.
         * The number of bytes actually read is returned as an integer.
         * This method blocks until input data is available, end of
         * file is detected, or an exception is thrown.
         * (For HTTP requests where the content length has been
         * specified in the response headers, the size of the read
         * may be reduced if there are fewer bytes left than the
         * size of the supplied input buffer.)
         *
         * @param      b   the buffer into which the data is read.
         * @return     the total number of bytes read into the buffer,
         *             or <code>-1</code> is there is no more data
         *             because the end of the stream has been reached.
         * @exception  IOException  if an I/O error occurs.
         * @see        java.io.InputStream#read(byte[])
         */
        public int read(byte[]b) throws IOException {
            long len = getLength();

            if (len != -1) {
                // More bytes are expected
                len -= bytesread;
            } else {
                // Buffered reading in chunks
                len = b.length;
            }

            if (len == 0) {
                eof = true ;
                // All expected bytes have been read
                return -1;
            }

            return read (b, 0, (int)(len < b.length ? len : b.length)) ;
        }

        /* Read the chunk size from the input.
         * It is a hex length followed by optional headers (ignored)
         * and terminated with <cr><lf>.
         */
        private int readChunkSize() throws IOException {
            int size = -1;
            try {
                String chunk = readLine(streamInput);
                if (chunk == null) {
                    throw new IOException("No Chunk Size");
                }

                int i;
                for (i=0; i < chunk.length(); i++) {
                    char ch = chunk.charAt(i);
                    if (Character.digit(ch, 16) == -1)
                        break;
                }

                /* look at extensions?.... */
                size = Integer.parseInt(chunk.substring(0, i), 16);

            } catch (NumberFormatException e) {
                throw new IOException("Bogus chunk size");
            }

            return size;
        }

        /*
         * Read <cr><lf> from the InputStream.
         * @exception IOException is thrown if either <CR> or <LF>
         * is missing.
         */
        private void readCRLF() throws IOException {
            int ch;

            ch = streamInput.read();
            if (ch != '\r') {
                throw new IOException("missing CRLF");
            }

            ch = streamInput.read();
            if (ch != '\n') {
                throw new IOException("missing CRLF");
            }
        }

        public void close() throws IOException {
            if(opened){
                opened=false;
                if (--opens == 0) disconnect();    
            }
        }

    } // End of class PrivateInputStream

    /**
     * Private OutputStream to allow the buffering of output
     * so the "Content-Length" header can be supplied.
     */
    class PrivateOutputStream extends OutputStream {
        private ByteArrayOutputStream output;
        private boolean opened;

        public PrivateOutputStream() {
            output = new ByteArrayOutputStream();
            opened=true;
        }

        public void write(int b) throws IOException {
            output.write(b);
        }

        public void flush() throws IOException {
            if (output.size() > 0) {
                connect();
            }
        }

        public byte[] toByteArray() {
            return output.toByteArray();
        }

        public int size() {
            return output.size();
        }

        public void close() throws IOException {
            if(opened){
                flush();
                opened=false;
                if (--opens == 0) disconnect();    
            }
        }

    } // End of class PrivateOutputStream

    public String getURL() {
        // RFC:  Add back protocol stripped by Content Connection.
        return protocol + ":" + url;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return  (host.length() == 0 ? null : host);
    }

    public String getFile() {
        return (file.length() == 0 ? null : file);
    }

    public String getRef() {
        return  (ref.length() == 0 ? null : ref);
    }

    public String getQuery() {
        return (query.length() == 0 ? null : query);
    }

    public int getPort() {
        return port;
    }

    public String getRequestMethod() {
        return method;
    }

    public void setRequestMethod(String method) throws IOException {
        if (!method.equals(HEAD) &&
            !method.equals(GET)  &&
            !method.equals(POST)) {
            throw new IOException("unsupported method: " + method);
        }
        this.method = method;
    }

    public String getRequestProperty(String key) {
        return (String)reqProperties.get(key);
    }

    public void setRequestProperty(String key, String value) throws IOException {
        if(connected){
            throw new IOException("Cannot set request property when already connected");                
        }
        reqProperties.put(key, value);
    }

    public int getResponseCode() throws IOException {
        connect();
        return responseCode;
    }

    public String getResponseMessage() throws IOException {
        connect();
        return responseMsg;
    }

    public long getLength() {
        try {
            connect();
        }
        catch (IOException x) {
            return -1;
        }
        return getHeaderFieldInt("content-length", -1);
    }

    public String getType() {
        try {
            connect();
        }
        catch (IOException x) {
            return null;
        }
        return getHeaderField("content-type");
    }

    public String getEncoding() {
        try {
            connect();
        }
        catch (IOException x) {
            return null;
        }
        return getHeaderField("content-encoding");
    }

    public long getExpiration() {
        return getHeaderFieldDate("expires", 0);
    }

    public long getDate() {
        return getHeaderFieldDate("date", 0);
    }

    public long getLastModified() {
        return getHeaderFieldDate("last-modified", 0);
    }

    public String getHeaderField(String name) {
        try {
            connect();
        }
        catch (IOException x) {
            return null;
        }
        return (String)headerFields.get(toLowerCase(name));
    }

    public String getHeaderField(int index) {
        try {
            connect();
        }
        catch (IOException x) {
            return null;
        }

        if (headerFieldValues == null) {
            makeHeaderFieldValues ();
        }

        if (index >= headerFieldValues.length)
            return null;

        return headerFieldValues[index];
    }

    public String getHeaderFieldKey(int index) {
        try {
            connect();
        }
        catch (IOException x) {
            return null;
        }

        if (headerFieldNames == null) {
            makeHeaderFields ();
        }

        if (index >= headerFieldNames.length) {
            return null;
        }

        return headerFieldNames[index];
    }

    private void makeHeaderFields() {
        int i = 0;
        headerFieldNames = new String[headerFields.size()];
        for (Enumeration e = headerFields.keys();
            e.hasMoreElements();
            headerFieldNames[i++] = (String)e.nextElement());
    }

    private void makeHeaderFieldValues() {
        int i = 0;
        headerFieldValues = new String[headerFields.size()];
        for (Enumeration e = headerFields.keys();
             e.hasMoreElements();
             headerFieldValues[i++]=(String)headerFields.get(e.nextElement()));
    }

    public int getHeaderFieldInt(String name, int def) {
        try {
            connect();
        }
        catch (IOException x) {
            return def;
        }

        try {
            return Integer.parseInt(getHeaderField(name));
        } catch(Throwable t) {}

        return def;
    }

    public long getHeaderFieldDate(String name, long def) {
        try {
            connect();
        }
        catch (IOException x) {
            return def;
        }

        try {
            return DateParser.parse(getHeaderField(name));
        } catch(Throwable t) {}

        return def;
    }

    protected void connect() throws IOException {

        if (connected) {
            return;
        }

        try {

            String proxyReqAdd = "";

            /* Determine port number */
            String httpProxy = VM.getManifestProperty(HTTP_PROXY_MANIFEST_PROPERTY);
            if (httpProxy == null) {
                // Try to get the property from the Isolates properties
                httpProxy = Isolate.currentIsolate().getProperty(HTTP_PROXY_MANIFEST_PROPERTY);
            }
            if (httpProxy != null) {
                httpProxy = httpProxy.trim();
            }

            // Open socket connection using a proxy or not.
            if (httpProxy != null) {
                streamConnnection = (SecureConnection) Connector.open("sslsocket://" + httpProxy+";CertificateErrorhandling=warn;HandshakeCommentary=on", Connector.READ_WRITE, timeouts);
                proxyReqAdd = protocol + "://" + host + ":" + port + "/";
            } else {        	
                streamConnnection = (SecureConnection) Connector.open("sslsocket://" + host + ":" + port+";CertificateErrorhandling=warn;HandshakeCommentary=on", Connector.READ_WRITE, timeouts);
            }

            // Open data output stream
            streamOutput = streamConnnection.openDataOutputStream();
            streamInput = streamConnnection.openDataInputStream();

            // HTTP 1.1 requests must contain content length for proxies
            if (getRequestProperty("Content-Length") == null) {
                setRequestProperty("Content-Length", "" + (out == null ? 0 : out.size()));
            }

            // HTTP 1/1 requests require the Host header to
            // distinguish virtual host locations.
            setRequestProperty("Host", host + ":" + port);

            String reqLine = method + " " + proxyReqAdd + getFile() + (getRef() == null ? "" : "#" + getRef()) + (getQuery() == null ? "" : "?" + getQuery()) + " " + http_version + "\r\n";

            streamOutput.write((reqLine).getBytes());

            Enumeration reqKeys = reqProperties.keys();
            while (reqKeys.hasMoreElements()) {
                String key = (String) reqKeys.nextElement();
                String reqPropLine = key + ": " + reqProperties.get(key) + "\r\n";
                streamOutput.write((reqPropLine).getBytes());
            }

            streamOutput.write("\r\n".getBytes());

            if (out != null) {
                streamOutput.write(out.toByteArray());
                streamOutput.write("\r\n".getBytes());
            }
            streamOutput.flush();

            readResponseMessage(streamInput);
            readHeaders(streamInput);

            // Ignore a continuation header and read the true headers again.
            // (Bug# 4382226 discovered with Jetty HTTP 1.1 web server.
            if (responseCode == 100) {
                readResponseMessage(streamInput);
                readHeaders(streamInput);
            }

            connected = true;

        } catch (IOException e) {

            if (streamConnnection != null) {
                streamConnnection.close();
            }

            if (streamOutput != null) {
                streamOutput.close();
            }

            if (streamInput != null) {
                streamInput.close();
            }

            throw e;
        }
    }


    private void readResponseMessage(InputStream in) throws IOException {
        String line = readLine(in);
        int httpEnd, codeEnd;

        responseCode = -1;
        responseMsg = null;

        malformed: {
            if (line == null)
                break malformed;

            httpEnd = line.indexOf(' ');

            if (httpEnd < 0)
                break malformed;

            String httpVer = line.substring(0, httpEnd);

            if (!httpVer.startsWith("HTTP"))
                break malformed;

            if (line.length() <= httpEnd)
                break malformed;

            codeEnd = line.substring(httpEnd + 1).indexOf(' ');

            if (codeEnd < 0)
                break malformed;

            codeEnd += (httpEnd + 1);

            if (line.length() <= codeEnd)
                break malformed;

            try {
                responseCode =
                    Integer.parseInt(line.substring(httpEnd + 1, codeEnd));
            }
            catch (NumberFormatException nfe) {
                break malformed;
            }

            responseMsg = line.substring(codeEnd + 1);
            return;
        }

        throw new IOException("malformed response message");
    }

    private void readHeaders(InputStream in) throws IOException {
        String line, key, value;
        // Index of colon
        int colonIndex;

        while(true) {
            line = readLine(in);

            if (line == null || line.equals(""))
                return;

            colonIndex = line.indexOf(':');

            if (colonIndex < 0)
                throw new IOException("malformed header field");

            key = line.substring(0, colonIndex);

            if (key.length() == 0)
                throw new IOException("malformed header field");

            
            if (line.length() <= colonIndex + 2) {
                value = "";
            } else {
                value = line.substring(colonIndex + 2);
            }

            headerFields.put(toLowerCase(key), value);
        }
    }

    /*
     * Uses the shared stringbuffer to read a line 
     */
    private String readLine(InputStream in) throws IOException {
        int c;
        stringbuffer.setLength(0);

        while(true) {

            c = in.read();
            
            if (c < 0) {
                return null;
            }

            if (c == '\r') {
                continue;
            }

            if (c == '\n') {
                break;
            }

            stringbuffer.append((char)c);
        }

        return stringbuffer.toString();
    }

    protected void disconnect() throws IOException {
        if (connected) {
            if (streamConnnection != null) {
                if(streamInput!=null){
                    streamInput.close(); 
                    streamInput=null;
                }
                
                if(streamOutput!=null){
                    streamOutput.close();
                    streamOutput=null;
                }
                
                streamConnnection.close();
                streamConnnection = null;
            }

            responseCode = -1;
            responseMsg = null;
            connected = false;
        }
    }

    private String parseHostname() throws IOException {
        String buf = url.substring(index);

        if (buf.startsWith("//")) {
            buf = buf.substring(2);
            index += 2;
        }

    int n = buf.indexOf(':');

        if (n < 0) n = buf.indexOf('/');

        if (n < 0) n = buf.length();

        String token = buf.substring(0, n);
        index += n;
        return token;
    }

    private int parsePort() throws IOException {
        int p = 443;
        String buf = url.substring(index);

        if (!buf.startsWith(":")) return p;

        buf = buf.substring(1);
        index++;

        int n = buf.indexOf('/');
        if (n < 0) n = buf.length();

        try {
            p = Integer.parseInt(buf.substring(0, n));

            if (p <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            throw new IOException("invalid port");
        }

        index += n;
        return p;
    }

    private String parseFile() throws IOException {
        String token = "";
        String buf = url.substring(index);

        if (buf.length() == 0) return token;

        if (!buf.startsWith("/")) {
            throw new IOException("invalid path");
        }

        int n = buf.indexOf('#');
        int m = buf.indexOf('?');

        if (n < 0 && m < 0) {
            n = buf.length(); // URL does not contain any query or frag id.
        } else if (n < 0 || (m > 0 && m < n)) {
            n = m ;           // Use query loc if no frag id is present
                              // or if query comes before frag id.
                              // otherwise just strip the frag id.
        }

        token = buf.substring(0, n);
        index += n;
        return token;
    }

    private String parseRef() throws IOException {
        String buf = url.substring(index);

        if (buf.length() == 0 || buf.charAt(0) == '?') return "";

        if (!buf.startsWith("#")) {
            throw new IOException("invalid ref");
        }

        int n = buf.indexOf('?');

        if (n < 0) n = buf.length();

        index += n;
        return buf.substring(1, n);
    }

    private String parseQuery() throws IOException {
        String buf = url.substring(index);

        if (buf.length() == 0) return "";

        if (buf.startsWith("?")) {
            String token = buf.substring(1);
            int n = buf.indexOf('#');

            if (n > 0) {
                token = buf.substring(1, n);
                index += n;
            }
            return token;
        }
        return "";
    }

    protected synchronized void parseURL() throws IOException {
        index = 0;
        host = parseHostname();
        port = parsePort();
        file = parseFile();
        query = parseQuery();
        ref = parseRef();
    }

    private String toLowerCase(String string) {

        // Uses the shared stringbuffer to create a lower case string.
        stringbuffer.setLength(0);

        for (int i=0; i<string.length(); i++) {
            stringbuffer.append(Character.toLowerCase(string.charAt(i)));
        }

        return stringbuffer.toString();
    }

    public SecurityInfo getSecurityInfo() throws IOException {
	return streamConnnection.getSecurityInfo();
	
    }
}


