package stateMachine;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class MetaModeState extends NamedObjectInStateMachineSystem implements Iterable<ModeState>{
	private HashMap<String,ModeState> metaModeSet=new HashMap<String,ModeState>();
	private HashSet<Mode.Type> metaModeTypeSet=new HashSet<Mode.Type>();

	public MetaModeState(String name,StateMachineSystem sms,Set<Mode.Type> typeSet) {
		super(name,sms);
		metaModeTypeSet.addAll(typeSet);
		
	}
	
	public synchronized void addModeState(ModeState modeState) {
//		if (!this.metaModeTypeSet.contains(modeState.getType())) {
//			throw new IllegalArgumentException("Incorrect type "+modeState.getType().getName()+" in meta mode state "+this.getName());
//		}
		this.metaModeSet.put(modeState.getName(),modeState);
	}
	public synchronized void removeModeState(ModeState modeState) {
		if (!this.metaModeTypeSet.contains(modeState.getType())) {
			throw new IllegalArgumentException("Incorrect type "+modeState.getType().getName()+" in meta mode state "+this.getName());
		}
		this.metaModeSet.remove(modeState.getName());
	}
	public synchronized boolean contains(ModeState modeState) {
		if (!this.metaModeTypeSet.contains(modeState.getType())) {
			throw new IllegalArgumentException("Incorrect type "+modeState.getType().getName()+" in meta mode state "+this.getName());
		}
		return this.metaModeSet.containsKey(modeState.getName());
	}

	@Override
	public synchronized Iterator<ModeState> iterator() {
		return this.metaModeSet.values().iterator();
	}
	
	public synchronized Set<Mode.Type> getTypeSet() {
		return this.metaModeTypeSet;
	}
	public synchronized boolean  containsType(Mode.Type type) {
		return this.metaModeTypeSet.contains(type);
	}

	public void removeMode(String modeStateName,State modeName) {
		if (!this.metaModeSet.containsKey(modeStateName)) {
			throw new IllegalArgumentException("No ModeState called "+modeStateName+" does exists");
		}
		this.metaModeSet.get(modeStateName).removeValue(modeName);
	}

	public final void addMode(String modeStateName,State modeName) {
		if (!this.metaModeSet.containsKey(modeStateName)) {
			throw new IllegalArgumentException("No ModeState called "+modeStateName+" does exists");
		}
		this.metaModeSet.get(modeStateName).addValue(modeName);
	}
	public final ModeState getModeState(String modeStateName) {
		return this.metaModeSet.get(modeStateName);
	}
	

}
