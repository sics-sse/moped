package common;

import java.io.Serializable;

public class GlobalVariables implements Serializable {
	/**
	 * 
	 */
	// same ECU
	public final static int PPORT2PPORT = -1;
	// native
	public final static int PPORT2VPORT = -2;
	// native or different ECU
	public final static int VPORT2PORT = -3;
	
	private static final long serialVersionUID = 1L;
	public static final byte SERVER = (byte)0;
	public static final int ECM = 1;
	public static final byte ALL = (byte)-1;

	public static final int SWC_PORT_TYPE1 = 1;
	public static final int SWC_PORT_TYPE2 = 2;
	public static final int SWC_PORT_TYPE3 = 3;
	public GlobalVariables() {}
}
