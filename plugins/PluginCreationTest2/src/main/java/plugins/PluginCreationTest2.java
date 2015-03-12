package plugins;

import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;

public class PluginCreationTest2 extends PlugInComponent {

	public PluginPPort steering;

	@Override
	public void run() {
		System.out.println("PluginCreationTest2 running");
		init();

	}

	@Override
	public void init() {
		System.out.println("PluginCreationTest2 initializing 3...");

		steering = new PluginPPort(this, "steeringAngle");

		steering.write(50);

		System.out.println("and done!");
	}

	public static void main(String[] args) {
		System.out.println("In PluginCreationTest2.main()");
		PluginCreationTest2 instance = new PluginCreationTest2();
		instance.run();
	}
}
