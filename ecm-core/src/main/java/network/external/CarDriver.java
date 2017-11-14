package network.external;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import messages.PWMMessage;
import ecm.Ecm;

public class CarDriver implements Runnable {
	private int pwmEcuId;
	private Ecm ecm;
	
	private static final int PORT = 9000;
	private List<Socket> mList = new ArrayList<Socket>();
	private ExecutorService mExecutorService = null; // thread pool

	public CarDriver(int pwmEcuId) {
		this.pwmEcuId = pwmEcuId;
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
			System.out.println("CarDriver server start (on ECM) ...");
			Socket client = null;
			while (true) {
				System.out.println("CarDriver: listening");
				client = server.accept();
				mList.add(client);
				System.out.println("CarDriver: starting new service");
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

		public Service(Socket socket) {
			try {
				in = socket.getInputStream();
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
			
			//System.out.println("RC message " + message);

			try {
			    res[0] = Byte.parseByte(message.substring(1, 5));
			    res[1] = Byte.parseByte(message.substring(6, 10));
			} catch (NumberFormatException e) {
			    res[0] = (byte) 'S';
			    res[1] = (byte) '7';
			}
				
			return res;
		}

		/**
		 * Listen for input from the Wirelessino app, skip old values, 
		 * and send the last incoming value to VCU
		 */
		public void run() {
			/* We only want to read the last 11 bytes from the input stream (5 for each bar + EOF) */
			byte[] incomingBytes = new byte[11];
			
			long unixTime2 = 0;
			long unixTime3 = 0;

			try {
				while (true) {

				    long unixTime = System.currentTimeMillis();
					int nrIncomingBytes = in.available();
					if (nrIncomingBytes > 0) {
						/* Skip all bytes except those that represent the last speed&steer command */
					    //System.out.println("incoming " + nrIncomingBytes);
						in.skip(nrIncomingBytes-incomingBytes.length);
						in.read(incomingBytes);
						
						long dff = unixTime - unixTime3;
						if (dff > 600) {
						    System.out.println(unixTime + " too long " + dff);
						}

						unixTime2 = unixTime;
						unixTime3 = unixTime;

						/* Convert the data into a format that will be understood on the recipient side and send it */ 
						String str = new String(incomingBytes, "UTF-8");
						byte[] data = interpretWirelessino(str);
						PWMMessage pwmMessage;
						if (str.charAt(0) == 'S') {
							pwmMessage = new PWMMessage(1, incomingBytes);
							//ecm.process(pwmMessage);
						} else
						    {
							pwmMessage = new PWMMessage(pwmEcuId, data);
							ecm.process(pwmMessage);
						    }
					} else {
					    long dff = unixTime - unixTime2;
					    if (dff > 600) {
						System.out.println(unixTime + " too long0 " + dff);
						unixTime2 = unixTime;
					    }
					}

					Thread.sleep(10);
				}
			} catch (Exception e) {
				System.out.println("Connection between cellphone and ECM was terminated");
				e.printStackTrace();
			}
		}
	}	
}
