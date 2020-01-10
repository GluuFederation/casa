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

import org.apache.logging.log4j.core.util.datetime.Format;
import org.gluu.casa.core.ConfigurationHandler;
import org.gluu.casa.core.ExtensionsManager;
import org.gluu.casa.core.PersistenceService;
import org.gluu.casa.core.model.CredentialsActiveUsersSummary;
import org.gluu.casa.core.model.PersonRecentLoginTime;
import org.gluu.casa.core.pojo.ColumnDatatables;
import org.gluu.casa.extension.AuthnMethod;
import org.gluu.casa.misc.Utils;
import org.gluu.search.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.cdi.DelegatingVariableResolver;

import com.unboundid.util.StaticUtils;

@VariableResolver(DelegatingVariableResolver.class)
public class UserCredEnrollmentReportViewModel extends MainViewModel {
	private static final long DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1);
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";

	@WireVariable("extensionsManager")
	private ExtensionsManager extManager;
	@WireVariable
	private PersistenceService persistenceService;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Init
	public void init() {
		List<Map> userCredentialData = new ArrayList<Map>();

		List<PersonRecentLoginTime> activeUsers = Collections.emptyList();
		List<CredentialsActiveUsersSummary> credentialMetricsList = new ArrayList<CredentialsActiveUsersSummary>();
		long now = System.currentTimeMillis();
		long todayStartAt = now - now % DAY_IN_MILLIS;
		ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.UTC);
		long start = todayStartAt - (t.getDayOfMonth() - 1) * DAY_IN_MILLIS;
		List<ColumnDatatables> columnList = new ArrayList<ColumnDatatables>();

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

			// define the columns in the table
			columnList.add(new ColumnDatatables("userId", "Username"));
			for (String method : ConfigurationHandler.DEFAULT_SUPPORTED_METHODS) {
				columnList.add(new ColumnDatatables(method, method));
			}
			columnList.add(new ColumnDatatables("lastLoginDate", "Recent Sign-in"));

			for (PersonRecentLoginTime user : activeUsers) {

				Map<String, String> userCredMap = new HashMap<String, String>();
				userCredMap.put("userId", user.getUid());

				for (String method : ConfigurationHandler.DEFAULT_SUPPORTED_METHODS) {
					AuthnMethod authMethod = extManager.getExtensionForAcr(method).get();

					userCredMap.put(method, String.valueOf(authMethod.getTotalUserCreds(user.getInum())));
				}
				userCredMap.put("lastLoginDate",
						formatter.format(StaticUtils.decodeGeneralizedTime(user.getRecentLoginTime())));
				userCredentialData.add(userCredMap);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		String jsonData = Utils.jsonFromObject(userCredentialData);
		String columnJsonData = Utils.jsonFromObject(columnList);
		Clients.evalJavaScript("loadReport(" + jsonData + "," + columnJsonData + ")");

	}

}
