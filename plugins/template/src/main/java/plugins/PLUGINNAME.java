package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class PLUGINNAME extends PlugInComponent {
    //private PluginPPort fs;
    //private PluginRPort ff;
	
    public PLUGINNAME() {}
	
    public PLUGINNAME(String[] args) {
	super(args);
    }
	
    public static void main(String[] args) {
	PLUGINNAME plugin = new PLUGINNAME(args);
	plugin.run();
    }

    public void init() {
	//fs = new PluginPPort(this, "fs");
	//ff = new PluginRPort(this, "ff");
    }
	
    public void doFunction() throws InterruptedException {
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
}
