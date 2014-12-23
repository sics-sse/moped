package dao;

import java.util.List;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import common.GlobalVariables;
import model.Ecu;
import model.Link;
import model.Port;
import model.VehicleConfig;

@Repository
public class VehicleConfigDaoImpl implements VehicleConfigDao {
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public VehicleConfig getVehicleConfig(int vehicleConfigId) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from VehicleConfig v where v.id = ?");
		query.setInteger(0, vehicleConfigId);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehicleConfig> vehicleConfigs = query.list();
		VehicleConfig vehicleConfig = vehicleConfigs.get(0);
		session.getTransaction().commit();
		return vehicleConfig;
	}

	@Override
	public VehicleConfig getVehicleConfig(String vehicle, String band) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from VehicleConfig vc where vc.name = ? and vc.brand = ?");
		query.setString(0, vehicle);
		query.setString(1, band);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehicleConfig> vehicleConfigs = query.list();
		VehicleConfig vehicleConfig = vehicleConfigs.get(0);
		session.getTransaction().commit();
		return vehicleConfig;
	}
	
	@Override
	public void savePort(Port port) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(port);
		session.getTransaction().commit();
	}

	@Override
	public void saveEcu(Ecu ecu) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(ecu);
		session.getTransaction().commit();
	}

	@Override
	public void saveVehicleConfig(VehicleConfig vehicleConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(vehicleConfig);
		session.getTransaction().commit();
	}

	// @Override
	// public LinkContextReceiverFormatForPV getLinkContext4VPort(String
	// vehicleName,
	// String brandName, byte ecuId, String portName, int remotePluginPortId) {
	// Session session = sessionFactory.getCurrentSession();
	// session.beginTransaction();
	// Query query =
	// session.createQuery("from VehicleConfig vc where vc.name = ? and vc.brand = ?");
	// query.setString(0, vehicleName);
	// query.setString(1, brandName);
	// query.setMaxResults(1);
	// @SuppressWarnings("unchecked")
	// List<VehicleConfig> vehicleConfigs = query.list();
	// VehicleConfig vehicleConfig = vehicleConfigs.get(0);
	// Set<Ecu> ecus = vehicleConfig.getEcus();
	// Iterator<Ecu> iterator = ecus.iterator();
	// while(iterator.hasNext()) {
	// Ecu ecu = iterator.next();
	// if(ecu.getEcuID()==ecuId) {
	// Set<Port> ports = ecu.getPorts();
	// Iterator<Port> portIterator = ports.iterator();
	// while(portIterator.hasNext()) {
	// Port port = portIterator.next();
	// if(port.getName().equals(portName)) {
	// byte portId = port.getPortID();
	// LinkContextReceiverFormatForPV linkContext4VPort = new
	// LinkContextReceiverFormatForPV(portId, remotePluginPortId);
	// session.getTransaction().commit();
	// return linkContext4VPort;
	// }
	// }
	// break;
	// }
	// }
	// session.getTransaction().commit();
	// return null;
	// }

