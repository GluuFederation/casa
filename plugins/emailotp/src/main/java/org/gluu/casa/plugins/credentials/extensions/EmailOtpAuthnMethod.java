package org.gluu.casa.plugins.credentials.extensions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.extension.AuthnMethod;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.emailotp.EmailOTPService;
import org.gluu.casa.service.ISessionContext;
import org.pf4j.Extension;

@Extension
public class EmailOtpAuthnMethod implements AuthnMethod {

	private EmailOTPService emailOTPService;
	private Logger logger = LogManager.getLogger(getClass());
	private ISessionContext sessionContext;

	public EmailOtpAuthnMethod() {
		sessionContext = Utils.managedBean(ISessionContext.class);
		emailOTPService = EmailOTPService.getInstance();
	}

	@Override
	public String getPanelBottomTextKey() {
		return "";
	}

	@Override
	public boolean mayBe2faActivationRequisite() {
		return Boolean.parseBoolean(Optional
				.ofNullable(EmailOTPService.getInstance().getScriptPropertyValue("2fa_requisite")).orElse("false"));

	}

	@Override
	public String getAcr() {
		return emailOTPService.getInstance().ACR;
	}

	@Override
	public List<BasicCredential> getEnrolledCreds(String arg0) {
		try {
			return emailOTPService.getInstance().getCredentials(sessionContext.getLoggedUser().getId())
					.stream().map(dev -> new BasicCredential(EmailOTPService.getMaskedEmail( dev.getNickName()), 0)).collect(Collectors.toList());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Override
	public String getPageUrl() {
		return "user/cred_details.zul";
	}

	@Override
	public String getPanelButtonKey() {
		return "panel.button";
	}

	@Override
	public String getPanelTextKey() {
		return "panel.text";
	}

	@Override
	public String getPanelTitleKey() {
		return "email.title";
	}

	@Override
	public int getTotalUserCreds(String arg0) {
		return emailOTPService.getInstance().getCredentialsTotal( sessionContext.getLoggedUser().getId());
	}

	@Override
	public String getUINameKey() {
		return "email.title";
	}

	@Override
	public void reloadConfiguration() {
		emailOTPService.getInstance().reloadConfiguration();
	}

}
