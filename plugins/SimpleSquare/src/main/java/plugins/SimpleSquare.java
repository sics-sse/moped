package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class SimpleSquare extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
	
    public SimpleSquare(String[] args) {
	super(args);
    }
	
    public SimpleSquare() {
    }
	
    public static void main(String[] args) {
	VM.println("SimpleSquare.main()\r\n");
	SimpleSquare ap = new SimpleSquare(args);
	ap.run();
	VM.println("SimpleSquare-main done");
    }

    public void init() {
	// Initiate PluginPPort
	VM.println("init 1");
	speed = new PluginPPort(this, "sp");
	VM.println("init 2");
	steering = new PluginPPort(this, "st");
	VM.println("init 3");
    }
	
    public void doFunction() throws InterruptedException {
	int st = 100;
	int sp = 100;

	VM.println("square 1");
	speed.write(0);
	steering.write(0);
	Thread.sleep(2000);

		
	// car2 drifts slightly to the right
	// car2: speed 10: turn 3500, straight 5000
	// speed 20: 1700, 2500
	// speed 30: 1200, 2000
	// speed 40: 900, 1500
	// 100: 400, 500

	while (true) {
	    VM.println("1");
	    speed.write(sp);
	    steering.write(st);

	    Thread.sleep(400);

	    VM.println("2");
	    speed.write(sp);
	    steering.write(0);

	    Thread.sleep(500);
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
