package stateMachine;

import java.security.KeyStore.Entry;
import java.util.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jdistlib.rng.RandomEngine;
import simulationBase.Base;
import simulationBase.Configuration.RandomNumberGeneratorConfiguration;
import simulationBase.Log;
import stateMachine.Combination.Domain;
import stateMachine.Mode.Type;

public class StateMachine extends NamedObjectInStateMachineSystem implements Evaluation {
	public enum Trace {lvl0,lvl1,lvl2,lvl3}
	static StateMachine getStateMachine(final String name, final StateMachineGroup smg, long seed, Priority priority) {
		StateMachine sm=new StateMachine(name,smg,seed,priority);
		final StateMachineSystem sms=smg.getStateMachineSystem();
		sm.stateMachineGroup=smg;
		sms.addStateMachine(sm);
		sm.modeState=new ModeState(sm.getQualifiedBaseName(),sms,sm);
		sms.addModeState(sm.modeState);		
		return sm;
	}
	private boolean allStatesAreDefined=false;
	private State currentState;
	private HashMap<State,HashMap<State,HashMap<TransitionRule,Edge>>> edgeMap=new HashMap<State,HashMap<State,HashMap<TransitionRule,Edge>>>();
	private ModeState modeState;
	private RandomNumberGeneratorConfiguration rngCfg;
	private RandomEngine randomEngine;
	private HashMap<String,Edge> s2e=new HashMap<String,Edge>();
	//private HashMap<Mode,HashMap<State,HashMap<Edge,EdgeProbability>>> probabilityMap=new HashMap<Mode,HashMap<State,HashMap<Edge,EdgeProbability>>>();
	private HashMap<State,StateEdgeProbabilitySpecification> s2seps=new HashMap<State,StateEdgeProbabilitySpecification>();;
	private HashMap<String,TransitionRule> s2tr=new HashMap<String,TransitionRule>();
	private State startState;
	//private static Log log=Log.createLog();
	private StateMachineGroup stateMachineGroup;
	private HashMap<String,State> stateMap=new HashMap<String,State>();
	private Trace tracing=Trace.lvl0;
	//private HashSet<Mode.Type> typeSet=new HashSet<Mode.Type>();

	private boolean valid=false;
	private Priority priority;
	public final RandomEngine getRandomEngine() {
		return randomEngine;
	}

	/**
	 * @param name
	 * @param sms
	 */
	private StateMachine(String name, StateMachineGroup smg, long seed, Priority priority ) {
		super(name, smg.getStateMachineSystem());
		rngCfg=new RandomNumberGeneratorConfiguration(name, seed);
		this.randomEngine=new jdistlib.rng.MersenneTwister();
		this.randomEngine.setSeed(seed);
		this.priority=priority;

	}
	public final synchronized void addStateEdgeProbabilitySpecification(StateEdgeProbabilitySpecification seps) {

		HashSet<Edge> allOutgoingEdgeSet=new HashSet<Edge>();
		final HashMap<State,HashMap<TransitionRule,Edge>> s2tr2e=this.edgeMap.get(seps.getState());
		if (s2tr2e!=null) {
			for (HashMap<TransitionRule,Edge> es:s2tr2e.values()) {
				allOutgoingEdgeSet.addAll(es.values());
			}
		}
		if (!(allOutgoingEdgeSet.containsAll(seps.getCandidateEdgeSet()) && seps.getCandidateEdgeSet().containsAll(allOutgoingEdgeSet))) {
			throw new IllegalArgumentException("Not all edges of state"+seps.getState()+" are covered");
		}

		s2seps.put(seps.getState(), seps);
	}


