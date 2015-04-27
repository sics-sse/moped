package plugins;

import com.sun.squawk.VM;

import sics.plugin.PlugInComponent;

public class PluginCreationTest extends PlugInComponent {

	@Override
	public void run() {
	    VM.println("hej1");
	    //VM.println("PluginCreationTest running");
		init();

	}

	@Override
	public void init() {
	    VM.println("hej2");
	    //VM.println("PluginCreationTest initializing...");
	    //VM.println("and done!");
	    //VM.println("Howdy!");
	}

	public static void main(String[] args) {
	    //VM.println("In PluginCreationTest.main()");
	    VM.println("hej3");
	    //		PluginCreationTest instance = new PluginCreationTest();
	    //		instance.run();
	}
}
