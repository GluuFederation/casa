package org.gluu.casa.plugins.duo.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class DuoCredential  implements Comparable<DuoCredential> {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String duoUserId;
	
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String nickName;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private long addedOn;
	
	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public long getAddedOn() {
		return addedOn;
	}

	public void setAddedOn(long addedOn) {
		this.addedOn = addedOn;
	}

	public String getDuoUserId() {
		return duoUserId;
	}

	public void setDuoUserId(String duoUserId) {
		this.duoUserId = duoUserId;
	}

	public DuoCredential() {
	}

	@Override
	public int compareTo(DuoCredential o) {
		long date1 = getAddedOn();
		long date2 = o.getAddedOn();
		return (date1 < date2) ? -1 : (date1 > date2 ? 1 : 0);
	}

}
