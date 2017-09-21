package stateMachine;

public class PrimitiveEventType extends EventType {
	


	/**
	 * @param name
	 * @param sms
	 * @param priority TODO
	 */
	private PrimitiveEventType(String name, StateMachineSystem sms, Priority priority) {
		super(name, sms, priority);
	}
	
	public static PrimitiveEventType getPrimitiveEvent(final String name, final StateMachineSystem sms, Priority priority) {
		return new PrimitiveEventType(name,sms, priority);
	}


}
