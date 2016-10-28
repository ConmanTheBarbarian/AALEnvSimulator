package simulationBase;

public class Base {

	public static final double epsilon=1e-9;
	public static final boolean withinEpsilonEquivalence(double value1,double value2) {
		return (value1>=value2-epsilon && value1<=value2+epsilon);
	}
	
}
