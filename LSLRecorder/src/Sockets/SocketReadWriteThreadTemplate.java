package Sockets;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotifierThread;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Sockets.Info.streamSocketProblem;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public abstract class SocketReadWriteThreadTemplate extends AbstractStoppableThread implements INotificationTask 
{
	protected Socket SOCKET;
		
	protected NotifierThread notifier;
	
	protected ITaskMonitor monitor;

	protected List< EventInfo > events = null;
	
	protected int numEvents = 0;
	
	public SocketReadWriteThreadTemplate( Socket socket, ITaskMonitor monitor ) throws IllegalArgumentException, IOException
	{
		if( socket == null )
		{
			throw new IllegalArgumentException( "Socket null" );
		}
		
		this.SOCKET = socket;
		
		this.taskMonitor( monitor );
		
		super.setName( this.SOCKET.getLocalAddress() + ":" + this.SOCKET.getLocalPort() + "<->" + this.SOCKET.getInetAddress() + ":" + this.SOCKET.getPort() );
	}
	
	public int getLocatPort()
	{
		return this.SOCKET.getLocalPort();
	}
	
	public InetAddress getLocalAddress()
	{
		return this.SOCKET.getInetAddress();
	}
	
	public Socket getSocket()
	{
		return this.SOCKET;
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
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.notifier = new NotifierThread( this.monitor, this );
		this.notifier.setName( this.notifier.getClass().getSimpleName() + "-" + super.getName() );
		this.notifier.startThread();
		
		this.events = new ArrayList< EventInfo >();
	}
	
	public void closeNofitier()
	{
		if( this.notifier != null )
		{
			this.notifier.stopThread( IStoppableThread.FORCE_STOP );
			this.notifier = null;
		}
	}
	
	@Override
	protected void runExceptionManager(Exception e) 
	{
		boolean report = ( !this.stopThread ) 
						 	&& !( e instanceof InterruptedException ) 
							&& !( e instanceof ClosedByInterruptException )
							&& !( e instanceof SocketException );
		
		this.stopThread = true;	

		if( report )
		{			
			e.printStackTrace();
			synchronized ( this.events )
			{	
				streamSocketProblem problem = new streamSocketProblem( new InetSocketAddress( this.SOCKET.getInetAddress()
																								, this.SOCKET.getPort() )
																		, e );

				this.events.add( new EventInfo( eventType.SOCKET_CONNECTION_PROBLEM, problem ) );
				
				synchronized( this.notifier )
				{
					this.notifier.notify();
				}
			}
		}
	}
	
	/*
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		synchronized ( this.events )
		{
			this.events.add( new EventInfo( eventType.THREAD_STOP, null ) );
		}
				
		synchronized ( this.notifier )
		{	
			if( this.events.size() > 0 )
			{
				this.notifier.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
				this.notifier.notify();		
			}
		}
		
		synchronized ( this.SOCKET ) 
		{
			this.SOCKET.close();
			this.SOCKET = null;
		}
		
		this.monitor = null;
	}
	*/

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#taskMonitor(Auxiliar.Tasks.ITaskMonitor)
	 */
	@Override
	public void taskMonitor( ITaskMonitor monitor ) 
	{		
		if( this.notifier != null )
		{
			throw new IllegalThreadStateException( "Thread is working." );
		}
			
		this.monitor = monitor;
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#getResult()
	 */
	@Override
	public List< EventInfo > getResult()
	{
		synchronized ( this.events ) 
		{	
			this.numEvents = this.events.size();
			List< EventInfo > evs = new ArrayList< EventInfo >();
			for( EventInfo ev : this.events )
			{
				evs.add( new EventInfo( ev.getEventType(), ev.getEventInformation() ) );
			}
			return evs;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#clearResult()
	 */
	@Override
	public void clearResult() 
	{
		synchronized ( this.events )
		{
			while( this.numEvents > 0 )
			{
				this.events.remove( 0 );
				this.numEvents--;
			}
		}
	}
}
