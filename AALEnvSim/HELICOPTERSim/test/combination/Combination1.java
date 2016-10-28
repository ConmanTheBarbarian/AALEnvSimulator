package combination;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import simulationBase.Configuration;
import stateMachine.Combination;
import stateMachine.Combination.Assignment;
import stateMachine.Combination.BooleanDomain;
import stateMachine.Combination.Domain;
import stateMachine.EngineData;
import stateMachine.Mode;
import stateMachine.NamedObject;
import stateMachine.NamedObjectInStateMachineSystem;
import stateMachine.StateMachineSystem;

public class Combination1 {
	static Set<Mode.Type> typeSet=new HashSet<Mode.Type>();
	static int n=2;
	static NamedObjectInStateMachineSystem noisms[]=new NamedObjectInStateMachineSystem[n];
	static EngineData ed=new EngineData(new Configuration(typeSet,Instant.parse("2015-01-01T00:00:00Z"),Instant.parse("2015-01-07T00:00:00Z")));
	static StateMachineSystem sms=StateMachineSystem.getStateMachineSystem("test", ed);
	static Domain d[]=new Domain[2];
	static Combination c;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		for (int i=0; i<noisms.length; ++i) {
			noisms[i]=new NamedObjectInStateMachineSystem(Integer.toString(i), sms);
			d[i]=BooleanDomain.getDomain(noisms[i]);
		}
		Combination.Type type=new Combination.Type(d);
		c=new Combination(type);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		try {
			System.out.println(c.getCurrentIndex());
			assertTrue(c.getCurrentIndex().compareTo(BigInteger.ZERO)==0);
			Assignment[] a=c.currentCombination();
			assertTrue(!a[0].getValue().getBoolean());
			assertTrue(!a[1].getValue().getBoolean());
			assertTrue(c.hasNext());
			c=c.next();
			System.out.println(c.getCurrentIndex());
			assertTrue(c.getCurrentIndex().compareTo(BigInteger.ONE)==0);
			a=c.currentCombination();
			assertTrue(a[0].getValue().getBoolean());
			assertTrue(!a[1].getValue().getBoolean());
			assertTrue(c.hasNext());
			c=c.next();
			System.out.println(c.getCurrentIndex());
			assertTrue(c.getCurrentIndex().compareTo(BigInteger.valueOf(2))==0);
			a=c.currentCombination();
			assertTrue(!a[0].getValue().getBoolean());
			assertTrue(a[1].getValue().getBoolean());
			System.out.println(c.hasNext());
			assertTrue(c.hasNext());
			c=c.next();
			System.out.println(c.getCurrentIndex());
			assertTrue(c.getCurrentIndex().compareTo(BigInteger.valueOf(3))==0);
			a=c.currentCombination();
			assertTrue(a[0].getValue().getBoolean());
			assertTrue(a[1].getValue().getBoolean());
			assertTrue(!c.hasNext());
			System.out.println(c.hasNext());
			a[0]=new Assignment(d[0],new Combination.Value(false));
			c.setCurrentCombination(a);
			System.out.println(c.getCurrentIndex());
			assertTrue(c.getCurrentIndex().compareTo(BigInteger.valueOf(2))==0);
			
//			System.out.println(c.getCurrentIndex());
			
			

		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

}
