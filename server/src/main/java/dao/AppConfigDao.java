package dao;


import java.util.List;
import model.AppConfig;
import model.PluginConfig;
import model.PluginLinkConfig;
import model.PluginPortConfig;

public interface AppConfigDao {
	public int saveAppConfig(AppConfig appConfig);
	public int savePluginConfig(PluginConfig pluginConfig);
	public int savePluginPortConfig(PluginPortConfig pluginPortConfig);
	public int savePluginLinkConfig(PluginLinkConfig pluginLinkConfig);
	public AppConfig getAppConfig(int id);
	public AppConfig getAppConfig(int appId, String vehicleName, String vehicleBrand);
	public PluginConfig getPluginConfig(PluginConfig config);
	public PluginPortConfig getPluginPortConfig(PluginPortConfig config);
	public List<AppConfig> getAppConfigs(int appId);
}

