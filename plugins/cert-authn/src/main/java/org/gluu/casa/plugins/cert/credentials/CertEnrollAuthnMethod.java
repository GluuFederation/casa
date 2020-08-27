package org.gluu.casa.plugins.cert.credentials;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.extension.AuthnMethod;
import org.gluu.casa.plugin.misc.Utils;
import org.gluu.casa.plugins.cert.service.CertService;

import org.gluu.casa.service.ISessionContext;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
public class CertEnrollAuthnMethod implements AuthnMethod{
	private Logger logger = LoggerFactory.getLogger(getClass());
	private ISessionContext sessionContext;
	private CertService certService;
	public CertEnrollAuthnMethod() {
		// TODO Auto-generated constructor stub
		sessionContext = Utils.managedBean(ISessionContext.class);
		certService = CertService.getInstance();
	}
	
	@Override
	public String getPanelBottomTextKey() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public boolean mayBe2faActivationRequisite() {
		// TODO Auto-generated method stub
		return Boolean.parseBoolean(Optional
				.ofNullable(CertService.getInstance().getScriptPropertyValue("2fa_requisite")).orElse("false"));
		
	}

	@Override
	public String getAcr() {
		// TODO Auto-generated method stub
		return certService.getInstance().ACR;
	}

	@Override
	public List<BasicCredential> getEnrolledCreds(String arg0) {
		// TODO Auto-generated method stub
		try {
			return certService.getInstance().getCredentials(sessionContext.getLoggedUser().getUserName());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Override
	public String getPageUrl() {
		// TODO Auto-generated method stub
		return "cert-details.zul";
	}

	@Override
	public String getPanelButtonKey() {
		// TODO Auto-generated method stub
		return "panel.button";
	}

	@Override
	public String getPanelTextKey() {
		// TODO Auto-generated method stub
		return "panel.test";
	}

	@Override
	public String getPanelTitleKey() {
		// TODO Auto-generated method stub
		return "usrcert.cert_title";
	}

	@Override
	public int getTotalUserCreds(String arg0) {
		// TODO Auto-generated method stub
		
		return certService.getInstance().getDevicesTotal(sessionContext.getLoggedUser().getUserName());
	}

	@Override
	public String getUINameKey() {
		// TODO Auto-generated method stub
		return "usrcert.cert_title";
	}

	@Override
	public void reloadConfiguration() {
		// TODO Auto-generated method stub
		certService.getInstance().reloadConfiguration();
	}

}
