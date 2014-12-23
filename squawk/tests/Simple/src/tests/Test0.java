package tests;

import java.io.*;
import javax.microedition.io.*;
import com.sun.squawk.*;
import com.sun.squawk.microedition.io.FileConnection;

public class Test0 {

    static int writingThreads = 0;

    private static synchronized int incCount() {
        int result = writingThreads;
        writingThreads++;
        return result;
    }

    public static void main (String[] args) {
        System.out.println("Dynamically loaded Hello World 0!");
/*if[FLOATS]*/
        float f = 4321.5678F;
        double d = 6789.4321D;
/*end[FLOATS]*/

        // Start some threads
        for (int i = 0; i != 3; ++i) {
            final int id = i;
            new Thread() {
                  public void run() {
//                      bench.cubes.Main.main(null);
                    try {
                        System.out.println("Writing thread " + id);
                        System.out.flush();
                        FileConnection f = (FileConnection)Connector.open("file://" + id + ".data", Connector.WRITE);
                        f.create();
                        incCount();
                        DataOutputStream os = f.openDataOutputStream();
                        
                        for (int j = 0; j != 10000; ++j) {
                            os.writeInt(j);
                            os.writeUTF(" ");
                        }
                        os.close();f.close();

                        System.out.println("Done writing thread " + id);
                        System.out.flush();
                        
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }
                  }
            }.start();
        }

        while (writingThreads != 3) {
            System.out.println("writing threads: " + writingThreads);
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
            }
        }

        int wakeCount = 0;
        while (Thread.activeCount() > 1) {

            try {
                System.out.println("Hibernating " + Isolate.currentIsolate());
                Isolate.currentIsolate().hibernate();
                System.out.println("Reawoke " + Isolate.currentIsolate() + ": " + (++wakeCount));
                if (wakeCount > 10) {
                    break;
                }
            }
            catch (IOException ex) {
                System.err.println("Error hibernating isolate: " + ex);
//                ex.printStackTrace();
            }
        }
    }
}
