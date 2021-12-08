package org.gluu.casa.plugins.emailotp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.emailotp.model.EmailPerson;
import org.gluu.casa.plugins.emailotp.model.GluuConfiguration;
import org.gluu.casa.plugins.emailotp.model.SmtpConfiguration;
import org.gluu.casa.plugins.emailotp.model.VerifiedEmail;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.util.StringHelper;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class EmailOTPService {
	private static EmailOTPService SINGLE_INSTANCE = null;
	public static Map<String, String> properties;
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String ACR = "email_2fa";
	private IPersistenceService persistenceService;
	private EmailPerson person;
	ObjectMapper mapper;
	private long connectionTimeout = 5000;

	private EmailOTPService() {
		persistenceService = Utils.managedBean(IPersistenceService.class);
		reloadConfiguration();
		mapper = new ObjectMapper();
	}

	public static EmailOTPService getInstance() {
		if (SINGLE_INSTANCE == null) {
			synchronized (EmailOTPService.class) {
				SINGLE_INSTANCE = new EmailOTPService();
			}
		}
		return SINGLE_INSTANCE;
	}

	public void reloadConfiguration() {
		ObjectMapper mapper = new ObjectMapper();
		properties = persistenceService.getCustScriptConfigProperties(ACR);
		if (properties == null) {
			logger.warn(
					"Config. properties for custom script '{}' could not be read. Features related to {} will not be accessible",
					ACR, ACR.toUpperCase());
		} else {
			try {
				logger.info("Settings found were: {}", mapper.writeValueAsString(properties));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public String getScriptPropertyValue(String value) {
		return properties.get(value);
	}

	public List<BasicCredential> getCredentials(String uniqueIdOfTheUser) {

		List<VerifiedEmail> verifiedEmails = getVerifiedEmail(uniqueIdOfTheUser);
		List<BasicCredential> list = new ArrayList<BasicCredential>();
		for (VerifiedEmail v : verifiedEmails)
			list.add(new BasicCredential(v.getEmail(), v.getAddedOn()));

		return list;
	}

	public List<VerifiedEmail> getVerifiedEmail(String userId) {

		List<VerifiedEmail> verifiedEmails = new ArrayList<>();
		try {
			EmailPerson person = persistenceService.get(EmailPerson.class, persistenceService.getPersonDn(userId));
			String json = person.getEmailIds();
			json = Utils.isEmpty(json) ? "[]" : mapper.readTree(json).get("email-ids").toString();
			logger.debug("json - " + json);
			verifiedEmails = mapper.readValue(json, new TypeReference<List<VerifiedEmail>>() {
			});

			VerifiedEmail primaryMail = getExtraEmailId(person.getMail(), verifiedEmails);
			// implies that this has not been already added
			if (primaryMail != null) {
				updateEmailIdAdd(userId, verifiedEmails, primaryMail);
				verifiedEmails.add(primaryMail);

			}
			logger.trace("getVerifiedEmail. User '{}' has {}", userId,
					verifiedEmails.stream().map(VerifiedEmail::getEmail).collect(Collectors.toList()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return verifiedEmails;

	}

	public int getCredentialsTotal(String uniqueIdOfTheUser) {
		return getVerifiedEmail(uniqueIdOfTheUser).size();
	}

	public GluuConfiguration getConfiguration() {

		GluuConfiguration result = persistenceService.find(GluuConfiguration.class, "ou=configuration,o=gluu", null)
				.get(0);
		return result;
	}

	public boolean sendEmailWithOTP(String emailId, String subject, String body) {
		logger.debug("sendEmailWithOTP");
		SmtpConfiguration smtpConfiguration = getConfiguration().getSmtpConfiguration();
		logger.debug("SmtpConfiguration - " + smtpConfiguration.getHost());
		if (smtpConfiguration == null) {
			logger.error("Failed to send email. SMTP settings not found. Please configure SMTP settings in oxTrust");
			return false;
		}
		Properties prop = new Properties();
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", smtpConfiguration.isRequiresSsl());
		prop.put("mail.smtp.host", smtpConfiguration.getHost());
		prop.put("mail.smtp.port", smtpConfiguration.getPort());
		prop.put("mail.smtp.ssl.trust", smtpConfiguration.isServerTrust());

		Session session = Session.getInstance(prop, new Authenticator()  {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(smtpConfiguration.getUserName(),
						decrypt(smtpConfiguration.getPassword()));
			}
		});

		Message message = new MimeMessage(session);
		logger.debug("Session created");
		try {
			message.setFrom(new InternetAddress(smtpConfiguration.getFromEmailAddress()));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailId));
			message.setSubject(subject);

			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(body, "text/html; charset=utf-8");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);
			logger.debug("Before sending");
			message.setContent(multipart);

			Transport.send(message);
			logger.debug("after sending");
		} catch (MessagingException e) {
			logger.error("Failed to send OTP: " + e.getMessage());
			return false;
		}

		return true;
	}

	public boolean isEmailRegistered(String email) {

		EmailPerson person = new EmailPerson();
		person.setMail(email);
		person.setBaseDn(persistenceService.getPeopleDn());
		logger.debug("Registered email id count: " + persistenceService.count(person));
		return persistenceService.count(person) > 0;

	}

	public String encrypt(String password) {

		try {
			return Utils.stringEncrypter().encrypt(password);
		} catch (EncryptionException ex) {
			logger.error("Failed to encrypt SMTP password: ", ex);
			return null;
		}
	}

	public String decrypt(String password) {
		try {
			return Utils.stringEncrypter().decrypt(password);
		} catch (EncryptionException e) {
			logger.error("Unable to decrypt :" + e.getMessage());
			return null;
		}
	}

	public boolean addEmail(String userId, VerifiedEmail newEmail) {
		return updateEmailIdAdd(userId, getVerifiedEmail(userId), newEmail);
	}

	public boolean updateEmailIdAdd(String userId, List<VerifiedEmail> emails, VerifiedEmail newEmail) {

		boolean success = false;
		try {
			List<VerifiedEmail> vEmails = new ArrayList<>(emails);
			if (newEmail != null) {

				// uniqueness of the new mail has already been verified at previous step
				vEmails.add(newEmail);
			}

			List<String> mailIds = vEmails.stream().map(VerifiedEmail::getEmail).collect(Collectors.toList());
			String json = mailIds.size() > 0 ? mapper.writeValueAsString(Collections.singletonMap("email-ids", vEmails))
					: null;

			logger.debug("Updating email ids for user '{}'", userId);
			EmailPerson person = persistenceService.get(EmailPerson.class, persistenceService.getPersonDn(userId));
			person.setEmailIds(json);

			success = persistenceService.modify(person);

			if (success && newEmail != null) {
				// modify list only if LDAP update took place
				emails.add(newEmail);
				logger.debug("Added {}", newEmail.getEmail());
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return success;

	}

	Pair<String, String> getDeleteMessages(String nick, String extraMessage) {

		StringBuilder text = new StringBuilder();
		if (extraMessage != null) {
			text.append(extraMessage).append("\n\n");
		}
		text.append(Labels.getLabel("email_del_confirm",
				new String[] { nick == null ? Labels.getLabel("general.no_named") : nick }));
		if (extraMessage != null) {
			text.append("\n");
		}

		return new Pair<>(Labels.getLabel("email_del_title"), text.toString());

	}

	/**
	 * Creates an instance of VerifiedEmail by looking up in the list of
	 * VerifiedEmail passed. If the item is not found in the list, it means the user
	 * had already that mail added by means of another application, ie. oxTrust. In
	 * this case the resulting object will not have properties like nickname, etc.
	 * Just the mail id
	 * 
	 * @param mail Email id (LDAP attribute "mail" inside a user entry)
	 * @param list List of existing email ids enrolled. Ideally, there is an item
	 *             here corresponding to the uid number passed
	 * @return VerifiedMobile object
	 */
	private VerifiedEmail getExtraEmailId(String mail, List<VerifiedEmail> list) {
		VerifiedEmail vEmail = new VerifiedEmail(mail);
		Optional<VerifiedEmail> extraEmail = list.stream().filter(ph -> mail.equals(ph.getEmail())).findFirst();
		if (extraEmail.isPresent() == false) {
			vEmail.setNickName(mail);
			return vEmail;
		} else {
			return null;
		}
	}

	public boolean sendMail(SmtpConfiguration mailSmtpConfiguration, String from, String fromDisplayName, String to,
			String toDisplayName, String subject, String message, String htmlMessage) {
		if (mailSmtpConfiguration == null) {
			logger.error("Failed to send message from '{}' to '{}' because the SMTP configuration isn't valid!", from,
					to);
			return false;
		}

		logger.debug("Host name: " + mailSmtpConfiguration.getHost() + ", port: " + mailSmtpConfiguration.getPort()
				+ ", connection time out: " + this.connectionTimeout);

		String mailFrom = from;
		if (StringHelper.isEmpty(mailFrom)) {
			mailFrom = mailSmtpConfiguration.getFromEmailAddress();
		}

		String mailFromName = fromDisplayName;
		if (StringHelper.isEmpty(mailFromName)) {
			mailFromName = mailSmtpConfiguration.getFromName();
		}

		Properties props = new Properties();
		props.put("mail.smtp.host", mailSmtpConfiguration.getHost());
		props.put("mail.smtp.port", mailSmtpConfiguration.getPort());
		props.put("mail.from", mailFrom);
		props.put("mail.smtp.connectiontimeout", this.connectionTimeout);
		props.put("mail.smtp.timeout", this.connectionTimeout);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.ssl.trust", mailSmtpConfiguration.getHost());

		if (mailSmtpConfiguration.isRequiresSsl()) {
			props.put("mail.smtp.socketFactory.port", mailSmtpConfiguration.getPort());
			props.put("mail.smtp.starttls.enable", true);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		}

		Session session = null;
		if (mailSmtpConfiguration.isRequiresAuthentication()) {
			props.put("mail.smtp.auth", "true");

			final String userName = mailSmtpConfiguration.getUserName();
			final String password = mailSmtpConfiguration.getPasswordDecrypted();

			session = Session.getInstance(props, new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(userName, password);
				}
			});
		} else {
			session = Session.getInstance(props, null);
		}

		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress(mailFrom, mailFromName));
			if (StringHelper.isEmpty(toDisplayName)) {
				msg.setRecipients(Message.RecipientType.TO, to);
			} else {
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to, toDisplayName));
			}
			msg.setSubject(subject, "UTF-8");
			msg.setSentDate(new Date());

			if (StringHelper.isEmpty(htmlMessage)) {
				msg.setText(message + "\n", "UTF-8", "plain");
			} else {
				// Unformatted text version
				final MimeBodyPart textPart = new MimeBodyPart();
				textPart.setText(message, "UTF-8", "plain");
				// HTML version
				final MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.setText(htmlMessage, "UTF-8", "html");

				// Create the Multipart. Add BodyParts to it.
				final Multipart mp = new MimeMultipart("alternative");
				mp.addBodyPart(textPart);
				mp.addBodyPart(htmlPart);

				// Set Multipart as the message's content
				msg.setContent(mp);
			}

			Transport.send(msg);
		} catch (Exception ex) {
			logger.error("Failed to send message", ex);
			return false;
		}

		return true;
	}

	public static String getMaskedEmail(String email) {
		String pattern = "([^@]+)@(.*)\\.(.*)";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(email);
		if (m.find()) {
			StringBuilder sb = new StringBuilder("");
			sb.append(m.group(1).charAt(0));
			sb.append(m.group(1).substring(1).replaceAll(".", "*"));
			sb.append("@");

			sb.append(m.group(2).charAt(0));
			sb.append(m.group(2).substring(1).replaceAll(".", "*"));

			sb.append(".").append(m.group(3));
			return sb.toString();
		}
		return null;
	}

}
