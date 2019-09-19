package org.gluu.casa.plugins.strongauthn;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.gluu.casa.core.ITrackable;
import org.gluu.casa.core.model.BasePerson;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.strongauthn.model.PersonPreferences;
import org.gluu.casa.service.IPersistenceService;
import org.gluu.search.filter.Filter;
import org.gluu.util.security.StringEncrypter;
import org.gluu.util.security.StringEncrypter.EncryptionException;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.unboundid.util.StaticUtils;

/**
 * A plugin for handling second factor authentication settings for
 * administrators and users.
 * 
 * @author jgomer
 */
public class StrongAuthnSettingsPlugin extends Plugin implements ITrackable {

	private Logger logger = LoggerFactory.getLogger(getClass());
	IPersistenceService persistenceService = Utils.managedBean(IPersistenceService.class);
	private static final String LAST_LOGON_ATTR = "oxLastLogonTime";
	private StringEncrypter stringEncrypter = new StringEncrypter();

	public StrongAuthnSettingsPlugin(PluginWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void delete() {

		// This impl is a extreme hack... there was no other feasible way though. This
		// plugin offers features orthogonal
		// to core Casa so there is no way to refactor the core without exposing many
		// vital classes that must not be
		// publicly accessible (eg. in casa-shared module). Unfortunately it generates a
		// coupling with ConfigurationHandler
		// and MainSettings classes.
		// This method helps prevent cheating (e.g. installing the plugin once to
		// configure 2FA settings and then remove it
		// (not paying $$$ more) and still enjoy the benefits of the configuration)
		Logger logger = LoggerFactory.getLogger(getClass());
		try {
			Class<?> clazz = Class.forName("org.gluu.casa.core.ConfigurationHandler");
			Method method = getAMethod("getSettings", clazz);
			Object settings = method.invoke(Utils.managedBean(clazz));

			logger.info("MainSettings obtained");
			clazz = settings.getClass();
			logger.info("Reverting 2FA settings to factory settings");

			method = getAMethod("setMinCredsFor2FA", clazz);
			method.invoke(settings, 2);

			method = getAMethod("setTrustedDevicesSettings", clazz);
			method.invoke(settings, new Object[] { null });

			Class<?> epcls = Class.forName("org.gluu.casa.conf.sndfactor.EnforcementPolicy");
			Object epclsInstance = epcls.cast(getAMethod("valueOf", epcls).invoke(epcls, "EVERY_LOGIN"));

			method = getAMethod("setEnforcement2FA", clazz);
			method.invoke(settings, Collections.singletonList(epclsInstance));

			method = getAMethod("save", clazz);
			method.invoke(settings);

			logger.info("Done");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Method getAMethod(String name, Class<?> clazz) {
		return Stream.of(clazz.getMethods()).filter(m -> m.getName().equals(name)).findFirst().orElse(null);
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

			// Step2. Check if the user has entries in the following fields -
			// oxPreferredMethod,oxStrongAuthPolicy,oxTrustedDevicesInfo in LDAP

			personList.forEach(person -> {
				PersonPreferences personSetting = persistenceService.get(PersonPreferences.class,
						persistenceService.getPersonDn(person.getInum()));

				List<String> info = new ArrayList<String>();
				if ("true".equals(personSetting.getPreferredMethod())) {
					info.add("preferredMethod:true");
				}
				
				info.add("strongAuthPolicy:" + Optional.ofNullable(personSetting.getStrongAuthPolicy()));
				try {
					info.add("trustedDevicesInfo:" +  Optional.ofNullable(stringEncrypter.decrypt(personSetting.getTrustedDevices())));
				} catch (EncryptionException ex) {
					logger.error(ex.getMessage(), ex);
				}
			});

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return userMap;
	}
}
