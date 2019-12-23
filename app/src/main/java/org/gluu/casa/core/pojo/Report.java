package org.gluu.casa.core.pojo;

import java.util.List;

import org.gluu.casa.core.model.CredentialsActiveUsersSummary;
import org.gluu.casa.core.model.PluginActiveUsersSummary;

public class Report {

	private String serverName;
	private String email;
	private String month;
	private String year;
	private int daysCovered;
	private String generatedOn;
	
	private List<PluginActiveUsersSummary> plugins;
	private List<CredentialsActiveUsersSummary> credentials;
	public String getServerName() {
		return serverName;
	}
	public int getDaysCovered() {
		return daysCovered;
	}
	public void setDaysCovered(int daysCovered) {
		this.daysCovered = daysCovered;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getGeneratedOn() {
		return generatedOn;
	}
	public void setGeneratedOn(String generatedOn) {
		this.generatedOn = generatedOn;
	}
	public List<PluginActiveUsersSummary> getPlugins() {
		return plugins;
	}
	public void setPlugins(List<PluginActiveUsersSummary> plugins) {
		this.plugins = plugins;
	}
	public List<CredentialsActiveUsersSummary> getCredentials() {
		return credentials;
	}
	public void setCredentials(List<CredentialsActiveUsersSummary> credentials) {
		this.credentials = credentials;
	}
	
	
	
}
