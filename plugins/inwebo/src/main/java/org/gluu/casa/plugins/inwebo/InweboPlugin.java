package org.gluu.casa.plugins.inwebo;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.search.filter.Filter;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inwebo.api.sample.User;
import com.inwebo.console.ConsoleAdmin;
import com.inwebo.console.ConsoleAdminService;
import com.unboundid.util.StaticUtils;

/**
 * A plugin for handling second factor authentication settings for
 * administrators and users.
 * 
 * @author jgomer
 */
public class InweboPlugin extends Plugin implements ITrackable {

	private static final String AUTHORIZATIONS_DN = "ou=authorizations,o=gluu";
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	private Logger logger = LoggerFactory.getLogger(getClass());
	private IPersistenceService persistenceService;

	public InweboPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public int getActiveUsers(long start, long end) {
		persistenceService = Utils.managedBean(IPersistenceService.class);
		List<String> activeUserList = Collections.emptyList();
		String peopleDN = persistenceService.getPeopleDn();

		String startTime = StaticUtils.encodeGeneralizedTime(start);
		String endTime = StaticUtils.encodeGeneralizedTime(end - 1);
		
		// Employed to compute users who have logged in a time period
		Filter filter = Filter.createANDFilter(Filter.createGreaterOrEqualFilter(LAST_LOGON_ATTR, startTime),
				Filter.createLessOrEqualFilter(LAST_LOGON_ATTR, endTime));
		activeUserList = persistenceService.find(BasePerson.class, peopleDN, filter).stream().map(BasePerson::getUid)
				.collect(Collectors.toList());
		ConsoleAdminService cas = new ConsoleAdminService();
		ConsoleAdmin consoleAdmin = cas.getConsoleAdmin();
		int totalActiveUsers = 0;
		for (String uid : activeUserList) {
			User inweboUser = InweboService.getInstance().getUser(uid);
			if (inweboUser != null) {
				totalActiveUsers++;
			}
		}

		return totalActiveUsers;
	}

}
