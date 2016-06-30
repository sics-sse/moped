package network.external;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
//import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import ecm.Ecm;

// TODO: Auto-generated Javadoc
/**
 * The Class CommunicationManager.
 */
public class SocketCommunicationManager implements CommunicationManager {

	/** The external plugin sw component. */
	private Ecm ecm;

	/** The session. */
	private IoSession session;

	/** The vin. */
	private String vin;

        private Boolean is_simulator;

	private String server;

	private int port;

	/**
	 * Instantiates a new communication manager.
	 * 
	 * @param externalPluginSWComponent
	 *            the external plugin sw component
	 * @param vin
	 *            the vin
	 */
    public SocketCommunicationManager(String vin, String server, int port,
				      Boolean is_simulator) {
		this.vin = vin;
		this.server = server;
		this.port = port;
		this.is_simulator = is_simulator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		int failCnt = 0;
		
		System.out.println("Running SocketCommunicationManager");
		boolean connectServer = false;
		if (!server.equals("none")) {
			NioSocketConnector connector = new NioSocketConnector();
			connector.getSessionConfig().setKeepAlive(true);

			connector.getFilterChain().addLast(
					"codec",
					new ProtocolCodecFilter(
							new ObjectSerializationCodecFactory()));
//							new TextLineCodecFactory(Charset.forName("UTF-8"))));
			connector.getFilterChain().addLast("logger", new LoggingFilter());
			connector.setHandler(new ClientHandler(this));

			connectServer = true;

			boolean isStart = false;

			while (true) {
				try {
//					server = "127.0.0.1"; //TEMP
					System.out.println("Trying to connect to " + server + ":" + port);
					ConnectFuture future = connector
							.connect(new InetSocketAddress(server, port));
					future.awaitUninterruptibly();
					session = future.getSession();
					
					isStart = true;
					failCnt = 0;
				} catch (Exception e) {
				    //System.out.println("Warning! Client failed to connect to " + server + ":" + port);
					System.out.println(e.toString());
					// System.exit(-1);
					isStart = false;
					failCnt++;
				}
				
				if (isStart) {
					System.out.println("Connected to trusted server");
					//break;
				}
				else {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (connectServer && isStart) {
				    session.getCloseFuture().awaitUninterruptibly();
				    //connector.dispose();
				    isStart = false;
				    failCnt = 0;
				}
			}
		}
	}

	/**
	 * Gets the session.
	 * 
	 * @return the session
	 */
	public IoSession getSession() {
		return session;
	}

	/**
	 * Sets the session.
	 * 
	 * @param session
	 *            the new session
	 */
	public void setSession(IoSession session) {
		this.session = session;
	}

	/**
	 * Gets the vin.
	 * 
	 * @return the vin
	 */
	public String getVin() {
		return vin;
	}

	public Boolean getIs_Simulator() {
		return is_simulator;
	}

	public void write(Object data) {
		session.write(data);
	}

	public Ecm getEcm() {
		return ecm;
	}

	@Override
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}

}
