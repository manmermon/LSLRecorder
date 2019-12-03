package Sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

import Auxiliar.WarningMessage;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotifierThread;
import Controls.IHandlerMinion;
import Controls.IHandlerSupervisor;
import Controls.MinionParameters;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.Sync.SyncMarker;
import Sockets.Info.StreamInputMessage;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

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
	
	private NotifierThread notifier = null;
	
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

		if( this.monitor != null )
		{
			this.notifier = new NotifierThread( this.monitor , this );
			this.notifier.setName( this.notifier.getClass().getSimpleName() + "-" + this.getClass().getSimpleName() );
			
			this.notifier.startThread();
		}
		
		this.working = true;		
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		if( this.socketMsg != null )
		{
			double time = this.socketMsg.receivedTime();
			
			double rtt = this.pingDuration( this.socketMsg.getOrigin(), this.numberOfPings );
			
			if( rtt != Double.NaN 
					&& rtt != Double.POSITIVE_INFINITY
					&& rtt != Double.NEGATIVE_INFINITY )
			{
				time = time - rtt/ 2;
			}
			
			if( this.notifier != null )
			{
				EventInfo ev = new EventInfo( EventType.INPUT_MARK_READY, new SyncMarker( this.Mark, time ) );

				synchronized ( this.events )
				{
					this.events.add( ev );
				}				
				
				synchronized ( this.notifier ) 
				{
					this.notifier.notify();
				}
				
			}
		}
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		this.stopThread = true;
		
		super.targetDone();
	}
	
	@Override
	protected void runExceptionManager(Exception e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			e.printStackTrace();
						
			if( this.notifier != null )
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
				
				this.notifier.notify();
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
		
		if( this.notifier != null )
		{
			this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			this.notifier = null;
		}
	}
	
	
	
	private double pingDuration( InetSocketAddress ipAddress, int numPings ) throws IOException
	{		
		IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
		request.setHost( ipAddress.getHostString() );
		
		double time = Double.POSITIVE_INFINITY;
		
		/*
		for( int i = 0; i < numPings; i++ )
		{
			long t = System.nanoTime();
			
			final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
			
			if ( geek.isReachable( 5000 ) )
			{
				t = ( System.nanoTime() - t);
				
				if( ( t / 1e9D )  < time )
				{
					time = t / 1e9D;
				}
			}
		}	
		*/	
		
		for( int i = 0; i < numPings; i++ )
		{			
			final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
			
			if ( response.getSuccessFlag() )
			{
				double t = response.getDuration() / 1e9D;
				
				if( t < time )
				{
					time = t;
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
