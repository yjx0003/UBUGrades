package es.ubu.lsi.controllers.charts;

import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import es.ubu.lsi.controllers.Controller;
import es.ubu.lsi.controllers.I18n;
import es.ubu.lsi.controllers.MainController;
import es.ubu.lsi.controllers.configuration.MainConfiguration;
import es.ubu.lsi.model.EnrolledUser;
import es.ubu.lsi.model.GradeItem;
import es.ubu.lsi.model.Group;
import es.ubu.lsi.util.UtilMethods;

public class BoxPlot extends ChartjsGradeItem {

	public BoxPlot(MainController mainController) {
		super(mainController, ChartType.BOXPLOT);
	}

	@Override
	public String createDataset(List<EnrolledUser> selectedUser, List<GradeItem> selectedGradeItems) {
		StringBuilder stringBuilder = new StringBuilder();
		MainConfiguration mainConfiguration = Controller.getInstance().getMainConfiguration();
		stringBuilder.append("{labels:[");
		stringBuilder.append(UtilMethods.joinWithQuotes(selectedGradeItems));
		stringBuilder.append("],datasets:[");
		if (!selectedUser.isEmpty()) {
			createData(selectedUser, selectedGradeItems, stringBuilder, I18n.get("text.selectedUsers"), false);

		}
		if (useGeneralButton) {
			createData(Controller.getInstance().getActualCourse().getEnrolledUsers(), selectedGradeItems, stringBuilder,
					I18n.get("text.all"), !(boolean) mainConfiguration.getValue(MainConfiguration.GENERAL, "generalActive"));
		}
		if (useGroupButton) {
			for (Group group : slcGroup.getCheckModel().getCheckedItems()) {
				if (group != null) {
					createData(group.getEnrolledUsers(), selectedGradeItems, stringBuilder, group.getGroupName(),
							!(boolean) mainConfiguration.getValue(MainConfiguration.GENERAL, "groupActive"));
				}

			}

		}

		stringBuilder.append("]}");

		return stringBuilder.toString();
	}

	private void createData(Collection<EnrolledUser> selectedUser, List<GradeItem> selectedGradeItems,
			StringBuilder stringBuilder, String text, boolean hidden) {
		stringBuilder.append("{label:'" + text + "',");
		stringBuilder.append("borderColor:" + rgba(text, 0.7) + ",");
		stringBuilder.append("backgroundColor:" + rgba(text, OPACITY) + ",");

		stringBuilder.append("padding: 10,");
		stringBuilder.append("itemRadius: 2,");
		stringBuilder.append("itemStyle: 'circle',");
		stringBuilder.append("itemBackgroundColor:" + hex(text) + ",");
		stringBuilder.append("outlierColor:" + hex(text) + ",");
		stringBuilder.append("borderWidth: 1,");
		stringBuilder.append("outlierRadius : 5,");
		stringBuilder.append("hidden:" + hidden + ",");
		stringBuilder.append("data:[");

		for (GradeItem gradeItem : selectedGradeItems) {
			stringBuilder.append("[");
			for (EnrolledUser user : selectedUser) {
				double grade = gradeItem.getEnrolledUserPercentage(user);
				if (!Double.isNaN(grade))
					stringBuilder.append(adjustTo10(grade) + ",");
			}
			stringBuilder.append("],");
		}
		stringBuilder.append("]},");
	}

	@Override
	public int onClick(int index) {
		return -1; // do nothing at the moment
	}

	@Override
	public String getOptions() {
		StringJoiner jsObject = getDefaultOptions();
		MainConfiguration mainConfiguration = Controller.getInstance().getMainConfiguration();
		boolean useHorizontal = mainConfiguration.getValue(getChartType(), "horizontalMode");
		int tooltipDecimals = mainConfiguration.getValue(getChartType(), "tooltipDecimals");
		addKeyValueWithQuote(jsObject, "typeGraph", useHorizontal ? "horizontalBoxplot" : "boxplot");
		addKeyValue(jsObject, "tooltipDecimals", tooltipDecimals);
		String xLabel = useHorizontal ? getYScaleLabel() :getXScaleLabel();
		String yLabel = useHorizontal ? getXScaleLabel() : getYScaleLabel();
		addKeyValue(jsObject, "scales", "{yAxes:[{" + yLabel + "}],xAxes:[{" + xLabel + "}]}");
		return jsObject.toString();
	}

}
