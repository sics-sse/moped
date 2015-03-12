package plugins;

import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;

public class PluginCreationTest3 extends PlugInComponent {

	public PluginPPort steering;

	@Override
	public void run() {
		System.out.println("PluginCreationTest3 running");
		init();

	}

	@Override
	public void init() {
		System.out.println("PluginCreationTest3 initializing 3...");

		steering = new PluginPPort(this, "steeringAngle");

		steering.write(-50);

		System.out.println("and done!");
	}

	public static void main(String[] args) {
		System.out.println("In PluginCreationTest3.main()");
		PluginCreationTest3 instance = new PluginCreationTest3();
		instance.run();
	}
}
