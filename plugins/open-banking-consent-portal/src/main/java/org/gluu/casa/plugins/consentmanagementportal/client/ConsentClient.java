package org.gluu.casa.plugins.consentmanagementportal.client;

import org.gluu.casa.plugins.consentmanagementportal.model.Consent;

import java.util.List;

public interface ConsentClient {

    public List<Consent> getAllConsents(String url, String path);
    public Consent getConsentById(String id, String url, String path);
}
