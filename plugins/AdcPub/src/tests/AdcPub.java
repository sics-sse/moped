package tests;

import com.sun.squawk.VM;
import java.lang.Math;
import sics.plugin.PlugInComponent;
import sics.port.PluginPPort;
import sics.port.PluginRPort;
import sics.PIRTE;

public class AdcPub extends PlugInComponent {
	private PluginPPort ap;
	public PluginRPort adc;
	
	public AdcPub(String[] args) {
		super(args);
	}
	
	public static void main(String[] args) {
		VM.println("ADCPub.main()\r\n");
		AdcPub adcPub = new AdcPub(args);
		adcPub.init();
		adcPub.doFunction();
		VM.println("ADCPub-main done\r\n");
	}

	public void init() {
		// Initiate PluginPPort
		ap = new PluginPPort(this, "ap");
		adc = new PluginRPort(this, "adc");
	}
	
	public void run() {}
	
	public void doFunction() {
		for(int i=0;i<100;i++) {
			VM.println("[AdcPub is running]");
			
			String adcStr = adc.readString();
			String pubData = "adc|" + adcStr;
			ap.write(pubData);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}
		}
	}

}