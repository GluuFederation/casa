package org.gluu.casa.plugins.duo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.gluu.casa.core.pojo.User;
import org.gluu.casa.misc.Utils;
import org.gluu.casa.plugins.duo.model.DuoCredential;
import org.gluu.casa.plugins.duo.model.DuoResponse;
import org.gluu.casa.plugins.duo.model.PersonDuo;
import org.gluu.casa.plugins.duo.model.Response;
import org.gluu.casa.service.IPersistenceService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.duosecurity.client.Http;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class that holds the logic to list and enroll duo creds
 * 
 * @author madhumita
 *
 */

public class DuoService {

	private static DuoService SINGLE_INSTANCE = null;
	public static Map<String, String> properties;
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static String ACR = "duo";
	public static final String UserAgentString = "Duo API Java/0.3.0";
	private static ObjectMapper mapper;
	private IPersistenceService persistenceService;

	private DuoService() {
		persistenceService = Utils.managedBean(IPersistenceService.class);
		reloadConfiguration();
		mapper = new ObjectMapper();

	}

	public static DuoService getInstance() {
		if (SINGLE_INSTANCE == null) {
			synchronized (DuoService.class) {
				SINGLE_INSTANCE = new DuoService();
			}
		}
		return SINGLE_INSTANCE;
	}

