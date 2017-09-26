package stateMachine;

import java.time.Instant;

public class EventOccurrence implements Comparable<EventOccurrence> {
	private EventType eventType;
	private Timestamp timeOfOccurrence;
	private boolean declaredAsFutureEvent;

	EventOccurrence(EventType eventType, Timestamp timeOfOccurrence, boolean declaredAsFutureEvent) {
		this.eventType=eventType;
		this.timeOfOccurrence=timeOfOccurrence;
		this.declaredAsFutureEvent=declaredAsFutureEvent;
	}

	/**
	 * @return the eventType
	 */
	public synchronized final EventType getEventType() {
		return eventType;
	}

	/**
	 * @return the timeOfOccurrence
	 */
	public synchronized final Timestamp getTimeOfOccurrence() {
		return timeOfOccurrence;
	}

	/**
	 * @return the intentional
	 */
	public synchronized final boolean isDeclaredAsFutureEvent() {
		return declaredAsFutureEvent;
	}

	@Override
	public int compareTo(EventOccurrence o) {
		final int n=timeOfOccurrence.compareTo(o.timeOfOccurrence);
		if (n!=0) {
			return n;
		}
		return eventType.compareTo(o.eventType);
	}
	
	
	

}
