package dao;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import model.DatabasePlugin;

@Repository
public class DatabasePluginDaoImpl implements DatabasePluginDao {
	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public DatabasePlugin getDatabasePlugin(int databasePluginId) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Query query = session.createQuery("from DatabasePlugin d where d.id = :databasePluginId");
		query.setInteger("databasePluginId", databasePluginId);
		query.setMaxResults(1);
		@SuppressWarnings("unchecked")
		List<DatabasePlugin> dps = query.list();
		DatabasePlugin databasePlugin = dps.get(0);
		session.getTransaction().commit();  
		return databasePlugin;
	}

	@Override
	public void saveDatabasePlugin(DatabasePlugin databasePlugin) {
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		session.save(databasePlugin);
		session.getTransaction().commit();  
	}

}
