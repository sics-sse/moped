package tests;

/**
 * This class runs some of the tests that still make sense.
 *
 * @author dw29446
 */
public class TestAuto {

    public static void main(String[] args)
            throws Exception {
        Test3.main(args);
        Test4.main(args);
        TestAlloc.main(args);

        TestArgs.main(new String[]{"-option1", "-option2", "argA"});
        //TestChannels.main(args); fails
        TestDouble.main(args);
        //TestExitHooks.main(args); excluded code
        TestInstantiationException.main(args);
        //TestIsolateLifecycleLocal.main(args);
        //TestMailboxes.main(args); excluded code
        //TestStackTrace.main(args); is trying to use dynamic classloading

        TestStackTraceIsolate.main(args);
        //TestSync.main(args); too slow!

        TestSystemProperties.main(args);
        TestSystemThreads.main(args);
        TestUnicode.main(args);
        TestUnsafe.main(args);
        ThreadOverhead.main(args);

        InvokeInterfaceBenchmark.main(args);
        ResourceTest.main(args);
    }

    private TestAuto() {
    }
}
