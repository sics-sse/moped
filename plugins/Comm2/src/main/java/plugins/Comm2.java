package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class Comm2 extends PlugInComponent {
    private PluginPPort fs;
	
            	public Comm2() {
            	}

            	public Comm2(String[] args) {
            		super(args);
            	}
        	
            	public static void main(String[] args) {
            		VM.println("Comm2.main()");
            		Comm2 publish = new Comm2(args);
            		publish.run();
            	}

	@Override
	public void init() {
		// Initiate PluginPPort
		fs = new PluginPPort(this, "fs");
	}
	
	public void run() {
	    init();
	    try {
		doFunction();
	    } catch (InterruptedException e) {
		VM.println("**************** Interrupted.");
		return;
	    }
	}
	
	public void doFunction() throws InterruptedException {
		String data;
		int cnt = 0;
		
		VM.println("[Comm2 is running]");
		while (true) {
		    int ultraSonicData = 42;
		    //fs.send(String.valueOf(ultraSonicData));
		    fs.send("abcdefghi123456789");
		    try {
			VM.println("Comm2: sending " + ultraSonicData);
			
		    } catch(Exception e2) {
			e2.printStackTrace();
		    }

		    Thread.sleep(10000);
		}
	}
}
