package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class SpeedBrake extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
    private PluginPPort led;
    private PluginPPort ap;
    public PluginRPort fw;
    public PluginRPort ab;
	
    public SpeedBrake(String[] args) {
	super(args);
    }
	
    public SpeedBrake() {
    }
	
    public static void main(String[] args) {
	VM.println("SpeedBrake.main()\r\n");
	SpeedBrake ap = new SpeedBrake(args);
	ap.run();
	VM.println("SpeedBrake-main done");
    }

    public void init() {
	// Initiate PluginPPort
	speed = new PluginPPort(this, "sp");
	steering = new PluginPPort(this, "st");
	fw = new PluginRPort(this, "fw");
	ab = new PluginRPort(this, "ab");
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
		//VM.println("format exception (" + s + ")");
		x = Integer.parseInt(s.substring(1));
		x = ((int) s.charAt(0)) * 1000 + x;
	    }
	} else {
	    x = -1;
	}
	//VM.println("dist = " + x);
	return x;
    }

    public void doFunction() throws InterruptedException {
	int x;

	while (true) {

	    x = readdist();
	    if (x > 0) {
		double f = Float11.log(x);

		VM.println("dist = " + x);
		VM.println("f = " + ((int) (f*10000)));

	    }

	    Thread.sleep(200);
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
