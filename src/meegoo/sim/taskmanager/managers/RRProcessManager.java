package meegoo.sim.taskmanager.managers;

import meegoo.sim.taskmanager.Main;
import meegoo.sim.taskmanager.ProcessEmu;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class RRProcessManager implements ProcessManager {
	private Map<Integer, Long> cpuStats = new HashMap<>();
	private Queue<ProcessEmu> queue = new LinkedBlockingQueue<>();
	private Map<Integer, ProcessEmu> history = new HashMap<>();
	private ProcessEmu running = null;
	private long startedRunningAt;
	private long pausedAt = 0;

	private boolean shutdown = false;
	private boolean stopCurrent = false;

	@Override
	public void addProcess(ProcessEmu process) {
		process.changeState(ProcessEmu.State.WAITING);
		queue.add(process);
		history.put(process.getId(), process);
	}

	@Override
	public void removeProcess(int id) {
		ProcessEmu process = history.get(id);
		if (running == process)
			stopCurrent = true;
		else
			queue.remove(process);
	}


	@Override
	public void removeProcess(ProcessEmu process) {
		if (running == process)
			stopCurrent = true;
		else
			queue.remove(process);

	}

	@Override
	public Map<Integer, Long> getCpuStats() {
		Map<Integer, Long> temp = cpuStats;
		cpuStats = new HashMap<>();
		return temp;
	}

	@Override
	public void run() {
		if (shutdown) {
			clear();
			return;
		}

		if (stopCurrent) {
			if (running != null) {
				running.changeState(ProcessEmu.State.KILLED);
				running = null;
			}
			queue.poll();
			stopCurrent = false;
		}

		try {
			if (queue.isEmpty()) return;

			if (pausedAt != 0) {
				startedRunningAt += System.nanoTime() - pausedAt;
				pausedAt = 0;
			}
			long timePassed = System.nanoTime() - startedRunningAt;

			if (running == null) { //start new process
				running = queue.peek();
				running.changeState(ProcessEmu.State.RUNNING);
				startedRunningAt = System.nanoTime();
			} else if (timePassed > Main.quantum) { //quantum ended.
				cpuStats.put(running.getId(), cpuStats.getOrDefault(running.getId(), 0L) + timePassed);
				queue.poll();
				if (!running.ranFor(timePassed)) {
					queue.add(running);
					running.changeState(ProcessEmu.State.WAITING);
				}
				running = queue.peek();
				if (running != null)
					running.changeState(ProcessEmu.State.RUNNING);
				startedRunningAt = System.nanoTime();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pause() {
		pausedAt = System.nanoTime();
	}

	@Override
	public List<ProcessEmu> exportProcesses() {
		return new ArrayList<>(queue);
	}

	@Override
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void clear() {
		queue.clear();
		cpuStats.clear();
		running = null;
	}
}
