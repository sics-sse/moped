package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class VCUfromSCU extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
	
    public VCUfromSCU(String[] args) {
	super(args);
    }
	
    public VCUfromSCU() {
    }
	
    public static void main(String[] args) {
	VM.println("VCUfromSCU.main()\r\n");
	VCUfromSCU ap = new VCUfromSCU(args);
	ap.run();
	VM.println("VCUfromSCU-main done");
    }

    public void init() {
	// Initiate PluginPPort
	VM.println("init 1");
	speed = new PluginPPort(this, "sp");
	VM.println("init 2");
	steering = new PluginPPort(this, "st");
	VM.println("init 3");
    }
	
    public void doFunction() {
	int st1 = 100;

	while (true) {
	    try {
		VM.println("circle 1");
		Thread.sleep(2000);
		speed.write(0);
		steering.write(0);

		VM.println("circle 2");
		Thread.sleep(2000);
		speed.write(0);
		Thread.sleep(2000);

		for (int i = 1; i < 11; i++) {
		    VM.println("circle " + (i + 2));
		    speed.write(10*i);
		    steering.write(st1);
		    Thread.sleep(10000);
		}
	    } catch (InterruptedException e) {
		//VM.println("Interrupted.");
	    }
	}
    }

    public void run() {
	init();
	doFunction();
    }
}
