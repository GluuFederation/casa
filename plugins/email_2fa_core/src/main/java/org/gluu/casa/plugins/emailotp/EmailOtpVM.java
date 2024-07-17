package org.gluu.casa.plugins.emailotp;

import java.security.SecureRandom;
import java.util.*;

import org.gluu.casa.core.pojo.User;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.emailotp.model.EmailPerson;
import org.gluu.casa.plugins.emailotp.model.VerifiedEmail;
import org.gluu.casa.service.ISessionContext;
import org.gluu.casa.service.SndFactorAuthenticationUtils;
import org.gluu.casa.ui.UIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

public class EmailOtpVM {
    private static Logger logger = LoggerFactory.getLogger(EmailOtpVM.class);

	private EmailOTPService emailOtpService;

	private boolean emailCodesMatch;
	private boolean uiEmailDelivered;

	public boolean isUiEmailDelivered() {
		return uiEmailDelivered;
	}

	public void setUiEmailDelivered(boolean uiEmailDelivered) {
		this.uiEmailDelivered = uiEmailDelivered;
	}

	private VerifiedEmail newEmail;
	private List<VerifiedEmail> emailIds;
	private String code;
	private String realCode;

	@WireVariable
	private ISessionContext sessionContext;
	EmailPerson person;

	SndFactorAuthenticationUtils sndFactorUtils;
	User user;

	public boolean isEmailCodesMatch() {
		return emailCodesMatch;
	}

	public void setEmailCodesMatch(boolean emailCodesMatch) {
		this.emailCodesMatch = emailCodesMatch;
	}

	public VerifiedEmail getNewEmail() {
		return newEmail;
	}

	public void setNewEmail(VerifiedEmail newEmail) {
		this.newEmail = newEmail;
	}

	public List<VerifiedEmail> getEmailIds() {
		return emailIds;
	}

