package plugins;

//import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.sun.squawk.VM;
import sics.plugin.PlugInComponent;

public class PluginCreationTest extends PlugInComponent {

    static String msg;

    private static void initmsg() {
    	Calendar cal = Calendar.getInstance();
	msg = "test26 " + cal.getTime() + " ";
    }

    public PluginCreationTest() {
	VM.println("PluginCreationTest()");
    }

    public PluginCreationTest(String [] args) {
	super(args);
	VM.println("PluginCreationTest(args) 2");
    }

    @Override
	public void run() {
	initmsg();
	VM.println(msg + "ehej1");
	init();
    }

    @Override
	public void init() {
	VM.println(msg + "ehej2");
	doFunction();
    }

    public static void main(String[] args) {
	initmsg();
	VM.println(msg + "ehej3");
	doFunction();
    }

    private static void doFunction() {
    	Calendar cal = Calendar.getInstance();
    	String msg1 = msg + " " + cal.getTime() + " ";
    	//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	//msg = msg + sdf.format(cal.getTime()) + " ";
	
	while (true) {
	    VM.println(msg1 + "ehej4");
	    try {
		Thread.sleep(5000);
	    } catch (Exception e) {
	    }
	}
    }
}
