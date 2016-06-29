package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class Square extends PlugInComponent {
	private PluginPPort fs;
	private PluginRPort ff;
	
	public Square(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("Square.main()");
		Square publish = new Square(args);
		publish.init();
		publish.doFunction();
		VM.println("Square-main done");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		fs = new PluginPPort(this, "fs");
		ff = new PluginRPort(this, "ff");
	}

	public void run() {}
	
	public void doFunction() {
		String data;
		for (int i = 0; i < 1000; i++) {
			VM.println("[Square X is running]");
			int val = ff.readInt();
			data = "Square|" + val;
			VM.println(data);
			fs.write(data);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.");
			}
		}
	}
	
}
