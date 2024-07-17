from java.lang import Long, System
from java.io import FileInputStream
from java.util import Arrays, HashSet, Properties
from java.util.stream import IntStream
from java.security import KeyStore, SecureRandom, Security

from javax.activation import CommandMap
from javax.faces.application import FacesMessage
from javax.faces.context import FacesContext
from javax.mail import Session, Message
from javax.mail.internet import MimeMessage, InternetAddress

from org.bouncycastle.asn1 import ASN1EncodableVector
from org.bouncycastle.cert.jcajce import JcaCertStore
from org.bouncycastle.cms.jcajce import JcaSimpleSignerInfoGeneratorBuilder
from org.bouncycastle.asn1.cms import AttributeTable, IssuerAndSerialNumber
from org.bouncycastle.asn1.smime import SMIMECapability, SMIMECapabilitiesAttribute, SMIMECapabilityVector, SMIMEEncryptionKeyPreferenceAttribute
from org.bouncycastle.asn1.x500 import X500Name
from org.bouncycastle.mail.smime import SMIMESignedGenerator, SMIMEUtil

from org.gluu.oxauth.security import Identity
from org.gluu.oxauth.service import AuthenticationService, UserService
from org.gluu.oxauth.service.common import ApplicationFactory, EncryptionService
from org.gluu.oxauth.util import ServerUtil
from org.gluu.jsf2.message import FacesMessages
from org.gluu.model import SmtpConnectProtectionType
from org.gluu.model.custom.script.type.auth import PersonAuthenticationType
from org.gluu.service.cdi.util import CdiUtil
from org.gluu.util import StringHelper
from org.gluu.util.security import SecurityProviderUtility

import java
import json
import os.path
import random
import sys

