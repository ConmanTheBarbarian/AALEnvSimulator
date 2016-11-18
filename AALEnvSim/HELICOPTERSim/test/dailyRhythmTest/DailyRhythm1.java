package dailyRhythmTest;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import simulationBase.Base;
import simulationBase.Configuration;
import simulationBase.Engine;
import stateMachine.Action;
import stateMachine.Condition;
import stateMachine.Edge;
import stateMachine.EdgeProbabilityDouble;
import stateMachine.Event;
import stateMachine.Mode;
import stateMachine.ModeState;
import stateMachine.State;
import stateMachine.StateMachine;
import stateMachine.StateMachineSystem;
import stateMachine.TransitionRule;

public class DailyRhythm1 {
	//static Mode.Type type;
	static Set<Mode.Type> typeSet=new HashSet<Mode.Type>();
	static Set<ModeState> modeStateSet=new HashSet<ModeState>();
	static Configuration cfg;
	static final String modeStateName="periodOfDay";
	
	static final private double bodyProcessFunctionProbability(Duration currentDuration,Duration typicalMaximumDuration,double rapidity) {
		final double tmp=typicalMaximumDuration.minus(currentDuration).getSeconds()/3600.0;
		final double result=Math.min(1.0,Math.exp(-tmp/rapidity));
		
		return result;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Mode.Type type=new Mode.Type("The type");
		final String dayStateNames[]={"evening_night","night","night_morning","morning","morning_lunch","lunch","lunch_afternoon","aftern)oon","afternoon_evening","evening"};
		for (String dsn:dayStateNames) {
			type.add(dsn);
		}
	
		typeSet.add(type);
		
		cfg=new Configuration(typeSet) {
			StateMachine sm;
			@Override
			public void initialize(StateMachineSystem sms,Event tick) {
				final ModeState modeState=new ModeState(modeStateName,sms,type);
				modeState.addValue(new Mode(type,"evening_night"));
				sms.addModeState(modeState);
				this.setAdvanceTime(Duration.parse("PT1M"));
				Instant[] interval={Instant.parse("2015-01-01T00:00:00Z"),Instant.parse("2015-01-07T00:00:00Z")};
				
				this.setInterval(interval);
				RandomNumberGeneratorConfiguration[] rncgArr={
					new RandomNumberGeneratorConfiguration("dailyRhythm",1L)	
				};
				for (RandomNumberGeneratorConfiguration rncg:rncgArr ) {
					sms.getEngineData().addRandomNumberGenerator(rncg.getName(), rncg.getSeed());
				}
				sm=new StateMachine("dailyRhythm",sms);
				String edgeNames[][]=new String[dayStateNames.length][2];
				int start=0;
				for (String dayStateName:dayStateNames) {
					type.add(dayStateName); // add states as modes
					State state=new State(dayStateName,sms);
					sm.addState(state);
					edgeNames[start][0]=dayStateName;
					final int previous=(dayStateNames.length+start-1)%dayStateNames.length;
					edgeNames[previous][1]=dayStateName;
					++start;
				}
				
				// edges
				// create default conditions and do nothing actions

				Action doNothingAction=new Action("do nothing",sms) {
					public boolean evaluate() {
						return true;
					}
				};
				
				Duration modeInterval=Duration.parse("PT24H");
				modeInterval=modeInterval.dividedBy(dayStateNames.length);
				Duration interModeInterval=modeInterval.dividedBy(4);
				OffsetTime timeSpec=OffsetTime.parse("00:00:00Z");
				// the remain rule (condition is true, action is nothing
				//Mode mode=new Mode(sms.getMode().getType());
				Vector dayStateNameVector=new Vector(Arrays.asList(dayStateNames));

				for (int i=0; i<dayStateNames.length; ++i) {
					
					final OffsetTime startOfMode=timeSpec.minus(modeInterval).plus(modeInterval.multipliedBy(i));
					final OffsetTime startOfTransferMode=timeSpec.minus(modeInterval).plus(modeInterval.multipliedBy(i+1));

					Condition remainIntervalCondition=new Condition(dayStateNames[i]+"_remainCondition",sms) {
						public  boolean evaluate() {
							final ZonedDateTime currentTime=sms.getEngineData().getTime().getTime().atZone(ZoneId.of("UTC"));
							final int hour=currentTime.getHour();
							final int diff=hour-startOfMode.getHour();
							if (diff>=0 || diff<-20) {
								if (diff>0||diff<-20) {
									return true;
								}
								final int minute=currentTime.getMinute();
								if (minute>=startOfMode.getMinute()) {
									return true;
								} 
							}
							return false;
						}
					};
					TransitionRule remainRule=new TransitionRule(dayStateNames[i]+"_remainRule",sms,tick,remainIntervalCondition,doNothingAction);

					// add a remain edge to each state
					Edge remainEdge=new Edge(dayStateNames[i]+"___"+dayStateNames[i],sms,dayStateNames[i],dayStateNames[i],remainRule);
					sm.addEdge(remainEdge);
					
					// create a transfer edge whose action is to move to the next state

					Condition transferIntervalCondition=new Condition(dayStateNames[i]+"_"+dayStateNames[(i+1)%dayStateNames.length]+"_transferCondition",sms) {
						public  boolean evaluate() {
							final ZonedDateTime currentTime=sms.getEngineData().getTime().getTime().atZone(ZoneId.of("UTC"));
							final int hour=currentTime.getHour();
							final int diff=hour-startOfTransferMode.getHour();
							if (diff>=0 || diff<-20) {
								if (diff>0||diff<-20) {
									return true;
								}
								final int minute=currentTime.getMinute();
								if (minute>=startOfTransferMode.getMinute()) {
									return true;
								} 
							}
							return false;
						}
					};
					final String startStateName=edgeNames[i][0];
					final String endStateName=edgeNames[i][1];
					final State endState=sm.getOrCreateState(edgeNames[i][1]);
					Action transferAction=new Action("transfer from "+edgeNames[i][0]+" to "+edgeNames[i][1]+" action",sms) {
						public synchronized boolean evaluate() {
							//sm.moveTo(endState);
							sms.removeMode("periodOfDay",startStateName);
							sms.addMode("periodOfDay",endStateName);
							return true;
						}
					};
					TransitionRule transferRule=new TransitionRule(dayStateNames[i]+"_transferRule",sms,tick,transferIntervalCondition,transferAction);
					Edge transferEdge=new Edge(edgeNames[i][0]+"___"+edgeNames[i][1],sms,edgeNames[i][0],edgeNames[i][1],transferRule);
					sm.addEdge(transferEdge);
					HashSet<Mode> modeSet=new HashSet<Mode>();
					for (ModeState ms:sms.getMetaModeState()) {
						for (String m:ms.getType()) {
							final Mode tmp=new Mode(ms.getType(),m);
							modeSet.add(tmp);
							if (dayStateNameVector.indexOf(m)==dayStateNameVector.indexOf(dayStateNames[i])) {
								sm.setProbability(remainEdge, modeSet, new EdgeProbabilityDouble(sm,0.9));
								sm.setProbability(transferEdge, modeSet, new EdgeProbabilityDouble(sm,0.1));
							} else {
								sm.setProbability(remainEdge,modeSet,new EdgeProbabilityDouble(sm,1.0-Base.epsilon));
								sm.setProbability(transferEdge,modeSet,new EdgeProbabilityDouble(sm,Base.epsilon));
							}
							modeSet.clear();
						}
					}
					
				}
				sm.setStartState(sm.getOrCreateState(dayStateNames[0]));
				sm.validate();
				
				
			}

		};
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
			
			Engine engine=new Engine(cfg);
			engine.start();
			engine.join();
			assert(true);

		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

}
