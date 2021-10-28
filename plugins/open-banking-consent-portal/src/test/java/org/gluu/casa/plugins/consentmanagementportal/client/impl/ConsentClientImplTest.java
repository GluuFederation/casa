package org.gluu.casa.plugins.consentmanagementportal.client.impl;

import junit.framework.Assert;
import org.gluu.casa.plugins.consentmanagementportal.client.ConsentClient;
import org.gluu.casa.plugins.consentmanagementportal.model.Consent;
import org.junit.jupiter.api.Test;


import java.util.List;

public class ConsentClientImplTest {


    @Test
    public void testResponse() throws Exception
    {
        // fill out a query param and execute a get request
        ConsentClient client = givenClient();
        List<Consent> consentList = client.getAllConsents("https://idp.openitio.com//openbanking/v3.1/","getUserConsentData?userId=boda@gmail.com");
        Assert.assertTrue( consentList.size()>0);


    }

    public ConsentClient givenClient(){
        return new ConsentClientImpl();
    }
}
