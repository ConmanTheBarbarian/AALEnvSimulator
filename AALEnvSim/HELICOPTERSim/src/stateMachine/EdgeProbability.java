package stateMachine;

public abstract  class EdgeProbability implements Comparable<EdgeProbability> {

	private StateMachine stateMachine;
	public  abstract double getValue();

	public EdgeProbability(StateMachine stateMachine) {
		this.stateMachine=stateMachine;
	}
	
	public final StateMachine getStateMachine() {
		return this.stateMachine;
	}
	@Override
	public int compareTo(EdgeProbability arg0) {
		double difference=this.getValue()-arg0.getValue();
		
		if (Math.abs(difference)<1e-9) {
			return 0;
		} else {
			return (int) Math.signum(difference);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EdgeProbability [getValue()=" + getValue() + ", getClass()="
				+ getClass() + "]";
	}
	
	

}
