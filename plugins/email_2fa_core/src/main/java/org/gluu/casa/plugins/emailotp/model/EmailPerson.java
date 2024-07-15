package org.gluu.casa.plugins.emailotp.model;

import java.util.*;

import org.gluu.casa.core.model.BasePerson;
import org.gluu.persist.annotation.*;

@DataEntry
@ObjectClass("gluuPerson")
public class EmailPerson extends BasePerson {

    /**
     * 
     */
    private static final long serialVersionUID = -3072709087880306209L;

	@AttributeName(name = "mail")
	private List<String> mails;

	public List<String> getMails() {
		return mails;
	}

	public void setMails(List<String> mails) {
		this.mails = mails;
	}

}
