package dao;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import model.Application;

@Repository
public class ApplicationDaoImpl implements ApplicationDao {
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void saveApplication(Application application) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(application);
		session.getTransaction().commit();
	}

	@Override
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

	@Override
	public Application getApplication(int appID) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Application a where a.applicationId = :appID");
		query.setInteger("appID", appID);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Application> apps = query.list();
		session.getTransaction().commit();
		return apps.get(0);
	}

	@Override
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

	@Override
	public int getNewestApplication(int oldAppID) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session
				.createQuery("from Application a where a.applicationId = :appID");
		query.setInteger("appID", oldAppID);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<Application> apps = query.list();
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

	@Override
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

	@Override
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
