package stateMachine;

public class EdgeProbabilityDouble extends EdgeProbability {

	private double edgeProbability;

	public EdgeProbabilityDouble(StateMachine stateMachine, double edgeProbability) {
		super(stateMachine);
		if (edgeProbability<0 || edgeProbability>1.0) {
			throw new IllegalArgumentException("Value "+edgeProbability+" is out of boundary");
		}
		this.edgeProbability=edgeProbability;
	}

	@Override
	public double getValue() {
		return this.edgeProbability;
	}

}
