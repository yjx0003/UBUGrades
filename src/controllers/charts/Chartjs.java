package controllers.charts;

import controllers.MainController;

public abstract class Chartjs extends Chart{
	
	
	public Chartjs(MainController mainController, ChartType chartType, Tabs tabName) {
		super(mainController, chartType, tabName);
		
		
	}

	@Override
	public void clear() {
		webViewChartsEngine.executeScript("clearChartjs()");

	}

	@Override
	public void hideLegend() {
		webViewChartsEngine.executeScript("hideLegendChartjs()");
		
	}
	
	@Override
	public String export() {
		return (String) webViewChartsEngine.executeScript("exportChartjs()");
	}
	
	

}
