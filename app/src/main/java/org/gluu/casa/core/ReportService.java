package org.gluu.casa.core;

import java.io.File;
import java.io.IOException;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

				Report report = Utils.objectFromJson(data, Report.class);
				return report;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return null;
	}

	public List<String> getAllFiles() {
		List<String> result = new ArrayList<String>();
		try (Stream<Path> walk = Files.walk(Paths.get(STATS_PATH))) {

			result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public Report getReportForCurrentMonthPvtKey(String fileName) {
		
		Path statsPath = Paths.get(STATS_PATH, fileName);
		boolean fileExists = Files.isRegularFile(statsPath);

		if (fileExists) {
			byte[] result = null;
			try {

				// load Pvt key
				byte[] bytesp = Files.readAllBytes(Paths.get("/etc/certs/casa.key"));
				PKCS8EncodedKeySpec ksp = new PKCS8EncodedKeySpec(bytesp);
				KeyFactory kfp = KeyFactory.getInstance("RSA");
				PrivateKey pvt = kfp.generatePrivate(ksp);

				byte[] data = Files.readAllBytes(statsPath);
				byte[] encrKey = Arrays.copyOfRange(data, 0, 256);
				encrKey = Utils.decrypt(encrKey, pvt, "RSA/ECB/PKCS1Padding");

				SecretKeySpec keySpec = new SecretKeySpec(encrKey, "AES");
				data = Arrays.copyOfRange(data, 256, data.length);
				result = Utils.decrypt(data, keySpec, "AES");// /CBC/PKCS5Padding

				Report report = Utils.objectFromJson(result, Report.class);
				return report;

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return null;
	}

	

}
