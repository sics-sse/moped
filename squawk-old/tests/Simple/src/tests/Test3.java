package tests;

import com.sun.squawk.*;

/**
 * Tests peek/poke mechanism from non-image class.
 */
public class Test3 {

    static void peekIntArray(Object raw, int offset, int length) {
        int end = offset + length;
        for (int i = offset; i != end; ++i) {
            System.out.println("Unsafe.getInt(raw, " + i + ") = " + Unsafe.getInt(Address.fromObject(raw), i));
        }
    }

    public static void main(String[] args) {
        int[] arr = new int[2];

        peekIntArray(arr, 0, 2);

        arr[0] = 555;
        Unsafe.setInt(Address.fromObject(arr), 1, 666);
        peekIntArray(arr, 0, 2);

        try {
            Class.forName("tests.Test3_error");
            System.out.println("expected a LinkageError");
        } catch (ClassNotFoundException ex) {
            System.out.println("expected a LinkageError");
        } catch (Error le) {
            System.out.println("got expected LinkageError: " + le);
        }
    }

}

class Test3_error {
    static void f() {
        // Access a public native method that is not allowed
//        VM.isBigEndian();
    }
}
