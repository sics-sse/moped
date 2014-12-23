    package tests;


    public class TestSync {
        
        final int LOOPCOUNT = 500000;
        
        Object lock = new Object();
        
        boolean dummyMessageSent = false;

        void testUncontendedLock() {
            lock = new Object();
            int dummy = 0;
            final int end = LOOPCOUNT;
            for (int i = 0; i < end; i++) {
                synchronized (lock) {
                    dummy++;
                }
            }

        }

        /** Note that this version does NOT fit in the fastpath cache */
        void testDeepNestedUncontendedLock() {
            lock = new Object();
            int dummy = 0;
            final int DEPTH = 10;
            final int end = LOOPCOUNT / DEPTH;
            for (int i = 0; i < end; i++) {
                synchronized (lock) {
                    dummy++;
                    synchronized (lock) {
                        dummy++;
                        synchronized (lock) {
                            dummy++;
                            synchronized (lock) {
                                dummy++;
                                synchronized (lock) {
                                    dummy++;
                                    synchronized (lock) {
                                        dummy++;
                                        synchronized (lock) {
                                            dummy++;
                                            synchronized (lock) {
                                                dummy++;
                                                synchronized (lock) {
                                                    dummy++;
                                                    synchronized (lock) {
                                                        dummy++;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        /** Note that this version does fit in the fastpath cache */
        void testNestedUncontendedLock() {
            lock = new Object();
            int dummy = 0;
            final int DEPTH = 6;
            final int end = LOOPCOUNT / DEPTH;
            for (int i = 0; i < end; i++) {
                synchronized (lock) {
                    dummy++;
                    synchronized (lock) {
                        dummy++;
                        synchronized (lock) {
                            dummy++;
                            synchronized (lock) {
                                dummy++;
                                synchronized (lock) {
                                    dummy++;
                                    synchronized (lock) {
                                        dummy++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        void testUncontendedNotify() {
            lock = new Object();
            int dummy = 0;
            for (int i = 0; i < LOOPCOUNT; i++) {
                synchronized (lock) {
                    dummy++;
                    lock.notify();
                }
            }
        }

        void testUncontendedNotifyAll() {
            lock = new Object();
            int dummy = 0;
            for (int i = 0; i < LOOPCOUNT; i++) {
                synchronized (lock) {
                    dummy++;
                    lock.notifyAll();
                }
            }
        }

        void testYield() {
            int dummy = 0;
            for (int i = 0; i < LOOPCOUNT; i++) {
                Thread.yield();
            }
        }

        void testWaitNotifyProducer(boolean timeout) {
            final int end = LOOPCOUNT / 4;
            for (int i = 0; i < end; i++) {
                synchronized (lock) {
                    while (dummyMessageSent) {
                        try {
                            if (timeout) {
                                lock.wait(10);
                            } else {
                                lock.wait();
                            }
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    dummyMessageSent = true;
                    lock.notifyAll();
                }
            }
        }

        void testWaitNotifyConsumer(boolean timeout) {
            final int end = LOOPCOUNT / 4;
            for (int i = 0; i < end; i++) {
                synchronized (lock) {
                    try {
                        while (!dummyMessageSent) {
                            if (timeout) {
                                lock.wait(10);
                            } else {
                                lock.wait();
                            }
                        }
                        dummyMessageSent = false;
                        lock.notifyAll();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        void testWaitNotify(final boolean timeout, int producerPriority, int consumerPriority) {
            lock = new Object();
            dummyMessageSent = false;
            
            Thread producer = new Thread(new Runnable() {
                public void run() {
                    testWaitNotifyProducer(timeout);
                }
            });

            Thread consumer = new Thread(new Runnable() {
                public void run() {
                    testWaitNotifyConsumer(timeout);
                }
            });
            producer.setPriority(producerPriority);
            consumer.setPriority(consumerPriority);
            
            producer.start();
            consumer.start();
            try {
                producer.join();
                consumer.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        private static int dummyCounter;
        
        void testContendedLock(final int threadCount, final int holdTime) {
            lock = new Object();
            Thread[] threads = new Thread[threadCount];
            
            for (int t = 0; t < threadCount; t++) {
                threads[t] = new Thread(new Runnable() {
                    public void run() {
                        final int end = LOOPCOUNT / 4;
                        for (int i = 0; i < end; i++) {
                            synchronized (lock) {
                                for (int h = 0; h < holdTime; h++) {
                                    dummyCounter++;
                                }
                            }
                        }
                    }
                });
            }

            for (int t = 0; t < threadCount; t++) {
                threads[t].start();
            }
            
            try {
                for (int t = 0; t < threadCount; t++) {
                    threads[t].join();
                }
                
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        void testWaitTimeout() {
            final int timeout = 10;
            final int end = LOOPCOUNT / (timeout * 1000);
            for (int i = 0; i < end; i++) {
                synchronized (lock) {
                    try {
                        lock.wait(timeout);
                    } catch(InterruptedException ex) {
                        ///ex.printStackTrace();
                    }
                }
            }
        }

        void runAllOnce() {
            long time = 0;
            System.out.println("------ TEST SET --------");
            System.gc();

            time = System.currentTimeMillis();
            testUncontendedLock();
            time = System.currentTimeMillis() - time;
            System.out.println("testUncontendedLock: " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testNestedUncontendedLock();
            time = System.currentTimeMillis() - time;
            System.out.println("testNestedUncontendedLock: " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testDeepNestedUncontendedLock();
            time = System.currentTimeMillis() - time;
            System.out.println("testDeepNestedUncontendedLock: " + time + "ms");
            System.gc();
          
            time = System.currentTimeMillis();
            testContendedLock(1, 10);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(1, 10): " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testContendedLock(5, 10);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(5, 10): " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testContendedLock(10, 10);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(10, 10): " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testContendedLock(50, 10);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(50, 10): " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testContendedLock(1, 100);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(1, 100): " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testContendedLock(5, 100);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(5, 100): " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testContendedLock(10, 100);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(10, 100): " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testContendedLock(50, 100);
            time = System.currentTimeMillis() - time;
            System.out.println("testContendedLock(50, 100): " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testUncontendedNotify();
            time = System.currentTimeMillis() - time;
            System.out.println("testUncontendedNotify: " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testUncontendedNotifyAll();
            time = System.currentTimeMillis() - time;
            System.out.println("testUncontendedNotifyAll: " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testYield();
            time = System.currentTimeMillis() - time;
            System.out.println("testYield: " + time + "ms");
            System.gc();

            time = System.currentTimeMillis();
            testWaitNotify(false, Thread.NORM_PRIORITY, Thread.NORM_PRIORITY);
            time = System.currentTimeMillis() - time;
            System.out.println("testWaitNotify: " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testWaitNotify(true, Thread.NORM_PRIORITY, Thread.NORM_PRIORITY);
            time = System.currentTimeMillis() - time;
            System.out.println("testWaitNotify - with timeout: " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testWaitNotify(false, Thread.MAX_PRIORITY, Thread.MIN_PRIORITY);
            time = System.currentTimeMillis() - time;
            System.out.println("testWaitNotify - with mixed priorities: " + time + "ms");
            System.gc();
            
            time = System.currentTimeMillis();
            testWaitTimeout();
            time = System.currentTimeMillis() - time;
            System.out.println("testWaitTimeout: " + time + "ms");
            System.gc();
        }

        public static void main(String[] args) {
            TestSync test = new TestSync();

            for (int i = 0 ; i < 4; i++) {
                test.runAllOnce();
            }
        }
    }