package Sockets;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotifierThread;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public class TCPSeverSocketTheard extends AbstractStoppableThread implements INotificationTask
{
	private ServerSocket server;
	private InetAddress socketAddress;
	private int serverPort;
	
	private Map< String, Socket > clients;
		
	private NotifierThread notifier;
	
	private ITaskMonitor monitor;
	
	private List< EventInfo > events;
			
	public TCPSeverSocketTheard( ) throws Exception 
	{		
		this.server = new ServerSocket( 0 );
		
		this.setServerSocketSetting();
	}
	
	public TCPSeverSocketTheard( InetSocketAddress address ) throws Exception
	{	
		this.server = new ServerSocket( address.getPort(), 50, address.getAddress() );
		
		this.setServerSocketSetting();
	}
	
	private void setServerSocketSetting() throws Exception
	{	
		this.server.setReuseAddress( true );
		
		this.socketAddress = this.server.getInetAddress();
		this.serverPort = this.server.getLocalPort();
		
		super.setName( "TCP-SERVER>>" + this.socketAddress + ":" + this.serverPort  );		
	}
	
	public int getServerPort()
	{
		return this.serverPort;
	}
		
	public InetAddress getIPAddress()
	{
		return this.socketAddress;
	}
		
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread( int friendliness ) throws Exception 
	{
		if( friendliness == IStoppableThread.FORCE_STOP )
		{
			this.server.close();
		}
	}

	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.clients = new HashMap< String, Socket >();
		this.events = new ArrayList< EventInfo >();		
		
		if( this.monitor == null )
		{
			throw new IllegalStateException( "Task monitor undefined" );
		}
		else
		{
			this.notifier = new NotifierThread( this.monitor, this );
			this.notifier.startThread();
		}
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{	
		Socket client = this.server.accept();
				
		synchronized( this.events )
		{			
			this.events.add( new EventInfo( eventType.SOCKET_CONNECTION_DONE, client ) );
			
			synchronized( this.notifier )
			{
				this.notifier.notify();
			}
		}		
	}
	
	@Override
	protected void runExceptionManager(Exception e) 
	{
		if( !( e instanceof SocketException ) && !super.stopThread )
		{
			super.runExceptionManager( e );
		}
	}
		
	@Override
	protected void cleanUp() throws Exception 
	{
		if( this.clients != null )
		{
			if( this.clients.size() > 0 )
			{
				for( Socket S : this.clients.values() )
				{
					S.close();
				}
			}
			
			if( this.server != null )
			{
				this.server.close();
				this.server = null;
				
				synchronized( this.events )
				{
					this.events.add( new EventInfo( eventType.SOCKET_SERVER_STOP, this.getID() ) );
				}
				
				synchronized ( notifier ) 
				{
					this.notifier.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
				}
			}
		}		
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
		this.monitor = monitor;
	}

	@Override
	public List< EventInfo > getResult() 
	{
		return this.events;
	}

	@Override
	public void clearResult() 
	{		
		this.events.clear();
	}

	@Override
	public String getID() 
	{
		return this.getName();
	}
}
