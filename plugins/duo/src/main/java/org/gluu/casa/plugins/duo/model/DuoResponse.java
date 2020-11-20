package org.gluu.casa.plugins.duo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DuoResponse   {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String stat;
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	
	private List<Response> response;
    public void setStat(String stat) {
         this.stat = stat;
     }
     public String getStat() {
         return stat;
     }

    public void setResponse(List<Response> response) {
         this.response = response;
     }
     public List<Response> getResponse() {
         return response;
     }
	@Override
	public String toString() {
		return "DuoResponse [stat=" + stat + ", response=" + response + "]";
	}

	
}
