package stateMachine;


public class NamedObject implements Comparable<NamedObject>{
	private String name;

	
	NamedObject(String name) {
		this.name=name;
	}

	/**
	 * @return the name
	 */
	public synchronized String getName() {
		return this.name;
	}
	
	public synchronized String getBaseName() {
		return this.name;
	}

	@Override
	public int compareTo(NamedObject arg0) {
		return this.name.compareTo(arg0.name);
	}


	
	
	
	
	

}
