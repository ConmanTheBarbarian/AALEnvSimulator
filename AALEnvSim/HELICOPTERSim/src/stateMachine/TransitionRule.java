package stateMachine;

public class TransitionRule extends NamedObjectInStateMachine {
	private Event event;
	private Condition condition;
	private Action action;
	
	
	/**
	 * @param name
	 * @param event
	 * @param condition
	 * @param action
	 */
	TransitionRule(String name, StateMachineSystem stateMachineSystem,StateMachine stateMachine, Event event, Condition condition,
			Action action) {
		super(name,stateMachineSystem,stateMachine);
		if (!event.getStateMachineSystem().equals(stateMachineSystem)|| !condition.getStateMachineSystem().equals(stateMachineSystem)||!action.getStateMachineSystem().equals(stateMachineSystem)) {
			throw new IllegalArgumentException("Part of transition rule does not belong to the same state machine system");
		}
		this.event = event;
		this.condition = condition;
		this.action = action;
		stateMachineSystem.addTransitionRule(this);
	}
	


	/**
	 * @return the event
	 */
	public synchronized final Event getEvent() {
		return event;
	}
	/**
	 * @return the condition
	 */
	public synchronized final Condition getCondition() {
		return condition;
	}
	/**
	 * @return the action
	 */
	public synchronized final Action getAction() {
		return action;
	}
	public synchronized final void subscribe(StateMachine stateMachine, Priority priority) {
		this.getEvent().subscribe(stateMachine, priority);
	}
	public synchronized final void unsubscribe(StateMachine stateMachine) {
		this.getEvent().unsubscribe(stateMachine);
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TransitionRule [getName()=" + getName() + ", event=" + event
				+ ", condition=" + condition + ", action=" + action + "]";
	}
	

	
}
