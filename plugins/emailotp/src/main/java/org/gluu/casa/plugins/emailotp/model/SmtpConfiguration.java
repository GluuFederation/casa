package org.gluu.casa.plugins.emailotp.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

}
