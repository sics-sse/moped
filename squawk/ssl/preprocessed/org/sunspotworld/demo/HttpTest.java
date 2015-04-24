package org.sunspotworld.demo;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class HttpTest {

    public static void main(String[] args) throws Exception {
        HttpConnection httpConn = null;
        InputStream is = null;
        String dataRead = "";
        String url = "http://www.google.com/";
        if (args != null && args.length > 0) {
            url = args[0];
        }
        try {
            httpConn = (HttpConnection) Connector.open(url);
            if ((httpConn.getResponseCode() == HttpConnection.HTTP_OK)) {
                int length = (int) httpConn.getLength();
                is = httpConn.openInputStream();
                if (length == -1) {// unknown length returned by server.
                    // It is more efficient to read the data in chunks, so we
                    // will be reading in chunk of 1500 = Maximum MTU possible

                    int chunkSize = 1500;
                    byte[] data = new byte[chunkSize];
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int dataSizeRead = 0;// size of data read from input stream.
                    while ((dataSizeRead = is.read(data)) != -1) {
                        // it is not recommended to write to string in the
                        // loop as it causes heap defragmentation and it is
                        // inefficient, therefore we use the
                        // ByteArrayOutputStream.
                        baos.write(data, 0, dataSizeRead);
                        System.out.println("Data Size Read = " + dataSizeRead);
                    }
                    dataRead = new String(baos.toByteArray());
                    baos.close();
                } else {// known length
                    DataInputStream dis = new DataInputStream(is);
                    byte[] data = new byte[length];
                    // try to read all the bytes returned from the server.
                    dis.readFully(data);
                    dataRead = new String(data);
                }
                System.out.println(dataRead);
            } else {
                System.out.println("\nServer returned unhandled "
                        + "response code. " + httpConn.getResponseCode());
            }
        } catch (Throwable t) {
            System.out.println("Exception occurred during GET " + t.toString());
        }
        // Since only limited number of network objects can be in open state
        // it is necessary to clean them up as soon as we are done with them.
        finally {// Networking done. Clean up the network objects
            try {
                if (is != null)
                    is.close();
            } catch (Throwable t) {
                System.out.println("Exception occurred while closing input "
                        + "stream.");
            }
            try {
                if (httpConn != null)
                    httpConn.close();
            } catch (Throwable t) {
                System.out.println("Exception occurred " + t.toString());
            }
        }
    }
}
