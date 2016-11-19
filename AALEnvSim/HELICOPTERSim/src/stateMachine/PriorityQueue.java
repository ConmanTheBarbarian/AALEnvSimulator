package stateMachine;

import java.util.TreeMap;
import java.util.Vector;

public class PriorityQueue<T> {
	private TreeMap<Priority,Vector<T>> p2q=new TreeMap<Priority,Vector<T>>();

	public PriorityQueue() {
	}
	public synchronized void enqueue(T element,Priority priority) {
		Vector<T> v=p2q.get(priority);
		if (v==null) {
			v=new Vector<T>();
			p2q.put(priority,v);
		}
		v.add(element);
	}
	public synchronized T head() {
		for (Priority p:p2q.keySet()) {
			Vector<T> v=p2q.get(p);
			if (v!=null) {
				return v.get(0);
			}
		}
		return null;
	}
	public synchronized T dequeue() {
		for (Priority p:p2q.keySet()) {
			Vector<T> v=p2q.get(p);
			if (v!=null && v.size()>0) {
				return v.remove(0);
			}
		}
		return null;
		
	}
	public synchronized T dequeue(Priority p) {
		Vector<T> v=p2q.get(p);
		if (v!=null && v.size()>0) {
			return v.remove(0);
		}
		return null;
	}
	public boolean isEmpty() {
		for (Priority p:p2q.keySet()) {
			Vector<T> v=p2q.get(p);
			if (!v.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	public boolean contains(T updateEvent) {
		for (Priority p:p2q.keySet()) {
			Vector<T> v=p2q.get(p);
			if (!v.contains(updateEvent)) {
				return true;
			}
		}
		return false;
	}

}
