package tests;

import com.sun.squawk.Isolate;

public class HelloWorldMain { 
	public void loadData() throws Exception {
		System.out.println("Creating plugin isolate from HelloWorldMain");

		Isolate iso = new Isolate(null, 
								  "tests.ClassLoaderInput", 
								  new String[0],  
								  null, 
								  "plugin://tests/ClassLoaderInput/ClassLoaderInput.suite");
		iso.start();
		iso.join();
	}

    public static void main(String[] args) {
        System.out.println("Hello World, men hallo (from main in imp)");
        
        try {
        	HelloWorldMain hw = new HelloWorldMain();
        	hw.loadData();
        }
        catch (Exception ex) {
        	System.out.println("EXCEPTION in HelloWorldMain");
        	ex.printStackTrace();
        }
        
        System.out.println("Over and out from the hello message...");
    }
}
