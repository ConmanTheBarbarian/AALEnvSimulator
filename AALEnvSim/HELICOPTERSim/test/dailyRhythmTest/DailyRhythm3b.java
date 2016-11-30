package dailyRhythmTest;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
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
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.stream.Collectors;

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
import stateMachine.Combination;
import stateMachine.Combination.Assignment;
import stateMachine.Combination.StateDomain;
import stateMachine.Combination.Type;
import stateMachine.Condition;
import stateMachine.Edge;
import stateMachine.EdgeProbability;
import stateMachine.EdgeProbabilityDouble;
import stateMachine.EdgeProbabilityFunction;
import stateMachine.EdgeProbabilityFunction.Function;
import stateMachine.EdgeProbabilityModifierDouble;
import stateMachine.Event;
import stateMachine.Mode;
import stateMachine.ModeState;
import stateMachine.Path;
import stateMachine.Priority;
import stateMachine.State;
import stateMachine.StateEdgeProbabilityFunctionSpecification;
import stateMachine.StateMachine;
import stateMachine.StateMachine.Trace;
import stateMachine.StateMachineGroup;
import stateMachine.StateMachineSystem;
import stateMachine.TransitionRule;
import stateMachine.Variable;

public class DailyRhythm3b {
	public static class DisruptionProbabilityFunction extends
			stateMachine.StateEdgeProbabilityFunctionSpecification.Function {
		@Override
		public TreeMap<Edge, Double> evaluate(Type type,State state, Set<Edge> edgeSet, Set<Edge> applicableEdgeSet) {
			TreeMap<Edge,Double> result=new TreeMap<Edge,Double>();
			for (Edge edge:edgeSet) {
				result.put(edge, 0.0);
			}



			// if zero, 0.0 probability for all possibilities
			if (applicableEdgeSet.size()==0) {
				return result;
			}
			// if one, 100% probability to that one!
			if (applicableEdgeSet.size()==1) {
				final Edge edge=applicableEdgeSet.iterator().next();
				result.remove(edge);
				result.put(edge, 1.0);
				return result;
			}
			if (applicableEdgeSet.size()==2) {
				for (Edge edge:applicableEdgeSet) {
					if (edge.getEndState().getBaseName().contains("disrupted")) {
						result.put(edge, 0.01);
					} else {
						result.put(edge, 0.99);						
					}
				}
				return result;
			}
			return null;
		};

	};

	//static Mode.Type type;
	static Set<Mode.Type> typeSet=new HashSet<Mode.Type>();
	static Set<ModeState> modeStateSet=new HashSet<ModeState>();
	static Configuration cfg;
	static final String modeStateName="periodOfDay";
	static double awakeIndex=0.1;
	static final String sleepStateName="sleepState";
	static final String sleepDirectionName="sleepDirection";
	static final String disruptorName="disruptor";

	static Instant lastSleepStateChangeTime=Instant.parse("2014-12-31T08:00:00Z");
	public static class Fatigue {
		private double value;
		private double awakeDuration;
		private double dayLength;
		private HashMap<String,Double> sleepStageDecreaseMap=new HashMap<String,Double>();
		public   double defaultIncrease;
		private void computeParameters(double awakeDuration) {
			if (awakeDuration<16.0 || awakeDuration>18.0) {
				throw new IllegalArgumentException("Day length out of bounds "+awakeDuration);
			}
			this.awakeDuration=awakeDuration;
			this.dayLength=(awakeDuration-17.0)+24.0;
			this.defaultIncrease=(1.0-Base.epsilon)/(awakeDuration*60.0);
			double night=8.0+(awakeDuration-17.0)*0.5;
			if (sleepStageDecreaseMap.isEmpty()) {
				sleepStageDecreaseMap.put("stage2", -(1.0)/(night*60.0));
				sleepStageDecreaseMap.put("stage3", -(1.0)/(night*60.0));
				sleepStageDecreaseMap.put("stage4", -(1.0)/(night*60.0));
				sleepStageDecreaseMap.put("stage1", -(1.0)/(night*60.0));				
				sleepStageDecreaseMap.put("REM",    -(1.0)/(night*60.0));	
				sleepStageDecreaseMap.put("awake",  defaultIncrease);

			}
			
		}
		public Fatigue(double value, double awakeDuration) {
			this.computeParameters(awakeDuration);
			this.value=Math.max(Base.epsilon, Math.min(1.0-Base.epsilon,value));
		}
		public final synchronized double getValue() {
			return this.value;
		}
		public final synchronized void increaseValue(State state) {
			if (state.getStateMachine().getLocalName().compareTo("sleepState")!=0) {
				throw new IllegalArgumentException("Stat "+state+" is of incorrect state machine "+state.getStateMachine());
			}
			this.value+=sleepStageDecreaseMap.get(state.getLocalName());
			if (this.value<Base.epsilon) {
				this.value=0.0;
			} else if (this.value>1.0-Base.epsilon) {
				this.value=1.0;
			}
		}
		public void setDayLength(double awakeDuration) {
			sleepStageDecreaseMap.clear();
			this.computeParameters(awakeDuration);
			
		}
	}
	//static final Fatigue fatigue=new Fatigue(1.0);

