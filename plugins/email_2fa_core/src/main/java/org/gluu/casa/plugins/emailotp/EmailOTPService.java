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
import java.util.*;
import java.util.stream.Collectors;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.smime.*;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.*;
import org.bouncycastle.operator.OperatorCreationException;
import org.gluu.casa.credential.BasicCredential;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.emailotp.model.*;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.util.security.SecurityProviderUtility;
import org.gluu.util.security.SecurityProviderUtility.SecurityModeType;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Pair;
import org.zkoss.util.resource.Labels;

public class EmailOTPService {

    private static final Logger logger = LoggerFactory.getLogger(EmailOTPService.class);

    private static EmailOTPService singleInstance = null;

    public static final String ACR = "email_2fa";

    public static final String DEF_MAIL_FROM                        = "mail.from";
    public static final String DEF_MAIL_TRANSPORT_PROTOCOL          = "mail.transport.protocol";
    public static final String DEF_MAIL_SMTP_HOST                   = "mail.smtp.host";
    public static final String DEF_MAIL_SMTP_PORT                   = "mail.smtp.port";
    public static final String DEF_MAIL_SMTP_CONNECTION_TIMEOUT     = "mail.smtp.connectiontimeout";
    public static final String DEF_MAIL_SMTP_TIMEOUT                = "mail.smtp.timeout";
    public static final String DEF_MAIL_SMTP_SOCKET_FACTORY_CLASS   = "mail.smtp.socketFactory.class";
    public static final String DEF_MAIL_SMTP_SOCKET_FACTORY_PORT    = "mail.smtp.socketFactory.port";
    public static final String DEF_MAIL_SMTP_SSL_TRUST              = "mail.smtp.ssl.trust";
    public static final String DEF_MAIL_SMTP_STARTTLS_ENABLE        = "mail.smtp.starttls.enable";
    public static final String DEF_MAIL_SMTP_STARTTLS_REQUIRED      = "mail.smtp.starttls.required";
    public static final String DEF_MAIL_TRANSPORT_PROTOCOL_RFC822   = "mail.transport.protocol.rfc822";
    public static final String DEF_MAIL_SMTP_SSL_ENABLE             = "mail.smtp.ssl.enable";
    public static final String DEF_MAIL_SMTPS_AUTH                  = "mail.smtps.auth";
    public static final String DEF_MAIL_SMTP_AUTH                   = "mail.smtp.auth";
    public static final String DEF_MAIL_SMTPS_HOST                  = "mail.smtps.host";
    public static final String DEF_MAIL_SMTPS_PORT                  = "mail.smtps.port";
    public static final String DEF_MAIL_SMTPS_CONNECTION_TIMEOUT    = "mail.smtps.connectiontimeout";
    public static final String DEF_MAIL_SMTPS_TIMEOUT               = "mail.smtps.timeout";
    public static final String DEF_MAIL_SSL_SOCKET_FACTORY          = "com.sun.mail.util.MailSSLSocketFactory";

