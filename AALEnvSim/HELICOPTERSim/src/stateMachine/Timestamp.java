package stateMachine;

import java.time.Instant;

public class Timestamp implements Comparable<Timestamp> {
	private Instant timestamp;
	private int count;



	/**
	 * @return the timestamp
	 */
	public synchronized final Instant getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the count
	 */
	public synchronized final int getCount() {
		return count;
	}

	/**
	 * @param timestamp
	 * @param count
	 */
	public Timestamp(Instant timestamp, int count) {
		this.timestamp = timestamp;
		this.count=count;
	}
	
	public Timestamp(Instant timestamp) {
		this.timestamp = timestamp;
		this.count=-1;		
	}
	

	@Override
	public int compareTo(Timestamp arg0) {
		final int n=this.timestamp.compareTo(arg0.timestamp);
		if (n!=0) {
			return n;
		}
		return this.count-arg0.count;
	}
	

}
