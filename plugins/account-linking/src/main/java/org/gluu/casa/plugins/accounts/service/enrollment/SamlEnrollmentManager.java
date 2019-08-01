package org.gluu.casa.plugins.accounts.service.enrollment;

import org.gluu.casa.core.ldap.IdentityPerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.pojo.Provider;

import java.util.*;

/**
 * @author jgomer
 */
public class SamlEnrollmentManager extends AbstractEnrollmentManager {

    private static final String OXEXTERNALUID_PREFIX = "passport-saml:";

    public SamlEnrollmentManager(Provider provider) {
        super(provider);
    }

    public String getUid(IdentityPerson p, boolean linked) {

        List<String> list = Utils.listfromArray(linked ? p.getOxExternalUid() : p.getOxUnlinkedExternalUids());
        for (String externalUid : list) {
            if (externalUid.startsWith(OXEXTERNALUID_PREFIX)) {

                int i = externalUid.lastIndexOf(":");
                if (i > OXEXTERNALUID_PREFIX.length() && i < externalUid.length() - 1) {
                    String providerName = externalUid.substring(OXEXTERNALUID_PREFIX.length(), i);
                    if (provider.getName().equals(providerName)) {
                        return externalUid.substring(i+1);
                    }
                }
            }
        }
        return null;

    }

    public boolean link(IdentityPerson p, String externalId) {

        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxExternalUid()));
        list.add(getFormattedAttributeVal(externalId));

        logger.info("Linked accounts for {} will be {}", p.getUid(), list);
        p.setOxExternalUid(list.toArray(new String[0]));
        return updatePerson(p);

    }

    public boolean remove(IdentityPerson p) {
        removeProvider(p);
        return updatePerson(p);
    }

    public boolean unlink(IdentityPerson p) {

        String uid = removeProvider(p);
        if (uid == null) {
            return false;
        }

        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxUnlinkedExternalUids()));
        list.add(getFormattedAttributeVal(uid));
        p.setOxUnlinkedExternalUids(list.toArray(new String[0]));
        return updatePerson(p);

    }

    public boolean enable(IdentityPerson p) {

        String uid = removeProvider(p);
        if (uid == null) {
            return false;
        }

        List<String> list = new ArrayList<>(Utils.listfromArray(p.getOxExternalUid()));
        list.add(getFormattedAttributeVal(uid));
        p.setOxExternalUid(list.toArray(new String[0]));
        return updatePerson(p);

    }

    public boolean isAssigned(String uid) {
        IdentityPerson p = new IdentityPerson();
        p.setOxExternalUid(getFormattedAttributeVal(uid));
        return ldapService.find(p, IdentityPerson.class, ldapService.getPeopleDn()).size() > 0;
    }

    private String removeProvider(IdentityPerson p) {

        String pattern = String.format("%s%s:",OXEXTERNALUID_PREFIX, provider.getName());

        Set<String> externalUids = new HashSet<>(Utils.listfromArray(p.getOxExternalUid()));
        Set<String> unlinkedUids = new HashSet<>(Utils.listfromArray(p.getOxUnlinkedExternalUids()));

        String externalUid = externalUids.stream().filter(str -> str.startsWith(pattern)).findFirst()
                .map(str -> str.substring(pattern.length())).orElse("");
        if (externalUid.length() == 0) {
            externalUid = unlinkedUids.stream().filter(str -> str.startsWith(pattern)).findFirst()
                    .map(str -> str.substring(pattern.length())).orElse("");
        }

        if (externalUid.length() > 0) {
            String str = getFormattedAttributeVal(externalUid);
            externalUids.remove(str);
            unlinkedUids.remove(str);
        } else {
            externalUid = null;
        }

        p.setOxExternalUid(externalUids.toArray(new String[0]));
        p.setOxUnlinkedExternalUids(unlinkedUids.toArray(new String[0]));
        return externalUid;

    }

    private String getFormattedAttributeVal(String uid) {
        return String.format("passport-saml:%s:%s", provider.getName(), uid);
    }

}
