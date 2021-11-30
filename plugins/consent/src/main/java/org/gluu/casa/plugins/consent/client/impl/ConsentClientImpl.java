package org.gluu.casa.plugins.consent.client.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.gluu.casa.plugins.consent.client.BaseExternalApiClient;
import org.gluu.casa.plugins.consent.client.ConsentClient;
import org.gluu.casa.plugins.consent.model.Consent;
import org.gluu.casa.plugins.consent.model.ConsentRequest;
import org.gluu.casa.plugins.consent.model.ConsentResponse;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * An app. scoped bean that encapsulates interactions with the consent-server.
 * @author
 */
@Named
@ApplicationScoped
public class ConsentClientImpl extends BaseExternalApiClient implements ConsentClient {


    @Inject
    private Logger logger;

    public ConsentResponse getAllConsents(ConsentRequest consentRequest, String url, String path) throws JsonProcessingException {
        return  (ConsentResponse) doPost(consentRequest,ConsentResponse.class, url, path);
    }
    public Consent getConsentById(String id, String url, String path) throws JsonProcessingException {
        return (Consent) doGet(Consent.class, url, path);
    }
    public ConsentResponse createConsent(ConsentRequest consentRequest, String url, String path) throws JsonProcessingException {
        return (ConsentResponse) doPost(consentRequest,ConsentResponse.class, url, path);
    }


}