	static final private double bodyProcessFunctionProbability(double currentDuration,double typicalMaximumDuration,double rapidity) {
		double tmp=1.0-(typicalMaximumDuration-currentDuration)/typicalMaximumDuration;
		if (tmp<0.0) {
			tmp=0.0;
		} else if (tmp>1.0) {
			tmp=1.0;
		}
		//final double result=Math.min(1.0-Base.epsilon,Math.exp(-tmp/rapidity));
		final double result=1.0-Beta.cumulative_raw(tmp, 50.0/(rapidity*25.0), 1.2, true, false);
		return result;
	}
	static final private boolean isSleepTime(State state) {
		if (!state.getStateMachine().getBaseName().endsWith(modeStateName)) {
			return false;
		}
		final String[] sleepTimePeriods={"evening_night","night","night_morning"};
		final Vector<String> stp_vector=new Vector<String>(Arrays.asList(sleepTimePeriods));
		if (stp_vector.indexOf(state.getLocalName())>=0) {
			return true;
		}
		return false;
	}

	protected Object sleepStageNames;
	//static int adummy=0;
	protected static String theSmgName="the state machine group";


	public static class Awaken extends Function {
		private static  String sleepMode[]={"awake","stage1","stage2","stage3","stage4","REM"};
		private static double modifier[]={0.0,1.0,1.0,0.01,0.01,1.0};

		@Override
		public Double evaluate(StateMachine sm) {
			final Fatigue fatigue=((Fatigue)sm.getStateMachineGroup().getVariable("fatigue").elementAt(0));

			final State currentState=sm.getCurrentState();
			final int index=Arrays.asList(sleepMode).indexOf(currentState.getBaseName());
			if (index<0) {
				throw new IllegalStateException("State machine in impossible state "+currentState.getName());
			}
			if (index==0) {
				return 0.0;
			}
			double fatigueModifier=Math.exp(-50*fatigue.getValue());
			final double result=fatigueModifier*modifier[index];
			if (sm.isTracing()) {
				System.out.println("Awaken fatigue="+fatigue.getValue()+" fatigue modifier="+fatigueModifier+" result="+result);
			}
			return  result;
		}
	}

	public static class AppTracer extends Log.Tracer {

		private StateMachineGroup smg;
		public AppTracer(Log log,StateMachineGroup smg) {
			super(log);
			this.smg=smg;
		}

		@Override
		public void addEvent(String eventTypeName, Instant t) {
			final Fatigue fatigue=((Fatigue)this.smg.getVariable("fatigue").elementAt(0));
			this.getLog().addDoubleData(eventTypeName, t, "fatigue", fatigue.getValue(), this.smg.getCurrentVirtualSubject());
		}

	}

	public static class AlertnessConditionSpecification {
		private double fatigueThreshold;
		private int expectedCompareToResult;
		private HashSet<Mode> applicableModeSet=new HashSet<Mode>();
		private int edgeBaseIndex;
		private EdgeProbability edgeProbability;
		/**
		 * @param fatigueThreshold
		 * @param expectedCompareToResult
		 * @param applicableModeCollection
		 * @param edgeProbability 
		 */
		public AlertnessConditionSpecification(
				int edgeBaseIndex,
				double fatigueThreshold,
				int expectedCompareToResult, 
				Collection<Mode> applicableModeCollection, EdgeProbability edgeProbability) {
			if (edgeBaseIndex<0 || edgeBaseIndex>11) {
				throw new IllegalArgumentException("Index out of boundaries");
			}
			if (fatigueThreshold<-Base.epsilon || fatigueThreshold>1.0+Base.epsilon) {
				throw new IllegalArgumentException("Incorrect fatigue specification: "+fatigueThreshold);
			}
			if (expectedCompareToResult>1 || expectedCompareToResult<-1) {
				throw new IllegalArgumentException("Expected compare to result should be in {-1,0,1}, received:"+expectedCompareToResult);
			}
			if (applicableModeCollection.isEmpty()) {
				throw new IllegalArgumentException("Applicable modes is empty");
			}
			if (edgeProbability==null) {
				throw new IllegalArgumentException("Edge probability is null");
			}
			this.edgeBaseIndex=edgeBaseIndex;
			this.fatigueThreshold = fatigueThreshold;
			this.expectedCompareToResult = expectedCompareToResult;
			this.applicableModeSet.addAll(applicableModeCollection);
			this.edgeProbability=edgeProbability;
		}

		/**
		 * @return the fatigueThreshold
		 */
		public synchronized final double getFatigueThreshold() {
			return fatigueThreshold;
		}
		/**
		 * @return the expectedCompareToResult
		 */
		public synchronized final int getExpectedCompareToResult() {
			return expectedCompareToResult;
		}
		/**
		 * @return the applicableModeSet
		 */
		public synchronized final HashSet<Mode> getApplicableModeSet() {
			return applicableModeSet;
		}

		/**
		 * @return the edgeBaseIndex
		 */
		public synchronized final int getEdgeBaseIndex() {
			return edgeBaseIndex;
		}

		/**
		 * @return the edgeProbability
		 */
		public synchronized final EdgeProbability getEdgeProbability() {
			return edgeProbability;
		}


	}

	static class DayCycleProbabilityFunction extends StateEdgeProbabilityFunctionSpecification.Function {

		private double count=0.0;
		private final double step=0.01;
		public DayCycleProbabilityFunction() {
			super();
		}