	//	public int compareTo(StateMachine stateMachine) {
	//		final int n=this.getStateMachineSystem().hashCode()-stateMachine.getStateMachineSystem().hashCode();
	//
	//		if (n!=0) {
	//			return n;
	//		}
	//		return this.getName().compareTo(stateMachine.getName());
	//	}
	@Override
	public boolean evaluate() {
		final HashMap<State,HashMap<TransitionRule,Edge>> string2edgeSet=edgeMap.get(currentState);
		if (isTracing()) {
			System.out.println("Evaluating "+this.getName());
		}

		// check what edges are applicable and add them to the set of assingments
		// to obtain the proper probability
//		Vector<Combination.Assignment> edgeConditionsAndContext=new Vector<Combination.Assignment>();
//		for (HashMap<TransitionRule,Edge> edgeSet: string2edgeSet.values()) {
//			for (Edge edge:edgeSet.values()) {
//				final Combination.Domain d=Combination.BooleanDomain.getDomain(edge);
//				final Condition condition=edge.getTransitionRule().getCondition();
//				final Combination.Assignment a=new Combination.Assignment(d,new Combination.Value(condition.evaluate(this)));
//				edgeConditionsAndContext.add(a);
//			}
//		}

//		if (edgeConditionsAndContext.isEmpty()) {
//			return false;
//		}


		// add context
		Edge resultEdge=null;
		final StateEdgeProbabilitySpecification seps=s2seps.get(this.getCurrentState());
		TreeMap <Edge,Double> possibleEdgeTransitions=seps.evaluate();
		if (isTracing()) {
			System.out.println("Time is "+this.getStateMachineSystem().getEngineData().getTime().getTime());
			System.out.println("\tApplicable edges are ");
			switch (getTracing()) {
			case lvl0:
				break;
			case lvl1:
				possibleEdgeTransitions.forEach((v,d)->System.out.println("\t\t"+v.getStartState()+"->"+v.getEndState()+" : probability = "+d+", "));
				break;
			case lvl2:
			case lvl3:
				possibleEdgeTransitions.forEach((v,d)->System.out.println("\t\t"+v.getStartState()+"->"+v.getEndState()+" : probability = "+d+", "));
				break;
			default:
				break;

			}
		}
		double r=this.getRandomEngine().nextDouble();
		for (HashMap.Entry<Edge,Double> e2d: possibleEdgeTransitions.entrySet()) {
			final double cp=e2d.getValue();
			if (r<cp) {
				resultEdge=e2d.getKey();
				break;
			}
			r-=cp;
		}

		if (resultEdge==null) {
			throw new IllegalStateException("No applicable edge");
		}
		if (isTracing()) {
			System.out.println("Taking edge "+resultEdge);

			if (!resultEdge.getEndState().equals(this.getCurrentState())) {
				Instant t=this.getStateMachineSystem().getEngineData().getTime().getTime();

				final Log log=this.getStateMachineSystem().getEngineData().getConfiguration().getLog();
				log.addEvent(resultEdge.getEventName(),t,"SM:"+this.getName());
				log.callTracer(resultEdge.getEventName(),t);

			}
		}
		resultEdge.getTransitionRule().getAction().evaluate(this);
		this.moveTo(resultEdge.getEndState());
		this.modeState.removeValue(Mode.getMode(this,resultEdge.getStartState()));
		this.modeState.addValue(Mode.getMode(this,resultEdge.getEndState()));

		return true;

		//Edge resultEdge=null;
		//		for (Edge e:currentProbability.keySet()) {
		//			final double cp=currentProbability.get(e);
		//			if (r<cp) {
		//				resultEdge=e;
		//				break;
		//			}
		//			r-=cp;
		//		}
		//		if (resultEdge==null) {
		//			resultEdge=currentProbability.lastKey();
		//		}
		//		resultEdge.getTransitionRule().getAction().evaluate(this);
		//		if (isTracing()) {
		//			System.out.println("Taking edge "+resultEdge);
		//		
		//			if (!resultEdge.getEndName().equals(this.getCurrentState().getName())) {
		//				Instant t=this.getStateMachineSystem().getEngineData().getTime().getTime();
		//
		//				final Log log=this.getStateMachineSystem().getEngineData().getConfiguration().getLog();
		//				log.addEvent(resultEdge.getEventName(),t,"SM:"+this.getName());
		//				log.callTracer(resultEdge.getEventName(),t);
		//				
		//			}
		//		}
		//		this.moveTo(stateMap.get(resultEdge.getEndName()));
		//		this.modeState.removeValue(Mode.getMode(this,resultEdge.getStartName()));
		//		this.modeState.addValue(Mode.getMode(this,resultEdge.getEndName()));
		//
		//		
		//		return true;
	}

	@Override
	public boolean evaluate(StateMachine sm) {
		throw new IllegalArgumentException("State machine do not call evaluate with StateMachine as an argument");
	}

