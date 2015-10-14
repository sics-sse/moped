package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class BWPub extends PlugInComponent {
	private PluginPPort pbw;
	private PluginRPort rbw;
	
	public BWPub() {
	}
	
	public BWPub(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("BWPub.main()\r\n");
		BWPub publish = new BWPub(args);
		publish.init();
		publish.doFunction();
		VM.println("BWPub-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		pbw = new PluginPPort(this, "pbw");
		rbw = new PluginRPort(this, "rbw");
	}
	
	public void run() {}

	public void doFunction() {
		String data;
		int extra = 0;
		while (true) {
			VM.println("[BWPub is running]");
			int rearWheelSpeedData = rbw.readInt();
			data = "bw|" + String.valueOf(rearWheelSpeedData+extra);
			pbw.write(data);
			VM.println(rearWheelSpeedData + " + " + extra);
			// Ensure that the values change, so something will
			// actually be published even if rearWheelSpeedData
			// is always 0.
			extra += 1;
			if (extra > 2)
			    extra = 0;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}

		}
	}
}
