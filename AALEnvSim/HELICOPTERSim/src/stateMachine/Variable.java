package stateMachine;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import stateMachine.StateMachineGroup.StateMachineOrGroup;

public class Variable<T> extends NamedObjectInStateMachineSystem {

	private Event updateEvent;
	private Vector<T> vector=new Vector<T>();
	private StateMachineGroup stateMachineGroup;
	private Type type;
	private Variable(String name, StateMachineGroup smg) {
		super(name, smg.getStateMachineSystem());
		this.stateMachineGroup=smg;
	}
	
	public enum Type { Long, Double, String, Object, ObjectSet};
	
	public static class Pair {
		private Object element[]=new Object[2];
		public enum Position {first,second};
		public Pair(Object f, Object s) {
			this.element[0]=f;
			this.element[1]=s;
		}
		public final Object get(Position position) {
			switch (position) {
			case first:
				return this.element[0];
			case second:
				return this.element[1];

			
			}
			throw new IllegalArgumentException("Impossible");
		}
	}
	
	static  Variable<?> getVariable(final Type type, final String name, final StateMachineGroup smg) {
		Variable<?> v;
		switch (type) {
		case Double:
			v=new Variable<Double>(name,smg);
			break;
		case Long:
			v=new Variable<Long>(name,smg);
			break;
		case Object:
			v=new Variable<Object>(name,smg);
			break;
		case String:
			v=new Variable<String>(name,smg);
			break;
		case ObjectSet:
			v=new Variable<HashSet<Object>>(name,smg);
			break;
		default:
			throw new IllegalArgumentException("Impossible!");
		
		}
		final StateMachineSystem sms=smg.getStateMachineSystem();
		v.updateEvent=PrimitiveEvent.getPrimitiveEvent(name+"_updateEvent",sms);
		v.type=type;
		smg.addVariable(v);

		return v;
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see java.util.Vector#add(int, java.lang.Object)
	 */
	public void add(int arg0, T arg1) {
		vector.add(arg0, arg1);
		getStateMachineSystem().signal(updateEvent);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#add(java.lang.Object)
	 */
	public boolean add(T arg0) {
		getStateMachineSystem().signal(updateEvent);
		return vector.add(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> arg0) {
		getStateMachineSystem().signal(updateEvent);
		return vector.addAll(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see java.util.Vector#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int arg0, Collection<? extends T> arg1) {
		getStateMachineSystem().signal(updateEvent);
		return vector.addAll(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see java.util.Vector#addElement(java.lang.Object)
	 */
	public void addElement(T arg0) {
		getStateMachineSystem().signal(updateEvent);
		vector.addElement(arg0);
	}
	/**
	 * @return
	 * @see java.util.Vector#capacity()
	 */
	public int capacity() {
		return vector.capacity();
	}
	/**
	 * 
	 * @see java.util.Vector#clear()
	 */
	public void clear() {
		getStateMachineSystem().signal(updateEvent);
		vector.clear();
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#contains(java.lang.Object)
	 */
	public boolean contains(Object arg0) {
		return vector.contains(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> arg0) {
		return vector.containsAll(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#elementAt(int)
	 */
	public T elementAt(int arg0) {
		return vector.elementAt(arg0);
	}
	/**
	 * @return
	 * @see java.util.Vector#elements()
	 */
	public Enumeration<T> elements() {
		return vector.elements();
	}
	/**
	 * @return
	 * @see java.util.Vector#firstElement()
	 */
	public T firstElement() {
		return vector.firstElement();
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#get(int)
	 */
	public T get(int arg0) {
		return vector.get(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see java.util.Vector#indexOf(java.lang.Object, int)
	 */
	public int indexOf(Object arg0, int arg1) {
		return vector.indexOf(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#indexOf(java.lang.Object)
	 */
	public int indexOf(Object arg0) {
		return vector.indexOf(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see java.util.Vector#insertElementAt(java.lang.Object, int)
	 */
	public void insertElementAt(T arg0, int arg1) {
		getStateMachineSystem().signal(updateEvent);
		vector.insertElementAt(arg0, arg1);
	}
	/**
	 * @return
	 * @see java.util.Vector#isEmpty()
	 */
	public boolean isEmpty() {
		return vector.isEmpty();
	}
	/**
	 * @return
	 * @see java.util.Vector#iterator()
	 */
	public Iterator<T> iterator() {
		return vector.iterator();
	}
	/**
	 * @return
	 * @see java.util.Vector#lastElement()
	 */
	public T lastElement() {
		return vector.lastElement();
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see java.util.Vector#lastIndexOf(java.lang.Object, int)
	 */
	public int lastIndexOf(Object arg0, int arg1) {
		return vector.lastIndexOf(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object arg0) {
		return vector.lastIndexOf(arg0);
	}
	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return vector.parallelStream();
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#remove(int)
	 */
	public T remove(int arg0) {
		getStateMachineSystem().signal(updateEvent);
		return vector.remove(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#remove(java.lang.Object)
	 */
	public boolean remove(Object arg0) {
		getStateMachineSystem().signal(updateEvent);
		return vector.remove(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> arg0) {
		getStateMachineSystem().signal(updateEvent);
		return vector.removeAll(arg0);
	}
	/**
	 * 
	 * @see java.util.Vector#removeAllElements()
	 */
	public void removeAllElements() {
		getStateMachineSystem().signal(updateEvent);
		vector.removeAllElements();
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#removeElement(java.lang.Object)
	 */
	public boolean removeElement(Object arg0) {
		getStateMachineSystem().signal(updateEvent);
		return vector.removeElement(arg0);
	}
	/**
	 * @param arg0
	 * @see java.util.Vector#removeElementAt(int)
	 */
	public void removeElementAt(int arg0) {
		getStateMachineSystem().signal(updateEvent);
		vector.removeElementAt(arg0);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see java.util.Vector#set(int, java.lang.Object)
	 */
	public T set(int arg0, T arg1) {
		getStateMachineSystem().signal(updateEvent);
		return vector.set(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @see java.util.Vector#setElementAt(java.lang.Object, int)
	 */
	public void setElementAt(T arg0, int arg1) {	
		getStateMachineSystem().signal(updateEvent);

		vector.setElementAt(arg0, arg1);
	}
	/**
	 * @param arg0
	 * @see java.util.Vector#setSize(int)
	 */
	public void setSize(int arg0) {
		getStateMachineSystem().signal(updateEvent);

		vector.setSize(arg0);
	}
	/**
	 * @return
	 * @see java.util.Vector#size()
	 */
	public int size() {
		return vector.size();
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @see java.util.Vector#subList(int, int)
	 */
	public List<T> subList(int arg0, int arg1) {
		return vector.subList(arg0, arg1);
	}
	/**
	 * @return
	 * @see java.util.Vector#toArray()
	 */
	public Object[] toArray() {
		return vector.toArray();
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.Vector#toArray(java.lang.Object[])
	 */
	public <U> U[] toArray(U[] arg0) {
		return vector.toArray(arg0);
	}
	/**
	 * 
	 * @see java.util.Vector#trimToSize()
	 */
	public void trimToSize() {
		getStateMachineSystem().signal(updateEvent);
		vector.trimToSize();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Variable [getName()=" + getName() + ", vector=" + vector
				+ ", updateEvent=" + updateEvent + "]";
	}
	
	public synchronized Event getUpdateEvent() {
		return this.updateEvent;
	}
	
	@SuppressWarnings("unchecked")
	public Variable<T> add(Variable<T> v) {
		Variable<T> result=new Variable<T>("temp",this.getStateMachineGroup());
		if (this.size()!=v.size()) {
			throw new IllegalArgumentException("Incompatible length of sequences");
		}
		if (this.getType()!=v.getType()) {
			throw new IllegalArgumentException("Incompatible types of operations");			
		}
		switch (type) {
		case Double:
			for (int i=0; i<size(); ++i) {
				final Double tmp=((Double)this.get(i))+((Double)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Long:
			for (int i=0; i<size(); ++i) {
				final Long tmp=((Long)this.get(i))+((Long)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Object:
			throw new IllegalArgumentException("Cannot add objects");
		case ObjectSet:
			for (int i=0; i<size(); ++i) {
				final HashSet<Object> tmp=new HashSet<Object>();
				tmp.addAll((HashSet<Object>)this.get(i));
				tmp.addAll((HashSet<Object>)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case String:
			for (int i=0; i<size(); ++i) {
				final String tmp=((String)this.get(i))+((String)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		default:
			break;
		
		}
		

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Variable<T> subtract(Variable<T> v) {
		Variable<T> result=new Variable<T>("temp",this.getStateMachineGroup());
		if (this.size()!=v.size()) {
			throw new IllegalArgumentException("Incompatible length of sequences");
		}
		if (this.getType()!=v.getType()) {
			throw new IllegalArgumentException("Incompatible types of operations");			
		}
		switch (type) {
		case Double:
			for (int i=0; i<size(); ++i) {
				final Double tmp=((Double)this.get(i))-((Double)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Long:
			for (int i=0; i<size(); ++i) {
				final Long tmp=((Long)this.get(i))-((Long)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Object:
			throw new IllegalArgumentException("Cannot subtract objects");
		case ObjectSet:
			for (int i=0; i<size(); ++i) {
				final HashSet<Object> tmp=new HashSet<Object>();
				tmp.addAll((HashSet<Object>)this.get(i));
				tmp.removeAll((HashSet<Object>)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case String:
			throw new IllegalArgumentException("Cannot subtract strings");
		default:
			break;
		
		}
		

		return result;
	}

	@SuppressWarnings("unchecked")
	public Variable<T> multiply(Variable<T> v) {
		Variable<T> result=new Variable<T>("temp",this.getStateMachineGroup());
		if (this.size()!=v.size()) {
			throw new IllegalArgumentException("Incompatible length of sequences");
		}
		if (this.getType()!=v.getType()) {
			throw new IllegalArgumentException("Incompatible types of operations");			
		}
		switch (type) {
		case Double:
			for (int i=0; i<size(); ++i) {
				final Double tmp=((Double)this.get(i))*((Double)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Long:
			for (int i=0; i<size(); ++i) {
				final Long tmp=((Long)this.get(i))*((Long)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Object:
			throw new IllegalArgumentException("Cannot multiply objects");
		case ObjectSet:
			throw new IllegalArgumentException("Cartesian products are not supported");
//			for (int i=0; i<size(); ++i) {
//				final HashSet<Pair> tmp=new HashSet<Pair>();
//				final HashSet<Object> tmpSet1=(HashSet<Object>)this.get(i);
//				final HashSet<Object> tmpSet2=(HashSet<Object>)v.get(i);
//				for (Object o1:tmpSet1) {
//					for (Object o2:tmpSet2) {
//						final Pair p=new Pair(o1,o2);
//						tmp.add(p);
//					}
//				}
//			}
//			break;
		case String:
			throw new IllegalArgumentException("Cannot multiply strings");
		default:
			break;
		
		}
		

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Variable<T> divide(Variable<T> v) {
		Variable<T> result=new Variable<T>("temp",this.getStateMachineGroup());
		if (this.size()!=v.size()) {
			throw new IllegalArgumentException("Incompatible length of sequences");
		}
		if (this.getType()!=v.getType()) {
			throw new IllegalArgumentException("Incompatible types of operations");			
		}
		switch (type) {
		case Double:
			for (int i=0; i<size(); ++i) {
				final Double tmp=((Double)this.get(i))/((Double)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Long:
			for (int i=0; i<size(); ++i) {
				final Long tmp=((Long)this.get(i))/((Long)v.get(i)); 
				result.add((T)tmp);
			}
			break;
		case Object:
			throw new IllegalArgumentException("Cannot divide objects");
		case ObjectSet:
			throw new IllegalArgumentException("Cannot divide object sets");
//			for (int i=0; i<size(); ++i) {
//				final HashSet<Pair> tmp=new HashSet<Pair>();
//				final HashSet<Object> tmpSet1=(HashSet<Object>)this.get(i);
//				final HashSet<Object> tmpSet2=(HashSet<Object>)v.get(i);
//				for (Object o1:tmpSet1) {
//					for (Object o2:tmpSet2) {
//						final Pair p=new Pair(o1,o2);
//						tmp.add(p);
//					}
//				}
//			}
//			break;
		case String:
			throw new IllegalArgumentException("Cannot divide strings");
		default:
			break;
		
		}
		

		return result;
	}
	@SuppressWarnings("unchecked")
	public void set(Variable<T> v) {
		this.vector=v.vector;
	}
	/**
	 * @return the stateMachineGroup
	 */
	public synchronized final StateMachineGroup getStateMachineGroup() {
		return stateMachineGroup;
	}
	/**
	 * @return the type
	 */
	public synchronized final Type getType() {
		return type;
	}
	/* (non-Javadoc)
	 * @see stateMachine.NamedObjectInStateMachineSystem#getQualifiedName()
	 */
	@Override
	public synchronized String getQualifiedName() {
		return stateMachineGroup.getQualifiedName()+"/"+this.getName();
	}
	/* (non-Javadoc)
	 * @see stateMachine.NamedObjectInStateMachineSystem#getStateMachineBackEnd(stateMachine.NamedObjectInStateMachineSystem, java.lang.String[])
	 */
	@Override
	NamedObjectInStateMachineSystem getNamedObjectInStateMachineSystemBackEnd(NamedObjectInStateMachineSystem noism,
			List<Path.Part> parts, int currentPosition) {
		Path.Part head=parts.remove(0);
		if (head.getPart().compareTo(".")==0) {
			return this;
		}
		if (head.getPart().compareTo("..")==0) {
			return this.stateMachineGroup;
		}
		throw new IllegalArgumentException("Part "+noism.getQualifiedName()+"  has no children");
	}
	
	



}
