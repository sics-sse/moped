package tests;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.PIRTE;

public class LEDLighter extends PlugInComponent {
	public PluginPPort led;
	
	public LEDLighter(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("LEDLighter.main()\r\n");
		LEDLighter ledLighter = new LEDLighter(args);
		ledLighter.init();
		ledLighter.doFunction();
		VM.println("LEDLighter-main done\r\n");
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
	}
	
	public void doFunction() {
		VM.println("doFunction");
		try {
			for (int i = 0; i < 100; i++) {
//				// Initial LEDs
//				led.write("1|0");
//				Thread.sleep(100);
//				led.write("2|0");
//				Thread.sleep(100);
//				led.write("3|0");
//				Thread.sleep(3000);
//				// Light RED on
//				led.write("1|1");
//				Thread.sleep(3000);
//				// Light RED off
//				led.write("1|0");
//				Thread.sleep(3000);
//				// Light Yellow1 on
//				led.write("2|1");
//				Thread.sleep(3000);
//				// Light Yellow1 off
//				led.write("2|0");
//				Thread.sleep(3000);
//				// Light Yellow2 on
//				led.write("3|1");
//				Thread.sleep(3000);
//				// Light Yellow2 off
//				led.write("3|0");
//				Thread.sleep(3000);
//				// Light all leds on
//				led.write("1|1");
//				Thread.sleep(100);
//				led.write("2|1");
//				Thread.sleep(100);
//				led.write("3|1");
//				Thread.sleep(3000);
//				// Light all leds off
//				led.write("1|0");
//				Thread.sleep(100);
//				led.write("2|0");
//				Thread.sleep(100);
//				led.write("3|0");
//				Thread.sleep(20000);
				led.write("1|0");
				Thread.sleep(2000);
				led.write("1|1");
				Thread.sleep(10000);
//				led.write("2|0");
//				Thread.sleep(2000);
//				led.write("2|1");
//				Thread.sleep(10000);
//				led.write("3|0");
//				Thread.sleep(2000);
//				led.write("3|1");
//				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			VM.println("Interrupted.\r\n");
		}
	}

	public void run() {}
	
}