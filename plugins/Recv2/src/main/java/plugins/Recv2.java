package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class Recv2 extends PlugInComponent {
	private PluginRPort ab;
	private PluginRPort ab3;
	private PluginPPort as3;
	private PluginPPort pbw;
	private PluginRPort rbw;
	private PluginRPort fw;
	
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

	public Recv2(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("Recv2.main()\r\n");
		Recv2 publish = new Recv2(args);
		publish.run();
		VM.println("Recv2-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		pbw = new PluginPPort(this, "pbw");
		rbw = new PluginRPort(this, "rbw");
		fw = new PluginRPort(this, "fw");
		ab = new PluginRPort(this, "abx");
		ab3 = new PluginRPort(this, "ab3");
		as3 = new PluginPPort(this, "as3");
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
	    Boolean pub = false;
		String data;
		int val = 0;
		int counter = 0;
		Boolean stop = false;

		VM.println("[Recv2 is running (waiting 10s)]");

		Thread.sleep(10000);

		VM.println("[Recv2 is running]");
		while (! stop) {

			int rearWheelSpeedData = rbw.readInt();

			rearWheelSpeedData = val;
			val += 2;

			if (val >= 1000)
			    val -= 1000;

			counter++;

			VM.println("bw1 (" + counter + ") = " + rearWheelSpeedData);
			if (false) {
			    data = "bw|" + String.valueOf(rearWheelSpeedData);
			    if (pub)
				pbw.write(data);

			    Thread.sleep(500);

			    int x;
			    if (true) {
				// does it still hang if we don't receive what
				// SCU sends?
				// answer: yes

				//Object obj = getval(ab);
				Object obj = ab.receive();
				if (obj != null) {
				    String s = (String) obj;
				    x = Integer.parseInt(s);
				} else {
				    x = -1;
				}
				VM.println("dist = " + x);

				if (true) {
				    // does it still hang if we don't publish
				    // what we received?
				    // answer: yes
				    Thread.sleep(500);

				    if (pub)
					pbw.write("dist|" + x);
				}
			    }

			    if (true) {
				// does it still hang if we don't receive what
				// SCU sends?
				// answer: yes

				//Object obj = getval(ab);
				Object obj = ab3.receive();
				if (obj != null) {
				    String s = (String) obj;
				    x = Integer.parseInt(s);
				} else {
				    x = -1;
				}
				VM.println("dist3 = " + x);

				if (true) {
				    // does it still hang if we don't publish
				    // what we received?
				    // answer: yes
				    Thread.sleep(500);

				    if (pub)
					pbw.write("dist|" + x);
				}
			    }

			}

			as3.send("71");
			stop = true;

			Thread.sleep(5000);

		}
	}
}
