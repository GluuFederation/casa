package org.gluu.casa.core.model;

import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.persist.model.base.CustomObjectAttribute;
import org.gluu.persist.model.base.InumEntry;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.AttributesList;
import org.gluu.persist.annotation.CustomObjectClass;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Serves as a minimal representation of a user (person) entry in Gluu database directory. Plugin developers can extend
 * this class by adding fields needed (with their respective getters/setters) in order to have access to more attributes.
 * Use this class in conjuction with {@link org.gluu.casa.service.IPersistenceService} to CRUD users to your server.
 */
@DataEntry
@ObjectClass("gluuPerson")
public class BasePerson extends InumEntry {

    /**
     * 
     */
    private static final long serialVersionUID = 6952879204157214152L;

    @AttributeName
    private String uid;

    @CustomObjectClass
    private static String[] customObjectClasses;

    @AttributesList(name = "name", value = "values", multiValued = "multiValued", sortByName = true)
    private List<CustomObjectAttribute> customAttributes = new ArrayList<CustomObjectAttribute>();

    static {
        IPersistenceService ips = Utils.managedBean(IPersistenceService.class);
        if (ips != null) {
            Set<String> ocs = ips.getPersonOCs();
            ocs.remove("top");
            ocs.remove("gluuPerson");
            setCustomObjectClasses(ocs.toArray(new String[0]));
        }
    }

    public String getUid() {
        return uid;
    }

    public static String[] getCustomObjectClasses() {
        return customObjectClasses;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public static void setCustomObjectClasses(String[] customObjectClasses) {
        BasePerson.customObjectClasses = customObjectClasses;
    }

    public List<CustomObjectAttribute> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(List<CustomObjectAttribute> customAttributes) {
        this.customAttributes = customAttributes;
    }
}
