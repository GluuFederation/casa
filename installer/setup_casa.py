#!/usr/bin/python

import os.path
import json
import traceback
import Properties
import sys
import socket
import getopt
import base64
import pyDes
from setup import *

class SetupCasa(object):

    def __init__(self,setup):
        
        self.setup = setup
        self.install_dir = self.setup.install_dir

        self.log = '%s/setup_casa.log' % self.install_dir
        self.logError = '%s/setup_casa_error.log' % self.install_dir

        self.setup_properties_fn = '%s/setup_casa.properties' % self.install_dir
        self.savedProperties = '%s/setup_casa.properties.last' % self.install_dir
        
        # Change this to final version
        self.casa_war = 'https://ox.gluu.org/maven/org/xdi/casa/3.1.7.Final/casa-3.1.7.Final.war'

        self.twilio_jar = 'twilio-7.17.0.jar'
        self.twilio_url = 'http://central.maven.org/maven2/com/twilio/sdk/twilio/7.17.0/%s' % self.twilio_jar

        self.application_max_ram = None  # in MB

        # Gluu components installation status
        self.installCasa = False
        self.install_oxd = "n"
        self.oxd_server_https = ""
        self.oxd_conf = '/etc/oxd/oxd-server/oxd-conf.json'
        self.distFolder = '/opt/dist'
        self.locality = "0"
        self.casa = '/etc/gluu/conf/casa.json'
        self.detectedHostname = self.detectedHostName()
        self.jetty_app_configuration = {
            'casa': {'name': 'casa',
                                'jetty': {'modules': 'server,deploy,resources,http,http-forwarded,console-capture,jsp'},
                                'memory': {'ratio': 1, "jvm_heap_ration": 0.7, "max_allowed_mb": 1024},
                                'installed': False
                                }
        }

        self.oxd_hostname = None
        self.oxd_port = 8099

        self.ldif_scripts_casa = '%s/scripts_casa.ldif' % self.setup.outputFolder

        self.casa_config = '%s/casa.json' % self.setup.outputFolder
    
    def __repr__(self):

        try:
            return 'Install Gluu Casa '.ljust(30) + repr(self.installCasa).rjust(35) + "\n"
        except:
            s = ""
            for key in self.__dict__.keys():
                val = self.__dict__[key]
                s = s + "%s\n%s\n%s\n\n" % (key, "-" * len(key), val)
            return s

    def check_properties(self):

        self.setup.logIt('Checking properties')
        if not self.setup.application_max_ram:
            self.setup.application_max_ram = 1024

    def propertiesForOxd(self):

        conf = "\n"
        if self.install_oxd == "y":
                conf +=  'oxd host'.ljust(30) + "localhost".rjust(35) + '\n' \
                        + "oxd port".ljust(30) + "8099".rjust(35)
        else:
            if self.locality == "1":
                conf +=  'oxd host'.ljust(30) + "localhost".rjust(35) + '\n' \
                        + "oxd port".ljust(30) + self.oxd_port.rjust(35)
            elif self.locality == "2":
                conf += 'oxd https URL'.ljust(30) + self.oxd_server_https.rjust(35)
        print conf

    def unobscure(self,s=""):

        cipher = pyDes.triple_des( self.key )
        decrypted = cipher.decrypt(base64.b64decode(s), padmode=pyDes.PAD_PKCS5)
        return decrypted

    def check_installed(self):

        if self.setup.check_installed():
            return os.path.exists('%s/casa.json' % self.setup.configFolder)
        else:
            print "\nPlease run './setup.py' to configure Gluu Server first!\n"
            sys.exit()

    def download_files(self):

        self.setup.logIt("Downloading files")
        if self.installCasa:
            # Casa is not part of CE package. We need to download it if needed
            distCasaPath = '%s/%s' % (self.setup.distGluuFolder, "casa.war")
            if not os.path.exists(distCasaPath):
                print "\nDownloading Casa war file...\n"
                self.setup.run(
                    ['/usr/bin/wget', self.casa_war, '--no-verbose', '--retry-connrefused', '--tries=10', '-O', distCasaPath])

            # Download Twilio
            twilioJarPath = '%s/%s' % (self.setup.distGluuFolder, self.twilio_jar)
            if not os.path.exists(twilioJarPath):
                print "Downloading Twilio jar file..."
                self.setup.run(
                    ['/usr/bin/wget', self.twilio_url, '--no-verbose', '--retry-connrefused', '--tries=10', '-O', twilioJarPath])

    def detectedHostName(self):

        detectedHostname = None
        try:
            f = open('/install/community-edition-setup/output/hostname')
            detectedHostname = f.read()
            f.close()
            detectedHostname = detectedHostname.strip()
        except:
            self.setup.logIt("Error detecting host name", True)
            self.setup.logIt(traceback.format_exc(), True)
        return detectedHostname

    def makeDirs(self):

        casa_static_dir = '/opt/gluu/jetty/casa/static/'
        casa_plugins_dir = '/opt/gluu/jetty/casa/plugins'
        directory_paths = [casa_static_dir,casa_plugins_dir]

        for path in directory_paths:
            if not os.path.exists(path):
                self.setup.run(['mkdir', '-p', path])
                self.setup.run(['chown', '-R', 'jetty:jetty', path])

    def promptForProperties(self):

        promptForCasa = self.setup.getPrompt("Install Gluu Casa? (Y/n)", "Y")[0].lower()

        if promptForCasa in ('yes', 'Yes', 'Y', 'y', ''):
            self.installCasa = True

            self.application_max_ram = self.setup.getPrompt("Enter maximum RAM for applications in MB", '1024')

            install_oxd = self.setup.getPrompt(
                "Do you have an existing oxd-server-3.1.4 installation? Note: oxd is used to integrate this product with the Gluu Server OP. (Y/n) ?",
                "n")[0].lower()

            if install_oxd == "y":
                self.promptForOxdServer()

            else:
                self.install_oxd = self.setup.getPrompt("Install oxd-server on this host now?", "Y")[
                    0].lower()
                if self.install_oxd == "n":
                    print "An oxd server instance is required when installing this product via Linux packages"
                    sys.exit(0)

            promptForLicense = self.setup.getPrompt("\nGluu License Agreement: https://github.com/GluuFederation/casa/blob/master/LICENSE.md \nDo you acknowledge that Casa is commercial software, and use of Casa is only permitted under the Gluu License Agreement for Gluu Casa? [Y/n]", "n")[0].lower()
        elif promptForCasa in ('no', 'No', 'N', 'n'):
            self.installCasa = False
            print('Exiting.')
            sys.exit()
        else:
            print('Please input a valid option.\n')
            self.promptForProperties()
        
        if self.installCasa:
            if promptForLicense in ('yes', 'Yes', 'Y', 'y'):
                pass
            else:
                print('You must accept the Gluu License Agreement to continue. Exiting.\n')
                sys.exit()

    def promptForOxdServer(self):

        while self.locality != "1" and self.locality != "2":
            self.locality = self.setup.getPrompt(
                "Will this product use localhost [1] or https [2] to connect to oxd-server. [1|2] ?", "1")

        if self.locality == "1":
            self.oxd_port = self.setup.getPrompt("Enter oxd server port", "8099")
        else:
            self.oxd_server_https = self.setup.getPrompt(
                "Enter the URL + port of your oxd-server (e.g. https://oxd.example.com:8443)").lower()

    def oxd_casa_json_config(self):
        data = ""
        try:
            with open(self.casa) as f:
                for line in f:
                    data += line
        except:
            self.setup.logIt("Error reading casa Template", True)
            self.setup.logIt(traceback.format_exc(), True)

        datastore = json.loads(data)
        if self.oxd_server_https != "":
            
            try:
                url=self.oxd_server_https.replace("https://", "", 1)
                url=url.split(":")

                datastore['oxd_config']['host'] = url[0]
                if len(url)==1:
                    self.setup.logIt("No port in https url (default 443 assumed)", True)
                    datastore['oxd_config']['port'] = 443
                else:
                    datastore['oxd_config']['port'] = int(url[1])

            except:
                self.setup.logIt("Problem parsing https url", True)
                self.setup.logIt(traceback.format_exc(), True)

        else:
            datastore['oxd_config']['host'] = "localhost"
            try:
                datastore['oxd_config']['port'] = int(self.oxd_port)
            except:
                self.setup.logIt("Unparsable port " + self.oxd_port + ". Defaulting to 8099", True)
                datastore['oxd_config']['port'] = 8099

        datastore['oxd_config']['use_https_extension'] = (self.oxd_server_https != "")
        
        try:
            with open(self.casa, 'w') as outfile:
                json.dump(datastore, outfile,indent=4)
        except:
            self.setup.logIt("Error writing Casa Template", True)
            self.setup.logIt(traceback.format_exc(), True)

    def oxd_json_config(self):
        data = ""
        # change here
        self.setup.run(['chmod', '644', '-R', self.oxd_conf])
        try:
            with open(self.oxd_conf) as f:
                for line in f:
                    data += line
        except:
            self.setup.logIt("Error reading oxd Template", True)
            self.setup.logIt(traceback.format_exc(), True)

        datastore = json.loads(data)
        datastore['server_name'] = self.detectedHostname
        try:
            with open(self.oxd_conf, 'w') as outfile:
                json.dump(datastore, outfile,indent=4)
        except:
            self.setup.logIt("Error writting oxd configuration file", True)
            self.setup.logIt(traceback.format_exc(), True)

    def import_ldif_ldap(self):
        self.setup.logIt("Importing LDIF files into LDAP")
        if self.installCasa:
            ldaptype_openldap = True
            ldappassword = ""
            try:
                with open('/etc/gluu/conf/ox-ldap.properties') as f:
                    for line in f:
                        if line.startswith("bindDN:"):
                            if "o=gluu" not in line:
                                ldaptype_openldap = False

                        if not ldaptype_openldap:
                            if line.startswith("bindPassword:"):
                                ldappassword = line.split(":")[1].split("\n")[0].strip()
            except:
                self.setup.logIt("Error reading ox-ldap.properties Template", True)
                self.setup.logIt(traceback.format_exc(), True)

            ldif = self.ldif_scripts_casa
            if not ldaptype_openldap:
                #Importing LDIF files into OpenDJ
                saltFn = "/etc/gluu/conf/salt"
                try:
                    f = open(saltFn)
                    salt_property = f.read()
                    f.close()
                    self.key = salt_property.split("=")[1].strip()
                    self.setup.ldapPass = self.unobscure(ldappassword)
                except Exception as e:
                    self.setup.logIt("Error reading salt template", True)
                    self.setup.logIt(e)
                    self.setup.logIt(traceback.format_exc(), True)

                createPwFile = not os.path.exists(self.setup.ldapPassFn)
                if createPwFile:
                    self.setup.createLdapPw()
                try:
                    #opendj bindn Add here
                    self.setup.ldap_binddn = "cn=directory manager"
                    self.setup.import_ldif_template_opendj(ldif)
                except Exception as e:
                    self.setup.logIt("Error importing LDIF into OpenDj")
                    self.setup.logIt(e)
                    self.setup.logIt(traceback.format_exc(), True)
                finally:
                    self.setup.deleteLdapPw()
            else:
                self.setup.import_ldif_template_openldap(ldif)

    def calculate_applications_memory(self):
        installedComponents = []

        # Jetty apps
        if self.installCasa:
            installedComponents.append(self.jetty_app_configuration['casa'])

        self.setup.calculate_aplications_memory(self.application_max_ram, self.jetty_app_configuration,
                                                installedComponents)

    def install_oxd_server(self):

        print "\nInstalling oxd from package..."
        packageRpm = True
        packageExtension = ".rpm"
        if self.setup.os_type in ['debian', 'ubuntu']:
            packageRpm = False
            packageExtension = ".deb"

        oxdDistFolder = "%s/%s" % (self.distFolder, "oxd")

        if not os.path.exists(oxdDistFolder):
            self.setup.logIt(oxdDistFolder+" Directory is not found")
            print oxdDistFolder+" Directory is not found"
            sys.exit(0)

        packageName = None
        for file in os.listdir(oxdDistFolder):
            if file.endswith(packageExtension):
                packageName = "%s/%s" % ( oxdDistFolder, file )

        #print packageName

        if packageName == None:
            self.setup.logIt('Failed to find oxd package in folder %s !' % oxdDistFolder)
            sys.exit(0)

        self.setup.logIt("Found package '%s' for install" % packageName)

        if not os.path.exists('/etc/oxd/oxd-server/oxd-conf.json'):
            if packageRpm:
                self.setup.run([self.setup.cmd_rpm, '--install', '--verbose', '--hash', packageName])
            else:
                self.setup.run([self.setup.cmd_dpkg, '--install', packageName])

        self.oxd_json_config()

        # Enable service autoload on Gluu-Server startup
        applicationName = 'oxd-server'
        if self.setup.os_type in ['centos', 'fedora', 'red']:
            if self.setup.os_initdaemon == 'systemd':
                self.setup.run(["/usr/bin/systemctl", 'enable', applicationName])
            else:
                self.setup.run(["/sbin/chkconfig", applicationName, "on"])
        elif self.setup.os_type in ['ubuntu', 'debian']:
            self.setup.run(["/usr/sbin/update-rc.d", applicationName, 'defaults', '50', '25'])

        # Start oxd-server
        print "Starting oxd-server..."
        # change start.sh permission
        self.setup.run(['chmod', '+x', '/opt/oxd-server/bin/oxd-start.sh'])
        self.setup.run_service_command(applicationName, 'start')
        
    def checkCryptoLevel(self):

        self.setup.logIt("Checking Crypto Level..")

        # Casa requires the unlimited crypto level in Java. Check to see if JCE is active
        # Else, configure the java.security file as such.

        jceFile = '/opt/jre/jre/lib/security/US_export_policy.jar'
        os.system('unzip -p {0} META-INF/MANIFEST.MF > /tmp/jceManifest'.format(jceFile))
        jceManFN = '/tmp/jceManifest'
        if os.path.exists(jceManFN):
            jceManifest = open(jceManFN)
            for line in jceManifest:
                if 'unlimited' in line:
                    self.setup.logIt("Crypto Level Satisfactory.")
                    os.remove(jceManFN)
                    return
            os.remove(jceManFN)

            # Force Java to use unlimited crypto if JCE not present
            self.setup.logIt("Modifying java.security File for Unlimited Crypto..")

            javaSecurityFN = '/opt/jre/jre/lib/security/java.security'
            javaSecurity = open(javaSecurityFN).read()
            javaSecurity = javaSecurity.replace('#crypto.policy=unlimited', 'crypto.policy=unlimited')
            javaSecurityFile = open(javaSecurityFN, 'w')
            javaSecurityFile.write(javaSecurity)
            javaSecurityFile.close

            self.setup.logIt('java.security crypto level configured.')

    def install_casa(self):
        self.setup.logIt("Configuring Casa...")
        
        self.checkCryptoLevel()
        
        self.setup.copyFile('%s/casa.json' % self.setup.outputFolder, self.setup.configFolder)
        self.setup.run(['chmod', 'g+w', '/opt/gluu/python/libs'])

        self.setup.logIt("Copying casa.war into jetty webapps folder...")

        jettyServiceName = 'casa'
        self.setup.installJettyService(self.jetty_app_configuration[jettyServiceName])

        jettyServiceWebapps = '%s/%s/webapps' % (self.setup.jetty_base, jettyServiceName)
        self.setup.copyFile('%s/casa.war' % self.setup.distGluuFolder, jettyServiceWebapps)

        jettyServiceOxAuthCustomLibsPath = '%s/%s/%s' % (self.setup.jetty_base, "oxauth", "custom/libs")
        self.setup.copyFile('%s/%s' % (self.setup.distGluuFolder, self.twilio_jar), jettyServiceOxAuthCustomLibsPath)
        self.setup.run([self.setup.cmd_chown, '-R', 'jetty:jetty', jettyServiceOxAuthCustomLibsPath])

        # Make necessary Directories for Casa
        self.makeDirs()

    def install_gluu_components(self):
        if self.installCasa:
            self.install_casa()

        if self.install_oxd == "y":
            self.install_oxd_server()
        
    def set_ownership(self):

        self.setup.run(['chown', '-R', 'jetty:jetty', '%s/casa.json' % self.setup.configFolder])
        self.setup.run(['chmod', 'g+w', '%s/casa.json' % self.setup.configFolder])
        self.oxd_casa_json_config()

    def start_services(self):

        # Restart oxAuth service to load new custom libs
        print "\nRestarting oxAuth"
        try:
            self.setup.run_service_command('oxauth', 'restart')
            print "oxAuth restarted!\n"
        except:
            print "Error starting oxAuth! Please review setup_casa_error.log."
            self.setup.logIt("Error starting oxAuth", True)
            self.setup.logIt(traceback.format_exc(), True)

        # Start Casa
        print "Starting Casa..."
        try:
            self.setup.run_service_command('casa', 'start')
            print "Casa started!\n"
        except:
            print "Error starting Casa! Please review setup_casa_error.log."
            self.setup.logIt("Error starting Casa", True)
            self.setup.logIt(traceback.format_exc(), True)

    def save_properties(self):
        self.setup.logIt('Saving properties to %s' % self.setup.savedProperties)

        def getString(value):
            if isinstance(value, str):
                return value.strip()
            elif isinstance(value, bool):
                return str(value)
            else:
                return ""
        try:
            p = Properties.Properties()
            keys = self.__dict__.keys()
            keys.sort()
            for key in keys:
                value = getString(self.__dict__[key])
                if value != '':
                    p[key] = value
            p.store(open(self.savedProperties, 'w'))
        except:
            self.setup.logIt("Error saving properties", True)
            self.setup.logIt(traceback.format_exc(), True)
    
    def load_properties(self, fn):
        self.setup.logIt('Loading Properties %s' % fn)
        p = Properties.Properties()
        try:
            p.load(open(fn))
            properties_list = p.keys()
            for prop in properties_list:
                try:
                    self.__dict__[prop] = p[prop]
                    if p[prop] == 'True':
                        self.__dict__[prop] = True
                    elif p[prop] == 'False':
                        self.__dict__[prop] = False
                except:
                    self.setup.logIt("Error loading property %s" % prop)
                    self.setup.logIt(traceback.format_exc(), True)
        except:
            self.setup.logIt("Error loading properties", True)
            self.setup.logIt(traceback.format_exc(), True)

