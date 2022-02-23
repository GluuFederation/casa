Incase you are installing casa plugin for EMail OTP on Gluu Server version under 4.3.1, follow the steps below:

1. Copy this to the script on oxTrust - `https://github.com/GluuFederation/casa/blob/master/plugins/emailotp/extras/Email2FAExternalAuthenticatorWithEmailSelection.py`
2. under `/opt/gluu/jetty/oxauth/custom/pages/casa/` copy 2 files - `https://github.com/GluuFederation/casa/blob/master/plugins/emailotp/extras/otp_email.xhtml` and `https://github.com/GluuFederation/casa/blob/master/plugins/emailotp/extras/otp_email_prompt.xhtml`
3. under `/opt/gluu/jetty/oxauth/custom/i18n/` create a file `oxauth.properties` with the following content
   ```
#casa plugin - email otp
casa.email_2fa.title= Email OTP
casa.email_2fa.text=The Email OTP method enables you to authenticate using the one-time password (OTP) that is sent to the registered email address.
casa.email.enter=Enter the code sent via Email
casa.email.choose=Choose an email-id to send an OTP to:
casa.email.send=Send
```
4. under `under `/opt/gluu/jetty/oxauth/custom/pages/casa/` copy the latest casa.xhtml (https://github.com/GluuFederation/oxAuth/blob/master/Server/src/main/webapp/casa/casa.xhtml) containing the following entry `"email_2fa" : '<i class="fas fa-envelope" />'`

5. copy this image file `https://github.com/GluuFederation/oxAuth/blob/master/Server/src/main/webapp/img/email-ver.png` to the location  `/opt/gluu/jetty/oxauth/custom/static/img`