package main;

import io.PublisherFactory;
import io.IPublisher;
import io.MQTTPublisher; // for 'vin'
import io.IMessage;
import io.IReceiver;
import io.ReceiverFactory;
import utils.PropertyAPI;
import network.external.CarDriver;
import network.external.CarMobile;
import network.external.CarNav;
import network.external.CommunicationManager;
import network.external.IoTManager;
import network.external.SocketCommunicationManager;
import network.internal.CanEcuManager;
import network.internal.EcuManager;
import network.internal.SocketEcuManager;
import ecm.Ecm;
import messages.PublishPacket;

public class Main {
    private static class Rec implements IMessage {
	public void receive(String msg) {
	    System.out.println("Rec message (" + msg + ")");
	}
    }


	public static void main(String[] args) {

		// Initiate ecuManager
		// Achieve configuration from settings.properties
//		int linux_autosar_port = Integer.parseInt(PropertyAPI.getInstance().getProperty("LINUX_AUTOSAR_PORT"));
//		EcuManager ecuManager = new SocketEcuManager(linux_autosar_port);	
		CanConfigParser.parseCanConfig("canConfig.xml");
		EcuManager ecuManager = new CanEcuManager(CanConfigParser.getSenders(), CanConfigParser.getReceivers());
		
		// Initiate trustServerManager
		// Achieve configuration from settings.properties
		String vin = PropertyAPI.getInstance().getProperty("VIN");
		String server = PropertyAPI.getInstance().getProperty("SERVER");
		int server_port = Integer.parseInt(PropertyAPI.getInstance().getProperty("SERVER_PORT"));
		CommunicationManager commuManager = new SocketCommunicationManager(vin, server,
										   server_port, false);
		
		// Initiate CarDriver APP
		CarDriver carDriver = new CarDriver(2);
		CarMobile carMobile = new CarMobile();
		CarNav carNav = new CarNav();
		
    //IReceiver rcv = ReceiverFactory.receiver("mqtt+retain+clean://iot.eclipse.org:1883/efrecon/20UYA31581L000000/speed");
    //rcv.subscribe("(\\d+)", 1, null);

//    IReceiver rcv = ReceiverFactory.receiver("ws://api.xively.com:8080/", "{\n" +
//            "  \"method\" : \"subscribe\",\n" +
//            "  \"resource\" : \"/feeds/803043226\",\n" +
//            "  \"headers\" : {\"X-ApiKey\":\"VkmywY0yGtYHD8ES92BSbPSbXQ8nZGYh83LMnDJ9NcC3uzSX\"}\n" +
//            "}");
//    rcv.subscribe(null);

    // Initiate iotManager
    //IPublisher publisher = PublisherFactory.publisher("https+put://api.xively.com/v2/feeds/235511358n", "{\"version\":\"1.0.0\",\"datastreams\":[{\"id\":\"frontSpeed\",\"current_value\":\"%value%\"}]}\n\t\t\t\t\t");
		//publisher.setRate(15000);
		//publisher.addHeader("X-ApiKey", "8oxsohCL6TrQ1hNjoFYqRW1BrFW5jr1TkInzNVy4Y6bEJHsq");
		//IoTManager iotManager = new IoTManager(publisher);
		//		IPublisher publisher = PublisherFactory.publisher("mqtt+retain+clean://iot.eclipse.org:1883/zeni/speed", "{\"version\":\"1.0.0\",\"datastreams\":[{\"id\":\"frontSpeed\",\"current_value\":\"%value%\"}]}\n\t\t\t\t\t");

		Rec rec = new Rec();

		String mqtthost = PropertyAPI.getInstance().getProperty("MQTTHOST");
		if (mqtthost == null) {
		    mqtthost = "test.mosquitto.org";
		}
		System.out.println("mqtt host = " + mqtthost);
		
		IReceiver receiver = ReceiverFactory.receiver("mqtt+retain+clean://" + mqtthost + ":1883/sics/moped/to-car/+", null);
		//receiver.unsubscribe();
		// Doesn't work, because there are two lists called
		// 'dispatchers'. One is the one which is used; the other
		// is private and is the one that we add to here.
		try {
		    receiver.subscribe(rec);
		} catch (NullPointerException a) {
		    receiver = null;
		    System.out.println("Couldn't subscribe");
		}

		IPublisher publisher = PublisherFactory.publisher("mqtt+retain+clean://" + mqtthost + ":1883/sics/moped/value", "{\"version\":\"1.0.0\",\"vin\":\"%VIN%\",\"datastreams\":[{\"id\":\"%key%\",\"current_value\":\"%value%\"}]}\n\t\t\t\t\t");

		MQTTPublisher mqttpub = (MQTTPublisher) publisher;
		mqttpub.vin = vin;

		IoTManager iotManager = new IoTManager(publisher, receiver);
		
		if (receiver == null) {
		    iotManager = null;
		}

//    IPublisher publisher = PublisherFactory.publisher("ws://api.xively.com:8080/", "{\n" +
//            "  \"method\" : \"put\",\n" +
//            "  \"resource\" : \"/feeds/803043226\",\n" +
//            "  \"params\" : {},\n" +
//            "  \"headers\" : {\"X-ApiKey\":\"VkmywY0yGtYHD8ES92BSbPSbXQ8nZGYh83LMnDJ9NcC3uzSX\"},\n" +
//            "  \"body\" :\n" +
//            "    {\n" +
//            "      \"version\" : \"1.0.0\",\n" +
//            "      \"datastreams\" : [\n" +
//            "        {\n" +
//            "          \"id\" : \"%key%\",\n" +
//            "          \"current_value\" : \"%value%\"\n" +
//            "        }\n" +
//            "      ]\n" +
//            "    }\n" +
//            "}");
//    publisher.setRate(5000);
//    IoTManager iotManager = new IoTManager(publisher);

		Ecm ecm = new Ecm();
		if (iotManager != null) {
		    iotManager.setEcm(ecm);
		}
		ecm.init(ecuManager, commuManager, iotManager, carDriver, carMobile, carNav);
		// Sometimes the mqtt manager is not ready yet, and then
		// we crash
		//iotManager.sendPacket(new PublishPacket("speed", "3.235"));

		ecm.start(args);
		//iotManager.sendPacket(new PublishPacket("speed", "3.234"));
	}
}
