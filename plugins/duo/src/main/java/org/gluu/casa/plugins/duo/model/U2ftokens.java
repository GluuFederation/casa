package org.gluu.casa.plugins.duo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class U2ftokens {

    @JsonProperty("date_added")
    private long dateAdded;
    @JsonProperty("registration_id")
    private String registrationId;
    public void setDateAdded(long dateAdded) {
    	// timestamps in seconds (10-digit), milliseconds (13-digit). DUO API's Json response contains 10 digits, casa's CustomDateConverter takes 13 digit as input
         this.dateAdded = dateAdded * 1000;
     }
     public long getDateAdded() {
         return dateAdded;
     }

    public void setRegistrationId(String registrationId) {
         this.registrationId = registrationId;
     }
     public String getRegistrationId() {
         return registrationId;
     }
	@Override
	public String toString() {
		return "U2ftokens [dateAdded=" + dateAdded + ", registrationId=" + registrationId + "]";
	}

}