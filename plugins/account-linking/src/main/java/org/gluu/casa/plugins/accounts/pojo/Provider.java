package org.gluu.casa.plugins.accounts.pojo;

import org.gluu.casa.plugins.accounts.service.enrollment.ProviderEnrollmentManager;
import org.gluu.casa.plugins.accounts.service.enrollment.SamlEnrollmentManager;
import org.gluu.casa.plugins.accounts.service.enrollment.SocialEnrollmentManager;

/**
 * @author jgomer
 */
public class Provider {

    private String logo;
    private String name;
    private ProviderType type;

    public String getName() {
        return name;
    }

    public String getLogo() {
        return logo;
    }

    public ProviderType getType() {
        return type;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ProviderType type) {
        this.type = type;
    }

    public ProviderEnrollmentManager getEnrollmentManager() {

        ProviderEnrollmentManager em = null;
        if (type != null) {
            switch (type) {
                case SAML:
                    em = new SamlEnrollmentManager(this);
                    break;
                case SOCIAL:
                    em = new SocialEnrollmentManager(this);
                    break;
            }
        }
        return em;

    }

}
