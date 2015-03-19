package service;

import java.util.ArrayList;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import service.exception.PluginWebServicesException;

@WebService
@SOAPBinding(style = Style.RPC)
public interface PluginWebServices {
    @WebMethod
	public String uploadApp(byte [] data,
				 String appname, String version)
	throws PluginWebServicesException;

    @WebMethod
	public boolean get_ack_status(String vin, int appId)
	throws PluginWebServicesException;
	
    @WebMethod
	public String installApp(String vin, int appID, String jvm)
	throws PluginWebServicesException;
	
    @WebMethod
	public String uninstallApp(String vin, int appID)
	throws PluginWebServicesException;

    @WebMethod
	public boolean upgrade(String vin, int oldAppID)
	throws PluginWebServicesException;

    @WebMethod
	public boolean restoreEcu(String vin, int ecuReference)
	throws PluginWebServicesException;

    @WebMethod
	public String addVehicleConfig(byte [] data) 
	throws PluginWebServicesException;

    @WebMethod
	public String compileApp(String appname, String version)
	throws PluginWebServicesException;

    @WebMethod
	public String infoVehicle(String vin)
	throws PluginWebServicesException;

    @WebMethod
	public String deleteVehicle(String vin)
	throws PluginWebServicesException;

    @WebMethod
	public String addVehicle(String name, String vin, String type)
	throws PluginWebServicesException;

    @WebMethod
	public String listVehicles()
	throws PluginWebServicesException;

    @WebMethod
	public String listUserVehicles(int user_id)
	throws PluginWebServicesException;

    @WebMethod
	public String listApplications()
	throws PluginWebServicesException;

    @WebMethod
	public String listVehicleConfigs()
	throws PluginWebServicesException;

    @WebMethod
	public String listUserVehicleAssociations(int user_id)
	throws PluginWebServicesException;

    @WebMethod
	public String listInstalledApps()
	throws PluginWebServicesException;

    @WebMethod
	public String addUserVehicleAssociation(int user_id, String vin,
						boolean defaultVehicle)
	throws PluginWebServicesException;

    @WebMethod
	public String setUserVehicleAssociationActive
	(int user_id, String vin, boolean active)
	throws PluginWebServicesException;

    @WebMethod
	public String deleteUserVehicleAssociation(int user_id, String vin)
	throws PluginWebServicesException;

    @WebMethod
	public String jsontest()
	throws PluginWebServicesException;

    @WebMethod
	public boolean checkpassword(String pwd, String hash)
	throws PluginWebServicesException;

    @WebMethod
	public String [] [] stringtest()
	throws PluginWebServicesException;
}
