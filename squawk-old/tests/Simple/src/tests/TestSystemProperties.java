package tests;

import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.io.Connector;

import com.sun.squawk.VM;

/**
 * Simple class that prints out all system properties on VM running under Squawk, if there is one.
 * 
 * @author ea149956
 *
 */
public class TestSystemProperties {

    public static void main(String[] args) {
        try {
            DataInputStream propertiesStream = Connector.openDataInputStream("systemproperties:");
            while (propertiesStream.available() != 0) {
                System.out.println(propertiesStream.readUTF() + "=" + propertiesStream.readUTF());
            }
        } catch (IOException e) {
            VM.print("Error systemproperties");
        }
    }

    private TestSystemProperties() {
    }

}
