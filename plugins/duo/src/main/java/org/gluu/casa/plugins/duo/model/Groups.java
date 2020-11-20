package org.gluu.casa.plugins.duo.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Groups {

    private String desc;
    @JsonProperty("group_id")
    private String groupId;
    @JsonProperty("mobile_otp_enabled")
    private boolean mobileOtpEnabled;
    private String name;
    @JsonProperty("push_enabled")
    private boolean pushEnabled;
    @JsonProperty("sms_enabled")
    private boolean smsEnabled;
    private String status;
    @JsonProperty("voice_enabled")
    private boolean voiceEnabled;
    public void setDesc(String desc) {
         this.desc = desc;
     }
     public String getDesc() {
         return desc;
     }

    public void setGroupId(String groupId) {
         this.groupId = groupId;
     }
     public String getGroupId() {
         return groupId;
     }

    public void setMobileOtpEnabled(boolean mobileOtpEnabled) {
         this.mobileOtpEnabled = mobileOtpEnabled;
     }
     public boolean getMobileOtpEnabled() {
         return mobileOtpEnabled;
     }

    public void setName(String name) {
         this.name = name;
     }
     public String getName() {
         return name;
     }

    public void setPushEnabled(boolean pushEnabled) {
         this.pushEnabled = pushEnabled;
     }
     public boolean getPushEnabled() {
         return pushEnabled;
     }

    public void setSmsEnabled(boolean smsEnabled) {
         this.smsEnabled = smsEnabled;
     }
     public boolean getSmsEnabled() {
         return smsEnabled;
     }

    public void setStatus(String status) {
         this.status = status;
     }
     public String getStatus() {
         return status;
     }

    public void setVoiceEnabled(boolean voiceEnabled) {
         this.voiceEnabled = voiceEnabled;
     }
     public boolean getVoiceEnabled() {
         return voiceEnabled;
     }

}