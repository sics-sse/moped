package org.sunspotworld;

import java.util.Enumeration;
import com.sun.squawk.Isolate;

class TestIsolate {

//   private static ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();
    public TestIsolate() {
    }

    public static void main(String[] args) {
        try {
            if (args[0] != null) {
                System.out.println("Isolate: " + args[0]);
            }
            
            Enumeration e = Isolate.currentIsolate().getProperties();
            System.out.println("Isolate Properties:");
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                System.out.print("Property ");
                System.out.print(key);
                System.out.print(" = ");
                System.out.println(Isolate.currentIsolate().getProperty(key));
            }
            e = null;

            Runtime.getRuntime().gc();
            System.out.print("RAM free after isolate start: ");
            System.out.println(Runtime.getRuntime().freeMemory());

            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
