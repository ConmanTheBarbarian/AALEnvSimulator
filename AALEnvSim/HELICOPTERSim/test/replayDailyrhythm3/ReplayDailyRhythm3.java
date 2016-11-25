package replayDailyrhythm3;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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
import se.his.drts.ilodids.Parameter;
import simulationBase.Configuration;
import simulationBase.Log;
import stateMachine.Mode;
import stateMachine.ModeState;

public class ReplayDailyRhythm3 {
	static Set<Mode.Type> typeSet=new HashSet<Mode.Type>();
	static Set<ModeState> modeStateSet=new HashSet<ModeState>();
	static Configuration cfg;
	static MultiDimensionalPointType multiDimensionalPointType;
	static DataStream dataStream;
	static int samplePeriodInMinutes=5;
	static int expirationWindow=24*60*7; // a week


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		cfg=new Configuration(typeSet,Instant.parse("2015-01-01T00:00:00Z"),Instant.parse("2015-02-28T00:00:00Z"));
		multiDimensionalPointType=new MultiDimensionalPointType();
		multiDimensionalPointType.addParameter(new Parameter("Timestamp",EuclidDate.class));
		multiDimensionalPointType.addParameter(new Parameter("Occupancy",EuclidDouble.class));
		dataStream=new DataStream(16,multiDimensionalPointType);

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
	
	private void insertDataPoint(Timestamp timestamp,double value) {
		Vector<Euclid> parVec=new Vector<Euclid>();
		parVec.add(new EuclidDate(timestamp));
		parVec.add(new EuclidDouble(value));
		dataStream.insertDataRecord(new MultiDimensionalPoint(multiDimensionalPointType, timestamp, parVec));

	}
	
	private void insertSamples(Instant start, Instant end,  double value) {
		final String deltaDurationString=String.format("PT%dM", samplePeriodInMinutes);
		final Duration delta=Duration.parse(deltaDurationString);
		final Duration interval=Duration.between(start,end);
		final long intervalInSeconds=interval.get(ChronoUnit.SECONDS);
		final long intervalInMinutes=intervalInSeconds/60L;
		if (intervalInMinutes<samplePeriodInMinutes) {
			
			long startInPeriodsSince1970Remainder=start.getEpochSecond()%(samplePeriodInMinutes*60L);
			long startInPeriodsSince1970Quotient=start.getEpochSecond()/(samplePeriodInMinutes*60L);
			long endInPeriodsSince1970Remainder=end.getEpochSecond()%(samplePeriodInMinutes*60L);
			long endInPeriodsSince1970Quotient=end.getEpochSecond()/(samplePeriodInMinutes*60L);

			if (startInPeriodsSince1970Quotient<endInPeriodsSince1970Quotient) {
				Instant timestamp=Instant.ofEpochSecond(endInPeriodsSince1970Quotient*samplePeriodInMinutes*60L);
				insertDataPoint(Timestamp.from(timestamp),value);
			}
		} else {
			for (Instant t=start; t.compareTo(end)<0; t=t.plus(delta)) {
				insertDataPoint(Timestamp.from(t),value);
			}
		}
	}

	@Test
	public void test() {
		Instant currentTime=null;
		Instant currentEndTime=null;
		double value=0.0;
		Log.LoggedEventIterator lei=cfg.getLog().new LoggedEventIterator(cfg.getLog().getConnection(), ".*awake.*->.*", ".*->.*awake.*", "1-1");
		Log.LoggedEventIterator lei2=cfg.getLog().new LoggedEventIterator(cfg.getLog().getConnection(),  ".*->.*awake.*", ".*awake.*->.*","1-1");
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
			System.out.println(current.getVirtualSubject()+", "+current.getTimestamp()+", "+current.getName()+", "+current.getDetails()+", "+current.get("fatigue")+", "+currentTime+", "+currentEndTime+", "+Timestamp.from(currentEndTime));
			
			insertSamples(currentTime,currentEndTime,value);
			dataStream.requestSharedAccess();
			final Date firstTimestamp=dataStream.getDate2MultiDimensionalPoint().firstKey();
			dataStream.releaseSharedAccess();
			final Duration currentLogInterval=Duration.between(firstTimestamp.toInstant(), currentEndTime);
			if (currentLogInterval.get(ChronoUnit.SECONDS)<expirationWindow*60) {
				dataStream.requestSharedAccess();
				Date lastNonremaningKey=dataStream.getDate2MultiDimensionalPoint().floorKey(firstTimestamp);
				Set<MultiDimensionalPoint> removalValues=new HashSet<MultiDimensionalPoint>();
				removalValues.addAll(((dataStream.getDate2MultiDimensionalPoint().subMap(firstTimestamp, lastNonremaningKey)).values()));
				dataStream.releaseSharedAccess();
				dataStream.removeDataRecord(removalValues);

			}
			
			
		}
		while (le1!=null) {
			System.out.println(le1.getVirtualSubject()+", "+le1.getTimestamp()+", "+le1.getName()+", "+le1.getDetails()+", "+le1.get("fatigue"));
			le1=lei.next();
			
		}
		while (le2!=null) {
			System.out.println(le2.getVirtualSubject()+", "+le2.getTimestamp()+", "+le2.getName()+", "+le2.getDetails()+", "+le2.get("fatigue"));
			le2=lei2.next();
			
		}
	}

}
