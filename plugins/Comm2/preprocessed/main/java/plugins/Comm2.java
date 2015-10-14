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
	    VM.println("Comm2.run()");
	    init();
	    doFunction();
	    VM.println("Comm2-main done");
	}

	public void doFunction() {
		String data;
		int cnt = 0;
		
		VM.println("[Comm2 is running]");
		while (true) {
		    int ultraSonicData = 42;
		    fs.send(String.valueOf(ultraSonicData));
		    try {
			VM.println("Comm2: sending " + ultraSonicData);
			
		    } catch(Exception e2) {
			e2.printStackTrace();
		    }
		    try {
			Thread.sleep(10000);
		    } catch (InterruptedException e) {
			VM.println("Interrupted.");
		    }

		}
	}
}
