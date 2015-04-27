package plugins;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;

public class AdcPub extends PlugInComponent {
	private PluginPPort ap;
	public PluginRPort adc;
	
	public AdcPub(String[] args) {
		super(args);
		System.out.println("AdcPub constructor 1");
	}
	
	public AdcPub() {
		System.out.println("AdcPub constructor 0");
	}
	
	public static void main(String[] args) {
		VM.println("ADCPub.main()\r\n");
		AdcPub adcPub = new AdcPub(args);
		adcPub.run();
		VM.println("ADCPub-main done\r\n");
	}

	public void init() {
		// Initiate PluginPPort
		ap = new PluginPPort(this, "ap");
		System.out.println("AdcPub init 2");
		adc = new PluginRPort(this, "adc");
	}
	
	public void run() {
	    System.out.println("AdcPub run");
	    init();
	    System.out.println("AdcPub run 2");
	    doFunction();
	}
	
	public void doFunction() {
		for(int i=0;i<100;i++) {
		    //VM.println("[AdcPub is running]");
			
			String adcStr = adc.readString();
			String pubData = "adc|" + adcStr;
			System.out.println(pubData);
			ap.write(pubData);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			    //	VM.println("Interrupted.\r\n");
			}
		}
	}

}
