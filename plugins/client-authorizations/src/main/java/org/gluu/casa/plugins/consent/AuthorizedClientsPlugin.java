package org.gluu.casa.plugins.consent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.core.model.Client;
import org.gluu.casa.core.model.Scope;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.consent.service.ClientAuthorizationsService;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.search.filter.Filter;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.util.StaticUtils;

/**
 * The plugin for consent management.
 * 
 * @author jgomer
 */
public class AuthorizedClientsPlugin extends Plugin implements ITrackable {

	private static final String AUTHORIZATIONS_DN = "ou=authorizations,o=gluu";
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	private Logger logger = LoggerFactory.getLogger(getClass());
	private IPersistenceService persistenceService;

	public AuthorizedClientsPlugin(PluginWrapper wrapper) {
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
		Filter filter = Filter.createANDFilter(Filter.createGreaterOrEqualFilter(LAST_LOGON_ATTR, startTime),
				Filter.createLessOrEqualFilter(LAST_LOGON_ATTR, endTime));
		activeUserList = persistenceService.find(BasePerson.class, peopleDN, filter).stream().map(BasePerson::getInum)
				.collect(Collectors.toList());

		int totalActiveUsers = 0;

		ClientAuthorizationsService cas = new ClientAuthorizationsService();
		for (String inum : activeUserList) {
			Map<Client, Set<Scope>> userClientPermissions = cas.getUserClientPermissions(inum);

			if (userClientPermissions.size() > 0) {
				totalActiveUsers++;
			}
		}

		return totalActiveUsers;
	}

}
