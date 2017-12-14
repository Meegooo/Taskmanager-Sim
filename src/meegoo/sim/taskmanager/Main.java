package meegoo.sim.taskmanager;

import javafx.application.Platform;
import meegoo.sim.taskmanager.gui.Controller;
import meegoo.sim.taskmanager.gui.GUI;
import meegoo.sim.taskmanager.managers.PPSProcessManager;
import meegoo.sim.taskmanager.managers.ProcessManager;
import meegoo.sim.taskmanager.managers.RRProcessManager;

import java.util.concurrent.*;

import static javafx.application.Application.launch;

public class Main {
	public static final int quantum = 100_000; //quantum length (nanoseconds).
	public static final int refreshPeriod = 100000;
	private static final ScheduledExecutorService processorExecutor = Executors.newSingleThreadScheduledExecutor();
	private static final ExecutorService miscExecutor = Executors.newSingleThreadExecutor();
	private static ProcessManager manager;
	private static final Runnable exec = () -> manager.run();
	private static boolean processorRunning = false;
	private static ScheduledFuture<?> future;

	public static void submitProcess(ProcessEmu process) {
		if (manager == null) throw new NullPointerException("Process Manager not set");
		miscExecutor.submit(() -> manager.addProcess(process));
	}

	public static void startProcessor() {
		if (manager == null) throw new NullPointerException("Process Manager not set");
		if (processorRunning) return;
		future = processorExecutor.scheduleAtFixedRate(exec, 0, refreshPeriod, TimeUnit.NANOSECONDS);
		processorRunning = true;
	}

	public static void stopProcessor() {
		manager.pause();
		future.cancel(false);
		processorRunning = false;
	}

	public static void restartProcessor() {
		stopProcessor();
		startProcessor();
	}

	public static ProcessManager getManager() {
		return manager;
	}

	public static void setManager(ProcessManager manager) {
		Main.manager.shutdown();
		Main.manager = manager;
		restartProcessor();
	}

	public static void runLater(Runnable runnable) {
		miscExecutor.submit(runnable);
	}

	public static void postInit() {
		TableManager.init();
		if (Controller.instance.radioMenuItem_roundRobin.isSelected())
			manager = new RRProcessManager();
		else if (Controller.instance.radioMenuItem_PPS.isSelected()) {
			manager = new PPSProcessManager();
		}

		Platform.runLater(() -> {
			if (Main.getManager() instanceof RRProcessManager) {
				Controller.instance.tableColumn_priority.setVisible(false);
				Controller.instance.field_processPriority.setDisable(true);
			}
		});
	}

	public static void shutdown() {
		processorExecutor.shutdownNow();
		miscExecutor.shutdownNow();
	}

	public static void main(String[] args) {
		launch(GUI.class, args);
	}

}
