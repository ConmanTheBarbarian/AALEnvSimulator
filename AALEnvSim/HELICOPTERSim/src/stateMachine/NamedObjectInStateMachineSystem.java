package stateMachine;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class NamedObjectInStateMachineSystem extends NamedObject {
	private StateMachineSystem stateMachineSystem;
	private long instance=0;
	private static final String instanceSuffix="___instance_";
	private String currentVirtualSubject=null;
	

	public NamedObjectInStateMachineSystem(String name, StateMachineSystem sms) {
		super(name);
		this.stateMachineSystem=sms;
	}
	/**
	 * @return the stateMachineSystem
	 */
	public synchronized final StateMachineSystem getStateMachineSystem() {
		return stateMachineSystem;
	}
	
	synchronized void increaseInstance() {
		++this.instance;
	}
	public long getInstance() {
		return this.instance;
	}
	
	@Override
	public synchronized String getName() {
		return super.getName()+instanceSuffix+instance;
	}
	
	public synchronized String getQualifiedName() {
		throw new IllegalStateException("getQualifiedName() is not implemented");
	}
	 synchronized String getQualifiedBaseName() {
		throw new IllegalStateException("getQualifiedName() is not implemented");
	}

	
	NamedObjectInStateMachineSystem getNamedObjectInStateMachineSystemBackEnd(final NamedObjectInStateMachineSystem noism, final List<Path.Part> parts, int currentPosition) {
		throw new IllegalArgumentException("Not implemented for "+this.getClass().getName());
	}
	List<String> splitAndCheckRoot(final String path,NamedObjectInStateMachineSystem element) {
		if (path.startsWith("/")) {
			element=this.getStateMachineSystem().getStateMachineGroupRoot();
		} else {
			element=this;
		}
		final String[] parts=path.split("/");
		final List<String> partList=Arrays.asList(parts);
		return partList;
		
	}
	
	public synchronized StateMachine getStateMachine(final Path path) {
		final List<Path.Part> partList=path.getPartVector();
		final NamedObjectInStateMachineSystem noism=this.getNamedObjectInStateMachineSystemBackEnd(this,partList, 0);
		if (!(noism instanceof StateMachine)) {
			throw new IllegalArgumentException("End node is not a StateMachine , but a "+noism.getClass().getName());
		}
		return (StateMachine)noism;

	}
	public synchronized StateMachineGroup getStateMachineGroup(final Path path) {
		NamedObjectInStateMachineSystem element=null;
		final List<Path.Part> partList=path.getPartVector();
		final NamedObjectInStateMachineSystem noism=this.getNamedObjectInStateMachineSystemBackEnd(element,partList, 0);
		if (!(noism instanceof StateMachineGroup)) {
			throw new IllegalArgumentException("End node is not a StateMachineGroup , but a "+noism.getClass().getName());
		}
		return (StateMachineGroup)noism;
		
	}
	public synchronized String getCurrentVirtualSubject() {
		return currentVirtualSubject;
	}
	public synchronized void setCurrentVirtualSubject(final String name) {
		this.currentVirtualSubject=name;
	}
	

}
