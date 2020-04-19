package es.ubu.lsi.ubumonitor.view.chart.risk;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.ubu.lsi.ubumonitor.controllers.Controller;
import es.ubu.lsi.ubumonitor.controllers.MainController;
import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import es.ubu.lsi.ubumonitor.util.JSArray;
import es.ubu.lsi.ubumonitor.util.JSObject;
import es.ubu.lsi.ubumonitor.view.chart.ChartType;
import es.ubu.lsi.ubumonitor.view.chart.Chartjs;
import es.ubu.lsi.ubumonitor.view.chart.Tabs;

public class Bubble extends Chartjs {

	public Bubble(MainController mainController) {
		super(mainController, ChartType.BUBBLE, Tabs.RISK);
		useGeneralButton = false;
		useLegend = true;
		useGroupButton = false;
	}

	@Override
	public void exportCSV(String path) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getOptions() {

		int limit = 14;

		JSObject jsObject = getDefaultOptions();
		jsObject.putWithQuote("typeGraph", "bubble");
		JSObject callbacks = new JSObject();

		callbacks.put("beforeTitle", "function(e,t){return'Course: '+e[0].xLabel}");
		callbacks.put("title", "function(e,t){return'Moodle: '+e[0].yLabel}");
		callbacks.put("label", "function(e,t){return t.datasets[e.datasetIndex].data[e.index].users}");
		jsObject.put("tooltips", "{callbacks:" + callbacks + "}");

		JSObject scales = new JSObject();
		
		JSObject ticks = new JSObject();
		ticks.put("min", 0);
		ticks.put("max", limit + 2);
		ticks.put("callback", "function(e,t,n){return " + limit + "==e?'>'+e:e>" + limit + "?'':e}");
		
		
		scales.put("yAxes", "[{"+getYScaleLabel()+",ticks:"+ticks+"}]");
		scales.put("xAxes", "[{"+getXScaleLabel()+",ticks:"+ticks+"}]");
		jsObject.put("scales", scales);

		return jsObject.toString();
	}

	@Override
	public void update() {
		String dataset = createDataset(getSelectedEnrolledUser());
		String options = getOptions();
		System.out.println(options);
		webViewChartsEngine.executeScript(String.format("updateChartjs(%s,%s)", dataset, options));

	}

	private String createDataset(List<EnrolledUser> selectedEnrolledUser) {
		int limit = 14;
		Map<Long, Map<Long, List<EnrolledUser>>> lastAccess = createLastAccess(selectedEnrolledUser, limit);
		JSObject data = new JSObject();

		JSArray datasets = new JSArray();
		JSObject dataset = new JSObject();
		dataset.putWithQuote("label", "Selected Users");
		dataset.putWithQuote("backgroundColor", "rgba(207,52,118,0.2)");
		dataset.putWithQuote("borderColor", "rgba(207,52,118,1)");
		JSArray datasetData = new JSArray();
		for (Map.Entry<Long, Map<Long, List<EnrolledUser>>> entry : lastAccess.entrySet()) {
			long lastCourseAccess = entry.getKey();
			Map<Long, List<EnrolledUser>> map = entry.getValue();
			for (Map.Entry<Long, List<EnrolledUser>> entryUsers : map.entrySet()) {
				JSObject jsObject = new JSObject();
				jsObject.put("x", lastCourseAccess);
				jsObject.put("y", entryUsers.getKey());
				jsObject.put("r", entryUsers.getValue()
						.size() * 10);
				JSArray users = new JSArray();
				entryUsers.getValue().forEach(u->users.addWithQuote(u.getFullName()));
				jsObject.put("users", users);
				datasetData.add(jsObject);
			}

		}
		dataset.put("data", datasetData);
		datasets.add(dataset);
		data.put("datasets", datasets);
		return data.toString();
	}

	public Map<Long, Map<Long, List<EnrolledUser>>> createLastAccess(List<EnrolledUser> selectedEnrolledUser,
			int limit) {
		ZonedDateTime lastLogTime = Controller.getInstance()
				.getActualCourse()
				.getLogs()
				.getLastDatetime();
		Map<Long, Map<Long, List<EnrolledUser>>> lastAccess = new HashMap<>();

		for (EnrolledUser user : selectedEnrolledUser) {
			long diffCourse = Math.min(Math.max(0L, ChronoUnit.DAYS.between(user.getLastcourseaccess(), lastLogTime)),
					limit);
			long diffServer = Math.min(Math.max(0, ChronoUnit.DAYS.between(user.getLastaccess(), lastLogTime)), limit);

			lastAccess.computeIfAbsent(diffCourse, k -> new HashMap<>())
					.computeIfAbsent(diffServer, k -> new ArrayList<>())
					.add(user);

		}
		return lastAccess;
	}

}
