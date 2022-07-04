package org.gluu.casa.plugins.emailotp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.mail.smime.SMIMEUtil;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.emailotp.model.EmailPerson;
import org.gluu.casa.plugins.emailotp.model.GluuConfiguration;
import org.gluu.casa.plugins.emailotp.model.SmtpConfiguration;
import org.gluu.casa.plugins.emailotp.model.SmtpConnectProtectionType;
import org.gluu.casa.plugins.emailotp.model.VerifiedEmail;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.util.StringHelper;
import org.gluu.util.security.SecurityProviderUtility;
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
	private static Logger logger = LoggerFactory.getLogger(EmailOTPService.class);
	public static String ACR = "email_2fa_core";
	private IPersistenceService persistenceService;
	ObjectMapper mapper;
	private long connectionTimeout = 5000;
    private KeyStore keyStore;

    static {
        SecurityProviderUtility.installBCProvider();
    }

    /**
     * 
     */
	private EmailOTPService() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap(); 
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html"); 
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml"); 
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain"); 
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed"); 
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");		
	}

	/**
	 * 
	 * @return
	 */
	public static EmailOTPService getInstance() {
		if (SINGLE_INSTANCE == null) {
			synchronized (EmailOTPService.class) {
				SINGLE_INSTANCE = new EmailOTPService();
			}
		}
		return SINGLE_INSTANCE;
	}

	/**
	 * 
	 * @param pluginId
	 */
	public void init(String pluginId) {
        persistenceService = Utils.managedBean(IPersistenceService.class);

        persistenceService.initialize();

        reloadConfiguration();
        mapper = new ObjectMapper();

        SmtpConfiguration smtpConfiguration = getConfiguration().getSmtpConfiguration();

        String keystoreFile = smtpConfiguration.getKeyStore();
        String keystoreSecret = smtpConfiguration.getKeyStorePassword();

        try(InputStream is = new FileInputStream(keystoreFile)) {
            keyStore = KeyStore.getInstance("PKCS12", SecurityProviderUtility.getBCProvider());
            keyStore.load(is, keystoreSecret.toCharArray());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
	}

	/**
	 * 
	 */
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

	/**
	 * 
	 * @param value
	 * @return
	 */
	public String getScriptPropertyValue(String value) {
		return properties.get(value);
	}

	/**
	 * 
	 * @param uniqueIdOfTheUser
	 * @return
	 */
	public List<BasicCredential> getCredentials(String uniqueIdOfTheUser) {

		List<VerifiedEmail> verifiedEmails = getVerifiedEmail(uniqueIdOfTheUser);
		List<BasicCredential> list = new ArrayList<BasicCredential>();
		for (VerifiedEmail v : verifiedEmails)
			list.add(new BasicCredential(v.getEmail(), v.getAddedOn()));

		return list;
	}

	/**
	 * 
	 * @param userId
	 * @return
	 */
	public List<VerifiedEmail> getVerifiedEmail(String userId) {
		List<VerifiedEmail> verifiedEmails = new ArrayList<>();
		try {
            EmailPerson testPerson = new EmailPerson();

            String searchMask = String.format("inum=%s,ou=people,o=gluu", userId);
            testPerson.setBaseDn(searchMask);

			EmailPerson person = persistenceService.get(EmailPerson.class, new String(persistenceService.getPersonDn(userId)));
			String json = person.getOxEmailAlternate();
			json = Utils.isEmpty(json) ? "[]" : mapper.readTree(json).get("email-ids").toString();
			verifiedEmails = mapper.readValue(json, new TypeReference<List<VerifiedEmail>>() { });
			VerifiedEmail primaryMail = getExtraEmailId(person.getMail(), verifiedEmails);
			// implies that this has not been already added
			if (primaryMail != null) {
				updateEmailIdAdd(userId, verifiedEmails, primaryMail);
				verifiedEmails.add(primaryMail);

			}
			logger.info("getVerifiedEmail. User '{}' has {}", userId,
					verifiedEmails.stream().map(VerifiedEmail::getEmail).collect(Collectors.toList()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return verifiedEmails;
	}

	/**
	 * 
	 * @param uniqueIdOfTheUser
	 * @return
	 */
	public int getCredentialsTotal(String uniqueIdOfTheUser) {
		return getVerifiedEmail(uniqueIdOfTheUser).size();
	}

	/**
	 * 
	 * @return
	 */
	public GluuConfiguration getConfiguration() {
		GluuConfiguration result = persistenceService.find(GluuConfiguration.class, "ou=configuration,o=gluu", null).get(0);
		return result;
	}

	/**
	 * 
	 * @param emailId
	 * @param subject
	 * @param body
	 * @return
	 */
	public boolean sendEmailWithOTP(String emailId, String subject, String body) {
		SmtpConfiguration smtpConfiguration = getConfiguration().getSmtpConfiguration();
		if (smtpConfiguration == null) {
			logger.error("Failed to send email. SMTP settings not found. Please configure SMTP settings in oxTrust");
			return false;
		}
		Properties prop = new Properties();

		prop.put("mail.smtp.auth", true);

        prop.put("mail.smtp.host", smtpConfiguration.getHost());
        prop.put("mail.smtp.port", smtpConfiguration.getPort());

        prop.put("mail.from", "Gluu Casa");
        prop.put("mail.transport.protocol", "smtp");

        SmtpConnectProtectionType smtpConnectProtect = smtpConfiguration.getConnectProtection();

        if (smtpConnectProtect == SmtpConnectProtectionType.StartTls) {
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            prop.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());
            prop.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            prop.put("mail.smtp.starttls.enable", true);
        }
        else if (smtpConnectProtect == SmtpConnectProtectionType.SslTls) {
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            prop.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());
            prop.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            prop.put("mail.smtp.ssl.enable", true);
        }

		Session session = Session.getInstance(prop, new Authenticator()  {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(smtpConfiguration.getUserName(),
						decrypt(smtpConfiguration.getPassword()));
			}
		});

		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(smtpConfiguration.getFromEmailAddress(), smtpConfiguration.getFromName()));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailId));
			message.setSubject(subject);

			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(body, "text/html; charset=utf-8");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);
			message.setContent(multipart);

			Transport.send(message);
		} catch (Exception e) {
			logger.error("Failed to send OTP: " + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param emailId
	 * @param subject
	 * @param body
	 * @return
	 */
    public boolean sendEmailWithOTPSigned(String emailId, String subject, String body) {
        SmtpConfiguration smtpConfiguration = getConfiguration().getSmtpConfiguration();
        if (smtpConfiguration == null) {
            logger.error("Failed to send email. SMTP settings not found. Please configure SMTP settings in oxTrust");
            return false;
        }

        Properties prop = new Properties();

        prop.put("mail.smtp.auth", true);

        prop.put("mail.smtp.host", smtpConfiguration.getHost());
        prop.put("mail.smtp.port", smtpConfiguration.getPort());

        prop.put("mail.from", "Gluu Casa");
        prop.put("mail.transport.protocol", "smtp");

        SmtpConnectProtectionType smtpConnectProtect = smtpConfiguration.getConnectProtection();

        if (smtpConnectProtect == SmtpConnectProtectionType.StartTls) {
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            prop.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());
            prop.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            prop.put("mail.smtp.starttls.enable", true);
        }
        else if (smtpConnectProtect == SmtpConnectProtectionType.SslTls) {
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            prop.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());
            prop.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            prop.put("mail.smtp.ssl.enable", true);
        }

        Session session = Session.getInstance(prop, new Authenticator()  {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpConfiguration.getUserName(),
                        decrypt(smtpConfiguration.getPassword()));
            }
        });

        PrivateKey privateKey = null;

        Certificate certificate = null; 
        X509Certificate x509Certificate = null;

        try {
            privateKey = (PrivateKey)keyStore.getKey(smtpConfiguration.getKeyStoreAlias(),
                    smtpConfiguration.getKeyStorePassword().toCharArray());
            
            certificate = keyStore.getCertificate(smtpConfiguration.getKeyStoreAlias());
            x509Certificate = (X509Certificate)certificate;
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        Message message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(smtpConfiguration.getFromEmailAddress()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailId));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(body, "text/html; charset=utf-8");

            MimeMultipart multiPart = createMultipartWithSignature(privateKey, x509Certificate, smtpConfiguration.getSigningAlgorithm(), mimeBodyPart);            

            message.setContent(multiPart);

            Transport.send(message);
        } catch (Exception e) {
            logger.error("Failed to send OTP: " + e.getMessage());
            return false;
        }

        return true;
    }
    
    /**
     * @param cert
     * @return
     * @throws CertificateParsingException
     */
    private static ASN1EncodableVector generateSignedAttributes(X509Certificate cert) throws CertificateParsingException {
        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector caps = new SMIMECapabilityVector();
        caps.addCapability(SMIMECapability.aES256_CBC);
        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.rC2_CBC, 128);
        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));
        signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(SMIMEUtil.createIssuerAndSerialNumberFor(cert)));
        return signedAttrs;
    }

    /**
     * 
     * @param key
     * @param cert
     * @param signingAlgorithm
     * @param dataPart
     * @return
     * @throws CertificateEncodingException
     * @throws CertificateParsingException
     * @throws OperatorCreationException
     * @throws SMIMEException
     */
    public static MimeMultipart createMultipartWithSignature(PrivateKey key, X509Certificate cert, String signingAlgorithm, MimeBodyPart dataPart) throws CertificateEncodingException, CertificateParsingException, OperatorCreationException, SMIMEException {
        List<X509Certificate> certList = new ArrayList<X509Certificate>();
        certList.add(cert);
        Store certs = new JcaCertStore(certList);
        ASN1EncodableVector signedAttrs = generateSignedAttributes(cert);

        SMIMESignedGenerator gen = new SMIMESignedGenerator();

        if (Utils.isEmpty(signingAlgorithm)) {
            signingAlgorithm = cert.getSigAlgName();
        }

        gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider(SecurityProviderUtility.getBCProvider()).setSignedAttributeGenerator(new AttributeTable(signedAttrs)).build(signingAlgorithm, key, cert));

        gen.addCertificates(certs);

        return gen.generate(dataPart);
    }    

    /**
     * 
     * @param email
     * @return
     */
	public boolean isEmailRegistered(String email) {

		EmailPerson person = new EmailPerson();
		person.setMail(email);
		person.setBaseDn(persistenceService.getPeopleDn());
		logger.debug("Registered email id count: " + persistenceService.count(person));
		return persistenceService.count(person) > 0;

	}

	/**
	 * 
	 * @param password
	 * @return
	 */
	public String encrypt(String password) {
		try {
			return Utils.stringEncrypter().encrypt(password);
		} catch (EncryptionException ex) {
			logger.error("Failed to encrypt SMTP password: ", ex);
			return null;
		}
	}

	/**
	 * 
	 * @param password
	 * @return
	 */
	public String decrypt(String password) {
		try {
			return Utils.stringEncrypter().decrypt(password);
		} catch (EncryptionException e) {
			logger.error("Unable to decrypt :" + e.getMessage());
			return null;
		}
	}

	/**
	 * 
	 * @param userId
	 * @param newEmail
	 * @return
	 */
	public boolean addEmail(String userId, VerifiedEmail newEmail) {
		return updateEmailIdAdd(userId, getVerifiedEmail(userId), newEmail);
	}

	/**
	 * 
	 * @param userId
	 * @param emails
	 * @param newEmail
	 * @return
	 */
	public boolean updateEmailIdAdd(String userId, List<VerifiedEmail> emails, VerifiedEmail newEmail) {
		boolean success = false;
		try {
			EmailPerson person = persistenceService.get(EmailPerson.class, persistenceService.getPersonDn(userId));

			List<VerifiedEmail> vEmails = new ArrayList<>(emails);
			if (newEmail != null) {
				// uniqueness of the new mail has already been verified at previous step
				vEmails.add(newEmail);
			}

			List<String> mailIds = vEmails.stream().map(VerifiedEmail::getEmail).collect(Collectors.toList());
			String json = mailIds.size() > 0 ? mapper.writeValueAsString(Collections.singletonMap("email-ids", vEmails))
					: null;

			person.setOxEmailAlternate(json);

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

	/**
	 * 
	 * @param nick
	 * @param extraMessage
	 * @return
	 */
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

	/**
	 * 
	 * @param mailSmtpConfiguration
	 * @param from
	 * @param fromDisplayName
	 * @param to
	 * @param toDisplayName
	 * @param subject
	 * @param message
	 * @param htmlMessage
	 * @return
	 */
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

        SmtpConnectProtectionType smtpConnectProtect = mailSmtpConfiguration.getConnectProtection();

        if (smtpConnectProtect == SmtpConnectProtectionType.StartTls) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", mailSmtpConfiguration.getPort());
            props.put("mail.smtp.ssl.trust", mailSmtpConfiguration.getHost());
            props.put("mail.smtp.starttls.enable", true);
        }
        else if (smtpConnectProtect == SmtpConnectProtectionType.SslTls) {
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", mailSmtpConfiguration.getPort());
            props.put("mail.smtp.ssl.trust", mailSmtpConfiguration.getHost());
            props.put("mail.smtp.ssl.enable", true);
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
}
