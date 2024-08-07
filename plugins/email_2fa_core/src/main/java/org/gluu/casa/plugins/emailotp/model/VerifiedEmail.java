package org.gluu.casa.plugins.emailotp.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a registered credential corresponding to a verified email address
 */
public class VerifiedEmail implements Comparable<VerifiedEmail> {

	private String email;

	private long addedOn;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String nickName;

	public VerifiedEmail() {
	}

	public VerifiedEmail(String email) {
		this.email = email;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.hashCode(email) == Objects.hashCode(((VerifiedEmail)obj).getEmail());
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(addedOn, email, nickName);
    }

    @Override
	public int compareTo(VerifiedEmail ph) {
		long date1 = getAddedOn();
		long date2 = ph.getAddedOn();
		if (date1 < date2) {
		    return -1;
		}
		else {
		    return date1 > date2 ? 1 : 0;
		}
	}

	public long getAddedOn() {
		return addedOn;
	}

	public void setAddedOn(long addedOn) {
		this.addedOn = addedOn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }
}
