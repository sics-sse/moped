package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class Comm1 extends PlugInComponent {
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

        	public Comm1(String[] args) {
        		super(args);
        	}
        
        	public static void main(String[] args) {
        		VM.println("Comm1.main()");
        		Comm1 autoBrake = new Comm1(args);
        		autoBrake.init();
        		autoBrake.doFunction();
        		VM.println("Comm1-main done");
        	}

	@Override
	public void init() {
		// Initiate PluginPPort
		ab = new PluginRPort(this, "ab");
	}
	
	public void run() {
	    VM.println("Comm1.run()");
	    init();
	    doFunction();
	    VM.println("Comm1-main done");
	}

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
		
	    VM.println("[Comm1 is running] 2");
	    while (true) {
		try {
		    Thread.sleep(10000);
		} catch (InterruptedException e) {
		    VM.println("Interrupted.");
		}

		try {
		    //Object obj = ab.receive();
		    Object obj = getval(ab);
		    if(obj != null) {
			VM.println("ab returned an object");
			VM.println("ab returned an object: " + obj);
		    } else {
			VM.println("ab returned null");
		    }
			
		} catch (Exception e) {
		    e.printStackTrace();
		    VM.println("Comm1: exception");
		}
	    }
	}
}
