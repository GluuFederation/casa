package org.gluu.casa.plugins.consentmanagementportal.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.gluu.casa.plugins.consentmanagementportal.enums.ConsentStatus;

import java.time.OffsetDateTime;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Consent {


        private String consentId;
        private ConsentStatus status;
        private OffsetDateTime createdDate;
        private OffsetDateTime updatedDate;
        private OffsetDateTime expirationDate;
        private List<Account> accounts;

        @Override
        public String toString() {
                final StringBuffer sb = new StringBuffer("Consent{");
                sb.append("consentId='").append(consentId).append('\'');
                sb.append(", status=").append(status);
                sb.append(", createdDate=").append(createdDate);
                sb.append(", updatedDate=").append(updatedDate);
                sb.append(", expirationDate=").append(expirationDate);
                sb.append(", accounts=").append(accounts);
                sb.append('}');
                return sb.toString();
        }
}
