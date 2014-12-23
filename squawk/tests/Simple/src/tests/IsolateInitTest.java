package tests;

import com.sun.squawk.*;

public class IsolateInitTest {

    public static void main(String[] args) {
        String isFirst = args[0];
        VM.print("IsolateInitTest isFirst: ");
        VM.println(isFirst);
        if ("true".equals(isFirst)) {
            Isolate isolate = new Isolate(ResourceTest.class.getName(), new String[0], Isolate.currentIsolate().getClassPath(), null);
            isolate.start();
            isolate.join();
        }
    }
    
}
