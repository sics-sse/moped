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
import model.PluginConfig;
import model.PluginLinkConfig;
import model.PluginPortConfig;
import model.VehicleConfig;

@Repository
public class AppConfigDaoImpl implements AppConfigDao {
	private SessionFactory sessionFactory;
	
	private DBConnection db;
	
	public AppConfigDaoImpl(DBConnection db) {
		this.db = db;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void saveAppConfig(AppConfig appConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(appConfig);
		session.getTransaction().commit();
	}

	//TODO: Encapsulate
	public AppConfig getAppConfig(int appId, String vehicleName, String vehicleBrand) {
		AppConfig config = (AppConfig)db.getSingleResult(
				"FROM AppConfig ac WHERE ac.appId = " + appId + 
				" AND ac.vehicleName = '" + vehicleName + "'" +  
				" AND ac.brand = '" + vehicleBrand + "'");
		
//		Session session = PluginWebServicePublisher.sqlSessionFactory.openSession();
//		Transaction tx = session.beginTransaction();
//		
//		try {
//			Query query = session.
//					createQuery("FROM AppConfig ac WHERE ac.appId = " + appId + 
//							" AND ac.vehicleName = '" + vehicleName + "'" +  
//							" AND ac.brand = '" + vehicleBrand + "'");
//
//			@SuppressWarnings("unchecked")
//			List<AppConfig> configs = query.list();
//			if (configs.isEmpty()) {
//				System.out.println("WARNING: NO CONFIGURATION WAS FOUND FOR APP WITH ID " + appId + " in the database");
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

	public List<AppConfig> getAppConfigs(int appId) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from AppConfig ac where ac.appId = ?");
		query.setInteger(0, appId);
		@SuppressWarnings("unchecked")
		List<AppConfig> appConfigs = query.list();
		session.getTransaction().commit();
		return appConfigs;
	}

	public void savePluginPortConfig(PluginPortConfig pluginPortConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(pluginPortConfig);
		session.getTransaction().commit();
	}

	public void savePluginConfig(PluginConfig pluginConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(pluginConfig);
		session.getTransaction().commit();
	}

	public void savePluginLinkConfig(PluginLinkConfig pluginLinkConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(pluginLinkConfig);
		session.getTransaction().commit();
	}
}
