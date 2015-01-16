package tests;

import java.io.IOException;
import com.sun.squawk.VM;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;
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
		VM.println("UltraSonicReader.main()\r\n");
		UltraSonicReader publish = new UltraSonicReader(args);
		publish.init();
		publish.doFunction();
		VM.println("UltraSonicReader-main done\r\n");
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
			VM.println("[UltraSonicReader is running]");

			int ultraSonicData = ff.readInt();
			VM.print("UltraSonicData:");
			VM.println(ultraSonicData);
			fs.send(String.valueOf(ultraSonicData));
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}

		}
	}
}
