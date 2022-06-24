from org.gluu.oxauth.service import AuthenticationService
from org.gluu.oxauth.service import UserService
from org.gluu.oxauth.auth import Authenticator
from org.gluu.oxauth.security import Identity
from org.gluu.model.custom.script.type.auth import PersonAuthenticationType
from org.gluu.model import SmtpConnectProtectionType

from org.gluu.service.cdi.util import CdiUtil
from org.gluu.service import CacheService
from org.gluu.util import StringHelper
from org.gluu.oxauth.util import ServerUtil
from org.gluu.oxauth.service.common import ConfigurationService
from org.gluu.oxauth.service.common import EncryptionService
from org.gluu.jsf2.message import FacesMessages
from org.gluu.model.casa import ApplicationConfiguration
from org.gluu.persist.exception import AuthenticationException
from org.gluu.persist import PersistenceEntryManager

from javax.faces.application import FacesMessage
from datetime import datetime, timedelta
from java.util import GregorianCalendar, TimeZone
from java.io import File
from java.io import FileInputStream
from java.util import Enumeration, Properties

from java.security import Security
from java.security import KeyStore

from javax.mail.internet import MimeMessage, InternetAddress
from javax.mail import Session, Message, Transport

from java.util import Arrays

from org.gluu.service import MailService

from javax.activation import CommandMap

from org.bouncycastle.asn1 import ASN1EncodableVector
from org.bouncycastle.asn1.cms import AttributeTable
from org.bouncycastle.asn1.cms import IssuerAndSerialNumber
from org.bouncycastle.asn1.smime import SMIMECapabilitiesAttribute
from org.bouncycastle.asn1.smime import SMIMECapability
from org.bouncycastle.asn1.smime import SMIMECapabilityVector
from org.bouncycastle.asn1.smime import SMIMEEncryptionKeyPreferenceAttribute
from org.bouncycastle.asn1.x509 import X509Name
from org.bouncycastle.cert.jcajce import JcaCertStore
from org.bouncycastle.cms import CMSAlgorithm
from org.bouncycastle.cms.jcajce import JcaSimpleSignerInfoGeneratorBuilder
from org.bouncycastle.cms.jcajce import JceCMSContentEncryptorBuilder
from org.bouncycastle.cms.jcajce import JceKeyTransRecipientInfoGenerator
from org.bouncycastle.jce.provider import BouncyCastleProvider
from org.bouncycastle.mail.smime import SMIMEEnvelopedGenerator
from org.bouncycastle.mail.smime import SMIMESignedGenerator

import random
import string
import re
import urllib
import java
try:
    import json
except ImportError:
    import simplejson as json

class EmailValidator():
    regex = '^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$'

    def check(self, email):

        if(re.search(self.regex,email)):
            print "EmailOTP.  - %s is a valid email format" % email
            return True
        else:
            print "EmailOTP.  - %s is an invalid email format" % email
            return False

class Token:
    #class that deals with string token

    def generateToken(self,lent):
        rand1="1234567890123456789123456789"
        rand2="9876543210123456789123456789"
        first = int(rand1[:int(lent)])
        first1 = int(rand2[:int(lent)])
        token = random.randint(first, first1)
        return token

