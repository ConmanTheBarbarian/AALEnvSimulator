package stateMachine;

public class PrimitiveEvent extends Event {
	


	/**
	 * @param name
	 * @param sms
	 * @param priority TODO
	 */
	private PrimitiveEvent(String name, StateMachineSystem sms, Priority priority) {
		super(name, sms, priority);
	}
	
	public static PrimitiveEvent getPrimitiveEvent(final String name, final StateMachineSystem sms, Priority priority) {
		return new PrimitiveEvent(name,sms, priority);
	}


}
