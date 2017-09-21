package stateMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

public class EventType extends NamedObjectInStateMachineSystem implements Evaluation {
	
	public class StateMachineSubscription implements Comparable<StateMachineSubscription> {
		private int count;
		private StateMachine stateMachine;
		private Priority priority;
		StateMachineSubscription(StateMachine stateMachine, Priority priority) {
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
		 * @return the priority
		 */
		public synchronized final Priority getPriority() {
			return priority;
		}
		/**
		 * @param priority the priority to set
		 */
		public synchronized final void setPriority(Priority priority) {
			this.priority = priority;
		}
		
		
	}

	protected TreeMap<Priority,HashMap<StateMachine,StateMachineSubscription>> subscriberMap=new TreeMap<Priority,HashMap<StateMachine,StateMachineSubscription>>();
	protected HashMap<StateMachine,Priority> sm2p=new HashMap<StateMachine,Priority>();
	private Priority priority;

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
	public boolean evaluate(final StateMachine sm) {
		return true;
	}
	synchronized final Collection<StateMachineSubscription> getSubscriberSet() {
		Vector<StateMachineSubscription> result=new Vector<StateMachineSubscription>();
		for (Priority p: subscriberMap.keySet()) {
			for (StateMachineSubscription sms:subscriberMap.get(p).values()) {
				result.add(sms);
			}
		}
		return result;
	}

	public synchronized  void subscribe(final StateMachine stateMachine, Priority priority) {
		final Priority registeredPriority=sm2p.get(stateMachine);
		if (registeredPriority!=null && registeredPriority.compareTo(priority)!=0) {
			throw new IllegalArgumentException("State machine "+stateMachine.getName()+" is already assigned to event at a different priority");
		}
		
		HashMap<StateMachine,StateMachineSubscription> sm2sms=subscriberMap.get(priority);
		if (sm2sms==null) {
			sm2sms=new HashMap<StateMachine,StateMachineSubscription>();
			subscriberMap.put(priority, sm2sms);
		}
		StateMachineSubscription smsu=sm2sms.get(stateMachine);
		if (smsu==null) {
			smsu=new StateMachineSubscription(stateMachine, priority);
			sm2sms.put(stateMachine,smsu);
			sm2p.put(stateMachine, priority);
		}
		smsu.increaseCount();
	}
	public synchronized  void unsubscribe(final StateMachine stateMachine) {
		final Priority priority=sm2p.get(stateMachine);
		if (priority==null) {
			throw new IllegalArgumentException("No such subscription for "+stateMachine);
		}
		HashMap<StateMachine,StateMachineSubscription> sm2sms=subscriberMap.get(priority);
		if (sm2sms==null) {
			throw new IllegalStateException("If priority exists, then there must be a subscription");
		}
		StateMachineSubscription smsu=sm2sms.get(stateMachine);
		if (smsu!=null) {
			smsu.decreaseCount();
			if (smsu.getCount()==0) {
				subscriberMap.remove(stateMachine);	
				sm2p.remove(stateMachine);
			}
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Event [getName()=" + getName() + "]";
	}
	@Override
	public boolean evaluate() {
		throw new IllegalArgumentException("You must call evaluate(StateMachine) on Condition objects");
	}
	public Priority getPriority() {
		return priority;
	}
	

}
