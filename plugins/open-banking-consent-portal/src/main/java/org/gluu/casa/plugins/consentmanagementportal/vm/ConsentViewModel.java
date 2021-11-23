package org.gluu.casa.plugins.consentmanagementportal.vm;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.ISessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.WireVariable;

public class ConsentViewModel {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @WireVariable
    private ISessionContext sessionContext;



    /**
     * Initialization method for this ViewModel.
     */
    @Init
    public void init() {

        sessionContext = Utils.managedBean(ISessionContext.class);


        String sigResponse = Executions.getCurrent().getParameter("sig_response");
        // sig response indicates that login was sucessful or unsuccessful
        // after this step, leave the duo_iframe open, send a javascript to the client.
        if (sigResponse != null) {


            Session session = Sessions.getCurrent();

        }

        else {

            Session session = Sessions.getCurrent();


            //

        }

        logger.debug("init invoked");

    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        logger.debug("afterCompose invoked");
        Selectors.wireEventListeners(view, this);
    }

    public boolean delete() {

        logger.debug("delete invoked");

        return false;

    }

    Pair<String, String> getDeleteMessages(String extraMessage) {

        StringBuilder text = new StringBuilder();
        if (extraMessage != null) {
            text.append(extraMessage).append("\n\n");
        }
        text.append(Labels.getLabel("duo_del_confirm", new String[] { Labels.getLabel("duo_credentials") }));
        if (extraMessage != null) {
            text.append("\n");
        }

        return new Pair<>(Labels.getLabel("duo_del_title"), text.toString());

    }

    public boolean edit() {
        logger.info("edit invoked");
        return true;
    }
}
