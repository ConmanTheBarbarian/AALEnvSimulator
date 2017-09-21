package simulationBase;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import stateMachine.EngineData;
import stateMachine.Mode;
import stateMachine.PrimitiveEventType;
import stateMachine.StateMachineSystem;

public class Engine extends Thread {
	
	private StateMachineSystem stateMachineSystem;
	private EngineData data;
	private boolean validated=false;

	public Engine(Configuration configuration) {
		this.data=new EngineData(configuration);


		this.stateMachineSystem=StateMachineSystem.getStateMachineSystem("The system", this.data);
		configuration.initialize(stateMachineSystem,stateMachineSystem.getTick());
		Duration d=this.data.getConfiguration().getAdvanceTime();
		
		this.data.setTime(new SimTime(this.data.getConfiguration().getInterval()[0],d));
	}

	/**
	 * @return the configuration
	 */
	public synchronized final Configuration getConfiguration() {
		return data.getConfiguration();
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		if (!validated) {
			throw new IllegalStateException("Engine is not validated");
		}
		final Instant endOfInterval=this.data.getConfiguration().getInterval()[1];
		while (this.data.getTime().getTime().isBefore(endOfInterval)) {
			this.stateMachineSystem.signal(stateMachineSystem.getTick());
			this.stateMachineSystem.evaluate();
			this.data.getTime().advanceTime();
		}
	}


	
	public final Instant getTime() {
		return this.data.getTime().getTime();
	}
	
	public synchronized void validate() {
		stateMachineSystem.validate();
		this.validated=true;
	}
	
	public synchronized boolean isValidated() {
		return this.validated;
	}
	
	

}
