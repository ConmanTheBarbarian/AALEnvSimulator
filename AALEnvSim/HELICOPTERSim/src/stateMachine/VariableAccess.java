package stateMachine;

public interface VariableAccess<T> {
	Variable<T> get(StateMachineSystem sms,String path);
	boolean isT(StateMachineSystem sms,String path);

}
