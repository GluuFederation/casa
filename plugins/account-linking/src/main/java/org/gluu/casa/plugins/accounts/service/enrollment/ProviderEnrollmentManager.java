package org.gluu.casa.plugins.accounts.service.enrollment;

import org.gluu.casa.core.ldap.IdentityPerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.ILdapService;

/**
 * @author jgomer
 */
public interface ProviderEnrollmentManager {

    ILdapService ldapService = Utils.managedBean(ILdapService.class);

    String getUid(IdentityPerson p, boolean linked);
    boolean link(IdentityPerson p, String externalId);
    boolean remove(IdentityPerson p);
    boolean unlink(IdentityPerson p);
    boolean enable(IdentityPerson p);
    boolean isAssigned(String uid);

}
