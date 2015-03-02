package dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import common.AllocationStrategy;
import model.Vehicle;

@Repository
public class VehicleDaoImpl implements VehicleDao {
	private SessionFactory sessionFactory;
	private DBConnection db;
	
	public VehicleDaoImpl(DBConnection db) {
		this.db = db;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public byte generateEcuId(String vin,
			AllocationStrategy allocationStrategy, byte ecuReference) {
		byte ecuId = 0;
		switch (allocationStrategy) {
		case SPECIFIC:
			ecuId = ecuReference;
			break;
		case RANDOM:
//			Session session = sessionFactory.getCurrentSession();
//			session.beginTransaction();
//			Query query = session.createQuery("from Vehicle v where v.VIN = ?");
//			query.setString(0, vin);
//			query.setMaxResults(1);
//			@SuppressWarnings("unchecked")
//			List<Vehicle> vehicles = query.list();
//			Vehicle vehicle = vehicles.get(0);
//			Set<Ecu> ecus = vehicle.getEcus();
//			int size = ecus.size();
//			Random random = new Random();
//			int index = random.nextInt(size);
//			ecuId =  (byte) (index + 1);
//			session.getTransaction().commit();
			break;
		default:
			System.out
					.println("Error! Until now, only Random and Specific type of allocation strategy have been supported");
			break;
		}
		return ecuId;
	}

	public Vehicle getVehicle(String vin) {
		Vehicle vehicle = (Vehicle)db.getSingleResult("FROM Vehicle V where V.VIN = '" + vin + "'");
				
//		Vehicle vehicle = null;
//		
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		try {
//			Query query = session.createQuery("FROM Vehicle V where V.VIN = '" + vin + "'");
//
//			@SuppressWarnings("unchecked")
//			List<Vehicle> vehicles = query.list();
//			if (vehicles.isEmpty()) {
//				System.out.println("WARNING: NO VEHICLE WAS FOUND WITH VIN " + vin + " in the database");
//			}
//			else {
//				vehicle = vehicles.get(0);
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
		
		return vehicle; 
	}

	public void addApp(String vin, int appId) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Vehicle v where v.VIN = ?");
		query.setString(0, vin);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Vehicle> vehicles = query.list();
		Vehicle vehicle = vehicles.get(0);
		String installed_APPS = vehicle.getINSTALLED_APPS();
		if(installed_APPS == null || installed_APPS.equals("")) {
			installed_APPS += appId;
		} else {
			installed_APPS += ",";
			installed_APPS += appId;
		}
		vehicle.setINSTALLED_APPS(installed_APPS);
		session.update(vehicle);
		session.getTransaction().commit();  
	}

	public void removeOneAppId(String vin, int uninstalledAppId) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Vehicle v where v.VIN = ?");
		query.setString(0, vin);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Vehicle> vehicles = query.list();
		Vehicle vehicle = vehicles.get(0);
		vehicle.removeApp(uninstalledAppId);
		session.update(vehicle);
		session.getTransaction().commit();  
	}

	public void saveVehicle(Vehicle vehicle) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(vehicle);
		session.getTransaction().commit();
	}
}
