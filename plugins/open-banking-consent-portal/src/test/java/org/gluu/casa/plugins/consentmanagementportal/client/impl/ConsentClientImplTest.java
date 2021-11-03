package org.gluu.casa.plugins.consentmanagementportal.client.impl;

import junit.framework.Assert;
import org.checkerframework.checker.units.qual.C;
import org.gluu.casa.plugins.consentmanagementportal.client.ConsentClient;
import org.gluu.casa.plugins.consentmanagementportal.model.Consent;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentRequest;
import org.gluu.casa.plugins.consentmanagementportal.model.ConsentResponse;
import org.junit.jupiter.api.Test;


import java.util.List;

public class ConsentClientImplTest {


    @Test
    public void testResponse() throws Exception
    {
        // fill out a query param and execute a get request
        ConsentClient client = givenClient();
        List<Consent> consentList = client.getAllConsents("https://idp.openitio.com/consent/getUserConsents","getUserConsentData?userId=boda@gmail.com");
        Assert.assertTrue( consentList.size()>0);


    }

    @Test
    public void createConsent() throws Exception{
        // fill out a query param and execute a get request
        ConsentClient client = givenClient();
        ConsentRequest consentRequest = new ConsentRequest();
        ConsentResponse consentResponse = client.createConsent(consentRequest,"https://idp.openitio.com/consent/getUserConsents","getUserConsentData?userId=boda@gmail.com");
        Assert.assertTrue( consentResponse!=null);
    }

    public ConsentClient givenClient(){
        return new ConsentClientImpl();
    }
}
