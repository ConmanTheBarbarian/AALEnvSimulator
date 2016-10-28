package stateMachine;

public class SMPath {

	public static VariableAccess<?> getVariableAccess(StateMachineSystem sms,String path) {
		VariableAccess<?> result=null;
		boolean root=false;
		if (path.startsWith("/")) {
			root=true;
		}
		String[] parts=path.split("/");
		
		
		return result;
	}

}
