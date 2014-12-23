package tests;

import java.io.*;

public class ResourceTest {
    
    public static void main(String[] args) {
        InputStream input = null;
        try {
            input = ResourceTest.class.getResourceAsStream("testResource");
            if (input == null) {
                System.out.println("No test resource to be found :(");
                return;
            }
            int read;
            while ((read = input.read()) != -1) {
                System.out.print((char) read);
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {input.close();} catch (IOException e) {};
            }
        }
    }

    private ResourceTest() {
    }
}
