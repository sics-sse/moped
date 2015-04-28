package car;

import hw.BaseEUnit;
import hw.Ecu;
import hw.instance.SpeedActuator;
import hw.instance.SteerActuator;
import io.IPublisher;
import io.IReceiver;
import io.PublisherFactory;
import io.ReceiverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import network.external.CarDriver;
import network.external.CommunicationManager;
import network.external.IoTManager;
import network.external.SocketCommunicationManager;
import network.internal.EcuManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import autosar.FakeEcuManager;
import autosar.RTE;
import autosar.SWC;
import autosar.SWCPPort;
import autosar.SWCRPort;
import ecm.Ecm;
import fresta.pirte.PIRTE;

public class CarFactory {
	private static CarFactory instance;
	private HashMap<String, BaseEUnit> hwUnit = new HashMap<String, BaseEUnit>();

	private CarFactory() {
		hwUnit.put("speedActuator", new SpeedActuator());
		hwUnit.put("steerActuator", new SteerActuator());
	}

	public static CarFactory getCarFactory() {
		if (instance == null) {
			instance = new CarFactory();
		}
		return instance;
	}

	public Car generateCar(String carConfigPath) {
		/*
		The function generateCar creates a new Car by reading in the XML file located in the carConfigPath. 
		The name and brand fields of the Car instance is read from the tags <name> and <brand>.
		The ecm field of the Car instance is set to a newly created Ecm (from the ecm-core package). 
		This Ecm consists of a new FakeEcuManager, SocketCommunicationManager, CarDriver and IotManager,
		all based on the data in the <server> part of the XML file.
		The RTE instance is also provided with this ECM.
		
		When reading the <ecus> part of the XML file, the following occurs for each <ecu>.
		A new directory is created ecus/ecuX, where X is the <id> tag of the <ecu>.
		An Ecu instance is also created, but it is never used for anything.
		
		If there are items under <swcs>, then for each <swcs> an SWC instance is created.
		If the <hasPirte> tag of that <swc> contains the value "true", a new PIRTE instance
		is also created, and associated with this ECU's id. The pirte attribute of the SWC
		is set to this new PIRTE, and the SWC is added to the RTE's list of SWCs with PIRTEs.
		If the SWC did not have a PIRTE, the SWC instance is discarded.
		The <ports> part of the <swc> is read, but ignored.
		The <sensors> part of the <ecu> is read, but ignored.
		The <actuators> part of the <ecu> is read, and for each actuator, a SWCPPort is created and
		added to the RTE's list of pports.
		
		When reading the <links> part of the XML file, for each link a pair of SWCPPort and SWCRport
		are created, and added to the RTE's pports and rports lists. In addition, a link is added
		to the RTE's links list.
		*/
		
		// Empty files in ECUs
		File ecusDir = new File("ecus");
		try {
			clearEcuFiles(ecusDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Car car = new Car();

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(carConfigPath);
			Document doc = dombuilder.parse(is);
			// vehicle
			Element root = doc.getDocumentElement();

			// name of vehicle
			Element vehicleName = (Element) root.getElementsByTagName("name")
					.item(0);
			String vehicleNameStr = vehicleName.getTextContent();

			// new instance of VehicleConfig
			if (vehicleNameStr == null || vehicleNameStr.isEmpty()) {
				System.out
						.println("There is no vehicle name in vehicle configuration file");
				return null;
			}

			car.setName(vehicleNameStr);

			// brand of vehicle
			Element vehicleBrand = (Element) root.getElementsByTagName("brand")
					.item(0);
			String vehicleBrandStr = vehicleBrand.getTextContent();
			if (vehicleBrandStr == null || vehicleBrandStr.isEmpty()) {
				System.out
						.println("There is no brand name in vehicle configuration file");
				return null;
			}

			car.setBrand(vehicleBrandStr);

			// VIN
			Element vinElement = (Element) root.getElementsByTagName("vin")
					.item(0);
			if (vinElement == null) {
				System.out
						.println("There is no vin definition in vehicle configuration file");
				return null;
			}
			String vinStr = vinElement.getTextContent();

			// ECM
			Element ecmElement = (Element) root.getElementsByTagName("ecm")
					.item(0);
			if (ecmElement == null) {
				System.out
						.println("There is no ecm definition in vehicle configuration file");
				return null;
			}
			// Server
			Element serverElement = (Element) ecmElement.getElementsByTagName(
					"server").item(0);
			if (serverElement == null) {
				System.out
						.println("There is no server definition in vehicle configuration file");
				return null;
			}
			// IP
			Element ipElement = (Element) serverElement.getElementsByTagName(
					"ip").item(0);
			if (ipElement == null) {
				System.out
						.println("There is no ip definition in vehicle configuration file");
				return null;
			}
			String ipStr = ipElement.getTextContent();
			// Socket Port
			Element socketPortElement = (Element) serverElement
					.getElementsByTagName("socketport").item(0);
			if (socketPortElement == null) {
				System.out
						.println("There is no socket port definition in vehicle configuration file");
				return null;
			}
			String sockePortStr = socketPortElement.getTextContent();
			int socketPortInt = Integer.parseInt(sockePortStr);

			// instance ECM
			EcuManager ecuManager = new FakeEcuManager();
			CommunicationManager commuManager = new SocketCommunicationManager(
											   vinStr, ipStr, socketPortInt,
											   true);
			CarDriver carDriver = new CarDriver(2);
			// IPublisher publisher = PublisherFactory
			// .publisher(
			// "https+put://api.xively.com/v2/feeds/235511358n",
			// "{\"version\":\"1.0.0\",\"datastreams\":[{\"id\":\"%key%\",\"current_value\":\"%value%\"}]}\n\t\t\t\t\t");
			// publisher.setRate(15000);
			// publisher.addHeader("X-ApiKey",
			// "8oxsohCL6TrQ1hNjoFYqRW1BrFW5jr1TkInzNVy4Y6bEJHsq");
			IPublisher publisher = PublisherFactory
					.publisher(
							"mqtt+retain+clean://iot.eclipse.org:1883/zeni/speed",
							"{\"version\":\"1.0.0\",\"datastreams\":[{\"id\":\"%key%\",\"current_value\":\"%value%\"}]}\n\t\t\t\t\t");
			IReceiver receiver = ReceiverFactory.receiver("mqtt+retain+clean://iot.eclipse.org:1883/zeni/speed");
			IoTManager iotManager = new IoTManager(publisher); //, receiver);
			Ecm ecm = new Ecm();
			ecm.init(ecuManager, commuManager, iotManager, carDriver);
			car.setEcm(ecm);

			RTE.getInstance().setEcm(ecm);

			// instance ECUs
			Element ecusElement = (Element) root.getElementsByTagName("ecus")
					.item(0);
			if (ecusElement == null) {
				System.out
						.println("There is no ecus definition in vehicle configuration file");
				return null;
			}
			NodeList list = ecusElement.getElementsByTagName("ecu");

			for (int i = 0; i < list.getLength(); i++) {
				// ecu
				Ecu ecu = new Ecu();
				Element ecuElement = (Element) list.item(i);
				Element ecuIdElement = (Element) ecuElement
						.getElementsByTagName("id").item(0);
				if (ecuIdElement == null) {
					System.out
							.println("There is no ecu ID definition in vehicle configuration file");
					return null;
				}
				String ecuIdStr = ecuIdElement.getTextContent();
				int ecuId = Integer.parseInt(ecuIdStr);
				ecu.setId(ecuId);

				// prepare working directories for each ECU
				new File("ecus/ecu" + ecuId).mkdirs();

				// SWCS
				Element swcsElement = (Element) ecuElement
						.getElementsByTagName("swcs").item(0);
				if (swcsElement == null) {
					System.out
							.println("There is no swcs definition in vehicle configuration file");
					return null;
				}
				NodeList swcList = swcsElement.getElementsByTagName("swc");
				for (int s = 0; s < swcList.getLength(); s++) {
					// swc
					Element swcElement = (Element) list.item(s);
					SWC swc = new SWC();
					// hasPirte
					Element hasPirteElement = (Element) swcElement
							.getElementsByTagName("hasPirte").item(0);
					if (hasPirteElement == null) {
						System.out
								.println("There is no hasPirte definition in vehicle configuration file");
						return null;
					}
					String hasPirteStr = hasPirteElement.getTextContent();
					if (hasPirteStr.equals("true")) {
						// import PIRTE
						PIRTE pirte = new PIRTE(ecuId);
						swc.setPirte(pirte);
						RTE.getInstance().addPirteSWC(ecuId, swc);
					} else if (hasPirteStr.equals("false")) {
						// Not import PIRTE
					} else {
						System.out
								.println("Enter wrong key word for hasPirte tag in vehicle configuraiton file");
						return null;
					}

					// ports
					Element portsElement = (Element) swcElement
							.getElementsByTagName("ports").item(0);
					if (portsElement == null) {
						System.out
								.println("There is no ports of swc in vehicle configuration file");
						return null;
					}
//					NodeList portList = portsElement
//							.getElementsByTagName("port");
//					for (int p = 0; p < portList.getLength(); p++) {
//						// port
//						Element portElement = (Element) portList.item(p);
//						if (portElement != null) {
//							// port ID
//							Element portIdElement = (Element) portElement
//									.getElementsByTagName("id").item(0);
//							if (portIdElement == null) {
//								System.out
//										.println("There is no port ID definition in vehicle configuration file");
//								return null;
//							}
//							String portIdStr = portIdElement.getTextContent();
//							int portIdInt = Integer.parseInt(portIdStr);
//							// port direction
//							Element portDirectionElement = (Element) portElement
//									.getElementsByTagName("direction").item(0);
//							if (portDirectionElement == null) {
//								System.out
//										.println("There is not port direction definition in vehicle configuration file");
//								return null;
//							}
//							String portDirectionStr = portDirectionElement
//									.getTextContent();
//							if (portDirectionStr.equals("from")) {
//								SWCPPort pport = new SWCPPort(portIdInt);
//								RTE.getInstance().addPPort(pport);
//							} else if (portDirectionStr.equals("to")) {
//								SWCRPort rport = new SWCRPort(portIdInt);
//								RTE.getInstance().addRPort(rport);
//							} else {
//								System.out
//										.println("Wrong definition for port direction in vehicle configuration file");
//								return null;
//							}
//						}
//
//					}
				}

				// instance Sensors
				Element sensorsElement = (Element) root.getElementsByTagName(
						"sensors").item(0);
				if (sensorsElement == null) {
					System.out
							.println("There is no sensors definition in vehicle configuration file");
					return null;
				}
				NodeList sensorList = sensorsElement
						.getElementsByTagName("sensor");
				for (int s = 0; s < sensorList.getLength(); s++) {
					// sensor
				}

				// instance actuators
				Element actuatorsElement = (Element) root.getElementsByTagName(
						"actuators").item(0);
				if (actuatorsElement == null) {
					System.out
							.println("There is no actuators definition in vehicle configuration file");
					return null;
				}
				NodeList actuatorList = actuatorsElement
						.getElementsByTagName("actuator");
				for (int a = 0; a < actuatorList.getLength(); a++) {
					// actuator
					Element actuatorElement = (Element) actuatorList.item(a);
					// name
					Element actuatorNameElement = (Element) actuatorElement
							.getElementsByTagName("name").item(0);
					if (actuatorNameElement == null) {
						System.out
								.println("There is no name of actuator definition in vehicle configuration file");
						return null;
					}
					String actuatorNameStr = actuatorNameElement
							.getTextContent();
					// ports
					Element portsElement = (Element) actuatorElement
							.getElementsByTagName("ports").item(0);
					if (portsElement == null) {
						System.out
								.println("There is no ports of actuator definition in vechicle configuration file");
						return null;
					}
					NodeList actuatorPortList = portsElement
							.getElementsByTagName("port");
					for (int ap = 0; ap < actuatorPortList.getLength(); ap++) {
						// port
						Element actuatorPortElement = (Element) actuatorPortList
								.item(ap);
						if (actuatorPortElement == null) {
							System.out
									.println("There is no port of acutation definition in vehicle configuration file");
							return null;
						}
						// id
						Element actPortId = (Element) actuatorPortElement
								.getElementsByTagName("id").item(0);
						if (actPortId == null) {
							System.out
									.println("There is no port Id of actuator definition in vehicle configuration file");
							return null;
						}
						String actPortIdStr = actPortId.getTextContent();
						int actPortIdInt = Integer.parseInt(actPortIdStr);
						// direction
//						Element actPortDirectionElement = (Element) actuatorPortElement
//								.getElementsByTagName("direction").item(0);
//						if (actPortDirectionElement == null) {
//							System.out
//									.println("There is no port direction of actuator definition in vehicle cconfiguration file");
//							return null;
//						}
//						String actPortDirectionStr = actPortDirectionElement
//								.getTextContent();
//						if (actPortDirectionStr.equals("from")) {
//							SWCPPort pport = new SWCPPort(actPortIdInt);
//							RTE.getInstance().addPPort(pport);
//						} else if (actPortDirectionStr.equals("to")) {
//							SWCRPort rport = new SWCRPort(actPortIdInt);
//							RTE.getInstance().addRPort(rport);
//						} else {
//							System.out
//									.println("Wrong type of actuator port direction in vehicle configuration");
//							return null;
//						}
						SWCPPort pport = new SWCPPort(actPortIdInt);
						RTE.getInstance().addPPort(pport);
					}
				}

				// instance links
				Element linksElement = (Element) root.getElementsByTagName(
						"links").item(0);
				if (linksElement == null) {
					System.out
							.println("There is no links definition in vehicle configuration file");
					return null;
				}
				NodeList linkList = linksElement.getElementsByTagName("link");

				for (int l = 0; l < linkList.getLength(); l++) {
					// link
					Element linkElement = (Element) linkList.item(l);
					if (linkElement != null) {
						// type
						Element typeElement = (Element) linkElement
								.getElementsByTagName("type").item(0);
						if (typeElement == null) {
							System.out
									.println("There is no type of link definition in vehicle configuration file");
							return null;
						}
						String typeStr = typeElement.getTextContent();
						int type = Integer.parseInt(typeStr);
						// from
						Element fromElement = (Element) linkElement
								.getElementsByTagName("from").item(0);
						if (fromElement == null) {
							System.out
									.println("There is no from of link definition in vehicle configuration file");
							return null;
						}
						String fromStr = fromElement.getTextContent();
						int fromInt = Integer.parseInt(fromStr);

						SWCPPort pport = new SWCPPort(fromInt);
						RTE.getInstance().addPPort(pport);

						Element toElement = (Element) linkElement
								.getElementsByTagName("to").item(0);
						if (toElement == null) {
							System.out
									.println("There is no to of link definition in vehicle configuration file");
							return null;
						}
						String toStr = toElement.getTextContent();
						int toInt = Integer.parseInt(toStr);

						SWCRPort rport = new SWCRPort(toInt);
						RTE.getInstance().addRPort(rport);

						RTE.getInstance().addLink(fromInt, toInt);
					}

				}
			}
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return car;
	}

	private void clearEcuFiles(File f) throws IOException {
		if (f.isDirectory()) {
			for (File c : f.listFiles())
				clearEcuFiles(c);
		} else if (!f.delete()) {
		    throw new FileNotFoundException("Failed to delete file: " + f);
		}
	}
}
