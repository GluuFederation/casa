package org.gluu.casa.core.pojo;

import java.util.List;

public class UserCredentialReport {
	
	public UserCredentialReport(List<UserCredential> data) {
		this.data = data;
	}

	private List<UserCredential> data;

	public List<UserCredential> getData() {
		return data;
	}

	public void setData(List<UserCredential> data) {
		this.data = data;
	}

}