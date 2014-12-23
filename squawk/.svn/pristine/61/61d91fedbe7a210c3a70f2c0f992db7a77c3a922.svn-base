/*
 * Copyright (c) 2007 Sun Microsystems, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

/*
 * StartApplication.java
 *
 * Created on Jun 18, 2007 2:12:17 PM;
 */

package org.sunspotworld.demo;

import com.sun.midp.pki.Utils;
import com.sun.midp.pki.X509Certificate;
import com.sun.spot.peripheral.TrustManager;
import com.sun.squawk.VM;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.HttpsConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.pki.CertificateException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 * 
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class StartApplication extends MIDlet {
	private String[] args;

    private String[] testURLs = {
//        "socket://www.google.com:80",
//        "http://www.google.com/",
//        "https://www.google.com/",
//        "sslsocket://www.google.com",
//          "https://login.yahoo.com/",
		    "https://welcome.intel.com/",
//          "sslsocket://login.yahoo.com:443",
        // reverse the order of the next two to see session reuse in action
//        "sslsocket://127.0.0.1:4433",
//        "https://127.0.0.1:4433/",   
//        "https://www.verisign.com/",
//        "https://webbranch.techcu.com/",
//        "sslsocket://webbranch.techcu.com:443",
//        "https://us.etrade.com/",

//        "https://www.amazon.com/",
    };
        
    public static void main(String[] args) throws Exception {
    	StartApplication startApplication = new StartApplication();
	    startApplication.args = args;
	    startApplication.startApp();
    }
    
    protected void startApp() throws MIDletStateChangeException {
        try {
            System.out.println("Started WebClient application ...");
    
    	    if(args == null || args.length == 0 || !args[0].equals("ignoreroots")) {
    		    System.out.println("Installing default root cerficates");
    		    installCertificate("Entrust");
    		    installCertificate("Equifax Secure Global eBusiness CA-1");
    		    installCertificate("GeoTrust");
    		    installCertificate("GlobalSign");
    		    installCertificate("GTE CyberTrust Global Root");
    		    installCertificate("Thawte Premium Server CA");
    		    installCertificate("Thawte Server CA");
    		    installCertificate("VeriSign Class 3 Public Primary CA");
    		    installCertificate("VeriSign Class 3 Public Primary Certification Authority - G5");
    		    installCertificate("VeriSign Secure Server Certification Authority");
    		    installCertificate("VeriSign Trust Network");
    	    }
    
            for (int i = 0; i < testURLs.length; i++) {
                System.out.println("Testing URL " + testURLs[i]);
                System.out.println("Memory available at start: " +
                        Runtime.getRuntime().freeMemory() + "/" +
                        Runtime.getRuntime().totalMemory());
                
                runtest(testURLs[i]);
                
                System.out.println("Memory available at end: " +
                        Runtime.getRuntime().freeMemory() + "/" +
                        Runtime.getRuntime().totalMemory());
    
            }
            
            System.out.println("\n *** FINISHED WEBCLIENT APPLICATION ***");
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
    }

	private void installCertificate(String certificateName) {
	    TrustManager trustManager = TrustManager.getTrustManager();
		if(trustManager.getCertStore().getCertByNickname(certificateName) == null) {
//		    try {
//                trustManager.getCertStore().addCert(certificateName, "", null);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
			try {
				InputStream is = StartApplication.class.getResourceAsStream("/" + certificateName + ".cer");
				byte[] buffer = new byte[4096];
				int byteCount = is.read(buffer);
				X509Certificate certificate = X509Certificate.generateCertificate(buffer, 0, byteCount);
				trustManager.getCertStore().addCert(certificateName, "", certificate);
				System.out.println("Installed certificate " + certificateName + " with fingerprint " + Utils.hexEncode(certificate.getFingerprint()));
				trustManager.flashTrustManager();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void pauseApp() {
        // This will never be called by the Squawk VM
    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }

    private void runtest(String url) {
        System.out.println("------------------------------------------------");
        System.out.println("Started testing GCF connection <" +
                url + ">\n");
        try {
            if (url.startsWith("sslsocket://") ||
                    url.startsWith("socket://")) {
                getViaStreamConnection(url);
            } else if (url.startsWith("http://")) {
                getViaHttpConnection(url);
            } else if (url.startsWith("https://")) {
                getViaHttpsConnection(url);
            } else {
                System.out.println("Unknown protocol.");
            }
        } catch (Exception exc) {
            System.out.println("Caught " + exc);
        }
        
        System.out.println("Finished testing GCF connection <" +
                url + ">\n");
        System.out.println("------------------------------------------------");
     }
     
    void getViaStreamConnection(String url) throws IOException {
         StreamConnection c = null;
         InputStream is = null;
         OutputStream os = null;
         try {
             long starttime = System.currentTimeMillis();
             c = (StreamConnection)Connector.open(url);
             is = c.openInputStream();
             long endTime = System.currentTimeMillis();
             System.out.println("Time for streamconnection set up: "
                    + (endTime - starttime) + " ms");
             os = c.openOutputStream();
             System.out.println("Writing GET request ...");
             os.write("GET / HTTP/1.0\r\n\r\n".getBytes());
             os.flush();
             int ch;
             System.out.println("Reading response ...");
             while ((ch = is.read()) != -1) {
                  System.out.print((char) ch);
             }
             System.out.flush();
             endTime = System.currentTimeMillis();
             System.out.println("Total time to retrieve page " +
                    "(including connection set up): " +
                    + (endTime - starttime) + " ms");
            
         } finally {
             if (is != null) 
                 is.close();
             if (os != null)
                 is.close();
             if (c != null)
                 c.close();
         }
     }
    
    void getViaHttpsConnection(String url) throws IOException {
        HttpsConnection c = null;
        InputStream is = null;
        try {
            long starttime = System.currentTimeMillis();
            c = (HttpsConnection)Connector.open(url);
            
            // Getting the InputStream will open the connection
            // and read the HTTP headers. They are stored until
            // requested.
            is = c.openInputStream();
            long endTime = System.currentTimeMillis();
            System.out.println("Time for HTTPS connection set up: "
                    + (endTime - starttime) + " ms");
            
            // Get the ContentType
            String type = c.getType();
            
            // Get the length and process the data
            int len = (int) c.getLength();
            if (len > 0) {
                byte[] data = new byte[len];
                int actual = is.read(data);
                System.out.println("Data received [" + actual + " of " +
                        len + " bytes] ...");
                System.out.write(data, 0, actual);
            } else {
                int ch;
                System.out.println("Data received ...");
                while ((ch = is.read()) != -1) {
                    System.out.print((char) ch);
                }
            }
            
            System.out.flush();
            
            endTime = System.currentTimeMillis();
            System.out.println("Total time to retrieve page " +
                    "(including connection set up): " +
                    + (endTime - starttime) + " ms");
            
            System.out.println("SSLSecurityInfo for <" + url + ">\n" +
                    c.getSecurityInfo());
           
        } finally {
            if (is != null)
                is.close();
            if (c != null)
                c.close();
        }
    }
              
    void getViaHttpConnection(String url) throws IOException {
        HttpConnection c = null;
        InputStream is = null;
        try {
            long starttime = System.currentTimeMillis();
            c = (HttpConnection)Connector.open(url);
            
            // Getting the InputStream will open the connection
            // and read the HTTP headers. They are stored until
            // requested.
            is = c.openInputStream();
            long endTime = System.currentTimeMillis();
            System.out.println("Time for HTTP connection set up: "
                    + (endTime - starttime) + " ms");
            
            // Get the ContentType
            String type = c.getType();
            
            // Get the length and process the data
            int len = (int) c.getLength();
            if (len > 0) {
                byte[] data = new byte[len];
                int actual = is.read(data);
                System.out.println("Data received [" + actual + " of " + 
                        len + " bytes] ...");
                System.out.write(data, 0, actual);
            } else {
                int ch;
                System.out.println("Data received ...");
                while ((ch = is.read()) != -1) {
                    System.out.print((char) ch);
                }                
            }
            
            System.out.flush();
            endTime = System.currentTimeMillis();
            System.out.println("Total time to retrieve page " +
                    "(including connection set up): " +
                    + (endTime - starttime) + " ms");
        } finally {
            if (is != null)
                is.close();
            if (c != null)
                c.close();
        }
    }
    
    /*    
    void postViaHttpConnection(String url) throws IOException {
        HttpConnection c = null;
        InputStream is = null;
        OutputStream os = null;
        
        try {
            c = (HttpConnection)Connector.open(url);
            
            // Set the request method and headers
            c.setRequestMethod(HttpConnection.POST);
            c.setRequestProperty("If-Modified-Since",
                    "29 Oct 1999 19:43:31 GMT");
            c.setRequestProperty("User-Agent",
                    "Profile/MIDP-1.0 Configuration/CLDC-1.0");
            c.setRequestProperty("Content-Language", "en-US");
            
            // Getting the output stream may flush the headers
            os = c.openOutputStream();
            os.write("LIST games\n".getBytes());
            os.flush();                // Optional, openInputStream will flush
            
            // Opening the InputStream will open the connection
            // and read the HTTP headers. They are stored until
            // requested.
            is = c.openInputStream();
            
            // Get the ContentType
            String type = c.getType();
            processType(type);
            
            // Get the length and process the data
            int len = (int)c.getLength();
            if (len > 0) {
                byte[] data = new byte[len];
                int actual = is.read(data);
                process(data);
            } else {
                int ch;
                while ((ch = is.read()) != -1) {
                    process((byte)ch);
                }
            }
        } finally {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
            if (c != null)
                c.close();
        }
    }
*/ 
}