	public void reloadConfiguration() {
		ObjectMapper mapper = new ObjectMapper();
		properties = persistenceService.getCustScriptConfigProperties(ACR);

		if (properties == null) {
			logger.warn(
					"Config. properties for custom script '{}' could not be read. Features related to {} will not be accessible",
					ACR, ACR.toUpperCase());
		} else {
			try {

				String duo_creds_file = properties.get("duo_creds_file");

				if (Utils.isNotEmpty(duo_creds_file)) {
					String contents = new String(Files.readAllBytes(Paths.get(duo_creds_file)), StandardCharsets.UTF_8);

					properties.put("ikey", mapper.readTree(contents).get("ikey").textValue());
					properties.put("skey", mapper.readTree(contents).get("skey").textValue());
					properties.put("akey", mapper.readTree(contents).get("akey").textValue());
					properties.put("admin_api_ikey", mapper.readTree(contents).get("admin_api_ikey").textValue());
					properties.put("admin_api_skey", mapper.readTree(contents).get("admin_api_skey").textValue());

				} else {
					logger.error("Property 'duo_creds_file' not found");
				}

				logger.info("Duo settings found were: {}", mapper.writeValueAsString(properties));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public String getScriptPropertyValue(String value) {
		return properties.get(value);
	}

	public int getDeviceTotal(User user) {

		return (getDuoCredentials(user) == null) ? 0 : 1;
	}

	public DuoCredential getDuoCredentials(User user) {

		DuoCredential device = null;
		try {
			PersonDuo person = persistenceService.get(PersonDuo.class, persistenceService.getPersonDn(user.getId()));
			if (person != null) {
				String json = person.getDuoDevices();
				device = Utils.isEmpty(json) ? null : mapper.readValue(json, new TypeReference<DuoCredential>() {
				});

				if (device == null) {
					// query DUO only if local copy is not present
					String duoUserId = DuoService.getInstance().getUserId(user.getUserName());
					if (duoUserId != null) {
						try {
							// this write will be the local copy of the "duoUserId"
							boolean write = DuoService.getInstance().writeToPersistence(duoUserId, user.getId());
							if (write) {
								device = new DuoCredential();
								device.setDuoUserId(duoUserId);
								device.setNickName("DUO credential");
								device.setAddedOn(System.currentTimeMillis());
							}
						} catch (JsonProcessingException e) {
							logger.error("Failed to initialize " + e.getMessage());
						}
					}
				}

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return device;

	}

	public boolean writeToPersistence(String duoUserId, String inum) throws JsonProcessingException {
		// add an entry to persistence
		/*
		 * { "oxDuoDevices": { "addedOn": 1580847151410 } }
		 */
		DuoCredential cred = new DuoCredential();
		cred.setNickName("DUO credentials");
		cred.setAddedOn(System.currentTimeMillis());
		cred.setDuoUserId(duoUserId);

		String json = mapper.writeValueAsString(cred);

		PersonDuo person = persistenceService.get(PersonDuo.class, persistenceService.getPersonDn(inum));
		person.setDuoDevices(json);

		boolean success = persistenceService.modify(person);
		return success;
	}

	public boolean removeFromPersistence(String userId) throws JsonProcessingException {

		PersonDuo person = persistenceService.get(PersonDuo.class, persistenceService.getPersonDn(userId));
		person.setDuoDevices(null);

		return persistenceService.modify(person);

	}

	public String getUserId(String username) {
		JSONObject result;
		JSONArray response;
		// JSONObject metadata;
		try {
			// Prepare request.
			Http request = new Http("GET", getScriptPropertyValue("duo_host"), "/admin/v1/users");
			String limit = "10";

			request.addParam("username", username);
			request.signRequest(getScriptPropertyValue("admin_api_ikey"), getScriptPropertyValue("admin_api_skey"));

			// Use proxy if one was specified.
			/*
			 * if (proxy_host != null) { request.setProxy(proxy_host, proxy_port); }
			 */

			result = (JSONObject) request.executeJSONRequest();
			response = result.getJSONArray("response");
			// metadata = result.getJSONObject("metadata");

			// iterate users and print them
			if (response.length() == 1) {
				JSONObject user = response.getJSONObject(0);
				logger.info("Fetched user: " + user.get("user_id"));
				return user.get("user_id").toString();

			}

		} catch (Exception e) {
			logger.error(e.toString());
		}
		return null;
	}

	public boolean deleteDUOCredential(String userName) throws Exception {

		String duoUserId = getUserId(userName);
		logger.info("duoUserId" + duoUserId);

		try { // Prepare request.
			Http request = new Http("DELETE", getScriptPropertyValue("duo_host"), "/admin/v1/users/" + duoUserId);
			request.signRequest(getScriptPropertyValue("admin_api_ikey"), getScriptPropertyValue("admin_api_skey"));

			// Use proxy if one was specified.
			/*
			 * if (proxy_host != null) { request.setProxy(proxy_host, proxy_port); }
			 */

			// Send the request to Duo and parse the response.

			String responseRaw = request.executeRequestRaw();
			String stat = mapper.readTree(responseRaw).get("stat").textValue();
			// String response = mapper.readTree(responseRaw).get("response").textValue();

			logger.info("delete result + stat -" + responseRaw + ":" + stat);

			if ("OK".equals(stat)) {

				return true;
			}

		} catch (Exception e) {
			logger.error("error in the delete request" + e.getMessage());
			return false;
		}
		return false;
	}

	public Response getUser(String username) {
		JSONObject result;
		// JSONObject metadata;
		try {
			// Prepare request.
			Http request = new Http("GET", getScriptPropertyValue("duo_host"), "/admin/v1/users");
			String limit = "10";

			request.addParam("username", username);
			request.signRequest(getScriptPropertyValue("admin_api_ikey"), getScriptPropertyValue("admin_api_skey"));

			// Use proxy if one was specified.
			/*
			 * if (proxy_host != null) { request.setProxy(proxy_host, proxy_port); }
			 */

			result = (JSONObject) request.executeJSONRequest();
			/*
			 * Although we have a way to serialize a Java object to JSON string, there is no
			 * way to convert it back using this library.If we want that kind of
			 * flexibility, we can switch to other libraries such as Jackson.
			 */
			DuoResponse duoResponse = mapper.readValue(result.toString(), DuoResponse.class);
			if (duoResponse != null)
			{
				if ("OK".equals(duoResponse.getStat()) && duoResponse.getResponse().size() == 1) {

					logger.info(duoResponse.toString());
					return duoResponse.getResponse().get(0);
				}
			}
				

		} catch (Exception e) {
			logger.error(e.toString());
		}
		return null;
	}
}
