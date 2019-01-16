/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package Prototype.Discarded.Malfunction.Socket;

import java.net.BindException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
//import java.util.concurrent.Semaphore;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotifierThread;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Prototype.Discarded.Malfunction.Socket.Manager.ManagerInOutStreamSocket;
import Sockets.Info.SocketSetting;
import Sockets.Info.SocketParameters;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public class streamServerSocket extends AbstractStoppableThread implements INotificationTask 
{
	private SocketParameters socketParameters = null;
	
	private ITaskMonitor monitor = null;
	private NotifierThread monitorThread = null;
	
	private ServerSocketChannel streamTCPServer = null;
	private DatagramChannel streamUDPServer = null;
	
	private List< EventInfo > events = null;
	
	private boolean isBlockedWaiting = true;
	
	//private Semaphore semEvents = null;
	
 	public streamServerSocket( SocketParameters pars ) throws Exception 
	{
		if( pars == null )
		{
			throw new IllegalArgumentException( "Socket parameter list null or empty." );
		}
		
		this.socketParameters = pars;
		
		super.setName( "ServerSocket");
				
		this.events = new ArrayList< EventInfo >();
		
		//this.semEvents = new Semaphore( 1, true );
	}
		
	@Override
	public void taskMonitor( ITaskMonitor m ) 
	{
		if( this.monitorThread != null )
		{
			throw new IllegalThreadStateException( "Thread is working." );
		}
		
		this.monitor = m;
	}

	@Override
	public List<EventInfo> getResult() 
	{
		synchronized ( this.events ) 
		{
			return this.events;
		}
	}

	@Override
	public void clearResult() 
	{
		synchronized ( this.events )
		{
			this.events.clear();
			
			if( this.getState().equals( State.TERMINATED ) )
			{
				this.monitorThread.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
			}
		}		
	}

	@Override
	protected void preStart() throws Exception 
	{
		this.setName( this.socketParameters.getSocketInfo().toString() );
		
		this.monitorThread = new NotifierThread( this.monitor, this );
		this.monitorThread.startThread();
		
		if( this.socketParameters.getSocketInfo().getProtocolType() == SocketSetting.TCP_PROTOCOL )
		{		
			try 
			{
				this.streamTCPServer = ServerSocketChannel.open();
				this.streamTCPServer.configureBlocking( true );
				//this.streamTCPServer.configureBlocking( false );
				this.streamTCPServer.socket().setReuseAddress( true );
				
				this.streamTCPServer.socket().bind( this.socketParameters.getSocketInfo().getSocketAddress() );								
			} 
			catch( BindException e )
			{
				if( this.streamTCPServer != null )
				{
					this.streamTCPServer.close();
					this.streamTCPServer = null;
				}
				
				throw new BindException( e.getMessage() + "\n"+ this.socketParameters.getSocketInfo().toString() + " is in used." );
			}
		}
		else if( this.socketParameters.getSocketInfo().getProtocolType() == SocketSetting.UDP_PROTOCOL )
		{
			this.streamUDPServer = DatagramChannel.open();
			//this.streamUDPServer.bind( this.socketParameters.getSocketInfo().getSocketAddress() );
			this.streamUDPServer.socket().bind( this.socketParameters.getSocketInfo().getSocketAddress() );
			
			this.streamUDPServer.socket().setSoTimeout( 0 );
			this.streamUDPServer.socket().setReuseAddress( true );	
			this.streamUDPServer.configureBlocking( true );
		}
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{		
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
		if( (friendliness == IStoppableThread.STOP_WITH_TASKDONE 
				|| friendliness == IStoppableThread.STOP_IN_NEXT_LOOP )
				&& this.isBlockedWaiting )
		{
			try
			{
				super.interrupt();
			}
			catch( SecurityException e )
			{
				
			}
		}
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		if( this.streamTCPServer != null )
		{	
			this.isBlockedWaiting = true;
			
			final SocketChannel socketChannel = this.streamTCPServer.accept();
			
			this.isBlockedWaiting = false;
			
			if( socketChannel != null )
			{
				// start communication in new thread
				socketChannel.configureBlocking( true );
				
				ManagerInOutStreamSocket ioss = new ManagerInOutStreamSocket( socketChannel, this.socketParameters.getDirectionSocketConnection() );
				
				synchronized( this.events )
				{					
					this.events.add( new EventInfo( eventType.SOCKET_INOUT_CHANNEL_CREATED, ioss ) );
				}
				
				this.notifyEvent();
			}
		}
		else
		{
			ManagerInOutStreamSocket ioss = new ManagerInOutStreamSocket( this.streamUDPServer, this.socketParameters.getDirectionSocketConnection() );
			
			synchronized ( this.events )
			{
				this.events.add( new EventInfo( eventType.SOCKET_INOUT_CHANNEL_CREATED, ioss ) );
			}
			
			this.notifyEvent();
			
			this.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		if( this.streamTCPServer != null )
		{
			this.streamTCPServer.close();
			this.streamTCPServer = null;
		}
		
		if( this.streamUDPServer != null )
		{
			this.streamUDPServer.close();
			this.streamUDPServer = null;
		}
		
		this.isBlockedWaiting = false;
	}
	
	@Override
	protected void runExceptionManager(Exception e) 
	{
		this.stopThread = true;
		
		synchronized ( this.events ) 
		{
			this.events.add( new EventInfo( eventType.SERVER_THREAD_STOP, e.getMessage() ) );
		}
		
		this.notifyEvent();
	}	
	
	private void notifyEvent()
	{
		synchronized ( this.monitorThread ) 
		{
			if( this.events.size() > 0 )
			{
				this.monitorThread.notify();
			}		
		}
	}
	
	public SocketParameters getSocketParameters()
	{
		return this.socketParameters;
	}

	@Override
	public String getID() 
	{
		return super.getName();
	}
}
