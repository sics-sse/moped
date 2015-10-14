package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class IMUPub extends PlugInComponent {
	private PluginPPort pub;
	private PluginRPort imu;
	
	public IMUPub(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("IMUPub.main()\r\n");
		IMUPub publish = new IMUPub(args);
		publish.init();
		publish.doFunction();
		VM.println("IMUPub-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		pub = new PluginPPort(this, "pub");
		imu = new PluginRPort(this, "imu");
	}

	public void run() {}
	
	public void doFunction() {
		String data;
		VM.println("[IMUPub is running]");
		while (true) {
			String val = imu.readString();
			data = "IMUPub|" + val;
			VM.println(data);
			pub.write(data);
			try {
			    Thread.sleep(200);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}
		}
	}
	
}
