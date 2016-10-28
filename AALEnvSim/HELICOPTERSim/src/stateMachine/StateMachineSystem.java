package stateMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import stateMachine.Event.StateMachineSubscription;

public class StateMachineSystem implements Evaluation {
	private final static HashMap<String,StateMachineSystem> stateMachineSystemMap=new HashMap<String,StateMachineSystem>(); 
	public static final synchronized StateMachineSystem getStateMachineSystem(String name,EngineData engineData) {
		final Set<Mode.Type> type=engineData.getConfiguration().getTypeSet();
		StateMachineSystem sms=stateMachineSystemMap.get(name);
		
		if (sms==null) {
			sms=new StateMachineSystem(name,engineData);
			stateMachineSystemMap.put(name, sms);
		}
		return sms;
		
	}
	private HashMap<String,Action> actionMap=new HashMap<String,Action>();
	private HashMap<String,Condition> conditionMap=new HashMap<String,Condition>();
	private EngineData engineData;
	private HashMap<String,Event> eventMap=new HashMap<String,Event>();
	private Vector<Event> eventQueue=new Vector<Event>();
	private MetaModeState metaMode;
	private String name;
	private HashMap<String,StateMachine> stateMachineMap=new HashMap<String,StateMachine>();
	private Event tick=PrimitiveEvent.getPrimitiveEvent("tick",this);
	private HashMap<String,TransitionRule> transitionRuleMap=new HashMap<String,TransitionRule>();
	private HashMap<String,StateMachineGroup> s2smg=new HashMap<String,StateMachineGroup>();
	

	
	private StateMachineSystem(String name,EngineData engineData) {
		this.name=name;
		this.metaMode=new MetaModeState(name+"_metaModeState",this,engineData.getConfiguration().getTypeSet());
		this.engineData=engineData;
	}
	
	synchronized void addAction(Action action) {
		final Action tmp=actionMap.get(action.getName());
		if (tmp!=null) {
			throw new IllegalArgumentException("Action "+action.getName()+" already exists");
		}
		actionMap.put(action.getName(), action);
	}

	synchronized void addCondition(Condition condition) {
		final Condition tmp=conditionMap.get(condition.getName());
		if (tmp!=null) {
			throw new IllegalArgumentException("Condition "+condition.getName()+" already exists");

		}
		conditionMap.put(condition.getName(),condition);
			
	}

	synchronized void addEvent(Event event) {
		final Event tmp=eventMap.get(event.getName());
		if (tmp!=null) {
			throw new IllegalArgumentException("Event "+event.getName()+" already exists");
		}
		eventMap.put(event.getName(), event);
	}

	public final void addMode(String modeStateName,State modeName) {
		this.metaMode.addMode(modeStateName,modeName);
	}

	void addModeState(ModeState modeState) {
		this.metaMode.addModeState(modeState);		
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
	 */
	StateMachine addStateMachine(StateMachine arg1) {
		return stateMachineMap.put(arg1.getQualifiedName(), arg1);
	}
	


	synchronized void addTransitionRule(final TransitionRule transitionRule) {
		final TransitionRule tmp=transitionRuleMap.get(transitionRule.getName());
		if (tmp!=null) {
			throw new IllegalArgumentException("TransitionRule "+transitionRule.getName()+" already exists");
		}
		transitionRuleMap.put(transitionRule.getName(),transitionRule);
		
	}
	
	@Override
	public boolean evaluate() {
		while(!eventQueue.isEmpty()) {
			final Event event=eventQueue.firstElement();
			eventQueue.remove(event);
			final Collection<StateMachineSubscription> smsuSet=event.getSubscriberSet();
			for (StateMachineSubscription smsu:smsuSet) {
				final StateMachine sm=smsu.getStateMachine();
				sm.evaluate();
			}
		}
		return false;
	}
	
	@Override
	public boolean evaluate(StateMachine sm) {
		throw new IllegalArgumentException("State machine systems do not call evaluate with StateMachine as an argument");
	}
	public synchronized Action getAction(String name) {
		return actionMap.get(name);
	}
	
	public synchronized Condition getCondition(String name) {
		return conditionMap.get(name);
	}
	/**
	 * @return the engineData
	 */
	public EngineData getEngineData() {
		return engineData;
	}
	public Event getEvent(String string) {
		return eventMap.get(string);
	}
	
	public MetaModeState getMetaModeState() {
		return this.metaMode;
	}

	public final String getName() {
		return this.name;
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	public StateMachine getStateMachine(String arg0) {
		return stateMachineMap.get(arg0);
	}
	
	private StateMachineGroup getOrCreateStateMachineGroupBackend(final String name,final StateMachineGroup parent) {
		StateMachineGroup smg=s2smg.get(name);
		if (smg==null) {
			smg=new StateMachineGroup(name,this,parent);
			s2smg.put(name, smg);
		}
		return smg;
	}
	public StateMachineGroup getOrCreateStateMachineGroup(final String name,final StateMachineGroup parent) {
		if (parent==null) {
			throw new IllegalArgumentException("A state machine group needs a parent");
		}
		return getOrCreateStateMachineGroupBackend(name, parent);
	}
	public StateMachineGroup getStateMachineGroupRoot() {
		return getOrCreateStateMachineGroupBackend("root",null);
	}




	/**
	 * @return the tick
	 */
	public synchronized final Event getTick() {
		return tick;
	}

	public synchronized TransitionRule getTransitionRule(String name) {
		return transitionRuleMap.get(name);
	}

	/**
	 * @return
	 * @see java.util.HashMap#keySet()
	 */
	public Set<String> keySetOfStateMachine() {
		return stateMachineMap.keySet();
	}

	public final void removeMode(String modeStateName,State modeName) {
		this.metaMode.removeMode(modeStateName,modeName);
	}

	/**
	 * @param arg0
	 * @return
	 * @see java.util.HashMap#remove(java.lang.Object)
	 */
	public StateMachine removeStateMachine(String arg0) {
		return stateMachineMap.remove(arg0);
	}

	public final synchronized void removeStateMachineSystem() {
		stateMachineSystemMap.remove(this.getName());
	}

	public synchronized void signal(Event updateEvent) {
		if (!eventQueue.contains(updateEvent)) {
			eventQueue.addElement(updateEvent);			
		}
	}

	/**
	 * @return
	 * @see java.util.HashMap#isEmpty()
	 */
	public boolean stateMachineIsEmpty() {
		return stateMachineMap.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StateMachineSystem [name=" + name + ", mode=" + metaMode
				+ ", eventQueue=" + eventQueue + ", tick=" + tick
				+ ", engineData=" + engineData + "]";
	}

	public synchronized void validate() {
		for (StateMachine sm:stateMachineMap.values()) {
			sm.validate();
		}
		
	}




	

}
