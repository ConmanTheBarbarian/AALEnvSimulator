package stateMachineTest;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import simulationBase.Configuration;
import stateMachine.Action;
import stateMachine.Condition;
import stateMachine.EngineData;
import stateMachine.Mode;
import stateMachine.PrimitiveEvent;
import stateMachine.StateMachineSystem;
import stateMachine.TransitionRule;

public class StateMachineSystem_BasicTest3 {
	static StateMachineSystem sms;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Mode.Type type=new Mode.Type("funnyType");

		Configuration cfg=new Configuration(type);
		EngineData engineData=new EngineData(cfg);
		sms=StateMachineSystem.getStateMachineSystem("Allan",engineData);
		PrimitiveEvent event=new PrimitiveEvent("Hello", sms);
		Condition condition=new Condition("Hohoho", sms);
		Action action=new Action("Hihihi", sms);
		TransitionRule transitionRule=new TransitionRule("rule_1",sms, event,condition,action);
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
		assert(sms.getEvent("")==null);
		assert(sms.getEvent("Hello")!=null);
	}


	

	@Test
	public void testGetCondition() {
		assert(sms.getCondition("")==null);
		assert(sms.getCondition("Hohoho")!=null);
	}


	

	@Test
	public void testGetAction() {
		assert(sms.getAction("")==null);
		assert(sms.getAction("Hihihi")!=null);
	}
	
	

}
