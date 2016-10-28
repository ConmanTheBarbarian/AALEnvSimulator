package stateMachine;

public class EdgeProbabilityFunction extends EdgeProbability {
	
	public static class Function {
		public Double  evaluate(StateMachine sm) {
			return null;
		}
	}
	private Function function;

	public EdgeProbabilityFunction(StateMachine stateMachine,Function function) {
		super(stateMachine);
		this.function=function;
	}

	@Override
	public double getValue() {
		return this.function.evaluate(getStateMachine());
	}

}
