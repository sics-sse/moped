package autosar;

import fresta.pirte.PIRTE;

public class SWC {
	private int id;
	private PIRTE pirte;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PIRTE getPirte() {
		return pirte;
	}

	public void setPirte(PIRTE pirte) {
		this.pirte = pirte;
		new Thread(pirte).start();
	}

}
