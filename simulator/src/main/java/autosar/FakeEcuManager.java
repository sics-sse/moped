package autosar;

import network.external.IoTManager;
import network.internal.EcuManager;
import ecm.Ecm;
import gui.CarModel;
import messages.InstallMessage;
import messages.LoadMessage;
import messages.Message;
import messages.MessageType;
import messages.PWMMessage;
import messages.PluginMessage;
import messages.PublishMessage;
import messages.PublishPacket;
import messages.UninstallMessage;

public class FakeEcuManager implements EcuManager {
	private Ecm ecm;

	public void run() {
		// TODO Auto-generated method stub

	}

	public void sendMessage(Message message) {
		try {
			int messageType = message.getMessageType();
			if (messageType != MessageType.PWM) {
			    System.out.println(">>> simulator/FakeEcuManager " + messageType);
			}
			switch (messageType) {
			case MessageType.INSTALL:
				System.out
						.println("[FakeEcuManager - sendInstallMessage(Message)]");
				InstallMessage installMessage = (InstallMessage) message;
		    System.out.println("<<< simulator/FakeEcuManager " + messageType);
				sendMessage(installMessage);
				break;
			case MessageType.UNINSTALL:
				System.out
						.println("[FakeEcuManager - sendUninstallMessage(Message)]");
				UninstallMessage uninstallMessage = (UninstallMessage) message;
		    System.out.println("<<< simulator/FakeEcuManager " + messageType);
				sendMessage(uninstallMessage);
				break;
			case MessageType.LOAD:
				LoadMessage loadMessage = (LoadMessage) message;
		    System.out.println("<<< simulator/FakeEcuManager " + messageType);
				sendMessage(loadMessage);
				break;
			case MessageType.PWM:
				PWMMessage pwmMessage = (PWMMessage) message;
				byte[] data = pwmMessage.getData();
//			System.out.println(data[0] + ":" + data[1]);
				int speed = data[0];
				CarModel.motorPower = speed / 100.0;
				if (CarModel.motorPower == 0.0) {
					CarModel.vehicleSpeed = 0;
				}
				int steer = data[1];
				CarModel.steeringAngle = steer / 100.0;
				break;
			case MessageType.PLUGIN_MESSAGE:
				PluginMessage pluginMessage = (PluginMessage) message;
		    System.out.println("<<< simulator/FakeEcuManager " + messageType);
				sendMessage(pluginMessage);
				break;
			default:
				System.out
						.println("Error: Wrong message type pushed to sending channel");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(InstallMessage message) {
		RTE.getInstance().addRteMessage(message);
	}

	private void sendMessage(UninstallMessage message) {
		RTE.getInstance().addRteMessage(message);
	}

	private void sendMessage(LoadMessage message) {
		System.out.println("LoadMessage");
		RTE.getInstance().addRteMessage(message);
	}

	private void sendMessage(PluginMessage message) {
		System.out.println("Subscribe message");
		RTE.getInstance().addRteMessage(message);
	}
	
	public boolean checkSpecificConnection(int id) {
		return false;
	}

        public void sendToVCU(String str, int portid) {
	}

	@Override
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}

	@Override
	public Ecm getEcm() {
		return ecm;
	}

}
