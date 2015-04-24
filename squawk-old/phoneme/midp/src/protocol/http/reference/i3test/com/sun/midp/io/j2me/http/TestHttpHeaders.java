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

package com.sun.midp.io.j2me.http;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

import com.sun.midp.i3test.TestCase;

public class TestHttpHeaders extends TestCase {

    final String URL =
        "http://nonexistent.example.com:8080/foo/bar?bazz#mumble";

    StubHttpProtocol conn;

    void setUp() throws IOException {
        conn = new StubHttpProtocol();
        conn.openPrim(getSecurityToken(), URL);
    }

    void tearDown() {
    }

    /**
     * Tests parsing of different pieces of the URL.
     */
    void testParsing() {
        assertEquals("URL", URL, conn.getURL());
        assertEquals("protocol", "http", conn.getProtocol());
        assertEquals("host", "nonexistent.example.com", conn.getHost());
        assertEquals("file", "/foo/bar", conn.getFile());
        assertEquals("query", "bazz", conn.getQuery());
        assertEquals("ref", "mumble", conn.getRef());
        assertEquals("port", 8080, conn.getPort());
    }

    /**
     * Tests that getting a property is case-insensitive relative to a 
     * property that had been set previously.
     */
    void testSetGet() throws IOException {
        conn.setRequestProperty("hElLo", "goodbye");
        assertEquals("two", "goodbye", conn.getRequestProperty("hello"));
    }

    /**
     * Tests overwriting of a property in a case-insensitive fashion.
     */
    void testOverwrite() throws IOException {
        conn.setRequestProperty("hello", "tweedledee");
        conn.setRequestProperty("HELLO", "tweedledum");
        assertEquals("three", "tweedledum", conn.getRequestProperty("hElLo"));
        assertEquals("count", 1, conn.reqProperties.size());
        assertEquals("value", "HELLO",
            conn.reqProperties.getKeyAt(0));
    }

    /**
     * Tests that the output matches the case of what was passed by the 
     * programmer.
     */
    void testOutput() throws IOException {
        conn.setRequestProperty("calvin", "hobbes");
        conn.setRequestProperty("CaLvIN", "Wittgenstein");
        conn.setInputBuffer("");

        conn.startRequest();

        String outbuf = conn.getOutputBuffer();
        assertTrue("calvin", outbuf.indexOf("CaLvIN: Wittgenstein") >= 0);
    }

    /**
     * Runs all the tests.
     */
    public void runTests() throws Throwable {
        declare("testParsing");
        setUp();
        testParsing();
        tearDown();

        declare("testSetGet");
        setUp();
        testSetGet();
        tearDown();

        declare("testOverwrite");
        setUp();
        testOverwrite();
        tearDown();

        declare("testOutput");
        setUp();
        testOutput();
        tearDown();
    }

}


/**
 * A stubbed Protocol class for HTTP. Uses StubStreamConnection to satisfy 
 * input requests and to buffer protocol output.
 */
class StubHttpProtocol extends Protocol {

    StubStreamConnection stream;
    String inbuf;

    protected StreamConnection connect() throws IOException {
        stream = new StubStreamConnection(inbuf);
        return stream;
    }

    String getOutputBuffer() {
        return stream.getOutputBuffer();
    }

    void setInputBuffer(String inbuf) {
        this.inbuf = inbuf;
    }

}


/**
 * A stubbed StreamConnection subclass. Reads are satisfied from a String 
 * provided to the constructor, and writes are buffered up and made available 
 * through getOutputBuffer().
 */
class StubStreamConnection implements StreamConnection {
    ByteArrayOutputStream baos;
    ByteArrayInputStream bais;
    String inbuf;

    StubStreamConnection(String inbuf) {
        this.inbuf = inbuf;
    }

    public InputStream openInputStream() throws IOException {
        if (bais == null) {
            bais = new ByteArrayInputStream(inbuf.getBytes());
        }
        return bais;
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public OutputStream openOutputStream() throws IOException {
        if (baos == null) {
            baos = new ByteArrayOutputStream();
        }
        return baos;
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    public void close() { }

    String getOutputBuffer() {
        return baos.toString();
    }
}
