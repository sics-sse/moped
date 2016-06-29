package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class MotorSpeed extends PlugInComponent {
    private PluginPPort pub, sp, st;
    private PluginRPort bw;
	
    public MotorSpeed() {}
	
    public MotorSpeed(String[] args) {
	super(args);
    }
	
    public static void main(String[] args) {
	MotorSpeed plugin = new MotorSpeed(args);
	plugin.run();
    }

    public void init() {
	sp = new PluginPPort(this, "sp");
	st = new PluginPPort(this, "st");
	pub = new PluginPPort(this, "pub");
	bw = new PluginRPort(this, "bw");
    }
	
    public void doFunction() throws InterruptedException {
	int speed, rspeed;
	int steering = -80;
	int cnt;
	int maxcnt;
	int maxspeed;
	java.util.Date date;

	sp.write(0);
	st.write(steering);
	Thread.sleep(2000);

	sp.write(0);
	st.write(steering);
	Thread.sleep(2000);

	for (speed = 7; speed < 100; speed++) {

	    cnt = 0;
	    maxcnt = 0;
	    maxspeed = -1;

	    while (true) {
		cnt++;

		sp.write(speed);
		st.write(steering);

		rspeed = bw.readInt();
		if (rspeed > maxspeed) {
		    maxcnt = cnt;
		    maxspeed = rspeed;
		} else {
		    if (cnt - maxcnt > 10)
			break;
		}

		date = new java.util.Date();
		String time = "" + date.getTime();

		VM.println("speed = " + rspeed);
		pub.write("MotorSpeed|" + time
			  + " " + cnt
			  + " " + speed
			  + " " + steering
			  + " " + rspeed);
		Thread.sleep(200);
	    }

	    sp.write(0);
	    st.write(steering);
	    for (int i = 0; i < 10; i++) {
		cnt++;
		rspeed = bw.readInt();
		date = new java.util.Date();
		String time = "" + date.getTime();
		pub.write("MotorSpeed|" + time
			  + " " + cnt
			  + " " + speed
			  + " " + steering
			  + " " + rspeed);

		Thread.sleep(200);
	    }

	}

	sp.write(0);
	st.write(0);
	Thread.sleep(2000);
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