    private Map<String, String> properties;
	private IPersistenceService persistenceService;
	private long connectionTimeout = 5000;
    private KeyStore keyStore;

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
		properties = persistenceService.getCustScriptConfigProperties(ACR);
		if (properties == null) {
		    if (logger.isWarnEnabled()) { // according to Sonar request, as ACR.toUpperCase() is provided before checking    
	            logger.warn(
	                    "Config. properties for custom script '{}' could not be read. Features related to {} will not be accessible",
	                    ACR, ACR.toUpperCase());
		    }
		} else {
			logger.info("Configuration parsed");
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
		List<BasicCredential> list = new ArrayList<>();
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
            String searchMask = String.format("inum=%s,ou=people,o=gluu", userId);
            EmailPerson testPerson = new EmailPerson();
            testPerson.setBaseDn(searchMask);

			EmailPerson person = persistenceService.get(EmailPerson.class, persistenceService.getPersonDn(userId));
			List<String> emails = Optional.ofNullable(person.getMails()).orElse(Collections.emptyList());
			
			emails.forEach(m -> verifiedEmails.add(new VerifiedEmail(m)));
			/*
			for (String mail : emails) {
			    verifiedEmails.add(new VerifiedEmail(mail));
			}
				*/		
			logger.info("getVerifiedEmail. User {} has {}", userId, emails);
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
		return persistenceService.find(GluuConfiguration.class, "ou=configuration,o=gluu", null).get(0);
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

        props.put(DEF_MAIL_FROM, "Gluu Casa");

        SmtpConnectProtectionType smtpConnectProtect = smtpConfiguration.getConnectProtection();

        if (smtpConnectProtect == SmtpConnectProtectionType.START_TLS) {
            props.put(DEF_MAIL_TRANSPORT_PROTOCOL, "smtp");

            props.put(DEF_MAIL_SMTP_HOST, smtpConfiguration.getHost());
            props.put(DEF_MAIL_SMTP_PORT, smtpConfiguration.getPort());
            props.put(DEF_MAIL_SMTP_CONNECTION_TIMEOUT, this.connectionTimeout);
            props.put(DEF_MAIL_SMTP_TIMEOUT, this.connectionTimeout);

            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_CLASS, DEF_MAIL_SSL_SOCKET_FACTORY);
            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_PORT, smtpConfiguration.getPort());

            if (smtpConfiguration.isServerTrust()) {
                props.put(DEF_MAIL_SMTP_SSL_TRUST, smtpConfiguration.getHost());
            }

