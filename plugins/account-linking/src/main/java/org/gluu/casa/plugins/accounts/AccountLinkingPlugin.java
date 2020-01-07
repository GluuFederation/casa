package org.gluu.casa.plugins.accounts;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.accounts.pojo.ExternalAccount;
import org.gluu.casa.plugins.accounts.service.AccountLinkingService;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.search.filter.Filter;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.util.StaticUtils;

/**
 * @author jgomer
 */
public class AccountLinkingPlugin extends Plugin implements ITrackable {

	private static final String AUTHORIZATIONS_DN = "ou=authorizations,o=gluu";
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	private Logger logger = LoggerFactory.getLogger(getClass());
	private IPersistenceService persistenceService;

    public AccountLinkingPlugin(PluginWrapper wrapper) {
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
		activeUserList = persistenceService.find(BasePerson.class, peopleDN, filter).stream().map(BasePerson::getUid)
				.collect(Collectors.toList());

		int totalActiveUsers = 0;

		AccountLinkingService accountLinkingService = new AccountLinkingService();
		for (String uid : activeUserList) {
			 List<ExternalAccount> linked = accountLinkingService.getAccounts(uid, true);
			 if(linked.size() >0)
			 {
				 totalActiveUsers ++;
			 }
		}

		return totalActiveUsers;
	}

	@Override
	public String getPluginActivity(String userId) {
		AccountLinkingService slService = new AccountLinkingService();
		 List<ExternalAccount> linked = slService.getAccounts(userId, true);
	     List<ExternalAccount> unlinked = slService.getAccounts(userId, false);
	     
	     StringBuilder sb = new StringBuilder();
	     sb.append(linked.size() +" Linked accounts ");
	     for(ExternalAccount account: linked)
	     {
	    	 sb.append(account.getProvider().getDisplayName());
	     }
	     sb.append("."+unlinked.size()+" Unlinked accounts");
	     for(ExternalAccount account: unlinked)
	     {
	    	 sb.append(account.getProvider().getDisplayName());
	     }
		return sb.toString();
	}
	
}
