package autosar;

import fresta.pirte.PIRTE;

public class SWCPPort<T> implements Runnable {
	private int id;
	private final Object lock = new Object();
	private T data;
	private PIRTE pirte;
	
	public SWCPPort(int id) {
		this.id = id;
	}
	
	public SWCPPort(PIRTE pirte) {
		this.pirte = pirte;
	}
	
	private void wakeup() {
		synchronized (lock) {
            lock.notify();
        }
	}
	
	private void await() throws InterruptedException {
		synchronized(lock) {
			lock.wait();
		}
	}

	public void run() {
		while(true) {
			try {
				await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// forward message
			if(pirte != null) {
				
			}
		}
	}
	
	public void updateData(T data) {
		this.data = data;
		wakeup();
	}
	
	public int getId() {
		return id;
	}
}
