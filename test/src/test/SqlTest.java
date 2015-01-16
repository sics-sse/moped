package test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class SqlTest {
	private SessionFactory sf = null;
	
	public static void main(String[] args) {
		SqlTest sql = new SqlTest();
		
		HashSet<Ecu> ecus = new HashSet<Ecu>();
		ecus.add(sql.getUniqueEcu("moped_tcu"));
		ecus.add(sql.getUniqueEcu("moped_vcu"));
		System.out.println("Added moped_3rpi_v1.0 with  id " + sql.addUniqueVehicleConfig("moped_3rpi", "1.0", ecus));
		
		ecus.add(sql.getUniqueEcu("moped_scu"));
		System.out.println("Added moped_3rpi_v1.1 with  id " + sql.addUniqueVehicleConfig("moped_3rpi", "1.1", ecus));
		
		System.out.println("Listing...");
		sql.listVehicleConfigs();
	}
	
	public SqlTest() {
		System.out.println("Sql test");
		
		try {
			Configuration config = new Configuration();
			config.configure();

			sf = config.buildSessionFactory(new StandardServiceRegistryBuilder().
					applySettings(config.getProperties()).build());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public int addVehicleConfig(String model, String version, HashSet<Ecu> ecus) {
		Session session = sf.openSession();
		Transaction tx = session.beginTransaction();
		
		VehicleConfig config = new VehicleConfig(model, version);
		config.setEcus(ecus);
		Integer id = (Integer)session.save(config);
		
		try {
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) 
				tx.rollback();
			ex.printStackTrace();
		} finally {
			session.close();
		}
		
		return id.intValue();
	}
	
	/* Method to list all the employees detail */
	public void listVehicleConfigs( ){
		Session session = sf.openSession();
		Transaction tx = session.beginTransaction();

		try{
			List configs = session.createQuery("FROM VehicleConfig").list(); 
			for (Iterator iterator1 = configs.iterator(); iterator1.hasNext();) {
				VehicleConfig config = (VehicleConfig) iterator1.next(); 
				
		        System.out.print("Model: " + config.getModel()); 
		        System.out.print("  Version: " + config.getVersion());
		        System.out.println("  id: " + config.getId());
		        
		        for (Iterator iterator2 = config.getEcus().iterator(); iterator2.hasNext();){
		        	Ecu ecu = (Ecu) iterator2.next(); 
		            System.out.print("\tEcu: " + ecu.getName()); 
		            System.out.println("  id: " + ecu.getId());
		        }
			}
			
			tx.commit();
      } catch (HibernateException e) {
    	  if (tx != null) 
    		  tx.rollback();
    	  e.printStackTrace(); 
      } finally {
    	  session.close(); 
      }
   }
	
	private Ecu getEcu(String name) {
		Session session = sf.openSession();
		Transaction tx = session.beginTransaction();
		Ecu ecu = null;
		
		try {
			List matchList = session.createQuery("FROM Ecu E WHERE E.name = '" + name + "'").list();
			if (!matchList.isEmpty())
				ecu = (Ecu)matchList.get(0);
		}
		catch (HibernateException ex) {
			if (tx != null) 
				tx.rollback();
			ex.printStackTrace(); 
		} finally {
			session.close();
		}
		
		return ecu;
	}
	
	private VehicleConfig getVehicleConfig(String model, String version) {
		Session session = sf.openSession();
		Transaction tx = session.beginTransaction();
		VehicleConfig config = null;
		
		try {
			List matchList = session.createQuery("FROM VehicleConfig V WHERE V.model = '" + 
					model + "' AND version = '" + version + "'").list();
			if (!matchList.isEmpty())
				config = (VehicleConfig)matchList.get(0);
		}
		catch (HibernateException ex) {
			if (tx != null) 
				tx.rollback();
			ex.printStackTrace(); 
		} finally {
			session.close();
		}
		
		return config;
	}
	
	private Ecu getUniqueEcu(String ecuName) {
		Ecu ecu = getEcu(ecuName);
		
		if (ecu == null)
			ecu = new Ecu(ecuName);
		
		return ecu;
	}
	
	private int addUniqueVehicleConfig(String model, String version, HashSet<Ecu> ecus) {
		int id;
		
		VehicleConfig config = getVehicleConfig(model, version);
		if (config == null) {
			id = addVehicleConfig(model, version, ecus);
		}
		else {
			id = config.getId();
		}
		
		return id;
	}
	
}