	public State getCurrentState() {
		return this.currentState;
	}
	public Edge getEdge(final String name) {
		final Edge edge=s2e.get(name);
		if (edge==null) {
			throw new IllegalStateException("Edge with name "+name+" does not exist");
		}
		return edge;
	}
	public Edge getEdge(final String name,final State startState,final State endState,final TransitionRule transitionRule) {

		if (!startState.getStateMachine().equals(this)) {
			throw new IllegalArgumentException("Edge "+startState.getName()+" belongs to the state machine: "+startState.getStateMachine().getName()+" instead of "+this.getName());
		}
		if (!endState.getStateMachine().equals(this)) {
			throw new IllegalArgumentException("Edge "+endState.getName()+" belongs to the state machine: "+endState.getStateMachine().getName()+" instead of "+this.getName());
		}


		HashMap<State,HashMap<TransitionRule,Edge>> tmp=edgeMap.get(startState);
		HashMap<TransitionRule,Edge> edgeSet=null;
		if (tmp!=null) {
			HashMap<TransitionRule,Edge> tmp2=tmp.get(endState);
			if (tmp2==null) {
				tmp2=new HashMap<TransitionRule,Edge>();
				tmp.put(endState, tmp2);
			}
			if (tmp2.containsKey(transitionRule)) {
				return tmp2.get(transitionRule);
			}
			edgeSet=tmp2;


		} else {
			tmp=new HashMap<State,HashMap<TransitionRule,Edge>>();
			edgeMap.put(startState,tmp);
			final HashMap<TransitionRule,Edge> tmp2=new HashMap<TransitionRule,Edge>();
			tmp.put(endState, tmp2);
			edgeSet=tmp2;

		}
		Edge edge=new Edge(name,getStateMachineSystem(),this,startState,endState,transitionRule);
		s2e.put(name, edge);
		edgeSet.put(transitionRule,edge);

		edge.getTransitionRule().getEvent().subscribe(this, this.getPriority());
		return edge;
	}
	public Edge getEdge(final String name,final String startStateName,final String endStateName,final TransitionRule transitionRule) {
		final State startState=stateMap.get(startStateName);
		final State endState=stateMap.get(endStateName);
		if (startState==null) {
			throw new IllegalArgumentException("There is not state "+startStateName);
		}
		if (endState==null) {
			throw new IllegalArgumentException("There is not state "+startStateName);
		}
		return getEdge(name,startState,endState,transitionRule);
	}


	public Set<Edge> getEdges() {
		HashSet<Edge> result=new HashSet<Edge>();
		for (HashMap<State,HashMap<TransitionRule,Edge>> s2es:edgeMap.values()) {
			for(HashMap<TransitionRule,Edge> s2e:s2es.values()) {
				result.addAll(s2e.values());
			}
		}
		return result;
	}
	public final synchronized ModeState getModeState() {
		return this.modeState;
	}
	/* (non-Javadoc)
	 * @see stateMachine.NamedObjectInStateMachineSystem#getStateMachineBackEnd(stateMachine.NamedObjectInStateMachineSystem, java.lang.String[])
	 */
	@Override
	NamedObjectInStateMachineSystem getNamedObjectInStateMachineSystemBackEnd(final NamedObjectInStateMachineSystem noism,
			final List<Path.Part> parts, final int currentPosition) {
		if (currentPosition<0 || currentPosition>=parts.size()) {
			throw new IllegalArgumentException("Current position "+currentPosition+" is out of range");
		}
		Path.Part head=parts.get(currentPosition);
		NamedObjectInStateMachineSystem nextNoism=null;
		if (head.getPart().compareTo(".")==0) {
			nextNoism=noism;
		}
		if (head.getPart().compareTo("..")==0) {
			nextNoism=((StateMachine)noism).getStateMachineGroup();
		}
		final int nextPosition=currentPosition+1;
		if (nextPosition<parts.size()) {
			return nextNoism.getNamedObjectInStateMachineSystemBackEnd(nextNoism, parts, nextPosition);
		} 
		if (nextNoism==null) {
			throw new IllegalArgumentException("Part "+noism.getQualifiedName()+"  has no children");
		}
		return nextNoism;
	}
	public synchronized final State getOrCreateState(String stateName) {
		State s=stateMap.get(stateName);
		if (s==null) {
			if (!isAllStatesAreDefined()) {
				s=new State(stateName,getStateMachineSystem(),this);
				stateMap.put(stateName, s);
			} else {
				throw new IllegalStateException("Cannot define more state in this state machine");
			}
		}
		return s;
	}
	@Override
	synchronized String getQualifiedBaseName() {
		return stateMachineGroup.getQualifiedName()+"/"+this.getBaseName();
	}
	@Override
	public synchronized String getQualifiedName() {
		return stateMachineGroup.getQualifiedName()+"/"+this.getName();
	}
	public final RandomNumberGeneratorConfiguration getRandomNumberGeneratorConfiguration() {
		return this.rngCfg;
	}
	/**
	 * @return the startState
	 */
	public State getStartState() {
		return startState;
	}
	public final synchronized StateEdgeProbabilitySpecification getStateEdgeProbabilitySpecification(State state) {
		return s2seps.get(state);
	}
	public StateMachineGroup getStateMachineGroup() {
		return stateMachineGroup;
	}
	public Trace getTracing() {
		return tracing;
	}

