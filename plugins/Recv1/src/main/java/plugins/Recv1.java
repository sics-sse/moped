package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class Recv1 extends PlugInComponent {
	private PluginRPort ab;
	
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

	public Recv1(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("Recv1.main()\r\n");
		Recv1 publish = new Recv1(args);
		publish.run();
		VM.println("Recv1-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
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
	    Boolean pub = false;
		String data;
		int val = 0;
		int counter = 0;
		Boolean stop = false;

		VM.println("[Recv1 is running]");
		while (! stop) {

		    counter++;

		    int x;
		    if (true) {
			// does it still hang if we don't receive what
			// SCU sends?
			// answer: yes

			Object obj;
			try {
			    obj = ab.receive();
			} catch (NullPointerException e) {
			    VM.println("NULL pointer.\r\n");
			    obj = "-2";
			}
			if (obj != null) {
			    String s = (String) obj;
			    x = Integer.parseInt(s);
			} else {
			    x = -1;
			}
			if (x != -1)
			    VM.println("dist " + counter + " = " + x);

		    }

		    //as3.send("71");
		    //stop = true;

		    Thread.sleep(100);
		}
	}
}