		/* (non-Javadoc)
		 * @see stateMachine.StateEdgeProbabilityFunctionSpecification#evaluate()
		 */
		@Override
		public TreeMap<Edge, Double> evaluate(Type type,State state, Set<Edge> edgeSet, Set<Edge> applicableEdgeSet) {
			TreeMap<Edge,Double> result=new TreeMap<Edge,Double>();
			for (Edge edge:edgeSet) {
				result.put(edge, 0.0);
			}



			// if zero, 0.0 probability for all possibilities
			if (applicableEdgeSet.size()==0) {
				this.count=0;
				return result;
			}
			// if one, 100% probability to that one!
			if (applicableEdgeSet.size()==1) {
				this.count=0;
				final Edge edge=applicableEdgeSet.iterator().next();
				result.remove(edge);
				result.put(edge, 1.0);
				return result;
			}
			// now we turn to the interesting cases
			count+=step;
			for (Edge edge:applicableEdgeSet) {
				if (edge.getTransitionRule().getBaseName().endsWith("transferRule")) {
					result.put(edge,step);
				} else {
					result.put(edge,1.0-step);
				}
			}
			return result;
		}



	}
	static class SleepStateProbabilityFunction extends StateEdgeProbabilityFunctionSpecification.Function {
		private static Awaken awaken=new Awaken();
		private static  String sleepMode[]={"awake","stage1","stage2","stage3","stage4","REM"};
		private static double typicalDurationOfPeriod[]={18.0,0.1,0.42,0.1,0.1,0.2};
		private static double maximumProbabilityDuringSleepPeriodToStay[]={0.5,1.0,1.0,1.0,1.0,1.0};
		private static double maximumProbabilityDuringAwakePeriodToStay[]={1.0,0.5,0.5,0.7,0.7,0.5};




		private double count=0.0;
		private final double step=0.01;
		public SleepStateProbabilityFunction() {
			super();
		}

		/* (non-Javadoc)
		 * @see stateMachine.StateEdgeProbabilityFunctionSpecification#evaluate()
		 */
		@Override
		public TreeMap<Edge, Double> evaluate(Type type,State state, Set<Edge> edgeSet, Set<Edge> applicableEdgeSet) {
			TreeMap<Edge,Double> result=new TreeMap<Edge,Double>();
			for (Edge edge:edgeSet) {
				result.put(edge, 0.0);
			}



			// if zero, 0.0 probability for all possibilities
			if (applicableEdgeSet.size()==0) {
				this.count=0;
				return result;
			}
			// if one, 100% probability to that one!
			if (applicableEdgeSet.size()==1) {
				this.count=0;
				final Edge edge=applicableEdgeSet.iterator().next();
				result.put(edge, 1.0);
				return result;
			}
			// now we turn to the interesting cases
			final Fatigue fatigue=((Fatigue)state.getStateMachine().getStateMachineGroup().getVariable("fatigue").elementAt(0));
			double stayInSleepModeProbability=1.0;
			double awakenProbability=0.0;
			double goToNextSleepModeProbability=0.0;
			final State currentState=state;
			final int index=Arrays.asList(sleepMode).indexOf(currentState.getBaseName());
			if (index<0) {
				throw new IllegalStateException("State machine in impossible state "+currentState.getName());
			}
			final ZonedDateTime currentTime=state.getStateMachine().getStateMachineSystem().getEngineData().getTime().getTime().atZone(ZoneId.systemDefault());
			double fatigueModifier=0.0;
			if (index==0) {
				fatigueModifier=1.0-Math.exp(-100*(1.0-fatigue.getValue()));
			} else {
				fatigueModifier=1.0-Math.exp(-50*fatigue.getValue());
			}


			StateMachine dailyCycleStateMachine=null;
			StateMachine sleepStateMachine=null;
			double maxProbability=1.0;
			final Vector<Combination.Domain> domainVector=type.getElementType();
			Combination.Domain sleepStateDomain,dayCycleDomain;
			for (int i=0; i<domainVector.size(); ++i) {
				final StateMachine sm=(StateMachine)domainVector.get(i).getNamedObject();
				if (sm.getBaseName().contains(sleepStateName)) {
					sleepStateMachine=sm;					
				} else if (sm.getBaseName().contains(modeStateName)) {
					dailyCycleStateMachine=sm;
				}
			} 
			if (isSleepTime(dailyCycleStateMachine.getCurrentState())) {
				maxProbability=fatigueModifier*maximumProbabilityDuringSleepPeriodToStay[index];
			} else {
				maxProbability=fatigueModifier*maximumProbabilityDuringAwakePeriodToStay[index];
			}
			long d=-currentTime.toLocalDateTime().until(lastSleepStateChangeTime.atZone(ZoneId.systemDefault()).toLocalDateTime(), ChronoUnit.MINUTES);
			final double duration=((double)d)/60.0;
			awakenProbability=awaken.evaluate(sleepStateMachine);
			if (awakenProbability<0.0) {
				awakenProbability=0.0;
			}
			stayInSleepModeProbability=maxProbability*bodyProcessFunctionProbability(duration, typicalDurationOfPeriod[index], 0.01)-awakenProbability+0.0;
			if (stayInSleepModeProbability<0.0) {
				stayInSleepModeProbability=0.0;
			}
			goToNextSleepModeProbability=1.0-stayInSleepModeProbability-awakenProbability;
			//TODO
			for (Edge edge:result.keySet()) {
				if (edge.getBaseName().contains("___awakeEdge")) {
					result.put(edge, awakenProbability);
				} else if (edge.getBaseName().contains("___goToNextSleepModeEdge")) {
					result.put(edge, goToNextSleepModeProbability);
				} else if (edge.getBaseName().contains("___stayInXEdge")) {
					result.put(edge, stayInSleepModeProbability);
				} else {
					throw new IllegalStateException("Impossible state, found edge"+edge.getBaseName());
				}
			}
			if (sleepStateMachine.isTracing()) {
				System.out.println("Stay in sleep state :"+stayInSleepModeProbability+" based on fatigue modifier = "+fatigueModifier+" max probability="+maxProbability+" and duration="+duration);
			}

			return result;
		}



	}

