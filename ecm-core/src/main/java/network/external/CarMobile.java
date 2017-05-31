package network.external;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import messages.PWMMessage;
import ecm.Ecm;

public class CarMobile implements Runnable {
	private int pwmEcuId;
	private Ecm ecm;
	
	private static final int PORT = 9001;
	private List<Socket> mList = new ArrayList<Socket>();
	private ExecutorService mExecutorService = null; // thread pool

	public CarMobile() {
	}

	public Ecm getEcm() {
		return ecm;
	}

	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}

	@Override
	public void run() {
		ServerSocket server;
		
		try {
			server = new ServerSocket(PORT);
			mExecutorService = Executors.newCachedThreadPool(); // create a
																// thread
			// pool
			System.out.println("CarMobile server start (on ECM) ...");
			Socket client = null;
			while (true) {
				System.out.println("CarMobile: listening");
				client = server.accept();
				mList.add(client);
				System.out.println("CarMobile: starting new service");
				mExecutorService.execute(new Service(client)); // start a new
																// thread
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	class Service implements Runnable {
		private InputStream in = null;
		private OutputStream out = null;

		public Service(Socket socket) {
			try {
				in = socket.getInputStream();
				out = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		public void run() {
			/* We only want to read the last 11 bytes from the input stream (5 for each bar + EOF) */
			String outs;
			
			try {
				while (true) {
				    if (ecm.crashbytes != null) {
					out.write(ecm.crashbytes);
				    }					

				    Thread.sleep(1000);
				}
			} catch (Exception e) {
				System.out.println("Connection between cellphone and ECM was terminated");
				e.printStackTrace();
			}
		}
	}	
}
