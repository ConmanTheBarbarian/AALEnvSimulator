package stateMachine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import stateMachine.Combination.Assignment;
import stateMachine.Combination.Type;

public final class StateEdgeProbabilityFunctionSpecification extends
		StateEdgeProbabilitySpecification {
	
	public static class Function {
		public TreeMap<Edge,Double> evaluate(Type type,State state, Set<Edge> candidateEdgeSet, Set<Edge> applicableEdgeSet) {
			throw new IllegalArgumentException("Not implemented");
		}
	}
	private Function function;

	public StateEdgeProbabilityFunctionSpecification(final Type type,
			final State state,
			final List<Edge> edgeList, Function function) {
		super(type, state, edgeList);
		this.function=function;
	}

	public StateEdgeProbabilityFunctionSpecification(final Type type, final State state, final Edge[] edgeArray,Function function) {
		super(type, state, edgeArray);
		this.function=function;
	}

	@Override
	public TreeMap<Edge, Double> evaluate() {
		HashSet<Edge> applicableEdges=this.checkApplicableEdges();
		
		return this.function.evaluate(this.getContextType(),this.getState(), this.getCandidateEdgeSet(),applicableEdges);
	}

	public final synchronized void validate() {
	}

}
