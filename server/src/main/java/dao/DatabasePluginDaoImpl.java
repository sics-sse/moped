package dao;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import model.AppConfig;
import model.DatabasePlugin;

@Repository
public class DatabasePluginDaoImpl implements DatabasePluginDao {
	private SessionFactory sessionFactory;

	private DBConnection db;
	
	public DatabasePluginDaoImpl(DBConnection db) {
		this.db = db;
	}
	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public DatabasePlugin getDatabasePlugin(int databasePluginId) {
		DatabasePlugin plugin = (DatabasePlugin)db.getSingleResult(
				"FROM DatabasePlugin d WHERE d.id = " + databasePluginId);

		return plugin;
	}
	
	public DatabasePlugin getDatabasePlugin(DatabasePlugin dbPlugin) {
		//TODO: Perhaps we should have fewer entries here...
		DatabasePlugin plugin = (DatabasePlugin)db.getSingleResult(
				"FROM DatabasePlugin d WHERE d.fullClassName = '" + dbPlugin.getFullClassName() + "'" + 
				" AND d.location = '" + dbPlugin.getLocation() + "'" +
				" AND d.name = '" + dbPlugin.getName() + "'" +
				" AND d.zipLocation = '" + dbPlugin.getZipLocation() + "'" +
				" AND d.zipName = '" + dbPlugin.getZipName() + "'" +
				" AND d.reference = " + dbPlugin.getReference() + 
				" AND d.application = " + dbPlugin.getApplication().getApplicationId());
		
		return plugin;
	}

	public int saveDatabasePlugin(DatabasePlugin databasePlugin) {
//		Session session = sessionFactory.getCurrentSession();
//		session.beginTransaction();
//		session.save(databasePlugin);
//		session.getTransaction().commit();  
		
		DatabasePlugin storedPlugin = getDatabasePlugin(databasePlugin);
		if (storedPlugin != null) {
			return storedPlugin.getId();
		}
		else {
			return db.addEntry(databasePlugin);
		}
	}

}
