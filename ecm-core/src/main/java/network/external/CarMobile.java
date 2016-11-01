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

		/**
		 * Listen for input from the Wirelessino app, skip old values, 
		 * and send the last incoming value to VCU
		 */
		public void run() {
			/* We only want to read the last 11 bytes from the input stream (5 for each bar + EOF) */
			String outs;
			
			try {
			    int n = 0;
				while (true) {
				    int crashn;
				    if (ecm.crashstatus > 0) {
					crashn = 9;
				    } else {
					crashn = n;
					n = (n+1)%9;
				    }
				    outs = "{\"crash\":" + crashn + ", \"x\":" +
					ecm.xpos + ", \"y\":" + ecm.ypos + "}\n";
				    byte[] bytes = new byte[outs.length()];
				    for (int i = 0; i < outs.length(); i++) {
					bytes[i] = (byte) outs.charAt(i);
				    }
				    out.write(bytes);

				    Thread.sleep(1000);
				}
			} catch (Exception e) {
				System.out.println("Connection between cellphone and ECM was terminated");
				e.printStackTrace();
			}
		}
	}	
}
