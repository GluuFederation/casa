package org.gluu.casa.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.casa.core.model.CredentialsActiveUsersSummary;
import org.gluu.casa.core.model.PluginActiveUsersSummary;
import org.gluu.casa.core.pojo.Report;
import org.gluu.casa.misc.Utils;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An app. scoped bean that encapsulates all functionality pertaining to
 * reports.
 * 
 * @author madhumita
 */
@Named
@ApplicationScoped
public class ReportService {

	private static final String STATS_PATH = System.getProperty("server.base") + File.separator + "stats";
	ObjectMapper mapper;
	@Inject
	private Logger logger;

	@Inject
	private PersistenceService persistenceService;

	@PostConstruct
	public void inited() {

	}

	public boolean initialize() {

		boolean success = false;

		return success;
	}

	public Report getReportForCurrentMonth() {
		long now = System.currentTimeMillis();
		mapper = new ObjectMapper();
		ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.UTC);

		// Computes current month and year
		String month = t.getMonth().toString();
		String year = Integer.toString(t.getYear());

		Path statsPath = Paths.get(STATS_PATH, month + year);
		boolean fileExists = Files.isRegularFile(statsPath);

		if (fileExists) {
			byte[] result = null;
			try {
				byte[] data = Files.readAllBytes(statsPath);
				byte[] encrKey = Arrays.copyOfRange(data, 0, 256);
				encrKey = Utils.decrypt(encrKey, Utils.getPublicKey(), "RSA/ECB/PKCS1Padding");

				SecretKeySpec keySpec = new SecretKeySpec(encrKey, "AES");
				data = Arrays.copyOfRange(data, 256, data.length);
				result = Utils.decrypt(data, keySpec, "AES");// /CBC/PKCS5Padding

				Report report = mapper.readValue(data, Report.class);
				return report;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return null;
	}

	public Report getReportForCurrentMonthPvtKey() {
		long now = System.currentTimeMillis();
		mapper = new ObjectMapper();
		ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.UTC);

		// Computes current month and year
		String month = t.getMonth().toString();
		String year = Integer.toString(t.getYear());

		Path statsPath = Paths.get(STATS_PATH, month + year);
		boolean fileExists = Files.isRegularFile(statsPath);

		if (fileExists) {
			byte[] result = null;
			try {
				
				// load Pvt key
				 byte[] bytesp = Files.readAllBytes(Paths.get( "/etc/certs/casa.key"));
			     PKCS8EncodedKeySpec ksp = new PKCS8EncodedKeySpec(bytesp);
			     KeyFactory kfp = KeyFactory.getInstance("RSA");
			     PrivateKey pvt = kfp.generatePrivate(ksp);
			        
				byte[] data = Files.readAllBytes(statsPath);
				byte[] encrKey = Arrays.copyOfRange(data, 0, 256);
				encrKey = Utils.decrypt(encrKey, pvt, "RSA/ECB/PKCS1Padding");

				SecretKeySpec keySpec = new SecretKeySpec(encrKey, "AES");
				data = Arrays.copyOfRange(data, 256, data.length);
				result = Utils.decrypt(data, keySpec, "AES");// /CBC/PKCS5Padding

				Report report = mapper.readValue(result, Report.class);
				return report;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return null;
	}
	
	public Report getMockData() {
		Report report = new Report();
		report.setServerName("dc.gluu.org");
		report.setEmail("ganesh@gluu.org");
		report.setMonth("July");
		report.setYear("2019");
		report.setGeneratedOn("Thu, 25 Jul 2019 00:53:18 GMT");

		List<PluginActiveUsersSummary> pluginList = new ArrayList<PluginActiveUsersSummary>();

		PluginActiveUsersSummary metric = new PluginActiveUsersSummary();
		metric.setActiveUsers(300);
		metric.setPluginId("authorized-clients");
		metric.setVersion("3.1.5-SNAPSHOT");
		metric.setDaysUsed(20);
		pluginList.add(metric);

		metric = new PluginActiveUsersSummary();
		metric.setActiveUsers(230);
		metric.setPluginId("custom-branding");
		metric.setVersion("3.1.5-SNAPSHOT");
		metric.setDaysUsed(28);
		pluginList.add(metric);

		metric = new PluginActiveUsersSummary();
		metric.setActiveUsers(300);
		metric.setPluginId("strong-authn-settings");
		metric.setVersion("3.1.5-SNAPSHOT");
		metric.setDaysUsed(23);
		pluginList.add(metric);

		metric = new PluginActiveUsersSummary();
		metric.setActiveUsers(120);
		metric.setPluginId("account-linking");
		metric.setVersion("3.1.5-SNAPSHOT");
		metric.setDaysUsed(25);
		pluginList.add(metric);

		metric = new PluginActiveUsersSummary();
		metric.setActiveUsers(440);
		metric.setPluginId("inwebo-plugin");
		metric.setVersion("3.1.5-SNAPSHOT");
		metric.setDaysUsed(26);

		List<CredentialsActiveUsersSummary> credentialMetricsList = new ArrayList<CredentialsActiveUsersSummary>();
		CredentialsActiveUsersSummary credMetric = new CredentialsActiveUsersSummary();
		credMetric.setActiveUsers(520);
		credMetric.setCredentialName("OTP");
		credentialMetricsList.add(credMetric);

		credMetric = new CredentialsActiveUsersSummary();
		credMetric.setActiveUsers(620);
		credMetric.setCredentialName("Twilio");
		credentialMetricsList.add(credMetric);

		credMetric = new CredentialsActiveUsersSummary();
		credMetric.setActiveUsers(320);
		credMetric.setCredentialName("Super GLuu");
		credentialMetricsList.add(credMetric);

		credMetric = new CredentialsActiveUsersSummary();
		credMetric.setActiveUsers(420);
		credMetric.setCredentialName("FIDO2");
		credentialMetricsList.add(credMetric);

		report.setCredentials(credentialMetricsList);
		report.setPlugins(pluginList);
		return report;
	}

}
