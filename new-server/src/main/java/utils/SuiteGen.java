package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
//import java.nio.file.Files;
//import java.nio.file.Path;
//
//import static java.nio.file.StandardCopyOption.*;

public class SuiteGen {
    // executable process
    private static Process process;
    private String squawkDir;
    private String cmd;
    private String reply;
	
    public SuiteGen(String squawkDir) {
	this.squawkDir = squawkDir;
	cmd = "./d.sh user-suite ";
    }

    public String getCmd() {
	return cmd;
    }

    public boolean generateSuite(String source, String[] r) {
	System.out.println("generating suite from source: " + source);
	System.out.println("squawkDir = " + squawkDir);
		
	int ret;
	reply = "";
	File sourceFileFolder = new File(source);
	if (sourceFileFolder.exists() && sourceFileFolder.isDirectory()) {
	    BufferedWriter bw = null;

	    try {			
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec("/bin/sh");
		// In case cmd.exe application is not initiated
		//Thread.sleep(1000);

		// Write command to console
		bw = new BufferedWriter(new OutputStreamWriter
					(process.getOutputStream()));
				
		// Go to directory of squawk
		bw.write("cd "+squawkDir+"\n");
		bw.flush();
				
		// Achieve output from console
		Thread runtimeInput = new Thread(new RuntimeInput());
		runtimeInput.start();
		//Thread.sleep(500);
				
                bw.write("./d.sh user-compile-r " + source +
                         " " + source + "/j2meclasses/plugins" + "\n");
                bw.write("mv " + source + "/weaved/*.class" + " " + source + "/j2meclasses/plugins" + "\n");
		bw.write(cmd + source + "\n");
		bw.close();

		ret = process.waitFor();
		if (ret != 0) {
		    r[0] = "subprocess returned " + ret + ":\n" + reply;
		    return false;
		}

		//Thread.sleep(1000);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		r[0] = "I/O exception";
		return false;
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		r[0] = "interrupted exception";
		return false;
	    }
	} else {
	    System.out.println("Didn't find a source folder");
	    r[0] = "no source folder: " + source;
	    return false;
	}
		
	r[0] = reply;
	return true;
    }

    public class RuntimeInput implements Runnable {

	public void run() {
	    BufferedReader br = new BufferedReader
		(new InputStreamReader(process.getInputStream()));
	    String content = null;
	    try {
		while ((content = br.readLine()) != null) {
		    reply += content + "\n";
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}

    }
}
