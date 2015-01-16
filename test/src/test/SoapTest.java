package test;

import javax.jws.WebService;

@WebService(endpointInterface = "test.ISoapTest")
public class SoapTest implements ISoapTest {
	@Override
	public void printString(String str) {
		System.out.println("printed str: " + str);
	}
	
	@Override
	public String install(String vin, int id) { 
		System.out.println("vin in install(): " + vin);
		System.out.println("id in install(): " + id);
		
		return "hej";
	}
	
	@Override
	public void printMessage(String msg) {
		System.out.println("message: " + msg);
	}
}
