package dao;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import model.AppConfig;
import model.PluginConfig;
import model.PluginLinkConfig;
import model.PluginPortConfig;

@Repository
public class AppConfigDaoImpl implements AppConfigDao {
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void saveAppConfig(AppConfig appConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(appConfig);
		session.getTransaction().commit();
	}

	@Override
	public AppConfig getAppConfig(int appId, String vehicleName, String vehicleBrand) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from AppConfig ac where ac.appId = ? and ac.vehicleName = ? and ac.brand = ?");
		query.setInteger(0, appId);
		query.setString(1, vehicleName);
		query.setString(2, vehicleBrand);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<AppConfig> appConfigs = query.list();
		session.getTransaction().commit();
		return appConfigs.get(0);
	}

	@Override
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

	@Override
	public void savePluginPortConfig(PluginPortConfig pluginPortConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(pluginPortConfig);
		session.getTransaction().commit();
	}

	@Override
	public void savePluginConfig(PluginConfig pluginConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(pluginConfig);
		session.getTransaction().commit();
	}

	@Override
	public void savePluginLinkConfig(PluginLinkConfig pluginLinkConfig) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(pluginLinkConfig);
		session.getTransaction().commit();
	}
}
