package org.gluu.casa.plugins.duo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {

	private String alias1;
	private String alias2;
	private String alias3;
	private String alias4;
	private Aliases aliases;
	private int created;
	private String email;
	private String firstname;
	private List<Groups> groups;
	@JsonProperty("is_enrolled")
	private boolean isEnrolled;
	@JsonProperty("last_directory_sync")
	private int lastDirectorySync;
	@JsonProperty("last_login")
	private int lastLogin;
	private String lastname;
	private String notes;
	private List<Phones> phones;
	private String realname;
	private String status;
	private List<Tokens> tokens;
	private List<U2ftokens> u2ftokens;
	@JsonProperty("user_id")
	private String userId;
	private String username;
	private List<Webauthncredentials> webauthncredentials;

	private List<String> desktoptokens;

	public List<String> getDesktoptokens() {
		return desktoptokens;
	}

	public void setDesktoptokens(List<String> desktoptokens) {
		this.desktoptokens = desktoptokens;
	}

	public void setEnrolled(boolean isEnrolled) {
		this.isEnrolled = isEnrolled;
	}

	public void setAlias1(String alias1) {
		this.alias1 = alias1;
	}

	public String getAlias1() {
		return alias1;
	}

	public void setAlias2(String alias2) {
		this.alias2 = alias2;
	}

	public String getAlias2() {
		return alias2;
	}

	public void setAlias3(String alias3) {
		this.alias3 = alias3;
	}

	public String getAlias3() {
		return alias3;
	}

	public void setAlias4(String alias4) {
		this.alias4 = alias4;
	}

	public String getAlias4() {
		return alias4;
	}

	public void setAliases(Aliases aliases) {
		this.aliases = aliases;
	}

	public Aliases getAliases() {
		return aliases;
	}

	public void setCreated(int created) {
		this.created = created;
	}

	public int getCreated() {
		return created;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setGroups(List<Groups> groups) {
		this.groups = groups;
	}

	public List<Groups> getGroups() {
		return groups;
	}

	public void setIsEnrolled(boolean isEnrolled) {
		this.isEnrolled = isEnrolled;
	}

	public boolean getIsEnrolled() {
		return isEnrolled;
	}

	public void setLastDirectorySync(int lastDirectorySync) {
		this.lastDirectorySync = lastDirectorySync;
	}

	public int getLastDirectorySync() {
		return lastDirectorySync;
	}

	public void setLastLogin(int lastLogin) {
		this.lastLogin = lastLogin;
	}

	public int getLastLogin() {
		return lastLogin;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}

	public void setPhones(List<Phones> phones) {
		this.phones = phones;
	}

	public List<Phones> getPhones() {
		return phones;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getRealname() {
		return realname;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setTokens(List<Tokens> tokens) {
		this.tokens = tokens;
	}

	public List<Tokens> getTokens() {
		return tokens;
	}

	public void setU2ftokens(List<U2ftokens> u2ftokens) {
		this.u2ftokens = u2ftokens;
	}

	public List<U2ftokens> getU2ftokens() {
		return u2ftokens;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Response [alias1=" + alias1 + ", alias2=" + alias2 + ", alias3=" + alias3 + ", alias4=" + alias4
				+ ", aliases=" + aliases + ", created=" + created + ", email=" + email + ", firstname=" + firstname
				+ ", groups=" + groups + ", isEnrolled=" + isEnrolled + ", lastDirectorySync=" + lastDirectorySync
				+ ", lastLogin=" + lastLogin + ", lastname=" + lastname + ", notes=" + notes + ", phones=" + phones
				+ ", realname=" + realname + ", status=" + status + ", tokens=" + tokens + ", u2ftokens=" + u2ftokens
				+ ", userId=" + userId + ", username=" + username + ", webauthncredentials=" + webauthncredentials
				+ "]";
	}

	public String getUserId() {
		return userId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setWebauthncredentials(List<Webauthncredentials> webauthncredentials) {
		this.webauthncredentials = webauthncredentials;
	}

	public List<Webauthncredentials> getWebauthncredentials() {
		return webauthncredentials;
	}

}