	//TODO
	static class SleepDirectionProbabilityFunction extends StateEdgeProbabilityFunctionSpecification.Function {
		private static Awaken awaken=new Awaken();
		private static  String sleepMode[]={"awake","stage1","stage2","stage3","stage4","REM"};
		private static double typicalDurationOfPeriod[]={18.0,0.1,0.42,0.1,0.1,0.2};
		private static double maximumProbabilityDuringSleepPeriodToStay[]={0.5,1.0,1.0,1.0,1.0,1.0};
		private static double maximumProbabilityDuringAwakePeriodToStay[]={1.0,0.5,0.5,0.7,0.7,0.5};




		private double count=0.0;
		private final double step=0.01;
		public SleepDirectionProbabilityFunction() {
			super();
		}

		/* (non-Javadoc)
		 * @see stateMachine.StateEdgeProbabilityFunctionSpecification#evaluate()
		 */
		@Override
		public TreeMap<Edge, Double> evaluate(Type type,State state, Set<Edge> edgeSet, Set<Edge> applicableEdgeSet) {
			TreeMap<Edge,Double> result=new TreeMap<Edge,Double>();
			for (Edge edge:edgeSet) {
				result.put(edge, 0.0);
			}



			// if zero, 0.0 probability for all possibilities
			if (applicableEdgeSet.size()==0) {
				this.count=0;
				return result;
			}
			// if one, 100% probability to that one!
			if (applicableEdgeSet.size()==1) {
				this.count=0;
				final Edge edge=applicableEdgeSet.iterator().next();
				result.put(edge, 1.0);
				return result;
			}
			// now we turn to the interesting cases

			double equal=1.0/result.size();
			for (Edge edge:edgeSet) {
				result.put(edge,equal);
			}

			return result;
		}



	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final String[] virtualSubjects={"1-1-2"};
		//Mode.Type type=new Mode.Type("The type");
		final String dayStateNames[]={"evening_night","night","night_morning","morning","morning_lunch","lunch","lunch_afternoon","afternoon","afternoon_evening","evening"};

		//typeSet.add(type);

		//Mode.Type sleepType=new Mode.Type("sleepType");
		final String sleepMode[]={"awake","stage1","stage2","stage3","stage4","REM"};
		//typeSet.add(sleepType);

