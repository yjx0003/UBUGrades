package es.ubu.lsi.ubumonitor.controllers.tabs;

import es.ubu.lsi.ubumonitor.controllers.MainController;
import es.ubu.lsi.ubumonitor.controllers.WebViewAction;
import es.ubu.lsi.ubumonitor.controllers.configuration.MainConfiguration;
import es.ubu.lsi.ubumonitor.model.Course;
import es.ubu.lsi.ubumonitor.view.chart.bridge.ForumConnector;
import es.ubu.lsi.ubumonitor.view.chart.bridge.JavaConnector;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ForumController extends WebViewAction {

	private ForumConnector javaConnector;
	@FXML
	private GridPane dateGridPane;
	@FXML
	private DatePicker datePickerStart;
	@FXML
	private DatePicker datePickerEnd;

	@Override
	public void init(MainController mainController, Tab tab, Course actualCourse, MainConfiguration mainConfiguration,
			Stage stage) {
		javaConnector = new ForumConnector(webViewController.getWebViewCharts(), mainConfiguration, mainController,
				actualCourse, dateGridPane, datePickerStart, datePickerEnd);
		init(tab, actualCourse, mainConfiguration, stage, javaConnector);
		mainController.getWebViewTabsController()
				.getVisualizationController()
				.bindDatePicker(this, datePickerStart, datePickerEnd);
	}

	@Override
	public void onWebViewTabChange() {

		javaConnector.updateOptionsImages();
		javaConnector.updateChart();

	}

	@Override
	public void updateListViewEnrolledUser() {
		updateChart();

	}

	@Override
	public void updatePredicadeEnrolledList() {
		updateChart();
	}

	@Override
	public void applyConfiguration() {
		updateChart();

	}

	@Override
	public void updateListViewForum() {
		updateChart();
	}

	@Override
	public JavaConnector getJavaConnector() {
		return javaConnector;
	}

	public GridPane getDateGridPane() {
		return dateGridPane;
	}

	public DatePicker getDatePickerStart() {
		return datePickerStart;
	}

	public DatePicker getDatePickerEnd() {
		return datePickerEnd;
	}

}
