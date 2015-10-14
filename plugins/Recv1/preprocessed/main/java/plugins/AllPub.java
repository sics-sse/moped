package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class Recv1 extends PlugInComponent {
	private PluginRPort ab;
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

	public Recv1(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("Recv1.main()\r\n");
		Recv1 publish = new Recv1(args);
		publish.init();
		publish.doFunction();
		VM.println("Recv1-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		pbw = new PluginPPort(this, "pbw");
		rbw = new PluginRPort(this, "rbw");
		fw = new PluginRPort(this, "fw");
		ab = new PluginRPort(this, "ab");
	}
	
	public void run() {}

    private Object getval(PluginRPort port) {
	WorkThread p = new WorkThread(ab);
	p.start();
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    VM.println("Interrupted.");
	}
	Object o2 = p.obj;
	//VM.println("plupp " + o2);
	if (p.obj != null) {
	    //p.stop();
	}
	return o2;
    }

	public void doFunction() {
		String data;
		VM.println("[Recv1 is running]");
		for (int i = 0; i < 1000; i++) {

			int rearWheelSpeedData = rbw.readInt();
			VM.println("bw = " + rearWheelSpeedData);
			data = "bw|" + String.valueOf(rearWheelSpeedData);
			//pbw.write(data);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}

			int frontWheelSpeedData = fw.readInt();

			VM.println("speed = " + frontWheelSpeedData);
			data = "speed|" + String.valueOf(frontWheelSpeedData);
			//pbw.write(data);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}

			int x;
			if (true) {
			    //Object obj = getval(ab);
			    Object obj = ab.receive();
			    if (obj != null) {
				String s = (String) obj;
				x = Integer.parseInt(s);
			    } else {
				x = -1;
			    }
			    VM.println("dist = " + x);
			    //pbw.write("dist|" + x);
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}

		}
	}
}
