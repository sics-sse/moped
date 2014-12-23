package org.sunspotworld;

//import com.sun.spot.sensorboard.EDemoBoard;
//import com.sun.spot.sensorboard.peripheral.ITriColorLED;
//import com.sun.spot.sensorboard.peripheral.LEDColor;
//import com.sun.spot.util.*;
import com.sun.squawk.Isolate;
import com.sun.squawk.VM;
import java.util.Hashtable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class IsolateMemoryTest extends MIDlet {

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }

    private void launchIso(int x, Hashtable t, String uri) {
        Isolate isolate = new Isolate(t, "org.sunspotworld.TestIsolate", new String[]{String.valueOf(x)}, null, uri);
            isolate.start();
            isolate.join();
            try {
                Thread.sleep(1000);

            } catch (InterruptedException interruptedException) {
            }
            isolate = null;
    }
    
    protected void startApp() throws MIDletStateChangeException {
        int x = 0;
        VM.Stats.initHeapStats();
        
        while (true) {
            String uri = VM.getCurrentIsolate().getParentSuiteSourceURI();
            Hashtable t = new Hashtable();
            t.put("spot.diagnostics", "true");
            Object sentinal = new Object(); // keep heap stats on objects younger than this one.
            
            launchIso(x, t, uri);

            // Utils.sleep(1000);
            System.out.println("Live&Dead objects created by isolate start:");
            VM.Stats.printHeapStats(sentinal, true);
            
            Runtime.getRuntime().gc();
            System.out.print("RAM free after isolate exit: ");
            System.out.println(Runtime.getRuntime().freeMemory());
            System.out.println("Live objects created by isolate start and remaining after isolate stop:");
            VM.Stats.printHeapStats(sentinal, true);
            
            x++;
            
            if (x % 10 == 0) {
                Isolate[] isos = Isolate.getIsolates();
                for (int i = 0; i < isos.length; i++) {
                    System.out.println(" Iso " + i + " = " + isos[i]);
                }
                isos = null;
            }
        }

    }
}
