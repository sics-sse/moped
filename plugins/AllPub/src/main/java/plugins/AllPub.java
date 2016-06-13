package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class AllPub extends PlugInComponent {
	private PluginRPort ab;
	private PluginRPort adc;
	private PluginPPort pbw;
	private PluginRPort rbw;
	private PluginRPort fw;
	private PluginPPort led;
	
class WorkThread extends Thread {

    PluginRPort port;
    Object obj;

    WorkThread(PluginRPort port) {
	this.port = port;
	this.obj = null;
    }

    public void run() {
	Object obj = port.receive();

	if (false) {
	    try {
		Thread.sleep(4);
	    } catch (InterruptedException e) {
		//VM.println("Interrupted.");
	    }
	}
	//obj = (Object) "hej";

	this.obj = obj;
	//VM.println("WorkThread done");
    }
}

	public AllPub(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("AllPub.main()");
		AllPub publish = new AllPub(args);
		VM.println("AllPub.main() 2");
		publish.init();
		VM.println("AllPub.main() 3");
		publish.run();
		VM.println("AllPub-main done");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		pbw = new PluginPPort(this, "pbw");
		rbw = new PluginRPort(this, "rbw");
		fw = new PluginRPort(this, "fw");
		ab = new PluginRPort(this, "ab");
		adc = new PluginRPort(this, "adc");
		led = new PluginPPort(this, "led");
	}
	
	public void run() {
	    try {
		doFunction();
	    } catch (InterruptedException e) {
		VM.println("**************** Interrupted.");
		return;
	    }
	}

    private Object getval(PluginRPort port) throws InterruptedException {
	WorkThread p = new WorkThread(ab);
	p.start();
	Thread.sleep(1000);
	Object o2 = p.obj;
	//VM.println("plupp " + o2);
	if (p.obj != null) {
	    //p.stop();
	}
	return o2;
    }

    public void doFunction() throws InterruptedException {
	Boolean pub = true;
	int w = 500;
	String data;
	int val = 0;
	int counter = 0;
	VM.println("[AllPub is running]");
	int mcnt = 0;
	String pstr = "";
	int delay1 = 200;
	int delay2 = 200;

	while (true) {
	    mcnt += 1;

	    int rearWheelSpeedData = rbw.readInt();

	    if (false) {
		rearWheelSpeedData = val;
		val += 2;

		if (val >= 1000)
		    val -= 1000;
	    }

	    counter++;

	    if (false) {
		if (pub)
		    pbw.write("cnt|" + counter);

		Thread.sleep(w);
	    }

	    if (false) {
		VM.println("bw1 (" + counter + ") = " + rearWheelSpeedData);
		data = "bw|" + String.valueOf(rearWheelSpeedData);
		if (pub)
		    pbw.write(data);

		Thread.sleep(w);
	    }

	    int frontWheelSpeedData = fw.readInt();
	    if (false) {

		VM.println("speed = " + frontWheelSpeedData);
		data = "speed|" + String.valueOf(frontWheelSpeedData);
		if (pub)
		    pbw.write(data);

		Thread.sleep(w);
	    }

	    String adcstring = "-1";
	    adcstring = adc.readString();
	    if (false) {
		adcstring = adc.readString();

		if (adcstring == null)
		    adcstring = "null";
		VM.println("adc = " + adcstring);
		if (false) {
		    data = "adc|" + adcstring;
		    if (pub)
			pbw.write(data);

		    Thread.sleep(w);
		}
	    }

	    int x;
	    if (true) {
		// does it still hang if we don't receive what
		// SCU sends?
		// answer: yes

		//Object obj = getval(ab);
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
		VM.println("counter = " + counter + ", dist = " + x);

		if (false) {
		    // does it still hang if we don't publish
		    // what we received?
		    // answer: yes
		    Thread.sleep(w);

		    if (pub)
			pbw.write("dist|" + x);
		}
	    }

	    if (x != -1 || true) {
		pstr += " " + counter;

		pstr += " " + adcstring;
		pstr += " " + rearWheelSpeedData;
		pstr += " " + frontWheelSpeedData;

		pstr += " " + x;
	    }

	    //pstr = "";
	    // light LED

	    if (x > 2000) {
		led.write("1|0");
		led.write("2|1");
		led.write("3|1");
	    } else if (x == -1) {
		led.write("1|0");
		led.write("2|0");
		led.write("3|0");
	    } else if (x > 200) {
		led.write("1|1");
		led.write("2|0");
		led.write("3|0");
	    } else if (x > 150) {
		led.write("1|1");
		led.write("2|0");
		led.write("3|1");
	    } else if (x > 125) {
		led.write("1|1");
		led.write("2|1");
		led.write("3|0");
	    } else {
		led.write("1|1");
		led.write("2|1");
		led.write("3|1");
	    }

	    //		    if (x < -1) {
	    if (counter % 20 == 0) {
		if (pub)
		    pbw.write("all|" + pstr);

		pstr = "";
		mcnt = 0;

		Thread.sleep(delay1);
	    } else {
		if (x != -1)
		    pstr += "; ";
		// 100 instead of 200 here -> failure to publish
		Thread.sleep(delay2);
	    }

	}
    }
}
