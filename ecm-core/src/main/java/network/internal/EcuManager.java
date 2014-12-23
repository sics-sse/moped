package network.internal;

import ecm.Ecm;
import messages.Message;

public interface EcuManager extends Runnable {
	public void sendMessage(Message message);
	public void setEcm(Ecm ecm);
	public Ecm getEcm();
//	public void loadPlugins(int ecuId);
}
