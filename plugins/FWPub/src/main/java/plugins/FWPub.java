package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class FWPub extends PlugInComponent {
	private PluginPPort fs;
	private PluginRPort ff;
	
	public FWPub() {}
	
	public FWPub(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("FWPub.main()");
		FWPub publish = new FWPub(args);
		publish.init();
		publish.doFunction();
		VM.println("FWPub-main done");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		fs = new PluginPPort(this, "fs");
		ff = new PluginRPort(this, "ff");
	}
	
	public void run() {
		VM.println("FWPub.main()");
		init();
		doFunction();
		VM.println("FWPub-main done");
	}

	public void doFunction() {
		String data;
		//		for (int i = 0; i < 1000; i++) {
		int i = 0;
		while (true) {
		    i++;
//			VM.println("[FWPub is running]");
			
		    if (false) {
			// with port 7 instead of 5, we read battery
			// move this to AdcPub.
			String s = ff.readString();
			VM.println("value " + i + " = " + s);
			data = "voltage|" + s;
			fs.write(data);
		    } else {
			int frontWheelSpeedData = ff.readInt();

			VM.println("speed = " + frontWheelSpeedData);
			data = "fw|" + String.valueOf(frontWheelSpeedData);
			data = "fw|66";
			data = "speed|67";
			data = "speed|" + String.valueOf(frontWheelSpeedData);
			fs.write(data);
		    } try {
			//Thread.sleep(800);
			Thread.sleep(3000);
		    } catch (InterruptedException e) {
//				VM.println("Interrupted.");
			}

		}
	}
}
