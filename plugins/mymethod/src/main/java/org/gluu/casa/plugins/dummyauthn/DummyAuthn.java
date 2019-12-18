package org.gluu.casa.plugins.dummyauthn;

import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.extension.AuthnMethod;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Created by jgomer on 2018-07-18.
 */
@Extension
public class DummyAuthn implements AuthnMethod {

    public static final String ACR = "dummy";

    private Logger logger = LoggerFactory.getLogger(getClass());

    public String getAcr() {
        return ACR;
    }

    public String getUINameKey(){
        return "dummy.method_name";
    }

    public String getPanelTitleKey() {
        return "dummy.method_name";
    }

    public String getPanelTextKey() {
        return "dummy.method_description";
    }

    public String getPanelButtonKey() {
        return "dummy.method_button";
    }

    public String getPageUrl() {
        return "dummy-detail.zul";
    }

    public void reloadConfiguration() {
        logger.debug("reloading");
    }

    public List<BasicCredential> getEnrolledCreds(String id) {
        return Collections.emptyList();
    }

    public int getTotalUserCreds(String id) {
        return 0;
    }

}
