package plugins;

import sics.plugin.PlugInComponent;

public class PluginCreationTest extends PlugInComponent {

	@Override
	public void run() {
		System.out.println("PluginCreationTest running");
		init();

	}

	@Override
	public void init() {
		System.out.println("PluginCreationTest initializing...");
		System.out.println("and done!");
		System.out.println("Howdy!");
	}

	public static void main(String[] args) {
		System.out.println("In PluginCreationTest.main()");
		PluginCreationTest instance = new PluginCreationTest();
		instance.run();
	}
}
