package stateMachine;

public class NamedObjectInStateMachine extends NamedObjectInStateMachineSystem {
	private StateMachine stateMachine;
	public NamedObjectInStateMachine(String name, StateMachineSystem sms,StateMachine sm) {
		super(name, sms);
		this.stateMachine=sm;
	}
	/**
	 * @return the stateMachine
	 */
	public synchronized final StateMachine getStateMachine() {
		return stateMachine;
	}
	

}
