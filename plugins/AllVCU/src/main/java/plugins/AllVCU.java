package plugins;

import java.io.IOException;
import com.sun.squawk.VM;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.plugin.PlugInComponent;

public class AllVCU extends PlugInComponent {
    private PluginPPort pub, sp, st, led;
    private PluginRPort ab, w1, w2, adc, sub;
	
    public AllVCU(String[] args) {
	super(args);
    }
	
    public static void main(String[] args) {
	VM.println("AllVCU.main()");
	AllVCU publish = new AllVCU(args);
	publish.run();
	VM.println("AllVCU-main done");
    }

    @Override
	public void init() {
	// Initiate PluginPPort
	pub = new PluginPPort(this, "pub");
	sp = new PluginPPort(this, "sp");
	st = new PluginPPort(this, "st");
	led = new PluginPPort(this, "led");

	w1 = new PluginRPort(this, "w1");
	w2 = new PluginRPort(this, "w2");
	adc = new PluginRPort(this, "adc");
	sub = new PluginRPort(this, "sub");
	ab = new PluginRPort(this, "ab");
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
	int bump = 0;
	VM.println("[AllVCU is running]");
	while (true) {
	    cnt += 1;
	    int val, val3, val4;
	    String val1, val2;

	    // Add: LED blinking, or: red for north, left for west, right for
	    // east

	    if (bump > 0) {
		led.write("1|0");
		led.write("2|0");
		led.write("3|0");
		bump--;
	    } else {
		led.write("1|1");
		led.write("2|1");
		led.write("3|1");
	    }

	    java.util.Date date = new java.util.Date();
	    val1 = "" + date.getTime();


	    //st.write((cnt%21-10)*10);
	    // to make it reverse, we need to set it to zero more often
	    //sp.write((cnt%21-10)*5);

	    val2 = adc.readString();

	    // w1 = front wheel
	    val3 = w1.readInt();
	    val4 = w2.readInt();

	    Object o5 = sub.receive();
	    String val5 = "";
	    if (o5 != null) {
		val5 = (String) o5;

	    }

	    Object o6 = ab.receive();
	    String val6 = "";
	    if (o6 != null) {
		val6 = (String) o6;

		int p1 = val6.indexOf(",");
		int p2 = val6.indexOf(",", p1+1);
		int p3 = val6.indexOf(",", p2+1);
		int p4 = val6.indexOf(",", p3+1);

		//String sarr[] = val5.split(",");
		//val6 = val6 + "(" + p1 + "," + p2 + "," + p3 + ")";
		val6 = val6 + "(" + val6.substring(p3+1,p4) + ")";

		int d = Integer.parseInt(val6.substring(p3+1,p4));
		if (d > 400 || d < -400) {
		    bump = 2;
		}
	    }

	    data = val1 + " " + val2 + " " + val3 + " " + val4 + " " + val5 +
		" " + val6 + "//";
	    VM.println(data);
	    data = "AllVCU| (" + cnt + ")" + data;
	    VM.println(data);
	    pub.write(data);
	    Thread.sleep(1000);
	}
    }
}
