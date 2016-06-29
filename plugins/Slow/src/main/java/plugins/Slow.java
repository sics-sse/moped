package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class Slow extends PlugInComponent {
    public PluginPPort speed;
    public PluginPPort steering, pub;
    public PluginRPort bw, fw;
	
    public Slow(String[] args) {
	super(args);
    }
	
    public Slow() {
    }
	
    public static void main(String[] args) {
	VM.println("Slow.main()\r\n");
	Slow ap = new Slow(args);
	ap.run();
	VM.println("Slow-main done");
    }

    public void init() {
	// Initiate PluginPPort
	VM.println("init 1");
	speed = new PluginPPort(this, "sp");
	bw = new PluginRPort(this, "bw");
	fw = new PluginRPort(this, "fw");
	VM.println("init 2");
	steering = new PluginPPort(this, "st");
	pub = new PluginPPort(this, "pub");
	VM.println("init 3");
    }
	
    public void doFunction() throws InterruptedException {
	int sp = 1;
	int st1 = 100;
	int bwspeed, fwspeed;
	int cnt = 0;

	VM.println("circle 1");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(st1);

	VM.println("circle 2");
	Thread.sleep(2000);
	speed.write(0);
	steering.write(st1);
	Thread.sleep(2000);
		
	sp = 7;

	while (true) {
	    cnt++;
	    VM.println("1");
	    speed.write(sp);
	    steering.write(st1);

	    if (sp == 6)
		sp = 7;

	    bwspeed = bw.readInt();
	    fwspeed = fw.readInt();
	    pub.write("Slow|" + cnt + " " + sp + " " + bwspeed + " " + fwspeed);

	    if (false) {
		if (bwspeed == 0)
		    sp += 1;
		else
		    sp -= 1;
	    
		if (sp < 0)
		    sp = 0;
		if (sp > 100)
		    sp = 100;
	    }

	    if (false) {
		if (bwspeed == 24)
		    sp = 6;
	    }

	    Thread.sleep(2000);

	}
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
