package tests;

/**
 * Tests speed of invokevirtual, invokestatic and invokeinterface.
 *
 * @author Doug Simon
 */
public class InvokeInterfaceBenchmark {


    public static void main(String[] args) {
        int repetitions = 10000;
        if (args.length > 0) {
            repetitions = Integer.parseInt(args[0]);
        }

        C c = new C();

        invokevirtual(repetitions, c);
        invokestatic(repetitions);
        invokeinterface(repetitions, (I)c);
    }

    static void invokeinterface(int repetitions, I iface) {
        long start = System.currentTimeMillis();

        // Manual loop unrolling amortizes cost of looping
        for (int i = 0; i != repetitions; ++i) {
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
            iface.i();
        }
        System.out.println("invokeinterface: time = " + (System.currentTimeMillis() - start));
    }

    static void invokevirtual(int repetitions, C c) {
        long start = System.currentTimeMillis();

        // Manual loop unrolling amortizes cost of looping
        for (int i = 0; i != repetitions; ++i) {
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
            c.m();
        }
        System.out.println("invokevirtual: time = " + (System.currentTimeMillis() - start));
    }

    static void invokestatic(int repetitions) {
        long start = System.currentTimeMillis();

        // Manual loop unrolling amortizes cost of looping
        for (int i = 0; i != repetitions; ++i) {
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
            C.s();
        }
        System.out.println("invokestatic: time = " + (System.currentTimeMillis() - start));
    }

    private InvokeInterfaceBenchmark() {
    }
}

interface I {
    void i();
}

class C implements I {
    public void i() {

    }
    public void m() {

    }
    public static void s() {

    }
}
