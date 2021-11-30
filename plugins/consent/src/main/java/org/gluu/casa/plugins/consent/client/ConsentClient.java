package org.gluu.casa.plugins.consent.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.gluu.casa.plugins.consent.model.Consent;
import org.gluu.casa.plugins.consent.model.ConsentRequest;
import org.gluu.casa.plugins.consent.model.ConsentResponse;


public interface ConsentClient {

    public ConsentResponse getAllConsents(ConsentRequest consentRequest, String url, String path) throws JsonProcessingException;
    public Consent getConsentById(String id, String url, String path) throws JsonProcessingException;
    public ConsentResponse createConsent(ConsentRequest consentRequest, String url, String path) throws JsonProcessingException;
}
