package stateMachine;

import java.util.HashMap;
import java.util.Random;

import jdistlib.rng.RandomEngine;
import simulationBase.Configuration;
import simulationBase.Log;
import simulationBase.SimTime;

public class EngineData {
	private SimTime time;
	private Configuration configuration;
	private HashMap<String, Random> randomNumberGeneratorMap=new HashMap<String,Random>();;
	private Log log=Log.createLog();

	public EngineData(Configuration configuration) {
		this.configuration=configuration;
	}

	/**
	 * @return the time
	 */
	public synchronized final SimTime getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public synchronized final void setTime(SimTime time) {
		this.time = time;
	}



	/**
	 * @return the configuration
	 */
	public synchronized final Configuration getConfiguration() {
		return configuration;
	}
	
	public synchronized final void addRandomNumberGenerator(String name,long seed) {
		randomNumberGeneratorMap.put(name, new Random(seed));
	}
	public synchronized final Double nextDouble(String name) {
		return randomNumberGeneratorMap.get(name).nextDouble();
	}

	public Random getRandomNumberGenerator(String name) {
		return randomNumberGeneratorMap.get(name);
	}
	
}