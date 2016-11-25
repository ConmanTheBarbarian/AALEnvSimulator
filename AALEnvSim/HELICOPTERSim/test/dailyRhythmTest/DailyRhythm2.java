package dailyRhythmTest;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import jdistlib.Beta;
import jdistlib.Uniform;
import jdistlib.rng.MersenneTwister;
import jdistlib.rng.RandomEngine;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import simulationBase.Base;
import simulationBase.Configuration;
import simulationBase.Engine;
import simulationBase.Log;
import stateMachine.Action;
import stateMachine.Condition;
import stateMachine.Edge;
import stateMachine.EdgeProbabilityDouble;
import stateMachine.EdgeProbabilityFunction;
import stateMachine.EdgeProbabilityFunction.Function;
import stateMachine.EdgeProbabilityModifierDouble;
import stateMachine.Event;
import stateMachine.Mode;
import stateMachine.ModeState;
import stateMachine.State;
import stateMachine.StateMachine;
import stateMachine.StateMachine.Trace;
import stateMachine.StateMachineSystem;
import stateMachine.TransitionRule;

public class DailyRhythm2 {
	//static Mode.Type type;
	static Set<Mode.Type> typeSet=new HashSet<Mode.Type>();
	static Set<ModeState> modeStateSet=new HashSet<ModeState>();
	static Configuration cfg;
	static final String modeStateName="periodOfDay";
	static double awakeIndex=0.1;
	static final String sleepStateName="sleepState";
	static Instant lastSleepStateChangeTime=Instant.parse("2014-12-31T08:00:00Z");
	public static class Fatigue {
		private double value;
		private HashMap<String,Double> sleepStageDecreaseMap=new HashMap<String,Double>();
		public static final double defaultIncrease=(1.0-Base.epsilon)/(18.0*60.0);
		public Fatigue(double value) {
			if (sleepStageDecreaseMap.isEmpty()) {
				sleepStageDecreaseMap.put("stage2", -(1.0)/(8.5*60.0));
				sleepStageDecreaseMap.put("stage3", -(1.0)/(8.5*60.0));
				sleepStageDecreaseMap.put("stage4", -(1.0)/(8.5*60.0));
				sleepStageDecreaseMap.put("stage1", -(1.0)/(8.5*60.0));				
				sleepStageDecreaseMap.put("REM",    -(1.0)/(8.5*60.0));	
				sleepStageDecreaseMap.put("awake",  defaultIncrease);
				
			}
			this.value=Math.max(Base.epsilon, Math.min(1.0-Base.epsilon,value));
		}
		public final synchronized double getValue() {
			return this.value;
		}
		public final synchronized void increaseValue(Mode mode) {
			if (mode.getType().getName().compareTo("sleepState_type")!=0) {
				throw new IllegalArgumentException("Mode "+mode+" is of incorrect type "+mode.getType().getName());
			}
			this.value+=sleepStageDecreaseMap.get(mode.getValue());
			if (this.value<Base.epsilon) {
				this.value=0.0;
			} else if (this.value>1.0-Base.epsilon) {
				this.value=1.0;
			}
		}
	}
	static final Fatigue fatigue=new Fatigue(1.0);
	
	static final private double bodyProcessFunctionProbability(double currentDuration,double typicalMaximumDuration,double rapidity) {
		final double tmp=1.0-(typicalMaximumDuration-currentDuration)/typicalMaximumDuration;
		//final double result=Math.min(1.0-Base.epsilon,Math.exp(-tmp/rapidity));
		final double result=1.0-Beta.cumulative_raw(tmp, 50.0/(rapidity*25.0), 1.2, true, false);
		return result;
	}
	static final private boolean isSleepTime(Mode mode) {
		if (mode.getType().getName().compareTo("periodOfDay_type")!=0) {
			throw new IllegalArgumentException("Mode "+mode.getValue()+" is of incorrect type");
		}
		final String[] sleepTimePeriods={"evening_night","night","night_morning"};
		final Vector<String> stp_vector=new Vector(Arrays.asList(sleepTimePeriods));
		if (stp_vector.indexOf(mode.getValue())>=0) {
			return true;
		}
		return false;
	}

	protected Object sleepStageNames;
	//static int adummy=0;
	
