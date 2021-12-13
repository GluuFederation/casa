package org.gluu.casa.plugins.consent;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.gluu.casa.core.pojo.User;
import org.gluu.casa.extension.AuthnMethod;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.consent.client.ConsentClient;
import org.gluu.casa.plugins.consent.client.impl.ConsentClientImpl;
import org.gluu.casa.plugins.consent.enums.ConsentStatus;
import org.gluu.casa.plugins.consent.model.Consent;
import org.gluu.casa.plugins.consent.model.ConsentDetails;
import org.gluu.casa.plugins.consent.model.ConsentRequest;
import org.gluu.casa.plugins.consent.model.ConsentResponse;

import org.gluu.casa.service.IPersistenceService;
import org.gluu.casa.service.ISessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;


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
    private User user;
    private ConsentDetails consentDetails;
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


    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public ConsentRequest getConsentRequest() {
        return consentRequest;
    }

    public void setConsentRequest(ConsentRequest consentRequest) {
        this.consentRequest = consentRequest;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ConsentDetails getConsentDetails() {
        return consentDetails;
    }

    public void setConsentDetails(ConsentDetails consentDetails) {
        this.consentDetails = consentDetails;
    }

    /**
     * Initialization method for this ViewModel.
     */
    @Init
    public void init() throws JsonProcessingException {
        logger.info("Consent ViewModel inited");
        persistenceService = Utils.managedBean(IPersistenceService.class);

        sessionContext = Utils.managedBean(ISessionContext.class);
        logger.info("There is a user logged in! " + sessionContext.getLoggedUser());
        if (sessionContext.getLoggedUser() != null) {
            logger.info("There is a user logged in!");
            user = sessionContext.getLoggedUser();
            logger.info("User data ! " + user.getClaim("email"));
            client = givenClient();
            String email = user.getClaim("email");
            // TODO remove this after adding new user
            if(email == null){
                email = CUSTOMERID;
            }
            consentRequest = createConsentRequest(email);
            consentResponse = client.getAllConsents(consentRequest, URL, PATH);
            consentDetails = createDummyConsentDetails();
        }


    }
    public ConsentClient givenClient(){
        return new ConsentClientImpl();
    }
    public ConsentRequest createConsentRequest(String email){
        ConsentRequest consentRequest = new ConsentRequest();
        consentRequest.setCustomerid(email);
        return consentRequest;
    }

    /**
     * The method called when the button on page <code>index.zul</code> is pressed. It redirects to page with consent details
     */

    public void openConsent(Consent consent) {
        logger.info("You opened consent {}",consent.getConsentId());
    }

    public ConsentDetails createDummyConsentDetails(){
        consentDetails = new ConsentDetails();
        consentDetails.setConsentId("lBs6uhzghfs4FZ85zwKXQYG3oRcbkBu_ufVN3_1VQMk=");
        consentDetails.setProvider("Yodlee");
        consentDetails.setStatus(ConsentStatus.REVOKED.name());
        consentDetails.setExpirationDate("-");
        consentDetails.setAccountId("No account found");
        consentDetails.setAccessGrantedDate("2021-11-25T13:01:24Z");
        return consentDetails;
    }

}
