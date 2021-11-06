package org.gluu.casa.plugins.consentmanagementportal.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.gluu.casa.plugins.consentmanagementportal.enums.AccountStatus;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {


    private String accountId;
    private AccountStatus status;
}
