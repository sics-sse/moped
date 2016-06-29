package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class VCUfromSCU extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
    public PluginRPort fw;
    public PluginRPort ab;
	
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
	VM.println("dist = " + x);
	return x;
    }

    public void doFunction() {
	int st1 = 100; // was 70

	try {
	    VM.println("star 1");
	    Thread.sleep(2000);
	    speed.write(0);
	    steering.write(st1);

	    VM.println("star 2");
	    Thread.sleep(2000);
	    speed.write(0);
	    steering.write(st1);
	    Thread.sleep(2000);
	} catch (InterruptedException e) {
	    //VM.println("Interrupted.");
	}

	int x;

	while (true) {

	    int count = 0;
	    int dir = 1;

	    int xmax = -1;

	    while (true) {
		count++;

		VM.println("star3 " + count + " " + dir);

		try {
		    steering.write(dir*st1);
		    if (dir > 0)
			speed.write(15);
		    else
			speed.write(-20);
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    //VM.println("Interrupted.");
		}

		int frontWheelSpeedData = fw.readInt();

		VM.println("star4 " + count + " " + dir*frontWheelSpeedData);

		try {
		    speed.write(0);
		    steering.write(0);
		    // 500 is sometimes too little for the motor to be able
		    // to switch to the other direction in the next step
		    // 1000 is enough, and 600 seems to be, too
		    Thread.sleep(600);
		} catch (InterruptedException e) {
		    //VM.println("Interrupted.");
		}

		if (dir > 0) {
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
		    VM.println("dist = " + x);

		    if (x != -1) {
			if (xmax < x)
			    xmax = x;
		    }

		    if (count > 10)
			break;

		}

		dir = -dir;
	    }
	    // now find a direction with at least 80% of xmax

	    VM.println("xmax " + xmax);

	    while (true) {
		count++;

		VM.println("star5 " + count + " " + dir);

		try {
		    steering.write(dir*st1);
		    if (dir > 0)
			speed.write(15);
		    else
			speed.write(-20);
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    //VM.println("Interrupted.");
		}

		int frontWheelSpeedData = fw.readInt();

		VM.println("star4 " + count + " " + dir*frontWheelSpeedData);

		try {
		    speed.write(0);
		    steering.write(0);
		    // 500 is sometimes too little for the motor to be able
		    // to switch to the other direction in the next step
		    // 1000 is enough, and 600 seems to be, too
		    Thread.sleep(600);
		} catch (InterruptedException e) {
		    //VM.println("Interrupted.");
		}

		if (dir > 0) {
		    x = readdist();
		    if (x != -1 && x > 0.8*xmax)
			break;
		}

		dir = -dir;
	    }

	    VM.println("found dist " + x);

	    speed.write(15);
	    steering.write(0);
	    while (true) {

		try {
		    Thread.sleep(200);
		} catch (InterruptedException e) {
		    //VM.println("Interrupted.");
		}

		x = readdist();
		VM.println("new dist " + x);
		if (x < 100) {
		    speed.write(0);
		    steering.write(0);
		    break;
		}

	    }
	    
	}
    }

    public void run() {
	init();
	doFunction();
    }
}
