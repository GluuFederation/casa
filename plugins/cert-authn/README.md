# Certificate Authentication Plugin
### Plugin allows enrollment and authentication via client certificates

**Note**:
This is a backport of the 4.2 cert-authn plugin by [Ikunalv](https://github.com/Ikunalv).

Steps:

- Enable "cert" custom script in oxTrust.
- Log in to casa, in casa admin console, go to "Enabled authentication methods" from the menu. Select "cert" as a 2fa method for authentication.
- Add the plugin jar file from the admin console
- Notice the newly created menu that reads "User Certificates" in the menu bar.