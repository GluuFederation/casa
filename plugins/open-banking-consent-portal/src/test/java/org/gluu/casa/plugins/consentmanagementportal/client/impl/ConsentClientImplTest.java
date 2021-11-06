package org.gluu.casa.plugins.consentmanagementportal.client.impl;

import junit.framework.Assert;
import org.gluu.casa.plugins.consentmanagementportal.client.ConsentClient;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentRequest;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentResponse;
import org.junit.jupiter.api.Test;

public class ConsentClientImplTest {


    public static final String URL = "https://idp.openitio.com/consent/";
    public static final String PATH = "getUserConsents";
    public static final String PATH1 = "getUserConsents/getUserConsentData?userId=boda@gmail.com";
    public static final String CUSTOMERID = "boda@gmail.com";


    @Test
    public void testGetAllConsents() throws Exception
    {
        // fill out a query param and execute a get request
        ConsentClient client = givenClient();
        ConsentRequest consentRequest = givenConsentRequest();
        ConsentResponse consent = client.getAllConsents(consentRequest, URL, PATH);
        Assert.assertTrue( consent!=null);
    }

    @Test
    public void createConsent() throws Exception{
        // fill out a query param and execute a get request
        ConsentClient client = givenClient();
        ConsentRequest consentRequest = new ConsentRequest();
        ConsentResponse consentResponse = client.createConsent(consentRequest, URL, PATH1);
        Assert.assertTrue( consentResponse!=null);
    }

    public ConsentClient givenClient(){
        return new ConsentClientImpl();
    }
    public ConsentRequest givenConsentRequest(){
        ConsentRequest consentRequest = new ConsentRequest();
        consentRequest.setCustomerid(CUSTOMERID);
        return consentRequest;
    }
}
