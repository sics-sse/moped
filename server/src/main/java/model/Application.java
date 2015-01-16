package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="Application")
public class Application implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int applicationId;
	private String applicationName;
	private String publisher;
	private String version;
	private Set<DatabasePlugin> databasePlugins = new HashSet<DatabasePlugin>();
	private boolean hasNewVersion;

	public Application() {
	}

	public Application(String applicationName, String publisher, String version) {
		this.applicationName = applicationName;
		this.publisher = publisher;
		this.version = version;
		hasNewVersion = false;
	}

	@Id
	@GeneratedValue
	public int getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(int applicationId) {
		this.applicationId = applicationId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@OneToMany(mappedBy = "application", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<DatabasePlugin> getDatabasePlugins() {
		return databasePlugins;
	}

	public void setDatabasePlugins(Set<DatabasePlugin> databasePlugins) {
		this.databasePlugins = databasePlugins;
	}

	public boolean isHasNewVersion() {
		return hasNewVersion;
	}

	public void setHasNewVersion(boolean hasNewVersion) {
		this.hasNewVersion = hasNewVersion;
	}
}
