package stateMachine;

import java.util.HashSet;
import java.util.stream.Stream;

public class ModeState extends NamedObjectInStateMachineSystem {
	private HashSet<Mode> modeSet=new HashSet<Mode>();
	private StateMachine stateMachine;

	ModeState(String name,StateMachineSystem sms,StateMachine stateMachine) {
		super(name,sms);
		this.stateMachine=stateMachine;
	}
	public synchronized final void addValue(State value) {
		Mode mode=Mode.getMode(this.stateMachine, value);
		this.modeSet.add(mode);
	}
	public synchronized final void addValue(Mode mode) {
		if (!mode.getType().equals(this.getType())) {
			throw new IllegalArgumentException("Tries to add mode "+mode.getValue()+" to mode "+this.getName()+" of type "+this.getType().getName());
		}
		this.modeSet.add(mode);
	}
	public synchronized final HashSet<Mode> getValueSet() {
		return this.modeSet;
	}

	/**
	 * @return
	 * @see java.lang.String#hashCode()
	 */
	public int hashCode() {
		return modeSet.hashCode();
	}

	public synchronized final void removeValue(State value) {
		final Mode mode=Mode.getMode(this.stateMachine,value);
		this.modeSet.remove(mode);
	}
	
	public synchronized final boolean containsValues(State value) {
		final Mode mode=Mode.getMode(this.stateMachine,value);
		return this.modeSet.contains(mode);
	}

	public synchronized final void removeValue(Mode mode) {
		this.modeSet.remove(mode);
	}
	
	public synchronized final boolean containsValues(Mode mode) {
		return this.modeSet.contains(mode);
	}

	
	
	public synchronized Stream<Mode> parallelStreamOfModes() {
		return modeSet.parallelStream();
	}
	/**
	 * @return the type
	 */
	public synchronized final Mode.Type getType() {
		return this.stateMachine.getType();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ModeState [getName()=" + getName() + ", modeSet=" + modeSet
				+ ", type=" + this.stateMachine.getType() + ", stateMachine="+stateMachine.getName()+"]";
	}
	
	

}
