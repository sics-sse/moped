package tests;

import com.sun.squawk.VM;
import java.io.IOException;

import sics.port.PluginPPort;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;

public class BurningWheels {
	public final static String MAILBOX_NAME = "PirteChannel";
    public static Channel clientChannel = null;
    
    private final static String SPEED_NAME = "BW.speed"; 
    private final static String STEERING_NAME = "BW.steering";
    
    private PluginPPort speed, steering;

	public BurningWheels() {
		speed = new PluginPPort(SPEED_NAME);
		speed.setId(0); //TODO: Don't

		steering = new PluginPPort(STEERING_NAME);
		steering.setId(1); //TODO: Don't

//		burn();
	}

	public static void main(String[] args) {
		VM.println("BurningWheels.main()\r\n");
		
		Client client = new Client();
		client.start();
		
		BurningWheels burningWheels = new BurningWheels();
		burningWheels.burn();
	}

	//TODO: Move this outside of each individual app class (either to Isolate or to a SquawkApp.java)
	static class Client extends Thread {
		public void run() {
			try {
				clientChannel = Channel.lookup(MAILBOX_NAME);if (clientChannel == null) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				}
				if (clientChannel == null) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {}
				}
				while(true) {
					clientChannel.receive();
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void burn() {
//		steering.write(0); // Drive straight forward
//		speed.write(100); // 100% torque
//
//		Thread.sleep(2000);
//
//		steering.write(100); // Turn max right (%)
//		speed.write(-100); // Brake as hell
//
//		Threed.sleep(500);
//		speed.write(0); // Safety brake

		try {
			for (int i=0; i<2; i++) {
				Thread.sleep(5000); // Patience
	
				write(STEERING_NAME, rescaleToPwm(90)); // Drive straight forward
				write(SPEED_NAME, rescaleToPwm(175)); // 100% torque
	
				Thread.sleep(2000);
	
				write(STEERING_NAME, rescaleToPwm(175)); // Turn max right (%)
				write(SPEED_NAME, rescaleToPwm(1)); // Brake as hell
	
				Thread.sleep(500);
				write(SPEED_NAME, rescaleToPwm(90)); // Safety brake
	
				Thread.sleep(10000);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void write(String key, int value) {
		int totalSize = key.length() + 4;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = (byte) (value >> 24);
		dataBytes[1] = (byte) (value >> 16);
		dataBytes[2] = (byte) (value >> 8);
		dataBytes[3] = (byte) (value);

		byte[] nameBytes = key.getBytes();
		for(int i=4;i<totalSize;i++) {
			dataBytes[i] = nameBytes[i-4];
		}

		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			clientChannel.send(dataEnv);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private int rescaleToPwm(int val) {
		return (int) (Math.ceil(100 + (0.55556 * val)) * 16.38);
	}
}
