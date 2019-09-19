package org.gluu.casa.plugins.consent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.consent.model.Client;
import org.gluu.casa.plugins.consent.model.Scope;
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
	IPersistenceService persistenceService = Utils.managedBean(IPersistenceService.class);
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	private ClientAuthorizationsService cas = new ClientAuthorizationsService();
	private Logger logger = LoggerFactory.getLogger(getClass());
	public AuthorizedClientsPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	public Map<String, String> getActiveUsers(long start, long end) {

		Map<String, String> userMap = new HashMap<String, String>();

		try {
			String peopleDN = persistenceService.getPeopleDn();
			String startTime = StaticUtils.encodeGeneralizedTime(start);
			String endTime = StaticUtils.encodeGeneralizedTime(end - 1);

			// Step1. Employed to compute users who have logged in a time period
			Filter filter = Filter.createANDFilter(Filter.createGreaterOrEqualFilter(LAST_LOGON_ATTR, startTime),
					Filter.createLessOrEqualFilter(LAST_LOGON_ATTR, endTime));
			List<BasePerson> personList = persistenceService.find(BasePerson.class, peopleDN, filter);
			
			// Step2. Check if the user has user-client-permission. If yes, he is an active user of this plugin
			personList.forEach(person -> {
				 
				Map<Client,Set<Scope>> map = cas.getUserClientPermissions(person.getUid());
				userMap.put(person.getUid(), String.join(",",map.keySet().stream().map(Client::getDisplayName).collect(Collectors.toList())));
				logger.info(person.getUid(),map.keySet().stream().map(Client::getInum));
			});

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return userMap;
	}
}
