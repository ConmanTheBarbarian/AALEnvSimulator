package stateMachine;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import stateMachine.EventType.StateMachineSubscription;

public class EventDisjunctionType extends EventType {
	private HashSet<PrimitiveEventType> disjunction=new HashSet<PrimitiveEventType>();

	/**
	 * @param name
	 * @param sms
	 */
	public EventDisjunctionType(String name, StateMachineSystem sms) {
		super(name, sms, null);
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.HashSet#add(java.lang.Object)
	 */
	public  synchronized  boolean add(PrimitiveEventType e) {
		this.getSubscriberSet().parallelStream().forEach(smsu -> e.subscribe(smsu.getStateMachine(), null));
		return disjunction.add(e);
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public  synchronized  boolean addAll(Collection<?> c) {
		if (!c.parallelStream().allMatch(p->p instanceof PrimitiveEventType)) {
			throw new IllegalArgumentException("Collection contains non-PrimitiveEvent objects");
		}
		this.getSubscriberSet().parallelStream().forEach(
				smsu ->
				c.parallelStream().forEach(pe-> ((EventType) pe).subscribe(smsu.getStateMachine(), null)
				));

		return disjunction.addAll((Collection<? extends PrimitiveEventType>) c);
	}
	/**
	 * 
	 * @see java.util.HashSet#clear()
	 */
	public  synchronized   void clear() {
		this.getSubscriberSet().parallelStream().forEach(
				smsu ->
				disjunction.parallelStream().forEach(pe-> ((EventType) pe).unsubscribe(smsu.getStateMachine())
				));
		disjunction.clear();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.HashSet#contains(java.lang.Object)
	 */
	public   synchronized  boolean contains(Object o) {
		return disjunction.contains(o);
	}
	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public  synchronized  boolean containsAll(Collection<?> c) {
		if (!c.parallelStream().allMatch(p->p instanceof PrimitiveEventType)) {
			throw new IllegalArgumentException("Collection contains non-PrimitiveEvent objects");
		}

		return disjunction.containsAll(c);
	}
	/**
	 * @return
	 * @see java.util.HashSet#isEmpty()
	 */
	public  synchronized  boolean noEvents() {
		return disjunction.isEmpty();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.HashSet#remove(java.lang.Object)
	 */
	public  synchronized  boolean remove(PrimitiveEventType o) {
		this.getSubscriberSet().parallelStream().forEach(smsu -> o.unsubscribe(smsu.getStateMachine()));
		return disjunction.remove(o);
	}
	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public  synchronized  boolean removeAll(Collection<?> c) {
		if (!c.parallelStream().allMatch(p->p instanceof PrimitiveEventType)) {
			throw new IllegalArgumentException("Collection contains non-PrimitiveEvent objects");
		}
		this.getSubscriberSet().parallelStream().forEach(
				smsu ->
				c.parallelStream().forEach(pe-> ((EventType) pe).unsubscribe(smsu.getStateMachine())
				));

		return disjunction.removeAll(c);
	}
	/**
	 * @return
	 * @see java.util.HashSet#size()
	 */
	public  synchronized  int size() {
		return disjunction.size();
	}
	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public  synchronized  Object[] toArray() {
		return disjunction.toArray();
	}
	/**
	 * @param arg0
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public  synchronized  <T> T[] toArray(T[] arg0) {
		return disjunction.toArray(arg0);
	}
	
	public synchronized Stream<PrimitiveEventType> parallelStreamOfPrimitiveEvents() {
		return disjunction.parallelStream();
	}
	@Override
	public synchronized  void subscribe(final StateMachine stateMachine, Priority priority) {
		disjunction.parallelStream().forEach(pe -> pe.subscribe(stateMachine, null));
	}

	@Override
	public synchronized  void unsubscribe(final StateMachine stateMachine) {
		disjunction.parallelStream().forEach(pe -> pe.unsubscribe(stateMachine));

	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EventDisjunction [getName()=" + getName() + ", disjunction="
				+ disjunction + "]";
	}
	

}
