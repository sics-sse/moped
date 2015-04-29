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

	try {
	    VM.println("circle 1");
	    Thread.sleep(2000);
	    speed.write(0);
	    steering.write(0);

	    VM.println("circle 2");
	    Thread.sleep(2000);
	    speed.write(0);
	    Thread.sleep(2000);

	    while (true) {
		VM.println("circle 3");
		speed.write(10);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 4");
		speed.write(20);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 5");
		speed.write(30);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 6");
		speed.write(40);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 7");
		speed.write(50);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 8");
		speed.write(60);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 8");
		speed.write(70);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 8");
		speed.write(80);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 8");
		speed.write(90);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 8");
		speed.write(100);
		steering.write(st1);
		Thread.sleep(10000);

		VM.println("circle 6");
	    }
	} catch (InterruptedException e) {
	    //VM.println("Interrupted.");
	}
    }

    public void run() {
	init();
	doFunction();
    }
}
