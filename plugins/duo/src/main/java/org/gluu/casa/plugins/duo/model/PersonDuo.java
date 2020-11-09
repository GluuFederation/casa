package org.gluu.casa.plugins.duo.model;

import org.gluu.casa.core.model.BasePerson;
import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry
@ObjectClass("gluuPerson")
public class PersonDuo extends BasePerson {

	private static final long serialVersionUID = 6729487374101818809L;
	@AttributeName(name = "oxDuoDevices")
	private String duoDevices;

	public String getDuoDevices() {
		return duoDevices;
	}

	public void setDuoDevices(String duoDevices) {
		this.duoDevices = duoDevices;
	}

}
