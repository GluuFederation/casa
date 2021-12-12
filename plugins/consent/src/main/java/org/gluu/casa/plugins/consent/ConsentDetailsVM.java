package org.gluu.casa.plugins.consent;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.checkerframework.checker.units.qual.C;
import org.gluu.casa.core.pojo.User;
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
public class ConsentDetailsVM {

    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final String URL = "https://idp.openitio.com/consent/";
    public static final String PATH = "getUserConsents";
    public static final String PATH1 = "getUserConsents/getUserConsentData?userId=boda@gmail.com";
    public static final String CUSTOMERID = "boda@gmail.com";

    private String message;
    private ISessionContext sessionContext;
    private ConsentResponse consentResponse;
    private ConsentDetails consentDetails;
    private IPersistenceService persistenceService;
    private User user;
    /**
     * Getter of private class field <code>message</code>.
     * @return A string value
     */
    public String getMessage() {
        return message;
    }


    /**
     * Initialization method for this ViewModel.
     */
    @Init
    public void init() throws JsonProcessingException {
        logger.info("Consent Details ViewModel initiated");
        persistenceService = Utils.managedBean(IPersistenceService.class);

        sessionContext = Utils.managedBean(ISessionContext.class);
        logger.info("There is a user logged in! " + sessionContext.getLoggedUser());
        if (sessionContext.getLoggedUser() != null) {
            logger.info("There is a user logged in!");
            user = sessionContext.getLoggedUser();
            logger.info("User data ! " + user.getClaim("email"));
            //todo populate after calling api
            consentDetails = new ConsentDetails();
            consentDetails.setConsentId("lBs6uhzghfs4FZ85zwKXQYG3oRcbkBu_ufVN3_1VQMk=");
            consentDetails.setProvider("Yodlee");
            consentDetails.setStatus(ConsentStatus.REVOKED.name());
            consentDetails.setExpirationDate("-");
            consentDetails.setAccountId("No account found");
            consentDetails.setAccessGrantedDate("2021-11-25T13:01:24Z");
            logger.info("consent details {}",consentDetails.toString());
        }


    }



}
