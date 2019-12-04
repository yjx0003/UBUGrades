package controllers.charts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import controllers.JavaConnector.ChartType;
import controllers.MainController;
import controllers.datasets.DataSetComponent;
import controllers.datasets.DataSetComponentEvent;
import controllers.datasets.DataSetSection;
import controllers.datasets.DatasSetCourseModule;
import controllers.datasets.StackedBarDataSet;
import model.Component;
import model.ComponentEvent;
import model.CourseModule;
import model.EnrolledUser;
import model.Section;

public class Stackedbar extends Chartjs {
	private static final Logger LOGGER = LoggerFactory.getLogger(Stackedbar.class);

	private StackedBarDataSet<Component> stackedBarComponent = new StackedBarDataSet<>();
	private StackedBarDataSet<ComponentEvent> stackedBarEvent = new StackedBarDataSet<>();
	private StackedBarDataSet<Section> stackedBarSection = new StackedBarDataSet<>();
	private StackedBarDataSet<CourseModule> stackedBarCourseModule = new StackedBarDataSet<>();

	public Stackedbar(MainController mainController) {
		super(mainController, ChartType.STACKED_BAR);

	}

	@Override
	public void update() {
		String stackedbardataset = null;
		List<EnrolledUser> selectedUsers = new ArrayList<>(listParticipants.getSelectionModel().getSelectedItems());
		List<EnrolledUser> enrolledUsers = new ArrayList<>(listParticipants.getItems());
		selectedUsers.removeAll(Collections.singletonList(null));

		LocalDate dateStart = datePickerStart.getValue();
		LocalDate dateEnd = datePickerEnd.getValue();

		if (tabUbuLogsComponent.isSelected()) {

			stackedbardataset = stackedBarComponent.createData(enrolledUsers, selectedUsers,
					listViewComponents.getSelectionModel().getSelectedItems(), choiceBoxDate.getValue(), dateStart,
					dateEnd, DataSetComponent.getInstance());

		} else if (tabUbuLogsEvent.isSelected()) {
			stackedbardataset = stackedBarEvent.createData(enrolledUsers, selectedUsers,
					listViewEvents.getSelectionModel().getSelectedItems(), choiceBoxDate.getValue(), dateStart, dateEnd,
					DataSetComponentEvent.getInstance());
		} else if (tabUbuLogsSection.isSelected()) {
			stackedbardataset = stackedBarSection.createData(enrolledUsers, selectedUsers,
					listViewSection.getSelectionModel().getSelectedItems(), choiceBoxDate.getValue(), dateStart,
					dateEnd, DataSetSection.getInstance());
		} else if (tabUbuLogsCourseModule.isSelected()) {
			stackedbardataset = stackedBarCourseModule.createData(enrolledUsers, selectedUsers,
					listViewCourseModule.getSelectionModel().getSelectedItems(), choiceBoxDate.getValue(), dateStart,
					dateEnd, DatasSetCourseModule.getInstance());
		}

		LOGGER.info("Dataset para el stacked bar en JS: {}", stackedbardataset);

		webViewChartsEngine.executeScript(
				String.format("updateStackedChart(%s)", stackedbardataset));

	}



}