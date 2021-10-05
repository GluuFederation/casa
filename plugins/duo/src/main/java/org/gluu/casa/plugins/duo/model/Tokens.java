package org.gluu.casa.plugins.duo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tokens {

    private String serial;
    @JsonProperty("token_id")
    private String tokenId;
    private String type;
    public void setSerial(String serial) {
         this.serial = serial;
     }
     public String getSerial() {
         return serial;
     }

    public void setTokenId(String tokenId) {
         this.tokenId = tokenId;
     }
     @Override
	public String toString() {
		return "Tokens [serial=" + serial + ", tokenId=" + tokenId + ", type=" + type + "]";
	}
	public String getTokenId() {
         return tokenId;
     }

    public void setType(String type) {
         this.type = type;
     }
     public String getType() {
         return type;
     }

}