def print_help():
    print "\nUse setup_casa.py to configure Gluu Casa and to add initial data required for"
    print "start. If setup_casa.properties is found in this folder, these"
    print "properties will automatically be used instead of the interactive setup"
    print "Options:"
    print ""
    print "    -c   Install Gluu Casa"


def getOpts(argv, setupOptions):
    try:
        opts, args = getopt.getopt(argv, "c", [])
    except getopt.GetoptError:
        print_help()
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-c':
            setupOptions['installCasa'] = True
    return setupOptions

if __name__ == '__main__':
    setupOptions = {
        'install_dir': '.',
        'setup_properties': None,
        'noPrompt': False,
        'installCasa': False
    }
    if len(sys.argv) > 1:
        setupOptions = getOpts(sys.argv[1:], setupOptions)

    setupObject = Setup(setupOptions['install_dir'])
    installObject = SetupCasa(setupObject)

    # Configure log redirect
    setupObject.logError = installObject.logError
    setupObject.log = installObject.log

    if installObject.check_installed():
        print "\nThis instance has already been configured. If you need to install new one you should reinstall package first."
        sys.exit(2)

    installObject.installCasa = setupOptions['installCasa']

    # Get the OS and init type
    (setupObject.os_type, setupObject.os_version) = setupObject.detect_os_type()
    setupObject.os_initdaemon = setupObject.detect_initd()

    print "\nInstalling Gluu Casa...\n"
    print "Detected OS  :  %s %s" % (setupObject.os_type, setupObject.os_version)
    print "Detected init:  %s" % setupObject.os_initdaemon

    print "\nFor more info see:\n  %s  \n  %s\n" % (installObject.log, installObject.logError)
    print "\n** All clear text passwords contained in %s.\n" % installObject.savedProperties
    try:
        os.remove(installObject.log)
        setupObject.logIt('Removed %s' % installObject.log)
    except:
        pass
    try:
        os.remove(installObject.logError)
        setupObject.logIt('Removed %s' % installObject.logError)
    except:
        pass

    setupObject.logIt("Installing Gluu Casa", True)

    if setupOptions['setup_properties']:
        setupObject.logIt('%s Properties found!\n' % setupOptions['setup_properties'])
        installObject.load_properties(setupOptions['setup_properties'])
    elif os.path.isfile(installObject.setup_properties_fn):
        setupObject.logIt('%s Properties found!\n' % installObject.setup_properties_fn)
        installObject.load_properties(installObject.setup_properties_fn)
    else:
        setupObject.logIt(
            "%s Properties not found. Interactive setup commencing..." % installObject.setup_properties_fn)
        installObject.promptForProperties()

    # Validate Properties
    installObject.check_properties()

    # Show to properties for approval
    if installObject.installCasa:
        installObject.propertiesForOxd()

    proceed = "NO"
    if not setupOptions['noPrompt']:
        proceed = raw_input('Proceed with these values [Y/n]? ').lower().strip()

    if (setupOptions['noPrompt'] or not len(proceed) or (len(proceed) and (proceed[0] == 'y'))):
        try:
            installObject.download_files()
            installObject.calculate_applications_memory()
            installObject.install_gluu_components()
            installObject.set_ownership()
            installObject.import_ldif_ldap()
            installObject.start_services()
            installObject.save_properties()
        except:
            setupObject.logIt("***** Error caught in main loop *****", True)
            setupObject.logIt(traceback.format_exc(), True)

        print "Gluu Casa installation successful! Point your browser to https://%s/casa\n" % installObject.detectedHostname

    else:
        installObject.save_properties()
        print "Properties saved to %s. Change filename to %s and run './setup_casa.py' if you want to re-use the same configuration." % \
              (installObject.savedProperties, installObject.setup_properties_fn)
