//package tests;
//
//import java.io.IOException;
//import java.io.DataOutputStream;
//import javax.microedition.io.Connector;
//import java.io.DataInputStream;
//import com.sun.squawk.*;
//
//
//class App {
//    public static void main (String[] args) throws Exception {
//        System.out.println("started App");
//        Isolate.currentIsolate().hibernate();
//        System.out.println("re-awoke App");
//    }
//}
//
//public class Test1 {
//
//    public static void main (String[] args) throws IOException {
//        if (args.length > 0) {
//            String name = args[0];
//            System.err.println("About to restart " + name + ".isolate"/* + " after " + VM.branchCount() + " branches"*/);
//
//            Isolate isolate = null;
//            String url = "file://" + name + ".isolate";
//
//            do {
//                DataInputStream dis = Connector.openDataInputStream(url);
//                isolate = Isolate.load(dis, url);
//                dis.close();
//
//                isolate.unhibernate();
//                isolate.join();
//                url = save(isolate);
//            } while (isolate.isHibernated());
//        }
//
//        System.out.println("started Test1");
//
//        if (args.length == 0) {
//            String cp = Isolate.currentIsolate().getClassPath();
//            Isolate isolate = new Isolate("tests.App", new String[0], cp, null);
//            isolate.start();
//            isolate.join();
//
//            save(isolate);
//        }
//    }
//
//    private static String saveIsolate(Isolate isolate) throws IOException {
//        String url = "file://" + isolate.getMainClassName() + "." + System.currentTimeMillis() + ".isolate";
//        DataOutputStream dos = Connector.openDataOutputStream(url);
//        isolate.save(dos, url);
//        return url;
//    }
//
//
//    private static String save(Isolate isolate) {
//        if (isolate.isHibernated()) {
//            try {
//                String url = saveIsolate(isolate);
//                System.out.println("saved isolate tests.Test1 to " + url);
//                return url;
//            } catch (java.io.IOException ioe) {
//                System.err.println("I/O error while trying to save isolate: ");
//                ioe.printStackTrace();
//            }
//        }
//        return null;
//    }
//
//}
