package replayDailyrhythm3v2;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import se.his.drts.ilodids.DataStream;
import se.his.drts.ilodids.Euclid;
import se.his.drts.ilodids.EuclidDate;
import se.his.drts.ilodids.EuclidDouble;
import se.his.drts.ilodids.MultiDimensionalPoint;
import se.his.drts.ilodids.MultiDimensionalPointType;
import se.his.drts.ilodids.MultiDimensionalPointView;
import se.his.drts.ilodids.Parameter;
import se.his.drts.ilodids.Utility;
import simulationBase.Configuration;
import simulationBase.Log;
import stateMachine.Mode;
import stateMachine.ModeState;

public class ReplayDailyRhythm3 {
	static Set<Mode.Type> typeSet=new HashSet<Mode.Type>();
	static Set<ModeState> modeStateSet=new HashSet<ModeState>();
	static Configuration cfg;
	static MultiDimensionalPointType multiDimensionalPointType;
	static int samplePeriodInMinutes=5;
	static int expirationWindowInMinutes=24*60*7; // a week
	static int analysisWindowInMinutes=24*60; // 24 hours
	static int analysisPeriodicityInMinutes=3*60; // 3 hours
	static String[] virtualSubjects={"1-2","1-3","1-4","1-5"};


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cfg=new Configuration(typeSet,Instant.parse("2015-01-01T00:00:00Z"),Instant.parse("2015-02-28T00:00:00Z"));
		multiDimensionalPointType=new MultiDimensionalPointType();
		multiDimensionalPointType.addParameter(new Parameter("Timestamp",EuclidDate.class));
		multiDimensionalPointType.addParameter(new Parameter("Occupancy",EuclidDouble.class));

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
	
	private class TestRunnable implements Runnable {
		private DataStream dataStream=new DataStream(16,multiDimensionalPointType);
		private String virtualSubject;
		private PrintStream ps;
		
		public TestRunnable(String virtualSubject) {
			this.virtualSubject=virtualSubject;
			File file=new File("./"+virtualSubject+"_log.txt");
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException(e);
			}
			this.ps=new PrintStream(fos);
		}



		private void insertDataPoint(Timestamp timestamp,double value) {
			Vector<Euclid> parVec=new Vector<Euclid>();
			parVec.add(new EuclidDate(timestamp));
			parVec.add(new EuclidDouble(value));
			dataStream.insertDataRecord(new MultiDimensionalPoint(multiDimensionalPointType, timestamp, parVec));
			//System.out.println(timestamp+" "+timestamp.getTime()%(analysisPeriodicityInMinutes*60L*1000L));
			if (timestamp.getTime()%(analysisPeriodicityInMinutes*60L*1000L)==0) {
				anomalyDetection(timestamp.toInstant());
			}

		}

		private void anomalyDetection(Instant t) {
			dataStream.requestSharedAccess();

			dataStream.setQuantileDistanceFactor(1.5);
			//dataStreamForFrequency[i].setQuantileDistanceFactor(((double)nextConfiguration.get("quantileDistanceFactor"))/10.0);

			Set<MultiDimensionalPointView> outliers=dataStream.outlierSet();

			TreeMap<Integer,Integer> noOfIncreasedOutliers=new TreeMap<Integer,Integer>();
			TreeMap<Integer,Integer> noOfDecreasedOutliers=new TreeMap<Integer,Integer>();

			for (MultiDimensionalPointView mdpv:outliers) {
				Vector<Double> neighbourValues=new Vector<Double>();
				for (MultiDimensionalPoint mdp:mdpv.getkNearestNeighbourPoints().keySet()) {
					neighbourValues.add(mdp.getParameter("Occupancy").getValueAsDouble());
				}
				Collections.sort(neighbourValues);
				final int noOfNeighbours=neighbourValues.size();
				final int middleOfNeighbours=(noOfNeighbours%2==0?noOfNeighbours/2+1:noOfNeighbours/2);
				Double median=Utility.median(neighbourValues,Utility.Quartile.middle);
				Double upperMedian=Utility.median(neighbourValues,Utility.Quartile.upper);
				Double lowerMedian=Utility.median(neighbourValues,Utility.Quartile.lower);
				// compute the index, 0-59 minutes back in time=0, 1h to 1h59 minutes=1, etc.
				final double nowInDays=((double)Timestamp.from(t).getTime())/(24.0*60.0*60.0*1000.0);
				final double outlierTimestampInDays=((double)mdpv.getDataRecord().getTimestamp().getTime())/(24.0*60.0*60.0*1000.0);
				int timeIndex=(int)(24.0*(nowInDays-outlierTimestampInDays));
				if (mdpv.getDataRecord().getParameterVector().get(1).getValueAsDouble()>=upperMedian) {
					Integer value;
					value=noOfIncreasedOutliers.get(timeIndex);

					if (value==null) {
						value=new Integer(0);
						noOfIncreasedOutliers.put(timeIndex, value);
					}
					++value;
					noOfIncreasedOutliers.put(timeIndex, value);
				} else if (mdpv.getDataRecord().getParameterVector().get(1).getValueAsDouble()<=lowerMedian) {
					Integer value;
					value=noOfDecreasedOutliers.get(timeIndex);
					if (value==null) {
						value=new Integer(0);
						noOfDecreasedOutliers.put(timeIndex, value);
					}
					++value;
					noOfDecreasedOutliers.put(timeIndex, value);

				}

			}
			int totalNoOfRecords=dataStream.getDate2MultiDimensionalPoint().size();

			dataStream.releaseSharedAccess();

			HashMap<String,Double> parameters=new HashMap<String,Double>();
			double noOfIO=noOfIncreasedOutliers.subMap(0,2).values().stream().reduce(0, (a,b)->a+b);
			double noOfDO=noOfDecreasedOutliers.subMap(0,2).values().stream().reduce(0, (a,b)->a+b);
			parameters.put("Increased outlier ratio",((double)noOfIO/(double)totalNoOfRecords));
			parameters.put("Decreased outlier ratio",((double)noOfDO/(double)totalNoOfRecords));
			parameters.put("Delta outlier ratio", ((double)(noOfIO-noOfDO)/(double)totalNoOfRecords));
			cfg.getLog().addResult(virtualSubject, Timestamp.from(t), "anomaly detection", parameters);

		}

