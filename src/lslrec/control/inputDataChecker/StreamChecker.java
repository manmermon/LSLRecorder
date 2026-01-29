package lslrec.control.inputDataChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.auxiliar.task.ITaskIdentity;
import lslrec.auxiliar.thread.timer.ActionTimerThread;
import lslrec.auxiliar.thread.timer.IAction;
import lslrec.auxiliar.thread.timer.Timer;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.control.notification.NotificationTask;
import lslrec.dataStream.binary.input.InputDataStreamReceiverTemplate;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.exceptions.SettingException;
import lslrec.stoppableThread.AbstractStoppableThread;

public class StreamChecker extends AbstractStoppableThread implements ITaskIdentity
{
	private Timer timer = null;
	
	private Map< InputDataStreamReceiverTemplate, Long > inputDataWaitingTime = null;
	
	private Map< InputDataStreamReceiverTemplate, Long > timeFromLastData = null;
	private Map< InputDataStreamReceiverTemplate, Long > receivedDataBlock = null;
	
	private boolean running = false;
	
	private Object sync = new Object();
	
	private NotificationTask notifTask = null;
	
	private final long timerTime = 100L;
	private final int reconnectionWarningMaxCounter = 50; // 50 times timerTimes
	
	private Map< InputDataStreamReceiverTemplate, Integer > reconnectionCounter = null;
		
	public StreamChecker() 
	{
		super.setName( super.getClass().getSimpleName() );
		
		this.timer = new Timer( this.timerTime, false, this.getActionTimer() );
		this.timer.setName( this.getClass().getSimpleName() + "-Timer");
		
		this.inputDataWaitingTime = new HashMap< InputDataStreamReceiverTemplate, Long >();
		
		this.reconnectionCounter = new HashMap<InputDataStreamReceiverTemplate, Integer>(); 
		
		this.timeFromLastData = new HashMap< InputDataStreamReceiverTemplate, Long>();
		
		this.receivedDataBlock = new HashMap< InputDataStreamReceiverTemplate, Long>();
	}
	
	private ActionTimerThread getActionTimer()
	{
		final StreamChecker checker = this;
		IAction action = new IAction() 
		{			
			@Override
			public void execute() 
			{
				synchronized( checker )
				{
					checker.notify();
				}
			}
		};
		
		ActionTimerThread act = new ActionTimerThread( action );
		
		return act;
	}
	
