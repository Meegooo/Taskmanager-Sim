package meegoo.sim.taskmanager.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import meegoo.sim.taskmanager.Main;
import meegoo.sim.taskmanager.TableManager;

import java.text.DecimalFormat;
import java.text.ParsePosition;

public class GUI extends Application {
	public static GUI instance;
	public Stage stage;

	public static TextFormatter<Object> createIntegerFormatter(int maxLength) {
		DecimalFormat format = new DecimalFormat("#,###");
		return new TextFormatter<>(c -> {
			if (c.getControlNewText().isEmpty()) return c;
			if (c.getControlNewText().length() > maxLength) return null;
			ParsePosition parsePosition = new ParsePosition(0);
			Object object = format.parse(c.getControlNewText(), parsePosition);
			if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) return null;
			else return c;
		});
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		instance = this;
		FXMLLoader loader = new FXMLLoader();
		Parent mainRoot = loader.load(getClass().getResource("main.fxml").openStream());
		primaryStage.setTitle("Stage");
		primaryStage.setScene(new Scene(mainRoot, 800, 600));
		primaryStage.show();
		Main.runLater(Main::postInit);
		Controller.instance.postInit();
		primaryStage.setOnCloseRequest(event -> {
			event.consume();
			TableManager.shutdown();
			Main.shutdown();
			Platform.exit();
		});
	}

}