class PersonAuthentication(PersonAuthenticationType):

    def __init__(self, currentTimeMillis):
        self.currentTimeMillis = currentTimeMillis
        self.jks_keystore = None
        self.keystore_password = None
        self.alias = None

    def init(self, customScript, configurationAttributes):
        print "EmailOTP.  - Initialization"

        #### Email Signing Code Begin ####
        
        jks_keystore_val = configurationAttributes.get("Signer_Cert_KeyStore")
        keystore_password_val = configurationAttributes.get("Signer_Cert_KeyStorePassword")
        alias_val = configurationAttributes.get("Signer_Cert_Alias")
        
        if jks_keystore_val != None:
            self.jks_keystore = jks_keystore_val.getValue2()
            
        if keystore_password_val != None:
            self.keystore_password = keystore_password_val.getValue2()

        if alias_val != None:
            self.alias = alias_val.getValue2()

        print "EmailOTP.  - Initialization - Initialized successfully"
        return True

    def destroy(self, configurationAttributes):
        print "EmailOTP.  - Destroyed successfully"
        return True

    def getApiVersion(self):
        return 11

    def isValidAuthenticationMethod(self, usageType, configurationAttributes):
        return True
    def getAuthenticationMethodClaims(self, configurationAttributes):
        return None

    def getAlternativeAuthenticationMethod(self, usageType, configurationAttributes):
        return None

    def authenticate(self, configurationAttributes, requestParameters, step):
        print "EmailOTP. - Authenticate for step %s" % ( step)
        authenticationService = CdiUtil.bean(AuthenticationService)
        identity = CdiUtil.bean(Identity)
        credentials = identity.getCredentials()

        user_name = credentials.getUsername()
        print "Email 2FA - Authenticate: user_name = {}".format(user_name)

        user_password = credentials.getPassword()
        print "Email 2FA - Authenticate: user_password = {}".format(user_password)
        
        facesMessages = CdiUtil.bean(FacesMessages)
        facesMessages.setKeepMessages()

        subject = "Gluu Authentication Token"

        session_attributes = identity.getSessionId().getSessionAttributes()

        multipleEmails = session_attributes.get("emailIds")

        if step == 1:
            try:
                 # Check if user authenticated already in another custom script
                user2 = authenticationService.getAuthenticatedUser()
                print "EmailOTP. - Authenticate: user2 = {}".format(user2)
                
                if user2 == None:
                    credentials = identity.getCredentials()
                    user_name = credentials.getUsername()
                    user_password = credentials.getPassword()

                    logged_in = False
                    if (StringHelper.isNotEmptyString(user_name) and StringHelper.isNotEmptyString(user_password)):
                        userService = CdiUtil.bean(UserService)
                        logged_in = authenticationService.authenticate(user_name, user_password)
                        if logged_in is True:
                            user2 = authenticationService.getAuthenticatedUser()
                            emailIds = user2.getAttribute("oxEmailAlternate")
                            if StringHelper.isNotEmptyString(emailIds):
                                data = json.loads(emailIds)
                                if len(data['email-ids']) > 1:
                                    commaSeperatedEmailString = []
                                    for email in data['email-ids']:
                                        reciever_id = email['email']
                                        commaSeperatedEmailString.append(reciever_id)
                                    # setting this in session is used to determine if this is a 2 or 3 step flow
                                    identity.setWorkingParameter("emailIds", ",".join(commaSeperatedEmailString))

                    return logged_in
            except AuthenticationException as err:
                print err
                return False
        else:
            #Means the selection email page was used
            user2 = authenticationService.getAuthenticatedUser()
            emailIds = user2.getAttribute("oxEmailAlternate")
            if emailIds != None:
                multipleEmails = []
                token = identity.getWorkingParameter("token")

                if StringHelper.isNotEmptyString(emailIds):
                    data = json.loads(emailIds)

                    # step2 and multiple email ids present, then user has been presented a choice of email which is fetched in OtpEmailLoginForm:indexOfEmail, send email
                    if step == 2 and len(data['email-ids']) > 1 :

                        for email in data['email-ids']:
                            reciever_id = email['email']
                            multipleEmails.append(reciever_id)

                        idx = ServerUtil.getFirstValue(requestParameters, "OtpEmailLoginForm:indexOfEmail")
                        if idx != None and token != None:
                            sendToEmail = multipleEmails[int(idx)]
                            print "EmailOtp. Sending email to : %s " % sendToEmail
                            email_validator = EmailValidator()
                            email_valid = email_validator.check(sendToEmail)
                            if email_valid == True:
                                body = "Here is your token: %s" % token
                                sender = EmailSender(self.jks_keystore, self.keystore_password, self.alias)
                                sender.sendEmail(sendToEmail, subject, body)
                                return True
                            else:
                                return False
                        else:
                            print "EmailOTP. Something wrong with index or token"
                            return False
                    # token verification - step 3 incase of email selection , else step 2
                    else:
                        input_token = ServerUtil.getFirstValue(requestParameters, "OtpEmailLoginForm:passcode").strip()
                        print "input token %s" % input_token
                        print "EmailOTP.  - Token input by user is %s" % input_token

                        token = str(identity.getWorkingParameter("token"))
                        min11 = int(identity.getWorkingParameter("sentmin"))
                        nww = datetime.now()
                        te = str(nww)
                        listew = te.split(':')
                        curtime = int(listew[1])

                        token_lifetime = int(configurationAttributes.get("token_lifetime").getValue2())
                        if ((min11<= 60) and (min11>= 50)):
                            if ((curtime>=50) and (curtime<=60)):
                                timediff1 =  curtime -  min11
                                if timediff1>token_lifetime:
                                    print "OTP Expired"
                                    facesMessages.add(FacesMessage.SEVERITY_ERROR, "OTP Expired")
                                    return False
                            elif ((curtime>=0) or (curtime<=10)):
                                timediff1 = 60 - min11
                                timediff1 =  timediff1 + curtime
                                if timediff1>token_lifetime:
                                    print "OTP Expired"
                                    facesMessages.add(FacesMessage.SEVERITY_ERROR, "OTP Expired")
                                    return False

                        if ((min11>=0) and (min11<=60) and (curtime>=0) and (curtime<=60)):
                            timediff2 = curtime - min11
                            if timediff2>token_lifetime:
                                print "OTP Expired"
                                facesMessages.add(FacesMessage.SEVERITY_ERROR, "OTP Expired")
                                return False
                        # compares token sent and token entered by user
                        print "Token from session: %s " % token
                        if input_token == token:
                            print "Email 2FA - token entered correctly"
                            identity.setWorkingParameter("token_valid", True)

                            return True

                        else:
                            facesMessages = CdiUtil.bean(FacesMessages)
                            facesMessages.setKeepMessages()
                            facesMessages.clear()
                            facesMessages.add(FacesMessage.SEVERITY_ERROR, "Wrong code entered")
                            print "EmailOTP. Wrong code entered"
                            return False

    def prepareForStep(self, configurationAttributes, requestParameters, step):
        print "EmailOTP.  - Preparing for step %s" % step
        authenticationService = CdiUtil.bean(AuthenticationService)

        user2 = authenticationService.getAuthenticatedUser()
        
        identity = CdiUtil.bean(Identity)
        
        if step == 1:
            self.prepareUIParams(identity)
            return True
            
        elif step == 2 and user2 is not None:
            uid = user2.getAttribute("uid")
            identity = CdiUtil.bean(Identity)
            self.prepareUIParams(identity)
            
            lent = configurationAttributes.get("token_length").getValue2()
            new_token = Token()
            token = new_token.generateToken(lent)

            otptime1 = datetime.now()
            tess = str(otptime1)
            listee = tess.split(':')

            identity.setWorkingParameter("sentmin", listee[1])
            identity.setWorkingParameter("token", token)

            return True

        else: 
            self.prepareUIParams(identity)
            return True

    def getExtraParametersForStep(self, configurationAttributes, step):
        return Arrays.asList("token","emailIds","token_valid","sentmin")

    def getCountAuthenticationSteps(self, configurationAttributes):

        print "EmailOTP. getCountAuthenticationSteps called"

        if CdiUtil.bean(Identity).getWorkingParameter("emailIds") == None:
            print "EmailOTP. getCountAuthenticationSteps called - 2 steps"
            return 2
        else:
            print "EmailOTP. getCountAuthenticationSteps called - 3 steps"
            return 3

    def getPageForStep(self, configurationAttributes, step):
        print "EmailOTP. getPageForStep called %s" % step

        defPage = "/casa/otp_email.xhtml"
        if step == 2:
            if CdiUtil.bean(Identity).getWorkingParameter("emailIds") == None:
                print "emailIds not set, returning otp_email page"
                return defPage
            else:
                return "/casa/otp_email_prompt.xhtml"
        elif step == 3:
            return defPage
        return ""

    def getNextStep(self, configurationAttributes, requestParameters, step):
        return -1

    def logout(self, configurationAttributes, requestParameters):
        return True

    def hasEnrollments(self, configurationAttributes, user):
        values = user.getAttributeValues("oxEmailAlternate")
        if values != None:
            return True
        else:
            return False
            
    def prepareUIParams(self, identity):
        print "EmailOTP. prepareUIParams. Reading UI branding params"
        cacheService = CdiUtil.bean(CacheService)
        email2FaAssets = cacheService.get("email_2fa_assets")

        if email2FaAssets == None:
            #This may happen when cache type is IN_MEMORY, where actual cache is merely a local variable
            #(a expiring map) living inside Casa webapp, not oxAuth webapp

            sets = self.getSettings()

            custPrefix = "/custom"
            logoUrl = "/images/logo.png"
            faviconUrl = "/images/favicon.ico"
            if ("extra_css" in sets and sets["extra_css"] != None) or sets["use_branding"]:
                logoUrl = custPrefix + logoUrl
                faviconUrl = custPrefix + faviconUrl

            prefix = custPrefix if sets["use_branding"] else ""

            email2FaAssets = {
                "contextPath": "/casa",
                "prefix" : prefix,
                "faviconUrl" : faviconUrl,
                "extraCss": sets["extra_css"] if "extra_css" in sets else None,
                "logoUrl": logoUrl
            }

        #Setting a single variable with the whole map does not work...
        identity.setWorkingParameter("casa_contextPath", email2FaAssets['contextPath'])
        identity.setWorkingParameter("casa_prefix", email2FaAssets['prefix'])
        identity.setWorkingParameter("casa_faviconUrl", email2FaAssets['contextPath'] + email2FaAssets['faviconUrl'])
        identity.setWorkingParameter("casa_extraCss", email2FaAssets['extraCss'])
        identity.setWorkingParameter("casa_logoUrl", email2FaAssets['contextPath'] + email2FaAssets['logoUrl'])
        
    def getSettings(self):
        entryManager = CdiUtil.bean(PersistenceEntryManager)
        config = ApplicationConfiguration()
        config = entryManager.find(config.getClass(), "ou=casa,ou=configuration,o=gluu")
        settings = None
        try:
            settings = json.loads(config.getSettings())
        except:
            print "Casa. getSettings. Failed to parse casa settings from DB"
        return settings

