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
package Sockets;

import java.net.InetSocketAddress;
import java.util.List;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Sockets.Info.SocketSetting;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public class TCP_UDPServer extends AbstractStoppableThread implements INotificationTask
{	
	private TCPSeverSocketTheard tcpServer;
	private UDPSeverSocketThread updServer;
	
	private int stopFriendliness = IStoppableThread.STOP_WITH_TASKDONE;
	
	public TCP_UDPServer( SocketSetting setting ) throws Exception
	{
		InetSocketAddress address = setting.getSocketAddress();
		
		if( setting.getProtocolType() == SocketSetting.UDP_PROTOCOL )
		{
			this.updServer = new UDPSeverSocketThread( address ); 
			super.setName( super.getClass().getCanonicalName() + ":" + this.updServer.getName() );
		}
		else
		{
			this.tcpServer = new TCPSeverSocketTheard( address );
			super.setName( super.getClass().getCanonicalName() + ":" + this.tcpServer.getName() );
		}
	}
		
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
		this.stopFriendliness = friendliness;
		
		if( friendliness == IStoppableThread.STOP_WITH_TASKDONE 
				|| friendliness == IStoppableThread.STOP_IN_NEXT_LOOP )
		{
			synchronized( this )
			{
				this.notify();
			}
		}
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		try
		{
			synchronized( this )
			{
				super.wait();
			}
		}
		catch( InterruptedException ex)
		{
			
		}
	}
	
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		if( this.tcpServer != null )
		{
			this.tcpServer.startThread();
		}
		else
		{
			this.updServer.startThread();
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.tcpServer != null )
		{
			this.tcpServer.stopThread( this.stopFriendliness );
		}
		else
		{
			this.updServer.stopThread( this.stopFriendliness );
		}
	}
	
	@Override
	public void taskMonitor( ITaskMonitor monitor ) 
	{		
		if( this.tcpServer != null )
		{
			this.tcpServer.taskMonitor( monitor );
		}
		else
		{
			this.updServer.taskMonitor( monitor );
		}
	}

	@Override
	public List<EventInfo> getResult() 
	{
		List< EventInfo > evs;
		
		if( this.tcpServer != null )
		{
			evs = this.tcpServer.getResult();
		}
		else
		{
			evs = this.updServer.getResult();
		}
		
		return evs;
	}

	@Override
	public void clearResult() 
	{
		if( this.tcpServer != null )
		{
			this.tcpServer.clearResult();
		}
		else
		{
			this.updServer.clearResult();
		}
	}

	@Override
	public String getID() 
	{
		String id = "";
		
		if( this.tcpServer != null )
		{
			id = this.tcpServer.getID();
		}
		else
		{
			id = this.updServer.getID();
		}
	
		return id;
	}

}
