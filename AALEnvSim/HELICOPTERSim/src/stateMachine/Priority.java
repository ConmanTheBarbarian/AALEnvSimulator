package stateMachine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class Priority implements Comparable<Priority> {

	protected static Vector<String> priorities = new Vector<String>();
	private static HashMap<String,Priority> s2i = new HashMap<String,Priority>();
	private static HashMap<Priority,HashSet<String>> p2sos=new HashMap<Priority,HashSet<String>>();
	private static HashMap<String,Priority> alias2p=new HashMap<String,Priority>();
	protected int index = 0;
	
	/**
	 * @param priorityNames
	 */
	public  static void initializePriority(String[] priorityNames) {
		synchronized(priorities) {
			if (priorities.size()>0) {
				throw new IllegalStateException("Priorities are already initialized");
			}
			priorities.addAll(Arrays.asList(priorityNames));
		}
	}
	
	public synchronized static void initializePriority(int rangeStart,int rangeEnd) {
		synchronized(priorities) {
			if (priorities.size()>0) {
				throw new IllegalStateException("Priorities are already initialized");
			}
			int delta=0;
			if (rangeStart<rangeEnd) {
				delta=1;
			} else {
				delta=-1;
			}
			for (int i=rangeStart; i<rangeEnd; i+=delta) {
				priorities.add(Integer.toString(i));
			}
		}
	}
	/**
	 * @param name
	 */
	private  Priority(String name) {
		this.index=getIndex(name);
	}
	
	private Priority(int index) {
		this.index=index;
	}
	

	/**
	 * @param name
	 * @return
	 */
	public static synchronized Priority getPriority(String name) {
		checkInitialization();
		Priority p2=alias2p.get(name);
		if (!priorities.contains(name) &&  p2==null) {
			
			throw new IllegalArgumentException("The priority "+name+" does not exist");
		}
		if (p2!=null) {
			return p2;
		}
		Priority p=s2i.get(name);
		if (p==null) {
			p=new Priority(name);
			s2i.put(name, p);
		}
		return p;
		
	}
	
	/**
	 * @param value
	 * @return
	 */
	public final static synchronized Priority getPriority(int value) {
		return Priority.getPriority(Integer.toString(value));
	}

	/**
	 * 
	 */
	private static final void checkInitialization() {
		if (priorities.size()==0) {
			throw new IllegalStateException("Priorities are not initialized");
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public static synchronized int getIndex(String name) {
		checkInitialization();
		final int n=priorities.indexOf(name);
		if (n<0) {
			throw new IllegalArgumentException("The  \""+name+"\" is not a priority");
		}
		return n;
	}

	public Priority() {
		super();
	}

	public int getIndex() {
		return this.index;
	}

	@Override
	public int compareTo(Priority arg0) {
		checkInitialization();
		return this.index-arg0.index;
	}
	public final static void setAlias(Priority priority,String alias) {
		checkInitialization();
		Priority registeredPriority=alias2p.get(alias);
		if (registeredPriority!=null) {
			throw new IllegalArgumentException("Alias "+alias+" already in use");
		}
		if (priorities.indexOf(alias)>=0) {
			throw new IllegalArgumentException("The proposed alias is already a name of a priority");
		}
		alias2p.put(alias, priority);
		HashSet<String> sos=p2sos.get(priority);
		if (sos==null) {
			sos=new HashSet<String>();
			p2sos.put(priority, sos);
		}
		sos.add(alias);
		
	}
	public final static Priority getPriorityFromAlias(String alias) {
		return alias2p.get(alias);
	}

	static Priority getPriorityWithIndex(int i) {
		return new Priority(i);
	}

}