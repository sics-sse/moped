package tests;

import java.io.IOException;
import com.sun.squawk.VM;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class AutoBrake extends PlugInComponent {
	private PluginRPort ab;
	private PluginPPort brake;
	private PluginPPort brakeLight;
	
	public AutoBrake(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("AutoBrake.main()\r\n");
		AutoBrake autoBrake = new AutoBrake(args);
		autoBrake.init();
		autoBrake.doFunction();
		VM.println("AutoBrake-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		ab = new PluginRPort(this, "ab");
		brake = new PluginPPort(this, "brake");
		brakeLight = new PluginPPort(this, "brakeLight");
	}
	
	public void run() {}

	public void doFunction() {
		String data;
		while (true) {
			VM.println("[AutoBrake is running]");

			Object obj = ab.receive();
			if(obj != null) {
				String distanceStr = (String)obj;
				VM.print("distance:");
				VM.println(distanceStr);
				
				int distance = Integer.parseInt(distanceStr);
				if (distance < 30) {
					VM.jnaSetSelect(1);
					brake.write(-100);
					
					brakeLight.write("1|0"); // "pin_nr (red/yellow_1/yellow_2) | on/off (0/1)" // Turn on red light
				}
				else {
					VM.jnaSetSelect(0);
					
					brakeLight.write("1|1"); // "pin_nr (red/yellow_1/yellow_2) | on/off (0/1)" // Turn off red light
				}
			}
		}
	}
}
