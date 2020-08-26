/*
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package sockets;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import auxiliar.tasks.BridgeNotifierThread;
import auxiliar.tasks.INotificationTask;
import auxiliar.tasks.ITaskMonitor;
import controls.messages.EventInfo;
import controls.messages.EventType;
import sockets.info.StreamInputMessage;
import stoppableThread.AbstractStoppableThread;
import stoppableThread.IStoppableThread;

public class UDPSeverSocketThread extends AbstractStoppableThread implements INotificationTask
{
	private DatagramSocket server;
			
	private BridgeNotifierThread notifier;
	
	private ITaskMonitor monitor;
	
	private ConcurrentLinkedQueue< EventInfo > events;
	
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
		
		this.events = new ConcurrentLinkedQueue< EventInfo >();		
		
		this.receivedPacket = new DatagramPacket( new byte[ this.bufferSize ], this.bufferSize );
		
		if( this.monitor == null )
		{
			throw new IllegalStateException( "Task monitor undefined" );
		}
		else
		{
			this.notifier = new BridgeNotifierThread( this.monitor, this );
			this.notifier.startThread();
		}		
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{		
		this.server.receive( this.receivedPacket );
	
		String msg = new String( this.receivedPacket.getData(), 0, this.receivedPacket.getLength() ).trim();
		
		
		StreamInputMessage inMsg  = new StreamInputMessage( msg , new InetSocketAddress( this.receivedPacket.getAddress(), this.receivedPacket.getPort() )
																, this.serverInetSokAddress );
			
		this.events.add( new EventInfo( this.getID(), EventType.SOCKET_INPUT_MSG, inMsg ) );
			
		synchronized( this.notifier )
		{
			this.notifier.notify();
		}		
	}
	
	@Override
	protected void runExceptionManager( Throwable e) 
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
		
		this.events.add( new EventInfo( this.getID(), EventType.SOCKET_SERVER_STOP, this.getID() ) );
		
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
	public List< EventInfo > getResult( boolean clear ) 
	{
		List< EventInfo > evs = new ArrayList< EventInfo >();
		
		evs.addAll( this.events );
			
		if( clear )
		{
			this.events.clear();
		}
		
		return evs;
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
