package simulationBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Log {
	//public final static Log log=new Log();
	
	private static Integer noOfConnections=0;
	private static final int maximumNoOfConnections=10;

	private final String jdbcConnectionString="jdbc:mysql://localhost:3306/simtest";
	private final String userName="access";
	private final String password="accessOnly";
	
	private Connection connection=null;
	private PreparedStatement addVirtualSubject=null;
	private PreparedStatement addVirtualSubjectConfiguration=null;

	private PreparedStatement addEventType=null;
	private PreparedStatement addEvent=null;
	private PreparedStatement addDouble=null;
	private PreparedStatement addInteger=null;
	private PreparedStatement getEvents=null;
	private PreparedStatement getRelatedDoubleData=null;
	private PreparedStatement addResult=null;
	private PreparedStatement addResultParameter=null;
	
	public abstract static class Tracer {
		private Log log;
		public Tracer(Log log) {
			this.log=log;
		}
		public final synchronized Log getLog() {
			return log;
		}
		public abstract void addEvent(String eventTypeName,Instant t);
	};
	private Tracer tracer=null;
	
	private Timestamp convertInstantToTimestamp(final Instant timestamp) {
		Instant tmp=timestamp;
		final Duration duration=Duration.parse("PT1H");
		tmp=tmp.minus(duration);
		final Timestamp theTimestamp=Timestamp.from(tmp);
		return theTimestamp;

	}
	
	public  class LoggedEvent {
		private String virtualSubject;
		private int databaseIdentityOfLogRecord;
		private Timestamp timestamp;
		private int databaseIdentityOfEventType;
		private String name;
		private String details;
		private HashMap<String,Double> parameter=new HashMap<String,Double>();
		/**
		 * @param virtualSubject
		 * @param databaseIdentityOfLogRecord 
		 * @param timestamp
		 * @param databaseIdentityOfEventType
		 * @param name
		 * @param details
		 */
		LoggedEvent(String virtualSubject, int databaseIdentityOfLogRecord,
				Timestamp timestamp, int databaseIdentityOfEventType, String name, String details) {
			this.virtualSubject = virtualSubject;
			this.databaseIdentityOfLogRecord=databaseIdentityOfLogRecord;
			this.timestamp = timestamp;
			this.databaseIdentityOfEventType = databaseIdentityOfEventType;
			this.name = name;
			this.details = details;
		}
		/**
		 * @return the virtualSubject
		 */
		public synchronized final String getVirtualSubject() {
			return virtualSubject;
		}
		/**
		 * @return the timestamp
		 */
		public synchronized final Timestamp getTimestamp() {
			return timestamp;
		}
		/**
		 * @return the databaseIdentityOfEvent
		 */
		public synchronized final int getDatabaseIdentityOfEventType() {
			return databaseIdentityOfEventType;
		}
		/**
		 * @return the name
		 */
		public synchronized final String getName() {
			return name;
		}
		/**
		 * @return the details
		 */
		public synchronized final String getDetails() {
			return details;
		}
		/**
		 * @return the databaseIdentityOfLogRecord
		 */
		public synchronized final int getDatabaseIdentityOfLogRecord() {
			return databaseIdentityOfLogRecord;
		}
		public synchronized void put(String key, Double value) {
			this.parameter.put(key, value);			
		}
		
		public synchronized Double get(String key) {
			return this.parameter.get(key);
		}
		
		
	}
	
	public  class LoggedEventIterator implements Iterator<LoggedEvent> {
		private PreparedStatement query;
		private ResultSet resultSet;
		boolean initiated=false;
		boolean queriedForHasNext=false;
		boolean fetched=false;
		boolean lastResultFromHasNext=false;
		
		public LoggedEventIterator(final Connection connection, String regexpPositive, String regexpNegative, String virtualSubject) {
			
			try {
				this.query=connection.prepareStatement("SELECT virtualSubject.name, log.id, log.t, eventType.id,eventType.name,eventType.details from log inner join eventType on log.eid=eventType.id inner join virtualSubject on virtualSubject.id=log.vid WHERE eventType.name REGEXP ? and NOT (eventType.name REGEXP ?) and log.vid=(SELECT id FROM virtualSubject WHERE name=?) order by log.t",1);
				query.setString(1, regexpPositive);
				query.setString(2, regexpNegative);
				query.setString(3, virtualSubject);
				this.resultSet=query.executeQuery();
			} catch (SQLException e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		public synchronized boolean hasNext() {
			try {
				if (!initiated) {
					initiated=true;
					queriedForHasNext=true;
					fetched=false;
					lastResultFromHasNext=resultSet.next();
				} else {
					if (queriedForHasNext && ! fetched) {
					} else if (!queriedForHasNext && fetched) {
						queriedForHasNext=true;
						fetched=false;
						lastResultFromHasNext=resultSet.next();						
					} else {
						throw new IllegalStateException("Incorrect state of cursor");
					}
				}
				return lastResultFromHasNext;
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public synchronized LoggedEvent next() {
			if (!hasNext()) {
				return null;
			}
			try {
				final LoggedEvent loggedEvent=new LoggedEvent(
						resultSet.getString(1), // virtualSubject
						resultSet.getInt(2), // log record identity
						resultSet.getTimestamp(3), // timestamp
						resultSet.getInt(4), // event type identity
						resultSet.getString(5), // event type name
						resultSet.getString(6) // event type details
						);
				getRelatedDoubleData.setInt(1,resultSet.getInt(2));
				final ResultSet rs=getRelatedDoubleData.executeQuery();
				while (rs.next()) {
					final String name=rs.getString(1);
					final Double value=rs.getDouble(2);
					loggedEvent.put(name,value);
				}
				rs.close();
				this.fetched=true;
				this.queriedForHasNext=false;
				return loggedEvent;
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		}
		
	}



	private Log() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Where is your MySQL JDBC Driver?");
		}
		
		// user the driver to access the database
		try {
			connection = DriverManager
			.getConnection(jdbcConnectionString,userName, password);
	 
		} catch (SQLException e) {
			throw new IllegalStateException("Connection failed: "+e.getMessage());
		}
		
		// double check if connection==null, should really be caught when connection
		if (connection==null) {
			throw new IllegalStateException("Connection failed, null returned");
		}
		
		try {
			addVirtualSubject=connection.prepareStatement("INSERT INTO simtest.virtualSubject(name) VALUES(?)",1);
			addVirtualSubjectConfiguration=connection.prepareStatement("INSERT INTO virtualSubjectConfiguration(vid,name,value) VALUES((SELECT id FROM simTest.virtualSubject WHERE name=?),?,?)",1);
			addEventType=connection.prepareStatement("INSERT INTO simtest.eventType(name,details) VALUES(?,?)",1);
			addEvent=connection.prepareStatement("INSERT INTO simtest.log(t,eid,information,vid) VALUES (?,(SELECT id FROM simTest.eventType WHERE name=?),?,(SELECT id FROM simTest.virtualSubject WHERE name=?))",1);
			addDouble=connection.prepareStatement("INSERT INTO simtest.doubleData(lid,name,data) VALUES((SELECT id FROM simTest.log WHERE eid=(SELECT id FROM simTest.eventType WHERE name=?) and t=? and vid=(SELECT id FROM simTest.virtualSubject WHERE name=?)),?,?)",1);
			addInteger=connection.prepareStatement("INSERT INTO simtest.intData(lid,name,data) VALUES(?,?,?)",1);
			getEvents=connection.prepareStatement("SELECT log.vid, log.id, log.t, eventType.id,eventType.name,eventType.details from log inner join eventType on log.eid=eventType.id WHERE eventType.name REGEXP ? and NOT (eventType.name REGEXP ?) and log.vid=(SELECT id FROM virtualSubject WHERE name=?)",1);
			getRelatedDoubleData=connection.prepareStatement("SELECT name,data FROM doubleData WHERE id=?",1);
			addResult=connection.prepareStatement("INSERT INTO simtest.result(vid,t,commentText) VALUES ((SELECT id FROM virtualSubject WHERE name=?),?,?)",1);
			addResultParameter=connection.prepareStatement("INSERT INTO simtest.resultParameter(rid,name,value) VALUES (?,?,?)",1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static  Log createLog() {
		synchronized(noOfConnections) {
			while (noOfConnections>maximumNoOfConnections-1) {
				System.out.println("createLog: to many connections: noOfConnections="+noOfConnections+" waiting thread:"+Thread.currentThread().toString());
				try {
					noOfConnections.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			noOfConnections++;
			System.out.println("CreateLog: noOfConnections="+noOfConnections);

			Log log=new Log();
			return log;
		}
	}
	
	public synchronized void addVirtualSubject(final String name) {
		try {
			addVirtualSubject.setString(1, name);
			addVirtualSubject.executeUpdate();
		} catch (SQLException e) {
			if (e.getErrorCode()!=1062) {
				throw new IllegalStateException(e);
			}
		}
	}

	public synchronized void addVirtualSubjectConfiguration(final String name,final String parameter, final Double value) {
		try {
			addVirtualSubjectConfiguration.setString(1, name);
			addVirtualSubjectConfiguration.setString(2, parameter);
			addVirtualSubjectConfiguration.setDouble(3, value);
			
			addVirtualSubjectConfiguration.executeUpdate();
		} catch (SQLException e) {
			if (e.getErrorCode()!=1062) {
				throw new IllegalStateException(e);
			}
		}
	}

	
	public synchronized void addEventType(final String name,final String detail) {
		try {
			addEventType.setString(1, name);
			addEventType.setString(2, detail);
			addEventType.executeUpdate();
		} catch (SQLException e) {
			if (e.getErrorCode()!=1062) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	public synchronized void addEvent(final String virtualSubject,final String eventTypeName, final Instant timestamp, final String comment)
												 {
		final Timestamp theTimestamp=convertInstantToTimestamp(timestamp);
		//final Timestamp theTimestamp=Timestamp.from(timestamp.atZone(ZoneId.of("UTC")).toInstant());
		try {

			addEvent.setString(2, eventTypeName);
			addEvent.setTimestamp(1,theTimestamp);
			addEvent.setString(3,comment);
			addEvent.setString(4, virtualSubject);
			addEvent.executeUpdate();
			
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		
		
	}
	public synchronized final void addDoubleData(final String eventTypeName,final Instant timestamp,final String name, final double data, String virtualSubject) {
		try {
			addDouble.setString(1,eventTypeName);
			final Timestamp theTimestamp=convertInstantToTimestamp(timestamp);
			addDouble.setTimestamp(2,theTimestamp);
			addDouble.setString(3,virtualSubject);
			addDouble.setString(4,name);
			addDouble.setDouble(5, data);;
			addDouble.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException(e);

		}
	}
	
	public synchronized final void addResult(String virtualSubject,Timestamp timestamp, String comment,HashMap<String,Double> parameters) {
		try {
			addResult.setString(1,virtualSubject);
			addResult.setTimestamp(2, timestamp);
			addResult.setString(3, comment);
			addResult.executeUpdate();
			ResultSet rs = addResult.getGeneratedKeys();
		    rs.next();
		    final int rid= rs.getInt(1);
	    	addResultParameter.setInt(1,rid);
		    for (HashMap.Entry<String,Double> es2d:parameters.entrySet()) {
		    	addResultParameter.setString(2,es2d.getKey());
		    	addResultParameter.setDouble(3, es2d.getValue());
		    	addResultParameter.executeUpdate();
		    }
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}
	



	public Connection getConnection() {
		return connection;
	}
	



	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		synchronized(noOfConnections) {
			noOfConnections.notifyAll();
			--noOfConnections;
			System.out.println("Finalize: noOfConnections="+noOfConnections);
		}
	}

	/**
	 * @return the tracer
	 */
	public synchronized final Tracer getTracer() {
		return tracer;
	}

	/**
	 * @param tracer the tracer to set
	 */
	public synchronized final void setTracer(Tracer tracer) {
		this.tracer = tracer;
	}
	
	public synchronized final void callTracer(final String eventTypeName, Instant timestamp) {
		if (this.tracer!=null) {
			this.tracer.addEvent(eventTypeName, timestamp);
		}
	}
	
	

}
