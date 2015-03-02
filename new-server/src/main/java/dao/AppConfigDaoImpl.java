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
		
	public PluginConfig getPluginConfig(PluginConfig config) {
		PluginConfig storedConfig = (PluginConfig)db.getSingleResult(
				"FROM PluginConfig pc WHERE pc.name = '" + config.getName() + "'" +  
				" AND pc.ecuId = " + config.getEcuId() + 
				" AND pc.appConfig = " + config.getAppConfig().getId());

		return storedConfig;
	}

	public int savePluginConfig(PluginConfig pluginConfig) {
		PluginConfig storedPluginConfig = getPluginConfig(pluginConfig); 
		if (storedPluginConfig != null) {
			return storedPluginConfig.getId();
		}
		else {
			return db.addEntry(pluginConfig);
		}
	}
	
	public PluginPortConfig getPluginPortConfig(PluginPortConfig config) {
		System.out.println("Searching for PPC with name: " + config.getName() + 
				" and pluginConfig_id: " + config.getPluginConfig().getId());
		PluginPortConfig storedConfig = (PluginPortConfig)db.getSingleResult(
				"FROM PluginPortConfig pc WHERE pc.name = '" + config.getName() + "'" +  
				" AND pc.pluginConfig = " + config.getPluginConfig().getId());

		return storedConfig;
	}

	public int savePluginPortConfig(PluginPortConfig pluginPortConfig) {	
		PluginPortConfig storedConfig = getPluginPortConfig(pluginPortConfig); 
		if (storedConfig != null) {
//			System.out.println("Found port: " + pluginPortConfig.getName() + ", id: " + storedConfig.getId());
			return storedConfig.getId();
		}
		else {
//			System.out.println("Inserting port: " + pluginPortConfig.getName());
			return db.addEntry(pluginPortConfig);
		}
	}
	
	public PluginLinkConfig getPluginLinkConfig(PluginLinkConfig config) {
		PluginLinkConfig storedConfig = (PluginLinkConfig)db.getSingleResult(
				"FROM PluginLinkConfig pc WHERE pc.fromStr = '" + config.getFromStr() + "'" + 
				" AND pc.toStr = '" + config.getToStr() + "'" +
				" AND pc.remote = '" + config.getRemote() + "'" + 
				" AND pc.pluginConfig = " + config.getPluginConfig().getId());

		return storedConfig;
	}

	public int savePluginLinkConfig(PluginLinkConfig pluginLinkConfig) {
		PluginLinkConfig storedConfig = getPluginLinkConfig(pluginLinkConfig); 
		if (storedConfig != null) {
//			System.out.println("Found link from " + storedConfig.getFromStr() + 
//					" to " + storedConfig.getToStr() + " with id: "+ storedConfig.getId());
			return storedConfig.getId();
		}
		else {
//			System.out.println("Inserting link from " + storedConfig.getFromStr() + 
//					" to " + storedConfig.getToStr());
			return db.addEntry(pluginLinkConfig);
		}
	}
}
