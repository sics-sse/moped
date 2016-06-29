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
	init();
	try {
	    doFunction();
	} catch (InterruptedException e) {
	    VM.println("**************** Interrupted.");
	    return;
	}
    }
	
    @Override
	public void init() {
	VM.println(msg + "ehej2");
    }

    public static void main(String[] args) {
	PluginCreationTest plugin = new PluginCreationTest(args);
	initmsg();
	VM.println(msg + "ehej3");
	plugin.run();
    }

    private void doFunction() throws InterruptedException {
    	Calendar cal = Calendar.getInstance();
    	String msg1 = msg + " " + cal.getTime() + " ";
    	//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	//msg = msg + sdf.format(cal.getTime()) + " ";
	
	while (true) {
	    VM.println(msg1 + "ehej4");
	    Thread.sleep(5000);
	}
    }
}
