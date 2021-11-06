package org.gluu.casa.plugins.consentmanagementportal.service;

import org.gluu.casa.rest.RSUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;



public class ResteasyClientService {


    private ResteasyClient resteasyClient;

    public ResteasyClientService() {
        this.resteasyClient = RSUtils.getClient();
    }

    public ResteasyClient getResteasyClient() {
        return resteasyClient;
    }
}
