package org.gluu.casa.plugins.consentmanagementportal.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentResponse {
    @JsonProperty("Consent")
    private List<Consent> consent;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ConsentResponse{");
        sb.append("consent=").append(consent);
        sb.append('}');
        return sb.toString();
    }
}
