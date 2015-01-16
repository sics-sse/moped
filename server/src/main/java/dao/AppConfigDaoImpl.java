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

	public int saveAppConfig(AppConfig appConfig) {
		AppConfig storedConfig = getAppConfig(appConfig.getAppId(),  
			 	   appConfig.getVehicleName(), appConfig.getBrand());
		if (storedConfig != null) {
			return storedConfig.getId();
		}
		else {
			return db.addEntry(appConfig);
		}
	}
	
	public AppConfig getAppConfig(int id) {
		AppConfig config = (AppConfig)db.getSingleResult(
				"FROM AppConfig ac WHERE ac.id = " + id);

		return config;
	}

	public AppConfig getAppConfig(int appId, String vehicleName, String vehicleBrand) {
		AppConfig config = (AppConfig)db.getSingleResult(
				"FROM AppConfig ac WHERE ac.appId = " + appId + 
				" AND ac.vehicleName = '" + vehicleName + "'" +  
				" AND ac.brand = '" + vehicleBrand + "'");

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
	
	private PluginConfig getPluginConfig(PluginConfig config) {
		PluginConfig storedConfig = (PluginConfig)db.getSingleResult(
				"FROM PluginConfig pc WHERE pc.name = '" + config.getName() + "'" +  
				" AND pc.ecuId = " + config.getEcuId());

		return storedConfig;
	}

	public int savePluginConfig(PluginConfig pluginConfig) {
//		Session session = sessionFactory.getCurrentSession();
//		session.beginTransaction();
//		session.save(pluginConfig);
//		session.getTransaction().commit();
		
		PluginConfig storedPluginConfig = getPluginConfig(pluginConfig); 
		if (storedPluginConfig != null) {
			return storedPluginConfig.getId();
		}
		else {
			return db.addEntry(pluginConfig);
		}
	}

	public void savePluginLinkConfig(PluginLinkConfig pluginLinkConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(pluginLinkConfig);
		session.getTransaction().commit();
	}
}