	public TransitionRule getTransitionRule(final String name) {
		final TransitionRule tr=s2tr.get(name);
		if (tr==null) {
			throw new IllegalStateException("Transition rule with name "+name+" does not exist");
		}
		return tr;
	}
	public TransitionRule getTransitionRule(final String name, Event event, Condition condition, Action action) {
		TransitionRule tr=s2tr.get(name);
		if (tr!=null) {
			throw new IllegalStateException("Transition rule "+tr+" already exists");
		}
		tr=new TransitionRule(name,getStateMachineSystem(),this,event,condition,action);
		s2tr.put(name, tr);
		return tr;

	}
	public Type getType() {
		Type type=Type.getType(this.getBaseName()+"_type");
		type.addAll(this.stateMap.keySet().stream().map(s->this.getOrCreateState(s)).collect(Collectors.toSet()));
		return type;
	}
	public synchronized final Set<Mode.Type> getTypeSet() {
		final HashSet<Mode.Type> tmp= new HashSet<Mode.Type>();
		tmp.add(this.getType());
		return tmp;
	}
	/**
	 * @return the allStatesAreDefined
	 */
	public synchronized final boolean isAllStatesAreDefined() {
		return allStatesAreDefined;
	}
	//	public synchronized void addEdge(Edge edge) {
	//		if (stateMap.get(edge.getStartName())==null) {
	//			throw new IllegalArgumentException("Edge "+edge+" does not link states properly");
	//		}
	//		if (!edge.getStateMachineSystem().equals(this.getStateMachineSystem())) {
	//			throw new IllegalArgumentException("Edge "+edge.getName()+" belongs to the incorrect state machine system: "+edge.getStateMachineSystem().getName()+" instead of "+this.getStateMachineSystem().getName());
	//		}
	//		
	//		HashMap<String,HashMap<Edge>> tmp=edgeMap.get(edge.getStartName());
	//		HashSet<Edge> edgeSet=null;
	//		if (tmp!=null) {
	//			HashSet<Edge> tmp2=tmp.get(edge.getEndName());
	//			if (tmp2==null) {
	//				tmp2=new HashSet<Edge>();
	//				tmp.put(edge.getEndName(), tmp2);
	//			}
	//			if (tmp2.contains(edge)) {
	//				throw new IllegalArgumentException("Edge starting at "+edge.getStartName()+" and ending at "+edge.getEndName()+" already exists");
	//			}
	//			edgeSet=tmp2;
	//			
	//			
	//		} else {
	//			tmp=new HashMap<String,HashSet<Edge>>();
	//			edgeMap.put(edge.getStartName(),tmp);
	//			final HashSet<Edge> tmp2=new HashSet<Edge>();
	//			tmp.put(edge.getEndName(), tmp2);
	//			edgeSet=tmp2;
	//			
	//		}
	//		edgeSet.add(edge);
	//		final State state=stateMap.get(edge.getStartName());
	//		if (state==null) {
	//			throw new IllegalStateException("State "+edge.getStartName()+" does not exist");
	//		}
	//		edge.getTransitionRule().getEvent().subscribe(this);
	//
	//	}
	public boolean isStateForEdge(final Edge edge) {
		if (!edge.getStateMachineSystem().equals(this.getStateMachineSystem())) {
			throw new IllegalArgumentException("Edge "+edge.getName()+" belongs to the incorrect state machine system: "+edge.getStateMachineSystem().getName()+" instead of "+this.getStateMachineSystem().getName());
		}
		final HashMap<State,HashMap<TransitionRule,Edge>> tmp=edgeMap.get(edge.getStartState());
		if (tmp==null) {
			return false;
		}
		final HashMap<TransitionRule,Edge> tmp3=tmp.get(edge.getEndState());
		if (tmp3==null) {
			return false;
		}
		return tmp3.get(edge.getTransitionRule())!=null;


	}

