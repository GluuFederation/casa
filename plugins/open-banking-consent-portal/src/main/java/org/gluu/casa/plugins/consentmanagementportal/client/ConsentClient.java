package org.gluu.casa.plugins.consentmanagementportal.client;

import org.gluu.casa.plugins.consentmanagementportal.model.Consent;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentRequest;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentResponse;

import java.util.List;

public interface ConsentClient {

    public List<Consent> getAllConsents(String url, String path);
    public Consent getConsentById(String id, String url, String path);
    public ConsentResponse createConsent(ConsentRequest consentRequest, String url, String path);
}
