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
	
	/**
	 * This class listens for Wirelessino inputs and relays them to VCU,
	 * skipping old values in each iteration (to avoid building upp buffers). 
	 * 
	 * @author zeni, avenir
	 *
	 */
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
		
		/**
		 * Interpret the input from Wirelessino and convert it into an appropriate format
		 * 
		 * Example: 
		 * 			Input:  "H0050V-097\0"
		 * 			Output: [50, -97]
		 * 
		 * @param message		----- a Wirelessino speed&steer command, such as "H0050V-097\0"
		 * @return				----- speed&steer command in VCU-readable format, such as [50, -97]
		 */
		private byte[] interpretWirelessino(String message) {
			byte[] res = new byte[2];
			
			System.out.println("RC message " + message);

			res[0] = Byte.parseByte(message.substring(1, 5));
			res[1] = Byte.parseByte(message.substring(6, 10));
				
			return res;
		}

		public void run() {
			/* We only want to read the last 11 bytes from the input stream (5 for each bar + EOF) */
			byte[] incomingBytes = new byte[11];
			byte[] bytes = new byte[4];
			
			try {
				while (true) {
					int nrIncomingBytes = in.available();
					if (nrIncomingBytes > 0) {
						in.read(incomingBytes);
						
						String str = new String(incomingBytes, "UTF-8");
						if (ecm.crashstatus != 1) {
						    System.out.println("CarNav got " + str);
						}
						ecm.crashstatus = 1;
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
