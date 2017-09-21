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
import stateMachine.Edge;
import stateMachine.EngineData;
import stateMachine.EventType;
import stateMachine.Mode;
import stateMachine.PrimitiveEventType;
import stateMachine.State;
import stateMachine.StateMachine;
import stateMachine.StateMachineSystem;
import stateMachine.TransitionRule;

public class StateMachineSystem_BasicTest5_Deviation {
	static StateMachineSystem sms,smsDeviation;
	static TransitionRule transitionRule;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Mode.Type type=new Mode.Type("funnyType");

		Configuration cfg=new Configuration(type);
		EngineData engineData=new EngineData(cfg);
		sms=StateMachineSystem.getStateMachineSystem("Allan",engineData);
		
		PrimitiveEventType event=new PrimitiveEventType("Hello", sms);
		Condition condition=new Condition("Hohoho", sms);
		Action action=new Action("Hihihi", sms);
		transitionRule=new TransitionRule("rule_1",sms, event,condition,action);
		
		EngineData engineDataDeviation=new EngineData(cfg);
		
		smsDeviation=StateMachineSystem.getStateMachineSystem("Bertil",engineDataDeviation);
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
	
	@Test
	public void testAddStateMachine() {
		StateMachine sm=new StateMachine("sm1",sms);
		assert(sm!=null);
		StateMachine deviantSm=new StateMachine("deviantSm",smsDeviation);
		String stateNames[]={"s1","s2","s3"};
		String edgeNames[][]=new String[stateNames.length][2];
		int start=0;
		try {
			for (String stateName:stateNames) {
				State state=new State(stateName,sms);
				assert(state!=null);
				sm.addState(state);
				edgeNames[start][0]=stateName;
				int previous=(stateNames.length+start-1)%stateNames.length;
				edgeNames[previous][1]=stateName;
				++start;
				State deviantState=new State(stateName,smsDeviation);
				try {
					sm.addState(deviantState);
				} catch (IllegalArgumentException iae) {
					assert(true);
				} catch (Exception e) {
					fail(e.getMessage());
				}
				deviantSm.addState(deviantState);
			
				
			}
			for (int i=0; i<start; ++i) {
				String baseName=edgeNames[i][0]+"_"+edgeNames[i][1];

				EventType event=new PrimitiveEventType(baseName+"_event",sms);
				
				Condition condition=new Condition(baseName+"_condition",sms);
				Action action=new Action(baseName+"_action",sms);
				TransitionRule tr=new TransitionRule(baseName+"_rule",sms,event,condition,action);
				assert(event!=null && action!=null && condition != null && action != null);
				Edge edge=new Edge(baseName+"_edge", sms,edgeNames[i][0],edgeNames[i][1],tr);
				assert(edge!=null);
				sm.addEdge(edge);
				try {
					deviantSm.addEdge(edge);
				} catch (IllegalArgumentException iae) {
					assert(true);
				}
				try {
					TransitionRule trDeviation=new TransitionRule(baseName+"_rule",smsDeviation,event,condition,action);
					fail("Could create faulty transition rule");
				} catch (IllegalArgumentException iae) {
					assert(true);
				} catch (Exception e) {
					fail(e.getMessage());
				}
				EventType eventDeviation=new PrimitiveEventType(baseName+"_eventDeviation",smsDeviation);
				
				Condition conditionDeviation=new Condition(baseName+"_conditionDeviation",smsDeviation);
				Action actionDeviation=new Action(baseName+"_actionDeviation",smsDeviation);
				TransitionRule trDeviation=new TransitionRule(baseName+"_rule",smsDeviation,eventDeviation,conditionDeviation,actionDeviation);
				Edge edgeDeviation=new Edge(baseName+"_edgeDeviation", smsDeviation,edgeNames[i][0],edgeNames[i][1],trDeviation);
				deviantSm.addEdge(edgeDeviation);
			}
		
		} catch(Exception e) {
			fail("Exception: "+e.getMessage());
		}
		
	}
	
	

}
