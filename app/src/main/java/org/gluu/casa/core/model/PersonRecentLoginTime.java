package org.gluu.casa.core.model;

import org.gluu.persist.annotation.AttributeName;
import org.gluu.persist.annotation.DataEntry;
import org.gluu.persist.annotation.ObjectClass;

@DataEntry
@ObjectClass("gluuPerson")
public class PersonRecentLoginTime extends BasePerson{

	@AttributeName(name = "oxLastLogonTime")
	private String recentLoginTime;

	public String getRecentLoginTime() {
		return recentLoginTime;
	}

	public void setRecentLoginTime(String recentLoginTime) {
		this.recentLoginTime = recentLoginTime;
	}
	
}
