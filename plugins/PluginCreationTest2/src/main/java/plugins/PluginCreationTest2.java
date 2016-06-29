package plugins;

import com.sun.squawk.VM;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;

public class PluginCreationTest2 extends PlugInComponent {

	public PluginPPort steering;

	public PluginCreationTest2(String[] args) {
		super(args);
		VM.println("ran constructor(args)");
	}
	
	public PluginCreationTest2() {
		super();
		VM.println("ran constructor()");
	}
	
	@Override
	public void run() {
	    init();
	}
	
	@Override
	public void init() {
	    VM.println("PluginCreationTest2 initializing 3...");

		steering = new PluginPPort(this, "steeringAngle");

	    VM.println("steering");

		steering.write(50);

		VM.println("and done!");
	}

	public static void main(String[] args) {
	    VM.println("In PluginCreationTest2.main()");
	    for (int i = 0; i < args.length; i++) {
		VM.println("arg " + args[i]);
	    }
		PluginCreationTest2 instance = new PluginCreationTest2(args);

	    VM.println("instance created");
		instance.run();
	}
}
