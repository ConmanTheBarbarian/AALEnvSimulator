package stateMachine;

import simulationBase.Base;

public class EdgeProbabilityModifierDouble extends EdgeProbability {

	private double edgeProbability;

	public EdgeProbabilityModifierDouble(StateMachine stateMachine, double edgeProbability) {
		super(stateMachine);
		if (edgeProbability!=0.0) {
			this.edgeProbability=edgeProbability;
		} else {
			this.edgeProbability=Base.epsilon;
		}
	}

	@Override
	public double getValue() {
		return this.edgeProbability;
	}

}
