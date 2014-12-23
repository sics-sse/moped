/*
 * Copyright 2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * This is a part of the Squawk JVM.
 */
package tests;

import java.io.*;
import javax.microedition.io.*;
import com.sun.squawk.*;

/**
 * Tests multicasting of System.out.
 *
 * @author  Doug Simon
 */
public class SystemStreams {

    public static void main(String[] args) throws IOException, InterruptedException {
        Parent.main(args);
    }

    public static void cat(String path) throws IOException {
/*
        VM.println();
        VM.println("contents of " + path + ":");
        Reader r = new InputStreamReader(Connector.openInputStream("file://" + path));
        int c = r.read();

        while (c != -1) {
            VM.print((char)c);
            c = r.read();
        }
        r.close();
*/
    }
}

class Parent {

    public static void main(String[] args) throws IOException, InterruptedException {
/*
        String cp = VM.getCurrentIsolate().getClassPath();
        Isolate child = new Isolate("tests.Child", new String[] {}, cp, null);

        String urls = "debug:;file://parent1.log;file://parent2.log";
        child.setProperty("java.lang.System.out", "multicastoutput:" + urls);
        VM.println();
        VM.println("Parent: initialized System.out of child to \"" + urls + "\"");

        child.addOut("file://parent4.log");

        child.start();

        Thread.sleep(5000);

        child.addOut("file://parent3.log");
        VM.println();
        VM.println("Parent: added \"file://parent3.log\" to System.out of child");

        Thread.sleep(5000);
        child.removeOut("file://parent3.log");
        VM.println();
        VM.println("Parent: removed \"file://parent3.log\" from System.out of child");
        VM.println("Parent: attempting to add \"socket://localhost:9999\" to System.out of child");
        child.addOut("socket://localhost:9999");

        child.join();

        SystemStreams.cat("parent1.log");
        SystemStreams.cat("parent2.log");
        SystemStreams.cat("parent3.log");
*/
    }

}

class Child {

    public static void main(String[] args) throws IOException, InterruptedException {
  /*
        for (int i = 0; i != 15; ++i) {
            String now = "" + i + " ";
            System.out.print(now);
            Thread.sleep(1000);
            if (i == 10) {
                Isolate isolate = Isolate.currentIsolate();
                isolate.clearOut();
                isolate.addOut("file://child.log");
                VM.println();
                VM.println("Child: reset System.out to \"file://child.log\"");

            }
        }

        VM.println();
        VM.println();
        VM.println();

        SystemStreams.cat("child.log");
  */
    }
}
