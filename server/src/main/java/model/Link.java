package model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Link")
public class Link implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int type;
	private int fromEcuId;
	private int toEcuId;
	private int fromPortId;
	private int toPortId;
	private VehicleConfig vehicleConfig;

	public Link() {
	}

	public Link(int type, int fromEcuId, int toEcuId,
			int fromPortId, int toPortId) {
		this.type = type;
		this.fromEcuId = fromEcuId;
		this.toEcuId = toEcuId;
		this.fromPortId = fromPortId;
		this.toPortId = toPortId;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFromEcuId() {
		return fromEcuId;
	}

	public void setFromEcuId(int fromEcuId) {
		this.fromEcuId = fromEcuId;
	}

	public int getToEcuId() {
		return toEcuId;
	}

	public void setToEcuId(int toEcuId) {
		this.toEcuId = toEcuId;
	}

	public int getFromPortId() {
		return fromPortId;
	}

	public void setFromPortId(int fromPortId) {
		this.fromPortId = fromPortId;
	}

	public int getToPortId() {
		return toPortId;
	}

	public void setToPortId(int toPortId) {
		this.toPortId = toPortId;
	}

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public VehicleConfig getVehicleConfig() {
		return vehicleConfig;
	}

	public void setVehicleConfig(
			VehicleConfig vehicleConfig) {
		this.vehicleConfig = vehicleConfig;
	}

}
