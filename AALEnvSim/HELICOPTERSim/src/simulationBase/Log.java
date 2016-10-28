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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
	private PreparedStatement addEventType=null;
	private PreparedStatement addEvent=null;
	private PreparedStatement addDouble=null;
	private PreparedStatement addInteger=null;
	
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
		final Timestamp t=Timestamp.from(timestamp);
		final GregorianCalendar gc1=new GregorianCalendar();
		gc1.setTimeZone(TimeZone.getTimeZone("UTC"));
		final GregorianCalendar gc2=new GregorianCalendar();
		gc1.setTime(Date.from(timestamp));
		gc2.setTime(t);
		int difference=(gc1.get(Calendar.HOUR)-gc2.get(Calendar.HOUR));
		Instant tmp=timestamp;
		if (difference!=0) {
			final Duration duration=Duration.parse("PT1H");
			for (int i=0; i<Math.abs(difference); ++i) {
				if (difference>0) {
					tmp=tmp.plus(duration);
				} else {
					tmp=tmp.minus(duration);
				}
			}
		}
		final Timestamp theTimestamp=Timestamp.from(tmp);
		return theTimestamp;

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
			addEventType=connection.prepareStatement("INSERT INTO simtest.eventType(name,details) VALUES(?,?)",1);
			addEvent=connection.prepareStatement("INSERT INTO simtest.log(t,eid,information) VALUES (?,(SELECT id FROM simTest.eventType WHERE name=?),?)",1);
			addDouble=connection.prepareStatement("INSERT INTO simtest.doubleData(lid,name,data) VALUES((SELECT id FROM simTest.log WHERE eid=(SELECT id FROM simTest.eventType WHERE name=?) and t=?),?,?)",1);
			addInteger=connection.prepareStatement("INSERT INTO simtest.intData(lid,name,data) VALUES(?,?,?)",1);
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
	
	public synchronized void addEvent(final String eventTypeName,final Instant timestamp, final String comment)
												 {
		final Timestamp theTimestamp=convertInstantToTimestamp(timestamp);
		//final Timestamp theTimestamp=Timestamp.from(timestamp.atZone(ZoneId.of("UTC")).toInstant());
		try {

			addEvent.setString(2, eventTypeName);
			addEvent.setTimestamp(1,theTimestamp);
			addEvent.setString(3,comment);
			addEvent.executeUpdate();
			
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
		
		
	}
	public synchronized final void addDoubleData(final String eventTypeName,final Instant timestamp,final String name, final double data) {
		try {
			addDouble.setString(1,eventTypeName);
			final Timestamp theTimestamp=convertInstantToTimestamp(timestamp);
			addDouble.setTimestamp(2,theTimestamp);
			addDouble.setString(3,name);
			addDouble.setDouble(4, data);;
			addDouble.executeUpdate();
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