	public void setNotificationTask( NotificationTask notif )
	{
		if( super.getState().equals( Thread.State.NEW ) )
		{
			synchronized ( this )
			{
				this.notifTask = notif;
			}			
		}
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
	public synchronized void startThread() throws Exception 
	{
		synchronized ( this )
		{
			if( this.notifTask == null )
			{
				throw new SettingException( "Notification task null." );
			}
			
			super.startThread();
		}
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		synchronized( this.sync )
		{
			super.stopThread = this.inputDataWaitingTime.isEmpty();
			
			if( !super.stopThread && this.timer != null )
			{
				long currentTime = System.currentTimeMillis();
				
				for( InputDataStreamReceiverTemplate str : this.inputDataWaitingTime.keySet() )
				{
					this.timeFromLastData.put( str, currentTime );
					this.receivedDataBlock.put( str, 0L );
					
					this.reconnectionCounter.put( str, 0 );
				}
				
				this.timer.startThread();
			}
			
			this.running = true;
		}
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{	
		synchronized( this )
		{
			this.wait();
		}
		
		long currentTime = System.currentTimeMillis();
		
		List< EventInfo > events = new ArrayList<EventInfo>();
				
		for( InputDataStreamReceiverTemplate str : this.inputDataWaitingTime.keySet() )
		{	
			long numBlocks = str.getNumberReceivedDataBlocks();
			long prevNumBlocks = this.receivedDataBlock.get( str );
			long prevTime = this.timeFromLastData.get( str );
			
			int reconnectionCounter = this.reconnectionCounter.get( str );
			
			Long waitingTime = this.inputDataWaitingTime.get( str );
			
			IStreamSetting iss = str.getStreamSetting();
		
			boolean updateTime = ( prevNumBlocks != numBlocks );
						
			if( !updateTime )
			{
				long time = currentTime - prevTime;
				
				if( time > waitingTime )
				{	
					IStreamSetting[] istr = DataStreamFactory.getStreamSetting( iss.getLibraryID(), "source_id", iss.source_id(), 0.2D );
					
					if(  istr != null 
							&& istr.length > 0  
							//&& iss.uid().equals( istr[0].uid() ) // To avoid a fail by reconnection 
							)
					{
						if( reconnectionCounter > 0 && reconnectionCounter < this.reconnectionWarningMaxCounter )
						{							
							updateTime = true;
						}
						else
						{						
							String evType = EventType.PROBLEM;
							
							if( iss.sampling_rate() == IStreamSetting.IRREGULAR_RATE )
							{
								evType = EventType.WARNING;
								updateTime = true; // To not send another message until more time has passed
							}
							
							// Data stream problem
							String errMsg = "Waiting time for input data from device <" + iss.name() + "> was exceeded.";
							EventInfo ev = new  EventInfo( this.getID(), evType, errMsg );
							
							events.add( ev );
						}
					}
					else if( (iss.reconnectionWaitingTime() > 0) 
								&& (time > waitingTime + iss.reconnectionWaitingTime()*1000 )) // millis
					{
						// Reconnection timeout expired
						
						String errMsg = "Reconnection time for device <" + iss.name() + "> was exceeded.";
						EventInfo ev = new  EventInfo( this.getID(), EventType.PROBLEM, errMsg );
						
						events.add( ev );
					}
					else
					{
						// Waiting reconnection
						
						reconnectionCounter--;
						
						if( reconnectionCounter <= 1 )
						{
							reconnectionCounter = this.reconnectionWarningMaxCounter;
							
							String errMsg = "Stream <" + iss.name() + "> is lost. Waiting to reconnect...";
							EventInfo ev = new  EventInfo( this.getID(), EventType.WARNING, errMsg );
							
							events.add( ev );
						}
						
						this.reconnectionCounter.put( str, reconnectionCounter );
					}
				}
			}
			
			if( updateTime )
			{
				this.timeFromLastData.put( str, currentTime );
				this.receivedDataBlock.put( str, numBlocks );
				
				if( reconnectionCounter > 0 && reconnectionCounter < this.reconnectionWarningMaxCounter )
				{
					String errMsg = "Reconnected stream <" + iss.name() + ">.";
					EventInfo ev = new  EventInfo( this.getID(), EventType.WARNING, errMsg );
					
					events.add( ev );
					
					reconnectionCounter = 0;
				}
				
				this.reconnectionCounter.put( str, reconnectionCounter );
			}
		}
		
		for( EventInfo ev : events )
		{
			this.notifTask.queueEvent( ev );
		}
		
		if( !events.isEmpty() )
		{
			synchronized( this.notifTask )
			{
				this.notifTask.notify();
			}
		}
		
		this.timer.restartTimer();
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		synchronized( this.sync )
		{
			if( this.timer != null )
			{
				this.timer.destroyTimer();
				this.timer = null;
			}		
			
			running = false;
		}
	}
	
	public boolean isRunning()
	{
		synchronized( this.sync )
		{
			return this.running;
		}
	}
	
	public void setInputDataTime( InputDataStreamReceiverTemplate stream, long waitingTime ) throws IllegalStateException
	{
		synchronized( this.sync )
		{
			if( this.running )
			{
				new IllegalStateException( "Stream  checker is running." );
			}
			
			this.inputDataWaitingTime.put( stream, waitingTime );
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			String errorMsg = e.getMessage();
			if( errorMsg == null )
			{
				errorMsg = "";
			}
			
			if ( errorMsg.isEmpty() )
			{
				Throwable t = e.getCause();
				if (t != null)
				{
					errorMsg = errorMsg + t.toString();
				}

				if (errorMsg.isEmpty())
				{
					errorMsg = errorMsg + e.getLocalizedMessage();
				}
			}

			e.printStackTrace();
			
			EventInfo ev = new EventInfo( this.getID(), EventType.PROBLEM, e );
			
			this.notifTask.queueAndSendEvent( ev );
			
			super.runExceptionManager(e);
		}
	}
	
	@Override
	public String getID() 
	{
		return this.getClass().getSimpleName();
	}
	
}