	public static class StayInSleepStateProbability extends Function {
		private static Awaken awaken=new Awaken();
		private static  String sleepMode[]={"awake","stage1","stage2","stage3","stage4","REM"};
		private static double typicalDurationOfPeriod[]={18.0,0.1,0.42,0.1,0.1,0.2};
		private static double maximumProbabilityDuringSleepPeriodToStay[]={0.5,1.0,1.0,1.0,1.0,1.0};
		private static double maximumProbabilityDuringAwakePeriodToStay[]={1.0,0.5,0.5,0.7,0.7,0.5};
		
		
		public StayInSleepStateProbability() {
			
		}

		/* (non-Javadoc)
		 * @see stateMachine.EdgeProbabilityFunction.Function#evaluate()
		 */
		@Override
		public Double evaluate(StateMachine sm) {
			double threshold=Base.epsilon;
			final State currentState=sm.getCurrentState();
			final int index=Arrays.asList(sleepMode).indexOf(currentState.getName());
			if (index<0) {
				throw new IllegalStateException("State machine in impossible state "+currentState.getName());
			}
			final ZonedDateTime currentTime=sm.getStateMachineSystem().getEngineData().getTime().getTime().atZone(ZoneId.systemDefault());
			final ModeState ms=sm.getStateMachineSystem().getMetaModeState().getModeState(modeStateName);
			double fatigueModifier=1.0-Math.exp(-50*fatigue.getValue());
			if (index==0) {
				fatigueModifier=1.0-fatigueModifier;
			}
			double maxProbability=1.0;
			if (ms.getValueSet().parallelStream().anyMatch(s->isSleepTime(s))) {
				maxProbability=fatigueModifier*maximumProbabilityDuringSleepPeriodToStay[index];
			} else {
				maxProbability=fatigueModifier*maximumProbabilityDuringAwakePeriodToStay[index];
			}
			long d=-currentTime.toLocalDateTime().until(lastSleepStateChangeTime.atZone(ZoneId.systemDefault()).toLocalDateTime(), ChronoUnit.MINUTES);
			final double duration=((double)d)/60.0;
			threshold=maxProbability*bodyProcessFunctionProbability(duration, typicalDurationOfPeriod[index], 0.01)-awaken.evaluate(sm);
			if (threshold<0.0) {
				threshold=0.0;
			}
			//adummy++;
			//System.out.println(adummy);
			if (sm.isTracing()) {
				System.out.println("Stay in sleep state :"+threshold+" based on fatigue modifier = "+fatigueModifier+" max probability="+maxProbability+" and duration="+duration);
			}
			return threshold;
		}
		
	}
	public static class GoToSleepStateProbability extends Function {
		private static StayInSleepStateProbability tmp=new StayInSleepStateProbability();

		/* (non-Javadoc)
		 * @see stateMachine.EdgeProbabilityFunction.Function#evaluate()
		 */
		@Override
		public Double evaluate(StateMachine sm) {
			final double result=Math.min(1.0-Base.epsilon, 1.0-tmp.evaluate(sm));
			if (sm.isTracing()) {
				System.out.println("Go to sleep state= "+result);
			}
			return result;
		}
		
	}
	public static class Awaken extends Function {
		private static  String sleepMode[]={"awake","stage1","stage2","stage3","stage4","REM"};
		private static double modifier[]={0.0,1.0,1.0,0.01,0.01,1.0};

		@Override
		public Double evaluate(StateMachine sm) {
			final State currentState=sm.getCurrentState();
			final int index=Arrays.asList(sleepMode).indexOf(currentState.getName());
			if (index<0) {
				throw new IllegalStateException("State machine in impossible state "+currentState.getName());
			}
			double fatigueModifier=1.0-Math.exp(-50*fatigue.getValue());
			if (index!=0) {
				fatigueModifier=1.0-fatigueModifier;
			}
			final double result=fatigueModifier*modifier[index];
			if (sm.isTracing()) {
				System.out.println("Awaken fatigue="+fatigue.getValue()+" fatigue modifier="+fatigueModifier+" result="+result);
			}
			return  result;
		}
	}
	
	public static class AppTracer extends Log.Tracer {

		public AppTracer(Log log) {
			super(log);
		}

