package main;

import io.PublisherFactory;
import io.IPublisher;
import io.IMessage;
import io.IReceiver;
import io.ReceiverFactory;
import utils.PropertyAPI;
import network.external.CarDriver;
import network.external.CommunicationManager;
import network.external.IoTManager;
import network.external.SocketCommunicationManager;
import network.internal.CanEcuManager;
import network.internal.EcuManager;
import network.internal.SocketEcuManager;
import ecm.Ecm;
import messages.PublishPacket;

public class Main {
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
				server_port);
		
		// Initiate CarDriver APP
		CarDriver carDriver = new CarDriver(2);
		
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
		IPublisher publisher = PublisherFactory.publisher("mqtt+retain+clean://iot.eclipse.org:1883/zeni/speed", "{\"version\":\"1.0.0\",\"datastreams\":[{\"id\":\"frontSpeed\",\"current_value\":\"%value%\"}]}\n\t\t\t\t\t");
		IoTManager iotManager = new IoTManager(publisher);
		
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
		ecm.init(ecuManager, commuManager, iotManager, carDriver);
		ecm.start();
    //iotManager.sendPacket(new PublishPacket("speed", "3.234"));
	}
}
