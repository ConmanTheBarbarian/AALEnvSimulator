package stateMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Event extends NamedObjectInStateMachineSystem implements Evaluation {
	
	public class StateMachineSubscription implements Comparable<StateMachineSubscription> {
		private int count;
		private StateMachine stateMachine;
		StateMachineSubscription(StateMachine stateMachine) {
			this.stateMachine=stateMachine;
			this.count=0;
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
		
	}

	protected HashMap<StateMachine,StateMachineSubscription> subscriberMap=new HashMap<StateMachine,StateMachineSubscription>();
	private Priority priority;

	/**
	 * @param name
	 * @param sms
	 * @param priority TODO
	 */
	public Event(String name, StateMachineSystem sms, Priority priority) {
		super(name, sms);
		sms.addEvent(this);
		this.priority=priority;
	}
	@Override
	public boolean evaluate(final StateMachine sm) {
		return true;
	}
	synchronized final Collection<StateMachineSubscription> getSubscriberSet() {
		return subscriberMap.values();
	}

	public synchronized  void subscribe(final StateMachine stateMachine) {
		
		StateMachineSubscription smsu=subscriberMap.get(stateMachine);
		if (smsu==null) {
			smsu=new StateMachineSubscription(stateMachine);
			subscriberMap.put(stateMachine,smsu);
		}
		smsu.increaseCount();
	}
	public synchronized  void unsubscribe(final StateMachine stateMachine) {
		StateMachineSubscription smsu=subscriberMap.get(stateMachine);
		if (smsu!=null) {
			smsu.decreaseCount();
			if (smsu.getCount()==0) {
				subscriberMap.remove(stateMachine);				
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
