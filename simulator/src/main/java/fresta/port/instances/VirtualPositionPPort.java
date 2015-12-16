package fresta.port.instances;

import sics.port.EcuVirtualPPort;
import gui.CarModel;

public class VirtualPositionPPort extends EcuVirtualPPort {
	
	public VirtualPositionPPort(int id) {
		super(id);
	}
	
    static long pos() {
	long l;

	long x, y, ang, q, t;
	x = (long)(100*CarModel.position_x);
	y = (long)(100*CarModel.position_y);
	ang = (int) (180*CarModel.direction/Math.PI*256/360);
	ang = ang % 256;
	q = 100;
	t = 0;

	return (x<<48) + (y<<32) + (ang<<24) + (q<<16) + t;
    }

	@Override
	public Object deliver() {
	    return (Long) pos();
	}

	@Override
	public Object deliver(int portId) {
	    return (Long) pos();
	}
}
