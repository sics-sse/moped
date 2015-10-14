package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class Circle extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
	
    public Circle(String[] args) {
	super(args);
    }
	
    public Circle() {
    }
	
    public static void main(String[] args) {
	VM.println("Circle.main()\r\n");
	Circle ap = new Circle(args);
	ap.run();
	VM.println("Circle-main done");
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

	if (false) {
	    while (true) {
		try {
		    VM.println("circle 1");
		    Thread.sleep(2000);
		    speed.write(0);
		    steering.write(st1);

		    VM.println("circle 2");
		    Thread.sleep(2000);
		    speed.write(0);
		    steering.write(st1);
		    Thread.sleep(2000);

		    // was 11 before
		    for (int i = 1; i < 6; i++) {
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

	if (true) {
	    try {
		VM.println("circle 1");
		Thread.sleep(2000);
		speed.write(0);
		steering.write(st1);

		VM.println("circle 2");
		Thread.sleep(2000);
		speed.write(0);
		steering.write(st1);
		Thread.sleep(2000);
	    } catch (InterruptedException e) {
		//VM.println("Interrupted.");
	    }
		
	    speed.write(10*1);
	    steering.write(st1);

	    while (true) {
		try {
		    Thread.sleep(10000);
		} catch (InterruptedException e) {
		    //VM.println("Interrupted.");
		}
	    }
	}
    }

    public void run() {
	init();
	doFunction();
    }
}
