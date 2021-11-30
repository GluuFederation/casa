package org.gluu.casa.plugins.consent.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentResponse {
    @JsonProperty("Consent")
    private List<Consent> consentList;


}
