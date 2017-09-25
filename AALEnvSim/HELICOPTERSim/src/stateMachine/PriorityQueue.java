package stateMachine;

import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class PriorityQueue<T extends Comparable<T>> {
	private TreeMap<Priority,TreeSet<T>> p2q=new TreeMap<Priority,TreeSet<T>>();

	public PriorityQueue() {
	}
	public synchronized void enqueue(T element,Priority priority) {
		TreeSet<T> v=p2q.get(priority);
		if (v==null) {
			v=new TreeSet<T>();
			p2q.put(priority,v);
		}
		v.add(element);
	}
	public synchronized T head() {
		for (Priority p:p2q.keySet()) {
			TreeSet<T> v=p2q.get(p);
			if (v!=null) {
				return v.first();
			}
		}
		return null;
	}
	public synchronized T dequeue() {
		for (Priority p:p2q.keySet()) {
			TreeSet<T> v=p2q.get(p);
			if (v!=null && v.size()>0) {
				return v.pollFirst();
			}
		}
		return null;
		
	}
	public synchronized T dequeue(Priority p) {
		TreeSet<T> v=p2q.get(p);
		if (v!=null && v.size()>0) {
			return v.pollFirst();
		}
		return null;
	}
	public synchronized T dequeueUntil(T element) {
		for (Priority p:p2q.keySet()) {
			TreeSet<T> v=p2q.get(p);
			if (v!=null && v.size()>0 && v.first().compareTo(element)<0) {
				return v.pollFirst();
			}
		}
		return null;
		
	}
	public synchronized T dequeueUntil(Priority p,T element) {
		TreeSet<T> v=p2q.get(p);
		if (v!=null && v.size()>0 && v.first().compareTo(element)<0) {
			return v.pollFirst();
		}
		return null;
	}
	public boolean isEmpty() {
		for (Priority p:p2q.keySet()) {
			TreeSet<T> v=p2q.get(p);
			if (!v.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	public boolean contains(T element) {
		for (Priority p:p2q.keySet()) {
			TreeSet<T> v=p2q.get(p);
			if (v.contains(element)) {
				return true;
			}
		}
		return false;
	}
	public boolean isEmptyUntil(T element) {
		for (Priority p:p2q.keySet()) {
			final TreeSet<T> v=p2q.get(p);
			if (v.first().compareTo(element)<0) {
				return false;
			}
		}
		return true;
	}

}