            props.put(DEF_MAIL_SMTP_STARTTLS_ENABLE, true);
            props.put(DEF_MAIL_SMTP_STARTTLS_REQUIRED, true);
        }
        else if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
            props.put(DEF_MAIL_TRANSPORT_PROTOCOL_RFC822, "smtps");

            props.put(DEF_MAIL_SMTPS_HOST, smtpConfiguration.getHost());
            props.put(DEF_MAIL_SMTPS_PORT, smtpConfiguration.getPort());
            props.put(DEF_MAIL_SMTPS_CONNECTION_TIMEOUT, this.connectionTimeout);
            props.put(DEF_MAIL_SMTPS_TIMEOUT, this.connectionTimeout);

            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_CLASS, DEF_MAIL_SSL_SOCKET_FACTORY);
            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_PORT, smtpConfiguration.getPort());

            if (smtpConfiguration.isServerTrust()) {
                props.put(DEF_MAIL_SMTP_SSL_TRUST, smtpConfiguration.getHost());
            }

            props.put(DEF_MAIL_SMTP_SSL_ENABLE, true);
        }
        else {
            props.put(DEF_MAIL_TRANSPORT_PROTOCOL, "smtp");

            props.put(DEF_MAIL_SMTP_HOST, smtpConfiguration.getHost());
            props.put(DEF_MAIL_SMTP_PORT, smtpConfiguration.getPort());
            props.put(DEF_MAIL_SMTP_CONNECTION_TIMEOUT, this.connectionTimeout);
            props.put(DEF_MAIL_SMTP_TIMEOUT, this.connectionTimeout);
        }

        Session session = null;
        if (smtpConfiguration.isRequiresAuthentication()) {

            if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
                props.put(DEF_MAIL_SMTPS_AUTH, "true");
            }
            else {
                props.put(DEF_MAIL_SMTP_AUTH, "true");
            }

            final String userName = smtpConfiguration.getUserName();
            final String password = decrypt(smtpConfiguration.getPassword());

            session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
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
			logger.error("Failed to send OTP: {}", e.getMessage());
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

        props.put(DEF_MAIL_FROM, "Gluu Casa");

        SmtpConnectProtectionType smtpConnectProtect = smtpConfiguration.getConnectProtection();

        if (smtpConnectProtect == SmtpConnectProtectionType.START_TLS) {
            props.put(DEF_MAIL_TRANSPORT_PROTOCOL, "smtp");

            props.put(DEF_MAIL_SMTP_HOST, smtpConfiguration.getHost());
            props.put(DEF_MAIL_SMTP_PORT, smtpConfiguration.getPort());
            props.put(DEF_MAIL_SMTP_CONNECTION_TIMEOUT, this.connectionTimeout);
            props.put(DEF_MAIL_SMTP_TIMEOUT, this.connectionTimeout);

            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_CLASS, DEF_MAIL_SSL_SOCKET_FACTORY);
            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_PORT, smtpConfiguration.getPort());
            if (smtpConfiguration.isServerTrust()) {
                props.put(DEF_MAIL_SMTP_SSL_TRUST, smtpConfiguration.getHost());
            }
            props.put(DEF_MAIL_SMTP_STARTTLS_ENABLE, true);
            props.put(DEF_MAIL_SMTP_STARTTLS_REQUIRED, true);
        }
        else if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
            props.put(DEF_MAIL_TRANSPORT_PROTOCOL_RFC822, "smtps");

            props.put(DEF_MAIL_SMTPS_HOST, smtpConfiguration.getHost());
            props.put(DEF_MAIL_SMTPS_PORT, smtpConfiguration.getPort());
            props.put(DEF_MAIL_SMTPS_CONNECTION_TIMEOUT, this.connectionTimeout);
            props.put(DEF_MAIL_SMTPS_TIMEOUT, this.connectionTimeout);

            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_CLASS, DEF_MAIL_SSL_SOCKET_FACTORY);
            props.put(DEF_MAIL_SMTP_SOCKET_FACTORY_PORT, smtpConfiguration.getPort());
            if (smtpConfiguration.isServerTrust()) {
                props.put(DEF_MAIL_SMTP_SSL_TRUST, smtpConfiguration.getHost());
            }
            props.put(DEF_MAIL_SMTP_SSL_ENABLE, true);
        }
        else {
            props.put(DEF_MAIL_TRANSPORT_PROTOCOL, "smtp");

            props.put(DEF_MAIL_SMTP_HOST, smtpConfiguration.getHost());
            props.put(DEF_MAIL_SMTP_PORT, smtpConfiguration.getPort());
            props.put(DEF_MAIL_SMTP_CONNECTION_TIMEOUT, this.connectionTimeout);
            props.put(DEF_MAIL_SMTP_TIMEOUT, this.connectionTimeout);
        }

        Session session = null;
        if (smtpConfiguration.isRequiresAuthentication()) {

            if (smtpConnectProtect == SmtpConnectProtectionType.SSL_TLS) {
                props.put(DEF_MAIL_SMTPS_AUTH, "true");
            }
            else {
                props.put(DEF_MAIL_SMTP_AUTH, "true");
            }

            final String userName = smtpConfiguration.getUserName();
            final String password = decrypt(smtpConfiguration.getPassword());

            session = Session.getInstance(props, new javax.mail.Authenticator() {
                @Override
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
            logger.error("Failed to send OTP: {}", e);
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
        List<X509Certificate> certList = new ArrayList<>();
        certList.add(cert);
        JcaCertStore certs = new JcaCertStore(certList);
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
			logger.error("Unable to decrypt: {}", e.getMessage());
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

			List<String> mails = vEmails.stream().map(VerifiedEmail::getEmail).collect(Collectors.toList());
			
			person.setMails(mails);
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
            if (securityMode == SecurityModeType.BCFIPS_SECURITY_MODE) {
                keyStorageType = SecurityProviderUtility.KeyStorageType.BCFKS_KS;
            }
            else if (securityMode == SecurityModeType.BCPROV_SECURITY_MODE) {
                keyStorageType = SecurityProviderUtility.KeyStorageType.PKCS12_KS;
            }
        }
        return keyStorageType;
    }

}
