package simulationBase;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import stateMachine.Event;
import stateMachine.Mode;
import stateMachine.ModeState;
import stateMachine.StateMachineSystem;

public class Configuration {
	
	public static class RandomNumberGeneratorConfiguration {
		private String name;
		private long seed;
		/**
		 * @param name
		 * @param seed
		 */
		public RandomNumberGeneratorConfiguration(String name, long seed) {
			this.name = name;
			this.seed = seed;
		}
		/**
		 * @return the name
		 */
		public synchronized final String getName() {
			return name;
		}
		/**
		 * @return the seed
		 */
		public synchronized final long getSeed() {
			return seed;
		}
		
	}
	private Duration advanceTime;
	private Instant[] interval;
	private HashMap<String,Object> string2value=new HashMap<String,Object>();
	private HashSet<Mode.Type> typeSet=new HashSet<Mode.Type>();
	private HashSet<ModeState> modeStateSet=new HashSet<ModeState>();
	private Log log=Log.createLog();

	public Configuration(final Set<Mode.Type> typeSet,Instant startTime,Instant endTime) {

		this.typeSet.addAll(typeSet);
		this.interval=new Instant[2];
		this.interval[0]=startTime;
		this.interval[1]=endTime;
	}
	

	/**
	 * @return the advanceTime
	 */
	public synchronized final Duration getAdvanceTime() {
		return advanceTime;
	}
	public Double getDouble(final String name) {
		Object o=string2value.get(name);
		if (o==null) {
			throw new IllegalArgumentException("Configuration parameter "+name+"  does not exist");
		}
		if (! (o instanceof Double)) {
			throw new IllegalArgumentException("Configuration parameter "+name+" is not a double");
		}
		return (Double)o;
	}

	public Integer getInteger(final String name) {
		Object o=string2value.get(name);
		if (o==null) {
			throw new IllegalArgumentException("Configuration parameter "+name+"  does not exist");
		}
		if (! (o instanceof Integer)) {
			throw new IllegalArgumentException("Configuration parameter "+name+" is not an integer");
		}
		return (Integer)o;
	}
	
	
	
	/**
	 * @return the interval
	 */
	public synchronized final Instant[] getInterval() {
		return interval;
	}
	
	protected synchronized final void setInterval(Instant[] interval) {
		this.interval=interval;
	}
	public String getString(String name) {
		Object o=string2value.get(name);
		if (o==null) {
			throw new IllegalArgumentException("Configuration parameter "+name+"  does not exist");
		}
		if (! (o instanceof String)) {
			throw new IllegalArgumentException("Configuration parameter "+name+" is not a string");
		}
		return (String)o;
	}
	public final synchronized Set<Mode.Type> getTypeSet() {
		return this.typeSet;
	}
	public final synchronized void addType(Mode.Type type) {
		this.typeSet.add(type);
	}
	
	public void initialize(StateMachineSystem sms,Event tick) {
		
	}


	protected synchronized void put(String name,Object o) {
		string2value.put(name, o);
	}
	protected synchronized void setAdvanceTime(Duration advanceTime) {
		this.advanceTime=advanceTime;
		
	}

	public final synchronized void addModeState(ModeState modeState) {
		if (!typeSet.contains(modeState.getType())) {
			throw new IllegalArgumentException("Mode state "+modeState.getName()+" is of the invalid type "+modeState.getType().getName());
		}
		this.modeStateSet.add(modeState);
	}

	public final synchronized Set<ModeState> getModeStateSet() {
		return this.modeStateSet;
	}


	/**
	 * @return the log
	 */
	public Log getLog() {
		return log;
	}




}