		cfg=new Configuration(typeSet,Instant.parse("2015-01-01T00:00:00Z"),Instant.parse("2015-02-28T00:00:00Z")) {
			StateMachine sm;
			StateMachine sleepStateMachine;
			@Override
			public void initialize(StateMachineSystem sms,Event tick) {
				
				
				String[] priorityNames={"high","medium","variableUpdate","low"};
				Priority.initializePriority(0,4);
				Priority.setAlias(Priority.getPriority(0),"high");
				Priority.setAlias(Priority.getPriority(1),"medium");
				Priority.setAlias(Priority.getPriority(2), "variableUpdate");
				Priority.setAlias(Priority.getPriority(3), "low");

				RandomEngine randomEngine=new MersenneTwister(6L);

				
				RandomNumberGeneratorConfiguration[] rncgArr={
						new RandomNumberGeneratorConfiguration(modeStateName,randomEngine.nextLong()),
						new RandomNumberGeneratorConfiguration(sleepStateName,randomEngine.nextLong()),
						new RandomNumberGeneratorConfiguration(sleepDirectionName,randomEngine.nextLong()),
						new RandomNumberGeneratorConfiguration(disruptorName,randomEngine.nextLong())


				};
				StateMachineGroup theSmg=sms.getOrCreateStateMachineGroup(theSmgName, sms.getStateMachineGroupRoot());
				theSmg.setCurrentVirtualSubject(virtualSubjects[0]);
				for (String vs:virtualSubjects) {
					this.getLog().addVirtualSubject(vs);
				}


				@SuppressWarnings("unchecked")
				Variable<Double> dayLength=(Variable<Double>) theSmg.getOrCreateVariable(Variable.Type.Object, "dayLength");
				dayLength.add(Uniform.random(16.0, 18.0, randomEngine));
				
				Variable<Fatigue> fatigue=(Variable<Fatigue>) theSmg.getOrCreateVariable(Variable.Type.Object,"fatigue");
				fatigue.add(new Fatigue(1.0,dayLength.get(0)));

				@SuppressWarnings("unchecked")
				Variable<Duration> dayCycleRemainDuration=(Variable<Duration>) theSmg.getOrCreateVariable(Variable.Type.Object, "dayCycleRemainDuration");
				@SuppressWarnings("unchecked")
				Variable<Duration> dayCycleTransferDuration=(Variable<Duration>) theSmg.getOrCreateVariable(Variable.Type.Object, "dayCycleTranserDuration");
				for (int i=0; i<dayStateNames.length; ++i) {
					dayCycleRemainDuration.add(Duration.parse("PT0h"));
					dayCycleTransferDuration.add(Duration.parse("PT0h"));
				}

				
				@SuppressWarnings("unchecked")
				Variable<Instant> disruptionTime=(Variable<Instant>) theSmg.getOrCreateVariable(Variable.Type.Object, "disruptionTime");
				double variation=Uniform.random(0, 7, randomEngine);
				final String disruptionTimeSpec="P"+((int)14+(int)Math.floor(variation))+"DT"+(int)Math.floor((variation-Math.floor(variation))*24.0)+"H";
				Duration duration=Duration.parse(disruptionTimeSpec);
				Instant disruptionInstant=this.getInterval()[0].plus(duration);
				disruptionTime.add(disruptionInstant);
				System.out.println(disruptionInstant);
				System.exit(0);
				
				final Path pathToDisruptionTime=new Path();
				pathToDisruptionTime.addPart(new Path.Part("..",0));
				pathToDisruptionTime.addPart(new Path.Part("disruptionTime",0));


				StateMachine sleepDirection=theSmg.getOrCreateStateMachine(sleepDirectionName,rncgArr[2].getSeed(),Priority.getPriority("low"));
				
				StateMachine disruptorStateMachine=theSmg.getOrCreateStateMachine(disruptorName, rncgArr[3].getSeed(), Priority.getPriority("low"));

				this.setAdvanceTime(Duration.parse("PT1M"));
				this.getLog().setTracer(new AppTracer(this.getLog(),theSmg));


				for (RandomNumberGeneratorConfiguration rncg:rncgArr ) {
					sms.getEngineData().addRandomNumberGenerator(rncg.getName(), rncg.getSeed());
				}
				sm=theSmg.getOrCreateStateMachine(modeStateName,rncgArr[0].getSeed(),Priority.getPriority("high"));
				sm.setTracing(Trace.lvl1);
				String edgeNames[][]=new String[dayStateNames.length][2];
				int start=0;
				for (String dayStateName:dayStateNames) {
					State state=sm.getOrCreateState(dayStateName);
					edgeNames[start][0]=dayStateName;
					final int previous=(dayStateNames.length+start-1)%dayStateNames.length;
					edgeNames[previous][1]=dayStateName;
					++start;
				}
				final ModeState modeState=sm.getModeState();
				modeState.addValue(Mode.getMode(sm,sm.getOrCreateState("evening_night")));

				// edges
				// create default conditions and do nothing actions


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

					final int index=i;

					Action doNothingAction=new Action("only update durations"+i,sms) {
						public boolean evaluate() {
							final ZonedDateTime currentTime=sms.getEngineData().getTime().getTime().atZone(ZoneId.systemDefault());
							Duration remainDuration=Duration.between(currentTime,startOfMode);
							Duration transferDuration=Duration.between(currentTime,startOfTransferMode);
							((Variable<Duration>)sm.getStateMachineGroup().getVariable("dayCycleRemainDuration")).setElementAt(remainDuration,index);
							((Variable<Duration>)sm.getStateMachineGroup().getVariable("dayCycleTransferDuration")).setElementAt(remainDuration,index);

							return true;
						}
					};

					Condition remainIntervalCondition=new Condition(dayStateNames[i]+"_remainCondition",sms) {
						@Override
						public  boolean evaluate(StateMachine sm) {
							return true;
//							final ZonedDateTime currentTime=sms.getEngineData().getTime().getTime().atZone(ZoneId.systemDefault());
//							final int hour=currentTime.getHour();
//							final int diff=hour-startOfMode.getHour();
//							if (diff>=0 || diff<-20) {
//								if (diff>0||diff<-20) {
//									return true;
//								}
//								final int minute=currentTime.getMinute();
//								if (minute>=startOfMode.getMinute()) {
//									return true;
//								} 
//							}
//							return false;
						}
					};
					TransitionRule remainRule=sm.getTransitionRule(dayStateNames[i]+"_remainRule",tick,remainIntervalCondition,doNothingAction);

					// add a remain edge to each state
					Edge remainEdge=sm.getEdge(dayStateNames[i]+"___"+dayStateNames[i],dayStateNames[i],dayStateNames[i],remainRule);
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
					TransitionRule transferRule=sm.getTransitionRule(dayStateNames[i]+"_transferRule",tick,transferIntervalCondition,transferAction);
					Edge transferEdge=sm.getEdge(edgeNames[i][0]+"___"+edgeNames[i][1],edgeNames[i][0],edgeNames[i][1],transferRule);
					this.getLog().addEventType(transferEdge.getEventName(), "daily cycle");

					Edge[] edges={remainEdge,transferEdge};
					Combination.Domain d=StateDomain.getDomain(sm);
					Vector<Combination.Domain> domainVector=new Vector<Combination.Domain>();
					domainVector.add(d);
					Combination.Type combinationType=new Combination.Type(domainVector);
					sm.addStateEdgeProbabilitySpecification(new StateEdgeProbabilityFunctionSpecification(combinationType, sm.getOrCreateState(startStateName), Arrays.asList(edges), new DayCycleProbabilityFunction()));


				}
				sm.setStartState(sm.getOrCreateState(dayStateNames[0]));
				sm.setAllStatesAreDefined(true);

				// sleep state machine
				sleepStateMachine=theSmg.getOrCreateStateMachine(sleepStateName,rncgArr[1].getSeed(),Priority.getPriority("medium"));
				sleepStateMachine.setTracing(Trace.lvl1);

				String sleepEdgeNames[][]=new String[sleepMode.length][2];
				int sleepStart=0;
				for (String sleepStage:sleepMode) {
					State state=sleepStateMachine.getOrCreateState(sleepStage);
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
				sleepModeState.addValue(Mode.getMode(sleepStateMachine,sleepStateMachine.getOrCreateState("awake")));
				String[][] stageIntervalStrings={
						{"PT17H","PT18H"}, // awake
						{"PT90M","PT110M"}, // stage 1
						{"PT90M","PT110M"}, // stage 2
						{"PT90M","PT110M"}, // stage 3
						{"PT90M","PT110M"}, // stage 4
						{"PT90M","PT110M"} // REM
				};
//				HashMap<String,Double> sleepInterval=new HashMap<String,Double>();
//				//TODO
//				sleepInterval.put(sleepMode[0],dayLength.elementAt(0));
//				double remaining=24.0-sleepInterval.get(sleepMode[0]);
//				sleepInterval.put(sleepMode[2], remaining/2.0);
//				sleepInterval.put(sleepMode[5],remaining*0.2);
//				sleepInterval.put(sleepMode[1],remaining*0.1);
//				sleepInterval.put(sleepMode[3],remaining*0.1);
//				sleepInterval.put(sleepMode[4],remaining*0.1);
				{
					for ( int i=0; i<sleepMode.length; ++i) {
						final String slmo=sleepMode[i];
						Condition stayInXCondition=new Condition("stayIn_"+sleepMode[i]+"_Condition",sms) {
							public boolean evaluate(StateMachine sm) {
								return true;
							};
						};
						Action stayInXAction=null;
						final String stayInXActionName="stayIn_"+sleepMode[i]+"_Action";
						stayInXAction= new Action(stayInXActionName,sms) {

							@Override
							public boolean evaluate(StateMachine sm) {
								final Fatigue fatigue=((Fatigue)sm.getStateMachineGroup().getVariable("fatigue").elementAt(0));
								fatigue.increaseValue(sm.getCurrentState());
								return true;
							}
						};
						TransitionRule stayInXRule=sleepStateMachine.getTransitionRule(sleepEdgeNames[i][0]+"_remainRule",tick,stayInXCondition,stayInXAction);
						final String stayInXEdgeName=sleepEdgeNames[i][0]+"___"+sleepEdgeNames[i][0]+"___stayInXEdge";
						Edge stayInXEdge=sleepStateMachine.getEdge(stayInXEdgeName,sleepMode[i],sleepMode[i],stayInXRule);
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
								final Fatigue fatigue=((Fatigue)sm.getStateMachineGroup().getVariable("fatigue").elementAt(0));
								lastSleepStateChangeTime=sm.getStateMachineSystem().getEngineData().getTime().getTime();
								fatigue.increaseValue(sm.getOrCreateState(endState));
								return true;
							}
						};
						TransitionRule goToNextRule=sleepStateMachine.getTransitionRule(sleepEdgeNames[i][0]+"_transferRule",tick,goToNextCondition,goToNextAction);
						final String goToEdgeName=sleepEdgeNames[i][0]+"___"+sleepEdgeNames[i][1]+"___goToNextSleepModeEdge";
						Edge goToNextEdge=sleepStateMachine.getEdge(goToEdgeName,sleepEdgeNames[i][0],sleepEdgeNames[i][1],goToNextRule);
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
									final Fatigue fatigue=((Fatigue)sm.getStateMachineGroup().getVariable("fatigue").elementAt(0));
									lastSleepStateChangeTime=sm.getStateMachineSystem().getEngineData().getTime().getTime();
									fatigue.increaseValue(sm.getOrCreateState(endState));
									return true;
								}

							};
							TransitionRule goToAwakeRule=sleepStateMachine.getTransitionRule("awake"+"_specialTransferRule_from_"+sleepMode[i],tick,goToAwakeCondition,goToAwakeAction);
							goToAwakeEdge=sleepStateMachine.getEdge(sleepEdgeNames[i][0]+"___"+sleepEdgeNames[0][0]+"___awakeEdge",sleepEdgeNames[i][0],sleepEdgeNames[0][0],goToAwakeRule);
							this.getLog().addEventType(goToAwakeEdge.getEventName(), "circadian");

						}

