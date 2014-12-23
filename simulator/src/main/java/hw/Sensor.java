package hw;

public abstract class Sensor implements BaseEUnit, Runnable {
	private final Object lock = new Object();

	public void wakeup() {
		synchronized (lock) {
			lock.notify();
		}
	}

	public void await() {
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
