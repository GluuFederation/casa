package org.gluu.casa.plugins.sample;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.IPersistenceService;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.util.StaticUtils;

/**
 * Main class of this project Note this class is referenced in plugin's manifest file (entry <code>Plugin-Class</code>).
 * <p>See <a href="http://www.pf4j.org/" target="_blank">PF4J</a> plugin framework.</p>
 * @author jgomer
 */
public class SamplePlugin extends Plugin implements ITrackable{

	private static final String AUTHORIZATIONS_DN = "ou=authorizations,o=gluu";
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	private IPersistenceService persistenceService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public SamplePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
    
    @Override
   	public int getActiveUsers(long start, long end) {
   		
       	String startTime = StaticUtils.encodeGeneralizedTime(start);
   		String endTime = StaticUtils.encodeGeneralizedTime(end - 1);
       	
   		persistenceService = Utils.managedBean(IPersistenceService.class);
   		List<String> activeUserList = Collections.emptyList();
   		String peopleDN = persistenceService.getPeopleDn();

   		// Employed to compute users who have logged in a time period
   		// any casa user will benefit from this plugin. Therefore, all users of the casa are active users of the plugin
   		
   		Filter filter = Filter.createANDFilter(Filter.createGreaterOrEqualFilter(LAST_LOGON_ATTR, startTime),
   				Filter.createLessOrEqualFilter(LAST_LOGON_ATTR, endTime));
   		activeUserList = persistenceService.find(BasePerson.class, peopleDN, filter).stream().map(BasePerson::getUid)
   				.collect(Collectors.toList());

   		return activeUserList.size();
   	}

}
