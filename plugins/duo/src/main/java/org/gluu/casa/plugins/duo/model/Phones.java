package org.gluu.casa.plugins.duo.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Phones {

	private boolean activated;
	private List<String> capabilities;
	private String encrypted;
	private String extension;
	private String fingerprint;
	@JsonProperty("last_seen")
	private Date lastSeen;
	private String model;
	private String name;
	private String number;
	@JsonProperty("phone_id")
	private String phoneId;
	private String platform;
	private String postdelay;
	private String predelay;
	private String screenlock;
	@JsonProperty("sms_passcodes_sent")
	private boolean smsPasscodesSent;
	private String tampered;
	private String type;

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	@Override
	public String toString() {
		return "Phones [activated=" + activated + ", capabilities=" + capabilities + ", encrypted=" + encrypted
				+ ", extension=" + extension + ", fingerprint=" + fingerprint + ", lastSeen=" + lastSeen + ", model="
				+ model + ", name=" + name + ", number=" + number + ", phoneId=" + phoneId + ", platform=" + platform
				+ ", postdelay=" + postdelay + ", predelay=" + predelay + ", screenlock=" + screenlock
				+ ", smsPasscodesSent=" + smsPasscodesSent + ", tampered=" + tampered + ", type=" + type + "]";
	}

	public boolean getActivated() {
		return activated;
	}

	public void setCapabilities(List<String> capabilities) {
		this.capabilities = capabilities;
	}

	public List<String> getCapabilities() {
		return capabilities;
	}

	public void setEncrypted(String encrypted) {
		this.encrypted = encrypted;
	}

	public String getEncrypted() {
		return encrypted;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setLastSeen(Date lastSeen) {
		this.lastSeen = lastSeen;
	}

	public Date getLastSeen() {
		return lastSeen;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getModel() {
		return model;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getNumber() {
		return number;
	}

	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}

	public String getPhoneId() {
		return phoneId;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPostdelay(String postdelay) {
		this.postdelay = postdelay;
	}

	public String getPostdelay() {
		return postdelay;
	}

	public void setPredelay(String predelay) {
		this.predelay = predelay;
	}

	public String getPredelay() {
		return predelay;
	}

	public void setScreenlock(String screenlock) {
		this.screenlock = screenlock;
	}

	public String getScreenlock() {
		return screenlock;
	}

	public void setSmsPasscodesSent(boolean smsPasscodesSent) {
		this.smsPasscodesSent = smsPasscodesSent;
	}

	public boolean getSmsPasscodesSent() {
		return smsPasscodesSent;
	}

	public void setTampered(String tampered) {
		this.tampered = tampered;
	}

	public String getTampered() {
		return tampered;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}