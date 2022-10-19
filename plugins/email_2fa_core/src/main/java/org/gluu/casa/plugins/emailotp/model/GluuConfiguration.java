package org.gluu.casa.plugins.emailotp.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.CustomObjectClass;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.JsonObject;
import org.gluu.persist.annotation.ObjectClass;
import org.gluu.persist.model.base.InumEntry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * GluuConfiguration
 * 
 */
@DataEntry
@ObjectClass(value = "gluuConfiguration")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GluuConfiguration extends InumEntry implements Serializable {

	private static final long serialVersionUID = -1817003894646725601L;

	@AttributeName
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@AttributeName
	private String displayName;

	@AttributeName(name = "oxSmtpConfiguration")
	@JsonObject
	private SmtpConfiguration smtpConfiguration;

	@CustomObjectClass
	private String[] customObjectClasses;

	public String[] getCustomObjectClasses() {
		return customObjectClasses;
	}

	public void setCustomObjectClasses(String[] customObjectClasses) {
		this.customObjectClasses = customObjectClasses;
	}

	public final SmtpConfiguration getSmtpConfiguration() {
		return smtpConfiguration;
	}

	public final void setSmtpConfiguration(SmtpConfiguration smtpConfiguration) {
		this.smtpConfiguration = smtpConfiguration;
	}

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        GluuConfiguration gluuConfigurationObj = (GluuConfiguration) obj;
        return Arrays.equals(customObjectClasses, gluuConfigurationObj.customObjectClasses)
                && description.equals(gluuConfigurationObj.description)
                && displayName.equals(gluuConfigurationObj.displayName)
                && smtpConfiguration.equals(gluuConfigurationObj.smtpConfiguration);
    }

    @Override
    public int hashCode() {
      return Objects.hash(description, displayName, smtpConfiguration, customObjectClasses);
    }
}
