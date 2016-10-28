package stateMachine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

/**
 * @author melj
 *
 */
public class Priority implements Comparable<Priority> {
	private static Vector<String> priorities=new Vector<String>();
	private static HashMap<String,Priority> s2i=new HashMap<String,Priority>();
	private int index=0;
	
	/**
	 * @param priorityNames
	 */
	public synchronized static void initializePriority(String[] priorityNames) {
		if (priorities.size()>0) {
			throw new IllegalStateException("Priorities are already initialized");
		}
		priorities.addAll(Arrays.asList(priorityNames));
	}
	
	/**
	 * @param name
	 */
	private  Priority(String name) {
		this.index=getIndex(name);
	}
	
	/**
	 * @param name
	 * @return
	 */
	public static Priority getPriority(String name) {
		Priority p=s2i.get(name);
		if (p==null) {
			p=new Priority(name);
			s2i.put(name, p);
		}
		return p;
		
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
	public synchronized static int getIndex(String name) {
		checkInitialization();
		final int n=priorities.indexOf(name);
		if (n<0) {
			throw new IllegalArgumentException("The  \""+name+"\" is not a priority");
		}
		return n;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Priority arg0) {
		checkInitialization();
		return this.index-arg0.index;
	}
	

}
