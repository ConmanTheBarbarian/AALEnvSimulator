package stateMachineTest;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import simulationBase.Configuration;
import stateMachine.EngineData;
import stateMachine.Mode;
import stateMachine.StateMachineSystem;

public class StateMachineSystem_BasicTest1 {
	static StateMachineSystem sms;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Mode.Type type=new Mode.Type("funnyType");

		Configuration cfg=new Configuration(type);
		EngineData engineData=new EngineData(cfg);

		sms=StateMachineSystem.getStateMachineSystem("Allan",engineData);
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
	public void testGetStateMachineSystem() {
		assertNotNull(sms);
	}


	@Test
	public void testGetName() {
		assertNotNull(sms.getName());
		assert(sms.getName().compareTo("Allan")==0);
	}

	@Test
	public void testGetStateMachine() {
		assert(sms.getStateMachine("")==null);
	}

	@Test
	public void testStateMachineIsEmpty() {
		assert(sms.stateMachineIsEmpty());
	}

	@Test
	public void testKeySetOfStateMachine() {
		assert(sms.keySetOfStateMachine()==null);
	}


	@Test
	public void testRemoveStateMachine() {
		try {
			sms.removeStateMachine("");
			assert(true);
		} catch (Exception e) {
			fail("Exception "+e.getMessage());
		}
	}

	@Test
	public void testGetEvent() {
		System.out.println("Banzai");
		assert(sms.getEvent("")==null);
	}


	@Test
	public void testGetCondition() {
		assert(sms.getCondition("")==null);
	}


	@Test
	public void testGetAction() {
		assert(sms.getAction("")==null);
	}

}
