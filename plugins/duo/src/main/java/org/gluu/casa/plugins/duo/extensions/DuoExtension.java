package org.gluu.casa.plugins.duo.extensions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.extension.AuthnMethod;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.duo.DuoService;
import org.gluu.casa.plugins.duo.model.DuoCredential;
import org.gluu.casa.service.ISessionContext;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author madhumita
 *
 */
@Extension
public class DuoExtension implements AuthnMethod {

	private DuoService duoService;
	

	private Logger logger = LoggerFactory.getLogger(getClass());
	private ISessionContext sessionContext;

	public DuoExtension() {
		sessionContext = Utils.managedBean(ISessionContext.class);
		duoService = DuoService.getInstance();
	}

	public String getUINameKey() {

		return "duo_label";
	}

	public String getAcr() {
		return DuoService.getInstance().ACR;
	}

	public String getPanelTitleKey() {
		return "duo_title";
	}

	public String getPanelTextKey() {
		return "duo_text";
	}

	public String getPanelButtonKey() {

		return "duo_manage";
	}

	public String getPanelBottomTextKey() {
		return "duo_download";
	}

	public String getPageUrl() {
		return "user/cred_details.zul";

	}

	public List<BasicCredential> getEnrolledCreds(String id) {
		//pass user name or anything that uniquely identifies a user 
		List<BasicCredential> creds = new ArrayList<BasicCredential>();
		try {
			DuoCredential device = DuoService.getInstance().getDuoCredentials(sessionContext.getLoggedUser());
			if(device != null)
			{
				creds.add(new BasicCredential(device.getNickName(), device.getAddedOn()));
			}
			return creds;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Collections.emptyList();
		}

	}

	public int getTotalUserCreds(String id) {
		//pass user name or anything that uniquely identifies a user 
		return DuoService.getInstance().getDeviceTotal(sessionContext.getLoggedUser());
	}

	public void reloadConfiguration() {
		DuoService.getInstance().reloadConfiguration();

	}

	public boolean mayBe2faActivationRequisite() {
		return Boolean.parseBoolean(Optional
				.ofNullable(DuoService.getInstance().getScriptPropertyValue("2fa_requisite")).orElse("false"));
	}
	
}
