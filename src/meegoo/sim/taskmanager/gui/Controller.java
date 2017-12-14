package meegoo.sim.taskmanager.gui;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import meegoo.sim.taskmanager.Loader;
import meegoo.sim.taskmanager.Main;
import meegoo.sim.taskmanager.ProcessEmu;
import meegoo.sim.taskmanager.TableManager;
import meegoo.sim.taskmanager.managers.PPSProcessManager;
import meegoo.sim.taskmanager.managers.RRProcessManager;

import java.text.DecimalFormat;

public class Controller {
	public static Controller instance;

	@FXML
	public Button button_createProcess;

	@FXML
	public ToggleGroup toggleGroup_processManager;
	@FXML
	public RadioMenuItem radioMenuItem_roundRobin;
	@FXML
	public RadioMenuItem radioMenuItem_PPS;

	@FXML
	public ToggleGroup toggleGroup_refreshRate;
	@FXML
	public RadioMenuItem radioMenuItem_010seconds;
	@FXML
	public RadioMenuItem radioMenuItem_025seconds;
	@FXML
	public RadioMenuItem radioMenuItem_050seconds;
	@FXML
	public RadioMenuItem radioMenuItem_100seconds;
	@FXML
	public RadioMenuItem radioMenuItem_200seconds;
	@FXML
	public RadioMenuItem radioMenuItem_500seconds;

	@FXML
	public TextField field_processName;
	@FXML
	public TextField field_processTime;
	@FXML
	public TextField field_processPriority;
	@FXML
	public Label label_processPriority;

	@FXML
	public TableColumn<ProcessEmu, Integer> tableColumn_id;
	@FXML
	public TableColumn<ProcessEmu, String> tableColumn_name;
	@FXML
	public TableColumn<ProcessEmu, String> tableColumn_cpu;
	@FXML
	public TableColumn<ProcessEmu, String> tableColumn_remaining;
	@FXML
	public TableColumn<ProcessEmu, ProcessEmu.State> tableColumn_state;
	@FXML
	public TableColumn<ProcessEmu, Integer> tableColumn_priority;
	@FXML
	public TableView<ProcessEmu> table_tasks;

	@FXML
	public MenuItem menuItem_start;
	@FXML
	public MenuItem menuItem_pause;
	@FXML
	public MenuItem menuItem_load;


	public Controller() {
		instance = this;
	}

	@FXML
	public void onRadioMenuItem100() {
		TableManager.setTableRefreshPeriod(100);
	}

	@FXML
	public void onRadioMenuItem250() {
		TableManager.setTableRefreshPeriod(250);
	}

	@FXML
	public void onRadioMenuItem500() {
		TableManager.setTableRefreshPeriod(500);
	}

	@FXML
	public void onRadioMenuItem1000() {
		TableManager.setTableRefreshPeriod(1000);
	}

	@FXML
	public void onRadioMenuItem2000() {
		TableManager.setTableRefreshPeriod(2000);
	}

	@FXML
	public void onRadioMenuItem5000() {
		TableManager.setTableRefreshPeriod(5000);
	}

	@FXML
	public void onRadioMenuItemRR() {
		tableColumn_priority.setVisible(false);
		field_processPriority.setDisable(true);
		Main.runLater(() -> Main.setManager(new RRProcessManager()));
		menuItem_pause.fire();
	}

	@FXML
	public void onRadioMenuItemPPS() {
		tableColumn_priority.setVisible(true);
		field_processPriority.setDisable(false);
		Main.runLater(() -> Main.setManager(new PPSProcessManager()));
		menuItem_pause.fire();
	}

	@FXML
	public void onButtonCreateProcess() {
		long time = Long.parseLong(field_processTime.getText());
		try {
			int priority = Integer.parseInt(field_processPriority.getText());
			ProcessEmu process = new ProcessEmu(field_processName.getText(), time * 1000000, priority);
			Main.submitProcess(process);
		} catch (NumberFormatException e) {
			ProcessEmu process = new ProcessEmu(field_processName.getText(), time * 1000000, 0);
			Main.submitProcess(process);
		}
	}

	@FXML
	public void onMenuItemPause() {
		menuItem_pause.setVisible(false);
		menuItem_start.setVisible(true);
		Main.runLater(Main::stopProcessor);
	}

	@FXML
	public void onMenuItemStart() {
		menuItem_pause.setVisible(true);
		menuItem_start.setVisible(false);
		Main.runLater(Main::startProcessor);
	}

	@FXML
	public void onMenuItemLoad() {
		Loader.load();
	}

	@FXML
	public void onMenuItemClear() {
		Main.getManager().clear();
	}

	public void postInit() {
		field_processTime.setTextFormatter(GUI.createIntegerFormatter(18));
		field_processPriority.setTextFormatter(GUI.createIntegerFormatter(9));
		tableColumn_id.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumn_name.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumn_cpu.setCellValueFactory(param -> {
			long time = TableManager.getCpuStats().getOrDefault(param.getValue().getId(), 0L);
			double percentage = (double) time / 1000000 / TableManager.getTableRefreshPeriod() * 100;
			if (percentage > 100) percentage = 100;
			return new SimpleObjectProperty<>(String.format("%.1f%%", percentage));
		});
		tableColumn_remaining.setCellValueFactory(param -> {
			DecimalFormat formatter = new DecimalFormat("#,###");
			return new SimpleObjectProperty<>(formatter.format(param.getValue().getRemainingTimeNanos()) + " ns");
		});
		tableColumn_state.setCellValueFactory(new PropertyValueFactory<>("state"));
		tableColumn_priority.setCellValueFactory(new PropertyValueFactory<>("priority"));

		tableColumn_remaining.setComparator((o1, o2) -> {
			long value1 = Long.parseLong(o1.replace(" ns", "").replace(",", "")
					.replace(" ", ""));
			long value2 = Long.parseLong(o2.replace(" ns", "").replace(",", "")
					.replace(" ", ""));
			return Long.compare(value1, value2);
		});

		//Context menu on rows
		ContextMenu cm = new ContextMenu();
		MenuItem menuItem_remove = new MenuItem("Remove");
		cm.getItems().add(menuItem_remove);

		MenuItem menuItem_stop = new MenuItem("Stop");
		menuItem_stop.setOnAction(event -> Main.runLater(() -> {
			ProcessEmu item = table_tasks.getSelectionModel().getSelectedItem();
			if (item != null)
				Main.getManager().removeProcess(item);
		}));
		ContextMenu menu = new ContextMenu();
		menu.getItems().add(menuItem_stop);
		table_tasks.setContextMenu(menu);
	}


}
