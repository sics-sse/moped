package service;

import javax.jws.WebMethod;
import javax.jws.WebService;

import service.exception.PluginWebServicesException;

@WebService
public interface PluginWebServices {
	@WebMethod
	public String install(String vin, int appID)
			throws PluginWebServicesException;
	
	@WebMethod
	public String install4Jdk(String vin, int appID)
			throws PluginWebServicesException;

	@WebMethod
	public boolean uninstall(String vin, int appID)
			throws PluginWebServicesException;

	@WebMethod
	public boolean upgrade(String vin, int oldAppID)
			throws PluginWebServicesException;

	@WebMethod
	public void setUpgradeFlag(int oldAppID) throws PluginWebServicesException;

	@WebMethod
	public boolean restoreEcu(String vin, int ecuReference)
			throws PluginWebServicesException;

	@WebMethod
	public String parseVehicleConfiguration(String path)
			throws PluginWebServicesException;

	@WebMethod
	public String parsePluginConfiguration(int appId, String path)
			throws PluginWebServicesException;
	
	@WebMethod
	public String generateSuite(String zipFile, String fullClassName) throws PluginWebServicesException;
}
