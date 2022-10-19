package org.gluu.casa.plugins.emailotp.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import javax.persistence.Transient;

/**
 * Hold SMTP configuration
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmtpConfiguration implements java.io.Serializable {

    private static final long serialVersionUID = -5675038049444038755L;

    @JsonProperty("host")
    private String host;

    @JsonProperty("port")
    private int port;

    @JsonProperty("connect-protection")
    private SmtpConnectProtectionType connectProtection;

    @JsonProperty("trust-host")
    private boolean serverTrust;

    @JsonProperty("from-name")
    private String fromName;

    @JsonProperty("from-email-address")
    private String fromEmailAddress;

    @JsonProperty("requires-authentication")
    private boolean requiresAuthentication;

    @JsonProperty("user-name")
    private String userName;

    @JsonProperty("password")
    private String password;

    @Transient
    @JsonIgnore
    private String passwordDecrypted;

    @JsonProperty("key-store")
    private String keyStore;

    @JsonProperty("key-store-password")
    private String keyStorePassword;

    @JsonProperty("key-store-alias")
    private String keyStoreAlias;
    
    @JsonProperty("signing-algorithm")
    private String signingAlgorithm;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public SmtpConnectProtectionType getConnectProtection() {
        return connectProtection;
    }

    public void setConnectProtection(SmtpConnectProtectionType connectProtection) {
        this.connectProtection = connectProtection;
    }

    public boolean isServerTrust() {
        return serverTrust;
    }

    public void setServerTrust(boolean serverTrust) {
        this.serverTrust = serverTrust;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public void setFromEmailAddress(String fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    public boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    public void setRequiresAuthentication(boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public boolean isValid() {
        return getHost() != null && getPort() != 0
                && ((!isRequiresAuthentication())
                || (getUserName() != null && getPassword() != null));
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordDecrypted() {
        return passwordDecrypted;
    }

    public void setPasswordDecrypted(String passwordDecrypted) {
        this.passwordDecrypted = passwordDecrypted;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public void setKeyStoreAlias(String keyStoreAlias) {
        this.keyStoreAlias = keyStoreAlias;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SmtpConfiguration smtpConfigurationObj = (SmtpConfiguration) obj;
        return host.equals(smtpConfigurationObj.host)
                && port == smtpConfigurationObj.port
                && connectProtection.getValue().equals(smtpConfigurationObj.connectProtection.getValue())
                && serverTrust == smtpConfigurationObj.serverTrust
                && fromName.equals(smtpConfigurationObj.fromName)
                && fromEmailAddress.equals(smtpConfigurationObj.fromEmailAddress)
                && requiresAuthentication == smtpConfigurationObj.requiresAuthentication
                && userName.equals(smtpConfigurationObj.userName)
                && password.equals(smtpConfigurationObj.password)
                && passwordDecrypted.equals(smtpConfigurationObj.passwordDecrypted)
                && keyStore.equals(smtpConfigurationObj.keyStore)
                && keyStorePassword.equals(smtpConfigurationObj.keyStorePassword)
                && keyStoreAlias.equals(smtpConfigurationObj.keyStoreAlias)
                && signingAlgorithm.equals(smtpConfigurationObj.signingAlgorithm);
    }

    @Override
    public int hashCode() {
      return Objects.hash(host, port, connectProtection.getValue(), serverTrust, fromName,
              fromEmailAddress, requiresAuthentication, userName, password, passwordDecrypted,
              keyStore, keyStorePassword, keyStoreAlias, signingAlgorithm);
    }
}