	public void setEmailIds(List<VerifiedEmail> emailIds) {
		this.emailIds = emailIds;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRealCode() {
		return realCode;
	}

	public void setRealCode(String realCode) {
		this.realCode = realCode;
	}

	public EmailOtpVM() {
		emailOtpService = EmailOTPService.getInstance();
	}

	@Init(superclass = true)
	public void childInit() {
		newEmail = new VerifiedEmail();
		user = Utils.managedBean(ISessionContext.class).getLoggedUser();
		emailIds = emailOtpService.getVerifiedEmail(user.getId());
		sndFactorUtils = Utils.managedBean(SndFactorAuthenticationUtils.class);
		logger.debug("init called");
	}

	@NotifyChange("uiEmailDelivered")
	public void sendCode(HtmlBasedComponent toFocus) {
        String theNewEmail = newEmail.getEmail(); 
		logger.debug("email entered: {}", theNewEmail);
		if (Utils.isNotEmpty(newEmail.getEmail())) { // Did user fill out the email text box?
			// Check for uniquess throughout all emails in LDAP. Only new emails are
			// accepted
			try {
				if (!validateEmail(theNewEmail)) {
					UIUtils.showMessageUI(Clients.NOTIFICATION_TYPE_WARNING,
							Labels.getLabel("usr.email_invalid_format"));
				}
				else if (emailIds.stream()
				        .filter(e -> theNewEmail.equals(e.getEmail())).findFirst().isPresent()) {
					UIUtils.showMessageUI(Clients.NOTIFICATION_TYPE_WARNING,
							Labels.getLabel("usr.email_already_exists"));
				} else {
					// Generate random in [100000, 999999]
					realCode = generateCode(Integer.valueOf(emailOtpService.getScriptPropertyValue("otp_length")));

					String body = Labels.getLabel("usr.email_body", new String[] { realCode });
					String subject = Labels.getLabel("usr.email_subject");
					logger.debug("sendCode. code={}", realCode);

					// Send message (service bean already knows all settings to perform this step)
					uiEmailDelivered = emailOtpService.sendEmailWithOTPSigned(theNewEmail, subject, body);
					logger.debug("Signed message delivery: {}", uiEmailDelivered);
					if (!uiEmailDelivered) {
                        uiEmailDelivered = emailOtpService.sendEmailWithOTP(theNewEmail, subject, body);
                        logger.debug("Non signed message delivery: {}", uiEmailDelivered);
					}
					if (uiEmailDelivered) {
						if (toFocus != null) {
							toFocus.focus();
						}
					} else {
						UIUtils.showMessageUI(false);
					}
				}
			} catch (Exception e) {
				UIUtils.showMessageUI(false);
				logger.error(e.getMessage(), e);
			}
		}
	}

	@NotifyChange({ "emailCodesMatch", "uiEmailDelivered", "code" })
	public void checkCode(HtmlBasedComponent toFocus) {
		emailCodesMatch = Utils.isNotEmpty(code) && Utils.isNotEmpty(realCode) && realCode.equals(code.trim());
		if (emailCodesMatch) {
			add();
			if (toFocus != null) {
				toFocus.focus();
			}
			uiEmailDelivered = false;
		} else {
			UIUtils.showMessageUI(Clients.NOTIFICATION_TYPE_WARNING, Labels.getLabel("usr.email_code_wrong"));
		}
	}

	@NotifyChange({ "emailCodesMatch", "code", "newEmail", "emailIds" })
	public void add() {

		if (Utils.isNotEmpty(newEmail.getEmail())) {

			if (emailOtpService.updateEmailIdAdd(user.getId(), emailIds, newEmail)) {
				UIUtils.showMessageUI(true, Labels.getLabel("enroll.success"));
				
				sndFactorUtils.notifyEnrollment(user, EmailOTPService.ACR);
				// trigger refresh (this method is asynchronous...)
				BindUtils.postNotifyChange(EmailOtpVM.this, "emailIds");
				BindUtils.postNotifyChange(EmailOtpVM.this, "newEmail");
			} else {
				UIUtils.showMessageUI(false, Labels.getLabel("enroll.error"));
			}
			cancel();
		}

	}

	@NotifyChange({ "uiCodesMatch", "code", "emailCodesMatch", "uiEmailDelivered", "newEmail" })
	public void cancel() {
		emailCodesMatch = false;
		realCode = null;
		code = null;
		uiEmailDelivered = false;
		newEmail = new VerifiedEmail();
	}

	public void delete(VerifiedEmail email) {

		String resetMessages = sndFactorUtils.removalConflict(EmailOTPService.ACR, 1, user).getY();
		boolean reset = resetMessages != null;
		Pair<String, String> delMessages = getDeleteMessages(email.getEmail(), resetMessages);

		Messagebox.show(delMessages.getY(), delMessages.getX(), Messagebox.YES | Messagebox.NO,
				reset ? Messagebox.EXCLAMATION : Messagebox.QUESTION, event -> {
					if (Messagebox.ON_YES.equals(event.getName())) {
						try {
							for (VerifiedEmail e : emailIds) {
								if (e.getEmail().equals(email.getEmail())) {
									emailIds.remove(e);
									break;
								}
							}
							boolean success = emailOtpService.updateEmailIdAdd(user.getId(), emailIds, null);
							if (success) {
								if (reset) {
									sndFactorUtils.turn2faOff(user);
								}
								// trigger refresh (this method is asynchronous...)
								BindUtils.postNotifyChange(EmailOtpVM.this, "emailIds");
							} else {
								emailIds.add(email);
							}
							UIUtils.showMessageUI(success);
						} catch (Exception e) {
							UIUtils.showMessageUI(false);
							logger.error(e.getMessage(), e);
						}
					}
				});
	}

	Pair<String, String> getDeleteMessages(String email, String extraMessage) {

		StringBuilder text = new StringBuilder();
		if (extraMessage != null) {
			text.append(extraMessage).append("\n\n");
		}
		text.append(Labels.getLabel("email_del_confirm",
				new String[] { email == null ? Labels.getLabel("general.no_named") : email }));
		if (extraMessage != null) {
			text.append("\n");
		}

		return new Pair<>(Labels.getLabel("email_del_title"), text.toString());

	}

	public boolean validateEmail(String email) {
	    return email.contains("@");
	}

	private String generateCode(int charLength) {
		return String.valueOf(charLength < 1 ? 0
				: new SecureRandom().nextInt((9 * (int) Math.pow(10.0, charLength - 1.0)) - 1)
						+ (int) Math.pow(10.0, charLength - 1.0));
	}

}
