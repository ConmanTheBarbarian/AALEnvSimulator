package stateMachine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import simulationBase.Base;
import stateMachine.Combination.Type;

public abstract class StateEdgeProbabilitySpecification {
	

	private Combination.Type contextTypeMap;
	private State state;
	private HashSet<Edge> candidateEdgeSet=new HashSet<Edge>();

	public StateEdgeProbabilitySpecification(final Type type, State state, final List<Edge> edgeList) {
		this.contextTypeMap=type;
		this.candidateEdgeSet.addAll(edgeList);
		this.state=state;
	}
	public StateEdgeProbabilitySpecification(final Type type, State state, final Edge[] edgeArray) {
		this.contextTypeMap=type;
		this.candidateEdgeSet.addAll(Arrays.asList(edgeArray));
		this.state=state;
	}
	public abstract TreeMap<Edge,Double> evaluate();
	/**
	 * @return the contextType
	 */
	public synchronized final Type getContextType() {
		return contextTypeMap;
	}
	/**
	 * @return the candidateEdgeSet
	 */
	public synchronized final HashSet<Edge> getCandidateEdgeSet() {
		return candidateEdgeSet;
	}
	/**
	 * @return the state
	 */
	public synchronized final State getState() {
		return state;
	}
	public abstract  void validate();
	protected HashSet<Edge> checkApplicableEdges() {
		final HashSet<Edge> applicableEdgeSet=new HashSet<Edge>();
		for (Edge edge: getCandidateEdgeSet()) {
			final TransitionRule tr=edge.getTransitionRule();
			final Condition condition=tr.getCondition();
			if (condition.evaluate(this.getState().getStateMachine())) {
				applicableEdgeSet.add(edge);
			}
		}
		return applicableEdgeSet;
	}
	

}
