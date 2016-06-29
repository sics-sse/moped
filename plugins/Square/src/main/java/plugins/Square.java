package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class Square extends PlugInComponent {
    private PluginPPort pub, sp, st, led;
    private PluginRPort ab, w1, w2, adc, sub;
	
    public Square(String[] args) {
	super(args);
    }
	
    public static void main(String[] args) {
	VM.println("Square.main()");
	Square publish = new Square(args);
	publish.run();
	VM.println("Square-main done");
    }

    @Override
	public void init() {
	// Initiate PluginPPort
	pub = new PluginPPort(this, "pub");
	sp = new PluginPPort(this, "sp");
	st = new PluginPPort(this, "st");
	led = new PluginPPort(this, "led");

	w1 = new PluginRPort(this, "w1");
	w2 = new PluginRPort(this, "w2");
	adc = new PluginRPort(this, "adc");
	sub = new PluginRPort(this, "sub");
	ab = new PluginRPort(this, "ab");
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
	
    public void doFunction() throws InterruptedException {
	String data;
	int cnt = 0;
	int lcnt = 0;
	VM.println("[Square is running]");

	int speed;
	int speed0 = 14;
	int state = 1;

	String val1, val2;
	int val, val3, val4;

	int mindist = 30000;
	int startdist = -1;
	int prevdist = -1;

	while (true) {

	    if (state == 1) {

		speed = speed0;

		sp.write(0);
		st.write(0);	    

		Thread.sleep(500);

		sp.write(speed);
		st.write(100);	    

		Thread.sleep(500);

		startdist = -1;
		mindist = 30000;

		lcnt = 0;

		Object o6 = ab.receive();
		String val6 = "";
		if (o6 != null) {
		    val6 = (String) o6;

		    startdist = Integer.parseInt(val6);
		    state = 3;
		}
		

		pub.write("Square|new state " + state);

	    } else if (state == 3) {

		// The car moves in a circle, and we are interested in the
		// part of the path that is closest to the wall.
		// This is the part closer than the distance when we enter
		// state 3 (origdist).
		// When we enter this part, we expect the distance to sink
		// until it either grows again, or becomes unknown (the latter
		// seems to be the rule).

		// Generally, we treat unknown distance (null returned from
		// the port) as infinity, and represent it as -1.
		// It may happen that it means that a point was missed,
		// and then should instead be ignored, but we don't handle
		// this situation.

		// When sampling distance rapidly, it could happen that noise
		// makes the change of distance non-monotonic. We don't handle
		// that. Some kind of low-pass filter is wanted then.

		int dist;

		cnt += 1;
		lcnt += 1;

		java.util.Date date = new java.util.Date();
		val1 = "" + date.getTime();

		// w1 = front wheel
		val3 = w1.readInt();
		val4 = w2.readInt();

		String closest = "";

		Object o6 = ab.receive();
		String val6 = "";
		if (o6 != null) {
		    val6 = (String) o6;

		    dist = Integer.parseInt(val6);
		} else {
		    dist = -1;
		}

		if (dist != -1 && dist < mindist) {
		    mindist = dist;
		}

		if (prevdist != -1 && prevdist < startdist && dist == -1) {
		    closest = "closest";
		}

		prevdist = dist;

		data = val1 + " " + val3 + " " + val4 + " " + val6 + " state" +
		    state + " mindist=" + mindist + " startdist=" + startdist + " prevdist=" + prevdist + " lcnt=" + lcnt + " " + closest;
		VM.println(data);
		data = "Square|" + cnt + " " + data;
		VM.println(data);
		pub.write(data);
		Thread.sleep(300);
		if (val4-val3 > 12 || val4-val3 < -12 || val3 == 0) {
		    // not on the floor, or ESC not reacting because it wasn't
		    // initialized: turn off motor and restart the app
		    led.write("1|0");
		    led.write("2|0");
		    led.write("3|0");

		    sp.write(0);
		    st.write(0);
		    Thread.sleep(5000);

		    state = 1;
		}
	    }
	}
    }
}
