package org.gluu.casa.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginActiveUsersSummary {

    private String pluginId;
    private String version;
    private int daysUsed;
    private int activeUsers;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getDaysUsed() {
        return daysUsed;
    }

    public void setDaysUsed(int daysUsed) {
        this.daysUsed = daysUsed;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

}