class EmailSender():
    #class that sends e-mail through smtp

    time_out = 5000

    def __init__(self, jks_keystore, keystore_password, alias):
        self.jks_keystore = jks_keystore
        self.keystore_password = keystore_password
        self.alias = alias

    def getSmtpConfig(self):
        print "EmailSender.  - getSmtpConfig"

        smtp_config = None
        smtpconfig = CdiUtil.bean(ConfigurationService).getConfiguration().getSmtpConfiguration()

        if smtpconfig is None:
            print "EmailSender.  - getSmtpConfig - SMTP CONFIG DOESN'T EXIST - Please configure"

        else:
            encryption_service = CdiUtil.bean(EncryptionService)
            smtp_config = {
                'host' : smtpconfig.getHost(),
                'port' : smtpconfig.getPort(),
                'user' : smtpconfig.getUserName(),
                'from' : smtpconfig.getFromEmailAddress(),
                'from_name' : smtpconfig.getFromName(),
                'pwd_decrypted' : encryption_service.decrypt(smtpconfig.getPassword()),
                'connect_protection' : smtpconfig.getConnectProtection(),
                'requires_authentication' : smtpconfig.isRequiresAuthentication(),
                'server_trust' : smtpconfig.isServerTrust(),
                
                'key_store' : smtpconfig.getKeyStore(),
                'key_store_password' : smtpconfig.getKeyStorePassword(),
                'key_store_alias' : smtpconfig.getKeyStoreAlias()
            }

        print "EmailSender.  - getSmtpConfig - Successfully"
        return smtp_config
        
    def signMessage(self, jks_keystore, keystore_password, alias, message):
        print "EmailSender.  - signMessage"

        isAliasWithPrivateKey = False

        keyStore = KeyStore.getInstance("JKS")
        file = File(jks_keystore)
        keyStore.load(FileInputStream(file), list(keystore_password))
        es = keyStore.aliases()

        while (es.hasMoreElements()):
            alias = es.nextElement()
            if (keyStore.isKeyEntry(alias)):
                isAliasWithPrivateKey = True
                break

        if (isAliasWithPrivateKey):
            pkEntry = keyStore.getEntry(alias,KeyStore.PasswordProtection(list(keystore_password)))
            myPrivateKey = pkEntry.getPrivateKey()
            
            chain = keyStore.getCertificateChain(alias)
            privateKey = myPrivateKey
            publicKey = chain[0]

        # Create the SMIMESignedGenerator
        capabilities = SMIMECapabilityVector()
        capabilities.addCapability(SMIMECapability.dES_EDE3_CBC)
        capabilities.addCapability(SMIMECapability.rC2_CBC, 128)
        capabilities.addCapability(SMIMECapability.dES_CBC)
        capabilities.addCapability(SMIMECapability.aES256_CBC)

        attributes = ASN1EncodableVector()
        attributes.add(SMIMECapabilitiesAttribute(capabilities))

        issAndSer = IssuerAndSerialNumber(X509Name(publicKey.getIssuerDN().getName()),publicKey.getSerialNumber())
        attributes.add(SMIMEEncryptionKeyPreferenceAttribute(issAndSer))
	
        signer = SMIMESignedGenerator()
	
        signer.addSignerInfoGenerator(JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").setSignedAttributeGenerator(AttributeTable(attributes)).build("SHA256withECDSA", privateKey, publicKey))

        # Add the list of certs to the generator
        certList = [publicKey]
	
        bcerts = JcaCertStore(certList)
        signer.addCertificates(bcerts)
	
        # Sign the message
        mm = signer.generate(message)

        # Set the content of the signed message
        message.setContent(mm, mm.getContentType())
        message.saveChanges()

        print "EmailSender.  - signMessage - Successfully"
        return message

    def sendEmail(self, user_email, message_subject, message_text):
        print "EmailSender.  - sendEmail"
        
        mc = CommandMap.getDefaultCommandMap()

        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822")

        # server connection
        smtp_config = self.getSmtpConfig()

        properties = Properties()

        properties.setProperty("mail.smtp.auth", str(True))
        properties.setProperty("mail.smtp.host", smtp_config['host'])
        properties.setProperty("mail.smtp.port", str(smtp_config['port']))
        properties.setProperty("mail.from", smtp_config['from'])

        properties.setProperty("mail.smtp.connectiontimeout", str(self.time_out))
        properties.setProperty("mail.smtp.timeout", str(self.time_out))
        properties.setProperty("mail.transport.protocol", "smtp")

        smtp_connect_protect = smtp_config['connect_protection']

        if smtp_connect_protect == SmtpConnectProtectionType.SslTls:
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            properties.setProperty("mail.smtp.socketFactory.port", str(smtp_config['port']))
            properties.setProperty("mail.smtp.ssl.trust", smtp_config['host'])
            properties.setProperty("mail.smtp.starttls.enable", str(True))

        elif smtp_connect_protect == SmtpConnectProtectionType.StartTls:
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            properties.setProperty("mail.smtp.socketFactory.port", str(smtp_config['port']))
            properties.setProperty("mail.smtp.ssl.trust", smtp_config['host'])
            properties.setProperty("mail.smtp.ssl.enable", str(True))

        session = Session.getDefaultInstance(properties)

        message = MimeMessage(session)
        message.setFrom(InternetAddress(smtp_config['from'], smtp_config['from_name']))
        message.addRecipient(Message.RecipientType.TO,InternetAddress(user_email))
        message.setSubject(message_subject)
        message.setContent(message_text, "text/html")

        if self.jks_keystore != None:
            jks_keystore = self.jks_keystore
        else:
            jks_keystore = smtp_config['key_store']

        if self.keystore_password != None:
            keystore_password = self.keystore_password
        else:
            keystore_password = smtp_config['key_store_password']

        if self.alias != None:
            alias = self.alias
        else:
            alias = smtp_config['key_store_alias']

        signed_message = self.signMessage(jks_keystore, keystore_password, alias, message)

        transport = session.getTransport("smtp")
        transport.connect(properties.get("mail.smtp.host"),int(properties.get("mail.smtp.port")), smtp_config['user'], smtp_config['pwd_decrypted'])
        transport.sendMessage(signed_message, signed_message.getRecipients(Message.RecipientType.TO))

        transport.close()

        print "EmailSender.  - sendEmail - Successfully"
