package Sockets;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotifierThread;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Sockets.Info.streamInputMessage;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public class UDPSeverSocketThread extends AbstractStoppableThread implements INotificationTask
{
	private DatagramSocket server;
			
	private NotifierThread notifier;
	
	private ITaskMonitor monitor;
	
	private List< EventInfo > events;
	
	private int bufferSize = 65507;
	private DatagramPacket receivedPacket;
	
	private InetSocketAddress serverInetSokAddress;
	
	public UDPSeverSocketThread( ) throws Exception 
	{		
		this.server = new DatagramSocket( );
		
		this.setServerSocketSetting();
	}
	
	public UDPSeverSocketThread( InetSocketAddress address ) throws Exception
	{	
		this.server = new DatagramSocket( address.getPort(), address.getAddress() );
		
		this.setServerSocketSetting();
	}
	
	private void setServerSocketSetting() throws Exception
	{	
		this.serverInetSokAddress = new InetSocketAddress( this.server.getLocalAddress(), this.server.getLocalPort() );		
		
		this.server.setReuseAddress( true );
				
		super.setName( "UDP-SERVER>>" + this.serverInetSokAddress.getAddress() + ":" + this.serverInetSokAddress.getPort() );
	}
	
	public int getServerPort()
	{
		return this.serverInetSokAddress.getPort();
	}
		
	public InetAddress getIPAddress()
	{
		return this.serverInetSokAddress.getAddress();
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
		
		this.events = new ArrayList< EventInfo >();		
		
		this.receivedPacket = new DatagramPacket( new byte[ this.bufferSize ], this.bufferSize );
		
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
		this.server.receive( this.receivedPacket );
	
		String msg = new String( this.receivedPacket.getData(), 0, this.receivedPacket.getLength() ).trim();
		
		synchronized( this.events )
		{
			streamInputMessage inMsg  = new streamInputMessage( msg , new InetSocketAddress( this.receivedPacket.getAddress(), this.receivedPacket.getPort() )
																, this.serverInetSokAddress );
			
			this.events.add( new EventInfo( eventType.SOCKET_INPUT_MSG, inMsg ) );
			
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
			super.runExceptionManager(e);
		}
	}
	
	@Override
	protected void finallyManager() 
	{
		super.finallyManager();
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{		
		this.receivedPacket = null;
		
		this.server.close();		
		
		synchronized ( this.events ) 
		{
			this.events.add( new EventInfo( eventType.SOCKET_SERVER_STOP, this.getID() ) );
		}
		
		synchronized( this.notifier )
		{	
			this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
		}
		
		this.notifier = null;
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
