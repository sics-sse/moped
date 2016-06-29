package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class AllSCU extends PlugInComponent {
    private PluginPPort fs;
    private PluginRPort ff, imu;
	
    public AllSCU(String[] args) {
	super(args);
    }
	
    public static void main(String[] args) {
	VM.println("AllSCU.main()");
	AllSCU publish = new AllSCU(args);
	publish.run();
	VM.println("AllSCU-main done");
    }

    @Override
	public void init() {
	// Initiate PluginPPort
	fs = new PluginPPort(this, "fs");

	ff = new PluginRPort(this, "ff");
	imu = new PluginRPort(this, "imu");
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
	int cnt = 0;
	VM.println("[AllSCU is running]");
	while (true) {
	    cnt += 1;
	    int val;
	    String val1, val2;

	    java.util.Date date = new java.util.Date();
	    val1 = "" + date.getTime();

	    // Right now, we get three compass values, then three gyro values.
	    val2 = imu.readString();

	    val = ff.readInt();

	    if (false) {
		val = 7;
		int l = (cnt%3)*3 + 10;
		byte arr[] = new byte[l];
		for (int i = 0; i < l; i++) {
		    arr[i] = (byte) (97+(cnt%4)*4+((i+cnt)%4));
		}
		val2 = new String(arr);
	    }

	    data = val1 + " " + val2 + " " + val;
	    fs.send(data);

	    VM.println(data);
	    //data = "AllSCU| (" + cnt + ")" + data;
	    //VM.println(data);

	    Thread.sleep(200);
	}
    }
}
