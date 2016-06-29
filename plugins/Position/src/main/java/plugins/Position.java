package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class Position extends PlugInComponent {
    public PluginRPort pos;
    public PluginPPort pub, sp, st;
	
    public Position(String[] args) {
	super(args);
    }
	
    public Position() {
    }
	
    public static void main(String[] args) {
	VM.println("Position.main()\r\n");
	Position ap = new Position(args);
	ap.run();
	VM.println("Position-main done");
    }

    public void init() {
	// Initiate PluginPPort
	pos = new PluginRPort(this, "pos");
	pub = new PluginPPort(this, "pub");
	sp = new PluginPPort(this, "sp");
	st = new PluginPPort(this, "st");
    }
	
    public void doFunction() throws InterruptedException {
	int cnt = 0;

	int goalx, goaly;
	int st_val = 0, sp_val;

	goalx = 100;
	goaly = 1300;

	// add: don't plan paths that go into walls
	// to handle actually going into objects, switch direction when
	// wheel speed = 0 and motor speed is not, and keep track of
	// which direction (forward/reverse) we're moving in
	// keep going when we haven't gotten any values for a while.

	while (true) {
	    cnt++;

	    long p;
	    p = 1;
	    p = pos.readLong();

	    int x, y, angle, quality, age;

	    double a1 = 0, a2 = 0, a3 = 0;

	    x = (int) (p >> 48);
	    y = (int) ((p >> 32) & 0xffff);
	    angle = (int) ((p >> 24) & 0xff);
	    quality = (int) ((p >> 16) & 0xff);
	    age = (int) (p & 0xffff);

	    if (y >= 32768)
		y -= 65536;

	    x = x + 300;
	    //y = 1000 - y;
	    y = y + 700;

	    VM.println("pos = " + x + " " + y + " " + angle + " "
		       + quality + " " + age);

	    if (quality > 20
		//&& x > 0
		&& y < 65000) {

		if (y < 300) {
		    goalx = 100;
		    goaly = 1300;
		} else if (y > 1300) {
		    goalx = 300;
		    goaly = 300;
		}

		VM.println("goal " + goalx + " " + goaly);

		a1 = -angle*360/256;
		a1 = (a1 + 720 + 90) % 360;
		a2 = Float11.atan2(goaly-y, goalx-x)*180/Math.PI;
		a3 = (a1-a2+720)%360;
		VM.println(a1 + " " + a2 + " " + a3);
		if (a3 < 180) {
		    st_val = (int) (100*a3/180);
		    if (st_val > 100)
			st_val = 100;
		} else {
		    st_val = (int) (-100*(360-a3)/180);
		    if (st_val < -100)
			st_val = -100;
		}
		sp_val = 100;
	    } else {
		sp_val = 0;
	    }

	    VM.println("speed " + sp_val + " steering " + st_val);

	    if (cnt > 6000) {
		sp.write(0);
		return;
	    }

	    st.write(st_val);
	    sp.write(sp_val);

	    //String str = "position = " + p + " " + cnt;
	    String str = "position " + cnt + ": " + x + " " + y + " " + angle + " " + quality + " " + age + " (" + st_val + " " + sp_val + " " + (int) a1 + " " + (int) a2 + " " + (int) a3 + ")";
	    //VM.println(str);
	    //pub.write("POS|" + str);

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