		@Override
		public void addEvent(String eventTypeName, Instant t) {
			this.getLog().addDoubleData(eventTypeName, t, "fatigue", fatigue.getValue(), "");
		}
		
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//Mode.Type type=new Mode.Type("The type");
		final String dayStateNames[]={"evening_night","night","night_morning","morning","morning_lunch","lunch","lunch_afternoon","aftern)oon","afternoon_evening","evening"};
		
		//typeSet.add(type);
		
		//Mode.Type sleepType=new Mode.Type("sleepType");
		final String sleepMode[]={"awake","stage1","stage2","stage3","stage4","REM"};
		//typeSet.add(sleepType);
		
		cfg=new Configuration(typeSet,Instant.parse("2015-01-01T00:00:00Z"),Instant.parse("2015-01-07T00:00:00Z")) {
			StateMachine sm;
			StateMachine sleepStateMachine;
			@Override
			public void initialize(StateMachineSystem sms,Event tick) {
				this.setAdvanceTime(Duration.parse("PT1M"));
				this.getLog().setTracer(new AppTracer(this.getLog()));

				RandomNumberGeneratorConfiguration[] rncgArr={
					new RandomNumberGeneratorConfiguration(modeStateName,1L),
					new RandomNumberGeneratorConfiguration(sleepStateName,2L)
					
				};
				for (RandomNumberGeneratorConfiguration rncg:rncgArr ) {
					sms.getEngineData().addRandomNumberGenerator(rncg.getName(), rncg.getSeed());
				}
				sm=new StateMachine(modeStateName,sms);
				sm.setTracing(Trace.lvl1);
				String edgeNames[][]=new String[dayStateNames.length][2];
				int start=0;
				for (String dayStateName:dayStateNames) {
					State state=new State(dayStateName,sms);
					sm.addState(state);
					edgeNames[start][0]=dayStateName;
					final int previous=(dayStateNames.length+start-1)%dayStateNames.length;
					edgeNames[previous][1]=dayStateName;
					++start;
				}
				final ModeState modeState=sm.getModeState();
				modeState.addValue(Mode.getMode(sm,"evening_night"));
				
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
						@Override
						public  boolean evaluate(StateMachine sm) {
							final ZonedDateTime currentTime=sms.getEngineData().getTime().getTime().atZone(ZoneId.systemDefault());
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
					this.getLog().addEventType(remainEdge.getEventName(), "daily cycle");
					
					// create a transfer edge whose action is to move to the next state

					Condition transferIntervalCondition=new Condition(dayStateNames[i]+"_"+dayStateNames[(i+1)%dayStateNames.length]+"_transferCondition",sms) {
						@Override
						public  boolean evaluate(StateMachine sm) {
							final ZonedDateTime currentTime=sms.getEngineData().getTime().getTime().atZone(ZoneId.systemDefault());
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
							return true;
						}
					};
					TransitionRule transferRule=new TransitionRule(dayStateNames[i]+"_transferRule",sms,tick,transferIntervalCondition,transferAction);
					Edge transferEdge=new Edge(edgeNames[i][0]+"___"+edgeNames[i][1],sms,edgeNames[i][0],edgeNames[i][1],transferRule);
					sm.addEdge(transferEdge);
					this.getLog().addEventType(transferEdge.getEventName(), "daily cycle");

					HashSet<Mode> modeSet=new HashSet<Mode>();
					for (ModeState ms:sms.getMetaModeState()) {
						for (String m:ms.getType()) {
							final Mode tmp=Mode.getMode(sm, m);
							modeSet.add(tmp);
							if (dayStateNameVector.indexOf(m)==dayStateNameVector.indexOf(dayStateNames[i])) {
								sm.setProbability(remainEdge, modeSet, new EdgeProbabilityModifierDouble(sm,10.0));
								sm.setProbability(transferEdge, modeSet, new EdgeProbabilityModifierDouble(sm,0.1));
							} else {
								sm.setProbability(remainEdge,modeSet,new EdgeProbabilityModifierDouble(sm,1.0/Base.epsilon));
								sm.setProbability(transferEdge,modeSet,new EdgeProbabilityModifierDouble(sm,Base.epsilon));
							}
							modeSet.clear();
						}
					}
					
				}
				sm.setStartState(sm.getOrCreateState(dayStateNames[0]));
				
				// sleep state machine
				sleepStateMachine=new StateMachine(sleepStateName,sms);
				sleepStateMachine.setTracing(Trace.lvl1);
				
				String sleepEdgeNames[][]=new String[sleepMode.length][2];
				int sleepStart=0;
				for (String sleepStage:sleepMode) {
					State state=new State(sleepStage,sms);
					sleepStateMachine.addState(state);
					sleepEdgeNames[sleepStart][0]=sleepStage;
					if (sleepStart!=1) {
						final int previous=(sleepMode.length+sleepStart-1)%sleepMode.length;
						sleepEdgeNames[previous][1]=sleepStage;
					} else {
						sleepEdgeNames[sleepMode.length-1][1]=sleepStage;
						sleepEdgeNames[0][1]=sleepStage;
					}
					++sleepStart;
				}
				final ModeState sleepModeState=sleepStateMachine.getModeState();
				sleepModeState.addValue(Mode.getMode(sleepStateMachine,"awake"));
				String[][] stageIntervalStrings={
						{"PT17H","PT18H"}, // awake
						{"PT90M","PT110M"}, // stage 1
						{"PT90M","PT110M"}, // stage 2
						{"PT90M","PT110M"}, // stage 3
						{"PT90M","PT110M"}, // stage 4
						{"PT90M","PT110M"} // REM
				};
				HashMap<String,Double> sleepInterval=new HashMap<String,Double>();
				RandomEngine re=new MersenneTwister(1L);
				sleepInterval.put(sleepMode[0],Uniform.random(16.0, 18.0, re));
				double remaining=24.0-sleepInterval.get(sleepMode[0]);
				sleepInterval.put(sleepMode[2], remaining/2.0);
				sleepInterval.put(sleepMode[5],remaining*0.2);
				sleepInterval.put(sleepMode[1],remaining*0.1);
				sleepInterval.put(sleepMode[3],remaining*0.1);
				sleepInterval.put(sleepMode[4],remaining*0.1);
				for (int i=0; i<sleepMode.length; ++i) {
					Condition stayInXCondition=new Condition("stayIn_"+sleepMode[i]+"_Condition",sms) {
						public boolean evaluate(StateMachine sm) {
							return true;
						};
					};
					Action stayInXAction=null;
					stayInXAction= new Action("stayIn_"+sleepMode[i]+"_Action",sms) {

						@Override
						public boolean evaluate(StateMachine sm) {
							fatigue.increaseValue(Mode.getMode(sm,sm.getCurrentState().getName()));
							return true;
						}
					};
					TransitionRule stayInXRule=new TransitionRule(sleepEdgeNames[i][0]+"_remainRule",sms,tick,stayInXCondition,stayInXAction);
					final String stayInXEdgeName=sleepEdgeNames[i][0]+"___"+sleepEdgeNames[i][0];
					Edge stayInXEdge=new Edge(stayInXEdgeName,sms,sleepMode[i],sleepMode[i],stayInXRule);
					sleepStateMachine.addEdge(stayInXEdge);
					this.getLog().addEventType(stayInXEdge.getEventName(), "circadian");

					String endNameOfGoto;
					String endState;
					if (i<sleepMode.length-1) {
						endNameOfGoto=sleepMode[(i+1)%sleepMode.length];
						endState=endNameOfGoto;
					} else {
						endNameOfGoto=sleepMode[1]+"_reiteration";
						endState=sleepMode[1];
					}
					Condition goToNextCondition=new Condition("goTo_"+endNameOfGoto+"_Condition",sms) {
						public boolean evaluate(StateMachine sm) {
							return true;
						}
					};
					Action goToNextAction=new Action("goTo_"+endNameOfGoto+"_Action",sms) {
						@Override
						public boolean evaluate(StateMachine sm) {
							lastSleepStateChangeTime=sm.getStateMachineSystem().getEngineData().getTime().getTime();
							fatigue.increaseValue(Mode.getMode(sm,endState));
							return true;
						}
					};
					TransitionRule goToNextRule=new TransitionRule(sleepEdgeNames[i][0]+"_transferRule",sms,tick,goToNextCondition,goToNextAction);
					final String goToEdgeName=sleepEdgeNames[i][0]+"___"+sleepEdgeNames[i][1];
					Edge goToNextEdge=new Edge(goToEdgeName,sms,sleepEdgeNames[i][0],sleepEdgeNames[i][1],goToNextRule);
					sleepStateMachine.addEdge(goToNextEdge);
					this.getLog().addEventType(goToNextEdge.getEventName(), "circadian");

					Edge goToAwakeEdge=null;
					if (i>0) {
						Condition goToAwakeCondition=new Condition("goTo_awake_from_"+sleepMode[i]+"_Condition",sms) {
							public boolean evaluate(StateMachine sm) {
								return true;
							}
						};
						Action goToAwakeAction=new Action("goTo_awake_from_"+sleepMode[i]+"_Action",sms) {
							@Override
							public boolean evaluate(StateMachine sm) {
								lastSleepStateChangeTime=sm.getStateMachineSystem().getEngineData().getTime().getTime();
								fatigue.increaseValue(Mode.getMode(sm,endState));
								return true;
							}

						};
						TransitionRule goToAwakeRule=new TransitionRule("awake"+"_specialTransferRule_from_"+sleepMode[i],sms,tick,goToAwakeCondition,goToAwakeAction);
						goToAwakeEdge=new Edge(sleepEdgeNames[i][0]+"___"+sleepEdgeNames[0][0],sms,sleepEdgeNames[i][0],sleepEdgeNames[0][0],goToAwakeRule);
						sleepStateMachine.addEdge(goToAwakeEdge);
						this.getLog().addEventType(goToAwakeEdge.getEventName(), "circadian");

					}
						
					HashSet<Mode> modeSet=new HashSet<Mode>();
					for (String m:sleepModeState.getType()) {
						final Mode tmp=Mode.getMode(sleepStateMachine,m);
						modeSet.add(tmp);
						if (m.compareTo(sleepMode[i])==0) {
							sleepStateMachine.setProbability(stayInXEdge, modeSet, new EdgeProbabilityFunction(sleepStateMachine,new StayInSleepStateProbability()));
							sleepStateMachine.setProbability(goToNextEdge, modeSet, new EdgeProbabilityFunction(sleepStateMachine,new GoToSleepStateProbability()));
							if (i>0) {
								sleepStateMachine.setProbability(goToAwakeEdge, modeSet, new EdgeProbabilityFunction(sleepStateMachine, new Awaken()));
							}
						} else {
							sleepStateMachine.setProbability(stayInXEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));
							sleepStateMachine.setProbability(goToNextEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,Base.epsilon));	
							if (i>0) {
								sleepStateMachine.setProbability(goToAwakeEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,Base.epsilon));
							}
						}
						modeSet.clear();
						
					}
					for (String m:dayStateNames) {
						final Mode tmp=Mode.getMode(sm, m);
						modeSet.add(tmp);
						if (isSleepTime(tmp)) {
							if (i==0) {
								sleepStateMachine.setProbability(stayInXEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,0.25));
								sleepStateMachine.setProbability(goToNextEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,4.0));
								//sleepStateMachine.setProbability(goToAwakeEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,Base.epsilon));

							} else {
								sleepStateMachine.setProbability(stayInXEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));
								sleepStateMachine.setProbability(goToNextEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));	
								sleepStateMachine.setProbability(goToAwakeEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));
							}
						} else {
							if (i==0) {
								sleepStateMachine.setProbability(stayInXEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,4.0));
								sleepStateMachine.setProbability(goToNextEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,0.25));
								//sleepStateMachine.setProbability(goToAwakeEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));

							} else {
								sleepStateMachine.setProbability(stayInXEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));
								sleepStateMachine.setProbability(goToNextEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));	
								sleepStateMachine.setProbability(goToAwakeEdge, modeSet, new EdgeProbabilityModifierDouble(sleepStateMachine,1.0));

							}
						}
						modeSet.clear();
					}

						
				}
				HashSet<Mode> modeSet=new HashSet<Mode>();
				for (Edge edge:sm.getEdges()) {
					for (String s:sleepMode) {
						modeSet.add(Mode.getMode(sleepStateMachine, s));
						sm.setProbability(edge, modeSet, new EdgeProbabilityModifierDouble(sm,1.0));
						modeSet.clear();
					}
				}
				sleepStateMachine.setStartState(sleepStateMachine.getOrCreateState(sleepMode[0]));

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
			engine.validate();
			engine.start();
			engine.join();
			assert(true);

		} catch(Exception e) {
			fail(e.getMessage());
		}
	}

}
