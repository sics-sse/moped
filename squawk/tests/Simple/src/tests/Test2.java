package tests;

import com.sun.squawk.*;

public class Test2 {

    public static void main (String[] args) throws java.io.IOException {
        System.out.println("Dynamically loaded Hello World 2!");

        String cp = Isolate.currentIsolate().getClassPath();
        Isolate isolate = new Isolate("tests.Test1", new String[0], cp, null);
        isolate.start();
        isolate.join();

    }
}


