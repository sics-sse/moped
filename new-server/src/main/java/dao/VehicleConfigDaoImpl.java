package dao;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import service.PluginWebServicePublisher;
import common.GlobalVariables;
import model.Application;
import model.Ecu;
import model.Link;
import model.Port;
import model.Vehicle;
import model.VehicleConfig;

@Repository
public class VehicleConfigDaoImpl implements VehicleConfigDao {
	private SessionFactory sessionFactory = null;
	private DBConnection db;
	
	public VehicleConfigDaoImpl(DBConnection db) {
		this.db = db;
	}
	
	public VehicleConfigDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	//TODO: This kind of operations could be encapsulated
	public VehicleConfig getVehicleConfig(int vehicleConfigId) {
		VehicleConfig config = (VehicleConfig)db.getSingleResult(
				"from VehicleConfig v where v.id = '" + vehicleConfigId + "'");
		
//		VehicleConfig config = null;
//		
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		try {
//			Query query = session.createQuery("from VehicleConfig v where v.id = '" + vehicleConfigId + "'");
//
//			@SuppressWarnings("unchecked")
//			List<VehicleConfig> configs = query.list();
//			if (configs.isEmpty()) {
//				System.out.println("WARNING: NO CONFIGURATION WAS FOUND FOR VEHICLE WITH ID " + vehicleConfigId + " in the database");
//			} 
//			else {
//				config = configs.get(0);
//			}
//			
//			tx.commit();
//		} catch (HibernateException ex) {
//			if (tx != null) 
//				tx.rollback();
//			ex.printStackTrace(); 
//		} finally {
//			session.close();
//		}

		return config;
	}

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
	
	public void savePort(Port port) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(port);
		session.getTransaction().commit();
	}

	public void saveEcu(Ecu ecu) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(ecu);
		session.getTransaction().commit();
	}
	
//	//TODO: AK_new
//	private test.Ecu getEcu(String ecuName) {
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		test.Ecu ecu = null;
//		
//		try {
//			List matchList = session.createQuery("FROM Ecu E WHERE E.name = '" + ecuName + "'").list();
//			if (!matchList.isEmpty())
//				ecu = (test.Ecu)matchList.get(0);
//			else
//				ecu = new test.Ecu(ecuName);
//		}
//		catch (HibernateException ex) {
//			if (tx != null) 
//				tx.rollback();
//			ex.printStackTrace(); 
//		} finally {
//			session.close();
//		}
//		
//		return ecu;
//	}

	public void saveVehicleConfig(VehicleConfig vehicleConfig) {
		//TODO: See http://www.tutorialspoint.com/hibernate/hibernate_sessions.htm
		// 		The session objects should not be kept open for a long time because 
		//		they are not usually thread safe and they should be created and destroyed them as needed. 
		//Thus: Session session = factory.openSession();
		//		Transaction tx = session.beginTransaction();
		//		tx.commit();
		//		finally { session.close(); }
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(vehicleConfig);
		session.getTransaction().commit();
	}
	
//	//TODO: AK_new
//	public void newSaveVehicleConfig(String model, String version, List<String> ecuNames) {
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		try {
//			System.out.println("searching for model: '" + model + "'; version: '" + version + "'");
//			List matchList = session.createQuery("FROM VehicleConfig V WHERE V.model = '" + 
//					model + "' AND version = '" + version + "'").list();
//			System.out.println("matchList.size: " + matchList.size());
//			
//			if (matchList.isEmpty()) {
//				test.VehicleConfig config = new test.VehicleConfig(model, version);
//			
//				HashSet<test.Ecu> ecus = new HashSet<test.Ecu>(ecuNames.size());
//				for (Iterator<String> iterator = ecuNames.iterator(); iterator.hasNext();) {
//					ecus.add(getEcu(iterator.next()));
//				}
//				config.setEcus(ecus);
//				
//				session.save(config);
//			}
//			
//			tx.commit();
//		} catch (HibernateException ex) {
//			if (tx != null) 
//				tx.rollback();
//			ex.printStackTrace(); 
//		} finally {
//			session.close();
//		}
//	}

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

	public int getSendingPortId(int vehicleConfigId, int recipientEcu) {
		int sendingPortId = -1;
		
//		VehicleConfig config = (VehicleConfig)db.getSingleResult(
//				"FROM VehicleConfig vc WHERE vc.id = " + vehicleConfigId);
		
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
		
//		try {
//			Query query = session.
//					createQuery("FROM VehicleConfig vc where vc.id = " + vehicleConfigId);
//
//			@SuppressWarnings("unchecked")
//			List<VehicleConfig> configs = query.list();
//			if (!configs.isEmpty()) {
//				System.out.println("WARNING: NO VEHICLE CONFIGURATION WAS FOUND FOR VEHICLE_CONFIG_ID " + 
//									vehicleConfigId + " in the database");
//			} 
//			else {
		
//				Set<Link> links = configs.get(0).getLinks();
//		if (config != null) {
			
		@SuppressWarnings("unchecked")
		List<Link> links = (List<Link>)db.getAllResults(
				"FROM Link l WHERE l.vehicleConfig = " + vehicleConfigId);
			
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
		
//		}
			
//			tx.commit();
//		} catch (HibernateException ex) {
//			if (tx != null) 
//				tx.rollback();
//			ex.printStackTrace(); 
//		} finally {
//			session.close();
//		}
		
		return sendingPortId;
	}

	//TODO: compare with getSendingPortId() and refactor
	public int getCallbackPortId(int vehicleConfigId, int sendingEcu) {
		int callbackPortId = -1;
		
//		List<VehicleConfig> configs = db.getAllResults("FROM VehicleConfig vc WHERE vc.id = " + vehicleConfigId);
		
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		try {
//			Query query = session.
//					createQuery("FROM VehicleConfig vc WHERE vc.id = " + vehicleConfigId);
//
//			@SuppressWarnings("unchecked")
//			List<VehicleConfig> configs = query.list();
//			if (!configs.isEmpty()) {
//				System.out.println("WARNING: NO VEHICLE CONFIGURATION WAS FOUND FOR VEHICLE_CONFIG_ID " + 
//									vehicleConfigId + " in the database");
//			} 
//			else {
//				Set<Link> links = configs.get(0).getLinks();
		
		@SuppressWarnings("unchecked")
		List<Link> links = (List<Link>)db.getAllResults(
				"FROM Link l WHERE l.vehicleConfig = " + vehicleConfigId);
		
		for (Link link : links) {
			int type = link.getType();
			if(type == GlobalVariables.SWC_PORT_TYPE1 ) {
				int fromEcuId = link.getFromEcuId();
				if(fromEcuId == sendingEcu) {
					int toEcuId = link.getToEcuId();
					if(toEcuId == GlobalVariables.ECM) {
						callbackPortId = link.getFromPortId(); //TODO: Is this really correct???
						break;
					}
				}
			}
		}
//			}
			
//			tx.commit();
//		} catch (HibernateException ex) {
//			if (tx != null) 
//				tx.rollback();
//			ex.printStackTrace(); 
//		} finally {
//			session.close();
//		}
		
		return callbackPortId;
	}

	public void saveLink(Link link) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(link);
		session.getTransaction().commit();
	}

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
