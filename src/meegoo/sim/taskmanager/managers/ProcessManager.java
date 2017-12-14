package meegoo.sim.taskmanager.managers;

import meegoo.sim.taskmanager.ProcessEmu;

import java.util.List;
import java.util.Map;

public interface ProcessManager {
	void addProcess(ProcessEmu process);

	void removeProcess(int id);

	void removeProcess(ProcessEmu process);

	Map<Integer, Long> getCpuStats();

	void run();

	void pause();

	List<ProcessEmu> exportProcesses();

	void shutdown();

	void clear();

}
