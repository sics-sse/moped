package tests;

import com.sun.squawk.*;

/**
 * This test is used to debug the Lisp2Collector. It creates a bit of garbage,
 * runs the collector and then exits.
 */
public class Test4 {
    private String name;

    private static final String id = new String("123456");

    Test4() {
        name = new String("Hello from Test4");
        String garbage = new String("garbage");
    }

    public static void main(String[] args) throws Exception {
        // Run the collector
//        VM.println("Test4::main - run GC 1");
        System.gc();

        // Create an instance which also creates some garbage
//        VM.println("Test4::main - create Test4 instance");
        Test4 t4 = new Test4();

        // create an ObjectAssociation by getting the hashCode
//        VM.println("Test4::main - get hashcode");
        int hashCode = t4.hashCode();


        // Run the collector twice
//        VM.println("Test4::main - run GC 2");
        System.gc();
//        VM.println("Test4::main - run GC 3");
        System.gc();

        System.out.println(t4);

        Class.forName("tests.BigObject").newInstance();
//        VM.println("Test4::main - run GC 4");
        System.gc();

        Element bottom = new Element(1, null);
        Element top = bottom;
        try {
            while (true) {
                top = top.push();
            }
        } catch (OutOfMemoryError e) {
            top = null;
            System.gc();
        }
    }
}

class Element {
    int id;
    Element previous;
    Element(int id, Element previous) {
        this.id = id;
        this.previous = previous;
    }
    Element push() {
        return new Element(id + 1, this);
    }
}

/**
 * This class is guaranteed to have an array for it's oop map.
 */
class BigObject {
    Object object1 = new String("value 1");
    Object object2 = new String("value 2");
    Object object3 = new String("value 3");
    Object object4 = new String("value 4");
    Object object5 = new String("value 5");
    Object object6 = new String("value 6");
    Object object7 = new String("value 7");
    Object object8 = new String("value 8");
    Object object9 = new String("value 9");
    Object object10 = new String("value 10");
    Object object11 = new String("value 11");
    Object object12 = new String("value 12");
    Object object13 = new String("value 13");
    Object object14 = new String("value 14");
    Object object15 = new String("value 15");
    Object object16 = new String("value 16");
    Object object17 = new String("value 17");
    Object object18 = new String("value 18");
    Object object19 = new String("value 19");
    Object object20 = new String("value 20");
    Object object21 = new String("value 21");
    Object object22 = new String("value 22");
    Object object23 = new String("value 23");
    Object object24 = new String("value 24");
    Object object25 = new String("value 25");
    Object object26 = new String("value 26");
    Object object27 = new String("value 27");
    Object object28 = new String("value 28");
    Object object29 = new String("value 29");
    Object object30 = new String("value 30");
    Object object31 = new String("value 31");
    Object object32 = new String("value 32");
    Object object33 = new String("value 33");
    Object object34 = new String("value 34");
    Object object35 = new String("value 35");
    Object object36 = new String("value 36");
    Object object37 = new String("value 37");
    Object object38 = new String("value 38");
    Object object39 = new String("value 39");
    Object object40 = new String("value 40");
    Object object41 = new String("value 41");
    Object object42 = new String("value 42");
    Object object43 = new String("value 43");
    Object object44 = new String("value 44");
    Object object45 = new String("value 45");
    Object object46 = new String("value 46");
    Object object47 = new String("value 47");
    Object object48 = new String("value 48");
    Object object49 = new String("value 49");
    Object object50 = new String("value 50");
    Object object51 = new String("value 51");
    Object object52 = new String("value 52");
    Object object53 = new String("value 53");
    Object object54 = new String("value 54");
    Object object55 = new String("value 55");
    Object object56 = new String("value 56");
    Object object57 = new String("value 57");
    Object object58 = new String("value 58");
    Object object59 = new String("value 59");
    Object object60 = new String("value 60");
    Object object61 = new String("value 61");
    Object object62 = new String("value 62");
    Object object63 = new String("value 63");
    Object object64 = new String("value 64");
    Object object65 = new String("value 65");
}
