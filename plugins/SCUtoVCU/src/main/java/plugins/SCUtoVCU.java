package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class SCUtoVCU extends PlugInComponent {
	private PluginPPort fs;
	private PluginRPort ff;
	
	public SCUtoVCU(String[] args) {
		super(args);
	}
	
	public SCUtoVCU() {
	}
	
	public static void main(String[] args) {

		VM.println("SCUtoVCU.main()");
		SCUtoVCU publish = new SCUtoVCU(args);
		publish.run();
		VM.println("SCUtoVCU-main done");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		fs = new PluginPPort(this, "fs");
		ff = new PluginRPort(this, "ff");
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
		int counter = 0;
		
		int val = 1;

		VM.println("[SCUtoVCU is running 1]");

		while (true) {
		    int ultraSonicData;
		    //String ultraSonicData = ff.readString();

		    if (false) {
			ultraSonicData = val;
			val += 1;

			if (val >= 1000)
			    val -= 1000;
		    } else {
			ultraSonicData = ff.readInt();
		    }

		    java.util.Date date = new java.util.Date();

		    counter++;
		    VM.println("data (" + counter + ") = " + ultraSonicData + " " + date.getTime());

		    fs.send(String.valueOf(ultraSonicData));

		    if (true) {
			Thread.sleep(10);
		    }

		}
	}
}
