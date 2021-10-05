package org.gluu.casa.plugins.duo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/* Time: 2020-11-16 18:58:59 @author freecodeformat.com @website http://www.freecodeformat.com/json2javabean.php */
public class Webauthncredentials {

    @JsonProperty("credential_name")
    private String credentialName;
    @JsonProperty("date_added")
    private int dateAdded;
    private String label;
    private String webauthnkey;
    public void setCredentialName(String credentialName) {
         this.credentialName = credentialName;
     }
     public String getCredentialName() {
         return credentialName;
     }

    public void setDateAdded(int dateAdded) {
         this.dateAdded = dateAdded;
     }
     public int getDateAdded() {
         return dateAdded;
     }

    public void setLabel(String label) {
         this.label = label;
     }
     public String getLabel() {
         return label;
     }

    public void setWebauthnkey(String webauthnkey) {
         this.webauthnkey = webauthnkey;
     }
     public String getWebauthnkey() {
         return webauthnkey;
     }
	@Override
	public String toString() {
		return "Webauthncredentials [credentialName=" + credentialName + ", dateAdded=" + dateAdded + ", label=" + label
				+ ", webauthnkey=" + webauthnkey + "]";
	}

}