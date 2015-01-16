package dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;

import service.PluginWebServicePublisher;
import model.AppConfig;
import model.Application;

@Repository
public class ApplicationDaoImpl implements ApplicationDao {
	private SessionFactory sessionFactory;
	
	private DBConnection db;
	
	public ApplicationDaoImpl(DBConnection db) {
		this.db = db;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void saveApplication(Application application) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(application);
		session.getTransaction().commit();
	}

	public String getVersion(String applicationName) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Application p where p.applicationName = ?");
		query.setString(0, applicationName);
		query.setMaxResults(1);
		Application plugin = (Application) query.uniqueResult();
		String version = plugin.getVersion();
		session.getTransaction().commit();
		return version;
	}

	//TODO: Encapsulate
	public Application getApplication(int appID) {
		Application app = (Application)db.getSingleResult(
				"FROM Application a WHERE a.applicationId = " + appID);
		
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		try {
//			Query query = session.
//					createQuery("FROM Application a WHERE a.applicationId = " + appID);
//
//			@SuppressWarnings("unchecked")
//			List<Application> apps = query.list();
//			if (apps.isEmpty()) {
//				System.out.println("WARNING: NO APPLICATION WAS FOUND FOR APP_ID " + appID + " in the database");
//			} 
//			else {
//				app = apps.get(0);
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

		return app;
	}

	public void setHasNewVersionFlag(int oldAppID) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Application a where a.applicationId = :oldAppID");
		query.setInteger("oldAppID", oldAppID);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Application> apps = query.list();
		apps.get(0).setHasNewVersion(true);
		session.getTransaction().commit();
	}

	public int getNewestApplication(int oldAppID) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Application a where a.applicationId = :appID");
		query.setInteger("appID", oldAppID);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Application> apps = query.list();
		
		if (apps.isEmpty())
			return -1;
		
		Application app = apps.get(0);
		String name = app.getApplicationName();
		query = session
				.createQuery("select Max(applicationId) from Application a where a.applicationName = :name");
		query.setString("name", name);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Integer> appIds = query.list();
		int appId = appIds.get(0);
		session.getTransaction().commit();
		return appId;
	}

	public boolean hasApplication(String name, String version) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Application a where a.applicationName = :name and a.version = :version ");
		query.setString("name", name);
		query.setString("version", version);
		@SuppressWarnings("unchecked")
		List<Application> apps = query.list();
		if (apps.isEmpty() == true) {
			return false;
		} else {
			return true;
		}
	}

	public void updateApplicationVersion(String applicationName, String version) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Application a where a.applicationName = :applicationName");
		query.setString("applicationName", applicationName);
		@SuppressWarnings("unchecked")
		List<Application> apps = query.list();
		for(Application application:apps) {
			String version2 = application.getVersion();
			if(version.compareToIgnoreCase(version2) > 0) {
				application.setHasNewVersion(true);
			}
		}
		session.getTransaction().commit();
	}

}
