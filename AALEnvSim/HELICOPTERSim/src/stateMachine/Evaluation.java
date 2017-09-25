package stateMachine;

public interface Evaluation {
	
	public boolean evaluate( StateMachine sm);
	public boolean evaluate();
	public boolean evaluate(StateMachine sm,final EventOccurrence eventOccurrence);
	public boolean evaluate(final EventOccurrence eventOccurrence);

}
