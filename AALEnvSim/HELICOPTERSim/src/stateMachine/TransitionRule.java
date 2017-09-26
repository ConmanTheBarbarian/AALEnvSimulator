package stateMachine;

public class TransitionRule extends NamedObjectInStateMachine {
	private Action action;
	private boolean basedOnDeclaredAsFutureEvent;
	private Condition condition;
	private EventType event;
	
	
	/**
	 * @param name
	 * @param event
	 * @param condition
	 * @param action
	 * @param basedOnDeclaredAsFutureEvent TODO
	 */
	TransitionRule(String name, StateMachineSystem stateMachineSystem,StateMachine stateMachine, EventType event, Condition condition,
			Action action, boolean basedOnDeclaredAsFutureEvent) {
		super(name,stateMachineSystem,stateMachine);
		if (!event.getStateMachineSystem().equals(stateMachineSystem)|| !condition.getStateMachineSystem().equals(stateMachineSystem)||!action.getStateMachineSystem().equals(stateMachineSystem)) {
			throw new IllegalArgumentException("Part of transition rule does not belong to the same state machine system");
		}
		this.event = event;
		this.condition = condition;
		this.action = action;
		this.basedOnDeclaredAsFutureEvent=basedOnDeclaredAsFutureEvent;
		stateMachineSystem.addTransitionRule(this);
	}
	


	/**
	 * @return the action
	 */
	public synchronized final Action getAction() {
		return action;
	}
	/**
	 * @return the condition
	 */
	public synchronized final Condition getCondition() {
		return condition;
	}
	/**
	 * @return the event
	 */
	public synchronized final EventType getEventType() {
		return event;
	}
	
	/**
	 * @return the basedOnDeclaredAsFutureEvent
	 */
	public synchronized final boolean isBasedOnDeclaredAsFutureEvent() {
		return basedOnDeclaredAsFutureEvent;
	}
	public synchronized final void subscribe(StateMachine stateMachine, Priority priority) {
		this.getEventType().subscribe(stateMachine, priority, false);
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TransitionRule [getName()=" + getName() + ", event=" + event + "based on future event="+basedOnDeclaredAsFutureEvent
				+ ", condition=" + condition + ", action=" + action + "]";
	}



	public synchronized final void unsubscribe(StateMachine stateMachine) {
		this.getEventType().unsubscribe(stateMachine);
	}
	

	
}
