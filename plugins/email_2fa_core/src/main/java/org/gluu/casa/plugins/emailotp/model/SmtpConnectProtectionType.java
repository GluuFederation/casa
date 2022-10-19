/**
 * 
 */
package org.gluu.casa.plugins.emailotp.model;

import java.util.HashMap;
import java.util.Map;

import org.gluu.persist.annotation.AttributeEnum;

/**
 * @author Sergey Manoylo
 * @version 06/30/2022
 */
public enum SmtpConnectProtectionType implements AttributeEnum {

    NONE("None", "NONE"), START_TLS("StartTls", "STARTTLS"), SSL_TLS("SslTls", "SSL/TLS");

    private String value;
    private String displayName;
    
    private static final Map<String, SmtpConnectProtectionType> mapByValues = new HashMap<>();
    
    static {
        for (SmtpConnectProtectionType enumType : values()) {
            mapByValues.put(enumType.getValue(), enumType);
        }
    }

    /**
     * 
     * @param value
     * @param displayName
     */
    private SmtpConnectProtectionType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    @Override
    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SmtpConnectProtectionType getByValue(String value) {
        return mapByValues.get(value);
    }

    @Override
    public Enum<? extends AttributeEnum> resolveByValue(String value) {
        return getByValue(value);
    }

    @Override
    public String toString() {
        return value;
    }    
}
