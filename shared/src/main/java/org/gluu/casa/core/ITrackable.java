package org.gluu.casa.core;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.search.filter.Filter;

import com.unboundid.util.StaticUtils;

/**
 * An interface that can be implemented by plugins in order to exhibit information about plugin usage.
 */
public interface ITrackable {

	public static final String AUTHORIZATIONS_DN = "ou=authorizations,o=gluu";
	public static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	public IPersistenceService persistenceService = Utils.managedBean(IPersistenceService.class);
	/**
	 * Computes the number of active users in the period of time [start, end)
	 * 
	 * @param start A timestamp (relative to UNIX epoch)
	 * @param end   A timestamp (relative to UNIX epoch)
	 * @return An integer value. 
	 */
	default int getActiveUsers(long start, long end) {
		String startTime = StaticUtils.encodeGeneralizedTime(start);
		String endTime = StaticUtils.encodeGeneralizedTime(end - 1);

		List<String> activeUserList = Collections.emptyList();
		String peopleDN = persistenceService.getPeopleDn();

		// Employed to compute users who have logged in a time period
		// any user logging in will benefit from this plugin. Therefore, all users of the who have logged in
		// are active users of the plugin

		Filter filter = Filter.createANDFilter(Filter.createGreaterOrEqualFilter(LAST_LOGON_ATTR, startTime),
				Filter.createLessOrEqualFilter(LAST_LOGON_ATTR, endTime));
		activeUserList = persistenceService.find(BasePerson.class, peopleDN, filter).stream().map(BasePerson::getUid)
				.collect(Collectors.toList());

		return activeUserList.size();
	}

	default String getPluginActivity(String userId) {
		return "";
	}


}
