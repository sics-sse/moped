package dao;

import model.DatabasePlugin;

public interface DatabasePluginDao {
	public DatabasePlugin getDatabasePlugin(int databasePluginId);
	public int saveDatabasePlugin(DatabasePlugin databasePlugin);
}

