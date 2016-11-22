package stateMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class StateMachineGroup extends NamedObjectInStateMachineSystem implements Evaluation {
	
	public static class StateMachineOrGroup implements Evaluation {
		private NamedObjectInStateMachineSystem noisms;
		StateMachineOrGroup(final StateMachine sm) {
			if (sm==null) {
				throw new IllegalArgumentException("State machine is null");
			}
			this.noisms=sm;
		}
		StateMachineOrGroup(final StateMachineGroup smg) {
			if (smg==null) {
				throw new IllegalArgumentException("State machine group is null");
			}
			this.noisms=smg;
		}
		@Override
		public boolean evaluate() {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public boolean evaluate(StateMachine sm) {
			throw new IllegalArgumentException("State machine groups do not call evaluate with StateMachine as an argument");

		}
		public final StateMachine getStateMachine() {
			if (!this.isStateMachine()) {
				throw new IllegalArgumentException("The object contains a state machine group, not a state machine");
			}
			return (StateMachine)this.noisms;
		}
		public final StateMachine getStateMachineGroup() {
			if (this.isStateMachine()) {
				throw new IllegalArgumentException("The object contains a state machine, not a state machine group");
			}
			return (StateMachine)this.noisms;
		}
		public final boolean isStateMachine() {
			return this.noisms instanceof StateMachine;
		}
	};
	
	private StateMachineGroup parent;
	private HashMap<String,StateMachine> s2sm=new HashMap<String,StateMachine>();
	private HashMap<String,StateMachineOrGroup> s2smosmg=new HashMap<String,StateMachineOrGroup>();
	private HashMap<String,Variable<?>> s2v=new HashMap<String,Variable<?>>();
	
	StateMachineGroup(String name, StateMachineSystem sms,StateMachineGroup smg) {
		super(name, sms);
		this.parent=smg;
	}
	

	
	public final synchronized void add(final StateMachine sm) {
		StateMachineOrGroup smog=s2smosmg.get(sm.getName());
		if (smog!=null) {
			throw new IllegalArgumentException("State machine or group"+sm.getName()+" is already member of the state machine group "+this.getName());
		}
		smog=new StateMachineOrGroup(sm);
		s2smosmg.put(sm.getName(), smog);
		
	}
	public final synchronized void add(final StateMachineGroup smg) {
		StateMachineOrGroup smog=s2smosmg.get(smg.getName());
		if (smog!=null) {
			throw new IllegalArgumentException("State machine or group"+smg.getName()+" is already member of the state machine group "+this.getName());
		}
		smog=new StateMachineOrGroup(smg);
		s2smosmg.put(smg.getName(), smog);
		
	}


	synchronized void addVariable(Variable<?> v) {
		s2v.put(v.getLocalName(),v);
		
	}
	public final synchronized boolean contains(StateMachine sm) {
		final StateMachineOrGroup smog=s2smosmg.get(sm.getName());
		return smog!=null && smog.isStateMachine();
	}
	public final synchronized boolean contains(StateMachineGroup smg) {
		final StateMachineOrGroup smog=s2smosmg.get(smg.getName());
		return smog!=null && !smog.isStateMachine();
		
	}
	public final synchronized boolean contains(final String name) {
		return this.s2smosmg.containsKey(name);
	}
	@Override
	public boolean evaluate() {
		for (StateMachineOrGroup smog:s2smosmg.values()) {
			smog.evaluate();
		}
		return false;
	}
	@Override
	public boolean evaluate(StateMachine sm) {
		throw new IllegalArgumentException("State machine groups do not call evaluate with StateMachine as an argument");
	}
	public final synchronized StateMachineOrGroup get(final String name) {
		final StateMachineOrGroup smog=s2smosmg.get(name);
		if (smog==null) {
			throw new IllegalStateException("No such state machine or group exists: "+name);
		}
		return smog;
	}
	public final synchronized Collection<StateMachineOrGroup> getChildren() {
		return this.s2smosmg.values();
	}
	/* (non-Javadoc)
	 * @see stateMachine.NamedObjectInStateMachineSystem#getStateMachineBackEnd(stateMachine.NamedObjectInStateMachineSystem, java.lang.String[])
	 */
	@Override
	NamedObjectInStateMachineSystem getNamedObjectInStateMachineSystemBackEnd(NamedObjectInStateMachineSystem noism,
			List<Path.Part> parts, int currentPosition) {
		if (currentPosition<0 || currentPosition>=parts.size()) {
			throw new IllegalArgumentException("Current position "+currentPosition+" out of range");
		}
		Path.Part head=parts.get(currentPosition);
		NamedObjectInStateMachineSystem nextNoism=null;
		if (head.getPart().compareTo(".")==0) {
			nextNoism=noism;
		}
		if (head.getPart().compareTo("..")==0) {
			if (((StateMachineGroup)noism).parent==null) {
				throw new IllegalArgumentException("There is nothing above the root element");
			}
			nextNoism=((StateMachineGroup)noism).parent;
		}
		final StateMachineOrGroup smog=s2smosmg.get(head.toString());
		if (smog!=null) {
			if (smog.isStateMachine()) {
				nextNoism=smog.getStateMachine();
			} else {
				nextNoism=smog.getStateMachineGroup();
			}
		}
		final Variable<?> v=s2v.get(head.getPart());
		if (v!=null) {
			nextNoism=v;
		}
		
		final int nextPosition=currentPosition+1;
		if (nextPosition<parts.size()) {
			nextNoism=nextNoism.getNamedObjectInStateMachineSystemBackEnd(nextNoism, parts, nextPosition);
		} 
		if (nextNoism==null) {
			throw new IllegalArgumentException("Part "+noism.getQualifiedName()+"  has no child named"+head);
		}
		return nextNoism;
	}

	public final synchronized StateMachine getOrCreateStateMachine(final String name, long seed, Priority priority) {
		StateMachine sm=this.s2sm.get(name);
		if (sm==null) {
			sm=StateMachine.getStateMachine(name,this,seed,priority);
			this.add(sm);
			s2sm.put(name, sm);
		}
		return sm;
	}

	public final synchronized Variable<?> getOrCreateVariable(final Variable.Type type,final String name) {
		Variable<?> v=this.getVariable(name);
		if (v==null) {
			v=Variable.getVariable(type,name,this);
			this.addVariable(v);
		}
		return v;
	}
	
	@Override
	public synchronized String getQualifiedName() {
		String prefix;
		if (this.isRoot()) {
			prefix="/";
		} else {
			prefix=this.parent.getName()+"/";
		}
		return prefix+this.getName();
	}
	
	public synchronized Variable<?> getVariable(String name) {
		return s2v.get(name);
	}

	public final boolean isRoot() {
		return this.parent==null;
	}
	public final synchronized void remove(final StateMachine sm) {
		StateMachineOrGroup smog=s2smosmg.get(sm.getName());
		if (smog==null) {
			throw new IllegalArgumentException("State machine "+sm.getName()+" is not a member of the state machine group "+this.getName());
		}
		s2smosmg.remove(sm.getName());
		
	}
	public final synchronized void remove(final StateMachineGroup smg) {
		StateMachineOrGroup smog=s2smosmg.get(smg.getName());
		if (smog==null) {
			throw new IllegalArgumentException("State machine group "+smg.getName()+" is not a member of the state machine group "+this.getName());
		}
		s2smosmg.remove(smg.getName());
		
	}
	
	
}
