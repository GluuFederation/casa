package org.gluu.casa.plugins.duo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DuoResponse   {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String stat;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	
	private String response;

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public DuoResponse() {
	}

	
}
