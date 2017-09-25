package simulationBase;

import java.time.Duration;
import java.time.Instant;

import stateMachine.Timestamp;

public class SimTime {
	private Instant time;
	private Duration advanceDuration;
	private int nextCount=0;
	public SimTime(Instant startTime,Duration advanceDuration) {
		this.time=startTime;
		this.advanceDuration=advanceDuration;
	}
	
	public Instant getTime() {
		return time;
	}
	
	public void advanceTime() {
		time=time.plus(advanceDuration);
		this.nextCount=0;
	}
	
	public synchronized Timestamp generateTimestamp() {
		Timestamp ts=new Timestamp(this.time,this.nextCount);
		this.nextCount++;
		return ts;
	}

}