	/**
	 * @return the tracing
	 */
	public boolean isTracing() {
		return tracing!=Trace.lvl0;
	}

	//	public synchronized void setProbability(Edge edge,Collection<Mode> mode,EdgeProbability probability) {
	//		if (!this.isStateForEdge(edge)) {
	//			throw new IllegalArgumentException("Edge "+edge.getName()+" does not belong to the state machine "+this.getName());
	//		}
	//		for (Mode m:mode) {
	//			HashMap<State,HashMap<Edge,EdgeProbability>> state2edge2probability=probabilityMap.get(m);
	//			if (state2edge2probability==null) {
	//				state2edge2probability=new HashMap<State,HashMap<Edge,EdgeProbability>>();
	//				probabilityMap.put(m, state2edge2probability);
	//			}
	//			final State state=stateMap.get(edge.getStartName());
	//			HashMap<Edge,EdgeProbability> edge2double=state2edge2probability.get(state);
	//			if (edge2double==null) {
	//				edge2double=new HashMap<Edge,EdgeProbability>();
	//				state2edge2probability.put(state, edge2double);
	//			}
	//			edge2double.put(edge, probability);
	//		}
	//	}
	//	public synchronized void addState(State state) {
	//		if (!state.getStateMachineSystem().equals(this.getStateMachineSystem())) {
	//			throw new IllegalArgumentException("State "+state.getName()+" belongs to the incorrect state machine system: "+state.getStateMachineSystem().getName()+" instead of "+this.getStateMachineSystem().getName());
	//
	//		}
	//		if (stateMap.get(state.getBaseName())!=null) {
	//			throw new IllegalArgumentException("State "+state.getName()+" already exists");
	//		}
	//		stateMap.put(state.getBaseName(), state);
	//	}
	/**
	 * @return the valid
	 */
	public synchronized final boolean isValid() {
		return valid;
	}

	public synchronized final void moveTo(State state) {
		this.currentState=state;
	}

	public synchronized void removeEdge(Edge egde) {

	}
	public synchronized void removeState(State state) {
		stateMap.remove(state.getBaseName());
	}

	/**
	 * @param allStatesAreDefined the allStatesAreDefined to set
	 */
	public synchronized final void setAllStatesAreDefined(
			boolean allStatesAreDefined) {
		if (!isAllStatesAreDefined()) { 
			this.allStatesAreDefined = allStatesAreDefined;
		} else if (!allStatesAreDefined) {
			throw new IllegalArgumentException("Cannot reset the fact that all states are defined");
		}
	}

	/**
	 * @param startState the startState to set
	 */
	public final synchronized void setStartState(State startState) {
		if (startState==null) {
			this.valid=false;
		} else if (!stateMap.containsValue(startState)) {
			throw new IllegalArgumentException("State "+startState.getName()+" is not a part of state machine"+this.getName());
		}
		this.startState = startState;
		this.currentState=startState;

	}



	/**
	 * @param tracing the tracing to set
	 */
	public void setTracing(Trace tracing) {
		this.tracing = tracing;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StateMachine [getName()=" + getName() + ", startState="
				+ startState + ", currentState=" + currentState + ", valid="
				+ valid + "]";
	}

	public synchronized final boolean validate() {

		for (State state:stateMap.values()) {
			// check for outgoing edges
			final HashMap<State, HashMap<TransitionRule,Edge>> tmp=edgeMap.get(state);
			if (tmp==null) {
				throw new IllegalStateException("State "+state.getName()+" has no outgoing edge");
			}
			for (StateEdgeProbabilitySpecification seps:this.s2seps.values()) {
				seps.validate();
			}
		}
		if (startState==null) {
			throw new IllegalStateException("No start state set");
		} 
		return valid;
	}

	/**
	 * @return the priority
	 */
	public synchronized final Priority getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public synchronized final void setPriority(Priority priority) {
		if (this.priority.compareTo(priority)!=0) {
			for (TransitionRule tr:s2tr.values()) {
				final Event event=tr.getEvent();
				event.unsubscribe(this);
				event.subscribe(this,priority);
			}
			this.priority = priority;
		}
	}


}
