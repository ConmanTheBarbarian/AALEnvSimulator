package simulationBase;

import java.time.Duration;
import java.time.Instant;

public class SimTime {
	private Instant time;
	private Duration advanceDuration;
	public SimTime(Instant startTime,Duration advanceDuration) {
		this.time=startTime;
		this.advanceDuration=advanceDuration;
	}
	
	public Instant getTime() {
		return time;
	}
	
	public void advanceTime() {
		time=time.plus(advanceDuration);
	}

}
