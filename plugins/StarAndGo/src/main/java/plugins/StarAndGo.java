package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class StarAndGo extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
    private PluginPPort led;
    private PluginPPort ap;
    public PluginRPort fw;
    public PluginRPort ab;
	
    public StarAndGo(String[] args) {
	super(args);
    }
	
    public StarAndGo() {
    }
	
    public static void main(String[] args) {
	VM.println("StarAndGo.main()\r\n");
	StarAndGo ap = new StarAndGo(args);
	ap.run();
	VM.println("StarAndGo-main done");
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
		VM.println("format exception (" + s + ")");
		x = -1;
	    }
	} else {
	    x = -1;
	}
	VM.println("dist = " + x);
	return x;
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

	int x;

	while (true) {

	    int count = 0;
	    int dir = 1;

	    int xmax = -1;

	    led.write("1|0");
	    led.write("2|1");
	    led.write("3|1");

	    while (true) {

		java.util.Date date= new java.util.Date();
		VM.println("" + date.getTime());


		VM.println("star33 " + count + " " + dir);

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

		    String pubData = "pub|" + x;
		    //ap.write(pubData);

		    if (x != -1) {
			if (xmax < x)
			    xmax = x;
			count++;
		    }

		    if (count > 10)
			break;

		}

		dir = -dir;
	    }
	    // now find a direction with at least 80% of xmax

	    VM.println("xmax " + xmax);

	    led.write("1|1");
	    led.write("2|0");
	    led.write("3|1");

	    while (true) {
		count++;

		VM.println("star5 " + count + " " + dir);

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

		if (dir > 0) {
		    x = readdist();
		    if (x != -1 && x > 0.8*xmax)
			break;
		}

		dir = -dir;
	    }

	    VM.println("found dist " + x);

	    led.write("1|1");
	    led.write("2|1");
	    led.write("3|0");

	    speed.write(15);
	    steering.write(0);
	    while (true) {

		Thread.sleep(200);

		x = readdist();
		VM.println("new dist " + x);
		
		if (x != -1 && x < 100) {
		    speed.write(0);
		    steering.write(0);
		    break;
		}

	    }
	    
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
