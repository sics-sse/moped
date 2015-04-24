/*
 * TestAlloc.java
 *
 * Created on June 20, 2006, 5:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

/**
 *
 * @author dw29446
 */
public class TestAlloc {

        final int LOOPCOUNT = 1000000;
        
        void testAllocObject() {
            final int end = LOOPCOUNT;
            for (int i = 0; i < end; i++) {
                Object tmp = new Object();
            }
        }

        void testAllocSmallArray() {
            final int len = 10;
            final int end = (LOOPCOUNT / len) * 4;
            for (int i = 0; i < end; i++) {
                Object tmp = new byte[len];
            }
        }

        void testAllocLargeArray() {
            final int len = 256;
            final int end = (LOOPCOUNT / len) * 4;
            for (int i = 0; i < end; i++) {
                Object tmp = new byte[len];
            }
        }

        void runAllOnce() {
            long time = 0;
            System.out.println("------ TEST SET --------");
            System.gc();

            time = System.currentTimeMillis();
            testAllocObject();
            time = System.currentTimeMillis() - time;
            System.out.println("testAllocObject:     " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testAllocSmallArray();
            time = System.currentTimeMillis() - time;
            System.out.println("testAllocSmallArray: " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testAllocLargeArray();
            time = System.currentTimeMillis() - time;
            System.out.println("testAllocLargeArray: " + time + "ms");
            System.gc();
        }

        public static void main(String[] args) {
            TestAlloc test = new TestAlloc();

            for (int i = 0 ; i < 4; i++) {
                test.runAllOnce();
            }
        }
}
