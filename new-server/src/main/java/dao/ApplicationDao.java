package dao;

import model.Application;

public interface ApplicationDao {
	public int saveApplication(Application application);
	public String getVersion(String applicationName);
	public Application getApplication(int appID);
	public Application getApplication(String name, String version);
	public void setHasNewVersionFlag(int oldAppID);
	public int getNewestApplication(int oldAppID);
	public boolean hasApplication(String name, String version);
	public void updateApplicationVersion(String applicationName, String version);
}

