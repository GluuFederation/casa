package org.gluu.casa.core.pojo;

import java.util.Map;

public class UserCredential {
	private String userId;
	private Map<String, Integer> credentials;
	private String lastLoginDate;

	public UserCredential(String userId,  Map<String, Integer> credentials, String lastLoginDate) {
		super();
		this.userId = userId;
		this.lastLoginDate = lastLoginDate;
		this.credentials = credentials;
	}

	public String getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(String lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Map<String, Integer> getCredentials() {
		return credentials;
	}

	public void setCredentials(Map<String, Integer> credentials) {
		this.credentials = credentials;
	}

	

}