package sics.port.instances;

import com.sun.squawk.Address;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;

import sics.PIRTE;
import sics.port.EcuVirtualPPort;
import sics.port.EcuVirtualRPort;

//TODO: Make this class generic (so that data doesn't need casting)
public class VirtualAdcPPort extends EcuVirtualPPort {
	private int id;
	
	public VirtualAdcPPort(int id) {
		super(id);
	}

	@Override
	public Object deliver() {
		long data = VM.jnaFetchAdcData();
		float res = ((float) data)/10000;
		String resStr = String.valueOf(res);
		return resStr;
	}

	@Override
	public Object deliver(int portId) {
		// TODO Auto-generated method stub
		return null;
	}

}
