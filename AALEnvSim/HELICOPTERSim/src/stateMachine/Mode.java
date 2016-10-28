package stateMachine;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Mode   implements Comparable<Mode> {
	private static HashMap<StateMachine,HashMap<State,Mode>> stateMachine2Mode=new HashMap<StateMachine,HashMap<State,Mode>>();
	private StateMachine stateMachine;

	public static class Type extends NamedObject implements Iterable<State> {
		private static TreeSet<Type> typeSet=new TreeSet<Type>();
		private Type(String name) {
			super(name);
			if (typeSet.parallelStream().anyMatch(t->t.getName().compareTo(name)==0)) {
				throw new IllegalArgumentException("Type "+name+" already in use");
			}
			typeSet.add(this);
		}
		public static final synchronized Type getType(final String name) {
			Set<Type> resultSet=typeSet.stream().filter(t->t.getName().compareTo(name)==0).collect(Collectors.toSet());
			if (resultSet.isEmpty()) {
				Type type=new Type(name);
				typeSet.add(type);
				return type;
			} else {
				return resultSet.iterator().next();
			}
			
		}

		private State defaultValue;
		private HashSet<State> modeSet=new HashSet<State>();
		private HashMap<State,Boolean> mode2enabler=new HashMap<State,Boolean>();



		/**
		 * @param e
		 * @return
		 * @see java.util.HashSet#add(java.lang.Object)
		 */
		synchronized boolean add(State e) {
			mode2enabler.put(e, false);
			return modeSet.add(e);
		}
		/**
		 * @param c
		 * @return
		 * @see java.util.AbstractCollection#addAll(java.util.Collection)
		 */
		
		@SuppressWarnings("unchecked")
		synchronized boolean addAll(Collection<?> c) {
			if (!c.parallelStream().allMatch(p->p instanceof String)) {
				throw new IllegalArgumentException("Collection contains non-String objects");
			}
			c.parallelStream().forEach(p->mode2enabler.put((State) p, false));
			return modeSet.addAll((Collection<? extends State>) c);
		}

		/**
		 * 
		 * @see java.util.HashSet#clear()
		 */
		synchronized void clearModes() {
			modeSet.clear();
			mode2enabler.clear();
		}

		/**
		 * @param o
		 * @return
		 * @see java.util.HashSet#contains(java.lang.Object)
		 */
		public  synchronized boolean contains(State o) {
			return modeSet.contains(o);
		}

		/**
		 * @param c
		 * @return
		 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
		 */
		public  synchronized boolean containsAll(Collection<?> c) {
			if (!c.parallelStream().allMatch(p->p instanceof State)) {
				throw new IllegalArgumentException("Collection contains non-String objects");
			}
			return modeSet.containsAll(c);
		}

		public  synchronized State getDefaultValue() {
			return this.defaultValue;
		}

		/**
		 * @return
		 * @see java.util.HashSet#isEmpty()
		 */
		public synchronized  boolean noModes() {
			return modeSet.isEmpty();
		}

		/**
		 * @return
		 * @see java.util.HashSet#iterator()
		 */
		
		
		public synchronized Iterator<State> iterator() {
			return modeSet.iterator();
		}

		/**
		 * @param o
		 * @return
		 * @see java.util.HashSet#remove(java.lang.Object)
		 */
		synchronized  boolean remove(State o) {
			mode2enabler.remove(o);
			return modeSet.remove(o);
		}

		/**
		 * @param c
		 * @return
		 * @see java.util.AbstractSet#removeAll(java.util.Collection)
		 */
		
		synchronized  boolean removeAll(Collection<?> c) {
			c.parallelStream().forEach(p->mode2enabler.remove(p));
			return modeSet.removeAll(c);
		}

		public  synchronized void setDefaultValue(State value) {
			if (!modeSet.contains(value)) {
				throw new IllegalArgumentException("Value "+value+" is not of type "+this.getName());
			}
			this.defaultValue=value;
		}

		/**
		 * @return
		 * @see java.util.HashSet#size()
		 */
		public  synchronized int size() {
			return modeSet.size();
		}

		/**
		 * @return
		 * @see java.util.AbstractCollection#toArray()
		 */
		public  synchronized Object[] toArray() {
			return modeSet.toArray();
		}

		/**
		 * @param a
		 * @return
		 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
		 */
		public  synchronized <T> T[] toArray(T[] a) {
			return modeSet.toArray(a);
		}
		
		public synchronized Stream<State> parallelStreamOfModes() {
			return modeSet.parallelStream();
		}
		public synchronized final boolean isEnabler(State value) {
			if (!modeSet.contains(value)) {
				throw new IllegalArgumentException("Value "+value+" is not of type "+this.getName());
			}
			return mode2enabler.get(value);
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Type [defaultValue=" + defaultValue + ", getName()="
					+ getName() + "]";
		}
		
		public Type getElementAt(int index) {
			if (index<0 || index >=this.typeSet.size()) {
				throw new IllegalArgumentException("Index out of range");
			}
			Type result=null;
			Iterator<Type> tsi=typeSet.iterator();
			for (int i=0; i<=index; ++i) {
				result=tsi.next();
			}
			return result;
		}
		public int indexOf(State state) {
			int n=0;
			boolean found=false;
			for (State s:modeSet) {
				if (s.equals(state)) {
					found=true;
					break;
				}
				++n;
			}
			if (!found) {
				throw new IllegalStateException("State not found where it was expected");
			}
			return n;
		}

	}
	private State value;
	/**
	 * @param name
	 * @param sms
	 */
	private Mode(StateMachine stateMachine,State value) {
		if (!stateMachine.getType().contains(value)) {
			throw new IllegalArgumentException("Value "+value+" is not of type "+this.getType().getName());
		}
		this.stateMachine=stateMachine;
		this.value=value;

	}
	
	public static final Mode getMode(StateMachine stateMachine,State value) {
		Mode result;
		final State state=value;
		if (!stateMachine.getType().contains(value)) {
			throw new IllegalArgumentException("No such state "+value+" in state machine");
		}
		synchronized(stateMachine2Mode) {
			HashMap<State,Mode> string2mode=stateMachine2Mode.get(stateMachine);
			if (string2mode==null) {
				string2mode=new HashMap<State,Mode>();
				stateMachine2Mode.put(stateMachine, string2mode);
			}
			result=string2mode.get(value);
			if (result==null) {
				result=new Mode(stateMachine,value);
				string2mode.put(value, result);
			}
		}
		return result;
		
	}





	public synchronized final Type getType() {
		 return stateMachine.getType();
	}
	public synchronized final State getValue() {
		return this.value;
	}
	public synchronized final String getQualifiedValue() {
		return this.stateMachine.getName()+":"+this.getValue();
	}





	@Override
	public int compareTo(Mode o) {
		return this.value.compareTo(o.value);
	}





	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	




	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Mode [value=" + value + ", type=" + stateMachine.getType() + "]";
	}





	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Mode) {
			return this.value.equals(((Mode)obj).value);
		} else {
			throw new IllegalArgumentException("Object of class "+obj.getClass().getName()+" submitted to equals of Mode");
		}
	}
	
	
	


}
