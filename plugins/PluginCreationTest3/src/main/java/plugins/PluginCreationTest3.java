package plugins;

import com.sun.squawk.VM;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;

public class PluginCreationTest3 extends PlugInComponent {

	public PluginPPort steering;

	public PluginCreationTest3(String[] args) {
		super(args);
		VM.println("ran constructor(args)");
	}
	
	public PluginCreationTest3() {
		super();
		VM.println("ran constructor()");
	}
	
	@Override
	public void run() {
	    VM.println("PluginCreationTest3 running");

	    //	    HelpClass c = new HelpClass();

		init();

	}

	@Override
	public void init() {
	    VM.println("PluginCreationTest3 initializing 3...");

		steering = new PluginPPort(this, "steeringAngle");

	    VM.println("steering");

		steering.write(-50);

		VM.println("and done!");
	}

	public static void main(String[] args) {
	    VM.println("In PluginCreationTest3.main()");
	    for (int i = 0; i < args.length; i++) {
		VM.println("arg " + args[i]);
	    }
		PluginCreationTest3 instance = new PluginCreationTest3(args);

	    VM.println("instance created");
		instance.run();
	}
}
