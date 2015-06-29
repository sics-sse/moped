package service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import javax.xml.ws.Endpoint;

import messages.InitPacket;
import mina.ServerHandler; //TODO (move this file to the service package)

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolCodecSession;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import service.exception.PluginWebServicesException;

import utils.PropertyAPI;

public class PluginWebServicePublisher {
	private final static int SOCKET_PORT = 9999;
	private final static int WSDL_PORT = 9990;
	private final static String PUB_ADDRESS = PropertyAPI.getInstance().getProperty("serverhost") + WSDL_PORT + "/moped/pws";
//	public static SessionFactory sqlSessionFactory;

	public static void main(String[] args) {		
		/* Start listening for incoming connections 
		 * (see e.g. https://mina.apache.org/mina-project/quick-start-guide.html) */
		SocketAcceptor socketAcceptor = new NioSocketAcceptor();
		
		socketAcceptor.getFilterChain().addLast("logger", new LoggingFilter());
		socketAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory())); //TODO: This is a default codec, should ideally not be used
//		socketAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

		ServerHandler handler = new ServerHandler();
		socketAcceptor.setHandler(handler);
		
		try {
			socketAcceptor.setReuseAddress(true);
			socketAcceptor.bind(new InetSocketAddress(SOCKET_PORT));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Make the php-java interface accessible */
		PluginWebServicesImpl pws = new PluginWebServicesImpl(handler);
		Endpoint.publish(PUB_ADDRESS, pws);
		
		System.out.println("published");

//		try {
//			Configuration config = new Configuration();
//			config.configure();
//
//			sqlSessionFactory = config.buildSessionFactory(new StandardServiceRegistryBuilder().
//					applySettings(config.getProperties()).build());
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
	}
}
