package tests;

import java.io.IOException;
import com.sun.squawk.VM;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class BWPub extends PlugInComponent {
	private PluginPPort pbw;
	private PluginRPort rbw;
	
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
		for (int i = 0; i < 1000; i++) {
			VM.println("[BWPub is running]");

			int rearWheelSpeedData = rbw.readInt();
			data = "bw|" + String.valueOf(rearWheelSpeedData);
			pbw.write(data);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}

		}
	}
}
