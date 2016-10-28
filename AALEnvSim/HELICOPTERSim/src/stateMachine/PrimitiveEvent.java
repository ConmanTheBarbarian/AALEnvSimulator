package stateMachine;

public class PrimitiveEvent extends Event {
	


	/**
	 * @param name
	 * @param sms
	 */
	private PrimitiveEvent(String name, StateMachineSystem sms) {
		super(name, sms);
	}
	
	public static PrimitiveEvent getPrimitiveEvent(final String name, final StateMachineSystem sms) {
		return new PrimitiveEvent(name,sms);
	}


}
