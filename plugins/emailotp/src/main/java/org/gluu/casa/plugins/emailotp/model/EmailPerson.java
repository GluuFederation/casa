package org.gluu.casa.plugins.emailotp.model;

import org.gluu.casa.core.model.BasePerson;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry
@ObjectClass("gluuPerson")
public class EmailPerson extends BasePerson {

    /**
     * 
     */
    private static final long serialVersionUID = -3072709087880306209L;

    @AttributeName(name = "oxEmailAlternate")
    private String oxEmailAlternate;

    public String getOxEmailAlternate() {
        return oxEmailAlternate;
    }

    public void setOxEmailAlternate(String oxEmailAlternate) {
        this.oxEmailAlternate = oxEmailAlternate;
    }

	@AttributeName(name = "mail")
	private String mail;

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

}
