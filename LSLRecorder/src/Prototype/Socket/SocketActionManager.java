package Prototype.Socket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Sockets.SocketReaderThread;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public class SocketActionManager extends AbstractStoppableThread implements ITaskMonitor, INotificationTask, Cloneable
{	
	private Socket socket;
	private ISocketAction socketAction;
	private String ID;
	
	private List< EventInfo > events;
	private List< EventInfo > stateEvent;
	
	private SocketReaderThread inputReader;
	private SocketWriterThread outputWriter;
	
	private ITaskMonitor monitor;
	
	private Semaphore semEvents;
	
	public SocketActionManager( Socket socket, ISocketAction action ) throws IllegalArgumentException, IOException  
	{		
		if( action == null )
		{
			throw new IllegalArgumentException( "Socket Action null" ); 
		}
		
		this.inputReader = new SocketReaderThread( socket, this );
		this.outputWriter = new SocketWriterThread( socket, this );
		
		this.socketAction = action;
		this.socket = socket;
		
		this.ID = this.socket.getLocalAddress().getHostAddress() + ":" + this.socket.getLocalPort()
					+ "<->" + this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
		
		super.setName( this.ID );
				
		this.events = new ArrayList< EventInfo >();
		this.stateEvent = new ArrayList< EventInfo >();
		
		this.semEvents = new Semaphore( 1, true );
	}

	@Override
	protected void preStopThread( int friendliness ) throws Exception 
	{	
	}

	@Override
	protected void postStopThread( int friendliness ) throws Exception 
	{		
	}
	
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.outputWriter.startThread();
		this.inputReader.startThread();
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized ( this )
		{	
			try
			{
				super.wait();
			}
			catch( InterruptedException e)
			{}
		}		
		
		try 
		{
			this.semEvents.acquire();
		} 
		catch (InterruptedException e) 
		{
		}
		
		for( EventInfo ev : this.events )
		{
			if( ev.getEventType().equals( eventType.SOCKET_INPUT_MSG ) )
			{
				String response = this.socketAction.prepareResponse( (String)ev.getEventInformation() );
				if( response != null )
				{
					this.outputWriter.sendMessage( response );
				}					
			}
			else if( ev.getEventType().equals( eventType.SOCKET_OUTPUT_MSG_OK ) )
			{

			}
			else if( ev.getEventType().equals( eventType.SOCKET_OUTPUT_MSG_SEND ) )
			{
				String outMsg = this.socketAction.prepareSending( (String)ev.getEventInformation() );
				if( outMsg != null )
				{
					this.outputWriter.sendMessage( outMsg );
				}
			}
			else if( ev.getEventType().equals( eventType.SOCKET_CHANNEL_CLOSE )
					|| ev.getEventType().equals( eventType.SOCKET_CONNECTION_PROBLEM ))
			{						
				this.stateEvent.add( new EventInfo( ev.getEventType(),  ev.getEventInformation() ) );

				this.inputReader.stopThread( IStoppableThread.FORCE_STOP );
				this.outputWriter.stopThread( IStoppableThread.FORCE_STOP );

				super.stopThread( IStoppableThread.FORCE_STOP );					
				break;
			}
		}
					
		this.events.clear();
		
		this.semEvents.release();
	}

	public synchronized void sendMessage( String msg ) 
	{
		try 
		{
			this.semEvents.acquire();
		} 
		catch (InterruptedException e) 
		{
		}
		//System.out.println("SocketActionManager.sendMessage() " + msg);
		this.events.add( new EventInfo( eventType.SOCKET_OUTPUT_MSG_SEND, msg ) );
				
		synchronized( this )
		{		
			this.notify();
		}
		
		this.semEvents.release();
	}
		
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		this.events.clear();
		
		this.inputReader.stopThread( IStoppableThread.FORCE_STOP );
		this.outputWriter.stopThread( IStoppableThread.FORCE_STOP );
		
		//System.out.println( "SocketActionManager.cleanUp()" );
		
		if( this.monitor != null )
		{
			synchronized( this.stateEvent )
			{			
				this.stateEvent.add( new EventInfo( eventType.THREAD_STOP, this.getID() ) );
				
				if( this.monitor != null )
				{
					try 
					{
						this.monitor.taskDone( this );
					}
					catch (Exception e) 
					{
						this.runExceptionManager( e );
					}
				}
			}
		}
		
		this.inputReader = null;
		this.outputWriter = null;
	}
	
	/**
	 * 
	 * @param task
	 * @throws Exception
	 */
	@Override
	public synchronized void taskDone( INotificationTask task ) throws Exception 
	{	
		List< EventInfo > events = task.getResult();
		
		synchronized ( events ) 
		{
			synchronized( this.events )
			{
				for( EventInfo ev : events )
				{
					this.events.add( new EventInfo( ev.getEventType(), ev.getEventInformation() ) );
				}

				super.notify();
			}
		}
		
		task.clearResult();
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
		this.monitor = monitor;
	}

	@Override
	public List<EventInfo> getResult() 
	{
		return this.stateEvent;
	}

	@Override
	public void clearResult() 
	{
		this.stateEvent.clear();
	}
	
	@Override
	public String getID() 
	{
		return this.ID;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException 
	{
		return super.clone();
	}
}
