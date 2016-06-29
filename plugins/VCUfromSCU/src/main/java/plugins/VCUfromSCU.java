package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class VCUfromSCU extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
    private PluginPPort led;
    private PluginPPort ap;
    public PluginRPort fw;
    public PluginRPort ab;
    public PluginRPort adc;
	
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
	speed = new PluginPPort(this, "sp");
	steering = new PluginPPort(this, "st");
	fw = new PluginRPort(this, "fw");
	ab = new PluginRPort(this, "ab");
	adc = new PluginRPort(this, "adc");
	ap = new PluginPPort(this, "ap");
	led = new PluginPPort(this, "led");
    }
	
    public int readdist() {
	int x;
	Object obj = ab.receive();
	if (obj != null) {
	    String s = (String) obj;
	    try {
		x = Integer.parseInt(s);
	    } catch (NumberFormatException e) {
		VM.println("format exception (" + s + ")");
		x = -1;
	    }
	} else {
	    x = -1;
	}
	//VM.println("dist = " + x);
	return x;
    }

    public void doFunction() throws InterruptedException {
	int x;
	int counter = 0;

	while (true) {
	    counter++;
	    x = readdist();

	    String a = adc.readString();

	    VM.println("got " + x + " " + counter + " " + a);

	    Thread.sleep(1000);
	}
    }

    public void run() {
	init();
	try {
	    doFunction();
	} catch (InterruptedException e) {
	    VM.println("**************** Interrupted.");
	    return;
	}
    }
	
}
