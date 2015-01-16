package test;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
public interface ISoapTest {
	@WebMethod 
	public void printString(String str);
	
	@WebMethod 
	public String install(String vin, int id);
	
	@WebMethod 
	public void printMessage(String msg);
}
