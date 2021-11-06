package org.gluu.casa.plugins.consentmanagementportal.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.gluu.casa.plugins.consentmanagementportal.model.Consent;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentRequest;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentResponse;

import java.util.List;

public interface ConsentClient {

    public ConsentResponse getAllConsents(ConsentRequest consentRequest, String url, String path) throws JsonProcessingException;
    public Consent getConsentById(String id, String url, String path) throws JsonProcessingException;
    public ConsentResponse createConsent(ConsentRequest consentRequest, String url, String path) throws JsonProcessingException;
}
