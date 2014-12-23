package autosar;

import hw.BaseEUnit;

public class SWCRPort<T> {
	private int id;
	private BaseEUnit baseEUnit;
	private T data;

	public SWCRPort(int id) {
		this.id = id;
	}
	
	public void setBaseEUnit(BaseEUnit baseEUnit) {
		this.baseEUnit = baseEUnit;
	}
	
	public void updateData(T data) {
		this.data = data;
		baseEUnit.wakeup();
	}
	
	public T read() {
		return data;
	}
	
	public int getId() {
		return id;
	}
}
