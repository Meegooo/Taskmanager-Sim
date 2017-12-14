package meegoo.sim.taskmanager;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

public class ProcessEmu {

	private static int lastProcess = 0;
	private int id;
	private long time;
	private SimpleLongProperty remainingTimeNanos;
	private int priority;
	private String name;
	private SimpleObjectProperty<State> state;
	private long createdAt;

	public ProcessEmu(String name, long time, int priority) {
		this.time = time;
		this.remainingTimeNanos = new SimpleLongProperty(time);
		this.priority = priority;
		this.name = name;
		this.id = lastProcess++;
		createdAt = System.nanoTime();
		state = new SimpleObjectProperty<>(State.CREATED);
	}

	public boolean ranFor(long timeNanos) {
		remainingTimeNanos.set(remainingTimeNanos.get() - timeNanos);
		if (remainingTimeNanos.get() < 0) {
			state.set(State.FINISHED);
			return true;
		} else return false;

	}

	public void changeState(State state) {
		this.state.set(state);
	}

	public int getId() {
		return id;
	}

	public long getTime() {
		return time;
	}

	public long getRemainingTimeNanos() {
		return remainingTimeNanos.get();
	}

	public SimpleLongProperty remainingTimeNanosProperty() {
		return remainingTimeNanos;
	}

	public int getPriority() {
		return priority;
	}

	public String getName() {
		return name;
	}

	public State getState() {
		return state.get();
	}

	public SimpleObjectProperty<State> stateProperty() {
		return state;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ProcessEmu that = (ProcessEmu) o;

		return id == that.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	public enum State {
		CREATED, RUNNING, WAITING, FINISHED, KILLED
	}
}
