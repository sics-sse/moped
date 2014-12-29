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
@Table(name = "Port")
public class Port implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int portId;
	private String direction;
	private Ecu ecu;

	public Port() {
	}

	public Port(byte portId, String direction) {
		this.portId = portId;
		this.direction = direction;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPortId() {
		return portId;
	}

	public void setPortId(int portId) {
		this.portId = portId;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Ecu getEcu() {
		return ecu;
	}

	public void setEcu(Ecu ecu) {
		this.ecu = ecu;
	}

}