						Edge[] edges={stayInXEdge,goToNextEdge};
						Vector<Edge> edgeVector=new Vector<Edge>();
						edgeVector.addAll(Arrays.asList(edges));

						if (i>0) {
							edgeVector.add(goToAwakeEdge);
						}
						Combination.Domain d=StateDomain.getDomain(sleepStateMachine);
						Vector<Combination.Domain> domainVector=new Vector<Combination.Domain>();
						domainVector.add(d);
						Combination.Domain d2=StateDomain.getDomain(sm);
						domainVector.add(d2);
						Combination.Type combinationType=new Combination.Type(domainVector);
						sleepStateMachine.addStateEdgeProbabilitySpecification(new StateEdgeProbabilityFunctionSpecification(combinationType, sleepStateMachine.getOrCreateState(sleepEdgeNames[i][0]), edgeVector, new SleepStateProbabilityFunction()));



					}
				}

				sleepStateMachine.setStartState(sleepStateMachine.getOrCreateState(sleepMode[0]));
				sleepStateMachine.setAllStatesAreDefined(true);


				final String[] sleepDirectionStateNames={"notResting","resting"};
				for (int i=0; i<sleepDirectionStateNames.length; ++i) {
					final String sdsn=sleepDirectionStateNames[i];
					State state=sleepDirection.getOrCreateState(sdsn);


				}
				ModeState sleepDirectionModeState=sleepDirection.getModeState();
				sleepDirectionModeState.addValue(Mode.getMode(sleepDirection, sleepDirection.getOrCreateState("notResting")));

				final String[][][] sleepDirectionBaseEdges={
						{ // 0
							{ sleepDirectionStateNames[0],sleepDirectionStateNames[0] }, // 0
							{ sleepDirectionStateNames[0],sleepDirectionStateNames[1] }, // 1
						}, 
						{ // 1
							{ sleepDirectionStateNames[1],sleepDirectionStateNames[1] }, // 0
							{ sleepDirectionStateNames[1],sleepDirectionStateNames[0] } // 1
						}
				};

				final Path pathToDailyCycleStateMachine=new Path();
				pathToDailyCycleStateMachine.addPart(new Path.Part("..",0)).addPart(new Path.Part(modeStateName, 0));
				final Path pathToSleepModeStateMachine=new Path();
				pathToSleepModeStateMachine.addPart(new Path.Part("..",0)).addPart(new Path.Part(sleepStateName, 0));
				final HashMap<AlertnessConditionSpecification,Edge> acs2e=new HashMap<AlertnessConditionSpecification,Edge>();

				HashSet<Mode> ms2=new HashSet<Mode>();
				for (int i=0; i<sleepDirectionBaseEdges.length; ++i) {
					Condition[] sX_sY_Condition=new Condition[2];;
					Action[] sX_sY_Action=new Action[2];
					TransitionRule[] sX_sY_TransitionRule=new TransitionRule[2];
					Edge[] sX_sY_Edge=new Edge[2];
					for (int j=0; j<sleepDirectionBaseEdges.length; ++j) {

						final String edgeBaseName=sleepDirectionBaseEdges[i][j][0]+"___"+sleepDirectionBaseEdges[i][j][1]+"___sleepDirectionEdge";
						if (j%2==0) { // remain

							// transfer edge
							sX_sY_Condition[j]=new Condition(edgeBaseName+"_remainCondition"+i,sms) {
								@Override
								public boolean evaluate(StateMachine sm) {


									return true;
								};
							};


						} else { // transfer
							final boolean notResting=sleepDirectionBaseEdges[i][j][0].compareTo("notResting")==0;

							// transfer edge
							sX_sY_Condition[j]=new Condition(edgeBaseName+"_transferCondition"+i,sms) {
								@Override
								public boolean evaluate(StateMachine sm) {
									final Fatigue fatigue=((Fatigue)sm.getStateMachineGroup().getVariable("fatigue").elementAt(0));
									final StateMachine dailyCycleStateMachine=sm.getStateMachine(pathToDailyCycleStateMachine);
									final StateMachine sleepModeStateMachine=sm.getStateMachine(pathToSleepModeStateMachine);

									boolean fatigueCondition=false;
									boolean awakeToSleepOrViceVersa=false;
									if (notResting) {
										if (isSleepTime(dailyCycleStateMachine.getCurrentState())) {
											fatigueCondition=Double.valueOf(fatigue.getValue())>0.90;
										} else {
											fatigueCondition=Double.valueOf(fatigue.getValue())>0.99;
										}
										awakeToSleepOrViceVersa=!sleepModeStateMachine.getCurrentState().equals(sleepModeStateMachine.getOrCreateState("awake"));


									} else { // descending
										if (isSleepTime(dailyCycleStateMachine.getCurrentState())) {
											fatigueCondition=Double.valueOf(fatigue.getValue())<0.10;
										} else {
											fatigueCondition=Double.valueOf(fatigue.getValue())<0.01;
										}	
										awakeToSleepOrViceVersa=sleepModeStateMachine.getCurrentState().equals(sleepModeStateMachine.getOrCreateState("awake"));

									}


									return fatigueCondition && awakeToSleepOrViceVersa;
								};

							};

						}
						sX_sY_Action[j]=new Action(edgeBaseName+"_Action"+i,sms) {
							@Override
							public boolean evaluate(StateMachine sm) {
								return true;
							}

						};
						sX_sY_TransitionRule[j]=sleepDirection.getTransitionRule(edgeBaseName+"_TransitionRule"+i,tick,sX_sY_Condition[j],sX_sY_Action[j]);
						sX_sY_Edge[j]=sleepDirection.getEdge(edgeBaseName+"_Edge"+i,sleepDirectionBaseEdges[i][j][0],sleepDirectionBaseEdges[i][j][1],sX_sY_TransitionRule[j]);
						this.getLog().addEventType(sX_sY_Edge[j].getEventName(), "sleep direction");
					}

					Edge[] edges={sX_sY_Edge[0],sX_sY_Edge[1]};
					Vector<Edge> edgeVector=new Vector<Edge>();
					edgeVector.addAll(Arrays.asList(edges));

					Combination.Domain d=StateDomain.getDomain(sleepDirection);
					Vector<Combination.Domain> domainVector=new Vector<Combination.Domain>();
					domainVector.add(d);
					Combination.Domain d2=StateDomain.getDomain(sleepStateMachine);
					domainVector.add(d2);
					Combination.Type combinationType=new Combination.Type(domainVector);
					sleepDirection.addStateEdgeProbabilitySpecification(new StateEdgeProbabilityFunctionSpecification(combinationType, sleepDirection.getOrCreateState(sleepDirectionBaseEdges[i][0][0]), edgeVector, new SleepDirectionProbabilityFunction()));


				}
				sleepDirection.setAllStatesAreDefined(true);
				sleepDirection.setStartState(sleepDirection.getOrCreateState("notResting"));
				sleepDirection.setTracing(Trace.lvl1);


				String disruptorStateName[]={"awaitingDisruption","disrupted"};
				for (int i=0; i<disruptorStateName.length; ++i) {
					final String dsn=disruptorStateName[i];
					State state=disruptorStateMachine.getOrCreateState(dsn);
				}
				
				final String[][][] disruptionBaseEdges={
					{ // 0 - awaitingDisruption
						{
							disruptorStateName[0],disruptorStateName[0]
						},
						{
							disruptorStateName[0],disruptorStateName[1]							
						}
						
					},
					{ // 1 - disrupted
						{
							disruptorStateName[1],disruptorStateName[1]							
						}
					}
				};
				
				for (int i=0; i<disruptionBaseEdges.length; ++i) {
					Condition[] sX_sY_Condition=new Condition[disruptionBaseEdges[i].length];
					Action[] sX_sY_Action=new Action[disruptionBaseEdges[i].length];
					TransitionRule[] sX_sY_TransitionRule=new TransitionRule[disruptionBaseEdges[i].length];
					Edge[] sX_sY_Edge=new Edge[disruptionBaseEdges[i].length];
					for (int j=0; j<disruptionBaseEdges[i].length;++j) {
						final String edgeBaseName=disruptionBaseEdges[i][j][0]+"___"+disruptionBaseEdges[i][j][1];
						String remainOrTransfer;
						if (j==0) { // remain
							remainOrTransfer="remain";
							sX_sY_Condition[j]=new Condition(edgeBaseName+"___remainCondition",sms) {
								@Override
								public boolean evaluate(StateMachine sm) {


									return true;
								};
							};
							sX_sY_Action[j]=new Action(edgeBaseName+"___remainAction",sms) {
								@Override
								public boolean evaluate(StateMachine sm) {


									return true;
								};
							
							};

						} else { // transfer
							remainOrTransfer="transfer";
							sX_sY_Condition[j]=new Condition(edgeBaseName+"___transferCondition",sms) {
								@Override
								public boolean evaluate(StateMachine sm) {
									final Instant currentTime=this.getStateMachineSystem().getEngineData().getTime().getTime();
									

									boolean result= currentTime.compareTo(disruptionTime.get(0))>0;
									return result;
								};
							};
							sX_sY_Action[j]=new Action(edgeBaseName+"___transferAction",sms) {
								@Override
								public boolean evaluate(StateMachine sm) {
									final Double dayLength=((Double)sm.getStateMachineGroup().getVariable("dayLength").elementAt(0));
									final Fatigue fatigue=((Fatigue)sm.getStateMachineGroup().getVariable("fatigue").elementAt(0));
									fatigue.setDayLength(dayLength);
									return true;
								};
							
							};
							
						}
						sX_sY_TransitionRule[j]=disruptorStateMachine.getTransitionRule(edgeBaseName+"___"+remainOrTransfer+"TransitionRule",tick,sX_sY_Condition[j],sX_sY_Action[j]);
						sX_sY_Edge[j]=disruptorStateMachine.getEdge(edgeBaseName+"___"+remainOrTransfer+"Edge", disruptorStateMachine.getOrCreateState(disruptionBaseEdges[i][j][0]), disruptorStateMachine.getOrCreateState(disruptionBaseEdges[i][j][1]), sX_sY_TransitionRule[j]);
						this.getLog().addEventType(sX_sY_Edge[j].getEventName(), "disruption");
					}
					Vector<Edge> edgeVector=new Vector<Edge>();
					edgeVector.addAll(Arrays.asList(sX_sY_Edge));
					
					Combination.Domain d=StateDomain.getDomain(disruptorStateMachine);
					Vector<Combination.Domain> domainVector=new Vector<Combination.Domain>();
					domainVector.add(d);
					Combination.Type combinationType=new Combination.Type(domainVector);
					disruptorStateMachine.addStateEdgeProbabilitySpecification(new StateEdgeProbabilityFunctionSpecification(combinationType, disruptorStateMachine.getOrCreateState(disruptionBaseEdges[i][0][0]), sX_sY_Edge, new DisruptionProbabilityFunction()));
					
				}
				disruptorStateMachine.setAllStatesAreDefined(true);
				disruptorStateMachine.setStartState(disruptorStateMachine.getOrCreateState(disruptorStateName[0]));


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
