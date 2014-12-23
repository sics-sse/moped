package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import messages.LinkContextEntry;

@Entity
@Table(name = "VehiclePlugin")
public class VehiclePlugin implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String vin;
	private int ecuId;
	private int appId;
	private String name;
	private int sendingPortId;
	private int callbackPortId;
	private HashMap<String, Integer> portInitialContext;
	private ArrayList<LinkContextEntry> portLinkingContext;
	private String location;
	private String executablePluginName;
	
	public VehiclePlugin() {
	}

	public VehiclePlugin(String vin, String name, int appId, int ecuId,
			int sendingPortId, int callbackPortId,
			HashMap<String, Integer> portInitialContext,
			ArrayList<LinkContextEntry> portLinkingContext, String location, String executablePluginName) {
		this.vin = vin;
		this.name = name;
		this.appId = appId;
		this.ecuId = ecuId;
		this.sendingPortId = sendingPortId;
		this.callbackPortId = callbackPortId;
		this.portInitialContext = portInitialContext;
		this.portLinkingContext = portLinkingContext;
		this.location = location;
		this.executablePluginName = executablePluginName;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getVin() {
		return vin;
	}

	public void setVin(String vin) {
		this.vin = vin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public int getEcuId() {
		return ecuId;
	}

	public void setEcuId(int ecuId) {
		this.ecuId = ecuId;
	}

	public int getSendingPortId() {
		return sendingPortId;
	}

	public void setSendingPortId(int sendingPortId) {
		this.sendingPortId = sendingPortId;
	}

	public int getCallbackPortId() {
		return callbackPortId;
	}

	public void setCallbackPortId(int callbackPortId) {
		this.callbackPortId = callbackPortId;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Lob
	public HashMap<String, Integer> getPortInitialContext() {
		return portInitialContext;
	}

	public void setPortInitialContext(
			HashMap<String, Integer> portInitialContext) {
		this.portInitialContext = portInitialContext;
	}

	@Lob
	public ArrayList<LinkContextEntry> getPortLinkingContext() {
		return portLinkingContext;
	}

	public void setPortLinkingContext(
			ArrayList<LinkContextEntry> portLinkingContext) {
		this.portLinkingContext = portLinkingContext;
	}

	public String getExecutablePluginName() {
		return executablePluginName;
	}

	public void setExecutablePluginName(String executablePluginName) {
		this.executablePluginName = executablePluginName;
	}

}
