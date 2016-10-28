package stateMachine;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

import simulationBase.Base;
import stateMachine.Combination.Type;

public class StateEdgeProbaibllityTableSpecification extends
		StateEdgeProbabilitySpecification {
	
	private Vector<HashMap<Edge,Double>> probabilityTable=new Vector<HashMap<Edge,Double>>();
	private Combination combination;
	private boolean validated=false;

	public StateEdgeProbaibllityTableSpecification(Type type, State state,
			List<Edge> edgeList) {
		super(type, state, edgeList);
		this.combination=new Combination(type);
		this.probabilityTable.ensureCapacity(combination.getDomainSize().intValue());
	}

	public StateEdgeProbaibllityTableSpecification(Type type, State state,
			Edge[] edgeArray) {
		super(type, state, edgeArray);
		this.combination=new Combination(type);
		this.probabilityTable.ensureCapacity(combination.getDomainSize().intValue());
	}
	public synchronized void setProbability(Combination.Assignment assignment[],Edge edge, Double probability) {
		if (!this.getCandidateEdgeSet().contains(edge)) {
			throw new IllegalArgumentException("Edge \""+edge+"\" is not declared as an outgoing edge of the state "+this.getState());
		}
		if (probability<-Base.epsilon || probability >1.0+Base.epsilon) {
			throw new IllegalArgumentException("Probability "+probability+" is out of range");
		}
		this.validated=false;
		this.combination.setCurrentCombination(assignment);
		final BigInteger index=this.combination.getCurrentIndex();
		HashMap<Edge,Double> e2d=this.probabilityTable.get(index.intValue());
		if (e2d==null) {
			e2d=new HashMap<Edge,Double>();
			this.probabilityTable.setElementAt(e2d, index.intValue());
		}
		e2d.put(edge, probability);
	}
	public synchronized HashMap<Edge,Double> getProbability(Combination.Assignment assignment[]) {
		if (!this.validated) {
			this.validate();
		}
		this.combination.setCurrentCombination(assignment);
		final BigInteger index=this.combination.getCurrentIndex();
		return probabilityTable.get(index.intValue());
	}

	@Override
	public final synchronized void validate() {
		boolean nullValueFound=false;
		boolean sumIsNotOne=false;
		for (HashMap<Edge,Double> e2d:probabilityTable) {
			if (e2d==null) {
				nullValueFound=true;
			}
			final Double sum=e2d.values().stream().reduce(0.0,(a,b)->a+b);
			if (!Base.withinEpsilonEquivalence(sum, 1.0)) {
				sumIsNotOne=true;
			}
		}
		StringBuffer sb=new StringBuffer();
		if (nullValueFound) {
			sb.append("Null value discovered.");
		}
		if (sumIsNotOne) {
			sb.append(" A sum is not 1.0");
		}
		if (sb.length()>0) {
			throw new IllegalStateException(sb.toString());
		}
		this.validated=true;
	}

	@Override
	public HashMap<Edge, Double> evaluate() {
		return null;
	}

}
