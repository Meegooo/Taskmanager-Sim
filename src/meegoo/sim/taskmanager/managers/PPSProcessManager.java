package meegoo.sim.taskmanager.managers;

import meegoo.sim.taskmanager.Main;
import meegoo.sim.taskmanager.ProcessEmu;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class PPSProcessManager implements ProcessManager {

	private Queue<ProcessEmu> queue = new PriorityBlockingQueue<>(1, (o1, o2) -> {
		if (o1.getPriority() == o2.getPriority())
			return Long.compare(o1.getCreatedAt(), o2.getCreatedAt());
		else return o2.getPriority() - o1.getPriority();
	});
	private Map<Integer, ProcessEmu> history = new HashMap<>();
	private Map<Integer, Long> cpuStats = new HashMap<>();
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
		if (running.getId() == id) stopCurrent = true;
		else {
			ProcessEmu process = history.get(id);
			process.changeState(ProcessEmu.State.KILLED);
			queue.remove(process);
		}
	}

	@Override
	public void removeProcess(ProcessEmu process) {
		if (running == process) stopCurrent = true;
		else {
			process.changeState(ProcessEmu.State.KILLED);
			queue.remove(process);
		}
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
			stopCurrent = false;
		}

		try {
			if (queue.isEmpty() && running == null) return;

			if (pausedAt != 0) {
				startedRunningAt += System.nanoTime() - pausedAt;
				pausedAt = 0;
			}
			long timePassed = System.nanoTime() - startedRunningAt;

			if (running == null) { //start new process
				running = queue.poll();
				running.changeState(ProcessEmu.State.RUNNING);
				startedRunningAt = System.nanoTime();
			} else if (timePassed > Main.quantum) { //quantum ended
				cpuStats.put(running.getId(), cpuStats.getOrDefault(running.getId(), 0L) + timePassed);

				//check if we need to change running process
				if (running.ranFor(timePassed)) { //current process ended
					running = queue.poll();
					if (running != null)
						running.changeState(ProcessEmu.State.RUNNING);

				} else if (!queue.isEmpty() && queue.peek().getPriority() > running.getPriority()) {//process with higher priority is in queue
					queue.add(running);
					running.changeState(ProcessEmu.State.WAITING);
					running = queue.poll();
					running.changeState(ProcessEmu.State.RUNNING);
				}

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
		LinkedList<ProcessEmu> export = new LinkedList<>(queue);
		if (running != null)
			export.addFirst(running);
		return export;
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
