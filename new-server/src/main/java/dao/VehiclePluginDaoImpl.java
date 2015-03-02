package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import cache.VehiclePluginRecord;
import messages.LinkContextEntry;
import model.VehiclePlugin;

@Repository
public class VehiclePluginDaoImpl implements VehiclePluginDao {
	private short currentMaxPluginId;
	private SessionFactory sessionFactory = null;
	
	private DBConnection db;
	
	public VehiclePluginDaoImpl(DBConnection db) {
		this.db = db;
	}
	
	public VehiclePluginDaoImpl(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	public short getCurrentMaxPluginId() {
		return currentMaxPluginId;
	}

	public void setCurrentMaxPluginId(short currentMaxPluginId) {
		this.currentMaxPluginId = currentMaxPluginId;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public VehiclePlugin getVehiclePlugin(String vin, String pluginName) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("from VehiclePlugin vp where vp.vin = ? and vp.name = ?");
		query.setString(0, vin);
		query.setString(1, pluginName);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehiclePlugin> vehiclePlugins = query.list();
		VehiclePlugin vehiclePlugin = vehiclePlugins.get(0);
		session.getTransaction().commit();
		return vehiclePlugin;
	}

//	public synchronized short generateVehiclePluginID(String vin) {
//		if (currentMaxPluginId != -1) {
//			currentMaxPluginId = (short) (currentMaxPluginId + 1);
//			return currentMaxPluginId;
//		} else {
//			Session session = sessionFactory.getCurrentSession();
//			session.beginTransaction();
//			Query query = session.createQuery("from Vehicle v where v.VIN = ?");
//			query.setString(0, vin);
//			query.setMaxResults(1);
//			@SuppressWarnings("unchecked")
//			List<Vehicle> ids = query.list();
//			Vehicle vehicle = ids.get(0);
//			Set<VehiclePlugin> vehiclePluginSet = vehicle.getVehiclePlugins();
//			short maxVehiclePluginID = getMaxVehiclePluginID(vehiclePluginSet);
//			currentMaxPluginId = (short) (maxVehiclePluginID + 1);
//			session.getTransaction().commit();
//			return currentMaxPluginId;
//		}
//	}

	public void saveVehiclePlugin(VehiclePlugin vehiclePlugin) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(vehiclePlugin);
		session.getTransaction().commit();
	}

//	public void setIsInstallFlag(String vin, String vehiclePluginName) {
//		Session session = sessionFactory.getCurrentSession();
//		session.beginTransaction();
//		Query query = session.createQuery("from Vehicle v where v.VIN = ?");
//		query.setString(0, vin);
//		query.setMaxResults(1);
//		@SuppressWarnings("unchecked")
//		List<Vehicle> vehicles = query.list();
//		Vehicle vehicle = vehicles.get(0);
//		Set<VehiclePlugin> vehiclePluginSet = vehicle.getVehiclePlugins();
//		for (VehiclePlugin vp : vehiclePluginSet) {
//			String vpName = vp.getVehilePluginName();
//			if (vpName.equals(vehiclePluginName))
//				vp.setInstalled(true);
//		}
//		session.getTransaction().commit();
//	}

	public synchronized void removeVehiclePlugin(String vin,
			String vehiclePluginName) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("delete VehiclePlugin vp where vp.vin = ? and vp.name = ?");
		query.setString(0, vin);
		query.setString(1, vehiclePluginName);
		query.executeUpdate();
		session.getTransaction().commit();
	}

	public List<VehiclePlugin> getPluginsInSpecificEcu(String vin,
			int ecuReference) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("from VehiclePlugin vp where vp.vin = ? and vp.ecuId = ?");
		query.setString(0, vin);
		query.setInteger(1, ecuReference);
		@SuppressWarnings("unchecked")
		List<VehiclePlugin> vehiclePlugins = query.list();
		session.getTransaction().commit();
		return vehiclePlugins;
	}

	public List<VehiclePlugin> getVehilePlugins(String vin, int appId) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("from VehiclePlugin vp where vp.vin = ? and vp.appId = ?");
		query.setString(0, vin);
		query.setInteger(1, appId);
		@SuppressWarnings("unchecked")
		List<VehiclePlugin> vehiclePlugins = query.list();
		session.getTransaction().commit();
		return vehiclePlugins;
	}

	public void saveVehiclePlugin(String vin, int appId, VehiclePluginRecord record) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		String pluginName = record.getPluginName();
		int ecuId = record.getEcuId();
		int sendingPortId = record.getSendingPortId();
		int callbackPortId = record.getCallbackPortId();
		HashMap<String,Integer> portInitialContext = record.getPortInitialContext();
		ArrayList<LinkContextEntry> linkingContext = record.getLinkingContext();
		String location = record.getLocation();
		String executablePluginName = record.getExecutablePluginName();
		
		VehiclePlugin vehiclePlugin = new VehiclePlugin(vin, pluginName, appId, ecuId, sendingPortId, callbackPortId, portInitialContext, linkingContext, location, executablePluginName);
		session.save(vehiclePlugin);
		session.getTransaction().commit();
	}

	public int getApplicationId(String vin, String pluginName) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("from VehiclePlugin vp where vp.vin = ? and vp.name = ?");
		query.setString(0, vin);
		query.setString(1, pluginName);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<VehiclePlugin> vehiclePlugins = query.list();
		VehiclePlugin vehiclePlugin = vehiclePlugins.get(0);
		session.getTransaction().commit();
		return vehiclePlugin.getAppId();
	}
}
