package es.ubu.lsi.ubumonitor.controllers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ubu.lsi.ubumonitor.AppInfo;
import es.ubu.lsi.ubumonitor.Style;
import es.ubu.lsi.ubumonitor.controllers.configuration.ConfigHelper;
import es.ubu.lsi.ubumonitor.controllers.configuration.ConfigurationController;
import es.ubu.lsi.ubumonitor.controllers.configuration.MainConfiguration;
import es.ubu.lsi.ubumonitor.controllers.load.Connection;
import es.ubu.lsi.ubumonitor.controllers.load.LogCreator;
import es.ubu.lsi.ubumonitor.export.CSVBuilderAbstract;
import es.ubu.lsi.ubumonitor.export.CSVExport;
import es.ubu.lsi.ubumonitor.export.dashboard.Excel;
import es.ubu.lsi.ubumonitor.export.photos.UserPhoto;
import es.ubu.lsi.ubumonitor.export.report.RankingReport;
import es.ubu.lsi.ubumonitor.model.Course;
import es.ubu.lsi.ubumonitor.model.CourseModule;
import es.ubu.lsi.ubumonitor.model.EnrolledUser;
import es.ubu.lsi.ubumonitor.model.GradeItem;
import es.ubu.lsi.ubumonitor.model.LogStats;
import es.ubu.lsi.ubumonitor.model.Logs;
import es.ubu.lsi.ubumonitor.model.datasets.DataSet;
import es.ubu.lsi.ubumonitor.model.log.FirstGroupBy;
import es.ubu.lsi.ubumonitor.model.log.GroupByAbstract;
import es.ubu.lsi.ubumonitor.model.log.TypeTimes;
import es.ubu.lsi.ubumonitor.persistence.Serialization;
import es.ubu.lsi.ubumonitor.util.Charsets;
import es.ubu.lsi.ubumonitor.util.FileUtil;
import es.ubu.lsi.ubumonitor.util.I18n;
import es.ubu.lsi.ubumonitor.util.LogAction;
import es.ubu.lsi.ubumonitor.util.UtilMethods;
import es.ubu.lsi.ubumonitor.view.chart.multi.RankingTable;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.stage.Stage;
import okhttp3.HttpUrl;

