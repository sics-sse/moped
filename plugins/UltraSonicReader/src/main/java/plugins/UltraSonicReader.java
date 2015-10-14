package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class UltraSonicReader extends PlugInComponent {
	private PluginPPort fs;
	private PluginRPort ff;
	
	public UltraSonicReader(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("UltraSonicReader.main()");
		UltraSonicReader publish = new UltraSonicReader(args);
		publish.init();
		publish.doFunction();
		VM.println("UltraSonicReader-main done");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		fs = new PluginPPort(this, "fs");
		ff = new PluginRPort(this, "ff");
	}
	
	public void run() {}

	public void doFunction() {
		String data;
		int cnt = 0;
		int counter = 0;
		int countermod = 10;
		int delay1 = 2000;
		int delay2 = 100;
		
		int val = 1;

		if (false) {
		    VM.println("[UltraSonicReader is running 0]");

		    try {
			Thread.sleep(20000);
		    } catch (InterruptedException e) {
			VM.println("Interrupted.");
		    }
		}

		VM.println("[UltraSonicReader is running 1]");

		while (true) {
		    int ultraSonicData;
		    //String ultraSonicData = ff.readString();

		    if (true) {
			ultraSonicData = val;
			val += 1;

			if (val >= 1000)
			    val -= 1000;
		    } else {
			ultraSonicData = ff.readInt();
		    }

		    counter++;
		    //VM.println("ultraSonicData (" + counter + ") = " + ultraSonicData);
		    //if (counter % countermod == 0)
			if (false)
			ultraSonicData = -ultraSonicData;

		    fs.send(String.valueOf(ultraSonicData));

		    if (false) {
			cnt++;
			if (cnt >= 10) {
			    VM.println("UltraSonicData:" + ultraSonicData);
			    cnt = 0;
			}
		    }
			
		    if (true) {
			try {
			    //if (counter % countermod == 0) {
			    if (false) {
				Thread.sleep(2000);
			    } else {
				Thread.sleep(10);
			    }
			} catch (InterruptedException e) {
			    VM.println("Interrupted.");
			}
		    }

		}
	}
}
