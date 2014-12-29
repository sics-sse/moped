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

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
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

	@Override
	public Vehicle getVehicle(String vin) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Vehicle v where v.VIN = ?");
		query.setString(0, vin);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Vehicle> vehicles = query.list();
		session.getTransaction().commit();  
		return vehicles.get(0);
	}

	@Override
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

	@Override
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

	@Override
	public void saveVehicle(Vehicle vehicle) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(vehicle);
		session.getTransaction().commit();
	}
}
