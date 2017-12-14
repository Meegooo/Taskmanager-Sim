package meegoo.sim.taskmanager;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TableView;
import meegoo.sim.taskmanager.gui.Controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TableManager {
	private static final TableView<ProcessEmu> table = Controller.instance.table_tasks;
	private static final ScheduledExecutorService tableUpdaterExecutor = Executors.newSingleThreadScheduledExecutor();
	private static Map<Integer, Long> cpuStats;
	private static final Runnable tableUpdater = () -> Platform.runLater(TableManager::updateTable);
	private static ScheduledFuture<?> future;

	private static IntegerProperty tableRefreshPeriod = new SimpleIntegerProperty(500); //milliseconds


	public static int getTableRefreshPeriod() {
		return tableRefreshPeriod.get();
	}

	public static void setTableRefreshPeriod(int tableRefreshPeriod) {
		TableManager.tableRefreshPeriod.set(tableRefreshPeriod);
	}

	private static void updateTable() {
		if (Main.getManager() == null) return;
		cpuStats = Main.getManager().getCpuStats();
		List<ProcessEmu> processes = Main.getManager().exportProcesses();

		Set<ProcessEmu> currentList = new HashSet<>(table.getItems());

		for (ProcessEmu process : processes) {
			if (!currentList.contains(process))
				table.getItems().add(process);
			else
				currentList.remove(process);

		}
		for (ProcessEmu process : currentList) {
			table.getItems().remove(process);
		}
		table.refresh();
		table.sort();
	}

	public static void init() {
		future = tableUpdaterExecutor.scheduleAtFixedRate(tableUpdater, 0, tableRefreshPeriod.get(), TimeUnit.MILLISECONDS);
		tableRefreshPeriod.addListener((observable, oldValue, newValue) -> {
			future.cancel(false);
			future = tableUpdaterExecutor.scheduleAtFixedRate(tableUpdater, 0, tableRefreshPeriod.get(), TimeUnit.MILLISECONDS);

		});
	}

	public static void shutdown() {
		tableUpdaterExecutor.shutdownNow();
	}

	public static Map<Integer, Long> getCpuStats() {
		return cpuStats;
	}

}
