package org.gluu.casa.plugins.consentmanagementportal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.gluu.casa.plugins.consentmanagementportal.enums.ConsentStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consent {


        private String consentId;
        private ConsentStatus status;
        private OffsetDateTime createdDate;
        private OffsetDateTime updatedDate;
        private OffsetDateTime expirationDate;
        private List<Account> accounts;


}
