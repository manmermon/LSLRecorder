package Sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.WarningMessage;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.IHandlerMinion;
import Controls.IHandlerSupervisor;
import Controls.MinionParameters;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.Sync.SyncMarker;
import Sockets.Info.StreamInputMessage;
import StoppableThread.AbstractStoppableThread;

public class SocketMessageDelayCalculator extends AbstractStoppableThread implements INotificationTask, IHandlerMinion
{
	public static final int DEFAULT_NUM_PINGS = 4;
	
	private StreamInputMessage socketMsg = null; 

	private int Mark = SyncMarker.NON_MARK;
		
	private int numberOfPings = 4;
	
	private ITaskMonitor monitor;
	
	private List< EventInfo > events;
	
	private IHandlerSupervisor supervisor;
	
	private boolean working;
	
	public SocketMessageDelayCalculator( StreamInputMessage msg, int markValue, int numPings ) 
	{
		this.socketMsg = msg;

		this.Mark = markValue;
		
		this.events = new ArrayList<EventInfo>();
		
		if( numPings > 0 )
		{
			this.numberOfPings = numPings;
		}
		
		super.setName( super.getClass().getSimpleName() + "-" + msg.getOrigin() );
	}

	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();

		this.working = true;
		
		super.stopThread = true;
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		if( this.socketMsg != null )
		{
			double time = this.socketMsg.receivedTime();

			double rtt = this.RTT( this.socketMsg.getOrigin(), this.numberOfPings );
			
			if( rtt != Double.NaN 
					&& rtt != Double.POSITIVE_INFINITY
					&& rtt != Double.NEGATIVE_INFINITY )
			{
				time = time - rtt/ 2;
			}
			
			if( this.monitor != null )
			{
				EventInfo ev = new EventInfo( EventType.INPUT_MARK_READY, new SyncMarker( this.Mark, time ) );

				synchronized ( this.events )
				{
					this.events.add( ev );
				}				
				
				this.monitor.taskDone( this );
			}
		}
	}
	
	@Override
	protected void runExceptionManager(Exception e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			e.printStackTrace();
			
			if( this.monitor != null )
			{
				double time = System.nanoTime() / 1e9D;
				
				if( this.socketMsg != null )
				{
					time = this.socketMsg.receivedTime();
				}
				
				EventInfo ev = new EventInfo( EventType.INPUT_MARK_READY, new SyncMarker( this.Mark, time ) );

				synchronized ( this.events )
				{
					this.events.add( ev );
				}				
				
				try 
				{
					this.monitor.taskDone( this );
				}
				catch (Exception e1) 
				{
				}				
			}
		}
	}

	@Override
	protected void cleanUp() throws Exception 
	{
		this.working = false;
		
		if( this.supervisor != null )
		{
			this.supervisor.eventNotification( this, new EventInfo( EventType.SOCKET_PING_END, this ));
		}
		
		super.cleanUp();
	}
	
	private double RTT( InetSocketAddress ipAddress, int numPings ) throws IOException
	{
		InetAddress geek = ipAddress.getAddress();
		
		double time = Double.POSITIVE_INFINITY;
		
		for( int i = 0; i < numPings; i++ )
		{
			long t = System.nanoTime();
			
			if ( geek.isReachable( 5000 ) )
			{
				t = ( System.nanoTime() - t);
				
				if( t < time )
				{
					time = t / 1e9D;
				}
			}
		}		
		
		double rtt = Double.NaN;
		
		if( time < Double.POSITIVE_INFINITY )
		{
			rtt = time;
		}
		
		return rtt;
	}
	
	@Override
	public void taskMonitor( ITaskMonitor m ) 
	{
		this.monitor = m;
	}

	@Override
	public List<EventInfo> getResult()
	{
		return this.events;
	}

	@Override
	public void clearResult() 
	{
		synchronized ( this.events )
		{
			this.events.clear();
		}
	}

	@Override
	public String getID() 
	{
		return super.getClass().getName();
	}

	@Override
	public void addSubordinates(MinionParameters pars) throws Exception 
	{	
	}

	@Override
	public void deleteSubordinates(int friendliness) 
	{	
	}

	@Override
	public void toWorkSubordinates(Object paramObject) throws Exception 
	{	
	}

	@Override
	public void setControlSupervisor( IHandlerSupervisor leader ) 
	{
		this.supervisor = leader;
	}

	@Override
	public boolean isWorking() 
	{
		return this.working;
	}

	@Override
	public WarningMessage checkParameters() 
	{
		return new WarningMessage();
	}


}
