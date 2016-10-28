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
		public final boolean isStateMachine() {
			return this.noisms instanceof StateMachine;
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
		@Override
		public boolean evaluate(StateMachine sm) {
			throw new IllegalArgumentException("State machine groups do not call evaluate with StateMachine as an argument");

		}
		@Override
		public boolean evaluate() {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	private HashMap<String,StateMachineOrGroup> s2smosmg=new HashMap<String,StateMachineOrGroup>();
	private StateMachineGroup parent;
	private HashMap<String,Variable<?>> s2v=new HashMap<String,Variable<?>>();
	
	public final synchronized Variable<?> getOrCreateVariable(final Variable.Type type,final String name) {
		Variable<?> v=Variable.getVariable(type,name,this);
		this.addVariable(v);
		return v;
	}
	

	
	public final synchronized StateMachine getOrCreateStateMachine(final String name, long seed) {
		StateMachine sm=StateMachine.getStateMachine(name,this,seed);
		this.add(sm);
		return sm;
	}
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
	public final synchronized void remove(final StateMachine sm) {
		StateMachineOrGroup smog=s2smosmg.get(sm.getName());
		if (smog==null) {
			throw new IllegalArgumentException("State machine "+sm.getName()+" is not a member of the state machine group "+this.getName());
		}
		s2smosmg.remove(sm.getName());
		
	}
	public final synchronized void add(final StateMachineGroup smg) {
		StateMachineOrGroup smog=s2smosmg.get(smg.getName());
		if (smog!=null) {
			throw new IllegalArgumentException("State machine or group"+smg.getName()+" is already member of the state machine group "+this.getName());
		}
		smog=new StateMachineOrGroup(smg);
		s2smosmg.put(smg.getName(), smog);
		
	}
	public final synchronized void remove(final StateMachineGroup smg) {
		StateMachineOrGroup smog=s2smosmg.get(smg.getName());
		if (smog==null) {
			throw new IllegalArgumentException("State machine group "+smg.getName()+" is not a member of the state machine group "+this.getName());
		}
		s2smosmg.remove(smg.getName());
		
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
	public final synchronized boolean contains(final String name) {
		return this.s2smosmg.containsKey(name);
	}
	public final synchronized boolean contains(StateMachine sm) {
		final StateMachineOrGroup smog=s2smosmg.get(sm.getName());
		return smog!=null && smog.isStateMachine();
	}
	public final synchronized boolean contains(StateMachineGroup smg) {
		final StateMachineOrGroup smog=s2smosmg.get(smg.getName());
		return smog!=null && !smog.isStateMachine();
		
	}

	@Override
	public boolean evaluate(StateMachine sm) {
		throw new IllegalArgumentException("State machine groups do not call evaluate with StateMachine as an argument");
	}

	@Override
	public boolean evaluate() {
		for (StateMachineOrGroup smog:s2smosmg.values()) {
			smog.evaluate();
		}
		return false;
	}
	
	public final boolean isRoot() {
		return this.parent==null;
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

	synchronized void addVariable(Variable<?> v) {
		s2v.put(v.getName(),v);
		
	}
	public synchronized Variable<?> getVariable(String name) {
		return s2v.get(name);
	}
	/* (non-Javadoc)
	 * @see stateMachine.NamedObjectInStateMachineSystem#getStateMachineBackEnd(stateMachine.NamedObjectInStateMachineSystem, java.lang.String[])
	 */
	@Override
	NamedObjectInStateMachineSystem getNamedObjectInStateMachineSystemBackEnd(NamedObjectInStateMachineSystem noism,
			List<Path.Part> parts) {
		Path.Part head=parts.remove(0);
		if (head.getPart().compareTo(".")==0) {
			return this;
		}
		if (head.getPart().compareTo("..")==0) {
			if (this.parent==null) {
				throw new IllegalArgumentException("There is nothing above the root element");
			}
			return this.parent;
		}
		final StateMachineOrGroup smog=s2smosmg.get(head.getPart());
		if (smog!=null) {
			if (smog.isStateMachine()) {
				return smog.getStateMachine();
			} else {
				return smog.getStateMachineGroup();
			}
		}
		final Variable<?> v=s2v.get(head.getPart());
		if (v!=null) {
			return v;
		}
		
		throw new IllegalArgumentException("Part "+noism.getQualifiedName()+"  has no child named"+head);
	}
	
	
}
