package org.gluu.casa.plugins.branding;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.IBrandingManager;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.search.filter.Filter;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.util.StaticUtils;

/**
 * The plugin for custom branding Gluu Casa.
 * @author jgomer
 */
public class CustomBrandingPlugin extends Plugin implements ITrackable{

	private static final String AUTHORIZATIONS_DN = "ou=authorizations,o=gluu";
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	private IPersistenceService persistenceService;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public CustomBrandingPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
   	public int getActiveUsers(long start, long end) {
   		
       	String startTime = StaticUtils.encodeGeneralizedTime(start);
   		String endTime = StaticUtils.encodeGeneralizedTime(end - 1);
       	
   		IPersistenceService persistenceService = Utils.managedBean(IPersistenceService.class);
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
    
    @Override
    public void delete() {

        try {
            IBrandingManager brandingManager = Utils.managedBean(IBrandingManager.class);
            brandingManager.factoryReset();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

}
