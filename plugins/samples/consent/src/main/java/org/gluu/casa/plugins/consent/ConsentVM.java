package org.gluu.casa.plugins.consent;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.consent.client.ConsentClient;
import org.gluu.casa.plugins.consent.client.impl.ConsentClientImpl;
import org.gluu.casa.plugins.consent.model.ConsentRequest;
import org.gluu.casa.plugins.consent.model.ConsentResponse;
import org.gluu.casa.plugins.consent.model.DummyConsent;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.casa.service.ISessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

/**
 * A ZK <a href="http://books.zkoss.org/zk-mvvm-book/8.0/viewmodel/index.html" target="_blank">ViewModel</a> that acts
 * as the "controller" of page <code>index.zul</code> in this sample plugin. See <code>viewModel</code> attribute of
 * panel component of <code>index.zul</code>.
 * @author jgomer
 */
public class ConsentVM {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String URL = "https://idp.openitio.com/consent/";
    public static final String PATH = "getUserConsents";
    public static final String PATH1 = "getUserConsents/getUserConsentData?userId=boda@gmail.com";
    public static final String CUSTOMERID = "boda@gmail.com";

    private String message;
    private String organizationName;
    private IPersistenceService persistenceService;
    private ISessionContext sessionContext;
    private ConsentResponse consentResponse;
    private  ConsentClient client;
    private ConsentRequest consentRequest;
    /**
     * Getter of private class field <code>organizationName</code>.
     * @return A string with the value of the organization name found in your Gluu installation. Find this value in
     * Gluu Server oxTrust GUI at "Configuration" &gt; "Organization configuration"
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * Getter of private class field <code>message</code>.
     * @return A string value
     */
    public String getMessage() {
        return message;
    }

    /**
     * Setter of private class field <code>message</code>.
     * @param message A string with the contents typed in text box of page index.zul
     */
    public void setMessage(String message) {
        this.message = message;
    }

    public ConsentResponse getConsentResponse() {
        return consentResponse;
    }

    public ConsentVM setConsentResponse(ConsentResponse consentResponse) {
        this.consentResponse = consentResponse;
        return this;
    }

    /**
     * Initialization method for this ViewModel.
     */
    @Init
    public void init() throws JsonProcessingException {
        logger.info("Consent ViewModel inited");
        persistenceService = Utils.managedBean(IPersistenceService.class);

        sessionContext = Utils.managedBean(ISessionContext.class);
        if (sessionContext.getLoggedUser() != null) {
            logger.info("There is a user logged in!");
        }
        client = givenClient();
        consentRequest = givenConsentRequest();
//        TODO  - dependency  resteasy-jaxb-provider not working maybe is excluded
//        consent = client.getAllConsents(consentRequest, URL, PATH);
          consentResponse = new DummyConsent();
    }
    public ConsentClient givenClient(){
        return new ConsentClientImpl();
    }
    public ConsentRequest givenConsentRequest(){
        ConsentRequest consentRequest = new ConsentRequest();
        consentRequest.setCustomerid(CUSTOMERID);
        return consentRequest;
    }

    /**
     * The method called when the button on page <code>index.zul</code> is pressed. It sets the value for
     * <code>organizationName</code>.
     */
    @NotifyChange("organizationName")
    public void loadOrgName() {
        logger.debug("You typed {}", message);
        organizationName = persistenceService.getOrganization().getDisplayName();
    }

}
