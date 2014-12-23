/*
 * ThreadOverhead.java
 *
 * Created on June 12, 2007, 12:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

/**
 *
 * @author dw29446
 */
public class ThreadOverhead {
    final static int THREADS = 100;
    
    /** Creates a new instance of ThreadOverhead */
    public ThreadOverhead() {
    }
    
    static class TestThread extends Thread {
        public TestThread() {
            super("test thread");
        }
        
        public void run() {
            // empty
        }
    }
    
    static void test() {
        Thread[] threads = new Thread[THREADS];
        long[] memStart = new long[THREADS];
        long[] memEnd = new long[THREADS];
        long[] memAfter = new long[THREADS];
        long[] time = new long[THREADS];
        long totalTime = 0;
        Runtime runtime = Runtime.getRuntime();
        
        // measure overhead of thread object:
        runtime.gc();
        runtime.gc();
        runtime.gc();
        runtime.gc();
        for (int i = 0; i < threads.length; i++) {
            memStart[i] = runtime.freeMemory();
            long start = System.currentTimeMillis();
            threads[i] = new TestThread();
            time[i] = System.currentTimeMillis() - start;
            totalTime += time[i];
            memEnd[i] = runtime.freeMemory();
            runtime.gc();
            memAfter[i] = runtime.freeMemory();
        }
        
        for (int i = 0; i < threads.length; i++) {
            long liveSize = memStart[i] - memAfter[i];
            long totalSize = memStart[i] - memEnd[i];
            long garbageSize = totalSize - liveSize;
            System.out.println("Alloc of Thread uses " + liveSize + " of live data and " + garbageSize + " of garbage. Took " + time[i] + "ms.");
        }
        System.out.println("Avg time per thread " + (totalTime / (double)THREADS) + "ms.");
        
        // measure overhead of thread start:
        runtime.gc();
        runtime.gc();
        runtime.gc();
        runtime.gc();
        totalTime = 0;
        for (int i = 0; i < threads.length; i++) {
            memStart[i] = runtime.freeMemory();
            long start = System.currentTimeMillis();
            threads[i].start();
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            time[i] = System.currentTimeMillis() - start;
            totalTime += time[i];
            memEnd[i] = runtime.freeMemory();
            runtime.gc();
            memAfter[i] = runtime.freeMemory();
        }
        
        for (int i = 0; i < threads.length; i++) {
            long liveSize = memStart[i] - memAfter[i];
            long totalSize = memStart[i] - memEnd[i];
            long garbageSize = totalSize - liveSize;
            System.out.println("Run of Thread uses " + liveSize + " of live data and " + garbageSize + " of garbage. Took " + time[i] + "ms.");
        }
       System.out.println("Avg time per thread " + (totalTime / (double)THREADS) + "ms.");
                
        // measure T of thread object:
        runtime.gc();
        runtime.gc();
        runtime.gc();
        runtime.gc();
        totalTime = System.currentTimeMillis();
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new TestThread();
        }
        totalTime = System.currentTimeMillis() - totalTime;
        System.out.println("Avg time to alloc thread " + (totalTime / (double)THREADS) + "ms.");
        
        // measure overhead of thread start:
        runtime.gc();
        runtime.gc();
        runtime.gc();
        runtime.gc();
        totalTime = System.currentTimeMillis();
        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        totalTime = System.currentTimeMillis() - totalTime;
        System.out.println("Avg time to run thread " + (totalTime / (double)THREADS) + "ms.");
        
    }
    
    public static void main(String[] args) {
        test();
        
    }
    
}
