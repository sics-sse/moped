package tests;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.PIRTE;

public class AP extends PlugInComponent {
	public PluginPPort speed;
	public PluginPPort steering;
	
	public AP(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("AP.main()\r\n");
		AP ap = new AP(args);
		ap.init();
		ap.doFunction();
		VM.println("AP-main done\r\n");
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
		speed = new PluginPPort(this, "sp");
		steering = new PluginPPort(this, "st");
	}
	
	public void doFunction() {
		try {
			for (int i = 0; i < 100; i++) {
				VM.println("[AP is running]");
				
				// warmup
				Thread.sleep(2000);
				speed.write(0);
				steering.write(0);
				Thread.sleep(2000);
				// forward
				speed.write(20);
				steering.write(0);
				Thread.sleep(2000);

				// turn left
				speed.write(20);
				steering.write(-80);
				Thread.sleep(3000);

				// right, right
				speed.write(20);
				steering.write(80);
				Thread.sleep(6000);

				// straight
				speed.write(20);
				steering.write(0);
				Thread.sleep(4000);

				// stop
				speed.write(0);
				steering.write(0);
				Thread.sleep(2000);
			}
		} catch (InterruptedException e) {
			VM.println("Interrupted.\r\n");
		}
	}

//	public PluginPPort getSpeedPort() { return speed; }
//	public PluginPPort getSteeringPort() { return steering; }
	
//	private int rescaleToPwm(int val) {
//		return (int) (Math.ceil(100 + (0.55556 * val)) * 16.38);
//	}
	public void run() {}
	
}