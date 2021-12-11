package org.gluu.casa.plugins.consent.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;



@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentDetails {

    private String updatedDate;
    private List<Account> accounts=new ArrayList<>();
    private String accountId;
    private String status;
    private String expirationDate;
    private String consentId;

}
