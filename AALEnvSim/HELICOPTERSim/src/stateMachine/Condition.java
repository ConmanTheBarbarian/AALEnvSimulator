package stateMachine;

import java.util.TreeSet;

import simulationBase.Base;



public  class Condition extends NamedObjectInStateMachineSystem implements Evaluation {
	
	public class DoubleRange extends Range {

		DoubleRange(Double start, Double end) {
			super(start, end);
			if (start.compareTo(end)>0) {
				throw new IllegalArgumentException("Start must be less or equal to end");
			}
		}
		public final Double getEnd() {
			return (Double)this.end;
		}
		public final Double getStart() {
			return (Double)this.start;
		}


	}

	public  class IntegerRange extends Range {

		IntegerRange(Integer start, Integer end) {
			super(start, end);
			if (start.compareTo(end)>0) {
				throw new IllegalArgumentException("Start must be less or equal to end");
			}
		}

		public final Integer getEnd() {
			return (Integer)this.end;
		}
		public final Integer getStart() {
			return (Integer)this.start;
		}
		
		

	}

	public  abstract class Range implements Comparable<Range>{
		private static final double precision=Base.epsilon;
		protected Object end;
		protected Object start;
		Range(Object start,Object end) {
			this.start=start;
			this.end=end;
		}
		
		@Override
		public int compareTo(Range arg0) {
			if (this instanceof IntegerRange) {
				if (arg0 instanceof IntegerRange) {
					final int n=((IntegerRange)this).getStart().compareTo(((IntegerRange)arg0).getStart());
					if (n!=0) {
						return n;
					}
					return ((IntegerRange)this).getEnd().compareTo(((IntegerRange)arg0).getEnd());
				} else if (arg0 instanceof DoubleRange) {
					final int n=((IntegerRange)this).getStart().compareTo((int) Math.round(((DoubleRange)arg0).getStart()));
					if (n!=0) {
						return n;
					}
					return ((IntegerRange)this).getEnd().compareTo((int) Math.round(((DoubleRange)arg0).getEnd()));
				} else {
					throw new IllegalStateException("Unhandled class");
				}
				
			} else if (this instanceof DoubleRange) {
				if (arg0 instanceof IntegerRange) {
				} else if (arg0 instanceof DoubleRange) {
					final int n1=((DoubleRange)this).getStart().compareTo(((DoubleRange)arg0).getStart()-precision);
					final int n2=((DoubleRange)this).getStart().compareTo(((DoubleRange)arg0).getStart()+precision);
					if ((n1<0 && n2<0) ||(n1>0 && n2>0)) {
						return n1;
					} else if (n1>=0 && n2<=0) {
						final int n11=((DoubleRange)this).getEnd().compareTo(((DoubleRange)arg0).getEnd()-precision);
						final int n12=((DoubleRange)this).getEnd().compareTo(((DoubleRange)arg0).getEnd()+precision);
						if ((n11<0&&n12<0)||(n11>0 && n12>0)) {
							return n11;
						} else if (n11>=0 && n12<=0) {
							return 0;
						} else {
							throw new IllegalStateException("Incorrect comparison, impossible state, HELP!!!");							
						}
					} else {
						throw new IllegalStateException("Incorrect comparison, impossible state, HELP!!!");
					}
				} else {
					throw new IllegalStateException("Unhandled class");
				}
				
				
			} else { 
				throw new IllegalStateException("Unhandled class");
			}
			return 0;				
		}

	}

	public class RangeSet {
		private TreeSet<Range> rangeSet=new TreeSet<Range>();

	}

	public static class Relation {
		public enum Operator {in,notIn};

	}



	/**
	 * @param name
	 * @param sms
	 */
	public Condition(String name, StateMachineSystem sms) {
		super(name, sms);
		sms.addCondition(this);
	}

	@Override
	public boolean evaluate() {
		throw new IllegalArgumentException("You must call evaluate(StateMachine) on Condition objects");
	}

	@Override
	public boolean evaluate(EventOccurrence eventOccurrence) {
		throw new IllegalArgumentException("You must call evaluate(StateMachine) on Condition objects");
	}

	public synchronized boolean evaluate(final StateMachine sm) {
		return false;
	}

	@Override
	public boolean evaluate(StateMachine sm, EventOccurrence eventOccurrence) {
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Condition [getName()=" + getName() + "]";
	}


}
