package es.ubu.lsi.ubumonitor.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.ubu.lsi.ubumonitor.controllers.configuration.Config;
import es.ubu.lsi.ubumonitor.util.UtilMethods;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Clase Loader. Inicializa la ventana de login
 * 
 * @author Félix Nogal Santamaría
 * @author Yi Peng
 * @version 1.0
 *
 */
public class Loader extends Application {

	private static final Logger LOGGER = LoggerFactory.getLogger(Loader.class);
	private Controller controller = Controller.getInstance();

	@Override
	public void start(Stage primaryStage) {

		try {
			controller.initialize();
			
			LOGGER.info("[Bienvenido a {}]", AppInfo.APPLICATION_NAME_WITH_VERSION);
			primaryStage.getIcons().add(new Image("/img/logo_min.png"));
			primaryStage.setTitle(AppInfo.APPLICATION_NAME_WITH_VERSION);
			primaryStage.setResizable(false);
			UtilMethods.changeScene(getClass().getResource("/view/Login.fxml"), primaryStage);
			Style.addStyle(Config.getProperty("style"), primaryStage.getScene().getStylesheets());
			controller.setStage(primaryStage);
			
		} catch (Exception e) {
			LOGGER.error("Error al iniciar controller: {}", e);
		}
	}

	@Override
	public void stop() {

		Config.save();
	}

	public static void initialize() {
		launch();
	}
}