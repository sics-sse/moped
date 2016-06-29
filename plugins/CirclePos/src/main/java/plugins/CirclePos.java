package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class CirclePos extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
    public PluginRPort pos;
	
    public CirclePos(String[] args) {
	super(args);
    }
	
    public CirclePos() {
    }
	
    public static void main(String[] args) {
	VM.println("CirclePos.main()\r\n");
	CirclePos ap = new CirclePos(args);
	ap.run();
	VM.println("CirclePos-main done");
    }

    public void init() {
	speed = new PluginPPort(this, "sp");
	steering = new PluginPPort(this, "st");
	pos = new PluginRPort(this, "pos");
    }
	
    public void doFunction() throws InterruptedException {
	int st1 = 100;

	VM.println("circle 1");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(st1);

	VM.println("circle 2");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(st1);
	Thread.sleep(2000);
		
	speed.write(7);
	steering.write(st1);

	while (true) {
	    Thread.sleep(1000);

	    long p = pos.readLong();
	    long x, y, dir;
	    x = (p>>48);
	    y = (p>>32) & 0xffff;
	    if (y >= 32768)
		y -= 65536;
	    dir = (p>>24) & 0xff;
	    VM.println(x + " " + y + " " + dir);

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
