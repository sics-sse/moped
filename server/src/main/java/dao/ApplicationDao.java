package dao;

import model.Application;

public interface ApplicationDao {
	public void saveApplication(Application application);
	public String getVersion(String applicationName);
	public Application getApplication(int appID);
	public void setHasNewVersionFlag(int oldAppID);
	public int getNewestApplication(int oldAppID);
	public boolean hasApplication(String name, String version);
	public void updateApplicationVersion(String applicationName, String version);
}

