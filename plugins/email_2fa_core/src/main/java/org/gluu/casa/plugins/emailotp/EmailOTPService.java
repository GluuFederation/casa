package org.gluu.casa.plugins.emailotp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FilenameUtils;
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
import org.gluu.util.security.SecurityProviderUtility;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class EmailOTPService {

    private static final Logger logger = LoggerFactory.getLogger(EmailOTPService.class);

    private static EmailOTPService singleInstance = null;

    public static final String ACR = "email_2fa_core";

    private Map<String, String> properties;
	private IPersistenceService persistenceService;
	private ObjectMapper mapper;
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
		if (singleInstance == null) {
			synchronized (EmailOTPService.class) {
				singleInstance = new EmailOTPService();
			}
		}
		return singleInstance;
	}

	public void init() {
        persistenceService = Utils.managedBean(IPersistenceService.class);

        persistenceService.initialize();

        reloadConfiguration();
        mapper = new ObjectMapper();

        SmtpConfiguration smtpConfiguration = getConfiguration().getSmtpConfiguration();

        String keystoreFile = smtpConfiguration.getKeyStore();
        String keystoreSecret = decrypt(smtpConfiguration.getKeyStorePassword());

        SecurityProviderUtility.KeyStorageType keystoreType = solveKeyStorageType(keystoreFile);

        try(InputStream is = new FileInputStream(keystoreFile)) {
            switch (keystoreType) {
            case JKS_KS: {
                keyStore = KeyStore.getInstance("JKS");
                break;
            }
            case PKCS12_KS: {
                keyStore = KeyStore.getInstance("PKCS12", SecurityProviderUtility.getBCProvider());
                break;
            }
            case BCFKS_KS: {
                keyStore = KeyStore.getInstance("BCFKS", SecurityProviderUtility.getBCProvider());
                break;
            }
            }
            keyStore.load(is, keystoreSecret.toCharArray());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
	}

	/**
	 * 
	 */
	public void reloadConfiguration() {
		ObjectMapper localMapper = new ObjectMapper();
		properties = persistenceService.getCustScriptConfigProperties(ACR);
		if (properties == null) {
			logger.warn(
					"Config. properties for custom script '{}' could not be read. Features related to {} will not be accessible",
					ACR, ACR.toUpperCase());
		} else {
			try {
				logger.info("Settings found were: {}", localMapper.writeValueAsString(properties));
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

        Properties props = new Properties();

        props.put("mail.from", "Gluu Casa");

        SmtpConnectProtectionType smtpConnectProtect = smtpConfiguration.getConnectProtection();

        if (smtpConnectProtect == SmtpConnectProtectionType.START_TLS) {
            props.put("mail.transport.protocol", "smtp");

            props.put("mail.smtp.host", smtpConfiguration.getHost());
            props.put("mail.smtp.port", smtpConfiguration.getPort());
            props.put("mail.smtp.connectiontimeout", this.connectionTimeout);
            props.put("mail.smtp.timeout", this.connectionTimeout);

            props.put("mail.smtp.socketFactory.class", "com.sun.mail.util.MailSSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());

            if (smtpConfiguration.isServerTrust()) {
                props.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            }
            
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.starttls.required", true);
        }
        else if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
            props.put("mail.transport.protocol.rfc822", "smtps");

            props.put("mail.smtps.host", smtpConfiguration.getHost());
            props.put("mail.smtps.port", smtpConfiguration.getPort());
            props.put("mail.smtps.connectiontimeout", this.connectionTimeout);
            props.put("mail.smtps.timeout", this.connectionTimeout);

            props.put("mail.smtp.socketFactory.class", "com.sun.mail.util.MailSSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());

            if (smtpConfiguration.isServerTrust()) {
                props.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            }

            props.put("mail.smtp.ssl.enable", true);
        } 
        else {
            props.put("mail.transport.protocol", "smtp");

            props.put("mail.smtp.host", smtpConfiguration.getHost());
            props.put("mail.smtp.port", smtpConfiguration.getPort());
            props.put("mail.smtp.connectiontimeout", this.connectionTimeout);
            props.put("mail.smtp.timeout", this.connectionTimeout);
        }

        Session session = null;
        if (smtpConfiguration.isRequiresAuthentication()) {

            if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
                props.put("mail.smtps.auth", "true");
            }
            else {
                props.put("mail.smtp.auth", "true");
            }

            final String userName = smtpConfiguration.getUserName();
            final String password = decrypt(smtpConfiguration.getPassword());

            session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            });
        } else {
            session = Session.getInstance(props, null);
        }

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

        Properties props = new Properties();

        props.put("mail.from", "Gluu Casa");

        SmtpConnectProtectionType smtpConnectProtect = smtpConfiguration.getConnectProtection();

        if (smtpConnectProtect == SmtpConnectProtectionType.START_TLS) {
            props.put("mail.transport.protocol", "smtp");

            props.put("mail.smtp.host", smtpConfiguration.getHost());
            props.put("mail.smtp.port", smtpConfiguration.getPort());
            props.put("mail.smtp.connectiontimeout", this.connectionTimeout);
            props.put("mail.smtp.timeout", this.connectionTimeout);

            props.put("mail.smtp.socketFactory.class", "com.sun.mail.util.MailSSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());
            if (smtpConfiguration.isServerTrust()) {
                props.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            }
            props.put("mail.smtp.starttls.enable", true);
            props.put("mail.smtp.starttls.required", true);
        }
        else if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
            props.put("mail.transport.protocol.rfc822", "smtps");

            props.put("mail.smtps.host", smtpConfiguration.getHost());
            props.put("mail.smtps.port", smtpConfiguration.getPort());
            props.put("mail.smtps.connectiontimeout", this.connectionTimeout);
            props.put("mail.smtps.timeout", this.connectionTimeout);

            props.put("mail.smtp.socketFactory.class", "com.sun.mail.util.MailSSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());
            if (smtpConfiguration.isServerTrust()) {
                props.put("mail.smtp.ssl.trust", smtpConfiguration.getHost());
            }
            props.put("mail.smtp.ssl.enable", true);
        } 
        else {
            props.put("mail.transport.protocol", "smtp");

            props.put("mail.smtp.host", smtpConfiguration.getHost());
            props.put("mail.smtp.port", smtpConfiguration.getPort());
            props.put("mail.smtp.connectiontimeout", this.connectionTimeout);
            props.put("mail.smtp.timeout", this.connectionTimeout);
        }

        Session session = null;
        if (smtpConfiguration.isRequiresAuthentication()) {

            if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
                props.put("mail.smtps.auth", "true");
            }
            else {
                props.put("mail.smtp.auth", "true");
            }

            final String userName = smtpConfiguration.getUserName();
            final String password = decrypt(smtpConfiguration.getPassword());

            session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            });
        } else {
            session = Session.getInstance(props, null);
        }

        PrivateKey privateKey = null;

        Certificate certificate = null; 
        X509Certificate x509Certificate = null;

        try {
            privateKey = (PrivateKey)keyStore.getKey(smtpConfiguration.getKeyStoreAlias(),
                    decrypt(smtpConfiguration.getKeyStorePassword()).toCharArray());
            
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
     * @return
     */
    private SecurityProviderUtility.KeyStorageType solveKeyStorageType(final String keyStoreFile) {
        SecurityProviderUtility.SecurityModeType securityMode = SecurityProviderUtility.getSecurityMode();
        if (securityMode == null) {
            throw new InvalidParameterException("Security Mode wasn't initialized. Call installBCProvider() before");
        }
        String keyStoreExt = FilenameUtils.getExtension(keyStoreFile);
        SecurityProviderUtility.KeyStorageType keyStorageType = SecurityProviderUtility.KeyStorageType.fromExtension(keyStoreExt);
        boolean ksTypeFound = false;
        for (SecurityProviderUtility.KeyStorageType ksType : securityMode.getKeystorageTypes()) {
            if (keyStorageType == ksType) {
                ksTypeFound = true;
                break;
            }
        }
        if (!ksTypeFound) {
            switch (securityMode) {
            case BCFIPS_SECURITY_MODE: {
                keyStorageType =  SecurityProviderUtility.KeyStorageType.BCFKS_KS;
                break;
            }
            case BCPROV_SECURITY_MODE: {
                keyStorageType = SecurityProviderUtility.KeyStorageType.PKCS12_KS;
                break;
            }
            }
        }
        return keyStorageType;
    }
}
