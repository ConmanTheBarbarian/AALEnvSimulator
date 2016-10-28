package stateMachine;

import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Combination<T extends  NamedObject & Evaluation> implements Iterator<Combination>, Comparable<Combination> {
	
	public static interface DomainInterface {
		public int getDomainSize();
		
	}
	
	public static abstract class Domain implements DomainInterface, Comparable<Domain> {
		protected static HashMap<NamedObject,Domain> no2d=new HashMap<NamedObject,Domain>();
		private int domainSize;
		private NamedObject namedObject;
		

		/**
		 * @param domainSize
		 */
		protected Domain(NamedObject namedObject,int domainSize) {
			this.namedObject=namedObject;
			this.domainSize = domainSize;
		}

		/**
		 * @return the domainSize
		 */
		public synchronized final int getDomainSize() {
			return domainSize;
		}
		
		public synchronized final NamedObject getNamedObject() {
			return namedObject;
		}
		
		public int compareTo(Domain d) {
			return this.namedObject.compareTo(d.getNamedObject());
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Domain [namedObject=%s, domainSize=%s]",
					namedObject, domainSize);
		}
		

		
	}
	public static class BooleanDomain extends Domain {

		/**
		 * @param b
		 */
		private BooleanDomain(NamedObject namedObject) {
			super(namedObject,2);
		}
		public  static Domain getDomain(NamedObject namedObject) {
			synchronized(no2d) {
				Domain d=no2d.get(namedObject);
				if (d!=null) {
					return d;
				}
				d=new BooleanDomain(namedObject);
				no2d.put(namedObject,d);
				return d;
			}
		}

		
		
	};
	
	
	public static class IntegerDomain extends Domain {

		/**
		 * @param i
		 */
		private IntegerDomain(NamedObject namedObject,int sz) {
			super(namedObject,sz);
		}

		public static Domain getDomain(NamedObject namedObject,int sz) {
			synchronized(no2d) {
				Domain d=no2d.get(namedObject);
				if (d!=null) {
					return d;
				}
				d=new IntegerDomain(namedObject,sz);
				no2d.put(namedObject, d);
				return d;
			}
		}



	};
	
	public static class StateDomain extends Domain {
		private Mode.Type modeType;
		private StateDomain(NamedObject namedObject,Mode.Type type) {
			super(namedObject,type.size());
			this.modeType=type;
			for (State state:modeType) {
				
			}
		}
		
		public static Domain getDomain(StateMachine stateMachine) {
			synchronized(no2d) {
				Domain d=no2d.get(stateMachine);
				if (d!=null) {
					return d;
				}
				d=new StateDomain(stateMachine,stateMachine.getType());
				return d;
			}
			
		}
		public final Mode.Type getType() {
			return modeType;
		}
//		public Combination.Value getValue() {
//			
//		}
	}
	

	public static class Type {
		private Vector<Domain> elementType=new Vector<Domain>();
		private HashMap<NamedObject,Domain> s2d=new HashMap<NamedObject,Domain>();
		public Type(Domain[] elementArray) {
			this.elementType.addAll(Arrays.asList(elementArray));
			Collections.sort(elementType);
			elementType.stream().forEach(t->s2d.put(t.getNamedObject(), t));
		}
		public Type(Collection<Domain> elementCollection) {
			this.elementType.addAll(elementCollection);
			Collections.sort(elementType);
			elementType.stream().forEach(t->s2d.put(t.getNamedObject(), t));
		}
		public final Vector<Domain> getElementType() {
			return elementType;
		}
		public final Domain getType(final NamedObject namedObject) {
			return s2d.get(namedObject);
		}

	};
	public static class Value {
		private Object object;
		public Value(Boolean b) {
			this.object=b;
		}
		public Value (Integer i) {
			if (i<0) {
				throw new IllegalArgumentException("Integer index cannot be less than zero");
			}
			this.object=i;
		}
		public Value(State s) {
			this.object=s;
		}
		public Integer getInteger() {
			if (!isClass(Integer.class)) {
				throw new IllegalStateException("Value is not an Integer");
			}
			return (Integer)object;
		}
		public Boolean getBoolean() {
			if (!isClass(Boolean.class)) {
				throw new IllegalStateException("Value is not an Boolean");
			}
			return (Boolean)object;
		}
		public State getState() {
			if (!isClass(State.class)) {
				throw new IllegalStateException("Value is not a state");
			}
			return (State)object;
		}
		public int getIndex() {
			if (isClass(Boolean.class)) {
				return getBoolean()?1:0;
			} else if (isClass(Integer.class)) {
				return getInteger();
			} else if (isClass(State.class)) {
				return getState().getStateMachine().getType().indexOf(getState());
			} else {
				throw new IllegalStateException("Impossible state (or perhaps not so impossible after all, if you read this) reached");
			}
		}

		public boolean isClass(Class<?> cls) {
			return cls.isInstance(this.object);
		}
		public Class<?> getType() {
			return object.getClass();
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if (isClass(Boolean.class)) {
				return String.format("Value [object=%s]", (Boolean)object);
			} else {
				return String.format("Value [object=%s]", (Integer)object);				
			}
		}
		

	};
	public static class Assignment implements Comparable<Assignment> {
		private Domain domain;
		private Value value;
		/**
		 * @param namedObject
		 * @param value
		 */
		public Assignment(Domain namedObject, Value value) {
			this.domain = namedObject;
			this.value = value;
		}
		/**
		 * @return the namedObject
		 */
		public synchronized final Domain getDomain() {
			return domain;
		}
		/**
		 * @return the value
		 */
		public synchronized final Value getValue() {
			return value;
		}
		@Override
		public synchronized int compareTo(Assignment o) {
			return this.getDomain().getNamedObject().compareTo(o.getDomain().getNamedObject());
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Assignment [domain=%s, value=%s]", domain,
					value);
		}
		
		
	}
	private BigInteger currentValue=BigInteger.ZERO;
	private BigInteger end;
	private Type type;
	public Combination(Type type) {
		this.type=type;
		this.end=BigInteger.ONE;
		for (Domain d:type.getElementType()) {
			this.end=this.end.multiply(BigInteger.valueOf(d.getDomainSize()));
		}
		this.end=this.end.subtract(BigInteger.ONE);
	}

	public Assignment[] currentCombination() {
		BigInteger remainder=currentValue;
		final Vector<Domain> et=this.type.getElementType();

		final Assignment[] result=new Assignment[et.size()];
		int i=0;
		while (remainder.compareTo(BigInteger.ZERO)!=0) {
			final BigInteger temp[]=remainder.divideAndRemainder(BigInteger.valueOf(et.get(i).getDomainSize()));
			if (et.get(i) instanceof BooleanDomain) {
				result[i]=new Assignment(et.get(i),new Value(temp[1].intValue()!=0?true:false));
			} else {
				result[i]=new Assignment(et.get(i),new Value(temp[1].intValue()));				
			}
			remainder=temp[0];
			++i;
		}
		for (int j=i; j<et.size(); ++j) {
			if (et.get(j) instanceof BooleanDomain) {
				result[j]=new Assignment(et.get(j),new Value(false));
			} else {
				result[j]=new Assignment(et.get(j),new Value(0));				
			}
			
		}
		return result;
	}
	public final void setCurrentCombination(Assignment[] assignment) {
		setCurrentCombination(Arrays.asList(assignment));
	}
	public final void setCurrentCombination(List<Assignment> assignment) {
		BigInteger result=BigInteger.ZERO;
		for (int i=assignment.size()-1; !(i<0); --i) {
			final Assignment a=assignment.get(i);
			final Value v=a.getValue();
			result=result.add(BigInteger.valueOf(v.getIndex()));
			if (i>0) {
				final int ds=this.type.getElementType().get(i).getDomainSize();
				result=result.multiply(BigInteger.valueOf(ds));
			}
		}
		this.currentValue=result;
	}
	public BigInteger getCurrentIndex() {
		if (this.end.compareTo(this.currentValue)<0) {
			throw new IllegalStateException("Index is not in range");
		}
		return this.currentValue;
	}
	@Override
	public boolean hasNext() {
		
		return this.end.compareTo(this.currentValue)>0 && !(BigInteger.ZERO.compareTo(this.currentValue)>0);
	}

	@Override
	public Combination next() {
		this.currentValue=this.currentValue.add(BigInteger.ONE);
		return this;
	}
	public void reset() {
		this.currentValue=BigInteger.ZERO;
	}

	@Override
	public int compareTo(Combination arg0) {
		return this.getCurrentIndex().compareTo(arg0.getCurrentIndex());
	}
	
	public final synchronized BigInteger getDomainSize() {
		return this.end.add(BigInteger.ONE);
	}

}
