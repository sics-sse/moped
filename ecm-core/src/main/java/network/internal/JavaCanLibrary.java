package network.internal;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

public interface JavaCanLibrary extends Library {
	public int init_can();
	public int sendData(int chn_num, int can_id, byte[] data);
	public int sendBigData(int chn_num, int can_id, int can_dlc, int dataSize, byte[] data);
	public Pointer receiveData(int chn_num, int can_id);
	public Pointer receiveByteData(int chn_num, int can_id);
	public int getPackageSize();
	public void resetPackageSize();
}
