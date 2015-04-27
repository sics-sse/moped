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
		
		VM.println("[UltraSonicReader is running]");
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
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.");
			}

		}
	}
}
