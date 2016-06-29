package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class AP extends PlugInComponent {
	public PluginPPort speed;
	public PluginPPort steering;
	
    public PluginRPort rspeed;
    public PluginRPort rsteer;

	public AP(String[] args) {
		super(args);
	}
	
	public AP() {
	}
	
	public static void main(String[] args) {
		VM.println("AP.main()\r\n");
		AP ap = new AP(args);
		ap.run();
		VM.println("AP-main done\r\n");
	}

//	public void setSpeed(PluginPPort speed) {
//		this.speed = speed;
//	}
//	
//	public void setSteering(PluginPPort steering) {
//		this.steering = steering;
//	}
	
	public void init() {
		// Initiate PluginPPort
	    VM.println("init 1");
		speed = new PluginPPort(this, "sp");
	    VM.println("init 2");
		steering = new PluginPPort(this, "st");
	    VM.println("init 3");
		rspeed = new PluginRPort(this, "rsp");
	    VM.println("init 4");
		rsteer = new PluginRPort(this, "rst");
	    VM.println("init 5");
	}
	
    private void show(String msg, PluginRPort port) {
	//VM.println("show 1");
	//Object obj = port.receive();
	//String obj = port.readString();
	int x = port.readInt();
	String obj = "(" + x + ")";
	//VM.println("show 2");
	if (obj == null) {
	    VM.println("received from port " + msg + ": " + "null");
	} else {
	    VM.println("received from port " + msg + ": " + (String) obj);
	}
    }

    // TODO: check getting stuck moving backwards too

	public void doFunction() throws InterruptedException {
	    int s;
	    for (int i = 0; i < 100; i++) {
				
		// warmup
		Thread.sleep(2000);
		speed.write(0);
		steering.write(0);
		Thread.sleep(2000);
		// forward

		show("speed", rspeed);
		//show("steering", rsteer);

		speed.write(20);
		steering.write(0);
		Thread.sleep(2000);

		s = rspeed.readInt();
		VM.println("s = " + s);
		if (s < 5) {
		    VM.println("reversing");
		    speed.write(0);
		    steering.write(0);
		    Thread.sleep(3000);
		    speed.write(-10);
		    steering.write(0);
		    show("speed", rspeed);
		    Thread.sleep(7000);
		    show("speed", rspeed);
		}

		// turn left
		speed.write(20);
		steering.write(-80);
		Thread.sleep(3000);

		s = rspeed.readInt();
		VM.println("s = " + s);
		//s = 1;
		if (s < 5) {
		    VM.println("reversing");
		    speed.write(0);
		    steering.write(0);
		    Thread.sleep(3000);
		    speed.write(-30);
		    steering.write(0);
		    show("speed", rspeed);
		    Thread.sleep(7000);
		    show("speed", rspeed);
		}

		show("speed", rspeed);
		//show("steering", rsteer);

		// right, right
		speed.write(20);
		steering.write(80);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);
		Thread.sleep(500);
		show("speed", rspeed);

		//show("steering", rsteer);

		s = rspeed.readInt();
		VM.println("s = " + s);
		if (s < 5) {
		    VM.println("reversing");
		    speed.write(0);
		    steering.write(0);
		    Thread.sleep(3000);
		    speed.write(-10);
		    steering.write(0);
		    show("speed", rspeed);
		    Thread.sleep(7000);
		    show("speed", rspeed);
		}

		// straight
		speed.write(20);
		steering.write(0);
		Thread.sleep(4000);

		s = rspeed.readInt();
		VM.println("s = " + s);
		if (s < 5) {
		    VM.println("reversing");
		    speed.write(0);
		    steering.write(0);
		    Thread.sleep(3000);
		    speed.write(-10);
		    steering.write(0);
		    show("speed", rspeed);
		    Thread.sleep(7000);
		    show("speed", rspeed);
		}

		// stop
		speed.write(0);
		steering.write(0);
		Thread.sleep(2000);
	    }
	}

//	public PluginPPort getSpeedPort() { return speed; }
//	public PluginPPort getSteeringPort() { return steering; }
	
//	private int rescaleToPwm(int val) {
//		return (int) (Math.ceil(100 + (0.55556 * val)) * 16.38);
//	}
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