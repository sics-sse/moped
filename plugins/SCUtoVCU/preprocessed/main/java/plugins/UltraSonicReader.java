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
	
	public static void main(String[] args) {
		VM.println("SCUtoVCU.main()");
		SCUtoVCU publish = new SCUtoVCU(args);
		publish.init();
		publish.doFunction();
		VM.println("SCUtoVCU-main done");
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
		
		VM.println("[SCUtoVCU is running]");
		while (true) {
		    int ultraSonicData = ff.readInt();
		    //String ultraSonicData = ff.readString();
			VM.println("ultraSonicData = " + ultraSonicData);
			fs.send(String.valueOf(ultraSonicData));

			cnt++;
			if (cnt >= 10) {
				VM.println("UltraSonicData:" + ultraSonicData);
				cnt = 0;
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				VM.println("Interrupted.");
			}

		}
	}
}
