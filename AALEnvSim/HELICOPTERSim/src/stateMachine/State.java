package stateMachine;

public class State extends NamedObjectInStateMachine {


	/**
	 * @param name
	 * @param sms
	 */
	State(String name, StateMachineSystem sms,StateMachine stateMachine) {
		super(name, sms,stateMachine);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "State [getName()=" + getName() + "]";
	}

	

	

}