		private void insertSamples(Instant start, Instant end,  double value) {
			final String deltaDurationString=String.format("PT%dM", samplePeriodInMinutes);
			final Duration delta=Duration.parse(deltaDurationString);
			final Duration interval=Duration.between(start,end);
			final long intervalInSeconds=interval.get(ChronoUnit.SECONDS);
			final long intervalInMinutes=intervalInSeconds/60L;
			long startInPeriodsSince1970Remainder=start.getEpochSecond()%(samplePeriodInMinutes*60L);
			long startInPeriodsSince1970Quotient=start.getEpochSecond()/(samplePeriodInMinutes*60L);
			if (intervalInMinutes<samplePeriodInMinutes) {

				long endInPeriodsSince1970Remainder=end.getEpochSecond()%(samplePeriodInMinutes*60L);
				long endInPeriodsSince1970Quotient=end.getEpochSecond()/(samplePeriodInMinutes*60L);

				if (startInPeriodsSince1970Quotient<endInPeriodsSince1970Quotient) {
					Instant timestamp=Instant.ofEpochSecond(endInPeriodsSince1970Quotient*samplePeriodInMinutes*60L);
					insertDataPoint(Timestamp.from(timestamp),value);
				}
			} else {
				Instant adjustedStart;
				if (startInPeriodsSince1970Remainder==0) {
					adjustedStart=start;
				} else {
					adjustedStart=Instant.ofEpochSecond((startInPeriodsSince1970Quotient+1)*samplePeriodInMinutes*60L);
				}
				for (Instant t=adjustedStart; t.compareTo(end)<0; t=t.plus(delta)) {

					insertDataPoint(Timestamp.from(t),value);
					if (t.getEpochSecond()%(analysisPeriodicityInMinutes*60L)==0) {
						anomalyDetection(t);
					}
				}
			}
		}

		@Override
		public void run() {
			Instant currentTime=null;
			Instant currentEndTime=null;
			double value=0.0;
			Log.LoggedEventIterator lei=cfg.getLog().new LoggedEventIterator(cfg.getLog().getConnection(), ".*awake.*->.*", ".*->.*awake.*", virtualSubject);
			Log.LoggedEventIterator lei2=cfg.getLog().new LoggedEventIterator(cfg.getLog().getConnection(),  ".*->.*awake.*", ".*awake.*->.*",virtualSubject);
			Log.LoggedEvent le1,le2,current;
			if (!lei.hasNext() && !lei2.hasNext()) {
				return;
			}
			le1=lei.next();
			le2=lei2.next();
			while (le1!=null && le2!=null) {
				if (le1.getTimestamp().compareTo(le2.getTimestamp())>0) {
					current=le2;
					le2=lei2.next();
					value=1.0;
					currentTime=le1.getTimestamp().toInstant();
					currentEndTime=le2.getTimestamp().toInstant();
					
				} else {
					current=le1;
					le1=lei.next();
					value=0.0;
					currentTime=le2.getTimestamp().toInstant();
					currentEndTime=le1.getTimestamp().toInstant();

				}
				this.ps.println(current.getVirtualSubject()+", "+current.getTimestamp()+", "+current.getName()+", "+current.getDetails()+", "+current.get("fatigue")+", "+currentTime+", "+currentEndTime+", "+Timestamp.from(currentEndTime));
				
				insertSamples(currentTime,currentEndTime,value);
				dataStream.requestSharedAccess();
				try {
					final Date firstTimestamp=dataStream.getDate2MultiDimensionalPoint().firstKey();
					dataStream.releaseSharedAccess();
					final Duration currentLogInterval=Duration.between(firstTimestamp.toInstant(), currentEndTime);
					if (currentLogInterval.get(ChronoUnit.SECONDS)<expirationWindowInMinutes*60) {
						dataStream.requestSharedAccess();
						Date lastNonremaningKey=dataStream.getDate2MultiDimensionalPoint().floorKey(firstTimestamp);
						Set<MultiDimensionalPoint> removalValues=new HashSet<MultiDimensionalPoint>();
						removalValues.addAll(((dataStream.getDate2MultiDimensionalPoint().subMap(firstTimestamp, lastNonremaningKey)).values()));
						dataStream.releaseSharedAccess();
						dataStream.removeDataRecord(removalValues);

					}
				} catch(NoSuchElementException nsee) {
					dataStream.releaseSharedAccess();
				}
				
			}
			while (le1!=null) {
				this.ps.println(le1.getVirtualSubject()+", "+le1.getTimestamp()+", "+le1.getName()+", "+le1.getDetails()+", "+le1.get("fatigue"));
				le1=lei.next();
				
			}
			while (le2!=null) {
				this.ps.println(le2.getVirtualSubject()+", "+le2.getTimestamp()+", "+le2.getName()+", "+le2.getDetails()+", "+le2.get("fatigue"));
				le2=lei2.next();
				
			}
			
		}
	};

	@Test
	public void test() {
		HashSet<Thread> threadSet=new HashSet<Thread>();
		for (String vs:virtualSubjects) {
			TestRunnable tr=new TestRunnable(vs);
			Thread trt=new Thread(tr);
			trt.start();
			threadSet.add(trt);
		}
		for (Thread t:threadSet) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
