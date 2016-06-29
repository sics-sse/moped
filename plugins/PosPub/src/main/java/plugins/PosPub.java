package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class PosPub extends PlugInComponent {
	private PluginPPort fs;
	private PluginRPort ff;
	
	public PosPub(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("PosPub.main()\r\n");
		PosPub publish = new PosPub(args);
		publish.run();
		VM.println("PosPub-main done\r\n");
	}

	@Override
	public void init() {
		// Initiate PluginPPort
		fs = new PluginPPort(this, "fs");
		ff = new PluginRPort(this, "ff");
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
	
	public void doFunction() throws InterruptedException {
		String data;
		for (int i = 0; i < 1000; i++) {
			VM.println("[PosPub is running]");
			long rawPosVal = ff.readLong();
			short[] posVal = parsePosition(rawPosVal);
			String val = posVal[0] + "," + posVal[1] + "," + posVal[2] + "," + posVal[3] + "," + posVal[4];
			data = "PosPub|" + val;
			fs.write(data);
			Thread.sleep(2000);
		}
	}
	
	private short[] parsePosition(long rawPosVal) {
		short[] res = new short[5];
		res[0] = (short) (rawPosVal >> 48);
		res[1] = (short) ((rawPosVal >> 32) & 0x0000FFFF);
		res[2] = (short) ((rawPosVal >> 24) & 0x00000000000000FF);
		res[3] = (short) ((rawPosVal >> 16) & 0x00000000000000FF);
		res[4] = (short) (rawPosVal & 0x000000000000FFFF);
		return res;
	}
}
