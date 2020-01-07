package org.gluu.casa.ui.vm.admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gluu.casa.core.ReportService;
import org.gluu.casa.core.model.CredentialsActiveUsersSummary;
import org.gluu.casa.core.model.PluginActiveUsersSummary;
import org.gluu.casa.core.pojo.Report;
import org.gluu.casa.misc.WebUtils;
import org.gluu.casa.ui.model.report.BarChart;
import org.gluu.casa.ui.model.report.BarData;
import org.gluu.casa.ui.model.report.BarDatasets;
import org.gluu.casa.ui.model.report.BarOptions;
import org.gluu.casa.ui.model.report.Scales;
import org.gluu.casa.ui.model.report.Ticks;
import org.gluu.casa.ui.model.report.YAxes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.web.Attributes;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.cdi.DelegatingVariableResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author madhumita
 *
 */
@VariableResolver(DelegatingVariableResolver.class)
public class ReportViewModel extends MainViewModel {

	private List<String> COLORS = Arrays.asList("#F0E68C", "#9ACD32", "#ff9999", "#80dfff", "#ffffb3", "#ff99ff",
			"#b399ff", "#9ce085", "#ffb3b3");
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static final String STATS_PATH = System.getProperty("server.base") + File.separator + "stats";
	private Report report;
	private List<String> reportNames;
	private String month;
	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public List<String> getReportNames() {
		return reportNames;
	}

	public void setReportNames(List<String> reportNames) {
		this.reportNames = reportNames;
	}

	String jsonData;

	@WireVariable
	private ReportService reportService;

	public String getJsonData() {
		return jsonData;
	}

	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}

	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	@Init // (superclass = true)
	public void init() {
		reloadConfig();
	}

	private void reloadConfig() {
		reportNames = getAllReportFileNames();
		long now = System.currentTimeMillis();
		ZonedDateTime t = ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneOffset.UTC);

		// Computes current month and year
		String month = t.getMonth().toString();
		String year = Integer.toString(t.getYear());
		
		month=month+year;

		report = reportService.getReportForCurrentMonthPvtKey(month+year);
		//report = reportService.getMockData();
		loadCredentialsChart();
		loadPluginsChart();
 
	}
	
	private void loadCredentialsChart()
	{
		
		if (report != null) {
			BarChart barchart = new BarChart();
			List<BarDatasets> datasetList = new ArrayList<BarDatasets>();
			BarDatasets dataset = new BarDatasets();
			dataset.setLabel("Active users of 2 Factor authentication methods");

			List<Integer> activeUsers = new ArrayList<Integer>();
			List<String> labelsOnXAxis = new ArrayList<String>();
			List<String> backgroundColorList = new ArrayList<String>();
			List<String> backgroundColorBorderList = new ArrayList<String>();
			int i = 0;
			
			for (CredentialsActiveUsersSummary credential : report.getCredentials()) {
				activeUsers.add(credential.getActiveUsers());
				labelsOnXAxis.add(credential.getCredentialName());
				backgroundColorList.add(COLORS.get(i));
				backgroundColorBorderList.add(COLORS.get(i++));

			}
			

			datasetList.add(dataset);
			dataset.setData(activeUsers);
			dataset.setBackgroundColor(backgroundColorList);
			dataset.setBorderColor(backgroundColorBorderList);
		//	dataset.setBorderWidth(2);

			BarData barData = new BarData();
			barData.setLabels(labelsOnXAxis);
			barData.setDatasets(datasetList);

			barchart.setData(barData);
			barchart.setType("bar");

			BarOptions options = new BarOptions();
			Scales scales = new Scales();
			Ticks ticks = new Ticks();
			ticks.setBeginAtZero(true);
			//ticks.setPadding(10);
			YAxes yaxes = new YAxes();
			yaxes.setTicks(ticks);
			scales.setYAxes(Arrays.asList(yaxes));
			options.setScales(scales);
			barchart.setOptions(options);
			ObjectMapper mapper = new ObjectMapper();
			try {
				jsonData = mapper.writeValueAsString(barchart);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				logger.error("error creating json" + e.getMessage());
			}

			Clients.evalJavaScript("loadChartCreds(" + jsonData + ")");
		}
	}
	private void loadPluginsChart()
	{
		if (report != null) {
			BarChart barchart = new BarChart();
			List<BarDatasets> datasetList = new ArrayList<BarDatasets>();
			BarDatasets dataset = new BarDatasets();
			dataset.setLabel("Active Users of Plugins");

			List<Integer> activeUsers = new ArrayList<Integer>();
			List<String> labelsOnXAxis = new ArrayList<String>();
			List<String> backgroundColorList = new ArrayList<String>();
			List<String> backgroundColorBorderList = new ArrayList<String>();
			int i = COLORS.size()-1;
			
			for (PluginActiveUsersSummary plugin : report.getPlugins()) {
				activeUsers.add(plugin.getActiveUsers());
				labelsOnXAxis.add(plugin.getPluginId());
				backgroundColorList.add(COLORS.get(i));
				backgroundColorBorderList.add(COLORS.get(i--));
			}

			datasetList.add(dataset);
			dataset.setData(activeUsers);
			dataset.setBackgroundColor(backgroundColorList);
			dataset.setBorderColor(backgroundColorBorderList);
		//	dataset.setBorderWidth(2);

			BarData barData = new BarData();
			barData.setLabels(labelsOnXAxis);
			barData.setDatasets(datasetList);

			barchart.setData(barData);
			barchart.setType("bar");

			BarOptions options = new BarOptions();
			Scales scales = new Scales();
			Ticks ticks = new Ticks();
			ticks.setBeginAtZero(true);
			//ticks.setPadding(10);
			YAxes yaxes = new YAxes();
			yaxes.setTicks(ticks);
			scales.setYAxes(Arrays.asList(yaxes));
			options.setScales(scales);
			barchart.setOptions(options);
			ObjectMapper mapper = new ObjectMapper();
			try {
				jsonData = mapper.writeValueAsString(barchart);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				logger.error("error creating json" + e.getMessage());
			}

			Clients.evalJavaScript("loadChartPlugins(" + jsonData + ")");
		}
	}
	
	public List<String> getAllReportFileNames()
	{
		try (Stream<Path> walk = Files.walk(Paths.get(STATS_PATH))) {

			List<String> result = walk.filter(Files::isRegularFile)
					.map(x -> Paths.get(x.toString()).getFileName().toString()).collect(Collectors.toList());

			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	@Command
	public void monthChanged(@BindingParam("month") String monthYear) {

		month=monthYear;
		report = reportService.getReportForCurrentMonthPvtKey(month);
		loadCredentialsChart();
		loadPluginsChart();
	}
}
