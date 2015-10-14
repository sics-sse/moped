package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;

public class LEDLighter extends PlugInComponent {
	public PluginPPort led;
	
    private int k;

	public LEDLighter(String[] args) {
		super(args);
	}
	
	public LEDLighter() {
	    k = 0;
	}
	
	public static void main(String[] args) {
	    //		VM.println("LEDLighter.main()\r\n");
		LEDLighter ledLighter = new LEDLighter(args);
		ledLighter.run();
		//		VM.println("LEDLighter-main done\r\n");
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
		led = new PluginPPort(this, "led");
		VM.println("new pluginpport led");
	}
	
    //    public void iter(int p, int e) {
    //	switch(p) {
    //	case 1:
    //	    VM.println("a");
    //	    break;
    //	case 2:
    //	    VM.println("b");
    //	    break;
    //	default:
    //	    VM.println("c");
    //	}
    //    }

    private void sleep(int ms) {
    	try {
    	    Thread.sleep(ms);
    	} catch (InterruptedException e) {
    	    VM.println("Interrupted.\r\n");
    	}
    }

    public void doFunction2() {
		VM.println("2|1");
		led.write("2|1");
    }

    private void setled(String str) {
	led.write(str);
	//	VM.println(str);
	sleep(500);
    }

    public void doFunction() {
	int i = 0;
	while (true) {
	    i++;
	    VM.println("1 cycle " + i);
	    setled("1|0");
	    setled("2|0");
	    setled("3|0");
	    setled("1|1");
	    setled("2|1");
	    setled("3|1");
	}
    }

	public void run() {
	    init();
	    doFunction();
	}
	
}