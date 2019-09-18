package org.gluu.casa.plugins.accounts;

import java.util.Set;

import org.gluu.casa.core.ITrackable;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * @author jgomer
 */
public class AccountLinkingPlugin extends Plugin implements ITrackable {

    public AccountLinkingPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
    public Set<String> getActiveUsers(long start, long end)
    {
    	return null;
    }
}
