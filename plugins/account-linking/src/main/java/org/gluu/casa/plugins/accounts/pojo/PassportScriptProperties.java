package org.gluu.casa.plugins.accounts.pojo;

/**
 * @author jgomer
 */
public class PassportScriptProperties {

    private String keyStoreFile;

    private String keyStorePassword;

    private String remoteUserNameAttribute;

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getRemoteUserNameAttribute() {
        return remoteUserNameAttribute;
    }

    public void setRemoteUserNameAttribute(String remoteUserNameAttribute) {
        this.remoteUserNameAttribute = remoteUserNameAttribute;
    }

}
