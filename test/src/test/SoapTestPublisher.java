package test;

import javax.xml.ws.Endpoint;

public class SoapTestPublisher {
	public static void main(String[] args) {
		Endpoint.publish("http://localhost:9999/moped/test", new SoapTest());
	}
}
