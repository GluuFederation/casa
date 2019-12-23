package org.gluu.casa.core.model;

import org.gluu.persist.annotation.AttributeName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CredentialsActiveUsersSummary {

	@AttributeName(name="credential-name")
    private String credentialName;
    private int activeUsers;
    
    public CredentialsActiveUsersSummary()
    {}
	public String getCredentialName() {
		return credentialName;
	}
	public void setCredentialName(String credentialName) {
		this.credentialName = credentialName;
	}
	public int getActiveUsers() {
		return activeUsers;
	}
	public void setActiveUsers(int activeUsers) {
		this.activeUsers = activeUsers;
	}
}
