package sics.port.instances;

import com.sun.squawk.VM;

import sics.PIRTE;
import sics.port.EcuVirtualPPort;
import sics.port.EcuVirtualRPort;

//TODO: Make this class generic (so that data doesn't need casting)
public class VirtualIMUPPort extends EcuVirtualPPort {
	private int id;
	
	public VirtualIMUPPort(int id) {
		super(id);
	}

	@Override
	public Object deliver() {
		return parseIMU(); 
	}

	@Override
	public Object deliver(int portId) {
		// TODO Auto-generated method stub
		return null;
	}

	private String parseIMU() {
		String res = "";
		long rawIMUPart1 = VM.jnaReadIMUPart1();
		short[] IMUPart1 = parseIMU(rawIMUPart1);
		res = IMUPart1[0] + "," + IMUPart1[1] + "," + IMUPart1[2];
		long rawIMUPart2 = VM.jnaReadIMUPart2();
		short[] IMUPart2 = parseIMU(rawIMUPart2);
		res = res + "," + IMUPart2[0] + "," + IMUPart2[1] + "," + IMUPart2[2]; 
		return res;
	}
	
	private short[] parseIMU(long rawIMUVal) {
		short[] res = new short[3];
		res[0] = (short) ((rawIMUVal >> 32) & 0x0000FFFF);
		res[1] = (short) ((rawIMUVal >> 16) & 0x00000000000000FF);
		res[2] = (short) (rawIMUVal & 0x000000000000FFFF);
		return res;
	}
}
