package dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class DBConnection {
	
	private SessionFactory sqlSessionFactory;
	
	public DBConnection() {
		try {
			Configuration config = new Configuration();
//			config.setProperty("hibernate.show_sql", "true"); //TEMP_DBG
			config.configure();
			
			sqlSessionFactory = config.buildSessionFactory(new StandardServiceRegistryBuilder().
					applySettings(config.getProperties()).build());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public List getAllResults(String query) {
		List results = null;
		
		Session session = sqlSessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		
		try {
			Query queryRes = session.createQuery(query);

			results = queryRes.list();
			if (results.isEmpty()) {
				System.out.println("WARNING: NO RESULTS WERE FOUND FOR QUERY: \"" + query + "\"");
			}
			else {}
			
			tx.commit();
		} catch (HibernateException ex) {
			if (tx != null) 
				tx.rollback();
			ex.printStackTrace(); 
		} finally {
			session.close();
		}
		
		return results;
	}
	
	@SuppressWarnings("rawtypes")
	public Object getSingleResult(String query) {
		List results = getAllResults(query);
		
		if (results == null ||
				results.isEmpty())
			return null;
		else
			return results.get(0);
	}
	
	public <T> int addEntry(T entry) {
		Session session = sqlSessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		
		Integer id = (Integer)session.save(entry);
		
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
}
