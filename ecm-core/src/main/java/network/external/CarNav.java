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

public class CarNav implements Runnable {
	private int pwmEcuId;
	private Ecm ecm;
	
	private static final int PORT = 9002;
	private List<Socket> mList = new ArrayList<Socket>();
	private ExecutorService mExecutorService = null; // thread pool

	public CarNav() {
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
			System.out.println("CarNav server start (on ECM) ...");
			Socket client = null;
			while (true) {
				System.out.println("CarNav: listening");
				client = server.accept();
				mList.add(client);
				System.out.println("CarNav: starting new service");
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
			byte[] incomingBytes;
			
			try {
				while (true) {
					int nrIncomingBytes = in.available();
					if (nrIncomingBytes > 0) {
					    incomingBytes = new byte[nrIncomingBytes];
						in.read(incomingBytes);
						
						ecm.crashbytes = incomingBytes;
					}
				    Thread.sleep(1);
				}
			} catch (Exception e) {
				System.out.println("Connection between cellphone and ECM was terminated");
				e.printStackTrace();
			}
		}
	}	
}
