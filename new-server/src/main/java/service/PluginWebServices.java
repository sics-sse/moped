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
	public boolean transferBytes(byte [] data,
				     String filename, String version)
	throws PluginWebServicesException;

    @WebMethod
	public boolean get_ack_status(String vin, int appId)
	throws PluginWebServicesException;
	
    @WebMethod
	public boolean install(String vin, int appID, String jvm)
	throws PluginWebServicesException;
	
    @WebMethod
	public boolean uninstall(String vin, int appID)
	throws PluginWebServicesException;

    @WebMethod
	public boolean upgrade(String vin, int oldAppID)
	throws PluginWebServicesException;

    @WebMethod
	public boolean restoreEcu(String vin, int ecuReference)
	throws PluginWebServicesException;

    @WebMethod
	public boolean parseVehicleConfiguration(String path)
	throws PluginWebServicesException;

    @WebMethod
	public boolean parseVehicleConfigurationFromStr(byte [] data) 
	throws PluginWebServicesException;

    @WebMethod
	public String generateSuite(String zipFile, String fullClassName) throws PluginWebServicesException;

    @WebMethod
	public String [] infoVehicle(String vin) throws PluginWebServicesException;
    @WebMethod
	public boolean deleteVehicle(String vin) throws PluginWebServicesException;
    @WebMethod
	public boolean addVehicle(String name, String vin, String type)
	throws PluginWebServicesException;

    @WebMethod
	public String [] listVehicles() throws PluginWebServicesException;

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
	public boolean addUserVehicleAssociation(int user_id, String vin,
						 boolean defaultVehicle)
	throws PluginWebServicesException;

    @WebMethod
	public boolean setUserVehicleAssociationActive
	(int user_id, String vin, boolean active)
	throws PluginWebServicesException;

    @WebMethod
	public boolean deleteUserVehicleAssociation(int user_id, String vin)
	throws PluginWebServicesException;

    @WebMethod
	public String jsontest() throws PluginWebServicesException;

    @WebMethod
	public boolean checkpassword(String pwd, String hash)
	throws PluginWebServicesException;

    @WebMethod
	public String [] [] stringtest()
	throws PluginWebServicesException;
}
