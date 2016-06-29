package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class Star extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
    public PluginRPort fw;
	
    public Star(String[] args) {
	super(args);
    }
	
    public Star() {
    }
	
    public static void main(String[] args) {
	VM.println("Star.main()\r\n");
	Star ap = new Star(args);
	ap.run();
	VM.println("Star-main done");
    }

    public void init() {
	// Initiate PluginPPort
	speed = new PluginPPort(this, "sp");
	steering = new PluginPPort(this, "st");
	fw = new PluginRPort(this, "fw");
    }
	
    public void doFunction() throws InterruptedException {
	int st1 = 100; // was 70

	VM.println("star 1");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(st1);

	VM.println("star 2");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(st1);
	Thread.sleep(2000);

	int count = 0;
	int dir = 1;

	while (true) {
	    count++;

	    VM.println("star3 " + count + " " + dir);

	    steering.write(dir*st1);
	    if (dir > 0)
		speed.write(15);
	    else
		speed.write(-20);
	    Thread.sleep(1000);

	    int frontWheelSpeedData = fw.readInt();

	    VM.println("star4 " + count + " " + dir*frontWheelSpeedData);

	    speed.write(0);
	    steering.write(0);
	    // 500 is sometimes too little for the motor to be able
	    // to switch to the other direction in the next step
	    // 1000 is enough, and 600 seems to be, too
	    Thread.sleep(600);

	    dir = -dir;
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
