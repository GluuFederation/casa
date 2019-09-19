package org.gluu.casa.plugins.accounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.gluu.casa.plugins.accounts.pojo.ExternalAccount;
import org.gluu.casa.plugins.accounts.pojo.Provider;
import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.service.AccountLinkingService;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.search.filter.Filter;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.unboundid.util.StaticUtils;

/**
 * @author jgomer
 */
public class AccountLinkingPlugin extends Plugin implements ITrackable {

	IPersistenceService persistenceService = Utils.managedBean(IPersistenceService.class);
	private static AccountLinkingService slService = new AccountLinkingService();
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";

	private Logger logger = LoggerFactory.getLogger(getClass());

	public AccountLinkingPlugin(PluginWrapper wrapper) {
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
			
			// Step2. Check if the user has linked accounts. If yes, he is an active user of this plugin
			personList.forEach(person -> {
				List<ExternalAccount> linkedAccounts = slService.getAccounts(person.getUid(), true);
				List<String> accountNames = linkedAccounts.stream().map(ExternalAccount::getProvider)
						.map(Provider::getType).collect(Collectors.toList());

				String accounts = String.join(",", accountNames);
				userMap.put(person.getUid(), accounts);
				logger.info(person.getUid(),accounts);
			});

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return userMap;
	}
}
