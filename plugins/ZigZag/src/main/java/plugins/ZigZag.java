package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class ZigZag extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering;
    private PluginPPort led;
    private PluginPPort ap;
    public PluginRPort fw;
    public PluginRPort ab;
	
    public ZigZag(String[] args) {
	super(args);
    }
	
    public ZigZag() {
    }
	
    public static void main(String[] args) {
	VM.println("ZigZag.main()\r\n");
	ZigZag ap = new ZigZag(args);
	ap.run();
	VM.println("ZigZag-main done");
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
	int speed1 = 20;
	int dist1 = 70;

	VM.println("zigzag 1");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(0);

	VM.println("zigzag 2");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(0);

	Thread.sleep(2000);
	speed.write(speed1);
	steering.write(0);

	int x;
	int turn;

	int curspeed = speed1;

	led.write("1|1");
	led.write("2|1");
	led.write("3|1");

	// direction to turn
	int dir = 1;

	// state 0: straight; state 1: turning; 
	int state = 0;
	int turncount = 0;

	int count = 0;

	while (true) {

	    if (dir == 1) {
		led.write("2|0");
		led.write("3|1");
	    } else {
		led.write("2|1");
		led.write("3|0");
	    }

	    x = readdist();
	    if (x > 1000) {
		VM.println("cmd = " + x);
		ap.write("CMD|" + x);
		int y = x/1000;
		int z = x%1000;
		if (y == 'L') { // 4C
		    VM.println(" setled " + z/10 + " " + z%10);
		    led.write(z/10 + "|" + z%10);
		} else if (y == 'S') { // 53
		    speed1 = z;
		} else if (y == 'D') { // 44
		    dist1 = z*10;
		}
	    } else {
		count++;

		if (x != -1) {
		    VM.println("dist = " + x);
		}

		if (turncount > 0) {
		    turn = 100*dir;
		    turncount--;
		    if (turncount == 0) {
			dir = -dir;
		    }
		} else if (x > -1 && x < dist1) {
		    turn = 100*dir;
		    turncount = 10;
		} else {
		    turn = 0;
		}
		steering.write(turn);
		speed.write(speed1);
		ap.write("DIST| " + count + " " + turn + " " + x + " " + dir + " " + turncount);

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