//	@Override
//	public byte getVPortId(String vehicleName, String brandName, byte ecuId,
//			String portName) {
//		Session session = sessionFactory.getCurrentSession();
//		session.beginTransaction();
//		Query query = session
//				.createQuery("from VehicleConfig vc where vc.name = ? and vc.brand = ?");
//		query.setString(0, vehicleName);
//		query.setString(1, brandName);
//		query.setMaxResults(1);
//		@SuppressWarnings("unchecked")
//		List<VehicleConfig> vehicleConfigs = query.list();
//		VehicleConfig vehicleConfig = vehicleConfigs.get(0);
//		Set<Ecu> ecus = vehicleConfig.getEcus();
//		Iterator<Ecu> iterator = ecus.iterator();
//		while (iterator.hasNext()) {
//			Ecu ecu = iterator.next();
//			if (ecu.getEcuID() == ecuId) {
//				Set<Port> ports = ecu.getPorts();
//				Iterator<Port> portIterator = ports.iterator();
//				while (portIterator.hasNext()) {
//					Port port = portIterator.next();
//					if (port.getName().equals(portName)) {
//						byte portId = port.getPortID();
//						session.getTransaction().commit();
//						return portId;
//					}
//				}
//				break;
//			}
//		}
//		session.getTransaction().commit();
//		return -1;
//	}

	@Override
	public int getSendingPortId(int vehicleConfigId, int recipientEcu) {
		int sendingPortId = -1;
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from VehicleConfig vc where vc.id = ?");
		query.setInteger(0, vehicleConfigId);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehicleConfig> vehicleConfigs = query.list();
		VehicleConfig vehicleConfig = vehicleConfigs.get(0);
		Set<Link> links = vehicleConfig.getLinks();
		for (Link link : links) {
			int type = link.getType();
			if(type == GlobalVariables.SWC_PORT_TYPE1 ) {
				int fromEcuId = link.getFromEcuId();
				if(fromEcuId == GlobalVariables.ECM) {
					int toEcuId = link.getToEcuId();
					if(toEcuId == recipientEcu) {
						sendingPortId = link.getFromPortId();
						break;
					}
				}
			}
		}
		session.getTransaction().commit();
		return sendingPortId;
	}

	@Override
	public int getCallbackPortId(int vehicleConfigId, int sendingEcu) {
		int callbackPortId = -1;
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from VehicleConfig vc where vc.id = ?");
		query.setInteger(0, vehicleConfigId);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehicleConfig> vehicleConfigs = query.list();
		VehicleConfig vehicleConfig = vehicleConfigs.get(0);
		Set<Link> links = vehicleConfig.getLinks();
		for (Link link : links) {
			int type = link.getType();
			if(type == GlobalVariables.SWC_PORT_TYPE1 ) {
				int fromEcuId = link.getFromEcuId();
				if(fromEcuId == sendingEcu) {
					int toEcuId = link.getToEcuId();
					if(toEcuId == GlobalVariables.ECM) {
						callbackPortId = link.getFromPortId();
						break;
					}
				}
			}
		}
		session.getTransaction().commit();
		return callbackPortId;
	}

	@Override
	public void saveLink(Link link) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(link);
		session.getTransaction().commit();
	}

	@Override
	public int[] getType2PortId(String vehicle, String brand, int fromEcuId, int toEcuId) {
		int[] result = new int[2];
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from VehicleConfig vc where vc.name = ? and vc.brand = ?");
		query.setString(0, vehicle);
		query.setString(1, brand);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehicleConfig> vehicleConfigs = query.list();
		VehicleConfig vehicleConfig = vehicleConfigs.get(0);
		Set<Link> links = vehicleConfig.getLinks();
		for(Link link:links) {
			int type = link.getType();
			int ffromEcuId = link.getFromEcuId();
			int ttoEcuId = link.getToEcuId();
			if(type == 2 && toEcuId == ttoEcuId && fromEcuId == ffromEcuId ) {
				int fromPortId = link.getFromPortId();
				int toPortId = link.getToPortId();
				session.getTransaction().commit();
				result[0] = fromPortId;
				result[1] = toPortId;
				return result;
			}
		}
		session.getTransaction().commit();
		return null;
	}
/*
	@Override
	public int getType2RPortId(String vehicle, String brand, int remoteEcuId) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from VehicleConfig vc where vc.name = ? and vc.brand = ?");
		query.setString(0, vehicle);
		query.setString(1, brand);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehicleConfig> vehicleConfigs = query.list();
		VehicleConfig vehicleConfig = vehicleConfigs.get(0);
		Set<Link> links = vehicleConfig.getLinks();
		for(Link link:links) {
			int type = link.getType();
			int fromEcuId = link.getFromEcuId();
			if(type == 2 && fromEcuId == remoteEcuId ) {
				int toPortId = link.getToPortId();
				session.getTransaction().commit();
				return toPortId;
			}
		}
		session.getTransaction().commit();
		return -1;
	}
*/
}