class PersonAuthentication(PersonAuthenticationType):
    def __init__(self, currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis

    def init(self, customScript, configurationAttributes):
        print "EmailOTP. Initializing"
        self.sender = EmailSender(CdiUtil.bean(ApplicationFactory).getSmtpConfiguration())
        self.RAND = SecureRandom()
        
        self.otpLength = configurationAttributes.get("otp_length")
        self.otpLength = 6 if self.otpLength == None else int(self.otpLength.getValue2())

        self.otpLifetime = configurationAttributes.get("otp_lifetime")
        self.otpLifetime = 1 if self.otpLifetime == None else int(self.otpLifetime.getValue2()) 
        
        print "EmailOTP. Using %i and %i for OTP length (digits) and lifetime (minutes) respectively" % (self.otpLength, self.otpLifetime)
        print "EmailOTP. Initialized successfully"
        return True

    def destroy(self, configurationAttributes):
        print "EmailOTP. Destroyed successfully"
        return True

    def getApiVersion(self):
        return 11

    def isValidAuthenticationMethod(self, usageType, configurationAttributes):
        return True

    def getAlternativeAuthenticationMethod(self, usageType, configurationAttributes):
        return None

    def authenticate(self, configurationAttributes, requestParameters, step):
        print "EmailOTP. Authenticate for step %i" % step
        identity = CdiUtil.bean(Identity)

        authenticationService = CdiUtil.bean(AuthenticationService)
        user = authenticationService.getAuthenticatedUser()

        if step == 1:
            
            if user == None:
                credentials = identity.getCredentials()
                user_name = credentials.getUsername()
                user_password = credentials.getPassword()
    
                if StringHelper.isNotEmptyString(user_name) and StringHelper.isNotEmptyString(user_password):
                    authenticationService.authenticate(user_name, user_password)
                    user = authenticationService.getAuthenticatedUser()

            if user != None:
                emailsSet = self.registeredEmails(user)
                nEmails = emailsSet.size()

                identity.setWorkingParameter("n-emails", str(nEmails))
                identity.setWorkingParameter("casa_contextPath", "/casa")  #needed only if running this script standalone
                
                if nEmails == 0:
                    self.setError("Account has no e-mails registered")
                    return False

                meh = emailsSet.toString()
                meh = meh[1 : len(meh) - 1]      # discard opening and ending brackets
            
                if nEmails == 1:
                    #send message to the only registered email
                    return self.sendMessage(meh, identity)
                
                identity.setWorkingParameter("emails", meh)

                return True

        elif step == 2:
            recipient = ServerUtil.getFirstValue(requestParameters, "sendTo")
            
            if recipient == None:       # it means this is a 2 step flow variant
                return self.validateCode(requestParameters, identity)
            
            # Send message to the selected e-mail and continue the flow
            return self.sendMessage(recipient, identity)
            
        elif step == 3:
            return self.validateCode(requestParameters, identity)
            
        return False
        
    def prepareForStep(self, configurationAttributes, requestParameters, step):
        #print "EmailOTP. Prepare for step %i" % step
        return True

    def getExtraParametersForStep(self, configurationAttributes, step):
        return Arrays.asList("n-emails", "emails", "otpSent", "iat", "casa_contextPath")

    def getCountAuthenticationSteps(self, configurationAttributes):
        print "EmailOTP. getCountAuthenticationSteps"
        n = CdiUtil.bean(Identity).getWorkingParameter("n-emails")
        
        steps = min(3, int(n) + 1)
        print "EmailOTP. Flow will have %i steps (e-mails found: %s)" % (steps, n)
        return steps

    def getNextStep(self, configurationAttributes, requestParameters, step):
        return -1

    def getPageForStep(self, configurationAttributes, step):
        print "EmailOTP. getPageForStep %i" % step
        
        if step == 1:
            return ""
        elif step == 2:
            n = CdiUtil.bean(Identity).getWorkingParameter("n-emails")
            # when n equals zero, the flow has only 1 step (check getCountAuthenticationSteps)
            return "/casa/otp_email.xhtml" if n == "1" else "/casa/otp_email_prompt.xhtml"
        elif step == 3:
            return "/casa/otp_email.xhtml"

    def logout(self, configurationAttributes, requestParameters):
        return True

    def getAuthenticationMethodClaims(self, requestParameters):
        return None

    def getLogoutExternalUrl(self, configurationAttributes, requestParameters):
        return None

    def registeredEmails(self, user):
        print "EmailOTP. Retrieving e-mails for user %s" % user.getUserId()
        data = []
        
        # oxEmailAlternate was a pretty bad idea
        #try:
        #    emailIds = user.getAttribute("oxEmailAlternate")
        #    emailIds = json.loads(emailIds)["email-ids"]
        #    
        #    for emailId in emailIds:
        #        if "email" in emailId:
        #            data.append(emailId["email"])
        #except:
        #    print sys.exc_info()[1]
        #    print "EmailOTP. Unable to parse oxEmailAlternate attribute"

        print "EmailOTP. Inspecting mail attribute..." 
        mails = user.getAttributeValues("mail")
        if mails != None:
            for mail in mails:
                data.append(mail)
            
        print "EmailOTP. e-mail addresses found: %i" % len(data)
        return HashSet(data)


    def sendMessage(self, address, identity):

        digits = self.RAND.ints(self.otpLength, 0, 10).toArray()
        otp = ""
        for d in digits:
            otp += str(d)
        
        subject = "Here is your passcode for authentication"
        body = "%s is your one-time passcode (OTP) to get access. This code will expire in %i minutes approximately" % (otp, self.otpLifetime)
        try:
            self.sender.sendEmail(address, subject, body)                
            identity.setWorkingParameter("otpSent", str(otp))
            identity.setWorkingParameter("iat", str(System.currentTimeMillis()))
            
            print "EmailOTP. passcode %s sent to user inbox" % otp
            return True
        except:
            print sys.exc_info()[1]
            self.setError("An error occurred: unable to send e-mail")
            return False

        
    def validateCode(self, requestParameters, identity):
        
        # email was sent, verify the code entered
        code = ServerUtil.getFirstValue(requestParameters, "OtpEmailLoginForm:passcode")
        codeSent = identity.getWorkingParameter("otpSent")
        
        match = code.strip() == codeSent
        print "EmailOTP. Codes match? %s" % match
        
        if match:
            iat = identity.getWorkingParameter("iat")
            print "EmailOTP. Passcode was issued at %s" % iat
            
            if Long.valueOf(iat) + self.otpLifetime*60000 < System.currentTimeMillis():
                print "EmailOTP. Passcode already expired"

                self.setError("Passcode has expired. Try again later")
                match = False
        else:
            self.setError("Wrong code entered")
            
        return match

            
    def setError(self, msg):
        facesMessages = CdiUtil.bean(FacesMessages)
        facesMessages.setKeepMessages()
        facesMessages.add(FacesMessage.SEVERITY_ERROR, msg)
        
    # Added for Casa compliance

    def hasEnrollments(self, configurationAttributes, user):
        return not self.registeredEmails(user).isEmpty()

# Mail delivery utilities
class EmailSender():
    
    def __init__(self, smtpConfiguration):
        eses = CdiUtil.bean(EncryptionService)
        smtpConfiguration.setPasswordDecrypted(eses.decrypt(smtpConfiguration.getPassword()))
        smtpConfiguration.setKeyStorePasswordDecrypted(eses.decrypt(smtpConfiguration.getKeyStorePassword()))
        self.smtpConf = smtpConfiguration
        self.time_out = 5000

        # what's mc for?
        mc = CommandMap.getDefaultCommandMap()

        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822")

    def sendEmail(self, user_email, message_subject, message_text):
        print "EmailSender.  - sendEmail"

        smtp_config = self.smtpConf
        host = smtp_config.getHost()
        port = str(smtp_config.getPort())
        time_out = str(self.time_out)
        smtp_connect_protect = smtp_config.getConnectProtection()

        properties = Properties()
        properties.put("mail.from", "Gluu Casa")
        properties.put("mail.smtp.host", host)
        properties.put("mail.smtp.port", port)
        properties.put("mail.smtp.connectiontimeout", time_out)
        properties.put("mail.smtp.timeout", time_out)

        if smtp_connect_protect == SmtpConnectProtectionType.NONE:
            properties.put("mail.transport.protocol", "smtp")            
        
        elif smtp_connect_protect == SmtpConnectProtectionType.START_TLS:
            properties.put("mail.transport.protocol", "smtp")
            properties.put("mail.smtp.starttls.enable", "true")
            properties.put("mail.smtp.starttls.required", "true")

            properties.put("mail.smtp.socketFactory.class", "com.sun.mail.util.MailSSLSocketFactory")
            properties.put("mail.smtp.socketFactory.port", port)

            if smtp_config.isServerTrust():
                properties.put("mail.smtp.ssl.trust", host)

        elif smtp_connect_protect == SmtpConnectProtectionType.SSL_TLS:
            properties.put("mail.transport.protocol.rfc822", "smtps")
            properties.put("mail.smtp.ssl.enable", "true")

            properties.put("mail.smtp.socketFactory.class", "com.sun.mail.util.MailSSLSocketFactory")
            properties.put("mail.smtp.socketFactory.port", port)

            if smtp_config.isServerTrust():
                properties.put("mail.smtp.ssl.trust", host)

        session = Session.getDefaultInstance(properties)

        message = MimeMessage(session)
        message.setFrom(InternetAddress(smtp_config.getFromEmailAddress(), smtp_config.getFromName()))
        message.addRecipient(Message.RecipientType.TO, InternetAddress(user_email))
        message.setSubject(message_subject)
        message.setContent(message_text, "text/html")

        jks_keystore = smtp_config.getKeyStore()
        signed = StringHelper.isNotEmptyString(jks_keystore)
        
        if signed:
            keystore_password = smtp_config.getKeyStorePasswordDecrypted()
            alias = smtp_config.getKeyStoreAlias()
            sign_alg = smtp_config.getSigningAlgorithm()
    
            message = self.signMessage(jks_keystore, keystore_password, alias, sign_alg, message)

        if smtp_connect_protect == SmtpConnectProtectionType.SSL_TLS:
            transport = session.getTransport("smtps")
        else:
            transport = session.getTransport("smtp")

        transport.connect(host, int(port), smtp_config.getUserName(), smtp_config.getPasswordDecrypted())
        transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))

        transport.close()

        print "EmailSender.  - sendEmail - Successful"
        
        
    def signMessage(self, jks_keystore, keystore_password, alias, signing_algorithm, message):
        print "EmailSender.  - signMessage"

        isAliasWithPrivateKey = False

        keystore_ext = self.getExtension(jks_keystore)

        print "EmailSender.  - signMessage - keystore_ext = %s" % keystore_ext

        if keystore_ext.lower() == ".jks":
            keyStore = KeyStore.getInstance("JKS", SecurityProviderUtility.getBCProvider())

        elif keystore_ext.lower() == ".pkcs12":
            keyStore = KeyStore.getInstance("PKCS12", SecurityProviderUtility.getBCProvider())

        elif keystore_ext.lower() == ".bcfks":
            keyStore = KeyStore.getInstance("BCFKS", SecurityProviderUtility.getBCProvider())

        fis = FileInputStream(jks_keystore)
        keyStore.load(fis, list(keystore_password))
        es = keyStore.aliases()

        while (es.hasMoreElements()):
            alias = es.nextElement()
            if (keyStore.isKeyEntry(alias)):
                isAliasWithPrivateKey = True
                break

        if (isAliasWithPrivateKey):
            pkEntry = keyStore.getEntry(alias,KeyStore.PasswordProtection(list(keystore_password)))
            privateKey = pkEntry.getPrivateKey()

        chain = keyStore.getCertificateChain(alias)

        publicKey = chain[0]

        certificate = keyStore.getCertificate(alias)

        fis.close()
        
        sign_algorithm = None

        if not signing_algorithm or not signing_algorithm.strip():
            sign_algorithm = certificate.getSigAlgName()
        else:
            sign_algorithm = signing_algorithm

        # Create the SMIMESignedGenerator
        capabilities = SMIMECapabilityVector()
        capabilities.addCapability(SMIMECapability.dES_EDE3_CBC)
        capabilities.addCapability(SMIMECapability.rC2_CBC, 128)
        capabilities.addCapability(SMIMECapability.dES_CBC)
        capabilities.addCapability(SMIMECapability.aES256_CBC)

        attributes = ASN1EncodableVector()
        attributes.add(SMIMECapabilitiesAttribute(capabilities))

        SMIMEUtil.createIssuerAndSerialNumberFor(certificate)

        issAndSer = IssuerAndSerialNumber(X500Name(publicKey.getIssuerDN().getName()),publicKey.getSerialNumber())        

        attributes.add(SMIMEEncryptionKeyPreferenceAttribute(issAndSer))

        signer = SMIMESignedGenerator()

        signer.addSignerInfoGenerator(JcaSimpleSignerInfoGeneratorBuilder().setProvider(SecurityProviderUtility.getBCProvider()).setSignedAttributeGenerator(AttributeTable(attributes)).build(sign_algorithm, privateKey, publicKey))

        # Add the list of certs to the generator
        bcerts = JcaCertStore(Arrays.asList(chain))
        signer.addCertificates(bcerts)

        # Sign the message
        mm = signer.generate(message)

        # Set the content of the signed message
        message.setContent(mm, mm.getContentType())
        message.saveChanges()

        print "EmailSender.  - signMessage - Successful"
        return message

    def getExtension(self, file_path):
        file_name_with_ext = os.path.basename(file_path)
        file_name, ext = os.path.splitext(file_name_with_ext)
        return ext
