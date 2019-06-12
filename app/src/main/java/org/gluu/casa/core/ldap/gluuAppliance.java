package org.gluu.casa.core.ldap;

import com.unboundid.ldap.sdk.ReadOnlyEntry;
import com.unboundid.ldap.sdk.persist.FilterUsage;
import com.unboundid.ldap.sdk.persist.LDAPEntryField;
import com.unboundid.ldap.sdk.persist.LDAPField;
import com.unboundid.ldap.sdk.persist.LDAPObject;

/**
 * @author jgomer
 */
@LDAPObject(structuralClass="gluuAppliance",
        superiorClass="top")
public class gluuAppliance {

    @LDAPEntryField()
    private ReadOnlyEntry ldapEntry;

    // The field used for RDN attribute inum.
    @LDAPField(inRDN=true,
            filterUsage= FilterUsage.ALWAYS_ALLOWED,
            requiredForEncode=true)
    private String[] inum;

    @LDAPField
    private String[] gluuVdsCacheRefreshEnabled;

    public String getGluuVdsCacheRefreshEnabled() {

        if ((gluuVdsCacheRefreshEnabled == null) || (gluuVdsCacheRefreshEnabled.length == 0)) {
            return null;
        } else {
            return gluuVdsCacheRefreshEnabled[0];
        }

    }

}
