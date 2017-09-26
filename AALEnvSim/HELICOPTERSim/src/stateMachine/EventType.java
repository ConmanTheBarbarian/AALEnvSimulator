package stateMachine;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

public class EventType extends NamedObjectInStateMachineSystem implements Evaluation {
	
	public class StateMachineSubscription implements Comparable<StateMachineSubscription> {
		private int count;
		private Priority priority;
		private StateMachine stateMachine;
		private boolean declaredAsFutureEvent;
		StateMachineSubscription(StateMachine stateMachine, Priority priority, boolean declaredAsFutureEvent) {
			this.stateMachine=stateMachine;
			this.count=0;
			this.priority=priority;
		}
		@Override
		public int compareTo(StateMachineSubscription arg0) {
			return this.stateMachine.compareTo(arg0.stateMachine);
		}
		final void decreaseCount() {
			if (this.count>0) {
				--this.count;
			}
		}
		int getCount() {
			return count;
		}
		/**
		 * @return the priority
		 */
		public synchronized final Priority getPriority() {
			return priority;
		}
		public final StateMachine getStateMachine() {
			return stateMachine;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return stateMachine.hashCode();
		}
		final void increaseCount() {
			++this.count;			
		}
		/**
		 * @param priority the priority to set
		 */
		public synchronized final void setPriority(Priority priority) {
			this.priority = priority;
		}
		/**
		 * @return the declaredAsFutureEvent
		 */
		public synchronized final boolean isDeclaredAsFutureEvent() {
			return declaredAsFutureEvent;
		}
		
		
		
	}

	private Priority priority;
	protected HashMap<StateMachine,Priority> sm2p=new HashMap<StateMachine,Priority>();
	protected TreeMap<Priority,TreeMap<Boolean,HashMap<StateMachine,StateMachineSubscription>>> subscriberMap=new TreeMap<Priority,TreeMap<Boolean,HashMap<StateMachine,StateMachineSubscription>>>();

	/**
	 * @param name
	 * @param sms
	 * @param priority2 TODO
	 */
	public EventType(String name, StateMachineSystem sms, Priority priority2) {
		super(name, sms);
		sms.addEvent(this);
		this.priority=priority2;
	}
	@Override
	public boolean evaluate() {
		throw new IllegalArgumentException("You must call evaluate(StateMachine) on Condition objects");
	}
	@Override
	public boolean evaluate(EventOccurrence eventOccurrence) {
		throw new IllegalArgumentException("You must call evaluate(StateMachine) on Condition objects");
	}

	@Override
	public boolean evaluate(final StateMachine sm) {
		return true;
	}
	@Override
	public boolean evaluate(StateMachine sm, EventOccurrence eventOccurrence) {
		return eventOccurrence.getEventType().equals(this);
	}
	public EventOccurrence generateEventOccurrence(Timestamp timeOfOccurrence, boolean intentional) {
		final EventOccurrence eventOccurrence=new EventOccurrence(this,timeOfOccurrence,intentional);
		return eventOccurrence;
	}
	public Priority getPriority() {
		return priority;
	}
	synchronized final Collection<StateMachineSubscription> getSubscriberSet() {
		Vector<StateMachineSubscription> result=new Vector<StateMachineSubscription>();
		for (Priority p: subscriberMap.keySet()) {
			for (int i=0; i<2; ++i) {
				final boolean dafe=(i%2==0);
				for (StateMachineSubscription sms:subscriberMap.get(p).get(dafe).values()) {
					result.add(sms);
				}
			}
		}
		return result;
	}
	
	public synchronized  void subscribe(final StateMachine stateMachine, Priority priority, boolean declaredAsFutureEvent) {
		final Priority registeredPriority=sm2p.get(stateMachine);
		if (registeredPriority!=null && registeredPriority.compareTo(priority)!=0) {
			throw new IllegalArgumentException("State machine "+stateMachine.getName()+" is already assigned to event at a different priority");
		}
		TreeMap<Boolean,HashMap<StateMachine,StateMachineSubscription>> b2sm2sms=subscriberMap.get(priority);
		if (b2sm2sms==null) {
			b2sm2sms=new TreeMap<Boolean,HashMap<StateMachine,StateMachineSubscription>>();
			subscriberMap.put(priority,b2sm2sms);
		}
		
		HashMap<StateMachine,StateMachineSubscription> sm2sms=b2sm2sms.get(declaredAsFutureEvent);
		if (sm2sms==null) {
			sm2sms=new HashMap<StateMachine,StateMachineSubscription>();
			b2sm2sms.put(declaredAsFutureEvent, sm2sms);
		}
		StateMachineSubscription smsu=sm2sms.get(stateMachine);
		if (smsu==null) {
			smsu=new StateMachineSubscription(stateMachine, priority, declaredAsFutureEvent);
			sm2sms.put(stateMachine,smsu);
			sm2p.put(stateMachine, priority);
		}
		smsu.increaseCount();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Event [getName()=" + getName() + "]";
	}
	public synchronized  void unsubscribe(final StateMachine stateMachine) {
		final Priority priority=sm2p.get(stateMachine);
		if (priority==null) {
			throw new IllegalArgumentException("No such subscription for "+stateMachine);
		}
		TreeMap<Boolean,HashMap<StateMachine,StateMachineSubscription>> b2sm2sms=subscriberMap.get(priority);
		for (boolean b:b2sm2sms.keySet()) {
			HashMap<StateMachine,StateMachineSubscription> sm2sms=b2sm2sms.get(b);
			if (sm2sms==null) {
				throw new IllegalStateException("If priority exists, then there must be a subscription");
			}
			StateMachineSubscription smsu=sm2sms.get(stateMachine);
			if (smsu!=null) {
				smsu.decreaseCount();
				if (smsu.getCount()==0) {
					subscriberMap.remove(stateMachine);	
					//sm2p.remove(stateMachine);
				}
			}
		}
	}
	

}
