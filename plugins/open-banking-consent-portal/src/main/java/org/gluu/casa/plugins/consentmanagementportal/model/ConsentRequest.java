package org.gluu.casa.plugins.consentmanagementportal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentRequest {


    private String customerid;

    public String getCustomerid() {
        return customerid;
    }

    public ConsentRequest setCustomerid(String customerid) {
        this.customerid = customerid;
        return this;
    }
}
