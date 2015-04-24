
package sics;

import com.sun.cldc.jna.Library;
import com.sun.cldc.jna.Native;

public class RTEBridge {

//	public interface CLibrary extends Library {
//    		CLibrary INSTANCE = (CLibrary)Native.loadLibrary("null", CLibrary.class);
//		public void output(String out);
//	}

	public static native void output(String out);

}
