package plugins;

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
		VM.println("DistPub.main()");
		DistPub publish = new DistPub(args);
		publish.init();
		publish.doFunction();
		VM.println("DistPub-main done");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		fs = new PluginPPort(this, "fs");
		ff = new PluginRPort(this, "ff");
	}

	public void run() {}
	
	public void doFunction() {
		String data = "";
		int cnt = 0;
		VM.println("[DistPub X is running]");
		while (true) {
		    cnt += 1;
		    int val = ff.readInt();
		    data += " " + val;
		    VM.println(data);
		    if (cnt % 20 == 0) {
			data = "DistPub| (" + cnt + ")" + data;
			//VM.println(data);
			fs.write(data);
			data = "";
			try {
			    Thread.sleep(200);
			} catch (InterruptedException e) {
			    VM.println("Interrupted.");
			}
		    }
		}
	}
	
}
