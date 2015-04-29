package tests;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class DistPub extends PlugInComponent {
	private PluginPPort fs;
	private PluginRPort ff;
	
	public DistPub(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("DistPub.main()\r\n");
		DistPub publish = new DistPub(args);
		publish.init();
		publish.doFunction();
		VM.println("DistPub-main done\r\n");
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
		for (int i = 0; i < 1000; i++) {
			VM.println("[DistPub is running]");
			int val = ff.readInt();
			data = "DistPub|" + val;
			fs.write(data);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}
		}
	}
	
}
