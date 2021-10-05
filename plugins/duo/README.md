# Duo plugin for enrolling and authenticating DUO as a 2fa method 

Steps to run the plugin -
1. Enable "duo" custom script in oxTrust
2. In the custom script for duo - add a new property 2fa_requisite = true
3. Log in to casa, in casa admin console, go to "Enabled authentication methods" from the menu. Select "duo" as a 2fa method for authentication.
4. Add the plugin jar file from the admin console
5. Notice the newly created menu that reads "DUO credentials" in the menu bar
