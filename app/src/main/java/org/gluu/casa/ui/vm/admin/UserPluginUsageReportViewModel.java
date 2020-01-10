package org.gluu.casa.ui.vm.admin;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.gluu.casa.core.ExtensionsManager;
import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.PersistenceService;
import org.gluu.casa.core.model.PersonRecentLoginTime;
import org.gluu.casa.core.pojo.ColumnDatatables;
import org.gluu.casa.misc.Utils;
import org.gluu.search.filter.Filter;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.cdi.DelegatingVariableResolver;

import com.unboundid.util.StaticUtils;

@VariableResolver(DelegatingVariableResolver.class)
public class UserPluginUsageReportViewModel extends MainViewModel {
	private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";

	@WireVariable("extensionsManager")
	private ExtensionsManager extManager;
	@WireVariable
	private PersistenceService persistenceService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Init
	public void init() {
		List<Map> userPluginData = new ArrayList<Map>();
		List<ColumnDatatables> columnList = new ArrayList<ColumnDatatables>();
		
		List<PersonRecentLoginTime> activeUsers = Collections.emptyList();
		long now = System.currentTimeMillis();
		long todayStartAt = now - now % DAY_IN_MILLIS;
		ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.UTC);
		long start = todayStartAt - (t.getDayOfMonth() - 1) * DAY_IN_MILLIS;

		// Implementing this method by iterating through every user in the database and
		// calling method
		// org.gluu.casa.extension.AuthnMethod.getTotalUserCreds() is prohibitely
		// expensive: we
		// have to solve it by using low-level direct queries
		try {
			String peopleDN = persistenceService.getPeopleDn();
			String startTime = StaticUtils.encodeGeneralizedTime(start);
			String endTime = StaticUtils.encodeGeneralizedTime(now - 1);
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
			// Employed to compute users who have logged in a time period
			Filter filter = Filter.createANDFilter(Filter.createGreaterOrEqualFilter(LAST_LOGON_ATTR, startTime),
					Filter.createLessOrEqualFilter(LAST_LOGON_ATTR, endTime));
			activeUsers = persistenceService.find(PersonRecentLoginTime.class, peopleDN, filter);
			
			
			List<PluginWrapper> pluginSummary = extManager.getPlugins().stream()
					.filter(pw -> pw.getDescriptor().getPluginClass().startsWith("org.gluu.casa.plugins")
							&& pw.getPluginState().equals(PluginState.STARTED))
					.collect(Collectors.toList());

			columnList.add(new ColumnDatatables("userId", "Username"));
			for (PluginWrapper wrapper : pluginSummary) {
				columnList.add(new ColumnDatatables(wrapper.getPluginId(), wrapper.getPluginId()));
			}
			columnList.add(new ColumnDatatables("lastLoginDate", "Recent Sign-in"));
			
			for (PersonRecentLoginTime user : activeUsers) {

				Map<String, String> userPluginMap = new HashMap<String, String>();
				userPluginMap.put("userId", user.getUid());

				for (PluginWrapper wrapper : pluginSummary) {

					try {
						// Is plugin implementing ITrackable?
						String pluginActivity = ITrackable.class.cast(wrapper.getPlugin()).getPluginActivity( user.getInum());
						userPluginMap.put(wrapper.getPluginId(), pluginActivity);
					} catch (ClassCastException e) {
						logger.info("Plugin {} does not implement ITrackable. Cannot compute active users");
					}
				}
				
				userPluginMap.put("lastLoginDate",formatter.format(StaticUtils.decodeGeneralizedTime(user.getRecentLoginTime())));
				
				userPluginData.add(userPluginMap);
			}
			

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		String jsonData = Utils.jsonFromObject(userPluginData);
		String columnJsonData = Utils.jsonFromObject(columnList);
		Clients.evalJavaScript("loadReport(" + jsonData + ","+columnJsonData+")");

	}

}
