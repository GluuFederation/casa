package org.gluu.casa.plugins.accounts.ldap;

/**
 * @author jgomer
 */

import com.unboundid.ldap.sdk.ReadOnlyEntry;
import com.unboundid.ldap.sdk.persist.FilterUsage;
import com.unboundid.ldap.sdk.persist.LDAPEntryField;
import com.unboundid.ldap.sdk.persist.LDAPField;
import com.unboundid.ldap.sdk.persist.LDAPObject;

@LDAPObject(structuralClass="oxPassportConfiguration",
        superiorClass="top")
public class oxPassportConfiguration {

    @LDAPEntryField()
    private ReadOnlyEntry ldapEntry;

    // The field used for RDN attribute ou.
    @LDAPField(inRDN=true,
            filterUsage= FilterUsage.ALWAYS_ALLOWED,
            requiredForEncode=true)
    private String[] ou;

    @LDAPField()
    private String[] gluuPassportConfiguration;

    /**
     * Retrieves the values for the field associated with the
     * gluuPassportConfiguration attribute, if present.
     *
     * @return  The values for the field associated with the
     *          gluuPassportConfiguration attribute, or
     *          {@code null} if that attribute was not present in the entry.
     */
    public String[] getGluuPassportConfiguration()
    {
        return gluuPassportConfiguration;
    }

}
