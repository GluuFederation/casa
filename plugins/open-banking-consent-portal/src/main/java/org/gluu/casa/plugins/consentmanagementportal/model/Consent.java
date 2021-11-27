package org.gluu.casa.plugins.consentmanagementportal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.gluu.casa.plugins.consentmanagementportal.enums.ConsentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consent {


        private String consentId;
        private ConsentStatus status;

        private LocalDateTime createdDate;

        private LocalDateTime updatedDate;

        private LocalDateTime expirationDate;
        private List<Account> accounts;


}
