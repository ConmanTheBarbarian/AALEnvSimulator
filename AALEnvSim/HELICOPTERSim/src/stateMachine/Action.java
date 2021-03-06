package stateMachine;

public class Action extends NamedObjectInStateMachineSystem implements Evaluation {
	

	
	
	/**
	 * @param name
	 * @param sms
	 */
	public Action(String name, StateMachineSystem sms) {
		super(name, sms);
		sms.addAction(this);
	}




	@Override
	public boolean evaluate(final StateMachine sm) {
		return false;
	}




	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Action [getName()=" + getName() + "]";
	}




	@Override
	public boolean evaluate() {
		throw new IllegalArgumentException("Must use evaluate(StateMachine) on Action objects");
	}

}
