package stateMachine;

public class Edge extends NamedObjectInStateMachine {
	private State startName;
	private State endName;
	private TransitionRule transitionRule;
	

	public Edge(String name, StateMachineSystem stateMachineSystem,StateMachine stateMachine,final State startName,final State endName,TransitionRule transitionRule) {
		super(name,stateMachineSystem,stateMachine);
		this.startName=startName;
		this.endName=endName;
		this.transitionRule=transitionRule;
	}

	public synchronized final State getStartState() {
		return startName;
	}

	public synchronized final State getEndState() {
		return endName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Edge [startName=" + startName + ", endName=" + endName
				+ ", transitionRule=" + transitionRule + "]";
	}

	/**
	 * @return the transitionRule
	 */
	public synchronized final TransitionRule getTransitionRule() {
		return transitionRule;
	}
	
	public final String getEventName() {
		return startName+"->"+endName;
	}
	
	

}
