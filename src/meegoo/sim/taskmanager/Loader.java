package meegoo.sim.taskmanager;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import meegoo.sim.taskmanager.gui.GUI;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Loader {
	public static void load() {
		final FutureTask<File> query = new FutureTask<>(() -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open File");
			fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Text Files", "txt"));
			return fileChooser.showOpenDialog(GUI.instance.stage);
		});
		Platform.runLater(query);

		ExecutorService service = Executors.newSingleThreadExecutor();
		service.submit(() -> {
			try {
				File file = query.get();
				Scanner sc = new Scanner(file);
				while (sc.hasNext()) {
					String next = sc.nextLine();
					try {
						String[] split = next.split(";");
						ProcessEmu processEmu = new ProcessEmu(split[0], Long.parseLong(split[1]) * 1000000, Integer.parseInt(split[2]));
						Main.getManager().addProcess(processEmu);
					} catch (Exception e) {
						System.err.println("Line " + next + " could not be parsed. Skipping");
						e.printStackTrace();
					}
				}
			} catch (InterruptedException | ExecutionException | FileNotFoundException e) {
				e.printStackTrace();
			}
		});
	}
}
