package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

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
		//VM.println("Interrupted.\r\n");
	    }
	}
	//obj = (Object) "hej";

	this.obj = obj;
	//VM.println("WorkThread done");
    }
}

public class AutoBrake extends PlugInComponent {
	private PluginRPort ab;
	private PluginPPort brake;
	private PluginPPort brakeLight;
	
	public AutoBrake(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("AutoBrake.main()\r\n");
		AutoBrake autoBrake = new AutoBrake(args);
		autoBrake.run();
		VM.println("AutoBrake-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		ab = new PluginRPort(this, "ab");
		brake = new PluginPPort(this, "brake");
		brakeLight = new PluginPPort(this, "brakeLight");
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
	Thread.sleep(3000);
	Object o2 = p.obj;
	VM.println("plupp " + o2);
	if (p.obj != null) {
	    //p.stop();
	}
	return o2;
    }

	public void doFunction() throws InterruptedException {
	    String data;
		
	    VM.println("[AutoBrake is running] 1");



	    VM.println("[AutoBrake is running] 2");
	    while (true) {
		Thread.sleep(1000);

		try {
		    //Object obj = ab.receive();
		    Object obj = getval(ab);
		    if(obj != null) {
			VM.println("ab returned an object");
			VM.println("ab returned an object: " + obj);
			String distanceStr = (String)obj;
			VM.print("distance:");
			VM.println(distanceStr);
				
			int distance = Integer.parseInt(distanceStr);
			if (distance < 30) {
			    VM.jnaSetSelect(1);
			    brake.write(-100);
					
			    brakeLight.write("1|0"); // "pin_nr (red/yellow_1/yellow_2) | on/off (0/1)" // Turn on red light
			}
			else {
			    brake.write(0);
			    VM.jnaSetSelect(0);
					
			    brakeLight.write("1|1"); // "pin_nr (red/yellow_1/yellow_2) | on/off (0/1)" // Turn off red light
			}
		    } else {
			VM.println("ab returned null");
		    }
			
		} catch (Exception e) {
		    VM.println("AutoBrake: exception");
		}
	    }
	}
}