public class MenuController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MenuController.class);

	private Controller controller = Controller.getInstance();
	@FXML
	private MenuItem updateCourse;
	@FXML
	private Menu menuTheme;
	@FXML
	private ImageView userPhoto;

	private MainController mainController;

	public void init(MainController mainController) {
		this.mainController = mainController;
		updateCourse.setDisable(controller.isOfflineMode());
		initMenuBar();
		initUserPhoto();
	}

	private void initMenuBar() {

		ToggleGroup group = new ToggleGroup();
		for (Entry<String, String> entry : Style.getStyles()
				.entrySet()) {
			String key = entry.getKey();
			String path = entry.getValue();
			RadioMenuItem menuItem = new RadioMenuItem();
			menuItem.setText(key);
			menuItem.setToggleGroup(group);

			if (key.equals(ConfigHelper.getProperty("style", "Modena"))) {
				menuItem.setSelected(true);
			}
			menuItem.setOnAction(event -> {

				controller.getStage()
						.getScene()
						.getStylesheets()
						.clear();
				if (path != null) {
					controller.getStage()
							.getScene()
							.getStylesheets()
							.add(path);
				}
				ConfigHelper.setProperty("style", key);
			});
			menuTheme.getItems()
					.add(menuItem);

		}
	}

	private void initUserPhoto() {
		Image image = new Image(new ByteArrayInputStream(controller.getDataBase()
				.getUserPhoto()));
		userPhoto.setImage(image);

		ContextMenu menu = new ContextMenu();
		MenuItem user = new MenuItem(controller.getDataBase()
				.getFullName(), new ImageView(image));
		MenuItem logout = new MenuItem(I18n.get("menu.logout"));
		MenuItem exit = new MenuItem(I18n.get("menu.exit"));

		logout.setOnAction(this::logOut);
		exit.setOnAction(this::closeApplication);
		menu.getItems()
				.addAll(user, logout, exit);
		menu.setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
		menu.setAutoHide(true);

		userPhoto.setOnMouseClicked(e -> menu.show(userPhoto, e.getScreenX(), e.getScreenY()));

	}

	/**
	 * Vuelve a la ventana de login de usuario.
	 * 
	 * @param actionEvent El ActionEvent.
	 */
	public void logOut(ActionEvent actionEvent) {
		LOGGER.info("Cerrando sesión de usuario");
		Connection.clearCookies();
		changeScene(getClass().getResource("/view/Login.fxml"));
	}

	private void changeScene(URL sceneFXML) {
		changeScene(sceneFXML, null);
	}

	/**
	 * Exporta todos los gráficos a un html.
	 * 
	 * @param actionEvent El ActionEvent.
	 */
	public void updateCourse(ActionEvent actionEvent) {
		if (controller.isOfflineMode()) {
			UtilMethods.errorWindow(I18n.get("error.updateofflinemode"));
		} else {
			changeScene(getClass().getResource("/view/Welcome.fxml"), new WelcomeController(true));
		}
	}

	/**
	 * Exporta todos los datos actuales en formato CSV.
	 * 
	 * @param actionEvent evento
	 * @since 2.4.0.0
	 */
	public void exportCSV() {
		LOGGER.info("Exportando ficheros CSV");
		try {
			DirectoryChooser dir = new DirectoryChooser();
			File file = new File(ConfigHelper.getProperty("csvFolderPath", "./"));
			if (file.exists() && file.isDirectory()) {
				dir.setInitialDirectory(file);
			}

			File selectedDir = dir.showDialog(controller.getStage());
			if (selectedDir != null) {
				CSVBuilderAbstract.setPath(selectedDir.toPath());
				Charsets charset = controller.getMainConfiguration()
						.getValue(MainConfiguration.GENERAL, "charset");
				CSVExport.run(charset.get());
				UtilMethods.showExportedDir(selectedDir);
				ConfigHelper.setProperty("csvFolderPath", selectedDir.getAbsolutePath());
			}

		} catch (Exception e) {
			LOGGER.error("Error al exportar ficheros CSV.", e);
			UtilMethods.errorWindow(I18n.get("error.savecsvfiles"), e);
		}
	}

	/**
	 * Cambia a la ventana de selección de asignatura.
	 * 
	 * @param actionEvent El ActionEvent.
	 * 
	 */
	public void changeCourse(ActionEvent actionEvent) {
		LOGGER.info("Cambiando de asignatura...");
		if (controller.isOfflineMode()) {
			changeScene(getClass().getResource("/view/WelcomeOffline.fxml"), new WelcomeOfflineController());
		} else {
			changeScene(getClass().getResource("/view/Welcome.fxml"), new WelcomeController());
		}

	}

	/**
	 * Permite cambiar la ventana actual.
	 * 
	 * @param sceneFXML La ventanan a la que se quiere cambiar.
	 */
	private void changeScene(URL sceneFXML, Object controllerObject) {
		try {
			UtilMethods.changeScene(sceneFXML, controller.getStage(), controllerObject);
			controller.getStage()
					.setResizable(false);
			controller.getStage()
					.setMaximized(false);

		} catch (Exception e) {
			LOGGER.error("Error al modifcar la ventana de JavaFX: {}", e);
		}
	}

	/**
	 * Deja de seleccionar los participantes/actividades y borra el gráfico.
	 * 
	 * @param actionEvent El ActionEvent.
	 */
	public void clearSelection(ActionEvent actionEvent) {
		SelectionController selectionController = mainController.getSelectionController();
		mainController.getSelectionUserController()
				.getListParticipants()
				.getSelectionModel()
				.clearSelection();
		selectionController.getTvwGradeReport()
				.getSelectionModel()
				.clearSelection();
		selectionController.getListViewComponents()
				.getSelectionModel()
				.clearSelection();
		selectionController.getListViewEvents()
				.getSelectionModel()
				.clearSelection();
		selectionController.getListViewSection()
				.getSelectionModel()
				.clearSelection();
		selectionController.getListViewCourseModule()
				.getSelectionModel()
				.clearSelection();
		selectionController.getListViewActivity()
				.getSelectionModel()
				.clearSelection();
	}

	public void changeConfiguration() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Configuration.fxml"),
				I18n.getResourceBundle());

		Stage stage = UtilMethods.createDialog(loader, controller.getStage(), Modality.APPLICATION_MODAL);
		stage.setTitle(I18n.get("text.courseconfiguration"));
		stage.getIcons()
				.setAll(new Image("/img/gear.png"));
		ConfigurationController configurationController = loader.getController();
		configurationController.init(mainController, controller.getMainConfiguration());

		configurationController.setOnClose();

	}

	public void importConfiguration() {

		UtilMethods.fileAction(null, ConfigHelper.getProperty("configurationFolderPath", "./"), controller.getStage(),
				FileUtil.FileChooserType.OPEN, file -> {
					ConfigurationController.loadConfiguration(controller.getMainConfiguration(), file.toPath());
					changeConfiguration();
					ConfigHelper.setProperty("configurationFolderPath", file.getParent());
				}, false, FileUtil.JSON);

	}

	public void exportConfiguration() {
		Course course = controller.getActualCourse();
		UtilMethods.fileAction(getFileName(course), ConfigHelper.getProperty("configurationFolderPath", "./"),
				controller.getStage(), FileUtil.FileChooserType.SAVE, file -> {
					ConfigurationController.saveConfiguration(controller.getMainConfiguration(), file.toPath());
					ConfigHelper.setProperty("configurationFolderPath", file.getParent());
				}, FileUtil.JSON);

	}

	/**
	 * Abre en el navegador el repositorio del proyecto.
	 * 
	 * @param actionEvent El ActionEvent.
	 */
	public void aboutApp() {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AboutApp.fxml"), I18n.getResourceBundle());

		UtilMethods.createDialog(loader, controller.getStage());

	}

	public void moreInfo() {
		UtilMethods.openURL(AppInfo.LANDING_PAGE);
	}

	public void gitHubPage() {
		UtilMethods.openURL(AppInfo.GITHUB);
	}

	public void courseStats() {
		UtilMethods.createDialog(
				new FXMLLoader(getClass().getResource("/view/CourseStats.fxml"), I18n.getResourceBundle()),
				controller.getStage());
	}

	public void exportUserPhoto() {
		exportPhoto(false);
	}

	public void exportDefaultPhoto() {
		exportPhoto(true);
	}

	private void exportPhoto(boolean defaultPhoto) {
		Course course = controller.getActualCourse();
		UtilMethods.fileAction(getFileName(course), ConfigHelper.getProperty("csvFolderPath", "./"),
				controller.getStage(), FileUtil.FileChooserType.SAVE, file -> {
					UserPhoto exportUserPhoto = new UserPhoto();
					exportUserPhoto.exportEnrolledUsersPhoto(course, mainController.getSelectionUserController()
							.getListParticipants()
							.getSelectionModel()
							.getSelectedItems(), file, defaultPhoto);
					ConfigHelper.setProperty("csvFolderPath", file.getParent());
				}, FileUtil.WORD);

	}

	public void exportDashboard() {

		Course course = controller.getActualCourse();
		UtilMethods.fileAction(getFileName(course), ConfigHelper.getProperty("csvFolderPath", "./"),
				controller.getStage(), FileUtil.FileChooserType.SAVE, file -> {
					Excel excel = new Excel();
					excel.createExcel(file.getAbsolutePath());
					ConfigHelper.setProperty("csvFolderPath", file.getParent());
				}, FileUtil.EXCEL);
	}

	/**
	 * Botón "Salir". Cierra la aplicación.
	 * 
	 * @param actionEvent El ActionEvent.
	 */
	public void closeApplication(ActionEvent actionEvent) {
		LOGGER.info("Cerrando aplicación");
		controller.getStage()
				.close();
	}

	public void userGuide() {
		HttpUrl url = new HttpUrl.Builder().scheme("https")
				.host(AppInfo.USER_GUIDE)
				.addPathSegment(Locale.getDefault()
						.getLanguage())
				.addPathSegment(AppInfo.VERSION)
				.build();
		UtilMethods.openURL(url.toString());
	}

	public void exportCourse() {
		try {
			Course course = controller.getActualCourse();
			Path source = controller.getHostUserModelversionDir()
					.resolve(controller.getCourseFile(course));
			LocalDate now = LocalDate.now();
			Path destDir = controller.getHostUserModelversionArchivedDir();

			TextInputDialog textInputDialog = new TextInputDialog(
					"(" + now.format(DateTimeFormatter.ISO_DATE) + ") " + controller.getCoursePathName(course));

			textInputDialog.setTitle(AppInfo.APPLICATION_NAME_WITH_VERSION);
			textInputDialog.setHeaderText(I18n.get("text.coursename"));
			textInputDialog.setContentText(I18n.get("text.coursenametextfield"));
			textInputDialog.getEditor()
					.setMinWidth(500);

			Stage stageAlert = (Stage) textInputDialog.getDialogPane()
					.getScene()
					.getWindow();
			stageAlert.getIcons()
					.add(new Image("/img/logo_min.png"));
			Button okButton = (Button) textInputDialog.getDialogPane()
					.lookupButton(ButtonType.OK);
			okButton.addEventFilter(ActionEvent.ACTION, event -> {
				Path dest = destDir.resolve(Paths.get(textInputDialog.getEditor()
						.getText()));
				if (dest.toFile()
						.isFile()) {
					event.consume();
					ButtonType buttonType = UtilMethods.confirmationWindow("text.override");
					if (buttonType == ButtonType.OK) {
						textInputDialog.close();
						try {
							FileUtil.exportFile(source, destDir, dest);
							UtilMethods.infoWindow(I18n.get("info.courseexported"));
						} catch (IOException e) {
							throw new IllegalStateException(e);
						}

					}
				} else {
					textInputDialog.close();
					try {
						FileUtil.exportFile(source, destDir, dest);
						UtilMethods.infoWindow(I18n.get("info.courseexported"));
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}

				}
			});

			textInputDialog.showAndWait();
		} catch (Exception e) {
			UtilMethods.errorWindow("Error when archiving the course", e);
		}
	}

	public void importLogs() {
		UtilMethods.fileAction(null, ConfigHelper.getProperty("importLogsFolderPath", "./"), controller.getStage(),
				FileUtil.FileChooserType.OPEN, file -> {
					ConfigHelper.setProperty("importLogsFolderPath", file.toString());
					createServiceImportLogs(file);
				}, false, FileUtil.CSV);
	}

	public void createServiceImportLogs(File file) {
		Task<Void> task = getImportLogsWorker(file);
		task.setOnSucceeded(
				e -> UtilMethods.changeScene(getClass().getResource("/view/Main.fxml"), controller.getStage(), true));
		task.setOnFailed(e -> UtilMethods.errorWindow("Cannot import the log", e.getSource()
				.getException()));
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	private Task<Void> getImportLogsWorker(File file) {

		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				LogCreator.setDateTimeFormatter(ZoneId.systemDefault());
				Logs logs = new Logs(ZoneId.systemDefault());
				Course course = controller.getActualCourse();
				LogCreator.parserResponse(logs, new FileReader(file));
				course.setLogs(logs);
				course.setLogStats(new LogStats(logs.getList()));
				Serialization.encrypt(controller.getPassword(), controller.getHostUserModelversionDir()
						.resolve(controller.getCourseFile(course))
						.toString(), controller.getDataBase());
				return null;
			}
		};
	}

	public void exportRankingReport() {

		RankingReport rankingReport = new RankingReport();
		SelectionController selectionController = mainController.getSelectionController();
		DatePicker start = mainController.getWebViewTabsController()
				.getMultiController()
				.getDatePickerStart();
		DatePicker end = mainController.getWebViewTabsController()
				.getMultiController()
				.getDatePickerEnd();
		List<EnrolledUser> users = mainController.getSelectionUserController()
				.getSelectedUsers();
		List<GradeItem> gradeItems = selectionController.getSelectedGradeItems();
		List<CourseModule> activities = new ArrayList<>(selectionController.getListViewActivity()
				.getSelectionModel()
				.getSelectedItems());
		Course course = controller.getActualCourse();
		Map<EnrolledUser, Integer> rankingLogs = UtilMethods
				.ranking(selectionController.typeLogsAction(new LogAction<Map<EnrolledUser, Integer>>() {

					@Override
					public <E extends Serializable, T extends Serializable> Map<EnrolledUser, Integer> action(
							List<E> logType, DataSet<E> dataSet,
							Function<GroupByAbstract<?>, FirstGroupBy<E, T>> function) {
						return logType.isEmpty() ? Collections.emptyMap()
								: RankingTable.getLogsPoints(users, logType, dataSet, controller.getActualCourse()
										.getLogStats()
										.getByType(TypeTimes.DAY), start.getValue(), end.getValue());
					}
				}));
		Map<EnrolledUser, Integer> rankingGrades = gradeItems.isEmpty() ? Collections.emptyMap()
				: UtilMethods.ranking(RankingTable.getGradeItemPoints(users, gradeItems),
						DescriptiveStatistics::getMean);
		Map<EnrolledUser, Integer> rankingActivities = activities.isEmpty() ? Collections.emptyMap()
				: UtilMethods.ranking(RankingTable.getActivityCompletionPoints(users, activities, start.getValue()
						.atStartOfDay(ZoneId.systemDefault())
						.toInstant(),
						end.getValue()
								.plusDays(1)
								.atStartOfDay(ZoneId.systemDefault())
								.toInstant()));
		UtilMethods.fileAction(getFileName(course), ConfigHelper.getProperty("csvFolderPath", "./"),
				controller.getStage(), FileUtil.FileChooserType.SAVE, file -> {
					rankingReport.createReport(file, controller.getDataBase(), users, rankingLogs, rankingGrades,
							rankingActivities, selectionController.getSelectedLogTypeTransLated(), gradeItems,
							activities, start.getValue(), end.getValue(), mainController.getSelectionController()
									.getTabPaneUbuLogs()
									.getSelectionModel()
									.getSelectedItem()
									.getText());
					ConfigHelper.setProperty("csvFolderPath", file.getParent());
				}, FileUtil.WORD);

	}

	private static String getFileName(Course course) {
		return UtilMethods.removeReservedChar(course.getFullName()) + "-" + course.getId() + "-"
				+ WebViewAction.FILE_FORMATTER.format(LocalDateTime.now());
	}

}
